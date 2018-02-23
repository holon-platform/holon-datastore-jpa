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
package com.holonplatform.datastore.jpa.internal.converters;

import com.holonplatform.core.exceptions.DataAccessException;
import com.holonplatform.core.internal.Logger;
import com.holonplatform.datastore.jpa.internal.JpqlDatastoreLogger;
import com.holonplatform.datastore.jpa.jpql.JPQLResultConverter;

/**
 * Abstract query results converter.
 * 
 * @param <Q> Query result type
 * @param <R> Expected result type
 * 
 * @since 5.1.0
 *
 */
public abstract class AbstractResultConverter<Q, R> implements JPQLResultConverter<Q, R> {

	/**
	 * Logger
	 */
	protected static final Logger LOGGER = JpqlDatastoreLogger.create();

	/**
	 * Get the result value which corresponds to given alias or index.
	 * @param queryResult Actual query result
	 * @param alias Result alias/label
	 * @param index Result index
	 * @return The result value
	 * @throws DataAccessException If an error occurred
	 */
	protected abstract Object getResult(Q queryResult, String alias, int index) throws DataAccessException;

}
