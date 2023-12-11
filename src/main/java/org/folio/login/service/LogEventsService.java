package org.folio.login.service;

import static org.folio.login.domain.dto.LogEventType.FAILED_LOGIN_ATTEMPT;
import static org.folio.login.domain.dto.LogEventType.PASSWORD_CHANGE;
import static org.folio.login.domain.dto.LogEventType.PASSWORD_RESET;
import static org.folio.login.domain.dto.LogEventType.SUCCESSFUL_LOGIN_ATTEMPT;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.MapUtils;
import org.codehaus.plexus.util.StringUtils;
import org.folio.login.domain.dto.LogEvent;
import org.folio.login.domain.dto.LogEventCollection;
import org.folio.login.domain.dto.LogEventType;
import org.folio.spring.FolioExecutionContext;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.AdminEventRepresentation;
import org.keycloak.representations.idm.EventRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Log4j2
public class LogEventsService {
  private static final Map<String, LogEventType> SUPPORTED_USER_EVENT_MAPPINGS = Map.of(
    "LOGIN", SUCCESSFUL_LOGIN_ATTEMPT,
    "LOGIN_ERROR", FAILED_LOGIN_ATTEMPT,
    "UPDATE_PASSWORD", PASSWORD_CHANGE,
    "RESET_PASSWORD", PASSWORD_RESET
  );
  private static final ArrayList<String> SUPPORTED_USER_EVENTS =
    new ArrayList<>(SUPPORTED_USER_EVENT_MAPPINGS.keySet());
  private static final String USER_ID_ATTR = "user_id";
  private static final String USER_RESOURCE_TYPE = "USER";
  private static final String ADMIN_EVENT_OPERATION_TYPE = "ACTION";
  private static final Pattern UUID_PATTERN =
    Pattern.compile("\\p{XDigit}{8}-\\p{XDigit}{4}-\\p{XDigit}{4}-\\p{XDigit}{4}-\\p{XDigit}{12}");
  private static final int BATCH_SIZE = 100;

  private final Keycloak keycloak;
  private final FolioExecutionContext folioExecutionContext;

  @SuppressWarnings("unused")
  public LogEventCollection getUserEvents(Integer start, Integer length, String query) {
    var tenant = folioExecutionContext.getTenantId();
    var realmResource = keycloak.realm(tenant);

    // load, merge and sort data from two sources before applying offset and limit
    var maxRecords = start + length;
    var userEvents = loadPaginatedData(offset -> loadUserEvents(realmResource, offset, BATCH_SIZE), maxRecords);
    var adminEvents = loadPaginatedData(offset -> loadAdminEvents(realmResource, offset, BATCH_SIZE), maxRecords);

    // currently there is no API for bulk search of users by ids in Keycloak, have to load all of them
    var usersTotal = realmResource.users().count();
    var users = loadPaginatedData(offset -> realmResource.users().list(offset, BATCH_SIZE), usersTotal);
    var usersMap = users.stream().collect(Collectors.toMap(UserRepresentation::getId, Function.identity()));

    var userLogEvents = mapSupportedUserEvents(userEvents, tenant, usersMap);
    var adminLogEvents = mapSupportedAdminEvents(adminEvents, tenant, usersMap);
    var mergedEvents = mergeAndSortWithOffset(userLogEvents, adminLogEvents, start, length);

    return new LogEventCollection()
      .loggingEvent(mergedEvents)
      .totalRecords(mergedEvents.size());
  }

  private static List<LogEvent> mergeAndSortWithOffset(List<LogEvent> list1, List<LogEvent> list2, Integer startIndex,
    Integer length) {
    var toIndex = startIndex + length;
    var mergedList = Stream.concat(list1.stream(), list2.stream())
      .sorted(Comparator.comparing(LogEvent::getTimestamp, Comparator.reverseOrder()))
      .limit(toIndex)
      .collect(Collectors.toList());
    return startIndex >= mergedList.size() ? Collections.emptyList() :
      mergedList.subList(startIndex, Math.min(toIndex, mergedList.size()));
  }

