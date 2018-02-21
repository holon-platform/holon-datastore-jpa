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

import static com.holonplatform.datastore.jpa.test.model.entity.TestTx.TEST_TX;
import static com.holonplatform.datastore.jpa.test.model.entity.TestTx.TX_CODE;
import static com.holonplatform.datastore.jpa.test.model.entity.TestTx.TX_TARGET;
import static com.holonplatform.datastore.jpa.test.model.entity.TestTx.TX_TEXT;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.holonplatform.core.datastore.Datastore.OperationResult;
import com.holonplatform.core.datastore.transaction.TransactionConfiguration;
import com.holonplatform.core.exceptions.DataAccessException;
import com.holonplatform.core.internal.utils.TestUtils;
import com.holonplatform.core.property.PropertyBox;
import com.holonplatform.datastore.jpa.context.EntityManagerHandler;

public class TransactionTest extends AbstractJpaDatastoreSuiteTest {

	@Test
	public void testTransactional() {

		long count = getDatastore().query().target(TX_TARGET).count();
		Assert.assertEquals(1L, count);

		getDatastore().requireTransactional().withTransaction(tx -> {
			PropertyBox box = PropertyBox.builder(TEST_TX).set(TX_CODE, 2L).set(TX_TEXT, "Two").build();
			getDatastore().insert(TX_TARGET, box);

			long cnt = getDatastore().query().target(TX_TARGET).count();
			Assert.assertEquals(2L, cnt);

			tx.rollback();
		});

		count = getDatastore().query().target(TX_TARGET).count();
		Assert.assertEquals(1L, count);

		getDatastore().requireTransactional().withTransaction(tx -> {
			PropertyBox box = PropertyBox.builder(TX_CODE, TX_TEXT).set(TX_CODE, 2L).set(TX_TEXT, "Two").build();
			getDatastore().insert(TX_TARGET, box);

			long cnt = getDatastore().query().target(TX_TARGET).count();
			Assert.assertEquals(2L, cnt);

			tx.commit();
		});

		count = getDatastore().query().target(TX_TARGET).count();
		Assert.assertEquals(2L, count);

		// test auto commit

		getDatastore().requireTransactional().withTransaction(tx -> {
			PropertyBox box = PropertyBox.builder(TX_CODE, TX_TEXT).set(TX_CODE, 3L).set(TX_TEXT, "Three").build();
			getDatastore().insert(TX_TARGET, box);
		}, TransactionConfiguration.withAutoCommit());

		count = getDatastore().query().target(TX_TARGET).count();
		Assert.assertEquals(3L, count);

		// rollback

		getDatastore().requireTransactional().withTransaction(tx -> {
			PropertyBox box = PropertyBox.builder(TX_CODE, TX_TEXT).set(TX_CODE, 4L).set(TX_TEXT, "ToRollback").build();
			getDatastore().insert(TX_TARGET, box);

			tx.rollback();
		});

		count = getDatastore().query().target(TX_TARGET).count();
		Assert.assertEquals(3L, count);

		// rollback on error

		TestUtils.expectedException(DataAccessException.class,
				() -> getDatastore().requireTransactional().withTransaction(tx -> {
					PropertyBox box = PropertyBox.builder(TX_CODE, TX_TEXT).set(TX_TEXT, "ToRollback").build();
					getDatastore().insert(TX_TARGET, box);

					tx.commit();
				}));

		count = getDatastore().query().target(TX_TARGET).count();
		Assert.assertEquals(3L, count);

		// test nested

		getDatastore().requireTransactional().withTransaction(tx -> {
			String txt = getDatastore().query().target(TX_TARGET).filter(TX_CODE.eq(2L)).findOne(TX_TEXT).orElse(null);
			Assert.assertEquals("Two", txt);

			OperationResult res = getDatastore().bulkUpdate(TX_TARGET).set(TX_TEXT, "Two*").filter(TX_CODE.eq(2L))
					.execute();
			Assert.assertEquals(1, res.getAffectedCount());

			String val = ((EntityManagerHandler) getDatastore()).withEntityManager(em -> {
				List<String> rs = em.createQuery("SELECT t.text FROM TestTx t WHERE t.code=2", String.class).getResultList();
				return (rs.size() > 0) ? rs.get(0) : null;
			});
			Assert.assertEquals("Two*", val);

			tx.rollback();
		});

		String txtv = getDatastore().query().target(TX_TARGET).filter(TX_CODE.eq(2L)).findOne(TX_TEXT).orElse(null);
		Assert.assertEquals("Two", txtv);

		getDatastore().requireTransactional().withTransaction(tx -> {

			OperationResult res = getDatastore().bulkUpdate(TX_TARGET).set(TX_TEXT, "Two_tx1").filter(TX_CODE.eq(2L))
					.execute();
			Assert.assertEquals(1, res.getAffectedCount());

			getDatastore().requireTransactional().withTransaction(tx2 -> {

				String txt = getDatastore().query().target(TX_TARGET).filter(TX_CODE.eq(2L)).findOne(TX_TEXT)
						.orElse(null);
				Assert.assertEquals("Two", txt);

				tx2.commit();
			});

			String txt = getDatastore().query().target(TX_TARGET).filter(TX_CODE.eq(2L)).findOne(TX_TEXT).orElse(null);
			Assert.assertEquals("Two_tx1", txt);

			tx.rollback();
		});

		txtv = getDatastore().query().target(TX_TARGET).filter(TX_CODE.eq(2L)).findOne(TX_TEXT).orElse(null);
		Assert.assertEquals("Two", txtv);

	}

}
