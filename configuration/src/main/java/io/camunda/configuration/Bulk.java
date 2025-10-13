/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.configuration;

import io.camunda.configuration.UnifiedConfigurationHelper.BackwardsCompatibilityMode;
import java.util.Set;

public class Bulk {

  private static final int DEFAULT_DELAY = 5;
  private static final int DEFAULT_SIZE = 1_000;
  private static final int DEFAULT_MEMORY_LIMIT = 20;

  private final String prefix;

  /** Delay before forced flush (in seconds) */
  private int delay = DEFAULT_DELAY;

  /** Bulk size before flush */
  private int size = DEFAULT_SIZE;

  /** Bulk memory utilisation before flush (in MB) */
  private int memoryLimit = DEFAULT_MEMORY_LIMIT;

  public Bulk(final String databaseName) {
    prefix = "camunda.data.secondary-storage.%s.bulk".formatted(databaseName);
  }

  public int getDelay() {
    return UnifiedConfigurationHelper.validateLegacyConfiguration(
        prefix + ".delay",
        delay,
        Integer.class,
        BackwardsCompatibilityMode.SUPPORTED_ONLY_IF_VALUES_MATCH,
        Set.of("zeebe.broker.exporters.camundaexporter.args.bulk.delay"));
  }

  public void setDelay(final int delay) {
    this.delay = delay;
  }

  public int getSize() {
    return UnifiedConfigurationHelper.validateLegacyConfiguration(
        prefix + ".size",
        size,
        Integer.class,
        BackwardsCompatibilityMode.SUPPORTED_ONLY_IF_VALUES_MATCH,
        Set.of("zeebe.broker.exporters.camundaexporter.args.bulk.size"));
  }

  public void setSize(final int size) {
    this.size = size;
  }

  public int getMemoryLimit() {
    return UnifiedConfigurationHelper.validateLegacyConfiguration(
        prefix + ".memory-limit",
        memoryLimit,
        Integer.class,
        BackwardsCompatibilityMode.SUPPORTED_ONLY_IF_VALUES_MATCH,
        Set.of("zeebe.broker.exporters.camundaexporter.args.bulk.memory-limit"));
  }

  public void setMemoryLimit(final int memoryLimit) {
    this.memoryLimit = memoryLimit;
  }
}
