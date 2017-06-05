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
package com.holonplatform.datastore.jpa.internal.expressions;

import java.util.Map;
import java.util.Optional;

import javax.persistence.EntityManagerFactory;

import com.holonplatform.core.Expression.InvalidExpressionException;
import com.holonplatform.core.ExpressionResolver.ExpressionResolverHandler;
import com.holonplatform.core.ExpressionResolver.ResolutionContext;
import com.holonplatform.core.Path;
import com.holonplatform.core.datastore.relational.RelationalTarget;
import com.holonplatform.core.internal.utils.ObjectUtils;
import com.holonplatform.datastore.jpa.JpaConfigProperties.ORMPlatform;
import com.holonplatform.datastore.jpa.internal.support.ParameterValue;

/**
 * JDBC {@link ResolutionContext}.
 *
 * @since 5.0.0
 */
public interface JpaResolutionContext extends ResolutionContext {

	public enum AliasMode {

		DEFAULT, AUTO, UNSUPPORTED;

	}

	/**
	 * Get the EntityManagerFactory.
	 * @return The EntityManagerFactory
	 */
	EntityManagerFactory getEntityManagerFactory();

	/**
	 * Get the ORM platform, if detected.
	 * @return Optional ORM platform
	 */
	Optional<ORMPlatform> getORMPlatform();

	/**
	 * Get the context sequence number
	 * @return The context sequence number, <code>0</code> for the main context (with no parent)
	 */
	int getSequence();

	/**
	 * Get the optional parent context
	 * @return Optional parent context
	 */
	Optional<JpaResolutionContext> getParent();

	/**
	 * Get the data targets alias handling mode
	 * @return Alias handling mode
	 */
	AliasMode getAliasMode();

	/**
	 * Get the optional alias associated to given data target path
	 * @param path Path to get the alias for, <code>null</code> for the the root data target alias, if available
	 * @return Optional path alias
	 */
	Optional<String> getTargetAlias(Path<?> path);

	/**
	 * Add a named parameter to context
	 * @param value Parameter value
	 * @return Generated parameter name
	 */
	String addNamedParameter(ParameterValue value);

	/**
	 * Get the context named parameters.
	 * @return Map of named parameters with name - value associations
	 */
	Map<String, ParameterValue> getNamedParameters();

	/**
	 * Set the resolution context data target
	 * @param target The data target to set
	 */
	void setTarget(RelationalTarget<?> target);

	/**
	 * Get the resolution context data target
	 * @return Optional resolution context data target
	 */
	Optional<RelationalTarget<?>> getTarget();

	/**
	 * Create a new {@link JpaResolutionContext} as child of this context
	 * @param aliasMode The {@link AliasMode} to use
	 * @return A new {@link JpaResolutionContext} with this context as parent
	 */
	JpaResolutionContext childContext(AliasMode aliasMode);

	/**
	 * Create a new {@link JpaResolutionContext}.
	 * @param entityManagerFactory EntityManagerFactory (not null)
	 * @param platform ORM platform
	 * @param expressionResolverHandler Expression resolver handler (not null)
	 * @param aliasMode Alias handling mode
	 * @return A new {@link JpaResolutionContext} instance
	 */
	static JpaResolutionContext create(EntityManagerFactory entityManagerFactory, ORMPlatform platform,
			ExpressionResolverHandler expressionResolverHandler, AliasMode aliasMode) {
		return new DefaultJpaResolutionContext(entityManagerFactory, platform, expressionResolverHandler, aliasMode);
	}

	/**
	 * Check the given context is a {@link JpaResolutionContext}.
	 * @param context Context to check (not null)
	 * @return The JdbcResolutionContext
	 * @throws InvalidExpressionException If given context is not a JdbcResolutionContext
	 */
	static JpaResolutionContext checkContext(ResolutionContext context) {
		ObjectUtils.argumentNotNull(context, "Null ResolutionContext");
		if (!JpaResolutionContext.class.isAssignableFrom(context.getClass())) {
			throw new InvalidExpressionException("Invalid ResolutionContext type: expected ["
					+ JpaResolutionContext.class.getName() + "], got [" + context.getClass().getName() + "]");
		}
		return (JpaResolutionContext) context;
	}

}
