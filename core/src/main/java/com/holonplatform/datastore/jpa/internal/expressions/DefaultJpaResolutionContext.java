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
package com.holonplatform.datastore.jpa.internal.expressions;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import javax.persistence.EntityManagerFactory;

import com.holonplatform.core.Expression;
import com.holonplatform.core.Expression.InvalidExpressionException;
import com.holonplatform.core.ExpressionResolver;
import com.holonplatform.core.ExpressionResolver.ExpressionResolverHandler;
import com.holonplatform.core.ExpressionResolver.ResolutionContext;
import com.holonplatform.core.ExpressionResolverRegistry;
import com.holonplatform.core.internal.utils.ObjectUtils;
import com.holonplatform.datastore.jpa.ORMPlatform;
import com.holonplatform.datastore.jpa.internal.support.ParameterValue;

/**
 * Default {@link JpaResolutionContext} implementation.
 *
 * @since 5.0.0
 */
public class DefaultJpaResolutionContext extends AbstractJpaResolutionContext {

	private final ExpressionResolverRegistry expressionResolverRegistry = ExpressionResolverRegistry.create();

	private final EntityManagerFactory entityManagerFactory;
	private final ORMPlatform platform;

	private int parameterSequence = 0;

	private final Map<String, ParameterValue> namedParameters = new HashMap<>();

	private final AtomicInteger sequenceProvider = new AtomicInteger(0);

