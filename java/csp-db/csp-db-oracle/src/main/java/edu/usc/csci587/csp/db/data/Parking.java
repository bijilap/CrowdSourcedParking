package edu.usc.csci587.csp.db.data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;

import com.vividsolutions.jts.geom.Geometry;

@Entity
public class Parking {
   
	@Id
    @GeneratedValue(generator="increment")
    @GenericGenerator(name="increment", strategy = "increment")
    private Long id;
	
	private String name;
    
    @Type(type="org.hibernate.spatial.GeometryType")
    private Geometry location;
    
    private Long averagetime;
    private Long capacity;
    private Double pricepermin;
    private Double priceperhour;
    private Double priceperday;
    
    public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Geometry getLocation() {
		location.setSRID(8307);
		return location;
	}

	public void setLocation(Geometry location) {
		this.location = location;
	}
	public Long getAverageTime() {
		return averagetime;
	}

	public void setAverageTime(Long averagetime) {
		this.averagetime = averagetime;
	}

	public Long getCapacity() {
		return capacity;
	}

	public void setCapacity(Long capacity) {
		this.capacity = capacity;
	}

	public Double getPricePerMin() {
		return pricepermin;
	}

	public void setPricePerMin(Double pricepermin) {
		this.pricepermin = pricepermin;
	}

	public Double getPricePerHour() {
		return priceperhour;
	}

	public void setPricePerHour(Double priceperhour) {
		this.priceperhour = priceperhour;
	}

	public Double getPricePerDay() {
		return priceperday;
	}

	public void setPricePerDay(Double priceperday) {
		this.priceperday = priceperday;
	}
}
