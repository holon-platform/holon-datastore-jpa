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
package com.holonplatform.datastore.jpa.internal.support;

import java.util.Optional;

import com.holonplatform.core.internal.utils.ObjectUtils;
import com.holonplatform.core.temporal.TemporalType;

/**
 * Default {@link ParameterValue} representation.
 *
 * @since 5.0.0
 */
public class DefaultParameterValue implements ParameterValue {

	private static final long serialVersionUID = 7020457740819143239L;

	private final Class<?> type;
	private final Object value;

	private TemporalType temporalType;

	public DefaultParameterValue(Class<?> type, Object value) {
		super();
		ObjectUtils.argumentNotNull(type, "Value type must be not null");
		this.type = type;
		this.value = value;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jpa.internal.support.ParameterValue#getType()
	 */
	@Override
	public Class<?> getType() {
		return type;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jpa.internal.support.ParameterValue#getTemporalType()
	 */
	@Override
	public Optional<TemporalType> getTemporalType() {
		return Optional.ofNullable(temporalType);
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jpa.internal.support.ParameterValue#getValue()
	 */
	@Override
	public Object getValue() {
		return value;
	}

	/**
	 * Set the value {@link TemporalType}.
	 * @param temporalType the temporal type to set
	 */
	public void setTemporalType(TemporalType temporalType) {
		this.temporalType = temporalType;
	}

}
