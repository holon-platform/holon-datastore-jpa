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

import java.time.temporal.Temporal;
import java.util.Optional;

import jakarta.persistence.LockTimeoutException;
import jakarta.persistence.OptimisticLockException;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.PessimisticLockException;
import jakarta.persistence.Tuple;

import com.holonplatform.core.exceptions.DataAccessException;
import com.holonplatform.core.query.QueryFunction;
import com.holonplatform.core.query.lock.LockAcquisitionException;
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
	 * Get the supported JPA major version.
	 * @return the supported JPA major version
	 */
	int getSupportedJPAMajorVersion();

	/**
	 * Get the supported JPA minor version.
	 * @return the supported JPA minor version
	 */
	int getSupportedJPAMinorVersion();

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
	 * Get whether {@link Temporal} type query parameters are supported.
	 * @return Whether {@link Temporal} type query parameters are supported
	 */
	default boolean temporalTypeParametersSupported() {
		return getSupportedJPAMajorVersion() >= 2 && getSupportedJPAMinorVersion() >= 1;
	}

	/**
	 * Get whether {@link Temporal} type query result is supported.
	 * @return Whether {@link Temporal} type query result is supported
	 */
	default boolean temporalTypeProjectionSupported() {
		return getSupportedJPAMajorVersion() >= 2 && getSupportedJPAMinorVersion() >= 1;
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
	 * Get whether the ESCAPE clause in LIKE predicate is supported.
	 * @return Whether the ESCAPE clause in LIKE predicate is supported.
	 */
	default boolean likeEscapeSupported() {
		return true;
	}

	/**
	 * Get the character to use for ESCAPE in LIKE predicate.
	 * @return the character to use for ESCAPE in LIKE predicate.
	 */
	default char getLikeEscapeCharacter() {
		return '!';
	}

	/**
	 * Serialize LIKE ESCAPE character as JPQL.
	 * @param escapeCharacter The LIKE ESCAPE character to use
	 * @return JPQL expression
	 */
	default String getLikeEscapeJPQL(char escapeCharacter) {
		return "'" + escapeCharacter + "'";
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
	 * Translates given {@link PersistenceException} into a suitable {@link DataAccessException}.
	 * @param exception Exception to translate (not null)
	 * @return Translated {@link PersistenceException}
	 */
	default DataAccessException translateException(PersistenceException exception) {
		if (LockTimeoutException.class.isAssignableFrom(exception.getClass())) {
			return new LockAcquisitionException("Failed to acquire lock", exception);
		}
		if (PessimisticLockException.class.isAssignableFrom(exception.getClass())) {
			return new LockAcquisitionException("Failed to acquire lock", exception);
		}
		if (OptimisticLockException.class.isAssignableFrom(exception.getClass())) {
			return new LockAcquisitionException("Failed to acquire lock", exception);
		}
		return new DataAccessException(exception);
	}

	/**
	 * Detect a suitable {@link ORMDialect} to use with given ORM platform, if available.
	 * @param platform ORM platform
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
				return Optional.of(new OpenJPADialect());
			case DATANUCLEUS:
				return Optional.of(new DatanucleusDialect());
			default:
				break;
			}
		}
		return Optional.empty();
	}

	/**
	 * Obtain the <em>Hibernate</em> ORM dialect implementation.
	 * @return A new {@link ORMDialect} instance
	 */
	static ORMDialect hibernate() {
		return new HibernateDialect();
	}

	/**
	 * Obtain the <em>Eclipselink</em> ORM dialect implementation.
	 * @return A new {@link ORMDialect} instance
	 */
	static ORMDialect eclipselink() {
		return new EclipselinkDialect();
	}

	/**
	 * Obtain the <em>OpenJPA</em> ORM dialect implementation.
	 * @return A new {@link ORMDialect} instance
	 */
	static ORMDialect openJPA() {
		return new OpenJPADialect();
	}

	/**
	 * Obtain the <em>Datanucleus</em> ORM dialect implementation.
	 * @return A new {@link ORMDialect} instance
	 */
	static ORMDialect datanucleus() {
		return new DatanucleusDialect();
	}

}
