package com.ctrip.zeus.service.rule.model;

public class RuleAttributeKeys {
    public static final String CLIENT_MAX_BODY_SIZE_KEY = "client-max-body-size";
    public static final String CLIENT_BODY_BUFFER_SIZE_KEY = "client-body-buffer-size";
    public static final String REQUEST_TIMEOUT_KEY = "proxy-read-timeout";
    public static final String REQUEST_KEEPALIVE_TIMEOUT_KEY = "keepalive-timeout";


    /// HSTS KEYS
    public static final String HSTS_MAX_AGE_KEY  = "hsts-max-age";
    public static final String HSTS_ENABLED_KEY  = "enabled";

    //DEFAULT　LISTON
    public static final String PROXY_PROTOCOL  = "proxy-protocol";
    public static final String HTTP_2  = "http2";
    public static final String BACKLOG  = "backlog";


    //GZIP
    public static final String GZIP_TYPES  = "gzip-types";
    public static final String GZIP_MIN_LEN  = "gzip-min-length";
    public static final String GZIP_COMP_LEVEL  = "gzip-comp-level";
    public static final String GZIP_BUFFER_COUNT  = "gzip-buffer-count";
    public static final String GZIP_BUFFER_SIZE  = "gzip-buffer-size";

    //KEEP ALIVE TIMEOUT
    public static final String KEEP_ALIVE_TIMEOUT  = "client-keep-alive-timeout";
    public static final String UPSTREAM_KEEP_ALIVE_TIMEOUT  = "upstream-keep-alive-timeout";
    public static final String UPSTREAM_KEEP_ALIVE_COUNT  = "upstream-keep-alive-count";
    //Large Client Header Buffer
    public static final String LARGE_CLIENT_HEADER_COUNT  = "large-client-header-buffers-count";
    public static final String LARGE_CLIENT_HEADER_SIZE  = "large-client-header-buffers-size";

    //ERROR PAGE
    public static final String ERROR_HOST_URL_KEY = "error-host-url";
    public static final String ERROR_PAGE_ACCEPT_KEY = "error-page-accept";

    //Normal Keys
    public static final String ENABLED_KEY  = "enabled";
    public static final String GLOBAL_LIST_ENABLED_KEY  = "global-list-enable";

    //Header Keys
    public static final String HEADER_KEY  = "header-key";
    public static final String HEADER_VALUE  = "header-value";

    //Lua
    public static final String LUA_COMMAND  = "lua-command";
    public static final String LUA_VAR  = "lua-var";

    //Black List
    public static final String REJECT_CODE  = "reject-code";
    public static final String REJECT_MESSAGE  = "reject-message";

    //Directive
    public static final String DIRECTIVE_KEY  = "directive-key";
    public static final String DIRECTIVE_VALUE  = "directive-value";
    public static final String DIRECTIVE_STAGE  = "directive-stage";



    // Server Http2 Config keys
    public static final String HTTP2_CHUNK_SIZE = "http2-chunk-size";
    public static final String HTTP2_BODY_PREREAD_SIZE = "http2-body-preread-size";
    public static final String HTTP2_IDLE_TIMEOUT = "http2-idle-timeout";
    public static final String HTTP2_MAX_CONCURRENT_STREAMS = "http2-max-concurrent-streams";
    public static final String HTTP2_MAX_FIELD_SIZE = "http2-max-field-size";
    public static final String HTTP2_MAX_HEADER_SIZE = "http2-max-header-size";
    public static final String HTTP2_MAX_REQUESTS = "http2-max-requests";
    public static final String HTTP2_RECV_BUFFER_SIZE = "http2-recv-buffer-size";
    public static final String HTTP2_RECV_TIMEOUT = "http2-recv-timeout";

    // Server Proxy Buffer Size rule
    public static final String PROXY_BUFFER_SIZE = "proxy-buffer-size";
    public static final String PROXY_BUFFERS_SIZE = "proxy-buffers-size";
    public static final String PROXY_BUFFERS_COUNT = "proxy-buffers-count";
    public static final String PROXY_BUSY_BUFFERS_SIZE = "proxy-busy-buffers-size";

    //SSL Config
    public static final String SSL_PREFER_SERVER_CIPHERS = "ssl-prefer-server-ciphers";
    public static final String SSL_ECDH_CURVE = "ssl-ecdh-curve";
    public static final String SSL_CIPHERS = "ssl-ciphers";
    public static final String SSL_BUFFER_SIZE = "ssl-buffer-size";
    public static final String SSL_PROTOCOL = "ssl-protocol";
    public static final String SSL_SESSION_CACHE = "ssl-session-cache";
    public static final String SSL_SESSION_CACHE_TIMEOUT = "ssl-session-cache-timeout";

    //Redirect Continue
    public static final String REDIRECT_CONDITION = "condition";
    public static final String REDIRECT_TARGET_URL = "target";
    public static final String REDIRECT_CODE = "response-code";

    // Named favicon
    public static final String FAVICON_BASE64_CODE = "favicon-base64-code";

    //Request header modify
    public static final String SET_REQUEST_HEADER_KEY = "header-key";
    public static final String SET_REQUEST_HEADER_VALUE = "header-value";


    //Header size
    public static final String HEADER_SIZE_INTEGER = "header-size-integer";
    public static final String HEADER_SIZE_LOG_LARGE_HEADERS = "header-size-log-large-headers";


    // default download image
    public static final String DEFAULT_DOWNLOAD_IMAGE_ROOT = "default-download-image-root";

    // bastion gateway
    public static final String BASTION_BASTION_IP_ENABLED = "bastion-bastion-ip-enabled";
    public static final String BASTION_GATEWAY_ENABLED = "bastion-gateway-enabled";
    public static final String BASTION_GATEWAY_FORCE_FORWARD = "bastion-gateway-force-forward";
    public static final String BASTION_GATEWAY_CANARY_IP = "bastion-gateway-canary-ip";
    public static final String BASTION_GATEWAY_CANARY_PORT = "bastion-gateway-canary-port";
    public static final String BASTION_GATEWAY_CLIENT_IP_PATTERN = "bastion-gateway-client-ip-pattern";
    public static final String BASTION_GATEWAY_HEADER_CMSGROUPID_ENABLED = "bastion-gateway-headers-cmsGroupId";
    public static final String BASTION_CLIENT_ADDR_VAR = "bastion-client-addr-var";
    public static final String BASTION_GROUP_ID = "bastion-group-id";
    public static final String BASTION_GROUP_APP_ID = "bastion-group-app-id";

    // access control
    public static final String ACCESS_CONTROL_GROUP_ID = "access-control-group-id";
    public static final String ACCESS_CONTROL_WHITE_LIST_ENABLED = "access-control-white-list-enabled";
    public static final String ACCESS_CONTROL_ALLOW_LIST = "access-control-allow-list";
    public static final String ACCESS_CONTROL_DENY_LIST = "access-control-deny-list";

    // server name hash rule
    public static final String SERVER_NAME_HASH_MAX_SIZE = "server-name-hash-max-size";
    public static final String SERVER_NAME_HASH_BUCKET_SIZE = "server-name-hash-bucket-size";


    // HttpRedirect
    public static final String HTTP_REDIRECT_CODE = "code";
    public static final String HTTP_REDIRECT_TARGETURL = "target";

}
