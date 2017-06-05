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
import javax.persistence.Column;

import com.holonplatform.core.beans.BeanProperty.Builder;
import com.holonplatform.core.beans.BeanPropertyPostProcessor;
import com.holonplatform.core.internal.Logger;
import com.holonplatform.datastore.jpa.internal.JpaDatastoreLogger;
import com.holonplatform.datastore.jpa.internal.JpaPropertyConfiguration;

/**
 * A {@link BeanPropertyPostProcessor} to setup property configuration attributes using JPA annotations such as
 * {@link Column}.
 *
 * @since 5.0.0
 */
@Priority(80)
public class JpaConfigBeanPropertyPostProcessor implements BeanPropertyPostProcessor {

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
		final String columnName = property.getAnnotation(Column.class).map(a -> a.name()).orElse(property.getName());
		property.configuration(JpaPropertyConfiguration.COLUMN_NAME, columnName);
		LOGGER.debug(() -> "JpaConfigBeanPropertyPostProcessor: setted property [" + property + "] configuration key ["
				+ JpaPropertyConfiguration.COLUMN_NAME.getKey() + "] to [" + columnName + "]");
		return property;
	}

}
