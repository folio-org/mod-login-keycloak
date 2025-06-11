## Version `v3.1.0-SNAPSHOT` (TBD)
* User can't log in due to invalid token error (MODLOGINKC-44)

## Version `v3.0.0` (12.03.2025)
* Update mod-login-keycloak Java 21 (MODLOGINKC-43)

## Version `v2.0.0` (01.11.2024)
* Increase keycloak-admin-client to v25.0.6 (KEYCLOAK-24)
* JIT AuthUser creation in POST /authn/update (MODLOGINKC-34)
* Post request to /authn/login-with-expirity return 400 when keycloak return 401 (MODLOGINKC-35)

## Version `v1.5.0` (24.09.2024)
* JIT AuthUser creation in POST /authn/credentials (MODLOGINKC-17)

## Version `v1.4.4` (30.08.2024)
* Support cross tenant token refreshing (EUREKA-255)
---

## Version `v1.4.3` (15.07.2024)
* Implement stub for authtoken and authtoken2 interfaces (MODLOGINKC-27)
---

## Version `v1.4.2` (10.07.2024)
* Add kafka producer for sending logout events (MODLOGINKC-19)
* Upgrade keycloak to v25.0.1 (KEYCLOAK-11)
* Handle cookie invalidation in filter and response advice (MODLOGINKC-23)
---

## Version `v1.4.1` (20.06.2024)
* Invalidate cookies upon failed token exchange (MODLOGINKC-23)
* Pack application to Docker Image and push it to ECR (RANCHER-1515)
* Apply OkHttpClient creation and build SslContext from app-poc-tools lib with support of keystore custom type and public trusted certs (APPPOCTOOL-20)
---

## Version `v1.4.0` (25.05.2024)
* Update dependencies

---

## Version `v1.3.0` (16.04.2024)
* Added HTTPS access to Keycloak (MODLOGINKC-12)

---
## Version `v1.2.0` (26.03.2024)
* Provide an expired cookie header upon logout (MODLOGINKC-15).

---
## Version `v1.1.0` (27.02.2024)
### Changes:
* Changed refresh token cookie path (MODLOGINKC-10).
