package org.minimalj.frontend.edit.fields;

import java.util.List;

import org.minimalj.frontend.toolkit.ClientToolkit;
import org.minimalj.frontend.toolkit.ClientToolkit.IComponent;
import org.minimalj.frontend.toolkit.ComboBox;
import org.minimalj.frontend.toolkit.SwitchComponent;
import org.minimalj.frontend.toolkit.TextField;
import org.minimalj.model.PropertyInterface;
import org.minimalj.model.annotation.Code;
import org.minimalj.util.Codes;
import org.minimalj.util.DemoEnabled;

public class CodeEditField extends AbstractEditField<Code> implements Enable, DemoEnabled {
	private final List<Code> codes;
	
	private final SwitchComponent switchComponent;
	private final ComboBox<Code> comboBox;
	private final TextField textFieldDisabled;

	public CodeEditField(PropertyInterface property, String prefix) {
		super(property, true);
		codes = Codes.get((Class<Code>) property.getFieldClazz());
		
		comboBox = ClientToolkit.getToolkit().createComboBox(listener());
		comboBox.setObjects(codes);
		
		textFieldDisabled = ClientToolkit.getToolkit().createReadOnlyTextField();
		textFieldDisabled.setText("-");
		
		switchComponent = ClientToolkit.getToolkit().createSwitchComponent(comboBox, textFieldDisabled);
		switchComponent.show(comboBox);
	}
	
	@Override
	public IComponent getComponent() {
		return switchComponent;
	}

	@Override
	public void setEnabled(boolean enabled) {
		if (enabled) {
			switchComponent.show(comboBox);
		} else {
			switchComponent.show(textFieldDisabled);
		}
	}

	@Override
	public Code getObject() {
		if (switchComponent.getShownComponent() == comboBox) {
			return comboBox.getSelectedObject();
		}
		return null;
	}

	@Override
	public void setObject(Code value) {
		if (switchComponent.getShownComponent() == textFieldDisabled) {
			return;
		}
		comboBox.setSelectedObject(value);
	}

	@Override
	public void fillWithDemoData() {
		int index = (int)(Math.random() * (double)codes.size());
		setObject(codes.get(index));
	}
	
}
