package org.folio.login.controller.cookie.filter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public record HttpRequestResponseHolder(HttpServletRequest request, HttpServletResponse response) {
}
