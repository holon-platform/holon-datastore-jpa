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

import java.util.Optional;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.criteria.CriteriaBuilder;

import com.holonplatform.core.Expression.InvalidExpressionException;
import com.holonplatform.core.ExpressionResolver;
import com.holonplatform.core.datastore.DataTarget;
import com.holonplatform.core.datastore.Datastore;
import com.holonplatform.core.datastore.DatastoreCommodity;
import com.holonplatform.core.datastore.DatastoreCommodityContext.CommodityConfigurationException;
import com.holonplatform.core.property.NumericProperty;
import com.holonplatform.core.query.Query;
import com.holonplatform.core.query.QueryFilter;
import com.holonplatform.datastore.jpa.JpaDatastore;
import com.holonplatform.datastore.jpa.ORMPlatform;
import com.holonplatform.datastore.jpa.config.JpaDatastoreCommodityContext;
import com.holonplatform.datastore.jpa.config.JpaDatastoreCommodityFactory;
import com.holonplatform.datastore.jpa.jpql.context.JPQLResolutionContext;
import com.holonplatform.datastore.jpa.jpql.expression.JPQLExpression;
import com.holonplatform.datastore.jpa.jpql.expression.JPQLToken;

@SuppressWarnings({ "unused", "serial" })
public class ExampleJpaDatastoreExtension {

	// tag::commodity[]
	interface MyCommodity extends DatastoreCommodity { // <1>

		CriteriaBuilder getCriteriaBuilder();

	}

	class MyCommodityImpl implements MyCommodity { // <2>

		private final EntityManagerFactory entityManagerFactory;

		public MyCommodityImpl(EntityManagerFactory entityManagerFactory) {
			super();
			this.entityManagerFactory = entityManagerFactory;
		}

		@Override
		public CriteriaBuilder getCriteriaBuilder() {
			return entityManagerFactory.getCriteriaBuilder();
		}

	}

	class MyCommodityFactory implements JpaDatastoreCommodityFactory<MyCommodity> { // <3>

		@Override
		public Class<? extends MyCommodity> getCommodityType() {
			return MyCommodity.class;
		}

		@Override
		public MyCommodity createCommodity(JpaDatastoreCommodityContext context)
				throws CommodityConfigurationException {
			EntityManagerFactory entityManagerFactory = context.getEntityManagerFactory();
			return new MyCommodityImpl(entityManagerFactory);
		}

	}
	// end::commodity[]

	public void commodityFactory() {
		// tag::factoryreg[]
		Datastore datastore = JpaDatastore.builder() //
				.withCommodity(new MyCommodityFactory()) // <1>
				.build();
		// end::factoryreg[]
	}

	class MyExpressionResolver implements ExpressionResolver<JPQLToken, JPQLToken> {

		@Override
		public Optional<JPQLToken> resolve(JPQLToken expression, ResolutionContext context)
				throws InvalidExpressionException {
			return Optional.of(expression);
		}

		@Override
		public Class<? extends JPQLToken> getExpressionType() {
			return JPQLToken.class;
		}

		@Override
		public Class<? extends JPQLToken> getResolvedType() {
			return JPQLToken.class;
		}

	}

	public void expressionResolverRegistration() {
		// tag::expreg1[]
		Datastore datastore = JpaDatastore.builder() //
				.withExpressionResolver(new MyExpressionResolver()) // <1>
				.build();
		// end::expreg1[]

		// tag::expreg2[]
		datastore.addExpressionResolver(new MyExpressionResolver()); // <1>
		// end::expreg2[]

		// tag::expreg3[]
		long result = datastore.query().target(DataTarget.named("Test")) //
				.withExpressionResolver(new MyExpressionResolver()) // <1>
				.count();
		// end::expreg3[]
	}

	// tag::expres1[]
	class KeyIs implements QueryFilter {

		private final Long value;

		public KeyIs(Long value) {
			this.value = value;
		}

		public Long getValue() {
			return value;
		}

		@Override
		public void validate() throws InvalidExpressionException {
			if (value == null) {
				throw new InvalidExpressionException("Kay value must be not null");
			}
		}

	}
	// end::expres1[]

	public void expres2() {
		// tag::expres2[]
		final NumericProperty<Long> KEY = NumericProperty.create("key", long.class);

		final ExpressionResolver<KeyIs, JPQLExpression> keyIsResolver = ExpressionResolver.create( //
				KeyIs.class, // <1>
				JPQLExpression.class, // <2>
				(keyIs, context) -> {
					String path = JPQLResolutionContext.isJPQLResolutionContext(context)
							.flatMap(ctx -> ctx.isStatementCompositionContext()).flatMap(ctx -> ctx.getAliasOrRoot(KEY))
							.map(alias -> alias + ".key").orElse("key");
					return Optional.of(JPQLExpression.create(path + " = " + keyIs.getValue()));
				}); // <3>
		// end::expres2[]

		// tag::expres3[]
		Datastore datastore = JpaDatastore.builder().withExpressionResolver(keyIsResolver) // <1>
				.build();

		Query query = datastore.query().filter(new KeyIs(1L)); // <2>
		// end::expres3[]
	}

	final static NumericProperty<Long> A_PROPERTY = NumericProperty.create("aproperty", long.class);

	class SomeExpressionResolver implements ExpressionResolver<JPQLExpression, JPQLExpression> {

		// tag::context1[]
		@Override
		public Optional<JPQLExpression> resolve(JPQLExpression expression, ResolutionContext context) // <1>
				throws InvalidExpressionException {

			JPQLResolutionContext.isJPQLResolutionContext(context).ifPresent(ctx -> { // <2>
				ORMPlatform paltform = ctx.getORMPlatform().orElse(null); // <3>

				ctx.isStatementCompositionContext().ifPresent(sctx -> { // <4>
					Optional<String> alias = sctx.getAliasOrRoot(A_PROPERTY); // <5>
				});
			});

			return Optional.empty();
		}
		// end::context1[]

		@Override
		public Class<? extends JPQLExpression> getExpressionType() {
			return null;
		}

		@Override
		public Class<? extends JPQLExpression> getResolvedType() {
			return null;
		}

	}

}
