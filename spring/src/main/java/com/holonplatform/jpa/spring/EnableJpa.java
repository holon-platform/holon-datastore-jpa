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

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.SharedCacheMode;
import javax.persistence.ValidationMode;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;

import com.holonplatform.core.datastore.Datastore;
import com.holonplatform.datastore.jpa.JpaDatastore;
import com.holonplatform.jdbc.DataSourceConfigProperties;
import com.holonplatform.jdbc.spring.SpringDataSourceConfigProperties;
import com.holonplatform.jpa.spring.internal.JpaRegistrar;
import com.holonplatform.spring.PrimaryMode;

/**
 * Annotation to be used on Spring Configuration classes to setup a full JPA enviroment bean stack: DataSource (if not
 * already available in Spring context), EntityManagerFactory and PlatformTransactionManager.
 * 
 * <p>
 * If a {@link DataSource} is not available in Spring context, a DataSource instance will be automatically configured
 * using external configuration properties according to {@link DataSourceConfigProperties}.
 * </p>
 * 
 * <p>
 * A data context id can be specified using {@link #dataContextId()}, to discriminate configuration properties using
 * given id as property prefix, and setting data context id as Spring bean qualifier to allow bean injection when
 * multiple JPA beans stack instances are registered in context, for example using {@link Qualifier} annotation.
 * </p>
 * 
 * <p>
 * When a data context id is specified, configuration properties must be written using that id as prefix, for example,
 * if data context id is <code>myid</code>: <br>
 * <code>holon.datasource.myid.url=...</code>
 * </p>
 * 
 * <p>
 * In case of multiple data context ids, {@link #primary()} or the external property
 * {@link SpringDataSourceConfigProperties#PRIMARY} can be used to mark one of the JPA beans stack as primary candidate
 * for dependency injection when a qualifier is not specified.
 * </p>
 * 
 * <p>
 * If {@link #enableDatastore()} is <code>true</code> (default value), a {@link JpaDatastore} bean will be configured
 * and registered in Spring Application Context, using a shared {@link EntityManager} obtained using this JPA stack.
 * </p>
 * 
 * @since 5.0.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import(JpaRegistrar.class)
public @interface EnableJpa {

	/**
	 * Default EntityManagerFactory registration bean name
	 */
	public static final String DEFAULT_ENTITYMANAGERFACTORY_BEAN_NAME = "entityManagerFactory";

	/**
	 * Bind JPA beans stack to given data context id.
	 * <p>
	 * The data context id will be used as JPA stack beans qualifier, allowing persistence sources discrimination in
	 * case of multiple data sources. You must ensure different data context ids are used when configuring multiple data
	 * sources using this annotation.
	 * </p>
	 * <p>
	 * This id will be setted as JPA persistence unit name too.
	 * </p>
	 * @return Data context id
	 */
	String dataContextId() default "";

	/**
	 * Whether to qualify registered JPA beans as <code>primary</code>, i.e. the preferential bean to be injected in a
	 * single-valued dependency when multiple candidates are present.
	 * <p>
	 * When mode is {@link PrimaryMode#AUTO}, the registered beans are marked as primary only when the
	 * {@link DataSource} bean to which JPA stack is bound is registered as primary bean.
	 * </p>
	 * @return Primary mode, defaults to {@link PrimaryMode#AUTO}
	 */
	PrimaryMode primary() default PrimaryMode.AUTO;

	/**
	 * Configures the name of the {@link DataSource} bean definition to be used to create the JPA
	 * {@link EntityManagerFactory} bean definition registered using this annotation. See {@link #dataContextId()} for
	 * informations about DataSource bean lookup when a specific name is not configured.
	 * <p>
	 * If the DataSource reference is not configured and no registered DataSource bean with consistent name is found in
	 * context, a new {@link DataSource} instance is configured and registered using {@link DataSourceConfigProperties}
	 * configuration properties.
	 * </p>
	 * @return The name of the {@link DataSource} bean definition to be used to create the JPA
	 *         {@link EntityManagerFactory}
	 */
	String dataSourceReference() default "";

	/**
	 * Whether to create a {@link JpaDatastore} bound to the {@link EntityManagerFactory} created and registered using
	 * this annotation.
	 * <p>
	 * Defaults to <code>true</code>.
	 * </p>
	 * @return True to create a JPA {@link Datastore} bean bound to the {@link EntityManagerFactory} created and
	 *         registered using this annotation
	 */
	boolean enableDatastore() default true;

	/**
	 * Get whether to enable the JPA datastore auto-flush mode. When auto-flush mode is enabled,
	 * {@link EntityManager#flush()} is called after each Datastore data manipulation operation, such as
	 * <code>save</code> or <code>delete</code> operations.
	 * <p>
	 * Default is <code>false</code>.
	 * </p>
	 * <p>
	 * This attribute is ignored is {@link #enableDatastore()} is <code>false</code>.
	 * </p>
	 * @return <code>true</code> to enable the auto-flush mode, <code>false</code> to disable
	 */
	boolean autoFlush() default false;

	/**
	 * If {@link #enableDatastore()} is <code>true</code>, get whether to add {@link Transactional} behaviour to
	 * transactional {@link Datastore} methods, to automatically create or partecipate in a transaction when methods are
	 * invoked. Affected methods are:
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
	boolean transactionalDatastore() default true;

	/**
	 * Package names to scan to map JPA Entity classes into EntityManagerFactory. If none specified, standard
	 * <code>persistence.xml</code> file will be used as mappings source.
	 * @return package names to scan to map JPA Entity classes
	 */
	String[] entityPackages() default {};

	/**
	 * Type-safe alternative to {@link #entityPackages()} for specifying the packages to scan to map JPA Entity classes
	 * into EntityManagerFactory. The package of each class specified will be scanned.
	 * <p>
	 * Consider creating a special no-op marker class or interface in each package that serves no purpose other than
	 * being referenced by this attribute.
	 * </p>
	 * @return classes from which to obtain package names to scan
	 */
	Class<?>[] entityPackageClasses() default {};

	/**
	 * Specify the JPA 2.0 validation mode for this persistence unit.
	 * @return Validation mode
	 */
	ValidationMode validationMode() default ValidationMode.AUTO;

	/**
	 * Specify the JPA 2.0 shared cache mode for this persistence unit.
	 * @return Shared cache mode
	 */
	SharedCacheMode sharedCacheMode() default SharedCacheMode.UNSPECIFIED;

	/**
	 * Set when this transaction manager should activate the thread-bound transaction synchronization support. Default
	 * is "always".
	 * <p>
	 * Note that transaction synchronization isn't supported for multiple concurrent transactions by different
	 * transaction managers. Only one transaction manager is allowed to activate it at any time.
	 * @see AbstractPlatformTransactionManager#SYNCHRONIZATION_ALWAYS
	 * @see AbstractPlatformTransactionManager#SYNCHRONIZATION_ON_ACTUAL_TRANSACTION
	 * @see AbstractPlatformTransactionManager#SYNCHRONIZATION_NEVER
	 * @return Transaction synchronization mode, or <code>-1</code> for default
	 */
	int transactionSynchronization() default -1;

	/**
	 * Specify the default timeout that this transaction manager should apply if there is no timeout specified at the
	 * transaction level, in seconds.
	 * <p>
	 * Default is the underlying transaction infrastructure's default timeout, e.g. typically 30 seconds in case of a
	 * JTA provider, indicated by the {@code TransactionDefinition.TIMEOUT_DEFAULT} value.
	 * </p>
	 * @return Default timeout in seconds or <code>-1</code> for default
	 */
	int defaultTimeout() default -1;

	/**
	 * Set whether existing transactions should be validated before participating in them.
	 * <p>
	 * When participating in an existing transaction (e.g. with PROPAGATION_REQUIRES or PROPAGATION_SUPPORTS
	 * encountering an existing transaction), this outer transaction's characteristics will apply even to the inner
	 * transaction scope. Validation will detect incompatible isolation level and read-only settings on the inner
	 * transaction definition and reject participation accordingly through throwing a corresponding exception.
	 * </p>
	 * <p>
	 * Default is "false", leniently ignoring inner transaction settings, simply overriding them with the outer
	 * transaction's characteristics. Switch this flag to "true" in order to enforce strict validation.
	 * </p>
	 * @return True to enable existing transactions validation
	 */
	boolean validateExistingTransaction() default false;

	/**
	 * Set whether to fail early in case of the transaction being globally marked as rollback-only.
	 * <p>
	 * Default is "false", only causing an UnexpectedRollbackException at the outermost transaction boundary. Switch
	 * this flag on to cause an UnexpectedRollbackException as early as the global rollback-only marker has been first
	 * detected, even from within an inner transaction boundary.
	 * </p>
	 * <p>
	 * Note that, as of Spring 2.0, the fail-early behavior for global rollback-only markers has been unified: All
	 * transaction managers will by default only cause UnexpectedRollbackException at the outermost transaction
	 * boundary. This allows, for example, to continue unit tests even after an operation failed and the transaction
	 * will never be completed. All transaction managers will only fail earlier if this flag has explicitly been set to
	 * "true".
	 * </p>
	 * @return True to set early fail
	 */
	boolean failEarlyOnGlobalRollbackOnly() default false;

	/**
	 * Set whether {@code doRollback} should be performed on failure of the {@code doCommit} call. Typically not
	 * necessary and thus to be avoided, as it can potentially override the commit exception with a subsequent rollback
	 * exception.
	 * <p>
	 * Default is "false".
	 * </p>
	 * @return True to activate
	 */
	boolean rollbackOnCommitFailure() default false;

}
