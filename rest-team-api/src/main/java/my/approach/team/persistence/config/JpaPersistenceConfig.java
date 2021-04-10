package my.approach.team.persistence.config;

import com.zaxxer.hikari.HikariDataSource;
import my.approach.team.persistence.dialect.Oracle12cExtendedDialect;
import my.approach.team.persistence.repositories.BaseRepositoryImpl;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@EnableTransactionManagement
@EnableJpaAuditing
@EnableJpaRepositories(
        repositoryBaseClass = BaseRepositoryImpl.class,
        basePackages = {"my.approach.team.teaming.persistence.repositories"})

public class JpaPersistenceConfig {

    @Bean
    @Primary
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource, @Qualifier("teamHibernateProperties") Properties properties) {
        final LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setDataSource(dataSource);
        factory.setPersistenceUnitName("GRPREG");
        factory.setJpaProperties(properties);
        factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        factory.setPackagesToScan("my.approach.team.teaming.model.domain.team","my.approach.team.teaming.model.auth");
        factory.afterPropertiesSet();
        return factory;
    }

    @Bean
    @Primary
    public PlatformTransactionManager transactionManager(EntityManagerFactory emf) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(emf);
        return transactionManager;
    }

    @Bean
    @Primary
    @ConfigurationProperties(prefix = "teaming.datasource")
    public HikariDataSource dataSource() {
        return new HikariDataSource();
    }


    @Bean("teamHibernateProperties")
    public Properties hibernateProperties() {
        final Properties props = new Properties();
        props.put("hibernate.dialect", Oracle12cExtendedDialect.class.getCanonicalName());
        return props;
    }
}
