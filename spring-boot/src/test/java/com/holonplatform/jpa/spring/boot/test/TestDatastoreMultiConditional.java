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
package com.holonplatform.jpa.spring.boot.test;

import static org.junit.Assert.assertNull;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.PlatformTransactionManager;

import com.holonplatform.core.datastore.Datastore;
import com.holonplatform.jpa.spring.boot.JpaAutoConfiguration;
import com.holonplatform.jpa.spring.boot.test.domain1.TestJpaDomain1;
import com.holonplatform.jpa.spring.boot.test.domain2.TestJpaDomain2;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@ActiveProfiles("standard")
public class TestDatastoreMultiConditional {

	@Configuration
	@EnableAutoConfiguration(exclude = JpaAutoConfiguration.class)
	protected static class Config {

		@Configuration
		static class Config1 {

			@Bean
			@Qualifier("one")
			public FactoryBean<EntityManagerFactory> entityManagerFactory1(DataSource dataSource) {
				LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
				emf.setDataSource(dataSource);
				HibernateJpaVendorAdapter va = new HibernateJpaVendorAdapter();
				va.setShowSql(true);
				emf.setJpaVendorAdapter(va);
				emf.setPackagesToScan(TestJpaDomain1.class.getPackage().getName());
				return emf;
			}

			@Bean
			public PlatformTransactionManager transactionManager1(@Qualifier("one") EntityManagerFactory emf) {
				return new JpaTransactionManager(emf);
			}

		}

		@Configuration
		static class Config2 {

			@Bean
			@Qualifier("two")
			public FactoryBean<EntityManagerFactory> entityManagerFactory2(DataSource dataSource) {
				LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
				emf.setDataSource(dataSource);
				HibernateJpaVendorAdapter va = new HibernateJpaVendorAdapter();
				va.setShowSql(true);
				emf.setJpaVendorAdapter(va);
				emf.setPackagesToScan(TestJpaDomain2.class.getPackage().getName());
				return emf;
			}

			@Bean
			public PlatformTransactionManager transactionManager2(@Qualifier("two") EntityManagerFactory emf) {
				return new JpaTransactionManager(emf);
			}

		}

	}

	@Autowired(required = false)
	private Datastore datastore;

	@Test
	public void testDatastore() {
		assertNull(datastore);
	}

}
