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
package com.holonplatform.datastore.jpa.examples;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import com.holonplatform.core.Expression.InvalidExpressionException;
import com.holonplatform.core.ExpressionResolver;
import com.holonplatform.core.TypedExpression;
import com.holonplatform.core.datastore.DataTarget;
import com.holonplatform.core.datastore.Datastore;
import com.holonplatform.core.property.StringProperty;
import com.holonplatform.core.query.QueryFunction;
import com.holonplatform.datastore.jpa.JpaDatastore;
import com.holonplatform.datastore.jpa.jpql.expression.JPQLFunction;

@SuppressWarnings({ "unused", "serial" })
public class ExampleJpaDatastoreFunction {

	// tag::function1[]
	public class Trim implements QueryFunction<String, String> {

		private final TypedExpression<String> expression; // <1>

		public Trim(TypedExpression<String> expression) {
			super();
			this.expression = expression;
		}

		@Override
		public Class<? extends String> getType() {
			return String.class;
		}

		@Override
		public void validate() throws InvalidExpressionException { // <2>
			if (expression == null) {
				throw new InvalidExpressionException("Null function expression");
			}

		}

		@Override
		public List<TypedExpression<? extends String>> getExpressionArguments() { // <3>
			return Collections.singletonList(expression);
		}

	}
	// end::function1[]

	// tag::function2[]
	public class TrimResolver implements ExpressionResolver<Trim, JPQLFunction> {

		@Override
		public Optional<JPQLFunction> resolve(Trim expression, ResolutionContext context)
				throws InvalidExpressionException {
			return Optional.of(JPQLFunction.create(args -> { // <1>
				StringBuilder sb = new StringBuilder();
				sb.append("TRIM(both from ");
				sb.append(args.get(0));
				sb.append(")");
				return sb.toString();
			}));
		}

		@Override
		public Class<? extends Trim> getExpressionType() {
			return Trim.class;
		}

		@Override
		public Class<? extends JPQLFunction> getResolvedType() {
			return JPQLFunction.class;
		}

	}
	// end::function2[]

	public void ifnull() {
		// tag::function3[]
		final StringProperty STR = StringProperty.create("stringAttribute");
		final DataTarget<?> TARGET = DataTarget.named("Test");

		Datastore datastore = JpaDatastore.builder() //
				.withExpressionResolver(new TrimResolver()) // <1>
				.build();

		Stream<String> trimmedValues = datastore.query(TARGET).stream(new Trim(STR)); // <2>
		// end::function3[]
	}

}
