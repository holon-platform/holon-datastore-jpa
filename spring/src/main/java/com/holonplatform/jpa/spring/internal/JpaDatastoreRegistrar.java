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
package com.holonplatform.jpa.spring.internal;

import java.util.Map;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.AutowireCandidateQualifier;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.transaction.annotation.Transactional;

import com.holonplatform.core.datastore.Datastore;
import com.holonplatform.core.internal.Logger;
import com.holonplatform.datastore.jpa.JpaDatastore;
import com.holonplatform.datastore.jpa.internal.JpaDatastoreLogger;
import com.holonplatform.jpa.spring.EnableJpa;
import com.holonplatform.jpa.spring.EnableJpaDatastore;
import com.holonplatform.spring.internal.AbstractConfigPropertyRegistrar;
import com.holonplatform.spring.internal.BeanRegistryUtils;
import com.holonplatform.spring.internal.GenericDataContextBoundBeanDefinition;
import com.holonplatform.spring.internal.PrimaryMode;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.TypeCache;
import net.bytebuddy.TypeCache.Sort;
import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.SuperMethodCall;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

/**
 * Registrar for JPA {@link Datastore} bean registration using {@link EnableJpaDatastore} annotation.
 * 
 * @since 5.0.0
 */
public class JpaDatastoreRegistrar extends AbstractConfigPropertyRegistrar implements BeanClassLoaderAware {

	/*
	 * Logger
	 */
	private static final Logger logger = JpaDatastoreLogger.create();

	/**
	 * Datastore enhanced classes cache
	 */
	private static final TypeCache<String> DATASTORE_PROXY_CACHE = new TypeCache<>(Sort.WEAK);

