/*
 * Copyright 2000-2016 Holon TDCN.
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
import com.holonplatform.core.query.BeanProjection;
import com.holonplatform.core.query.QueryResults.QueryResultConversionException;

/**
 * {@link BeanProjection} results array converter.
 * 
 * @param <T> Bean type
 * 
 * @since 5.0.0
 */
public class BeanResultArrayConverter<T> extends AbstractBeanConverter<Object[], T> {

	public BeanResultArrayConverter(BeanPropertySet<T> beanPropertySet, Path<?>[] selection,
			Map<Path<?>, String> selectionAlias) {
		super(beanPropertySet, selection, selectionAlias);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.holonplatform.datastore.jpa.internal.jpql.converters.AbstractBeanConverter#getResult(com.holonplatform.core.
	 * Path, java.lang.Object, java.lang.String, int)
	 */
	@Override
	protected Object getResult(Path<?> path, Object[] queryResult, String alias, int index)
			throws QueryResultConversionException {
		if (index < 0 || index > (queryResult.length - 1)) {
			throw new QueryResultConversionException(
					"Invalid index [" + index + "] - Tuple size: " + queryResult.length);
		}
		return getResult(path, queryResult[index]);
	}

}
