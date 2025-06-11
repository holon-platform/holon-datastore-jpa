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
package com.holonplatform.datastore.jpa.dialect;

import java.lang.reflect.Method;
import java.util.Optional;

import jakarta.persistence.PersistenceException;

import org.hibernate.PessimisticLockException;
import org.hibernate.dialect.lock.LockingStrategyException;

import com.holonplatform.core.exceptions.DataAccessException;
import com.holonplatform.core.internal.Logger;
import com.holonplatform.core.internal.utils.ClassUtils;
import com.holonplatform.core.query.lock.LockAcquisitionException;
import com.holonplatform.datastore.jpa.internal.JpaDatastoreLogger;

/**
 * Hibernate {@link ORMDialect}.
 *
 * @since 5.1.0
 */
public class HibernateDialect implements ORMDialect {

	private static final Logger LOGGER = JpaDatastoreLogger.create();

	private int supportedJPAMajorVersion = 2;
	private int supportedJPAMinorVersion = 1;

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jpa.dialect.ORMDialect#init(com.holonplatform.datastore.jpa.context.
	 * ORMDialectContext)
	 */
	@Override
	public void init(ORMDialectContext context) {
		try {
			String version = context.withEntityManager(em -> {
				try {
					Class<?> versionCls = ClassUtils.forName("org.hibernate.Version",
							em.getDelegate().getClass().getClassLoader());
					Method m = versionCls.getDeclaredMethod("getVersionString");
					return (String) m.invoke(null);
				} catch (Exception e) {
					LOGGER.warn("Failed to detect Hibernate version", e);
					return null;
				}
			});

			int majorVersion = -1;
			int minorVersion = -1;

			int dix = version.indexOf('.');
			if (dix > -1) {
				majorVersion = Integer.parseInt(version.substring(0, dix));
				String minor = version.substring(dix + 1);
				minorVersion = Integer.parseInt(minor.substring(0, minor.indexOf('.')));
			}

			if (majorVersion > -1 && minorVersion > -1) {
				if (majorVersion <= 3) {
					if (minorVersion >= 5) {
						supportedJPAMajorVersion = 2;
						supportedJPAMinorVersion = 0;
					} else {
						supportedJPAMajorVersion = 1;
						supportedJPAMinorVersion = 0;
					}
				} else if (majorVersion == 4) {
					if (minorVersion < 3) {
						supportedJPAMajorVersion = 2;
						supportedJPAMinorVersion = 0;
					} else {
						supportedJPAMajorVersion = 2;
						supportedJPAMinorVersion = 1;
					}
				} else if (majorVersion > 4) {
					if (minorVersion < 3) {
						supportedJPAMajorVersion = 2;
						supportedJPAMinorVersion = 1;
					} else {
						supportedJPAMajorVersion = 2;
						supportedJPAMinorVersion = 2;
					}
				}
			}

		} catch (Exception e) {
			LOGGER.warn("Failed to detect Hibernate version", e);
		}

	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jpa.dialect.ORMDialect#getSupportedJPAMajorVersion()
	 */
	@Override
	public int getSupportedJPAMajorVersion() {
		return supportedJPAMajorVersion;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jpa.dialect.ORMDialect#getSupportedJPAMinorVersion()
	 */
	@Override
	public int getSupportedJPAMinorVersion() {
		return supportedJPAMinorVersion;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jpa.dialect.ORMDialect#isTupleSupported()
	 */
	@Override
	public boolean isTupleSupported() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jpa.dialect.ORMDialect#getBatchSizeConfigurationProperty()
	 */
	@Override
	public Optional<String> getBatchSizeConfigurationProperty() {
		return Optional.of("hibernate.jdbc.batch_size");
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jpa.dialect.ORMDialect#updateStatementAliasSupported()
	 */
	@Override
	public boolean updateStatementAliasSupported() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jpa.dialect.ORMDialect#updateStatementSetAliasSupported()
	 */
	@Override
	public boolean updateStatementSetAliasSupported() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jpa.dialect.ORMDialect#deleteStatementAliasSupported()
	 */
	@Override
	public boolean deleteStatementAliasSupported() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.holonplatform.datastore.jpa.dialect.ORMDialect#translateException(jakarta.persistence.PersistenceException)
	 */
	@Override
	public DataAccessException translateException(PersistenceException exception) {
		if (org.hibernate.exception.LockAcquisitionException.class.isAssignableFrom(exception.getClass())) {
			return new LockAcquisitionException("Failed to acquire lock", exception);
		}
		if (LockingStrategyException.class.isAssignableFrom(exception.getClass())) {
			return new LockAcquisitionException("Failed to acquire lock", exception);
		}
		if (PessimisticLockException.class.isAssignableFrom(exception.getClass())) {
			return new LockAcquisitionException("Failed to acquire lock", exception);
		}
		return ORMDialect.super.translateException(exception);
	}

}
