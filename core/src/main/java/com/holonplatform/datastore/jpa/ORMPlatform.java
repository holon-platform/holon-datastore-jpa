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
package com.holonplatform.datastore.jpa;

import java.util.Arrays;
import java.util.List;

import jakarta.persistence.EntityManager;

import com.holonplatform.core.internal.utils.ClassUtils;

/**
 * Enumeration of ORM platforms.
 * 
 * @since 5.0.0
 */
public enum ORMPlatform {

	HIBERNATE("org.hibernate.Session", "org.hibernate.ejb.HibernateEntityManager"),

	ECLIPSELINK("org.eclipse.persistence.jpa.JpaEntityManager"),

	OPENJPA("org.apache.openjpa.persistence.OpenJPAEntityManager"),

	DATANUCLEUS("org.datanucleus.jpa.EntityManagerImpl", "org.datanucleus.ObjectManager",
			"org.datanucleus.ExecutionContext");

	/*
	 * EntitManager delegates class names
	 */
	private final List<String> delegates;

	/**
	 * @param delegates
	 */
	private ORMPlatform(String... delegates) {
		this.delegates = Arrays.asList(delegates);
	}

	/**
	 * EntitManager delegates class names
	 * @return Delegates class names
	 */
	public List<String> getDelegates() {
		return delegates;
	}

	/**
	 * Try to detect ORM platform to use form classpath
	 * @return Detected ORMPlatform, or <code>null</code> if not found
	 * @throws IllegalStateException If more than one ORM provide is found in classpath
	 */
	public static ORMPlatform detectFromClasspath() throws IllegalStateException {
		ORMPlatform platform = null;
		for (ORMPlatform p : values()) {
			for (String className : p.getDelegates()) {
				try {
					Class.forName(className);
					if (platform != null) {
						throw new IllegalStateException(
								"More than one ORM provider found in classpath: " + platform + ", " + p);
					}
					platform = p;
				} catch (@SuppressWarnings("unused") Exception e) {
					// not found
				}
			}
		}
		return platform;
	}

	/**
	 * Determines the ORMPlatform from the given {@link EntityManager}
	 * @param em EntityManager (must be not null)
	 * @return Resolved ORMPlatform. If none of know platforms match, <code>null</code> is returned
	 */
	public static ORMPlatform resolve(EntityManager em) {
		if (em == null) {
			throw new IllegalArgumentException("Null EntityManager");
		}
		for (ORMPlatform platform : values()) {
			for (String entityManagerClassName : platform.getDelegates()) {
				if (isEntityManagerOfType(em, entityManagerClassName)) {
					return platform;
				}
			}
		}
		return null;
	}

	/**
	 * Returns whether the given {@link EntityManager} is of the given type.
	 * @param em EntityManager (must be not null)
	 * @param type the fully qualified expected EntityManager type
	 * @return True if type match
	 */
	private static boolean isEntityManagerOfType(EntityManager em, String type) {
		try {
			Class<?> emType = ClassUtils.forName(type, em.getDelegate().getClass().getClassLoader());
			if (emType.isAssignableFrom(em.getDelegate().getClass())) {
				return true;
			}
		} catch (@SuppressWarnings("unused") Exception e) {
			// ignore
		}
		return false;
	}

}
