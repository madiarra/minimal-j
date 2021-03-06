package org.minimalj.util;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class CsvReaderTest {

	@Test
	public void testReadFile() throws Exception {
		CsvReader reader = reader("a,b\na,b");
		List<String> values = reader.readRecord();
		Assert.assertTrue(isAB(values));
		values = reader.readRecord();
		Assert.assertTrue(isAB(values));
	}
	
	@Test
	public void testReadRecord() throws Exception {
		CsvReader reader = reader("a,b");
		List<String> values = reader.readRecord();
		Assert.assertTrue(isAB(values));
		
		reader = reader("a,\"b\"");
		values = reader.readRecord();
		Assert.assertTrue(isAB(values));
	}
	
	private boolean isAB(List<String> strings) {
		if (strings.size() != 2) return false;
		return strings.get(0).equals("a") && strings.get(1).equals("b");
	}
	
	@Test
	public void testReadField() throws Exception {
		CsvReader reader = reader("\"ab\"");
		Assert.assertEquals("ab", reader.readField());
		
		reader = reader("\"ab\"\n");
		Assert.assertEquals("ab", reader.readField());

		reader = reader("\"ab\",");
		Assert.assertEquals("ab", reader.readField());
		
		reader = reader("ab");
		Assert.assertEquals("ab", reader.readField());
		
		reader = reader("ab\n");
		Assert.assertEquals("ab", reader.readField());
		
		reader = reader("ab,");
		Assert.assertEquals("ab", reader.readField());
	}
	
	
	@Test
	public void testReadEscaped() throws Exception {
		CsvReader reader = reader("\"ab\"");
		Assert.assertEquals("ab", reader.readEscaped());
		
		reader = reader("\"ab\"\n");
		Assert.assertEquals("ab", reader.readEscaped());

		reader = reader("\"ab\",");
		Assert.assertEquals("ab", reader.readEscaped());
	}
	
	@Test
	public void testReadNonEscaped() throws Exception {
		CsvReader reader = reader("ab");
		Assert.assertEquals("ab", reader.readNonEscaped());
		
		reader = reader("ab\n");
		Assert.assertEquals("ab", reader.readNonEscaped());
		
		reader = reader("ab,");
		Assert.assertEquals("ab", reader.readNonEscaped());
	}
	
	@Test
	public void testReadFields() throws Exception {
		CsvReader reader = reader("i,l,bd,s, ld\n142, 123456789012345, 2.1, s, " + LocalDate.of(2012, 3, 4));
		List<CsvReaderTestA> result = reader.readValues(CsvReaderTestA.class);
		Assert.assertEquals(1, result.size());
		Assert.assertEquals((long)142, (long) result.get(0).i);
		Assert.assertEquals(123456789012345L, (long) result.get(0).l);
		Assert.assertEquals(new BigDecimal("2.1"), result.get(0).bd);
		Assert.assertEquals("s", result.get(0).s);
		Assert.assertEquals(LocalDate.of(2012, 3, 4), result.get(0).ld);
	}
	
	private static CsvReader reader(String s) {
		ByteArrayInputStream bais = new ByteArrayInputStream(s.getBytes());
		return new CsvReader(bais);
	}
	
	public static class CsvReaderTestA {
		public Integer i;
		public Long l;
		public BigDecimal bd;
		public String s;
		public LocalDate ld;
	}
}
