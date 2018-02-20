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
package com.holonplatform.datastore.jpa.internal.transaction;

import javax.persistence.EntityManager;

import com.holonplatform.core.datastore.transaction.Transaction.TransactionException;
import com.holonplatform.core.datastore.transaction.TransactionConfiguration;

/**
 * {@link JpaTransaction} implementation provider.
 *
 * @since 5.1.0
 */
@FunctionalInterface
public interface JpaTransactionProvider {

	/**
	 * Build a new {@link JpaTransaction}.
	 * @param entityManager The {@link EntityManager} to use (not null)
	 * @param configuration Configuration (not null)
	 * @return A new {@link JpaTransaction} (not null)
	 * @throws TransactionException If an error occurred
	 */
	JpaTransaction createTransaction(EntityManager entityManager, TransactionConfiguration configuration)
			throws TransactionException;

	/**
	 * Get the default {@link JpaTransactionProvider}.
	 * @return the default {@link JpaTransactionProvider}
	 */
	static JpaTransactionProvider getDefault() {
		return DefaultJpaTransactionProvider.INSTANCE;
	}

}
