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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.holonplatform.datastore.jpa.ORMPlatform;
import com.holonplatform.datastore.jpa.dialect.DatanucleusDialect;
import com.holonplatform.datastore.jpa.dialect.EclipselinkDialect;
import com.holonplatform.datastore.jpa.dialect.HibernateDialect;
import com.holonplatform.datastore.jpa.dialect.OpenJPADialect;
import com.holonplatform.datastore.jpa.test.config.DatastoreConfigCommodity;

public class PlatformConfigTest extends AbstractJpaDatastoreSuiteTest {

	@Test
	public void testConfig() {

		DatastoreConfigCommodity c = getDatastore().create(DatastoreConfigCommodity.class);

		if (AbstractJpaDatastoreTestSuite.platform != null) {
			switch (AbstractJpaDatastoreTestSuite.platform) {
			case DATANUCLEUS:
				assertEquals(ORMPlatform.DATANUCLEUS, c.getPlatform());
				assertEquals(DatanucleusDialect.class, c.getDialect().getClass());
				break;
			case ECLIPSELINK:
				assertEquals(ORMPlatform.ECLIPSELINK, c.getPlatform());
				assertEquals(EclipselinkDialect.class, c.getDialect().getClass());
				break;
			case HIBERNATE:
				assertEquals(ORMPlatform.HIBERNATE, c.getPlatform());
				assertEquals(HibernateDialect.class, c.getDialect().getClass());
				break;
			case OPENJPA:
				assertEquals(ORMPlatform.OPENJPA, c.getPlatform());
				assertEquals(OpenJPADialect.class, c.getDialect().getClass());
				break;
			default:
				break;
			}
		}

	}

}
