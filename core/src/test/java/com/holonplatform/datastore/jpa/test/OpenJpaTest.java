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
import static com.holonplatform.datastore.jpa.test.model.TestDataModel.STR;
import static com.holonplatform.datastore.jpa.test.model.TestDataModel.TMS;
import static com.holonplatform.datastore.jpa.test.model.TestDataModel.VIRTUAL_STR;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceProvider;
import javax.persistence.spi.PersistenceProviderResolverHolder;

import org.apache.openjpa.persistence.PersistenceProviderImpl;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.holonplatform.core.internal.utils.ConversionUtils;
import com.holonplatform.core.property.PathProperty;
import com.holonplatform.core.property.PropertySet;
import com.holonplatform.core.property.PropertyValueConverter;
import com.holonplatform.core.property.TemporalProperty;
import com.holonplatform.datastore.jpa.JpaDatastore;
import com.holonplatform.datastore.jpa.JpaTarget;
import com.holonplatform.datastore.jpa.ORMPlatform;
import com.holonplatform.datastore.jpa.test.config.DatastoreConfigCommodity;
import com.holonplatform.datastore.jpa.test.expression.KeyIsFilter;
import com.holonplatform.datastore.jpa.test.suite.AbstractJpaDatastoreTestSuite;
import com.holonplatform.jdbc.DataSourceBuilder;

public class OpenJpaTest extends AbstractJpaDatastoreTestSuite {

	private static EntityManagerFactory entityManagerFactory;

	@BeforeClass
	public static void initDatastore() {

		// init db
		DataSourceBuilder.builder().url("jdbc:h2:mem:datastore3;DB_CLOSE_ON_EXIT=FALSE").username("sa")
				.withInitScriptResource("h2/init.sql").build();

		org.apache.openjpa.persistence.PersistenceProviderImpl openjpaProvider = null;
		List<PersistenceProvider> pp = PersistenceProviderResolverHolder.getPersistenceProviderResolver()
				.getPersistenceProviders();
		for (PersistenceProvider p : pp) {
			if (p instanceof org.apache.openjpa.persistence.PersistenceProviderImpl) {
				openjpaProvider = (PersistenceProviderImpl) p;
			}
		}

		if (openjpaProvider == null) {
			throw new RuntimeException("Failed to load org.apache.openjpa.persistence.PersistenceProviderImpl");
		}

		entityManagerFactory = openjpaProvider.createEntityManagerFactory(null, "META-INF/persistence-openjpa.xml",
				null);

		datastore = JpaDatastore.builder().entityManagerFactory(entityManagerFactory).traceEnabled(true)
				.withCommodity(DatastoreConfigCommodity.FACTORY).withExpressionResolver(KeyIsFilter.RESOLVER).build();

		platform = ORMPlatform.OPENJPA;

		rightJoinTest = false;
		blobArrayProjectionTest = false;
		entityProjectionTest = false;
		temporalPartFunctionTest = false;
		temporalProjectionTest = false;
		transactionalTest = false;
		avgProjectionTest = false;
		saveOperationTypeTest = false;
		saveOperationTest = false;
		updateNullsTest = false;

		JPA_TARGET = JpaTarget.of(com.holonplatform.datastore.jpa.test.model.oentity.Test1.class);

		LDAT = TemporalProperty.localDate("localDateValue").converter(PropertyValueConverter.localDate());
		LTMS = TemporalProperty.localDateTime("localDatetimeValue").converter(PropertyValueConverter.localDateTime());
		TIME = TemporalProperty.localTime("localTimeValue").converter(Date.class, d -> ConversionUtils.toLocalTime(d),
				t -> {
					if (t == null)
						return null;
					Calendar c = Calendar.getInstance();
					c.set(Calendar.YEAR, 1970);
					c.set(Calendar.MONTH, 0);
					c.set(Calendar.DAY_OF_MONTH, 1);
					c.set(Calendar.HOUR_OF_DAY, t.getHour());
					c.set(Calendar.MINUTE, t.getMinute());
					c.set(Calendar.SECOND, t.getSecond());
					c.set(Calendar.MILLISECOND, 0);
					return c.getTime();
				});

		PROPERTIES = PropertySet.builderOf(KEY, STR, DBL, DAT, LDAT, ENM, NBOOL, NST_STR, NST_DEC, TMS, LTMS, TIME)
				.withIdentifier(KEY).build();
		PROPERTIES_NOID = PropertySet.of(KEY, STR, DBL, DAT, LDAT, ENM, NBOOL, NST_STR, NST_DEC, TMS, LTMS, TIME);
		PROPERTIES_V = PropertySet
				.builderOf(KEY, STR, DBL, DAT, LDAT, ENM, NBOOL, NST_STR, NST_DEC, TMS, LTMS, TIME, VIRTUAL_STR)
				.withIdentifier(KEY).build();

		CLOB_SET_STR = PropertySet.of(PROPERTIES, CLOB_STR);

		TEST3 = JpaTarget.of(com.holonplatform.datastore.jpa.test.model.oentity.Test3.class);

		TEST3_CODE_P = PathProperty.create("pk.code", long.class).parent(TEST3);
		TEST3_TEXT_P = PathProperty.create("text", String.class).parent(TEST3);
	}

	@AfterClass
	public static void closeEmf() {
		entityManagerFactory.close();
	}

}
