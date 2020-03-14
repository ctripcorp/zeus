package com.ctrip.zeus.util;

import com.netflix.config.ConfigurationManager;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Configuration
public class SpringPropertiesLoader {

    @Bean
    public PropertyPlaceholderConfigurer propertyConfigurer() throws Exception {
        String env = ConfigurationManager.getDeploymentContext().getDeploymentEnvironment();
        String app = ConfigurationManager.getDeploymentContext().getApplicationId();
        PropertyPlaceholderConfigurer res =
                new PropertyPlaceholderConfigurer();
        ClassPathResource resource = new ClassPathResource(app + "-" + env + ".properties");
        if (resource.exists()){
            res.setLocation(new ClassPathResource(app + "-" + env + ".properties"));
        }
        return res;
    }
}
