/*
 * Copyright 2016-2017 Axioma srl.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.holonplatform.datastore.jpa.tx;

import javax.persistence.EntityManager;

import com.holonplatform.core.datastore.transaction.Transaction;
import com.holonplatform.core.datastore.transaction.TransactionConfiguration;
import com.holonplatform.datastore.jpa.internal.tx.DefaultJpaTransaction;

/**
 * JPA {@link Transaction}.
 *
 * @since 5.1.0
 */
public interface JpaTransaction extends Transaction {

	/**
	 * Start the transaction, configuring the connection.
	 * @throws TransactionException If an error occurred
	 */
	void start() throws TransactionException;

	/**
	 * Finalize the transaction.
	 * @throws TransactionException If an error occurred
	 */
	void end() throws TransactionException;

	/**
	 * Get the {@link EntityManager} which owns the transaction.
	 * @return The transaction {@link EntityManager}
	 */
	EntityManager getEntityManager();

	/**
	 * Get the transaction configuration.
	 * @return the transaction configuration
	 */
	TransactionConfiguration getConfiguration();

	/**
	 * Add a transaction lifecycle handler.
	 * @param handler The handler to add (not null)
	 */
	void addLifecycleHandler(JpaTransactionLifecycleHandler handler);

	/**
	 * Remove a transaction lifecycle handler.
	 * @param handler The handler to remove
	 */
	void removeLifecycleHandler(JpaTransactionLifecycleHandler handler);

	/**
	 * Create a new {@link JpaTransaction}.
	 * @param entityManager EntityManager (not null)
	 * @param configuration Transaction configuration (not null)
	 * @param endTransactionWhenCompleted Whether the transaction should be finalized when completed (i.e. when the
	 *        transaction is committed or rollbacked)
	 */
	static JpaTransaction create(EntityManager entityManager, TransactionConfiguration configuration,
			boolean endTransactionWhenCompleted) {
		return new DefaultJpaTransaction(entityManager, configuration, endTransactionWhenCompleted);
	}

}
