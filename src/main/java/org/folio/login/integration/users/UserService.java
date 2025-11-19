package org.folio.login.integration.users;

import static java.lang.String.format;
import static org.folio.common.utils.CollectionUtils.findOne;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.login.integration.users.model.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Log4j2
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

  private final UsersClient usersClient;

  public User getUserByUsername(String username) {
    log.debug("Getting user by username: {}", username);

    return findOne(usersClient.query("username==" + username, 1).getRecords())
      .orElseThrow(() -> new EntityNotFoundException(format("Failed to find user by name: username = %s", username)));
  }
}
