package ch.openech.mj.db.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import junit.framework.Assert;

import org.junit.Test;

import ch.openech.mj.db.DbPersistence;
import ch.openech.mj.model.EnumUtils;

public class EnumSetTest {

	@Test
	public void testEnumToIntFirstElement() {
		Set<E> set = Collections.singleton(E.e0);
		testConversionAndDb(set);
	}

	void testConversionAndDb(Set<E> set) {
		Assert.assertTrue(test(set));
		Assert.assertTrue(testWithDb(set));
	}

	@Test
	public void testEnumToIntSomeElements() {
		Set<E> set = new HashSet<E>();
		set.add(E.e1);
		set.add(E.e5);
		set.add(E.e14);
		testConversionAndDb(set);
	}
	
	@Test
	public void testEnumToIntAllElements() {
		Set<E> set = new HashSet<E>();
		set.addAll(Arrays.asList(E.values()));
		testConversionAndDb(set);
	}

	@Test
	public void testEnumToIntNoElements() {
		Set<E> set = Collections.emptySet();
		testConversionAndDb(set);
	}

	@Test
	public void testEnumToIntLastElement() {
		Set<E> set = Collections.singleton(E.e31);
		testConversionAndDb(set);
	}
	
	private boolean test(Set<E> testSet) {
		int i = EnumUtils.getInt(testSet, E.class);
		Set<E> resultSet = new HashSet<>();
		EnumUtils.fillSet(i, E.class, resultSet);
		return compareSets(testSet, resultSet);
	}

	private boolean testWithDb(Set<E> testSet) {
		boolean ok = true;
		for (int i = 0; i<2; i++) {
			DbPersistence persistence = new DbPersistence(i == 0 ? DbPersistence.embeddedDataSource() : DbPersistence.mariaDbDataSource("OpenEch", "APP", "APP"), true);
			persistence.addClass(ObjectWithE.class);
			
			ObjectWithE object = new ObjectWithE();
			object.setOfE.addAll(testSet);
			int id = persistence.insert(object);
			
			ObjectWithE readObject = persistence.read(ObjectWithE.class, id);
			Set<E> resultSet = readObject.setOfE;
			
			ok &= compareSets(testSet, resultSet);
		}
		return ok;
	}

	private boolean compareSets(Set<E> testSet, Set<E> resultSet) {
		for (E e : E.values()) {
			boolean inTestSet = testSet.contains(e);
			boolean inResultSet = resultSet.contains(e);
			if (inTestSet != inResultSet) return false;
		}
		return true;
	}

	public static enum E {
		e0,  e1, e2, e3, e4, e5, e6, e7,
		e8, e9, e10, e11, e12, e13, e14, e15, 
		e16, e17, e18, e19, e20, e21, e22, e23,
		e24, e25, e26, e27, e28, e29, e30, e31;
	}
	
	public static class ObjectWithE {
		public final Set<E> setOfE = new HashSet<>();
	}
}