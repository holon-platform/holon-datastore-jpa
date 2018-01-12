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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import com.holonplatform.core.datastore.Datastore;
import com.holonplatform.core.datastore.Datastore.OperationResult;
import com.holonplatform.core.property.NumericProperty;
import com.holonplatform.core.property.PropertySet;
import com.holonplatform.core.property.TemporalProperty;
import com.holonplatform.core.query.QueryFunction;
import com.holonplatform.core.temporal.TemporalType;
import com.holonplatform.datastore.jpa.JpaTarget;
import com.holonplatform.datastore.jpa.test.domain.TestTemporal;

public abstract class AbstractTemporalsTest {

	protected final static JpaTarget<TestTemporal> TARGET = JpaTarget.of(TestTemporal.class);

	protected final static NumericProperty<Long> ID = NumericProperty.create("id", long.class);
	protected final static TemporalProperty<Date> DAT = TemporalProperty.date("dateValue")
			.temporalType(TemporalType.DATE);
	protected final static TemporalProperty<Date> TMS = TemporalProperty.date("timestampValue")
			.temporalType(TemporalType.DATE_TIME);
	protected final static TemporalProperty<LocalDate> LDAT = TemporalProperty.localDate("localDateValue");
	protected final static TemporalProperty<LocalDateTime> LTMS = TemporalProperty.localDateTime("localDateTimeValue");

	protected final static PropertySet<?> PROPS = PropertySet.of(ID, DAT, TMS, LDAT, LTMS);

	protected abstract Datastore getDatastore();

	@Test
	@Transactional
	@Rollback
	public void testCurrentDate() {

		final Calendar now = Calendar.getInstance();

		List<Date> dates = getDatastore().query().target(TARGET).list(QueryFunction.currentDate());
		assertTrue(dates.size() > 0);
		Date date = dates.get(0);

		Calendar dc = Calendar.getInstance();
		dc.setTime(date);

		assertEquals(now.get(Calendar.YEAR), dc.get(Calendar.YEAR));
		assertEquals(now.get(Calendar.MONTH), dc.get(Calendar.MONTH));
		assertEquals(now.get(Calendar.DAY_OF_MONTH), dc.get(Calendar.DAY_OF_MONTH));

		long cnt = getDatastore().query().target(TARGET).filter(DAT.lt(QueryFunction.currentDate())).count();
		assertEquals(2L, cnt);

		OperationResult result = getDatastore().bulkUpdate(TARGET).set(DAT, QueryFunction.currentDate())
				.filter(ID.eq(1L)).execute();
		assertEquals(1, result.getAffectedCount());

		date = getDatastore().query().target(TARGET).filter(ID.eq(1L)).findOne(DAT).orElse(null);
		assertNotNull(date);

		dc = Calendar.getInstance();
		dc.setTime(date);

		assertEquals(now.get(Calendar.YEAR), dc.get(Calendar.YEAR));
		assertEquals(now.get(Calendar.MONTH), dc.get(Calendar.MONTH));
		assertEquals(now.get(Calendar.DAY_OF_MONTH), dc.get(Calendar.DAY_OF_MONTH));

		// LocalDate

		result = getDatastore().bulkUpdate(TARGET).set(LDAT, QueryFunction.currentLocalDate()).filter(ID.eq(1L))
				.execute();
		assertEquals(1, result.getAffectedCount());

		cnt = getDatastore().query().target(TARGET).filter(LDAT.loe(QueryFunction.currentLocalDate())).count();
		assertEquals(2L, cnt);
	}

