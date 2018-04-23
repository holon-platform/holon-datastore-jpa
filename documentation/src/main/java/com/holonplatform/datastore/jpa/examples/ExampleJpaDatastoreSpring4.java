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

import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.transaction.PlatformTransactionManager;

import com.holonplatform.core.datastore.Datastore;
import com.holonplatform.jpa.spring.EnableJpa;

public class ExampleJpaDatastoreSpring4 {

	class TestEntity {
	}

	// tag::config[]
	@EnableJpa(entityPackageClasses = TestEntity.class) // <1>
	@PropertySource("jdbc.properties")
	@Configuration
	class Config {

	}

	@Autowired
	DataSource dataSource;
	
	@PersistenceUnit
	EntityManagerFactory entityManagerFactory;
	
	@Autowired
	PlatformTransactionManager transactionManager;

	@Autowired
	Datastore datastore;
	// end::config[]

}
