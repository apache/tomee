package com.example.infrastructure.config

import javax.enterprise.inject.Produces
import javax.inject.Singleton

/**
 * Configuration producer creates a config object for CDI, using TypeSafe Config
 *
 * Config file located (by default) at resources/application.conf
 */
class ConfigProducer {
  @Produces
  @Singleton
  fun create(): AppConfig = AppConfig(listOf("admin", "bob"))
}
