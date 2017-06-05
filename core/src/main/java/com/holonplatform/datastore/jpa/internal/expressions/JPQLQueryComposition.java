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

import java.io.Serializable;
import java.util.Optional;

import com.holonplatform.core.Expression;
import com.holonplatform.core.query.Query.QueryBuildException;

/**
 * Represents the composition of a JPQL query, providing query clauses and a {@link #serialize()} method to obtain the
 * actual JPQL query.
 * 
 * @param <Q> Query result type
 * @param <T> Projection result type
 *
 * @since 5.0.0
 */
public interface JPQLQueryComposition<Q, T> extends Expression, Serializable {

	/**
	 * Get the <code>SELECT</code> clause.
	 * @return Select JPQL clause
	 */
	String getSelect();

	/**
	 * Get the <code>FROM</code> clause.
	 * @return From JPQL clause
	 */
	String getFrom();

	/**
	 * Get the <code>WHERE</code> clause.
	 * @return Optional where JPQL clause
	 */
	Optional<String> getWhere();

	/**
	 * Get the <code>ORDER BY</code> clause.
	 * @return Optional order by JPQL clause
	 */
	Optional<String> getOrderBy();

	/**
	 * Get the <code>GROUP BY</code> clause.
	 * @return Optional group by JPQL clause
	 */
	Optional<String> getGroupBy();

	/**
	 * Get result set limit.
	 * @return Results limit, an empty Optional indicates no limit.
	 */
	Optional<Integer> getLimit();

	/**
	 * Get 0-based results offset.
	 * @return Results offset 0-based index, an empty Optional indicates no offset.
	 */
	Optional<Integer> getOffset();

	/**
	 * Get the query projection.
	 * @return the query projection
	 */
	ProjectionContext<Q, T> getProjection();

	/**
	 * Serialize the query cluases into a JPQL query
	 * @return JPQL query
	 * @throws QueryBuildException Error serializing query
	 */
	String serialize() throws QueryBuildException;

}
