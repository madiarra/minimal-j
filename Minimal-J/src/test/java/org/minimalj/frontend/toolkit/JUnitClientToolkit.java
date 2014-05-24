package org.minimalj.frontend.toolkit;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.minimalj.frontend.toolkit.Caption;
import org.minimalj.frontend.toolkit.CheckBox;
import org.minimalj.frontend.toolkit.ClientToolkit;
import org.minimalj.frontend.toolkit.ComboBox;
import org.minimalj.frontend.toolkit.FlowField;
import org.minimalj.frontend.toolkit.GridFormLayout;
import org.minimalj.frontend.toolkit.HorizontalLayout;
import org.minimalj.frontend.toolkit.IAction;
import org.minimalj.frontend.toolkit.IComponent;
import org.minimalj.frontend.toolkit.IDialog;
import org.minimalj.frontend.toolkit.IFocusListener;
import org.minimalj.frontend.toolkit.ITable;
import org.minimalj.frontend.toolkit.SwitchLayout;
import org.minimalj.frontend.toolkit.TextField;
import org.minimalj.frontend.toolkit.ITable.TableActionListener;

public class JUnitClientToolkit extends ClientToolkit {

	private String lastError = null;
	private DialogListener nextConfirmAnswer = null;
	
	public String pullError() {
		String error = lastError;
		lastError = null;
		return error;
	}

	public void setNextConfirAnswer(DialogListener answer) {
		this.nextConfirmAnswer = answer;
	}

	@Override
	public IComponent createLabel(String string) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IComponent createLabel(IAction action) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IComponent createTitle(String string) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TextField createReadOnlyTextField() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TextField createTextField(InputComponentListener changeListener,
			int maxLength) {
		return new TextField() {
			
			@Override
			public void setText(String text) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setFocusListener(IFocusListener focusListener) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setEditable(boolean editable) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setCommitListener(Runnable runnable) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public String getText() {
				// TODO Auto-generated method stub
				return null;
			}
		};
	}

	@Override
	public TextField createTextField(InputComponentListener changeListener,
			int maxLength, String allowedCharacters) {
		return createTextField(changeListener, maxLength);
	}

	@Override
	public FlowField createFlowField() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> ComboBox<T> createComboBox(InputComponentListener changeListener) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CheckBox createCheckBox(InputComponentListener changeListener, String text) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> ITable<T> createTable(Object[] fields) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IComponent createLink(String text, String address) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Caption decorateWithCaption(final IComponent component, String caption) {
		return new Caption() {
			@Override
			public void setValidationMessages(List<String> validationMessages) {
			}
			
			@Override
			public IComponent getComponent() {
				return component;
			}
		};
	}

	@Override
	public HorizontalLayout createHorizontalLayout(IComponent... components) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SwitchLayout createSwitchLayout() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public GridFormLayout createGridLayout(int columns, int columnWidth) {
		return new GridFormLayout() {
			
			@Override
			public void add(IComponent field, int span) {
				// TODO Auto-generated method stub
				
			}
		};
	}

	@Override
	public IComponent createFormAlignLayout(IComponent content) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IDialog createDialog(IComponent parent, String title,
			IComponent content, IAction... actions) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void showMessage(Object parent, String text) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void showError(Object parent, String text) {
		if (lastError != null) {
			throw new IllegalStateException();
		}
		lastError = text;
	}

	@Override
	public void showConfirmDialog(IComponent component, String message,
			String title, ConfirmDialogType type, DialogListener listener) {
		if (nextConfirmAnswer == null) {
			throw new IllegalStateException();
		}
		listener.close(nextConfirmAnswer);
		nextConfirmAnswer = null;
	}
	
	@Override
	public OutputStream store(IComponent parent, String buttonText) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public InputStream load(IComponent parent, String buttonText) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> IDialog createSearchDialog(IComponent parent, Search<T> index, Object[] keys, TableActionListener<T> listener) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> ILookup<T> createLookup(InputComponentListener changeListener, Search<T> index, Object[] keys) {
		// TODO Auto-generated method stub
		return null;
	}


}