/*
 * Copyright 2000-2016 Holon TDCN.
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
package com.holonplatform.jpa.spring.boot.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.holonplatform.core.datastore.DataTarget;
import com.holonplatform.core.datastore.Datastore;
import com.holonplatform.core.property.PathProperty;
import com.holonplatform.core.property.PropertyBox;
import com.holonplatform.datastore.jpa.JpaTarget;
import com.holonplatform.jpa.spring.boot.test.domain1.TestJpaDomain1;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@ActiveProfiles("standard")
public class TestDatastoreAutoConfig {

	@Configuration
	@EnableAutoConfiguration
	@EntityScan(basePackageClasses = TestJpaDomain1.class)
	protected static class Config {

	}

	private final static PathProperty<Long> KEY = PathProperty.create("key", long.class);
	private final static PathProperty<String> STR = PathProperty.create("stringValue", String.class);
	private final static PathProperty<Double> DEC = PathProperty.create("decimalValue", Double.class);

	private final static DataTarget<TestJpaDomain1> TARGET = JpaTarget.of(TestJpaDomain1.class);

	@Autowired
	private Datastore datastore;

	@Transactional
	@Test
	public void testJpa() {

		assertNotNull(datastore);

		TestJpaDomain1 td = new TestJpaDomain1();
		td.setKey(7L);
		td.setStringValue("Test ds");
		td.setDecimalValue(7.7);

		datastore.save(TARGET,
				PropertyBox.builder(KEY, STR, DEC).set(KEY, 7L).set(STR, "Test ds").set(DEC, 7.7).build());

		Optional<Long> found = datastore.query().target(TARGET).filter(KEY.eq(7L)).findOne(KEY);
		assertTrue(found.isPresent());

	}

}
