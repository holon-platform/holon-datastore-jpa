/*
 * Copyright 2000-2016 Holon TDCN.
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
package com.holonplatform.jpa.spring.internal;

import java.util.Properties;

import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;

/**
 * Configurator for JpaTransactionManager beans
 * 
 * @since 5.0.0
 */
class TransactionManagerConfigurator {

	/*
	 * Data context id
	 */
	protected String dataContextId;
	/*
	 * Vendor-specific jpa properties
	 */
	protected Properties jpaProperties;
	/*
	 * Transaction synch mode
	 */
	private Integer transactionSynchronization;
	/*
	 * Default transaction timeout
	 */
	private Integer defaultTimeout;
	/*
	 * Enforce transaction validation
	 */
	private Boolean validateExistingTransaction;
	/*
	 * Global rollback setting
	 */
	private Boolean globalRollbackOnParticipationFailure;
	/*
	 * Fail early setting
	 */
	private Boolean failEarlyOnGlobalRollbackOnly;
	/*
	 * Rollback on commit behaviour
	 */
	private Boolean rollbackOnCommitFailure;

	/**
	 * Set data context id. Data context id will be setted as JpaTransactionManager <code>PersistenceUnit</code> name
	 * too.
	 * @param dataContextId Data context id
	 */
	public void setDataContextId(String dataContextId) {
		this.dataContextId = dataContextId;
	}

	/**
	 * Set vendor-specific JPA property to be passed into {@code EntityManagerFactory.createEntityManager(Map)}
	 * @param name Property name
	 * @param value Property value
	 */
	public void addJpaProperty(String name, Object value) {
		if (name != null) {
			if (jpaProperties == null) {
				jpaProperties = new Properties();
			}
			jpaProperties.put(name, value);
		}
	}

	/**
	 * Set when this transaction manager should activate the thread-bound transaction synchronization support. Default
	 * is "always".
	 * <p>
	 * Note that transaction synchronization isn't supported for multiple concurrent transactions by different
	 * transaction managers. Only one transaction manager is allowed to activate it at any time.
	 * @see AbstractPlatformTransactionManager#SYNCHRONIZATION_ALWAYS
	 * @see AbstractPlatformTransactionManager#SYNCHRONIZATION_ON_ACTUAL_TRANSACTION
	 * @see AbstractPlatformTransactionManager#SYNCHRONIZATION_NEVER
	 * @param transactionSynchronization Transaction synchronization mode
	 */
	public void setTransactionSynchronization(int transactionSynchronization) {
		this.transactionSynchronization = transactionSynchronization;
	}

	/**
	 * Specify the default timeout that this transaction manager should apply if there is no timeout specified at the
	 * transaction level, in seconds.
	 * <p>
	 * Default is the underlying transaction infrastructure's default timeout, e.g. typically 30 seconds in case of a
	 * JTA provider, indicated by the {@code TransactionDefinition.TIMEOUT_DEFAULT} value.
	 * </p>
	 * @param defaultTimeout Default timeout in seconds
	 */
	public void setDefaultTimeout(int defaultTimeout) {
		this.defaultTimeout = defaultTimeout;
	}

	/**
	 * Set whether existing transactions should be validated before participating in them.
	 * <p>
	 * When participating in an existing transaction (e.g. with PROPAGATION_REQUIRES or PROPAGATION_SUPPORTS
	 * encountering an existing transaction), this outer transaction's characteristics will apply even to the inner
	 * transaction scope. Validation will detect incompatible isolation level and read-only settings on the inner
	 * transaction definition and reject participation accordingly through throwing a corresponding exception.
	 * </p>
	 * <p>
	 * Default is "false", leniently ignoring inner transaction settings, simply overriding them with the outer
	 * transaction's characteristics. Switch this flag to "true" in order to enforce strict validation.
	 * </p>
	 * @param validateExistingTransaction True to enable existing transactions validation
	 */
	public void setValidateExistingTransaction(boolean validateExistingTransaction) {
		this.validateExistingTransaction = validateExistingTransaction;
	}

