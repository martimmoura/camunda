/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.zeebe.broker.system.configuration;

import io.camunda.zeebe.util.ssl.SslConfig;
import java.nio.file.Path;

public final class SecurityCfg extends SslConfig implements ConfigurationEntry {
  @Override
  public void init(final BrokerCfg globalConfig, final String brokerBase) {
    final var brokerBasePath = Path.of(brokerBase);

    final var certificateChainPath = getCertificateChainPath();
    if (certificateChainPath != null) {
      setCertificateChainPath(brokerBasePath.resolve(certificateChainPath));
    }

    final var privateKeyPath = getPrivateKeyPath();
    if (privateKeyPath != null) {
      setPrivateKeyPath(brokerBasePath.resolve(privateKeyPath));
    }

    final var keyStore = getKeyStore();
    final var keyStorePath = keyStore.getFilePath();
    if (keyStorePath != null) {
      keyStore.setFilePath(brokerBasePath.resolve(keyStorePath));
    }
  }
}
