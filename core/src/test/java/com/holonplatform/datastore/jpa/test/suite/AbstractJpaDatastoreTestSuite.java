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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import com.holonplatform.core.datastore.Datastore;
import com.holonplatform.core.property.PathProperty;
import com.holonplatform.core.property.PropertySet;
import com.holonplatform.core.property.TemporalProperty;
import com.holonplatform.datastore.jpa.JpaTarget;
import com.holonplatform.datastore.jpa.ORMPlatform;

@RunWith(Suite.class)
@Suite.SuiteClasses({ PlatformConfigTest.class, QueryProjectionTest.class, QueryFilterTest.class, QuerySortTest.class,
		QueryAggregationTest.class, QueryRestrictionsTest.class, QueryJoinsTest.class, SubQueryTest.class,
		RefreshTest.class, InsertTest.class, UpdateTest.class, SaveTest.class, DeleteTest.class, ClobTest.class,
		BlobTest.class, BulkInsertTest.class, BulkUpdateTest.class, BulkDeleteTest.class, BulkUpdateAliasTest.class,
		BulkDeleteAliasTest.class, AggregationFunctionsTest.class, StringFunctionsTest.class,
		TemporalFunctionsTest.class, DataTargetResolverTest.class, CustomExpressionsTest.class,
		EntityProjectionTest.class, InsertedKeysTest.class, TransactionTest.class, QueryHintTest.class,
		LockModeTest.class, SharedEntityManagerTest.class })
public abstract class AbstractJpaDatastoreTestSuite {

	public static Datastore datastore;

	public static ORMPlatform platform;

	public static JpaTarget<?> JPA_TARGET;

	public static TemporalProperty<LocalDate> LDAT;
	public static TemporalProperty<LocalDateTime> LTMS;
	public static TemporalProperty<LocalTime> TIME;

	public static PropertySet<?> PROPERTIES;
	public static PropertySet<?> PROPERTIES_NOID;
	public static PropertySet<?> PROPERTIES_V;

	public static PropertySet<?> CLOB_SET_STR;

	public static JpaTarget<?> TEST3;

	public static PathProperty<Long> TEST3_CODE_P;
	public static PathProperty<String> TEST3_TEXT_P;

	public static boolean updateAliasTest = true;

	public static boolean rightJoinTest = true;

	public static boolean avgProjectionTest = true;

	public static boolean txExpectedErrorTest = true;

	public static boolean blobArrayProjectionTest = true;

	public static boolean entityProjectionTest = true;

	public static boolean temporalPartFunctionTest = true;

	public static boolean temporalProjectionTest = true;

	public static boolean transactionalTest = true;
	
	public static boolean saveOperationTest = true;

	public static boolean saveOperationTypeTest = true;

	public static boolean updateNullsTest = true;

	public static boolean enumProjectionTest = true;
	
	public static boolean updateWithFunctionTest = true;
	
	public static boolean updateNestedTest = true;
	
	public static boolean customFunctionExpressionTest = true;

}
