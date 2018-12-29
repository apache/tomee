package com.example.rest

import com.example.domain.player.CreatePlayer
import com.example.domain.player.api.PlayerReadRepository
import java.net.URI
import javax.enterprise.event.Event
import javax.inject.Inject
import javax.inject.Singleton
import javax.ws.rs.Consumes
import javax.ws.rs.DefaultValue
import javax.ws.rs.GET
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType.APPLICATION_JSON
import javax.ws.rs.core.Response
import javax.ws.rs.core.Response.created
import javax.ws.rs.core.Response.ok

@Singleton
@Path("/")
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
class PlayerController @Inject constructor(
  private val createPlayer: Event<com.example.domain.player.CreatePlayer>,
  private val playerReadRepository: PlayerReadRepository
) {
  @GET
  @Path("/players/{id}")
  fun getPlayer(
    @PathParam("id")
    @DefaultValue("")
    id: String
  ): Response {
    // Looking up a user via a read repository does not need to go through a domain service
    val player = playerReadRepository.findById(id) ?: throw PlayerNotFoundException(id)
    return ok(player.toRest()).build()
  }

  @POST
  @Path("/players")
  fun create(player: NewPlayer): Response {
    // Synchronously fire a CreatePlayer CDI Event (command). This is one technique to de-couple
    // your code by not needing to know how/what to call to handle creation of a player (other than emitting a command)
    createPlayer.fire(CreatePlayer(player.toDomain()))

    // Because the CDI Event was fired synchronously, we can read back the newly created player.
    // If for some reason we cannot find the newly created user, throw an explicit exception,
    // which will be handled by an associated ExceptionMapper (see exceptions package)
    val newPlayer = playerReadRepository.findByName(player.name) ?: throw CreatePlayerException(player)

    // Finally, return a 201 Created with a Location header to the newly created Player resource
    return created(URI.create("/players/${newPlayer.id}")).build()
  }
}