	public DefaultJpaResolutionContext(EntityManagerFactory entityManagerFactory, ORMPlatform platform,
			ExpressionResolverHandler expressionResolverHandler, AliasMode aliasMode) {
		super(null, 0, aliasMode);
		ObjectUtils.argumentNotNull(entityManagerFactory, "EntityManagerFactory must be not null");
		ObjectUtils.argumentNotNull(expressionResolverHandler, "ExpressionResolverHandler must be not null");
		this.entityManagerFactory = entityManagerFactory;
		this.platform = platform;

		// inherit resolvers
		addExpressionResolvers(expressionResolverHandler.getExpressionResolvers());
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jpa.internal.jpql.expressions.JpaResolutionContext#getEntityManagerFactory()
	 */
	@Override
	public EntityManagerFactory getEntityManagerFactory() {
		return entityManagerFactory;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jpa.internal.jpql.expressions.JpaResolutionContext#getORMPlatform()
	 */
	@Override
	public Optional<ORMPlatform> getORMPlatform() {
		return Optional.ofNullable(platform);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.holonplatform.datastore.jpa.internal.jpql.expressions.JpaResolutionContext#childContext(com.holonplatform.
	 * datastore.jpa.internal.jpql.expressions.JpaResolutionContext.AliasMode)
	 */
	@Override
	public JpaResolutionContext childContext(AliasMode aliasMode) {
		int nextSequence = sequenceProvider.incrementAndGet();
		return new SubContext(this, nextSequence, sequenceProvider, aliasMode);
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
	 * @see com.holonplatform.core.ExpressionResolver.ExpressionResolverHandler#getExpressionResolvers()
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public Iterable<ExpressionResolver> getExpressionResolvers() {
		return expressionResolverRegistry.getExpressionResolvers();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.holonplatform.core.ExpressionResolver.ExpressionResolverSupport#addExpressionResolver(com.holonplatform.core.
	 * ExpressionResolver)
	 */
	@Override
	public <E extends Expression, R extends Expression> void addExpressionResolver(
			ExpressionResolver<E, R> expressionResolver) {
		expressionResolverRegistry.addExpressionResolver(expressionResolver);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.holonplatform.core.ExpressionResolver.ExpressionResolverSupport#removeExpressionResolver(com.holonplatform.
	 * core.ExpressionResolver)
	 */
	@Override
	public <E extends Expression, R extends Expression> void removeExpressionResolver(
			ExpressionResolver<E, R> expressionResolver) {
		expressionResolverRegistry.removeExpressionResolver(expressionResolver);
	}

	@Override
	public synchronized String addNamedParameter(ParameterValue value) {
		final String name = generateParameterName();
		namedParameters.put(name, value);
		return name;
	}

	@Override
	public Map<String, ParameterValue> getNamedParameters() {
		return Collections.unmodifiableMap(namedParameters);
	}

	/**
	 * Generate a named parameter name
	 * @return Parameter name
	 */
	protected String generateParameterName() {
		parameterSequence++;
		return "p" + String.format("%04d", parameterSequence);
	}

	class SubContext extends AbstractJpaResolutionContext {

		private final AtomicInteger parentSequenceProvider;

		public SubContext(JpaResolutionContext parent, int sequence, AtomicInteger sequenceProvider,
				AliasMode aliasMode) {
			super(parent, sequence, aliasMode);
			ObjectUtils.argumentNotNull(parent, "Parent context must be not null");
			this.parentSequenceProvider = sequenceProvider;
		}

		protected JpaResolutionContext parent() {
			return getParent().orElseThrow(() -> new IllegalStateException("Missing parent context"));
		}

		/*
		 * (non-Javadoc)
		 * @see com.holonplatform.datastore.jpa.internal.jpql.expressions.JpaResolutionContext#getEntityManagerFactory()
		 */
		@Override
		public EntityManagerFactory getEntityManagerFactory() {
			return parent().getEntityManagerFactory();
		}

		/*
		 * (non-Javadoc)
		 * @see com.holonplatform.datastore.jpa.internal.jpql.expressions.JpaResolutionContext#getORMPlatform()
		 */
		@Override
		public Optional<ORMPlatform> getORMPlatform() {
			return parent().getORMPlatform();
		}

		/*
		 * (non-Javadoc)
		 * @see com.holonplatform.datastore.jpa.internal.jpql.expressions.JpaResolutionContext#addNamedParameter(com.
		 * holonplatform.datastore.jpa.internal.support.ParameterValue)
		 */
		@Override
		public String addNamedParameter(ParameterValue value) {
			return parent().addNamedParameter(value);
		}

		/*
		 * (non-Javadoc)
		 * @see com.holonplatform.datastore.jpa.internal.jpql.expressions.JpaResolutionContext#getNamedParameters()
		 */
		@Override
		public Map<String, ParameterValue> getNamedParameters() {
			return parent().getNamedParameters();
		}

		/*
		 * (non-Javadoc)
		 * @see com.holonplatform.core.ExpressionResolver.ExpressionResolverHandler#resolve(com.holonplatform.core.
		 * Expression, java.lang.Class, com.holonplatform.core.ExpressionResolver.ResolutionContext)
		 */
		@Override
		public <E extends Expression, R extends Expression> Optional<R> resolve(E expression, Class<R> resolutionType,
				ResolutionContext context) throws InvalidExpressionException {
			return parent().resolve(expression, resolutionType, context);
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * com.holonplatform.core.ExpressionResolver.ExpressionResolverSupport#addExpressionResolver(com.holonplatform.
		 * core.ExpressionResolver)
		 */
		@Override
		public <E extends Expression, R extends Expression> void addExpressionResolver(
				ExpressionResolver<E, R> expressionResolver) {
			parent().addExpressionResolver(expressionResolver);
		}

		/*
		 * (non-Javadoc)
		 * @see com.holonplatform.core.ExpressionResolver.ExpressionResolverSupport#removeExpressionResolver(com.
		 * holonplatform.core.ExpressionResolver)
		 */
		@Override
		public <E extends Expression, R extends Expression> void removeExpressionResolver(
				ExpressionResolver<E, R> expressionResolver) {
			parent().removeExpressionResolver(expressionResolver);
		}

		/*
		 * (non-Javadoc)
		 * @see com.holonplatform.core.ExpressionResolver.ExpressionResolverHandler#getExpressionResolvers()
		 */
		@SuppressWarnings("rawtypes")
		@Override
		public Iterable<ExpressionResolver> getExpressionResolvers() {
			return parent().getExpressionResolvers();
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * com.holonplatform.datastore.jpa.internal.jpql.expressions.JpaResolutionContext#childContext(com.holonplatform
		 * .datastore.jpa.internal.jpql.expressions.JpaResolutionContext.AliasMode)
		 */
		@Override
		public JpaResolutionContext childContext(AliasMode aliasMode) {
			int nextSequence = parentSequenceProvider.incrementAndGet();
			return new SubContext(this, nextSequence, parentSequenceProvider, aliasMode);
		}
	}

}
