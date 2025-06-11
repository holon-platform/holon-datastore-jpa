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
package com.holonplatform.datastore.jpa.internal.jpql.context;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Query;

import com.holonplatform.core.Expression;
import com.holonplatform.core.Expression.InvalidExpressionException;
import com.holonplatform.core.ExpressionResolver;
import com.holonplatform.core.ExpressionResolver.ResolutionContext;
import com.holonplatform.core.ExpressionResolverRegistry;
import com.holonplatform.core.internal.Logger;
import com.holonplatform.core.internal.utils.ObjectUtils;
import com.holonplatform.core.internal.utils.TypeUtils;
import com.holonplatform.core.temporal.TemporalType;
import com.holonplatform.datastore.jpa.ORMPlatform;
import com.holonplatform.datastore.jpa.context.JpaContext;
import com.holonplatform.datastore.jpa.dialect.ORMDialect;
import com.holonplatform.datastore.jpa.internal.JpqlDatastoreLogger;
import com.holonplatform.datastore.jpa.jpql.JPQLValueDeserializer;
import com.holonplatform.datastore.jpa.jpql.JPQLValueSerializer;
import com.holonplatform.datastore.jpa.jpql.context.JPQLContextParametersHandler;
import com.holonplatform.datastore.jpa.jpql.context.JPQLResolutionContext;

/**
 * Default {@link JPQLResolutionContext} implementation.
 *
 * @since 5.1.0
 */
public class DefaultJPQLResolutionContext implements JPQLResolutionContext {

	private final static Logger LOGGER = JpqlDatastoreLogger.create();

	/**
	 * Expression resolvers
	 */
	private final ExpressionResolverRegistry expressionResolverRegistry = ExpressionResolverRegistry.create();

	/**
	 * JPA context
	 */
	private final JpaContext context;

	/**
	 * Optional parent context
	 */
	private final JPQLResolutionContext parent;

	/**
	 * Named parameters handler
	 */
	private final JPQLContextParametersHandler namedParametersHandler;

	/**
	 * Context hierarchy sequence
	 */
	private final int contextSequence;

	/**
	 * Default constructor.
	 * @param context JPA context (not null)
	 */
	public DefaultJPQLResolutionContext(JpaContext context) {
		super();
		ObjectUtils.argumentNotNull(context, "JPA context must be not null");
		this.context = context;
		this.parent = null;
		this.contextSequence = 0;
		this.namedParametersHandler = JPQLContextParametersHandler.create();
		// inherit resolvers
		addExpressionResolvers(context.getExpressionResolvers());
	}

	/**
	 * Constructor with parent composition context.
	 * @param parent Parent context (not null)
	 * @param parent Parent composition context
	 */
	public DefaultJPQLResolutionContext(JPQLResolutionContext parent) {
		super();
		ObjectUtils.argumentNotNull(parent, "Parent context must be not null");
		this.context = parent;
		this.parent = parent;
		this.contextSequence = JPQLResolutionContext.getContextSequence(parent, JPQLResolutionContext.class) + 1;
		this.namedParametersHandler = parent.getNamedParametersHandler();
		// inherit resolvers
		addExpressionResolvers(parent.getExpressionResolvers());
	}

	/**
	 * Get the JPA context.
	 * @return the JPA context
	 */
	protected JpaContext getContext() {
		return context;
	}

	/**
	 * Get the context hierarchy sequence.
	 * @return the context sequence
	 */
	protected int getContextSequence() {
		return contextSequence;
	}

