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

import jakarta.persistence.Tuple;

import com.holonplatform.core.TypedExpression;
import com.holonplatform.core.exceptions.DataAccessException;
import com.holonplatform.core.property.Property;
import com.holonplatform.core.property.PropertyBox;
import com.holonplatform.core.property.PropertySet;

/**
 * {@link PropertyBox} {@link Tuple} converter.
 * 
 * @since 5.0.0
 */
public class PropertyBoxTupleConverter extends AbstractPropertyBoxConverter<Tuple> {

	/**
	 * Constructor.
	 * @param propertySet Property set to use to build the {@link PropertyBox} instance (not null)
	 * @param selection Query selection (not null)
	 * @param selectionAlias Selection aliases
	 * @param selectionProperties Selection properties
	 */
	public PropertyBoxTupleConverter(PropertySet<?> propertySet, TypedExpression<?>[] selection,
			Map<TypedExpression<?>, String> selectionAlias, Map<TypedExpression<?>, Property<?>> selectionProperties) {
		super(propertySet, selection, selectionAlias, selectionProperties);
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jpa.operation.JpaResultConverter#getQueryResultType()
	 */
	@Override
	public Class<? extends Tuple> getQueryResultType() {
		return Tuple.class;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jpa.internal.converters.AbstractPropertyBoxConverter#getResult(java.lang.Object,
	 * java.lang.String, int)
	 */
	@Override
	protected Object getResult(Tuple queryResult, String alias, int index) throws DataAccessException {
		try {
			if (alias == null) {
				return queryResult.get(index);
			}
			return queryResult.get(alias);
		} catch (Exception e) {
			throw new DataAccessException("Failed to obtain result from tuple [" + queryResult + "] at index [" + index
					+ "] with alias [" + alias + "]", e);
		}
	}

}
