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
import com.holonplatform.core.query.QueryResults.QueryResultConversionException;

/**
 * Abstract bean query results converter.
 * 
 * @param <Q> Query result type
 * @param <T> Bean type
 * 
 * @since 5.0.0
 */
public abstract class AbstractBeanConverter<Q, T> extends AbstractConverter<Q, T> {

	private final BeanPropertySet<T> beanPropertySet;
	@SuppressWarnings("rawtypes")
	private final Path[] selection;
	private final Map<Path<?>, String> selectionAlias;

	public AbstractBeanConverter(BeanPropertySet<T> beanPropertySet, Path<?>[] selection,
			Map<Path<?>, String> selectionAlias) {
		super();
		this.beanPropertySet = beanPropertySet;
		this.selection = selection;
		this.selectionAlias = selectionAlias;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jpa.internal.jpql.expressions.QueryResultConverter#convert(java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public T convert(Q result) throws QueryResultConversionException {
		try {

			T instance = beanPropertySet.getBeanClass().newInstance();

			for (int i = 0; i < selection.length; i++) {
				beanPropertySet.write(selection[i],
						getResult(selection[i], result, selectionAlias.get(selection[i]), i), instance);
			}

			return instance;

		} catch (InstantiationException | IllegalAccessException e) {
			throw new QueryResultConversionException("Unsupporterted bean projection type - bean class ["
					+ beanPropertySet.getBeanClass().getName() + "] must provide a public empty constructor", e);
		} catch (Exception e) {
			throw new QueryResultConversionException(
					"Failed to convert results using bean class [" + beanPropertySet.getBeanClass().getName() + "]", e);
		}
	}

	protected abstract Object getResult(Path<?> path, Q queryResult, String alias, int index)
			throws QueryResultConversionException;

}
