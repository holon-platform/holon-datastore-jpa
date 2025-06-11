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

import static com.holonplatform.datastore.jpa.test.model.TestDataModel.CLOB_STR;
import static com.holonplatform.datastore.jpa.test.model.TestDataModel.DAT;
import static com.holonplatform.datastore.jpa.test.model.TestDataModel.DBL;
import static com.holonplatform.datastore.jpa.test.model.TestDataModel.ENM;
import static com.holonplatform.datastore.jpa.test.model.TestDataModel.KEY;
import static com.holonplatform.datastore.jpa.test.model.TestDataModel.NBOOL;
import static com.holonplatform.datastore.jpa.test.model.TestDataModel.NST_DEC;
import static com.holonplatform.datastore.jpa.test.model.TestDataModel.NST_STR;
import static com.holonplatform.datastore.jpa.test.model.TestDataModel.STR1;
import static com.holonplatform.datastore.jpa.test.model.TestDataModel.TMS;
import static com.holonplatform.datastore.jpa.test.model.TestDataModel.VIRTUAL_STR;

import java.util.List;
import java.util.Properties;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.spi.PersistenceProvider;
import jakarta.persistence.spi.PersistenceProviderResolverHolder;

import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.holonplatform.core.property.PathProperty;
import com.holonplatform.core.property.PropertySet;
import com.holonplatform.core.property.TemporalProperty;
import com.holonplatform.datastore.jpa.JpaDatastore;
import com.holonplatform.datastore.jpa.JpaTarget;
import com.holonplatform.datastore.jpa.ORMPlatform;
import com.holonplatform.datastore.jpa.test.config.DatastoreConfigCommodity;
import com.holonplatform.datastore.jpa.test.expression.KeyIsFilter;
import com.holonplatform.datastore.jpa.test.model.entity.Test1;
import com.holonplatform.datastore.jpa.test.model.entity.Test3;
import com.holonplatform.datastore.jpa.test.suite.AbstractJpaDatastoreTestSuite;
import com.holonplatform.jdbc.DataSourceBuilder;

public class TestEclipselink extends AbstractJpaDatastoreTestSuite {

	private static EntityManagerFactory entityManagerFactory;

	@BeforeClass
	public static void initDatastore() {

		// init db
		DataSourceBuilder.builder().url("jdbc:h2:mem:datastore2;DB_CLOSE_ON_EXIT=FALSE").username("sa")
				.withInitScriptResource("h2/init.sql").build();

		Properties props = new Properties();
		props.setProperty(PersistenceUnitProperties.ECLIPSELINK_PERSISTENCE_XML,
				"META-INF/persistence-eclipselink.xml");

		entityManagerFactory = getEclipselinkPersistenceProvider().createEntityManagerFactory("test_eclipselink",
				props);

		datastore = JpaDatastore.builder().entityManagerFactory(entityManagerFactory).traceEnabled(true)
				.withCommodity(DatastoreConfigCommodity.FACTORY).withExpressionResolver(KeyIsFilter.RESOLVER).build();

		platform = ORMPlatform.ECLIPSELINK;

		rightJoinTest = false;
		avgProjectionTest = false;
		txExpectedErrorTest = false;
		temporalProjectionTest = false;

		JPA_TARGET = JpaTarget.of(Test1.class);

		LDAT = TemporalProperty.localDate("localDateValue");
		LTMS = TemporalProperty.localDateTime("localDatetimeValue");
		TIME = TemporalProperty.localTime("localTimeValue");

		PROPERTIES = PropertySet.builderOf(KEY, STR1, DBL, DAT, LDAT, ENM, NBOOL, NST_STR, NST_DEC, TMS, LTMS, TIME)
				.withIdentifier(KEY).build();
		PROPERTIES_NOID = PropertySet.of(KEY, STR1, DBL, DAT, LDAT, ENM, NBOOL, NST_STR, NST_DEC, TMS, LTMS, TIME);
		PROPERTIES_V = PropertySet
				.builderOf(KEY, STR1, DBL, DAT, LDAT, ENM, NBOOL, NST_STR, NST_DEC, TMS, LTMS, TIME, VIRTUAL_STR)
				.withIdentifier(KEY).build();

		CLOB_SET_STR = PropertySet.of(PROPERTIES, CLOB_STR);

		TEST3 = JpaTarget.of(Test3.class);

		TEST3_CODE_P = PathProperty.create("pk.code", long.class).parent(TEST3);
		TEST3_TEXT_P = PathProperty.create("text", String.class).parent(TEST3);
	}

	@AfterClass
	public static void closeEmf() {
		entityManagerFactory.close();
	}

	public static PersistenceProvider getEclipselinkPersistenceProvider() {
		List<PersistenceProvider> providers = PersistenceProviderResolverHolder.getPersistenceProviderResolver()
				.getPersistenceProviders();

		for (PersistenceProvider provider : providers) {
			if (provider instanceof org.eclipse.persistence.jpa.PersistenceProvider) {
				return provider;
			}
		}

		return null;
	}

}
