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
package com.holonplatform.jpa.spring.boot.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.annotation.Order;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import com.holonplatform.jpa.spring.boot.JpaEntityScan;
import com.holonplatform.jpa.spring.boot.JpaEntityScans;
import com.holonplatform.spring.internal.AbstractRepeatableAnnotationRegistrar;

/**
 * Class for storing {@link JpaEntityScan} specified packages for reference later.
 */
public class JpaEntityScanPackages {

	private static final String BASE_BEAN_NAME = JpaEntityScanPackages.class.getName();

	private static final JpaEntityScanPackages NONE = new JpaEntityScanPackages();

	private final List<String> packageNames;

	JpaEntityScanPackages(String... packageNames) {
		List<String> packages = new ArrayList<>();
		for (String name : packageNames) {
			if (StringUtils.hasText(name)) {
				packages.add(name);
			}
		}
		this.packageNames = Collections.unmodifiableList(packages);
	}

	/**
	 * Return the package names specified from all JpaEntityScan annotations.
	 * @return the entity scan package names
	 */
	public List<String> getPackageNames() {
		return this.packageNames;
	}

	/**
	 * Return the {@link JpaEntityScanPackages} for the given bean factory.
	 * @param beanFactory the source bean factory
	 * @param dataContextId Data context id
	 * @return the {@link JpaEntityScanPackages} for the bean factory (never {@code null})
	 */
	public static JpaEntityScanPackages get(BeanFactory beanFactory, String dataContextId) {
		try {
			return beanFactory.getBean(buildBeanName(dataContextId), JpaEntityScanPackages.class);
		} catch (@SuppressWarnings("unused") NoSuchBeanDefinitionException ex) {
			return NONE;
		}
	}

	/**
	 * Register the specified entity scan packages with the system.
	 * @param dataContextId Data context id
	 * @param registry the source registry
	 * @param packageNames the package names to register
	 */
	public static void registerPackageNames(String dataContextId, BeanDefinitionRegistry registry,
			Collection<String> packageNames) {
		Assert.notNull(registry, "Registry must not be null");
		Assert.notNull(packageNames, "PackageNames must not be null");

		String beanName = buildBeanName(dataContextId);

		if (registry.containsBeanDefinition(beanName)) {
			BeanDefinition beanDefinition = registry.getBeanDefinition(beanName);
			ConstructorArgumentValues constructorArguments = beanDefinition.getConstructorArgumentValues();
			constructorArguments.addIndexedArgumentValue(0, addPackageNames(constructorArguments, packageNames));
		} else {
			GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
			beanDefinition.setBeanClass(JpaEntityScanPackages.class);
			beanDefinition.getConstructorArgumentValues().addIndexedArgumentValue(0,
					packageNames.toArray(new String[packageNames.size()]));
			beanDefinition.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
			registry.registerBeanDefinition(beanName, beanDefinition);
		}
	}

	private static String buildBeanName(String dataContextId) {
		return (dataContextId != null) ? (BASE_BEAN_NAME + "_" + dataContextId) : BASE_BEAN_NAME;
	}

	private static String[] addPackageNames(ConstructorArgumentValues constructorArguments,
			Collection<String> packageNames) {
		String[] existing = (String[]) constructorArguments.getIndexedArgumentValue(0, String[].class).getValue();
		Set<String> merged = new LinkedHashSet<>();
		merged.addAll(Arrays.asList(existing));
		merged.addAll(packageNames);
		return merged.toArray(new String[merged.size()]);
	}

	/**
	 * {@link ImportBeanDefinitionRegistrar} to store the base package from the importing configuration.
	 */
	@Order(Ordered.HIGHEST_PRECEDENCE)
	public static class Registrar extends AbstractRepeatableAnnotationRegistrar {

		public Registrar() {
			super(JpaEntityScan.class, JpaEntityScans.class);
		}

		/*
		 * (non-Javadoc)
		 * @see com.holonplatform.spring.internal.AbstractRepeatableAnnotationRegistrar#register(java.util.Map,
		 * org.springframework.beans.factory.support.BeanDefinitionRegistry, boolean)
		 */
		@Override
		protected void register(Map<String, Object> attributes, BeanDefinitionRegistry registry,
				boolean fromRepeatableAnnotationContainer) {
			AnnotationAttributes annotationAttributes = AnnotationAttributes.fromMap(attributes);
			registerPackageNames(annotationAttributes.getString("value"), registry,
					getPackagesToScan(annotationAttributes));
		}

		private static Set<String> getPackagesToScan(AnnotationAttributes attributes) {
			String[] basePackages = attributes.getStringArray("basePackages");
			Class<?>[] basePackageClasses = attributes.getClassArray("basePackageClasses");
			Set<String> packagesToScan = new LinkedHashSet<>();
			packagesToScan.addAll(Arrays.asList(basePackages));
			for (Class<?> basePackageClass : basePackageClasses) {
				packagesToScan.add(ClassUtils.getPackageName(basePackageClass));
			}
			return packagesToScan;
		}

	}

}
