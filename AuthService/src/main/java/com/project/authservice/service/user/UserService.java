package com.project.authservice.service.user;

import com.project.authservice.entity.User;
import java.util.Optional;

public interface UserService {
  void emailExistCheck(String email);

  User save(User user);

  User findByEmailIgnoreCase(String normalize);

  User getUserById(Long id);

  /** Empty for an unknown user and for one who exists but may no longer act. */
  Optional<User> findActiveById(Long id);
}
