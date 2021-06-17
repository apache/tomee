package com.example.domain.player

import com.example.domain.player.api.PlayerService
import com.example.domain.player.api.PlayerValidator
import com.example.domain.player.api.PlayerWriteRepository
import javax.enterprise.event.Event
import javax.enterprise.inject.Instance
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Player Domain Service Implementation
 *
 * Handles validation checking, persisting the new player, and firing domain events
 */
@Singleton
class PlayerService @Inject constructor(
  private val validators: Instance<PlayerValidator>,
  private val playerWriteRepository: PlayerWriteRepository,
  private val playerCreated: Event<PlayerCreated>
): PlayerService {
  override fun create(player: Player) {
    // Iterate over each PlayerValidate, which will throw a PlayerValidationException on any errors
    validators.forEach { it.validate(player) }

    // Persist the new player
    playerWriteRepository.create(player)

    // Fire a domain event for PlayerCreated
    playerCreated.fire(PlayerCreated(player))
  }
}
