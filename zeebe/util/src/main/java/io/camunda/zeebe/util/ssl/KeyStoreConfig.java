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

public final class KeyStoreConfig {
  private Path filePath;
  private String password;

  public Path getFilePath() {
    return filePath;
  }

  public KeyStoreConfig setFilePath(final Path filePath) {
    this.filePath = filePath;
    return this;
  }

  public String getPassword() {
    return password;
  }

  public char[] getKeyStorePassword() {
    if (password == null || password.isBlank()) {
      return null;
    }

    return password.toCharArray();
  }

  public KeyStoreConfig setPassword(final String password) {
    this.password = password;
    return this;
  }

  public boolean isConfigured() {
    return filePath != null;
  }

  public void validate() throws IllegalStateException {
    if (!Files.isReadable(filePath)) {
      throw new IllegalArgumentException(
          String.format(
              """
                  Expected the configured network security keystore file '%s' to point to a \
                  readable file, but it does not""",
              filePath));
    }
  }

  @Override
  public String toString() {
    return "KeyStoreConfig{"
        + "filePath="
        + filePath
        + ", password='"
        + (password == null ? "''" : "***")
        + '\''
        + '}';
  }

  @Override
  public boolean equals(final Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    final KeyStoreConfig that = (KeyStoreConfig) o;
    return Objects.equals(filePath, that.filePath) && Objects.equals(password, that.password);
  }

  @Override
  public int hashCode() {
    return Objects.hash(filePath, password);
  }
}
