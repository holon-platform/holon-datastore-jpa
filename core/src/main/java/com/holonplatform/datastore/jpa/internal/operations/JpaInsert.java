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

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.metamodel.Attribute.PersistentAttributeType;
import javax.persistence.metamodel.EmbeddableType;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.SingularAttribute;

import com.holonplatform.core.Path;
import com.holonplatform.core.beans.BeanPropertySet;
import com.holonplatform.core.datastore.Datastore.OperationResult;
import com.holonplatform.core.datastore.Datastore.OperationType;
import com.holonplatform.core.datastore.DatastoreCommodityContext.CommodityConfigurationException;
import com.holonplatform.core.datastore.DatastoreCommodityFactory;
import com.holonplatform.core.datastore.DefaultWriteOption;
import com.holonplatform.core.datastore.operation.InsertOperation;
import com.holonplatform.core.internal.Logger;
import com.holonplatform.core.internal.datastore.operation.AbstractInsertOperation;
import com.holonplatform.core.property.PathProperty;
import com.holonplatform.core.property.PathPropertyBoxAdapter;
import com.holonplatform.core.property.PropertyBox;
import com.holonplatform.datastore.jpa.JpaWriteOption;
import com.holonplatform.datastore.jpa.config.JpaDatastoreCommodityContext;
import com.holonplatform.datastore.jpa.context.JpaOperationContext;
import com.holonplatform.datastore.jpa.internal.JpaDatastoreLogger;
import com.holonplatform.datastore.jpa.jpql.context.JPQLResolutionContext;
import com.holonplatform.datastore.jpa.jpql.expression.JpaEntity;

/**
 * JPA {@link InsertOperation}.
 *
 * @since 5.1.0
 */
public class JpaInsert extends AbstractInsertOperation {

	private static final long serialVersionUID = 463250918269446451L;

	/**
	 * Logger
	 */
	private final static Logger LOGGER = JpaDatastoreLogger.create();

	// Commodity factory
	@SuppressWarnings("serial")
	public static final DatastoreCommodityFactory<JpaDatastoreCommodityContext, InsertOperation> FACTORY = new DatastoreCommodityFactory<JpaDatastoreCommodityContext, InsertOperation>() {

		@Override
		public Class<? extends InsertOperation> getCommodityType() {
			return InsertOperation.class;
		}

		@Override
		public InsertOperation createCommodity(JpaDatastoreCommodityContext context)
				throws CommodityConfigurationException {
			return new JpaInsert(context);
		}
	};

	private final JpaOperationContext operationContext;

	public JpaInsert(JpaOperationContext operationContext) {
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
			// Bean property set
			final BeanPropertySet<Object> set = operationContext.getBeanIntrospector().getPropertySet(entity);
			// persist entity
			entityManager.persist(set.write(getConfiguration().getValue(), instance));
			
			operationContext.traceOperation("PERSIST entity [" + entity.getName() + "]");

			// check auto-flush
			if (operationContext.isAutoFlush() || getConfiguration().hasWriteOption(JpaWriteOption.FLUSH)) {
				entityManager.flush();
				
				operationContext.traceOperation("FLUSH EntityManager");
			}

			OperationResult.Builder result = OperationResult.builder().type(OperationType.INSERT).affectedCount(1);

			// get ids
			setInsertedIds(result, entityManager, set, entity, instance,
					getConfiguration().hasWriteOption(DefaultWriteOption.BRING_BACK_GENERATED_IDS),
					getConfiguration().getValue());

			return result.build();

		});
	}

	/**
	 * Set the entity id values of given <code>entity</code> instance to be returned as an {@link OperationResult}.
	 * @param result OperationResult in which to set the ids
	 * @param entityManager EntityManager
	 * @param set Entity bean property set
	 * @param entity Entity class
	 * @param instance Entity instance
	 */
	@SuppressWarnings("unchecked")
	private static void setInsertedIds(OperationResult.Builder result, EntityManager entityManager,
			BeanPropertySet<Object> set, Class<?> entity, Object instance, boolean bringBackGeneratedIds,
			PropertyBox propertyBox) {
		final PathPropertyBoxAdapter adapter;
		if (bringBackGeneratedIds) {
			adapter = PathPropertyBoxAdapter.create(propertyBox);
		} else {
			adapter = null;
		}
		try {
			getIds(entityManager, set, entity).forEach(p -> {
				Object keyValue = set.read(p, instance);
				result.withInsertedKey(p, keyValue);
				if (adapter != null && keyValue != null && adapter.contains(p)) {
					// set in propertybox
					adapter.setValue(p, keyValue);
				}
			});
		} catch (Exception e) {
			LOGGER.warn("Failed to obtain entity id(s) value", e);
		}
	}

	/**
	 * Get the id {@link Path}s of given <code>entity</code>.
	 * @param entityManager EntityManager
	 * @param set Entity bean property set
	 * @param entity Entity class
	 * @return Entity ids, an empty list if none
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static List<Path> getIds(EntityManager entityManager, BeanPropertySet<Object> set, Class<?> entity) {
		final List<Path> ids = new LinkedList<>();
		final EntityType et = entityManager.getMetamodel().entity(entity);
		try {
			if (et.hasSingleIdAttribute()) {
				SingularAttribute attribute = et.getId(et.getIdType().getJavaType());
				if (attribute.getPersistentAttributeType() == PersistentAttributeType.EMBEDDED) {
					final String idName = attribute.getName();
					final Path parent = Path.of(idName, attribute.getJavaType());
					EmbeddableType<?> emb = entityManager.getMetamodel().embeddable(attribute.getJavaType());
					emb.getAttributes().forEach(a -> {
						ids.add(Path.of(a.getName(), a.getJavaType()).parent(parent));
					});
				} else {
					String idName = et.getId(et.getIdType().getJavaType()).getName();
					Optional<PathProperty<Object>> idProperty = set.getProperty(idName);
					idProperty.ifPresent(p -> ids.add(p));
				}
			} else {
				try {
					Set<SingularAttribute> attributes = et.getIdClassAttributes();
					if (attributes != null) {
						attributes.forEach(a -> {
							Optional<PathProperty<Object>> idProperty = set.getProperty(a.getName());
							idProperty.ifPresent(p -> ids.add(p));
						});
					}
				} catch (@SuppressWarnings("unused") IllegalArgumentException e) {
					// ignore
				}
			}
		} catch (Exception e) {
			LOGGER.warn("Failed to obtain entity id(s) value", e);
		}
		return ids;
	}

}
