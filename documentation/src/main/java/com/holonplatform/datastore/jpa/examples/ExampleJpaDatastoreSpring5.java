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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import com.holonplatform.core.datastore.Datastore;
import com.holonplatform.jpa.spring.EnableJpa;

public class ExampleJpaDatastoreSpring5 {

	class TestEntity1 {
	}
	
	class TestEntity2 {
	}

	// tag::config[]
	@Configuration
	@PropertySource("jdbc.properties")
	static class Config {

		@Configuration
		@EnableJpa(dataContextId = "one", entityPackageClasses = TestEntity1.class)
		static class Config1 {
		}

		@Configuration
		@EnableJpa(dataContextId = "two", entityPackageClasses = TestEntity2.class)
		static class Config2 {
		}

	}

	@Autowired
	@Qualifier("one")
	Datastore datastore1;

	@Autowired
	@Qualifier("two")
	Datastore datastore2;
	// end::config[]

}
