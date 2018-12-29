package com.example.domain.player

import com.example.domain.player.api.PlayerValidator
import com.example.infrastructure.config.AppConfig
import javax.inject.Inject
import javax.inject.Singleton

/**
 * A player validator class, which will throw an exception if the player name is an admin name
 */
@Singleton
class BlacklistNameValidator @Inject constructor(private val appConfig: AppConfig) : PlayerValidator {
  override fun validate(player: Player) {
    if (appConfig.admins.contains(player.name.trim().toLowerCase())) {
      throw BlacklistedNameException(player.name)
    }
  }
}
