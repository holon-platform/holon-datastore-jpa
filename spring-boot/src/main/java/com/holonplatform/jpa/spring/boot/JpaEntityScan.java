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
package com.holonplatform.jpa.spring.boot;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.persistence.EntityManagerFactory;

import org.springframework.context.annotation.Import;

import com.holonplatform.jpa.spring.boot.internal.JpaEntityScanPackages;

/**
 * Configures the base packages used by auto-configuration when scanning for JPA entity classes, when
 * {@link EntityManagerFactory} auto-configuration is enabled.
 * 
 * <p>
 * One of {@link #basePackageClasses()} or {@link #basePackages()} may be specified to define specific packages to scan.
 * If specific packages are not defined scanning will occur from the package of the class with this annotation.
 * </p>
 * 
 * <p>
 * A data context id can be specified through the {@link #value()} attribute to declare the data context id to which
 * this entity scan configuration is bound.
 * </p>
 * 
 * <p>
 * This is a repeatable annotation.
 * </p>
 * 
 * @since 5.0.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Repeatable(JpaEntityScans.class)
@Import(JpaEntityScanPackages.Registrar.class)
public @interface JpaEntityScan {

	/**
	 * The data context id to which this entity scan configuration is bound
	 * @return Data context id
	 */
	String value();

	/**
	 * Base packages to scan for entities.
	 * <p>
	 * Use {@link #basePackageClasses()} for a type-safe alternative to String-based package names.
	 * @return the base packages to scan
	 */
	String[] basePackages() default {};

	/**
	 * Type-safe alternative to {@link #basePackages()} for specifying the packages to scan for entities. The package of
	 * each class specified will be scanned.
	 * <p>
	 * Consider creating a special no-op marker class or interface in each package that serves no purpose other than
	 * being referenced by this attribute.
	 * @return classes from the base packages to scan
	 */
	Class<?>[] basePackageClasses() default {};

}
