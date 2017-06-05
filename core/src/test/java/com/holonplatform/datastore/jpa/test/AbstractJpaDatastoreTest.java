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
package com.holonplatform.datastore.jpa.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.time.DateUtils;
import org.junit.Test;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import com.holonplatform.core.beans.BeanIntrospector;
import com.holonplatform.core.datastore.DataTarget;
import com.holonplatform.core.datastore.Datastore;
import com.holonplatform.core.datastore.Datastore.OperationResult;
import com.holonplatform.core.datastore.DefaultWriteOption;
import com.holonplatform.core.datastore.bulk.BulkDelete;
import com.holonplatform.core.datastore.bulk.BulkUpdate;
import com.holonplatform.core.datastore.relational.SubQuery;
import com.holonplatform.core.internal.query.filter.NotFilter;
import com.holonplatform.core.property.PathProperty;
import com.holonplatform.core.property.Property;
import com.holonplatform.core.property.PropertyBox;
import com.holonplatform.core.property.PropertySet;
import com.holonplatform.core.property.PropertyValueConverter;
import com.holonplatform.core.query.BeanProjection;
import com.holonplatform.core.query.QueryAggregation;
import com.holonplatform.core.query.QueryFilter;
import com.holonplatform.datastore.jpa.JpaQueryHint;
import com.holonplatform.datastore.jpa.JpaTarget;
import com.holonplatform.datastore.jpa.JpaWriteOption;
import com.holonplatform.datastore.jpa.test.data.KeyIs;
import com.holonplatform.datastore.jpa.test.data.TestData;
import com.holonplatform.datastore.jpa.test.data.TestProjectionBean;
import com.holonplatform.datastore.jpa.test.domain.TestEnum;
import com.holonplatform.datastore.jpa.test.domain.TestGeneratedId;
import com.holonplatform.datastore.jpa.test.domain.TestJpaDomain;
import com.holonplatform.datastore.jpa.test.domain.TestJpaDomainBis;
import com.holonplatform.datastore.jpa.test.domain.TestNested;
import com.holonplatform.datastore.jpa.test.domain.TestOtherDomain;

public abstract class AbstractJpaDatastoreTest {

	protected final static DataTarget<String> NAMED_TARGET = DataTarget.named(TestJpaDomain.class.getName());
	protected final static JpaTarget<TestJpaDomain> ENTITY_TARGET = JpaTarget.of(TestJpaDomain.class);
	protected final static DataTarget<TestJpaDomainBis> ENTITY_TARGET_BIS = JpaTarget.of(TestJpaDomainBis.class);

	protected final static PathProperty<Long> KEY = PathProperty.create("key", long.class);
	protected final static PathProperty<String> STR = PathProperty.create("stringValue", String.class);
	protected final static PathProperty<Double> DBL = PathProperty.create("decimalValue", Double.class);
	protected final static PathProperty<Date> DAT = PathProperty.create("dateValue", Date.class);
	protected final static PathProperty<TestEnum> ENM = PathProperty.create("enumValue", TestEnum.class);
	protected final static PathProperty<TestNested> NST = PathProperty.create("nested", TestNested.class);
	protected final static PathProperty<String> NST_STR = PathProperty.create("nested.nestedStringValue", String.class);
	protected final static PathProperty<BigDecimal> NST_DEC = PathProperty.create("nested.nestedDecimalValue",
			BigDecimal.class);

	@SuppressWarnings("serial")
	protected final static PathProperty<Boolean> NBOOL = PathProperty.create("numericBooleanValue", boolean.class)
			.converter(new PropertyValueConverter<Boolean, Integer>() {

				@Override
				public Boolean fromModel(Integer value, Property<Boolean> property) throws PropertyConversionException {
					return (value != null && value.intValue() > 0) ? Boolean.TRUE : Boolean.FALSE;
				}

				@Override
				public Integer toModel(Boolean value, Property<Boolean> property) throws PropertyConversionException {
					return (value != null && value.booleanValue()) ? 1 : 0;
				}

				@Override
				public Class<Boolean> getPropertyType() {
					return Boolean.class;
				}

				@Override
				public Class<Integer> getModelType() {
					return int.class;
				}
			});

	protected final static PropertySet<?> PROPS = PropertySet.of(KEY, STR, DBL, DAT, ENM, NBOOL, NST, NST_STR, NST_DEC);

