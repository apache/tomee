package com.example.domain.player.api

import com.example.domain.player.Player

/**
 * Player Validator Domain Service
 */
interface PlayerValidator {
  /**
   * Validate player domain model
   */
  fun validate(player: Player)
}
