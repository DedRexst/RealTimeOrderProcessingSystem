package com.project.authservice.security;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Owns the RSA keypair that separates the two halves of this design: only this service can sign a
 * token, while anyone who can read {@code /.well-known/jwks.json} can verify one. That is what
 * lets OrderService or a partner check a caller without a round trip here, and what makes losing
 * the private key the one incident that matters.
 *
 * <p>The keypair is persisted as PEM rather than generated per boot: an ephemeral key would
 * invalidate every issued token on restart and, worse, do it silently for anyone still holding a
 * cached JWKS.
 */
@Slf4j
@Component
public class JwtKeyManager {

  private static final String PRIVATE_KEY_FILE = "private-key.pem";
  private static final String PUBLIC_KEY_FILE = "public-key.pem";
  private static final String PRIVATE_PEM_TYPE = "PRIVATE KEY";
  private static final String PUBLIC_PEM_TYPE = "PUBLIC KEY";
  private static final int KEY_SIZE = 2048;

  private final RSAKey signingKey;
  private final JWSSigner signer;

  public JwtKeyManager(JwtProperties properties) {
    KeyPair keyPair = loadOrGenerate(Path.of(properties.getKeyDirectory()));
    try {
      this.signingKey = new RSAKey.Builder((RSAPublicKey) keyPair.getPublic())
          .privateKey((RSAPrivateKey) keyPair.getPrivate())
          .keyUse(KeyUse.SIGNATURE)
          .algorithm(JWSAlgorithm.RS256)
          .keyIDFromThumbprint()
          .build();
      this.signer = new RSASSASigner(this.signingKey);
    } catch (JOSEException e) {
      throw new IllegalStateException("Cannot build the JWT signing key", e);
    }
  }

  public JWSSigner signer() {
    return signer;
  }

  public String keyId() {
    return signingKey.getKeyID();
  }

  public RSAPublicKey publicKey() {
    try {
      return signingKey.toRSAPublicKey();
    } catch (JOSEException e) {
      throw new IllegalStateException("Cannot expose the JWT public key", e);
    }
  }

  /** The published JWK set — public parameters only, never the private exponent. */
  public Map<String, Object> publicJwkSet() {
    return new JWKSet(signingKey.toPublicJWK()).toJSONObject();
  }

  private KeyPair loadOrGenerate(Path directory) {
    Path privateKeyPath = directory.resolve(PRIVATE_KEY_FILE);
    Path publicKeyPath = directory.resolve(PUBLIC_KEY_FILE);
    try {
      if (Files.exists(privateKeyPath) && Files.exists(publicKeyPath)) {
        KeyFactory factory = KeyFactory.getInstance("RSA");
        return new KeyPair(
            factory.generatePublic(new X509EncodedKeySpec(readPem(publicKeyPath))),
            factory.generatePrivate(new PKCS8EncodedKeySpec(readPem(privateKeyPath))));
      }
      KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
      generator.initialize(KEY_SIZE);
      KeyPair generated = generator.generateKeyPair();
      Files.createDirectories(directory);
      writePem(privateKeyPath, PRIVATE_PEM_TYPE, generated.getPrivate().getEncoded());
      writePem(publicKeyPath, PUBLIC_PEM_TYPE, generated.getPublic().getEncoded());
      log.warn("Generated a new RSA keypair in {}. Keep it: tokens signed with it stop verifying "
          + "if it is lost, and anyone holding it can mint tokens for this system.", directory);
      return generated;
    } catch (IOException | GeneralSecurityException e) {
      throw new IllegalStateException("Cannot load or create the JWT keypair in " + directory, e);
    }
  }

  private static byte[] readPem(Path file) throws IOException {
    String pem = Files.readString(file)
        .replaceAll("-----(BEGIN|END) (RSA )?(PRIVATE|PUBLIC) KEY-----", "")
        .replaceAll("\\s", "");
    return Base64.getDecoder().decode(pem);
  }

  private static void writePem(Path file, String type, byte[] der) throws IOException {
    String body = Base64.getMimeEncoder(64, System.lineSeparator().getBytes(StandardCharsets.US_ASCII)).encodeToString(der);
    Files.writeString(file, "-----BEGIN %s-----%n%s%n-----END %s-----%n"
        .formatted(type, body, type));
    restrictToOwner(file);
  }

  /** Best effort: on a filesystem without POSIX permissions the key is only as safe as the disk. */
  private static void restrictToOwner(Path file) {
    try {
      Files.setPosixFilePermissions(file,
          Set.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE));
    } catch (IOException | UnsupportedOperationException e) {
      log.warn("Could not restrict permissions on {}: {}", file, e.getMessage());
    }
  }
}
