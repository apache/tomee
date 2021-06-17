package com.example.domain.player.api

import com.example.domain.player.Player
import com.example.domain.player.PlayerId
import java.util.UUID

/**
 * NewPlayer Domain Service
 */
interface PlayerService {
  /**
   * Generate a new player ID.
   *
   * Default implementation
   */
  fun generateId(): PlayerId = UUID.randomUUID().toString()

  /**
   * Create a player
   */
  fun create(player: Player)
}
