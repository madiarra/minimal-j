package org.minimalj.backend.db;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.minimalj.backend.Persistence.Return;
import org.minimalj.backend.sql.SqlPersistence;

public class SqlEnumTest {
	
	private static SqlPersistence persistence;
	
	@BeforeClass
	public static void setupPersistence() {
		persistence = new SqlPersistence(SqlPersistence.embeddedDataSource(), F.class);
	}
	
	@AfterClass
	public static void shutdownPersistence() {
	}
	
	@Test
	public void testCrudDates() {
		F f = new F();
		f.fenum.add(FEnum.element2);
		f.fenum.add(FEnum.element3);
		
		F f2 = persistence.insert(f, Return.COMPLETE);

		//
		
		Assert.assertEquals(f.fenum.size(), f2.fenum.size());
		Assert.assertFalse(f2.fenum.contains(FEnum.element1));
		Assert.assertTrue(f2.fenum.contains(FEnum.element2));
		Assert.assertTrue(f2.fenum.contains(FEnum.element3));
		
		f2.fenum.remove(FEnum.element2);
		F f3 = persistence.update(f2, Return.COMPLETE);
		
		Assert.assertFalse(f3.fenum.contains(FEnum.element1));
		Assert.assertFalse(f3.fenum.contains(FEnum.element2));
		Assert.assertTrue(f3.fenum.contains(FEnum.element3));
	}

}