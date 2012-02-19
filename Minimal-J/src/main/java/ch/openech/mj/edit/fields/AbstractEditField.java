package ch.openech.mj.edit.fields;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import ch.openech.mj.db.model.Constants;
import ch.openech.mj.edit.ChangeableValue;
import ch.openech.mj.edit.validation.Indicator;
import ch.openech.mj.toolkit.ClientToolkit;
import ch.openech.mj.toolkit.IComponent;
import ch.openech.mj.toolkit.IComponentDelegate;
import ch.openech.mj.util.StringUtils;

public abstract class AbstractEditField<T> implements IComponentDelegate, EditField<T>, Indicator {

	private final String name;
	
	private ChangeListener forwardingChangeListener;
	private ChangeListener changeListener;
	private boolean adjusting = false;
	
	protected AbstractEditField(Object key) {
		this.name = Constants.getConstant(key);
	}

	@Override
	public String getName() {
		return name;
	}
	
	//
	
	@Override
	public boolean isEmpty() {
		Object object = getObject();
		return object == null || (object instanceof String) && StringUtils.isEmpty((String) object);
	}
	
	// Listener
	
	public void setAdjusting(boolean adjusting) {
		this.adjusting = adjusting;
	}

	protected void listenTo(ChangeableValue<?> changeable) {
		changeable.setChangeListener(listener());
	}

	protected ChangeListener listener() {
		if (forwardingChangeListener == null) {
			forwardingChangeListener = new ForwardingChangeListener();
		}
		return forwardingChangeListener;
	}
	
	@Override
	public void setChangeListener(ChangeListener changeListener) {
		this.changeListener = changeListener;
	}

	protected void fireChange() {
		if (!adjusting && changeListener != null) {
			changeListener.stateChanged(new ChangeEvent(AbstractEditField.this));
		}
	}
	
	private class ForwardingChangeListener implements ChangeListener {
		@Override
		public void stateChanged(ChangeEvent e) {
			fireChange();
		}
	}
	
	//
	
	protected static void showBubble(IComponent component, String text) {
		ClientToolkit.getToolkit().showNotification(component, text);
	}
	
}
