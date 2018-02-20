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
package com.holonplatform.datastore.jpa.dialect;

import java.util.Optional;

import com.holonplatform.core.query.QueryFunction;
import com.holonplatform.core.query.TemporalFunction.Day;
import com.holonplatform.core.query.TemporalFunction.Hour;
import com.holonplatform.core.query.TemporalFunction.Month;
import com.holonplatform.core.query.TemporalFunction.Year;
import com.holonplatform.datastore.jpa.internal.dialect.DialectFunctionsRegistry;
import com.holonplatform.datastore.jpa.internal.jpql.expression.ExtractJPQLFunction;
import com.holonplatform.datastore.jpa.jpql.expression.JPQLFunction;

/**
 * Eclipselink {@link ORMDialect}.
 *
 * @since 5.1.0
 */
public class EclipselinkDialect implements ORMDialect {

	private final DialectFunctionsRegistry functions = new DialectFunctionsRegistry();

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jpa.dialect.ORMDialect#init(com.holonplatform.datastore.jpa.context.
	 * ORMDialectContext)
	 */
	@Override
	public void init(ORMDialectContext context) {
		this.functions.registerFunction(Year.class, new ExtractJPQLFunction("YEAR"));
		this.functions.registerFunction(Month.class, new ExtractJPQLFunction("MONTH"));
		this.functions.registerFunction(Day.class, new ExtractJPQLFunction("DAY"));
		this.functions.registerFunction(Hour.class, new ExtractJPQLFunction("HOUR"));
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.holonplatform.datastore.jpa.dialect.ORMDialect#resolveFunction(com.holonplatform.core.query.QueryFunction)
	 */
	@Override
	public Optional<JPQLFunction> resolveFunction(QueryFunction<?, ?> function) {
		return functions.getFunction(function);
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jpa.dialect.ORMDialect#isTupleSupported()
	 */
	@Override
	public boolean isTupleSupported() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jpa.dialect.ORMDialect#updateStatementAliasSupported()
	 */
	@Override
	public boolean updateStatementAliasSupported() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jpa.dialect.ORMDialect#updateStatementSetAliasSupported()
	 */
	@Override
	public boolean updateStatementSetAliasSupported() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jpa.dialect.ORMDialect#deleteStatementAliasSupported()
	 */
	@Override
	public boolean deleteStatementAliasSupported() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jpa.dialect.ORMDialect#getBatchSizeConfigurationProperty()
	 */
	@Override
	public Optional<String> getBatchSizeConfigurationProperty() {
		return Optional.of("eclipselink.jdbc.batch-writing.size");
	}

}
