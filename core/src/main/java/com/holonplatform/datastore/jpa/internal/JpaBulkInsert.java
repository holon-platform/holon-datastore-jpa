/*
 * Copyright 2000-2016 Holon TDCN.
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.holonplatform.core.Expression;
import com.holonplatform.core.Expression.InvalidExpressionException;
import com.holonplatform.core.ExpressionResolver;
import com.holonplatform.core.ExpressionResolver.ExpressionResolverHandler;
import com.holonplatform.core.ExpressionResolver.ResolutionContext;
import com.holonplatform.core.ExpressionResolverRegistry;
import com.holonplatform.core.beans.BeanIntrospector;
import com.holonplatform.core.beans.BeanPropertySet;
import com.holonplatform.core.datastore.DataTarget;
import com.holonplatform.core.datastore.Datastore.OperationResult;
import com.holonplatform.core.datastore.Datastore.OperationType;
import com.holonplatform.core.datastore.Datastore.WriteOption;
import com.holonplatform.core.datastore.bulk.BulkInsert;
import com.holonplatform.core.exceptions.DataAccessException;
import com.holonplatform.core.internal.Logger;
import com.holonplatform.core.internal.utils.ObjectUtils;
import com.holonplatform.core.property.PropertyBox;
import com.holonplatform.core.property.PropertySet;
import com.holonplatform.datastore.jpa.config.JpaDatastoreCommodityContext;
import com.holonplatform.datastore.jpa.internal.expressions.JpaEntity;
import com.holonplatform.datastore.jpa.internal.expressions.JpaResolutionContext;
import com.holonplatform.datastore.jpa.internal.expressions.JpaResolutionContext.AliasMode;

/**
 * JPA {@link BulkInsert} implementation.
 * 
 * @since 5.0.0
 */
public class JpaBulkInsert implements BulkInsert, ExpressionResolverHandler {

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
	 * BeanIntrospector
	 */
	private final BeanIntrospector beanIntrospector;

	/**
	 * DataTarget
	 */
	private final DataTarget<?> target;

	/**
	 * PropertySet
	 */
	private final PropertySet<?> propertySet;

	/**
	 * Write options
	 */
	private final WriteOption[] writeOptions;

	/**
	 * Values to insert
	 */
	private final List<PropertyBox> values = new ArrayList<>();

	/**
	 * Create a JPA batch insert.
	 * @param context Datastore context
	 * @param beanIntrospector Bean introspector
	 * @param target Data target
	 * @param propertySet Property set
	 * @param writeOptions Write options
	 */
	@SuppressWarnings("unchecked")
	public JpaBulkInsert(JpaDatastoreCommodityContext context, BeanIntrospector beanIntrospector, DataTarget<?> target,
			PropertySet<?> propertySet, WriteOption[] writeOptions) {
		super();

		ObjectUtils.argumentNotNull(context, "JpaDatastoreCommodityContext must be not null");
		ObjectUtils.argumentNotNull(target, "Data target must be not null");
		ObjectUtils.argumentNotNull(propertySet, "PropertySet must be not null");

		this.context = context;
		this.beanIntrospector = beanIntrospector;
		this.target = target;
		this.propertySet = propertySet;
		this.writeOptions = writeOptions;

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
	public <E extends Expression, R extends Expression> BulkInsert withExpressionResolver(
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
	 * @see com.holonplatform.core.datastore.bulk.BulkInsert#add(com.holonplatform.core.property.PropertyBox)
	 */
	@Override
	public BulkInsert add(PropertyBox propertyBox) {
		ObjectUtils.argumentNotNull(propertyBox, "PropertyBox to insert must be not null");
		values.add(propertyBox);
		return this;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.datastore.bulk.DMLClause#execute()
	 */
	@Override
	public OperationResult execute() {
		if (values.isEmpty()) {
			throw new DataAccessException("No values to insert");
		}

		return context.withEntityManager(entityManager -> {

			// try to detect batch size
			int batchSize = 0;
			Map<String, Object> properties = entityManager.getEntityManagerFactory().getProperties();
			if (properties != null) {
				try {
					Object hibernateBatchSize = properties.get("hibernate.jdbc.batch_size");
					if (hibernateBatchSize != null) {
						if (hibernateBatchSize instanceof Number) {
							batchSize = ((Number) hibernateBatchSize).intValue();
						} else if (hibernateBatchSize instanceof String) {
							batchSize = Integer.valueOf((String) hibernateBatchSize);
						}
					}

					if (batchSize <= 0) {
						Object eclipselinkBatchSize = properties.get("eclipselink.jdbc.batch-writing.size");
						if (eclipselinkBatchSize instanceof Number) {
							batchSize = ((Number) eclipselinkBatchSize).intValue();
						} else if (eclipselinkBatchSize instanceof String) {
							batchSize = Integer.valueOf((String) eclipselinkBatchSize);
						}
					}
				} catch (Exception e) {
					LOGGER.warn("Failed to detect batch insert size", e);
				}
			}

			// Get the JPA entity class
			final JpaResolutionContext resolutionContext = JpaResolutionContext.create(
					context.getEntityManagerFactory(), context.getORMPlatform().orElse(null), this,
					AliasMode.UNSUPPORTED);

			JpaEntity<?> jpaEntity = resolutionContext.resolve(target, JpaEntity.class, resolutionContext).orElseThrow(
					() -> new InvalidExpressionException("Failed to resolve data target [" + target + "]"));
			jpaEntity.validate();

			final Class<?> entity = jpaEntity.getEntityClass();

			// Bean property set
			final BeanPropertySet<Object> set = beanIntrospector.getPropertySet(entity);

			int i = 0;
			for (PropertyBox value : values) {
				// persist entity
				entityManager.persist(set.write(adapt(value), entity.newInstance()));
				// check flush
				if (batchSize > 0 && i % batchSize == 0) {
					entityManager.flush();
					entityManager.clear();
				}
			}

			// check auto-flush
			if (context.isAutoFlush() || JpaDatastoreUtils.isFlush(writeOptions)) {
				entityManager.flush();
			}

			return OperationResult.builder().type(OperationType.INSERT).affectedCount(values.size()).build();

		});
	}

	@SuppressWarnings("unchecked")
	private PropertyBox adapt(PropertyBox propertyBox) {
		PropertyBox.Builder builder = PropertyBox.builder(propertySet);
		propertySet.forEach(p -> {
			builder.set(p, propertyBox.getValue(p));
		});
		return builder.build();
	}

}
