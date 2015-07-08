package org.minimalj.frontend.swing.toolkit;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.FocusTraversalPolicy;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Locale;

import javax.swing.AbstractAction;
import javax.swing.FocusManager;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;

import org.minimalj.application.ApplicationContext;
import org.minimalj.frontend.page.Page;
import org.minimalj.frontend.swing.SwingFrame;
import org.minimalj.frontend.swing.SwingFrontend;
import org.minimalj.frontend.swing.SwingTab;
import org.minimalj.frontend.toolkit.Action;
import org.minimalj.frontend.toolkit.Action.ActionChangeListener;
import org.minimalj.frontend.toolkit.ClientToolkit;
import org.minimalj.frontend.toolkit.FormContent;
import org.minimalj.frontend.toolkit.IDetail;
import org.minimalj.frontend.toolkit.IDialog;
import org.minimalj.frontend.toolkit.IList;
import org.minimalj.frontend.toolkit.ProgressListener;
import org.minimalj.model.Rendering;
import org.minimalj.model.Rendering.RenderType;

public class SwingClientToolkit extends ClientToolkit {

	private static ThreadLocal<SwingTab> tabByThread = new ThreadLocal<SwingTab>();
	
	public static SwingTab getTab() {
		return tabByThread.get();
	}
	
	public static void setTab(SwingTab tab) {
		tabByThread.set(tab);
	}
	
	@Override
	public IComponent createLabel(String string) {
		return new SwingLabel(string);
	}
	
	@Override
	public IComponent createLabel(Action action) {
		return new SwingActionLabel(action);
	}

	public static class SwingActionLabel extends JLabel implements IComponent {
		private static final long serialVersionUID = 1L;

