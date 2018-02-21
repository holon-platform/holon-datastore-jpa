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

import java.lang.reflect.Field;

import com.holonplatform.core.internal.Logger;
import com.holonplatform.core.internal.utils.ClassUtils;
import com.holonplatform.datastore.jpa.internal.JpaDatastoreLogger;

/**
 * OpenJPA {@link ORMDialect}.
 *
 * @since 5.1.0
 */
public class OpenJPADialect implements ORMDialect {

	private static final Logger LOGGER = JpaDatastoreLogger.create();

	private int supportedJPAMajorVersion = 2;
	private int supportedJPAMinorVersion = 0;

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jpa.dialect.ORMDialect#init(com.holonplatform.datastore.jpa.context.
	 * ORMDialectContext)
	 */
	@Override
	public void init(ORMDialectContext context) {

		int majorRelease = context.withEntityManager(em -> {
			try {
				Class<?> versionCls = ClassUtils.forName("org.apache.openjpa.conf.OpenJPAVersion",
						em.getDelegate().getClass().getClassLoader());
				Field f = versionCls.getDeclaredField("MAJOR_RELEASE");
				return f.getInt(null);
			} catch (Exception e) {
				LOGGER.warn("Failed to detect OpenJPA version", e);
				return -1;
			}
		});

		if (majorRelease > -1) {
			if (majorRelease < 2) {
				supportedJPAMajorVersion = 1;
			} else {
				supportedJPAMajorVersion = 2;
			}
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
	 * @see com.holonplatform.datastore.jpa.dialect.ORMDialect#temporalTypeParametersSupported()
	 */
	@Override
	public boolean temporalTypeParametersSupported() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jpa.dialect.ORMDialect#temporalTypeProjectionSupported()
	 */
	@Override
	public boolean temporalTypeProjectionSupported() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jpa.dialect.ORMDialect#isTupleSupported()
	 */
	@Override
	public boolean isTupleSupported() {
		return false;
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

}
