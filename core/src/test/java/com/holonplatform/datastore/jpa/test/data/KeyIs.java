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
package com.holonplatform.datastore.jpa.test.data;

import java.util.Optional;

import com.holonplatform.core.property.PathProperty;
import com.holonplatform.core.query.QueryFilter;

@SuppressWarnings("serial")
public class KeyIs implements QueryFilter {

	public static final QueryFilterResolver<KeyIs> RESOLVER = new Resolver();

	private final long value;

	public KeyIs(long value) {
		super();
		this.value = value;
	}

	public long getValue() {
		return value;
	}

	@Override
	public void validate() throws InvalidExpressionException {
	}

	public static final class Resolver implements QueryFilterResolver<KeyIs> {

		private final static PathProperty<Long> KEY = PathProperty.create("key", long.class);

		/*
		 * (non-Javadoc)
		 * @see com.holonplatform.core.ExpressionResolver#getExpressionType()
		 */
		@Override
		public Class<? extends KeyIs> getExpressionType() {
			return KeyIs.class;
		}

		/*
		 * (non-Javadoc)
		 * @see com.holonplatform.core.Expression.ExpressionResolverFunction#resolve(com.holonplatform.core.Expression,
		 * com.holonplatform.core.ExpressionResolver.ResolutionContext)
		 */
		@Override
		public Optional<QueryFilter> resolve(KeyIs expression,
				com.holonplatform.core.ExpressionResolver.ResolutionContext context) throws InvalidExpressionException {
			return Optional.of(KEY.eq(expression.getValue()));
		}

	}

}
