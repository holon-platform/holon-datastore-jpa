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

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import jakarta.persistence.Entity;
import jakarta.persistence.IdClass;
import jakarta.persistence.metamodel.Attribute.PersistentAttributeType;
import jakarta.persistence.metamodel.EmbeddableType;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.IdentifiableType;
import jakarta.persistence.metamodel.ManagedType;
import jakarta.persistence.metamodel.Metamodel;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.Type;

import com.holonplatform.core.beans.BeanPropertySet;
import com.holonplatform.core.internal.Logger;
import com.holonplatform.core.internal.utils.AnnotationUtils;
import com.holonplatform.core.internal.utils.ObjectUtils;
import com.holonplatform.datastore.jpa.internal.JpaDatastoreLogger;
import com.holonplatform.datastore.jpa.jpql.expression.JpaEntity;

/**
 * Default {@link JpaEntity} implementation.
 *
 * @param <T> Entity type
 *
 * @since 5.0.0
 */
public class DefaultJpaEntity<T> implements JpaEntity<T> {

	private static final long serialVersionUID = -2297497762504490622L;

	/**
	 * Logger
	 */
	private static final Logger LOGGER = JpaDatastoreLogger.create();

	/**
	 * Entity class
	 */
	private final Class<T> entityClass;

	/**
	 * Entity name
	 */
	private final String entityName;

	/**
	 * Identifier metadata
	 */
	private transient IdMetadata<T> idMetadata;

	/**
	 * Version attribute
	 */
	private transient SingularAttribute<? super T, ?> versionAttribute;

	/**
	 * Entity class property set
	 */
	private final transient BeanPropertySet<T> beanPropertySet;

