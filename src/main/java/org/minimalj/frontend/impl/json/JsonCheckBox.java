package org.minimalj.frontend.impl.json;

import org.minimalj.frontend.Frontend.InputComponentListener;

public class JsonCheckBox extends JsonInputComponent<Boolean> {

	public JsonCheckBox(String text, InputComponentListener changeListener) {
		super("CheckBox", changeListener);
		put("text", text);
	}

	@Override
	public void setValue(Boolean selected) {
		put(VALUE, Boolean.toString(selected));
	}

	@Override
	public Boolean getValue() {
		return Boolean.TRUE.toString().equals(get(VALUE));
	}
}
