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
package com.holonplatform.datastore.jpa.internal.jpql;

import com.holonplatform.core.exceptions.DataAccessException;
import com.holonplatform.core.internal.utils.ObjectUtils;
import com.holonplatform.datastore.jpa.context.JpaExecutionContext;
import com.holonplatform.datastore.jpa.jpql.JPQLResultConverter;

/**
 * Identity result converter.
 * 
 * @param <T> Result type
 *
 * @since 5.1.0
 */
public class IdentityJPQLResultConverter<T> implements JPQLResultConverter<T, T> {

	private final Class<? extends T> type;

	/**
	 * Constructor.
	 * @param type Query result type (not null)
	 */
	public IdentityJPQLResultConverter(Class<? extends T> type) {
		super();
		ObjectUtils.argumentNotNull(type, "Query result type must be not null");
		this.type = type;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jpa.jpql.JPQLResultConverter#getQueryResultType()
	 */
	@Override
	public Class<? extends T> getQueryResultType() {
		return type;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jpa.jpql.JPQLResultConverter#getConversionType()
	 */
	@Override
	public Class<? extends T> getConversionType() {
		return type;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jpa.jpql.JPQLResultConverter#convert(com.holonplatform.datastore.jpa.context.
	 * JpaExecutionContext, java.lang.Object)
	 */
	@Override
	public T convert(JpaExecutionContext context, T result) throws DataAccessException {
		return result;
	}

}