	/**
	 * Constructor.
	 * @param metamodel JPA Metamodel (not null)
	 * @param entityClass Entity class (not null)
	 */
	public DefaultJpaEntity(Metamodel metamodel, Class<T> entityClass) {
		super();
		ObjectUtils.argumentNotNull(metamodel, "Metamodel must be not null");
		ObjectUtils.argumentNotNull(entityClass, "Entity class must be not null");
		this.entityClass = entityClass;

		this.beanPropertySet = BeanPropertySet.create(entityClass);

		// inspect entity metadata
		ManagedType<T> type = metamodel.managedType(entityClass);
		if (type == null) {
			throw new IllegalArgumentException("Entity class [" + entityClass.getName() + "] not found in Metamodel");
		}

		if (type instanceof EntityType) {
			this.entityName = ((EntityType<?>) type).getName();
		} else {
			this.entityName = getEntityNameFromAnnotation(entityClass).orElse(entityClass.getSimpleName());
		}

		if (type instanceof IdentifiableType) {
			try {
				this.idMetadata = new IdMetadata<>((IdentifiableType<T>) type, metamodel);
				this.versionAttribute = lookupVersionAttribute(metamodel, (IdentifiableType<T>) type).orElse(null);
			} catch (Exception e) {
				LOGGER.warn("Failed to resolve identifier metadata for entity [" + entityClass.getName() + "]");
				LOGGER.debug(() -> "Failed to resolve identifier metadata for entity [" + entityClass.getName() + "]",
						e);
				this.idMetadata = null;
				this.versionAttribute = null;
			}
		} else {
			this.idMetadata = null;
			this.versionAttribute = null;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jpa.internal.JpaEntity#getEntityClass()
	 */
	@Override
	public Class<T> getEntityClass() {
		return entityClass;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jpa.internal.JpaEntity#getEntityName()
	 */
	@Override
	public String getEntityName() {
		return entityName;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.Expression#validate()
	 */
	@Override
	public void validate() throws InvalidExpressionException {
		if (getEntityClass() == null) {
			throw new InvalidExpressionException("Null entity class");
		}
		if (getEntityName() == null) {
			throw new InvalidExpressionException("Null entity name");
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jpa.jpql.expression.JpaEntity#getIdType()
	 */
	@Override
	public Optional<Class<?>> getIdType() {
		return Optional.of(getIdMetadata().getType());
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jpa.jpql.expression.JpaEntity#hasCompositeId()
	 */
	@Override
	public boolean hasCompositeId() {
		return !getIdMetadata().hasSimpleId();
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jpa.jpql.expression.JpaEntity#getId(java.lang. Object)
	 */
	@Override
	public Optional<Object> getId(T entity) {
		ObjectUtils.argumentNotNull(entity, "Entity instance must be not null");

		final IdMetadata<T> idm = getIdMetadata();

		if (idm.hasSimpleId()) {
			return Optional.ofNullable(beanPropertySet.read(idm.getSimpleIdAttribute().get().getName(), entity));
		}
		// idclass
		Set<SingularAttribute<? super T, ?>> idClassAttributes = idm.getIdClassAttributes();
		if (idClassAttributes != null && !idClassAttributes.isEmpty()) {
			final BeanPropertySet<Object> idClassPropertySet = idm.getIdClassPropertySet();
			try {
				Object id = idm.getType().getDeclaredConstructor().newInstance();
				for (SingularAttribute<? super T, ?> ica : idClassAttributes) {
					final Object value = beanPropertySet.read(ica.getName(), entity);
					if (value != null) {
						idClassPropertySet.write(ica.getName(), value, id);
					}
				}
				return Optional.of(id);
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException e) {
				throw new RuntimeException("Failed to istantiate id class [" + idm.getType() + "]", e);
			}
		}

		return Optional.empty();
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jpa.jpql.expression.JpaEntity#isNew(java.lang. Object)
	 */
	@Override
	public boolean isNew(T entity) {
		ObjectUtils.argumentNotNull(entity, "Entity instance must be not null");
		if (versionAttribute == null || versionAttribute.getJavaType().isPrimitive()) {
			// use id
			final IdMetadata<T> idm = getIdMetadata();

			final Class<?> idType = getIdType().orElse(Object.class);

			if (idm.hasSimpleId()) {
				final SingularAttribute<? super T, ?> singleId = idm.getSimpleIdAttribute().get();
				final Object idValue = beanPropertySet.read(singleId.getName(), entity);
				boolean isNull = isNullIdValue(idValue, idType);
				if (!isNull && idm.getEmbeddedIdAttributes() != null && !idm.getEmbeddedIdAttributes().isEmpty()) {
					// embedded id
					for (SingularAttribute<?, ?> eia : idm.getEmbeddedIdAttributes()) {
						boolean isNullAttribute = isNullIdValue(
								idm.getIdClassPropertySet().read(eia.getName(), idValue), eia.getJavaType());
						if (isNullAttribute) {
							return true;
						}
					}
					return false;
				}
				return isNull;
			} else {
				// check multiple id using IdClass
				Set<SingularAttribute<? super T, ?>> idClassAttributes = idm.getIdClassAttributes();
				if (idClassAttributes != null && !idClassAttributes.isEmpty()) {
					for (SingularAttribute<? super T, ?> ica : idClassAttributes) {
						final Object value = beanPropertySet.read(ica.getName(), entity);
						boolean isNullAttribute = isNullIdValue(value, ica.getJavaType());
						if (isNullAttribute) {
							return true;
						}
					}
					return false;
				}
			}

			return isNullIdValue(getId(entity).orElse(null), idType);
		}

		// use version
		if (versionAttribute != null) {
			return beanPropertySet.read(versionAttribute.getName(), entity) == null;
		}

		// default
		return true;
	}

	/**
	 * Get the entity identifier metadata
	 * @return The entity identifier metadata
	 * @throws IllegalStateException if the entity identifier metadata is not available
	 */
	protected IdMetadata<T> getIdMetadata() {
		if (idMetadata == null) {
			throw new IllegalStateException(
					"Entity identifier metadata not available for entity [" + getEntityClass() + "]");
		}
		return idMetadata;
	}

	/**
	 * Checks whether given identifier value is to be considered <code>null</code>.
	 * @param id Identifier value
	 * @param type Identifier value type
	 * @return <code>true</code> if it is to be considered <code>null</code>
	 */
	private static boolean isNullIdValue(Object id, Class<?> type) {
		if (type.isPrimitive() && (id instanceof Number)) {
			return ((Number) id).longValue() == 0L;
		}
		return id == null;
	}

	/**
	 * Get the entity name using {@link Entity#name()} annotation attribute, if available.
	 * @param entityClass Entity class (not null)
	 * @return The entity name as specified using {@link Entity#name()} annotation attribute, or an
	 *         empty Optional if the {@link Entity} annotation is not present or the <code>name</code>
	 *         attribute has no value
	 */
	private static Optional<String> getEntityNameFromAnnotation(Class<?> entityClass) {
		if (entityClass.isAnnotationPresent(Entity.class)) {
			String name = entityClass.getAnnotation(Entity.class).name();
			if (name != null && !name.trim().equals("")) {
				return Optional.of(name);
			}
		}
		return Optional.empty();
	}

	/**
	 * Try to obtain the <em>version</em> attribute of the entity type, if available.
	 * @param metamodel JPA Metamodel
	 * @param type Entity type
	 * @return Optional version attribute
	 */
	@SuppressWarnings("unchecked")
	private static <T> Optional<SingularAttribute<? super T, ?>> lookupVersionAttribute(Metamodel metamodel,
			IdentifiableType<T> type) {
		try {
			return Optional.ofNullable(type.getVersion(Object.class));
		} catch (@SuppressWarnings("unused") IllegalArgumentException e) {
			// ignore
		}
		for (SingularAttribute<? super T, ?> attribute : type.getSingularAttributes()) {
			if (attribute.isVersion()) {
				return Optional.of(attribute);
			}
		}
		// check super type
		try {
			ManagedType<?> managedSuperType = metamodel.managedType(type.getJavaType().getSuperclass());
			if (!(managedSuperType instanceof IdentifiableType)) {
				return Optional.empty();
			}
			return lookupVersionAttribute(metamodel, (IdentifiableType<T>) managedSuperType);
		} catch (@SuppressWarnings("unused") IllegalArgumentException e) {
			return Optional.empty();
		}
	}

	private static class IdMetadata<T> {

		private final IdentifiableType<T> identifiableType;
		private final SingularAttribute<? super T, ?> simpleIdAttribute;
		private final Set<SingularAttribute<?, ?>> embeddedIdAttributes;
		private final Set<SingularAttribute<? super T, ?>> idClassAttributes;
		private final BeanPropertySet<Object> idClassPropertySet;
		private Class<?> idType;

		public IdMetadata(IdentifiableType<T> identifiableType, Metamodel metamodel) {
			this.identifiableType = identifiableType;
			if (identifiableType.hasSingleIdAttribute()) {
				this.simpleIdAttribute = identifiableType.getDeclaredId(identifiableType.getIdType().getJavaType());
				this.idClassAttributes = null;
				if (this.simpleIdAttribute != null
						&& PersistentAttributeType.EMBEDDED == this.simpleIdAttribute.getPersistentAttributeType()) {
					this.idClassPropertySet = BeanPropertySet.create(identifiableType.getIdType().getJavaType());
					this.embeddedIdAttributes = new HashSet<>();
					try {
						EmbeddableType<?> et = metamodel.embeddable(identifiableType.getIdType().getJavaType());
						this.embeddedIdAttributes.addAll(et.getSingularAttributes());
					} catch (@SuppressWarnings("unused") IllegalArgumentException e) {
						// ignore
					}
				} else {
					this.embeddedIdAttributes = null;
					this.idClassPropertySet = null;
				}
			} else {
				this.simpleIdAttribute = null;
				this.idClassAttributes = identifiableType.getIdClassAttributes();
				this.embeddedIdAttributes = null;
				this.idClassPropertySet = BeanPropertySet.create(getType());
			}
		}

		public boolean hasSimpleId() {
			return simpleIdAttribute != null;
		}

		public Class<?> getType() {
			if (idType == null) {
				idType = getIdType().orElseThrow(
						() -> new IllegalStateException("Failed to resolve ID type of [" + identifiableType + "]"));
			}
			return idType;
		}

		private Optional<Class<?>> getIdType() {
			Type<?> idType = null;
			try {
				idType = identifiableType.getIdType();
			} catch (@SuppressWarnings("unused") IllegalStateException e) {
				// ignore
			}
			if (idType != null) {
				return Optional.of(idType.getJavaType());
			}
			// Try to get id using IdClass annotation
			return AnnotationUtils.getAnnotation(identifiableType.getJavaType(), IdClass.class).map(a -> a.value());
		}

		public Optional<SingularAttribute<? super T, ?>> getSimpleIdAttribute() {
			return Optional.of(simpleIdAttribute);
		}

		public BeanPropertySet<Object> getIdClassPropertySet() {
			return idClassPropertySet;
		}

		public Set<SingularAttribute<?, ?>> getEmbeddedIdAttributes() {
			return embeddedIdAttributes;
		}

		public Set<SingularAttribute<? super T, ?>> getIdClassAttributes() {
			return idClassAttributes;
		}
	}

}
