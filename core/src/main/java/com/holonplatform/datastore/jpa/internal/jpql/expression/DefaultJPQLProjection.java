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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.holonplatform.core.internal.Logger;
import com.holonplatform.core.internal.utils.ObjectUtils;
import com.holonplatform.datastore.jpa.internal.JpaDatastoreLogger;
import com.holonplatform.datastore.jpa.jpql.JPQLResultConverter;
import com.holonplatform.datastore.jpa.jpql.context.JPQLResolutionContext;
import com.holonplatform.datastore.jpa.jpql.expression.JPQLProjection;

/**
 * Default {@link JPQLProjection} implementation.
 * 
 * @param <Q> Query result type
 * @param <R> Projection result type
 *
 * @since 5.0.0
 */
public class DefaultJPQLProjection<Q, R> implements JPQLProjection<Q, R> {

	private final static Logger LOGGER = JpaDatastoreLogger.create();

	/**
	 * Allowed alias characters
	 */
	private static final String ALIAS_CHARS = "abcdefghijklmnopqrstuvwxyw0123456789_";

	/**
	 * Query result type
	 */
	private final Class<? extends Q> queryResultType;

	/**
	 * Projection type
	 */
	private final Class<? extends R> projectionType;

	/**
	 * Alias main sequence
	 */
	private final int aliasMainSequence;

	/**
	 * Selection expressions
	 */
	private final List<String> selections = new ArrayList<>();

	/**
	 * Selection expressions aliases
	 */
	private final Map<String, String> aliases = new HashMap<>();

	/**
	 * Results converter
	 */
	private JPQLResultConverter<? super Q, R> converter;

	// Alias generation

	private int aliasCounter = 0;
	private Map<String, Integer> generatedSelection = new HashMap<>();
	private Map<String, Integer> generatedAlias = new HashMap<>();

	/**
	 * Contructor.
	 * @param context JPQL context from which to obtain the generated alias main sequence, which will be appended to any
	 *        generated selection alias name (not null)
	 * @param queryResultType Query result type (not null)
	 * @param projectionType Projection type (not null)
	 */
	public DefaultJPQLProjection(JPQLResolutionContext context, Class<? extends Q> queryResultType,
			Class<? extends R> projectionType) {
		this(JPQLResolutionContext.getContextSequence(context, JPQLResolutionContext.class), queryResultType,
				projectionType);
	}

