package org.minimalj;
import java.util.Locale;

import junit.framework.Assert;

import org.junit.Test;
import org.threeten.bp.Year;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.format.FormatStyle;
import org.threeten.bp.temporal.ChronoField;
import org.threeten.bp.temporal.TemporalAccessor;


public class LocalDateExampleTest {

	@Test
	public void testParseAndFormatISO() {
		TemporalAccessor date = DateTimeFormatter.ISO_DATE.parse("2012-11-03");
		Assert.assertEquals(2012, date.get(ChronoField.YEAR));
		Assert.assertEquals(11, date.get(ChronoField.MONTH_OF_YEAR));
		Assert.assertEquals(3, date.get(ChronoField.DAY_OF_MONTH));
		String dateString = DateTimeFormatter.ISO_DATE.format(date);
		Assert.assertEquals("2012-11-03", dateString);
	}
	
	@Test
	public void testParseAndFormatLocale() {
		Locale.setDefault(Locale.GERMAN);
		TemporalAccessor date = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).parse("03.11.2012");
		Assert.assertEquals(2012, date.get(ChronoField.YEAR));
		Assert.assertEquals(11, date.get(ChronoField.MONTH_OF_YEAR));
		Assert.assertEquals(3, date.get(ChronoField.DAY_OF_MONTH));
		String dateString = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).format(date);
		Assert.assertEquals("03.11.2012", dateString);

		dateString = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).format(date);
		Assert.assertEquals("03.11.2012", dateString);
	}
	
	@Test
	public void testParse() {
		Locale.setDefault(Locale.GERMAN);
		TemporalAccessor p = Year.of(2012);
		Assert.assertEquals(2012, p.get(ChronoField.YEAR));
		Assert.assertFalse(p.isSupported(ChronoField.MONTH_OF_YEAR));
	}

}
