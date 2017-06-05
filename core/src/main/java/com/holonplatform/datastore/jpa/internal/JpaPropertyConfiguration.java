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

import java.io.Serializable;

import com.holonplatform.core.config.ConfigProperty;
import com.holonplatform.core.property.Property;

/**
 * JPA {@link Property} configuration attributes.
 *
 * @since 5.0.0
 */
public final class JpaPropertyConfiguration implements Serializable {

	private static final long serialVersionUID = 7762835057082096136L;

	/**
	 * Database column name
	 */
	public static final ConfigProperty<String> COLUMN_NAME = ConfigProperty
			.create("com.holonplatform.datastore.jpa.property.configuration.COLUMN_NAME", String.class);

	private JpaPropertyConfiguration() {
	}

}
