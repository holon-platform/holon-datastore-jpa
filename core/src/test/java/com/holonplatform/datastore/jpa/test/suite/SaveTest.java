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
import static com.holonplatform.datastore.jpa.test.model.TestDataModel.TEST2;
import static com.holonplatform.datastore.jpa.test.model.TestDataModel.TEST2_CODE;
import static com.holonplatform.datastore.jpa.test.model.TestDataModel.TEST2_PROPERTIES;
import static com.holonplatform.datastore.jpa.test.model.TestDataModel.TEST2_TEXT;
import static com.holonplatform.datastore.jpa.test.model.TestDataModel.TMS;
import static com.holonplatform.datastore.jpa.test.suite.AbstractJpaDatastoreTestSuite.JPA_TARGET;
import static com.holonplatform.datastore.jpa.test.suite.AbstractJpaDatastoreTestSuite.LDAT;
import static com.holonplatform.datastore.jpa.test.suite.AbstractJpaDatastoreTestSuite.LTMS;
import static com.holonplatform.datastore.jpa.test.suite.AbstractJpaDatastoreTestSuite.PROPERTIES;
import static com.holonplatform.datastore.jpa.test.suite.AbstractJpaDatastoreTestSuite.TIME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Optional;

import org.junit.Test;

import com.holonplatform.core.datastore.Datastore.OperationResult;
import com.holonplatform.core.datastore.Datastore.OperationType;
import com.holonplatform.core.property.PropertyBox;
import com.holonplatform.datastore.jpa.JpaWriteOption;
import com.holonplatform.datastore.jpa.test.model.TestEnum;
import com.holonplatform.datastore.jpa.test.model.TestSampleData;

public class SaveTest extends AbstractJpaDatastoreSuiteTest {

	@Test
	public void testSaveAsInsert() {
		if (AbstractJpaDatastoreTestSuite.saveOperationTest) {
			inTransaction(() -> {

				PropertyBox value = PropertyBox.builder(TEST2_PROPERTIES).set(TEST2_TEXT, "test_ins").build();

				OperationResult result = getDatastore().save(TEST2, value, JpaWriteOption.FLUSH);
				assertEquals(1, result.getAffectedCount());

				if (AbstractJpaDatastoreTestSuite.saveOperationTypeTest) {
					assertEquals(OperationType.INSERT, result.getOperationType().orElse(null));
				}

				Optional<Long> insertedKey = result.getInsertedKey(TEST2_CODE);
				assertTrue(insertedKey.isPresent());

				Long key = insertedKey.get();
				assertNotNull(key);

				value = getDatastore().query(TEST2).filter(TEST2_CODE.eq(key)).findOne(TEST2_PROPERTIES).orElse(null);
				assertNotNull(value);
				assertEquals(key, value.getValue(TEST2_CODE));
				assertEquals("test_ins", value.getValue(TEST2_TEXT));

			});
		}
	}

	@Test
	public void testSaveAsUpdate() {
		if (AbstractJpaDatastoreTestSuite.saveOperationTest) {
			inTransaction(() -> {

				PropertyBox value = PropertyBox.builder(PROPERTIES).set(KEY, 1L).set(STR1, "k401").set(DBL, 7.45)
						.set(DAT, TestSampleData.DATE1).set(LDAT, TestSampleData.LDATE1).set(ENM, TestEnum.SECOND)
						.set(NST_STR, "str1").set(NST_DEC, TestSampleData.BD1).set(NBOOL, false)
						.set(TMS, TestSampleData.DATETIME1).set(LTMS, TestSampleData.LDATETIME1)
						.set(TIME, TestSampleData.LTIME1).build();

				OperationResult result = getDatastore().save(JPA_TARGET, value);
				assertEquals(1, result.getAffectedCount());

				if (AbstractJpaDatastoreTestSuite.saveOperationTypeTest) {
					assertEquals(OperationType.UPDATE, result.getOperationType().orElse(null));
				}

				value = getDatastore().query(JPA_TARGET).filter(KEY.eq(1L)).findOne(PROPERTIES).orElse(null);
				assertNotNull(value);
				assertEquals(Long.valueOf(1), value.getValue(KEY));
				assertEquals("k401", value.getValue(STR1));
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
	}

}