	@Override
	public Optional<JPQLResolutionContext> getParent() {
		return Optional.ofNullable(parent);
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jpa.context.JPQLCompositionContext#childContext()
	 */
	@Override
	public JPQLResolutionContext childContext() {
		return new DefaultJPQLResolutionContext(this);
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jpa.context.JpaContext#getEntityManagerFactory()
	 */
	@Override
	public EntityManagerFactory getEntityManagerFactory() {
		return getContext().getEntityManagerFactory();
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jpa.context.JpaContext#getDialect()
	 */
	@Override
	public ORMDialect getDialect() {
		return getContext().getDialect();
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jpa.context.JpaContext#getORMPlatform()
	 */
	@Override
	public Optional<ORMPlatform> getORMPlatform() {
		return getContext().getORMPlatform();
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jpa.context.JpaContext#getValueSerializer()
	 */
	@Override
	public JPQLValueSerializer getValueSerializer() {
		return getContext().getValueSerializer();
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jpa.context.JpaContext#getValueDeserializer()
	 */
	@Override
	public JPQLValueDeserializer getValueDeserializer() {
		return getContext().getValueDeserializer();
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jpa.context.JpaContext#trace(java.lang.String)
	 */
	@Override
	public void trace(String jpql) {
		getContext().trace(jpql);
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jpa.context.JpaContext#traceOperation(java.lang.String)
	 */
	@Override
	public void traceOperation(String operation) {
		getContext().traceOperation(operation);
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jpa.context.JPQLCompositionContext#getNamedParametersHandler()
	 */
	@Override
	public JPQLContextParametersHandler getNamedParametersHandler() {
		return namedParametersHandler;
	}

	// Expression resolvers

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

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jpa.context.JPQLCompositionContext#setupQueryParameters(jakarta.persistence.Query)
	 */
	@Override
	public void setupQueryParameters(Query query) {
		ObjectUtils.argumentNotNull(query, "Query must be not null");

		LOGGER.debug(() -> "Setup query parameters - Named parameters count: "
				+ getNamedParametersHandler().getNamedParameters().size());

		getNamedParametersHandler().getNamedParameters().forEach((n, p) -> {

			// date and times
			if (TypeUtils.isDate(p.getType())) {
				query.setParameter(n, (Date) p.getValue(), convert(p.getTemporalType().orElse(TemporalType.DATE)));

				LOGGER.debug(() -> "Setted parameter with name [" + n + "] using Date value [" + p.getValue() + "]");

			} else if (TypeUtils.isCalendar(p.getType())) {
				query.setParameter(n, (Calendar) p.getValue(), convert(p.getTemporalType().orElse(TemporalType.DATE)));

				LOGGER.debug(
						() -> "Setted parameter with name [" + n + "] using Calendar value [" + p.getValue() + "]");

			} else if (TypeUtils.isLocalTemporal(p.getType()) && !getDialect().temporalTypeParametersSupported()) {
				final Date date;
				jakarta.persistence.TemporalType tt = null;
				if (LocalDate.class.isAssignableFrom(p.getType())) {
					date = java.sql.Date.valueOf((LocalDate) p.getValue());
					tt = jakarta.persistence.TemporalType.DATE;
				} else if (LocalDateTime.class.isAssignableFrom(p.getType())) {
					date = java.sql.Timestamp.valueOf((LocalDateTime) p.getValue());
					tt = jakarta.persistence.TemporalType.TIMESTAMP;
				} else if (LocalTime.class.isAssignableFrom(p.getType())) {
					date = java.sql.Time.valueOf((LocalTime) p.getValue());
					tt = jakarta.persistence.TemporalType.TIME;
				} else {
					date = null;
				}

				query.setParameter(n, date, tt);

				LOGGER.debug(
						() -> "Setted Temporal type parameter with name [" + n + "] using Date  value [" + date + "]");
			} else {
				// default
				query.setParameter(n, p.getValue());

				LOGGER.debug(() -> "Setted parameter with name [" + n + "] using value [" + p.getValue() + "]");
			}

		});

	}

	/**
	 * Convert given <code>temporalType</code> into a JPA {@link jakarta.persistence.TemporalType}.
	 * @param temporalType Temporal type to convert
	 * @return Converted temporal type
	 */
	private static jakarta.persistence.TemporalType convert(TemporalType temporalType) {
		if (temporalType != null) {
			switch (temporalType) {
			case DATE:
				return jakarta.persistence.TemporalType.DATE;
			case DATE_TIME:
				return jakarta.persistence.TemporalType.TIMESTAMP;
			case TIME:
				return jakarta.persistence.TemporalType.TIME;
			default:
				break;
			}
		}
		return null;
	}

}