	/**
	 * Set whether to globally mark an existing transaction as rollback-only after a participating transaction failed.
	 * <p>
	 * Default is "true": If a participating transaction (e.g. with PROPAGATION_REQUIRES or PROPAGATION_SUPPORTS
	 * encountering an existing transaction) fails, the transaction will be globally marked as rollback-only. The only
	 * possible outcome of such a transaction is a rollback: The transaction originator <i>cannot</i> make the
	 * transaction commit anymore.
	 * </p>
	 * <p>
	 * Switch this to "false" to let the transaction originator make the rollback decision. If a participating
	 * transaction fails with an exception, the caller can still decide to continue with a different path within the
	 * transaction. However, note that this will only work as long as all participating resources are capable of
	 * continuing towards a transaction commit even after a data access failure: This is generally not the case for a
	 * Hibernate Session, for example; neither is it for a sequence of JDBC insert/update/delete operations.
	 * </p>
	 * <p>
	 * <b>Note:</b>This flag only applies to an explicit rollback attempt for a subtransaction, typically caused by an
	 * exception thrown by a data access operation (where TransactionInterceptor will trigger a
	 * {@code PlatformTransactionManager.rollback()} call according to a rollback rule). If the flag is off, the caller
	 * can handle the exception and decide on a rollback, independent of the rollback rules of the subtransaction. This
	 * flag does, however, <i>not</i> apply to explicit {@code setRollbackOnly} calls on a {@code TransactionStatus},
	 * which will always cause an eventual global rollback (as it might not throw an exception after the rollback-only
	 * call).
	 * </p>
	 * <p>
	 * The recommended solution for handling failure of a subtransaction is a "nested transaction", where the global
	 * transaction can be rolled back to a savepoint taken at the beginning of the subtransaction. PROPAGATION_NESTED
	 * provides exactly those semantics; however, it will only work when nested transaction support is available. This
	 * is the case with DataSourceTransactionManager, but not with JtaTransactionManager.
	 * </p>
	 * @param globalRollbackOnParticipationFailure True to activate
	 */
	public void setGlobalRollbackOnParticipationFailure(boolean globalRollbackOnParticipationFailure) {
		this.globalRollbackOnParticipationFailure = globalRollbackOnParticipationFailure;
	}

	/**
	 * Set whether to fail early in case of the transaction being globally marked as rollback-only.
	 * <p>
	 * Default is "false", only causing an UnexpectedRollbackException at the outermost transaction boundary. Switch
	 * this flag on to cause an UnexpectedRollbackException as early as the global rollback-only marker has been first
	 * detected, even from within an inner transaction boundary.
	 * </p>
	 * <p>
	 * Note that, as of Spring 2.0, the fail-early behavior for global rollback-only markers has been unified: All
	 * transaction managers will by default only cause UnexpectedRollbackException at the outermost transaction
	 * boundary. This allows, for example, to continue unit tests even after an operation failed and the transaction
	 * will never be completed. All transaction managers will only fail earlier if this flag has explicitly been set to
	 * "true".
	 * </p>
	 * @param failEarlyOnGlobalRollbackOnly True to set
	 */
	public void setFailEarlyOnGlobalRollbackOnly(boolean failEarlyOnGlobalRollbackOnly) {
		this.failEarlyOnGlobalRollbackOnly = failEarlyOnGlobalRollbackOnly;
	}

	/**
	 * Set whether {@code doRollback} should be performed on failure of the {@code doCommit} call. Typically not
	 * necessary and thus to be avoided, as it can potentially override the commit exception with a subsequent rollback
	 * exception.
	 * <p>
	 * Default is "false".
	 * </p>
	 * @param rollbackOnCommitFailure True to activate
	 */
	public void setRollbackOnCommitFailure(boolean rollbackOnCommitFailure) {
		this.rollbackOnCommitFailure = rollbackOnCommitFailure;
	}

	/**
	 * Configure given bean using provided configuration settings
	 * @param tm Bean to configure
	 */
	public void configure(JpaTransactionManager tm) {

		// data context id
		if (dataContextId != null) {
			tm.setPersistenceUnitName(dataContextId);
		}

		// jpa properties
		if (jpaProperties != null) {
			tm.setJpaProperties(jpaProperties);
		}

		// settings
		if (transactionSynchronization != null) {
			tm.setTransactionSynchronization(transactionSynchronization);
		}
		if (defaultTimeout != null) {
			tm.setDefaultTimeout(defaultTimeout);
		}
		if (validateExistingTransaction != null) {
			tm.setValidateExistingTransaction(validateExistingTransaction);
		}
		if (globalRollbackOnParticipationFailure != null) {
			tm.setGlobalRollbackOnParticipationFailure(globalRollbackOnParticipationFailure);
		}
		if (failEarlyOnGlobalRollbackOnly != null) {
			tm.setFailEarlyOnGlobalRollbackOnly(failEarlyOnGlobalRollbackOnly);
		}
		if (rollbackOnCommitFailure != null) {
			tm.setRollbackOnCommitFailure(rollbackOnCommitFailure);
		}

	}

}
