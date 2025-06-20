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
import static com.holonplatform.datastore.jpa.test.model.TestDataModel.STR1;
import static com.holonplatform.datastore.jpa.test.model.TestDataModel.TMS;
import static com.holonplatform.datastore.jpa.test.model.TestDataModel.VIRTUAL_STR;
import static com.holonplatform.datastore.jpa.test.suite.AbstractJpaDatastoreTestSuite.JPA_TARGET;
import static com.holonplatform.datastore.jpa.test.suite.AbstractJpaDatastoreTestSuite.LDAT;
import static com.holonplatform.datastore.jpa.test.suite.AbstractJpaDatastoreTestSuite.LTMS;
import static com.holonplatform.datastore.jpa.test.suite.AbstractJpaDatastoreTestSuite.PROPERTIES;
import static com.holonplatform.datastore.jpa.test.suite.AbstractJpaDatastoreTestSuite.PROPERTIES_NOID;
import static com.holonplatform.datastore.jpa.test.suite.AbstractJpaDatastoreTestSuite.PROPERTIES_V;
import static com.holonplatform.datastore.jpa.test.suite.AbstractJpaDatastoreTestSuite.TIME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import com.holonplatform.core.datastore.Datastore.OperationResult;
import com.holonplatform.core.property.PropertyBox;
import com.holonplatform.datastore.jpa.JpaWriteOption;
import com.holonplatform.datastore.jpa.test.model.TestEnum;
import com.holonplatform.datastore.jpa.test.model.TestSampleData;

public class UpdateTest extends AbstractJpaDatastoreSuiteTest {

	@Test
	public void testUpdate() {
		inTransaction(() -> {

			PropertyBox value = getDatastore().query().target(JPA_TARGET).filter(KEY.eq(1L)).findOne(PROPERTIES)
					.orElse(null);
			assertNotNull(value);

			value.setValue(STR1, "Ustr");
			value.setValue(DBL, 432.67d);
			value.setValue(DAT, TestSampleData.DATE1);
			value.setValue(LDAT, TestSampleData.LDATE1);
			value.setValue(ENM, TestEnum.THIRD);
			value.setValue(NBOOL, false);
			value.setValue(NST_STR, "Unstr");
			value.setValue(NST_DEC, TestSampleData.BD1);
			value.setValue(TMS, TestSampleData.DATETIME1);
			value.setValue(LTMS, TestSampleData.LDATETIME1);
			value.setValue(TIME, TestSampleData.LTIME1);

			OperationResult result = getDatastore().update(JPA_TARGET, value, JpaWriteOption.FLUSH);
			assertEquals(1, result.getAffectedCount());

			value = getDatastore().query().target(JPA_TARGET).filter(KEY.eq(1L)).findOne(PROPERTIES).orElse(null);
			assertNotNull(value);
			assertEquals(Long.valueOf(1), value.getValue(KEY));
			assertEquals("Ustr", value.getValue(STR1));
			assertEquals(Double.valueOf(432.67), value.getValue(DBL));
			assertEquals(TestSampleData.DATE1, value.getValue(DAT));
			assertEquals(TestSampleData.LDATE1, value.getValue(LDAT));
			assertEquals(TestEnum.THIRD, value.getValue(ENM));

			if (AbstractJpaDatastoreTestSuite.updateNestedTest) {
				assertEquals("Unstr", value.getValue(NST_STR));
				assertEquals(TestSampleData.BD1, value.getValue(NST_DEC));
			}

			assertFalse(value.getValue(NBOOL));
			assertEquals(TestSampleData.DATETIME1, value.getValue(TMS));
			assertEquals(TestSampleData.LDATETIME1, value.getValue(LTMS));
			assertEquals(TestSampleData.LTIME1, value.getValue(TIME));

		});
	}

	@Test
	public void testUpdateVirtual() {
		inTransaction(() -> {

			PropertyBox value = getDatastore().query().target(JPA_TARGET).filter(KEY.eq(1L)).findOne(PROPERTIES_V)
					.orElse(null);
			assertNotNull(value);
			assertEquals("[One]", value.getValue(VIRTUAL_STR));

			value.setValue(STR1, "Ustr");
			OperationResult result = getDatastore().update(JPA_TARGET, value);
			assertEquals(1, result.getAffectedCount());

			value = getDatastore().query().target(JPA_TARGET).filter(KEY.eq(1L)).findOne(PROPERTIES_V).orElse(null);
			assertNotNull(value);
			assertEquals("[Ustr]", value.getValue(VIRTUAL_STR));

		});
	}

	@Test
	public void testUpdateNulls() {
		if (AbstractJpaDatastoreTestSuite.updateNullsTest) {
			inTransaction(() -> {

				PropertyBox value = getDatastore().query().target(JPA_TARGET).filter(KEY.eq(1L)).findOne(PROPERTIES)
						.orElse(null);
				assertNotNull(value);

				value.setValue(STR1, null);

				OperationResult result = getDatastore().update(JPA_TARGET, value, JpaWriteOption.FLUSH);
				assertEquals(1, result.getAffectedCount());

				value = getDatastore().query().target(JPA_TARGET).filter(KEY.eq(1L)).findOne(PROPERTIES).orElse(null);
				assertNotNull(value);
				assertNull(value.getValue(STR1));

			});
		}
	}

	@Test
	public void testUpdateNoId() {
		inTransaction(() -> {

			PropertyBox value = getDatastore().query().target(JPA_TARGET).filter(KEY.eq(1L)).findOne(PROPERTIES_NOID)
					.orElse(null);
			assertNotNull(value);

			value.setValue(STR1, "uxs");

			OperationResult result = getDatastore().update(JPA_TARGET, value);
			assertEquals(1, result.getAffectedCount());

			value = getDatastore().query().target(JPA_TARGET).filter(KEY.eq(1L)).findOne(PROPERTIES).orElse(null);
			assertNotNull(value);
			assertEquals("uxs", value.getValue(STR1));

		});
	}

}
