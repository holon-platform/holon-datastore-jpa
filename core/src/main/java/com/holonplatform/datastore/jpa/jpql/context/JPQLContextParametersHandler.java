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
package com.holonplatform.datastore.jpa.jpql.context;

import java.io.Serializable;
import java.util.Map;

import com.holonplatform.datastore.jpa.internal.jpql.context.DefaultJPQLContextParametersHandler;
import com.holonplatform.datastore.jpa.jpql.expression.JPQLParameter;

/**
 * Handler to generate, hold and obtain the JPQL named parameters of a context hierarchy.
 *
 * @since 5.1.0
 */
public interface JPQLContextParametersHandler extends Serializable {

	/**
	 * Add named parameter using given {@link JPQLParameter} definition.
	 * @param <T> Parameter expression type
	 * @param parameter Parameter definition (not null)
	 * @return Generated parameter name
	 */
	<T> String addNamedParameter(JPQLParameter<T> parameter);

	/**
	 * Get the named parameters.
	 * @return A map of parameter names and the associated {@link JPQLParameter} definition, empty if none
	 */
	Map<String, JPQLParameter<?>> getNamedParameters();

	/**
	 * Create a new {@link JPQLContextParametersHandler}.
	 * @return A new {@link JPQLContextParametersHandler}
	 */
	static JPQLContextParametersHandler create() {
		return new DefaultJPQLContextParametersHandler();
	}

}
