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

import java.util.Optional;

import com.holonplatform.core.config.ConfigProperty;
import com.holonplatform.core.config.ConfigPropertySet;
import com.holonplatform.core.datastore.DataContextBound;
import com.holonplatform.core.internal.config.DefaultConfigPropertySet;
import com.holonplatform.datastore.jpa.ORMPlatform;
import com.holonplatform.jdbc.DatabasePlatform;

/**
 * A {@link ConfigPropertySet} for JPA configuration.
 *
 * @since 5.0.0
 */
public interface JpaConfigProperties extends ConfigPropertySet, DataContextBound {

	/**
	 * Configuration property set default name
	 */
	static final String DEFAULT_NAME = "holon.jpa";

	/**
	 * Whether to instruct JPA ORM engine to show executed SQL statements, if supported by concrete ORM implementation.
	 */
	static final ConfigProperty<Boolean> SHOW_SQL = ConfigProperty.create("show-sql", Boolean.class);

	/**
	 * Whether to initialize the schema on startup.
	 */
	static final ConfigProperty<Boolean> GENERATE_DDL = ConfigProperty.create("generate-ddl", Boolean.class);

	/**
	 * Set JPA ORM dialect class name to use, if supported by concrete ORM implementation.
	 */
	static final ConfigProperty<String> DIALECT = ConfigProperty.create("dialect", String.class);

	/**
	 * Set the database platform to use. Must be one of the names enumerated in {@link DatabasePlatform}.
	 */
	static final ConfigProperty<DatabasePlatform> DATABASE = ConfigProperty.create("database", DatabasePlatform.class);

	/**
	 * Set JPA ORM platform to use. Must be one of the names enumerated in {@link ORMPlatform}.
	 */
	static final ConfigProperty<ORMPlatform> ORM_PLATFORM = ConfigProperty.create("orm", ORMPlatform.class);

	/**
	 * Builder to create property set instances bound to a property data source.
	 * @param dataContextId Optional data context id to which DataSource is bound
	 * @return ConfigPropertySet builder
	 */
	static Builder<JpaConfigProperties> builder(String dataContextId) {
		return new DefaultConfigPropertySet.DefaultBuilder<>(new JpaConfigPropertiesImpl(dataContextId));
	}

	/**
	 * Builder to create property set instances bound to a property data source, without data context id specification.
	 * @return ConfigPropertySet builder
	 */
	static Builder<JpaConfigProperties> builder() {
		return new DefaultConfigPropertySet.DefaultBuilder<>(new JpaConfigPropertiesImpl(null));
	}

	/**
	 * Default implementation
	 */
	static class JpaConfigPropertiesImpl extends DefaultConfigPropertySet implements JpaConfigProperties {

		private final String dataContextId;

		public JpaConfigPropertiesImpl(String dataContextId) {
			super((dataContextId != null && !dataContextId.trim().equals("")) ? (DEFAULT_NAME + "." + dataContextId)
					: DEFAULT_NAME);
			this.dataContextId = (dataContextId != null && !dataContextId.trim().equals("")) ? dataContextId : null;
		}

		/*
		 * (non-Javadoc)
		 * @see com.holonplatform.core.datastore.DataContextBound#getDataContextId()
		 */
		@Override
		public Optional<String> getDataContextId() {
			return Optional.ofNullable(dataContextId);
		}

	}

}
