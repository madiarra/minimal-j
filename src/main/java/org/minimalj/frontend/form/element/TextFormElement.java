package org.minimalj.frontend.form.element;

import java.util.Locale;

import org.minimalj.frontend.toolkit.ClientToolkit;
import org.minimalj.frontend.toolkit.ClientToolkit.IComponent;
import org.minimalj.frontend.toolkit.ClientToolkit.Input;
import org.minimalj.model.EnumUtils;
import org.minimalj.model.Rendering;
import org.minimalj.model.Rendering.RenderType;
import org.minimalj.model.properties.PropertyInterface;

/**
 * Read only FormElement. This element is used as a complement
 * for elements providing only editable mode. For those elements
 * the Form (ElementFactory) has to construct the right element.
 *
 */
public class TextFormElement implements FormElement<Object> {

	private final PropertyInterface property;

	protected final Input<String> textField;

	public TextFormElement(PropertyInterface property) {
		this.property = property;
		this.textField = ClientToolkit.getToolkit().createReadOnlyTextField();
	}

	@Override
	public PropertyInterface getProperty() {
		return property;
	}

	@Override
	public IComponent getComponent() {
		return textField;
	}

	@Override
	public Object getValue() {
		throw new RuntimeException("getObject() on TextElement " + this.getClass().getSimpleName() + " must not be called");
	}

	@Override
	public void setChangeListener(FormElementListener changeListener) {
		// ignored
	}

	@Override
	public void setValue(Object object) {
		if (object instanceof Rendering) {
			Rendering rendering = (Rendering) object;
			textField.setValue(rendering.render(RenderType.PLAIN_TEXT, Locale.getDefault()));
		} else if (object instanceof Enum) {
			textField.setValue(EnumUtils.getText((Enum) object));
		} else if (object != null) {
			textField.setValue(object.toString());
		} else {
			textField.setValue("");
		}
	}

}