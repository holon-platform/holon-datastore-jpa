/*
 * Copyright 2016-2018 Axioma srl.
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
package com.holonplatform.datastore.jpa.test.suite;

import static com.holonplatform.datastore.jpa.test.model.TestDataModel.DAT;
import static com.holonplatform.datastore.jpa.test.model.TestDataModel.DBL;
import static com.holonplatform.datastore.jpa.test.model.TestDataModel.ENM;
import static com.holonplatform.datastore.jpa.test.model.TestDataModel.KEY;
import static com.holonplatform.datastore.jpa.test.model.TestDataModel.NBOOL;
import static com.holonplatform.datastore.jpa.test.model.TestDataModel.NST_DEC;
import static com.holonplatform.datastore.jpa.test.model.TestDataModel.NST_STR;
import static com.holonplatform.datastore.jpa.test.model.TestDataModel.STR;
import static com.holonplatform.datastore.jpa.test.model.TestDataModel.TMS;
import static com.holonplatform.datastore.jpa.test.suite.AbstractJpaDatastoreTestSuite.JPA_TARGET;
import static com.holonplatform.datastore.jpa.test.suite.AbstractJpaDatastoreTestSuite.LDAT;
import static com.holonplatform.datastore.jpa.test.suite.AbstractJpaDatastoreTestSuite.LTMS;
import static com.holonplatform.datastore.jpa.test.suite.AbstractJpaDatastoreTestSuite.PROPERTIES;
import static com.holonplatform.datastore.jpa.test.suite.AbstractJpaDatastoreTestSuite.TIME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.Test;

import com.holonplatform.core.beans.BeanIntrospector;
import com.holonplatform.core.property.PropertyBox;
import com.holonplatform.core.query.BeanProjection;
import com.holonplatform.core.query.ConstantExpression;
import com.holonplatform.core.query.SelectAllProjection;
import com.holonplatform.datastore.jpa.test.model.TestData;
import com.holonplatform.datastore.jpa.test.model.TestEnum;
import com.holonplatform.datastore.jpa.test.model.TestProjectionBean;
import com.holonplatform.datastore.jpa.test.model.entity.Test1;

public class QueryProjectionTest extends AbstractJpaDatastoreSuiteTest {

	@Test
	public void testPropertySet() {

		PropertyBox result = getDatastore().query().target(JPA_TARGET).filter(KEY.eq(1L)).findOne(PROPERTIES)
				.orElse(null);
		checkKey1Value(result);

		result = getDatastore().query().target(JPA_TARGET).filter(KEY.eq(2L)).findOne(PROPERTIES).orElse(null);
		checkKey2Value(result);

		List<PropertyBox> results = getDatastore().query().target(JPA_TARGET).sort(KEY.asc()).list(PROPERTIES);

		assertNotNull(results);
		assertEquals(2, results.size());
		checkKey1Value(results.get(0));
		checkKey2Value(results.get(1));

	}

	@Test
	public void testProperties() {
		PropertyBox result = getDatastore().query().target(JPA_TARGET).filter(KEY.eq(1L)).findOne(KEY, STR, NBOOL)
				.orElse(null);
		assertEquals(Long.valueOf(1), result.getValue(KEY));
		assertEquals("One", result.getValue(STR));
		assertEquals(Boolean.TRUE, result.getValue(NBOOL));

		assertFalse(result.contains(DBL));

		List<PropertyBox> results = getDatastore().query().target(JPA_TARGET).filter(KEY.eq(1L)).list(KEY, STR, NBOOL);
		assertNotNull(results);
		assertEquals(1, results.size());
		assertEquals(Long.valueOf(1), results.get(0).getValue(KEY));
		assertEquals("One", results.get(0).getValue(STR));
		assertEquals(Boolean.TRUE, results.get(0).getValue(NBOOL));
	}

	@Test
	public void testProperty() {
		Long key = getDatastore().query().target(JPA_TARGET).filter(KEY.eq(1L)).findOne(KEY).orElse(null);
		assertNotNull(key);
		assertEquals(Long.valueOf(1), key);

		String str = getDatastore().query().target(JPA_TARGET).filter(KEY.eq(1L)).findOne(STR).orElse(null);
		assertNotNull(str);
		assertEquals("One", str);

		Double dbl = getDatastore().query().target(JPA_TARGET).filter(KEY.eq(1L)).findOne(DBL).orElse(null);
		assertNotNull(dbl);
		assertEquals(Double.valueOf(7.4), dbl);

		if (AbstractJpaDatastoreTestSuite.enumProjectionTest) {
			TestEnum enm = getDatastore().query().target(JPA_TARGET).filter(KEY.eq(1L)).findOne(ENM).orElse(null);
			assertNotNull(enm);
			assertEquals(TestEnum.FIRST, enm);
		}

		Boolean nb = getDatastore().query().target(JPA_TARGET).filter(KEY.eq(1L)).findOne(NBOOL).orElse(null);
		assertNotNull(nb);
		assertEquals(Boolean.TRUE, nb);

		BigDecimal bd = getDatastore().query().target(JPA_TARGET).filter(KEY.eq(1L)).findOne(NST_DEC).orElse(null);
		assertNotNull(bd);
		assertEquals(BigDecimal.valueOf(12.65), bd.setScale(2, RoundingMode.CEILING));

		Date dat = getDatastore().query().target(JPA_TARGET).filter(KEY.eq(1L)).findOne(DAT).orElse(null);
		assertNotNull(dat);
		Calendar c = Calendar.getInstance();
		c.setTime(dat);
		assertEquals(2016, c.get(Calendar.YEAR));
		assertEquals(4, c.get(Calendar.MONTH));
		assertEquals(19, c.get(Calendar.DAY_OF_MONTH));

		LocalDate ld = getDatastore().query().target(JPA_TARGET).filter(KEY.eq(1L)).findOne(LDAT).orElse(null);
		assertNotNull(ld);
		assertEquals(2016, ld.getYear());
		assertEquals(Month.MAY, ld.getMonth());
		assertEquals(19, ld.getDayOfMonth());

		LocalTime lt = getDatastore().query().target(JPA_TARGET).filter(KEY.eq(1L)).findOne(TIME).orElse(null);
		assertNotNull(lt);
		assertEquals(18, lt.getHour());
		assertEquals(30, lt.getMinute());
		assertEquals(15, lt.getSecond());

		Optional<LocalDateTime> dt = getDatastore().query().target(JPA_TARGET).filter(KEY.eq(1L)).findOne(LTMS);
		assertFalse(dt.isPresent());

		Date tms = getDatastore().query().target(JPA_TARGET).filter(KEY.eq(2L)).findOne(TMS).orElse(null);
		assertNotNull(tms);
		c = Calendar.getInstance();
		c.setTime(tms);
		assertEquals(2017, c.get(Calendar.YEAR));
		assertEquals(2, c.get(Calendar.MONTH));
		assertEquals(23, c.get(Calendar.DAY_OF_MONTH));
		assertEquals(15, c.get(Calendar.HOUR_OF_DAY));
		assertEquals(30, c.get(Calendar.MINUTE));
		assertEquals(25, c.get(Calendar.SECOND));

		LocalDateTime ldt = getDatastore().query().target(JPA_TARGET).filter(KEY.eq(2L)).findOne(LTMS).orElse(null);
		assertNotNull(ldt);
		assertEquals(2017, ldt.getYear());
		assertEquals(Month.MARCH, ldt.getMonth());
		assertEquals(23, ldt.getDayOfMonth());
		assertEquals(15, ldt.getHour());
		assertEquals(30, ldt.getMinute());
		assertEquals(25, ldt.getSecond());

	}

	@Test
	public void testLiteral() {
		Long value = getDatastore().query().target(JPA_TARGET).filter(KEY.eq(1L)).findOne(ConstantExpression.create(1L))
				.orElse(null);
		assertNotNull(value);
		assertEquals(Long.valueOf(1L), value);
	}

	@Test
	public void testConversion() {
		List<Long> keys = getDatastore().query().target(JPA_TARGET).sort(KEY.asc()).stream(PROPERTIES)
				.map((r) -> r.getValue(KEY)).collect(Collectors.toList());
		assertNotNull(keys);
		assertEquals(2, keys.size());
		assertEquals(new Long(1), keys.get(0));
		assertEquals(new Long(2), keys.get(1));
	}

	@Test
	public void testCount() {
		long count = getDatastore().query().target(JPA_TARGET).count();
		assertEquals(2, count);

		count = getDatastore().query().target(JPA_TARGET).filter(KEY.eq(1L)).count();
		assertEquals(1, count);
	}

	@Test
	public void testBeanConversion() {
		List<TestData> results = getDatastore().query().target(JPA_TARGET).sort(KEY.asc()).stream(PROPERTIES)
				.map(r -> BeanIntrospector.get().write(r, new Test1())).collect(Collectors.toList());
		assertNotNull(results);
		assertEquals(2, results.size());

		TestData first = results.get(0);
		assertEquals(Long.valueOf(1L), first.getKey());
	}

	@Test
	public void testProjectionBean() {
		List<TestProjectionBean> results = getDatastore().query().target(JPA_TARGET).sort(KEY.asc())
				.list(BeanProjection.of(TestProjectionBean.class));
		assertNotNull(results);
		assertEquals(2, results.size());

		assertEquals(1L, results.get(0).getKey());
		assertEquals("One", results.get(0).getStringValue());

		assertEquals(2L, results.get(1).getKey());
		assertEquals("Two", results.get(1).getStringValue());
	}

	@Test
	public void testPropertyConversion() {
		List<Boolean> values = getDatastore().query().target(JPA_TARGET).sort(KEY.asc()).list(NBOOL);
		assertNotNull(values);
		assertEquals(2, values.size());
		assertEquals(Boolean.TRUE, values.get(0));
		assertEquals(Boolean.FALSE, values.get(1));
	}

	@Test
	public void testSelectAll() {
		Map<String, Object> result = getDatastore().query().target(JPA_TARGET).filter(KEY.eq(1L))
				.findOne(SelectAllProjection.create()).orElse(null);

		assertNotNull(result);
		assertTrue(result.containsKey("key"));
		assertNotNull(result.get("key"));
	}

	private static void checkKey1Value(PropertyBox value) {
		assertNotNull(value);
		assertEquals(Long.valueOf(1), value.getValue(KEY));
		assertEquals("One", value.getValue(STR));
		assertEquals(Double.valueOf(7.4), value.getValue(DBL));
		assertEquals(TestEnum.FIRST, value.getValue(ENM));
		assertEquals(Boolean.TRUE, value.getValue(NBOOL));
		assertEquals("n1", value.getValue(NST_STR));
		assertEquals(BigDecimal.valueOf(12.65), value.getValue(NST_DEC).setScale(2, RoundingMode.CEILING));

		Calendar c = Calendar.getInstance();
		c.setTime(value.getValue(DAT));
		assertEquals(2016, c.get(Calendar.YEAR));
		assertEquals(4, c.get(Calendar.MONTH));
		assertEquals(19, c.get(Calendar.DAY_OF_MONTH));

		LocalDate ld = value.getValue(LDAT);
		assertNotNull(ld);
		assertEquals(2016, ld.getYear());
		assertEquals(Month.MAY, ld.getMonth());
		assertEquals(19, ld.getDayOfMonth());

		LocalTime tm = value.getValue(TIME);
		assertNotNull(tm);
		assertEquals(18, tm.getHour());
		assertEquals(30, tm.getMinute());
		assertEquals(15, tm.getSecond());
	}

	private static void checkKey2Value(PropertyBox value) {
		assertNotNull(value);
		assertEquals(Long.valueOf(2), value.getValue(KEY));
		assertEquals("Two", value.getValue(STR));
		assertNull(value.getValue(DBL));
		assertEquals(TestEnum.SECOND, value.getValue(ENM));
		assertEquals(Boolean.FALSE, value.getValue(NBOOL));
		assertEquals("n2", value.getValue(NST_STR));
		assertEquals(BigDecimal.valueOf(3), value.getValue(NST_DEC).setScale(0, RoundingMode.UNNECESSARY));

		Calendar c = Calendar.getInstance();
		c.setTime(value.getValue(DAT));
		assertEquals(2016, c.get(Calendar.YEAR));
		assertEquals(3, c.get(Calendar.MONTH));
		assertEquals(19, c.get(Calendar.DAY_OF_MONTH));

		LocalDate ld = value.getValue(LDAT);
		assertNotNull(ld);
		assertEquals(2016, ld.getYear());
		assertEquals(Month.APRIL, ld.getMonth());
		assertEquals(19, ld.getDayOfMonth());

		assertNull(value.getValue(TIME));

		c = Calendar.getInstance();
		c.setTime(value.getValue(TMS));
		assertEquals(2017, c.get(Calendar.YEAR));
		assertEquals(2, c.get(Calendar.MONTH));
		assertEquals(23, c.get(Calendar.DAY_OF_MONTH));
		assertEquals(15, c.get(Calendar.HOUR_OF_DAY));
		assertEquals(30, c.get(Calendar.MINUTE));
		assertEquals(25, c.get(Calendar.SECOND));

		LocalDateTime ldt = value.getValue(LTMS);
		assertNotNull(ldt);
		assertEquals(2017, ldt.getYear());
		assertEquals(Month.MARCH, ldt.getMonth());
		assertEquals(23, ldt.getDayOfMonth());
		assertEquals(15, ldt.getHour());
		assertEquals(30, ldt.getMinute());
		assertEquals(25, ldt.getSecond());
	}

}
