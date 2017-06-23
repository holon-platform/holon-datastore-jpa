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
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import com.holonplatform.core.beans.BeanProperty.Builder;
import com.holonplatform.core.beans.BeanPropertyPostProcessor;
import com.holonplatform.core.internal.Logger;
import com.holonplatform.core.property.PropertyValueConverter;
import com.holonplatform.datastore.jpa.internal.JpaDatastoreLogger;

/**
 * A {@link BeanPropertyPostProcessor} to setup a suitable enum {@link PropertyValueConverter} when the JPA
 * {@link Enumerated} annotation is detected on a bean property.
 *
 * @since 5.0.0
 */
@Priority(85)
public class JpaEnumeratedBeanPropertyPostProcessor implements BeanPropertyPostProcessor {

	/**
	 * Logger
	 */
	private static final Logger LOGGER = JpaDatastoreLogger.create();

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.beans.BeanPropertyPostProcessor#processBeanProperty(com.holonplatform.core.beans.
	 * BeanProperty.Builder, java.lang.Class)
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Builder<?> processBeanProperty(Builder<?> property, Class<?> beanOrNestedClass) {
		property.getAnnotation(Enumerated.class).ifPresent(a -> {
			final EnumType enumType = a.value();
			if (enumType == EnumType.STRING) {
				((Builder) property).converter(PropertyValueConverter.enumByName());
			} else {
				((Builder) property).converter(PropertyValueConverter.enumByOrdinal());
			}
			LOGGER.debug(() -> "JpaEnumeratedBeanPropertyPostProcessor: setted property [" + property
					+ "] value converter to default enumeration converter using [" + enumType.name() + "] mode");
		});
		return property;
	}

}