	/**
	 * Beans class loader
	 */
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
	 * org.springframework.context.annotation.ImportBeanDefinitionRegistrar#registerBeanDefinitions(org.springframework.
	 * core.type.AnnotationMetadata, org.springframework.beans.factory.support.BeanDefinitionRegistry)
	 */
	@Override
	public void registerBeanDefinitions(AnnotationMetadata annotationMetadata, BeanDefinitionRegistry registry) {

		if (!annotationMetadata.isAnnotated(EnableJpaDatastore.class.getName())) {
			// ignore call from sub classes
			return;
		}

		Map<String, Object> attributes = annotationMetadata.getAnnotationAttributes(EnableJpaDatastore.class.getName());

		// attributes
		String dataContextId = BeanRegistryUtils.getAnnotationValue(attributes, "dataContextId", null);
		PrimaryMode primaryMode = BeanRegistryUtils.getAnnotationValue(attributes, "primary", PrimaryMode.AUTO);
		String entityManagerFactoryReference = BeanRegistryUtils.getAnnotationValue(attributes,
				"entityManagerFactoryReference", null);

		String emfBeanName = entityManagerFactoryReference;
		if (emfBeanName == null) {
			emfBeanName = BeanRegistryUtils.buildBeanName(dataContextId,
					EnableJpa.DEFAULT_ENTITYMANAGERFACTORY_BEAN_NAME);
		}

		registerDatastore(registry, dataContextId, primaryMode, emfBeanName,
				BeanRegistryUtils.getAnnotationValue(attributes, "transactional", true),
				BeanRegistryUtils.getAnnotationValue(attributes, "autoFlush", false), beanClassLoader);

	}

	/**
	 * Register a {@link JpaDatastore} bean
	 * @param registry BeanDefinitionRegistry
	 * @param dataContextId Data context id
	 * @param primaryMode Primary mode
	 * @param entityManagerFactoryBeanName EntityManagerFactory bean name reference
	 * @param transactional Whether to add transactional behaviour to transactional datastore methods
	 * @param beanClassLoader Bean class loader
	 * @return Registered Datastore bean name
	 */
	public static String registerDatastore(BeanDefinitionRegistry registry, String dataContextId,
			PrimaryMode primaryMode, String entityManagerFactoryBeanName, boolean transactional, boolean autoFlush,
			ClassLoader beanClassLoader) {

		boolean primary = PrimaryMode.TRUE == primaryMode;
		if (!primary && PrimaryMode.AUTO == primaryMode) {
			if (registry.containsBeanDefinition(entityManagerFactoryBeanName)) {
				BeanDefinition bd = registry.getBeanDefinition(entityManagerFactoryBeanName);
				primary = bd.isPrimary();
			}
		}

		GenericDataContextBoundBeanDefinition definition = new GenericDataContextBoundBeanDefinition();
		definition.setDataContextId(dataContextId);

		Class<?> datastoreClass = transactional
				? addTransactionalAnnotations(DefaultSpringJpaDatastore.class, dataContextId, beanClassLoader)
				: DefaultSpringJpaDatastore.class;

		definition.setBeanClass(datastoreClass);

		definition.setAutowireCandidate(true);
		definition.setPrimary(primary);
		definition.setDependsOn(entityManagerFactoryBeanName);

		if (dataContextId != null) {
			definition.addQualifier(new AutowireCandidateQualifier(Qualifier.class, dataContextId));
		}

		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.add("entityManagerFactory", new RuntimeBeanReference(entityManagerFactoryBeanName));
		pvs.add("autoFlush", autoFlush);
		definition.setPropertyValues(pvs);

		String beanName = BeanRegistryUtils.buildBeanName(dataContextId,
				EnableJpaDatastore.DEFAULT_DATASTORE_BEAN_NAME);

		registry.registerBeanDefinition(beanName, definition);

		StringBuilder log = new StringBuilder();
		if (dataContextId != null) {
			log.append("<Data context id: ");
			log.append(dataContextId);
			log.append("> ");
		}
		log.append("Registered JPA Datastore bean with name \"");
		log.append(beanName);
		log.append("\"");
		if (dataContextId != null) {
			log.append(" and qualifier \"");
			log.append(dataContextId);
			log.append("\"");
		}
		log.append(" bound to EntityManagerFactory bean: ");
		log.append(entityManagerFactoryBeanName);
		logger.info(log.toString());

		return beanName;

	}

	private static final ElementMatcher<MethodDescription> TRANSACTIONAL_METHOD_NAMES = ElementMatchers.named("refresh")
			.or(ElementMatchers.named("save")).or(ElementMatchers.named("delete")).or(ElementMatchers.named("insert"))
			.or(ElementMatchers.named("update"));

	private static final ElementMatcher<MethodDescription> TRANSACTIONAL_METHODS = ElementMatchers.isPublic()
			.and(TRANSACTIONAL_METHOD_NAMES);

	private synchronized static <T extends Datastore> Class<?> addTransactionalAnnotations(
			Class<? extends T> datastoreClass, String dataContextId, ClassLoader classLoader) {

		// Proxy class name
		final StringBuilder nameBuilder = new StringBuilder();
		nameBuilder.append(datastoreClass.getName());
		nameBuilder.append("$Proxy$");
		if (dataContextId != null) {
			nameBuilder.append(dataContextId);
		} else {
			nameBuilder.append("default");
		}
		nameBuilder.append("$");
		nameBuilder.append(classLoader.hashCode());

		final String proxyName = nameBuilder.toString();

		// check cache
		Class<?> cached = DATASTORE_PROXY_CACHE.find(classLoader, proxyName);
		if (cached != null) {
			return cached;
		}

		try {

			// Transactional annotation
			AnnotationDescription.Builder annotationBuilder = AnnotationDescription.Builder.ofType(Transactional.class);
			if (dataContextId != null) {
				annotationBuilder = annotationBuilder.define("value", dataContextId);
			}
			final AnnotationDescription transactionalAnnotation = annotationBuilder.build();

			// Build proxy class
			Class<?> proxy = new ByteBuddy().subclass(datastoreClass).name(proxyName).method(TRANSACTIONAL_METHODS)
					.intercept(SuperMethodCall.INSTANCE).annotateMethod(transactionalAnnotation).make()
					.load(classLoader, ClassLoadingStrategy.Default.INJECTION).getLoaded();

			DATASTORE_PROXY_CACHE.insert(classLoader, proxyName, proxy);

			return proxy;

		} catch (Exception e) {
			logger.warn("Failed to enhance datastore class [" + datastoreClass.getName()
					+ "] with transactional annotations", e);
		}

		return datastoreClass;
	}

}
