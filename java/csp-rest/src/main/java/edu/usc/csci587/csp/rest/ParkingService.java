package edu.usc.csci587.csp.rest;

import java.util.Iterator;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.usc.csci587.csp.db.data.Parking;
import edu.usc.csci587.csp.db.data.util.JPAUtil;
import edu.usc.csci587.csp.db.data.util.ParkingManager;

@Path("/parking")
public class ParkingService {
	
	private static Logger LOG = LoggerFactory.getLogger(ParkingService.class);
	
	@DELETE
	@Path("/{id}")
	public Response delete(@PathParam("id") String id)
	{
		EntityManager em = JPAUtil.createEntityManager();
		em.getTransaction().begin();
		Parking parking = em.find(Parking.class, Long.parseLong(id));
		
		if(parking != null)
		{
			em.remove(parking);
			em.getTransaction().commit();
			em.close();
			return Response.status(204).build();
		}
		em.getTransaction().commit();
		em.close();
		return Response.status(404).build();
		
	}
	@POST
	@Path("/report")
	public Response reportParking(String parkingAsJSONString)
	{
		LOG.info("Creating entry for user reported parking");
		JSONObject parkingAsJSON = new JSONObject(parkingAsJSONString);
		EntityManager em = JPAUtil.createEntityManager();
		em.getTransaction().begin();
		Parking parking = this.translateJSONObjectToParking(parkingAsJSON);
		em.persist(parking);
		em.getTransaction().commit();
		em.close();
		LOG.info("Creating entry for user reported parking with id " + parking.getId());
		return Response.status(200).build();
	}
	
	@POST
	@Path("/{id}")
	public Response putParking(@PathParam("id") String id, String parkingAsJSONString)
	{
		LOG.info("Creating entry for user reported parking " + id);
		JSONObject parkingAsJSON = new JSONObject(parkingAsJSONString);
		EntityManager em = JPAUtil.createEntityManager();
		em.getTransaction().begin();
		Parking parking = em.find(Parking.class, Long.parseLong(id));
		if(parking == null)
		{
			parking = this.translateJSONObjectToParking(parkingAsJSON);
		}
		else
		{
			parking = this.mergeJSONObjectToParking(parking, parkingAsJSON);
		}
		
		em.persist(parking);
		em.getTransaction().commit();
		em.close();
		return Response.status(200).build();
	}
	
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
		criteria.where(cb.equal(parkingRoot.get(parkingRoot.getModel().getSingularAttribute("id")), Long.parseLong(id)));
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
	@Path("/query/name")
	public Response getParkingByName(@QueryParam("name") String name, @QueryParam("point") String point, @QueryParam("radius") Double radius)
	{
	
		EntityManager em = JPAUtil.createEntityManager();
		Query query = getSearchForParkingGarageByNameQuery(point, radius, name, em);
		
	    List untypedResults = query.getResultList();
	    return getParkingGaragesFromQuery(untypedResults);
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
		Query query = getSearchForParkingGarageQuery(point, radius, em);
	    List untypedResults = query.getResultList();
	    
	    return getParkingGaragesFromQuery(untypedResults);
	}
	
	private Query getSearchForParkingGarageByNameQuery(String point, Double radius, String name,
			EntityManager em) {
		Query query = em.createQuery("select p from Parking p where p.name=:name and distance(p.location, :query_point) < :radius", Parking.class);
	    query.setParameter("query_point", ParkingManager.wktToGeometry(point));
	    query.setParameter("radius", radius);
	    query.setParameter("name", name);
		return query;
	}
	
	private Query getSearchForParkingGarageQuery(String point, Double radius,
			EntityManager em) {
		Query query = em.createQuery("select p from Parking p where distance(p.location, :query_point) < :radius", Parking.class);
	    query.setParameter("query_point", ParkingManager.wktToGeometry(point));
	    query.setParameter("radius", radius);
		return query;
	}
	private Response getParkingGaragesFromQuery(List untypedResults) {
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
	
	private Parking translateJSONObjectToParking(JSONObject o)
	{
		Parking parking = new Parking();
		if(o.has("id"))
		{
			parking.setId(Long.parseLong(o.getString("id")));
		}
		if(o.has("name"))
		{
			parking.setName(o.getString("name"));
		}
		if(o.has("location"))
		{
			parking.setLocation(ParkingManager.wktToGeometry(o.getString("location")));
		}
		if(o.has("capacity"))
		{
			parking.setCapacity(o.getLong("capacity"));
		}
		if(o.has("pricePerDay"))
		{
			parking.setPricePerDay(o.getDouble("pricePerDay"));
		}
		if(o.has("pricePerHour"))
		{
			parking.setPricePerHour(o.getDouble("pricePerHour"));
		}
		if(o.has("pricePerMin"))
		{
			parking.setPricePerMin(o.getDouble("pricePerMin"));
		}
		return parking;
	}

	private Parking mergeJSONObjectToParking(Parking parking, JSONObject parkingAsJSON) {
		Parking tempParking = translateJSONObjectToParking(parkingAsJSON);
		if(tempParking.getAverageTime() != null)
		{
			parking.setAverageTime(tempParking.getAverageTime());
		}
		if(tempParking.getCapacity() != null)
		{
			parking.setCapacity(tempParking.getCapacity());
		}
		if(tempParking.getLocation() != null)
		{
			parking.setLocation(tempParking.getLocation());
		}
		if(tempParking.getName() != null)
		{
			parking.setName(tempParking.getName());
		}
		if(tempParking.getPricePerDay() != null)
		{
			parking.setPricePerDay(tempParking.getPricePerDay());
		}
		if(tempParking.getPricePerHour() != null)
		{
			parking.setPricePerHour(tempParking.getPricePerHour());
		}
		if(tempParking.getPricePerMin() != null)
		{
			parking.setPricePerMin(tempParking.getPricePerMin());
		}
		return parking;
	}
	
	private JSONObject translateParkingToJSONObject(Parking p) {
		JSONObject parkingGarage = new JSONObject();
		parkingGarage.put("id", p.getId());
		parkingGarage.put("location", p.getLocation().toText());
		parkingGarage.put("name", p.getName());
		parkingGarage.put("averagetime", p.getAverageTime());
		parkingGarage.put("capacity", p.getCapacity());
		parkingGarage.put("pricePerDay", p.getPricePerDay());
		parkingGarage.put("pricePerHour", p.getPricePerHour());
		parkingGarage.put("pricePerMin", p.getPricePerMin());
		return parkingGarage;
	}
}
