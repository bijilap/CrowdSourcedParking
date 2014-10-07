package edu.usc.csci587.csp.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.JSONObject;

@Path("/parking")
public class ParkingService {
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{id}")
	public Response updateUserLocation(@PathParam("id") String id)
	{
		JSONObject parkingGarage = new JSONObject();
		parkingGarage.put("id", id);
		return Response.status(200).entity(parkingGarage.toString()).build();
	}
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/query")
	public Response searchForParkingGarages(@QueryParam("polygon") String polygon)
	{
		System.out.println("looking in " + polygon);
		JSONObject parkingGarage = new JSONObject();
		parkingGarage.put("id", 1);
		return Response.status(200).entity(parkingGarage.toString()).build();
	}
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/query")
	public Response searchForParkingGarages(@QueryParam("point") String point, @QueryParam("radius") Double radius)
	{
		System.out.println("looking in " + point + " with radius " + radius );
		JSONObject parkingGarage = new JSONObject();
		parkingGarage.put("id", 1);
		return Response.status(200).entity(parkingGarage.toString()).build();
	}
}
