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

import com.holonplatform.core.datastore.Datastore.OperationResult;
import com.holonplatform.core.datastore.Datastore.OperationType;
import com.holonplatform.core.datastore.DatastoreCommodityContext.CommodityConfigurationException;
import com.holonplatform.core.datastore.DatastoreCommodityFactory;
import com.holonplatform.core.datastore.operation.Delete;
import com.holonplatform.core.internal.datastore.operation.AbstractDelete;
import com.holonplatform.datastore.jpa.JpaWriteOption;
import com.holonplatform.datastore.jpa.config.JpaDatastoreCommodityContext;
import com.holonplatform.datastore.jpa.context.JpaOperationContext;
import com.holonplatform.datastore.jpa.jpql.context.JPQLResolutionContext;
import com.holonplatform.datastore.jpa.jpql.expression.JpaEntity;

/**
 * JPA {@link Delete}.
 *
 * @since 5.1.0
 */
public class JpaDelete extends AbstractDelete {

	private static final long serialVersionUID = 3232028846502770705L;

	// Commodity factory
	@SuppressWarnings("serial")
	public static final DatastoreCommodityFactory<JpaDatastoreCommodityContext, Delete> FACTORY = new DatastoreCommodityFactory<JpaDatastoreCommodityContext, Delete>() {

		@Override
		public Class<? extends Delete> getCommodityType() {
			return Delete.class;
		}

		@Override
		public Delete createCommodity(JpaDatastoreCommodityContext context) throws CommodityConfigurationException {
			return new JpaDelete(context);
		}
	};

	private final JpaOperationContext operationContext;

	public JpaDelete(JpaOperationContext operationContext) {
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
		final Class<?> entity = context.resolveOrFail(getConfiguration().getTarget(), JpaEntity.class).getEntityClass();

		return operationContext.withEntityManager(entityManager -> {

			// create a new instance
			Object instance = entity.newInstance();

			// write box values into instance
			operationContext.getBeanIntrospector().write(getConfiguration().getValue(), instance);

			// merge to ensure entity is not detached
			if (!entityManager.contains(instance)) {
				instance = entityManager.merge(instance);
			}

			// delete entity
			entityManager.remove(instance);

			operationContext.traceOperation("REMOVE entity [" + entity.getName() + "]");

			// check auto-flush
			if (operationContext.isAutoFlush() || getConfiguration().hasWriteOption(JpaWriteOption.FLUSH)) {
				entityManager.flush();

				operationContext.traceOperation("FLUSH EntityManager");
			}

			return OperationResult.builder().type(OperationType.DELETE).affectedCount(1).build();

		});
	}

}
