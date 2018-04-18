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

import java.util.Collections;
import java.util.List;

import com.holonplatform.core.TypedExpression;
import com.holonplatform.core.query.QueryFunction;

public class TrimFunction implements QueryFunction<String, String> {

	private final TypedExpression<String> expression;

	public TrimFunction(TypedExpression<String> expression) {
		super();
		this.expression = expression;
	}

	@Override
	public Class<? extends String> getType() {
		return String.class;
	}

	@Override
	public void validate() throws InvalidExpressionException {
		if (expression == null) {
			throw new InvalidExpressionException("Null function expression");
		}

	}

	@Override
	public List<TypedExpression<? extends String>> getExpressionArguments() {
		return Collections.singletonList(expression);
	}

}
