package com.project.authservice.service.user.impl;

import com.project.authservice.entity.User;
import com.project.authservice.repository.UserRepository;
import com.project.authservice.service.user.UserService;
import com.project.authservice.util.exception.BusinessException;
import com.project.authservice.util.exception.ErrorCode;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;

  @Override
  public void emailExistCheck(String email) {
    if (userRepository.existsByEmailIgnoreCase(email)) {
      throw new BusinessException(ErrorCode.EMAIL_ALREADY_USED, email);
    }
  }

  @Override
  public User save(User user) {
    return userRepository.save(user);
  }

  @Override
  public User findByEmailIgnoreCase(String email) {
    return userRepository.findByEmailIgnoreCase(email).orElseThrow(() -> new BusinessException(ErrorCode.INVALID_EMAIL));
  }

  @Override
  public User getUserById(Long id) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, id));
    if (!user.isActive()) {
      throw new BusinessException(ErrorCode.ACCOUNT_INACTIVE);
    }
    return user;
  }

  @Override
  public Optional<User> findActiveById(Long id) {
    return userRepository.findById(id).filter(User::isActive);
  }
}
