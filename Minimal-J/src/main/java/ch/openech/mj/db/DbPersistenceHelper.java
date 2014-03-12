package ch.openech.mj.db;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.derby.client.am.Types;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.joda.time.ReadablePartial;

import ch.openech.mj.model.EnumUtils;
import ch.openech.mj.model.InvalidValues;
import ch.openech.mj.model.PropertyInterface;
import ch.openech.mj.util.DateUtils;
import ch.openech.mj.util.GenericUtils;

public class DbPersistenceHelper {
	public static final Logger sqlLogger = Logger.getLogger("SQL");

	private final DbPersistence dbPersistence;
	
	public DbPersistenceHelper(DbPersistence dbPersistence) {
		this.dbPersistence = dbPersistence;
	}
	
	public static boolean isReference(PropertyInterface property) {
		if (property.getFieldClazz().getName().startsWith("java")) return false;
		if (property.getFieldClazz().getName().startsWith("org.joda")) return false;
		if (Enum.class.isAssignableFrom(property.getFieldClazz())) return false;
		return true;
	}
	
	public void setParameter(PreparedStatement preparedStatement, int param, Object value, PropertyInterface property) throws SQLException {
		if (value == null) {
			setParameterNull(preparedStatement, param, property.getFieldClazz());
		} else {
			if (value instanceof Enum<?>) {
				Enum<?> e = (Enum<?>) value;
				if (!InvalidValues.isInvalid(e)) {
					value = e.ordinal();
				} else {
					setParameterNull(preparedStatement, param, property.getFieldClazz());
					return;
				}
			} else if (value instanceof LocalDate) {
				value = new java.sql.Date(((LocalDate) value).toDate().getTime());
			} else if (value instanceof LocalTime) {
				value = new java.sql.Time(((LocalTime) value).toDateTimeToday().getMillis());
			} else if (value instanceof LocalDateTime) {
				value = new java.sql.Timestamp(((LocalDateTime) value).toDate().getTime());
			} else if (value instanceof ReadablePartial) {
				value = DateUtils.formatPartial((ReadablePartial) value);
			} else if (value instanceof Set<?>) {
				Set<?> set = (Set<?>) value;
				Class<?> enumClass = GenericUtils.getGenericClass(property.getType());
				value = EnumUtils.getInt(set, enumClass);
			}
			preparedStatement.setObject(param, value);
		} 
	}

	public void setParameterNull(PreparedStatement preparedStatement, int param, Class<?> clazz) throws SQLException {
		if (clazz == String.class) {
			preparedStatement.setNull(param, Types.VARCHAR);
		} else if (clazz == Integer.class) {
			preparedStatement.setNull(param, Types.INTEGER);
		} else if (clazz == Boolean.class) {
			preparedStatement.setNull(param, Types.INTEGER);
		} else if (clazz == BigDecimal.class) {
			preparedStatement.setNull(param, Types.DECIMAL);
		} else if (Enum.class.isAssignableFrom(clazz)) {
			preparedStatement.setNull(param, Types.INTEGER);
		} else if (clazz == LocalDate.class) {
			preparedStatement.setNull(param, Types.DATE);
		} else if (clazz == LocalTime.class) {
			preparedStatement.setNull(param, Types.TIME);
		} else if (clazz == LocalDateTime.class) {
			preparedStatement.setNull(param, Types.DATE);
		} else if (clazz == ReadablePartial.class) {
			preparedStatement.setNull(param, Types.CHAR);
		} else if (dbPersistence.getTable(clazz) != null) {
			preparedStatement.setNull(param, Types.INTEGER);
		} else {
			throw new IllegalArgumentException(clazz.getSimpleName());
		}
	}
	
	@SuppressWarnings("unchecked")
	protected Object convertToFieldClass(Class<?> fieldClass, Object value) {
		if (value == null) return null;
		
		if (fieldClass == LocalDate.class) {
			if (value instanceof java.sql.Date) {
				value = new LocalDate((java.sql.Date) value);
			} else {
				throw new IllegalArgumentException(value.getClass().getSimpleName());
			}
		} else if (fieldClass == LocalTime.class) {
			if (value instanceof java.sql.Time) {
				value = new LocalTime((java.sql.Time) value);
			} else {
				throw new IllegalArgumentException(value.getClass().getSimpleName());
			}
		} else if (fieldClass == LocalDateTime.class) {
			if (value instanceof java.sql.Timestamp) {
				value = new LocalDateTime((java.sql.Timestamp) value);
			} else if (value instanceof java.sql.Date) {
				value = new LocalDateTime((java.sql.Date) value);
			} else {
				throw new IllegalArgumentException(value.getClass().getSimpleName());
			}
		} else if (fieldClass == ReadablePartial.class) {
			if (value instanceof String) {
				String text = ((String) value).trim(); // cut the spaces from CHAR - Column
				value = DateUtils.parsePartial(text);
			} else if (value instanceof java.sql.Date) {
				// this should not happen, but is maybe usefull for migrating a DB
				value = new LocalDate(((java.sql.Date) value).getTime());
			} else if (value != null) {
				throw new IllegalArgumentException(value.getClass().getSimpleName());
			}
		} else if (fieldClass == Boolean.class) {
			if (value instanceof Boolean) {
				return (Boolean) value;
			} else if (value instanceof Integer) {
				value = Boolean.valueOf(((int) value) == 1);
			} else if (value != null) {
				throw new IllegalArgumentException(value.getClass().getSimpleName());
			}
		} else if (Enum.class.isAssignableFrom(fieldClass)) {
			value = EnumUtils.valueList((Class<Enum>)fieldClass).get((Integer) value);
		}
		return value;
	}

}
