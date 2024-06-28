package org.folio.login.controller.cookie;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public record HttpRequestResponseHolder(HttpServletRequest request, HttpServletResponse response) {
}
