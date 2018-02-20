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
package com.holonplatform.datastore.jpa.jpql;

import com.holonplatform.core.temporal.TemporalType;
import com.holonplatform.datastore.jpa.internal.jpql.DefaultJPQLValueSerializer;

/**
 * JPQL constant value serializer.
 *
 * @since 5.1.0
 */
public interface JPQLValueSerializer {

	/**
	 * Serialize given value as a JPQL string.
	 * @param value Value to serialize
	 * @param temporalType Optional {@link TemporalType} to use with temporal values
	 * @return Serialized JPQL value
	 */
	String serialize(Object value, TemporalType temporalType);

	/**
	 * Get the default {@link JPQLValueSerializer}.
	 * @return the default {@link JPQLValueSerializer}
	 */
	static JPQLValueSerializer getDefault() {
		return DefaultJPQLValueSerializer.INSTANCE;
	}

}
