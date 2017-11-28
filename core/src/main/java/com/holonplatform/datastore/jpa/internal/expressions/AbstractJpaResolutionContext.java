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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.holonplatform.core.Path;
import com.holonplatform.core.datastore.relational.Aliasable.AliasablePath;
import com.holonplatform.core.datastore.relational.RelationalTarget;
import com.holonplatform.core.internal.utils.ObjectUtils;

/**
 * Base {@link JpaResolutionContext} class.
 *
 * @since 5.0.0
 */
public abstract class AbstractJpaResolutionContext implements JpaResolutionContext {

	private static final String ALIAS_CHARS = "abcdefghijklmnopqrstuvwxyw0123456789_";

	private final JpaResolutionContext parent;

	private final AliasMode aliasMode;

	private RelationalTarget<?> target;

	private final Map<String, String> pathAlias = new HashMap<>(4);

	private Map<String, Integer> generatedAlias = new HashMap<>();

	private final int sequence;

	public AbstractJpaResolutionContext(JpaResolutionContext parent, int sequence, AliasMode aliasMode) {
		super();
		this.parent = parent;
		this.aliasMode = (aliasMode != null) ? aliasMode : AliasMode.DEFAULT;
		this.sequence = sequence;
	}

	/**
	 * Get the overall context sequence.
	 * @return the context sequence
	 */
	@Override
	public int getSequence() {
		return sequence;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jpa.internal.jpql.expressions.JpaResolutionContext#getParent()
	 */
	@Override
	public Optional<JpaResolutionContext> getParent() {
		return Optional.ofNullable(parent);
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jpa.internal.jpql.expressions.JpaResolutionContext#getAliasMode()
	 */
	@Override
	public AliasMode getAliasMode() {
		return aliasMode;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.holonplatform.datastore.jpa.internal.jpql.expressions.JpaResolutionContext#getAlias(com.holonplatform.core.
	 * Path)
	 */
	@Override
	public Optional<String> getTargetAlias(Path<?> path) {
		if (path == null) {
			// root alias
			return (target != null) ? Optional.ofNullable(pathAlias.get(target.getName())) : Optional.empty();
		}

		// check Aliasable
		if (AliasablePath.class.isAssignableFrom(path.getClass())) {
			final AliasablePath<?, ?> ap = (AliasablePath<?, ?>) path;
			if (ap.getAlias().isPresent()) {
				return ap.getAlias();
			}
		}

		String alias = pathAlias.get(path.fullName());
		// check parent
		if (alias == null && getParent().isPresent()) {
			JpaResolutionContext ctx = getParent().get();
			while (ctx != null) {
				if (ctx.getAliasMode() != AliasMode.UNSUPPORTED) {
					Optional<String> parentAlias = ctx.getTargetAlias(path);
					if (parentAlias.isPresent()) {
						alias = parentAlias.get();
						break;
					}
				}
				ctx = ctx.getParent().orElse(null);
			}
		}
		return Optional.ofNullable(alias);
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jpa.internal.jpql.expressions.JpaResolutionContext#getTarget()
	 */
	@Override
	public Optional<RelationalTarget<?>> getTarget() {
		return Optional.ofNullable(target);
	}

	@Override
	public void setTarget(RelationalTarget<?> target) {
		ObjectUtils.argumentNotNull(target, "DataTarget must be not null");
		this.target = target;
		// alias
		getOrCreatePathAlias(target).ifPresent(a -> pathAlias.put(target.getName(), a));
		// check joins
		target.getJoins().forEach(j -> {
			getOrCreatePathAlias(j).ifPresent(a -> pathAlias.put(j.getName(), a));
		});
	}

	private Optional<String> getOrCreatePathAlias(AliasablePath<?, ?> path) {
		if (AliasMode.UNSUPPORTED != getAliasMode()) {
			if (path != null) {
				if (path.getAlias().isPresent()) {
					return path.getAlias();
				}
				if (AliasMode.AUTO == getAliasMode()) {
					// generate alias
					return Optional.of(generateTargetAlias(path.getName()));
				}
			}
		}
		return Optional.empty();
	}

	protected String generateTargetAlias(String target) {

		String targetName = target;
		int idx = target.lastIndexOf('.');
		if (idx > -1 && idx < (target.length() - 1)) {
			targetName = target.substring(idx + 1, target.length());
		}

		StringBuilder sb = new StringBuilder();

		String prefix = ((targetName.length() <= 4) ? targetName : targetName.substring(0, 4)).toLowerCase();
		char[] pa = prefix.toCharArray();
		char[] sanitized = new char[pa.length];
		for (int i = 0; i < pa.length; i++) {
			sanitized[i] = (ALIAS_CHARS.indexOf(pa[i]) > -1) ? pa[i] : '_';
		}

		sb.append(sanitized);

		String partialAlias = sb.toString();

		int duplicateCount = 0;
		if (generatedAlias.containsKey(partialAlias)) {
			duplicateCount = generatedAlias.get(partialAlias) + 1;
		}
		generatedAlias.put(partialAlias, duplicateCount);
		sb.append("_");
		sb.append(duplicateCount);
		sb.append("_");

		sb.append(getSequence());

		return sb.toString();
	}

}
