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
package com.holonplatform.datastore.jpa.jpql;

import com.holonplatform.core.exceptions.DataAccessException;
import com.holonplatform.datastore.jpa.context.JpaExecutionContext;
import com.holonplatform.datastore.jpa.internal.jpql.IdentityJPQLResultConverter;

/**
 * Jpa query result converter.
 * 
 * @param <Q> Query result type
 * @param <R> Expected result type
 * 
 * @since 5.0.0
 */
public interface JPQLResultConverter<Q, R> {

	/**
	 * Get the query result type.
	 * @return The query result type
	 */
	Class<? extends Q> getQueryResultType();

	/**
	 * Get the type into which this converter is able to convert a result.
	 * @return The conversion type
	 */
	Class<? extends R> getConversionType();

	/**
	 * Convert a query result into expected result type.
	 * @param context Execution context
	 * @param result Result to convert
	 * @return Converted result
	 * @throws DataAccessException If the conversion failed
	 */
	R convert(JpaExecutionContext context, Q result) throws DataAccessException;

	/**
	 * Create a {@link JPQLResultConverter} which does not perform any result conversion.
	 * @param <T> Query result type
	 * @param type Query result type (not null)
	 * @return A new identity {@link JPQLResultConverter}
	 */
	static <T> JPQLResultConverter<T, T> identity(Class<? extends T> type) {
		return new IdentityJPQLResultConverter<>(type);
	}

}
