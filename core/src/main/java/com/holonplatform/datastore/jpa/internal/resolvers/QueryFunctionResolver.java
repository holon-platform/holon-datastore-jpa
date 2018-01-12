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

import java.util.List;
import java.util.Optional;

import javax.annotation.Priority;

import com.holonplatform.core.Expression.InvalidExpressionException;
import com.holonplatform.core.ExpressionResolver;
import com.holonplatform.core.query.QueryExpression;
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
import com.holonplatform.datastore.jpa.ORMPlatform;
import com.holonplatform.datastore.jpa.internal.JpaDatastoreUtils;
import com.holonplatform.datastore.jpa.internal.expressions.JPQLToken;
import com.holonplatform.datastore.jpa.internal.expressions.JpaResolutionContext;

/**
 * JPA {@link QueryFunction} expression resolver.
 *
 * @since 5.0.0
 */
@SuppressWarnings("rawtypes")
@Priority(Integer.MAX_VALUE)
public enum QueryFunctionResolver implements ExpressionResolver<QueryFunction, JPQLToken> {

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
	public Class<? extends JPQLToken> getResolvedType() {
		return JPQLToken.class;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.ExpressionResolver#resolve(com.holonplatform.core.Expression,
	 * com.holonplatform.core.ExpressionResolver.ResolutionContext)
	 */
	@Override
	public Optional<JPQLToken> resolve(QueryFunction expression, ResolutionContext context)
			throws InvalidExpressionException {

		// validate
		expression.validate();

		// check no args
		if (CurrentDate.class.isAssignableFrom(expression.getClass()))
			return Optional.of(JPQLToken.create("CURRENT_DATE"));
		if (CurrentLocalDate.class.isAssignableFrom(expression.getClass()))
			return Optional.of(JPQLToken.create("CURRENT_DATE"));
		if (CurrentTimestamp.class.isAssignableFrom(expression.getClass()))
			return Optional.of(JPQLToken.create("CURRENT_TIMESTAMP"));
		if (CurrentLocalDateTime.class.isAssignableFrom(expression.getClass()))
			return Optional.of(JPQLToken.create("CURRENT_TIMESTAMP"));

		// resolve arguments
		@SuppressWarnings("unchecked")
		List<QueryExpression> arguments = expression.getExpressionArguments();

		final String functionArgument;
		if (arguments != null && !arguments.isEmpty()) {
			functionArgument = JpaDatastoreUtils.resolveExpression(context, arguments.get(0), JPQLToken.class, context)
					.getValue();
		} else {
			functionArgument = null;
		}

		final JpaResolutionContext jpaContext = JpaResolutionContext.checkContext(context);

		// resolve function
		return serializeFunction(expression, functionArgument, jpaContext.getORMPlatform().orElse(null)).map(f -> {
			return JPQLToken.create(f);
		});
	}

	private static Optional<String> serializeFunction(QueryFunction function, String argument, ORMPlatform platform) {
		if (Count.class.isAssignableFrom(function.getClass())) {
			return Optional.of("COUNT(" + argument + ")");
		}
		if (Avg.class.isAssignableFrom(function.getClass())) {
			return Optional.of("AVG(" + argument + ")");
		}
		if (Min.class.isAssignableFrom(function.getClass())) {
			return Optional.of("MIN(" + argument + ")");
		}
		if (Max.class.isAssignableFrom(function.getClass())) {
			return Optional.of("MAX(" + argument + ")");
		}
		if (Sum.class.isAssignableFrom(function.getClass())) {
			return Optional.of("SUM(" + argument + ")");
		}
		if (Lower.class.isAssignableFrom(function.getClass())) {
			return Optional.of("LOWER(" + argument + ")");
		}
		if (Upper.class.isAssignableFrom(function.getClass())) {
			return Optional.of("UPPER(" + argument + ")");
		}

		// Temporals
		boolean isHibernate = (platform != null && platform == ORMPlatform.HIBERNATE);
		if (Year.class.isAssignableFrom(function.getClass())) {
			return Optional.of(isHibernate ? ("YEAR(" + argument + ")") : "EXTRACT(YEAR FROM " + argument + ")");
		}
		if (Month.class.isAssignableFrom(function.getClass())) {
			return Optional.of(isHibernate ? ("MONTH(" + argument + ")") : "EXTRACT(MONTH FROM " + argument + ")");
		}
		if (Day.class.isAssignableFrom(function.getClass())) {
			return Optional.of(isHibernate ? ("DAY(" + argument + ")") : "EXTRACT(DAY FROM " + argument + ")");
		}
		if (Hour.class.isAssignableFrom(function.getClass())) {
			return Optional.of(isHibernate ? ("HOUR(" + argument + ")") : "EXTRACT(HOUR FROM " + argument + ")");
		}

		return Optional.empty();
	}

}
