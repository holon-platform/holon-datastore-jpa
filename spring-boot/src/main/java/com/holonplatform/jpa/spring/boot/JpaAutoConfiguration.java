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
package com.holonplatform.jpa.spring.boot;

import jakarta.persistence.EntityManager;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import com.holonplatform.core.datastore.Datastore;
import com.holonplatform.core.internal.Logger;
import com.holonplatform.datastore.jpa.internal.JpaDatastoreLogger;
import com.holonplatform.jdbc.spring.boot.DataSourcesAutoConfiguration;
import com.holonplatform.jdbc.spring.boot.DataSourcesTransactionManagerAutoConfiguration;
import com.holonplatform.jpa.spring.boot.internal.JpaAutoConfigurationRegistrar;

/**
 * Spring boot auto-configuration to enable JPA stack and {@link Datastore} beans.
 * 
 * @since 5.0.0
 */
@AutoConfiguration
@ConditionalOnClass({ LocalContainerEntityManagerFactoryBean.class, EntityManager.class })
@AutoConfigureAfter(DataSourcesAutoConfiguration.class)
@AutoConfigureBefore({ DataSourceAutoConfiguration.class, DataSourceTransactionManagerAutoConfiguration.class,
		DataSourcesTransactionManagerAutoConfiguration.class, HibernateJpaAutoConfiguration.class })
public class JpaAutoConfiguration {

	private final static Logger LOGGER = JpaDatastoreLogger.create();

	@Configuration
	@ConditionalOnMissingBean(name = { "jakarta.persistence.EntityManagerFactory",
			"org.springframework.orm.jpa.LocalEntityManagerFactoryBean" })
	@Import(JpaAutoConfigurationRegistrar.class)
	static class JpaStackConfiguration implements InitializingBean {

		@Override
		public void afterPropertiesSet() throws Exception {
			LOGGER.debug(() -> "JpaStackConfiguration initialized");
		}

	}

}
