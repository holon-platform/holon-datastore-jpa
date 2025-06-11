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

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import com.holonplatform.core.beans.BeanPropertySet;
import com.holonplatform.core.datastore.DataTarget;
import com.holonplatform.core.datastore.Datastore;
import com.holonplatform.core.property.PropertyBox;
import com.holonplatform.datastore.jpa.JpaDatastore;
import com.holonplatform.datastore.jpa.JpaTarget;

@SuppressWarnings("unused")
public class ExampleJpaDatastoreBean {

	// tag::beans1[]
	static //
	@Entity public class Test {

		public static final BeanPropertySet<Test> PROPERTIES = BeanPropertySet.create(Test.class); // <1>

		public static final DataTarget<Test> TARGET = JpaTarget.of(Test.class); // <2>

		@Id
		private Long id;

		private String value;

		// getters and setters omitted

	}

	void operations() {
		Datastore datastore = getJpaDatastore();

		PropertyBox value = PropertyBox.builder(Test.PROPERTIES).set(Test.PROPERTIES.property("id"), 1L)
				.set(Test.PROPERTIES.property("value"), "test").build(); // <3>
		datastore.save(Test.TARGET, value);

		List<PropertyBox> values = datastore.query(Test.TARGET).filter(Test.PROPERTIES.property("id").gt(0L))
				.sort(Test.PROPERTIES.property("value").desc()).list(Test.PROPERTIES); // <4>
	}
	// end::beans1[]

	public static JpaDatastore getJpaDatastore() {
		return null;
	}

}
