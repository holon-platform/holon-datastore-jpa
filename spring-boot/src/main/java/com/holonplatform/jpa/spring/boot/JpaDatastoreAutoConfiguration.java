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

import javax.persistence.EntityManagerFactory;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.holonplatform.core.datastore.Datastore;
import com.holonplatform.datastore.jpa.JpaDatastore;
import com.holonplatform.jpa.spring.boot.internal.JpaDatastoreAutoConfigurationRegistrar;
import com.holonplatform.spring.EnableDatastoreConfiguration;

/**
 * Spring boot auto-configuration to enable JPA {@link Datastore} beans.
 * 
 * @since 5.0.0
 */
@Configuration
@ConditionalOnClass(JpaDatastore.class)
@AutoConfigureAfter({ HibernateJpaAutoConfiguration.class, JpaAutoConfiguration.class })
public class JpaDatastoreAutoConfiguration {

	@Configuration
	@ConditionalOnMissingBean({ JpaDatastore.class })
	@ConditionalOnSingleCandidate(EntityManagerFactory.class)
	@EnableDatastoreConfiguration
	@Import(JpaDatastoreAutoConfigurationRegistrar.class)
	static class JpaDatastoreConfiguration {

	}

}
