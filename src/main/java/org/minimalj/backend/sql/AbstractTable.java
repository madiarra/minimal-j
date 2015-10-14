package org.minimalj.backend.sql;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.minimalj.model.Code;
import org.minimalj.model.Keys;
import org.minimalj.model.View;
import org.minimalj.model.ViewUtil;
import org.minimalj.model.annotation.NotEmpty;
import org.minimalj.model.properties.ChainedProperty;
import org.minimalj.model.properties.FieldProperty;
import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.transaction.criteria.FieldOperator;
import org.minimalj.util.EqualsHelper;
import org.minimalj.util.FieldUtils;
import org.minimalj.util.IdUtils;
import org.minimalj.util.LoggingRuntimeException;
import org.minimalj.util.StringUtils;

/**
 * Minimal-J internal<p>
 *
 * Base class of all table representing classes in this persistence layer.
 * Normally you should not need to extend from this class directly. Use
 * the existing subclasses or only the methods in SqlPersistence.
 * 
 */
public abstract class AbstractTable<T> {
	public static final Logger sqlLogger = Logger.getLogger("SQL");
	
	protected final SqlPersistence sqlPersistence;
	protected final SqlHelper helper;
	protected final Class<T> clazz;
	protected final LinkedHashMap<String, PropertyInterface> columns;
	protected final LinkedHashMap<String, PropertyInterface> lists;
	
	protected final String name;

	protected final PropertyInterface idProperty;

	protected final List<String> indexes = new ArrayList<>();
	
	protected final Map<Connection, Map<String, PreparedStatement>> statements = new HashMap<>();

	protected final String selectByIdQuery;
	protected final String insertQuery;
	protected final String clearQuery;
	
	// TODO: its a little bit strange to pass the idProperty here. Also because the property
	// is not allways a property of clazz. idProperty is only necessary because the clazz AND the
	// size of the idProperty is needed
	protected AbstractTable(SqlPersistence sqlPersistence, String name, Class<T> clazz, PropertyInterface idProperty) {
		this.sqlPersistence = sqlPersistence;
		this.helper = new SqlHelper(sqlPersistence);
		this.name = buildTableName(sqlPersistence, name != null ? name : StringUtils.toSnakeCase(clazz.getSimpleName()));
		this.clazz = clazz;
		this.idProperty = idProperty;
		this.columns = sqlPersistence.findColumns(clazz);
		this.lists = findLists(clazz);
		
		this.selectByIdQuery = selectByIdQuery();
		this.insertQuery = insertQuery();
		this.clearQuery = clearQuery();
		
		findCodes();
		findDependables();
		findIndexes();
	}
	
	public static String buildTableName(SqlPersistence persistence, String name) {
		name = SqlHelper.buildName(name, persistence.getMaxIdentifierLength(), persistence.getTableNames());

		// the persistence adds the table name too late. For subtables it's important
		// to add the table name here. Note that tableNames is a Set. Multiple
		// adds don't do any harm.
		persistence.getTableNames().add(name);
		return name;
	}
	
	protected static LinkedHashMap<String, PropertyInterface> findLists(Class<?> clazz) {
		LinkedHashMap<String, PropertyInterface> properties = new LinkedHashMap<String, PropertyInterface>();
		
		for (Field field : clazz.getFields()) {
			if (!FieldUtils.isPublic(field) || FieldUtils.isStatic(field) || FieldUtils.isTransient(field)) continue;
			if (FieldUtils.isFinal(field) && !FieldUtils.isList(field)) {
				// This is needed to check if an inline Property contains a List
				Map<String, PropertyInterface> inlinePropertys = findLists(field.getType());
				boolean hasClassName = FieldUtils.hasClassName(field);
				for (String inlineKey : inlinePropertys.keySet()) {
					String key = inlineKey;
					if (!hasClassName) {
						key = field.getName() + StringUtils.upperFirstChar(inlineKey);
					}
					properties.put(key, new ChainedProperty(clazz, field, inlinePropertys.get(inlineKey)));
				}
			} else if (FieldUtils.isList(field)) {
				properties.put(field.getName(), new FieldProperty(field));
			}
		}
		return properties; 
	}
	
	protected LinkedHashMap<String, PropertyInterface> getColumns() {
		return columns;
	}

	protected LinkedHashMap<String, PropertyInterface> getLists() {
		return lists;
	}
	
