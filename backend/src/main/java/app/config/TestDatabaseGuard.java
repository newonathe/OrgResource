package app.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/** Prevents Hibernate test startup from applying DDL to the regular development schema. */
@Component
@Profile("test")
public class TestDatabaseGuard implements BeanFactoryPostProcessor, EnvironmentAware {
    private Environment environment;

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        String url = environment.getProperty("ORG_TEST_DB_URL");
        if (url == null || !url.startsWith("jdbc:mysql://")) {
            throw new IllegalStateException("The test profile requires ORG_TEST_DB_URL targeting the isolated orgresource_test schema.");
        }
        int queryStart = url.indexOf('?');
        String jdbcLocation = queryStart < 0 ? url : url.substring(0, queryStart);
        if (!jdbcLocation.endsWith("/orgresource_test")) {
            throw new IllegalStateException("The test profile refuses any database schema except orgresource_test.");
        }
        String configuredUrl = environment.getProperty("spring.datasource.url");
        if (configuredUrl == null || !configuredUrl.equals(url)) {
            throw new IllegalStateException("The test datasource must use the isolated ORG_TEST_DB_URL value.");
        }
    }
}
