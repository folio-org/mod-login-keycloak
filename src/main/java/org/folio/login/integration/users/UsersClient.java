package org.folio.login.integration.users;

import org.folio.common.domain.model.ResultList;
import org.folio.login.integration.users.model.User;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange(url = "users")
public interface UsersClient {

  @GetExchange
  ResultList<User> query(@RequestParam("query") String query, @RequestParam("limit") Integer limit);
}
