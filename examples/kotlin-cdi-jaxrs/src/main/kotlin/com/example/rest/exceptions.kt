package com.example.rest

class CreatePlayerException(val player: NewPlayer) : Exception()
class PlayerNotFoundException(val id: String) : Exception()
