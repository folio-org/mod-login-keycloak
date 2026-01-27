package org.folio.login.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.login.support.TestConstants.ACCESS_TOKEN;
import static org.folio.login.support.TestConstants.CLIENT_ID;
import static org.folio.login.support.TestConstants.PASSWORD;
import static org.folio.login.support.TestConstants.REALM;
import static org.folio.login.support.TestConstants.USERNAME;
import static org.folio.login.support.TestValues.keycloakAuthentication;
import static org.folio.login.support.TestValues.loginRequest;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;

import com.github.benmanes.caffeine.cache.Cache;
import org.folio.login.configuration.TokenCacheConfiguration;
import org.folio.login.configuration.property.TokenCacheProperties;
import org.folio.login.domain.model.KeycloakAuthentication;
import org.folio.login.integration.keycloak.KeycloakClient;
import org.folio.login.integration.keycloak.config.KeycloakAdminProperties;
import org.folio.login.integration.keycloak.config.KeycloakProperties;
import org.folio.test.types.UnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

@UnitTest
@SpringBootTest(classes = {AdminTokenService.class, TokenCacheConfiguration.class, TokenCacheProperties.class},
                webEnvironment = NONE)
@EnableConfigurationProperties(TokenCacheProperties.class)
class AdminTokenServiceTest {

  private static final String CACHE_KEY = "admin-cli-token";

  @Autowired private AdminTokenService adminTokenService;
  @MockitoSpyBean @Qualifier("adminTokenCache") private Cache<String, KeycloakAuthentication> adminTokenCache;
  @MockitoBean private KeycloakProperties keycloakProperties;
  @MockitoBean private KeycloakAdminProperties adminProperties;
  @MockitoBean private KeycloakClient keycloakClient;

  @BeforeEach
  void setUp() {
    adminTokenCache.invalidateAll();
    when(keycloakProperties.getAdmin()).thenReturn(adminProperties);
    when(adminProperties.getClientId()).thenReturn(CLIENT_ID);
    when(adminProperties.getPassword()).thenReturn(PASSWORD);
    when(adminProperties.getUsername()).thenReturn(USERNAME);
    when(adminProperties.getRealm()).thenReturn(REALM);
  }

  @Test
  void getAdminToken_positive() {
    var keycloakAuth = keycloakAuthentication();
    var requestData = loginRequest(USERNAME, PASSWORD, CLIENT_ID);

    when(keycloakClient.callTokenEndpoint(REALM, requestData, null, null))
      .thenReturn(keycloakAuth);

    var issuedToken = adminTokenService.getAdminToken(null, null);

    assertThat(issuedToken).isEqualTo("Bearer " + ACCESS_TOKEN);
    verify(keycloakClient).callTokenEndpoint(REALM, requestData, null, null);

    // Verify cache.get(key, mappingFunction) was called - this method is thread-safe
    // and ensures atomic computation: only one thread will execute the mapping function
    // for a cache miss, preventing race conditions and duplicate token requests
    verify(adminTokenCache).get(eq(CACHE_KEY), any());
    
    // Verify token is cached
    var cachedToken = adminTokenCache.getIfPresent(CACHE_KEY);
    assertThat(cachedToken).isNotNull();
    assertThat(cachedToken.getAccessToken()).isEqualTo(ACCESS_TOKEN);
  }

  @Test
  void getAdminToken_positive_cacheHit() {
    var keycloakAuth = keycloakAuthentication();
    var requestData = loginRequest(USERNAME, PASSWORD, CLIENT_ID);

    when(keycloakClient.callTokenEndpoint(REALM, requestData, null, null))
      .thenReturn(keycloakAuth);

    // First call - cache miss
    var firstToken = adminTokenService.getAdminToken(null, null);
    assertThat(firstToken).isEqualTo("Bearer " + ACCESS_TOKEN);
    verify(keycloakClient, times(1)).callTokenEndpoint(REALM, requestData, null, null);

    // Second call - cache hit
    var secondToken = adminTokenService.getAdminToken(null, null);
    assertThat(secondToken).isEqualTo("Bearer " + ACCESS_TOKEN);
    
    // Verify cache.get() was called twice (once per getAdminToken call)
    // Using cache.get(key, mappingFunction) is thread-safe: it atomically checks and computes
    verify(adminTokenCache, times(2)).get(eq(CACHE_KEY), any());
    
    // Verify Keycloak client was called only once (second call used cache)
    verify(keycloakClient, times(1)).callTokenEndpoint(REALM, requestData, null, null);
  }
}
