package com.project.authservice.util;

import java.util.Locale;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserCredentialsUtil {
  public static String normalize(String email) {
    return email.trim().toLowerCase(Locale.ROOT);
  }
}
