package org.minimalj.frontend.page;

import java.text.MessageFormat;
import java.util.List;

import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.Frontend.IContent;
import org.minimalj.frontend.Frontend.ITable;
import org.minimalj.frontend.Frontend.TableActionListener;
import org.minimalj.frontend.editor.Editor;
import org.minimalj.util.CloneHelper;
import org.minimalj.util.GenericUtils;
import org.minimalj.util.resources.Resources;

/**
 * Shows a table of objects of one class. 
 *
 * @param <T> Class of objects in this overview
 */
public abstract class TablePage<T> extends Page implements TableActionListener<T> {

	private final Object[] keys;
	private transient ITable<T> table;
	private transient List<T> objects;
	
	/*
	 * this flag indicates if the next call of getContent should trigger a new loading
	 * of the data. A second call of getContent probably means that the user revisits
	 * the page and doesn't want to see the old data. 
	 */
	private transient boolean reloadFlag;
	
	public TablePage(Object[] keys) {
		this.keys = keys;
	}

	protected abstract List<T> load();

	@Override
	public String getTitle() {
		String title = Resources.getStringOrNull(getClass());
		if (title != null) {
			return title;
		} else {
			Class<?> tableClazz = GenericUtils.getGenericClass(getClass());
			String className = Resources.getString(tableClazz);
			return MessageFormat.format(Resources.getString(TablePage.class.getSimpleName() + ".title"), className);
		}
	}
	
	@Override
	public IContent getContent() {
		table = Frontend.getInstance().createTable(keys, this);
		if (objects == null || reloadFlag) {
			objects = load();
			reloadFlag = true;
		}
		table.setObjects(objects);
		return table;
	}

	public int getResultCount() {
		if (objects == null) {
			objects = load();
			reloadFlag = false;
		}
		return objects.size();
	}
	
	public void refresh() {
		if (table != null) {
			objects = load();
			table.setObjects(objects);
			reloadFlag = false;
		}
	}
	
	public static abstract class TablePageWithDetail<T, DETAIL_PAGE extends Page> extends TablePage<T> {
		
		private DETAIL_PAGE detailPage;

		public TablePageWithDetail(Object[] keys) {
			super(keys);
		}

		protected abstract DETAIL_PAGE createDetailPage(T mainObject);

		protected abstract DETAIL_PAGE updateDetailPage(DETAIL_PAGE page, T mainObject);

		@Override
		public void action(T selectedObject) {
			if (detailPage != null) {
				updateDetailPage(selectedObject);
			} else {
				detailPage = createDetailPage(selectedObject);
				if (detailPage != null) {
					Frontend.showDetail(TablePageWithDetail.this, detailPage);
				}
			}
		}

		@Override
		public void selectionChanged(T selectedObject, List<T> selectedObjects) {
			boolean detailVisible = detailPage != null && Frontend.isDetailShown(detailPage); 
			if (detailVisible) {
				if (selectedObject != null) {
					updateDetailPage(selectedObject);
				} else {
					Frontend.hideDetail(detailPage);
				}
			}
		}
		
		private void updateDetailPage(T selectedObject) {
			DETAIL_PAGE updatedDetailPage = updateDetailPage(detailPage, selectedObject);
			if (Frontend.isDetailShown(detailPage)) {
				if (updatedDetailPage == null || updatedDetailPage != detailPage) {
					Frontend.hideDetail(detailPage);
				}
			}
			if (updatedDetailPage != null) {
				Frontend.showDetail(TablePageWithDetail.this, updatedDetailPage);
				detailPage = updatedDetailPage;
			}
		}
		
		public abstract class NewDetailEditor<DETAIL> extends Editor<DETAIL, T> {
			
			@Override
			protected DETAIL createObject() {
				@SuppressWarnings("unchecked")
				Class<DETAIL> clazz = (Class<DETAIL>) GenericUtils.getGenericClass(getClass());
				DETAIL newInstance = CloneHelper.newInstance(clazz);
				return newInstance;
			}
			
			@Override
			protected void finished(T result) {
				TablePageWithDetail.this.refresh();
				// after closing the editor the user expects the new object
				// to be displayed as the detail. This call provides that
				TablePageWithDetail.this.action(result);
			}
		}
	}
	
	public static abstract class SimpleTablePageWithDetail<T> extends TablePageWithDetail<T, ObjectPage<T>> {

		public SimpleTablePageWithDetail(Object[] keys) {
			super(keys);
		}
		
		@Override
		protected ObjectPage<T> updateDetailPage(ObjectPage<T> detailPage, T mainObject) {
			detailPage.setObject(mainObject);
			return detailPage;
		}
	}
	
}
