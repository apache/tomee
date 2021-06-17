package com.example.rest

import com.example.domain.player.Player as DomainPlayer

// Data class for sending proper API errors back to consumers
data class ApiError(val code: Int, val message: String)

// Write-side Model (CQRS)
data class NewPlayer(var name: String = "")

// Read-side Model (CQRS)
data class Player(val id: String, val name: String)

// Extension Functions for port <-> domain model conversions
internal fun NewPlayer.toDomain() = DomainPlayer(null, name)
internal fun DomainPlayer.toRest() = Player(id ?: "", name)
