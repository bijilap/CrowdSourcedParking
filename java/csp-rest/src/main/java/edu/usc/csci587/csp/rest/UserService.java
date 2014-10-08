package edu.usc.csci587.csp.rest;

import javax.persistence.EntityManager;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.json.JSONObject;

import com.vividsolutions.jts.geom.Point;

import edu.usc.csci587.csp.db.data.User;
import edu.usc.csci587.csp.db.data.util.JPAUtil;
import edu.usc.csci587.csp.db.data.util.ParkingManager;

@Path("/user")
public class UserService {

	@GET
	@Path("/{id}")
	public Response getUser(@PathParam("id") Long id)
	{
		EntityManager em = JPAUtil.createEntityManager();
		em.getTransaction().begin();
		User user = em.find(User.class, id);
		em.getTransaction().commit();
		em.close();
		JSONObject userJSON = new JSONObject();
		userJSON.put("id",user.getId());
		userJSON.put("timestamp", user.getTimestamp());
		userJSON.put("name", user.getName());
		userJSON.put("location", user.getLocation());
		
		return Response.status(200).entity(userJSON.toString()).build();
	}
	
	
	@GET
	@Path("/{id}/location")
	public Response getLastUserReportedLocation(@PathParam("id") Long id)
	{
		System.out.println("Getting user reported location for " + id);
		EntityManager em = JPAUtil.createEntityManager();
		em.getTransaction().begin();
		User user = em.find(User.class, id);
		em.getTransaction().commit();
		em.close();
		JSONObject userJSON = new JSONObject();
		userJSON.put("timestamp", user.getTimestamp());
		userJSON.put("location", user.getLocation());
		return Response.status(200).entity(userJSON.toString()).build();
	}
	
	@POST
	@Path("/{id}/location")
	public Response updateUserLocation(@PathParam("id") Long id, @FormParam("location") String locationWKT, @FormParam("timestamp") Long timestamp)
	{
		System.out.println("Updating user reported location for " + id);
		EntityManager em = JPAUtil.createEntityManager();
		em.getTransaction().begin();
		User user = em.find(User.class, id);
		user.setTimestamp(timestamp);
		user.setLocation((Point)ParkingManager.wktToGeometry(locationWKT));
		em.persist(user);
		em.getTransaction().commit();
		em.close();
		return Response.status(200).build();
	}
}
