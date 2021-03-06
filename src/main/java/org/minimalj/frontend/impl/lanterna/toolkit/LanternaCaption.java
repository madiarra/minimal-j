package org.minimalj.frontend.impl.lanterna.toolkit;

import java.util.List;

import com.googlecode.lanterna.gui.Component;
import com.googlecode.lanterna.gui.component.CommonCheckBox;
import com.googlecode.lanterna.gui.component.Label;
import com.googlecode.lanterna.gui.component.Panel;
import com.googlecode.lanterna.gui.layout.HorisontalLayout;
import com.googlecode.lanterna.gui.layout.VerticalLayout;
import com.googlecode.lanterna.terminal.Terminal;

public class LanternaCaption extends Panel {

	private Label validationLabel;
	
	public LanternaCaption(Component component, String caption) {
		super(Panel.Orientation.VERTICAL);
		Panel panel = new Panel();
		panel.setLayoutManager(new HorisontalLayout());
		panel.addComponent(new Label(caption));
		
		validationLabel = new Label("", Terminal.Color.RED);
		panel.addComponent(validationLabel);
		
		addComponent(panel);
		if (component instanceof CommonCheckBox) {
			addComponent(component);
		} else {
			addComponent(component, VerticalLayout.MAXIMIZES_HORIZONTALLY);
		}
	}
	
	public void setValidationMessages(List<String> validationMessages) {
		String text = validationMessages.isEmpty() ? "" : "*";
		if (!text.equals(validationLabel.getText())) {
			validationLabel.setText(text);
		}
	}

}
