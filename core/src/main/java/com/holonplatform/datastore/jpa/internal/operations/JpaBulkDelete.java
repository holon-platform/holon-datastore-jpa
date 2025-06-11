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
import com.holonplatform.core.datastore.bulk.BulkDelete;
import com.holonplatform.core.internal.datastore.bulk.AbstractBulkDelete;
import com.holonplatform.core.internal.datastore.operation.common.DeleteOperationConfiguration;
import com.holonplatform.datastore.jpa.JpaWriteOption;
import com.holonplatform.datastore.jpa.config.JpaDatastoreCommodityContext;
import com.holonplatform.datastore.jpa.context.JpaOperationContext;
import com.holonplatform.datastore.jpa.jpql.context.JPQLResolutionContext;
import com.holonplatform.datastore.jpa.jpql.expression.JPQLStatement;

/**
 * JPA {@link BulkDelete} implementation.
 * 
 * @since 5.1.0
 */
public class JpaBulkDelete extends AbstractBulkDelete {

	private static final long serialVersionUID = 2063304568395041817L;

	// Commodity factory
	@SuppressWarnings("serial")
	public static final DatastoreCommodityFactory<JpaDatastoreCommodityContext, BulkDelete> FACTORY = new DatastoreCommodityFactory<JpaDatastoreCommodityContext, BulkDelete>() {

		@Override
		public Class<? extends BulkDelete> getCommodityType() {
			return BulkDelete.class;
		}

		@Override
		public BulkDelete createCommodity(JpaDatastoreCommodityContext context) throws CommodityConfigurationException {
			return new JpaBulkDelete(context);
		}
	};

	private final JpaOperationContext operationContext;

	public JpaBulkDelete(JpaOperationContext operationContext) {
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
		final DeleteOperationConfiguration configuration = DeleteOperationConfiguration.builder()
				.target(getConfiguration().getTarget()).withWriteOptions(getConfiguration().getWriteOptions())
				.withExpressionResolvers(getConfiguration().getExpressionResolvers())
				.filter(getConfiguration().getFilter().orElse(null)).build();

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

			return OperationResult.builder().type(OperationType.DELETE).affectedCount(results).build();

		});
	}

}
