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

import java.util.Optional;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import com.holonplatform.core.datastore.DataTarget;
import com.holonplatform.core.datastore.Datastore;
import com.holonplatform.core.datastore.Datastore.OperationResult;
import com.holonplatform.core.datastore.DefaultWriteOption;
import com.holonplatform.core.datastore.transaction.TransactionConfiguration;
import com.holonplatform.core.property.NumericProperty;
import com.holonplatform.core.property.PropertyBox;
import com.holonplatform.core.property.PropertySet;
import com.holonplatform.core.property.StringProperty;
import com.holonplatform.datastore.jpa.JpaDatastore;
import com.holonplatform.datastore.jpa.JpaWriteOption;

@SuppressWarnings("unused")
public class ExampleJpaDatastore {

	@Entity
	public class Test {

		@Id
		@Column(name = "code")
		private Long id;

		@Column(name = "text")
		private String value;

		// getters and setters omitted

	}

	public static final NumericProperty<Long> ID = NumericProperty.longType("id");
	public static final StringProperty VALUE = StringProperty.create("value");

	public static final PropertySet<?> TEST = PropertySet.of(ID, VALUE);

	public static final DataTarget<?> TARGET = DataTarget.named("Test");

	public void flush() {
		// tag::flush[]
		PropertyBox valueToSave = PropertyBox.builder(TEST).set(ID, 1L).set(VALUE, "test").build();

		getJpaDatastore().save(TARGET, valueToSave, JpaWriteOption.FLUSH); // <1>
		// end::flush[]

	}

	public void ids1() {
		// tag::ids1[]
		Datastore datastore = getJpaDatastore();

		PropertyBox value = PropertyBox.builder(TEST).set(VALUE, "test").build();

		OperationResult result = datastore.insert(TARGET, value, JpaWriteOption.FLUSH); // <1>

		Optional<Long> idValue = result.getInsertedKey(ID); // <2>
		// end::ids1[]
	}

	public void ids2() {
		// tag::ids2[]
		Datastore datastore = getJpaDatastore();

		PropertyBox value = PropertyBox.builder(TEST).set(VALUE, "test").build();

		OperationResult result = datastore.insert(TARGET, value, JpaWriteOption.FLUSH,
				DefaultWriteOption.BRING_BACK_GENERATED_IDS); // <1>

		Long idValue = value.getValue(ID); // <2>
		// end::ids2[]
	}

	public void transactional() {
		// tag::transactional[]
		final Datastore datastore = getJpaDatastore(); // build or obtain a JPA Datastore

		datastore.requireTransactional().withTransaction(tx -> { // <1>
			PropertyBox value = buildPropertyBoxValue();
			datastore.save(TARGET, value);

			tx.commit(); // <2>
		});

		OperationResult result = datastore.requireTransactional().withTransaction(tx -> { // <3>

			PropertyBox value = buildPropertyBoxValue();
			return datastore.save(TARGET, value);

		}, TransactionConfiguration.withAutoCommit()); // <4>
		// end::transactional[]
	}

	public void withEntityManager() {
		// tag::wem[]
		JpaDatastore datastore = getJpaDatastore(); // build or obtain a JpaDatastore

		datastore.withEntityManager(em -> { // <1>
			em.persist(new Test());
		});

		Test result = datastore.withEntityManager(em -> { // <2>
			return em.find(Test.class, 1);
		});
		// end::wem[]
	}

	private static JpaDatastore getJpaDatastore() {
		return null;
	}

	private static PropertyBox buildPropertyBoxValue() {
		return null;
	}

}
