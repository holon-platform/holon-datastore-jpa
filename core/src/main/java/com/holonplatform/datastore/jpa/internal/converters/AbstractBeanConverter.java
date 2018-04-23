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
import com.holonplatform.core.internal.utils.ObjectUtils;
import com.holonplatform.datastore.jpa.context.JpaExecutionContext;
import com.holonplatform.datastore.jpa.jpql.JPQLValueDeserializer;

/**
 * Abstract bean query results converter.
 * 
 * @param <Q> Query result type
 * @param <T> Bean type
 * 
 * @since 5.0.0
 */
public abstract class AbstractBeanConverter<Q, T> extends AbstractResultConverter<Q, T> {

	/**
	 * Bean property set
	 */
	private final BeanPropertySet<T> beanPropertySet;

	/**
	 * Selection
	 */
	private final Path<?>[] selection;

	/**
	 * Selection alias
	 */
	private final Map<Path<?>, String> selectionAlias;

	/**
	 * Constructor.
	 * @param beanPropertySet Bean property set (not null)
	 * @param selection Selection paths (not null)
	 * @param selectionAlias Selection aliases
	 */
	public AbstractBeanConverter(BeanPropertySet<T> beanPropertySet, Path<?>[] selection,
			Map<Path<?>, String> selectionAlias) {
		super();
		ObjectUtils.argumentNotNull(beanPropertySet, "BeanPropertySet must be not null");
		ObjectUtils.argumentNotNull(selection, "Selection must be not null");
		this.beanPropertySet = beanPropertySet;
		this.selection = selection;
		this.selectionAlias = selectionAlias;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jpa.operation.JpaResultConverter#getConversionType()
	 */
	@Override
	public Class<? extends T> getConversionType() {
		return beanPropertySet.getBeanClass();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.holonplatform.datastore.jpa.operation.JpaResultConverter#convert(com.holonplatform.datastore.jpa.context.
	 * JpaExecutionContext, java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public T convert(JpaExecutionContext context, Q result) throws DataAccessException {

		final JPQLValueDeserializer deserializer = context.getValueDeserializer();

		T instance;
		try {
			instance = beanPropertySet.getBeanClass().newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new DataAccessException("Failed to istantiate bean class [" + beanPropertySet.getBeanClass() + "]",
					e);
		}

		LOGGER.debug(() -> "Convert result to a bean instance of type [" + beanPropertySet.getBeanClass() + "]");

		for (int i = 0; i < selection.length; i++) {

			final Path<?> expression = selection[i];

			// result index and alias
			final int index = i;
			final String alias = selectionAlias.get(expression);

			// get result value
			final Object value = getResult(result, alias, index);

			LOGGER.debug(() -> "Result value for selection alias [" + alias + "] at index [" + index + "] is [" + value
					+ "]");

			// deserialize value
			Object deserialized = deserializer.deserialize(context, expression, value);

			LOGGER.debug(() -> "Deserialized value for selection alias [" + alias + "] at index [" + index + "] is ["
					+ deserialized + "]");

			// write value in bean instance
			beanPropertySet.write((Path<Object>) expression, deserialized, instance);
		}

		return instance;
	}

}
