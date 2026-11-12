package com.project.authservice.util;

import com.project.authservice.controller.oauth2.facade.model.ClientCredentials;
import com.project.authservice.util.exception.OAuth2Exception;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ClientCredentialsUtil {
  private static final String BASIC_PREFIX = "Basic ";

  /**
   * Basic wins over body parameters when both are present, as RFC 6749 §2.3.1 prefers: the header
   * is the form less likely to end up in a server access log.
   */
  public static ClientCredentials resolve(String authorization, String clientId,
                                          String clientSecret) {
    if (authorization != null && authorization.startsWith(BASIC_PREFIX)) {
      return fromBasicHeader(authorization.substring(BASIC_PREFIX.length()).trim());
    }
    if (isBlank(clientId) || isBlank(clientSecret)) {
      throw OAuth2Exception.invalidClient(
          "Authenticate with HTTP Basic or with client_id and client_secret");
    }
    return new ClientCredentials(clientId, clientSecret);
  }

  private static ClientCredentials fromBasicHeader(String encoded) {
    String decoded = decodeBase64(encoded);
    int separator = decoded.indexOf(':');
    if (separator < 0) {
      throw malformedBasicHeader();
    }
    // The two halves are form-encoded before being base64'd, so a secret containing '+' or '%'
    // arrives escaped and has to be decoded back before it is compared to the stored hash.
    return new ClientCredentials(
        URLDecoder.decode(decoded.substring(0, separator), StandardCharsets.UTF_8),
        URLDecoder.decode(decoded.substring(separator + 1), StandardCharsets.UTF_8));
  }

  private static String decodeBase64(String encoded) {
    try {
      return new String(Base64.getDecoder().decode(encoded), StandardCharsets.UTF_8);
    } catch (IllegalArgumentException e) {
      throw malformedBasicHeader();
    }
  }

  private static OAuth2Exception malformedBasicHeader() {
    return OAuth2Exception.invalidClient("Malformed Basic authorization header");
  }

  private static boolean isBlank(String value) {
    return value == null || value.isBlank();
  }
}
