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
import javax.persistence.EntityTransaction;

import com.holonplatform.core.datastore.transaction.TransactionConfiguration;
import com.holonplatform.core.internal.Logger;
import com.holonplatform.core.internal.datastore.transaction.AbstractTransaction;
import com.holonplatform.core.internal.utils.ObjectUtils;
import com.holonplatform.datastore.jpa.internal.JpaDatastoreLogger;

/**
 * Default {@link JpaTransaction} implementation.
 *
 * @since 5.1.0
 */
public class DefaultJpaTransaction extends AbstractTransaction implements JpaTransaction {

	private static final Logger LOGGER = JpaDatastoreLogger.create();

	private final EntityManager entityManager;

	private final TransactionConfiguration configuration;

	private EntityTransaction entityTransaction;

	/**
	 * Constructor
	 * @param entityManager EntityManager (not null)
	 * @param configuration Transaction configuration (not null)
	 */
	public DefaultJpaTransaction(EntityManager entityManager, TransactionConfiguration configuration) {
		super();
		ObjectUtils.argumentNotNull(entityManager, "EntityManager must be not null");
		ObjectUtils.argumentNotNull(configuration, "TransactionConfiguration must be not null");
		this.entityManager = entityManager;
		this.configuration = configuration;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jpa.internal.transaction.JpaTransaction#getEntityManager()
	 */
	@Override
	public EntityManager getEntityManager() {
		return entityManager;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jpa.internal.transaction.JpaTransaction#start()
	 */
	@Override
	public void start() throws TransactionException {
		this.entityTransaction = getEntityManager().getTransaction();
		try {
			// check rollback only
			if (super.isRollbackOnly()) {
				this.entityTransaction.setRollbackOnly();
			}
			// begin transaction
			this.entityTransaction.begin();
		} catch (Exception e) {
			throw new TransactionException("Failed to start transaction", e);
		}
		LOGGER.debug(() -> "JPA transaction started");
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jpa.internal.transaction.JpaTransaction#end()
	 */
	@Override
	public void end() throws TransactionException {
		if (entityTransaction == null) {
			throw new IllegalTransactionStatusException("Cannot end the transaction: the transaction was not started");
		}
		// check completed
		if (!isCompleted()) {
			if (isRollbackOnly()) {
				rollback();
			} else {
				if (getConfiguration().isAutoCommit()) {
					commit();
				}
			}
		}
		LOGGER.debug(() -> "Jpa transaction finalized");
	}

	/**
	 * Get the transaction configuration.
	 * @return the transaction configuration
	 */
	@Override
	public TransactionConfiguration getConfiguration() {
		return configuration;
	}

	/**
	 * Get whether the transaction is active.
	 * @return whether the transaction is active
	 */
	protected boolean isActive() {
		return entityTransaction != null && entityTransaction.isActive();
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.datastore.transaction.Transaction#commit()
	 */
	@Override
	public void commit() throws TransactionException {
		if (!isActive()) {
			throw new IllegalTransactionStatusException("Cannot commit the transaction: the transaction is not active");
		}
		if (isCompleted()) {
			throw new IllegalTransactionStatusException(
					"Cannot commit the transaction: the transaction is already completed");
		}
		try {
			// check rollback only
			if (isRollbackOnly()) {
				rollback();
			} else {
				entityTransaction.commit();
				LOGGER.debug(() -> "Jpa transaction committed");
			}
		} catch (Exception e) {
			throw new TransactionException("Failed to commit the transaction", e);
		}
		setCompleted();
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.datastore.transaction.Transaction#rollback()
	 */
	@Override
	public void rollback() throws TransactionException {
		if (!isActive()) {
			throw new IllegalTransactionStatusException(
					"Cannot rollback the transaction: the transaction is not active");
		}
		if (isCompleted()) {
			throw new IllegalTransactionStatusException(
					"Cannot rollback the transaction: the transaction is already completed");
		}
		try {
			entityTransaction.rollback();
			LOGGER.debug(() -> "Jpa transaction rolled back");
		} catch (Exception e) {
			throw new TransactionException("Failed to rollback the transaction", e);
		}
		setCompleted();
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.internal.datastore.transaction.AbstractTransaction#setRollbackOnly()
	 */
	@Override
	public void setRollbackOnly() {
		super.setRollbackOnly();
		if (entityTransaction != null) {
			entityTransaction.setRollbackOnly();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.internal.datastore.transaction.AbstractTransaction#isRollbackOnly()
	 */
	@Override
	public boolean isRollbackOnly() {
		if (entityTransaction != null && entityTransaction.getRollbackOnly()) {
			return true;
		}
		return super.isRollbackOnly();
	}

}
