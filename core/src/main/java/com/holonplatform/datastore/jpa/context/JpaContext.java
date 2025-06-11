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
package com.holonplatform.datastore.jpa.context;

import java.util.Optional;

import jakarta.persistence.EntityManagerFactory;

import com.holonplatform.core.ExpressionResolver.ExpressionResolverProvider;
import com.holonplatform.datastore.jpa.ORMPlatform;
import com.holonplatform.datastore.jpa.dialect.ORMDialect;
import com.holonplatform.datastore.jpa.jpql.JPQLValueDeserializer;
import com.holonplatform.datastore.jpa.jpql.JPQLValueSerializer;

/**
 * Base JPA context.
 *
 * @since 5.1.0
 */
public interface JpaContext extends ExpressionResolverProvider {

	/**
	 * Get the EntityManagerFactory.
	 * @return The EntityManagerFactory
	 */
	EntityManagerFactory getEntityManagerFactory();

	/**
	 * Get the {@link ORMDialect} to use.
	 * @return The ORM dialect
	 */
	ORMDialect getDialect();

	/**
	 * Get the ORM platform, if detected.
	 * @return Optional ORM platform
	 */
	Optional<ORMPlatform> getORMPlatform();

	/**
	 * Get the {@link JPQLValueSerializer} of this context.
	 * @return the {@link JPQLValueSerializer}
	 */
	default JPQLValueSerializer getValueSerializer() {
		return JPQLValueSerializer.getDefault();
	}

	/**
	 * Get the {@link JPQLValueDeserializer} of this context.
	 * @return the {@link JPQLValueDeserializer}
	 */
	default JPQLValueDeserializer getValueDeserializer() {
		return JPQLValueDeserializer.getDefault();
	}

	/**
	 * Trace given JPQL statement.
	 * <p>
	 * If tracing is enabled, the JPQL statement is logged using the <code>INFO</code> level, otherwise it is logged
	 * using the <code>DEBUG</code> level.
	 * </p>
	 * @param jpql JPQL to trace
	 */
	void trace(String jpql);

	/**
	 * Trace given JPA operation.
	 * <p>
	 * If tracing is enabled, the operation is logged using the <code>INFO</code> level, otherwise it is logged using
	 * the <code>DEBUG</code> level.
	 * </p>
	 * @param operation Operation to trace
	 */
	void traceOperation(String operation);

}