	protected abstract Datastore getDatastore();

	@Test
	@Transactional
	public void testDML() {
		getDatastore().save(ENTITY_TARGET, PropertyBox.builder(KEY, STR).set(KEY, 21L).set(STR, "Test save").build());

		Optional<PropertyBox> found = getDatastore().query().target(NAMED_TARGET).filter(KEY.eq(21L)).findOne(PROPS);
		assertTrue(found.isPresent());
		assertEquals(new Long(21), found.get().getValue(KEY));
	}

	@Test
	@Transactional
	public void testDatastorePropertyBox() {

		final Date now = DateUtils.truncate(new Date(), Calendar.DATE);
		final BigDecimal bd = new BigDecimal(7);

		PropertyBox box = PropertyBox.builder(PROPS).set(KEY, 22L).set(STR, "Test22").set(DBL, 2.3).set(DAT, now)
				.set(ENM, TestEnum.THIRD).set(NBOOL, Boolean.TRUE).set(NST_STR, "NestedStr").set(NST_DEC, bd).build();

		getDatastore().save(ENTITY_TARGET, box);

		Optional<PropertyBox> fnd = getDatastore().query().target(NAMED_TARGET).filter(KEY.eq(22L)).findOne(PROPS);
		assertTrue(fnd.isPresent());

		PropertyBox found = fnd.get();

		assertEquals(new Long(22), found.getValue(KEY));
		assertEquals("Test22", found.getValue(STR));
		assertEquals(new Double(2.3), found.getValue(DBL));
		assertEquals(now, found.getValue(DAT));
		assertEquals(TestEnum.THIRD, found.getValue(ENM));
		assertEquals("NestedStr", found.getValue(NST_STR));
		assertEquals((Double) bd.doubleValue(), (Double) found.getValue(NST_DEC).doubleValue());

		getDatastore().delete(NAMED_TARGET, found);

		Optional<String> str = getDatastore().query().target(NAMED_TARGET).filter(KEY.eq(22L)).findOne(STR);
		assertFalse(str.isPresent());

	}

	@Test
	public void testQueryResults() {
		List<PropertyBox> results = getDatastore().query().target(ENTITY_TARGET).filter(KEY.eq(-100L)).list(PROPS);
		assertNotNull(results);
		assertEquals(0, results.size());
	}

	@Test
	public void testFindByKey() {
		Optional<PropertyBox> pb = getDatastore().query().target(ENTITY_TARGET).filter(KEY.eq(1L)).findOne(PROPS);
		assertTrue(pb.isPresent());
		assertEquals("One", pb.get().getValue(STR));
	}

	@Test
	public void testCount() {
		long count = getDatastore().query().target(ENTITY_TARGET).count();
		assertEquals(2, count);
	}

	@Test
	public void testDataTarget() {
		List<TestData> results = getDatastore().query().target(ENTITY_TARGET).sort(KEY.asc()).stream(PROPS)
				.map(r -> BeanIntrospector.get().write(r, new TestJpaDomain())).collect(Collectors.toList());
		assertNotNull(results);
		assertEquals(2, results.size());

		TestData first = results.get(0);
		assertEquals(Long.valueOf(1), first.getKey());
	}

	@Test
	public void testQueryProjection() {

		List<PropertyBox> results = getDatastore().query().target(ENTITY_TARGET).list(PROPS);
		assertNotNull(results);
		assertEquals(2, results.size());

		List<String> values = getDatastore().query().target(ENTITY_TARGET)
				.list(PathProperty.create("stringValue", String.class));
		assertNotNull(values);
		assertEquals(2, values.size());

		values = getDatastore().query().target(ENTITY_TARGET).list(STR);
		assertNotNull(values);
		assertEquals(2, values.size());

		Optional<Long> count = getDatastore().query().target(ENTITY_TARGET).findOne(STR.count());
		assertEquals(new Long(2), count.get());

		// results converter

		List<Long> keys = getDatastore().query().target(ENTITY_TARGET).sort(KEY.asc()).stream(PROPS)
				.map((r) -> r.getValue(KEY)).collect(Collectors.toList());
		assertNotNull(keys);
		assertEquals(2, keys.size());
		assertEquals(new Long(1), keys.get(0));
		assertEquals(new Long(2), keys.get(1));
	}

