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

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Configuration;

import com.holonplatform.jpa.spring.boot.JpaEntityScan;

public class ExampleJpaDatastoreSpringBoot3 {

	class TestEntity1 {

	}

	class TestEntity2 {

	}

	// tag::config[]
	@Configuration
	@EnableAutoConfiguration
	@JpaEntityScan(value = "one", basePackageClasses = TestEntity1.class) // <1>
	@JpaEntityScan(value = "two", basePackageClasses = TestEntity2.class) // <2>
	class Config {

	}
	// end::config[]

}
