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
package com.holonplatform.jpa.spring.internal;

import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import javax.persistence.SharedCacheMode;
import javax.persistence.ValidationMode;

import org.eclipse.persistence.config.TargetDatabase;
import org.hibernate.dialect.DB2400Dialect;
import org.hibernate.dialect.DB2Dialect;
import org.hibernate.dialect.DerbyTenSevenDialect;
import org.hibernate.dialect.H2Dialect;
import org.hibernate.dialect.HANARowStoreDialect;
import org.hibernate.dialect.HSQLDialect;
import org.hibernate.dialect.InformixDialect;
import org.hibernate.dialect.MySQL5Dialect;
import org.hibernate.dialect.Oracle10gDialect;
import org.hibernate.dialect.PostgreSQL82Dialect;
import org.hibernate.dialect.SQLServerDialect;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.AbstractJpaVendorAdapter;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.EclipseLinkJpaVendorAdapter;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.orm.jpa.vendor.OpenJpaVendorAdapter;

import com.holonplatform.core.internal.Logger;
import com.holonplatform.core.internal.utils.ObjectUtils;
import com.holonplatform.datastore.jpa.ORMPlatform;
import com.holonplatform.datastore.jpa.internal.JpaDatastoreLogger;
import com.holonplatform.jdbc.DatabasePlatform;

/**
 * Configurator for EntityManagerFactory beans
 * 
 * @since 5.0.0
 */
class EntityManagerFactoryConfigurator {

	/*
	 * Logger
	 */
	private static final Logger logger = JpaDatastoreLogger.create();

	/*
	 * Data context id
	 */
	protected String dataContextId;
	/*
	 * Entity packages to scan
	 */
	protected final List<String> entityPackages = new LinkedList<>();
	/*
	 * Database platform
	 */
	protected DatabasePlatform database;
	/*
	 * Dialect class name
	 */
	protected String dialect;
	/*
	 * Initialize schema on startup
	 */
	protected boolean generateDdl = false;
	/*
	 * Show sql
	 */
	protected boolean showSql = false;
	/*
	 * Validation mode
	 */
	protected ValidationMode validationMode;
	/*
	 * Sahred cache mode
	 */
	protected SharedCacheMode sharedCacheMode;
	/*
	 * ORM platform
	 */
	protected ORMPlatform ormPlatform;
	/*
	 * Vendor-specific jpa properties
	 */
	protected Properties jpaProperties;

	/**
	 * Set data context id. Data context id will be setted as EntityManagerFactory <code>PersistenceUnit</code> name
	 * too.
	 * @param dataContextId Data context id
	 */
	public void setDataContextId(String dataContextId) {
		this.dataContextId = dataContextId;
	}

	/**
	 * Add a package name to scan for automatic JPA Entity declation for Persistence Unit bound to EntityManagerFactory.
	 * This way, entity classes declaration in <code>persistence.xml</code> file can be omitted.
	 * @param packageName Package name to scan to search for Entity classes
	 */
	public void addEntityPackage(String packageName) {
		ObjectUtils.argumentNotNull(packageName, "Package name must be not null");
		entityPackages.add(packageName.trim());
	}

	/**
	 * Add a package name to scan for automatic JPA Entity declation for Persistence Unit bound to EntityManagerFactory,
	 * using the package name to which given <code>referenceClass</code> is bound
	 * @param referenceClass Reference class to obtain the package name
	 */
	public void addEntityPackage(Class<?> referenceClass) {
		ObjectUtils.argumentNotNull(referenceClass, "Reference class must be not null");
		Package pkg = referenceClass.getPackage();
		if (pkg == null || pkg.getName() == null) {
			throw new IllegalArgumentException(
					"Package of class " + referenceClass.getName() + " is null or has a null name");
		}
		addEntityPackage(pkg.getName());
	}

