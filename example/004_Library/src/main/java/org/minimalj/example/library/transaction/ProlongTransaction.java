package org.minimalj.example.library.transaction;

import java.io.Serializable;

import org.minimalj.transaction.Transaction;

public class ProlongTransaction implements Transaction<Serializable> {
	private static final long serialVersionUID = 1L;

	@Override
	public Serializable execute() {
		return null;
	}

}
