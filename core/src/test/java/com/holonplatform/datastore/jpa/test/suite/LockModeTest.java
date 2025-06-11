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
import static com.holonplatform.datastore.jpa.test.suite.AbstractJpaDatastoreTestSuite.JPA_TARGET;
import static com.holonplatform.datastore.jpa.test.suite.AbstractJpaDatastoreTestSuite.PROPERTIES;

import jakarta.persistence.LockModeType;

import org.junit.Test;

import com.holonplatform.datastore.jpa.JpaDatastore;

public class LockModeTest extends AbstractJpaDatastoreSuiteTest {

	@Test
	public void testLock() {
		inTransaction(() -> {
			getDatastore().query().target(JPA_TARGET).filter(KEY.eq(1L))
					.parameter(JpaDatastore.QUERY_PARAMETER_LOCK_MODE, LockModeType.PESSIMISTIC_WRITE)
					.findOne(PROPERTIES).orElse(null);
		});
	}

}
