package org.folio.login.controller;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface InvalidateCookies {

  Strategy strategy() default Strategy.ALWAYS;

  enum Strategy {
    ALWAYS,
    EXCEPTION_ONLY
  }
}
