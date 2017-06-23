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
package com.holonplatform.datastore.jpa.internal.resolvers;

import java.util.Optional;

import javax.annotation.Priority;

import com.holonplatform.core.Expression.InvalidExpressionException;
import com.holonplatform.core.ExpressionResolver;
import com.holonplatform.core.Path;
import com.holonplatform.core.datastore.DataTarget;
import com.holonplatform.datastore.jpa.internal.expressions.JPQLToken;
import com.holonplatform.datastore.jpa.internal.expressions.JpaResolutionContext;

/**
 * {@link Path} expression resolver.
 *
 * @since 5.0.0
 */
@SuppressWarnings("rawtypes")
@Priority(Integer.MAX_VALUE)
public enum PathResolver implements ExpressionResolver<Path, JPQLToken> {

	INSTANCE;

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.ExpressionResolver#getExpressionType()
	 */
	@Override
	public Class<? extends Path> getExpressionType() {
		return Path.class;
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
	public Optional<JPQLToken> resolve(Path expression, ResolutionContext context) throws InvalidExpressionException {

		final JpaResolutionContext ctx = JpaResolutionContext.checkContext(context);

		// intermediate resolution and validation
		Path<?> path = context.resolve(expression, Path.class, context).orElse(expression);
		path.validate();

		// data targets
		if (DataTarget.class.isAssignableFrom(path.getClass())) {
			return Optional.of(JPQLToken.create(ctx.getTargetAlias(expression).orElse(path.getName())));
		}

		// get path name
		final String name = path.relativeName();

		// ignore wildcard resolution
		if ("*".equals(name)) {
			return Optional.of(JPQLToken.create(name));
		}

		// Root parent
		Path<?> parent = path.getParent().orElse(null);
		while (parent != null && !DataTarget.class.isAssignableFrom(parent.getClass())) {
			parent = parent.getParent().orElse(null);
		}

		// resolve checking alias
		String jpql = ctx.getTargetAlias(parent).map(a -> a + "." + name).orElse(name);
		return Optional.of(JPQLToken.create(jpql));
	}

}
