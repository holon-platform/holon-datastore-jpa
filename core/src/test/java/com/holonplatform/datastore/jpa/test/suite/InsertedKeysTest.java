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

import static com.holonplatform.datastore.jpa.test.model.TestDataModel.TEST2;
import static com.holonplatform.datastore.jpa.test.model.TestDataModel.TEST2_CODE;
import static com.holonplatform.datastore.jpa.test.model.TestDataModel.TEST2_PROPERTIES;
import static com.holonplatform.datastore.jpa.test.model.TestDataModel.TEST2_TEXT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Optional;

import org.junit.Test;

import com.holonplatform.core.datastore.Datastore.OperationResult;
import com.holonplatform.core.datastore.DefaultWriteOption;
import com.holonplatform.core.property.PropertyBox;
import com.holonplatform.datastore.jpa.JpaWriteOption;

public class InsertedKeysTest extends AbstractJpaDatastoreSuiteTest {

	@Test
	public void testGeneratedId() {

		getDatastore().requireTransactional().withTransaction(tx -> {

			long count = getDatastore().query(TEST2).count();
			assertEquals(0L, count);

			OperationResult result = getDatastore().insert(TEST2,
					PropertyBox.builder(TEST2_PROPERTIES).set(TEST2_TEXT, "Test value").build(), JpaWriteOption.FLUSH);
			assertEquals(1, result.getAffectedCount());
			assertEquals(1, result.getInsertedKeys().size());

			Optional<Long> insertedKey = result.getInsertedKey(TEST2_CODE);
			assertTrue(insertedKey.isPresent());

			// bring back ids
			PropertyBox box = PropertyBox.builder(TEST2_PROPERTIES).set(TEST2_TEXT, "Test v2").build();
			result = getDatastore().save(TEST2, box, JpaWriteOption.FLUSH, DefaultWriteOption.BRING_BACK_GENERATED_IDS);
			assertEquals(1, result.getAffectedCount());
			assertEquals(1, result.getInsertedKeys().size());

			insertedKey = result.getInsertedKey(TEST2_CODE);
			assertTrue(insertedKey.isPresent());

			assertNotNull(box.getValue(TEST2_CODE));

			tx.rollback();
		});
	}

}
