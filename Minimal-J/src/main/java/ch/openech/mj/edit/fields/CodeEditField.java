package ch.openech.mj.edit.fields;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.text.JTextComponent;

import ch.openech.mj.autofill.DemoEnabled;
import ch.openech.mj.db.model.Code;
import ch.openech.mj.db.model.Constants;
import ch.openech.mj.edit.validation.Indicator;
import ch.openech.mj.edit.validation.ValidationMessage;
import ch.openech.mj.swing.PreferencesHelper;
import ch.openech.mj.toolkit.ClientToolkit;
import ch.openech.mj.toolkit.ComboBox;
import ch.openech.mj.toolkit.ContextLayout;
import ch.openech.mj.toolkit.IComponent;
import ch.openech.mj.toolkit.SwitchLayout;
import ch.openech.mj.toolkit.TextField;
import ch.openech.mj.toolkit.TextField.TextFieldFilter;
import ch.openech.mj.util.StringUtils;


public class CodeEditField extends AbstractEditField<String> implements PreferenceAware, DemoEnabled, Indicator {
	private Code code;
	
	private final ContextLayout contextLayout;
	private final SwitchLayout switchLayout;
	private final ComboBox comboBox;
	private final TextField textField;
	private final TextField textFieldUnknown;

	public CodeEditField(Object key, Code code) {
		super(Constants.getConstant(key));
		this.code = code;

		comboBox = ClientToolkit.getToolkit().createComboBox(listener());
		comboBox.setObjects(code.getTexts());
		
		textField = ClientToolkit.getToolkit().createTextField(listener(), new CodeTextFieldFilter(code.getSize()));

		textFieldUnknown = ClientToolkit.getToolkit().createReadOnlyTextField();
		textFieldUnknown.setText(code.getUnknownText());

		switchLayout = ClientToolkit.getToolkit().createSwitchLayout();
		switchLayout.show(comboBox);
		contextLayout = ClientToolkit.getToolkit().createContextLayout(switchLayout);
		
		createMenu();
		setDefault();
	}
	
	@Override
	public Object getComponent() {
		return contextLayout;
	}

	public void createMenu() {
		boolean codesFree = true || PreferencesHelper.preferences() == null || PreferencesHelper.preferences().getBoolean("codesFree", false);
		boolean codesClear = true || PreferencesHelper.preferences() == null || PreferencesHelper.preferences().getBoolean("codesClear", false);
		
		if (codesFree || codesClear) {
			List<Action> actions = new ArrayList<Action>();

			Action select = new AbstractAction("Auswahl " + code.getDisplayName()) {
				@Override
				public void actionPerformed(ActionEvent e) {
					modeChoice();
				}
	        };
	        actions.add(select);
	
	        if (codesClear) {
	        	Action unbekannt = new AbstractAction(code.getDisplayName() + " entfernen") {
	        		@Override
	        		public void actionPerformed(ActionEvent e) {
	        			modeUnknown();
	        			// Man kann zwar nichts editieren, aber der Fokus
	        			// hängt sonst im nirgendwo
	        			textField.requestFocus();
	        		}
	        	};
	        	actions.add(unbekannt);
	        }
	        
	        if (codesFree) {
	        	Action freeEntry = new AbstractAction("Freie Eingabe") {
	        		@Override
	        		public void actionPerformed(ActionEvent e) {
	        			modeFreeEntry();
	        		}
	        	};
	        	actions.add(freeEntry);
	        }
	        
	        contextLayout.setActions(actions);
		}
	}

	@Override
	public String[] getKeys() {
		return new String[]{"codesFree", "codesClear"};
	}

	
	private void modeChoice() {
		int index = code.indexOf(getObject());
		if (index >= 0) {
			comboBox.setSelectedObject(getObject()); 
		} else {
			setDefault();
		}
		switchLayout.show(comboBox);
		comboBox.requestFocus();
	}

	private void modeUnknown() {
		switchLayout.show(textFieldUnknown);
	}

	private void modeFreeEntry() {
		textField.setText(getObject() != null ? getObject() : null);
		textField.requestFocus();
		switchLayout.show(textField);
	}

	private void setDefault() {
        if (!StringUtils.isBlank(code.getDefault())) {
        	setObject(code.getDefault());
        } else {
    		modeUnknown();
        }
	}
	
	@Override
	public String getObject() {
		if (switchLayout.getShownComponent() == comboBox) {
			String text = (String) comboBox.getSelectedObject();
			return code.getKey(text);
		} else if (switchLayout.getShownComponent() == textField) {
			return textField.getText();
		} else{
			return null;
		}
	}

	@Override
	public void setObject(String value) {
		if (StringUtils.isBlank(value)) {
			modeUnknown();
			return;
		}

		int index = code.indexOf(value);
		if (index >= 0) {
			comboBox.setSelectedObject(value);
			switchLayout.show(comboBox);
			return;
		} else {
			switchLayout.show(textField);
			textField.setText(value);
		}
	}

	@Override
	public void fillWithDemoData() {
		if (Math.random() < 0.2) {
			setObject(code.getDefault());
		} else {
			int index = (int)(Math.random() * (double)code.count());
			setObject(code.getKey(index));
		}
	}
	
	private static class CodeTextFieldFilter implements TextFieldFilter {
		private int limit;

		public CodeTextFieldFilter(int limit) {
			this.limit = limit;
		}

		@Override
		public String filter(IComponent textField, String str) {
			if (str == null)
				return null;

			// TODO TextField.isEditable()
			if (textField instanceof JTextComponent && !((JTextComponent) textField).isEditable()) {
				return str;
			}
			if (/* isUnknown() ||  !textField.isEditable() || */ str.length() <= limit) {
				return str;
			} else {
				showBubble(textField, "Eingabe auf " + limit + " Zeichen beschränkt");
				return str.substring(0, limit);
			}
		}
	}

	@Override
	public void setValidationMessages(List<ValidationMessage> validationMessages) {
		textField.setValidationMessages(validationMessages);
		comboBox.setValidationMessages(validationMessages);
	}
	
}
