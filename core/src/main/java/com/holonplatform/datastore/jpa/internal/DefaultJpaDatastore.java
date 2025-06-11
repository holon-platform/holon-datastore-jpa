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
package com.holonplatform.datastore.jpa.internal;

import java.util.Optional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import com.holonplatform.core.Expression;
import com.holonplatform.core.ExpressionResolver;
import com.holonplatform.core.beans.BeanIntrospector;
import com.holonplatform.core.datastore.DatastoreCommodity;
import com.holonplatform.core.datastore.DatastoreConfigProperties;
import com.holonplatform.core.datastore.transaction.TransactionConfiguration;
import com.holonplatform.core.datastore.transaction.TransactionStatus.TransactionException;
import com.holonplatform.core.datastore.transaction.TransactionalOperation;
import com.holonplatform.core.exceptions.DataAccessException;
import com.holonplatform.core.internal.Logger;
import com.holonplatform.core.internal.datastore.AbstractInitializableDatastore;
import com.holonplatform.core.internal.utils.ObjectUtils;
import com.holonplatform.datastore.jpa.JpaDatastore;
import com.holonplatform.datastore.jpa.ORMPlatform;
import com.holonplatform.datastore.jpa.config.JpaDatastoreCommodityContext;
import com.holonplatform.datastore.jpa.config.JpaDatastoreCommodityFactory;
import com.holonplatform.datastore.jpa.config.JpaDatastoreExpressionResolver;
import com.holonplatform.datastore.jpa.context.EntityManagerOperation;
import com.holonplatform.datastore.jpa.dialect.DefaultDialect;
import com.holonplatform.datastore.jpa.dialect.ORMDialect;
import com.holonplatform.datastore.jpa.dialect.ORMDialectContext;
import com.holonplatform.datastore.jpa.internal.operations.JpaBulkDelete;
import com.holonplatform.datastore.jpa.internal.operations.JpaBulkInsert;
import com.holonplatform.datastore.jpa.internal.operations.JpaBulkUpdate;
import com.holonplatform.datastore.jpa.internal.operations.JpaDelete;
import com.holonplatform.datastore.jpa.internal.operations.JpaInsert;
import com.holonplatform.datastore.jpa.internal.operations.JpaQuery;
import com.holonplatform.datastore.jpa.internal.operations.JpaRefresh;
import com.holonplatform.datastore.jpa.internal.operations.JpaSave;
import com.holonplatform.datastore.jpa.internal.operations.JpaUpdate;
import com.holonplatform.datastore.jpa.internal.resolvers.CollectionExpressionResolver;
import com.holonplatform.datastore.jpa.internal.resolvers.ConstantExpressionResolver;
import com.holonplatform.datastore.jpa.internal.resolvers.DataTargetEntityResolver;
import com.holonplatform.datastore.jpa.internal.resolvers.DataTargetResolver;
import com.holonplatform.datastore.jpa.internal.resolvers.DefaultQueryFunctionResolver;
import com.holonplatform.datastore.jpa.internal.resolvers.DeleteOperationConfigurationResolver;
import com.holonplatform.datastore.jpa.internal.resolvers.DialectQueryFunctionResolver;
import com.holonplatform.datastore.jpa.internal.resolvers.ExistFilterResolver;
import com.holonplatform.datastore.jpa.internal.resolvers.JPQLLiteralValueResolver;
import com.holonplatform.datastore.jpa.internal.resolvers.JPQLParameterizableExpressionResolver;
import com.holonplatform.datastore.jpa.internal.resolvers.JPQLQueryDefinitionResolver;
import com.holonplatform.datastore.jpa.internal.resolvers.JPQLQueryResolver;
import com.holonplatform.datastore.jpa.internal.resolvers.JPQLTokenResolver;
import com.holonplatform.datastore.jpa.internal.resolvers.JpaEntityExpressionResolver;
import com.holonplatform.datastore.jpa.internal.resolvers.NotExistFilterResolver;
import com.holonplatform.datastore.jpa.internal.resolvers.NullExpressionResolver;
import com.holonplatform.datastore.jpa.internal.resolvers.PathResolver;
import com.holonplatform.datastore.jpa.internal.resolvers.QueryAggregationResolver;
import com.holonplatform.datastore.jpa.internal.resolvers.QueryFilterResolver;
import com.holonplatform.datastore.jpa.internal.resolvers.QueryFunctionResolver;
import com.holonplatform.datastore.jpa.internal.resolvers.QueryOperationResolver;
import com.holonplatform.datastore.jpa.internal.resolvers.QueryResolver;
import com.holonplatform.datastore.jpa.internal.resolvers.QuerySortResolver;
import com.holonplatform.datastore.jpa.internal.resolvers.RelationalTargetResolver;
import com.holonplatform.datastore.jpa.internal.resolvers.SubQueryResolver;
import com.holonplatform.datastore.jpa.internal.resolvers.UpdateOperationConfigurationResolver;
import com.holonplatform.datastore.jpa.internal.resolvers.VisitableQueryFilterResolver;
import com.holonplatform.datastore.jpa.internal.resolvers.VisitableQuerySortResolver;
import com.holonplatform.datastore.jpa.internal.resolvers.projection.BeanProjectionResolver;
import com.holonplatform.datastore.jpa.internal.resolvers.projection.ConstantExpressionProjectionResolver;
import com.holonplatform.datastore.jpa.internal.resolvers.projection.CountAllProjectionResolver;
import com.holonplatform.datastore.jpa.internal.resolvers.projection.CurrentLocalDateProjectionResolver;
import com.holonplatform.datastore.jpa.internal.resolvers.projection.CurrentLocalDateTimeProjectionResolver;
import com.holonplatform.datastore.jpa.internal.resolvers.projection.DataTargetProjectionResolver;
import com.holonplatform.datastore.jpa.internal.resolvers.projection.PropertySetProjectionResolver;
import com.holonplatform.datastore.jpa.internal.resolvers.projection.QueryProjectionResolver;
import com.holonplatform.datastore.jpa.internal.resolvers.projection.SelectAllProjectionResolver;
import com.holonplatform.datastore.jpa.internal.resolvers.projection.TypedExpressionProjectionResolver;
import com.holonplatform.datastore.jpa.jpql.JPQLValueDeserializer;
import com.holonplatform.datastore.jpa.jpql.JPQLValueSerializer;
import com.holonplatform.datastore.jpa.tx.JpaTransaction;
import com.holonplatform.datastore.jpa.tx.JpaTransactionFactory;

