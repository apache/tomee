package com.example.domain.player

// Domain Type Aliases
typealias PlayerId = String

// Domain Models
data class Player(val id: PlayerId?, val name: String)

// Domain Commands
sealed class Command
data class CreatePlayer(val player: Player) : Command()

// Domain Events
sealed class Event
data class PlayerCreated(val player: Player) : Event()
