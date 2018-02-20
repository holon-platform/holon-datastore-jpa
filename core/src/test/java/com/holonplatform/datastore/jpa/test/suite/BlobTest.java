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

import static com.holonplatform.datastore.jpa.test.model.TestDataModel.BLOB_BYS;
import static com.holonplatform.datastore.jpa.test.model.TestDataModel.KEY;
import static com.holonplatform.datastore.jpa.test.model.TestDataModel.JPA_TARGET;
import static com.holonplatform.datastore.jpa.test.model.TestDataModel.NBOOL;
import static com.holonplatform.datastore.jpa.test.model.TestDataModel.PROPERTIES;
import static com.holonplatform.datastore.jpa.test.model.TestDataModel.STR;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

import com.holonplatform.core.property.PropertyBox;
import com.holonplatform.core.property.PropertySet;
import com.holonplatform.datastore.jpa.test.model.TestDataModel;

public class BlobTest extends AbstractJpaDatastoreSuiteTest {

	private static final PropertySet<?> BLOB_SET_BYT = PropertySet.of(PROPERTIES, BLOB_BYS);

	@Test
	public void testBlobBytes() {
		inTransaction(() -> {

			// query

			byte[] bval = getDatastore().query().target(JPA_TARGET).filter(KEY.eq(1L)).findOne(BLOB_BYS).orElse(null);
			assertNotNull(bval);
			assertTrue(Arrays.equals(TestDataModel.DEFAULT_BLOB_VALUE, bval));

			PropertyBox value = getDatastore().query().target(JPA_TARGET).filter(KEY.eq(1L)).findOne(BLOB_SET_BYT)
					.orElse(null);
			assertNotNull(value);
			assertTrue(Arrays.equals(TestDataModel.DEFAULT_BLOB_VALUE, value.getValue(BLOB_BYS)));

			// update

			final byte[] bytes = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16 };

			value.setValue(BLOB_BYS, bytes);
			getDatastore().update(JPA_TARGET, value);

			value = getDatastore().query().target(JPA_TARGET).filter(KEY.eq(1L)).findOne(BLOB_SET_BYT).orElse(null);
			assertNotNull(value);
			assertTrue(Arrays.equals(bytes, value.getValue(BLOB_BYS)));

			// insert

			value = PropertyBox.builder(BLOB_SET_BYT).set(KEY, 77L).set(STR, "Test clob").set(NBOOL, false)
					.set(BLOB_BYS, bytes).build();
			getDatastore().insert(JPA_TARGET, value);

			bval = getDatastore().query().target(JPA_TARGET).filter(KEY.eq(77L)).findOne(BLOB_BYS).orElse(null);
			assertNotNull(bval);
			assertTrue(Arrays.equals(bytes, bval));

		});
	}

}
