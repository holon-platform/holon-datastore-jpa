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
package com.holonplatform.jpa.spring;

import java.util.Optional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import org.springframework.transaction.annotation.Transactional;

import com.holonplatform.core.config.ConfigProperty;
import com.holonplatform.core.config.ConfigPropertySet;
import com.holonplatform.core.datastore.DataContextBound;
import com.holonplatform.core.datastore.Datastore;
import com.holonplatform.core.internal.config.DefaultConfigPropertySet;

/**
 * A {@link ConfigPropertySet} for JPA Datastore configuration, using {@link #DEFAULT_NAME} as property prefix.
 *
 * @since 5.1.0
 */
public interface JpaDatastoreConfigProperties extends ConfigPropertySet, DataContextBound {

	/**
	 * Configuration property set default name
	 */
	static final String DEFAULT_NAME = "holon.datastore.jpa";

	/**
	 * Whether to qualify the Datastore bean as <code>primary</code>, i.e. the preferential bean to be injected in a
	 * single-valued dependency when multiple candidates are present.
	 * <p>
	 * By default, the registred Datastore bean is marked as primary only when the {@link EntityManagerFactory} bean to
	 * which is bound is registered as primary candidate bean.
	 * </p>
	 */
	static final ConfigProperty<Boolean> PRIMARY = ConfigProperty.create("primary", Boolean.class);

	/**
	 * Whether to enable the {@link EntityManager} auto-flush mode. When auto-flush is enabled, the
	 * {@link EntityManager#flush()} method is invoked after the execution of any Datastore data manipulation operation.
	 */
	static final ConfigProperty<Boolean> AUTO_FLUSH = ConfigProperty.create("auto-flush", Boolean.class);

	/**
	 * Whether to add {@link Transactional} behaviour to transactional Datastore methods, to automatically create or
	 * partecipate in a transaction when methods are invoked. Affected methods are: <code>refresh</code>,
	 * <code>insert</code>, <code>update</code>, <code>save</code>, <code>delete</code>.
	 */
	static final ConfigProperty<Boolean> TRANSACTIONAL = ConfigProperty.create("transactional", Boolean.class);

	/**
	 * Builder to create property set instances bound to a property data source.
	 * @param dataContextId Optional data context id to which {@link Datastore} is bound
	 * @return ConfigPropertySet builder
	 */
	static Builder<JpaDatastoreConfigProperties> builder(String dataContextId) {
		return new DefaultConfigPropertySet.DefaultBuilder<>(new JpaDatastoreConfigPropertiesImpl(dataContextId));
	}

	/**
	 * Builder to create property set instances bound to a property data source, without data context id specification.
	 * @return ConfigPropertySet builder
	 */
	static Builder<JpaDatastoreConfigProperties> builder() {
		return new DefaultConfigPropertySet.DefaultBuilder<>(new JpaDatastoreConfigPropertiesImpl(null));
	}

	/**
	 * Default implementation
	 */
	static class JpaDatastoreConfigPropertiesImpl extends DefaultConfigPropertySet
			implements JpaDatastoreConfigProperties {

		private final String dataContextId;

		public JpaDatastoreConfigPropertiesImpl(String dataContextId) {
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
