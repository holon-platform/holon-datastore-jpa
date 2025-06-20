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
import static com.holonplatform.datastore.jpa.test.suite.AbstractJpaDatastoreTestSuite.JPA_TARGET;
import static com.holonplatform.datastore.jpa.test.suite.AbstractJpaDatastoreTestSuite.PROPERTIES;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.holonplatform.core.property.PropertyBox;
import com.holonplatform.datastore.jpa.JpaDatastore;

public class SharedEntityManagerTest extends AbstractJpaDatastoreSuiteTest {

	@Test
	public void testSharedEntityManager() {
		assertTrue(getDatastore() instanceof JpaDatastore);

		final JpaDatastore ds = (JpaDatastore) getDatastore();

		ds.withEntityManager(em -> {

			PropertyBox value = PropertyBox.builder(PROPERTIES).set(KEY, 881L).set(STR1, "TestSE").build();

			ds.insert(JPA_TARGET, value);

			Object found = em.find(JPA_TARGET.getEntityClass(), Long.valueOf(881L));
			assertNotNull(found);

			ds.delete(JPA_TARGET, value);

		});

	}

}
