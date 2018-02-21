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
package com.holonplatform.datastore.jpa.test.suite;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import com.holonplatform.core.datastore.Datastore;

@RunWith(Suite.class)
@Suite.SuiteClasses({ QueryProjectionTest.class, QueryFilterTest.class, QuerySortTest.class, QueryAggregationTest.class,
		QueryRestrictionsTest.class, QueryJoinsTest.class, SubQueryTest.class, RefreshTest.class, InsertTest.class,
		UpdateTest.class, SaveTest.class, DeleteTest.class, ClobTest.class, BlobTest.class, BulkInsertTest.class,
		BulkUpdateTest.class, BulkDeleteTest.class, BulkUpdateAliasTest.class, BulkDeleteAliasTest.class,
		AggregationFunctionsTest.class, StringFunctionsTest.class, TemporalFunctionsTest.class,
		DataTargetResolverTest.class, CustomExpressionsTest.class, EntityProjectionTest.class, InsertedKeysTest.class,
		TransactionTest.class, QueryHintTest.class, LockModeTest.class })
public abstract class AbstractJpaDatastoreTestSuite {

	public static Datastore datastore;

	public static boolean updateAliasTest = true;

	public static boolean rightJoinTest = true;

	public static boolean avgProjectionTest = true;

}
