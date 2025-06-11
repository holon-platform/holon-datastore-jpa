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
package com.holonplatform.datastore.jpa.internal.resolvers.projection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import jakarta.annotation.Priority;
import jakarta.persistence.Tuple;

import com.holonplatform.core.Expression.InvalidExpressionException;
import com.holonplatform.core.TypedExpression;
import com.holonplatform.core.internal.Logger;
import com.holonplatform.core.property.Property;
import com.holonplatform.core.query.PropertySetProjection;
import com.holonplatform.core.query.QueryProjection;
import com.holonplatform.datastore.jpa.internal.JpqlDatastoreLogger;
import com.holonplatform.datastore.jpa.internal.converters.PropertyBoxResultArrayConverter;
import com.holonplatform.datastore.jpa.internal.converters.PropertyBoxTupleConverter;
import com.holonplatform.datastore.jpa.internal.jpql.expression.DefaultJPQLProjection;
import com.holonplatform.datastore.jpa.jpql.context.JPQLContextExpressionResolver;
import com.holonplatform.datastore.jpa.jpql.context.JPQLResolutionContext;
import com.holonplatform.datastore.jpa.jpql.expression.JPQLExpression;
import com.holonplatform.datastore.jpa.jpql.expression.JPQLProjection;

/**
 * Property set projection resolver.
 *
 * @since 5.1.0
 */
@SuppressWarnings("rawtypes")
@Priority(Integer.MAX_VALUE - 100)
public enum PropertySetProjectionResolver
		implements JPQLContextExpressionResolver<PropertySetProjection, JPQLProjection> {

	/**
	 * Singleton instance
	 */
	INSTANCE;

	/**
	 * Logger
	 */
	private static final Logger LOGGER = JpqlDatastoreLogger.create();

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jpa.resolvers.JPQLContextExpressionResolver#resolve(com.holonplatform.core.
	 * Expression, com.holonplatform.datastore.jpa.context.JPQLResolutionContext)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Optional<JPQLProjection> resolve(PropertySetProjection expression, JPQLResolutionContext context)
			throws InvalidExpressionException {

		// validate
		expression.validate();

		LOGGER.debug(() -> "Resolve property set projection [" + expression + "] - Tuple supported: "
				+ context.getDialect().isTupleSupported());

		final DefaultJPQLProjection projection = new DefaultJPQLProjection(context,
				context.getDialect().isTupleSupported() ? Tuple.class : Object[].class, expression.getType());

		final int size = expression.getPropertySet().size();

		List<TypedExpression<?>> selection = new ArrayList<>(size);
		Map<TypedExpression<?>, String> selectionAlias = new HashMap<>(size);
		Map<TypedExpression<?>, Property<?>> selectionProperties = new HashMap<>(size);

		for (Property<?> property : expression.getPropertySet()) {
			if (QueryProjection.class.isAssignableFrom(property.getClass())) {
				selection.add(property);
				// resolve and get alias
				final String alias = projection
						.addSelection(context.resolveOrFail(property, JPQLExpression.class).getValue());
				selectionAlias.put(property, alias);
				selectionProperties.put(property, property);
			}
		}

		if (context.getDialect().isTupleSupported()) {
			projection.setConverter(new PropertyBoxTupleConverter(expression.getPropertySet(),
					selection.toArray(new TypedExpression<?>[selection.size()]), selectionAlias, selectionProperties));
		} else {
			projection.setConverter(new PropertyBoxResultArrayConverter(expression.getPropertySet(),
					selection.toArray(new TypedExpression<?>[selection.size()]), selectionAlias, selectionProperties));
		}

		return Optional.of(projection);
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.ExpressionResolver#getExpressionType()
	 */
	@Override
	public Class<? extends PropertySetProjection> getExpressionType() {
		return PropertySetProjection.class;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.ExpressionResolver#getResolvedType()
	 */
	@Override
	public Class<? extends JPQLProjection> getResolvedType() {
		return JPQLProjection.class;
	}

}
