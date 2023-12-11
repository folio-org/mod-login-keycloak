package org.folio.login.service;

import static java.util.Collections.emptyList;
import static org.folio.login.support.TestConstants.ACCESS_TOKEN;
import static org.folio.login.support.TestConstants.KEYCLOAK_USER_ID;
import static org.folio.login.support.TestConstants.TENANT;
import static org.folio.login.support.TestConstants.USERNAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.folio.login.domain.model.KeycloakUser;
import org.folio.login.integration.KeycloakUserClient;
import org.folio.spring.FolioExecutionContext;
import org.folio.spring.exception.NotFoundException;
import org.folio.test.types.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class KeycloakUserServiceTest {

  @Mock private KeycloakUserClient client;
  @Mock private FolioExecutionContext context;

  @InjectMocks private KeycloakUserService keycloakUserService;

  @Test
  void findKeycloakUserIdByUserId_positive() {
    when(context.getTenantId()).thenReturn(TENANT);
    when(client.findUsersWithAttrs(anyString(), eq(TENANT), anyString(), eq(true))).thenReturn(
      List.of(createKeycloakResponse()));

    var keycloakUserId = keycloakUserService.findKeycloakUserIdByUserId("userId", "anyToken");

    assertEquals(KEYCLOAK_USER_ID, keycloakUserId);
    verify(client).findUsersWithAttrs(anyString(), eq(TENANT), anyString(), eq(true));
  }

  @Test
  void findKeycloakUserIdByUserId_negative_returnsMoreThanOneUsers() {
    var userId = "userId";
    when(context.getTenantId()).thenReturn(TENANT);
    when(client.findUsersWithAttrs(anyString(), eq(TENANT), anyString(), eq(true))).thenReturn(
      List.of(createKeycloakResponse(), createKeycloakResponse()));

    assertThrows(IllegalStateException.class, () -> keycloakUserService.findKeycloakUserIdByUserId("userId",
      "anyToken"), "Too many keycloak users with 'user_id' attribute: " + userId);
  }

  @Test
  void findKeycloakUserIdByUserId_negative_returnEmptyResponse() {
    var userId = "userId";
    when(context.getTenantId()).thenReturn(TENANT);
    when(client.findUsersWithAttrs(anyString(), eq(TENANT), anyString(), eq(true))).thenReturn(
      List.of());

    assertThrows(NotFoundException.class, () -> keycloakUserService.findKeycloakUserIdByUserId("userId",
      "anyToken"), "Keycloak user doesn't exist with the given 'user_id' attribute: " + userId);
  }

  @Test
  void findKeycloakUserByUsername_positive() {
    when(context.getTenantId()).thenReturn(TENANT);
    when(client.findUsers(ACCESS_TOKEN, TENANT, USERNAME)).thenReturn(List.of(createKeycloakResponse()));

    var keycloakUser = keycloakUserService.findKeycloakUserByUsername(USERNAME, ACCESS_TOKEN);

    assertEquals(KEYCLOAK_USER_ID, keycloakUser.getId());
    verify(client).findUsers(ACCESS_TOKEN, TENANT, USERNAME);
  }

  @Test
  void findKeycloakUserByUsername_positive_returnsMoreThanOneUsers() {
    when(context.getTenantId()).thenReturn(TENANT);
    when(client.findUsers(ACCESS_TOKEN, TENANT, USERNAME)).thenReturn(
      List.of(createKeycloakResponse(), createKeycloakResponse()));

    var keycloakUser = keycloakUserService.findKeycloakUserByUsername(USERNAME, ACCESS_TOKEN);

    assertEquals(KEYCLOAK_USER_ID, keycloakUser.getId());
    verify(client).findUsers(ACCESS_TOKEN, TENANT, USERNAME);
  }

  @Test
  void findKeycloakUserByUsername_negative_returnEmptyResponse() {
    when(context.getTenantId()).thenReturn(TENANT);
    when(client.findUsers(ACCESS_TOKEN, TENANT, USERNAME)).thenReturn(emptyList());

    assertThrows(NotFoundException.class, () -> keycloakUserService.findKeycloakUserByUsername(USERNAME, ACCESS_TOKEN),
      "Keycloak user doesn't exist with the given 'username': " + USERNAME);
  }

  private static KeycloakUser createKeycloakResponse() {
    var keycloakUser = new KeycloakUser();
    keycloakUser.setId(KEYCLOAK_USER_ID);
    keycloakUser.setUserName(USERNAME);
    return keycloakUser;
  }
}
