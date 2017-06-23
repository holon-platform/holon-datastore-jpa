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
package com.holonplatform.datastore.jpa.internal.beans;

import javax.annotation.Priority;
import javax.persistence.Temporal;

import com.holonplatform.core.beans.BeanProperty.Builder;
import com.holonplatform.core.beans.BeanPropertyPostProcessor;
import com.holonplatform.core.internal.Logger;
import com.holonplatform.core.temporal.TemporalType;
import com.holonplatform.datastore.jpa.internal.JpaDatastoreLogger;

/**
 * A {@link BeanPropertyPostProcessor} to setup a the property {@link TemporalType} using the JPA {@link Temporal}
 * annotation.
 *
 * @since 5.0.0
 */
@Priority(90)
public class JpaTemporalBeanPropertyPostProcessor implements BeanPropertyPostProcessor {

	/**
	 * Logger
	 */
	private static final Logger LOGGER = JpaDatastoreLogger.create();

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.beans.BeanPropertyPostProcessor#processBeanProperty(com.holonplatform.core.beans.
	 * BeanProperty.Builder, java.lang.Class)
	 */
	@Override
	public Builder<?> processBeanProperty(Builder<?> property, Class<?> beanOrNestedClass) {
		property.getAnnotation(Temporal.class).ifPresent(a -> {
			property.temporalType(convert(a.value()));
			LOGGER.debug(() -> "JpaTemporalBeanPropertyPostProcessor: setted property [" + property
					+ "] temporalType to: [" + a.value() + "]");
		});
		return property;
	}

	/**
	 * Convert a JPA {@link javax.persistence.TemporalType} enumeration value into a {@link TemporalType} value.
	 * @param temporalType JPA enumeration value
	 * @return {@link TemporalType} value
	 */
	private static TemporalType convert(javax.persistence.TemporalType temporalType) {
		if (temporalType != null) {
			switch (temporalType) {
			case DATE:
				return TemporalType.DATE;
			case TIME:
				return TemporalType.TIME;
			case TIMESTAMP:
				return TemporalType.DATE_TIME;
			default:
				break;
			}
		}
		return null;
	}

}