	/**
	 * Constructor
	 * @param aliasMainSequence Selection expressions alias main sequence, which will be appended to the alias name when
	 *        it is auto-generated
	 * @param queryResultType Query result type (not null)
	 * @param projectionType Projection type (not null)
	 */
	public DefaultJPQLProjection(int aliasMainSequence, Class<? extends Q> queryResultType,
			Class<? extends R> projectionType) {
		super();
		ObjectUtils.argumentNotNull(queryResultType, "Query result type must be not null");
		ObjectUtils.argumentNotNull(projectionType, "Projection type must be not null");
		this.queryResultType = queryResultType;
		this.projectionType = projectionType;
		this.aliasMainSequence = aliasMainSequence;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jpa.internal.jpql.expressions.ProjectionContext#getSelection()
	 */
	@Override
	public List<String> getSelection() {
		return selections;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.holonplatform.datastore.jpa.internal.jpql.expressions.ProjectionContext#getSelectionAlias(java.lang.String)
	 */
	@Override
	public Optional<String> getSelectionAlias(String selection) {
		return Optional.ofNullable(aliases.get(selection));
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.TypedExpression#getType()
	 */
	@Override
	public Class<? extends R> getType() {
		return projectionType;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jpa.jpql.expression.JPQLProjection#getQueryResultType()
	 */
	@Override
	public Class<? extends Q> getQueryResultType() {
		return queryResultType;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jpa.internal.jpql.expressions.ProjectionContext#getConverter()
	 */
	@Override
	public Optional<JPQLResultConverter<? super Q, R>> getConverter() {
		return Optional.ofNullable(converter);
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.Expression#validate()
	 */
	@Override
	public void validate() throws InvalidExpressionException {
		if (getSelection() == null || getSelection().isEmpty()) {
			throw new InvalidExpressionException("Null or empty selection");
		}
	}

	/**
	 * Set the result converter.
	 * @param converter The converter to set
	 */
	public void setConverter(JPQLResultConverter<? super Q, R> converter) {
		this.converter = converter;
	}

	/**
	 * Add a selection expression with selection alias.
	 * @param selection Selection to add (not null)
	 * @param alias Selection alias (not null)
	 */
	public void addSelection(String selection, String alias) {
		ObjectUtils.argumentNotNull(selection, "Selection must be not null");
		if (selection.trim().equals("")) {
			throw new IllegalArgumentException("Selection must be not empty");
		}
		ObjectUtils.argumentNotNull(alias, "Selection alias must be not null");

		selections.add(selection);
		aliases.put(selection, alias);

		LOGGER.debug(() -> "Added selection label [" + selection + "] with alias [" + alias + "]");
	}

	/**
	 * Add a selection expression, with selection alias auto-generation.
	 * @param selection Selection to add (not null)
	 * @return The generated selection alias
	 */
	public String addSelection(String selection) {
		return addSelection(selection, true);
	}

	/**
	 * Add a selection expression.
	 * @param selection Selection to add (not null)
	 * @param generateAlias Whether to generate an alias label for the selection expression
	 * @return If <code>generateAlias</code> was <code>true</code>, the generated selection alias. Given
	 *         <code>selection</code> otherwise.
	 */
	public String addSelection(String selection, boolean generateAlias) {

		ObjectUtils.argumentNotNull(selection, "Selection must be not null");
		if (selection.trim().equals("")) {
			throw new IllegalArgumentException("Selection must be not empty");
		}

		selections.add(selection);

		if (generateAlias) {
			String alias = generateAlias(selection);
			aliases.put(selection, alias);
			LOGGER.debug(() -> "Added selection label [" + selection + "] with alias [" + alias + "]");
			return alias;
		} else {
			LOGGER.debug(() -> "Added selection label [" + selection + "]");
			return selection;
		}
	}

	/**
	 * Generate an alias name for given selection expression
	 * @param selectionExpression Selection expression for which to generate the alias
	 * @return Generated alias
	 */
	protected String generateAlias(String selectionExpression) {

		String selection = selectionExpression;
		int idx = selectionExpression.lastIndexOf('.');
		if (selectionExpression.indexOf('(') < 0 && idx > -1 && idx < (selectionExpression.length() - 1)) {
			selection = selectionExpression.substring(idx + 1, selectionExpression.length());
		}

		aliasCounter++;

		StringBuilder sb = new StringBuilder();

		String prefix = ((selection.length() <= 4) ? selection : selection.substring(0, 4)).toLowerCase();
		char[] pa = prefix.toCharArray();
		char[] sanitized = new char[pa.length];
		for (int i = 0; i < pa.length; i++) {
			sanitized[i] = (ALIAS_CHARS.indexOf(pa[i]) > -1) ? pa[i] : '_';
		}

		sb.append(sanitized);

		if (aliasMainSequence > 0) {
			sb.append(aliasMainSequence);
			sb.append("_");
		}

		sb.append(aliasCounter);
		sb.append("_");

		int subcount = 0;
		if (generatedSelection.containsKey(selection)) {
			subcount = generatedSelection.get(selection) + 1;
		}
		generatedSelection.put(selection, subcount);
		sb.append(subcount);

		String partialAlias = sb.toString();

		int duplicateCount = 0;
		if (generatedAlias.containsKey(partialAlias)) {
			duplicateCount = generatedAlias.get(partialAlias) + 1;
		}
		generatedAlias.put(partialAlias, duplicateCount);

		if (duplicateCount > 0) {
			sb.append("_");
			sb.append(duplicateCount);
		}

		return sb.toString();
	}

}
