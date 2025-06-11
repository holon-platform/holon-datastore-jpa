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
package com.holonplatform.jpa.spring.test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import com.holonplatform.core.property.PathProperty;
import com.holonplatform.core.property.PropertyBox;
import com.holonplatform.datastore.jpa.JpaDatastore;
import com.holonplatform.datastore.jpa.JpaTarget;
import com.holonplatform.jpa.spring.EnableJpa;
import com.holonplatform.jpa.spring.test.domain1.TestJpaDomain1;
import com.holonplatform.jpa.spring.test.domain2.TestJpaDomain2;

import jakarta.persistence.EntityManagerFactory;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestMultiDataContextWithPrimaryExternal.Config.class)
@DirtiesContext
public class TestMultiDataContextWithPrimaryExternal {

	@Configuration
	@PropertySource("test3.properties")
	@EnableTransactionManagement
	protected static class Config {

		@Configuration
		@EnableJpa(dataContextId = "one", entityPackageClasses = TestJpaDomain1.class)
		static class Config1 {
		}

		@Configuration
		@EnableJpa(dataContextId = "two", entityPackageClasses = TestJpaDomain2.class)
		static class Config2 {
		}
	}

	private final static PathProperty<Long> KEY = PathProperty.create("key", long.class);
	private final static PathProperty<String> STR1 = PathProperty.create("stringValue", String.class);
	private final static PathProperty<Double> DEC = PathProperty.create("decimalValue", Double.class);

	@Autowired
	private DataSource dataSource1;
	@Autowired
	private EntityManagerFactory entityManagerFactory1;
	@Autowired
	private PlatformTransactionManager transactionManager1;
	@Autowired
	private JpaDatastore datastore1;

	@Autowired
	@Qualifier("two")
	private DataSource dataSource2;
	@Autowired
	@Qualifier("two")
	private EntityManagerFactory entityManagerFactory2;
	@Autowired
	@Qualifier("two")
	private PlatformTransactionManager transactionManager2;
	@Autowired
	@Qualifier("two")
	private JpaDatastore datastore2;

	@Transactional
	@Test
	public void testDataContext1() {
		assertNotNull(dataSource1);
		assertNotNull(entityManagerFactory1);
		assertNotNull(transactionManager1);
		assertNotNull(datastore1);

		datastore1.save(JpaTarget.of(TestJpaDomain1.class),
				PropertyBox.builder(KEY, STR1, DEC).set(KEY, 7L).set(STR1, "Test ds").set(DEC, 7.7).build());

		Optional<Long> found = datastore1.query().target(JpaTarget.of(TestJpaDomain1.class)).filter(KEY.eq(7L))
				.findOne(KEY);
		assertTrue(found.isPresent());
	}

	@Transactional("two")
	@Test
	public void testDataContext2() {
		assertNotNull(dataSource2);
		assertNotNull(entityManagerFactory2);
		assertNotNull(transactionManager2);
		assertNotNull(datastore2);

		datastore2.save(JpaTarget.of(TestJpaDomain2.class),
				PropertyBox.builder(KEY, STR1, DEC).set(KEY, 7L).set(STR1, "Test ds").set(DEC, 7.7).build());

		Optional<Long> found = datastore2.query().target(JpaTarget.of(TestJpaDomain2.class)).filter(KEY.eq(7L))
				.findOne(KEY);
		assertTrue(found.isPresent());
	}

}