	protected Collection<String> getIndexes() {
		return indexes;
	}
	
	protected PreparedStatement getStatement(Connection connection, String query, boolean returnGeneratedKeys) throws SQLException {
		if (!statements.containsKey(connection)) {
			statements.put(connection, new HashMap<String, PreparedStatement>());
		}
		Map<String, PreparedStatement> statementsForConnection = statements.get(connection);
		if (!statementsForConnection.containsKey(query)) {
			statementsForConnection.put(query, createStatement(connection, query, returnGeneratedKeys));
		}
		return statementsForConnection.get(query);
	}
	
	static PreparedStatement createStatement(Connection connection, String query, boolean returnGeneratedKeys) throws SQLException {
		int autoGeneratedKeys = returnGeneratedKeys ? Statement.RETURN_GENERATED_KEYS : Statement.NO_GENERATED_KEYS;
		if (sqlLogger.isLoggable(Level.FINE)) {
			return new LoggingPreparedStatement(connection, query, autoGeneratedKeys, sqlLogger);
		} else {
			return connection.prepareStatement(query, autoGeneratedKeys);
		}
	}
	
	protected void execute(String s) {
		try (PreparedStatement statement = createStatement(sqlPersistence.getConnection(), s.toString(), false)) {
			statement.execute();
		} catch (SQLException x) {
			throw new LoggingRuntimeException(x, sqlLogger, "Statement failed: \n" + s.toString());
		}
	}

	protected void createTable(SqlSyntax syntax) {
		StringBuilder s = new StringBuilder();
		syntax.addCreateStatementBegin(s, getTableName());
		addSpecialColumns(syntax, s);
		addFieldColumns(syntax, s);
		addPrimaryKey(syntax, s);
		syntax.addCreateStatementEnd(s);
		
		execute(s.toString());
	}
	
	protected abstract void addSpecialColumns(SqlSyntax syntax, StringBuilder s);
	
	protected void addFieldColumns(SqlSyntax syntax, StringBuilder s) {
		for (Map.Entry<String, PropertyInterface> column : getColumns().entrySet()) {
			s.append(",\n ").append(column.getKey()).append(' '); 

			PropertyInterface property = column.getValue();
			syntax.addColumnDefinition(s, property);
			boolean isNotEmpty = property.getAnnotation(NotEmpty.class) != null;
			s.append(isNotEmpty ? " NOT NULL" : " DEFAULT NULL");
		}
	}

	protected void addPrimaryKey(SqlSyntax syntax, StringBuilder s) {
		syntax.addPrimaryKey(s, "ID");
	}
	
	protected void createIndexes(SqlSyntax syntax) {
		for (String index : indexes) {
			String s = syntax.createIndex(getTableName(), index, this instanceof HistorizedTable);
			execute(s.toString());
		}
	}
	
	protected void createConstraints(SqlSyntax syntax) {
		for (Map.Entry<String, PropertyInterface> column : getColumns().entrySet()) {
			PropertyInterface property = column.getValue();
			
			if (SqlHelper.isDependable(property) || ViewUtil.isReference(property)) {
				Class<?> fieldClass = ViewUtil.resolve(property.getClazz());
				AbstractTable<?> referencedTable = sqlPersistence.getAbstractTable(fieldClass);

				String s = syntax.createConstraint(getTableName(), column.getKey(), referencedTable.getTableName(), referencedTable instanceof HistorizedTable);
				if (s != null) {
					execute(s.toString());
				}
			}
		}
	}
	
	public void clear() {
		try {
			PreparedStatement statement = getStatement(sqlPersistence.getConnection(), clearQuery, false);
			statement.execute();
		} catch (SQLException x) {
			throw new LoggingRuntimeException(x, sqlLogger, "Clear of Table " + getTableName() + " failed");
		}
	}

	private String findColumn(String fieldPath) {
		for (Map.Entry<String, PropertyInterface> entry : columns.entrySet()) {
			if (entry.getValue().getPath().equals(fieldPath)) {
				return entry.getKey();
			}
		}
		return null;
	}

	public String column(PropertyInterface property) {
		return findColumn(property.getPath());
	}
	
	public String column(Object key) {
		return column(Keys.getProperty(key));
	}

	protected String getTableName() {
		return name;
	}
	
	public Class<T> getClazz() {
		return clazz;
	}
	