	@Test
	public void testQueryAggregateProjection() {

		Optional<Long> key = getDatastore().query().target(ENTITY_TARGET).findOne(KEY.max());
		assertTrue(key.isPresent());
		assertEquals(new Long(2), key.get());

		key = getDatastore().query().target(ENTITY_TARGET).findOne(KEY.min());
		assertTrue(key.isPresent());
		assertEquals(new Long(1), key.get());

		Optional<Long> sum = getDatastore().query().target(NAMED_TARGET).findOne(KEY.sum());
		assertTrue(sum.isPresent());
		assertEquals(new Long(3), sum.get());

		Optional<Long> count = getDatastore().query().target(NAMED_TARGET).findOne(KEY.count());
		assertEquals(new Long(2), count.get());
	}

	@Test
	public void testAvg() {
		Optional<Double> avg = getDatastore().query().target(ENTITY_TARGET).findOne(KEY.avg());
		assertTrue(avg.isPresent());
		assertEquals(new Double(1.5), avg.get());
	}

	@Test
	public void testPropertyConversion() {
		List<Boolean> values = getDatastore().query().target(ENTITY_TARGET).list(NBOOL);
		assertNotNull(values);
		Boolean value = values.get(0);
		assertNotNull(value);

		long cnt = getDatastore().query().target(ENTITY_TARGET).filter(NBOOL.eq(Boolean.TRUE)).count();
		assertEquals(1, cnt);
	}

	@Test
	public void testMultiSelect() {

		List<PropertyBox> results = getDatastore().query().target(ENTITY_TARGET).sort(KEY.asc()).list(KEY, STR);

		assertNotNull(results);
		assertEquals(2, results.size());

		PropertyBox box = results.get(0);

		assertNotNull(box);

		Long key = box.getValue(KEY);

		assertNotNull(key);
		assertEquals(new Long(1), key);

		String str = box.getValue(STR);
		assertNotNull(str);
		assertEquals("One", str);
	}

	@Test
	public void testRestrictions() {
		List<String> str = getDatastore().query().target(ENTITY_TARGET).restrict(1, 0).sort(KEY.asc()).list(STR);
		assertEquals(1, str.size());
		assertEquals("One", str.get(0));

		str = getDatastore().query().target(ENTITY_TARGET).restrict(1, 1).sort(KEY.asc())
				.parameter(JpaQueryHint.QUERY_PARAMETER_HINT, JpaQueryHint.create("test", "tv")).list(STR);
		assertEquals(1, str.size());
		assertEquals("Two", str.get(0));
	}

	@Test
	public void testSorts() {
		List<Long> res = getDatastore().query().target(ENTITY_TARGET).sort(STR.desc()).sort(KEY.desc()).list(KEY);
		assertEquals(new Long(2), res.get(0));
	}

	@Test
	public void testFilters() {

		long count = getDatastore().query().target(ENTITY_TARGET).filter(STR.eq("One")).count();
		assertEquals(1, count);

		count = getDatastore().query().target(ENTITY_TARGET).filter(new NotFilter(STR.eq("One"))).count();
		assertEquals(1, count);

		count = getDatastore().query().target(ENTITY_TARGET).filter(STR.eq("One").not()).count();
		assertEquals(1, count);

		count = getDatastore().query().target(ENTITY_TARGET).filter(STR.neq("Two")).count();
		assertEquals(1, count);

		count = getDatastore().query().target(ENTITY_TARGET).filter(STR.isNotNull()).count();
		assertEquals(2, count);

		count = getDatastore().query().target(ENTITY_TARGET).filter(DBL.isNull()).count();
		assertEquals(1, count);

		count = getDatastore().query().target(ENTITY_TARGET).filter(STR.contains("x")).count();
		assertEquals(0, count);

		count = getDatastore().query().target(ENTITY_TARGET).filter(STR.contains("N")).count();
		assertEquals(0, count);

		count = getDatastore().query().target(ENTITY_TARGET).filter(STR.containsIgnoreCase("O")).count();
		assertEquals(2, count);

		count = getDatastore().query().target(ENTITY_TARGET).filter(DBL.gt(7d)).count();
		assertEquals(1, count);

		count = getDatastore().query().target(ENTITY_TARGET).filter(DBL.lt(8d)).count();
		assertEquals(1, count);

		count = getDatastore().query().target(ENTITY_TARGET).filter(NST_DEC.goe(new BigDecimal(3))).count();
		assertEquals(2, count);

		count = getDatastore().query().target(ENTITY_TARGET).filter(NST_DEC.loe(new BigDecimal(3))).count();
		assertEquals(1, count);

		count = getDatastore().query().target(ENTITY_TARGET).filter(KEY.between(1L, 2L)).count();
		assertEquals(2, count);

		count = getDatastore().query().target(ENTITY_TARGET).filter(KEY.in(1L, 2L)).count();
		assertEquals(2, count);

		count = getDatastore().query().target(ENTITY_TARGET).filter(KEY.nin(1L, 2L)).count();
		assertEquals(0, count);

		count = getDatastore().query().target(ENTITY_TARGET).filter(KEY.eq(1L).or(KEY.eq(2L))).count();
		assertEquals(2, count);

		count = getDatastore().query().target(ENTITY_TARGET).filter(KEY.eq(1L).and(STR.eq("One"))).count();
		assertEquals(1, count);

	}

