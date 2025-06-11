package com.holonplatform.datastore.jpa.examples;

import jakarta.persistence.EntityManagerFactory;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.holonplatform.core.datastore.Datastore;
import com.holonplatform.datastore.jpa.JpaDatastore;
import com.holonplatform.jpa.spring.EnableJpaDatastore;
import com.holonplatform.jpa.spring.SpringEntityManagerLifecycleHandler;
import com.holonplatform.spring.EnableDatastoreConfiguration;

public class ExampleJpaDatastoreSpring12 {

	// tag::config[]
	@Configuration
	@EnableJpaDatastore
	@EnableDatastoreConfiguration // <1>
	class Config {

		@Bean
		public Datastore jpaDatastore(EntityManagerFactory entityManagerFactory) {
			return JpaDatastore.builder().entityManagerFactory(entityManagerFactory)
					.entityManagerHandler(SpringEntityManagerLifecycleHandler.create()) // <2>
					.build();
		}

	}
	// end::config[]

}
