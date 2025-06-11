/*
 * Copyright 2016-2018 Axioma srl.
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
package com.holonplatform.jpa.spring.internal;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import org.springframework.orm.jpa.SharedEntityManagerCreator;

import com.holonplatform.datastore.jpa.JpaDatastore.EntityManagerFinalizer;
import com.holonplatform.datastore.jpa.JpaDatastore.EntityManagerInitializer;
import com.holonplatform.jpa.spring.SpringEntityManagerLifecycleHandler;

/**
 * An {@link EntityManagerInitializer} and {@link EntityManagerFinalizer} for Spring integration, which uses
 * {@link SharedEntityManagerCreator} to provide a Spring managed {@link EntityManager} proxy.
 *
 * @since 5.2.0
 */
public class DefaultSpringEntityManagerLifecycleHandler implements SpringEntityManagerLifecycleHandler {

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jpa.JpaDatastore.EntityManagerInitializer#getEntityManager(jakarta.persistence.
	 * EntityManagerFactory)
	 */
	@Override
	public EntityManager getEntityManager(EntityManagerFactory entityManagerFactory) {
		return SharedEntityManagerCreator.createSharedEntityManager(entityManagerFactory);
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jpa.JpaDatastore.EntityManagerFinalizer#finalizeEntityManager(jakarta.persistence.
	 * EntityManager)
	 */
	@Override
	public void finalizeEntityManager(EntityManager entityManager) {
		// noop
	}

}
