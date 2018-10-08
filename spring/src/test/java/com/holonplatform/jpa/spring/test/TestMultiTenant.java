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

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import javax.sql.DataSource;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import com.holonplatform.core.property.PathProperty;
import com.holonplatform.core.property.PropertyBox;
import com.holonplatform.core.tenancy.TenantResolver;
import com.holonplatform.datastore.jpa.JpaDatastore;
import com.holonplatform.datastore.jpa.JpaTarget;
import com.holonplatform.jdbc.MultiTenantDataSource;
import com.holonplatform.jdbc.TenantDataSourceProvider;
import com.holonplatform.jpa.spring.EnableJpa;
import com.holonplatform.jpa.spring.test.domain1.TestJpaDomain1;
import com.holonplatform.spring.EnableBeanContext;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestMultiTenant.Config.class)
public class TestMultiTenant {

	private static final ThreadLocal<String> TENANT = new ThreadLocal<>();

	@Configuration
	@EnableBeanContext
	@EnableTransactionManagement
	@EnableJpa(entityPackageClasses = TestJpaDomain1.class)
	protected static class Config {

		@Bean
		public TenantResolver tenantResolver() {
			return () -> Optional.ofNullable(TENANT.get());
		}

		@Bean
		public TenantDataSourceProvider tenantDataSourceProvider() {
			return tenantId -> {
				if (tenantId == null) {
					// for hibernate startup
					return new EmbeddedDatabaseBuilder().setType(EmbeddedDatabaseType.H2).setName("startup")
							.addScript("scripts/test-db-schema.sql").build();
				}
				return new EmbeddedDatabaseBuilder().setType(EmbeddedDatabaseType.H2).setName(tenantId)
						.addScript("scripts/test-db-schema.sql").addScript("scripts/test-db-data-" + tenantId + ".sql")
						.build();
			};
		}

		@Bean
		public DataSource dataSource() {
			return MultiTenantDataSource.builder().resolver(tenantResolver()).provider(tenantDataSourceProvider())
					.build();
		}

	}

	private final static PathProperty<Long> KEY = PathProperty.create("key", long.class);
	private final static PathProperty<String> STR = PathProperty.create("stringValue", String.class);
	private final static PathProperty<Double> DEC = PathProperty.create("decimalValue", Double.class);

	@Autowired
	private JpaDatastore datastore;

	@BeforeAll
	public static void before() {
		TENANT.set("tenant1");
	}

	@AfterAll
	public static void after() {
		TENANT.remove();
	}

	@Transactional
	@Test
	public void testDatastore() {

		datastore.save(JpaTarget.of(TestJpaDomain1.class),
				PropertyBox.builder(KEY, STR, DEC).set(KEY, 7L).set(STR, "Test ds").set(DEC, 7.7).build());

		Optional<Long> found = datastore.query().target(JpaTarget.of(TestJpaDomain1.class)).filter(KEY.eq(7L))
				.findOne(KEY);
		assertTrue(found.isPresent());

	}

}
