package org.folio.login.controller;

import org.folio.login.domain.dto.RefreshToken;
import org.folio.login.domain.dto.SignRefreshToken;
import org.folio.login.domain.dto.SignTokenPayload;
import org.folio.login.domain.dto.Token;
import org.folio.login.domain.dto.TokenResponse;
import org.folio.login.domain.dto.TokenResponseLegacy;
import org.folio.login.rest.resource.AuthtokenApi;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthtokenController implements AuthtokenApi {

  @Override
  public ResponseEntity<Void> tokenInvalidate(String okapiTenant, String okapiUrl, RefreshToken refreshToken) {
    throw new UnsupportedOperationException("tokenInvalidate() method is not implemented");
  }

  @Override
  public ResponseEntity<Void> tokenInvalidateAll(String okapiTenant, String okapiUrl) {
    throw new UnsupportedOperationException("tokenInvalidateAll() method is not implemented");
  }

  @Override
  public ResponseEntity<TokenResponseLegacy> tokenLegacy(String tenant, String okapiUrl, SignTokenPayload payload) {
    throw new UnsupportedOperationException("tokenLegacy() method is not implemented");
  }

  @Override
  public ResponseEntity<TokenResponse> tokenRefresh(String okapiTenant, String okapiUrl, RefreshToken refreshToken) {
    throw new UnsupportedOperationException("tokenRefresh() method is not implemented");
  }

  @Override
  public ResponseEntity<TokenResponse> tokenSign(String okapiTenant, String okapiUrl, SignTokenPayload payload) {
    throw new UnsupportedOperationException("tokenSign() method is not implemented");
  }

  @Override
  public ResponseEntity<Token> tokenSignLegacy(String okapiTenant, String okapiUrl, SignRefreshToken signRefreshToken) {
    throw new UnsupportedOperationException("tokenSignLegacy() method is not implemented");
  }
}