	/**
	 * Set target Database platform. If not specified, builder will try to detect Database platform from DataSource
	 * connection settings
	 * @param database Database platform
	 */
	public void setDatabase(DatabasePlatform database) {
		this.database = database;
	}

	/**
	 * Set ORM vendor-specific dialect class to use for underlying Database access. If not specified, builder will try
	 * to detect appropriate dialect to use from Database platform or DataSource connection.
	 * @param dialect Vendor-specific dialect class
	 */
	public void setDialect(String dialect) {
		this.dialect = dialect;
	}

	/**
	 * Set whether to show SQL in the log
	 * @param showSql True to show sql
	 */
	public void setShowSql(boolean showSql) {
		this.showSql = showSql;
	}

	/**
	 * Sets whether to initialize schema on startup
	 * @param generateDdl <code>true</code> to initialize schema on startup
	 */
	public void setGenerateDdl(boolean generateDdl) {
		this.generateDdl = generateDdl;
	}

	/**
	 * Specify the JPA 2.0 validation mode for this persistence unit, overriding a value in {@code persistence.xml} if
	 * set.
	 * @param validationMode Validation mode
	 */
	public void setValidationMode(ValidationMode validationMode) {
		this.validationMode = validationMode;
	}

	/**
	 * Specify the JPA 2.0 shared cache mode for this persistence unit, overriding a value in {@code persistence.xml} if
	 * set.
	 * @param sharedCacheMode Shared cache mode
	 */
	public void setSharedCacheMode(SharedCacheMode sharedCacheMode) {
		this.sharedCacheMode = sharedCacheMode;
	}

	/**
	 * Set the ORM platform to use
	 * @param ormPlatform ORM platform to use
	 */
	public void setOrmPlatform(ORMPlatform ormPlatform) {
		this.ormPlatform = ormPlatform;
	}

	/**
	 * Add vendor-specific JPA property, usually loaded from <code>persistence.xml</code> file.
	 * @param name Property name
	 * @param value Property value
	 */
	public void addJpaProperty(String name, Object value) {
		if (name != null) {
			if (jpaProperties == null) {
				jpaProperties = new Properties();
			}
			jpaProperties.put(name, value);
		}
	}

	/**
	 * Configure given bean using provided configuration settings
	 * @param emf Bean to configure
	 */
	public void configure(LocalContainerEntityManagerFactoryBean emf) {

		if (dataContextId != null) {
			emf.setPersistenceUnitName(dataContextId);
		}

		if (!entityPackages.isEmpty()) {
			emf.setPackagesToScan(entityPackages.toArray(new String[0]));
		}

		if (validationMode != null) {
			emf.setValidationMode(validationMode);
		}

		if (sharedCacheMode != null) {
			emf.setSharedCacheMode(sharedCacheMode);
		}

		if (jpaProperties != null) {
			emf.setJpaProperties(jpaProperties);
		}

		// ORM adapter
		ORMPlatform orm = ormPlatform;
		if (orm == null) {
			// try to detect
			try {
				orm = ORMPlatform.detectFromClasspath();
			} catch (IllegalStateException e) {
				logger.warn("Ensure to declare ORM platfrom through property or persistence.xml", e);
			}
		}

		if (orm != null) {

			JpaVendorAdapter adapter = buildJpaVendorAdapter(orm);

			if (adapter != null) {
				emf.setJpaVendorAdapter(adapter);
			} else {
				logger.warn("Failed to detect JpaVendorAdapter to use. "
						+ "Delegating adapter setup to LocalContainerEntityManagerFactoryBean");
			}

		} else {
			logger.warn("Failed to detect ORM platform to use. "
					+ "Delegating JpaVendorAdapter setup to LocalContainerEntityManagerFactoryBean");
		}
	}

