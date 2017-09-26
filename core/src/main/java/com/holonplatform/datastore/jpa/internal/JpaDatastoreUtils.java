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

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZonedDateTime;
import java.time.chrono.ChronoLocalDate;
import java.time.chrono.ChronoLocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;

import javax.persistence.Entity;
import javax.persistence.Query;

import com.holonplatform.core.Expression;
import com.holonplatform.core.Expression.InvalidExpressionException;
import com.holonplatform.core.ExpressionResolver;
import com.holonplatform.core.ExpressionResolver.ExpressionResolverHandler;
import com.holonplatform.core.ExpressionResolver.ExpressionResolverSupport;
import com.holonplatform.core.ExpressionResolver.ResolutionContext;
import com.holonplatform.core.Path;
import com.holonplatform.core.datastore.Datastore.WriteOption;
import com.holonplatform.core.internal.Logger;
import com.holonplatform.core.internal.utils.ConversionUtils;
import com.holonplatform.core.internal.utils.ObjectUtils;
import com.holonplatform.core.internal.utils.TypeUtils;
import com.holonplatform.core.property.Property;
import com.holonplatform.core.query.QueryExpression;
import com.holonplatform.core.temporal.TemporalType;
import com.holonplatform.datastore.jpa.JpaWriteOption;
import com.holonplatform.datastore.jpa.internal.expressions.JpaResolutionContext;

/**
 * JPA Datastore utilities.
 *
 * @since 5.0.0
 */
public final class JpaDatastoreUtils implements Serializable {

	private static final long serialVersionUID = 2217939571335356145L;

	private final static Logger LOGGER = JpaDatastoreLogger.create();

	private JpaDatastoreUtils() {
	}

