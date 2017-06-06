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
package com.holonplatform.jpa.examples;

import javax.persistence.Entity;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Id;

import com.holonplatform.core.datastore.DataTarget;
import com.holonplatform.core.datastore.Datastore;
import com.holonplatform.core.datastore.Datastore.OperationResult;
import com.holonplatform.core.datastore.DatastoreConfigProperties;
import com.holonplatform.core.datastore.DefaultWriteOption;
import com.holonplatform.core.property.PathProperty;
import com.holonplatform.core.property.PropertyBox;
import com.holonplatform.datastore.jpa.JpaDatastore;
import com.holonplatform.datastore.jpa.JpaTarget;
import com.holonplatform.datastore.jpa.JpaWriteOption;

@SuppressWarnings("unused")
public class ExampleJpaDatastore {

	public void withEntityManager() {
		// tag::wem[]
		JpaDatastore datastore = getJpaDatastore(); // build or obtain a JpaDatastore

		Test result = datastore.withEntityManager(em -> {
			return em.find(Test.class, 1);
		});
		// end::wem[]
	}

	public void setup() {
		// tag::setup[]
		EntityManagerFactory entityManagerFactory = getEntityManagerFactory(); // get the EntityManagerFactory to use

		JpaDatastore datastore = JpaDatastore.builder() // obtain the builder
				.entityManagerFactory(entityManagerFactory) // <1>
				.autoFlush(true) // <2>
				.traceEnabled(true) // <3>
				.build();

		datastore = JpaDatastore.builder() // obtain the builder
				.entityManagerFactory(entityManagerFactory) // <4>
				.entityManagerInitializer(emf -> emf.createEntityManager()) // <5>
				.entityManagerFinalizer(em -> em.close()) // <6>
				.build();

		datastore = JpaDatastore.builder() // obtain the builder
				.entityManagerFactory(entityManagerFactory) // <7>
				.dataContextId("test") // <8>
				.configuration(
						DatastoreConfigProperties.builder("test").withPropertySource("datastore.properties").build()) // <9>
				.build();
		// end::setup[]
	}

	public void jpaTarget() {
		// tag::jpatarget[]
		JpaTarget<Test> TARGET = JpaTarget.of(Test.class); // <1>
		// end::jpatarget[]
	}

	// tag::flush[]
	@Entity
	class Test {

		@Id
		private Integer id;
		private String name;

		public Integer getId() {
			return id;
		}

		public void setId(Integer id) {
			this.id = id;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

	}

	public void insertAndFlush() {

		final PathProperty<Integer> ID = PathProperty.create("id", Integer.class);
		final PathProperty<String> NAME = PathProperty.create("name", String.class);

		final DataTarget<Test> TARGET = JpaTarget.of(Test.class);

		OperationResult result = getDatastore().insert(TARGET,
				PropertyBox.builder(ID, NAME).set(NAME, "Test name").build(), JpaWriteOption.FLUSH);

	}
	// end::flush[]

	public void ids() {
		// tag::ids[]
		final PathProperty<Integer> ID = PathProperty.create("id", Integer.class); // <1>
		final PathProperty<String> NAME = PathProperty.create("name", String.class);

		Datastore datastore = getDatastore(); // build or obtain a Datastore

		PropertyBox value = PropertyBox.builder(ID, NAME).set(NAME, "Test name").build(); // <2>

		datastore.insert(JpaTarget.of(Test.class), value, JpaWriteOption.FLUSH,
				DefaultWriteOption.BRING_BACK_GENERATED_IDS); // <3>

		Integer idValue = value.getValue(ID); // <4>
		// end::ids[]
	}

	@SuppressWarnings("static-method")
	private Datastore getDatastore() {
		return null;
	}

	@SuppressWarnings("static-method")
	private JpaDatastore getJpaDatastore() {
		return null;
	}

	@SuppressWarnings("static-method")
	private EntityManagerFactory getEntityManagerFactory() {
		return null;
	}

}
