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
package com.holonplatform.datastore.jpa.internal.jpql.expression;

import java.util.Collections;
import java.util.Map;

import com.holonplatform.core.internal.utils.ObjectUtils;
import com.holonplatform.datastore.jpa.jpql.JPQLResultConverter;
import com.holonplatform.datastore.jpa.jpql.expression.JPQLParameter;
import com.holonplatform.datastore.jpa.jpql.expression.JPQLQuery;

/**
 * Default {@link JPQLQuery} implementation.
 * 
 * @param <Q> Query result type
 * @param <R> Conversion result type
 *
 * @since 5.1.0
 */
public class DefaultJPQLQuery<Q, R> extends DefaultJPQLStatement implements JPQLQuery<Q, R> {

	/**
	 * JPQL typed query result type
	 */
	private final Class<? extends Q> queryResultType;

	/**
	 * Result converter
	 */
	private final JPQLResultConverter<? super Q, R> resultConverter;

	/**
	 * Constructor.
	 * @param jpql JPQL statement (not null)
	 * @param queryResultType Query result type (not null)
	 * @param resultConverter Result converter (not null)
	 */
	public DefaultJPQLQuery(String jpql, Class<? extends Q> queryResultType,
			JPQLResultConverter<? super Q, R> resultConverter) {
		this(jpql, queryResultType, resultConverter, Collections.emptyMap());
	}

	/**
	 * Constructor with parameters.
	 * @param jpql JPQL statement (not null)
	 * @param queryResultType Query result type (not null)
	 * @param resultConverter Result converter (not null)
	 * @param parameters JPQL named parameters
	 */
	public DefaultJPQLQuery(String jpql, Class<? extends Q> queryResultType,
			JPQLResultConverter<? super Q, R> resultConverter, Map<String, JPQLParameter<?>> parameters) {
		super(jpql, parameters);
		ObjectUtils.argumentNotNull(queryResultType, "Query result type must be not null");
		ObjectUtils.argumentNotNull(resultConverter, "Result converter must be not null");
		this.queryResultType = queryResultType;
		this.resultConverter = resultConverter;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jpa.expression.JPQLQuery#getQueryResultType()
	 */
	@Override
	public Class<? extends Q> getQueryResultType() {
		return queryResultType;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jpa.expression.JPQLQuery#getResultConverter()
	 */
	@Override
	public JPQLResultConverter<? super Q, R> getResultConverter() {
		return resultConverter;
	}

}
