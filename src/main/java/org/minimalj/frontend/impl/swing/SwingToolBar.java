package org.minimalj.frontend.impl.swing;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.JToolBar;

import org.minimalj.application.Application;
import org.minimalj.frontend.impl.swing.toolkit.SwingFrontend;
import org.minimalj.frontend.page.Page;

public class SwingToolBar extends JToolBar {
	private static final long serialVersionUID = 1L;
	
	private final SwingTab tab;
	private JTextField textFieldSearch;
	private SearchAction searchAction;

	public SwingToolBar(SwingTab tab) {
		super();
		this.tab = tab;
		
		searchAction = new SearchAction();		
		setFloatable(false);
		fillToolBar();
	}
	
	protected void fillToolBar() {
		fillToolBarNavigation();
		fillToolBarRefresh();
		add(Box.createHorizontalGlue());
		fillToolBarSearch();
	}
	
	protected void fillToolBarNavigation() {
		add(tab.previousAction);
		add(tab.nextAction);
	}
	
	protected void fillToolBarRefresh() {
		add(tab.refreshAction);
	}
	
	protected void fillToolBarSearch() {
		textFieldSearch = new JTextField();
		textFieldSearch.setPreferredSize(new Dimension(200, textFieldSearch.getPreferredSize().height));
		textFieldSearch.setMaximumSize(textFieldSearch.getPreferredSize());
		add(textFieldSearch);
		JButton button = add(searchAction);
		button.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
		textFieldSearch.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SwingFrontend.pushContext();
				try {
					button.doClick();
				} finally {
					SwingFrontend.popContext();
				}
			}
		});
	}
	
	protected class SearchAction extends SwingResourceAction {
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			SwingFrontend.pushContext();
			try {
				String query = textFieldSearch.getText();
				Page page = Application.getApplication().createSearchPage(query);
				tab.show(page);
			} finally {
				SwingFrontend.popContext();
			}
		}
	}
	
	void onHistoryChanged() {
		// nothing to do right now
	}
	
}