		public SwingActionLabel(final Action action) {
			setText(action.getName());
//			label.setToolTipText(Resources.getResourceBundle().getString(runnable.getClass().getSimpleName() + ".description"));
			
			setForeground(Color.BLUE);
			setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					updateEventTab((Component) e.getSource());
					action.action();
				}
			});
		}
	}
	
	public static SwingTab findSwingTab(Component c) {
		while (c != null && !(c instanceof SwingTab)) {
			if (c instanceof JPopupMenu) {
				c = ((JPopupMenu) c).getInvoker();
			} else {
				c = c.getParent();
			}
		}
		return (SwingTab) c;
	}
	
	public static void updateEventTab(Component c) {
		SwingTab swingTab = findSwingTab(c);
		tabByThread.set(swingTab);
	}

	@Override
	public IComponent createTitle(String string) {
		return new SwingTitle(string);

	}

	@Override
	public Input<String> createReadOnlyTextField() {
		return new SwingReadOnlyTextField();
	}

	@Override
	public Input<String> createTextField(int maxLength, String allowedCharacters, InputType inputType, List<String> choice,
			InputComponentListener changeListener) {
		if (choice == null) {
			return new SwingTextField(changeListener, maxLength, allowedCharacters);
		} else {
			return new SwingTextFieldAutocomplete(changeListener, choice);
		}
	}

	@Override
	public Input<String> createAreaField(int maxLength, String allowedCharacters, InputComponentListener changeListener) {
		return new SwingTextAreaField(changeListener, maxLength, null);
	}

	@Override
	public IList createList(Action... actions) {
		return new SwingList(actions);
	}

	@Override
	public <T> Input<T> createComboBox(List<T> objects, InputComponentListener changeListener) {
		return new SwingComboBox<T>(objects, changeListener);
	}

	@Override
	public Input<Boolean> createCheckBox(InputComponentListener changeListener, String text) {
		return new SwingCheckBox(changeListener, text);
	}

	@Override
	public IComponent createComponentGroup(IComponent... components) {
		return new SwingHorizontalLayout(components);
	}

	@Override
	public SwitchContent createSwitchContent() {
		return new SwingSwitchContent();
	}

	@Override
	public FormContent createFormContent(int columns, int columnWidthPercentage) {
		return new SwingGridFormLayout(columns, columnWidthPercentage);
	}

	public static void focusFirstComponent(JComponent jComponent) {
		if (jComponent.isShowing()) {
			focusFirstComponentNow(jComponent);
		} else {
			focusFirstComponentLater(jComponent);
		}
	}

	private static void focusFirstComponentNow(JComponent component) {
		FocusTraversalPolicy focusPolicy = component.getFocusTraversalPolicy();
		if (component instanceof JTextComponent || component instanceof JComboBox || component instanceof JCheckBox) {
			component.requestFocus();
		} else if (focusPolicy != null && focusPolicy.getFirstComponent(component) != null) {
			focusPolicy.getFirstComponent(component).requestFocus();
		} else {
			FocusManager.getCurrentManager().focusNextComponent(component);
		}
	}

	private static void focusFirstComponentLater(final JComponent component) {
		component.addHierarchyListener(new HierarchyListener() {
			@Override
			public void hierarchyChanged(HierarchyEvent e) {
				component.removeHierarchyListener(this);
				focusFirstComponent(component);
			}
		});
	}

	@Override
	public void showMessage(String text) {
		Window window = findWindow();
		JOptionPane.showMessageDialog(window, text, "Information", JOptionPane.INFORMATION_MESSAGE);
	}

	@Override
	public void showError(String text) {
		Window window = findWindow();
		JOptionPane.showMessageDialog(window, text, "Fehler", JOptionPane.ERROR_MESSAGE);
	}

	@Override
	public void showConfirmDialog(String message, String title, ConfirmDialogType type,
			DialogListener listener) {
		int optionType = type.ordinal();
		int result = JOptionPane.showConfirmDialog(getTab(), message, title, optionType);
		listener.close(ConfirmDialogResult.values()[result]);
	}

	@Override
	public <T> ITable<T> createTable(Object[] keys, TableActionListener<T> listener) {
		return new SwingTable<T>(keys, listener);
	}

	@Override
	public void show(Page page) {
		((SwingFrame) SwingFrame.getActiveWindow()).getVisibleTab().show(page);
	}

	@Override
	public IDetail showDetail(Page detail) {
		((SwingFrame) SwingFrame.getActiveWindow()).getVisibleTab().showDetail(detail);
	}

	public ProgressListener showProgress(String text) {
		SwingProgressInternalFrame frame = new SwingProgressInternalFrame(text);
		getTab().openModalDialog(frame);
		return frame;
	}

	@Override
	public IDialog showDialog(String title, IContent content, Action saveAction, Action closeAction, Action... actions) {
		JComponent contentComponent = new SwingEditorPanel(content, actions);
		return createDialog(title, contentComponent, saveAction, closeAction);
	}

	private IDialog createDialog(String title, JComponent content, Action saveAction, Action closeAction) {
		return new SwingInternalFrame(getTab(), title, content, saveAction, closeAction);
	}

	public static Window findWindow() {
		Component parentComponent = getTab();
		while (parentComponent != null && !(parentComponent instanceof Window)) {
			if (parentComponent instanceof JPopupMenu) {
				parentComponent = ((JPopupMenu) parentComponent).getInvoker();
			} else {
				parentComponent = parentComponent.getParent();
			}
		}
		return (Window) parentComponent;
	}
		
	@Override
	public <T> Input<T> createLookup(InputComponentListener changeListener, Search<T> index, Object[] keys) {
		return new SwingLookup<T>(changeListener, index, keys);
	}
	
	private static class SwingLookup<T> extends JPanel implements Input<T> {
		private static final long serialVersionUID = 1L;
		
		private final InputComponentListener changeListener;
		private final Search<T> search;
		private final Object[] keys;
		private final SwingLookupLabel actionLabel;
		private final SwingRemoveLabel removeLabel;
		private IDialog dialog;
		private T selectedObject;
		
		public SwingLookup(InputComponentListener changeListener, Search<T> search, Object[] keys) {
			super(new BorderLayout());
			
			this.changeListener = changeListener;
			this.search = search;
			this.keys = keys;
			
			this.actionLabel = new SwingLookupLabel();
			this.removeLabel = new SwingRemoveLabel();
			add(actionLabel, BorderLayout.CENTER);
			add(removeLabel, BorderLayout.LINE_END);
		}

		@Override
		public void setValue(T value) {
			this.selectedObject = value;
			display();
		}
		
		protected void display() {
			if (selectedObject instanceof Rendering) {
				Rendering rendering = (Rendering) selectedObject;
				actionLabel.setText(rendering.render(RenderType.PLAIN_TEXT, Locale.getDefault()));
			} else if (selectedObject != null) {
				actionLabel.setText(selectedObject.toString());
			} else {
				actionLabel.setText("[+]");
			}
		}

		@Override
		public T getValue() {
			return selectedObject;
		}
		
		@Override
		public void setEditable(boolean editable) {
			actionLabel.setEnabled(editable);
			removeLabel.setEnabled(editable);
		}
		
		private class SwingLookupLabel extends JLabel {
			private static final long serialVersionUID = 1L;

			public SwingLookupLabel() {
				setForeground(Color.BLUE);
				setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						dialog = ((SwingClientToolkit) ClientToolkit.getToolkit()).showSearchDialog(search, keys, new LookupClickListener());
					}
				});
			}
		}
		
		private class SwingRemoveLabel extends JLabel {
			private static final long serialVersionUID = 1L;

			public SwingRemoveLabel() {
				super("[x]");
				setForeground(Color.BLUE);
				setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						SwingLookup.this.selectedObject = null;
						changeListener.changed(SwingLookup.this);
					}
				});
			}
		}
		
		private class LookupClickListener implements TableActionListener<T> {
			@Override
			public void action(T selectedObject) {
				SwingLookup.this.selectedObject = selectedObject;
				dialog.closeDialog();
				changeListener.changed(SwingLookup.this);
			}
		}

	}

	@Override
	public <T> IDialog showSearchDialog(Search<T> index, Object[] keys, TableActionListener<T> listener) {
		SwingSearchPanel<T> panel = new SwingSearchPanel<T>(index, keys, listener);
		return createDialog(null, panel, null, null);
	}

	@Override
	public OutputStream store(String buttonText) {
		JFileChooser chooser = new JFileChooser();
		chooser.setMultiSelectionEnabled(false);
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		if (JFileChooser.APPROVE_OPTION == chooser.showDialog(getTab(), buttonText)) {
			File outputFile = chooser.getSelectedFile();
			try {
				return new FileOutputStream(outputFile);
			} catch (FileNotFoundException e) {
				throw new RuntimeException(e);
			}
		} else {
			return null;
		}
	}

	@Override
	public InputStream load(String buttonText) {
		JFileChooser chooser = new JFileChooser();
		chooser.setMultiSelectionEnabled(false);
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		if (JFileChooser.APPROVE_OPTION == chooser.showDialog(getTab(), buttonText)) {
			File inputFile = chooser.getSelectedFile();
			try {
				return new FileInputStream(inputFile);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				return null;
			}
		} else {
			return null;
		}
	}

	public static boolean verticallyGrowing(Component component) {
		if (component instanceof SwingList || component instanceof JTable || component instanceof SwingTextAreaField) {
			return true;
		}
		if (component instanceof Container) {
			Container container = (Container) component;
			for (Component c : container.getComponents()) {
				if (verticallyGrowing(c)) {
					return true;
				}
			}
		}
		return false;
	}
	
	public static javax.swing.Action[] adaptActions(Action[] actions) {
		javax.swing.Action[] swingActions = new javax.swing.Action[actions.length];
		for (int i = 0; i<actions.length; i++) {
			swingActions[i] = adaptAction(actions[i]);
		}
		return swingActions;
	}

	public static javax.swing.Action adaptAction(final Action action) {
		final javax.swing.Action swingAction = new AbstractAction(action.getName()) {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				if (SwingUtilities.isEventDispatchThread()) {
					SwingClientToolkit.updateEventTab((Component) e.getSource());
				}
				action.action();
			}
		};
		swingAction.putValue(javax.swing.Action.SHORT_DESCRIPTION, action.getDescription());
		action.setChangeListener(new ActionChangeListener() {
			{
				update();
			}
			
			@Override
			public void change() {
				update();
			}

			protected void update() {
				swingAction.setEnabled(action.isEnabled());
			}
		});
		return swingAction;
	}
	
	@Override
	public void refresh() {
		getTab().refresh();
	}

	@Override
	public ApplicationContext getApplicationContext() {
		return SwingFrontend.getApplicationContext();
	}
	
}
