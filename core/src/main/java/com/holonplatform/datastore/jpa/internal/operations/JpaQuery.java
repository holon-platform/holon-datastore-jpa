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
package com.holonplatform.datastore.jpa.internal.operations;

import java.util.Optional;
import java.util.stream.Stream;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.TypedQuery;

import com.holonplatform.core.datastore.DatastoreCommodityContext.CommodityConfigurationException;
import com.holonplatform.core.datastore.DatastoreCommodityFactory;
import com.holonplatform.core.exceptions.DataAccessException;
import com.holonplatform.core.internal.query.QueryAdapterQuery;
import com.holonplatform.core.internal.query.QueryDefinition;
import com.holonplatform.core.internal.query.QueryUtils;
import com.holonplatform.core.internal.query.lock.LockQueryAdapterQuery;
import com.holonplatform.core.internal.utils.TypeUtils;
import com.holonplatform.core.query.Query;
import com.holonplatform.core.query.QueryAdapter;
import com.holonplatform.core.query.QueryConfiguration;
import com.holonplatform.core.query.QueryOperation;
import com.holonplatform.core.query.SelectAllProjection;
import com.holonplatform.core.query.lock.LockAcquisitionException;
import com.holonplatform.core.query.lock.LockMode;
import com.holonplatform.core.query.lock.LockQuery;
import com.holonplatform.core.query.lock.LockQueryAdapter;
import com.holonplatform.datastore.jpa.JpaDatastore;
import com.holonplatform.datastore.jpa.JpaQueryHint;
import com.holonplatform.datastore.jpa.config.JpaDatastoreCommodityContext;
import com.holonplatform.datastore.jpa.context.JpaExecutionContext;
import com.holonplatform.datastore.jpa.context.JpaOperationContext;
import com.holonplatform.datastore.jpa.jpql.JPQLResultConverter;
import com.holonplatform.datastore.jpa.jpql.context.JPQLResolutionContext;
import com.holonplatform.datastore.jpa.jpql.expression.JPQLQuery;

/**
 * JPA {@link QueryAdapter}.
 *
 * @since 5.1.0
 */
public class JpaQuery implements LockQueryAdapter<QueryConfiguration> {

	// Commodity factory
	@SuppressWarnings("serial")
	public static final DatastoreCommodityFactory<JpaDatastoreCommodityContext, Query> FACTORY = new DatastoreCommodityFactory<JpaDatastoreCommodityContext, Query>() {

		@Override
		public Class<? extends Query> getCommodityType() {
			return Query.class;
		}

		@Override
		public Query createCommodity(JpaDatastoreCommodityContext context) throws CommodityConfigurationException {
			return new QueryAdapterQuery<>(new JpaQuery(context), QueryDefinition.create());
		}
	};

	// LockQuery Commodity factory
	@SuppressWarnings("serial")
	public static final DatastoreCommodityFactory<JpaDatastoreCommodityContext, LockQuery> LOCK_FACTORY = new DatastoreCommodityFactory<JpaDatastoreCommodityContext, LockQuery>() {

		@Override
		public Class<? extends LockQuery> getCommodityType() {
			return LockQuery.class;
		}

		@Override
		public LockQuery createCommodity(JpaDatastoreCommodityContext context) throws CommodityConfigurationException {
			return new LockQueryAdapterQuery<>(new JpaQuery(context), QueryDefinition.create());
		}
	};

	private final JpaOperationContext operationContext;