/**
 * Default {@link JpaDatastore} implementation.
 * <p>
 * The Datastore instance must be initialized using the {@link #initialize()} method before using
 * it.
 * </p>
 * 
 * @since 5.0.0
 */
public class DefaultJpaDatastore extends AbstractInitializableDatastore<JpaDatastoreCommodityContext>
		implements JpaDatastore, JpaDatastoreCommodityContext {

	private static final long serialVersionUID = -8695844962665825169L;

	/**
	 * Logger
	 */
	protected final static Logger LOGGER = JpaDatastoreLogger.create();

	/**
	 * Current operation EntityManager
	 */
	private static final ThreadLocal<EntityManager> CURRENT_ENTITY_MANAGER = new ThreadLocal<>();

	/**
	 * Current local transaction
	 */
	private static final ThreadLocal<JpaTransaction> CURRENT_TRANSACTION = new ThreadLocal<>();

	/**
	 * Datastore EntityManagerFactory
	 */
	private EntityManagerFactory entityManagerFactory;

	/**
	 * ORM platform
	 */
	private ORMPlatform platform;

	/**
	 * Dialect
	 */
	protected ORMDialect dialect;

	/**
	 * EntityManager initializer
	 */
	private EntityManagerInitializer entityManagerInitializer = EntityManagerInitializer.createDefault();

	/**
	 * EntityManager finalizer
	 */
	private EntityManagerFinalizer entityManagerFinalizer = EntityManagerFinalizer.createDefault();

	/**
	 * Transaction factory
	 */
	private JpaTransactionFactory transactionFactory = JpaTransactionFactory.getDefault();

	/**
	 * Auto-flush mode
	 */
	private boolean autoFlush = false;

	/**
	 * Constructor.
	 */
	public DefaultJpaDatastore() {
		super(JpaDatastoreCommodityFactory.class, JpaDatastoreExpressionResolver.class);

		// EntityManager lifecycle
		setEntityManagerInitializer(emf -> emf.createEntityManager());
		setEntityManagerFinalizer(em -> em.close());

		// register resolvers
		addExpressionResolver(NullExpressionResolver.INSTANCE);
		addExpressionResolver(JpaEntityExpressionResolver.INSTANCE);
		addExpressionResolver(DataTargetEntityResolver.INSTANCE);
		addExpressionResolver(RelationalTargetResolver.INSTANCE);
		addExpressionResolver(DataTargetResolver.INSTANCE);
		addExpressionResolver(PathResolver.INSTANCE);
		addExpressionResolver(ConstantExpressionResolver.INSTANCE);
		addExpressionResolver(CollectionExpressionResolver.INSTANCE);
		addExpressionResolver(QueryFilterResolver.INSTANCE);
		addExpressionResolver(QuerySortResolver.INSTANCE);
		addExpressionResolver(VisitableQueryFilterResolver.INSTANCE);
		addExpressionResolver(VisitableQuerySortResolver.INSTANCE);
		addExpressionResolver(ExistFilterResolver.INSTANCE);
		addExpressionResolver(NotExistFilterResolver.INSTANCE);
		addExpressionResolver(QueryAggregationResolver.INSTANCE);
		addExpressionResolver(CurrentLocalDateProjectionResolver.INSTANCE);
		addExpressionResolver(CurrentLocalDateTimeProjectionResolver.INSTANCE);
		addExpressionResolver(QueryFunctionResolver.INSTANCE);
		addExpressionResolver(DialectQueryFunctionResolver.INSTANCE);
		addExpressionResolver(DefaultQueryFunctionResolver.INSTANCE);
		addExpressionResolver(QueryOperationResolver.INSTANCE);
		addExpressionResolver(QueryResolver.INSTANCE);
		addExpressionResolver(SubQueryResolver.INSTANCE);
		addExpressionResolver(QueryProjectionResolver.INSTANCE);
		addExpressionResolver(DataTargetProjectionResolver.INSTANCE);
		addExpressionResolver(SelectAllProjectionResolver.INSTANCE);
		addExpressionResolver(ConstantExpressionProjectionResolver.INSTANCE);
		addExpressionResolver(TypedExpressionProjectionResolver.INSTANCE);
		addExpressionResolver(PropertySetProjectionResolver.INSTANCE);
		addExpressionResolver(BeanProjectionResolver.INSTANCE);
		addExpressionResolver(CountAllProjectionResolver.INSTANCE);
		addExpressionResolver(JPQLLiteralValueResolver.INSTANCE);
		addExpressionResolver(JPQLParameterizableExpressionResolver.INSTANCE);
		addExpressionResolver(JPQLQueryDefinitionResolver.INSTANCE);
		addExpressionResolver(JPQLQueryResolver.INSTANCE);
		addExpressionResolver(JPQLTokenResolver.INSTANCE);
		addExpressionResolver(UpdateOperationConfigurationResolver.INSTANCE);
		addExpressionResolver(DeleteOperationConfigurationResolver.INSTANCE);

		// register operation commodities
		registerCommodity(JpaRefresh.FACTORY);
		registerCommodity(JpaInsert.FACTORY);
		registerCommodity(JpaUpdate.FACTORY);
		registerCommodity(JpaSave.FACTORY);
		registerCommodity(JpaDelete.FACTORY);
		registerCommodity(JpaBulkInsert.FACTORY);
		registerCommodity(JpaBulkUpdate.FACTORY);
		registerCommodity(JpaBulkDelete.FACTORY);
		registerCommodity(JpaQuery.FACTORY);
		registerCommodity(JpaQuery.LOCK_FACTORY);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.holonplatform.core.internal.datastore.AbstractInitializableDatastore#initialize(java.lang.
	 * ClassLoader)
	 */
	@Override
	protected boolean initialize(ClassLoader classLoader) {

		// check getEntityManagerFactory
		if (getEntityManagerFactory() == null) {
			throw new IllegalStateException("No EntityManagerFactory available");
		}

		// platform
		if (!getORMPlatform().isPresent()) {
			// try to detect
			detectORMPlatform().ifPresent(platform -> setORMPlatform(platform));
		}

		// dialect
		if (getDialect() == null) {
			setDialect(getORMPlatform().flatMap(platform -> ORMDialect.detect(platform)).orElse(new DefaultDialect()));
		}

		// init dialect
		final ORMDialect dialect = getDialect();
		try {
			dialect.init(new JpaDatastoreDialectContext());
		} catch (Exception e) {
			throw new IllegalStateException("Failed to initialize dialect [" + dialect.getClass().getName() + "]", e);
		}

		// default factories and resolvers
		loadExpressionResolvers(classLoader);
		loadCommodityFactories(classLoader);

		LOGGER.info("JpaDatastore initialized - Using dialect [" + dialect.getClass().getName() + "]");
		getORMPlatform().ifPresent(platform -> LOGGER.info("JpaDatastore ORM platform: " + platform));

		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.internal.datastore.AbstractDatastore#getCommodityContext()
	 */
	@Override
	protected JpaDatastoreCommodityContext getCommodityContext() throws CommodityConfigurationException {
		return this;
	}

	/**
	 * Set EntityManagerFactory to obtain EntityManager references
	 * @param entityManagerFactory EntityManagerFactory to set, must be not <code>null</code>
	 */
	public void setEntityManagerFactory(EntityManagerFactory entityManagerFactory) {
		ObjectUtils.argumentNotNull(entityManagerFactory, "EntityManagerFactory must be not null");
		this.entityManagerFactory = entityManagerFactory;
		// reset
		this.platform = null;
		this.dialect = null;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jpa.internal.ConfigurableJpaDatastore#getEntityManagerFactory()
	 */
	@Override
	public EntityManagerFactory getEntityManagerFactory() {
		return entityManagerFactory;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jpa.internal.ConfigurableJpaDatastore#isAutoFlush()
	 */
	@Override
	public boolean isAutoFlush() {
		return autoFlush;
	}

	/**
	 * Set whether to auto-flush mode is enabled. When auto-flush mode is enabled,
	 * {@link EntityManager#flush()} is called after each Datastore data manipulation operation, such as
	 * <code>save</code> or <code>delete</code>.
	 * @param autoFlush <code>true</code> to enable the auto-flush mode, <code>false</code> to disable
	 */
	public void setAutoFlush(boolean autoFlush) {
		this.autoFlush = autoFlush;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jpa.context.JpaContext#getDialect()
	 */
	@Override
	public ORMDialect getDialect() {
		return dialect;
	}

	/**
	 * Set the ORM dialect
	 * @param dialect the dialect to set
	 */
	public void setDialect(ORMDialect dialect) {
		this.dialect = dialect;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jpa.context.JpaOperationContext#getBeanIntrospector()
	 */
	@Override
	public BeanIntrospector getBeanIntrospector() {
		return BeanIntrospector.get();
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jpa.context.JpaOperationContext#trace(java.lang.String)
	 */
	@Override
	public void trace(String jpql) {
		if (isTraceEnabled()) {
			LOGGER.info("(TRACE) JPQL: [" + jpql + "]");
		} else {
			LOGGER.debug(() -> "JPQL: [" + jpql + "]");
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jpa.context.JpaContext#traceOperation(java.lang.String)
	 */
	@Override
	public void traceOperation(String operation) {
		if (isTraceEnabled()) {
			LOGGER.info("(TRACE) [" + operation + "]");
		} else {
			LOGGER.debug(() -> "[" + operation + "]");
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jpa.internal.ConfigurableJpaDatastore#getORMPlatform()
	 */
	@Override
	public Optional<ORMPlatform> getORMPlatform() {
		return Optional.ofNullable(platform);
	}

	/**
	 * Set the {@link ORMPlatform}.
	 * @param platform the platform to set
	 */
	public void setORMPlatform(ORMPlatform platform) {
		this.platform = platform;
	}

	/**
	 * Try to detect the {@link ORMPlatform} using current {@link EntityManagerFactory}.
	 * @return Detected {@link ORMPlatform}, empty if not available
	 */
	protected Optional<ORMPlatform> detectORMPlatform() {
		try {
			final ORMPlatform platform = withEntityManager(em -> {
				return ORMPlatform.resolve(em);
			});
			return Optional.ofNullable(platform);
		} catch (Exception e) {
			LOGGER.warn("Failed to detected ORM platform");
			LOGGER.debug(() -> "Failed to detected ORM platform", e);
			return Optional.empty();
		}
	}

	/**
	 * Sets the {@link EntityManager} instance provider for Datastore operations execution
	 * @param entityManagerInitializer the EntityManagerInitializer to set
	 */
	public void setEntityManagerInitializer(EntityManagerInitializer entityManagerInitializer) {
		ObjectUtils.argumentNotNull(entityManagerInitializer, "EntityManagerInitializer must be not null");
		this.entityManagerInitializer = entityManagerInitializer;
	}

	/**
	 * Gets the {@link EntityManager} instance provider for Datastore operations execution
	 * @return the EntityManagerInitializer
	 */
	protected EntityManagerInitializer getEntityManagerInitializer() {
		return entityManagerInitializer;
	}

	/**
	 * Sets the {@link EntityManager} finalizer to use after Datastore operations execution.
	 * @param entityManagerFinalizer the EntityManagerFinalizer to set
	 */
	public void setEntityManagerFinalizer(EntityManagerFinalizer entityManagerFinalizer) {
		this.entityManagerFinalizer = entityManagerFinalizer;
	}

	/**
	 * Get the {@link EntityManager} finalizer to use after Datastore operations execution.
	 * @return the EntityManagerFinalizer, if available
	 */
	protected Optional<EntityManagerFinalizer> getEntityManagerFinalizer() {
		return Optional.ofNullable(entityManagerFinalizer);
	}

	/**
	 * Get an {@link EntityManager} instance using current {@link EntityManagerFactory} and
	 * {@link EntityManagerInitializer}.
	 * @return The {@link EntityManager} instance
	 */
	protected EntityManager obtainEntityManager() {
		EntityManagerFactory emf = getEntityManagerFactory();
		if (emf == null) {
			throw new IllegalStateException(
					"No EntityManagerFactory available. Check the JPA Datastore configuration.");
		}
		final EntityManager entityManager = getEntityManagerInitializer().getEntityManager(emf);
		if (entityManager == null) {
			throw new IllegalStateException("The EntityManagerInitializer [" + getEntityManagerInitializer()
					+ "] returned a null EntityManager");
		}

		LOGGER.debug(() -> "EntityManager [" + entityManager.hashCode() + "] initialized: " + entityManager);

		return entityManager;
	}

	/**
	 * Finalize given {@link EntityManager} using current {@link EntityManagerFinalizer}, if available.
	 * @param entityManager EntityManager to finalize
	 */
	protected void finalizeEntityManager(final EntityManager entityManager) {
		if (entityManager != null) {
			getEntityManagerFinalizer().ifPresent(f -> f.finalizeEntityManager(entityManager));

			LOGGER.debug(() -> "EntityManager [" + entityManager.hashCode() + "] finalized");
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jpa.EntityManagerHandler#withEntityManager(com.holonplatform.
	 * datastore.jpa.context. EntityManagerOperation)
	 */
	@Override
	public <R> R withEntityManager(EntityManagerOperation<R> operation) {
		checkInitialized();
		ObjectUtils.argumentNotNull(operation, "Operation must be not null");

		EntityManager entityManager = null;
		try {

			// check current
			final EntityManager current = CURRENT_ENTITY_MANAGER.get();
			if (current != null) {
				return operation.execute(current);
			}

			// if a transaction is active, use current transaction EntityManager
			EntityManager txem = getCurrentTransactionEntityManager().orElse(null);
			if (txem != null) {
				return operation.execute(txem);
			}

			// get an EntityManager from handler
			CURRENT_ENTITY_MANAGER.set(entityManager = obtainEntityManager());
			return operation.execute(entityManager);

		} catch (DataAccessException e) {
			throw e;
		} catch (Exception e) {
			throw new DataAccessException("Failed to execute operation", e);
		} finally {
			// check active transaction: avoid EntityManager finalization if present
			if (entityManager != null) {
				// finalize EntityManager
				finalizeEntityManager(entityManager);
				// remove current
				CURRENT_ENTITY_MANAGER.remove();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.holonplatform.core.datastore.transaction.Transactional#withTransaction(com.holonplatform.core
	 * .datastore. transaction.TransactionalOperation,
	 * com.holonplatform.core.datastore.transaction.TransactionConfiguration)
	 */
	@Override
	public <R> R withTransaction(TransactionalOperation<R> operation,
			TransactionConfiguration transactionConfiguration) {
		checkInitialized();
		ObjectUtils.argumentNotNull(operation, "TransactionalOperation must be not null");

		// check active transaction or create a new one
		final JpaTransaction tx = getCurrentTransaction().map(t -> JpaTransaction.delegate(t))
				.orElseGet(() -> startTransaction(transactionConfiguration));

		try {
			// execute operation
			return operation.execute(tx);
		} catch (Exception e) {
			// check rollback transaction
			if (tx.getConfiguration().isRollbackOnError() && !tx.isCompleted()) {
				tx.setRollbackOnly();
			}
			throw e;
		} finally {
			try {
				endTransaction(tx);
			} catch (Exception e) {
				throw new TransactionException("Failed to finalize transaction", e);
			}
		}
	}

	/**
	 * Get the {@link JpaTransactionFactory} to use to create a new JPA transaction.
	 * @return the transaction factory
	 */
	protected JpaTransactionFactory getTransactionFactory() {
		return transactionFactory;
	}

	/**
	 * Set the {@link JpaTransactionFactory} to use to create a new JPA transaction.
	 * @param transactionFactory the transaction factory to set (not null)
	 */
	public void setTransactionFactory(JpaTransactionFactory transactionFactory) {
		this.transactionFactory = transactionFactory;
	}

	/**
	 * Get the current transaction, if active.
	 * @return Optional current transaction
	 */
	private static Optional<JpaTransaction> getCurrentTransaction() {
		return Optional.ofNullable(CURRENT_TRANSACTION.get());
	}

	/**
	 * Get the {@link EntityManager} bound to the current transaction, if available.
	 * @return Optional {@link EntityManager} bound to the current transaction
	 */
	private static Optional<EntityManager> getCurrentTransactionEntityManager() {
		return getCurrentTransaction().map(tx -> tx.getEntityManager());
	}

	/**
	 * Starts a {@link JpaTransaction}. If a local transaction is active, it will be forcedly finalized.
	 * @param configuration Transaction configuration. If <code>null</code>, a default configuration
	 *        will be used
	 * @return A new transaction
	 */
	private JpaTransaction startTransaction(TransactionConfiguration configuration) throws TransactionException {

		// check if a current transaction is present
		getCurrentTransaction().ifPresent(tx -> {
			LOGGER.warn("A thread bound transaction was already present [" + tx
					+ "] - The current transaction will be finalized");
			try {
				endTransaction(tx);
			} catch (Exception e) {
				LOGGER.warn("Failed to force current transaction finalization", e);
			}
		});

		// configuration
		final TransactionConfiguration cfg = (configuration != null) ? configuration
				: TransactionConfiguration.getDefault();

		// entitymanager
		final EntityManager entityManager;
		try {
			entityManager = obtainEntityManager();
		} catch (Exception e) {
			throw new TransactionException("Failed to obtain an EntityManager to start a transaction", e);
		}

		// create a new transaction
		final JpaTransaction tx = getTransactionFactory().createTransaction(entityManager, cfg);

		// start transaction
		try {
			// start transaction
			tx.start();
		} catch (TransactionException e) {
			// ensure entitymanager finalization
			finalizeEntityManager(entityManager);
			// propagate
			throw e;
		}

		// set as current transaction
		CURRENT_TRANSACTION.set(tx);

		LOGGER.debug(() -> "JPA transaction [" + tx + "] created and setted as current transaction");

		// return the transaction
		return tx;
	}

	/**
	 * Finalize the given transaction, only if the transaction is new.
	 * @param tx Transaction to finalize
	 * @throws TransactionException Error finalizing transaction
	 * @return <code>true</code> if the transaction was actually finalized
	 */
	protected boolean endTransaction(JpaTransaction tx) throws TransactionException {
		ObjectUtils.argumentNotNull(tx, "Transaction must be not null");

		// check new
		if (!tx.isNew()) {
			LOGGER.debug(() -> "JPA transaction [" + tx + "] was not finalized because it is not new");
			return false;
		}

		// remove reference
		getCurrentTransaction().filter(current -> current == tx).ifPresent(current -> CURRENT_TRANSACTION.remove());

		try {
			if (tx.isActive()) {
				tx.end();
			}
		} finally {
			// finalize entity manager
			try {
				finalizeEntityManager(tx.getEntityManager());
			} catch (Exception e) {
				throw new TransactionException("Failed to finalize the transaction EntityManager", e);
			}
		}

		LOGGER.debug(() -> "JPA transaction [" + tx + "] finalized");

		return true;
	}

	// ------- Dialect context

	class JpaDatastoreDialectContext implements ORMDialectContext {

		/*
		 * (non-Javadoc)
		 * @see com.holonplatform.datastore.jpa.operation.EntityManagerHandler#withEntityManager(com.
		 * holonplatform.datastore. jpa.operation.EntityManagerOperation)
		 */
		@Override
		public <R> R withEntityManager(EntityManagerOperation<R> operation) {
			return DefaultJpaDatastore.this.withEntityManager(operation);
		}

		/*
		 * (non-Javadoc)
		 * @see com.holonplatform.datastore.jpa.context.ORMDialectContext#getEntityManagerFactory()
		 */
		@Override
		public EntityManagerFactory getEntityManagerFactory() {
			return DefaultJpaDatastore.this.entityManagerFactory;
		}

		/*
		 * (non-Javadoc)
		 * @see com.holonplatform.datastore.jpa.context.ORMDialectContext#getValueSerializer()
		 */
		@Override
		public JPQLValueSerializer getValueSerializer() {
			return DefaultJpaDatastore.this.getValueSerializer();
		}

		/*
		 * (non-Javadoc)
		 * @see com.holonplatform.datastore.jpa.context.ORMDialectContext#getValueDeserializer()
		 */
		@Override
		public JPQLValueDeserializer getValueDeserializer() {
			return DefaultJpaDatastore.this.getValueDeserializer();
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * com.holonplatform.core.ExpressionResolver.ExpressionResolverSupport#addExpressionResolver(com.
		 * holonplatform. core.ExpressionResolver)
		 */
		@Override
		public <E extends Expression, R extends Expression> void addExpressionResolver(
				ExpressionResolver<E, R> expressionResolver) {
			DefaultJpaDatastore.this.addExpressionResolver(expressionResolver);
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * com.holonplatform.core.ExpressionResolver.ExpressionResolverSupport#removeExpressionResolver(com.
		 * holonplatform.core.ExpressionResolver)
		 */
		@Override
		public <E extends Expression, R extends Expression> void removeExpressionResolver(
				ExpressionResolver<E, R> expressionResolver) {
			DefaultJpaDatastore.this.removeExpressionResolver(expressionResolver);
		}

	}

	// Builder

	/**
	 * Default {@link JpaDatastore.Builder}.
	 */
	public abstract static class AbstractBuilder<D extends JpaDatastore, I extends DefaultJpaDatastore>
			implements JpaDatastore.Builder<D> {

		protected final I datastore;

		public AbstractBuilder(I datastore) {
			super();
			this.datastore = datastore;
		}

		/*
		 * (non-Javadoc)
		 * @see com.holonplatform.core.datastore.Datastore.Builder#dataContextId(java.lang.String)
		 */
		@Override
		public JpaDatastore.Builder<D> dataContextId(String dataContextId) {
			datastore.setDataContextId(dataContextId);
			return this;
		}

		/*
		 * (non-Javadoc)
		 * @see com.holonplatform.core.datastore.Datastore.Builder#traceEnabled(boolean)
		 */
		@Override
		public JpaDatastore.Builder<D> traceEnabled(boolean trace) {
			datastore.setTraceEnabled(trace);
			return this;
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * com.holonplatform.core.ExpressionResolver.ExpressionResolverBuilder#withExpressionResolver(com.
		 * holonplatform. core.ExpressionResolver)
		 */
		@Override
		public <E extends Expression, R extends Expression> JpaDatastore.Builder<D> withExpressionResolver(
				ExpressionResolver<E, R> expressionResolver) {
			datastore.addExpressionResolver(expressionResolver);
			return this;
		}

		/*
		 * (non-Javadoc)
		 * @see com.holonplatform.core.datastore.Datastore.Builder#configuration(com.holonplatform.core.
		 * datastore. DatastoreConfigProperties)
		 */
		@Override
		public JpaDatastore.Builder<D> configuration(DatastoreConfigProperties configuration) {
			ObjectUtils.argumentNotNull(configuration, "Datastore configuration must be not null");
			datastore.setTraceEnabled(configuration.isTrace());
			if (configuration.getDialect() != null) {
				dialect(configuration.getDialect());
			}
			return this;
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * com.holonplatform.datastore.jpa.JpaDatastore.Builder#entityManagerFactory(jakarta.persistence.
		 * EntityManagerFactory)
		 */
		@Override
		public JpaDatastore.Builder<D> entityManagerFactory(EntityManagerFactory entityManagerFactory) {
			datastore.setEntityManagerFactory(entityManagerFactory);
			return this;
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * com.holonplatform.datastore.jpa.JpaDatastore.Builder#entityManagerInitializer(com.holonplatform.
		 * datastore.jpa .JpaDatastore.EntityManagerInitializer)
		 */
		@Override
		public JpaDatastore.Builder<D> entityManagerInitializer(EntityManagerInitializer entityManagerInitializer) {
			datastore.setEntityManagerInitializer(entityManagerInitializer);
			return this;
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * com.holonplatform.datastore.jpa.JpaDatastore.Builder#entityManagerFinalizer(com.holonplatform.
		 * datastore. jpa.JpaDatastore.EntityManagerFinalizer)
		 */
		@Override
		public JpaDatastore.Builder<D> entityManagerFinalizer(EntityManagerFinalizer entityManagerFinalizer) {
			datastore.setEntityManagerFinalizer(entityManagerFinalizer);
			return this;
		}

		/*
		 * (non-Javadoc)
		 * @see com.holonplatform.datastore.jpa.JpaDatastore.Builder#entityManagerHandler(com.holonplatform.
		 * datastore.jpa. JpaDatastore.EntityManagerLifecycleHandler)
		 */
		@Override
		public Builder<D> entityManagerHandler(EntityManagerLifecycleHandler entityManagerHandler) {
			ObjectUtils.argumentNotNull(entityManagerHandler, "EntityManagerLifecycleHandler must be not null");
			datastore.setEntityManagerInitializer(entityManagerHandler);
			datastore.setEntityManagerFinalizer(entityManagerHandler);
			return this;
		}

		/*
		 * (non-Javadoc)
		 * @see com.holonplatform.datastore.jpa.JpaDatastore.Builder#transactionFactory(com.holonplatform.
		 * datastore.jpa.tx. JpaTransactionFactory)
		 */
		@Override
		public Builder<D> transactionFactory(JpaTransactionFactory transactionFactory) {
			datastore.setTransactionFactory(transactionFactory);
			return this;
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * com.holonplatform.datastore.jpa.JpaDatastore.Builder#platform(com.holonplatform.datastore.jpa.
		 * ORMPlatform)
		 */
		@Override
		public JpaDatastore.Builder<D> platform(ORMPlatform platform) {
			datastore.setORMPlatform(platform);
			return this;
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * com.holonplatform.datastore.jpa.JpaDatastore.Builder#dialect(com.holonplatform.datastore.jpa.
		 * dialect. ORMDialect)
		 */
		@Override
		public JpaDatastore.Builder<D> dialect(ORMDialect dialect) {
			datastore.setDialect(dialect);
			return this;
		}

		/*
		 * (non-Javadoc)
		 * @see com.holonplatform.datastore.jpa.JpaDatastore.Builder#dialect(java.lang.String)
		 */
		@Override
		public JpaDatastore.Builder<D> dialect(String dialectClassName) {
			ObjectUtils.argumentNotNull(dialectClassName, "Dialect class name must be not null");
			try {
				datastore.setDialect(
						(ORMDialect) Class.forName(dialectClassName).getDeclaredConstructor().newInstance());
			} catch (Exception e) {
				throw new IllegalArgumentException("Failed to istantiate dialect class [" + dialectClassName + "]", e);
			}
			return this;
		}

		/*
		 * (non-Javadoc)
		 * @see com.holonplatform.datastore.jpa.JpaDatastore.Builder#autoFlush(boolean)
		 */
		@Override
		public JpaDatastore.Builder<D> autoFlush(boolean autoFlush) {
			datastore.setAutoFlush(autoFlush);
			return this;
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * com.holonplatform.datastore.jpa.JpaDatastore.Builder#withCommodity(com.holonplatform.datastore.
		 * jpa.config. JpaDatastoreCommodityFactory)
		 */
		@Override
		public <C extends DatastoreCommodity> com.holonplatform.datastore.jpa.JpaDatastore.Builder<D> withCommodity(
				JpaDatastoreCommodityFactory<C> commodityFactory) {
			datastore.registerCommodity(commodityFactory);
			return this;
		}

	}

	/**
	 * Default {@link JpaDatastore.Builder}.
	 */
	public static class DefaultBuilder extends AbstractBuilder<JpaDatastore, DefaultJpaDatastore> {

		public DefaultBuilder() {
			super(new DefaultJpaDatastore());
		}

		/*
		 * (non-Javadoc)
		 * @see com.holonplatform.core.datastore.Datastore.Builder#build()
		 */
		@Override
		public JpaDatastore build() {
			// init
			datastore.initialize();
			return datastore;
		}

	}

}
