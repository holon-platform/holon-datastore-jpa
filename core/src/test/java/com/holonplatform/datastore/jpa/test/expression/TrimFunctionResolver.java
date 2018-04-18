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
package com.holonplatform.datastore.jpa.test.expression;

import java.util.Optional;

import com.holonplatform.core.Expression.InvalidExpressionException;
import com.holonplatform.core.ExpressionResolver;
import com.holonplatform.datastore.jpa.jpql.expression.JPQLFunction;

@SuppressWarnings("serial")
public class TrimFunctionResolver implements ExpressionResolver<TrimFunction, JPQLFunction> {

	@Override
	public Optional<JPQLFunction> resolve(TrimFunction expression, ResolutionContext context)
			throws InvalidExpressionException {
		return Optional.of(JPQLFunction.create(args -> {
			StringBuilder sb = new StringBuilder();
			sb.append("TRIM(both from ");
			sb.append(args.get(0));
			sb.append(")");
			return sb.toString();
		}));
	}

	@Override
	public Class<? extends TrimFunction> getExpressionType() {
		return TrimFunction.class;
	}

	@Override
	public Class<? extends JPQLFunction> getResolvedType() {
		return JPQLFunction.class;
	}

}
