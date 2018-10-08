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

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import com.holonplatform.core.property.PathProperty;
import com.holonplatform.core.property.PropertyBox;
import com.holonplatform.datastore.jpa.JpaDatastore;
import com.holonplatform.datastore.jpa.JpaTarget;
import com.holonplatform.jdbc.spring.EnableDataSource;
import com.holonplatform.jpa.spring.EnableJpaDatastore;
import com.holonplatform.jpa.spring.test.domain1.TestJpaDomain1;
import com.holonplatform.jpa.spring.test.domain2.TestJpaDomain2;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestEnableDatastoreMulti.Config.class)
public class TestEnableDatastoreMulti {

	@Configuration
	@EnableTransactionManagement
	@Import({ Config1.class, Config2.class })
	protected static class Config {

	}

	@Configuration
	@PropertySource("test2.properties")
	@EnableDataSource(dataContextId = "one")
	@EnableJpaDatastore(dataContextId = "one", entityManagerFactoryReference = "entityManagerFactory1")
	protected static class Config1 {

		@Bean
		@Primary
		public FactoryBean<EntityManagerFactory> entityManagerFactory1(@Qualifier("one") DataSource dataSource) {
			LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
			emf.setDataSource(dataSource);
			HibernateJpaVendorAdapter va = new HibernateJpaVendorAdapter();
			va.setShowSql(true);
			emf.setJpaVendorAdapter(va);
			emf.setPackagesToScan(TestJpaDomain1.class.getPackage().getName());
			emf.setPersistenceUnitName("one");
			return emf;
		}

		@Bean
		@Primary
		@Qualifier("one")
		public PlatformTransactionManager transactionManager1(EntityManagerFactory emf) {
			return new JpaTransactionManager(emf);
		}

	}

	@Configuration
	@PropertySource("test3.properties")
	@EnableDataSource(dataContextId = "two")
	@EnableJpaDatastore(dataContextId = "two", entityManagerFactoryReference = "entityManagerFactory2")
	protected static class Config2 {

		@Bean
		@Qualifier("two")
		public FactoryBean<EntityManagerFactory> entityManagerFactory2(@Qualifier("two") DataSource dataSource) {
			LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
			emf.setDataSource(dataSource);
			HibernateJpaVendorAdapter va = new HibernateJpaVendorAdapter();
			va.setShowSql(true);
			emf.setJpaVendorAdapter(va);
			emf.setPackagesToScan(TestJpaDomain2.class.getPackage().getName());
			emf.setPersistenceUnitName("two");
			return emf;
		}

		@Bean
		@Qualifier("two")
		public PlatformTransactionManager transactionManager2(@Qualifier("two") EntityManagerFactory emf) {
			return new JpaTransactionManager(emf);
		}

	}

	private final static PathProperty<Long> KEY = PathProperty.create("key", long.class);
	private final static PathProperty<String> STR = PathProperty.create("stringValue", String.class);
	private final static PathProperty<Double> DEC = PathProperty.create("decimalValue", Double.class);

	@Autowired
	private JpaDatastore datastore;

	@Autowired
	@Qualifier("two")
	private JpaDatastore datastore2;

	@Transactional
	@Test
	public void testDatastore1() {

		assertNotNull(datastore);

		datastore.save(JpaTarget.of(TestJpaDomain1.class),
				PropertyBox.builder(KEY, STR, DEC).set(KEY, 7L).set(STR, "Test ds").set(DEC, 7.7).build());

		Optional<Long> found = datastore.query().target(JpaTarget.of(TestJpaDomain1.class)).filter(KEY.eq(7L))
				.findOne(KEY);
		assertTrue(found.isPresent());

	}

	@Transactional("two")
	@Test
	public void testDatastore2() {

		assertNotNull(datastore2);

		datastore2.save(JpaTarget.of(TestJpaDomain2.class),
				PropertyBox.builder(KEY, STR, DEC).set(KEY, 7L).set(STR, "Test ds").set(DEC, 7.7).build());

		Optional<Long> found = datastore2.query().target(JpaTarget.of(TestJpaDomain2.class)).filter(KEY.eq(7L))
				.findOne(KEY);
		assertTrue(found.isPresent());

	}

}
