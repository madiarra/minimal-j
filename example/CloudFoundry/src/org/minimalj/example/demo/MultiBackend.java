package org.minimalj.example.demo;

import java.util.HashMap;
import java.util.Map;

import org.minimalj.backend.Backend;
import org.minimalj.backend.sql.SqlBackend;
import org.minimalj.transaction.Transaction;

public class MultiBackend extends Backend {

	private final Map<String, Backend> backends = new HashMap<>();

	@Override
	public <T> T doExecute(Transaction<T> transaction) {
		return getBackend().doExecute(transaction);
	}

	private Backend getBackend() {
		String context = DemoContext.getContext();
		if (!backends.containsKey(context)) {
			backends.put(context, new SqlBackend());
		}
		return backends.get(context);
	}
	
}
