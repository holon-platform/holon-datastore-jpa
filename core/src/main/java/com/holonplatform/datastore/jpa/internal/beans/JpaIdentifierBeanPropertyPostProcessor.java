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
package com.holonplatform.datastore.jpa.internal.beans;

import javax.annotation.Priority;
import javax.persistence.EmbeddedId;
import javax.persistence.Id;

import com.holonplatform.core.beans.BeanProperty;
import com.holonplatform.core.beans.BeanProperty.Builder;
import com.holonplatform.core.beans.BeanPropertyPostProcessor;
import com.holonplatform.core.internal.Logger;
import com.holonplatform.datastore.jpa.internal.JpaDatastoreLogger;

/**
 * A {@link BeanPropertyPostProcessor} to set a bean property as identifier using the JPA {@link Id} annotation.
 *
 * @since 5.1.0
 */
@Priority(95)
public class JpaIdentifierBeanPropertyPostProcessor implements BeanPropertyPostProcessor {

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
		// check Id
		property.getAnnotation(Id.class).ifPresent(a -> {
			property.identifier(true);
			LOGGER.debug(
					() -> "JpaIdentifierBeanPropertyPostProcessor: property [" + property + "] setted as identifier");
		});
		// check EmbeddedId
		property.getParent().ifPresent(parent -> {
			if (parent instanceof BeanProperty && ((BeanProperty<?>) parent).hasAnnotation(EmbeddedId.class)) {
				property.identifier(true);
				LOGGER.debug(() -> "JpaIdentifierBeanPropertyPostProcessor: property [" + property
						+ "] setted as identifier since part of an EmbeddedId");
			}
		});
		return property;
	}

}
