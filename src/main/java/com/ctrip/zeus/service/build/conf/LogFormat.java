package com.ctrip.zeus.service.build.conf;

import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;

/**
 * Created by zhoumy on 2015/11/20.
 */
public class LogFormat {
    private static DynamicStringProperty logFormat = DynamicPropertyFactory.getInstance().getStringProperty("slb.nginx.log-format",
            "'[$time_local] $host $hostname $server_addr $request_method $uri '\n" +
                    "'\"$query_string\" $server_port $remote_user $remote_addr $http_x_forwarded_for '\n" +
                    "'$server_protocol \"$http_user_agent\" \"$cookie_COOKIE\" \"$http_referer\" '\n" +
                    "'$host $status $body_bytes_sent $request_time $upstream_response_time '\n" +
                    "'$upstream_addr $upstream_status $proxy_host'"
    );

    public static String getMain() {
        return logFormat.get();
    }

    public static String getMainCompactString() {
        return logFormat.get().replaceAll("'", "").replaceAll("\\n", "");
    }
}
