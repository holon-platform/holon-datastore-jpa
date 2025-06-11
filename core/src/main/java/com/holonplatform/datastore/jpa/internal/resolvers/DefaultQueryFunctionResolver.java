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
package com.holonplatform.datastore.jpa.internal.resolvers;

import java.util.Optional;

import jakarta.annotation.Priority;

import com.holonplatform.core.Expression.InvalidExpressionException;
import com.holonplatform.core.query.QueryFunction;
import com.holonplatform.core.query.QueryFunction.Avg;
import com.holonplatform.core.query.QueryFunction.Count;
import com.holonplatform.core.query.QueryFunction.Max;
import com.holonplatform.core.query.QueryFunction.Min;
import com.holonplatform.core.query.QueryFunction.Sum;
import com.holonplatform.core.query.StringFunction.Lower;
import com.holonplatform.core.query.StringFunction.Upper;
import com.holonplatform.core.query.TemporalFunction.CurrentDate;
import com.holonplatform.core.query.TemporalFunction.CurrentLocalDate;
import com.holonplatform.core.query.TemporalFunction.CurrentLocalDateTime;
import com.holonplatform.core.query.TemporalFunction.CurrentTimestamp;
import com.holonplatform.core.query.TemporalFunction.Day;
import com.holonplatform.core.query.TemporalFunction.Hour;
import com.holonplatform.core.query.TemporalFunction.Month;
import com.holonplatform.core.query.TemporalFunction.Year;
import com.holonplatform.datastore.jpa.jpql.context.JPQLContextExpressionResolver;
import com.holonplatform.datastore.jpa.jpql.context.JPQLResolutionContext;
import com.holonplatform.datastore.jpa.jpql.expression.JPQLFunction;

/**
 * JPA {@link QueryFunction} expression resolver.
 *
 * @since 5.0.0
 */
@SuppressWarnings("rawtypes")
@Priority(Integer.MAX_VALUE)
public enum DefaultQueryFunctionResolver implements JPQLContextExpressionResolver<QueryFunction, JPQLFunction> {

	/**
	 * Singleton instance.
	 */
	INSTANCE;

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.ExpressionResolver#getExpressionType()
	 */
	@Override
	public Class<? extends QueryFunction> getExpressionType() {
		return QueryFunction.class;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.ExpressionResolver#getResolvedType()
	 */
	@Override
	public Class<? extends JPQLFunction> getResolvedType() {
		return JPQLFunction.class;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jpa.resolvers.JPQLContextExpressionResolver#resolve(com.holonplatform.core.
	 * Expression, com.holonplatform.datastore.jpa.context.JPQLResolutionContext)
	 */
	@Override
	public Optional<JPQLFunction> resolve(QueryFunction expression, JPQLResolutionContext context)
			throws InvalidExpressionException {

		// validate
		expression.validate();

		// default function resolution
		final Class<? extends QueryFunction> functionType = expression.getClass();

		JPQLFunction function = null;

		// aggregate
		if (Count.class.isAssignableFrom(functionType))
			function = JPQLFunction.create("COUNT", true);
		if (Avg.class.isAssignableFrom(functionType))
			function = JPQLFunction.create("AVG", true);
		if (Min.class.isAssignableFrom(functionType))
			function = JPQLFunction.create("MIN", true);
		if (Max.class.isAssignableFrom(functionType))
			function = JPQLFunction.create("MAX", true);
		if (Sum.class.isAssignableFrom(functionType))
			function = JPQLFunction.create("SUM", true);

		// string
		if (Lower.class.isAssignableFrom(functionType))
			function = JPQLFunction.create("LOWER", true);
		if (Upper.class.isAssignableFrom(functionType))
			function = JPQLFunction.create("UPPER", true);

		// temporal
		if (CurrentDate.class.isAssignableFrom(functionType))
			function = JPQLFunction.create("CURRENT_DATE", false);
		if (CurrentLocalDate.class.isAssignableFrom(functionType))
			function = JPQLFunction.create("CURRENT_DATE", false);
		if (CurrentTimestamp.class.isAssignableFrom(functionType))
			function = JPQLFunction.create("CURRENT_TIMESTAMP", false);
		if (CurrentLocalDateTime.class.isAssignableFrom(functionType))
			function = JPQLFunction.create("CURRENT_TIMESTAMP", false);
		if (Year.class.isAssignableFrom(functionType))
			function = JPQLFunction.create("YEAR", true);
		if (Month.class.isAssignableFrom(functionType))
			function = JPQLFunction.create("MONTH", true);
		if (Day.class.isAssignableFrom(functionType))
			function = JPQLFunction.create("DAY", true);
		if (Hour.class.isAssignableFrom(functionType))
			function = JPQLFunction.create("HOUR", true);

		return Optional.ofNullable(function);

	}

}
