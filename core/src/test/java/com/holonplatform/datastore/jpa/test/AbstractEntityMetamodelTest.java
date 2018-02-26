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
package com.holonplatform.datastore.jpa.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.holonplatform.core.datastore.DataTarget;
import com.holonplatform.core.datastore.Datastore;
import com.holonplatform.core.datastore.DatastoreCommodity;
import com.holonplatform.core.datastore.DatastoreCommodityContext.CommodityConfigurationException;
import com.holonplatform.core.internal.utils.TypeUtils;
import com.holonplatform.datastore.jpa.JpaTarget;
import com.holonplatform.datastore.jpa.config.JpaDatastoreCommodityContext;
import com.holonplatform.datastore.jpa.config.JpaDatastoreCommodityFactory;
import com.holonplatform.datastore.jpa.context.JpaOperationContext;
import com.holonplatform.datastore.jpa.jpql.context.JPQLResolutionContext;
import com.holonplatform.datastore.jpa.jpql.expression.JpaEntity;
import com.holonplatform.datastore.jpa.test.model.metamodel.EmbeddedEntityId;
import com.holonplatform.datastore.jpa.test.model.metamodel.EmbeddedIdEntity;
import com.holonplatform.datastore.jpa.test.model.metamodel.EntityIdClass;
import com.holonplatform.datastore.jpa.test.model.metamodel.IdClassEntity;
import com.holonplatform.datastore.jpa.test.model.metamodel.MultiEmbeddedId;
import com.holonplatform.datastore.jpa.test.model.metamodel.MultiEmbeddedIdEntity;
import com.holonplatform.datastore.jpa.test.model.metamodel.SingleIdEntity;
import com.holonplatform.datastore.jpa.test.model.metamodel.SinglePrimitiveIdEntity;

@SuppressWarnings("serial")
public class AbstractEntityMetamodelTest {

	protected static Datastore datastore;

	protected static Datastore getDatastore() {
		return datastore;
	}

	@Test
	public void testSingleId() {

		JpaEntity<SingleIdEntity> entity = getDatastore().create(EntityMetamodelCommodity.class)
				.resolve(JpaTarget.of(SingleIdEntity.class));

		assertEquals(SingleIdEntity.class, entity.getEntityClass());

		assertEquals("SingleIdEntity", entity.getEntityName());
		assertTrue(entity.getIdType().isPresent());
		assertEquals(String.class, entity.getIdType().get());

		SingleIdEntity i1 = new SingleIdEntity();
		i1.setValue("test");

		assertFalse(entity.getId(i1).isPresent());
		assertTrue(entity.isNew(i1));

		i1.setId("xxx");
		assertTrue(entity.getId(i1).isPresent());
		assertEquals("xxx", entity.getId(i1).get());
		assertFalse(entity.isNew(i1));

	}

	@Test
	public void testSinglePrimitiveId() {

		JpaEntity<SinglePrimitiveIdEntity> entity = getDatastore().create(EntityMetamodelCommodity.class)
				.resolve(JpaTarget.of(SinglePrimitiveIdEntity.class));

		assertEquals(SinglePrimitiveIdEntity.class, entity.getEntityClass());

		assertEquals("primitive_id", entity.getEntityName());

		assertTrue(entity.getIdType().isPresent());
		assertTrue(TypeUtils.isLong(entity.getIdType().get()));

		SinglePrimitiveIdEntity i1 = new SinglePrimitiveIdEntity();
		i1.setValue("test");

		assertTrue(entity.isNew(i1));

		i1.setId(1L);
		assertTrue(entity.getId(i1).isPresent());
		assertEquals(Long.valueOf(1L), entity.getId(i1).get());
		assertFalse(entity.isNew(i1));

	}

