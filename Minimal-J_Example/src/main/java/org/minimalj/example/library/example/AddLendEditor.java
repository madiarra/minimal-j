package org.minimalj.example.library.example;

import org.minimalj.backend.Backend;
import org.minimalj.example.library.model.Customer;
import org.minimalj.example.library.model.Lend;
import org.minimalj.frontend.edit.Editor;
import org.minimalj.frontend.edit.form.IForm;

public class AddLendEditor extends Editor<Lend> {

	private Customer startWithCustomer;
	
	public AddLendEditor() {
		// empty
	}
	
	public AddLendEditor(Customer customer) {
		this.startWithCustomer = customer;
	}
	
	@Override
	public IForm<Lend> createForm() {
		return new LendForm(true);
	}
	
	@Override
	protected Lend newInstance() {
		Lend lend = new Lend();
		lend.customer = startWithCustomer;
		return lend;
	}

	@Override
	public String save(Lend lend) throws Exception {
		Backend.getInstance().insert(lend);
		return "";
	}

	@Override
	public String getTitle() {
		return "Ausleihe hinzufügen";
	}

}