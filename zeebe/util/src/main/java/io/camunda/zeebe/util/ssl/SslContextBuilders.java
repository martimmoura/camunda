/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.zeebe.util.ssl;

import io.netty.handler.ssl.SslContextBuilder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import org.apache.http.ssl.SSLContextBuilder;

public final class SslContextBuilders {
  private SslContextBuilders() {}

  public static SslContextBuilder nettyServerContextBuilder(final SslConfig config)
      throws UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException {
    if (config.useKeyStore()) {
      final var keyStorePassword = getKeyStorePassword(config.getKeyStore());
      final var keyStore = toKeyStore(config.getKeyStore().getFilePath(), keyStorePassword);
      final var keyManagerFactory =
          KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
      keyManagerFactory.init(keyStore, keyStorePassword);

      return SslContextBuilder.forServer(keyManagerFactory.getKeyManagers()[0]);
    }

    final var certChain = config.getCertificateChainPath().toFile();
    final var privateKey = config.getPrivateKeyPath().toFile();
    final var password =
        config.getPrivateKeyPassword() == null || config.getPrivateKeyPassword().isBlank()
            ? null
            : config.getPrivateKeyPassword();
    return SslContextBuilder.forServer(certChain, privateKey, password);
  }

  public static SslContextBuilder nettyClientContextBuilder(final SslConfig config)
      throws KeyStoreException, NoSuchAlgorithmException {
    if (!config.useKeyStore()) {
      return SslContextBuilder.forClient().trustManager(config.getCertificateChainPath().toFile());
    }

    final var keyStore = getKeyStore(config.getKeyStore());
    final var factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
    factory.init(keyStore);

    return SslContextBuilder.forClient().trustManager(factory.getTrustManagers()[0]);
  }

  public static SSLContextBuilder apacheClientContextBuilder(final SslConfig config) {
    final var sslBuilder = SSLContextBuilder.create();
    try {
      if (config.useKeyStore()) {
        final var keyStore = config.getKeyStore();
        sslBuilder.loadTrustMaterial(
            keyStore.getFilePath().toFile(), keyStore.getKeyStorePassword());
      } else {
        sslBuilder.loadTrustMaterial(config.getCertificateChainPath().toFile());
      }

      return sslBuilder;
    } catch (final Exception e) {
      throw new IllegalStateException("Failed to configure SSL trust manager for the exporter", e);
    }
  }

  private static KeyStore getKeyStore(final KeyStoreConfig config) throws KeyStoreException {
    final var keyStorePassword = getKeyStorePassword(config);
    return toKeyStore(config.getFilePath(), keyStorePassword);
  }

  private static char[] getKeyStorePassword(final KeyStoreConfig config) {
    return config.getPassword() == null || config.getPassword().isBlank()
        ? null
        : config.getPassword().toCharArray();
  }

  private static KeyStore toKeyStore(final Path filePath, final char[] keyStorePassword)
      throws KeyStoreException {
    final var keyStore = KeyStore.getInstance("PKCS12");

    try {
      keyStore.load(Files.newInputStream(filePath), keyStorePassword);
    } catch (final Exception e) {
      throw new IllegalStateException(
          """
          Keystore failed to load file: %s, please ensure it is a valid PKCS12 keystore, and you \
          are using the right password (%s)"""
              .formatted(
                  filePath, keyStorePassword.length == 0 ? "no password given" : "password = ***"),
          e);
    }

    return keyStore;
  }
}
