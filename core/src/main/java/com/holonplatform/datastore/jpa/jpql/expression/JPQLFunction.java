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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import com.holonplatform.core.Expression;
import com.holonplatform.datastore.jpa.internal.jpql.expression.CallbackJPQLFunction;
import com.holonplatform.datastore.jpa.internal.jpql.expression.DefaultJPQLFunction;
import com.holonplatform.datastore.jpa.internal.jpql.expression.ExtractJPQLFunction;

/**
 * {@link Expression} which represents a JPQL function.
 * 
 * @since 5.1.0
 */
public interface JPQLFunction extends Expression {

	/**
	 * Serialize the function as JPQL.
	 * @param arguments Optional function arguments
	 * @return Serialized function
	 * @throws InvalidExpressionException If the function arguments are not valid
	 */
	String serialize(List<String> arguments) throws InvalidExpressionException;

	/**
	 * Serialize the function as JPQL.
	 * @param arguments Function arguments
	 * @return Serialized function
	 */
	default String serialize(String... arguments) {
		return serialize((arguments != null) ? Arrays.asList(arguments) : Collections.emptyList());
	}

	/**
	 * Create a {@link JPQLFunction} using given <code>function</code> as serialization logic.
	 * @param function Callback function (not null)
	 * @return A new {@link JPQLFunction} using given <code>function</code> for serialization
	 */
	static JPQLFunction create(Function<List<String>, String> function) {
		return new CallbackJPQLFunction(function);
	}

	/**
	 * Create a default {@link JPQLFunction}.
	 * @param name Function name (not null)
	 * @param parenthesesIfNoArguments Whether parentheses are required if there are no arguments
	 * @return A new {@link JPQLFunction} with given name
	 */
	static JPQLFunction create(String name, boolean parenthesesIfNoArguments) {
		return new DefaultJPQLFunction(name, parenthesesIfNoArguments);
	}

	/**
	 * Create a default {@link JPQLFunction}. With this builder, parentheses are not added if there are no arguments.
	 * @param name Function name (not null)
	 * @return A new {@link JPQLFunction} with given name
	 */
	static JPQLFunction create(String name) {
		return create(name, false);
	}

	/**
	 * Create an <code>EXTRACT</code> function, which will be serialized this way:
	 * <code>EXTRACT(partName FROM argument)</code>.
	 * @param partName The EXTRACT function part name
	 * @return A new <code>EXTRACT</code> {@link JPQLFunction}
	 */
	static JPQLFunction extract(String partName) {
		return new ExtractJPQLFunction(partName);
	}

}
