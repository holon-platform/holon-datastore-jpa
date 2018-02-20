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
package com.holonplatform.datastore.jpa.jpql.expression;

import java.util.Map;

import com.holonplatform.datastore.jpa.internal.jpql.expression.DefaultJPQLQuery;
import com.holonplatform.datastore.jpa.jpql.JPQLResultConverter;

/**
 * JPQL query expression.
 * 
 * @param <Q> Query result type
 * @param <R> Conversion result type
 *
 * @since 5.1.0
 */
public interface JPQLQuery<Q, R> extends JPQLStatement {

	/**
	 * Get the query result type.
	 * @return The query result type
	 */
	Class<? extends Q> getQueryResultType();

	/**
	 * Get the JPQL result converter to be used with this query.
	 * @return The query result converter
	 */
	JPQLResultConverter<? super Q, R> getResultConverter();

	/**
	 * Create a new {@link JPQLQuery}.
	 * @param <Q> Query result type
	 * @param <R> Conversion result type
	 * @param jpql Query JPQL statement (not null)
	 * @param queryResultType Query result type (not null)
	 * @param resultConverter Query result converter
	 * @param parameters Statement parameters
	 * @return A new {@link JPQLQuery}
	 */
	static <Q, R> JPQLQuery<Q, R> create(String jpql, Class<? extends Q> queryResultType,
			JPQLResultConverter<? super Q, R> resultConverter, Map<String, JPQLParameter<?>> parameters) {
		return new DefaultJPQLQuery<>(jpql, queryResultType, resultConverter, parameters);
	}

	/**
	 * Create a new {@link JPQLQuery}.
	 * @param <Q> Query result type
	 * @param <R> Conversion result type
	 * @param jpql Query JPQL statement (not null)
	 * @param queryResultType Query result type (not null)
	 * @param resultConverter Query result converter
	 * @return A new {@link JPQLQuery}
	 */
	static <Q, R> JPQLQuery<Q, R> create(String jpql, Class<? extends Q> queryResultType,
			JPQLResultConverter<? super Q, R> resultConverter) {
		return new DefaultJPQLQuery<>(jpql, queryResultType, resultConverter);
	}

}
