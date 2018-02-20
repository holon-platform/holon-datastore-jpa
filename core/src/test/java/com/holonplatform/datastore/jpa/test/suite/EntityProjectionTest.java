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

import static com.holonplatform.datastore.jpa.test.model.TestDataModel.JPA_TARGET;
import static com.holonplatform.datastore.jpa.test.model.TestDataModel.KEY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Test;

import com.holonplatform.core.datastore.DataTarget;
import com.holonplatform.datastore.jpa.test.model.entity.Test1;

public class EntityProjectionTest extends AbstractJpaDatastoreSuiteTest {

	@Test
	public void testEntityProjection1() {
		List<Test1> results = getDatastore().query().target(JPA_TARGET).filter(KEY.eq(1L)).sort(KEY.asc())
				.list(JPA_TARGET);
		assertNotNull(results);
		assertEquals(1, results.size());

		Test1 first = results.get(0);
		assertEquals(Long.valueOf(1L), first.getKey());
	}

	@Test
	public void testEntityProjection2() {
		List<Test1> results = getDatastore().query().target(DataTarget.named(Test1.class.getSimpleName()))
				.filter(KEY.eq(1L)).sort(KEY.asc()).list(JPA_TARGET);
		assertNotNull(results);
		assertEquals(1, results.size());

		Test1 first = results.get(0);
		assertEquals(Long.valueOf(1L), first.getKey());
	}

}
