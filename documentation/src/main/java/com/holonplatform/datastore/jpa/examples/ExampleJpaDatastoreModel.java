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

import java.util.List;
import java.util.stream.Stream;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import com.holonplatform.core.datastore.DataTarget;
import com.holonplatform.core.datastore.Datastore;
import com.holonplatform.core.property.NumericProperty;
import com.holonplatform.core.property.PropertyBox;
import com.holonplatform.core.property.PropertySet;
import com.holonplatform.core.property.StringProperty;
import com.holonplatform.datastore.jpa.JpaDatastore;
import com.holonplatform.datastore.jpa.JpaTarget;

@SuppressWarnings("unused")
public class ExampleJpaDatastoreModel {

	// tag::model1[]
	@Entity
	public class Test {

		@Id
		@Column(name = "code")
		private Long id;

		@Column(name = "text")
		private String value;

		// getters and setters omitted

	}
	// end::model1[]

	// tag::model2[]
	public static final NumericProperty<Long> ID = NumericProperty.longType("id"); // <1>
	public static final StringProperty VALUE = StringProperty.create("value"); // <2>

	public static final PropertySet<?> TEST = PropertySet.of(ID, VALUE);

	public static final DataTarget<?> TARGET = DataTarget.named("Test"); // <3>
	// end::model2[]

	void operations() {
		// tag::model3[]
		Datastore datastore = getJpaDatastore();

		Stream<Long> ids = datastore.query().target(TARGET).filter(ID.gt(0L)).sort(VALUE.asc()).stream(ID); // <1>
		Stream<PropertyBox> results = datastore.query().target(TARGET).filter(ID.gt(0L)).sort(VALUE.asc()).stream(TEST); // <2>

		PropertyBox valueToSave = PropertyBox.builder(TEST).set(ID, 1L).set(VALUE, "test").build();
		datastore.save(TARGET, valueToSave); // <3>
		// end::model3[]
	}

	// tag::model4[]
	public static final JpaTarget<Test> JPA_TARGET = JpaTarget.of(Test.class); // <1>

	void query() {
		Datastore datastore = getJpaDatastore();

		List<Test> resultEntities = datastore.query(JPA_TARGET).filter(ID.gt(0L)).list(JPA_TARGET); // <2>
	}
	// end::model4[]

	public static JpaDatastore getJpaDatastore() {
		return null;
	}

}
