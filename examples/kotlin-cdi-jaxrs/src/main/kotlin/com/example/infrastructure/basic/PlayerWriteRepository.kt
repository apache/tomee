package com.example.infrastructure.basic

import com.example.domain.player.Player
import com.example.domain.player.api.PlayerWriteRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayerWriteRepository @Inject constructor(private val playerDatabase: PlayerDatabase): PlayerWriteRepository {
  override fun create(player: Player) = playerDatabase.persist(player)
}
