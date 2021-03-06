package org.minimalj.example.petclinic.frontend;

import java.util.List;

import org.minimalj.backend.Backend;
import org.minimalj.example.petclinic.model.Owner;
import org.minimalj.frontend.page.ObjectPage;
import org.minimalj.frontend.page.SearchPage.SimpleSearchPage;
import org.minimalj.transaction.criteria.By;

public class OwnerSearchPage extends SimpleSearchPage<Owner> {

	private static final Object[] keys = {Owner.$.person.getName(), Owner.$.address, Owner.$.city, Owner.$.telephone};
	
	public OwnerSearchPage(String query) {
		super(query, keys);
	}

	@Override
	protected List<Owner> load(String query) {
		return Backend.read(Owner.class, By.search(query), 100);
	}

	@Override
	public ObjectPage<Owner> createDetailPage(Owner owner) {
		return new OwnerPage(owner);
	}
}
