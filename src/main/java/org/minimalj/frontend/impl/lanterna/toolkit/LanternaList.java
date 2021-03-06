package org.minimalj.frontend.impl.lanterna.toolkit;

import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.Frontend.IComponent;
import org.minimalj.frontend.Frontend.IList;
import org.minimalj.frontend.action.Action;

import com.googlecode.lanterna.gui.Component;
import com.googlecode.lanterna.gui.component.Panel;
import com.googlecode.lanterna.gui.layout.VerticalLayout;

public class LanternaList extends Panel implements IList {
	
	private final int actionCount;
	
	public LanternaList(Action... actions) {
		setLayoutManager(new VerticalLayout());
		if (actions != null) {
			for (Action action : actions) {
				addComponent((Component) Frontend.getInstance().createText(action));
			}
			actionCount = actions.length;
		} else {
			actionCount = 0;
		}
	}
	
	@Override
	public void clear() {
		for (int i = getComponentCount() - actionCount - 1; i>=0; i--) {
			removeComponent(getComponentAt(i));
		}
	}

	@Override
	public void setEnabled(boolean enabled) {	
		// TODO
		
	}

	@Override
	public void add(IComponent component, Action... actions) {
		super.addComponent((Component) component);
		// TODO actions
	}

}