	@Test
	public void testSubQuery() {

		final PathProperty<String> OTHER_CODE = PathProperty.create("code", String.class);
		final PathProperty<Long> OTHER_SEQ = PathProperty.create("sequence", long.class);

		long count = getDatastore().query()
				.target(ENTITY_TARGET).filter(KEY.in(SubQuery.create(getDatastore(), Long.class)
						.target(JpaTarget.of(TestOtherDomain.class)).filter(OTHER_CODE.eq("CODE1")).select(OTHER_SEQ)))
				.count();
		assertEquals(1, count);

		count = getDatastore().query().target(ENTITY_TARGET).filter(KEY.nin(SubQuery.create(getDatastore(), OTHER_SEQ)
				.target(JpaTarget.of(TestOtherDomain.class)).filter(OTHER_CODE.eq("CODE1")))).count();
		assertEquals(1, count);

		final PathProperty<Long> TARGETED_KEY = PathProperty.create("key", long.class).parent(ENTITY_TARGET);

		count = getDatastore().query().target(ENTITY_TARGET)
				.filter(SubQuery.create(getDatastore()).target(JpaTarget.of(TestOtherDomain.class))
						.filter(OTHER_CODE.eq("CODE1").and(OTHER_SEQ.eq(TARGETED_KEY))).exists())
				.count();
		assertEquals(1, count);

		count = getDatastore().query().target(ENTITY_TARGET)
				.filter(SubQuery.create(getDatastore()).target(JpaTarget.of(TestOtherDomain.class))
						.filter(OTHER_CODE.eq("CODE1").and(OTHER_SEQ.eq(TARGETED_KEY))).notExists())
				.count();
		assertEquals(1, count);

		count = getDatastore().query().target(ENTITY_TARGET)
				.filter(SubQuery.create(getDatastore()).target(JpaTarget.of(TestOtherDomain.class))
						.filter(OTHER_CODE.eq("CODE1").and(OTHER_SEQ.eq(ENTITY_TARGET.property("key", long.class))))
						.notExists())
				.count();
		assertEquals(1, count);
	}

	@Test
	public void testAggregation() {

		List<Double> ds = getDatastore().query().target(ENTITY_TARGET_BIS).aggregate(DBL).list(DBL);
		assertNotNull(ds);
		assertEquals(3, ds.size());

		List<PropertyBox> results = getDatastore().query().target(ENTITY_TARGET_BIS).aggregate(DBL).list(KEY.max(),
				DBL);
		assertNotNull(results);
		assertEquals(3, results.size());

		results = getDatastore().query().target(ENTITY_TARGET_BIS).filter(NST_DEC.lt(BigDecimal.valueOf(10)))
				.aggregate(DBL).list(KEY.max(), DBL);
		assertNotNull(results);
		assertEquals(2, results.size());

		results = getDatastore().query().target(ENTITY_TARGET_BIS)
				.filter(NST_DEC.lt(BigDecimal.valueOf(10)).and(ENM.eq(TestEnum.FIRST))).sort(DBL.asc()).aggregate(DBL)
				.list(KEY.max(), DBL);
		assertNotNull(results);
		assertEquals(1, results.size());

		results = getDatastore().query().target(ENTITY_TARGET_BIS)
				.aggregate(QueryAggregation.builder().path(DBL).filter(QueryFilter.gt(DBL.count(), 1L)).build())
				.list(KEY.max(), DBL);
		assertNotNull(results);
		assertEquals(1, results.size());

	}