	/**
	 * Checks if the {@link JpaWriteOption#FLUSH} is present among given write options.
	 * @param options Write options
	 * @return <code>true</code> if the {@link JpaWriteOption#FLUSH} is present
	 */
	static boolean isFlush(WriteOption[] options) {
		if (options != null) {
			for (WriteOption option : options) {
				if (JpaWriteOption.FLUSH == option) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Get the JPA entity name, using {@link Entity} annotation if available or the class simple name otherwise.
	 * @param entityClass Entity class (not null)
	 * @return The entity name
	 */
	public static String getEntityName(Class<?> entityClass) {
		ObjectUtils.argumentNotNull(entityClass, "Entity class must be not null");
		// check Entity annotation or use the class simple name
		return getEntityNameFromAnnotation(entityClass).orElse(entityClass.getSimpleName());
	}

	/**
	 * Get the entity name using {@link Entity#name()} annotation attribute, if available.
	 * @param entityClass Entity class
	 * @return The entity name as specified using {@link Entity#name()} annotation attribute, or an empty Optional if
	 *         the {@link Entity} annotation is not present or the <code>name</code> attribute has no value
	 */
	private static Optional<String> getEntityNameFromAnnotation(Class<?> entityClass) {
		if (entityClass.isAnnotationPresent(Entity.class)) {
			String name = entityClass.getAnnotation(Entity.class).name();
			if (name != null && !name.trim().equals("")) {
				return Optional.of(name);
			}
		}
		return Optional.empty();
	}

	/**
	 * Resolve given <code>expression</code> using given <code>resolver</code> to obtain a <code>resolutionType</code>
	 * type expression. If no {@link ExpressionResolver} is available to resolve given expression, an
	 * {@link InvalidExpressionException} is thrown. The resolved expression is validate using
	 * {@link Expression#validate()} before returning it to caller.
	 * @param <E> Expression type to resolve
	 * @param <R> Resolved expression type
	 * @param resolver {@link ExpressionResolverHandler}
	 * @param expression Expression to resolve
	 * @param resolutionType Expression type to obtain
	 * @param context Resolution context
	 * @return Resolved expression
	 * @throws InvalidExpressionException If an error occurred during resolution, or if no {@link ExpressionResolver} is
	 *         available to resolve given expression or if expression validation failed
	 */
	public static <E extends Expression, R extends Expression> R resolveExpression(ExpressionResolverHandler resolver,
			E expression, Class<R> resolutionType, ResolutionContext context) throws InvalidExpressionException {
		// resolve
		R resolved = resolver.resolve(expression, resolutionType, context).map(e -> {
			// validate
			e.validate();
			return e;
		}).orElse(null);
		// check
		if (resolved == null) {
			LOGGER.debug(() -> "No ExpressionResolver available to resolve expression [" + expression + "]");
			if (resolver instanceof ExpressionResolverSupport) {
				LOGGER.debug(() -> "Available ExpressionResolvers: "
						+ ((ExpressionResolverSupport) resolver).getExpressionResolvers());
			}
			throw new InvalidExpressionException("Failed to resolve expression [" + expression + "]");
		}
		return resolved;
	}

	/**
	 * Try to obtain the {@link TemporalType} of given <code>expression</code>, if the expression type is a temporal
	 * type.
	 * @param expression Query expression
	 * @param treatDateTypeAsDate <code>true</code> to return {@link TemporalType#DATE} for {@link Date} type if
	 *        temporal information is not available
	 * @return The expression {@link TemporalType}, empty if not available or applicable
	 */
	public static Optional<TemporalType> getTemporalType(Expression expression, boolean treatDateTypeAsDate) {
		if (expression != null) {
			Class<?> type = null;
			if (Path.class.isAssignableFrom(expression.getClass())) {
				type = ((Path<?>) expression).getType();
			} else if (QueryExpression.class.isAssignableFrom(expression.getClass())) {
				type = ((QueryExpression<?>) expression).getType();
			}

			if (type != null) {
				if (LocalDate.class.isAssignableFrom(type) || ChronoLocalDate.class.isAssignableFrom(type)) {
					return Optional.of(TemporalType.DATE);
				}
				if (LocalTime.class.isAssignableFrom(type) || OffsetTime.class.isAssignableFrom(type)) {
					return Optional.of(TemporalType.TIME);
				}
				if (LocalDateTime.class.isAssignableFrom(type) || OffsetDateTime.class.isAssignableFrom(type)
						|| ZonedDateTime.class.isAssignableFrom(type)
						|| ChronoLocalDateTime.class.isAssignableFrom(type)) {
					return Optional.of(TemporalType.DATE);
				}

				if (Date.class.isAssignableFrom(type) || Calendar.class.isAssignableFrom(type)) {
					if (Property.class.isAssignableFrom(expression.getClass())) {
						Optional<TemporalType> tt = ((Property<?>) expression).getConfiguration().getTemporalType();
						return treatDateTypeAsDate ? Optional.of(tt.orElse(TemporalType.DATE)) : tt;
					} else {
						return treatDateTypeAsDate ? Optional.of(TemporalType.DATE) : Optional.empty();
					}
				}

			}
		}
		return Optional.empty();
	}

	/**
	 * Convert given <code>temporalType</code> into a JPA {@link javax.persistence.TemporalType}.
	 * @param temporalType Temporal type to convert
	 * @return Converted temporal type
	 */
	public static javax.persistence.TemporalType convert(TemporalType temporalType) {
		if (temporalType != null) {
			switch (temporalType) {
			case DATE:
				return javax.persistence.TemporalType.DATE;
			case DATE_TIME:
				return javax.persistence.TemporalType.TIMESTAMP;
			case TIME:
				return javax.persistence.TemporalType.TIME;
			default:
				break;
			}
		}
		return null;
	}

	/**
	 * Setup query named parameters
	 * @param query Query
	 * @param context Resolution context
	 */
	public static void setupQueryParameters(Query query, JpaResolutionContext context) {
		context.getNamedParameters().forEach((n, p) -> {

			// date and times
			if (TypeUtils.isDate(p.getType())) {
				query.setParameter(n, (Date) p.getValue(),
						JpaDatastoreUtils.convert(p.getTemporalType().orElse(TemporalType.DATE)));
			} else if (TypeUtils.isCalendar(p.getType())) {
				query.setParameter(n, (Calendar) p.getValue(),
						JpaDatastoreUtils.convert(p.getTemporalType().orElse(TemporalType.DATE)));
			} else if (TypeUtils.isLocalTemporal(p.getType())) {
				if (LocalDate.class.isAssignableFrom(p.getType())) {
					query.setParameter(n, ConversionUtils.fromLocalDate((LocalDate) p.getValue()),
							JpaDatastoreUtils.convert(TemporalType.DATE));
				} else if (LocalTime.class.isAssignableFrom(p.getType())) {
					query.setParameter(n, fromLocalTime((LocalTime) p.getValue()),
							JpaDatastoreUtils.convert(TemporalType.TIME));
				} else if (LocalDateTime.class.isAssignableFrom(p.getType())) {
					query.setParameter(n, ConversionUtils.fromLocalDateTime((LocalDateTime) p.getValue()),
							JpaDatastoreUtils.convert(TemporalType.DATE_TIME));
				}
			} else {
				// default
				query.setParameter(n, p.getValue());
			}

		});
	}

	private static Date fromLocalTime(LocalTime time) {
		if (time != null) {
			final Calendar c = Calendar.getInstance();
			c.set(Calendar.YEAR, 1970);
			c.set(Calendar.MONTH, 0);
			c.set(Calendar.DAY_OF_MONTH, 1);
			c.set(Calendar.HOUR, 0);
			c.set(Calendar.MINUTE, 0);
			c.set(Calendar.SECOND, 0);
			c.set(Calendar.MILLISECOND, 0);
			return c.getTime();
		}
		return null;
	}

}
