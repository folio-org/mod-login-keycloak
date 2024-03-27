package org.folio.login.service;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.login.service.AdminTokenServiceTest.TestCacheConfiguration;
import static org.folio.login.support.TestConstants.ACCESS_TOKEN;
import static org.folio.login.support.TestConstants.CLIENT_ID;
import static org.folio.login.support.TestConstants.PASSWORD;
import static org.folio.login.support.TestConstants.REALM;
import static org.folio.login.support.TestConstants.USERNAME;
import static org.folio.login.support.TestValues.keycloakAuthentication;
import static org.folio.login.support.TestValues.loginRequest;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;

import java.util.Optional;
import org.folio.login.integration.keycloak.KeycloakClient;
import org.folio.login.integration.keycloak.config.KeycloakAdminProperties;
import org.folio.login.integration.keycloak.config.KeycloakProperties;
import org.folio.test.types.UnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;

@UnitTest
@SpringBootTest(classes = {AdminTokenService.class, TestCacheConfiguration.class}, webEnvironment = NONE)
class AdminTokenServiceTest {

  private static final String TOKEN_CACHE = "token";

  @Autowired private AdminTokenService adminTokenService;
  @Autowired private CacheManager cacheManager;
  @MockBean private KeycloakProperties keycloakProperties;
  @MockBean private KeycloakAdminProperties adminProperties;
  @MockBean private KeycloakClient keycloakClient;

  @BeforeEach
  void setUp() {
    cleanUpCaches(cacheManager);
  }

  @Test
  void issueAdminToken_positive() {
    var keycloakAuth = keycloakAuthentication();

    when(keycloakProperties.getAdmin()).thenReturn(adminProperties);
    when(adminProperties.getClientId()).thenReturn(CLIENT_ID);
    when(adminProperties.getPassword()).thenReturn(PASSWORD);
    when(adminProperties.getUsername()).thenReturn(USERNAME);
    when(adminProperties.getRealm()).thenReturn(REALM);

    var requestData = loginRequest(adminProperties.getUsername(),
      adminProperties.getPassword(),
      adminProperties.getClientId());

    when(keycloakClient.callTokenEndpoint(REALM, requestData, null, null))
      .thenReturn(keycloakAuth);

    var issuedToken = adminTokenService.getAdminToken(null, null);

    assertThat(issuedToken).isEqualTo("Bearer " + ACCESS_TOKEN);
    assertThat(getCachedValue()).isEqualTo(Optional.of("Bearer " + ACCESS_TOKEN));
  }

  private Optional<Object> getCachedValue() {
    return Optional.ofNullable(cacheManager.getCache(TOKEN_CACHE)).map(cache -> cache.get("admin-cli-token"))
      .map(Cache.ValueWrapper::get);
  }

  public static void cleanUpCaches(CacheManager cacheManager) {
    cacheManager.getCacheNames().forEach(name -> requireNonNull(cacheManager.getCache(name)).clear());
  }

  @EnableCaching
  @TestConfiguration
  static class TestCacheConfiguration {

    @Bean
    CacheManager cacheManager() {
      return new ConcurrentMapCacheManager(TOKEN_CACHE);
    }
  }
}
