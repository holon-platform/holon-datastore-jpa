/*
 * Copyright 2000-2016 Holon TDCN.
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
package com.holonplatform.datastore.jpa.internal;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.metamodel.Attribute.PersistentAttributeType;
import javax.persistence.metamodel.EmbeddableType;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.SingularAttribute;

import com.holonplatform.core.Expression;
import com.holonplatform.core.Expression.InvalidExpressionException;
import com.holonplatform.core.ExpressionResolver;
import com.holonplatform.core.ExpressionResolver.ResolutionContext;
import com.holonplatform.core.Path;
import com.holonplatform.core.beans.BeanIntrospector;
import com.holonplatform.core.beans.BeanPropertySet;
import com.holonplatform.core.datastore.DataTarget;
import com.holonplatform.core.datastore.DatastoreConfigProperties;
import com.holonplatform.core.datastore.DefaultWriteOption;
import com.holonplatform.core.datastore.bulk.BulkDelete;
import com.holonplatform.core.datastore.bulk.BulkInsert;
import com.holonplatform.core.datastore.bulk.BulkUpdate;
import com.holonplatform.core.exceptions.DataAccessException;
import com.holonplatform.core.internal.Logger;
import com.holonplatform.core.internal.datastore.AbstractDatastore;
import com.holonplatform.core.internal.utils.ClassUtils;
import com.holonplatform.core.internal.utils.ObjectUtils;
import com.holonplatform.core.property.PathProperty;
import com.holonplatform.core.property.Property;
import com.holonplatform.core.property.PropertyBox;
import com.holonplatform.core.property.PropertySet;
import com.holonplatform.datastore.jpa.JpaConfigProperties.ORMPlatform;
import com.holonplatform.datastore.jpa.JpaDatastore;
import com.holonplatform.datastore.jpa.config.JpaDatastoreCommodityContext;
import com.holonplatform.datastore.jpa.config.JpaDatastoreCommodityFactory;
import com.holonplatform.datastore.jpa.config.JpaDatastoreExpressionResolver;
import com.holonplatform.datastore.jpa.internal.expressions.JpaEntity;
import com.holonplatform.datastore.jpa.internal.expressions.JpaResolutionContext;
import com.holonplatform.datastore.jpa.internal.expressions.JpaResolutionContext.AliasMode;
import com.holonplatform.datastore.jpa.internal.resolvers.ConstantExpressionResolver;
import com.holonplatform.datastore.jpa.internal.resolvers.DataTargetEntityResolver;
import com.holonplatform.datastore.jpa.internal.resolvers.DataTargetResolver;
import com.holonplatform.datastore.jpa.internal.resolvers.ExistFilterResolver;
import com.holonplatform.datastore.jpa.internal.resolvers.JpaTargetEntityResolver;
import com.holonplatform.datastore.jpa.internal.resolvers.LiteralValueResolver;
import com.holonplatform.datastore.jpa.internal.resolvers.NotExistFilterResolver;
import com.holonplatform.datastore.jpa.internal.resolvers.OperationStructureResolver;
import com.holonplatform.datastore.jpa.internal.resolvers.PathFunctionResolver;
import com.holonplatform.datastore.jpa.internal.resolvers.PathResolver;
import com.holonplatform.datastore.jpa.internal.resolvers.PropertyConstantExpressionResolver;
import com.holonplatform.datastore.jpa.internal.resolvers.QueryAggregationResolver;
import com.holonplatform.datastore.jpa.internal.resolvers.QueryFilterResolver;
import com.holonplatform.datastore.jpa.internal.resolvers.QueryProjectionResolver;
import com.holonplatform.datastore.jpa.internal.resolvers.QuerySortResolver;
import com.holonplatform.datastore.jpa.internal.resolvers.QueryStructureResolver;
import com.holonplatform.datastore.jpa.internal.resolvers.RelationalTargetResolver;
import com.holonplatform.datastore.jpa.internal.resolvers.SubQueryResolver;
import com.holonplatform.datastore.jpa.internal.resolvers.VisitableQueryFilterResolver;
import com.holonplatform.datastore.jpa.internal.resolvers.VisitableQueryProjectionResolver;
import com.holonplatform.datastore.jpa.internal.resolvers.VisitableQuerySortResolver;

/**
 * Default {@link JpaDatastore} implementation.
 * <p>
 * Uses JPA {@link EntityManager} and criteria query api to configure and execute datastore operations.
 * </p>
 * 
 * @since 5.0.0
 */
