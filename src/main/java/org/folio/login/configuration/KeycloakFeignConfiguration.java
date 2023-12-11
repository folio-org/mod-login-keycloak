package org.folio.login.configuration;

import feign.Client;
import feign.codec.Encoder;
import feign.form.FormEncoder;
import feign.okhttp.OkHttpClient;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.context.annotation.Bean;

/**
 * Keycloak feign client additional configuration.
 */
public class KeycloakFeignConfiguration {

  /**
   * Additional map to form-data encoder for feign client.
   *
   * @param converters - existing {@link HttpMessageConverters} factory
   * @return configured {@link Encoder} object
   */
  @Bean
  Encoder feignFormEncoder(ObjectFactory<HttpMessageConverters> converters) {
    return new FormEncoder(new SpringEncoder(converters));
  }

  /**
   * Feign {@link OkHttpClient} based client.
   *
   * @param okHttpClient - {@link OkHttpClient} from spring context
   * @return created feign {@link Client} object
   */
  @Bean
  public Client feignClient(okhttp3.OkHttpClient okHttpClient) {
    return new OkHttpClient(okHttpClient);
  }
}
