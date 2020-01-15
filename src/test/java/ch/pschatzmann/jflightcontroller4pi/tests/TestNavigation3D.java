package ch.pschatzmann.jflightcontroller4pi.tests;

import static org.junit.Assert.assertEquals;

import org.junit.Assert;
import org.junit.Test;

import ch.pschatzmann.jflightcontroller4pi.guidence.navigation.CompassNavigation;
import ch.pschatzmann.jflightcontroller4pi.guidence.navigation.INavigation;
import ch.pschatzmann.jflightcontroller4pi.guidence.navigation.Navigation2D;
import ch.pschatzmann.jflightcontroller4pi.guidence.navigation.Navigation3D;
import ch.pschatzmann.jflightcontroller4pi.guidence.navigation.coordinates.Coordinate3D;
import ch.pschatzmann.jflightcontroller4pi.guidence.navigation.coordinates.Coordinate3DValue;

/*
 * Unit tests for the navigation using the 3D implementation
 */
public class TestNavigation3D {
	INavigation navigation = new Navigation3D();
	
	public TestNavigation3D(){
	}
	
	@Test
	public void testCoordinate() throws InterruptedException {
		Coordinate3DValue c = new Coordinate3DValue(40,26,45);
		int[] c3 = c.getDegMinSec();
		Assert.assertEquals(40.446, c.deg(),0.001);
		Assert.assertEquals(40, c3[0]);
		Assert.assertEquals(26, c3[1]);
		Assert.assertEquals(45, c3[2]);
	}
	
	@Test
	public void testHeading() throws InterruptedException {		
		Assert.assertEquals(90.00, navigation.getHeading(new Coordinate3D(0, 0), new Coordinate3D(0, 1)), 0.1);
		Assert.assertEquals(90.00, navigation.getHeading(new Coordinate3D(1, 1), new Coordinate3D(1, 2)), 0.1);
		Assert.assertEquals(0.00, navigation.getHeading(new Coordinate3D(0, 0), new Coordinate3D(1, 0)), 0.1);
		Assert.assertEquals(270.00, navigation.getHeading(new Coordinate3D(0, 0), new Coordinate3D(0, -1)), 0.1);
		Assert.assertEquals(270.00, navigation.getHeading(new Coordinate3D(1, 1), new Coordinate3D(1, 0)), 0.1);
		Assert.assertEquals(180.00, navigation.getHeading(new Coordinate3D(0, 0), new Coordinate3D(-1, 0)), 0.1);
	}
	
	@Test
	public void testDistance() throws InterruptedException {
		Assert.assertEquals(2.236, navigation.getDistance(new Coordinate3D(0, 0), new Coordinate3D(2, 1)), 0.001);		
		Assert.assertEquals(4.472, navigation.getDistance(new Coordinate3D(-2, -1), new Coordinate3D(2, 1)), 0.001);		
	}

	@Test
	public void testDestinationCoordinate() throws InterruptedException {		
		Coordinate3D start = new Coordinate3D(0,0);
		Coordinate3D result = new Coordinate3D();
		navigation.navigate(start,45, 6, result);
		
		Assert.assertEquals(4.242, result.getX(), 0.1);
		Assert.assertEquals(4.242, result.getY(), 0.1);
	}
	
	@Test
	public void testHome() throws InterruptedException {		
		Coordinate3D home = new Coordinate3D(0,0);
		Coordinate3D pos = new Coordinate3D(5,2);
		
		double dist = 5.38516;
		double heading = 201.8014;
		Assert.assertEquals(dist, navigation.getDistance(pos, home), 0.001);		
		Assert.assertEquals(heading, navigation.getHeading(pos, home), 0.1);

		Coordinate3D result = new Coordinate3D();
		navigation.navigate(pos, heading, dist, result);
		
		Assert.assertEquals(0, result.getY(), 0.1);
		Assert.assertEquals(0, result.getX(), 0.1);		
	}
	
	
	/**
	 * We fly for 10 seconds at 200 km/h with a heading of 10 deg. Then we ask
	 * for the return to home
	 * 
	 * @throws InterruptedException
	 */
	@Test
	public void testCompassNavigationSimulation() throws InterruptedException {
		CompassNavigation cn = new CompassNavigation();
		cn.setNavigation(navigation);
		cn.setHomePosition(new Coordinate3D(0, 0));
		// 200 kmh = 55.5556 m/sec * 10 sec = 550.55m = 0.555km 		
		cn.recordHeading(10, 200);
		Thread.sleep(1000 * 10);
		cn.recordHeading(10, 200);

		Assert.assertEquals(190, cn.getHomeBearing(), 10);
		Assert.assertEquals(0.555, cn.getHomeDistance(), 0.001);
		Assert.assertEquals(10, cn.getHomeTimeS(), 0.9);				

	}

}
