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
package com.holonplatform.datastore.jpa.internal.context;

import java.util.Optional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import com.holonplatform.core.ExpressionResolver;
import com.holonplatform.datastore.jpa.ORMPlatform;
import com.holonplatform.datastore.jpa.context.JpaContext;
import com.holonplatform.datastore.jpa.context.JpaExecutionContext;
import com.holonplatform.datastore.jpa.dialect.ORMDialect;

/**
 * Default {@link JpaExecutionContext} implementation.
 *
 * @since 5.1.0
 */
public class DefaultJpaExecutionContext implements JpaExecutionContext {

	private final JpaContext context;
	private final EntityManager entityManager;

	public DefaultJpaExecutionContext(JpaContext context, EntityManager entityManager) {
		super();
		this.context = context;
		this.entityManager = entityManager;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jpa.context.JpaContext#getEntityManagerFactory()
	 */
	@Override
	public EntityManagerFactory getEntityManagerFactory() {
		return context.getEntityManagerFactory();
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jpa.context.JpaContext#getDialect()
	 */
	@Override
	public ORMDialect getDialect() {
		return context.getDialect();
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jpa.context.JpaContext#getORMPlatform()
	 */
	@Override
	public Optional<ORMPlatform> getORMPlatform() {
		return context.getORMPlatform();
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jpa.context.JpaContext#trace(java.lang.String)
	 */
	@Override
	public void trace(String jpql) {
		context.trace(jpql);
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jpa.context.JpaContext#traceOperation(java.lang.String)
	 */
	@Override
	public void traceOperation(String operation) {
		context.traceOperation(operation);
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.ExpressionResolver.ExpressionResolverProvider#getExpressionResolvers()
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public Iterable<ExpressionResolver> getExpressionResolvers() {
		return context.getExpressionResolvers();
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jpa.context.JpaExecutionContext#getEntityManager()
	 */
	@Override
	public EntityManager getEntityManager() {
		return entityManager;
	}

}
