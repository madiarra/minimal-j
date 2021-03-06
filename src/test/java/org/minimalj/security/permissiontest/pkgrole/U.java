package org.minimalj.security.permissiontest.pkgrole;

import org.minimalj.backend.Persistence;
import org.minimalj.transaction.PersistenceTransaction;
import org.minimalj.transaction.Role;

@Role("transactionRole")
public class U<ENTITY, RETURN> extends PersistenceTransaction<ENTITY, RETURN> {
	private static final long serialVersionUID = 1L;

	public U() {
	}

	@Override
	protected RETURN execute(Persistence persistence) {
		return null;
	}
}

