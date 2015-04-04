package org.minimalj.transaction;

import java.io.Serializable;

import org.minimalj.backend.Backend;

/**
 * The transaction is the action the frontend passes to the backend for
 * execution.<p>
 * 
 * For simple CRUD-Transactions there exist shortcuts in the abstract Backend
 * class.<p>
 *
 * @param <T> type of return value.
 * (should extend Serializable, but thats not enforced by 'extends Serializable'
 * because signatures of methods get complicated by that and Void-Transactions
 * would not be possible because Void is not Serializable!)
 */
public interface Transaction<T> extends Serializable {

	/**
	 * The invocation method for the backend. Application code should not need
	 * to call this method directly.
	 * 
	 * @param backend if the transcation needs things from the Backend this
	 * provides the access.
	 * @return the return value from the transaction
	 */
	public T execute(Backend backend);
	
}