  private static List<EventRepresentation> loadUserEvents(RealmResource realm, int offset, int maxResults) {
    return realm.getEvents(SUPPORTED_USER_EVENTS, null, null, null, null, null, offset, maxResults);
  }

  private static List<AdminEventRepresentation> loadAdminEvents(RealmResource realm, int offset, int maxResults) {
    return realm.getAdminEvents(
      List.of(ADMIN_EVENT_OPERATION_TYPE), null, null, null, null, null, null, null, offset, maxResults);
  }

  private static <T> List<T> loadPaginatedData(IntFunction<List<T>> offsetLoadFunction, Integer limit) {
    int currBatchSize;
    var offset = 0;
    var allRecords = new ArrayList<T>();

    do {
      var result = offsetLoadFunction.apply(offset);
      currBatchSize = result.size();
      offset += currBatchSize;
      allRecords.addAll(result);
    } while (currBatchSize > 0 && allRecords.size() < limit);

    return allRecords;
  }

  private static List<LogEvent> mapSupportedUserEvents(List<EventRepresentation> userEvents, String tenant,
    Map<String, UserRepresentation> usersMap) {
    var logEvents = new ArrayList<LogEvent>();
    for (var userEvent : userEvents) {
      var eventType = resolveUserEventType(userEvent.getType());
      var eventUserId = userEvent.getUserId();
      if (eventType != null && isFolioUser(eventUserId, usersMap)) {
        var event = new LogEvent()
          .eventType(eventType)
          .userId(getFolioUserId(eventUserId, usersMap))
          .ip(userEvent.getIpAddress())
          .tenant(tenant)
          .timestamp(new Date(userEvent.getTime()));
        logEvents.add(event);
      }
    }
    return logEvents;
  }

  private static List<LogEvent> mapSupportedAdminEvents(List<AdminEventRepresentation> adminEvents, String tenant,
    Map<String, UserRepresentation> usersMap) {
    var logEvents = new ArrayList<LogEvent>();
    for (var adminEvent : adminEvents) {
      var eventType = resolveAdminEventType(adminEvent);
      var authDetails = adminEvent.getAuthDetails();
      if (eventType != null) {
        var resourcePath = adminEvent.getResourcePath();
        extractFirstUuid(resourcePath).ifPresent(userId -> {
          if (isFolioUser(userId, usersMap)) {
            var event = new LogEvent()
              .eventType(eventType)
              .userId(getFolioUserId(userId, usersMap))
              .ip(authDetails.getIpAddress())
              .tenant(tenant)
              .timestamp(new Date(adminEvent.getTime()));
            logEvents.add(event);
          }
        });
      }
    }
    return logEvents;
  }

  private static boolean isFolioUser(String userId, Map<String, UserRepresentation> usersMap) {
    var user = usersMap.get(userId);
    return user != null && MapUtils.emptyIfNull(user.getAttributes()).containsKey(USER_ID_ATTR);
  }

  private static String getFolioUserId(String userId, Map<String, UserRepresentation> usersMap) {
    var user = usersMap.get(userId);
    return user.getAttributes().get(USER_ID_ATTR).get(0);
  }

  private static Optional<String> extractFirstUuid(String resourcePath) {
    var matcher = UUID_PATTERN.matcher(resourcePath);
    return matcher.find() ? Optional.of(matcher.group(0)) : Optional.empty();
  }

  private static LogEventType resolveAdminEventType(AdminEventRepresentation adminEvent) {
    var resourceType = adminEvent.getResourceType();
    var resourcePath = adminEvent.getResourcePath();
    if (USER_RESOURCE_TYPE.equals(resourceType) && StringUtils.contains(resourcePath, "/reset-password")) {
      return PASSWORD_RESET;
    }
    return null;
  }

  private static LogEventType resolveUserEventType(String eventType) {
    return SUPPORTED_USER_EVENT_MAPPINGS.get(eventType);
  }
}