	@Test
	public void testEmbeddedId() {

		JpaEntity<EmbeddedIdEntity> entity = getDatastore().create(EntityMetamodelCommodity.class)
				.resolve(JpaTarget.of(EmbeddedIdEntity.class));

		assertEquals(EmbeddedIdEntity.class, entity.getEntityClass());

		assertTrue(entity.getIdType().isPresent());
		assertEquals(EmbeddedEntityId.class, entity.getIdType().get());

		EmbeddedIdEntity i1 = new EmbeddedIdEntity();
		i1.setValue("aaa");

		assertTrue(entity.isNew(i1));

		EmbeddedEntityId id = new EmbeddedEntityId();
		i1.setId(id);

		assertTrue(entity.isNew(i1));

		id.setCode(2L);

		assertFalse(entity.isNew(i1));

		assertTrue(entity.getId(i1).isPresent());
		assertEquals(EmbeddedEntityId.class, entity.getId(i1).get().getClass());
		assertEquals(Long.valueOf(2L), ((EmbeddedEntityId) entity.getId(i1).get()).getCode());

	}

	@Test
	public void testMultiEmbeddedId() {

		JpaEntity<MultiEmbeddedIdEntity> entity = getDatastore().create(EntityMetamodelCommodity.class)
				.resolve(JpaTarget.of(MultiEmbeddedIdEntity.class));

		assertEquals(MultiEmbeddedIdEntity.class, entity.getEntityClass());

		assertTrue(entity.getIdType().isPresent());
		assertEquals(MultiEmbeddedId.class, entity.getIdType().get());

		MultiEmbeddedIdEntity i1 = new MultiEmbeddedIdEntity();
		i1.setValue("bbb");

		assertTrue(entity.isNew(i1));

		MultiEmbeddedId id = new MultiEmbeddedId();
		i1.setId(id);

		assertTrue(entity.isNew(i1));

		id.setPk2("pk2");

		assertTrue(entity.isNew(i1));

		id.setPk1(1L);

		assertFalse(entity.isNew(i1));

		assertTrue(entity.getId(i1).isPresent());
		assertEquals(MultiEmbeddedId.class, entity.getId(i1).get().getClass());

	}

	@Test
	public void testMultiIdClass() {

		JpaEntity<IdClassEntity> entity = getDatastore().create(EntityMetamodelCommodity.class)
				.resolve(JpaTarget.of(IdClassEntity.class));

		assertEquals(IdClassEntity.class, entity.getEntityClass());

		assertTrue(entity.getIdType().isPresent());
		assertEquals(EntityIdClass.class, entity.getIdType().get());

		IdClassEntity i1 = new IdClassEntity();
		i1.setValue("ccc");

		assertTrue(entity.isNew(i1));

		i1.setPk1(3L);

		assertTrue(entity.isNew(i1));

		i1.setPk2("pk2");

		assertFalse(entity.isNew(i1));

		Object id = entity.getId(i1).orElse(null);
		assertNotNull(id);

		assertEquals(EntityIdClass.class, id.getClass());

		final EntityIdClass ic = (EntityIdClass) id;
		assertEquals(Long.valueOf(3L), ic.getPk1());
		assertEquals("pk2", ic.getPk2());
	}

	static final class EntityMetamodelCommodity implements DatastoreCommodity {

		private final JpaOperationContext operationContext;

		public EntityMetamodelCommodity(JpaOperationContext operationContext) {
			super();
			this.operationContext = operationContext;
		}

		@SuppressWarnings("unchecked")
		<T> JpaEntity<T> resolve(DataTarget<?> target) {
			return JPQLResolutionContext.create(operationContext).resolveOrFail(target, JpaEntity.class);
		}

	}

	static final class EntityMetamodelCommodityFactory
			implements JpaDatastoreCommodityFactory<EntityMetamodelCommodity> {

		@Override
		public Class<? extends EntityMetamodelCommodity> getCommodityType() {
			return EntityMetamodelCommodity.class;
		}

		@Override
		public EntityMetamodelCommodity createCommodity(JpaDatastoreCommodityContext context)
				throws CommodityConfigurationException {
			return new EntityMetamodelCommodity(context);
		}

	}

}
