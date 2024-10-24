package org.folio.login.integration.users;

import org.folio.common.domain.model.ResultList;
import org.folio.login.integration.users.model.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "users")
public interface UsersClient {

  @GetMapping
  ResultList<User> query(@RequestParam("query") String query, @RequestParam("limit") Integer limit);
}
