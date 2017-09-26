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
package com.holonplatform.datastore.jpa.internal.expressions;

import com.holonplatform.core.query.QueryResults.QueryResultConversionException;

/**
 * Query result converter.
 * 
 * @param <Q> Query result type
 * @param <R> Expected result type
 * 
 * @since 5.0.0
 */
public interface QueryResultConverter<Q, R> {

	/**
	 * Convert a query result into expected result type.
	 * @param result Result to convert
	 * @return Converted result
	 * @throws QueryResultConversionException If the conversion failed
	 */
	R convert(Q result) throws QueryResultConversionException;

}
