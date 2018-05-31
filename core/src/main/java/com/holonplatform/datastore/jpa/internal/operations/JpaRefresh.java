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

import com.holonplatform.core.datastore.DatastoreCommodityContext.CommodityConfigurationException;
import com.holonplatform.core.datastore.DatastoreCommodityFactory;
import com.holonplatform.core.datastore.operation.Refresh;
import com.holonplatform.core.internal.datastore.operation.AbstractRefresh;
import com.holonplatform.core.property.PropertyBox;
import com.holonplatform.datastore.jpa.config.JpaDatastoreCommodityContext;
import com.holonplatform.datastore.jpa.context.JpaOperationContext;
import com.holonplatform.datastore.jpa.jpql.context.JPQLResolutionContext;
import com.holonplatform.datastore.jpa.jpql.expression.JpaEntity;

/**
 * JPA {@link Refresh}.
 *
 * @since 5.1.0
 */
public class JpaRefresh extends AbstractRefresh {

	private static final long serialVersionUID = -7246131153825486563L;

	// Commodity factory
	@SuppressWarnings("serial")
	public static final DatastoreCommodityFactory<JpaDatastoreCommodityContext, Refresh> FACTORY = new DatastoreCommodityFactory<JpaDatastoreCommodityContext, Refresh>() {

		@Override
		public Class<? extends Refresh> getCommodityType() {
			return Refresh.class;
		}

		@Override
		public Refresh createCommodity(JpaDatastoreCommodityContext context)
				throws CommodityConfigurationException {
			return new JpaRefresh(context);
		}
	};

	private final JpaOperationContext operationContext;

	public JpaRefresh(JpaOperationContext operationContext) {
		super();
		this.operationContext = operationContext;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.datastore.operation.ExecutableOperation#execute()
	 */
	@Override
	public PropertyBox execute() {

		// validate
		getConfiguration().validate();

		// composition context
		final JPQLResolutionContext context = JPQLResolutionContext.create(operationContext);
		context.addExpressionResolvers(getConfiguration().getExpressionResolvers());

		// get entity class
		final Class<?> entity = context.resolveOrFail(getConfiguration().getTarget(), JpaEntity.class).getEntityClass();

		return operationContext.withEntityManager(entityManager -> {

			// create a new instance
			Object instance = operationContext.getBeanIntrospector().write(getConfiguration().getValue(),
					entity.newInstance());

			// ensure managed
			Object managed = !entityManager.contains(instance) ? entityManager.merge(instance) : instance;
			// refresh
			entityManager.refresh(managed);

			operationContext.traceOperation("REFRESH entity [" + entity.getName() + "]");

			// return refreshed entity property values
			return operationContext.getBeanIntrospector()
					.read(PropertyBox.builder(getConfiguration().getValue()).invalidAllowed(true).build(), managed);
		});
	}

}
