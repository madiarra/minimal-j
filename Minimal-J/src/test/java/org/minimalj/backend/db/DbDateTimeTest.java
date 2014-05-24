package org.minimalj.backend.db;

import java.sql.SQLException;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.minimalj.backend.db.DbPersistence;
import org.minimalj.util.DateUtils;

public class DbDateTimeTest {
	
	private static DbPersistence persistence;
	
	@BeforeClass
	public static void setupDb() throws SQLException {
		persistence = new DbPersistence(DbPersistence.embeddedDataSource(), D.class);
	}
	
	@AfterClass
	public static void shutdownDb() throws SQLException {
	}
	
	@Test
	public void testCrudDates() throws SQLException {
		D d = new D();
		d.localDate = new LocalDate(2000, 01, 02);
		d.localTime = new LocalTime(12, 34, 56);
		d.localDateTime = new LocalDateTime(2001, 02, 03, 10, 20, 30);
		
		long id = persistence.insert(d);

		//
		
		D d2 = persistence.read(D.class, id);
		Assert.assertEquals(d.localDate, d2.localDate);
		Assert.assertEquals(d.localTime, d2.localTime);
		Assert.assertEquals(d.localDateTime, d2.localDateTime);
	}

	@Test
	public void testCrudPartials() throws SQLException {
		D d = new D();
		d.p1 = DateUtils.newPartial("2012");
		d.p2 = DateUtils.newPartial("2012", "10");
		d.p3 = DateUtils.newPartial("2012", "9", "8");
		
		long id = persistence.insert(d);

		//
		
		D d2 = persistence.read(D.class, id);
		Assert.assertEquals(d.p1, d2.p1);
		Assert.assertEquals(d.p2, d2.p2);
		Assert.assertEquals(d.p3, d2.p3);
		
		// update
		
		d2.p1 = DateUtils.newPartial("998");
		d2.p2 = DateUtils.newPartial("997", "8");
		d2.p3 = DateUtils.newPartial("92", "10", "2");
		
		persistence.update(d2);
		
		D d3 = persistence.read(D.class, id);

		Assert.assertEquals(d2.p1, d3.p1);
		Assert.assertEquals(d2.p2, d3.p2);
		Assert.assertEquals(d2.p3, d3.p3);
	}

}