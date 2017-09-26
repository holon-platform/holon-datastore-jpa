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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.PlatformTransactionManager;

import com.holonplatform.datastore.jpa.test.domain.TestJpaDomain;
import com.holonplatform.jdbc.MultiTenantDataSource;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestMultiTenancy.Config.class)
public class TestMultiTenancy {

	private static final ThreadLocal<String> TENANT = new ThreadLocal<>();

	@Configuration
	protected static class Config {

		@Bean(destroyMethod = "close")
		public MultiTenantDataSource dataSource() {
			return MultiTenantDataSource.builder().resolver(() -> Optional.ofNullable(TENANT.get()))
					.provider(tenantId -> {
						if (tenantId == null) {
							// for hibernate startup
							return new EmbeddedDatabaseBuilder().setType(EmbeddedDatabaseType.H2).setName("startup")
									.addScript("test-db-schema.sql").build();
						}
						return new EmbeddedDatabaseBuilder().setType(EmbeddedDatabaseType.H2).setName(tenantId)
								.addScript("test-db-schema.sql").addScript("test-db-data-" + tenantId + ".sql").build();
					}).build();
		}

		@Bean
		public FactoryBean<EntityManagerFactory> entityManagerFactory() {
			LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
			emf.setDataSource(dataSource());

			HibernateJpaVendorAdapter va = new HibernateJpaVendorAdapter();
			va.setDatabase(Database.H2);
			emf.setJpaVendorAdapter(va);

			emf.setPackagesToScan(TestJpaDomain.class.getPackage().getName());
			emf.setPersistenceUnitName("test");
			return emf;
		}

		@Bean
		public PlatformTransactionManager transactionManager(EntityManagerFactory emf) {
			return new JpaTransactionManager(emf);
		}

	}

	@PersistenceContext
	private EntityManager entityManager;

	@Test
	public void testQuery() {

		TENANT.set("tenant2");

		TestJpaDomain result = entityManager
				.createQuery("SELECT t FROM TestJpaDomain t WHERE t.key=?", TestJpaDomain.class).setParameter(0, 2L)
				.getSingleResult();

		assertNotNull(result);
		assertEquals("Tenant2", result.getStringValue());

		TENANT.remove();

	}

	@Test
	public void testQuery2() {

		TENANT.set("tenant1");

		TestJpaDomain result = entityManager
				.createQuery("SELECT t FROM TestJpaDomain t WHERE t.key=?", TestJpaDomain.class).setParameter(0, 1L)
				.getSingleResult();

		assertNotNull(result);
		assertEquals("Tenant1", result.getStringValue());

		TENANT.remove();

	}

}