	@Test
	@Transactional
	@Rollback
	public void testCurrentTimestamp() {

		final Calendar now = Calendar.getInstance();

		List<Date> dates = getDatastore().query().target(TARGET).list(QueryFunction.currentTimestamp());
		assertTrue(dates.size() > 0);
		Date date = dates.get(0);

		Calendar dc = Calendar.getInstance();
		dc.setTime(date);

		assertEquals(now.get(Calendar.YEAR), dc.get(Calendar.YEAR));
		assertEquals(now.get(Calendar.MONTH), dc.get(Calendar.MONTH));
		assertEquals(now.get(Calendar.DAY_OF_MONTH), dc.get(Calendar.DAY_OF_MONTH));

		long cnt = getDatastore().query().target(TARGET)
				.filter(TMS.isNotNull().and(TMS.lt(QueryFunction.currentTimestamp()))).count();
		assertEquals(2L, cnt);

		OperationResult result = getDatastore().bulkUpdate(TARGET).set(TMS, QueryFunction.currentTimestamp())
				.filter(ID.eq(2L)).execute();
		assertEquals(1, result.getAffectedCount());

		date = getDatastore().query().target(TARGET).filter(ID.eq(2L)).findOne(TMS).orElse(null);
		assertNotNull(date);

		dc = Calendar.getInstance();
		dc.setTime(date);

		assertEquals(now.get(Calendar.YEAR), dc.get(Calendar.YEAR));
		assertEquals(now.get(Calendar.MONTH), dc.get(Calendar.MONTH));
		assertEquals(now.get(Calendar.DAY_OF_MONTH), dc.get(Calendar.DAY_OF_MONTH));

		// LocalDateTime

		LocalDateTime lnow = LocalDateTime.now().withSecond(0).withNano(0);

		result = getDatastore().bulkUpdate(TARGET).set(LTMS, QueryFunction.currentLocalDateTime()).filter(ID.eq(1L))
				.execute();
		assertEquals(1, result.getAffectedCount());

		LocalDateTime ldate = getDatastore().query().target(TARGET).filter(ID.eq(1L)).findOne(LTMS).orElse(null);
		assertNotNull(ldate);

		ldate = ldate.withSecond(0).withNano(0);

		assertEquals(lnow.toLocalDate(), ldate.toLocalDate());

		cnt = getDatastore().query().target(TARGET).filter(LTMS.loe(QueryFunction.currentLocalDateTime())).count();
		assertEquals(2L, cnt);

	}

	@Test
	public void testTemporalFunctions() {
		Integer value = getDatastore().query().target(TARGET).filter(ID.eq(1L)).findOne(DAT.year()).orElse(null);
		assertNotNull(value);
		assertEquals(Integer.valueOf(2016), value);

		value = getDatastore().query().target(TARGET).filter(ID.eq(1L)).findOne(LDAT.year()).orElse(null);
		assertNotNull(value);
		assertEquals(Integer.valueOf(2016), value);

		value = getDatastore().query().target(TARGET).filter(ID.eq(1L)).findOne(DAT.month()).orElse(null);
		assertNotNull(value);
		assertEquals(Integer.valueOf(5), value);

		value = getDatastore().query().target(TARGET).filter(ID.eq(1L)).findOne(LDAT.month()).orElse(null);
		assertNotNull(value);
		assertEquals(Integer.valueOf(5), value);

		value = getDatastore().query().target(TARGET).filter(ID.eq(1L)).findOne(DAT.day()).orElse(null);
		assertNotNull(value);
		assertEquals(Integer.valueOf(19), value);

		value = getDatastore().query().target(TARGET).filter(ID.eq(1L)).findOne(LDAT.day()).orElse(null);
		assertNotNull(value);
		assertEquals(Integer.valueOf(19), value);

		value = getDatastore().query().target(TARGET).filter(ID.eq(2L)).findOne(TMS.hour()).orElse(null);
		assertNotNull(value);
		assertEquals(Integer.valueOf(15), value);

		value = getDatastore().query().target(TARGET).filter(ID.eq(2L)).findOne(LTMS.hour()).orElse(null);
		assertNotNull(value);
		assertEquals(Integer.valueOf(15), value);

		long cnt = getDatastore().query().target(TARGET).filter(LDAT.month().eq(5).and(ID.isNotNull())).count();
		assertEquals(1L, cnt);
	}

}
