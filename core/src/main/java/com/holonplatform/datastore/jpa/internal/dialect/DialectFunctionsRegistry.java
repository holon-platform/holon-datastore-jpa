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
package com.holonplatform.datastore.jpa.internal.dialect;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import com.holonplatform.core.internal.utils.ObjectUtils;
import com.holonplatform.core.query.QueryFunction;
import com.holonplatform.datastore.jpa.jpql.expression.JPQLFunction;

/**
 * A registry to provide dialect-specific {@link JPQLFunction}s.
 * 
 * @since 5.1.0
 */
public class DialectFunctionsRegistry implements Serializable {

	private static final long serialVersionUID = -5843159531217543210L;

	@SuppressWarnings("rawtypes")
	private final Map<Class<? extends QueryFunction>, JPQLFunction> functions = new HashMap<>();

	/**
	 * Register a {@link JPQLFunction} associated with given {@link QueryFunction} type.
	 * @param functionType Function type (not null)
	 * @param function JPQL function (not null)
	 */
	public void registerFunction(Class<? extends QueryFunction<?, ?>> functionType, JPQLFunction function) {
		ObjectUtils.argumentNotNull(functionType, "Function type must be not null");
		ObjectUtils.argumentNotNull(function, "JPQLFunction must be not null");
		functions.put(functionType, function);
	}

	/**
	 * Get the {@link JPQLFunction} associated with given {@link QueryFunction} class, if available.
	 * @param function Query function
	 * @return The {@link JPQLFunction} associated with given {@link QueryFunction} class, if available
	 */
	@SuppressWarnings("rawtypes")
	public Optional<JPQLFunction> getFunction(QueryFunction<?, ?> function) {
		for (Entry<Class<? extends QueryFunction>, JPQLFunction> entry : functions.entrySet()) {
			if (entry.getKey().isAssignableFrom(function.getClass())) {
				return Optional.ofNullable(entry.getValue());
			}
		}
		return Optional.empty();
	}

}
