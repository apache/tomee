package com.example.domain.player.api

import com.example.domain.player.Player
import com.example.domain.player.PlayerId

/**
 * NewPlayer Read (CQRS) Infrastructure Service
 */
interface PlayerReadRepository {
  /**
   * Find a player by ID
   */
  fun findById(id: PlayerId): Player?

  /**
   * Find a player by name
   */
  fun findByName(name: String): Player?
}
