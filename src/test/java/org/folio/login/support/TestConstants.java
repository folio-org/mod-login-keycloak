package org.folio.login.support;

import java.sql.Date;
import java.util.UUID;

public class TestConstants {

  public static final String PASSWORD = "test-password";
  public static final String USERNAME = "test-username";
  public static final String ADMIN_USERNAME = "admin-username";
  public static final String ADMIN_PASSWORD = "admin-password";
  public static final String ADMIN_USER_ID = "11111111-2222-4999-0000-999999999999";
  public static final String NEW_PASSWORD = "test-new-password";
  public static final String CLIENT_ID = "test-tenant-login-application";
  public static final String CLIENT_SECRET = "kc-client-password";
  public static final String USER_ID = "99999999-9999-4999-9999-999999999999";
  public static final String KEYCLOAK_USER_ID = "99999999-1111-1111-1111-999999999999";
  public static final String USER_CREDENTIAL_ID = "00000000-0000-0000-0000-000000000000";
  public static final String TENANT = "test";
  public static final String REALM = TENANT;
  public static final String OKAPI_URL = "http://okapi:9130";
  public static final String AUTH_CODE = "secret_code";
  public static final String TOKEN_CACHE = "token";
  public static final String KEYCLOAK_CONFIG_CACHE = "keycloak-configuration";
  public static final String PASSWORD_RESET_ACTION_ID = "aa69563e-78e7-4e5d-a264-e40a55616150";
  public static final UUID PASSWORD_RESET_ACTION_UUID = UUID.fromString(PASSWORD_RESET_ACTION_ID);
  public static final UUID USER_UUID = UUID.fromString(USER_ID);
  public static final Date EXPIRATION_TIME = Date.valueOf("2023-05-05");
  public static final String ACCESS_TOKEN =
    "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpYXQiOjE2OTQwNjU3NDMsImV4cCI6MTY5NDE1MjE0Mywic3ViIjoidGVzdCJ9"
      + ".7H6sfLynriwVv4jvIJuqrg_LsPfTKaOqin70w7cdT_E";
  public static final String REFRESH_TOKEN =
    "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpYXQiOjE2OTQwNjU3NDMsImV4cCI6MTY5NDE1MjE0Mywic3ViIjoidGVzdF9yZWZyZXNoIn0"
      + ".3js-w0zcRx3_MRGrCm5Krr4vI0yrx4-14KGXiD4PIsU";
  public static final long EXPIRES_IN = 3600;
  public static final long REFRESH_EXPIRES_IN = 10800;
  public static final String ACCESS_TOKEN_EXPIRATION_DATE = "2023-08-28T16:44:31Z";
  public static final String REFRESH_TOKEN_EXPIRATION_DATE = "2023-09-28T16:44:31Z";
  public static final String BEARER_TOKEN = "Bearer " + ACCESS_TOKEN;
}
