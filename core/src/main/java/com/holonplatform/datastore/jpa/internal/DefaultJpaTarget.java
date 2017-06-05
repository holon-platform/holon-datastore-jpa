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
package com.holonplatform.datastore.jpa.internal;

import com.holonplatform.core.internal.datastore.DefaultDataTarget;
import com.holonplatform.datastore.jpa.JpaTarget;

/**
 * Default {@link JpaTarget} implementation
 *
 * @since 4.5.0
 */
public class DefaultJpaTarget<T> extends DefaultDataTarget<T> implements JpaTarget<T> {

	private static final long serialVersionUID = 1979070987995081298L;

	/**
	 * Constructor
	 * @param entityClass Entity class
	 */
	public DefaultJpaTarget(Class<? extends T> entityClass) {
		super(JpaDatastoreUtils.getEntityName(entityClass), entityClass);
	}

}
