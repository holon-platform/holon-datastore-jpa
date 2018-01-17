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
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Priority;
import javax.persistence.Tuple;

import com.holonplatform.core.Expression.InvalidExpressionException;
import com.holonplatform.core.ExpressionResolver;
import com.holonplatform.core.Path;
import com.holonplatform.core.beans.BeanIntrospector;
import com.holonplatform.core.beans.BeanPropertySet;
import com.holonplatform.core.query.BeanProjection;
import com.holonplatform.core.query.QueryExpression;
import com.holonplatform.datastore.jpa.ORMPlatform;
import com.holonplatform.datastore.jpa.internal.converters.BeanResultArrayConverter;
import com.holonplatform.datastore.jpa.internal.converters.BeanTupleConverter;
import com.holonplatform.datastore.jpa.internal.expressions.DefaultProjectionContext;
import com.holonplatform.datastore.jpa.internal.expressions.JPQLToken;
import com.holonplatform.datastore.jpa.internal.expressions.JpaResolutionContext;
import com.holonplatform.datastore.jpa.internal.expressions.ProjectionContext;

/**
 * Bean projection resolver.
 *
 * @since 5.1.0
 */
@SuppressWarnings("rawtypes")
@Priority(Integer.MAX_VALUE)
public enum BeanProjectionResolver implements ExpressionResolver<BeanProjection, ProjectionContext> {

	INSTANCE;

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.Expression.ExpressionResolverFunction#resolve(com.holonplatform.core.Expression,
	 * com.holonplatform.core.ExpressionResolver.ResolutionContext)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Optional<ProjectionContext> resolve(BeanProjection expression, ResolutionContext context)
			throws InvalidExpressionException {

		// validate
		expression.validate();

		// context
		final JpaResolutionContext jpaContext = JpaResolutionContext.checkContext(context);

		final boolean tupleSupported = isTupleSupported(jpaContext);

		final DefaultProjectionContext ctx = new DefaultProjectionContext(jpaContext,
				tupleSupported ? Tuple.class : Object[].class);
		final BeanPropertySet<?> bps = BeanIntrospector.get().getPropertySet(expression.getBeanClass());

		List<Path> selection = ((BeanProjection<?>) expression).getSelection().map(s -> Arrays.asList(s)).orElse(null);
		if (selection == null) {
			// use bean property set
			selection = bps.stream().map(p -> (Path) p).collect(Collectors.toList());
		}

		final List<Path<?>> selectionPaths = new ArrayList<>(selection.size());
		final Map<Path<?>, String> selectionAlias = new LinkedHashMap<>();

		for (Path<?> path : selection) {
			if (QueryExpression.class.isAssignableFrom(path.getClass())) {
				selectionPaths.add(path);
				final String alias = ctx.addSelection(
						jpaContext.resolveExpression((QueryExpression<?>) path, JPQLToken.class).getValue());
				selectionAlias.put(path, alias);
			}
		}

		if (tupleSupported) {
			ctx.setConverter(new BeanTupleConverter(bps, selectionPaths.toArray(new Path<?>[0]), selectionAlias));
		} else {
			ctx.setConverter(new BeanResultArrayConverter(bps, selectionPaths.toArray(new Path<?>[0]), selectionAlias));
		}

		return Optional.of(ctx);
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
	public Class<? extends ProjectionContext> getResolvedType() {
		return ProjectionContext.class;
	}

	// TODO check openjpa/DATANUCLEUS
	private static boolean isTupleSupported(JpaResolutionContext context) {
		return context.getORMPlatform().map(p -> p == ORMPlatform.HIBERNATE).orElse(null);
	}

}