	@Test
	@Transactional
	@Rollback
	public void testBulk() {
		BulkUpdate upd = getDatastore().bulkUpdate(ENTITY_TARGET);
		upd.set(ENM, TestEnum.THIRD);
		upd.filter(KEY.loe(1L));

		upd.withExpressionResolver(KeyIs.RESOLVER);

		OperationResult result = upd.execute();

		assertEquals(1, result.getAffectedCount());

		upd = getDatastore().bulkUpdate(ENTITY_TARGET);
		upd.set(ENM, TestEnum.THIRD);
		upd.filter(KEY.eq(1L));
		result = upd.execute();

		assertEquals(1, result.getAffectedCount());

		BulkDelete del = getDatastore().bulkDelete(ENTITY_TARGET).filter(KEY.goe(10L));
		del.withExpressionResolver(KeyIs.RESOLVER);
		result = del.execute();

		assertEquals(0, result.getAffectedCount());

		getDatastore().save(ENTITY_TARGET, PropertyBox.builder(KEY, STR).set(KEY, 99L).set(STR, "Test bulk").build());

		del = getDatastore().bulkDelete(NAMED_TARGET).filter(KEY.gt(98L));
		result = del.execute();

		assertEquals(1, result.getAffectedCount());

	}

	@Test
	@Transactional
	@Rollback
	public void testBulkInsert() {
		OperationResult result = getDatastore().bulkInsert(ENTITY_TARGET, PropertySet.of(KEY, STR))
				.add(PropertyBox.builder(KEY, STR).set(KEY, 201L).set(STR, "Test bulk 201").build())
				.add(PropertyBox.builder(KEY, STR).set(KEY, 202L).set(STR, "Test bulk 202").build())
				.add(PropertyBox.builder(KEY, STR).set(KEY, 203L).set(STR, "Test bulk 203").build())
				.add(PropertyBox.builder(KEY, STR).set(KEY, 204L).set(STR, "Test bulk 204").build())
				.add(PropertyBox.builder(KEY, STR).set(KEY, 205L).set(STR, "Test bulk 205").build())
				.add(PropertyBox.builder(KEY, STR).set(KEY, 206L).set(STR, "Test bulk 206").build())
				.add(PropertyBox.builder(KEY, STR).set(KEY, 207L).set(STR, "Test bulk 207").build())
				.add(PropertyBox.builder(KEY, STR).set(KEY, 208L).set(STR, "Test bulk 208").build())
				.add(PropertyBox.builder(KEY, STR).set(KEY, 209L).set(STR, "Test bulk 209").build())
				.add(PropertyBox.builder(KEY, STR).set(KEY, 210L).set(STR, "Test bulk 210").build()).execute();
		assertEquals(10, result.getAffectedCount());
	}

	@Test
	public void testJpaProjection() {
		List<TestJpaDomain> results = getDatastore().query().target(ENTITY_TARGET).filter(KEY.gt(1L)).sort(KEY.asc())
				.list(ENTITY_TARGET);
		assertNotNull(results);
		assertEquals(1, results.size());

		TestJpaDomain first = results.get(0);
		assertEquals(Long.valueOf(2), first.getKey());
	}

	@Test
	public void testProjectionBean() {
		List<TestProjectionBean> results = getDatastore().query().target(ENTITY_TARGET).sort(KEY.asc())
				.list(BeanProjection.of(TestProjectionBean.class));
		assertNotNull(results);
		assertEquals(2, results.size());

		assertEquals(1L, results.get(0).getKey());
		assertEquals("One", results.get(0).getStringValue());

		assertEquals(2L, results.get(1).getKey());
		assertEquals("Two", results.get(1).getStringValue());

		// Jpa entity bean

		List<TestJpaDomain> eresults = getDatastore().query().target(ENTITY_TARGET).sort(KEY.asc()).list(ENTITY_TARGET);
		assertNotNull(eresults);
		assertEquals(2, eresults.size());

		assertEquals(Long.valueOf(1), eresults.get(0).getKey());
		assertEquals("One", eresults.get(0).getStringValue());

		assertEquals(Long.valueOf(2), eresults.get(1).getKey());
		assertEquals("Two", eresults.get(1).getStringValue());

		// as normal bean

		eresults = getDatastore().query().target(ENTITY_TARGET).sort(KEY.asc())
				.list(BeanProjection.of(TestJpaDomain.class));
		assertNotNull(eresults);
		assertEquals(2, eresults.size());

		assertEquals(Long.valueOf(1), eresults.get(0).getKey());
		assertEquals("One", eresults.get(0).getStringValue());

		assertEquals(Long.valueOf(2), eresults.get(1).getKey());
		assertEquals("Two", eresults.get(1).getStringValue());
	}

