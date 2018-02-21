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

/**
 * Default {@link ORMDialect}.
 *
 * @since 5.1.0
 */
public class DefaultDialect implements ORMDialect {

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jpa.dialect.ORMDialect#init(com.holonplatform.datastore.jpa.context.
	 * ORMDialectContext)
	 */
	@Override
	public void init(ORMDialectContext context) {
	}

	/* (non-Javadoc)
	 * @see com.holonplatform.datastore.jpa.dialect.ORMDialect#getSupportedJPAMajorVersion()
	 */
	@Override
	public int getSupportedJPAMajorVersion() {
		return 2;
	}

	/* (non-Javadoc)
	 * @see com.holonplatform.datastore.jpa.dialect.ORMDialect#getSupportedJPAMinorVersion()
	 */
	@Override
	public int getSupportedJPAMinorVersion() {
		return 1;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jpa.dialect.ORMDialect#isTupleSupported()
	 */
	@Override
	public boolean isTupleSupported() {
		return false;
	}

}
