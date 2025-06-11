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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import com.holonplatform.core.datastore.DataContextBound;
import com.holonplatform.core.datastore.Datastore;
import com.holonplatform.datastore.jpa.JpaDatastore;
import com.holonplatform.jpa.spring.internal.JpaDatastoreRegistrar;
import com.holonplatform.spring.EnableDatastoreConfiguration;
import com.holonplatform.spring.PrimaryMode;

/**
 * Annotation to be used on Spring Configuration classes to setup a JPA {@link Datastore}.
 * <p>
 * For successful {@link Datastore} registration, an {@link EntityManagerFactory} bean must be available in context. If
 * no data context is specified, a bean named <code>entityManagerFactory</code> is required. Otherwise, an
 * EntityManagerFactory bean named using pattern <code>dataContextId_entityManagerFactory</code> is required. A custom
 * EntityManagerFactory bean name can be configured using {@link #entityManagerFactoryReference()} attribute.
 * </p>
 * <p>
 * The registered bean name will be {@link #DEFAULT_DATASTORE_BEAN_NAME} if {@link #dataContextId()} is not specified.
 * Otherwise, the registered bean name will be the composition of the data context id and the default bean name using
 * this pattern: {@link #DEFAULT_DATASTORE_BEAN_NAME} + <code>_dataContextId</code>.
 * </p>
 * 
 * @since 5.0.0
 * 
 * @see JpaDatastore
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import(JpaDatastoreRegistrar.class)
@EnableDatastoreConfiguration
public @interface EnableJpaDatastore {

	/**
	 * Default {@link Datastore} registration bean name.
	 */
	public static final String DEFAULT_DATASTORE_BEAN_NAME = "jpaDatastore";

	/**
	 * Optional data context id to use to discriminate Datastores when more than one persistence source is configured,
	 * i.e. when multiple DataSources and JPA persistence units are configured in context.
	 * <p>
	 * The configured data context id will be returned by the {@link DataContextBound#getDataContextId()} method of the
	 * registered {@link Datastore}.
	 * </p>
	 * <p>
	 * When a data context id is specified, the registered Datastore should be bound to the EntityManagerFactory with a
	 * persistence unit name which matches the data context id. During registration phase, if the data context id is not
	 * null/empty and a {@link #entityManagerFactoryReference()} is not specified, an EntityManagerFactory bean is
	 * searched in context using the bean name pattern: <code>entityManagerFactory_[datacontextid]</code> where
	 * <code>[datacontextid]</code> is equal to {@link #dataContextId()} attribute.
	 * </p>
	 * @return Data context id
	 */
	String dataContextId() default "";

	/**
	 * Configures the name of the {@link EntityManagerFactory} bean definition to be used to create the
	 * {@link Datastore} registered using this annotation. See {@link #dataContextId()} for informations about
	 * EntityManagerFactory bean lookup when a specific name is not configured.
	 * @return The name of the {@link EntityManagerFactory} bean definition to be used to create the {@link Datastore}
	 */
	String entityManagerFactoryReference() default "";

	/**
	 * Whether to qualify {@link Datastore} bean as <code>primary</code>, i.e. the preferential bean to be injected in a
	 * single-valued dependency when multiple candidates are present.
	 * <p>
	 * When mode is {@link PrimaryMode#AUTO}, the registred Datastore bean is marked as primary only when the
	 * {@link EntityManagerFactory} bean to which is bound is registered as primary bean.
	 * </p>
	 * @return Primary mode, defaults to {@link PrimaryMode#AUTO}
	 */
	PrimaryMode primary() default PrimaryMode.AUTO;

	/**
	 * Get whether to enable the auto-flush mode. When auto-flush mode is enabled, {@link EntityManager#flush()} is
	 * called after each Datastore data manipulation operation, such as <code>save</code> or <code>delete</code>
	 * operations.
	 * <p>
	 * Default is <code>false</code>.
	 * </p>
	 * @return <code>true</code> to enable the auto-flush mode, <code>false</code> to disable
	 */
	boolean autoFlush() default false;

	/**
	 * Get whether to add {@link Transactional} behaviour to transactional {@link Datastore} methods, to automatically
	 * create or partecipate in a transaction when methods are invoked. Affected methods are:
	 * <ul>
	 * <li>{@link Datastore#refresh(com.holonplatform.core.datastore.DataTarget, com.holonplatform.core.property.PropertyBox)}</li>
	 * <li>{@link Datastore#insert(com.holonplatform.core.datastore.DataTarget, com.holonplatform.core.property.PropertyBox, com.holonplatform.core.datastore.DatastoreOperations.WriteOption...)}</li>
	 * <li>{@link Datastore#update(com.holonplatform.core.datastore.DataTarget, com.holonplatform.core.property.PropertyBox, com.holonplatform.core.datastore.DatastoreOperations.WriteOption...)}</li>
	 * <li>{@link Datastore#save(com.holonplatform.core.datastore.DataTarget, com.holonplatform.core.property.PropertyBox, com.holonplatform.core.datastore.DatastoreOperations.WriteOption...)}</li>
	 * <li>{@link Datastore#delete(com.holonplatform.core.datastore.DataTarget, com.holonplatform.core.property.PropertyBox, com.holonplatform.core.datastore.DatastoreOperations.WriteOption...)}</li>
	 * </ul>
	 * @return Whether to add {@link Transactional} behaviour to transactional datastore methods. Defaults to
	 *         <code>true</code>.
	 */
	boolean transactional() default true;

}
