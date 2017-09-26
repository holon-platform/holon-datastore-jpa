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
package com.holonplatform.jpa.spring.internal;

import javax.persistence.EntityManager;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.orm.jpa.SharedEntityManagerCreator;
import org.springframework.transaction.annotation.Transactional;

import com.holonplatform.datastore.jpa.JpaDatastore;
import com.holonplatform.datastore.jpa.internal.DefaultJpaDatastore;
import com.holonplatform.jpa.spring.SpringJpaDatastore;
import com.holonplatform.spring.internal.datastore.DatastoreInitializer;

/**
 * {@link JpaDatastore} implementation using Spring shared transactional EntityManagers, ensuring consistency with
 * Spring transactions synchronization.
 * 
 * <p>
 * Methods which requires a transaction are annotated with {@link Transactional}.
 * </p>
 *
 * @since 5.0.0
 */
public class DefaultSpringJpaDatastore extends DefaultJpaDatastore
		implements SpringJpaDatastore, InitializingBean, BeanNameAware, BeanFactoryAware, BeanClassLoaderAware {

	private static final long serialVersionUID = 6656514093021783034L;

	/**
	 * Bean name
	 */
	private String beanName;

	/**
	 * BeanFactory
	 */
	private transient volatile BeanFactory beanFactory;

	/**
	 * ClassLoader
	 */
	private transient volatile ClassLoader classLoader;

	/**
	 * Constructor.
	 * <p>
	 * Sets an {@link EntityManagerInitializer} which create a Spring shared {@link EntityManager}.
	 * </p>
	 */
	public DefaultSpringJpaDatastore() {
		super(false);

		setEntityManagerInitializer((emf) -> SharedEntityManagerCreator.createSharedEntityManager(emf));
		setEntityManagerFinalizer((em) -> {
		});
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.springframework.beans.factory.BeanFactoryAware#setBeanFactory(org.springframework.beans.factory.BeanFactory)
	 */
	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.beans.factory.BeanNameAware#setBeanName(java.lang.String)
	 */
	@Override
	public void setBeanName(String name) {
		this.beanName = name;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.beans.factory.BeanClassLoaderAware#setBeanClassLoader(java.lang.ClassLoader)
	 */
	@Override
	public void setBeanClassLoader(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	@Override
	public void afterPropertiesSet() throws Exception {

		// intialization
		initialize(classLoader);

		// configure
		DatastoreInitializer.configureDatastore(this, beanName, beanFactory);
	}

	public static class Builder
			extends DefaultJpaDatastore.AbstractBuilder<SpringJpaDatastore, DefaultSpringJpaDatastore> {

		public Builder() {
			super(new DefaultSpringJpaDatastore());
		}

		/*
		 * (non-Javadoc)
		 * @see com.holonplatform.core.datastore.Datastore.Builder#build()
		 */
		@Override
		public SpringJpaDatastore build() {
			if (datastore.getEntityManagerFactory() == null) {
				throw new IllegalStateException("Missing EntityManagerFactory");
			}
			return datastore;
		}

	}

}
