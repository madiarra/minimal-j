package ch.openech.mj.db;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

import ch.openech.mj.util.StringUtils;

public abstract class SearchableTable<T> extends Table<T> {
	private static final Logger logger = Logger.getLogger(SearchableTable.class.getName());
	
	private static final Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_34);
	private RAMDirectory directory;
    //Directory directory = FSDirectory.open("/tmp/testindex");
	private IndexWriter iwriter;
	private PreparedStatement selectAll;

	private final String[] indexFields;
	
	public SearchableTable(DbPersistence dbPersistence, Class<T> clazz, String[] indexFields) {
		super(dbPersistence, clazz);
		this.indexFields = indexFields;
	}
	
	@Override
	public void initialize() throws SQLException {
		super.initialize();
		selectAll = prepareSelectAll();
	}
	
	public void initializeIndex() throws SQLException {
		if (directory == null) {
			directory = new RAMDirectory();
			try {
				iwriter = new IndexWriter(directory, analyzer, true,
				        new IndexWriter.MaxFieldLength(25000));
			} catch (CorruptIndexException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (LockObtainFailedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			refillIndex();
		}
	}

	protected void refillIndex() throws SQLException {
		ResultSet resultSet = selectAll.executeQuery();
		
		int idColumn = -1;
		ResultSetMetaData metaData = resultSet.getMetaData();
		for (int i = 1; i<=metaData.getColumnCount(); i++) {
			if ("id".equals(metaData.getColumnName(i))) {
				idColumn = i;
				break;
			}
		}
		if (idColumn < 0) throw new RuntimeException("Searchable Table must have an id column");
		
		int count = 0;
		while (resultSet.next()) {
			int id = resultSet.getInt(idColumn);
			T object = readResultSetRow(resultSet, null);
			writeInIndex(id, object);
			if (logger.isLoggable(Level.FINER)) logger.finer("RefillIndex: " + getClazz() + " / " + id);
			count++;
		}
		resultSet.close();
		logger.info("Refilled the index " + getClazz() +" with " + count);
		logger.info("Size of index is " + directory.sizeInBytes());
	}
	
	@Override
	public int insert(T object) throws SQLException {
		int id = super.insert(object);
		
		writeInIndex(id, object);
		
		// TODO clean
		QueryParser	parser = new QueryParser(Version.LUCENE_34, "id", analyzer);
		try {
			Query query = parser.parse(Integer.toString(id));
			
			IndexSearcher isearcher = new IndexSearcher(directory, true); // read-only=true
			ScoreDoc[] hits = isearcher.search(query, null, 1000).scoreDocs;
			if (hits.length != 1) throw new IllegalStateException("Twice in index : " + id);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}		
		
		return id;
	}

	@Override
	public void update(T object) throws SQLException {
		super.update(object);

		// TODO clean
		Integer id = getId(object);
		QueryParser	parser = new QueryParser(Version.LUCENE_34, "id", analyzer);
		try {
			Query query = parser.parse(Integer.toString(id));
			
			IndexSearcher isearcher = new IndexSearcher(directory, true); // read-only=true
			ScoreDoc[] hits = isearcher.search(query, null, 1000).scoreDocs;
			if (hits.length != 1) throw new IllegalStateException("Id : " + id);
			
			iwriter.deleteDocuments(query);
			writeInIndex(id, object);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}		
	}
	
	
	@Override
	public void clear() throws SQLException {
		super.clear();
		try {
			if (iwriter != null) {
				iwriter.deleteAll();
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}	
	}

	public int count(String text) {
		return find(text).size();
	}
	
	protected abstract Field getField(String fieldName, T object);

	protected Field createIdField(int id) {
		Field.Index index = Field.Index.NOT_ANALYZED;
		
		return new Field("id", Integer.toString(id), Field.Store.YES, index);
	}
	
	private void writeInIndex(int id, T object) {
		try {
			initializeIndex();
			Document doc = new Document();
			doc.add(createIdField(id));
			for (String fieldName : indexFields) {
				Field field = getField(fieldName, object);
				if (field != null) {
					doc.add(field);
				}
			}
		    iwriter.addDocument(doc);
		    iwriter.commit();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public List<T> find(String text) {
		return find (text, indexFields);
	}
	
	public List<T> find(String text, String[] queryFields) {
		List<T> result = new ArrayList<T>();
		IndexSearcher isearcher = null;
		try {
			initializeIndex();
			if (directory.listAll().length == 0 || StringUtils.isBlank(text)) return result;

			isearcher = new IndexSearcher(directory, true); // read-only=true
			
			QueryParser parser;
			if (queryFields.length > 1) {
				parser = new MultiFieldQueryParser(Version.LUCENE_34, queryFields, analyzer);
			} else {
				parser = new QueryParser(Version.LUCENE_34, queryFields[0], analyzer);
			}
			Query query = parser.parse(text);
			ScoreDoc[] hits = isearcher.search(query, null, 1000).scoreDocs;
			
			for (int i = 0; i < hits.length; i++) {
				Document hitDoc = isearcher.doc(hits[i].doc);
				T object = documentToObject(hitDoc);
				result.add(object);
			}
		} catch (ParseException x) {
			// user entered something like "*" which is not allowed
			logger.info(x.getLocalizedMessage());
		} catch (Exception x) {
			logger.severe(x.getLocalizedMessage());
		} finally {
			if (isearcher != null) {
				try {
					isearcher.close();
				} catch (IOException e) {
					logger.severe(e.getLocalizedMessage());
				}
			}
		}
		return result;
	}
	
	protected abstract T createResultObject();

	protected abstract void setField(T result, String fieldName, String value);

	protected T documentToObject(Document document) {
		T result = createResultObject();
		
		for (String fieldName : indexFields) {
			setField(result, fieldName, document.get(fieldName));
		}
		return result;
	}
	
	// Statements

	protected PreparedStatement prepareSelectAll() throws SQLException {
		StringBuilder s = new StringBuilder();
		s.append("SELECT * FROM "); s.append(getTableName()); s.append(" WHERE version = 0");
		return getConnection().prepareStatement(s.toString());
	}
}
