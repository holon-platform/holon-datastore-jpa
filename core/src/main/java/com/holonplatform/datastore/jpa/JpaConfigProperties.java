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
package com.holonplatform.datastore.jpa;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;

import com.holonplatform.core.config.ConfigProperty;
import com.holonplatform.core.config.ConfigPropertySet;
import com.holonplatform.core.datastore.DataContextBound;
import com.holonplatform.core.internal.config.DefaultConfigPropertySet;
import com.holonplatform.core.internal.utils.ClassUtils;
import com.holonplatform.core.query.Query;
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
	 * {@link Query} parameter to set lock mode (use {@link Query#parameter(String, Object)} to set query parameters).
	 * <p>
	 * Value must be {@link LockModeType} enum value.
	 * </p>
	 */
	public static final String QUERY_PARAMETER_LOCK_MODE = "jpaQueryLockMode";

	/**
	 * Enumeration for common ORM platforms
	 */
	public enum ORMPlatform {

		HIBERNATE("org.hibernate.Session", "org.hibernate.ejb.HibernateEntityManager"),

		ECLIPSELINK("org.eclipse.persistence.jpa.JpaEntityManager"),

		OPENJPA("org.apache.openjpa.persistence.OpenJPAEntityManager"),

		DATANUCLEUS("org.datanucleus.jpa.EntityManagerImpl", "org.datanucleus.ObjectManager",
				"org.datanucleus.ObjectManagerImpl"),

		BATOO("org.batoo.jpa.core.impl.manager.EntityManagerImpl");

		/*
		 * EntitManager delegates class names
		 */
		private final List<String> delegates;

		/**
		 * @param delegates
		 */
		private ORMPlatform(String... delegates) {
			this.delegates = Arrays.asList(delegates);
		}

		/**
		 * EntitManager delegates class names
		 * @return Delegates class names
		 */
		public List<String> getDelegates() {
			return delegates;
		}

		/**
		 * Try to detect ORM platform to use form classpath
		 * @return Detected ORMPlatform, or <code>null</code> if not found
		 * @throws IllegalStateException If more than one ORM provide is found in classpath
		 */
		public static ORMPlatform detectFromClasspath() throws IllegalStateException {
			ORMPlatform platform = null;
			for (ORMPlatform p : values()) {
				for (String className : p.getDelegates()) {
					try {
						Class.forName(className);
						if (platform != null) {
							throw new IllegalStateException(
									"More than one ORM provider found in classpath: " + platform + ", " + p);
						}
						platform = p;
					} catch (@SuppressWarnings("unused") Exception e) {
						// not found
					}
				}
			}
			return platform;
		}

		/**
		 * Determines the ORMPlatform from the given {@link EntityManager}
		 * @param em EntityManager (must be not null)
		 * @return Resolved ORMPlatform. If none of know platforms match, <code>null</code> is returned
		 */
		public static ORMPlatform resolve(EntityManager em) {
			if (em == null) {
				throw new IllegalArgumentException("Null EntityManager");
			}
			for (ORMPlatform platform : values()) {
				for (String entityManagerClassName : platform.getDelegates()) {

					if (isEntityManagerOfType(em, entityManagerClassName)) {
						return platform;
					}
				}
			}
			return null;
		}

		/**
		 * Returns whether the given {@link EntityManager} is of the given type.
		 * @param em EntityManager (must be not null)
		 * @param type the fully qualified expected EntityManager type
		 * @return True if type match
		 */
		private static boolean isEntityManagerOfType(EntityManager em, String type) {
			try {
				Class<?> emType = ClassUtils.forName(type, em.getDelegate().getClass().getClassLoader());
				if (emType.isAssignableFrom(em.getDelegate().getClass())) {
					return true;
				}
			} catch (@SuppressWarnings("unused") Exception e) {
				// ignore
			}
			return false;
		}

	}

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
			if (dataContextId == null) {
				// add alias for spring config
				addAliasName("spring.jpa");
			}
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
