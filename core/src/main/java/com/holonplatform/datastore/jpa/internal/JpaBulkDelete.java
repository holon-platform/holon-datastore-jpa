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

import javax.persistence.Query;

import com.holonplatform.core.Expression;
import com.holonplatform.core.Expression.InvalidExpressionException;
import com.holonplatform.core.ExpressionResolver;
import com.holonplatform.core.ExpressionResolver.ExpressionResolverHandler;
import com.holonplatform.core.ExpressionResolver.ResolutionContext;
import com.holonplatform.core.ExpressionResolverRegistry;
import com.holonplatform.core.datastore.DataTarget;
import com.holonplatform.core.datastore.Datastore.OperationResult;
import com.holonplatform.core.datastore.Datastore.OperationType;
import com.holonplatform.core.datastore.Datastore.WriteOption;
import com.holonplatform.core.datastore.bulk.BulkDelete;
import com.holonplatform.core.datastore.bulk.BulkUpdate;
import com.holonplatform.core.exceptions.DataAccessException;
import com.holonplatform.core.internal.Logger;
import com.holonplatform.core.internal.utils.ObjectUtils;
import com.holonplatform.core.query.QueryFilter;
import com.holonplatform.datastore.jpa.config.JpaDatastoreCommodityContext;
import com.holonplatform.datastore.jpa.internal.expressions.JPQLToken;
import com.holonplatform.datastore.jpa.internal.expressions.JpaResolutionContext;
import com.holonplatform.datastore.jpa.internal.expressions.JpaResolutionContext.AliasMode;
import com.holonplatform.datastore.jpa.internal.expressions.OperationStructure;

/**
 * {@link BulkUpdate} implementation using JPA Criteria API
 * 
 * @since 5.0.0
 */
public class JpaBulkDelete implements BulkDelete, ExpressionResolverHandler {

	/**
	 * Logger
	 */
	private final static Logger LOGGER = JpaDatastoreLogger.create();

	/**
	 * ExpressionResolverRegistry
	 */
	private final ExpressionResolverRegistry expressionResolverRegistry = ExpressionResolverRegistry.create();

	/**
	 * Datastore context
	 */
	private final JpaDatastoreCommodityContext context;

	/**
	 * DataTarget
	 */
	private final DataTarget<?> target;

	/**
	 * Write options
	 */
	private final WriteOption[] writeOptions;

	/**
	 * Restrictions
	 */
	private QueryFilter filter;

	/**
	 * Constructor
	 * @param context Datastore context (not null)
	 * @param target Data target (not null)
	 * @param options Write options
	 */
	@SuppressWarnings("unchecked")
	public JpaBulkDelete(JpaDatastoreCommodityContext context, DataTarget<?> target, WriteOption[] options) {
		super();

		ObjectUtils.argumentNotNull(context, "JpaDatastoreCommodityContext must be not null");
		ObjectUtils.argumentNotNull(target, "Data target must be not null");

		this.context = context;
		this.target = target;
		this.writeOptions = options;

		// inherit resolvers
		context.getExpressionResolvers().forEach(r -> expressionResolverRegistry.addExpressionResolver(r));
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.holonplatform.core.ExpressionResolver.ExpressionResolverBuilder#withExpressionResolver(com.holonplatform.core
	 * .ExpressionResolver)
	 */
	@Override
	public <E extends Expression, R extends Expression> BulkDelete withExpressionResolver(
			ExpressionResolver<E, R> expressionResolver) {
		expressionResolverRegistry.addExpressionResolver(expressionResolver);
		return this;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.holonplatform.core.ExpressionResolver.ExpressionResolverHandler#resolve(com.holonplatform.core.Expression,
	 * java.lang.Class, com.holonplatform.core.ExpressionResolver.ResolutionContext)
	 */
	@Override
	public <E extends Expression, R extends Expression> Optional<R> resolve(E expression, Class<R> resolutionType,
			ResolutionContext context) throws InvalidExpressionException {
		return expressionResolverRegistry.resolve(expression, resolutionType, context);
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.query.QueryFilter.QueryFilterSupport#filter(com.holonplatform.core.query.QueryFilter)
	 */
	@Override
	public BulkDelete filter(QueryFilter filter) {
		if (filter != null) {
			if (this.filter == null) {
				this.filter = filter;
			} else {
				this.filter = this.filter.and(filter);
			}
		}
		return this;
	}

	/**
	 * Get the filter
	 * @return Optional filter
	 */
	protected Optional<QueryFilter> getFilter() {
		return Optional.ofNullable(filter);
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.datastore.bulk.DMLClause#execute()
	 */
	@Override
	public OperationResult execute() {

		final JpaResolutionContext resolutionContext = JpaResolutionContext.create(context.getEntityManagerFactory(),
				context.getORMPlatform().orElse(null), this, AliasMode.AUTO);

		final String jpql;
		try {

			OperationStructure.Builder builder = OperationStructure.builder(OperationType.DELETE, target);
			getFilter().ifPresent(f -> builder.withFilter(f));

			// resolve OperationStructure
			jpql = JpaDatastoreUtils.resolveExpression(this, builder.build(), JPQLToken.class, resolutionContext)
					.getValue();

		} catch (InvalidExpressionException e) {
			throw new DataAccessException("Failed to configure delete operation", e);
		}

		// trace
		if (context.isTraceEnabled()) {
			LOGGER.info("(TRACE) JPQL: [" + jpql + "]");
		} else {
			LOGGER.debug(() -> "JPQL: [" + jpql + "]");
		}

		// execute
		return context.withEntityManager(entityManager -> {

			final Query query = entityManager.createQuery(jpql);

			JpaDatastoreUtils.setupQueryParameters(query, resolutionContext);

			int results = query.executeUpdate();

			// check auto-flush
			if (context.isAutoFlush() || JpaDatastoreUtils.isFlush(writeOptions)) {
				entityManager.flush();
			}

			return OperationResult.builder().type(OperationType.INSERT).affectedCount(results).build();

		});

	}

}
