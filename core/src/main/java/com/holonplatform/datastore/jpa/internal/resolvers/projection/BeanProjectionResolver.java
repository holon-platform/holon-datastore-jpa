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
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.annotation.Priority;
import jakarta.persistence.Tuple;

import com.holonplatform.core.Expression.InvalidExpressionException;
import com.holonplatform.core.Path;
import com.holonplatform.core.beans.BeanIntrospector;
import com.holonplatform.core.beans.BeanPropertySet;
import com.holonplatform.core.internal.Logger;
import com.holonplatform.core.query.BeanProjection;
import com.holonplatform.core.query.QueryExpression;
import com.holonplatform.datastore.jpa.internal.JpqlDatastoreLogger;
import com.holonplatform.datastore.jpa.internal.converters.BeanResultArrayConverter;
import com.holonplatform.datastore.jpa.internal.converters.BeanTupleConverter;
import com.holonplatform.datastore.jpa.internal.jpql.expression.DefaultJPQLProjection;
import com.holonplatform.datastore.jpa.jpql.context.JPQLContextExpressionResolver;
import com.holonplatform.datastore.jpa.jpql.context.JPQLResolutionContext;
import com.holonplatform.datastore.jpa.jpql.expression.JPQLExpression;
import com.holonplatform.datastore.jpa.jpql.expression.JPQLProjection;

/**
 * Bean projection resolver.
 *
 * @since 5.1.0
 */
@SuppressWarnings("rawtypes")
@Priority(Integer.MAX_VALUE - 100)
public enum BeanProjectionResolver implements JPQLContextExpressionResolver<BeanProjection, JPQLProjection> {

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
	public Optional<JPQLProjection> resolve(BeanProjection expression, JPQLResolutionContext context)
			throws InvalidExpressionException {

		// validate
		expression.validate();

		LOGGER.debug(() -> "Resolve bean projection [" + expression + "] - Tuple supported: "
				+ context.getDialect().isTupleSupported());

		final DefaultJPQLProjection<?, ?> projection = new DefaultJPQLProjection<>(context,
				context.getDialect().isTupleSupported() ? Tuple.class : Object[].class, expression.getBeanClass());

		final BeanPropertySet<?> bps = BeanIntrospector.get().getPropertySet(expression.getBeanClass());

		List<Path> selection = ((BeanProjection<?>) expression).getSelection().map(Arrays::asList).orElse(null);
		if (selection == null) {
			// use bean property set
			selection = bps.stream().map(p -> (Path) p).collect(Collectors.toList());
		}

		final List<Path<?>> selectionPaths = new ArrayList<>(selection.size());
		final Map<Path<?>, String> selectionAlias = new LinkedHashMap<>();

		for (Path path : selection) {
			if (QueryExpression.class.isAssignableFrom(path.getClass())) {
				selectionPaths.add(path);
				final String alias = projection.addSelection(
						context.resolveOrFail((QueryExpression<?>) path, JPQLExpression.class).getValue());
				selectionAlias.put(path, alias);
			}
		}

		if (context.getDialect().isTupleSupported()) {
			projection
					.setConverter(new BeanTupleConverter(bps, selectionPaths.toArray(new Path<?>[0]), selectionAlias));
		} else {
			projection.setConverter(
					new BeanResultArrayConverter(bps, selectionPaths.toArray(new Path<?>[0]), selectionAlias));
		}

		return Optional.of(projection);
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.ExpressionResolver#getExpressionType()
	 */
	@Override
	public Class<? extends BeanProjection> getExpressionType() {
		return BeanProjection.class;
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
