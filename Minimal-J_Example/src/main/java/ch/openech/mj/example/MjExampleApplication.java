package ch.openech.mj.example;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import ch.openech.mj.application.MjApplication;
import ch.openech.mj.example.model.Book;
import ch.openech.mj.example.model.Customer;
import ch.openech.mj.example.model.Lend;
import ch.openech.mj.example.page.BookTablePage;
import ch.openech.mj.example.page.CustomerTablePage;
import ch.openech.mj.page.EditorPageAction;
import ch.openech.mj.page.PageContext;
import ch.openech.mj.toolkit.IAction;

public class MjExampleApplication extends MjApplication {

	public MjExampleApplication() {
	}
	
	@Override
	public ResourceBundle getResourceBundle() {
		return ResourceBundle.getBundle("ch.openech.mj.example.MjExampleApplication");
	}

	@Override
	public List<IAction> getActionsNew(PageContext context) {
		List<IAction> items = new ArrayList<>();
		items.add(new EditorPageAction(new AddBookEditor()));
		items.add(new EditorPageAction(new AddCustomerEditor()));
		items.add(new EditorPageAction(new AddLendEditor()));
		return items;
	}

	@Override
	public String getWindowTitle(PageContext pageContext) {
		return "Minimal-J Example Application";
	}

	@Override
	public Class<?>[] getSearchClasses() {
		return new Class<?>[]{BookTablePage.class, CustomerTablePage.class};
	}

	@Override
	public Class<?> getPreferencesClass() {
		return null;
	}

	@Override
	public Class<?>[] getEntityClasses() {
		return new Class<?>[]{Book.class, Customer.class, Lend.class};
	}
}
