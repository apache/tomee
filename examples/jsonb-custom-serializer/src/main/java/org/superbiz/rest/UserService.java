package org.superbiz.rest;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Stateless;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.superbiz.model.Address;
import org.superbiz.model.User;

@Path("users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Stateless
public class UserService {

	@GET
	public List<User> users() {
		List<User> users = new ArrayList<>();
		User user1 = new User(1, "user 1", new Address("addr1"));
		User user2 = new User(2, "user 2", new Address("addr2"));
		users.add(user1);
		users.add(user2);

		return users;
	}

	@POST
	public User addUser(User u) {
		// Just to show the deserialization
		return u;
	}
}
