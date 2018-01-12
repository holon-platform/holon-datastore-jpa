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
package com.holonplatform.datastore.jpa.internal.expressions;

import com.holonplatform.core.datastore.DataTarget;
import com.holonplatform.core.query.QueryExpression;

/**
 * A {@link QueryExpression} representing a {@link DataTarget}.
 * 
 * @param <T> Target type
 * 
 * @since 5.1.0
 */
public class SimpleTargetExpression<T> implements DataTarget<T>, QueryExpression<T> {

	private static final long serialVersionUID = -973694465488642639L;

	private final DataTarget<T> target;

	public SimpleTargetExpression(DataTarget<T> target) {
		super();
		this.target = target;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.Path#getName()
	 */
	@Override
	public String getName() {
		return target.getName();
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.Path#getType()
	 */
	@Override
	public Class<? extends T> getType() {
		return target.getType();
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.Expression#validate()
	 */
	@Override
	public void validate() throws InvalidExpressionException {
	}

}
