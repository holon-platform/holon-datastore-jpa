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

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.EntityManagerFactory;
import javax.persistence.SharedCacheMode;
import javax.persistence.ValidationMode;
import javax.sql.DataSource;

import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.AutowireCandidateQualifier;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;

import com.holonplatform.core.datastore.DatastoreConfigProperties;
import com.holonplatform.core.internal.Logger;
import com.holonplatform.datastore.jpa.internal.JpaDatastoreLogger;
import com.holonplatform.jdbc.DataSourceConfigProperties;
import com.holonplatform.jdbc.DatabasePlatform;
import com.holonplatform.jdbc.spring.EnableDataSource;
import com.holonplatform.jdbc.spring.internal.DataSourceRegistrar;
import com.holonplatform.jpa.spring.EnableJpa;
import com.holonplatform.jpa.spring.JpaConfigProperties;
import com.holonplatform.jpa.spring.JpaDatastoreConfigProperties;
import com.holonplatform.spring.EnvironmentConfigPropertyProvider;
import com.holonplatform.spring.PrimaryMode;
import com.holonplatform.spring.internal.AbstractConfigPropertyRegistrar;
import com.holonplatform.spring.internal.BeanRegistryUtils;
import com.holonplatform.spring.internal.DefaultEnvironmentConfigPropertyProvider;
import com.holonplatform.spring.internal.GenericDataContextBoundBeanDefinition;

/**
 * Registrar for JPA beans registration using {@link EnableJpa} annotation.
 * 
 * @since 5.0.0
 */
public class JpaRegistrar extends AbstractConfigPropertyRegistrar implements BeanClassLoaderAware, BeanFactoryAware {

	/*
	 * Logger
	 */
	private static final Logger logger = JpaDatastoreLogger.create();

	/**
	 * Beans class loader
	 */
	private ClassLoader beanClassLoader;

