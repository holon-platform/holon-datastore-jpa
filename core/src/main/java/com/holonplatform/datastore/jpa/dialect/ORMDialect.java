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

import java.util.Optional;

import javax.persistence.Tuple;

import com.holonplatform.core.query.QueryFunction;
import com.holonplatform.datastore.jpa.ORMPlatform;
import com.holonplatform.datastore.jpa.jpql.expression.JPQLFunction;

/**
 * JPA ORM dialect.
 *
 * @since 5.1.0
 */
public interface ORMDialect {

	/**
	 * Dialect initialization hook at parent datastore initialization.
	 * @param context Dialect context
	 */
	void init(ORMDialectContext context);

	/**
	 * Returns whether the {@link Tuple} query result type is supported.
	 * @return Whether the {@link Tuple} query result type is supported
	 */
	boolean isTupleSupported();

	/**
	 * Get the JPA configuration property name which corresponds to bulk operations batch size.
	 * @return The batch size configuration property name, if available
	 */
	default Optional<String> getBatchSizeConfigurationProperty() {
		return Optional.empty();
	}

	/**
	 * Get whether alias is supported in UPDATE statements.
	 * @return Whether alias is supported in UPDATE statements.
	 */
	default boolean updateStatementAliasSupported() {
		return false;
	}

	/**
	 * Get whether alias is supported in UPDATE statement SET clause.
	 * @return Whether alias is supported in UPDATE statement SET clause.
	 */
	default boolean updateStatementSetAliasSupported() {
		return false;
	}

	/**
	 * Get whether alias is supported in DELETE statements.
	 * @return Whether alias is supported in DELETE statements.
	 */
	default boolean deleteStatementAliasSupported() {
		return false;
	}

	/**
	 * Resolve given <code>function</code> into a dialect-specific {@link JPQLFunction}.
	 * @param function The function to resolve (not null)
	 * @return A dialect-specific function resolution, or empty to fallback to the default function resolution, if
	 *         available
	 */
	default Optional<JPQLFunction> resolveFunction(QueryFunction<?, ?> function) {
		return Optional.empty();
	}

	/**
	 * Detect a suitable {@link ORMDialect} to use with given ORM platform, if available.
	 * @param database ORM platform
	 * @return Optional {@link ORMDialect} for given platform
	 */
	static Optional<ORMDialect> detect(ORMPlatform platform) {
		if (platform != null) {
			switch (platform) {
			case HIBERNATE:
				return Optional.of(new HibernateDialect());
			case ECLIPSELINK:
				return Optional.of(new EclipselinkDialect());
			case OPENJPA:
				return Optional.of(new DefaultDialect());
			case DATANUCLEUS:
				return Optional.of(new DefaultDialect());
			default:
				break;
			}
		}
		return Optional.empty();
	}

}
