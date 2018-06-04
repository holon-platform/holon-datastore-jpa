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
package com.holonplatform.datastore.jpa.internal.operations;

import java.util.Map;

import com.holonplatform.core.Expression.InvalidExpressionException;
import com.holonplatform.core.beans.BeanPropertySet;
import com.holonplatform.core.datastore.Datastore.OperationResult;
import com.holonplatform.core.datastore.Datastore.OperationType;
import com.holonplatform.core.datastore.DatastoreCommodityContext.CommodityConfigurationException;
import com.holonplatform.core.datastore.DatastoreCommodityFactory;
import com.holonplatform.core.datastore.bulk.BulkInsert;
import com.holonplatform.core.internal.Logger;
import com.holonplatform.core.internal.datastore.bulk.AbstractBulkInsert;
import com.holonplatform.core.property.PropertyBox;
import com.holonplatform.core.property.PropertySet;
import com.holonplatform.datastore.jpa.JpaWriteOption;
import com.holonplatform.datastore.jpa.config.JpaDatastoreCommodityContext;
import com.holonplatform.datastore.jpa.context.JpaOperationContext;
import com.holonplatform.datastore.jpa.internal.JpaDatastoreLogger;
import com.holonplatform.datastore.jpa.jpql.context.JPQLResolutionContext;
import com.holonplatform.datastore.jpa.jpql.expression.JpaEntity;

/**
 * JPA {@link BulkInsert} implementation.
 * 
 * @since 5.1.0
 */
public class JpaBulkInsert extends AbstractBulkInsert {

	private static final long serialVersionUID = -2659369449773116773L;

	private final static Logger LOGGER = JpaDatastoreLogger.create();

	// Commodity factory
	@SuppressWarnings("serial")
	public static final DatastoreCommodityFactory<JpaDatastoreCommodityContext, BulkInsert> FACTORY = new DatastoreCommodityFactory<JpaDatastoreCommodityContext, BulkInsert>() {

		@Override
		public Class<? extends BulkInsert> getCommodityType() {
			return BulkInsert.class;
		}

		@Override
		public BulkInsert createCommodity(JpaDatastoreCommodityContext context) throws CommodityConfigurationException {
			return new JpaBulkInsert(context);
		}
	};

	private final JpaOperationContext operationContext;

	public JpaBulkInsert(JpaOperationContext operationContext) {
		super();
		this.operationContext = operationContext;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.datastore.operation.ExecutableOperation#execute()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public OperationResult execute() {
		// validate
		getConfiguration().validate();

		// composition context
		final JPQLResolutionContext context = JPQLResolutionContext.create(operationContext);
		context.addExpressionResolvers(getConfiguration().getExpressionResolvers());

		// property set
		final PropertySet<?> propertySet = getConfiguration().getPropertySet()
				.orElseThrow(() -> new InvalidExpressionException("Missing bulk insert operation property set"));

		// get entity class
		final Class<?> entity = context.resolveOrFail(getConfiguration().getTarget(), JpaEntity.class).getEntityClass();

		return operationContext.withEntityManager(entityManager -> {

			// try to detect batch size
			int batchSize = operationContext.getDialect().getBatchSizeConfigurationProperty().map(propertyName -> {
				Map<String, Object> properties = entityManager.getEntityManagerFactory().getProperties();
				if (properties != null) {
					try {
						Object batchSizeValue = properties.get(propertyName);
						if (batchSizeValue != null) {
							if (batchSizeValue instanceof Number) {
								return ((Number) batchSizeValue).intValue();
							} else if (batchSizeValue instanceof String) {
								return Integer.valueOf((String) batchSizeValue);
							}
						}
					} catch (Exception e) {
						LOGGER.warn("Failed to detect batch insert size", e);
					}
				}
				return 0;
			}).orElse(0);

			// Bean property set
			final BeanPropertySet<Object> set = operationContext.getBeanIntrospector().getPropertySet(entity);

			int i = 0;
			for (PropertyBox value : getConfiguration().getValues()) {

				PropertyBox box = PropertyBox.builder(propertySet).invalidAllowed(true).build();
				propertySet.forEach(property -> {
					if (value.contains(property)) {
						box.setValue(property, value.getValue(property));
					}
				});

				// persist entity
				entityManager.persist(set.write(box, entity.newInstance()));

				operationContext.traceOperation("Bulk PERSIST entity [" + entity.getName() + "]");

				// check flush
				if (batchSize > 0 && i % batchSize == 0) {
					entityManager.flush();
					entityManager.clear();
				}
			}

			// check auto-flush
			if (operationContext.isAutoFlush() || getConfiguration().hasWriteOption(JpaWriteOption.FLUSH)) {
				entityManager.flush();

				operationContext.traceOperation("FLUSH EntityManager");
			}

			return OperationResult.builder().type(OperationType.INSERT)
					.affectedCount(getConfiguration().getValues().size()).build();

		});
	}

}
