package ch.openech.mj.edit.value;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import ch.openech.mj.edit.value.CloneHelper;

public class CloneHelperTest {

	@Test
	public void testClone() {
		CloneHelperTestA a = new CloneHelperTestA();
		a.a = "Hallo";
		a.t = "Transient";

		CloneHelperTestB b = new CloneHelperTestB();
		b.b = "Du";
		a.b.add(b);

		CloneHelperTestC c = new CloneHelperTestC();
		c.c = "Test";
		c.d = new CloneHelperTestD();
		c.d.d = 23;
		a.c.add(c);
		
		CloneHelperTestA a2 = CloneHelper.clone(a);

		// Prüfen ob richtig kopiert wurde
		Assert.assertEquals(a.a, a2.a);
		Assert.assertEquals("Transient values not cloned!", a.t, a2.t);
		
		Assert.assertEquals(a.b.size(), a2.b.size());
		Assert.assertEquals(a.b.get(0).b, a2.b.get(0).b);

		Assert.assertEquals(a.c.size(), a2.c.size());
		Assert.assertEquals(a.c.get(0).c, a2.c.get(0).c);
		Assert.assertEquals(a.c.get(0).d.d, a2.c.get(0).d.d);

		Assert.assertEquals(null, a2.empty);
	}
	
	public static class CloneHelperTestA {
		public String a;
		public final List<CloneHelperTestB> b = new ArrayList<CloneHelperTestB>();
		public List<CloneHelperTestC> c = new ArrayList<CloneHelperTestC>();
		public List<CloneHelperTestC> empty;
		public transient String t;
	}

	public static class  CloneHelperTestB {
		public String b;
	}

	public static class  CloneHelperTestC {
		public String c;
		public CloneHelperTestD d;
	}
	
	public static class  CloneHelperTestD {
		public Integer d;
	}

}
