package edu.usc.csci587.csp.rest;

import java.util.Iterator;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.JSONArray;
import org.json.JSONObject;

import edu.usc.csci587.csp.db.data.Parking;
import edu.usc.csci587.csp.db.data.util.JPAUtil;
import edu.usc.csci587.csp.db.data.util.ParkingManager;

@Path("/parking")
public class ParkingService {
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{id}")
	public Response getParking(@PathParam("id") String id)
	{
	
		EntityManager em = JPAUtil.createEntityManager();
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Parking> criteria = cb.createQuery(Parking.class);
		Root<Parking> parkingRoot  = criteria.from(Parking.class);
		criteria.select(parkingRoot);
		criteria.where(cb.equal(parkingRoot.get(parkingRoot.getModel().getSingularAttribute("id")), id));
		TypedQuery<Parking> query = em.createQuery(criteria);
		List<Parking> results = query.getResultList();
		Parking p = results.iterator().next();
		if(p != null)
		{
		JSONObject parkingGarage = translateParkingToJSONObject(p);
		
		return Response.status(200).entity(parkingGarage.toString()).build();
		}
		return Response.status(404).build();
	}
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/query/polygon")
	public Response searchForParkingGarages(@QueryParam("polygon") String polygon)
	{
		System.out.println("looking in " + polygon);
		JSONObject parkingGarage = new JSONObject();
		parkingGarage.put("id", 1);
		return Response.status(200).entity(parkingGarage.toString()).build();
	}
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/query/point")
	public Response searchForParkingGarages(@QueryParam("point") String point, @QueryParam("radius") Double radius)
	{
		System.out.println("looking in " + point + " with radius " + radius );
		
		EntityManager em = JPAUtil.createEntityManager();
		Query query = em.createQuery("select p from Parking p where distance(p.location, :query_point) < :radius", Parking.class);
	    query.setParameter("query_point", ParkingManager.wktToGeometry(point));
	    query.setParameter("radius", radius);
	    List untypedResults = query.getResultList();
	    
	    JSONArray parkingGarages = new JSONArray();
	    if(untypedResults != null)
	    {
	    	Iterator untypedResultsIterator = untypedResults.iterator();
	    	while(untypedResultsIterator.hasNext())
	    	{
			    Object o = untypedResultsIterator.next();
			    if(o != null && o instanceof Parking)
			    {
			    	Parking p =(Parking)o;	
					JSONObject parkingGarage = translateParkingToJSONObject(p);
					parkingGarages.put(parkingGarage);
					
					
			    }
	    	}
	    	if(parkingGarages.length() > 1)
	    	{
	    		return Response.status(200).entity(parkingGarages.toString()).build();
	    	}
	    	else if(parkingGarages.length() == 1)
	    	{
	    		return Response.status(200).entity(parkingGarages.get(0).toString()).build(); 
	    	}
	    	else
	    	{
	    		return Response.status(200).entity("[]").build();
	    	}
	    	
	    }
		return Response.status(404).build();
	}

	private JSONObject translateParkingToJSONObject(Parking p) {
		JSONObject parkingGarage = new JSONObject();
		parkingGarage.put("id", p.getId());
		parkingGarage.put("location", p.getLocation().toText());
		parkingGarage.put("name", p.getName());
		return parkingGarage;
	}
}
