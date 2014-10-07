package edu.usc.csci587.csp.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/user")
public class UserService {
	
	@POST
	@Path("/{id}/location")
	public Response updateUserLocation(@PathParam("id") String id, @FormParam("location") String locationWKT, @FormParam("timestamp") Long timestamp)
	{
		System.out.println("User "+ id + " is at " + locationWKT + " at " + timestamp);
		return Response.status(200).build();
	}
}
