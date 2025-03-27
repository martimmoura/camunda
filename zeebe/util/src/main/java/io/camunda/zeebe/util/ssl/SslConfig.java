/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.zeebe.util.ssl;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class SslConfig {
  private static final boolean DEFAULT_ENABLED = false;

  private boolean enabled = DEFAULT_ENABLED;
  private Path certificateChainPath;
  private Path privateKeyPath;
  private String privateKeyPassword;
  private KeyStoreConfig keyStore = new KeyStoreConfig();

  public boolean useKeyStore() {
    return keyStore.isConfigured();
  }

  public KeyStoreConfig getKeyStore() {
    return keyStore;
  }

  public SslConfig setKeyStore(final KeyStoreConfig keyStore) {
    this.keyStore = keyStore;
    return this;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public SslConfig setEnabled(final boolean enabled) {
    this.enabled = enabled;
    return this;
  }

  public Path getCertificateChainPath() {
    return certificateChainPath;
  }

  public SslConfig setCertificateChainPath(final Path certificateChainPath) {
    this.certificateChainPath = certificateChainPath;
    return this;
  }

  public Path getPrivateKeyPath() {
    return privateKeyPath;
  }

  public SslConfig setPrivateKeyPath(final Path privateKeyPath) {
    this.privateKeyPath = privateKeyPath;
    return this;
  }

  public String getPrivateKeyPassword() {
    return privateKeyPassword;
  }

  public void setPrivateKeyPassword(final String privateKeyPassword) {
    this.privateKeyPassword = privateKeyPassword;
  }

  public void validate() throws IllegalStateException {
    if ((certificateChainPath != null || privateKeyPath != null) && keyStore.isConfigured()) {
      throw new IllegalArgumentException(
          """
              Expected to configure with a certificate and private key pair, or with a key store \
              and password, but both were provided. Please select only one approach""");
    }

    if (keyStore.isConfigured()) {
      keyStore.validate();
      return;
    }

    validateReadableFile(certificateChainPath, "certificate chain");
    validateReadableFile(privateKeyPath, "private key");
  }

  private void validateReadableFile(final Path file, final String name) {
    if (file == null) {
      throw new IllegalArgumentException(
          "Expected to have a valid %s path for network security, but none configured"
              .formatted(name));
    }

    if (!Files.isReadable(file)) {
      throw new IllegalArgumentException(
          """
          Expected the configured network security %s path '%s' to point to a \
          readable file, but it does not"""
              .formatted(name, file));
    }
  }

  @Override
  public boolean equals(final Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    final SslConfig sslConfig = (SslConfig) o;
    return enabled == sslConfig.enabled
        && Objects.equals(keyStore, sslConfig.keyStore)
        && Objects.equals(certificateChainPath, sslConfig.certificateChainPath)
        && Objects.equals(privateKeyPath, sslConfig.privateKeyPath)
        && Objects.equals(privateKeyPassword, sslConfig.privateKeyPassword);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        keyStore, enabled, certificateChainPath, privateKeyPath, privateKeyPassword);
  }

  @Override
  public String toString() {
    return "SslConfig{"
        + "keyStore="
        + keyStore
        + ", enabled="
        + enabled
        + ", certificateChainPath="
        + certificateChainPath
        + ", privateKeyPath="
        + privateKeyPath
        + ", privateKeyPassword='"
        + (privateKeyPassword == null ? "''" : "***")
        + '\''
        + '}';
  }
}
