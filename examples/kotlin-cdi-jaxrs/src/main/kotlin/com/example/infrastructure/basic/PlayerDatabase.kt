package com.example.infrastructure.basic

import com.example.domain.player.Player
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Singleton

@Singleton
class PlayerDatabase {
  private val players = ConcurrentHashMap<String, Player>(mapOf(
    "admin" to Player("admin", "Admin User 1"),
    "bob" to Player("bob", "Admin User 2")
  ))

  fun persist(player: Player) {
    players.putIfAbsent(player.id!!, player)
  }

  fun findById(id: String): Player? = players[id]
  fun findByName(name: String): Player? = players.entries.firstOrNull { it.value.name == name.trim() }?.value
}
