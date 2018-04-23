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

import java.util.stream.Stream;

import javax.persistence.TypedQuery;

import com.holonplatform.core.datastore.DatastoreCommodityContext.CommodityConfigurationException;
import com.holonplatform.core.datastore.DatastoreCommodityFactory;
import com.holonplatform.core.exceptions.DataAccessException;
import com.holonplatform.core.internal.query.QueryAdapterQuery;
import com.holonplatform.core.internal.query.QueryDefinition;
import com.holonplatform.core.internal.query.QueryUtils;
import com.holonplatform.core.internal.utils.TypeUtils;
import com.holonplatform.core.query.Query;
import com.holonplatform.core.query.QueryAdapter;
import com.holonplatform.core.query.QueryConfiguration;
import com.holonplatform.core.query.QueryOperation;
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
public class JpaQuery implements QueryAdapter<QueryConfiguration> {

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

		/// composition context
		final JPQLResolutionContext context = JPQLResolutionContext.create(operationContext);
		context.addExpressionResolvers(queryOperation.getConfiguration().getExpressionResolvers());

		// resolve to SQLQuery
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
			final TypedQuery<?> q = entityManager.createQuery(query.getJPQL(), query.getQueryResultType());

			// resolve parameters
			context.setupQueryParameters(q);

			// setup limit and offset
			queryOperation.getConfiguration().getLimit().ifPresent((l) -> q.setMaxResults(l));
			queryOperation.getConfiguration().getOffset().ifPresent((o) -> q.setFirstResult(o));

			// query hints and lock mode
			queryOperation.getConfiguration().getParameter(JpaQueryHint.QUERY_PARAMETER_HINT)
					.ifPresent(p -> q.setHint(p.getName(), p.getValue()));
			queryOperation.getConfiguration().getParameter(JpaDatastore.QUERY_PARAMETER_LOCK_MODE)
					.ifPresent(p -> q.setLockMode(p));
			queryOperation.getConfiguration().getParameter(JpaDatastore.QUERY_PARAMETER_FLUSH_MODE)
					.ifPresent(p -> q.setFlushMode(p));

			// execute and convert results
			final JpaExecutionContext ctx = JpaExecutionContext.create(operationContext, entityManager);

			return QueryUtils.asResultsStream(q.getResultList(), null).map(t -> converter.convert(ctx, t));

		});

	}

}
