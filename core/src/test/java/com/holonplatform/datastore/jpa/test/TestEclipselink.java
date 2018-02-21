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
package com.holonplatform.datastore.jpa.test;

import static org.junit.Assert.assertEquals;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.junit.BeforeClass;
import org.junit.Test;

import com.holonplatform.datastore.jpa.JpaDatastore;
import com.holonplatform.datastore.jpa.ORMPlatform;
import com.holonplatform.datastore.jpa.dialect.EclipselinkDialect;
import com.holonplatform.datastore.jpa.test.config.DatastoreConfigCommodity;
import com.holonplatform.datastore.jpa.test.expression.KeyIsFilter;
import com.holonplatform.datastore.jpa.test.suite.AbstractJpaDatastoreTestSuite;
import com.holonplatform.jdbc.DataSourceBuilder;

public class TestEclipselink extends AbstractJpaDatastoreTestSuite {

	@BeforeClass
	public static void initDatastore() {

		// init db
		DataSourceBuilder.builder().url("jdbc:h2:mem:datastore;DB_CLOSE_ON_EXIT=FALSE").username("sa")
				.withInitScriptResource("h2/init.sql").build();

		final EntityManagerFactory emf = Persistence.createEntityManagerFactory("test_eclipselink");

		datastore = JpaDatastore.builder().entityManagerFactory(emf).traceEnabled(true)
				.withCommodity(DatastoreConfigCommodity.FACTORY).withExpressionResolver(KeyIsFilter.RESOLVER).build();

		rightJoinTest = false;
		avgProjectionTest = false;
	}

	@Test
	public void testConfig() {
		DatastoreConfigCommodity c = datastore.create(DatastoreConfigCommodity.class);
		assertEquals(ORMPlatform.ECLIPSELINK, c.getPlatform());
		assertEquals(EclipselinkDialect.class, c.getDialect().getClass());
	}

}