	private void findCodes() {
		for (Map.Entry<String, PropertyInterface> column : getColumns().entrySet()) {
			PropertyInterface property = column.getValue();
			Class<?> fieldClazz = property.getClazz();
			if (Code.class.isAssignableFrom(fieldClazz) && fieldClazz != clazz) {
				sqlPersistence.addClass(fieldClazz);
			}
		}
	}
	
	private void findDependables() {
		for (Map.Entry<String, PropertyInterface> column : getColumns().entrySet()) {
			PropertyInterface property = column.getValue();
			Class<?> fieldClazz = property.getClazz();
			if (SqlHelper.isDependable(property) && fieldClazz != clazz) {
				if (!View.class.isAssignableFrom(property.getClazz())) {
					sqlPersistence.addClass(fieldClazz);
				}
			}
		}
	}

	protected void findIndexes() {
		for (Map.Entry<String, PropertyInterface> column : columns.entrySet()) {
			PropertyInterface property = column.getValue();
			if (ViewUtil.isReference(property)) {
				createIndex(property, property.getPath());
			}
		}
	}
	
	protected String whereStatement(final String wholeFieldPath, FieldOperator criteriaOperator) {
		String fieldPath = wholeFieldPath;
		String column;
		while (true) {
			column = findColumn(fieldPath);
			if (column != null) break;
			int pos = fieldPath.lastIndexOf('.');
			if (pos < 0) throw new IllegalArgumentException("FieldPath " + wholeFieldPath + " not even partially found in " + getTableName());
			fieldPath = fieldPath.substring(0, pos);
		}
		if (fieldPath.length() < wholeFieldPath.length()) {
			String restOfFieldPath = wholeFieldPath.substring(fieldPath.length() + 1);
			if ("id".equals(restOfFieldPath)) {
				return column + " " + criteriaOperator.getOperatorAsString() + " ?";
			} else {
				PropertyInterface subProperty = columns.get(column);
				AbstractTable<?> subTable = sqlPersistence.getAbstractTable(ViewUtil.resolve(subProperty.getClazz()));
				return column + " = (select ID from " + subTable.getTableName() + " where " + subTable.whereStatement(restOfFieldPath, criteriaOperator) + ")";
			}
		} else {
			return column + " " + criteriaOperator.getOperatorAsString() + " ?";
		}
	}

	// execution helpers

	protected T executeSelect(PreparedStatement preparedStatement) throws SQLException {
		try (ResultSet resultSet = preparedStatement.executeQuery()) {
			if (resultSet.next()) {
				return sqlPersistence.readResultSetRow(clazz,  resultSet);
			} else {
				return null;
			}
		}
	}

	protected List<T> executeSelectAll(PreparedStatement preparedStatement) throws SQLException {
		return executeSelectAll(preparedStatement, Long.MAX_VALUE);
	}
	
	protected List<T> executeSelectAll(PreparedStatement preparedStatement, long maxResults) throws SQLException {
		List<T> result = new ArrayList<T>();
		try (ResultSet resultSet = preparedStatement.executeQuery()) {
			Map<Class<?>, Map<Object, Object>> loadedReferences = new HashMap<>();
			while (resultSet.next() && result.size() < maxResults) {
				T object = sqlPersistence.readResultSetRow(clazz,  resultSet, loadedReferences);
				if (this instanceof Table) {
					Object id = IdUtils.getId(object);
					((Table<T>) this).loadRelations(object, id);
				}
				result.add(object);
			}
		}
		return result;
	}

	protected enum ParameterMode {
		INSERT, UPDATE, HISTORIZE;
	}
	
