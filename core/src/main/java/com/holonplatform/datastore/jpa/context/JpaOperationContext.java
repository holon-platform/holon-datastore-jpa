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
package com.holonplatform.datastore.jpa.context;

import jakarta.persistence.EntityManager;

import com.holonplatform.core.beans.BeanIntrospector;
import com.holonplatform.core.datastore.DatastoreCommodityHandler;

/**
 * JPA datastore operations execution context.
 *
 * @since 5.1.0
 */
public interface JpaOperationContext extends JpaContext, EntityManagerHandler, DatastoreCommodityHandler {

	/**
	 * Get the {@link BeanIntrospector} to use to introspect entity beans.
	 * @return BeanIntrospector (not null)
	 */
	BeanIntrospector getBeanIntrospector();

	/**
	 * Checks whether to auto-flush mode is enabled. When auto-flush mode is enabled, {@link EntityManager#flush()} is
	 * called after each Datastore data manipulation operation, such as <code>save</code> or <code>delete</code>.
	 * @return <code>true</code> if auto-flush mode is enabled, <code>false</code> otherwise
	 */
	boolean isAutoFlush();

}
