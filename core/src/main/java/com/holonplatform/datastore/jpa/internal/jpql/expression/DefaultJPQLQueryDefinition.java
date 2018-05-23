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
package com.holonplatform.datastore.jpa.internal.jpql.expression;

import java.util.Optional;

import com.holonplatform.datastore.jpa.jpql.JPQLResultConverter;
import com.holonplatform.datastore.jpa.jpql.expression.JPQLQueryDefinition;

/**
 * Default {@link JPQLQueryDefinition}.
 *
 * @since 5.1.0
 */
public class DefaultJPQLQueryDefinition implements JPQLQueryDefinition {

	/**
	 * SELECT clause
	 */
	private String select;

	/**
	 * DISTINCT clause
	 */
	private boolean distinct;

	/**
	 * FROM clause
	 */
	private String from;

	/**
	 * WHERE clause
	 */
	private String where;

	/**
	 * ORDER BY clause
	 */
	private String orderBy;

	/**
	 * GROUP BY clause
	 */
	private String groupBy;

	/**
	 * Query result type
	 */
	private Class<?> queryResultType;

	/**
	 * Result converter
	 */
	private JPQLResultConverter<?, ?> resultConverter;

	public DefaultJPQLQueryDefinition() {
		super();
	}

	@Override
	public String getSelect() {
		return select;
	}

	@Override
	public boolean isDistinct() {
		return distinct;
	}

	@Override
	public String getFrom() {
		return from;
	}

	@Override
	public Optional<String> getWhere() {
		return Optional.ofNullable(where);
	}

	@Override
	public Optional<String> getOrderBy() {
		return Optional.ofNullable(orderBy);
	}

	@Override
	public Optional<String> getGroupBy() {
		return Optional.ofNullable(groupBy);
	}

	/**
	 * Set the SELECT query part
	 * @param select The part to set
	 */
	public void setSelect(String select) {
		this.select = select;
	}

	/**
	 * Set whether to add the DISTINCT caluse.
	 * @param distinct Whether to add the DISTINCT caluse
	 */
	public void setDistinct(boolean distinct) {
		this.distinct = distinct;
	}

	/**
	 * Set the FROM query part
	 * @param from The part to set
	 */
	public void setFrom(String from) {
		this.from = from;
	}

	/**
	 * Set the WHERE query part
	 * @param where The part to set
	 */
	public void setWhere(String where) {
		this.where = where;
	}

	/**
	 * Set the ORDER BY query part
	 * @param orderBy The part to set
	 */
	public void setOrderBy(String orderBy) {
		this.orderBy = orderBy;
	}

	/**
	 * Set the GROUP BY query part
	 * @param groupBy The part to set
	 */
	public void setGroupBy(String groupBy) {
		this.groupBy = groupBy;
	}

	/**
	 * Set the query result type.
	 * @param queryResultType the query result type to set
	 */
	public void setQueryResultType(Class<?> queryResultType) {
		this.queryResultType = queryResultType;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jpa.jpql.expression.JPQLQueryDefinition#getQueryResultType()
	 */
	@Override
	public Class<?> getQueryResultType() {
		return queryResultType;
	}

	/**
	 * Set the {@link JPQLResultConverter}.
	 * @param resultConverter the result converter to set
	 */
	public void setResultConverter(JPQLResultConverter<?, ?> resultConverter) {
		this.resultConverter = resultConverter;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jpa.jpql.expression.JPQLQueryDefinition#getResultConverter()
	 */
	@Override
	public Optional<JPQLResultConverter<?, ?>> getResultConverter() {
		return Optional.ofNullable(resultConverter);
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.Expression#validate()
	 */
	@Override
	public void validate() throws InvalidExpressionException {
		if (getSelect() == null || getSelect().trim().equals("")) {
			throw new InvalidExpressionException("Missing query SELECT clause");
		}
		if (getFrom() == null || getFrom().trim().equals("")) {
			throw new InvalidExpressionException("Missing query FROM clause");
		}
		if (getQueryResultType() == null) {
			throw new InvalidExpressionException("Missing query result type");
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "DefaultJPQLQueryDefinition [select=" + select + ", from=" + from + ", where=" + where + ", orderBy="
				+ orderBy + ", groupBy=" + groupBy + ", resultConverter=" + resultConverter + "]";
	}

}
