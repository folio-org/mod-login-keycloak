package org.folio.login.integration.users;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.folio.login.support.TestConstants.USERNAME;
import static org.folio.login.support.TestValues.user;
import static org.mockito.Mockito.when;

import jakarta.persistence.EntityNotFoundException;
import org.folio.common.domain.model.ResultList;
import org.folio.test.types.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @Mock private UsersClient usersClient;
  @InjectMocks private UserService userService;

  @Test
  void getUserByUsername_positive() {
    var user = user();
    when(usersClient.query("username==" + user.getUsername(), 1)).thenReturn(ResultList.asSinglePage(user));

    var result = userService.getUserByUsername(user.getUsername());

    assertThat(result).isEqualTo(user);
  }

  @Test
  void getUserByUsername_negative_notFound() {
    when(usersClient.query("username==" + USERNAME, 1)).thenReturn(ResultList.empty());

    assertThatThrownBy(() -> userService.getUserByUsername(USERNAME))
      .isInstanceOf(EntityNotFoundException.class)
      .hasMessage("Failed to find user by name: username = " + USERNAME);
  }
}
