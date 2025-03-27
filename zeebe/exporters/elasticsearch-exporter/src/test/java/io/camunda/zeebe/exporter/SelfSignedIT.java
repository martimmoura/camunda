/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Zeebe Community License 1.1. You may not use this file
 * except in compliance with the Zeebe Community License 1.1.
 */
package io.camunda.zeebe.exporter;

import static org.assertj.core.api.Assertions.assertThatNoException;

import io.camunda.zeebe.test.util.testcontainers.TestSearchContainers;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import java.io.IOException;
import java.nio.file.Path;
import java.security.cert.CertificateException;
import java.util.Objects;
import java.util.UUID;
import org.agrona.LangUtil;
import org.junit.jupiter.api.Test;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;

@Testcontainers
final class SelfSignedIT {

  @Container
  private static final ElasticsearchContainer CONTAINER =
      TestSearchContainers.createDefeaultElasticsearchContainer()
          .withEnv("xpack.license.self_generated.type", "trial")
          .withEnv("xpack.security.enabled", "true")
          .withEnv("xpack.security.http.ssl.enabled", "true")
          .withEnv("xpack.security.http.ssl.certificate", "cert.pem")
          .withEnv("xpack.security.http.ssl.key", "key.pem")
          .withEnv("xpack.security.authc.anonymous.username", "anon")
          .withEnv("xpack.security.authc.anonymous.roles", "superuser")
          .withEnv("xpack.security.authc.anonymous.authz_exception", "true")
          .withCopyFileToContainer(
              MountableFile.forHostPath("/tmp/certificate.pem", 0777),
              "/usr/share/elasticsearch/config/cert.pem")
          .withCopyFileToContainer(
              MountableFile.forHostPath("/tmp/key.pem", 0777),
              "/usr/share/elasticsearch/config/key.pem");

  @Test
  void shouldConnectWithSelfSignedCertificate() throws IOException {
    // when
    // force recreating the client
    final var config = new ElasticsearchExporterConfiguration();
    config.index.prefix = UUID.randomUUID() + "-test-record";
    config.url = "https://" + CONTAINER.getHttpHostAddress();

    // when
    try (final var client = new ElasticsearchClient(config, new SimpleMeterRegistry())) {
      // then
      assertThatNoException().isThrownBy(client::putComponentTemplate);
    }
  }
}
