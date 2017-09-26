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
package com.holonplatform.datastore.jpa;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.LockModeType;

import com.holonplatform.core.datastore.Datastore;
import com.holonplatform.core.datastore.DatastoreCommodityRegistrar;
import com.holonplatform.core.exceptions.DataAccessException;
import com.holonplatform.core.query.Query;
import com.holonplatform.datastore.jpa.config.JpaDatastoreCommodityContext;
import com.holonplatform.datastore.jpa.internal.DefaultJpaDatastore;

/**
 * JPA {@link Datastore}.
 * 
 * @since 5.0.0
 */
public interface JpaDatastore extends Datastore, DatastoreCommodityRegistrar<JpaDatastoreCommodityContext> {

	/**
	 * {@link Query} parameter to set lock mode (use {@link Query#parameter(String, Object)} to set query parameters).
	 * <p>
	 * Value must be {@link LockModeType} enum value.
	 * </p>
	 */
	public static final String QUERY_PARAMETER_LOCK_MODE = "jpaQueryLockMode";

	/**
	 * Execute given <code>operation</code> using an {@link EntityManager} instance provided by the Datastore and return
	 * the operation result.
	 * <p>
	 * The {@link EntityManager} lifecycle is managed by Datastore, obtaining an instance using
	 * {@link EntityManagerInitializer} and performing any close operation using {@link EntityManagerFinalizer}.
	 * </p>
	 * @param <R> Operation result type
	 * @param operation The operation to execute (not null)
	 * @return Operation result
	 * @throws IllegalStateException If a {@link EntityManagerFactory} is not available
	 * @throws DataAccessException If an error occurred during {@link EntityManager} management or operation execution
	 */
	<R> R withEntityManager(EntityManagerOperation<R> operation);

	/**
	 * Represents an operation to be executed using a Datastore managed {@link EntityManager}.
	 * @param <R> Operation result type
	 */
	public interface EntityManagerOperation<R> {

		/**
		 * Execute an operation and returns a result.
		 * @param entityManager EntityManager to use
		 * @return Operation result
		 * @throws Exception If an operation execution error occurred
		 */
		R execute(EntityManager entityManager) throws Exception;

	}

	// Builder

	/**
	 * Get a builder to create a {@link JpaDatastore} instance.
	 * @return Datastore builder
	 */
	static Builder<JpaDatastore> builder() {
		return new DefaultJpaDatastore.DefaultBuilder();
	}

	/**
	 * {@link JpaDatastore} builder.
	 */
	public interface Builder<D extends JpaDatastore> extends Datastore.Builder<D, Builder<D>> {

		/**
		 * Set the {@link EntityManagerFactory} to use to obtain {@link EntityManager}s used for datastore operations.
		 * @param entityManagerFactory The EntityManagerFactory to set (not null)
		 * @return this
		 */
		Builder<D> entityManagerFactory(EntityManagerFactory entityManagerFactory);

		/**
		 * Sets the {@link EntityManager} instance provider for Datastore operations execution.
		 * @param entityManagerInitializer the {@link EntityManagerInitializer} to set (not null)
		 * @return this
		 */
		Builder<D> entityManagerInitializer(EntityManagerInitializer entityManagerInitializer);

		/**
		 * Sets the {@link EntityManager} finalizer to use after Datastore operations execution.
		 * @param entityManagerFinalizer the {@link EntityManagerFinalizer} to set
		 * @return this
		 */
		Builder<D> entityManagerFinalizer(EntityManagerFinalizer entityManagerFinalizer);

		/**
		 * Set whether to auto-flush mode is enabled. When auto-flush mode is enabled, {@link EntityManager#flush()} is
		 * called after each Datastore data manipulation operation, such as <code>save</code> or <code>delete</code>.
		 * <p>
		 * Default is <code>false</code>.
		 * </p>
		 * @param autoFlush <code>true</code> to enable the auto-flush mode, <code>false</code> to disable
		 * @return this
		 */
		Builder<D> autoFlush(boolean autoFlush);

	}

	// Support

	/**
	 * Interface to provide {@link EntityManager} instance to use when executing a Datastore operation
	 */
	@FunctionalInterface
	public interface EntityManagerInitializer {

		/**
		 * Get/create the {@link EntityManager} instance to use when executing a Datastore operation
		 * @param entityManagerFactory EntityManagerFactory bound to the Datastore
		 * @return EntityManager to use
		 */
		EntityManager getEntityManager(EntityManagerFactory entityManagerFactory);

	}

	/**
	 * Interface to perform any close/finalize operation on the {@link EntityManager} instance used for a Datastore
	 * operation execution
	 */
	@FunctionalInterface
	public interface EntityManagerFinalizer {

		/**
		 * Get/create the {@link EntityManager} instance to use when executing a Datastore operation
		 * @param entityManager EntityManager to finalize
		 */
		void finalizeEntityManager(EntityManager entityManager);

	}

}
