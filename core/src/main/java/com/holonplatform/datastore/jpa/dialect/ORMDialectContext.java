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
package com.holonplatform.datastore.jpa.dialect;

import jakarta.persistence.EntityManagerFactory;

import com.holonplatform.core.ExpressionResolver.ExpressionResolverSupport;
import com.holonplatform.datastore.jpa.context.EntityManagerHandler;
import com.holonplatform.datastore.jpa.jpql.JPQLValueDeserializer;
import com.holonplatform.datastore.jpa.jpql.JPQLValueSerializer;

/**
 * Context which can be used for {@link ORMDialect} initialization.
 *
 * @since 5.1.0
 */
public interface ORMDialectContext extends ExpressionResolverSupport, EntityManagerHandler {

	/**
	 * Get the EntityManagerFactory.
	 * @return The EntityManagerFactory
	 */
	EntityManagerFactory getEntityManagerFactory();

	/**
	 * Get the {@link JPQLValueSerializer} of this context.
	 * @return the {@link JPQLValueSerializer}
	 */
	JPQLValueSerializer getValueSerializer();

	/**
	 * Get the {@link JPQLValueDeserializer} of this context.
	 * @return the {@link JPQLValueDeserializer}
	 */
	JPQLValueDeserializer getValueDeserializer();

}
