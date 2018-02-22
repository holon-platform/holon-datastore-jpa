/*
 * Copyright 2016-2018 Axioma srl.
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
package com.holonplatform.datastore.jpa.test.suite;

import static com.holonplatform.datastore.jpa.test.model.TestDataModel.DAT;
import static com.holonplatform.datastore.jpa.test.model.TestDataModel.ENM;
import static com.holonplatform.datastore.jpa.test.model.TestDataModel.KEY;
import static com.holonplatform.datastore.jpa.test.model.TestDataModel.STR;
import static com.holonplatform.datastore.jpa.test.suite.AbstractJpaDatastoreTestSuite.JPA_TARGET;
import static com.holonplatform.datastore.jpa.test.suite.AbstractJpaDatastoreTestSuite.PROPERTIES;
import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Optional;

import org.junit.Test;

import com.holonplatform.core.Expression.InvalidExpressionException;
import com.holonplatform.core.ExpressionResolver;
import com.holonplatform.core.datastore.Datastore.OperationResult;
import com.holonplatform.core.property.PropertyBox;
import com.holonplatform.datastore.jpa.jpql.context.JPQLContextExpressionResolver;
import com.holonplatform.datastore.jpa.jpql.context.JPQLResolutionContext;
import com.holonplatform.datastore.jpa.jpql.expression.JPQLExpression;
import com.holonplatform.datastore.jpa.test.expression.KeyIsFilter;
import com.holonplatform.datastore.jpa.test.expression.StrKeySort;
import com.holonplatform.datastore.jpa.test.model.TestEnum;

public class CustomExpressionsTest extends AbstractJpaDatastoreSuiteTest {

	@Test
	public void testQueryFilter() {
		inTransaction(() -> {

			long count = getDatastore().query().target(JPA_TARGET).filter(new KeyIsFilter(1)).count();
			assertEquals(1, count);

			Optional<String> str = getDatastore().query().target(JPA_TARGET).filter(new KeyIsFilter(1)).findOne(STR);
			assertEquals("One", str.get());

			OperationResult result = getDatastore().bulkUpdate(JPA_TARGET).set(ENM, TestEnum.THIRD)
					.filter(new KeyIsFilter(1)).execute();
			assertEquals(1, result.getAffectedCount());

			result = getDatastore().bulkUpdate(JPA_TARGET).set(ENM, TestEnum.FIRST).filter(new KeyIsFilter(1))
					.execute();
			assertEquals(1, result.getAffectedCount());

			Optional<PropertyBox> pb = getDatastore().query().target(JPA_TARGET).filter(new KeyIsFilter(2))
					.findOne(PROPERTIES);
			assertEquals(TestEnum.SECOND, pb.get().getValue(ENM));

			result = getDatastore().bulkUpdate(JPA_TARGET).filter(new KeyIsFilter(2)).setNull(DAT).execute();
			assertEquals(1, result.getAffectedCount());

			pb = getDatastore().query().target(JPA_TARGET).filter(new KeyIsFilter(1)).findOne(PROPERTIES);
			assertEquals("One", pb.get().getValue(STR));

		});
	}

	@SuppressWarnings("serial")
	@Test
	public void testQueryFilterExpression() {
		final ExpressionResolver<KeyIsFilter, JPQLExpression> SQL_RESOLVER_ALIAS = new JPQLContextExpressionResolver<KeyIsFilter, JPQLExpression>() {

			@Override
			public Class<? extends KeyIsFilter> getExpressionType() {
				return KeyIsFilter.class;
			}

			@Override
			public Class<? extends JPQLExpression> getResolvedType() {
				return JPQLExpression.class;
			}

			@Override
			public Optional<JPQLExpression> resolve(KeyIsFilter expression, JPQLResolutionContext context)
					throws InvalidExpressionException {
				String path = context.isStatementCompositionContext().flatMap(ctx -> ctx.getAliasOrRoot(KEY))
						.map(alias -> alias + ".key").orElse("key");
				return Optional.of(JPQLExpression.create(path + " > " + expression.getValue()));
			}
		};

		Optional<String> str = getDatastore().query().withExpressionResolver(SQL_RESOLVER_ALIAS).target(JPA_TARGET)
				.filter(new KeyIsFilter(1)).findOne(STR);
		assertEquals("Two", str.get());
	}

	@Test
	public void testQuerySort() {
		List<Long> res = getDatastore().query().withExpressionResolver(StrKeySort.RESOLVER).target(JPA_TARGET)
				.sort(new StrKeySort()).list(KEY);
		assertEquals(2, res.size());
		assertEquals(Long.valueOf(2), res.get(0));
	}

}
