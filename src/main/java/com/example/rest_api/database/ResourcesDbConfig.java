package com.example.rest_api.database;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import org.springframework.boot.jdbc.DataSourceBuilder;

@Configuration
@EnableJpaRepositories(
        basePackages = "com.example.rest_api.database.repository.resources",
        entityManagerFactoryRef = "resourcesEntityManagerFactory",
        transactionManagerRef = "resourcesTransactionManager"
)
public class ResourcesDbConfig {

    @Bean(name = "resourcesDataSource")
    public DataSource resourcesDataSource() {
        return DataSourceBuilder.create()
                .url("jdbc:postgresql://localhost:5432/tw_lab_resources")
                .username("postgres")
                .password("1q2w3e")
                .driverClassName("org.postgresql.Driver")
                .build();
    }

    @Bean(name = "entityManagerFactoryBuilder")
    public EntityManagerFactoryBuilder entityManagerFactoryBuilder(JpaProperties jpaProperties) {
        JpaVendorAdapter jpaVendorAdapter = new org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter();
        return new EntityManagerFactoryBuilder(jpaVendorAdapter, jpaProperties.getProperties(), null);
    }


    @Bean(name = "resourcesEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean resourcesEntityManagerFactory(
            @Qualifier("entityManagerFactoryBuilder") EntityManagerFactoryBuilder builder,
            @Qualifier("resourcesDataSource") DataSource dataSource) {
        return builder
                .dataSource(dataSource)
                .packages("com.example.rest_api.database.model.resources") // Package for second database entities
                .persistenceUnit("resources")
                .build();
    }

    @Bean(name = "resourcesTransactionManager")
    public PlatformTransactionManager resourcesTransactionManager(
            @Qualifier("resourcesEntityManagerFactory") LocalContainerEntityManagerFactoryBean resourcesEntityManagerFactory) {
        return new JpaTransactionManager(resourcesEntityManagerFactory.getObject());
    }
}
