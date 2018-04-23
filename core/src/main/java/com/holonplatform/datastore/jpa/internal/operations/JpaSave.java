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

import com.holonplatform.core.beans.BeanPropertySet;
import com.holonplatform.core.datastore.Datastore.OperationResult;
import com.holonplatform.core.datastore.DatastoreCommodityContext.CommodityConfigurationException;
import com.holonplatform.core.datastore.DatastoreCommodityFactory;
import com.holonplatform.core.datastore.operation.InsertOperation;
import com.holonplatform.core.datastore.operation.PropertyBoxOperationConfiguration;
import com.holonplatform.core.datastore.operation.SaveOperation;
import com.holonplatform.core.datastore.operation.UpdateOperation;
import com.holonplatform.core.internal.Logger;
import com.holonplatform.core.internal.datastore.operation.AbstractSaveOperation;
import com.holonplatform.datastore.jpa.config.JpaDatastoreCommodityContext;
import com.holonplatform.datastore.jpa.context.JpaOperationContext;
import com.holonplatform.datastore.jpa.internal.JpaDatastoreLogger;
import com.holonplatform.datastore.jpa.jpql.context.JPQLResolutionContext;
import com.holonplatform.datastore.jpa.jpql.expression.JpaEntity;

/**
 * JPA {@link SaveOperation}.
 *
 * @since 5.1.0
 */
public class JpaSave extends AbstractSaveOperation {

	private static final long serialVersionUID = -823102485809986906L;

	private final static Logger LOGGER = JpaDatastoreLogger.create();

	// Commodity factory
	@SuppressWarnings("serial")
	public static final DatastoreCommodityFactory<JpaDatastoreCommodityContext, SaveOperation> FACTORY = new DatastoreCommodityFactory<JpaDatastoreCommodityContext, SaveOperation>() {

		@Override
		public Class<? extends SaveOperation> getCommodityType() {
			return SaveOperation.class;
		}

		@Override
		public SaveOperation createCommodity(JpaDatastoreCommodityContext context)
				throws CommodityConfigurationException {
			return new JpaSave(context);
		}
	};

	private final JpaOperationContext operationContext;

	public JpaSave(JpaOperationContext operationContext) {
		super();
		this.operationContext = operationContext;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.datastore.operation.ExecutableOperation#execute()
	 */
	@Override
	public OperationResult execute() {

		// validate
		getConfiguration().validate();

		// composition context
		final JPQLResolutionContext context = JPQLResolutionContext.create(operationContext);
		context.addExpressionResolvers(getConfiguration().getExpressionResolvers());

		// get entity class
		@SuppressWarnings("unchecked")
		final JpaEntity<Object> entity = context.resolveOrFail(getConfiguration().getTarget(), JpaEntity.class);

		return operationContext.withEntityManager(entityManager -> {

			// Bean property set
			final BeanPropertySet<Object> set = operationContext.getBeanIntrospector()
					.getPropertySet(entity.getEntityClass());

			// create instance and write values
			Object instance = set.write(getConfiguration().getValue(), entity.getEntityClass().newInstance());

			OperationResult result;

			// check has identifier
			boolean isNew;
			try {
				isNew = entity.isNew(instance);
			} catch (IllegalStateException e) {
				LOGGER.debug(
						() -> "New entity instance check not available from entity metadata, falling back to PersistenceUnitUtil.getIdentifier",
						e);
				isNew = operationContext.getEntityManagerFactory().getPersistenceUnitUtil()
						.getIdentifier(instance) == null;
			}

			if (isNew) {
				result = insert(getConfiguration());
			} else {
				result = update(getConfiguration());
			}

			return result;

		});
	}

	/**
	 * Perform an insert operation using given <code>configuration</code>.
	 * @param configuration Operation configuration
	 * @return Operation result
	 */
	private OperationResult insert(PropertyBoxOperationConfiguration configuration) {
		return operationContext.create(InsertOperation.class).target(configuration.getTarget())
				.value(configuration.getValue()).withWriteOptions(configuration.getWriteOptions()).execute();
	}

	/**
	 * Perform an update operation using given <code>configuration</code>.
	 * @param configuration Operation configuration
	 * @return Operation result
	 */
	private OperationResult update(PropertyBoxOperationConfiguration configuration) {
		return operationContext.create(UpdateOperation.class).target(configuration.getTarget())
				.value(configuration.getValue()).withWriteOptions(configuration.getWriteOptions()).execute();
	}

}
