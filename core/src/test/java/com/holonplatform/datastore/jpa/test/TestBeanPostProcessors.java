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
package com.holonplatform.datastore.jpa.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.holonplatform.core.beans.BeanPropertySet;
import com.holonplatform.core.internal.property.EnumByOrdinalConverter;
import com.holonplatform.core.temporal.TemporalType;
import com.holonplatform.datastore.jpa.test.model.entity.Test1;
import com.holonplatform.datastore.jpa.test.model.entity.Test3;

public class TestBeanPostProcessors {

	@Test
	public void testBeanPropertyPostProcessors() {

		BeanPropertySet<Test1> set = BeanPropertySet.create(Test1.class);

		assertTrue(set.getProperty("dateValue").isPresent());
		assertEquals(TemporalType.DATE,
				set.getProperty("dateValue").get().getConfiguration().getTemporalType().orElse(null));

		assertTrue(set.getProperty("enumValue").isPresent());
		assertTrue(set.getProperty("enumValue").get().getConverter().isPresent());
		assertEquals(EnumByOrdinalConverter.class, set.getProperty("enumValue").get().getConverter().get().getClass());

		// identifier

		assertTrue(set.getFirstIdentifier().isPresent());
		assertEquals("key", set.getFirstIdentifier().get().getName());

		// embedded id
		BeanPropertySet<Test3> set2 = BeanPropertySet.create(Test3.class);

		assertTrue(set2.getFirstIdentifier().isPresent());
		assertEquals("pk.code", set2.getFirstIdentifier().get().relativeName());

	}

}
