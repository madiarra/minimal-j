package org.minimalj.backend.sql;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.util.IdUtils;

/**
 * Minimal-J internal
 * 
 * - In this tables the parentId is used as id
 * - An additional column named position
 * - has no sub tables
 */
public class SubTable<PARENT, ELEMENT> extends AbstractTable<ELEMENT> implements ListTable<PARENT, ELEMENT> {

	protected final PropertyInterface parentIdProperty;
	
	public SubTable(SqlPersistence sqlPersistence, String name, Class<ELEMENT> clazz, PropertyInterface parentIdProperty) {
		super(sqlPersistence, name, clazz);
		
		this.parentIdProperty = parentIdProperty;
	}
	
	@Override
	public void addAll(PARENT parent, List<ELEMENT> objects) {
		try (PreparedStatement insertStatement = createStatement(sqlPersistence.getConnection(), insertQuery, false)) {
			for (int position = 0; position<objects.size(); position++) {
				ELEMENT object = objects.get(position);
				int parameterPos = setParameters(insertStatement, object, false, ParameterMode.INSERT, IdUtils.getId(parent));
				insertStatement.setInt(parameterPos++, position);
				insertStatement.execute();
			}
		} catch (SQLException x) {
			throw new RuntimeException(x.getMessage());
		}
	}

	@Override
	public void replaceAll(PARENT parent, List<ELEMENT> objects) {
		Object parentId = IdUtils.getId(parent);
		List<ELEMENT> objectsInDb = readAll(parent);
		int position = 0;
		while (position < Math.max(objects.size(), objectsInDb.size())) {
			if (position < objectsInDb.size() && position < objects.size()) {
				update(parentId, position, objects.get(position));
			} else if (position < objectsInDb.size()) {
				// delete all beginning from this position with one delete statement
				delete(parentId, position);
				break; 
			} else /* if (position < objects.size()) */ {
				insert(parentId, position, objects.get(position));
			}
			position++;
		}
	}
	
	private void update(Object parentId, int position, ELEMENT object) {
		try (PreparedStatement updateStatement = createStatement(sqlPersistence.getConnection(), updateQuery, false)) {
			int parameterPos = setParameters(updateStatement, object, false, ParameterMode.UPDATE, parentId);
			updateStatement.setInt(parameterPos++, position);
			updateStatement.execute();
		} catch (SQLException x) {
			throw new RuntimeException(x.getMessage());
		}
	}

	private void insert(Object parentId, int position, ELEMENT object) {
		try (PreparedStatement insertStatement = createStatement(sqlPersistence.getConnection(), insertQuery, false)) {
			int parameterPos = setParameters(insertStatement, object, false, ParameterMode.INSERT, parentId);
			insertStatement.setInt(parameterPos++, position);
			insertStatement.execute();
		} catch (SQLException x) {
			throw new RuntimeException(x.getMessage());
		}
	}
	
	private void delete(Object parentId, int position) {
		try (PreparedStatement deleteStatement = createStatement(sqlPersistence.getConnection(), deleteQuery, false)) {
			deleteStatement.setObject(1, parentId);
			deleteStatement.setInt(2, position);
			deleteStatement.execute();
		} catch (SQLException x) {
			throw new RuntimeException(x.getMessage());
		}
	}

	@Override
	public List<ELEMENT> readAll(PARENT parent) {
		try (PreparedStatement selectByIdStatement = createStatement(sqlPersistence.getConnection(), selectByIdQuery, false)) {
			selectByIdStatement.setObject(1, IdUtils.getId(parent));
			return executeSelectAll(selectByIdStatement);
		} catch (SQLException x) {
			throw new RuntimeException(x.getMessage());
		}
	}

	// Queries
	
	@Override
	protected String selectByIdQuery() {
		StringBuilder query = new StringBuilder();
		query.append("SELECT * FROM "); query.append(getTableName()); query.append(" WHERE id = ? ORDER BY position");
		return query.toString();
	}
	
	@Override
	protected String insertQuery() {
		StringBuilder s = new StringBuilder();
		
		s.append("INSERT INTO ").append(getTableName()).append(" (");
		for (Object columnNameObject : getColumns().keySet()) {
			// myst, direkt auf columnNames zugreiffen funktionert hier nicht
			String columnName = (String) columnNameObject;
			s.append(columnName).append(", ");
		}
		s.append("id, position) VALUES (");
		for (int i = 0; i<getColumns().size(); i++) {
			s.append("?, ");
		}
		s.append("?, ?)");

		return s.toString();
	}

	@Override
	protected String updateQuery() {
		StringBuilder s = new StringBuilder();
		
		s.append("UPDATE ").append(getTableName()).append(" SET ");
		for (Object columnNameObject : getColumns().keySet()) {
			s.append((String) columnNameObject).append("= ?, ");
		}
		s.delete(s.length()-2, s.length());
		s.append(" WHERE id = ? AND position = ?");

		return s.toString();
	}
	
	@Override
	protected String deleteQuery() {
		return "DELETE FROM " + getTableName() + " WHERE id = ? AND position >= ?";
	}

	@Override
	protected void addSpecialColumns(SqlSyntax syntax, StringBuilder s) {
		s.append(" id ");
		syntax.addColumnDefinition(s, parentIdProperty);
		s.append(",\n position INTEGER NOT NULL");
	}
	
	@Override
	protected void addPrimaryKey(SqlSyntax syntax, StringBuilder s) {
		syntax.addPrimaryKey(s, "id, position");
	}

}