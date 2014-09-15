package org.minimalj.transaction.persistence;

import org.minimalj.backend.Backend;
import org.minimalj.transaction.Transaction;
import org.minimalj.util.SerializationContainer;

public class DeleteTransaction implements Transaction<Void> {
	private static final long serialVersionUID = 1L;

	private final Object object;

	public DeleteTransaction(Object object) {
		this.object = SerializationContainer.wrap(object);
	}

	@Override
	public Void execute(Backend backend) {
		backend.delete(SerializationContainer.unwrap(object));
		return null;
	}

}