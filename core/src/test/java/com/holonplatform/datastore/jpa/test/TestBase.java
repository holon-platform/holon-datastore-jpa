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
package com.holonplatform.datastore.jpa.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.sql.DataSource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.PlatformTransactionManager;

import com.holonplatform.core.beans.BeanPropertySet;
import com.holonplatform.core.internal.property.EnumByOrdinalConverter;
import com.holonplatform.core.internal.utils.TestUtils;
import com.holonplatform.core.temporal.TemporalType;
import com.holonplatform.datastore.jpa.JpaConfigProperties;
import com.holonplatform.datastore.jpa.JpaConfigProperties.ORMPlatform;
import com.holonplatform.datastore.jpa.internal.JpaPropertyConfiguration;
import com.holonplatform.datastore.jpa.test.domain.TestJpaDomain;
import com.holonplatform.jdbc.DataSourceBuilder;
import com.holonplatform.jdbc.DataSourceConfigProperties;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestBase.Config.class)
public class TestBase {

	@Configuration
	protected static class Config {

		@Bean(destroyMethod = "close")
		public DataSource dataSource() {
			return DataSourceBuilder.create()
					.build(DataSourceConfigProperties.builder().withDefaultPropertySources().build());
		}

		@Bean
		public FactoryBean<EntityManagerFactory> entityManagerFactory() {
			LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
			emf.setDataSource(dataSource());
			emf.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
			emf.setPackagesToScan(TestJpaDomain.class.getPackage().getName());
			emf.setPersistenceUnitName("test");
			return emf;
		}

		@Bean
		public PlatformTransactionManager transactionManager(EntityManagerFactory emf) {
			JpaTransactionManager jtm = new JpaTransactionManager(emf);
			jtm.setDataSource(dataSource());
			return jtm;
		}

	}

	@PersistenceContext
	private EntityManager entityManager;

	@Test
	public void testBase() {
		TestUtils.checkEnum(JpaConfigProperties.ORMPlatform.class);
	}

	@Test
	public void testORM() {

		ORMPlatform ptf = ORMPlatform.detectFromClasspath();
		assertNotNull(ptf);
		assertEquals(ORMPlatform.HIBERNATE, ptf);

	}

	@Test
	public void testUtils() {

		ORMPlatform ptf = ORMPlatform.resolve(entityManager);
		assertNotNull(ptf);
		assertEquals(ORMPlatform.HIBERNATE, ptf);

		TestUtils.expectedException(IllegalArgumentException.class, new Runnable() {

			@Override
			public void run() {
				ORMPlatform.resolve(null);
			}
		});

	}

	@Test
	public void testBeanPropertyPostProcessors() {

		BeanPropertySet<TestJpaDomain> set = BeanPropertySet.create(TestJpaDomain.class);

		assertTrue(set.getProperty("dateValue").isPresent());
		assertEquals(TemporalType.DATE,
				set.getProperty("dateValue").get().getConfiguration().getTemporalType().orElse(null));

		assertTrue(set.getProperty("enumValue").isPresent());
		assertTrue(set.getProperty("enumValue").get().getConverter().isPresent());
		assertEquals(EnumByOrdinalConverter.class, set.getProperty("enumValue").get().getConverter().get().getClass());

		assertEquals("enmv", set.getProperty("enumValue").get().getConfiguration()
				.getParameter(JpaPropertyConfiguration.COLUMN_NAME).orElse(null));
		assertEquals("nested", set.getProperty("nested").get().getConfiguration()
				.getParameter(JpaPropertyConfiguration.COLUMN_NAME).orElse(null));

	}

}
