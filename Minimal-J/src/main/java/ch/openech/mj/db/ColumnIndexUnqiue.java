package ch.openech.mj.db;//

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

import ch.openech.mj.model.PropertyInterface;


public class ColumnIndexUnqiue<T> extends AbstractIndex<T> {

	protected ColumnIndex<?> innerIndex;
	
	ColumnIndexUnqiue(DbPersistence dbPersistence, AbstractTable<T> table, PropertyInterface property, String column, ColumnIndex<?> innerIndex) {
		super(dbPersistence, table, property, column);
		this.innerIndex = innerIndex;
	}
	
	public Integer findId(Connection connection, Object query) {		
		try {
			PreparedStatement selectStatement = table.getStatement(connection, selectQuery, false);
			if (innerIndex != null) {
				List<Integer> ids = innerIndex.findIds(connection, query);
				for (Integer id : ids) {
					helper.setParameter(selectStatement, 1, id, property);
					Integer result = executeSelectId(selectStatement);
					if (result != null) {
						return result;
					}
				}
				return null;
			} else {
				helper.setParameter(selectStatement, 1, query, property);
				return executeSelectId(selectStatement);
			}
		} catch (SQLException x) {
			String message = "Couldn't use index of column + " + column + " of table " + table.getTableName() + " with query " + query;
			sqlLogger.log(Level.SEVERE, message, x);
			throw new RuntimeException(message);
		}
	}
	

	@Override
	public List<Integer> findIds(Connection connection, Object query) {
		return Collections.singletonList(findId(connection, query));
	}
	
	public T find(Connection connection, Object query) {
		Integer id = findId(connection, query);
		return lookup(connection, id);
	}

	
	@Override
	protected String selectQuery() {
		StringBuilder query = new StringBuilder();
		query.append("SELECT id FROM "); query.append(table.getTableName()); 
		query.append(" WHERE "); query.append(column); query.append(" = ?");
		if (table instanceof HistorizedTable) {
			query.append(" AND version = 0");
		}
		return query.toString();
	}

}
