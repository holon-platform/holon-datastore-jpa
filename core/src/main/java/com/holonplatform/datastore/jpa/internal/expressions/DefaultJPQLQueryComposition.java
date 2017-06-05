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

import java.util.Optional;

import com.holonplatform.core.query.Query.QueryBuildException;

/**
 * Default {@link JPQLQueryComposition} implementation.
 * 
 * @param <Q> Query result type
 * @param <T> Projection result type
 *
 * @since 5.0.0
 */
public class DefaultJPQLQueryComposition<Q, T> implements JPQLQueryComposition<Q, T> {

	private static final long serialVersionUID = -7925670155981691045L;

	private String select;
	private String from;
	private String where;
	private String orderBy;
	private String groupBy;

	private Integer limit;
	private Integer offset;

	private ProjectionContext<Q, T> projection;

	public DefaultJPQLQueryComposition() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jpa.internal.jpql.expressions.JPQLQueryComposition#getSelect()
	 */
	@Override
	public String getSelect() {
		return select;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jpa.internal.jpql.expressions.JPQLQueryComposition#getFrom()
	 */
	@Override
	public String getFrom() {
		return from;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jpa.internal.jpql.expressions.JPQLQueryComposition#getWhere()
	 */
	@Override
	public Optional<String> getWhere() {
		return Optional.ofNullable(where);
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jpa.internal.jpql.expressions.JPQLQueryComposition#getOrderBy()
	 */
	@Override
	public Optional<String> getOrderBy() {
		return Optional.ofNullable(orderBy);
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jpa.internal.jpql.expressions.JPQLQueryComposition#getGroupBy()
	 */
	@Override
	public Optional<String> getGroupBy() {
		return Optional.ofNullable(groupBy);
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jpa.internal.jpql.expressions.JPQLQueryComposition#getLimit()
	 */
	@Override
	public Optional<Integer> getLimit() {
		return Optional.ofNullable(limit);
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jpa.internal.jpql.expressions.JPQLQueryComposition#getOffset()
	 */
	@Override
	public Optional<Integer> getOffset() {
		return Optional.ofNullable(offset);
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jpa.internal.jpql.expressions.JPQLQueryComposition#getProjection()
	 */
	@Override
	public ProjectionContext<Q, T> getProjection() {
		return projection;
	}

	/**
	 * Set the SELECT query part
	 * @param select The part to set
	 */
	public void setSelect(String select) {
		this.select = select;
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
	 * Set the results limit.
	 * @param limit the limit to set
	 */
	public void setLimit(Integer limit) {
		this.limit = limit;
	}

	/**
	 * Set the 0-based results offset.
	 * @param offset the offset to set
	 */
	public void setOffset(Integer offset) {
		this.offset = offset;
	}

	/**
	 * Set the query projection context
	 * @param projection the projection to set
	 */
	public void setProjection(ProjectionContext<Q, T> projection) {
		this.projection = projection;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jpa.internal.jpql.expressions.JPQLQueryComposition#serialize()
	 */
	@Override
	public String serialize() throws QueryBuildException {
		final StringBuilder query = new StringBuilder();

		if (getSelect() == null || getSelect().trim().equals("")) {
			throw new QueryBuildException("Missing query SELECT clause");
		}
		if (getFrom() == null || getFrom().trim().equals("")) {
			throw new QueryBuildException("Missing query FROM clause");
		}

		query.append("SELECT ");
		query.append(getSelect());
		query.append(" FROM ");
		query.append(getFrom());

		getWhere().ifPresent(c -> {
			query.append(" WHERE ");
			query.append(c);
		});

		getGroupBy().ifPresent(c -> {
			query.append(" GROUP BY ");
			query.append(c);
		});

		getOrderBy().ifPresent(c -> {
			query.append(" ORDER BY ");
			query.append(c);
		});

		return query.toString();
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.Expression#validate()
	 */
	@Override
	public void validate() throws InvalidExpressionException {
		if (getProjection() == null) {
			throw new InvalidExpressionException("Missing projection context");
		}
	}

}
