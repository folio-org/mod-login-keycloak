package org.folio.login.service;

import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.folio.spring.integration.XOkapiHeaders.TENANT;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;
import org.folio.common.configuration.properties.FolioEnvironment;
import org.folio.login.domain.model.KeycloakRealmConfiguration;
import org.folio.login.integration.keycloak.config.KeycloakProperties;
import org.folio.login.service.RealmConfigurationProviderTest.TestContextConfiguration;
import org.folio.spring.DefaultFolioExecutionContext;
import org.folio.spring.FolioExecutionContext;
import org.folio.test.types.UnitTest;
import org.folio.tools.store.SecureStore;
import org.folio.tools.store.exception.NotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.Cache.ValueWrapper;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;

@UnitTest
@SpringBootTest(classes = {RealmConfigurationProvider.class, TestContextConfiguration.class})
class RealmConfigurationProviderTest {

  private static final String KEY = "test_test_tenant_test_tenant-app";
  private static final String TENANT_ID = "test_tenant";
  private static final String CACHE_NAME = "keycloak-configuration";
  private static final String SECRET = "kc-client-secret";

  @Autowired private RealmConfigurationProvider realmConfigurationProvider;
  @Autowired private CacheManager cacheManager;
  @MockBean private SecureStore secureStore;
  @MockBean private FolioEnvironment folioEnvironment;
  @MockBean private KeycloakProperties keycloakProperties;

  @AfterEach
  void tearDown() {
    cacheManager.getCacheNames().forEach(cacheName -> requireNonNull(cacheManager.getCache(cacheName)).clear());
  }

  @Test
  void getRealmConfiguration_positive() {
    when(keycloakProperties.getClientSuffix()).thenReturn("-app");
    when(folioEnvironment.getEnvironment()).thenReturn("test");
    when(secureStore.get(KEY)).thenReturn(SECRET);

    var actual = realmConfigurationProvider.getRealmConfiguration();

    var expectedValue = new KeycloakRealmConfiguration()
      .clientId("test_tenant-app")
      .clientSecret("kc-client-secret");

    assertThat(actual).isEqualTo(expectedValue);
    assertThat(getCachedValue()).isPresent().get().isEqualTo(expectedValue);
  }

  @Test
  void getRealmConfiguration_clientSecretNotFound() {
    when(keycloakProperties.getClientSuffix()).thenReturn("-app");
    when(folioEnvironment.getEnvironment()).thenReturn("test");
    when(secureStore.get(KEY)).thenThrow(new NotFoundException("not found"));

    assertThatThrownBy(() -> realmConfigurationProvider.getRealmConfiguration())
      .isInstanceOf(IllegalStateException.class)
      .hasMessage("Failed to get value from secure store [tenantId: test_tenant, clientId: test_tenant-app]");

    assertThat(getCachedValue()).isEmpty();
  }

  private Optional<Object> getCachedValue() {
    return ofNullable(cacheManager.getCache(CACHE_NAME))
      .map(cache -> cache.get(TENANT_ID))
      .map(ValueWrapper::get);
  }

  @EnableCaching
  @TestConfiguration
  static class TestContextConfiguration {

    @Bean
    CacheManager cacheManager() {
      return new ConcurrentMapCacheManager(CACHE_NAME);
    }

    @Bean
    FolioExecutionContext folioExecutionContext() {
      return new DefaultFolioExecutionContext(null, Map.of(TENANT, singletonList(TENANT_ID)));
    }
  }
}
