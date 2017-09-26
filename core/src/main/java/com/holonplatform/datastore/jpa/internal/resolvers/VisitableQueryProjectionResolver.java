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
import java.util.HashMap;
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
import com.holonplatform.core.datastore.DataTarget;
import com.holonplatform.core.datastore.relational.RelationalTarget;
import com.holonplatform.core.internal.query.ConstantExpressionProjection;
import com.holonplatform.core.internal.query.QueryProjectionVisitor;
import com.holonplatform.core.internal.query.QueryProjectionVisitor.VisitableQueryProjection;
import com.holonplatform.core.internal.utils.TypeUtils;
import com.holonplatform.core.property.PathProperty;
import com.holonplatform.core.property.Property;
import com.holonplatform.core.query.BeanProjection;
import com.holonplatform.core.query.CountAllProjection;
import com.holonplatform.core.query.FunctionExpression;
import com.holonplatform.core.query.PathExpression;
import com.holonplatform.core.query.PropertySetProjection;
import com.holonplatform.core.query.QueryExpression;
import com.holonplatform.datastore.jpa.ORMPlatform;
import com.holonplatform.datastore.jpa.internal.JpaDatastoreUtils;
import com.holonplatform.datastore.jpa.internal.converters.BeanResultArrayConverter;
import com.holonplatform.datastore.jpa.internal.converters.BeanTupleConverter;
import com.holonplatform.datastore.jpa.internal.converters.PropertyBoxResultArrayConverter;
import com.holonplatform.datastore.jpa.internal.converters.PropertyBoxTupleConverter;
import com.holonplatform.datastore.jpa.internal.converters.SingleSelectionResultConverter;
import com.holonplatform.datastore.jpa.internal.expressions.DefaultProjectionContext;
import com.holonplatform.datastore.jpa.internal.expressions.JPQLToken;
import com.holonplatform.datastore.jpa.internal.expressions.JpaResolutionContext;
import com.holonplatform.datastore.jpa.internal.expressions.ProjectionContext;

/**
 * {@link VisitableQueryProjection} expression resolver.
 * 
 * @since 5.0.0
 */
