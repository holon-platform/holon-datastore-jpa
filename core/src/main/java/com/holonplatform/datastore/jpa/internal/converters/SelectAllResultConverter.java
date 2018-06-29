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
package com.holonplatform.datastore.jpa.internal.converters;

import java.util.HashMap;
import java.util.Map;

import com.holonplatform.core.beans.BeanPropertySet;
import com.holonplatform.core.exceptions.DataAccessException;
import com.holonplatform.core.property.PathProperty;
import com.holonplatform.core.property.PropertyBox;
import com.holonplatform.datastore.jpa.context.JpaExecutionContext;
import com.holonplatform.datastore.jpa.jpql.JPQLResultConverter;

/**
 * Select all projection result converter.
 *
 * @since 5.2.0
 */
public class SelectAllResultConverter<T> implements JPQLResultConverter<T, Map<String, Object>> {

	private final Class<? extends T> queryResultType;

	private final BeanPropertySet<T> beanPropertySet;

	public SelectAllResultConverter(Class<? extends T> queryResultType) {
		super();
		this.queryResultType = queryResultType;
		this.beanPropertySet = BeanPropertySet.create(queryResultType);
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jpa.jpql.JPQLResultConverter#getQueryResultType()
	 */
	@Override
	public Class<? extends T> getQueryResultType() {
		return queryResultType;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jpa.jpql.JPQLResultConverter#getConversionType()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Class<? extends Map<String, Object>> getConversionType() {
		return (Class<? extends Map<String, Object>>) (Class<?>) Map.class;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jpa.jpql.JPQLResultConverter#convert(com.holonplatform.datastore.jpa.context.
	 * JpaExecutionContext, java.lang.Object)
	 */
	@Override
	public Map<String, Object> convert(JpaExecutionContext context, T result) throws DataAccessException {
		if (result != null) {

			PropertyBox values = beanPropertySet.read(result);

			final Map<String, Object> map = new HashMap<>(values.size());

			for (PathProperty<?> property : beanPropertySet) {
				map.put(property.relativeName(), values.contains(property) ? values.getValue(property) : null);
			}

			return map;
		}
		return null;
	}

}