public class DefaultJpaDatastore extends AbstractDatastore<JpaDatastoreCommodityContext>
		implements JpaDatastoreCommodityContext {

	private static final long serialVersionUID = -8695844962665825169L;

	/**
	 * Logger
	 */
	private final static Logger LOGGER = JpaDatastoreLogger.create();

	/*
	 * Datastore EntityManagerFactory
	 */
	private EntityManagerFactory entityManagerFactory;

	/*
	 * ORM platform
	 */
	private ORMPlatform platform;

	/*
	 * EntityManager initializer
	 */
	private EntityManagerInitializer entityManagerInitializer;
	/*
	 * EntityManager finalizer
	 */
	private EntityManagerFinalizer entityManagerFinalizer;

	/*
	 * Auto-flush mode
	 */
	private boolean autoFlush = false;

	/**
	 * Whether the datastore was initialized
	 */
	private boolean initialized = false;

	/**
	 * Whether to auto-initialize the Datastore at EntityManagerFactory setup
	 */
	private final boolean autoInitialize;

	/**
	 * Default constructor
	 */
	public DefaultJpaDatastore() {
		this(true);
	}

	/**
	 * Constructor
	 * @param autoInitialize Whether to initialize the Datastore at EntityManagerFactory setup
	 */
	public DefaultJpaDatastore(boolean autoInitialize) {
		super(JpaDatastoreCommodityFactory.class, JpaDatastoreExpressionResolver.class);
		this.autoInitialize = autoInitialize;

		// EntityManager lifecycle
		setEntityManagerInitializer((emf) -> emf.createEntityManager());
		setEntityManagerFinalizer((em) -> em.close());

		// register default resolvers
		addExpressionResolver(JpaTargetEntityResolver.INSTANCE);
		addExpressionResolver(DataTargetEntityResolver.INSTANCE);
		addExpressionResolver(RelationalTargetResolver.INSTANCE);
		addExpressionResolver(DataTargetResolver.INSTANCE);
		addExpressionResolver(PathFunctionResolver.INSTANCE);
		addExpressionResolver(PathResolver.INSTANCE);
		addExpressionResolver(ConstantExpressionResolver.INSTANCE);
		addExpressionResolver(PropertyConstantExpressionResolver.INSTANCE);
		addExpressionResolver(LiteralValueResolver.INSTANCE);
		addExpressionResolver(SubQueryResolver.INSTANCE);
		addExpressionResolver(ExistFilterResolver.INSTANCE);
		addExpressionResolver(NotExistFilterResolver.INSTANCE);
		addExpressionResolver(VisitableQueryFilterResolver.INSTANCE);
		addExpressionResolver(VisitableQuerySortResolver.INSTANCE);
		addExpressionResolver(QueryFilterResolver.INSTANCE);
		addExpressionResolver(QuerySortResolver.INSTANCE);
		addExpressionResolver(VisitableQueryProjectionResolver.INSTANCE);
		addExpressionResolver(QueryProjectionResolver.INSTANCE);
		addExpressionResolver(QueryAggregationResolver.INSTANCE);
		addExpressionResolver(QueryStructureResolver.INSTANCE);
		addExpressionResolver(OperationStructureResolver.INSTANCE);

		// Query commodity factory
		registerCommodity(new JpaQueryFactory());
	}

	/**
	 * Whether to initialize the Datastore at EntityManagerFactory setup.
	 * @return the autoInitialize <code>true</code> if auto-initialize is enabled
	 */
	protected boolean isAutoInitialize() {
		return autoInitialize;
	}

	/**
	 * Initialize the datastore
	 * @param classLoader ClassLoader to use to load default factories and resolvers
	 * @throws IllegalStateException If initialization fails
	 */
	protected void initialize(ClassLoader classLoader) throws IllegalStateException {
		if (!initialized) {

			// default factories and resolvers
			loadExpressionResolvers(classLoader);
			loadCommodityFactories(classLoader);

			initialized = true;
		}
	}

	/**
	 * Checks whether to auto-initialize the Datastore, if {@link #isAutoInitialize()} is <code>true</code> and the
	 * Datastore wasn't already initialized.
	 */
	protected void checkInitialize() {
		if (isAutoInitialize()) {
			initialize(ClassUtils.getDefaultClassLoader());
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.internal.datastore.AbstractDatastore#getCommodityContext()
	 */
	@Override
	protected JpaDatastoreCommodityContext getCommodityContext() throws CommodityConfigurationException {
		return this;
	}

	/**
	 * Set EntityManagerFactory to obtain EntityManager references
	 * @param entityManagerFactory EntityManagerFactory to set, must be not <code>null</code>
	 */
	public void setEntityManagerFactory(EntityManagerFactory entityManagerFactory) {
		ObjectUtils.argumentNotNull(entityManagerFactory, "EntityManagerFactory must be not null");
		this.entityManagerFactory = entityManagerFactory;
		this.platform = null;
		checkInitialize();
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jpa.internal.ConfigurableJpaDatastore#getEntityManagerFactory()
	 */
	@Override
	public EntityManagerFactory getEntityManagerFactory() {
		return entityManagerFactory;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jpa.internal.ConfigurableJpaDatastore#getORMPlatform()
	 */
	@Override
	public Optional<ORMPlatform> getORMPlatform() {
		if (platform == null) {
			// try to detect
			try {
				platform = withEntityManager(em -> ORMPlatform.resolve(em));
			} catch (Exception e) {
				LOGGER.warn("Failed to detected ORM platform");
				LOGGER.debug(() -> "Failed to detected ORM platform", e);
			}
		}
		return Optional.ofNullable(platform);
	}

	/**
	 * Sets the {@link EntityManager} instance provider for Datastore operations execution
	 * @param entityManagerInitializer the EntityManagerInitializer to set
	 */
	protected void setEntityManagerInitializer(EntityManagerInitializer entityManagerInitializer) {
		ObjectUtils.argumentNotNull(entityManagerInitializer, "EntityManagerInitializer must be not null");
		this.entityManagerInitializer = entityManagerInitializer;
	}

	/**
	 * Gets the {@link EntityManager} instance provider for Datastore operations execution
	 * @return the EntityManagerInitializer
	 */
	protected EntityManagerInitializer getEntityManagerInitializer() {
		return entityManagerInitializer;
	}

	/**
	 * Sets the {@link EntityManager} finalizer to use after Datastore operations execution.
	 * @param entityManagerFinalizer the EntityManagerFinalizer to set
	 */
	protected void setEntityManagerFinalizer(EntityManagerFinalizer entityManagerFinalizer) {
		this.entityManagerFinalizer = entityManagerFinalizer;
	}

	/**
	 * Get the {@link EntityManager} finalizer to use after Datastore operations execution.
	 * @return the EntityManagerFinalizer, if available
	 */
	protected Optional<EntityManagerFinalizer> getEntityManagerFinalizer() {
		return Optional.ofNullable(entityManagerFinalizer);
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jpa.config.JpaDatastoreCommodityContext#getEntityManager()
	 */
	@Override
	public EntityManager getEntityManager() {
		EntityManagerFactory emf = getEntityManagerFactory();
		if (emf == null) {
			throw new IllegalStateException("No EntityManagerFactory available. Check your JPA configuration.");
		}
		return getEntityManagerInitializer().getEntityManager(emf);
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jpa.JpaDatastore#withEntityManager(com.holonplatform.datastore.jpa.JpaDatastore.
	 * EntityManagerOperation)
	 */
	@Override
	public <R> R withEntityManager(EntityManagerOperation<R> operation) {
		ObjectUtils.argumentNotNull(operation, "Operation must be not null");

		// initialize
		final EntityManager entityManager = getEntityManager();
		if (entityManager == null) {
			throw new IllegalStateException(
					"Obtained a null EntityManager from initializer [" + getEntityManagerInitializer() + "]");
		}
		try {
			// execute operation
			return operation.execute(entityManager);
		} catch (Exception e) {
			throw new DataAccessException("Failed to execute operation", e);
		} finally {
			getEntityManagerFinalizer().ifPresent(f -> f.finalizeEntityManager(entityManager));
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jpa.internal.ConfigurableJpaDatastore#isAutoFlush()
	 */
	@Override
	public boolean isAutoFlush() {
		return autoFlush;
	}

	/**
	 * Set whether to auto-flush mode is enabled. When auto-flush mode is enabled, {@link EntityManager#flush()} is
	 * called after each Datastore data manipulation operation, such as <code>save</code> or <code>delete</code>.
	 * @param autoFlush <code>true</code> to enable the auto-flush mode, <code>false</code> to disable
	 */
	public void setAutoFlush(boolean autoFlush) {
		this.autoFlush = autoFlush;
	}

	/**
	 * Get the {@link BeanIntrospector} to use to introspect entity beans.
	 * @return BeanIntrospector (not null)
	 */
	protected BeanIntrospector getBeanIntrospector() {
		return BeanIntrospector.get();
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.datastore.Datastore#refresh(com.holonplatform.core.datastore.DataTarget,
	 * com.holonplatform.core.property.PropertyBox)
	 */
	@Override
	public PropertyBox refresh(DataTarget<?> target, PropertyBox propertyBox) {

		ObjectUtils.argumentNotNull(target, "DataTarget must be not null");
		ObjectUtils.argumentNotNull(propertyBox, "PropertyBox must be not null");

		return withEntityManager(entityManager -> {

			// get entity class
			Class<?> entity = getEntityClass(target, entityManager);

			// create a new instance
			Object instance = getBeanIntrospector().write(propertyBox, entity.newInstance());

			// ensure managed
			Object managed = !entityManager.contains(instance) ? entityManager.merge(instance) : instance;
			// refresh
			entityManager.refresh(managed);

			// return refresh entity property values
			return getBeanIntrospector().read(PropertyBox.builder(propertyBox).invalidAllowed(true).build(), managed);
		});
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.datastore.Datastore#insert(com.holonplatform.core.datastore.DataTarget,
	 * com.holonplatform.core.property.PropertyBox, com.holonplatform.core.datastore.Datastore.WriteOption[])
	 */
	@Override
	public OperationResult insert(DataTarget<?> target, PropertyBox propertyBox, WriteOption... options) {

		ObjectUtils.argumentNotNull(target, "DataTarget must be not null");
		ObjectUtils.argumentNotNull(propertyBox, "PropertyBox must be not null");

		return withEntityManager(entityManager -> {

			// get entity class
			Class<?> entity = getEntityClass(target, entityManager);

			// create a new instance
			Object instance = entity.newInstance();
			// Bean property set
			final BeanPropertySet<Object> set = getBeanIntrospector().getPropertySet(entity);
			// persist entity
			entityManager.persist(set.write(propertyBox, instance));

			// check auto-flush
			if (isAutoFlush() || JpaDatastoreUtils.isFlush(options)) {
				entityManager.flush();
			}

			OperationResult.Builder result = OperationResult.builder().type(OperationType.INSERT).affectedCount(1);

			// get ids
			setInsertedIds(result, entityManager, set, entity, instance, isBringBackGeneratedIds(options), propertyBox);

			return result.build();

		});
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.datastore.Datastore#update(com.holonplatform.core.datastore.DataTarget,
	 * com.holonplatform.core.property.PropertyBox, com.holonplatform.core.datastore.Datastore.WriteOption[])
	 */
	@Override
	public OperationResult update(DataTarget<?> target, PropertyBox propertyBox, WriteOption... options) {

		ObjectUtils.argumentNotNull(target, "DataTarget must be not null");
		ObjectUtils.argumentNotNull(propertyBox, "PropertyBox must be not null");

		return withEntityManager(entityManager -> {

			// get entity class
			Class<?> entity = getEntityClass(target, entityManager);

			// create a new instance
			Object instance = entity.newInstance();

			// merge entity
			entityManager.merge(getBeanIntrospector().write(propertyBox, instance));

			// check auto-flush
			if (isAutoFlush() || JpaDatastoreUtils.isFlush(options)) {
				entityManager.flush();
			}

			return OperationResult.builder().type(OperationType.UPDATE).affectedCount(1).build();

		});
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.datastore.Datastore#save(com.holonplatform.core.datastore.DataTarget,
	 * com.holonplatform.core.property.PropertyBox, com.holonplatform.core.datastore.Datastore.WriteOption[])
	 */
	@Override
	public OperationResult save(DataTarget<?> target, PropertyBox propertyBox, WriteOption... options) {

		ObjectUtils.argumentNotNull(target, "DataTarget must be not null");
		ObjectUtils.argumentNotNull(propertyBox, "PropertyBox must be not null");

		return withEntityManager(entityManager -> {

			// get entity class
			Class<?> entity = getEntityClass(target, entityManager);

			OperationResult.Builder result = OperationResult.builder().affectedCount(1);

			// Bean property set
			final BeanPropertySet<Object> set = getBeanIntrospector().getPropertySet(entity);

			// create instance and write values
			Object instance = set.write(propertyBox, entity.newInstance());

			// check has identifier
			if (entityManager.getEntityManagerFactory().getPersistenceUnitUtil().getIdentifier(instance) == null) {
				result.type(OperationType.INSERT);
				entityManager.persist(instance);
			} else {
				result.type(OperationType.UPDATE);
				entityManager.merge(instance);
			}

			// check auto-flush
			if (isAutoFlush() || JpaDatastoreUtils.isFlush(options)) {
				entityManager.flush();
			}

			// get ids
			setInsertedIds(result, entityManager, set, entity, instance, isBringBackGeneratedIds(options), propertyBox);

			return result.build();

		});
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.datastore.Datastore#delete(com.holonplatform.core.datastore.DataTarget,
	 * com.holonplatform.core.property.PropertyBox, com.holonplatform.core.datastore.Datastore.WriteOption[])
	 */
	@Override
	public OperationResult delete(DataTarget<?> target, PropertyBox propertyBox, WriteOption... options) {

		ObjectUtils.argumentNotNull(target, "DataTarget must be not null");
		ObjectUtils.argumentNotNull(propertyBox, "PropertyBox must be not null");

		return withEntityManager(entityManager -> {

			// get entity class
			Class<?> entity = getEntityClass(target, entityManager);

			// create a new instance
			Object instance = entity.newInstance();

			// write box values into instance
			getBeanIntrospector().write(propertyBox, instance);

			// merge to ensure entity is not detached
			instance = entityManager.merge(instance);

			// delete entity
			entityManager.remove(instance);

			// check auto-flush
			if (isAutoFlush() || JpaDatastoreUtils.isFlush(options)) {
				entityManager.flush();
			}

			return OperationResult.builder().type(OperationType.DELETE).affectedCount(1).build();

		});
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.holonplatform.core.ExpressionResolver.ExpressionResolverHandler#resolve(com.holonplatform.core.Expression,
	 * java.lang.Class, com.holonplatform.core.ExpressionResolver.ResolutionContext)
	 */
	@Override
	public <E extends Expression, R extends Expression> Optional<R> resolve(E expression, Class<R> resolutionType,
			ResolutionContext context) throws InvalidExpressionException {
		return getExpressionResolverRegistry().resolve(expression, resolutionType, context);
	}

	/**
	 * Resolve given <code>target</code> to obtain a JPA entity class
	 * @param target DataTarget to resolve
	 * @param entityManager EntityManager
	 * @return Resolved JPA entity class
	 * @throws InvalidExpressionException If the given target cannot be resolved
	 */
	protected Class<?> getEntityClass(DataTarget<?> target, EntityManager entityManager) {
		final JpaResolutionContext targetContext = JpaResolutionContext.create(getEntityManagerFactory(),
				getORMPlatform().orElse(null), this, AliasMode.DEFAULT);
		JpaEntity<?> entity = targetContext.resolve(target, JpaEntity.class, targetContext)
				.orElseThrow(() -> new InvalidExpressionException("Failed to resolve data target [" + target + "]"));
		entity.validate();
		return entity.getEntityClass();
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.datastore.Datastore#bulkInsert(com.holonplatform.core.datastore.DataTarget,
	 * com.holonplatform.core.property.PropertySet, com.holonplatform.core.datastore.Datastore.WriteOption[])
	 */
	@Override
	public BulkInsert bulkInsert(DataTarget<?> target, PropertySet<?> propertySet, WriteOption... options) {
		return new JpaBulkInsert(this, getBeanIntrospector(), target, propertySet, options);
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.datastore.Datastore#bulkUpdate(com.holonplatform.core.datastore.DataTarget,
	 * com.holonplatform.core.datastore.Datastore.WriteOption[])
	 */
	@Override
	public BulkUpdate bulkUpdate(DataTarget<?> target, WriteOption... options) {
		return new JpaBulkUpdate(this, target, options);
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.datastore.Datastore#bulkDelete(com.holonplatform.core.datastore.DataTarget,
	 * com.holonplatform.core.datastore.Datastore.WriteOption[])
	 */
	@Override
	public BulkDelete bulkDelete(DataTarget<?> target, WriteOption... options) {
		return new JpaBulkDelete(this, target, options);
	}

	/**
	 * Get the id {@link Path}s of given <code>entity</code>.
	 * @param entityManager EntityManager
	 * @param set Entity bean property set
	 * @param entity Entity class
	 * @return Entity ids, an empty list if none
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static List<Path> getIds(EntityManager entityManager, BeanPropertySet<Object> set, Class<?> entity) {
		final List<Path> ids = new LinkedList<>();
		final EntityType et = entityManager.getMetamodel().entity(entity);
		try {
			if (et.hasSingleIdAttribute()) {
				SingularAttribute attribute = et.getId(et.getIdType().getJavaType());
				if (attribute.getPersistentAttributeType() == PersistentAttributeType.EMBEDDED) {
					final String idName = attribute.getName();
					final Path parent = Path.of(idName, attribute.getJavaType());
					EmbeddableType<?> emb = entityManager.getMetamodel().embeddable(attribute.getJavaType());
					emb.getAttributes().forEach(a -> {
						ids.add(Path.of(a.getName(), a.getJavaType()).parent(parent));
					});
				} else {
					String idName = et.getId(et.getIdType().getJavaType()).getName();
					Optional<PathProperty<Object>> idProperty = set.getProperty(idName);
					idProperty.ifPresent(p -> ids.add(p));
				}
			} else {
				try {
					Set<SingularAttribute> attributes = et.getIdClassAttributes();
					if (attributes != null) {
						attributes.forEach(a -> {
							Optional<PathProperty<Object>> idProperty = set.getProperty(a.getName());
							idProperty.ifPresent(p -> ids.add(p));
						});
					}
				} catch (@SuppressWarnings("unused") IllegalArgumentException e) {
					// ignore
				}
			}
		} catch (Exception e) {
			LOGGER.warn("Failed to obtain entity id(s) value", e);
		}
		return ids;
	}

	/**
	 * Set the entity id values of given <code>entity</code> instance to be returned as an {@link OperationResult}.
	 * @param result OperationResult in which to set the ids
	 * @param entityManager EntityManager
	 * @param set Entity bean property set
	 * @param entity Entity class
	 * @param instance Entity instance
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static void setInsertedIds(OperationResult.Builder result, EntityManager entityManager,
			BeanPropertySet<Object> set, Class<?> entity, Object instance, boolean bringBackGeneratedIds,
			PropertyBox propertyBox) {
		try {
			getIds(entityManager, set, entity).forEach(p -> {
				Object keyValue = set.read(p, instance);
				result.withInsertedKey(p, keyValue);
				if (bringBackGeneratedIds && keyValue != null) {
					// set in propertybox
					Property property = getPropertyForPath(p, propertyBox);
					if (property != null) {
						propertyBox.setValue(property, keyValue);
					}
				}
			});
		} catch (Exception e) {
			LOGGER.warn("Failed to obtain entity id(s) value", e);
		}
	}

	/**
	 * Try to obtain a property from given <code>propertySet</code> wich path name corresponds to given
	 * <code>path</code> name.
	 * @param path Path
	 * @param propertySet Property set
	 * @return The property of given <code>propertySet</code> wich path name corresponds to given <code>path</code>
	 *         name, or <code>null</code> if not found
	 */
	private static Property<?> getPropertyForPath(com.holonplatform.core.Path<?> path, PropertySet<?> propertySet) {
		if (path instanceof Property && propertySet.contains((Property<?>) path)) {
			return (Property<?>) path;
		}
		final String name = path.getName();
		for (Property<?> property : propertySet) {
			if (com.holonplatform.core.Path.class.isAssignableFrom(property.getClass())) {
				if (name.equals(((com.holonplatform.core.Path<?>) property).getName())) {
					return property;
				}
			}
		}
		return null;
	}

	/**
	 * Checks if the {@link DefaultWriteOption#BRING_BACK_GENERATED_IDS} is present among given write options.
	 * @param options Write options
	 * @return <code>true</code> if the {@link DefaultWriteOption#BRING_BACK_GENERATED_IDS} is present
	 */
	private static boolean isBringBackGeneratedIds(WriteOption[] options) {
		if (options != null) {
			for (WriteOption option : options) {
				if (DefaultWriteOption.BRING_BACK_GENERATED_IDS == option) {
					return true;
				}
			}
		}
		return false;
	}

	// Builder

	/**
	 * Default {@link JpaDatastore.Builder}.
	 */
	public abstract static class AbstractBuilder<D extends JpaDatastore, I extends DefaultJpaDatastore>
			implements JpaDatastore.Builder<D> {

		protected final I datastore;

		public AbstractBuilder(I datastore) {
			super();
			this.datastore = datastore;
		}

		/*
		 * (non-Javadoc)
		 * @see com.holonplatform.core.datastore.Datastore.Builder#dataContextId(java.lang.String)
		 */
		@Override
		public JpaDatastore.Builder<D> dataContextId(String dataContextId) {
			datastore.setDataContextId(dataContextId);
			return this;
		}

		/*
		 * (non-Javadoc)
		 * @see com.holonplatform.core.datastore.Datastore.Builder#traceEnabled(boolean)
		 */
		@Override
		public JpaDatastore.Builder<D> traceEnabled(boolean trace) {
			datastore.setTraceEnabled(trace);
			return this;
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * com.holonplatform.core.ExpressionResolver.ExpressionResolverBuilder#withExpressionResolver(com.holonplatform.
		 * core.ExpressionResolver)
		 */
		@Override
		public <E extends Expression, R extends Expression> JpaDatastore.Builder<D> withExpressionResolver(
				ExpressionResolver<E, R> expressionResolver) {
			datastore.addExpressionResolver(expressionResolver);
			return this;
		}

		/*
		 * (non-Javadoc)
		 * @see com.holonplatform.core.datastore.Datastore.Builder#configuration(com.holonplatform.core.datastore.
		 * DatastoreConfigProperties)
		 */
		@Override
		public JpaDatastore.Builder<D> configuration(DatastoreConfigProperties configuration) {
			ObjectUtils.argumentNotNull(configuration, "Datastore configuration must be not null");
			datastore.setTraceEnabled(configuration.isTrace());
			return this;
		}

		/*
		 * (non-Javadoc)
		 * @see com.holonplatform.datastore.jpa.JpaDatastore.Builder#entityManagerFactory(javax.persistence.
		 * EntityManagerFactory)
		 */
		@Override
		public JpaDatastore.Builder<D> entityManagerFactory(EntityManagerFactory entityManagerFactory) {
			datastore.setEntityManagerFactory(entityManagerFactory);
			return this;
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * com.holonplatform.datastore.jpa.JpaDatastore.Builder#entityManagerInitializer(com.holonplatform.datastore.jpa
		 * .JpaDatastore.EntityManagerInitializer)
		 */
		@Override
		public JpaDatastore.Builder<D> entityManagerInitializer(EntityManagerInitializer entityManagerInitializer) {
			datastore.setEntityManagerInitializer(entityManagerInitializer);
			return this;
		}

		/*
		 * (non-Javadoc)
		 * @see com.holonplatform.datastore.jpa.JpaDatastore.Builder#entityManagerFinalizer(com.holonplatform.datastore.
		 * jpa.JpaDatastore.EntityManagerFinalizer)
		 */
		@Override
		public JpaDatastore.Builder<D> entityManagerFinalizer(EntityManagerFinalizer entityManagerFinalizer) {
			datastore.setEntityManagerFinalizer(entityManagerFinalizer);
			return this;
		}

		/*
		 * (non-Javadoc)
		 * @see com.holonplatform.datastore.jpa.JpaDatastore.Builder#autoFlush(boolean)
		 */
		@Override
		public JpaDatastore.Builder<D> autoFlush(boolean autoFlush) {
			datastore.setAutoFlush(autoFlush);
			return this;
		}

	}

	/**
	 * Default {@link JpaDatastore.Builder}.
	 */
	public static class DefaultBuilder extends AbstractBuilder<JpaDatastore, DefaultJpaDatastore> {

		public DefaultBuilder() {
			super(new DefaultJpaDatastore());
		}

		/*
		 * (non-Javadoc)
		 * @see com.holonplatform.core.datastore.Datastore.Builder#build()
		 */
		@Override
		public JpaDatastore build() {
			if (datastore.getEntityManagerFactory() == null) {
				throw new IllegalStateException(
						"No EntityManagerFactory available: a EntityManagerFactory must be provided to build the JpaDatastore instance");
			}
			return datastore;
		}

	}

}
