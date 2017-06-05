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
package com.holonplatform.jpa.spring;

import javax.persistence.EntityManager;

import com.holonplatform.datastore.jpa.JpaDatastore;
import com.holonplatform.jpa.spring.internal.DefaultSpringJpaDatastore;
import com.holonplatform.spring.DatastoreCommodityFactory;
import com.holonplatform.spring.DatastoreResolver;

/**
 * Spring-enabled {@link JpaDatastore}.
 * <p>
 * This {@link JpaDatastore} uses Spring's shared {@link EntityManager} architecture to provide seamless integration
 * with Spring transaction management and JPA support.
 * </p>
 * <p>
 * Supports {@link DatastoreResolver} and {@link DatastoreCommodityFactory} annotated beans automatic registration.
 * </p>
 * 
 * @since 5.0.0
 */
public interface SpringJpaDatastore extends JpaDatastore {

	/**
	 * Get a builder to create a {@link SpringJpaDatastore} instance.
	 * @return Datastore builder
	 */
	static Builder<SpringJpaDatastore> builder() {
		return new DefaultSpringJpaDatastore.Builder();
	}

}
