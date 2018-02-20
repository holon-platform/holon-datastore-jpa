/*
 * Copyright 2016-2018 Axioma srl.
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
package com.holonplatform.datastore.jpa.test.config;

import com.holonplatform.core.datastore.DatastoreCommodity;
import com.holonplatform.core.datastore.DatastoreCommodityContext.CommodityConfigurationException;
import com.holonplatform.datastore.jpa.ORMPlatform;
import com.holonplatform.datastore.jpa.config.JpaDatastoreCommodityContext;
import com.holonplatform.datastore.jpa.config.JpaDatastoreCommodityFactory;
import com.holonplatform.datastore.jpa.dialect.ORMDialect;

public class DatastoreConfigCommodity implements DatastoreCommodity {

	private static final long serialVersionUID = 8478847929033283098L;

	public static final JpaDatastoreCommodityFactory<DatastoreConfigCommodity> FACTORY = new DatabasePlatformCommodityFactory();

	private final ORMPlatform platform;
	private final ORMDialect dialect;

	public DatastoreConfigCommodity(ORMPlatform platform, ORMDialect dialect) {
		super();
		this.platform = platform;
		this.dialect = dialect;
	}

	public ORMPlatform getPlatform() {
		return platform;
	}

	public ORMDialect getDialect() {
		return dialect;
	}

	@SuppressWarnings("serial")
	public static final class DatabasePlatformCommodityFactory
			implements JpaDatastoreCommodityFactory<DatastoreConfigCommodity> {

		@Override
		public Class<? extends DatastoreConfigCommodity> getCommodityType() {
			return DatastoreConfigCommodity.class;
		}

		@Override
		public DatastoreConfigCommodity createCommodity(JpaDatastoreCommodityContext context)
				throws CommodityConfigurationException {
			return new DatastoreConfigCommodity(context.getORMPlatform().orElse(null), context.getDialect());
		}

	}

}
