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
package com.holonplatform.jpa.datastore.examples;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.holonplatform.core.datastore.Datastore;
import com.holonplatform.datastore.jpa.JpaDatastore;
import com.holonplatform.jdbc.spring.EnableDataSource;
import com.holonplatform.jpa.spring.EnableJpa;
import com.holonplatform.jpa.spring.EnableJpaDatastore;

@SuppressWarnings("unused")
public class ExampleJpaDatastoreSpring {

	// tag::spring[]
	@EnableJpaDatastore
	@EnableDataSource
	@PropertySource("datasource.properties")
	@Configuration
	class Config {

		@Bean
		public FactoryBean<EntityManagerFactory> entityManagerFactory(DataSource dataSource) {
			LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
			emf.setDataSource(dataSource);
			emf.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
			emf.setPackagesToScan("com.exmaple.test.entities");
			return emf;
		}

		@Bean
		public PlatformTransactionManager transactionManager(EntityManagerFactory emf) {
			return new JpaTransactionManager(emf);
		}

	}

	class MyBean {

		@Autowired
		private Datastore datastore;

	}
	// end::spring[]

	// tag::jpa[]
	@PropertySource("jpa.properties")
	@EnableJpa(entityPackageClasses = Test.class)
	@Configuration
	class JpaConfig {

	}
	// end::jpa[]

	class Test {

	}

	// tag::multi[]
	@Configuration
	@PropertySource("jpa.properties")
	@EnableTransactionManagement
	class MultiJpaConfig {

		@Configuration
		@EnableJpa(dataContextId = "one", entityPackageClasses = Test1.class)
		class ConfigOne {
		}

		@Configuration
		@EnableJpa(dataContextId = "two", entityPackageClasses = Test2.class)
		class ConfigTwo {
		}

	}

	@Autowired
	@Qualifier("one")
	private DataSource dataSource1;
	@Autowired
	@Qualifier("one")
	private EntityManagerFactory entityManagerFactory1;
	@Autowired
	@Qualifier("one")
	private PlatformTransactionManager transactionManager1;
	@Autowired
	@Qualifier("one")
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
	// end::multi[]

	class Test1 {

	}

	class Test2 {

	}

}
