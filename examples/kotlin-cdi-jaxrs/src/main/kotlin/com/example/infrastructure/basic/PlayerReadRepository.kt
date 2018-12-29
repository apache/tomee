package com.example.infrastructure.basic

import com.example.domain.player.Player
import com.example.domain.player.PlayerId
import com.example.domain.player.api.PlayerReadRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayerReadRepository @Inject constructor(private val playerDatabase: PlayerDatabase): PlayerReadRepository {
  override fun findById(id: PlayerId): Player? = playerDatabase.findById(id)
  override fun findByName(name: String): Player? = playerDatabase.findByName(name)
}
