package edu.usc.csci587.csp.db.data.util;
import javax.persistence.EntityManager;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

import edu.usc.csci587.csp.db.data.Parking;

public class ParkingManager {

    public static void main(String[] args) {
    	ParkingManager mgr = new ParkingManager();

        if (args[0].equals("store")) {
            mgr.createAndStoreParking("USC Parking Garage 3",  assemble(args));
        }
        JPAUtil.close();
    }

    private void createAndStoreParking(String title, String wktPoint) {
        Geometry geom = wktToGeometry(wktPoint);

        if (!geom.getGeometryType().equals("Point")) {
            throw new RuntimeException("Geometry must be a point. Got a " + geom.getGeometryType());
        }

        EntityManager em = JPAUtil.createEntityManager();

        em.getTransaction().begin();

        Parking parking = new Parking();
        parking.setName(title);
        parking.setLocation((Point) geom);
        em.persist(parking);
        em.getTransaction().commit();
        em.close();
    }

    private Geometry wktToGeometry(String wktPoint) {
        WKTReader fromText = new WKTReader(new GeometryFactory(new PrecisionModel(), 8307));
       // WKTReader fromText = new WKTReader();
        Geometry geom = null;
        try {
            geom = fromText.read(wktPoint);
            geom.setSRID(8307);
            
        } catch (ParseException e) {
            throw new RuntimeException("Not a WKT string:" + wktPoint);
        }
        return geom;
    }

    /**
     * Utility method to assemble all arguments save the first into a String
     */
    private static String assemble(String[] args) {
        StringBuilder builder = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            builder.append(args[i]).append(" ");
        }
        return builder.toString();
    }

}
