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
package com.holonplatform.datastore.jpa.examples;

import javax.sql.DataSource;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import com.holonplatform.core.datastore.Datastore;

import jakarta.persistence.EntityManagerFactory;

public class ExampleJpaDatastoreSpringBoot1 {

	// tag::config[]
	@Configuration
	@EnableAutoConfiguration
	class Config {

		@Bean
		public DataSource dataSource() {
			return buildDataSource();
		}

		@Bean
		public FactoryBean<EntityManagerFactory> entityManagerFactory(DataSource dataSource) {
			LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
			emf.setDataSource(dataSource);
			emf.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
			emf.setPackagesToScan("com.example.entities");
			return emf;
		}

	}

	@Autowired
	Datastore datastore; // <1>
	// end::config[]

	public static DataSource buildDataSource() {
		return null;
	}

}
