package ch.openech.mj.toolkit;

import java.util.List;

import ch.openech.mj.edit.fields.Focusable;

public interface ComboBox<T> extends IComponent, Focusable {

	public void setObjects(List<T> object);
	
	public void setSelectedObject(T object) throws IllegalArgumentException;

	public T getSelectedObject();

}
