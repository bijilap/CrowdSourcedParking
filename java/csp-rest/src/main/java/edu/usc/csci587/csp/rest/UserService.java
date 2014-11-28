package edu.usc.csci587.csp.rest;

import java.util.Iterator;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.json.JSONArray;
import org.json.JSONObject;

import com.vividsolutions.jts.geom.Point;

import edu.usc.csci587.csp.db.data.User;
import edu.usc.csci587.csp.db.data.util.JPAUtil;
import edu.usc.csci587.csp.db.data.util.ParkingManager;

@Path("/user")
public class UserService {

	@GET
	@Path("/{id}")
	public Response getUser(@PathParam("id") String id)
	{
		EntityManager em = JPAUtil.createEntityManager();
		em.getTransaction().begin();
		User user = em.find(User.class, id);
		em.getTransaction().commit();
		em.close();
		if(user != null)
		{
			JSONObject userJSON = new JSONObject();
			userJSON.put("id",user.getId());
			userJSON.put("timestamp", user.getTimestamp());
			userJSON.put("name", user.getName());
			userJSON.put("location", user.getLocation());
			
			return Response.status(200).entity(userJSON.toString()).build();
		}
		else
		{
			return Response.status(404).build();
		}
	}
	
	@GET
	@Path("/query/parking/{id}")
	public Response getUsersNearParkingGarage(@PathParam("id") String id)
	{
		JSONObject parkingJSON = ParkingService.getParkingJSON(id);
		if(parkingJSON == null)
		{
			return Response.status(404).build();
		}
		return searchForUsersDirectly(parkingJSON.getString("location"), 1000.0);
	}
	
	@GET
	@Path("/query/point")
	public Response searchForUsers(@QueryParam("point") String point, @QueryParam("radius") Double radius)
	{
		return searchForUsersDirectly(point, radius);
	}

	@SuppressWarnings("rawtypes")
	private Response searchForUsersDirectly(String point, Double radius) {
		EntityManager em = JPAUtil.createEntityManager();
		Query query = getSearchForUserQuery(point, radius, em);
	    List untypedResults = query.getResultList();
	    
	    return getUsersFromQuery(untypedResults);
	}
	@GET
	@Path("/{id}/location")
	public Response getLastUserReportedLocation(@PathParam("id") String id)
	{
		System.out.println("Getting user reported location for " + id);
		EntityManager em = JPAUtil.createEntityManager();
		em.getTransaction().begin();
		User user = em.find(User.class, id);
		em.getTransaction().commit();
		em.close();
		if(user != null)
		{
			JSONObject userJSON = new JSONObject();
			userJSON.put("timestamp", user.getTimestamp());
			userJSON.put("location", user.getLocation());
			return Response.status(200).entity(userJSON.toString()).build();
		}
		else
		{
			return Response.status(404).build();
		}
	}
	
	@POST
	@Path("/{id}/location")
	public Response updateUserLocation(@PathParam("id") String id, @FormParam("location") String locationWKT, @FormParam("timestamp") Long timestamp)
	{
		System.out.println("Updating user reported location for " + id);
		EntityManager em = JPAUtil.createEntityManager();
		em.getTransaction().begin();
		User user = em.find(User.class, id);
		if(user == null)
		{
			user = new User();
			user.setId(id);
		}
		user.setTimestamp(timestamp);
		user.setLocation((Point)ParkingManager.wktToGeometry(locationWKT));
		em.persist(user);
		em.getTransaction().commit();
		em.close();
		return Response.status(200).build();
	}
	
	private Query getSearchForUserQuery(String point, Double radius,
			EntityManager em) {
		Query query = em.createQuery("select u from User u where distance(u.location, :query_point) < :radius", User.class);
	    query.setParameter("query_point", ParkingManager.wktToGeometry(point));
	    query.setParameter("radius", radius);
		return query;
	}
	
	@SuppressWarnings("rawtypes")
	protected static Response getUsersFromQuery(List untypedResults) {
		JSONArray users = new JSONArray();
	    if(untypedResults != null)
	    {
	    	Iterator untypedResultsIterator = untypedResults.iterator();
	    	while(untypedResultsIterator.hasNext())
	    	{
			    Object o = untypedResultsIterator.next();
			    if(o != null && o instanceof User)
			    {
			    	User u =(User)o;	
					JSONObject user = translateUserToJSONObject(u);
					users.put(user);
					
					
			    }
	    	}
	    	if(users.length() > 0)
	    	{
	    		return Response.status(200).entity(users.toString()).build();
	    	}
	    	else
	    	{
	    		return Response.status(200).entity("[]").build();
	    	}
	    	
	    }
		return Response.status(404).build();
	}
	
	protected static JSONObject translateUserToJSONObject(User u) {
		JSONObject user = new JSONObject();
		user.put("id", u.getId());
		user.put("location", u.getLocation().toText());
		user.put("name", u.getName());
		user.put("timestamp", u.getTimestamp());
		return user;
	}
}
