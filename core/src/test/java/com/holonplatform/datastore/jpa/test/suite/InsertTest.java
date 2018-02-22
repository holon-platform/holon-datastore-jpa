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
import static com.holonplatform.datastore.jpa.test.model.TestDataModel.VIRTUAL_STR;
import static com.holonplatform.datastore.jpa.test.suite.AbstractJpaDatastoreTestSuite.JPA_TARGET;
import static com.holonplatform.datastore.jpa.test.suite.AbstractJpaDatastoreTestSuite.LDAT;
import static com.holonplatform.datastore.jpa.test.suite.AbstractJpaDatastoreTestSuite.LTMS;
import static com.holonplatform.datastore.jpa.test.suite.AbstractJpaDatastoreTestSuite.PROPERTIES;
import static com.holonplatform.datastore.jpa.test.suite.AbstractJpaDatastoreTestSuite.PROPERTIES_V;
import static com.holonplatform.datastore.jpa.test.suite.AbstractJpaDatastoreTestSuite.TIME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.holonplatform.core.datastore.Datastore.OperationResult;
import com.holonplatform.core.property.PropertyBox;
import com.holonplatform.datastore.jpa.test.model.TestEnum;
import com.holonplatform.datastore.jpa.test.model.TestSampleData;

public class InsertTest extends AbstractJpaDatastoreSuiteTest {

	@Test
	public void testInsert() {

		inTransaction(() -> {

			PropertyBox value = PropertyBox.builder(PROPERTIES).set(KEY, 301L).set(STR, "k301").set(DBL, 7.45)
					.set(DAT, TestSampleData.DATE1).set(LDAT, TestSampleData.LDATE1).set(ENM, TestEnum.SECOND)
					.set(NST_STR, "str1").set(NST_DEC, TestSampleData.BD1).set(NBOOL, false)
					.set(TMS, TestSampleData.DATETIME1).set(LTMS, TestSampleData.LDATETIME1)
					.set(TIME, TestSampleData.LTIME1).build();

			OperationResult result = getDatastore().insert(JPA_TARGET, value);
			assertEquals(1, result.getAffectedCount());

			value = getDatastore().query().target(JPA_TARGET).filter(KEY.eq(301L)).findOne(PROPERTIES).orElse(null);
			assertNotNull(value);
			assertEquals(Long.valueOf(301), value.getValue(KEY));
			assertEquals("k301", value.getValue(STR));
			assertEquals(Double.valueOf(7.45), value.getValue(DBL));
			assertEquals(TestSampleData.DATE1, value.getValue(DAT));
			assertEquals(TestSampleData.LDATE1, value.getValue(LDAT));
			assertEquals(TestEnum.SECOND, value.getValue(ENM));
			assertEquals("str1", value.getValue(NST_STR));
			assertEquals(TestSampleData.BD1, value.getValue(NST_DEC));
			assertFalse(value.getValue(NBOOL));
			assertEquals(TestSampleData.DATETIME1, value.getValue(TMS));
			assertEquals(TestSampleData.LDATETIME1, value.getValue(LTMS));
			assertEquals(TestSampleData.LTIME1, value.getValue(TIME));

		});
	}

	@Test
	public void testInsertVirtual() {
		inTransaction(() -> {

			PropertyBox value = PropertyBox.builder(PROPERTIES_V).set(KEY, 301L).set(STR, "k301").set(NBOOL, true)
					.build();
			OperationResult result = getDatastore().insert(JPA_TARGET, value);
			assertEquals(1, result.getAffectedCount());

			value = getDatastore().query().target(JPA_TARGET).filter(KEY.eq(301L)).findOne(PROPERTIES_V).orElse(null);
			assertNotNull(value);
			assertEquals(Long.valueOf(301), value.getValue(KEY));
			assertEquals("k301", value.getValue(STR));
			assertTrue(value.getValue(NBOOL));
			assertEquals("[k301]", value.getValue(VIRTUAL_STR));

		});
	}

}
