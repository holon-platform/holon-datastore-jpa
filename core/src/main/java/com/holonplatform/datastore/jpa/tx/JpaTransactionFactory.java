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

import jakarta.persistence.EntityManager;

import com.holonplatform.core.datastore.transaction.TransactionConfiguration;
import com.holonplatform.core.datastore.transaction.TransactionStatus.TransactionException;

/**
 * Factory to create and configure new {@link JpaTransaction} implementation using an {@link EntityManager} and a
 * {@link TransactionConfiguration} definition.
 *
 * @since 5.2.0
 */
@FunctionalInterface
public interface JpaTransactionFactory {

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
	 * Get the default {@link JpaTransactionFactory}.
	 * @return the default {@link JpaTransactionFactory}
	 */
	static JpaTransactionFactory getDefault() {
		return (entityManager, configuration) -> JpaTransaction.create(entityManager, configuration);
	}

}
