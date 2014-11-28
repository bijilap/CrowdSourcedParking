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
		Parking parking = translateJSONObjectToParking(parkingAsJSON);
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
			parking = translateJSONObjectToParking(parkingAsJSON);
		}
		else
		{
			parking = mergeJSONObjectToParking(parking, parkingAsJSON);
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
	
		JSONObject parkingGarage = getParkingJSON(id);
		if(parkingGarage != null)
		{
			return Response.status(200).entity(parkingGarage.toString()).build();
		}
		return Response.status(404).build();
	}

	public static JSONObject getParkingJSON(String id)
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
			return translateParkingToJSONObject(p);
		}
		return null;
		
	}
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/query/name")
	@SuppressWarnings("rawtypes")
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
	@SuppressWarnings("rawtypes")
	public Response searchForParkingGarages(@QueryParam("point") String point, @QueryParam("radius") Double radius)
	{
		System.out.println("looking in " + point + " with radius " + radius );
		
		EntityManager em = JPAUtil.createEntityManager();
		Query query = getSearchForParkingGarageQuery(point, radius, em);
	    List untypedResults = query.getResultList();
	    
	    return getParkingGaragesFromQuery(untypedResults);
	}
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/query/nearest")
	@SuppressWarnings("rawtypes")
	public Response searchForNearestParkingGarages(@QueryParam("count") Integer count, @QueryParam("userid") String userId)
	{
		System.out.println("looking in for " + count + " parking garages near userid " + userId );
		
		EntityManager em = JPAUtil.createEntityManager();
		Query query = getSearchForNearestParkingGarageQuery(count, userId, em);
	    List untypedResults = query.getResultList();
	    
	    return getParkingGaragesFromQuery(untypedResults);
	}
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/query/cheapest")
	@SuppressWarnings("rawtypes")
	public Response searchForCheapestParkingGarages(@QueryParam("hours") Double hours, @QueryParam("userid") String userId)
	{
		System.out.println("looking for cheapest parking garages near userid " + userId );
		
		EntityManager em = JPAUtil.createEntityManager();
		Query query = getSearchForCheapestParkingGarageQuery(hours, userId, em);
	    List untypedResults = query.getResultList();
	    
	    return getParkingGaragesFromQuery(untypedResults);
	}
	
	private Query getSearchForCheapestParkingGarageQuery( Double hours, String userId,
			EntityManager em) {
		Query query = em.createNativeQuery("select p.*, case when (p.pricepermin*:hours*60 <p.priceperhour* :hours and :hours> 1 or p.pricepermin*:hours*60 < p.priceperhour) and p.pricepermin*:hours*60 < p.priceperday then p.pricepermin*:hours*60 when (p.priceperhour * :hours < p.priceperday and :hours> 1) then p.priceperhour*:hours else p.priceperday end as price from Parking p where SDO_WITHIN_DISTANCE(p.location, (select u.location from csp_user u where u.id=:userid), 'distance = 2000') = 'TRUE' order by price", Parking.class);
	    query.setParameter("hours", hours);
	    query.setParameter("userid", userId);
		return query;
	}
	
	private Query getSearchForParkingGarageByNameQuery(String point, Double radius, String name,
			EntityManager em) {
		Query query = em.createQuery("select p from Parking p where p.name=:name and distance(p.location, :query_point) < :radius", Parking.class);
	    query.setParameter("query_point", ParkingManager.wktToGeometry(point));
	    query.setParameter("radius", radius);
	    query.setParameter("name", name);
		return query;
	}
	private Query getSearchForNearestParkingGarageQuery( Integer count, String userId,
			EntityManager em) {
		Query query = em.createNativeQuery("select * from Parking p where SDO_NN( p.location, (select u.location from csp_user u where u.id=:userid), 'sdo_num_res=' || TO_CHAR(:count)) = 'TRUE'", Parking.class);
	    query.setParameter("count", count);
	    query.setParameter("userid", userId);
		return query;
	}
	
	private Query getSearchForParkingGarageQuery(String point, Double radius,
			EntityManager em) {
		Query query = em.createQuery("select p from Parking p where distance(p.location, :query_point) < :radius", Parking.class);
	    query.setParameter("query_point", ParkingManager.wktToGeometry(point));
	    query.setParameter("radius", radius);
		return query;
	}
	
	@SuppressWarnings("rawtypes")
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
	    	if(parkingGarages.length() > 0)
	    	{
	    		return Response.status(200).entity(parkingGarages.toString()).build();
	    	}
	    	else
	    	{
	    		return Response.status(200).entity("[]").build();
	    	}
	    	
	    }
		return Response.status(404).build();
	}
	
	protected static Parking translateJSONObjectToParking(JSONObject o)
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

	protected static Parking mergeJSONObjectToParking(Parking parking, JSONObject parkingAsJSON) {
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
	
	protected static JSONObject translateParkingToJSONObject(Parking p) {
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
