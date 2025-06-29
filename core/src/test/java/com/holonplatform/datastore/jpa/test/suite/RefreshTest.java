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

import static com.holonplatform.datastore.jpa.test.model.TestDataModel.KEY;
import static com.holonplatform.datastore.jpa.test.model.TestDataModel.STR1;
import static com.holonplatform.datastore.jpa.test.model.TestDataModel.VIRTUAL_STR;
import static com.holonplatform.datastore.jpa.test.suite.AbstractJpaDatastoreTestSuite.JPA_TARGET;
import static com.holonplatform.datastore.jpa.test.suite.AbstractJpaDatastoreTestSuite.PROPERTIES;
import static com.holonplatform.datastore.jpa.test.suite.AbstractJpaDatastoreTestSuite.PROPERTIES_NOID;
import static com.holonplatform.datastore.jpa.test.suite.AbstractJpaDatastoreTestSuite.PROPERTIES_V;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.holonplatform.core.datastore.Datastore.OperationResult;
import com.holonplatform.core.property.PropertyBox;

public class RefreshTest extends AbstractJpaDatastoreSuiteTest {

	@Test
	public void testRefresh() {
		PropertyBox value = getDatastore().query().target(JPA_TARGET).filter(KEY.eq(1L)).findOne(PROPERTIES)
				.orElse(null);
		assertNotNull(value);

		PropertyBox refreshed = getDatastore().refresh(JPA_TARGET, value);
		assertNotNull(refreshed);
		assertEquals(Long.valueOf(1), refreshed.getValue(KEY));
	}

	@Test
	public void testRefreshVirtual() {
		PropertyBox value = getDatastore().query().target(JPA_TARGET).filter(KEY.eq(1L)).findOne(PROPERTIES_V)
				.orElse(null);
		assertNotNull(value);
		assertEquals("[One]", value.getValue(VIRTUAL_STR));

		PropertyBox refreshed = getDatastore().refresh(JPA_TARGET, value);
		assertNotNull(refreshed);
		assertEquals(Long.valueOf(1), refreshed.getValue(KEY));
		assertEquals("[One]", refreshed.getValue(VIRTUAL_STR));
	}

	@Test
	public void testUpdateRefresh() {

		inTransaction(() -> {
			PropertyBox value = getDatastore().query().target(JPA_TARGET).filter(KEY.eq(1L)).findOne(PROPERTIES)
					.orElse(null);
			assertNotNull(value);
			assertEquals("One", value.getValue(STR1));

			// update STR1 value
			OperationResult result = getDatastore().bulkUpdate(JPA_TARGET).set(STR1, "OneX").filter(KEY.eq(1L))
					.execute();
			assertEquals(1, result.getAffectedCount());

			// refresh
			PropertyBox refreshed = getDatastore().refresh(JPA_TARGET, value);
			assertNotNull(refreshed);
			assertEquals(Long.valueOf(1), refreshed.getValue(KEY));
			assertEquals("OneX", refreshed.getValue(STR1));

		});
	}

	@Test
	public void testRefreshNoId() {
		PropertyBox value = getDatastore().query().target(JPA_TARGET).filter(KEY.eq(1L)).findOne(PROPERTIES_NOID)
				.orElse(null);
		assertNotNull(value);

		PropertyBox refreshed = getDatastore().refresh(JPA_TARGET, value);
		assertNotNull(refreshed);
		assertEquals(Long.valueOf(1), refreshed.getValue(KEY));
	}

}
