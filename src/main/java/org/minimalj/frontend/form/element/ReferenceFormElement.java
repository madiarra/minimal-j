package org.minimalj.frontend.form.element;

import java.util.List;

import org.minimalj.backend.Backend;
import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.Frontend.IComponent;
import org.minimalj.frontend.Frontend.Input;
import org.minimalj.frontend.Frontend.InputComponentListener;
import org.minimalj.frontend.Frontend.Search;
import org.minimalj.model.Keys;
import org.minimalj.model.ViewUtil;
import org.minimalj.transaction.criteria.SearchCriteria;
import org.minimalj.util.CloneHelper;

public class ReferenceFormElement<T> extends AbstractFormElement<T> {
	// private static final Logger logger = Logger.getLogger(ReferenceField.class.getName());
	
	private final Class<?> fieldClazz;
	private final Object[] searchColumns;
	protected final Input<T> lookup;
	
	public ReferenceFormElement(Object key, Object... searchColumns) {
		super(Keys.getProperty(key));
		fieldClazz = getProperty().getClazz();
		this.searchColumns = searchColumns;
		lookup = Frontend.getInstance().createLookup(new ReferenceFieldChangeListener(), new ReferenceFieldSearch(), searchColumns);
	}

	private class ReferenceFieldSearch implements Search<T> {

		@Override
		public List<T> search(String searchText) {
			return (List<T>) Backend.read(fieldClazz, new SearchCriteria(searchText, searchColumns), 100);
		}
	}
	
	@Override
	public IComponent getComponent() {
		return lookup;
	}

	@Override
	public T getValue() {
		return lookup.getValue();
	}

	@Override
	public void setValue(T object) {
		lookup.setValue(object);
	}

	private class ReferenceFieldChangeListener implements InputComponentListener {

		@Override
		public void changed(IComponent source) {
			Object selectedObject = lookup.getValue();
			@SuppressWarnings("unchecked")
			T objectAsView = (T) ViewUtil.view(selectedObject, CloneHelper.newInstance(getProperty().getClazz()));
			setValue(objectAsView);
			fireChange();
		}
		
	}

}
