package com.example.domain.player.api

import com.example.domain.player.Player

/**
 * NewPlayer Write (CQRS) Infrastructure Service
 */
interface PlayerWriteRepository {
  /**
   * Create a player, persisting to database, file, etc
   */
  fun create(player: Player)
}
