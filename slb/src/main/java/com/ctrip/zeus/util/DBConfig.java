package com.ctrip.zeus.util;

import com.netflix.config.ConfigurationManager;
import org.apache.tomcat.jdbc.pool.DataSourceFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.aop.target.HotSwappableTargetSource;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Properties;

/*todo: EnableDalMybatis need to be commented in released version*/
@Configuration
@MapperScan("com.ctrip.zeus.dao.mapper")
public class DBConfig implements ApplicationContextAware {

    private final DataSourceFactory tomcatFactory = new DataSourceFactory();
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private volatile ApplicationContext context;
    private ProxyFactoryBean proxyFactoryBean;

    @Autowired
    private ConfigurableBeanFactory configurableBeanFactory;

    @Bean
    public DataSource dataSource() throws Exception {
        String confDir = ConfigurationManager.getConfigInstance().getString("CONF_DIR");
//            String confDir = "D:\\Users\\jyan\\Documents\\SLB\\slb\\conf\\regression";
        Properties properties = new Properties();
        FileInputStream file = null;
        try {
            file = new FileInputStream(confDir + "/db.properties");
            properties.load(file);
            String passwordEncoded = properties.getProperty("passwordEncoded", "False");
            if ("true".equalsIgnoreCase(passwordEncoded)) {
                String password = properties.getProperty("password");
                properties.setProperty("password", Cipher.decode(password));
            }
            setProperties(properties);
        } catch (FileNotFoundException fe) {
            logger.error("[DBConfig] Failed to read db connection property file. Exception:" + fe.getMessage());
            throw fe;
        } finally {
            if (file != null) file.close();
        }

        HotSwappableTargetSource targetSource = new HotSwappableTargetSource(tomcatFactory.createDataSource(properties));
        ProxyFactoryBean proxyFactory = new ProxyFactoryBean();
        proxyFactory.setSingleton(true);
        proxyFactory.setInterfaces(DataSource.class);
        proxyFactory.setBeanFactory(this.context);
        proxyFactory.setTargetSource(targetSource);

        this.proxyFactoryBean = proxyFactory;
        if (configurableBeanFactory != null) {
            configurableBeanFactory.registerSingleton("dataSourceFactory", this.proxyFactoryBean);
        }
        return (DataSource) proxyFactory.getObject();
    }

    @Bean
    public DataSourceTransactionManager mybatisTransactionManager() throws Exception {
        return new DataSourceTransactionManager(dataSource());
    }

    @Bean
    public SqlSessionFactoryBean sqlSessionFactoryBean(ResourceLoader resourceLoader) throws Exception {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(dataSource());
        sqlSessionFactoryBean.setConfigLocation(resourceLoader.getResource("classpath:mybatis-config.xml"));
        return sqlSessionFactoryBean;
    }

    public static void setProperties(Properties properties) {
        properties.setProperty("minIdle", properties.getProperty("minIdle", "1"));
        properties.setProperty("maxActive", properties.getProperty("maxActive", "3"));
        properties.setProperty("initialSize", properties.getProperty("initialSize", "3"));
        properties.setProperty("maxWait", properties.getProperty("maxWait", "30000"));
        properties.setProperty("timeBetweenEvictionRunsMillis", properties.getProperty("timeBetweenEvictionRunsMillis", "5000"));
        properties.setProperty("minEvictableIdleTimeMillis ", properties.getProperty("timeBetweenEvictionRunsMillis", "600000"));
        properties.setProperty("numTestsPerEvictionRun ", properties.getProperty("numTestsPerEvictionRun", "10"));
        properties.setProperty("poolPreparedStatements", properties.getProperty("poolPreparedStatements", "true"));
        properties.setProperty("maxOpenPreparedStatements", properties.getProperty("maxOpenPreparedStatements", "1000"));
        properties.setProperty("validationQuery", properties.getProperty("validationQuery", "SELECT 1"));
        properties.setProperty("validationQueryTimeout", properties.getProperty("validationQueryTimeout", "60"));
        properties.setProperty("useDisposableConnectionFacade", properties.getProperty("useDisposableConnectionFacade", "true"));
        properties.setProperty("testOnBorrow", properties.getProperty("testOnBorrow", "true"));
        properties.setProperty("testWhileIdle", properties.getProperty("testWhileIdle", "true"));
        properties.setProperty("removeAbandoned", properties.getProperty("removeAbandoned", "false"));
        properties.setProperty("removeAbandonedTimeout", properties.getProperty("removeAbandonedTimeout", "60"));
        properties.setProperty("validationInterval", properties.getProperty("validationInterval", "10000"));
        properties.setProperty("maxAge", properties.getProperty("maxAge", "120000"));
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }
}