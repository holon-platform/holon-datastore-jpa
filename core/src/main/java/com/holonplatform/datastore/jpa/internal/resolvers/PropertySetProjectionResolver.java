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
package com.holonplatform.datastore.jpa.internal.resolvers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Priority;
import javax.persistence.Tuple;

import com.holonplatform.core.Expression.InvalidExpressionException;
import com.holonplatform.core.ExpressionResolver;
import com.holonplatform.core.property.Property;
import com.holonplatform.core.query.PropertySetProjection;
import com.holonplatform.core.query.QueryExpression;
import com.holonplatform.datastore.jpa.ORMPlatform;
import com.holonplatform.datastore.jpa.internal.JpaDatastoreUtils;
import com.holonplatform.datastore.jpa.internal.converters.PropertyBoxResultArrayConverter;
import com.holonplatform.datastore.jpa.internal.converters.PropertyBoxTupleConverter;
import com.holonplatform.datastore.jpa.internal.expressions.DefaultProjectionContext;
import com.holonplatform.datastore.jpa.internal.expressions.JPQLToken;
import com.holonplatform.datastore.jpa.internal.expressions.JpaResolutionContext;
import com.holonplatform.datastore.jpa.internal.expressions.ProjectionContext;

/**
 * Property set projection resolver.
 *
 * @since 5.1.0
 */
@SuppressWarnings("rawtypes")
@Priority(Integer.MAX_VALUE)
public enum PropertySetProjectionResolver implements ExpressionResolver<PropertySetProjection, ProjectionContext> {

	INSTANCE;

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.Expression.ExpressionResolverFunction#resolve(com.holonplatform.core.Expression,
	 * com.holonplatform.core.ExpressionResolver.ResolutionContext)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Optional<ProjectionContext> resolve(PropertySetProjection expression, ResolutionContext context)
			throws InvalidExpressionException {

		// validate
		expression.validate();

		// context
		final JpaResolutionContext jpaContext = JpaResolutionContext.checkContext(context);

		final boolean tupleSupported = isTupleSupported(jpaContext);

		final DefaultProjectionContext ctx = new DefaultProjectionContext(jpaContext,
				tupleSupported ? Tuple.class : Object[].class);
		final List<Property<?>> selection = new ArrayList<>(expression.getPropertySet().size());
		final Map<Property<?>, String> selectionAlias = new HashMap<>();

		for (Property<?> property : expression.getPropertySet()) {
			if (QueryExpression.class.isAssignableFrom(property.getClass())) {
				selection.add(property);
				final String alias = ctx.addSelection(JpaDatastoreUtils
						.resolveExpression(context, (QueryExpression<?>) property, JPQLToken.class, context)
						.getValue());
				selectionAlias.put(property, alias);
			}
		}

		if (tupleSupported) {
			ctx.setConverter(new PropertyBoxTupleConverter(expression.getPropertySet(),
					selection.toArray(new Property<?>[0]), selectionAlias));
		} else {
			ctx.setConverter(new PropertyBoxResultArrayConverter(expression.getPropertySet(),
					selection.toArray(new Property<?>[0]), selectionAlias));
		}

		return Optional.of(ctx);
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
	public Class<? extends ProjectionContext> getResolvedType() {
		return ProjectionContext.class;
	}

	// TODO check openjpa/DATANUCLEUS
	private static boolean isTupleSupported(JpaResolutionContext context) {
		return context.getORMPlatform().map(p -> p == ORMPlatform.HIBERNATE).orElse(null);
	}

}
