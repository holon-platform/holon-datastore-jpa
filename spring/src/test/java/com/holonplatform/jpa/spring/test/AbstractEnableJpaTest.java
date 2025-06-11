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
package com.holonplatform.jpa.spring.test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;

import com.holonplatform.core.property.PathProperty;
import com.holonplatform.core.property.PropertyBox;
import com.holonplatform.datastore.jpa.JpaDatastore;
import com.holonplatform.datastore.jpa.JpaTarget;
import com.holonplatform.jpa.spring.test.domain1.TestJpaDomain1;

@Transactional
@ExtendWith(SpringExtension.class)
@DirtiesContext
public abstract class AbstractEnableJpaTest {

	private final static PathProperty<Long> KEY = PathProperty.create("key", long.class);

	@Autowired
	protected DataSource dataSource;

	@Autowired
	protected EntityManagerFactory entityManagerFactory;

	@Autowired
	protected PlatformTransactionManager transactionManager;

	@Autowired
	protected JpaDatastore datastore;

	@Test
	public void testSetup() {

		assertNotNull(dataSource);
		assertNotNull(entityManagerFactory);
		assertNotNull(transactionManager);
		assertNotNull(datastore);

	}

	@Test
	public void testDatastore() {

		final PathProperty<String> STR1 = PathProperty.create("stringValue", String.class);
		final PathProperty<Double> DEC = PathProperty.create("decimalValue", Double.class);

		datastore.save(JpaTarget.of(TestJpaDomain1.class),
				PropertyBox.builder(KEY, STR1, DEC).set(KEY, 7L).set(STR1, "Test ds").set(DEC, 7.7).build());

		Optional<Long> found = datastore.query().target(JpaTarget.of(TestJpaDomain1.class)).filter(KEY.eq(7L))
				.findOne(KEY);
		assertTrue(found.isPresent());
	}

}
