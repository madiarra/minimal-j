package org.minimalj.frontend.impl.lanterna.toolkit;

import org.minimalj.frontend.Frontend.Input;
import org.minimalj.frontend.Frontend.InputComponentListener;
import org.minimalj.util.StringUtils;

import com.googlecode.lanterna.gui.component.InteractableComponent;
import com.googlecode.lanterna.gui.component.TextBox;
import com.googlecode.lanterna.gui.listener.ComponentAdapter;
import com.googlecode.lanterna.input.Key;

public class LanternaTextField extends TextBox implements Input<String> {

	private final InputComponentListener changeListener;
	
	private String textOnFocusLost;

	public LanternaTextField(InputComponentListener changeListener) {
		this.changeListener = changeListener;
		addComponentListener(new TextFieldComponentListener());
	}

	@Override
	public void setEditable(boolean editable) {
		super.setVisible(editable);
	}

	@Override
	public String getValue() {
		String text = super.getText();
		if (text.length() == 0) return null;
		return text;
	}

	@Override
	public void setValue(String text) {
		if (text == null) {
			text = "";
		}
		textOnFocusLost = text;
		if (!hasFocus()) {
			super.setText(text);
		}
	}
	
	private void fireChangeEvent() {
		changeListener.changed(LanternaTextField.this);
	}
	
	@Override
	public Result keyboardInteraction(Key key) {
		Result result = super.keyboardInteraction(key);
		if (result != Result.EVENT_NOT_HANDLED) {
			fireChangeEvent();
		}
		return result;
	}

	private class TextFieldComponentListener extends ComponentAdapter {

		@Override
		public void onComponentReceivedFocus(InteractableComponent interactableComponent) {
			textOnFocusLost = getText();
		}
		
		@Override
		public void onComponentLostFocus(InteractableComponent interactableComponent) {
			if (!StringUtils.equals(textOnFocusLost, getText())) {
				setText(textOnFocusLost);
			}
		}
	}
	
}