	protected int setParameters(PreparedStatement statement, T object, boolean doubleValues, ParameterMode mode, Object id) throws SQLException {
		int parameterPos = 1;
		for (Map.Entry<String, PropertyInterface> column : columns.entrySet()) {
			PropertyInterface property = column.getValue();
			Object value = property.getValue(object);
			if (value instanceof Code) {
				value = findId((Code) value);
			} else if (ViewUtil.isReference(property)) {
				if (value != null) {
					value = IdUtils.getId(value);
				}
			} else if (SqlHelper.isDependable(property)) {
				Table dependableTable = sqlPersistence.getTable(property.getClazz());
				if (mode == ParameterMode.INSERT) {
					if (value != null) {
						value = dependableTable.insert(value);
					}							
				} else {
					// update
					String dependableColumnName = column.getKey();
					Object dependableId = getDependableId(id, dependableColumnName);
					if (value != null) {
						value = updateDependable(dependableTable, dependableId, value, mode);
					} else {
						if (mode == ParameterMode.UPDATE) {
							// to delete a dependable the value where its used has to be set
							// to null first. This problem could also be solved by setting the
							// reference constraint to 'deferred'. But this 'deferred' is more
							// expensive for database and doesn't work with maria db (TODO: really?)
							setColumnToNull(id, dependableColumnName);
							dependableTable.delete(dependableId);
						}
					}
				}
			} 
			helper.setParameter(statement, parameterPos++, value, property);
			if (doubleValues) helper.setParameter(statement, parameterPos++, value, property);
		}
		statement.setObject(parameterPos++, id);
		if (doubleValues) statement.setObject(parameterPos++, id);
		return parameterPos;
	}

	protected Object updateDependable(Table dependableTable, Object dependableId, Object dependableObject, ParameterMode mode) {
		if (dependableId != null) {
			Object objectInDb = dependableTable.read(dependableId);
			if (!EqualsHelper.equals(dependableObject, objectInDb)) {
				if (mode == ParameterMode.HISTORIZE) {
					IdUtils.setId(dependableObject, null);
					dependableObject = dependableTable.insert(dependableObject);
				} else {
					dependableTable.update(dependableId, dependableObject);
				}
			}
		} else {
			dependableObject = dependableTable.insert(dependableObject);
		}
		return dependableObject;
	}
	
	// TODO multiple dependables could be get with one (prepared) statement
	private Object getDependableId(Object id, String column) throws SQLException {
		String query = "SELECT " + column + " FROM " + getTableName() + " WHERE ID = ?";
		if (this instanceof HistorizedTable) {
			query += " AND VERSION = 0";
		}
		PreparedStatement preparedStatement = getStatement(sqlPersistence.getConnection(), query, false);
		preparedStatement.setObject(1, id);
		try (ResultSet resultSet = preparedStatement.executeQuery()) {
			if (resultSet.next()) {
				return resultSet.getObject(1);
			} else {
				return null;
			}
		}
	}

	private void setColumnToNull(Object id, String column) throws SQLException {
		String update = "UPDATE " + getTableName() + " SET " + column + " = NULL WHERE ID = ?";
		PreparedStatement preparedStatement = getStatement(sqlPersistence.getConnection(), update, false);
		preparedStatement.setObject(1, id);
		preparedStatement.execute();
	}

	private Object findId(Code code) {
		Object id = IdUtils.getId(code);
		if (id != null) {
			return id;
		}
		List<?> codes = sqlPersistence.getCodes(code.getClass());
		for (Object c : codes) {
			if (code.equals(c)) {
				return IdUtils.getId(c);
			}
		}
		return null;
	}
			
	protected abstract String insertQuery();

	protected abstract String selectByIdQuery();

	protected String clearQuery() {
		StringBuilder query = new StringBuilder();
		query.append("DELETE FROM "); query.append(getTableName()); 
		return query.toString();
	}
	
	//

	public void createIndex(Object key) {
		PropertyInterface property = Keys.getProperty(key);
		String fieldPath = property.getPath();
		createIndex(property, fieldPath);
	}
	
	public void createIndex(PropertyInterface property, String fieldPath) {
		Map.Entry<String, PropertyInterface> entry = findX(fieldPath);
		if (indexes.contains(entry.getKey())) {
			return;
		}
		
		String myFieldPath = entry.getValue().getPath();
		if (fieldPath.length() > myFieldPath.length()) {
			String rest = fieldPath.substring(myFieldPath.length() + 1);
			AbstractTable<?> innerTable = sqlPersistence.getAbstractTable(entry.getValue().getClazz());
			innerTable.createIndex(property, rest);
		}
		indexes.add(entry.getKey());
	}
	
	//
	
	protected Entry<String, PropertyInterface> findX(String fieldPath) {
		while (true) {
			for (Map.Entry<String, PropertyInterface> entry : columns.entrySet()) {
				String columnFieldPath = entry.getValue().getPath();
				if (columnFieldPath.equals(fieldPath)) {
					return entry;
				}
			}
			int index = fieldPath.lastIndexOf('.');
			if (index < 0) throw new IllegalArgumentException();
			fieldPath = fieldPath.substring(0, index);
		}
	}

}