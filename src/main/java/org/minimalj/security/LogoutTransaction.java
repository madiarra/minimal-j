package org.minimalj.security;

import org.minimalj.backend.Backend;
import org.minimalj.transaction.Transaction;

public class LogoutTransaction implements Transaction<Void> {
	private static final long serialVersionUID = 1L;

	public LogoutTransaction() {
	}

	@Override
	public Void execute() {
		Backend.getAuthorization().logout();
		return null;
	}

}
