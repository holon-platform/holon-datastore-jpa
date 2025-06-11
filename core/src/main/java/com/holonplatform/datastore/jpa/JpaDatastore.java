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

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.FlushModeType;
import jakarta.persistence.LockModeType;

import com.holonplatform.core.config.ConfigProperty;
import com.holonplatform.core.datastore.Datastore;
import com.holonplatform.core.datastore.DatastoreCommodity;
import com.holonplatform.core.datastore.DatastoreCommodityRegistrar;
import com.holonplatform.core.datastore.DatastoreOperations;
import com.holonplatform.core.datastore.transaction.Transactional;
import com.holonplatform.core.query.Query;
import com.holonplatform.datastore.jpa.config.JpaDatastoreCommodityContext;
import com.holonplatform.datastore.jpa.config.JpaDatastoreCommodityFactory;
import com.holonplatform.datastore.jpa.context.EntityManagerHandler;
import com.holonplatform.datastore.jpa.dialect.ORMDialect;
import com.holonplatform.datastore.jpa.internal.DefaultJpaDatastore;
import com.holonplatform.datastore.jpa.tx.JpaTransactionFactory;

/**
 * JPA {@link Datastore}.
 * 
 * @since 5.0.0
 */
public interface JpaDatastore extends Datastore, Transactional, EntityManagerHandler,
		DatastoreCommodityRegistrar<JpaDatastoreCommodityContext> {

	/**
	 * A {@link Query} parameter to set the {@link LockModeType}, using {@link Query#parameter(ConfigProperty, Object)}.
	 */
	public static final ConfigProperty<LockModeType> QUERY_PARAMETER_LOCK_MODE = ConfigProperty
			.create("jpaQueryLockMode", LockModeType.class);

	/**
	 * A {@link Query} parameter to set the {@link FlushModeType}, using
	 * {@link Query#parameter(ConfigProperty, Object)}.
	 */
	public static final ConfigProperty<FlushModeType> QUERY_PARAMETER_FLUSH_MODE = ConfigProperty
			.create("jpaQueryFlushMode", FlushModeType.class);

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
	public interface Builder<D extends JpaDatastore> extends DatastoreOperations.Builder<D, Builder<D>> {

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
		 * Set both the {@link EntityManager} initializer (instance provider) and finalizer using the
		 * {@link EntityManagerLifecycleHandler} convenience interface.
		 * @param entityManagerHandler the {@link EntityManagerLifecycleHandler} to set (not null)
		 * @return this
		 */
		Builder<D> entityManagerHandler(EntityManagerLifecycleHandler entityManagerHandler);

		/**
		 * Set a custom {@link JpaTransactionFactory} to be used by the Datastore to create new transactions.
		 * @param transactionFactory The transaction factory to set (not null)
		 * @return this
		 */
		Builder<D> transactionFactory(JpaTransactionFactory transactionFactory);

		/**
		 * Set the {@link ORMPlatform} to use.
		 * <p>
		 * By default, the ORM platform is auto-detected using the configured {@link EntityManagerFactory}.
		 * </p>
		 * @param platform The ORM platform to set
		 * @return this
		 */
		Builder<D> platform(ORMPlatform platform);

		/**
		 * Set the ORM dialect to use.
		 * <p>
		 * By default, the ORM dialect is auto-detected using the configured {@link EntityManagerFactory}.
		 * </p>
		 * @param dialect The dialect to set (not null)
		 * @return this
		 */
		Builder<D> dialect(ORMDialect dialect);

		/**
		 * Set the fully qualified dialect class name to use as ORM dialect.
		 * <p>
		 * By default, the ORM dialect is auto-detected using the configured {@link EntityManagerFactory}.
		 * </p>
		 * @param dialectClassName The dialect class name to set (not null)
		 * @return this
		 */
		Builder<D> dialect(String dialectClassName);

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

		/**
		 * Register a {@link JpaDatastoreCommodityFactory}.
		 * @param <C> Commodity type
		 * @param commodityFactory The factory to register (not null)
		 * @return this
		 */
		<C extends DatastoreCommodity> Builder<D> withCommodity(JpaDatastoreCommodityFactory<C> commodityFactory);

	}

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

		/**
		 * Create a default {@link EntityManagerInitializer}, which uses
		 * {@link EntityManagerFactory#createEntityManager()} to create a new {@link EntityManager} instance.
		 * @return A default {@link EntityManagerInitializer}
		 */
		static EntityManagerInitializer createDefault() {
			return emf -> emf.createEntityManager();
		}

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

		/**
		 * Create a default {@link EntityManagerFinalizer}, which invokes the {@link EntityManager#close()} method to
		 * finalize the {@link EntityManager} instance.
		 * @return A default {@link EntityManagerFinalizer}
		 */
		static EntityManagerFinalizer createDefault() {
			return em -> em.close();
		}

	}

	/**
	 * Convenience interface which combines {@link EntityManagerInitializer} and {@link EntityManagerFinalizer}.
	 * 
	 * @since 5.2.0
	 */
	public interface EntityManagerLifecycleHandler extends EntityManagerInitializer, EntityManagerFinalizer {

	}

}
