package ch.openech.mj.model;


public class ModelClass<T> {

	private final Class<T> clazz;

	public ModelClass(Class<T> clazz) {
		this.clazz = clazz;
	}

	public Class<T> getClazz() {
		return clazz;
	}

	/**
	 * Used to declare an fulltext search on all
	 * the specified fields.
	 * 
	 * @param keys Array of objects generated by Keys class
	 * @return Index 
	 */
	public <T> Index<T> fulltext(Object... keys) {
		return new Index<T>(Index.INDEX_TYPE.FULLTEXT, keys);
	}
	
	/**
	 * Used to declare an index on a field. Every object of that
	 * class has a different value (at a time)
	 * 
	 * @param key Key object generated by Keys class
	 * @return Index 
	 */
	public <T> Index<T> byUnique(Object key) {
		return new Index<T>(Index.INDEX_TYPE.UNIQUE, key);
	}
	
	/**
	 * Used to declare an index on a field. The field
	 * references an other entity class.
	 * 
	 * @param key Key object generated by Keys class
	 * @return Index 
	 */
	public <T> Index<T> by(Object key) {
		return new Index<T>(Index.INDEX_TYPE.REFERENCE, key);
	}
	
	public static class Unique {
		
	}

}
