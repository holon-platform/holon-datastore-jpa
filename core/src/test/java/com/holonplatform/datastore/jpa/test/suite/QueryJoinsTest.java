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

import static com.holonplatform.datastore.jpa.test.model.TestDataModel.R_NAME;
import static com.holonplatform.datastore.jpa.test.model.TestDataModel.R_PARENT;
import static com.holonplatform.datastore.jpa.test.model.TestDataModel.R_TARGET;
import static com.holonplatform.datastore.jpa.test.model.TestDataModel.TEST3_CODE;
import static com.holonplatform.datastore.jpa.test.model.TestDataModel.TEST3_TEXT;
import static com.holonplatform.datastore.jpa.test.suite.AbstractJpaDatastoreTestSuite.JPA_TARGET;
import static com.holonplatform.datastore.jpa.test.suite.AbstractJpaDatastoreTestSuite.TEST3;
import static com.holonplatform.datastore.jpa.test.suite.AbstractJpaDatastoreTestSuite.TEST3_CODE_P;
import static com.holonplatform.datastore.jpa.test.suite.AbstractJpaDatastoreTestSuite.TEST3_TEXT_P;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.holonplatform.core.datastore.relational.RelationalTarget;
import com.holonplatform.core.property.PathProperty;
import com.holonplatform.core.property.PropertyBox;
import com.holonplatform.core.query.QueryFilter;
import com.holonplatform.datastore.jpa.test.config.DatastoreConfigCommodity;
import com.holonplatform.datastore.jpa.test.model.TestDataModel;

public class QueryJoinsTest extends AbstractJpaDatastoreSuiteTest {

	// with parent
	public final static PathProperty<Long> KEY_P = JPA_TARGET.property(TestDataModel.KEY);
	public final static PathProperty<String> STR_P = JPA_TARGET.property(TestDataModel.STR1);

	@Test
	public void testJoins() {

		int jpaMayVer = getDatastore().create(DatastoreConfigCommodity.class).getDialect()
				.getSupportedJPAMajorVersion();
		int jpaMinVer = getDatastore().create(DatastoreConfigCommodity.class).getDialect()
				.getSupportedJPAMinorVersion();

		if (jpaMayVer > 2 || (jpaMayVer == 2 && jpaMinVer > 0)) {

			long key = getDatastore().query().target(TEST3).filter(TEST3_CODE.eq(2L)).findOne(TEST3_CODE).orElse(null);
			assertEquals(2, key);

			List<PropertyBox> results = getDatastore().query()
					.target(RelationalTarget.of(TEST3).innerJoin(JPA_TARGET).on(KEY_P.eq(TEST3_CODE)).add())
					.list(TEST3_TEXT, STR_P);
			assertNotNull(results);
			assertEquals(1, results.size());
			assertEquals("Two", results.get(0).getValue(STR_P));
			assertEquals("TestJoin", results.get(0).getValue(TEST3_TEXT));

			results = getDatastore().query()
					.target(RelationalTarget.of(TEST3).innerJoin(JPA_TARGET).on(KEY_P.eq(TEST3_CODE_P)).add())
					.list(TEST3_TEXT_P, STR_P);
			assertNotNull(results);
			assertEquals(1, results.size());
			assertEquals("Two", results.get(0).getValue(STR_P));
			assertEquals("TestJoin", results.get(0).getValue(TEST3_TEXT_P));

			results = getDatastore().query()
					.target(RelationalTarget.of(TEST3).innerJoin(JPA_TARGET).on(KEY_P.eq(TEST3_CODE_P)).add())
					.list(TEST3_TEXT_P, STR_P);
			assertNotNull(results);
			assertEquals(1, results.size());

			results = getDatastore().query()
					.target(RelationalTarget.of(JPA_TARGET).leftJoin(TEST3).on(KEY_P.eq(TEST3_CODE_P)).add())
					.list(TEST3_TEXT_P, STR_P);
			assertNotNull(results);
			assertEquals(2, results.size());

		}

	}

	@Test
	public void testRightJoins() {

		if (AbstractJpaDatastoreTestSuite.rightJoinTest) {

			List<PropertyBox> results = getDatastore().query()
					.target(RelationalTarget.of(JPA_TARGET).rightJoin(TEST3).on(KEY_P.eq(TEST3_CODE_P)).add())
					.list(TEST3_TEXT_P, STR_P);
			assertNotNull(results);
			assertEquals(2, results.size());

			results = getDatastore().query()
					.target(RelationalTarget.of(TEST3).rightJoin(JPA_TARGET).on(KEY_P.eq(TEST3_CODE_P)).add())
					.list(TEST3_TEXT_P, STR_P);
			assertNotNull(results);
			assertEquals(2, results.size());

		}

	}

	@Test
	public void testRecur() {

		int jpaMayVer = getDatastore().create(DatastoreConfigCommodity.class).getDialect()
				.getSupportedJPAMajorVersion();
		int jpaMinVer = getDatastore().create(DatastoreConfigCommodity.class).getDialect()
				.getSupportedJPAMinorVersion();

		if (jpaMayVer > 2 || (jpaMayVer == 2 && jpaMinVer > 0)) {

			List<String> parents = new ArrayList<>();
			findParents(parents, "test3");

			assertEquals(2, parents.size());

		}

	}

	private void findParents(List<String> parents, String name) {
		if (name != null) {
			RelationalTarget<?> group_alias_1 = RelationalTarget.of(R_TARGET).alias("g1");
			RelationalTarget<?> group_alias_2 = RelationalTarget.of(R_TARGET).alias("g2");

			QueryFilter f1 = group_alias_1.property(R_PARENT).isNotNull();
			QueryFilter f2 = group_alias_2.property(R_NAME).eq(group_alias_1.property(R_PARENT));

			RelationalTarget<?> target = group_alias_1.innerJoin(group_alias_2).on(f1.and(f2)).add();

			List<String> group_parents = getDatastore().query().target(target)
					.filter(group_alias_1.property(R_NAME).eq(name)).list(group_alias_2.property(R_NAME));

			if (!group_parents.isEmpty()) {
				parents.addAll(group_parents);
				for (String p : group_parents) {
					findParents(parents, p);
				}
			}
		}
	}

}
