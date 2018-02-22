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
package com.holonplatform.jpa.spring.boot.internal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.domain.EntityScanPackages;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;

import com.holonplatform.core.datastore.Datastore;
import com.holonplatform.datastore.jpa.JpaDatastore;
import com.holonplatform.jdbc.spring.internal.DataSourceFactoryBean;
import com.holonplatform.jpa.spring.EnableJpa;
import com.holonplatform.jpa.spring.EnableJpaDatastore;
import com.holonplatform.jpa.spring.JpaDatastoreConfigProperties;
import com.holonplatform.jpa.spring.boot.JpaEntityScan;
import com.holonplatform.jpa.spring.internal.JpaDatastoreRegistrar;
import com.holonplatform.jpa.spring.internal.JpaRegistrar;
import com.holonplatform.spring.internal.BeanRegistryUtils;
import com.holonplatform.spring.internal.DataContextBoundBeanDefinition;

/**
 * Registrar for JPA stack and {@link Datastore} beans registration, using Spring boot {@link EntityScan} annotation.
 * 
 * @since 5.0.0
 */
public class JpaAutoConfigurationRegistrar
		implements ImportBeanDefinitionRegistrar, EnvironmentAware, BeanFactoryAware, BeanClassLoaderAware {

	private BeanFactory beanFactory;

	private Environment environment;

	private ClassLoader beanClassLoader;

	/*
	 * (non-Javadoc)
	 * @see org.springframework.beans.factory.BeanClassLoaderAware#setBeanClassLoader(java.lang.ClassLoader)
	 */
	@Override
	public void setBeanClassLoader(ClassLoader classLoader) {
		this.beanClassLoader = classLoader;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.springframework.beans.factory.BeanFactoryAware#setBeanFactory(org.springframework.beans.factory.BeanFactory)
	 */
	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.context.EnvironmentAware#setEnvironment(org.springframework.core.env.Environment)
	 */
	@Override
	public void setEnvironment(Environment environment) {
		this.environment = environment;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.springframework.context.annotation.ImportBeanDefinitionRegistrar#registerBeanDefinitions(org.springframework.
	 * core.type.AnnotationMetadata, org.springframework.beans.factory.support.BeanDefinitionRegistry)
	 */
	@Override
	public void registerBeanDefinitions(AnnotationMetadata annotationMetadata, BeanDefinitionRegistry registry) {
		if (beanFactory instanceof ListableBeanFactory) {
			for (String[] dataSourceDefinition : BeanRegistryUtils.getBeanNamesWithDataContextId(registry, beanFactory,
					DataSource.class, DataSourceFactoryBean.class)) {
				final String dataSourceBeanName = dataSourceDefinition[0];
				final String dataContextId = dataSourceDefinition[1];
				// check EntityManagerFactory bean
				String emfBeanName = isBeanRegistered((ListableBeanFactory) beanFactory, registry,
						EntityManagerFactory.class, dataContextId, BeanRegistryUtils.buildBeanName(dataContextId,
								EnableJpa.DEFAULT_ENTITYMANAGERFACTORY_BEAN_NAME));

				if (emfBeanName == null) {
					// register JPA stack
					Map<String, Object> attributes = new HashMap<>();
					attributes.put("dataContextId", dataContextId);
					attributes.put("dataSourceReference", dataSourceBeanName);
					attributes.put("entityPackages", getPackagesToScan(dataContextId));
					attributes.put("enableDatastore", Boolean.FALSE);
					emfBeanName = JpaRegistrar.registerJpaBeans(registry, beanFactory, environment, attributes,
							beanClassLoader);

				}

				// check JpaDatastore bean
				if (emfBeanName != null && isBeanRegistered((ListableBeanFactory) beanFactory, registry,
						JpaDatastore.class, dataContextId, BeanRegistryUtils.buildBeanName(dataContextId,
								EnableJpaDatastore.DEFAULT_DATASTORE_BEAN_NAME)) == null) {
					// register JPA Datastore
					JpaDatastoreRegistrar.registerDatastore(registry, environment, dataContextId, emfBeanName,
							JpaDatastoreConfigProperties.builder(dataContextId).build(), beanClassLoader);
				}

			}
		}
	}

	/**
	 * Checks if a bean of given type and bound to given data context id is already registered
	 * @param beanFactory Bean factory
	 * @param registry Bean registry
	 * @param type Bean type to check
	 * @param dataContextId Data context id
	 * @param defaultBeanName Default bean name to check
	 * @return The registered bean name if found, <code>null</code> otherwise
	 */
	private static String isBeanRegistered(ListableBeanFactory beanFactory, BeanDefinitionRegistry registry,
			Class<?> type, String dataContextId, String defaultBeanName) {
		String[] beanNames = beanFactory.getBeanNamesForType(type);
		if (beanNames != null && beanNames.length > 0) {
			for (String beanName : beanNames) {
				BeanDefinition bd = registry.getBeanDefinition(beanName);
				if (bd instanceof DataContextBoundBeanDefinition) {
					String did = ((DataContextBoundBeanDefinition) bd).getDataContextId().orElse(null);
					if ((dataContextId == null && did == null)
							|| (dataContextId != null && dataContextId.equals(did))) {
						return beanName;
					}
				} else {
					if (dataContextId == null) {
						return beanName;
					}
				}
			}
		}
		if (defaultBeanName != null && registry.containsBeanDefinition(defaultBeanName)) {
			return defaultBeanName;
		}
		return null;
	}

	/**
	 * Gets package names to scan to detect JPA Entity classes, using {@link JpaEntityScan} (or {@link EntityScan} for
	 * default data context id) annotation.
	 * @param dataContextId Data context id
	 * @return Package names to scan to detect JPA Entity classes
	 */
	protected String[] getPackagesToScan(String dataContextId) {
		List<String> packages = JpaEntityScanPackages.get(beanFactory, dataContextId).getPackageNames();
		if (packages.isEmpty() && dataContextId == null) {
			packages = EntityScanPackages.get(beanFactory).getPackageNames();
		}
		return packages.toArray(new String[packages.size()]);
	}

}
