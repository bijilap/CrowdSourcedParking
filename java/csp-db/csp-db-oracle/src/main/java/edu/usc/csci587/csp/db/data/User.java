package edu.usc.csci587.csp.db.data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;

import com.vividsolutions.jts.geom.Point;

@Entity
@Table(name="CSP_USER")
public class User {
	@Id
    @GeneratedValue(generator="increment")
    @GenericGenerator(name="increment", strategy = "increment")
    private Long id;

    private String name;

    @Type(type="org.hibernate.spatial.GeometryType")
    private Point location;
    
    private Long timestamp; 
    
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

	public Point getLocation() {
		location.setSRID(8307);
		return location;
	}

	public void setLocation(Point location) {
		this.location = location;
	}

	public Long getTimestamp()
	{
		return timestamp;
	}
	
	public void setTimestamp(Long timestamp)
	{
		this.timestamp=timestamp;
	}

}
