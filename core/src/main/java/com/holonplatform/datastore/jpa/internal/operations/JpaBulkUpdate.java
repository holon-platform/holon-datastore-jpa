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

import jakarta.persistence.Query;

import com.holonplatform.core.datastore.Datastore.OperationResult;
import com.holonplatform.core.datastore.Datastore.OperationType;
import com.holonplatform.core.datastore.DatastoreCommodityContext.CommodityConfigurationException;
import com.holonplatform.core.datastore.DatastoreCommodityFactory;
import com.holonplatform.core.datastore.bulk.BulkUpdate;
import com.holonplatform.core.internal.datastore.bulk.AbstractBulkUpdate;
import com.holonplatform.core.internal.datastore.operation.common.UpdateOperationConfiguration;
import com.holonplatform.datastore.jpa.JpaWriteOption;
import com.holonplatform.datastore.jpa.config.JpaDatastoreCommodityContext;
import com.holonplatform.datastore.jpa.context.JpaOperationContext;
import com.holonplatform.datastore.jpa.jpql.context.JPQLResolutionContext;
import com.holonplatform.datastore.jpa.jpql.expression.JPQLStatement;

/**
 * JPA {@link BulkUpdate} implementation.
 * 
 * @since 5.1.0
 */
public class JpaBulkUpdate extends AbstractBulkUpdate {

	private static final long serialVersionUID = -470171503703187731L;

	// Commodity factory
	@SuppressWarnings("serial")
	public static final DatastoreCommodityFactory<JpaDatastoreCommodityContext, BulkUpdate> FACTORY = new DatastoreCommodityFactory<JpaDatastoreCommodityContext, BulkUpdate>() {

		@Override
		public Class<? extends BulkUpdate> getCommodityType() {
			return BulkUpdate.class;
		}

		@Override
		public BulkUpdate createCommodity(JpaDatastoreCommodityContext context) throws CommodityConfigurationException {
			return new JpaBulkUpdate(context);
		}
	};

	private final JpaOperationContext operationContext;

	public JpaBulkUpdate(JpaOperationContext operationContext) {
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

		// create operation configuration
		final UpdateOperationConfiguration configuration = UpdateOperationConfiguration.builder()
				.target(getConfiguration().getTarget()).withWriteOptions(getConfiguration().getWriteOptions())
				.withExpressionResolvers(getConfiguration().getExpressionResolvers())
				.values(getConfiguration().getValues()).filter(getConfiguration().getFilter().orElse(null)).build();

		// resolve
		final JPQLStatement statement = context.resolveOrFail(configuration, JPQLStatement.class);

		// trace
		operationContext.trace(statement.getJPQL());

		// execute
		return operationContext.withEntityManager(entityManager -> {

			final Query query = entityManager.createQuery(statement.getJPQL());

			context.setupQueryParameters(query);

			int results = query.executeUpdate();

			// check auto-flush
			if (operationContext.isAutoFlush() || getConfiguration().hasWriteOption(JpaWriteOption.FLUSH)) {
				entityManager.flush();

				operationContext.traceOperation("FLUSH EntityManager");
			}

			return OperationResult.builder().type(OperationType.UPDATE).affectedCount(results).build();

		});
	}

}