	/**
	 * Build the JPA ORM vendor adapter
	 * @param orm ORM platform
	 * @return JpaVendorAdapter
	 */
	protected JpaVendorAdapter buildJpaVendorAdapter(ORMPlatform orm) {
		AbstractJpaVendorAdapter adapter = null;
		switch (orm) {
		case DATANUCLEUS:
			break;
		case ECLIPSELINK:
			adapter = new EclipseLinkJpaVendorAdapter();
			break;
		case HIBERNATE:
			adapter = new HibernateJpaVendorAdapter();
			break;
		case OPENJPA:
			adapter = new OpenJpaVendorAdapter();
			break;
		default:
			break;
		}

		if (adapter != null) {

			adapter.setShowSql(showSql);

			adapter.setGenerateDdl(generateDdl);

			// dialect

			if (dialect != null) {
				adapter.setDatabasePlatform(dialect);
			} else {
				// try to auto-detect
				if (database != null) {
					boolean dialectSetted = false;
					if (orm == ORMPlatform.HIBERNATE) {
						Class<?> dc = hibernateDatabaseDialectClass(database);
						if (dc != null) {
							adapter.setDatabasePlatform(dc.getName());
							dialectSetted = true;
						}
					} else if (orm == ORMPlatform.ECLIPSELINK) {
						String tdb = eclipselinkTargetDatabaseName(database);
						if (tdb != null) {
							adapter.setDatabasePlatform(tdb);
							dialectSetted = true;
						}
					}

					if (!dialectSetted) {
						adapter.setDatabase(convertToSpringVendor(database));
					}
				}
			}

		}

		return adapter;
	}

	private static Class<?> hibernateDatabaseDialectClass(DatabasePlatform database) {
		switch (database) {
		case DB2:
			return DB2Dialect.class;
		case DB2_AS400:
			return DB2400Dialect.class;
		case DERBY:
			return DerbyTenSevenDialect.class;
		case H2:
			return H2Dialect.class;
		case HSQL:
			return HSQLDialect.class;
		case INFORMIX:
			return InformixDialect.class;
		case MYSQL:
			return MySQL5Dialect.class;
		case ORACLE:
			return Oracle10gDialect.class;
		case POSTGRESQL:
			return PostgreSQL82Dialect.class;
		case SQL_SERVER:
			return SQLServerDialect.class;
		case MARIADB:
			return MySQL5Dialect.class;
		case HANA:
			return HANARowStoreDialect.class;
		case SQLITE:
		case NONE:
		default:
			break;
		}
		return null;
	}

	private static String eclipselinkTargetDatabaseName(DatabasePlatform database) {
		switch (database) {
		case DB2:
			return TargetDatabase.DB2;
		case DB2_AS400:
			return TargetDatabase.DB2;
		case DERBY:
			return TargetDatabase.Derby;
		case HSQL:
			return TargetDatabase.HSQL;
		case INFORMIX:
			return TargetDatabase.Informix;
		case MYSQL:
			return TargetDatabase.MySQL4;
		case ORACLE:
			return TargetDatabase.Oracle;
		case POSTGRESQL:
			return TargetDatabase.PostgreSQL;
		case SQL_SERVER:
			return TargetDatabase.SQLServer;
		case HANA:
			return TargetDatabase.HANA;
		case H2:
		case MARIADB:
		case SQLITE:
		case NONE:
		default:
			break;
		}
		return TargetDatabase.Auto;
	}

	private static Database convertToSpringVendor(DatabasePlatform database) {
		switch (database) {
		case DB2:
			return Database.DB2;
		case DB2_AS400:
			return Database.DB2;
		case DERBY:
			return Database.DERBY;
		case H2:
			return Database.H2;
		case HSQL:
			return Database.HSQL;
		case INFORMIX:
			return Database.INFORMIX;
		case MARIADB:
			return Database.MYSQL;
		case MYSQL:
			return Database.MYSQL;
		case ORACLE:
			return Database.ORACLE;
		case POSTGRESQL:
			return Database.POSTGRESQL;
		case SQL_SERVER:
			return Database.SQL_SERVER;
		case HANA:
		case SQLITE:
		case NONE:
		default:
			break;
		}
		return null;
	}

}
