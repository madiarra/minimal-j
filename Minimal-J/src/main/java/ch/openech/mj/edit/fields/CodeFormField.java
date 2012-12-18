package ch.openech.mj.edit.fields;

import java.util.List;

import ch.openech.mj.db.model.EnumUtils;
import ch.openech.mj.db.model.PropertyInterface;
import ch.openech.mj.toolkit.ClientToolkit;
import ch.openech.mj.toolkit.IComponent;
import ch.openech.mj.toolkit.TextField;

// TODO: Typisierung bringt hier so was von nichts
public class CodeFormField<E extends Enum<E>> extends AbstractEditField<E> {
	
	private final TextField textFieldDisabled;

	public CodeFormField(PropertyInterface property) {
		this(property, null);
	}

	@SuppressWarnings("unchecked")
	public CodeFormField(PropertyInterface property, List<E> allowedValues) {
		super(property, true);
		
		textFieldDisabled = ClientToolkit.getToolkit().createReadOnlyTextField();
	}
	
	@Override
	public IComponent getComponent() {
		return textFieldDisabled;
	}

	@Override
	public E getObject() {
		throw new IllegalStateException();
	}

	@Override
	public void setObject(E value) {
		textFieldDisabled.setText(EnumUtils.getText(value));
	}
	
}