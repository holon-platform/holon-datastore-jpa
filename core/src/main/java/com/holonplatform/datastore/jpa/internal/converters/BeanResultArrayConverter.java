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

import java.util.Map;

import com.holonplatform.core.Path;
import com.holonplatform.core.beans.BeanPropertySet;
import com.holonplatform.core.exceptions.DataAccessException;
import com.holonplatform.core.query.BeanProjection;

/**
 * {@link BeanProjection} results array converter.
 * 
 * @param <T> Bean type
 * 
 * @since 5.0.0
 */
public class BeanResultArrayConverter<T> extends AbstractBeanConverter<Object[], T> {

	/**
	 * Constructor.
	 * @param beanPropertySet Bean property set (not null)
	 * @param selection Selection paths (not null)
	 * @param selectionAlias Selection aliases
	 */
	public BeanResultArrayConverter(BeanPropertySet<T> beanPropertySet, Path<?>[] selection,
			Map<Path<?>, String> selectionAlias) {
		super(beanPropertySet, selection, selectionAlias);
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jpa.operation.JpaResultConverter#getQueryResultType()
	 */
	@Override
	public Class<? extends Object[]> getQueryResultType() {
		return Object[].class;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jpa.internal.converters.AbstractResultConverter#getResult(java.lang.Object,
	 * java.lang.String, int)
	 */
	@Override
	protected Object getResult(Object[] queryResult, String alias, int index) throws DataAccessException {
		if (index < 0 || index > (queryResult.length - 1)) {
			throw new DataAccessException("Invalid result index [" + index + "] - Tuple size: " + queryResult.length);
		}
		return queryResult[index];
	}

}
