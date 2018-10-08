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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

@Transactional
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestEnableDatastore.Config.class)
public class TestEnableDatastore {

	@Configuration
	@PropertySource("test.properties")
	@EnableTransactionManagement
	@EnableDataSource
	@EnableJpaDatastore
	protected static class Config {

		@Bean
		@Primary
		public FactoryBean<EntityManagerFactory> entityManagerFactory(DataSource dataSource) {
			LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
			emf.setDataSource(dataSource);
			HibernateJpaVendorAdapter va = new HibernateJpaVendorAdapter();
			va.setShowSql(true);
			emf.setJpaVendorAdapter(va);
			emf.setPackagesToScan(TestJpaDomain1.class.getPackage().getName());
			return emf;
		}

		@Bean
		public PlatformTransactionManager transactionManager(EntityManagerFactory emf) {
			return new JpaTransactionManager(emf);
		}

	}

	private final static PathProperty<Long> KEY = PathProperty.create("key", long.class);
	private final static PathProperty<String> STR = PathProperty.create("stringValue", String.class);
	private final static PathProperty<Double> DEC = PathProperty.create("decimalValue", Double.class);

	@Autowired
	private JpaDatastore datastore;

	@Transactional
	@Test
	public void testDatastore() {

		assertNotNull(datastore);

		datastore.save(JpaTarget.of(TestJpaDomain1.class),
				PropertyBox.builder(KEY, STR, DEC).set(KEY, 7L).set(STR, "Test ds").set(DEC, 7.7).build());

		Optional<Long> found = datastore.query().target(JpaTarget.of(TestJpaDomain1.class)).filter(KEY.eq(7L))
				.findOne(KEY);
		assertTrue(found.isPresent());

	}

}
