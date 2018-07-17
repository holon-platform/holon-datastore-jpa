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

import static com.holonplatform.datastore.jpa.test.model.TestDataModel.TEST3_CODE;
import static com.holonplatform.datastore.jpa.test.model.TestDataModel.TEST3_TEXT;
import static com.holonplatform.datastore.jpa.test.suite.AbstractJpaDatastoreTestSuite.TEST3;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

import com.holonplatform.core.property.PropertyBox;
import com.holonplatform.datastore.jpa.JpaTarget;
import com.holonplatform.datastore.jpa.test.model.entity.Test3;

public class DistinctTest extends AbstractJpaDatastoreSuiteTest {

	@Test
	public void testDistinctSingle() {
		inTransaction(() -> {

			getDatastore().insert(TEST3,
					PropertyBox.builder(TEST3_CODE, TEST3_TEXT).set(TEST3_CODE, 101L).set(TEST3_TEXT, "v1").build());
			getDatastore().insert(TEST3,
					PropertyBox.builder(TEST3_CODE, TEST3_TEXT).set(TEST3_CODE, 102L).set(TEST3_TEXT, "v2").build());
			getDatastore().insert(TEST3,
					PropertyBox.builder(TEST3_CODE, TEST3_TEXT).set(TEST3_CODE, 103L).set(TEST3_TEXT, "v1").build());

			List<String> values = getDatastore().query(TEST3).filter(TEST3_CODE.goe(101L)).list(TEST3_TEXT);
			assertEquals(3, values.size());

			values = getDatastore().query(TEST3).filter(TEST3_CODE.goe(101L)).distinct().list(TEST3_TEXT);
			assertEquals(2, values.size());
			assertTrue(values.contains("v1"));
			assertTrue(values.contains("v2"));

		});
	}

	@Test
	public void testDistinctMultiple() {
		inTransaction(() -> {

			getDatastore().insert(TEST3,
					PropertyBox.builder(TEST3_CODE, TEST3_TEXT).set(TEST3_CODE, 101L).set(TEST3_TEXT, "v1").build());
			getDatastore().insert(TEST3,
					PropertyBox.builder(TEST3_CODE, TEST3_TEXT).set(TEST3_CODE, 102L).set(TEST3_TEXT, "v2").build());
			getDatastore().insert(TEST3,
					PropertyBox.builder(TEST3_CODE, TEST3_TEXT).set(TEST3_CODE, 103L).set(TEST3_TEXT, "v1").build());

			List<PropertyBox> values = getDatastore().query(TEST3).filter(TEST3_CODE.goe(101L)).list(TEST3_CODE,
					TEST3_TEXT);
			assertEquals(3, values.size());

			values = getDatastore().query(TEST3).filter(TEST3_CODE.goe(101L)).distinct().list(TEST3_CODE, TEST3_TEXT);
			assertEquals(3, values.size());

			List<Long> keys = values.stream().map(v -> v.getValue(TEST3_CODE)).collect(Collectors.toList());
			assertTrue(keys.contains(Long.valueOf(101L)));
			assertTrue(keys.contains(Long.valueOf(102L)));
			assertTrue(keys.contains(Long.valueOf(103L)));

		});
	}

	@Test
	public void testDistinctEntity() {
		if (AbstractJpaDatastoreTestSuite.entityProjectionTest) {
			inTransaction(() -> {

				getDatastore().insert(TEST3, PropertyBox.builder(TEST3_CODE, TEST3_TEXT).set(TEST3_CODE, 101L)
						.set(TEST3_TEXT, "v1").build());
				getDatastore().insert(TEST3, PropertyBox.builder(TEST3_CODE, TEST3_TEXT).set(TEST3_CODE, 102L)
						.set(TEST3_TEXT, "v2").build());
				getDatastore().insert(TEST3, PropertyBox.builder(TEST3_CODE, TEST3_TEXT).set(TEST3_CODE, 103L)
						.set(TEST3_TEXT, "v1").build());

				List<Test3> values = getDatastore().query(JpaTarget.of(Test3.class)).filter(TEST3_CODE.goe(101L))
						.list(JpaTarget.of(Test3.class));
				assertEquals(3, values.size());

				values = getDatastore().query(JpaTarget.of(Test3.class)).filter(TEST3_CODE.goe(101L)).distinct()
						.list(JpaTarget.of(Test3.class));
				assertEquals(3, values.size());

				List<Long> keys = values.stream().map(v -> v.getPk().getCode()).collect(Collectors.toList());
				assertTrue(keys.contains(Long.valueOf(101L)));
				assertTrue(keys.contains(Long.valueOf(102L)));
				assertTrue(keys.contains(Long.valueOf(103L)));

			});
		}
	}

}
