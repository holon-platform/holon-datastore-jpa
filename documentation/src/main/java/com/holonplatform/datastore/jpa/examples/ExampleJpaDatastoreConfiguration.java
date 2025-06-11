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

import jakarta.persistence.EntityManagerFactory;

import com.holonplatform.core.datastore.Datastore;
import com.holonplatform.core.datastore.DatastoreConfigProperties;
import com.holonplatform.datastore.jpa.JpaDatastore;
import com.holonplatform.datastore.jpa.ORMPlatform;
import com.holonplatform.datastore.jpa.dialect.ORMDialect;

@SuppressWarnings("unused")
public class ExampleJpaDatastoreConfiguration {

	public void builder1() {
		// tag::builder1[]
		JpaDatastore datastore = JpaDatastore.builder() // <1>
				// Datastore configuration omitted
				.build();
		// end::builder1[]
	}

	public void builder2() {
		// tag::builder2[]
		Datastore datastore = JpaDatastore.builder() // <1>
				// Datastore configuration omitted
				.build();
		// end::builder2[]
	}

	public void setup1() {
		// tag::setup1[]
		Datastore datastore = JpaDatastore.builder()
				// DataSource configuration omitted
				.dataContextId("mydataContextId") // <1>
				.traceEnabled(true) // <2>
				.build();
		// end::setup1[]
	}

	public void setup2() {
		// tag::setup2[]
		Datastore datastore = JpaDatastore.builder()
				// DataSource configuration omitted
				.configuration(DatastoreConfigProperties.builder().withPropertySource("datastore.properties").build()) // <1>
				.build();
		// end::setup2[]
	}

	public void setup3() {
		// tag::setup3[]
		EntityManagerFactory entityManagerFactory = createOrObtainEntityManagerFactory();

		Datastore datastore = JpaDatastore.builder() //
				.entityManagerFactory(entityManagerFactory) // <1>
				.build();
		// end::setup3[]
	}

	public void setup4() {
		// tag::setup4[]
		EntityManagerFactory entityManagerFactory = createOrObtainEntityManagerFactory();

		Datastore datastore = JpaDatastore.builder() //
				.entityManagerFactory(entityManagerFactory) //
				.autoFlush(true) // <1>
				.build();
		// end::setup4[]
	}

	public void setup5() {
		// tag::setup5[]
		EntityManagerFactory entityManagerFactory = createOrObtainEntityManagerFactory();

		Datastore datastore = JpaDatastore.builder() //
				.entityManagerFactory(entityManagerFactory) //
				.platform(ORMPlatform.HIBERNATE) // <1>
				.build();
		// end::setup5[]
	}

	public void setup6() {
		// tag::setup6[]
		EntityManagerFactory entityManagerFactory = createOrObtainEntityManagerFactory();

		Datastore datastore = JpaDatastore.builder() //
				.entityManagerFactory(entityManagerFactory) //
				.dialect(ORMDialect.hibernate()) // <1>
				.dialect("com.holonplatform.datastore.jpa.dialect.HibernateDialect") // <2>
				.build();
		// end::setup6[]
	}

	public void setup7() {
		// tag::setup7[]
		EntityManagerFactory entityManagerFactory = createOrObtainEntityManagerFactory();

		Datastore datastore = JpaDatastore.builder() //
				.entityManagerFactory(entityManagerFactory) //
				.configuration(DatastoreConfigProperties.builder().withPropertySource("datastore.properties").build()) // <1>
				.build();
		// end::setup7[]
	}

	public void setup8() {
		// tag::setup8[]
		EntityManagerFactory entityManagerFactory = createOrObtainEntityManagerFactory();

		Datastore datastore = JpaDatastore.builder() //
				.entityManagerFactory(entityManagerFactory) //
				.entityManagerInitializer(emf -> emf.createEntityManager()) // <1>
				.entityManagerFinalizer(em -> em.close()) // <2>
				.build();
		// end::setup8[]
	}

	private static EntityManagerFactory createOrObtainEntityManagerFactory() {
		return null;
	}

}
