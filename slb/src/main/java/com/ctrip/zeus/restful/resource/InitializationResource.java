package com.ctrip.zeus.restful.resource;

import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.restful.message.ResponseHandler;
import com.ctrip.zeus.service.config.SlbConfigService;
import com.ctrip.zeus.util.DBConfig;
import com.ctrip.zeus.util.MySQLUtils;
import com.ctrip.zeus.util.PropertiesUtils;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.netflix.config.ConfigurationManager;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;
import org.apache.tomcat.jdbc.pool.DataSourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.aop.target.HotSwappableTargetSource;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.util.*;

/**
 * @Discription
 **/
@Component
@Path("/init")
public class InitializationResource implements ApplicationContextAware {

    @Resource
    private ResponseHandler responseHandler;
    @Resource
    private MySQLUtils mySQLUtils;
    @Resource
    private SlbConfigService slbConfigService;
    @Resource
    private DataSource dataSource;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private final DynamicStringProperty initSQLScript = DynamicPropertyFactory.getInstance().getStringProperty("init.sql.script", "/init-db.sql");
    private final DataSourceFactory tomcatFactory = new DataSourceFactory();
    private final DynamicStringProperty apiHostString = DynamicPropertyFactory.getInstance().getStringProperty("slb.host", "");
    private volatile ApplicationContext applicationContext;
    private final DynamicStringProperty slb_table = DynamicPropertyFactory.getInstance().getStringProperty("slb.table.name", "slb_slb");

    private final String DEFAULT_MYSQL_DRIVER_CLASS = "com.mysql.jdbc.Driver";
    private final String URL_KEY = "url";
    private final String USERNAME_KEY = "username";
    private final String PASSWORD_KEY = "password";

    @GET
    @Path("/check")
    public Response checkReady(@Context HttpServletRequest request,
                               @Context HttpHeaders headers) throws Exception {
        Map<String, Boolean> result = new HashMap<>();
        result.put("result", initializationNeeded());
        return responseHandler.handle(result, headers.getMediaType());
    }

    @GET
    @Path("/host")
    public Response initApi(@Context HttpServletRequest request,
                            @Context HttpHeaders headers) throws Exception {
        // check whether db has been initialized
        Map<String, String> result = new HashMap<>();
        Map<String, String> existed = null;
        String apiHost = apiHostString.get();

        if (!initializationNeeded()) {
            List<String> keys = new ArrayList<>();
            keys.add(apiHost);
            existed = slbConfigService.query(keys);
            if (existed != null && existed.containsKey(apiHost)) {
                result.put("api", existed.get(apiHost));
            } else {
                result.put("api", System.getProperty("agent.api.host"));
            }
        } else {
            result.put("api", System.getProperty("agent.api.host"));
        }

        return responseHandler.handle(result, headers.getMediaType());
    }

    @GET
    @Path("/db")
    public Response initDB(@Context HttpServletRequest request,
                           @Context HttpHeaders headers,
                           @QueryParam("config") List<String> initConfigs) throws Exception {
        // insert user-passed configuration
        Map<String, String> configs = parseConfigs(initConfigs);
        slbConfigService.batchUpsertValue(configs, false);

        // check whether db has been initialized
        if (!initializationNeeded()) {
            return responseHandler.handle("DB initialized successfully", headers.getMediaType());
        }
        mySQLUtils.runScript(null, new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream(initSQLScript.get()))));

        return responseHandler.handle("DB initialized successfully", headers.getMediaType());
    }

    @POST
    @Path("/connection")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, "*/*"})
    public Response setDBConnection(@Context HttpServletRequest request,
                                    @Context HttpHeaders headers,
                                    @RequestBody Map<String, String> body) throws Exception {
        validateConnectionProperties(body);

        body.putIfAbsent("driverClassName", DEFAULT_MYSQL_DRIVER_CLASS);
        Properties properties = new Properties();
        properties.putAll(body);

        DBConfig.setProperties(properties);

        DataSource dataSource = tomcatFactory.createDataSource(properties);

        registerDataSource(dataSource, properties);
        saveChangeToLocal(properties);
        return responseHandler.handle("db set successfully. ", headers.getMediaType());
    }

    private void saveChangeToLocal(Properties properties) throws Exception {
        String localPath = ConfigurationManager.getConfigInstance().getString("CONF_DIR") + "/db.properties";
        Map<String, String> incremental = (Map) properties;

        PropertiesUtils.updatePropertiesFileOnDisk(localPath, incremental);
    }

    private void registerDataSource(DataSource dataSource, Properties properties) {
        if (this.applicationContext == null) {
            return;
        }

        TargetSource targetSource = ((ProxyFactoryBean) applicationContext.getBean("&dataSourceFactory")).getTargetSource();
        if (targetSource instanceof HotSwappableTargetSource) {
            ((HotSwappableTargetSource) targetSource).swap(dataSource);
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    private void validateConnectionProperties(Map<String, String> properties) throws Exception {
        if (properties == null) {
            throw new ValidationException("not correct or empty request body");
        }

        if (!properties.containsKey(USERNAME_KEY)) {
            throw new ValidationException("username is needed");
        }
        if (!properties.containsKey(URL_KEY)) {
            throw new ValidationException("url is needed");
        }
        if (!properties.containsKey(PASSWORD_KEY)) {
            throw new ValidationException("password is needed");
        }
    }

    private boolean initializationNeeded() throws Exception {
        Set<String> tables = mySQLUtils.getTables(null);
        return !tables.contains(slb_table.get());
    }

    /*
     * @Description: parse multiple "key:value" token to {key:value}
     * @return
     **/
    private Map<String, String> parseConfigs(List<String> configs) throws ValidationException {
        Map<String, String> result = Maps.newHashMap();

        if (!CollectionUtils.isEmpty(configs)) {
            for (String token : configs) {
                String[] tokens = token.split("=");
                if (tokens.length != 2) {
                    throw new ValidationException("invalid config pattern. Please pass something like 'key:value'");
                }
                String key = tokens[0].trim();
                String value = tokens[1].trim();

                if (!Strings.isNullOrEmpty(key) && !Strings.isNullOrEmpty(value)) {
                    result.put(key, value);
                }
            }
        }
        return result;
    }
}
