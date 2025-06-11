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
package com.holonplatform.datastore.jpa.jpql.context;

import java.util.Optional;

import jakarta.persistence.Query;

import com.holonplatform.core.Expression;
import com.holonplatform.core.Expression.InvalidExpressionException;
import com.holonplatform.core.ExpressionResolver;
import com.holonplatform.core.ExpressionResolver.ExpressionResolverSupport;
import com.holonplatform.core.ExpressionResolver.ResolutionContext;
import com.holonplatform.core.internal.utils.ObjectUtils;
import com.holonplatform.datastore.jpa.context.JpaContext;
import com.holonplatform.datastore.jpa.internal.jpql.context.DefaultJPQLResolutionContext;
import com.holonplatform.datastore.jpa.jpql.expression.JPQLParameter;

/**
 * JPQL {@link ResolutionContext}.
 * <p>
 * Supports {@link ExpressionResolver}s to resolve JPQL expressions.
 * </p>
 * <p>
 * Supports named parameters definitions, which can be added using {@link #addNamedParameter(JPQLParameter)}. The named
 * parameters can provided to a query definition using {@link #setupQueryParameters(Query)}.
 * </p>
 * <p>
 * JPQL compostion contexts are hierarchical and provides methods to get the parent context and to create children using
 * {@link #childContext()}.
 * </p>
 *
 * @since 5.1.0
 */
public interface JPQLResolutionContext extends JpaContext, ResolutionContext, ExpressionResolverSupport {

	/**
	 * Get the parent context, if available.
	 * @return Optional parent context
	 */
	Optional<JPQLResolutionContext> getParent();

	/**
	 * Get the JPQL named parameters handler.
	 * @return the JPQL named parameters handler
	 */
	JPQLContextParametersHandler getNamedParametersHandler();

	/**
	 * Convenience method to add a named parameter using current {@link JPQLContextParametersHandler}.
	 * @param <T> Parameter expression type
	 * @param parameter Parameter definition (not null)
	 * @return The generated parameter name
	 */
	default <T> String addNamedParameter(JPQLParameter<T> parameter) {
		return getNamedParametersHandler().addNamedParameter(parameter);
	}

	/**
	 * Setup given <code>query</code> parameters using the named parameters defined in this context.
	 * @param query The JPQL query to configure (not null)
	 * @throws JPQLStatementPreparationException If an error occurred
	 */
	void setupQueryParameters(Query query);

	/**
	 * Try to resolve given <code>expression</code> using current context resolvers to obtain a
	 * <code>resolutionType</code> type expression.
	 * <p>
	 * The resolved expression is validate using {@link Expression#validate()} before returning it to caller.
	 * </p>
	 * @param <E> Expression type
	 * @param <R> Resolution type
	 * @param expression Expression to resolve
	 * @param resolutionType Expression type to obtain
	 * @return Resolved expression
	 */
	default <E extends Expression, R extends Expression> Optional<R> resolve(E expression, Class<R> resolutionType)
			throws InvalidExpressionException {
		// resolve
		return resolve(expression, resolutionType, this).map(e -> {
			// validate
			e.validate();
			return e;
		});
	}

	/**
	 * Resolve given <code>expression</code> using current context resolvers to obtain a <code>resolutionType</code>
	 * type expression. If no {@link ExpressionResolver} is available to resolve given expression, an
	 * {@link InvalidExpressionException} is thrown.
	 * <p>
	 * The resolved expression is validate using {@link Expression#validate()} before returning it to caller.
	 * </p>
	 * @param <E> Expression type
	 * @param <R> Resolution type
	 * @param expression Expression to resolve
	 * @param resolutionType Expression type to obtain
	 * @return Resolved expression
	 * @throws InvalidExpressionException If an error occurred during resolution, or if no {@link ExpressionResolver} is
	 *         available to resolve given expression or if expression validation failed
	 */
	default <E extends Expression, R extends Expression> R resolveOrFail(E expression, Class<R> resolutionType) {
		return resolve(expression, resolutionType)
				.orElseThrow(() -> new InvalidExpressionException("Failed to resolve expression [" + expression + "]"));
	}

	// builders

	/**
	 * Create a new {@link JPQLResolutionContext} as child of this context. This context will be setted as parent of the
	 * new context.
	 * @return A new {@link JPQLResolutionContext} with this context as parent
	 */
	JPQLResolutionContext childContext();

	/**
	 * Checks whether this context is a {@link JPQLStatementResolutionContext}.
	 * @return If this context is a {@link JPQLStatementResolutionContext} returns the context itself as
	 *         {@link JPQLStatementResolutionContext}, otherwise returns an empty Optional
	 */
	default Optional<JPQLStatementResolutionContext> isStatementCompositionContext() {
		return Optional.ofNullable(
				(this instanceof JPQLStatementResolutionContext) ? (JPQLStatementResolutionContext) this : null);
	}

	/**
	 * Create a new default {@link JPQLResolutionContext}.
	 * @param context JPA context to use (not null)
	 * @return A new {@link JPQLResolutionContext}
	 */
	static JPQLResolutionContext create(JpaContext context) {
		return new DefaultJPQLResolutionContext(context);
	}

	/**
	 * Checks if given {@link ResolutionContext} is a {@link JPQLResolutionContext}.
	 * @param context The context to check
	 * @return if given context is a {@link JPQLResolutionContext}, it is returned as a {@link JPQLResolutionContext}
	 *         type. Otherwise, an empty Optional is returned.
	 */
	static Optional<JPQLResolutionContext> isJPQLResolutionContext(ResolutionContext context) {
		if (context instanceof JPQLResolutionContext) {
			return Optional.of((JPQLResolutionContext) context);
		}
		return Optional.empty();
	}

	// Utils

	/**
	 * Get the given <code>context</code> hierarchy sequence, where <code>0</code> is the sequence number of the root
	 * context.
	 * @param context The context for which to obtain the sequence (not null)
	 * @param contextType The context type to take into account to calculate the sequence
	 * @return Context sequence
	 */
	public static int getContextSequence(JPQLResolutionContext context,
			Class<? extends JPQLResolutionContext> contextType) {
		ObjectUtils.argumentNotNull(context, "Context must be not null");
		final Class<?> type = (contextType != null) ? contextType : context.getClass();
		int sequence = -1;
		JPQLResolutionContext ctx = context;
		while (ctx != null) {
			if (type.isAssignableFrom(ctx.getClass())) {
				sequence++;
			}
			ctx = ctx.getParent().orElse(null);
		}
		return sequence;
	}

	// Exceptions

	/**
	 * Runtime exception related to JPQL statements preparation errors.
	 */
	public class JPQLStatementPreparationException extends RuntimeException {

		private static final long serialVersionUID = -3053162143629153499L;

		/**
		 * Constructor.
		 * @param message Error message
		 */
		public JPQLStatementPreparationException(String message) {
			super(message);
		}

		/**
		 * Constructor.
		 * @param message Error message
		 * @param cause The {@link Throwable} which caused this exception
		 */
		public JPQLStatementPreparationException(String message, Throwable cause) {
			super(message, cause);
		}

	}

}
