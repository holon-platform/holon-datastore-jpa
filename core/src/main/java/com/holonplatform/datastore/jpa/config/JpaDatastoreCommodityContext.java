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
package com.holonplatform.datastore.jpa.config;

import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import com.holonplatform.core.datastore.DatastoreCommodityContext;
import com.holonplatform.datastore.jpa.JpaDatastore;
import com.holonplatform.datastore.jpa.ORMPlatform;

/**
 * JPA Datastore {@link DatastoreCommodityContext}.
 *
 * @since 5.0.0
 */
public interface JpaDatastoreCommodityContext extends DatastoreCommodityContext, JpaDatastore {

	/**
	 * Get the EntityManagerFactory.
	 * @return The EntityManagerFactory
	 */
	EntityManagerFactory getEntityManagerFactory();

	/**
	 * Obtain an {@link EntityManager} instance using configured {@link EntityManagerInitializer}.
	 * @return A new {@link EntityManager} instance
	 */
	EntityManager getEntityManager();

	/**
	 * Get the ORM platform, if detected.
	 * @return Optional ORM platform
	 */
	Optional<ORMPlatform> getORMPlatform();

	/**
	 * Checks whether to auto-flush mode is enabled. When auto-flush mode is enabled, {@link EntityManager#flush()} is
	 * called after each Datastore data manipulation operation, such as <code>save</code> or <code>delete</code>.
	 * @return <code>true</code> if auto-flush mode is enabled, <code>false</code> otherwise
	 */
	boolean isAutoFlush();

	/**
	 * Get whether to trace Datastore operations.
	 * @return the trace <code>true</code> if tracing is enabled, <code>false</code> otherwise
	 */
	boolean isTraceEnabled();

}
