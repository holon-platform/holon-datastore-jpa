package com.holonplatform.datastore.jpa.examples;

import javax.sql.DataSource;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import com.holonplatform.core.datastore.Datastore;
import com.holonplatform.jpa.spring.EnableJpaDatastore;

import jakarta.persistence.EntityManagerFactory;

public class ExampleJpaDatastoreSpring1 {

	// tag::config[]
	@EnableJpaDatastore(entityManagerFactoryReference = "myEntityManagerFactory") // <1>
	@Configuration
	class Config {

		@Bean(name = "myEntityManagerFactory")
		public FactoryBean<EntityManagerFactory> entityManagerFactory(DataSource dataSource) {
			LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
			emf.setDataSource(dataSource);
			emf.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
			emf.setPackagesToScan("com.example.entities");
			return emf;
		}

	}

	@Autowired
	Datastore datastore; // <2>
	// end::config[]

}
