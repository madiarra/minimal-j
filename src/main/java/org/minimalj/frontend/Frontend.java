package org.minimalj.frontend;

import java.util.Collections;
import java.util.List;

import org.minimalj.application.Subject;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.page.PageBrowser;

/**
 * To provide a new kind (Xy) of client you have to implement two things:
 * <OL>
 * <LI>This class, like XyFrontend</LI>
 * <LI>Some kind of XyApplication with a main. The XyApplication should take an instance of Application and 
 * start the client. Take a look at the existing SwingFrontend, VaadinFrontend or LanternaFrontend. The trickiest part will be to implement
 * the PageBrowser.</LI>
 * </OL>
 *
 */

public abstract class Frontend {

	private static Frontend frontend;
	private static ThreadLocal<PageBrowser> pageBrowserByThread = new ThreadLocal<PageBrowser>();
	
	public static Frontend getInstance() {
		if (frontend == null) {
			throw new IllegalStateException("Frontend has to be initialized");
		}
		return frontend;
	}

	public static synchronized void setInstance(Frontend frontend) {
		if (Frontend.frontend != null) {
			throw new IllegalStateException("Frontend cannot be changed");
		}		
		if (frontend == null) {
			throw new IllegalArgumentException("Frontend cannot be null");
		}
		Frontend.frontend = frontend;
	}

	/**
	 * Components are the smallest part of the gui. Things like textfields
	 * and comboboxes. A form is filled with components.
	 */
	public interface IComponent {
	}
	
	public interface Input<T> extends IComponent {
		
		public void setValue(T value);

		public T getValue();

		public void setEditable(boolean editable);
	}

	 // http://www.w3schools.com/html/html_form_input_types.asp 
	public enum InputType { FREE, EMAIL, URL, TEL, NUMBER; }

	public abstract IComponent createLabel(String string);
	public abstract IComponent createLabel(Action action);
	public abstract IComponent createTitle(String string);
	public abstract Input<String> createReadOnlyTextField();
	public abstract Input<String> createTextField(int maxLength, String allowedCharacters, InputType inputType, List<String> choice, InputComponentListener changeListener);
	public abstract Input<String> createAreaField(int maxLength, String allowedCharacters, InputComponentListener changeListener);
	public abstract IList createList(Action... actions);
	public abstract <T> Input<T> createComboBox(List<T> object, InputComponentListener changeListener);
	public abstract Input<Boolean> createCheckBox(InputComponentListener changeListener, String text);

	public interface IList extends IComponent {
		/**
		 * @param enabled if false no content should be shown (or
		 * only in gray) and all actions must get disabled
		 */
		public void setEnabled(boolean enabled);
		
		public void clear();
		
		public void add(Object object, Action... actions);
		
	}
	
	public interface InputComponentListener {
	    void changed(IComponent source);
	}
	
	public interface Search<S> {
		public List<S> search(String query);
	}
	
	public abstract <T> Input<T> createLookup(InputComponentListener changeListener, Search<T> index, Object[] keys);
	
	public abstract IComponent createComponentGroup(IComponent... components);

	/**
	 * Content means the content of a dialog or of a page
	 */
	public interface IContent {
	}

	public interface FormContent extends IContent {
		public void add(IComponent component);
		public void add(String caption, IComponent component, int span);
		public void setValidationMessages(IComponent component, List<String> validationMessages);
	}
	
	public abstract FormContent createFormContent(int columns, int columnWidth);

	public interface SwitchContent extends IContent {
		public void show(IContent content);
	}
	
	public abstract SwitchContent createSwitchContent();

	public interface ITable<T> extends IContent {
		public void setObjects(List<T> objects);
	}

	public static interface TableActionListener<U> {

		public default void selectionChanged(U selectedObject, List<U> selectedObjects) {
		}
		
		public default void action(U selectedObject) {
		}
		
		public default List<Action> getActions(U selectedObject, List<U> selectedObjects) {
			return Collections.emptyList();
		}
	}
	
	public abstract <T> ITable<T> createTable(Object[] keys, TableActionListener<T> listener);
	
	//
	
	public static void setBrowser(PageBrowser pageBrowser) {
		pageBrowserByThread.set(pageBrowser);
		Subject.set(pageBrowser != null ? pageBrowser.getSubject() : null);
	}
	
	public static PageBrowser getBrowser() {
		return pageBrowserByThread.get();
	}
	
}