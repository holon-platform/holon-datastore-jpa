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

import java.util.Optional;

import com.holonplatform.core.internal.utils.ObjectUtils;
import com.holonplatform.core.temporal.TemporalType;
import com.holonplatform.datastore.jpa.jpql.expression.JPQLParameter;

/**
 * Default {@link JPQLParameter} implementation.
 *
 * @since 5.1.0
 */
public class DefaultJPQLParameter<T> implements JPQLParameter<T> {

	/**
	 * Parameter value
	 */
	private final T value;

	/**
	 * Parameter value type
	 */
	private final Class<? extends T> type;

	/**
	 * Optional temporal type
	 */
	private final TemporalType temporalType;

	/**
	 * Constructor.
	 * @param value Parameter value
	 * @param type Parameter value type (not null)
	 * @param temporalType Optional temporal type
	 */
	public DefaultJPQLParameter(T value, Class<? extends T> type, TemporalType temporalType) {
		super();
		ObjectUtils.argumentNotNull(type, "Parameter type must be not null");
		this.value = value;
		this.type = type;
		this.temporalType = temporalType;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.TypedExpression#getType()
	 */
	@Override
	public Class<? extends T> getType() {
		return type;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.TypedExpression#getTemporalType()
	 */
	@Override
	public Optional<TemporalType> getTemporalType() {
		return Optional.ofNullable(temporalType);
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jpa.expression.JPQLParameter#getValue()
	 */
	@Override
	public T getValue() {
		return value;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.Expression#validate()
	 */
	@Override
	public void validate() throws InvalidExpressionException {
		if (getType() == null) {
			throw new InvalidExpressionException("Null parameter type");
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "DefaultJPQLParameter [value=" + value + ", type=" + type + ", temporalType=" + temporalType + "]";
	}

}