	@Test
	@Transactional
	@Rollback
	public void testCustomFilter() {

		long count = getDatastore().query().target(ENTITY_TARGET).filter(new KeyIs(1)).count();
		assertEquals(1, count);

		Optional<String> str = getDatastore().query().target(NAMED_TARGET).filter(new KeyIs(1)).findOne(STR);
		assertEquals("One", str.get());

		OperationResult result = getDatastore().bulkUpdate(ENTITY_TARGET).set(ENM, TestEnum.THIRD).filter(new KeyIs(1))
				.execute();
		assertEquals(1, result.getAffectedCount());

		Optional<PropertyBox> pb = getDatastore().query().target(ENTITY_TARGET).filter(new KeyIs(1)).findOne(PROPS);
		assertEquals(TestEnum.THIRD, pb.get().getValue(ENM));

		result = getDatastore().bulkUpdate(ENTITY_TARGET).filter(new KeyIs(2)).setNull(DAT).execute();
		assertEquals(1, result.getAffectedCount());

		pb = getDatastore().query().target(ENTITY_TARGET).filter(new KeyIs(2)).findOne(PROPS);
		assertFalse(pb.get().containsValue(DAT));
	}

	@Test
	@Transactional
	@Rollback
	public void testInsertIds() {

		OperationResult result = getDatastore().insert(ENTITY_TARGET,
				PropertyBox.builder(PROPS).set(KEY, 77L).set(STR, "Test insert ids").build());
		assertEquals(1, result.getAffectedCount());
		assertEquals(1, result.getInsertedKeys().size());
		assertEquals("key", result.getInsertedKeys().keySet().iterator().next().getName());
		assertEquals(Long.valueOf(77), result.getInsertedKeys().values().iterator().next());

		result = getDatastore().delete(ENTITY_TARGET,
				getDatastore().query().target(ENTITY_TARGET).filter(KEY.eq(77L)).findOne(PROPS).orElse(null));
		assertEquals(1, result.getAffectedCount());

		result = getDatastore().save(ENTITY_TARGET,
				PropertyBox.builder(PROPS).set(KEY, 78L).set(STR, "Test insert ids").build());
		assertEquals(1, result.getAffectedCount());
		assertEquals(1, result.getInsertedKeys().size());
		assertEquals("key", result.getInsertedKeys().keySet().iterator().next().getName());
		assertEquals(Long.valueOf(78), result.getInsertedKeys().values().iterator().next());

		result = getDatastore().delete(ENTITY_TARGET,
				getDatastore().query().target(ENTITY_TARGET).filter(KEY.eq(78L)).findOne(PROPS).orElse(null));
		assertEquals(1, result.getAffectedCount());
	}

	@Test
	@Transactional
	@Rollback
	public void testGeneratedId() {
		final PathProperty<Long> CODE = PathProperty.create("code", Long.class);
		final PathProperty<String> VALUE = PathProperty.create("value", String.class);

		final DataTarget<TestGeneratedId> TARGET = JpaTarget.of(TestGeneratedId.class);

		OperationResult result = getDatastore().insert(TARGET,
				PropertyBox.builder(CODE, VALUE).set(VALUE, "Test value").build(), JpaWriteOption.FLUSH);
		assertEquals(1, result.getAffectedCount());
		assertEquals(1, result.getInsertedKeys().size());

		assertEquals(Long.valueOf(1), result.getInsertedKeys().values().iterator().next());

		// bring back ids
		PropertyBox box = PropertyBox.builder(CODE, VALUE).set(VALUE, "Test v2").build();
		result = getDatastore().insert(TARGET, box, JpaWriteOption.FLUSH, DefaultWriteOption.BRING_BACK_GENERATED_IDS);
		assertEquals(1, result.getAffectedCount());
		assertEquals(1, result.getInsertedKeys().size());
		assertEquals(Long.valueOf(2), box.getValue(CODE));
	}

}
