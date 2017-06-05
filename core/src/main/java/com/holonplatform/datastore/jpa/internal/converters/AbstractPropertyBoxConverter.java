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

import com.holonplatform.core.property.Property;
import com.holonplatform.core.property.PropertyBox;
import com.holonplatform.core.property.PropertySet;
import com.holonplatform.core.query.QueryResults.QueryResultConversionException;

/**
 * Abstract {@link PropertyBox} query results converter.
 * 
 * @param <Q> Query result type
 * 
 * @since 5.0.0
 */
public abstract class AbstractPropertyBoxConverter<Q> extends AbstractConverter<Q, PropertyBox> {

	private final PropertySet<?> propertySet;
	@SuppressWarnings("rawtypes")
	private final Property[] selection;
	private final Map<Property<?>, String> selectionAlias;

	public AbstractPropertyBoxConverter(PropertySet<?> propertySet, Property<?>[] selection,
			Map<Property<?>, String> selectionAlias) {
		super();
		this.propertySet = propertySet;
		this.selection = selection;
		this.selectionAlias = selectionAlias;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jpa.internal.jpql.expressions.QueryResultConverter#convert(java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public PropertyBox convert(Q result) throws QueryResultConversionException {
		try {
			PropertyBox.Builder builder = PropertyBox.builder(propertySet).invalidAllowed(true);
			for (int i = 0; i < selection.length; i++) {
				builder.setIgnoreReadOnly(selection[i],
						getResult(selection[i], result, selectionAlias.get(selection[i]), i));
			}
			return builder.build();
		} catch (QueryResultConversionException e) {
			throw e;
		} catch (Exception e) {
			throw new QueryResultConversionException(e);
		}
	}

	protected abstract Object getResult(Property<?> property, Q queryResult, String alias, int index)
			throws QueryResultConversionException;

}