	/**
	 * Bean factory
	 */
	private BeanFactory beanFactory;

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
	 * @see
	 * org.springframework.context.annotation.ImportBeanDefinitionRegistrar#registerBeanDefinitions(org.springframework.
	 * core.type.AnnotationMetadata, org.springframework.beans.factory.support.BeanDefinitionRegistry)
	 */
	@Override
	public void registerBeanDefinitions(AnnotationMetadata annotationMetadata, BeanDefinitionRegistry registry) {

		if (!annotationMetadata.isAnnotated(EnableJpa.class.getName())) {
			// ignore call from sub classes
			return;
		}

		Map<String, Object> attributes = annotationMetadata.getAnnotationAttributes(EnableJpa.class.getName());

		registerJpaBeans(registry, beanFactory, getEnvironment(), attributes, beanClassLoader);
	}

	/**
	 * Register JPA beans relying on given attributes
	 * @param registry BeanDefinitionRegistry
	 * @param beanFactory Bean factory
	 * @param environment Optional Environment
	 * @param attributes Attributes
	 * @param beanClassLoader Beans class loader
	 * @return Registered {@link EntityManagerFactory} bean name
	 */
	public static String registerJpaBeans(BeanDefinitionRegistry registry, BeanFactory beanFactory,
			Environment environment, Map<String, Object> attributes, ClassLoader beanClassLoader) {

		String dataContextId = BeanRegistryUtils.getAnnotationValue(attributes, "dataContextId", null);
		String dataSourceReference = BeanRegistryUtils.getAnnotationValue(attributes, "dataSourceReference", null);

		PrimaryMode primaryMode = BeanRegistryUtils.getAnnotationValue(attributes, "primary", PrimaryMode.AUTO);
		boolean primary = PrimaryMode.TRUE == primaryMode;

		String dsBeanName = dataSourceReference;
		if (dsBeanName == null) {
			// check DataSource
			dsBeanName = getDataSourceBeanName(registry, beanFactory, dataContextId);
			if (dsBeanName == null) {
				dsBeanName = BeanRegistryUtils.buildBeanName(dataContextId,
						EnableDataSource.DEFAULT_DATASOURCE_BEAN_NAME);
				// create and register a DataSource
				if (environment != null) {
					dsBeanName = DataSourceRegistrar.registerDataSource(environment, registry, dataContextId,
							primaryMode);
				}
			}
		}

		// check primary
		if (dsBeanName != null && registry.containsBeanDefinition(dsBeanName)) {
			if (!primary && PrimaryMode.AUTO == primaryMode) {
				BeanDefinition bd = registry.getBeanDefinition(dsBeanName);
				primary = bd.isPrimary();
			}
		}

		// check DataSource bean name
		if (dsBeanName == null) {
			throw new BeanCreationException("Failed to register JPA beans: missing DataSource bean name");
		}

		// JPA configuration

		JpaConfigProperties jpaConfigProperties = JpaConfigProperties.builder(dataContextId)
				.withPropertySource(EnvironmentConfigPropertyProvider.create(environment)).build();

		// Datastore configuration
		DatastoreConfigProperties datastoreConfig = null;
		try {
			datastoreConfig = DatastoreConfigProperties.builder(dataContextId)
					.withPropertySource(EnvironmentConfigPropertyProvider.create(environment)).build();
		} catch (Exception e) {
			logger.warn("Failed to load DatastoreConfigProperties", e);
		}

		// ------- EntityManagerFactory

		ValidationMode validationMode = BeanRegistryUtils.getAnnotationValue(attributes, "validationMode",
				ValidationMode.AUTO);
		SharedCacheMode sharedCacheMode = BeanRegistryUtils.getAnnotationValue(attributes, "sharedCacheMode",
				SharedCacheMode.UNSPECIFIED);

		String[] entityPackages = BeanRegistryUtils.getAnnotationValue(attributes, "entityPackages", new String[0]);
		Class<?>[] entityPackageClasses = BeanRegistryUtils.getAnnotationValue(attributes, "entityPackageClasses",
				new Class<?>[0]);

		List<String> entityPackageNames = new LinkedList<>();
		entityPackageNames.addAll(Arrays.asList(entityPackages));
		for (Class<?> cls : entityPackageClasses) {
			Package pkg = cls.getPackage();
			if (pkg != null && pkg.getName() != null) {
				entityPackageNames.add(pkg.getName());
			}
		}

		if (entityPackageNames.isEmpty()) {
			StringBuilder wrn = new StringBuilder();
			if (dataContextId != null) {
				wrn.append("<Data context id: ");
				wrn.append(dataContextId);
				wrn.append("> ");
			}
			wrn.append("No entity package names configured to build EntityManagerFactory");
			wrn.append(": expecting a persistence.xml file for unit name: ");
			wrn.append(dataContextId);
			logger.warn(wrn.toString());
		}

		EntityManagerFactoryConfigurator emfConfigurator = new EntityManagerFactoryConfigurator();
		emfConfigurator.setDataContextId(dataContextId);

		for (String entityPackage : entityPackageNames) {
			emfConfigurator.addEntityPackage(entityPackage);
		}

		emfConfigurator.setGenerateDdl(
				jpaConfigProperties.getConfigPropertyValue(JpaConfigProperties.GENERATE_DDL, Boolean.FALSE));

		emfConfigurator.setDialect(jpaConfigProperties.getConfigPropertyValue(JpaConfigProperties.DIALECT, null));

		// check datastore trace

		boolean showSql = false;
		if (datastoreConfig != null) {
			showSql = datastoreConfig.getConfigPropertyValue(DatastoreConfigProperties.TRACE, Boolean.FALSE);
		}
		if (!showSql) {
			showSql = jpaConfigProperties.getConfigPropertyValue(JpaConfigProperties.SHOW_SQL, Boolean.FALSE);
		}
		emfConfigurator.setShowSql(showSql);

		if (validationMode != null) {
			emfConfigurator.setValidationMode(validationMode);
		}
		if (sharedCacheMode != null) {
			emfConfigurator.setSharedCacheMode(sharedCacheMode);
		}

		emfConfigurator
				.setOrmPlatform(jpaConfigProperties.getConfigPropertyValue(JpaConfigProperties.ORM_PLATFORM, null));

		// Database platform
		DatabasePlatform database = jpaConfigProperties.getConfigPropertyValue(JpaConfigProperties.DATABASE, null);
		if (database != null) {
			emfConfigurator.setDatabase(database);
		} else {
			// check from datasource
			DataSourceConfigProperties dataSourceConfigProperties = DataSourceConfigProperties.builder(dataContextId)
					.withPropertySource(new DefaultEnvironmentConfigPropertyProvider(environment)).build();
			database = dataSourceConfigProperties.getConfigPropertyValue(DataSourceConfigProperties.PLATFORM, null);
			if (database != null) {
				emfConfigurator.setDatabase(database);
			}
		}

		// Vendor specific configuration properties
		Map<String, String> vendorProperties = jpaConfigProperties.getSubPropertiesUsingPrefix("properties");
		for (Entry<String, String> entry : vendorProperties.entrySet()) {
			emfConfigurator.addJpaProperty(entry.getKey(), entry.getValue());
		}

		GenericDataContextBoundBeanDefinition definition = new GenericDataContextBoundBeanDefinition();
		definition.setDataContextId(dataContextId);
		definition.setBeanClass(ConfigurableLocalContainerEntityManagerFactoryBean.class);
		definition.setAutowireCandidate(true);
		definition.setPrimary(primary);
		definition.setDependsOn(dsBeanName);

		if (dataContextId != null) {
			definition.addQualifier(new AutowireCandidateQualifier(Qualifier.class, dataContextId));
		}

		ConstructorArgumentValues avs = new ConstructorArgumentValues();
		avs.addIndexedArgumentValue(0, emfConfigurator);
		definition.setConstructorArgumentValues(avs);

		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.add("dataSource", new RuntimeBeanReference(dsBeanName));
		definition.setPropertyValues(pvs);

		String emfBeanName = BeanRegistryUtils.buildBeanName(dataContextId,
				EnableJpa.DEFAULT_ENTITYMANAGERFACTORY_BEAN_NAME);

		registry.registerBeanDefinition(emfBeanName, definition);

		StringBuilder log = new StringBuilder();
		if (dataContextId != null) {
			log.append("<Data context id: ");
			log.append(dataContextId);
			log.append("> ");
		}
		log.append("Registered EntityManagerFactory bean with name \"");
		log.append(emfBeanName);
		log.append("\"");
		if (dataContextId != null) {
			log.append(" and qualifier \"");
			log.append(dataContextId);
			log.append("\"");
		}
		log.append(" bound to DataSource bean: ");
		log.append(dsBeanName);
		logger.info(log.toString());

		// ------- TransactionManager

		TransactionManagerConfigurator tmConfigurator = new TransactionManagerConfigurator();
		tmConfigurator.setDataContextId(dataContextId);

		for (Entry<String, String> entry : vendorProperties.entrySet()) {
			tmConfigurator.addJpaProperty(entry.getKey(), entry.getValue());
		}

		int transactionSynchronization = BeanRegistryUtils.getAnnotationValue(attributes, "transactionSynchronization",
				-1);
		if (transactionSynchronization > -1) {
			tmConfigurator.setTransactionSynchronization(transactionSynchronization);
		}

		int defaultTimeout = BeanRegistryUtils.getAnnotationValue(attributes, "defaultTimeout", -1);
		if (defaultTimeout > -1) {
			tmConfigurator.setDefaultTimeout(defaultTimeout);
		}

		boolean validateExistingTransaction = BeanRegistryUtils.getAnnotationValue(attributes,
				"validateExistingTransaction", false);
		if (validateExistingTransaction) {
			tmConfigurator.setValidateExistingTransaction(true);
		}

		boolean failEarlyOnGlobalRollbackOnly = BeanRegistryUtils.getAnnotationValue(attributes,
				"failEarlyOnGlobalRollbackOnly", false);
		if (failEarlyOnGlobalRollbackOnly) {
			tmConfigurator.setFailEarlyOnGlobalRollbackOnly(true);
		}

		boolean rollbackOnCommitFailure = BeanRegistryUtils.getAnnotationValue(attributes, "rollbackOnCommitFailure",
				false);
		if (rollbackOnCommitFailure) {
			tmConfigurator.setRollbackOnCommitFailure(true);
		}

		definition = new GenericDataContextBoundBeanDefinition();
		definition.setDataContextId(dataContextId);
		definition.setBeanClass(ConfigurableJpaTransactionManager.class);
		definition.setAutowireCandidate(true);
		definition.setPrimary(primary);
		definition.setDependsOn(emfBeanName);

		if (dataContextId != null) {
			definition.addQualifier(new AutowireCandidateQualifier(Qualifier.class, dataContextId));
		}

		avs = new ConstructorArgumentValues();
		avs.addIndexedArgumentValue(0, tmConfigurator);
		definition.setConstructorArgumentValues(avs);

		pvs = new MutablePropertyValues();
		pvs.add("entityManagerFactory", new RuntimeBeanReference(emfBeanName));
		definition.setPropertyValues(pvs);

		String tmBeanName = BeanRegistryUtils.buildBeanName(dataContextId,
				EnableDataSource.DEFAULT_TRANSACTIONMANAGER_BEAN_NAME);

		registry.registerBeanDefinition(tmBeanName, definition);

		log = new StringBuilder();
		if (dataContextId != null) {
			log.append("<Data context id: ");
			log.append(dataContextId);
			log.append("> ");
		}
		log.append("Registered JpaTransactionManager bean with name \"");
		log.append(tmBeanName);
		log.append("\"");
		if (dataContextId != null) {
			log.append(" and qualifier \"");
			log.append(dataContextId);
			log.append("\"");
		}
		logger.info(log.toString());

		// ------- Datastore

		boolean enableDatastore = BeanRegistryUtils.getAnnotationValue(attributes, "enableDatastore", true);
		if (enableDatastore) {
			// register a JpaDatastore

			JpaDatastoreConfigProperties defaultConfig = JpaDatastoreConfigProperties.builder(dataContextId)
					.withProperty(JpaDatastoreConfigProperties.PRIMARY,
							(primaryMode == PrimaryMode.TRUE) ? Boolean.TRUE : null)
					.withProperty(JpaDatastoreConfigProperties.AUTO_FLUSH,
							BeanRegistryUtils.getAnnotationValue(attributes, "autoFlush", false))
					.withProperty(JpaDatastoreConfigProperties.TRANSACTIONAL,
							BeanRegistryUtils.getAnnotationValue(attributes, "transactionalDatastore", true))

					.build();

			JpaDatastoreRegistrar.registerDatastore(registry, environment, dataContextId, emfBeanName, defaultConfig,
					beanClassLoader);
		}

		return emfBeanName;

	}

	/**
	 * Get the {@link DataSource} type bean name which corresponds to given data context id
	 * @param registry Bean registry
	 * @param beanFactory Bean factory
	 * @param dataContextId Optional data context id
	 * @return The DataSource bean name, or <code>null</code> if not found
	 */
	private static String getDataSourceBeanName(BeanDefinitionRegistry registry, BeanFactory beanFactory,
			String dataContextId) {
		// check unique DataSource if no data context id specified
		if (dataContextId == null && beanFactory instanceof ListableBeanFactory) {
			String[] dataSourceBeanNames = BeanFactoryUtils.beanNamesForTypeIncludingAncestors(
					(ListableBeanFactory) beanFactory, DataSource.class, false, false);
			if (dataSourceBeanNames != null && dataSourceBeanNames.length == 1) {
				return dataSourceBeanNames[0];
			}
		}
		// check bean name using data context id
		String dsBeanName = BeanRegistryUtils.buildBeanName(dataContextId,
				EnableDataSource.DEFAULT_DATASOURCE_BEAN_NAME);
		if (registry.containsBeanDefinition(dsBeanName) && beanFactory.isTypeMatch(dsBeanName, DataSource.class)) {
			return dsBeanName;
		}
		return null;
	}

}
