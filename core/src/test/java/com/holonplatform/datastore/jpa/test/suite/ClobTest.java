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

import static com.holonplatform.datastore.jpa.test.model.TestDataModel.CLOB_STR;
import static com.holonplatform.datastore.jpa.test.model.TestDataModel.KEY;
import static com.holonplatform.datastore.jpa.test.model.TestDataModel.NBOOL;
import static com.holonplatform.datastore.jpa.test.model.TestDataModel.STR;
import static com.holonplatform.datastore.jpa.test.suite.AbstractJpaDatastoreTestSuite.CLOB_SET_STR;
import static com.holonplatform.datastore.jpa.test.suite.AbstractJpaDatastoreTestSuite.JPA_TARGET;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.holonplatform.core.property.PropertyBox;

public class ClobTest extends AbstractJpaDatastoreSuiteTest {

	

	@Test
	public void testClobString() {
		inTransaction(() -> {

			// query

			String sval = getDatastore().query().target(JPA_TARGET).filter(KEY.eq(1L)).findOne(CLOB_STR).orElse(null);
			assertNotNull(sval);
			assertEquals("clocbcontent", sval);

			PropertyBox value = getDatastore().query().target(JPA_TARGET).filter(KEY.eq(1L)).findOne(CLOB_SET_STR)
					.orElse(null);
			assertNotNull(value);
			assertEquals("clocbcontent", value.getValue(CLOB_STR));

			// update

			value.setValue(CLOB_STR, "updclob");
			getDatastore().update(JPA_TARGET, value);

			value = getDatastore().query().target(JPA_TARGET).filter(KEY.eq(1L)).findOne(CLOB_SET_STR).orElse(null);
			assertNotNull(value);
			assertEquals("updclob", value.getValue(CLOB_STR));

			// insert
			value = PropertyBox.builder(CLOB_SET_STR).set(KEY, 77L).set(STR, "Test clob").set(NBOOL, false)
					.set(CLOB_STR, "savedclob").build();
			getDatastore().insert(JPA_TARGET, value);

			sval = getDatastore().query().target(JPA_TARGET).filter(KEY.eq(77L)).findOne(CLOB_STR).orElse(null);
			assertNotNull(sval);
			assertEquals("savedclob", sval);

		});
	}

}