@SuppressWarnings("rawtypes")
@Priority(Integer.MAX_VALUE - 10)
public enum VisitableQueryProjectionResolver implements ExpressionResolver<VisitableQueryProjection, ProjectionContext>,
		QueryProjectionVisitor<ProjectionContext, JpaResolutionContext> {

	INSTANCE;

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.ExpressionResolver#getExpressionType()
	 */
	@Override
	public Class<? extends VisitableQueryProjection> getExpressionType() {
		return VisitableQueryProjection.class;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.ExpressionResolver#getResolvedType()
	 */
	@Override
	public Class<? extends ProjectionContext> getResolvedType() {
		return ProjectionContext.class;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.Expression.ExpressionResolverFunction#resolve(com.holonplatform.core.Expression,
	 * com.holonplatform.core.ExpressionResolver.ResolutionContext)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Optional<ProjectionContext> resolve(VisitableQueryProjection expression, ResolutionContext context)
			throws InvalidExpressionException {

		// validate
		expression.validate();

		// resolve using visitor
		return Optional
				.ofNullable((ProjectionContext) expression.accept(this, JpaResolutionContext.checkContext(context)));
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.holonplatform.core.internal.query.QueryProjectionVisitor#visit(com.holonplatform.core.datastore.DataTarget,
	 * java.lang.Object)
	 */
	@Override
	public <T> ProjectionContext visit(DataTarget<T> projection, JpaResolutionContext context) {
		final RelationalTarget<?> target = JpaDatastoreUtils.resolveExpression(context, projection,
				RelationalTarget.class, context);
		DefaultProjectionContext<?, T> ctx = new DefaultProjectionContext<>(context, target.getType());
		ctx.addSelection(JpaDatastoreUtils.resolveExpression(context, target, JPQLToken.class, context).getValue(),
				false);
		ctx.setConverter(
				new SingleSelectionResultConverter<>(PathExpression.create(target.getName(), target.getType())));
		return ctx;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.holonplatform.core.internal.query.QueryProjectionVisitor#visit(com.holonplatform.core.property.PathProperty,
	 * java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T> ProjectionContext visit(PathProperty<T> projection, JpaResolutionContext context) {
		final Class<?> type = projection.getConverter().map(c -> c.getModelType())
				.orElse(((PathProperty) projection).getType());
		DefaultProjectionContext<Object, T> ctx = new DefaultProjectionContext<>(context, TypeUtils.box(type));
		ctx.addSelection(JpaDatastoreUtils.resolveExpression(context, projection, JPQLToken.class, context).getValue(),
				false);
		ctx.setConverter(new SingleSelectionResultConverter<>(projection));
		return ctx;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.internal.query.QueryProjectionVisitor#visit(com.holonplatform.core.internal.query.
	 * ConstantExpressionProjection, java.lang.Object)
	 */
	@Override
	public <T> ProjectionContext visit(ConstantExpressionProjection<T> projection, JpaResolutionContext context) {
		DefaultProjectionContext<T, T> ctx = new DefaultProjectionContext<>(context,
				TypeUtils.box(projection.getType()));
		ctx.addSelection(serializeLiteralValue(projection.getValue()), false);
		ctx.setConverter(new SingleSelectionResultConverter<>(projection));
		return ctx;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.internal.query.QueryProjectionVisitor#visit(com.holonplatform.core.query.
	 * FunctionExpression, java.lang.Object)
	 */
	@Override
	public <T> ProjectionContext visit(FunctionExpression<T> projection, JpaResolutionContext context) {
		DefaultProjectionContext<T, T> ctx = new DefaultProjectionContext<>(context,
				TypeUtils.box(projection.getType()));
		ctx.addSelection(JpaDatastoreUtils.resolveExpression(context, projection, JPQLToken.class, context).getValue(),
				false);
		ctx.setConverter(new SingleSelectionResultConverter<>(projection));
		return ctx;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.internal.query.QueryProjectionVisitor#visit(com.holonplatform.core.query.
	 * PropertySetProjection, java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public ProjectionContext visit(PropertySetProjection projection, JpaResolutionContext context) {

		final boolean tupleSupported = isTupleSupported(context);

		final DefaultProjectionContext ctx = new DefaultProjectionContext<>(context,
				tupleSupported ? Tuple.class : Object[].class);
		final List<Property<?>> selection = new ArrayList<>(projection.getPropertySet().size());
		final Map<Property<?>, String> selectionAlias = new HashMap<>();

		for (Property<?> property : projection.getPropertySet()) {
			if (QueryExpression.class.isAssignableFrom(property.getClass())) {
				selection.add(property);
				final String alias = ctx.addSelection(JpaDatastoreUtils
						.resolveExpression(context, (QueryExpression<?>) property, JPQLToken.class, context)
						.getValue());
				selectionAlias.put(property, alias);
			}
		}

		if (tupleSupported) {
			ctx.setConverter(new PropertyBoxTupleConverter(projection.getPropertySet(),
					selection.toArray(new Property<?>[0]), selectionAlias));
		} else {
			ctx.setConverter(new PropertyBoxResultArrayConverter(projection.getPropertySet(),
					selection.toArray(new Property<?>[0]), selectionAlias));
		}
		return ctx;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.holonplatform.core.internal.query.QueryProjectionVisitor#visit(com.holonplatform.core.query.BeanProjection,
	 * java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T> ProjectionContext visit(BeanProjection<T> projection, JpaResolutionContext context) {

		final boolean tupleSupported = isTupleSupported(context);

		final DefaultProjectionContext ctx = new DefaultProjectionContext<>(context,
				tupleSupported ? Tuple.class : Object[].class);
		final BeanPropertySet<T> bps = BeanIntrospector.get().getPropertySet(projection.getBeanClass());

		List<Path> selection = projection.getSelection().map(s -> Arrays.asList(s))
				.orElse(bps.stream().map(p -> (Path) p).collect(Collectors.toList()));

		final List<Path<?>> selectionPaths = new ArrayList<>(selection.size());
		final Map<Path<?>, String> selectionAlias = new LinkedHashMap<>();

		for (Path<?> path : selection) {
			if (QueryExpression.class.isAssignableFrom(path.getClass())) {
				selectionPaths.add(path);
				final String alias = ctx.addSelection(JpaDatastoreUtils
						.resolveExpression(context, (QueryExpression<?>) path, JPQLToken.class, context).getValue());
				selectionAlias.put(path, alias);
			}
		}

		if (tupleSupported) {
			ctx.setConverter(new BeanTupleConverter<>(bps, selectionPaths.toArray(new Path<?>[0]), selectionAlias));
		} else {
			ctx.setConverter(
					new BeanResultArrayConverter<>(bps, selectionPaths.toArray(new Path<?>[0]), selectionAlias));
		}
		return ctx;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.internal.query.QueryProjectionVisitor#visit(com.holonplatform.core.query.
	 * CountAllProjection, java.lang.Object)
	 */
	@Override
	public ProjectionContext visit(CountAllProjection projection, JpaResolutionContext context) {
		return visit(FunctionExpression.count(
				context.getTarget().orElseThrow(() -> new InvalidExpressionException("Missing context data target"))),
				context);
	}

	// TODO check openjpa/DATANUCLEUS
	private static boolean isTupleSupported(JpaResolutionContext context) {
		return context.getORMPlatform().map(p -> p == ORMPlatform.HIBERNATE).orElse(null);
	}

	private static String serializeLiteralValue(Object value) {
		if (value != null) {
			if (TypeUtils.isNumber(value.getClass())) {
				if (TypeUtils.isLong(value.getClass())) {
					return value.toString() + "L";
				}
				if (TypeUtils.isFloat(value.getClass())) {
					return value.toString() + "f";
				}
				return value.toString();
			}
			if (TypeUtils.isEnum(value.getClass())) {
				return value.getClass().getName() + "." + ((Enum) value).name();
			}

			// TODO date and time
		}
		return null;
	}

}
