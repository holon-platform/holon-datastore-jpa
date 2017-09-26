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

import java.util.stream.Stream;

import javax.persistence.LockModeType;
import javax.persistence.TypedQuery;

import com.holonplatform.core.internal.Logger;
import com.holonplatform.core.internal.query.QueryAdapter;
import com.holonplatform.core.internal.query.QueryStructure;
import com.holonplatform.core.internal.query.QueryUtils;
import com.holonplatform.core.internal.utils.ObjectUtils;
import com.holonplatform.core.query.Query.QueryBuildException;
import com.holonplatform.core.query.QueryConfiguration;
import com.holonplatform.core.query.QueryProjection;
import com.holonplatform.core.query.QueryResults.QueryExecutionException;
import com.holonplatform.datastore.jpa.JpaDatastore;
import com.holonplatform.datastore.jpa.JpaQueryHint;
import com.holonplatform.datastore.jpa.config.JpaDatastoreCommodityContext;
import com.holonplatform.datastore.jpa.internal.expressions.JPQLQueryComposition;
import com.holonplatform.datastore.jpa.internal.expressions.JpaResolutionContext.AliasMode;
import com.holonplatform.datastore.jpa.internal.expressions.QueryResultConverter;

/**
 * {@link QueryAdapter} for concrete {@link com.holonplatform.core.query.Query} configuration and execution using JPA
 * Criteria API
 * 
 * @since 5.0.0
 */
public class JpaQueryAdapter implements QueryAdapter<QueryConfiguration> {

	/**
	 * Logger
	 */
	private final static Logger LOGGER = JpaDatastoreLogger.create();

	/*
	 * Datastore context
	 */
	private final JpaDatastoreCommodityContext context;

	/**
	 * Constructor
	 * @param context Datastore context
	 */
	public JpaQueryAdapter(JpaDatastoreCommodityContext context) {
		super();
		ObjectUtils.argumentNotNull(context, "JpaDatastoreCommodityContext must be not null");
		this.context = context;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.internal.query.QueryAdapter#stream(com.holonplatform.core.query.QueryConfiguration,
	 * com.holonplatform.core.query.QueryProjection)
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public <R> Stream<R> stream(QueryConfiguration configuration, QueryProjection<R> projection)
			throws QueryExecutionException {

		// context
		final com.holonplatform.datastore.jpa.internal.expressions.JpaResolutionContext resolutionContext = com.holonplatform.datastore.jpa.internal.expressions.JpaResolutionContext
				.create(context.getEntityManagerFactory(), context.getORMPlatform().orElse(null), configuration,
						AliasMode.AUTO);

		final JPQLQueryComposition<?, R> query;
		final String jpql;
		try {

			// resolve query
			query = resolutionContext.resolve(QueryStructure.create(configuration, projection),
					JPQLQueryComposition.class, resolutionContext)
					.orElseThrow(() -> new QueryBuildException("Failed to resolve query"));

			query.validate();

			// JPQL
			jpql = query.serialize();

			// trace
			if (context.isTraceEnabled()) {
				LOGGER.info("(TRACE) JPQL: [" + jpql + "]");
			} else {
				LOGGER.debug(() -> "JPQL: [" + jpql + "]");
			}

		} catch (Exception e) {
			throw new QueryExecutionException("Failed to build query", e);
		}

		// execute
		return context.withEntityManager(entityManager -> {

			// configure query
			final TypedQuery q = entityManager.createQuery(jpql, query.getProjection().getQueryResultType());

			configuration.getLimit().ifPresent((l) -> q.setMaxResults(l));
			configuration.getOffset().ifPresent((o) -> q.setFirstResult(o));

			configuration.getParameter(JpaQueryHint.QUERY_PARAMETER_HINT, JpaQueryHint.class)
					.ifPresent(p -> q.setHint(p.getName(), p.getValue()));
			configuration.getParameter(JpaDatastore.QUERY_PARAMETER_LOCK_MODE, LockModeType.class)
					.ifPresent(p -> q.setLockMode(p));

			JpaDatastoreUtils.setupQueryParameters(q, resolutionContext);

			final QueryResultConverter converter = query.getProjection().getConverter();

			// execute and convert results
			return QueryUtils.asResultsStream(q.getResultList(), null).map(t -> converter.convert(t));

		});

	}

}
