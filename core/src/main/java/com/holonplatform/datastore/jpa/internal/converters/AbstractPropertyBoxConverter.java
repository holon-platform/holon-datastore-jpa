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

import java.util.Collections;
import java.util.Map;

import com.holonplatform.core.TypedExpression;
import com.holonplatform.core.exceptions.DataAccessException;
import com.holonplatform.core.internal.utils.ObjectUtils;
import com.holonplatform.core.property.Property;
import com.holonplatform.core.property.PropertyBox;
import com.holonplatform.core.property.PropertySet;
import com.holonplatform.datastore.jpa.context.JpaExecutionContext;
import com.holonplatform.datastore.jpa.jpql.JPQLValueDeserializer;

/**
 * Abstract {@link PropertyBox} query results converter.
 * 
 * @param <Q> Query result type
 * 
 * @since 5.0.0
 */
public abstract class AbstractPropertyBoxConverter<Q> extends AbstractResultConverter<Q, PropertyBox> {

	/**
	 * Property set
	 */
	private final PropertySet<?> propertySet;

	/**
	 * Selection
	 */
	private final TypedExpression<?>[] selection;

	/**
	 * Selection aliases
	 */
	private final Map<TypedExpression<?>, String> selectionAlias;

	/**
	 * Selection properties
	 */
	private final Map<TypedExpression<?>, Property<?>> selectionProperties;

	/**
	 * Constructor.
	 * @param propertySet Property set to use to build the {@link PropertyBox} instance (not null)
	 * @param selection Query selection (not null)
	 * @param selectionAlias Selection aliases
	 * @param selectionProperties Selection properties
	 */
	public AbstractPropertyBoxConverter(PropertySet<?> propertySet, TypedExpression<?>[] selection,
			Map<TypedExpression<?>, String> selectionAlias, Map<TypedExpression<?>, Property<?>> selectionProperties) {
		super();
		ObjectUtils.argumentNotNull(propertySet, "PropertySet must be not null");
		ObjectUtils.argumentNotNull(selection, "Selection must be not null");
		this.propertySet = propertySet;
		this.selection = selection;
		this.selectionAlias = (selectionAlias != null) ? selectionAlias : Collections.emptyMap();
		this.selectionProperties = (selectionProperties != null) ? selectionProperties : Collections.emptyMap();
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jpa.operation.JpaResultConverter#getConversionType()
	 */
	@Override
	public Class<? extends PropertyBox> getConversionType() {
		return PropertyBox.class;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.holonplatform.datastore.jpa.operation.JpaResultConverter#convert(com.holonplatform.datastore.jpa.context.
	 * JpaExecutionContext, java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public PropertyBox convert(JpaExecutionContext context, Q result) throws DataAccessException {

		final JPQLValueDeserializer deserializer = context.getValueDeserializer();

		try {
			// build the PropertyBox
			PropertyBox.Builder builder = PropertyBox.builder(propertySet).invalidAllowed(true);

			LOGGER.debug(() -> "Convert result to a PropertyBox using property set [" + propertySet + "]");

			for (int i = 0; i < selection.length; i++) {

				final TypedExpression<?> expression = selection[i];

				// result index and alias
				final int index = i;
				final String alias = selectionAlias.get(expression);

				// property
				final Property<?> property = selectionProperties.get(expression);

				if (property != null) {

					// get result value
					final Object value = getResult(result, alias, index);

					LOGGER.debug(() -> "Result value for selection alias [" + alias + "] at index [" + index + "] is ["
							+ value + "]");

					// deserialize value
					Object deserialized = deserializer.deserialize(context, expression, value);

					LOGGER.debug(() -> "Deserialized value for selection alias [" + alias + "] at index [" + index
							+ "] is [" + deserialized + "]");

					// set property value
					builder.setIgnoreReadOnly((Property<Object>) property, deserialized);

				} else {
					LOGGER.debug(() -> "No property available for selection [" + alias + "] at index [" + index
							+ "] - skip result value");
				}

			}
			return builder.build();
		} catch (DataAccessException e) {
			throw e;
		} catch (Exception e) {
			throw new DataAccessException("Failed to convert result [" + result + "] to PropertyBox ", e);
		}
	}

}
