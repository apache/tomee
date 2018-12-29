package com.example.application

import com.example.domain.player.CreatePlayer
import com.example.domain.player.PlayerCreated
import com.example.domain.player.api.PlayerService
import java.util.logging.Logger.getLogger
import javax.enterprise.event.Observes
import javax.inject.Inject
import javax.inject.Singleton

/**
 * CDI Event Handler for NewPlayer
 *
 * Constructor injected dependencies include the player service
 */
@Singleton
class CreatePlayerHandler @Inject constructor(
  private val playerService: PlayerService
) {
  private val logger = getLogger(CreatePlayerHandler::class.java.simpleName)

  /**
   * Observe the NewPlayer CDI event (domain command) and call the player service to create the player
   */
  fun handle(@Observes cmd: CreatePlayer) {
    playerService.create(cmd.player.copy(id = playerService.generateId()))
  }

  /**
   * Observe the PlayerCreated CDI event and react by sending an email or slack, then logging the event
   */
  fun handle(@Observes event: PlayerCreated) {
    // Send an email
    // Send a slack notification
    logger.info("PlayerCreated=${event.player.id}")
  }
}
