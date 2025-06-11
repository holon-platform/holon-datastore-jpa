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

import java.util.Properties;

import jakarta.persistence.EntityManagerFactory;

import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import com.holonplatform.datastore.jpa.JpaDatastore;

public class TestEclipselinkEntityMetamodel extends AbstractEntityMetamodelTest {

	private static EntityManagerFactory entityManagerFactory;

	@BeforeAll
	public static void initDatastore() {

		Properties props = new Properties();
		props.setProperty(PersistenceUnitProperties.ECLIPSELINK_PERSISTENCE_XML,
				"META-INF/persistence-eclipselink.xml");

		entityManagerFactory = TestEclipselink.getEclipselinkPersistenceProvider()
				.createEntityManagerFactory("test_metamodel", props);

		datastore = JpaDatastore.builder().entityManagerFactory(entityManagerFactory)
				.withCommodity(new EntityMetamodelCommodityFactory()).build();

	}

	@AfterAll
	public static void closeEmf() {
		entityManagerFactory.close();
	}

	@Override
	public void testSinglePrimitiveId() {
		// skip
		// see: https://bugs.eclipse.org/bugs/show_bug.cgi?id=415027
	}

}
