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
package com.holonplatform.datastore.jpa.internal;

import com.holonplatform.core.internal.utils.ObjectUtils;
import com.holonplatform.datastore.jpa.JpaQueryHint;

/**
 * Default {@link JpaQueryHint} implementation.
 * 
 * @since 5.0.0
 */
public class DefaultQueryHint implements JpaQueryHint {

	private static final long serialVersionUID = 6007343114091165488L;

	private final String name;
	private final Object value;

	/**
	 * Costructor
	 * @param name Hint name (not null)
	 * @param value Hint value (not null)
	 */
	public DefaultQueryHint(String name, Object value) {
		super();
		ObjectUtils.argumentNotNull(name, "Hint name must be not null");
		ObjectUtils.argumentNotNull(value, "Hint value must be not null");
		this.name = name;
		this.value = value;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jpa.JpaDatastore.QueryHint#getName()
	 */
	@Override
	public String getName() {
		return name;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jpa.JpaDatastore.QueryHint#getValue()
	 */
	@Override
	public Object getValue() {
		return value;
	}

}
