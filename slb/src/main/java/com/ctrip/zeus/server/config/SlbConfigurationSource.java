package com.ctrip.zeus.server.config;

import com.ctrip.zeus.dao.entity.SlbConfig;
import com.ctrip.zeus.dao.entity.SlbConfigExample;
import com.ctrip.zeus.dao.mapper.SlbConfigMapper;
import com.ctrip.zeus.util.DBConfig;
import com.netflix.config.WatchedConfigurationSource;
import com.netflix.config.WatchedUpdateListener;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Discription
 **/
public class SlbConfigurationSource implements WatchedConfigurationSource {

    @Override
    public void addUpdateListener(WatchedUpdateListener l) {
    }

    @Override
    public void removeUpdateListener(WatchedUpdateListener l) {
    }

    @Override
    public Map<String, Object> getCurrentData() throws Exception {
        Map<String, Object> configMap = new HashMap<>();

        DataSource dataSource = new DBConfig().dataSource();
        Environment environment = new Environment("", new JdbcTransactionFactory(), dataSource);
        Configuration configuration = new Configuration(environment);
        configuration.addMapper(SlbConfigMapper.class);
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);

        SqlSession sqlSession = sqlSessionFactory.openSession();
        SlbConfigMapper mapper = sqlSession.getMapper(SlbConfigMapper.class);

        List<SlbConfig> configs = mapper.selectByExample(new SlbConfigExample());

        for (SlbConfig config : configs) {
            configMap.put(config.getPropertyKey(), config.getPropertyValue());
        }

        return configMap;
    }
}
