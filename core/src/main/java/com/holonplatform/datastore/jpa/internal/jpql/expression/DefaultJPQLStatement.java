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

import java.util.Collections;
import java.util.Map;

import com.holonplatform.core.internal.utils.ObjectUtils;
import com.holonplatform.datastore.jpa.jpql.expression.JPQLParameter;
import com.holonplatform.datastore.jpa.jpql.expression.JPQLStatement;

/**
 * Default {@link JPQLStatement} implementation.
 *
 * @since 5.1.0
 */
public class DefaultJPQLStatement implements JPQLStatement {

	/**
	 * Statement JPQL
	 */
	private final String jpql;

	/**
	 * Statement parameters
	 */
	private final Map<String, JPQLParameter<?>> parameters;

	public DefaultJPQLStatement(String jpql, Map<String, JPQLParameter<?>> parameters) {
		super();
		ObjectUtils.argumentNotNull(jpql, "JPQL statement must be not null");
		this.jpql = jpql;
		this.parameters = parameters;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jpa.expression.JPQLStatement#getJPQL()
	 */
	@Override
	public String getJPQL() {
		return jpql;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jpa.expression.JPQLStatement#getParameters()
	 */
	@Override
	public Map<String, JPQLParameter<?>> getParameters() {
		return (parameters != null) ? parameters : Collections.emptyMap();
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.Expression#validate()
	 */
	@Override
	public void validate() throws InvalidExpressionException {
		if (getJPQL() == null) {
			throw new InvalidExpressionException("Null JPQL statement");
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "DefaultJPQLStatement [jpql=" + jpql + ", parameters=" + parameters + "]";
	}

}
