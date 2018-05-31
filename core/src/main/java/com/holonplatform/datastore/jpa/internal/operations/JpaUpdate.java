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
import com.holonplatform.core.datastore.operation.Update;
import com.holonplatform.core.internal.datastore.operation.AbstractUpdate;
import com.holonplatform.datastore.jpa.JpaWriteOption;
import com.holonplatform.datastore.jpa.config.JpaDatastoreCommodityContext;
import com.holonplatform.datastore.jpa.context.JpaOperationContext;
import com.holonplatform.datastore.jpa.jpql.context.JPQLResolutionContext;
import com.holonplatform.datastore.jpa.jpql.expression.JpaEntity;

/**
 * JPA {@link Update}.
 *
 * @since 5.1.0
 */
public class JpaUpdate extends AbstractUpdate {

	private static final long serialVersionUID = 118863316193871221L;

	// Commodity factory
	@SuppressWarnings("serial")
	public static final DatastoreCommodityFactory<JpaDatastoreCommodityContext, Update> FACTORY = new DatastoreCommodityFactory<JpaDatastoreCommodityContext, Update>() {

		@Override
		public Class<? extends Update> getCommodityType() {
			return Update.class;
		}

		@Override
		public Update createCommodity(JpaDatastoreCommodityContext context)
				throws CommodityConfigurationException {
			return new JpaUpdate(context);
		}
	};

	private final JpaOperationContext operationContext;

	public JpaUpdate(JpaOperationContext operationContext) {
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

			// merge entity
			entityManager.merge(operationContext.getBeanIntrospector().write(getConfiguration().getValue(), instance));

			operationContext.traceOperation("MERGE entity [" + entity.getName() + "]");

			// check auto-flush
			if (operationContext.isAutoFlush() || getConfiguration().hasWriteOption(JpaWriteOption.FLUSH)) {
				entityManager.flush();

				operationContext.traceOperation("FLUSH EntityManager");
			}

			return OperationResult.builder().type(OperationType.UPDATE).affectedCount(1).build();

		});
	}

}