	public JpaQuery(JpaOperationContext operationContext) {
		super();
		this.operationContext = operationContext;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.query.QueryAdapter#stream(com.holonplatform.core.query.QueryOperation)
	 */
	@Override
	public <R> Stream<R> stream(QueryOperation<QueryConfiguration, R> queryOperation) throws DataAccessException {

		// composition context
		final JPQLResolutionContext context = JPQLResolutionContext.create(operationContext);
		context.addExpressionResolvers(queryOperation.getConfiguration().getExpressionResolvers());

		// resolve to JPQLQuery
		@SuppressWarnings("unchecked")
		final JPQLQuery<Object, R> query = context.resolveOrFail(queryOperation, JPQLQuery.class);

		// converter
		final JPQLResultConverter<Object, R> converter = query.getResultConverter();

		// check types consistency
		if (!TypeUtils.isAssignable(converter.getConversionType(), queryOperation.getProjection().getType())) {
			throw new DataAccessException("The results converter type [" + converter.getConversionType()
					+ "] is not compatible with the query projection type [" + queryOperation.getProjection().getType()
					+ "]");
		}
		if (!TypeUtils.isAssignable(query.getQueryResultType(), converter.getQueryResultType())) {
			throw new DataAccessException("The results converter query type [" + converter.getQueryResultType()
					+ "] is not compatible with the JPQL query result type [" + query.getQueryResultType() + "]");
		}

		// trace
		operationContext.trace(query.getJPQL());

		// execute
		return operationContext.withEntityManager(entityManager -> {

			// configure query
			final TypedQuery<?> q = createEntityManagerQuery(context, entityManager, query,
					queryOperation.getConfiguration());

			// execute and convert results
			final JpaExecutionContext ctx = JpaExecutionContext.create(operationContext, entityManager);

			try {
				return QueryUtils.asResultsStream(q.getResultList(), null).map(t -> converter.convert(ctx, t));
			} catch (PersistenceException e) {
				// translate PersistenceException using dialect
				throw operationContext.getDialect().translateException(e);
			}

		});

	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.query.lock.LockQueryAdapter#tryLock(com.holonplatform.core.query.QueryConfiguration)
	 */
	@Override
	public boolean tryLock(QueryConfiguration queryConfiguration) {

		// composition context
		final JPQLResolutionContext context = JPQLResolutionContext.create(operationContext);
		context.addExpressionResolvers(queryConfiguration.getExpressionResolvers());

		final QueryOperation<?, ?> queryOperation = QueryOperation.create(queryConfiguration,
				SelectAllProjection.create());

		// resolve to JPQLQuery
		final JPQLQuery<?, ?> jpqlQuery = context.resolveOrFail(queryOperation, JPQLQuery.class);

		// trace
		operationContext.trace(jpqlQuery.getJPQL());

		// execute
		return operationContext.withEntityManager(entityManager -> {

			// configure query
			final TypedQuery<?> q = JpaQuery.createEntityManagerQuery(context, entityManager, jpqlQuery,
					queryOperation.getConfiguration());

			// execute
			try {
				q.getResultList();
			} catch (PersistenceException e) {
				// translate PersistenceException using dialect
				DataAccessException dae = operationContext.getDialect().translateException(e);
				// check lock acquistion exception
				if (LockAcquisitionException.class.isAssignableFrom(dae.getClass())) {
					return false;
				}
				throw e;
			}

			return true;
		});
	}

	/**
	 * Create a JPA {@link TypedQuery} using given {@link JPQLQuery} statement and configured according to given query
	 * configuration.
	 * @param context Resolution content
	 * @param entityManager EntityManager to use
	 * @param query JPQL query definition
	 * @param configuration Query configuration
	 * @return A new {@link TypedQuery} using given {@link JPQLQuery} statement and configured according to given query
	 *         configuration
	 */
	static TypedQuery<?> createEntityManagerQuery(JPQLResolutionContext context, EntityManager entityManager,
			JPQLQuery<?, ?> query, QueryConfiguration configuration) {

		// configure query
		final TypedQuery<?> q = entityManager.createQuery(query.getJPQL(), query.getQueryResultType());

		// resolve parameters
		context.setupQueryParameters(q);

		// setup limit and offset
		configuration.getLimit().ifPresent((l) -> q.setMaxResults(l));
		configuration.getOffset().ifPresent((o) -> q.setFirstResult(o));

		// query hints
		configuration.getParameter(JpaQueryHint.QUERY_PARAMETER_HINT)
				.ifPresent(p -> q.setHint(p.getName(), p.getValue()));

		// flush mode
		configuration.getParameter(JpaDatastore.QUERY_PARAMETER_FLUSH_MODE).ifPresent(p -> q.setFlushMode(p));

		// lock mode
		if (configuration.hasNotNullParameter(LockQueryAdapterQuery.LOCK_MODE)) {
			getJpaLockMode(configuration.getParameter(LockQueryAdapterQuery.LOCK_MODE, null)).ifPresent(lm -> {
				q.setLockMode(lm);
				// timeout
				configuration.getParameter(LockQueryAdapterQuery.LOCK_TIMEOUT).filter(t -> t != null && t > -1)
						.ifPresent(timeout -> {
							entityManager.setProperty("jakarta.persistence.lock.timeout", timeout);
						});
			});
		} else {
			configuration.getParameter(JpaDatastore.QUERY_PARAMETER_LOCK_MODE).ifPresent(p -> q.setLockMode(p));
		}

		return q;
	}

	/**
	 * Get the JPA {@link LockModeType} which corresponds to given {@link LockMode}, if available.
	 * @param lockMode Lock mode
	 * @return Optional JPA {@link LockModeType}
	 */
	private static Optional<LockModeType> getJpaLockMode(LockMode lockMode) {
		if (lockMode != null) {
			switch (lockMode) {
			case PESSIMISTIC:
				return Optional.of(LockModeType.PESSIMISTIC_WRITE);
			default:
				break;
			}
		}
		return Optional.empty();
	}

}
