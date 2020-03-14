var headerInfoApp = angular.module('headerInfoApp', ["angucomplete-alt"]);
headerInfoApp.controller('headerInfoController', function ($scope, $http) {
    $scope.query = {};
    $scope.generateLink = function (a) {
        var link = '';
        switch (a) {
            case 'hc': {
                link = "/portal/statistics/statistics-hc#?env=" + G.env;
                break;
            }
            case 'basic': {
                link = "/portal/statistics#?env=" + G.env;
                break;
            }
            case 'policy': {
                link = "/portal/statistics/statistics-netjava#?env=" + G.env;
                break;
            }
            case 'abtest': {
                link = "/portal/statistics/statistics-normalpolicy#?env=" + G.env;
                break;
            }
            case 'pie': {
                link = "/portal/statistics/charts#?env=" + G.env;
                break;
            }
            case 'traffic': {
                link = "/portal/statistics/traffic#?env=" + G.env;

                break;
            }
            case 'bu-traffic': {
                link = "/portal/statistics/butraffic#?env=" + G.env + '&bu=All';
                break;
            }
            case 'version': {
                link = "/portal/statistics/release#?env=" + G.env;
                break;
            }
            case 'health': {
                link = "/portal/statistics/slbhealth#?env=" + G.env;
                break;
            }
            case 'log': {
                link = "/portal/statistics/opslog#?env=" + G.env;
                break;
            }
            case 'database': {
                link = "/portal/statistics/dbtraffic#?env=" + G.env;
                break;
            }
            case 'deploy': {
                link = "/portal/statistics/deployment#?env=" + G.env;
                break;
            }
            case 'ctripprogress': {
                link = "/portal/statistics/statistics-ctrip-netjava#?env=" + G.env;
                break;
            }
            case 'cert': {
                link = "/portal/statistics/certificates#?env=" + G.env;
                break;
            }
            case 'ctriplanguage': {
                link = "/portal/statistics/statistics-ctrip-language#?env=" + G.env;
                break;
            }
            case 'comments': {
                link = "/portal/statistics/statistics-feedback#?env=" + G.env;
                break;
            }
            case 'unhealthy': {
                link = "/portal/statistics/statistics-unhealthy#?env=" + G.env;
                break;
            }
            case 'rule': {
                link = "/portal/statistics/rule#?env=" + G.env;
                break;
            }
            default:
                break;
        }
        return link;
    }
    $scope.hashChanged = function (hashData) { $scope.resource = H.resource;
        if (hashData.env) {
            $scope.env = hashData.env;
        }
        if (hashData.appId) {
            $scope.query.appId = hashData.appId;
        }
    };
    H.addListener("infoLinksApp", $scope, $scope.hashChanged);
});
angular.bootstrap(document.getElementById("header-area"), ['headerInfoApp']);


var operationLogApp = angular.module('operationLogApp', ['http-auth-interceptor', 'ngSanitize']);
operationLogApp.controller('operationLogController', function ($scope, $http, $q) {
    var title = {};
    var systemdefault;
    var envdefault;
    var slbdefault;
    var parentdefault;
    $scope.data = {
        block: [
            {
                'text': '白名单',
                'value': 'white'
            },
            {
                'text': '黑名单',
                'value': 'black'
            },
            {
                'text': '无',
                'value': 'default'
            }
        ]
    };
    var pagerules = {};
    var pageRulesSnapShot = {};
    $scope.snap = pageRulesSnapShot;
    var rules = {
        'global': '',
        'parent': '',
        'self': ''
    };
    var config = {
        'keep-alive-timeout': {
            'default': 60,
            'min': 1
        },
        'enable-hsts': {
            enabled: false,
            'hsts-max-age': {
                'default': 3600,
                'min': 1,
                'max': 200000
            }
        },
        'large-client-header': {
            'count': {
                'max': 100,
                'min': 4,
                'default': 4
            },
            'size': {
                'max': 256,
                'min': 4,
                'default': 16
            }
        },
        'error-page': {
            'default': false
        },
        'server-http2-config-rule': {
            'enabled': false,
            'http2-body-preread-size': {
                'min': 64,
                'max': 100,
                'default': 64
            },
            'http2-chunk-size': {
                'min': 8,
                'max': 100,
                'default': 8
            },
            'http2-idle-timeout': {
                'min': 3,
                'max': 100,
                'default': 3
            },
            'http2-max-concurrent-streams': {
                'min': 128,
                'max': 1000,
                'default': 128
            },
            'http2-max-field-size': {
                'min': 4,
                'max': 100,
                'default': 4
            },
            'http2-max-header-size': {
                'min': 16,
                'max': 100,
                'default': 16
            },
            'http2-max-requests': {
                'min': 1000,
                'max': 5000,
                'default': 1000
            },
            'http2-recv-buffer-size': {
                'min': 256,
                'max': 1000,
                'default': 256
            },
            'http2-recv-timeout': {
                'min': 30,
                'max': 100,
                'default': 30
            }
        },
        'server-proxy-buffer-size-rule': {
            enabled: false,
            'proxy-buffer-size': {
                min: 4,
                max: 256,
                'default': 4
            },
            'proxy-buffers': {
                'count': {
                    'default': 4,
                    'min': 4,
                    'max': 100
                },
                'size': {
                    'default': 8,
                    'min': 8,
                    'max': 1000
                }
            },
            'proxy-busy-buffers-size': {
                min: 4,
                max: 256,
                'default': 4
            }
        },
        'ssl-config': {
            'ssl-prefer-server-ciphers': false,
            'ssl-ecdh-curve': 'X25519:P-256:P-384:P-224:P-521',
            'ssl-ciphers': '[ECDHE-ECDSA-AES128-GCM-SHA256|ECDHE-ECDSA-CHACHA20-POLY1305|ECDHE-RSA-AES128-GCM-SHA256|ECDHE-RSA-CHACHA20-POLY1305]:ECDHE+AES128:RSA+AES128:ECDHE+AES256:RSA+AES256:ECDHE+3DES:RSA+3DES',
            'ssl-buffer-size': 10,
            'ssl-protocol': 'TLSv1 TLSv1.1 TLSv1.2'
        },
        'default-ssl-config': {
            'ssl-prefer-server-ciphers': false,
            'ssl-ecdh-curve': 'X25519:P-256:P-384:P-224:P-521',
            'ssl-ciphers': 'EECDH+CHACHA20:EECDH+CHACHA20-draft:EECDH+AES128:RSA+AES128:EECDH+AES256:RSA+AES256:EECDH+3DES:RSA+3DES:!MD5',
            'ssl-buffer-size': 10,
            'ssl-protocol': 'TLSv1 TLSv1.1 TLSv1.2'
        },
        'client-max-body-size': {
            'default': 30,
            'min': 1
        },
        'proxy-read-timeout': {
            'default': 120,
            'min': 1
        },
        'client-body-buffer-size': {
            'default': 16,
            'max': 100,
            'min': 16
        },
        'upstream-keep-alive-timeout': {
            'default': 55,
            'min': 1
        },
        'upstream-keep-alive-count': {
            'default': 50,
            'min': 1
        },
        'default-listen-rule': {
            'proxy-protocol': false,
            'http2': false,
            'backlog': {
                "default": 512,
                'min': 1
            }
        },
        'group-error-page-enable': {
            'default': false
        },
        'protocol-response-header': {
            'default': false
        },
        'request-id-enable': {
            'default': false
        },
        'access-by-lua': {
            'default': ''
        },
        'rewrite-by-lua': {
            'default': ''
        },
        'init-by-lua': {
            'default': ''
        },
        'set-by-lua': {
            "default": []
        },
        'add-header': {
            "default": []
        },
        'set-request-header': {
            "default": []
        },
        'hide-header': {
            "default": []
        },
        'condition-redirect': {
            "default": []
        },
        'gzip': {
            'enabled': true,
            'types': {
                'default': 'text/css',
                'scope': [
                    "text/html",
                    "text/plain",
                    "application/x-javascript",
                    "text/css",
                    "application/xml",
                    "text/javascript",
                    "application/x-httpd-php",
                    "image/jpeg",
                    "image/gif",
                    "image/png",
                    "*"]
            },
            'buffer': {
                'count': {
                    'min': 4,
                    'default': 16,
                    'max': 32
                },
                'size': {
                    'min': 8,
                    'default': 8,
                    'max': 64
                }
            },
            'min-length': {
                'min': 1,
                'default': 100,
                'max': 1024
            },
            'compress': {
                'min': 1,
                'default': 1,
                'max': 9
            }
        },
        'favicon-rule': {
            'enabled': false,
            'favicon-base64-code': 'AAABAAEAEBAAAAEAIABoBAAAFgAAACgAAAAQAAAAIAAAAAEAIAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAON3' +
            'JXvjdyXX5Xcj9+h2IP/pdh7/6HYf/+Z3IqvkdyQqAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA43clNON3Jf/jdyX/5Xcjb+p2HtbydRX/soBZ//Z0Ef/vdRj/6HYg/+R3JKoAAAAAA' +
            'AAAAAAAAAAAAAAA43clM+N3Jf/jdyX/5HckSud2Ic7YeTG9MJXhKgCe/3oHm///l4R1//F1Fv/odiD/5HckhQAAAAAAAAAAAAAAAON3Jf/jdyX/43cl3OR3JALodiDcAAAAAAAAAAAAA' +
            'AAABZv/FwCd//+4f1P/7XUb/+V3I/8AAAAAAAAAAON3JYPjdyX/43cl/+N3JXbjdyUJ53YhfQAAAAAAAAAAAAAAAAAAAAAEm/+hWo20//R0E//odiD/AAAAAAAAAADjdyXj43cl/+N3Jf/j' +
            'dyV3AAAAAOV3I2wAAAAAAAAAAAAAAAAAAAAACZv/gDCV4f/+cgn/7HYb/wAAAAAAAAAA43cl/+N3Jf/jdyX/43cl2wAAAADkdyRRAAAAAAAAAAAAAAAAAAAAAAqa/7QEm///HJj15/N1FA0AAAA' +
            'AAAAAAON3Jf/jdyX/43cl/+N3Jf/jdyVN43clAeR3JP3pdh9LAAAAAAAAAAAFm/8BB5v/fACe/xMAAAAAAAAAAAAAAADjdyX/43cl/+N3Jf/jdyX/43cl/eN3JVjjdyXm5Xcj/+x1G10AAAAAAAAAA' +
            'AAAAAAAAAAAAAAAAAAAAAAAAAAA43cl/+N3Jf/jdyX/43cl/+N3Jf/jdyX/43cl/+R3JP/mdiLuAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAON3JX/jdyX/43cl/+N3Jf/jdyX/43cl/+N3Jf/jd' +
            'yX/5Hck/+Z2IVAAAAAAAAAAAAAAAAAAAAAAAAAAAON3JTrjdyVh43cl/+N3Jf/jdyX/43cl/+N3Jf/jdyX/43Yk/+N2I//jdiP/5XYi/+Z2IafmdiFKAAAAAAAAAADjdyXu43cl/+N3Jf/jdyX/43cl/' +
            '+N3Jf/jdyX/43Yk/+N1Iv/ullP/54E0/+N2JP/jdyT/43cl/+N3JdvjdyUMAAAAAON3JXXjdyWl43cl7eN3Jf/jdyX/43cl/+N2JP/jdSL/6LqX/+N1If/jdiT/43cl/+N3JdHjdyVBAAAAAAAAAAAAAA' +
            'AAAAAAAAAAAAAAAAAA43clkeN3Jf/jdyT/43Yj/+N1Iv/jdiP/43ck/+N3JfUAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAADjdyUr43cll+N3JP/jdyT/43ck/+N3JenjdyUYAAAAAAAAA' +
            'AAAAAAA+AecQeADnEHAAZxBwcGcQYHhnEGF4ZxBheGcQYBjnEGAP5xBgD+cQYAfnEEAA5xBAACcQYABnEH4B5xB/AecQQ=='
        },
        'socket-io-enabled': {
            'default': false
        },
        'log-set-cookie-value': {
            'default': false
        },
        'request-intercept-for-ip-black-list-rule': {
            'enabled': false,
            'reject-code': "432",
            'global-list-enable': false,
            'reject-message': 'Rejected'
        },
        'header-x-ctrip-source-region': {
            'default': false
        },
        'default-download-image': {
            'enabled': false,
            'path': 'http://test/pages/viewpage.action?pageId=159204317#id-1.SLB%E7%94%A8%E6%88%B7%E6%89%8B%E5%86%8C-6.32%E5%BC%80%E5%90%AFDownloadImage%E6%B5%8B%E9%80%9F'
        },
        'page-id': {
            'default': false
        },
        'proxy-request-buffer-enable': {
            'default': false
        },
        'log-large-header-size': {
            'enabled': false,
            'header-size-log-large-headers': true,
            'header-size-integer': {
                'default': 100,
                'min': 0,
                'max': 1024
            }
        },
        'server-name-hash': {
            'server-name-hash-max-size': {
                "default": 10000,
                'min': 1,
                'max': 20000
            },
            'server-name-hash-bucket-size': {
                "default": 128,
                'min': 1,
                'max': 1000
            }
        },
        'access-control': {
            /*0: default*/
            /*1: white*/
            /*2: black*/
            enabled: 0,
            'access-control-allow-list': '*',
            'access-control-deny-list': '*'
        },
        'waf-rule': {
            'default': false
        }
    };

    // Scope data
    $scope.query = {
        saveResult: {},
        toggleShowAll: false,
        toggleMaxBodySize: false,
        toggleRequestTimeout: false,
        toggleClientBufferSize: false,
        toggleKeepAliveTimeout: false,
        toggleKeepAliveCount: false,
        toggleAccessByLua: false,
        toggleRewriteByLua: false,
        toggleInitByLua: false,


        toggleLocationGzip: false,
        toggleSlbHeader: false,
        toggleRequestId: false,
        toggleDefaultSSL: false,
        toggleSSL: false,

        toggleServerProxyBufferSize: false,
        toggleLargeClientHeader: false,
        toggleKeepVsAliveTimeout: false,
        toggleHsts: false,
        toggleHttp2: false,
        toggleErrorPage: false,
        toggleDefaultListen: false,


        changedSettings: false,
        toggleFavicon: false,
        toggleErrorPageEnable: false,
        toggleSocketIO: false,
        toggleIntercept: false,
        toggleSourceRegion: false,
        toggleHeaderSize: false,
        toggleSetCookie: false,
        togglePageIdEnable: false,
        toggleBodyBufferingEnable: false,
        toggleServerHash: false,
        toggleSetWaf: false
    };
    $scope.data = {};

    $scope.slb = {
        online: '',
        offline: ''
    };

    $scope.revertingRules = {};

    $scope.title = title;

    $scope.pagerules = pagerules;

    $scope.config = config;

    $scope.setLuas = [];
    $scope.addedHeders = [];
    $scope.addedRequestHeders = [];
    $scope.hideHeaders = [];
    $scope.redirects = [];

    $scope.tobeRemovedRules = [];
    var tobeRemovedRules = [];

    var parentRules = {};
    $scope.parentRules = parentRules;

    // Config keys:
    var maxbodysizekey = 'client-max-body-size';
    var requesttimeoutkey = 'proxy-read-timeout';
    var clientbuffersizekey = 'client-body-buffer-size';
    var accessbyluakey = 'access-by-lua';
    var rewritebyluakey = 'rewrite-by-lua';
    var initbyluakey = 'init-by-lua';
    var setbyluakey = 'set-by-lua';
    var addheaderkey = 'add-header';
    var addrequestheaderkey = 'set-request-header';
    var hideheaderkey = 'hide-header';
    var redirectkey = 'condition-redirect';
    var setcookieenablekey = 'log-set-cookie-value';

    var gzipsettingkey = 'gzip';
    var enableslbheaderkey = 'protocol-response-header';
    var upstreamkeepalivekey = 'upstream-keep-alive-timeout';
    var keepalivecountkey = 'upstream-keep-alive-count';
    var enablerequestidkey = "request-id-enable";
    var servernamehashkey = 'server-name-hash';
    var enableaccesscontrolkey = 'access-control';
    var wafrulekey = 'waf-rule';

    var enableserverproxybuffersizekey = "server-proxy-buffer-size-rule";
    var largeclientbuffersizekey = 'large-client-header';
    var keepalivekey = 'keep-alive-timeout';
    var enablehstskey = 'enable-hsts';

    var enableerrorpagekey = 'error-page';
    var enablehttp2key = 'server-http2-config-rule';
    var sslkey = 'ssl-config';
    var defaultsslkey = 'default-ssl-config';
    var defaultlistenkey = 'default-listen-rule';
    var enablefaviconkey = 'favicon-rule';
    var enableerrorpageenablekey = 'group-error-page-enable';
    var socketioenabledkey = "socket-io-enabled";

    var enablesourceregionkey = 'header-x-ctrip-source-region';
    var enabledownloadimagekey = 'default-download-image';
    var setpageidenablekey = 'page-id';
    var enableheaderlongkey = 'log-large-header-size';
    var proxurequestbufferingkey = 'proxy-request-buffer-enable';

    // todo: new is vs
    var defaultmaxbodysizekey = 'max-body-size';
    var defaultminbodysizekey = 'min-body-size';
    var defaultmaxreadtimeoutkey = 'max-read-timeout';
    var defaultminreadtimeoutkey = 'min-read-timeout';
    var enableinterceptkey = 'request-intercept-for-ip-black-list-rule';

    var hstsheaderkey = 'hsts-header';
    var hstsmaxagekey = 'hsts-max-age';

    $scope.getEnglishAccess = function (t) {
        var resource = $scope.resource;
        if (!resource || _.keys(resource).length == 0) return;

        return resource['slb-rule']['slb_rule_operationLogApp_accessmap'][t] || t;
    };
    // Help
    var helpArea;

    $scope.viewDoc = function (type) {
        helpArea = type;
        $('#HelpModel').modal('show');
    };

    $scope.showHelp = function (type) {
        return type == helpArea;
    };

    // Black list
    $scope.getEnableBlackListGlobalClass = function () {
        var closed = 'fa fa-toggle-off status-gray';
        var opened = 'fa fa-toggle-on status-green';

        var rule = $scope.pagerules[enableinterceptkey];
        if (!rule || rule.length > 1) return closed;

        var enabled = rule[0]['value']['global-list-enable'] == true;
        if (enabled) {
            return opened;
        }
        return closed;
    };

    $scope.toggleBlackListGlobalEnabled = function () {
        var rule = $scope.pagerules[enableinterceptkey];
        if (!rule) return false;

        rule[0]['value']['global-list-enable'] = rule[0]['value']['global-list-enable'] == undefined ? true : !rule[0]['value']['global-list-enable'];
    };

    var helpMap = {

    };
    $scope.getHelpTitle = function () {
        return helpMap[helpArea] ? helpMap[helpArea]['text'] : '';
    };

    $scope.getHelpLink = function () {
        return helpMap[helpArea] ? helpMap[helpArea]['link'] : '';
    };

    // Common Functions
    $scope.showConfigArea = function (type) {
        if (!pagerules || !pagerules[type]) return;

        var usingSelf = pagerules[type].length == 1 && (pagerules[type][0]['target'] == 'self' || pagerules[type][0]['target'] == 'global');
        return usingSelf;
    };

    $scope.getParentMaxSetting = function (type, attribute, unit) {
        var resource = $scope.resource;
        if(!resource || _.keys(resource).length==0) return;
        if (!pagerules || !pagerules[type]) return;
        var result = [];
        for (var i = 0; i < pagerules[type].length; i++) {
            var current = pagerules[type][i];

            var ruleAttributes = current['value'];
            var value = ruleAttributes[attribute];
            if (result.indexOf(value) == -1 && value) result.push(value);

            if (current.target == 'self') break;
        }

        result.sort(function (a, b) {
            return a - b;
        });

        if (result.length == 1) {
            return result[0] + unit;
        } else if (result.length == 0) {
            return resource['slb-rule']['slb_rule_operationLogApp_configtable']['未配置'];
        } else {
            return '[' + result.join(unit + ', ') + unit + ']';
        }
    };

    $scope.showSetting = function (key) {
        var rules = pagerules[key];
        if (!rules) return;
        if (rules.length == 1 && rules[0].target == 'self' && rules[0].status) return true;
        return false;
    };

    $scope.toggleParentArea = function (type) {
        var currents = pagerules[type];
        if (currents && currents.length == 1 && (currents[0].target == 'self' || currents[0].target == 'global')) {
            pagerules[type] = pagerules[type];
            pagerules[type][0].status = true;
        } else {
            pagerules[type] = [
                {
                    target: 'self',
                    status: true,
                    type: type.replace(/-/g, '_').toUpperCase(),
                    value: pagerules[type][0].value
                }
            ];
        }
    };

    // Delete
    var deleteRuleId;

    $scope.showDeleteCurrentRule = function (key) {
        var rules = pagerules[key];
        if (!rules) return;
        if (rules.length == 1 && rules[0].target == 'self' && rules[0].canDelete) return true;
        return false;
    };

    $scope.deleteCurrentRule = function (key) {
        deleteRuleId = pagerules[key][0].id;
        $('#confirmDeleteRule').modal('show');
    };

    $scope.confirmDeleteRule = function () {
        var request = {
            method: 'GET',
            url: G.baseUrl + '/api/rule/delete?ruleId=' + deleteRuleId
        };
        $http(request).success(function (response, code) {
            if (code == 200) {
                $scope.query.saveResult['delete'] = true;
            } else {
                $scope.query.saveResult['delete'] = response;
            }
            $('#saveSettingsModel').modal('show');
            setTimeout(function () {
                $('#closeSaveBt').click();
            }, 3000)
        });
    };

    $scope.toggleRedo = function (type) {
        for (var i = 0; i < pagerules[type].length; i++) {
            pagerules[type][i]['target'] = pageRulesSnapShot[type][i]['target'];
            pagerules[type][i]['value'] = $.extend(true, {}, pageRulesSnapShot[type][i]['value']);
        }
        pagerules[type][0].status = false;
    };

    // Enable Disable
    $scope.getParentEnable = function (type) {
        if (!pagerules || !pagerules[type]) return;

        var enables = [];
        for (var i = 0; i < pagerules[type].length; i++) {
            var current = pagerules[type][i];

            if (current.target == 'self') continue;
            var ruleAttributes = current['value'];
            var value = ruleAttributes["enabled"];
            if (value == undefined) value = true;
            if (enables.indexOf(value) == -1) enables.push(value);
        }

        return enables;
    };

    $scope.showEnableSetting = function (key) {
        var rules = pagerules[key];
        if (!rules) return;
        if (rules.length == 1 && rules[0].target == 'self') return true;
        return false;
    };

    $scope.getEnableClass = function (type) {
        var parentEnables = $scope.getParentEnable(type);
        if (!pagerules[type]) return;
        var usingSelf = pagerules[type].length == 1 && pagerules[type][0]['target'] == 'self';

        var enabled = false;
        if (usingSelf) {
            enabled = pagerules[type][0]['value']['enabled'] == undefined ? true : pagerules[type][0]['value']['enabled'];
        } else if (parentEnables && parentEnables.length == 1) {
            enabled = parentEnables[0];
        }

        if (enabled) {
            return 'fa fa-toggle-on status-green';
        }
        return 'fa fa-toggle-off status-gray';
    };

    $scope.toggleEnabled = function (type) {
        if (!pagerules || !pagerules[type]) return;

        var usingSelf = pagerules[type].length == 1 && pagerules[type][0]['target'] == 'self';
        if (usingSelf) {
            var status = pagerules[type][0]['status'];
            if (!status) {
                alert('请先点击右侧配置按钮');
                return;
            }

            var enabled = pagerules[type][0].value['enabled'];
            if (enabled == true) {
                pagerules[type][0].value['enabled'] = false;
            } else {
                pagerules[type][0].value['enabled'] = true;
            }
        } else {
            alert('请先点击右侧配置按钮');
            return;
        }
    };

    $scope.toggleSettingTarget = function (queryKey, type, attribtute) {
        var rules = pageRulesSnapShot;
        if (!rules) return {};

        $scope.query[queryKey] = !$scope.query[queryKey];

        if (parentRules[type]) return;
        parentRules[type] = ruleSizeMap(rules[type], attribtute);
    };

    $scope.configTarget = function (type) {
        var rules = pagerules[type];
        if (!rules) return;

        var target;
        var targets = _.unique(_.pluck(rules, 'target'));
        if (targets.length == 1) {
            target = targets[0];
        }
        if (target == 'self' || target == 'global') {
            return '<span class="badge bg-default">'+systemdefault+'</span>'
        } else if (target == 'config') {
            return '<span class="badge bg-info">'+envdefault+'</span>'
        }
    };

    $scope.showSizeError = function (type, att, max1, min1) {
        if (!pagerules[type]) return;
        var rule = pagerules[type][0];
        if (!rule) return;
        if (rule.target != 'self') return false;
        var value = rule['value'][type];

        if (att) {
            value = rule['value'][att];
        }

        var max = config[type]['max'];
        var min = config[type]['min'];

        if (max1) {
            max = max1;
        }
        if (min1) {
            min = min1;
        }
        if (!value) return true;

        var result = false;
        if (max != undefined) {
            result = value > max;
        }
        if (min != undefined) {
            result = result || (value < min);
        }
        return result;
    };

    $scope.showHeaderLogError = function (type, att) {
        if (!pagerules[type]) return;
        var rule = pagerules[type][0];
        if (!rule) return;

        if (rule.target != 'self') return false;

        var value = rule['value'][att];

        var max = config[type][att]['max'];
        var min = config[type][att]['min'];
        if (!value) return true;

        var result = false;
        if (max != undefined) {
            result = value > max;
        }
        if (min != undefined) {
            result = result || (value < min);
        }
        return result;
    };

    // Logging
    $scope.getEnableBigHeaderRecord = function (key) {
        if (!pagerules[key]) return;

        var enabled = pagerules[key][0]['value']['header-size-log-large-headers'] == true;

        if (enabled) return 'fa fa-toggle-on status-green';
        return 'fa fa-toggle-off';
    };

    $scope.toggleBigHeaderRecord = function (type) {
        if (!pagerules[type]) return;
        var usingSelf = pagerules[type].length == 1 && pagerules[type][0]['target'] == 'self';
        if (usingSelf) {
            var status = pagerules[type][0]['status'];
            if (!status) {
                alert('请先点击右侧配置按钮');
                return;
            }
            pagerules[type][0]['value']['header-size-log-large-headers'] = !pagerules[type][0]['value']['header-size-log-large-headers'];
        } else {
            alert('请先点击右侧配置按钮');
            return;
        }
    };

    $scope.getItemSrc = function (item) {
        if (item == 'config') return envdefault;
        if (item == 'global') return systemdefault;

        if (item == 'self') return systemdefault;

        return item;
    };

    $scope.showEnabledConfigures = function (type) {
        var clazz = $scope.getEnableClass(type);
        if (clazz && clazz.indexOf('fa-toggle-on') != -1) return true;
        return false;
    };

    $scope.calculateRuleMap = function () {
        $scope.query.toggleShowAll = !$scope.query.toggleShowAll;

        var rules = pageRulesSnapShot;
        if (!rules) return {};

        parentRules[maxbodysizekey] = ruleSizeMap(rules[maxbodysizekey], maxbodysizekey);
    };

    $scope.getMaxRestriction = function (max, min) {
        var result = '';
        if (min != undefined && max != undefined) {
            result = '[' + min + ',' + max + ']';
        } else if (max == undefined) {
            result = '>' + min;
        }
        return result;
    };

    function ruleSizeMap(rules, key) {
        var result = {};
        for (var i = 0; i < rules.length; i++) {
            var rule = rules[i];
            var attribute = rule['value'];
            var v = attribute[key];
            if (!result[v]) result[v] = [];
            result[v].push(rule.target);
        }

        result = _.mapObject(result, function (v, k, item) {
            v = _.unique(v);
            return v;
        });
        return result;
    }

    function ruleSizesMap(rules, keys) {
        var result = {};
        for (var i = 0; i < rules.length; i++) {
            var rule = rules[i];
            var attribute = rule['value'];
            var v = '';

            for (var j = 0; j < keys.length; j++) {
                v += attribute[keys[j]];
                if (j == 0) v += ' ';
                else v += 'k';
            }
            if (!result[v]) result[v] = [];
            result[v].push(rule.target);
        }

        result = _.mapObject(result, function (v, k, item) {
            v = _.unique(v);
            return v;
        });
        return result;
    }

    // Status
    $scope.showActivateBt = function () {
        var canActivate = A.canDo("SLB", "ACTIVATE", "*");
        return canActivate;
    };

    $scope.activateSlbs = function () {
        var request = {
            method: 'GET',
            url: G.baseUrl + '/api/slbs?type=info'
        };

        $http(request).success(function (response, code) {
            if (code == 200) {
                $scope.data.slbs = _.indexBy(response['slbs'], 'id');
                $('#activateSlbsModel').modal('show');
            }
        });
    };

    $scope.batchActivateSlbs = function () {
        var slbIds = _.keys($scope.data.slbs);
        if (slbIds.length > 0) {
            var str = slbIds.join(',');
            var request = {
                method: 'GET',
                url: G.baseUrl + '/api/activate/slb?slbId=' + str
            };

            $http(request).success(function (response, code) {
                var result;
                if (code == 200) {
                    result = 'SUCCESS';
                } else {
                    result = 'FAIL';
                }

                $scope.data.slbs = _.mapObject($scope.data.slbs, function (v, k, i) {
                    v.status = result;
                    return v;
                });
            });
        }
    };

    $scope.getActivateSlbResultClass = function (status) {
        var s = status == 'SUCCESS' ? 'status-green' : 'status-red';
        return s;
    };

    $scope.getActivateSlbResultText = function (status) {
        var result = '';
        switch (status) {
            case 'SUCCESS': {
                result = '成功';
                break
            }
            case 'FAIL': {
                result = '失败';
                break
            }
            default: {
                result = '-';
                break;
            }
        }

        return result;
    };

    // SSL
    $scope.getEnableSslServer = function (key) {
        if (!pagerules[key]) return;

        var enabled = pagerules[key][0]['value']['ssl-prefer-server-ciphers'] == true;

        if (enabled) return 'fa fa-toggle-on status-green';
        return 'fa fa-toggle-off';
    };

    $scope.sslConfigTarget = function (key) {
        var rules = pagerules[key];
        if (!rules) return;

        var target;
        var targets = _.unique(_.pluck(rules, 'target'));
        if (targets.length == 1) {
            target = targets[0];
        }
        if (target == 'self') {
            return '<span class="badge bg-default">'+systemdefault+'</span>'
        } else if (target == 'global') {
            return '<span class="badge bg-default">'+systemdefault+'</span>'
        } else if (target == 'config') {
            return '<span class="badge bg-info">'+envdefault+'</span>'
        } else {
            return '<span class="badge bg-danger">'+parentdefault+'</span>'
        }
    };

    $scope.showSslConfig = function (key) {
        var rules = pagerules[key];
        if (!rules) return;

        var target;
        var targets = _.unique(_.pluck(rules, 'target'));
        if (targets.length == 1) {
            target = targets[0];
        }
        if (target == 'self') {
            return false;
        }
        return true;
    };

    $scope.showDisableSSLSetting = function (type) {
        var rules = pagerules[type];
        if (!rules) return false;

        if (rules.length == 1 && rules[0].target == 'self') {
            return false;
        }

        var results = [];

        for (var i = 0; i < rules.length; i++) {
            var rule = rules[i];

            var clipher = rule['value']['ssl-prefer-server-ciphers'];
            var curve = rule['value']['ssl-ecdh-curve'];
            var cliphers = rule['value']['ssl-ciphers'];
            var buffersize = rule['value']['ssl-buffer-size'];
            var protocol = rule['value']['ssl-protocol'];

            var str = clipher + '/' + curve + '/' + cliphers + '/' + buffersize + '/' + protocol;
            if (results.indexOf(str) == -1) {
                results.push(str);
            }
        }
        if (results.length > 1) return true;

        return false;
    };

    $scope.toggleSSLTarget = function (type, toggle) {
        var rules = pageRulesSnapShot;
        if (!rules) return {};

        $scope.query[toggle] = !$scope.query[toggle];

        if (parentRules[type]) return;

        parentRules[type] = {};

        var rulesArray = rules[type];
        for (var i = 0; i < rulesArray.length; i++) {
            var rule = rulesArray[i];

            var clipher = rule['value']['ssl-prefer-server-ciphers'];
            var curve = rule['value']['ssl-ecdh-curve'];
            var cliphers = rule['value']['ssl-ciphers'];
            var buffersize = rule['value']['ssl-buffer-size'];
            var protocol = rule['value']['ssl-protocol'];

            var str = clipher + '/' + curve + '/' + cliphers + '/' + buffersize + '/' + protocol;
            if (!parentRules[type][str]) {
                parentRules[type][str] = rule['value'].target;
            }
        }
    };

    $scope.toggleSslEnableSSlServer = function (type) {
        if (!pagerules[type]) return;
        var usingSelf = pagerules[type].length == 1 && pagerules[type][0]['target'] == 'self';
        if (usingSelf) {
            var status = pagerules[type][0]['status'];
            if (!status) {
                alert('请先点击右侧配置按钮');
                return;
            }
            pagerules[type][0]['value']['ssl-prefer-server-ciphers'] = !pagerules[type][0]['value']['ssl-prefer-server-ciphers'];
        } else {
            alert('请先点击右侧配置按钮');
            return;
        }
    };

    // Default server
    $scope.showDisableDefaultServerSetting = function () {
        var type = defaultlistenkey;

        var rules = pagerules[type];
        if (!rules) return false;

        if (rules.length == 1 && rules[0].target == 'self') {
            return false;
        }

        var results = [];

        for (var i = 0; i < rules.length; i++) {
            var rule = rules[i];

            var protocol = rule['value']['proxy-protocol'];
            var http2 = rule['value']['http2'];
            var backlog = rule['value']['backlog'];

            var str = protocol + '/' + http2 + '/' + backlog;
            if (results.indexOf(str) == -1) {
                results.push(str);
            }
        }
        if (results.length > 1) return true;

        return false;
    };

    $scope.showDisableServerHashSetting = function () {
        var type = servernamehashkey;

        var rules = pagerules[type];
        if (!rules) return false;

        if (rules.length == 1 && rules[0].target == 'self') {
            return false;
        }

        var results = [];

        for (var i = 0; i < rules.length; i++) {
            var rule = rules[i];

            var protocol = rule['value']['server-name-hash-max-size'];
            var http2 = rule['value']['server-name-hash-bucket-size'];

            var str = protocol + '/' + http2;
            if (results.indexOf(str) == -1) {
                results.push(str);
            }
        }
        if (results.length > 1) return true;

        return false;
    };

    $scope.toggleDefaultServerTarget = function () {
        var type = defaultlistenkey;
        var rules = pageRulesSnapShot;
        if (!rules) return {};

        $scope.query.toggleDefaultListen = !$scope.query.toggleDefaultListen;

        if (parentRules[type]) return;

        parentRules[type] = {};

        var rulesArray = rules[type];
        for (var i = 0; i < rulesArray.length; i++) {
            var rule = rulesArray[i];


            var protocol = rule['value']['proxy-protocol'];
            var http2 = rule['value']['http2'];
            var backlog = rule['value']['backlog'];

            var str = protocol + '/' + http2 + '/' + backlog;
            if (!parentRules[type][str]) {
                parentRules[type][str] = rule['value'].target;
            }
        }
    };

    $scope.editDefaultListen = function () {
        $scope.enableEditDefaultServer = true;
        pagerules[defaultlistenkey] = [
            {
                target: 'self',
                type: sslkey.replace(/-/g, '_').toUpperCase(),
                value: pagerules[defaultlistenkey][0]['value']
            }
        ];
    };

    $scope.defaultListenConfigTarget = function () {
        var rules = pagerules[defaultlistenkey];
        if (!rules) return;

        var target;
        var targets = _.unique(_.pluck(rules, 'target'));
        if (targets.length == 1) {
            target = targets[0];
        }
        if (target == 'self') {
            return '<span class="badge bg-default">'+systemdefault+'</span>'
        } else if (target == 'global') {
            return '<span class="badge bg-default">'+systemdefault+'</span>'
        } else if (target == 'config') {
            return '<span class="badge bg-info">'+envdefault+'</span>'
        } else {
            return '<span class="badge bg-danger">'+parentdefault+'</span>'
        }
    };

    $scope.toggleServerHashTarget = function () {
        var type = servernamehashkey;
        var rules = pageRulesSnapShot;
        if (!rules) return {};

        $scope.query.toggleServerHash = !$scope.query.toggleServerHash;

        if (parentRules[type]) return;

        parentRules[type] = {};

        var rulesArray = rules[type];
        for (var i = 0; i < rulesArray.length; i++) {
            var rule = rulesArray[i];


            var protocol = rule['value']['server-name-hash-max-size'];
            var http2 = rule['value']['server-name-hash-bucket-size'];

            var str = protocol + '/' + http2;
            if (!parentRules[type][str]) {
                parentRules[type][str] = rule['value'].target;
            }
        }
    };

    $scope.defaultServerHashTarget = function () {
        var rules = pagerules[servernamehashkey];
        if (!rules) return;

        var target;
        var targets = _.unique(_.pluck(rules, 'target'));
        if (targets.length == 1) {
            target = targets[0];
        }
        if (target == 'self') {
            return '<span class="badge bg-success">'+systemdefault+'</span>'
        } else if (target == 'global') {
            return '<span class="badge bg-default">'+systemdefault+'</span>'
        } else if (target == 'config') {
            return '<span class="badge bg-info">生产环境默认值</span>'
        } else {
            return '<span class="badge bg-danger"'+parentdefault+'</span>'
        }
    };

    $scope.getEnableProxyProtocol = function () {
        if (!pagerules[defaultlistenkey]) return;

        var enabled = pagerules[defaultlistenkey][0]['value']['proxy-protocol'] == true;

        if (enabled) return 'fa fa-toggle-on status-green';
        return 'fa fa-toggle-off';
    };

    $scope.toggleEnableProxyProtocol = function () {
        var type = defaultlistenkey;
        if (!pagerules[type]) return;
        var usingSelf = pagerules[type].length == 1 && pagerules[type][0]['target'] == 'self';
        if (usingSelf) {
            var status = pagerules[type][0]['status'];
            if (!status) {
                alert('请先点击右侧配置按钮');
                return;
            }
            pagerules[type][0]['value']['proxy-protocol'] = !pagerules[type][0]['value']['proxy-protocol'];
        } else {
            alert('请先点击右侧配置按钮');
            return;
        }
    };

    $scope.getEnableServerHttp2 = function () {
        if (!pagerules[defaultlistenkey]) return;

        var enabled = pagerules[defaultlistenkey][0]['value']['http2'] == true;

        if (enabled) return 'fa fa-toggle-on status-green';
        return 'fa fa-toggle-off';
    };

    $scope.toggleEnableServerHttp2 = function () {
        var type = defaultlistenkey;
        if (!pagerules[type]) return;
        var usingSelf = pagerules[type].length == 1 && pagerules[type][0]['target'] == 'self';
        if (usingSelf) {
            var status = pagerules[type][0]['status'];
            if (!status) {
                alert('请先点击右侧配置按钮');
                return;
            }
            pagerules[type][0]['value']['http2'] = !pagerules[type][0]['value']['http2'];
        } else {
            alert('请先点击右侧配置按钮');
            return;
        }
    };

    // Gzip
    $scope.getGzipParentBuffers = function () {
        var type = gzipsettingkey;
        if (!pagerules || !pagerules[type]) return;
        var result = [];
        for (var i = 0; i < pagerules[type].length; i++) {
            var current = pagerules[type][i];


            var ruleAttributes = current['value'];
            var count = ruleAttributes ? ruleAttributes['gzip-buffer-count'] : 0;
            var size = ruleAttributes ? ruleAttributes['gzip-buffer-size'] : 0;

            var value = count + "个,每个" + size + "k";

            if (result.indexOf(value) == -1) result.push(value);

            if (current.target == 'self') break;
        }

        if (result.length == 1) {
            return result[0];
        } else {
            return '[' + result.join('; ') + ']';
        }
    };

    $scope.showGzipBufferCountError = function () {
        var type = "gzip";

        if (!pagerules[type]) return;
        var rule = pagerules[type][0];
        if (!rule) return;
        if (rule.target != 'self') return false;

        var value = rule['value'];
        if (!value) return true;

        var buffersCount = value['gzip-buffer-count'];
        if (!buffersCount) return true;

        var min = config["gzip"]["buffer"]["count"]["min"];
        var max = config["gzip"]["buffer"]["count"]["max"];

        if (buffersCount > max || buffersCount < min) return true;

        return false;
    };

    $scope.getGzipBufferCountErrorHtml = function () {
        var defaults = config["gzip"]["buffer"]["count"]["default"];
        var min = config["gzip"]["buffer"]["count"]["min"];
        var max = config["gzip"]["buffer"]["count"]["max"];

        return "*个数默认 " + defaults + "个, 范围:[" + min + "," + max + "]";
    };

    $scope.showGzipBufferSizeError = function () {
        var type = "gzip";

        if (!pagerules[type]) return;
        var rule = pagerules[type][0];
        if (!rule) return;
        if (rule.target != 'self') return false;

        var value = rule['value'];
        if (!value) return true;

        var buffersSize = value['gzip-buffer-size'];
        if (!buffersSize) return true;

        var min = config["gzip"]["buffer"]["size"]["min"];
        var max = config["gzip"]["buffer"]["size"]["max"];

        if (buffersSize > max || buffersSize < min) return true;

        return false;
    };

    $scope.getGzipBufferSizeErrorHtml = function () {
        var defaults = config["gzip"]["buffer"]["size"]["default"];
        var min = config["gzip"]["buffer"]["size"]["min"];
        var max = config["gzip"]["buffer"]["size"]["max"];

        return "*单个大小默认 " + defaults + "k, 范围:[" + min + "," + max + "]";
    };

    $scope.showGzipMinLengthError = function () {
        var gziprule = pagerules[gzipsettingkey];
        if (!gziprule) return;

        var rule = gziprule[0];
        if (!rule) return;

        if (rule.target != 'self') return false;


        var minLength = rule['value']['gzip-min-length'];

        var config = $scope.config;
        var max = config['gzip']['min-length']['max'];
        var min = config['gzip']['min-length']['min'];

        if (!minLength) return true;

        if (minLength > max || minLength < min || !minLength) return true;
        return false;
    };

    $scope.showGzipCompressError = function () {
        var gziprule = pagerules[gzipsettingkey];
        if (!gziprule) return;

        var rule = gziprule[0];
        if (!rule) return;
        if (rule.target != 'self') return false;
        var compressLevel = rule['value']['gzip-comp-level'];

        var config = $scope.config;
        var max = config['gzip']['compress']['max'];
        var min = config['gzip']['compress']['min'];

        if (!compressLevel) return true;

        if (compressLevel > max || compressLevel < min || !compressLevel) return true;
        return false;
    };

    $scope.showGzipTypeError = function () {
        var type = "gzip";

        if (!pagerules[type]) return;
        var rule = pagerules[type][0];
        if (!rule) return;
        if (rule.target != 'self') return false;

        var value = rule['value'];
        if (!value) return true;

        var gziptypes = value['gzip-types'];
        if (!gziptypes) return true;

        var allowed = config[type]['types']['scope'];
        var gziparray = gziptypes.split(' ');
        var common = _.intersection(allowed, gziparray);
        if (common && common.length == gziparray.length) return false;
        return true;
    };

    // Proxy Buffer Size
    $scope.getProxyBufferParentBuffers = function () {
        var type = enableserverproxybuffersizekey;
        if (!pagerules || !pagerules[type]) return;
        var result = [];
        for (var i = 0; i < pagerules[type].length; i++) {
            var current = pagerules[type][i];

            var ruleAttributes = current['value'];
            var count = ruleAttributes ? ruleAttributes['proxy-buffers-count'] : 0;
            var size = ruleAttributes ? ruleAttributes['proxy-buffers-size'] : 0;

            var value = count + " " + size + "k";

            if (result.indexOf(value) == -1) result.push(value);

            if (current.target == 'self') break;
        }

        if (result.length == 1) {
            return result[0];
        } else {
            return '[' + result.join(', ') + ']';
        }
    };

    $scope.showServerProxyBufferCountError = function () {
        var type = enableserverproxybuffersizekey;

        if (!pagerules[type]) return;
        var rule = pagerules[type][0];
        if (!rule) return;
        if (rule.target != 'self') return false;

        var value = rule['value'];
        if (!value) return true;

        var buffersCount = value['proxy-buffers-count'];
        if (!buffersCount) return true;

        var min = config[type]["proxy-buffers"]["count"]["min"];
        var max = config[type]["proxy-buffers"]["count"]["max"];

        if (buffersCount > max || buffersCount < min) return true;

        return false;
    };

    $scope.getServerProxyBufferCountErrorHtml = function () {
        var defaults = config[enableserverproxybuffersizekey]["proxy-buffers"]["count"]["default"];
        var min = config[enableserverproxybuffersizekey]["proxy-buffers"]["count"]["min"];
        var max = config[enableserverproxybuffersizekey]["proxy-buffers"]["count"]["max"];

        return "*个数默认 " + defaults + "个, 范围:[" + min + "," + max + "]";
    };

    $scope.showServerProxyBufferSizeError = function () {
        var type = enableserverproxybuffersizekey;

        if (!pagerules[type]) return;
        var rule = pagerules[type][0];
        if (!rule) return;
        if (rule.target != 'self') return false;

        var value = rule['value'];
        if (!value) return true;

        var buffersSize = value['proxy-buffers-size'];
        if (!buffersSize) return true;

        var min = config[enableserverproxybuffersizekey]["proxy-buffers"]["size"]["min"];
        var max = config[enableserverproxybuffersizekey]["proxy-buffers"]["size"]["max"];

        if (buffersSize > max || buffersSize < min) return true;

        return false;
    };

    $scope.getServerProxyBufferSizeErrorHtml = function () {
        var defaults = config[enableserverproxybuffersizekey]["proxy-buffers"]["size"]["default"];
        var min = config[enableserverproxybuffersizekey]["proxy-buffers"]["size"]["min"];
        var max = config[enableserverproxybuffersizekey]["proxy-buffers"]["size"]["max"];

        return "*单个大小默认 " + defaults + "k, 范围:[" + min + "," + max + "]";
    };

    // Large client header
    $scope.getLargeClientHeaderParent = function () {
        var type = largeclientbuffersizekey;
        if (!pagerules || !pagerules[type]) return;
        var result = [];
        for (var i = 0; i < pagerules[type].length; i++) {
            var current = pagerules[type][i];

            var ruleAttributes = current['value'];
            var count = ruleAttributes ? ruleAttributes['large-client-header-buffers-count'] : 0;
            var size = ruleAttributes ? ruleAttributes['large-client-header-buffers-size'] : 0;

            var value = count + " " + size + "k";

            if (result.indexOf(value) == -1) result.push(value);
            if (current.target == 'self') break;
        }

        if (result.length == 1) {
            return result[0];
        } else {
            return '[' + result.join(',') + ']';
        }
    };

    $scope.toggleLargeClientHeader = function (queryKey) {
        var type = largeclientbuffersizekey;
        var rules = pageRulesSnapShot;
        if (!rules) return {};

        $scope.query[queryKey] = !$scope.query[queryKey];

        var keys = ['large-client-header-buffers-count', 'large-client-header-buffers-size'];
        if (parentRules[type]) return;
        parentRules[type] = ruleSizesMap(rules[type], keys);
    };

    $scope.showLargeClientCountError = function () {
        var type = "large-client-header";

        if (!pagerules[type]) return;
        var rule = pagerules[type][0];
        if (!rule) return;
        if (rule.target != 'self') return false;

        var value = rule['value'];
        if (!value) return true;

        var count = value['large-client-header-buffers-count'];
        if (!count) return true;

        var min = config[type]["count"]["min"];
        var max = config[type]["count"]["max"];

        if (count > max || count < min) return true;

        return false;
    };

    $scope.getLargeClientCountErrorHtml = function () {
        var type = 'large-client-header';
        var defaults = config[type]["count"]["default"];
        var min = config[type]["count"]["min"];
        var max = config[type]["count"]["max"];

        return "*个数默认 " + defaults + "个, 范围:[" + min + "," + max + "]";
    };

    $scope.showLargeClientSizeError = function () {
        var type = 'large-client-header';

        if (!pagerules[type]) return;
        var rule = pagerules[type][0];
        if (!rule) return;
        if (rule.target != 'self') return false;

        var value = rule['value'];
        if (!value) return true;

        var buffersSize = value['large-client-header-buffers-size'];
        if (!buffersSize) return true;

        var min = config[type]["size"]["min"];
        var max = config[type]["size"]["max"];

        if (buffersSize > max || buffersSize < min) return true;

        return false;
    };

    $scope.getLargeClientSizeErrorHtml = function () {
        var type = 'large-client-header';
        var defaults = config[type]["size"]["default"];
        var min = config[type]["size"]["min"];
        var max = config[type]["size"]["max"];

        return "*单个大小默认 " + defaults + "k, 范围:[" + min + "," + max + "]";
    };

    // Lua
    $scope.showSetLuas = function () {
        var luas = $scope.setLuas;
        if (luas && luas.length > 0) return true;
        return false;
    };

    $scope.addLua = function () {
        var set = $scope.setLuas;
        var validate = validateAddCollection(set);
        if (!validate) {
            return;
        }
        $scope.setLuas.push({
            key: '',
            value: '',
            'isnewlyadded': true,
            'editable': true
        });
        resetSetLua();
    };

    $scope.changeSetLua = function () {
        resetSetLua();
    };

    $scope.showRemoveSetLuaBt = function (editable) {
        if (editable == true) {
            return true;
        }
        return false;
    };

    $scope.showEditLuaBt = function (editable, isnewlyadded) {
        if (editable == true && isnewlyadded == false) {
            return true;
        }
        return false;
    };

    $scope.editSetLua = function (index) {
        $scope.setLuas[index].isnewlyadded = true;
    };

    $scope.removeSetLua = function (index) {
        tobeRemovedRules.push($scope.setLuas[index].id);
        $scope.setLuas.splice(index, 1);
        // record this to be removed rule
        resetSetLua();
    };

    function resetSetLua() {
        var savedrules = _.filter($scope.setLuas, function (v) {
            return v.key && v.value && v.editable == true && v.isnewlyadded == true;
        });

        pagerules[setbyluakey] = _.map(savedrules, function (v) {
            var s = {
                target: 'self',
                type: 'SET_BY_LUA',
                value: {
                    'lua-command': v.value,
                    'lua-var': v.key
                }
            };
            if (v.id) {
                s.id = v.id;
            }

            return s;
        });
    }

    // Add header
    $scope.showAddHeaders = function () {
        var set = $scope.addedHeders;
        if (set && set.length > 0) return true;
        return false;
    };
    $scope.showAddRequestHeaders = function () {
        var set = $scope.addedRequestHeders;
        if (set && set.length > 0) return true;
        return false;
    };

    $scope.addHeader = function () {
        var set = $scope.addedHeders;
        var validate = validateAddCollection(set);
        if (!validate) {
            return;
        }
        $scope.addedHeders.push({
            key: '',
            value: '',
            'isnewlyadded': true,
            'editable': true
        });
        resetAddHeader();
    };
    $scope.addRequestHeader = function () {
        var set = $scope.addedRequestHeders;
        var validate = validateAddCollection(set);

        if (!validate) {
            return;
        }
        $scope.addedRequestHeders.push({
            key: '',
            value: '',
            'isnewlyadded': true,
            'editable': true
        });
        resetAddRequestHeader();
    };

    $scope.changeAddedHeader = function () {
        resetAddHeader();
    };
    $scope.changeAddedRequestHeader = function () {
        resetAddRequestHeader();
    };

    $scope.showRemoveAddHeaderBt = function (editable) {
        if (editable == true) {
            return true;
        }
        return false;
    };
    $scope.showRemoveAddRequestHeaderBt = function (editable) {
        if (editable == true) {
            return true;
        }
        return false;
    };

    $scope.showEditAddHeaderBt = function (editable, isnewlyadded) {
        if (editable == true && isnewlyadded == false) {
            return true;
        }
        return false;
    };
    $scope.showEditAddRequestHeaderBt = function (editable, isnewlyadded) {
        if (editable == true && isnewlyadded == false) {
            return true;
        }
        return false;
    };

    $scope.editAddedHeader = function (index) {
        $scope.addedHeders[index].isnewlyadded = true;
    };
    $scope.editAddedRequestHeader = function (index) {
        $scope.addedRequestHeders[index].isnewlyadded = true;
    };

    $scope.removeHideHeader = function (index) {
        tobeRemovedRules.push($scope.hideHeaders[index].id);
        $scope.hideHeaders.splice(index, 1);
        // record this to be removed rule
        resetHideHeader();
    };
    $scope.removeAddRequestHeader = function (index) {
        tobeRemovedRules.push($scope.addedRequestHeders[index].id);
        $scope.addedRequestHeders.splice(index, 1);
        // record this to be removed rule
        resetAddRequestHeader();
    };

    function validateAddHeaders(headers) {
        if (!headers || headers.length == 0) return true;
        var m = {};
        for (var i = 0; i < headers.length; i++) {
            var v = headers[i];
            var key = v['key'];
            var value = v['value'];

            if (!key || !value) return false;

            if (m[key + "/" + value]) {
                return false;
            } else {
                m[key + "/" + value] = 1;
            }
        }

        return true;
    }


    function validateHideHeaders(headers) {
        if (!headers || headers.length == 0) return true;
        var m = {};

        for (var i = 0; i < headers.length; i++) {
            var v = headers[i];
            var key = v['key'];
            if (!key) return false;
            if (m[key]) {
                return false
            } else {
                m[key] = 1;
            }
        }

        return true;
    }

    function resetAddRequestHeader() {
        var savedrules = _.filter($scope.addedRequestHeders, function (v) {
            return v.key && v.value && v.editable == true && v.isnewlyadded == true;
        });

        pagerules[addrequestheaderkey] = _.map(savedrules, function (v) {
            var s = {
                target: 'self',
                type: 'SET_REQUEST_HEADER',
                value: {
                    'header-key': v.key,
                    'header-value': v.value
                }
            };
            if (v.id) {
                s.id = v.id;
            }

            return s;
        });
    }

    // Access Control
    $scope.toggleAccessControl = function (value) {
        var rule = $scope.pagerules[enableaccesscontrolkey] ? $scope.pagerules[enableaccesscontrolkey][0]['value'] : '';
        if (!rule) return;

        switch (value) {
            case 'default': {
                rule['enabled'] = 0;
                break;
            }
            case 'white': {
                rule['enabled'] = 1;
                break;
            }
            case 'black': {
                rule['enabled'] = 2;
                break;
            }
            default: {
                break;
            }
        }
    };

    $scope.accessControlChecked = function (value) {
        var rule = $scope.pagerules[enableaccesscontrolkey] ? $scope.pagerules[enableaccesscontrolkey][0]['value'] : '';
        if (!rule) return;
        var enabled = rule['enabled'];

        switch (enabled) {
            case 0: {
                return value == 'default';
            }
            case 1: {
                return value == 'white';
            }
            case 2: {
                return value == 'black';
            }
        }
    };

    function resetAddHeader() {
        var savedrules = _.filter($scope.addedHeders, function (v) {
            return v.key && v.value && v.editable == true && v.isnewlyadded == true;
        });

        pagerules[addheaderkey] = _.map(savedrules, function (v) {
            var s = {
                target: 'self',
                type: 'ADD_HEADER',
                value: {
                    'header-key': v.key,
                    'header-value': v.value
                }
            };
            if (v.id) {
                s.id = v.id;
            }

            return s;
        });
    }

    function validateAddCollection(set) {
        if (set.length == 0) return true;
        // blank keys
        var invalidItems = _.filter(set, function (v) {
            return v.key == '' || v.value == '';
        });
        if (invalidItems && invalidItems.length > 0) {
            alert('Set record shall both has key and value!');
            return false;
        }

        var countBy = _.countBy(_.map(set, function (v) {
            v.key == v.key.toLowerCase();
            return v;
        }), 'key');

        var hasMore = _.filter(_.values(countBy), function (v) {
            return v > 1;
        });
        if (hasMore && hasMore.length > 0) {
            alert("Set record's key shall be unique");
            return false;
        }

        return true;
    };

    // Hide header
    $scope.showHideHeaders = function () {
        var set = $scope.hideHeaders;
        if (set && set.length > 0) return true;
        return false;
    };

    $scope.addHideHeader = function () {
        var set = $scope.hideHeaders;

        var validate = validateHideCollection(set);
        if (!validate) {
            return;
        }
        $scope.hideHeaders.push({
            key: '',
            'isnewlyadded': true,
            'editable': true
        });
        resetHideHeader();
    };

    $scope.changeHideHeader = function () {
        resetHideHeader();
    };

    $scope.showRemoveHideHeaderBt = function (editable) {
        if (editable == true) {
            return true;
        }
        return false;
    };

    $scope.showEditHideHeaderBt = function (editable, isnewlyadded) {
        if (editable == true && isnewlyadded == false) {
            return true;
        }
        return false;
    };

    $scope.editHideHeader = function (index) {
        $scope.hideHeaders[index].isnewlyadded = true;
    };

    $scope.removeAddHeader = function (index) {
        tobeRemovedRules.push($scope.addedHeders[index].id);
        $scope.addedHeders.splice(index, 1);
        // record this to be removed rule
        resetAddHeader();
    };

    function resetHideHeader() {
        var savedrules = _.filter($scope.hideHeaders, function (v) {
            return v.key && v.editable == true && v.isnewlyadded == true;
        });

        pagerules[hideheaderkey] = _.map(savedrules, function (v) {
            var s = {
                target: 'self',
                type: 'HIDE_HEADER',
                value: {
                    'header-key': v.key
                }
            };
            if (v.id) {
                s.id = v.id;
            }

            return s;
        });
    }

    function validateHideCollection(set) {
        if (set.length == 0) return true;
        // blank keys
        var invalidItems = _.filter(set, function (v) {
            return v.key == '';
        });
        if (invalidItems && invalidItems.length > 0) {
            alert('Set record shall has key!');
            return false;
        }

        var countBy = _.countBy(_.map(set, function (v) {
            v.key == v.key.toLowerCase();
            return v;
        }), 'key');

        var hasMore = _.filter(_.values(countBy), function (v) {
            return v > 1;
        });
        if (hasMore && hasMore.length > 0) {
            alert("Set record's key shall be unique");
            return false;
        }

        return true;
    };

    // Redirect control
    $scope.showRedirect = function () {
        var redirects = $scope.redirects;
        if (redirects && redirects.length > 0) return true;
        return false;
    };

    $scope.addRedirect = function () {
        var set = $scope.redirects;

        var validate = validateRedirects(set);
        if (!validate) {
            return;
        }
        $scope.redirects.push({
            target: '',
            condition: '',
            code: '',
            'isnewlyadded': true,
            'editable': true
        });
        resetRedirect();
    };

    $scope.changeRedirect = function () {
        resetRedirect();
    };

    $scope.editRedirect = function (index) {
        $scope.redirects[index].isnewlyadded = true;
    };

    $scope.showRemoveRedirectBt = function (editable) {
        if (editable == true) {
            return true;
        }
        return false;
    };

    $scope.removeRedirect = function (index) {
        tobeRemovedRules.push($scope.redirects[index].id);
        $scope.redirects.splice(index, 1);
        // record this to be removed rule
        resetRedirect();
    };

    $scope.showEditRedirectBt = function (editable, isnewlyadded) {
        if (editable == true && isnewlyadded == false) {
            return true;
        }
        return false;
    };

    function validateRedirects(set) {
        if (set.length == 0) return true;
        // blank keys
        var invalidItems = _.filter(set, function (v) {
            return v.target == '' || v.condition == '' || v.code == '';
        });
        if (invalidItems && invalidItems.length > 0) {
            alert('Set record shall has key, value and condition!');
            return false;
        }

        var countBy = _.countBy(_.map(set, function (v) {
            v.target == v.target.toLowerCase();
            return v;
        }), 'key');

        var hasMore = _.filter(_.values(countBy), function (v) {
            return v > 1;
        });
        if (hasMore && hasMore.length > 0) {
            alert("Set record's key shall be unique");
            return false;
        }

        return true;
    }

    function resetRedirect() {
        var savedrules = _.filter($scope.redirects, function (v) {
            return v.target && v.editable == true && v.code && v.condition && v.isnewlyadded == true;
        });

        pagerules[redirectkey] = _.map(savedrules, function (v) {
            var s = {
                target: 'self',
                type: 'CONDITION_REDIRECT',
                value: {
                    'target': v.target,
                    'response-code': v.code,
                    'condition': v.condition
                }
            };
            if (v.id) {
                s.id = v.id;
            }

            return s;
        });
    }

    // Save result
    $scope.showSuccessMsg = function () {
        var result = $scope.query.saveResult;
        var show = false;
        if (result && _.keys(result).length > 0) {
            var update = result['update'];
            var deleted = result['delete'];

            if (update != undefined) {
                show = update == true;
            }
            if (deleted != undefined) {
                show = deleted == true;
            }
        }
        return show;
    };

    $scope.getErrorMsg = function () {
        var result = $scope.query.saveResult;
        var content = '<div>';
        if (result && _.keys(result).length > 0) {
            var update = result['update'];
            var deleted = result['delete'];

            if (update != undefined) {
                if (update == true) {
                    content += '<div class="status-green">更新：成功</div>';
                } else {
                    content += '<div>更新失败信息 ' + update.message + '</div>';
                }
            }
            if (deleted != undefined) {
                if (deleted == true) {
                    content += '<div class="status-green">删除：成功</div>';
                } else {
                    content += '<div>删除失败信息 ' + deleted.message + '</div>';
                }
            }
        }
        content += '</div>';
        return content;
    };


    // Redirect control
    $scope.showRedirect = function () {
        var redirects = $scope.redirects;
        if (redirects && redirects.length > 0) return true;
        return false;
    };

    $scope.addRedirect = function () {
        var set = $scope.redirects;

        var validate = validateRedirects(set);
        if (!validate) {
            return;
        }
        $scope.redirects.push({
            target: '',
            condition: '',
            code: '',
            'isnewlyadded': true,
            'editable': true
        });
        resetRedirect();
    };

    $scope.changeRedirect = function () {
        resetRedirect();
    };

    $scope.editRedirect = function (index) {
        $scope.redirects[index].isnewlyadded = true;
    };

    $scope.showRemoveRedirectBt = function (editable) {
        if (editable == true) {
            return true;
        }
        return false;
    };

    $scope.removeRedirect = function (index) {
        tobeRemovedRules.push($scope.redirects[index].id);
        $scope.redirects.splice(index, 1);
        // record this to be removed rule
        resetRedirect();
    };

    $scope.showEditRedirectBt = function (editable, isnewlyadded) {
        if (editable == true && isnewlyadded == false) {
            return true;
        }
        return false;
    };

    function validateRedirects(set) {
        if (set.length == 0) return true;
        // blank keys
        var invalidItems = _.filter(set, function (v) {
            return v.target == '' || v.condition == '' || v.code == '';
        });
        if (invalidItems && invalidItems.length > 0) {
            alert('Set record shall has key, value and condition!');
            return false;
        }

        var countBy = _.countBy(_.map(set, function (v) {
            v.target == v.target.toLowerCase();
            return v;
        }), 'key');

        var hasMore = _.filter(_.values(countBy), function (v) {
            return v > 1;
        });
        if (hasMore && hasMore.length > 0) {
            alert("Set record's key shall be unique");
            return false;
        }

        return true;
    }

    function resetRedirect() {
        var savedrules = _.filter($scope.redirects, function (v) {
            return v.target && v.editable == true && v.code && v.condition && v.isnewlyadded == true;
        });

        pagerules[redirectkey] = _.map(savedrules, function (v) {
            var s = {
                target: 'self',
                type: 'CONDITION_REDIRECT',
                value: {
                    'target': v.target,
                    'response-code': v.code,
                    'condition': v.condition
                }
            };
            if (v.id) {
                s.id = v.id;
            }

            return s;
        });
    }

    // Rights
    $scope.showChangeRule = function () {
        // can do group update?
        var groupId = $scope.query.groupId;
        if (!groupId) return false;
        var groupUpdate = A.canDo("Group", "UPDATE", groupId);
        var groupActivate = A.canDo("Group", "ACTIVATE", groupId);
        var groupOp = groupUpdate && groupActivate;
        return !groupOp;
    };

    $scope.getAuthLink = function () {
        var groupId = $scope.query.groupId;
        var url1 = G[$scope.env].urls.api
            + '/api/auth/apply/mail?userName=' +
            $scope.query.user +
            '&targetId=' + groupId
            + '&op=ACTIVATE,UPDATE,READ&type=Group&env=' + $scope.env;

        var request = {
            method: 'GET',
            url: url1
        };
        $http(request).success(
            function () {
                alert('权限申请已经发出，请等待SLB Team审批通过。');
            }
        );
    };

    // Actions
    $scope.showRevert = function () {
        var id = $scope.query.groupId;
        if (!id) return false;
        var access = A.canDo("Group", "UPDATE", id);

        // rules related to child
        if (!access) return false;

        // is there rules need to be deleted or updated?
        var needRemovedRules = getNeedDeleteRules();
        var needUpdateRules = getNeedUpdateRules();
        var needAddedbackrules = getNeedAddedRules();

        if (needRemovedRules && needUpdateRules && needAddedbackrules) {
            if (needRemovedRules.length > 0 || needUpdateRules.length > 0 || needAddedbackrules.length > 0) {
                return true;
            }
        }
        return false;
    };

    $scope.disableSaving = function () {
        var same = compareTwoPageRule(pagerules, pageRulesSnapShot);

        // control size validation
        // control size validation
        var errors = false;
        if (pagerules[enableinterceptkey] && pagerules[enableinterceptkey].length == 1 && pagerules[enableinterceptkey][0]['value'].enabled == true) {
            errors = !pagerules[enableinterceptkey][0]['value']['reject-code'] || !pagerules[enableinterceptkey][0]['value']['reject-message'];
            if (errors) return true;
        }

        if (pagerules[gzipsettingkey] && pagerules[gzipsettingkey].length == 1 && pagerules[gzipsettingkey][0]['value'].enabled) {
            errors = $scope.showGzipMinLengthError() ||
                $scope.showGzipCompressError() ||
                $scope.showGzipTypeError() ||
                $scope.showGzipBufferCountError() ||
                $scope.showGzipBufferSizeError();
            if (errors) return errors;
        }

        if (pagerules[enableaccesscontrolkey] && pagerules[enableaccesscontrolkey].length == 1 && pagerules[enableaccesscontrolkey][0]['value'].enabled > 0) {
            var e = pagerules[enableaccesscontrolkey][0]['value']['enabled'];
            var es = false;
            var white = pagerules[enableaccesscontrolkey][0]['value']['access-control-allow-list'];
            var deny = pagerules[enableaccesscontrolkey][0]['value']['access-control-deny-list'];

            switch (e) {
                case 0: {
                    break;
                }
                case 1: {
                    es = !white || white.length == 0;
                    // white list
                    break;
                }
                case 2: {
                    es = !deny || deny.length == 0;
                    break;
                    // black list
                }
            }

            if (es) return es;
        }


        if (pagerules[servernamehashkey] && pagerules[servernamehashkey].length == 1 && pagerules[servernamehashkey][0]['target'] == 'self') {
            errors = $scope.showSizeError('server-name-hash', 'server-name-hash-max-size',
                config['server-name-hash']['server-name-hash-max-size']['max'],
                config['server-name-hash']['server-name-hash-max-size']['min'])
                ||
                $scope.showSizeError('server-name-hash', 'server-name-hash-bucket-size',
                    config['server-name-hash']['server-name-hash-bucket-size']['max'],
                    config['server-name-hash']['server-name-hash-bucket-size']['min']);

            if (errors) return errors;
        }

        if (pagerules[largeclientbuffersizekey] && pagerules[largeclientbuffersizekey].length == 1 && pagerules[largeclientbuffersizekey][0].target == 'self') {
            errors = $scope.showLargeClientCountError() || $scope.showLargeClientSizeError();
            if (errors) return errors;
        }

        if (pagerules[enableserverproxybuffersizekey] && pagerules[enableserverproxybuffersizekey].length == 1 && pagerules[enableserverproxybuffersizekey][0]['value'].enabled) {
            errors = $scope.showSizeError(enableserverproxybuffersizekey, 'proxy-buffer-size')
                || $scope.showServerProxyBufferSizeError()
                || $scope.showServerProxyBufferCountError()
                || $scope.showSizeError(enableserverproxybuffersizekey, 'proxy-busy-buffers-size');
            if (errors) return errors;
        }

        if (pagerules[enablehstskey] && pagerules[enablehstskey].length == 1 && pagerules[enablehstskey][0]['value'].enabled == true) {
            errors = $scope.showSizeError(enablehstskey, 'hsts-max-age', config[enablehstskey]['hsts-max-age']['max'], config[enablehstskey]['hsts-max-age']['min']);
            if (errors) return errors;
        }

        if (pagerules[enablehttp2key] && pagerules[enablehttp2key].length == 1 && pagerules[enablehttp2key][0]['value'].enabled == true) {
            errors = $scope.showSizeError('server-http2-config-rule', 'http2-body-preread-size', config['server-http2-config-rule']['http2-body-preread-size']['max'], config['server-http2-config-rule']['http2-body-preread-size']['min'])
                || $scope.showSizeError('server-http2-config-rule', 'http2-chunk-size', config['server-http2-config-rule']['http2-chunk-size']['max'], config['server-http2-config-rule']['http2-chunk-size']['min'])
                || $scope.showSizeError('server-http2-config-rule', 'http2-idle-timeout', config['server-http2-config-rule']['http2-idle-timeout']['max'], config['server-http2-config-rule']['http2-idle-timeout']['min'])
                || $scope.showSizeError('server-http2-config-rule', 'http2-max-concurrent-streams', config['server-http2-config-rule']['http2-max-concurrent-streams']['max'], config['server-http2-config-rule']['http2-max-concurrent-streams']['min'])
                || $scope.showSizeError('server-http2-config-rule', 'http2-max-field-size', config['server-http2-config-rule']['http2-max-field-size']['max'], config['server-http2-config-rule']['http2-max-field-size']['min'])
                || $scope.showSizeError('server-http2-config-rule', 'http2-max-header-size', config['server-http2-config-rule']['http2-max-header-size']['max'], config['server-http2-config-rule']['http2-max-header-size']['min'])
                || $scope.showSizeError('server-http2-config-rule', 'http2-max-requests', config['server-http2-config-rule']['http2-max-requests']['max'], config['server-http2-config-rule']['http2-max-requests']['min'])
                || $scope.showSizeError('server-http2-config-rule', 'http2-recv-buffer-size', config['server-http2-config-rule']['http2-recv-buffer-size']['max'], config['server-http2-config-rule']['http2-recv-buffer-size']['min'])
                || $scope.showSizeError('server-http2-config-rule', 'http2-recv-timeout', config['server-http2-config-rule']['http2-recv-timeout']['max'], config['server-http2-config-rule']['http2-recv-timeout']['min']);

            if (errors) return errors;
        }

        if (pagerules[sslkey] && pagerules[sslkey].length == 1 && pagerules[sslkey][0]['target'] == "self") {
            errors = !pagerules[sslkey][0]['value']['ssl-buffer-size']
                || pagerules[sslkey][0]['value']['ssl-buffer-size'] < 0
                || !pagerules[sslkey][0]['value']['ssl-protocol'];

            if (errors) return errors;
        }

        if (pagerules[defaultlistenkey] && pagerules[defaultlistenkey].length == 1 && pagerules[defaultlistenkey][0]['target'] == "self") {
            errors = !pagerules[defaultlistenkey][0]['value']['backlog']
                || pagerules[sslkey][0]['value']['backlog'] < 0;

            if (errors) return errors;
        }

        if (pagerules[defaultlistenkey] && pagerules[defaultlistenkey].length == 1 && pagerules[defaultlistenkey][0]['target'] == 'self') {
            errors = $scope.showSizeError('default-listen-rule', 'backlog', config['default-listen-rule']['backlog']['max'], config['default-listen-rule']['backlog']['min']);
            if (errors) return errors;
        }

        if (pagerules[enablefaviconkey] && pagerules[enablefaviconkey].length == 1 && pagerules[enablefaviconkey][0]['target'] == "self") {
            errors = !pagerules[enablefaviconkey][0]['value']['favicon-base64-code'];
            if (errors) return errors;
        }

        if (pagerules[enableheaderlongkey] && pagerules[enableheaderlongkey].length == 1 && pagerules[enableheaderlongkey][0]['value'].enabled == true) {
            errors = $scope.showHeaderLogError(enableheaderlongkey, 'header-size-integer', config[enableheaderlongkey]['header-size-integer']['max'],
                config[enableheaderlongkey]['header-size-integer']['min']);
            if (errors) return true;
        }
        if (pagerules[enabledownloadimagekey] && pagerules[enabledownloadimagekey].length == 1 && pagerules[enabledownloadimagekey][0]['target'] == "self") {
            errors = !pagerules[enabledownloadimagekey][0]['value']['path'];
            if (errors) return errors;
        }

        errors = $scope.showSizeError(maxbodysizekey);
        if (errors) return errors;
        errors = $scope.showSizeError(requesttimeoutkey);
        if (errors) return errors;
        errors = $scope.showSizeError(clientbuffersizekey);
        if (errors) return errors;
        errors = $scope.showSizeError(upstreamkeepalivekey, "upstream-keep-alive-timeout");
        if (errors) return errors;
        errors = $scope.showSizeError(keepalivekey, "client-keep-alive-timeout");
        if (errors) return errors;

        // control required validtion
        var showRewriteByLua = $scope.showConfigArea('rewrite-by-lua');
        var showAccessByLua = $scope.showConfigArea('access-by-lua');
        var showInitByLua = $scope.showConfigArea('init-by-lua');

        var requiredPassed = true;
        if (!pagerules['rewrite-by-lua'] || !pagerules['access-by-lua'] || !pagerules['init-by-lua']) {
            return false;
        }

        var rewriteCmd = pagerules['rewrite-by-lua'] ? pagerules['rewrite-by-lua'][0]['value']['lua-command'] : '';
        if (showRewriteByLua && !rewriteCmd) requiredPassed = false;

        var accessCmd = pagerules['access-by-lua'] ? pagerules['access-by-lua'][0]['value']['lua-command'] : '';
        if (showAccessByLua && !accessCmd) requiredPassed = false;

        var initCmd = pagerules['init-by-lua'] ? pagerules['init-by-lua'][0]['value']['lua-command'] : '';
        if (showInitByLua && !initCmd) requiredPassed = false;

        var kvvalidatepassed = true;

        var headers = $scope.addedHeders;
        var requestheaders = $scope.addedRequestHeders;
        var hideheaders = $scope.hideHeaders;

        if (headers && headers.length > 0) {
            kvvalidatepassed = kvvalidatepassed && validateAddHeaders(headers);
        }
        if (requestheaders && requestheaders.length > 0) {
            kvvalidatepassed = kvvalidatepassed && validateAddHeaders(requestheaders);
        }
        if (hideheaders && hideheaders.length > 0) {
            kvvalidatepassed = kvvalidatepassed && validateHideHeaders(hideheaders);
        }


        var result = false;
        if (same) {
            result = true;
        }
        if (!requiredPassed) {
            result = true;
        }
        if (errors) {
            result = true;
        }
        if (!kvvalidatepassed) {
            result = true;
        }
        return result;
    };

    $scope.revert = function () {
        var needRemovedRules = getNeedDeleteRules();
        var needUpdateRules = getNeedUpdateRules();
        var needAddedRules = getNeedAddedRules();

        var torevertrules = needRemovedRules.concat(needUpdateRules).concat(needAddedRules);
        $scope.revertingRules = mapRuleValues(torevertrules);

        $('#revertModel').modal('show');
    };

    $scope.confirmRevert = function () {
        var groupId = $scope.query.groupId;

        if (!groupId) return;

        var needRemovedRules = getNeedDeleteRules();
        var needUpdateRules = getNeedUpdateRules();
        var needAddedRules = getNeedAddedRules();

        var requests = [];
        if (needRemovedRules.length > 0) {
            requests.push(
                $http({
                    method: 'GET',
                    url: G.baseUrl + '/api/rule/delete?ruleId=' + needRemovedRules.join(',')
                }).success(function (response, code) {

                })
            );
        }

        if (needUpdateRules.length > 0 || needAddedRules.length > 0) {
            var newBody = {
                'target-id': groupId,
                'target-type': 'Group',
                rules: []
            };
            $.each(needUpdateRules, function (i, v) {
                newBody.rules.push(v);
            });

            $.each(needAddedRules, function (i, v) {
                newBody.rules.push(v);
            });

            requests.push($http({
                method: 'POST',
                url: G.baseUrl + '/api/rule/set',
                data: newBody
            }))
        }

        if (requests.length == 0) {
            alert('Nothing to revert');
            return;
        }

        $q.all(requests).then(function (data) {
            if (data.length > 0) {
                $scope.query.saveResult = true;
            } else {
                $scope.query.saveResult = data;
            }
            $('#operationConfrimModel').modal('show');
        });
    };

    $scope.getRuleType = function (rule) {
        if (!rule) return '';

        var ruleType = rule['rule-type'];
        var convertedRuleType = ruleType.replace(/_/g, '-').toLowerCase();

        return title[convertedRuleType];
    };

    $scope.getNewValue = function (rule) {
        if (!rule) return;

        var online = $scope.slb['online'];
        if (!online) return;

        var ruleType = rule['rule-type'];
        var convertedType = ruleType.replace(/_/g, '-').toLowerCase();

        var rulesets = _.indexBy(online['rule-set'], 'id');
        if (rulesets[rule.id]) {
            return JSON.parse(rulesets[rule.id]['attributes'])[convertedType] || JSON.parse(rulesets[rule.id]['attributes'])["enabled"];
        } else {
            return 'Default';
        }
    };

    $scope.saveSettings = function () {
        var disabled = $scope.disableSaving();
        if (disabled) return;

        var set = {
            'target-id': "*",
            'target-type': 'DEFAULT',
            'rules': []
        };

        var page = $scope.pagerules;
        var old = pageRulesSnapShot;

        $.each(page, function (k, v) {
            var s = old[k];
            var same = compareTwoPageRuleList(v, s) && compareTwoPageRuleList(s, v);
            if (!same) {
                for (var i = 0; i < v.length; i++) {
                    var rule = v[i];
                    if (rule['target'] == 'self' || rule['target'] == 'global') {
                        var named = 'Global_' + rule.type;
                        if (named.length > 50) {
                            named = named.substring(0, 40);
                        }

                        // new a rule
                        var saved = {
                            'name': named,
                            'target-id': '*',
                            'target-type': 'DEFAULT',
                            'rule-type': rule['type'],
                            'attributes': JSON.stringify(rule['value'])
                        };
                        if (rule.id) {
                            saved.id = rule.id;
                        }
                        set.rules.push(saved);
                    }
                }
            }
        });
        for (var i = 0; i < set.rules.length; i++) {
            // for access control rule
            if (set.rules[i]['rule-type'] == 'ACCESS_CONTROL') {
                var attr = set.rules[i]['attributes'];
                if (attr) {
                    var attrJson = JSON.parse(attr);
                    switch (attrJson['enabled']) {
                        case 0: {
                            // no rule
                            if (set.rules[i].id) {
                                tobeRemovedRules.push(set.rules[i].id);
                            }
                            set.rules.splice(i, 1);
                            break;
                        }

                        case 1: {
                            // white
                            attrJson['enabled'] = true;
                            delete attrJson['access-control-deny-list'];
                            set.rules[i]['attributes'] = JSON.stringify(attrJson);
                            break;
                        }

                        case 2: {
                            // black
                            attrJson['enabled'] = false;
                            delete attrJson['access-control-allow-list'];
                            set.rules[i]['attributes'] = JSON.stringify(attrJson);
                            break;
                        }
                    }
                }
            }
        }
        var requests = [];
        if (set.rules.length > 0) {
            var request = {
                url: G.baseUrl + '/api/rule/set',
                method: 'POST',
                data: set
            };
            requests.push($http(request).success(function (response, code) {
                if (code == 200) {
                    $scope.query.saveResult['update'] = true;
                } else {
                    $scope.query.saveResult['update'] = response;
                }
            }));
        }

        if (tobeRemovedRules.length > 0) {
            var params = _.map(tobeRemovedRules, function (v) {
                return 'ruleId=' + v;
            });
            var url = G.baseUrl + '/api/rule/delete?' + params.join('&');
            var request2 = {
                url: url,
                method: 'GET'
            };

            requests.push($http(request2).success(function (response, code) {
                    if (code == 200) {
                        $scope.query.saveResult['delete'] = true;
                    } else {
                        $scope.query.saveResult['delete'] = response;
                    }
                })
            );
        }


        if (requests.length > 0) {
            $q.all(requests).then(function (data) {
                    $('#saveSettingsModel').modal("show");
                    setTimeout(function () {
                        var pair = {
                            timeStamp: new Date().getTime()
                        };
                        $('#saveSettingsModel').modal("hide");
                        H.setData(pair);
                    }, 2000);
                }
            );
        }
    };

    // Common methods
    $scope.loadData = function () {
        tobeRemovedRules = [];
        $scope.query.saveResult = {};
        // default rules
        var defaultRulesQuery = {
            method: 'GET',
            url: G.baseUrl + '/api/rules?targetId=*&targetType=default'
        };

        var defaultRulesRequest = $http(defaultRulesQuery).success(function (response, code) {
            if (code == 200) {
                var temp = response['rules'];
                rules['global'] = _.groupBy(temp, 'rule-type');
            }
        });



        $q.all([defaultRulesRequest]).then(function () {
            var globalRules = rules['global'];

            var configs = $scope.config;
            var configGzip = {};
            var configMaxBodySize = {};
            var configRequestTimeout = {};
            var configClientBodyBufferSize = {};
            var configEnableSlbHeader = {};
            var configEnableRequestId = {};
            var configKeepAliveTime = {};
            var configKeepAliveCount = {};
            var configClientKeepAliveTime = {};
            var configAccessByLua = {};
            var configRewriteByLua = {};
            var configInitByLua = {};
            var configSetCookieEnable = {};
            var configAccessControl = {};
            var configWafRule = {};

            var configSourceRegion = {};
            var configDownloadImageEnable = {};
            var configPageIdEnable = {};
            var configHeaderSizeLogEnable = {};
            var configRequestBodyBufferEnable = {};


            var configSetByLua = [];
            var configAddHeader = [];
            var configHideHeader = [];
            var configConditionRedirect = [];
            var configBuffersize = {};
            var configLargeClientBufferSize = {};
            var configHsts = {};
            var configHttp2 = {};
            var configErrorPage = {};
            var configSsl = {};
            var configDefaultSsl = {};
            var configDefautlServer = {};
            var configFavicon = {};
            var configErrorPageEnable = {};
            var configWebsocketEnable = {};
            var configInterceptEnable = {};
            var configServerNameHash = {};
            configWebsocketEnable['enabled'] = configs['socket-io-enabled']['default'];

            configMaxBodySize[maxbodysizekey] = configs['client-max-body-size']['default'];
            configRequestTimeout[requesttimeoutkey] = configs['proxy-read-timeout']['default'];
            configClientBodyBufferSize[clientbuffersizekey] = configs['client-body-buffer-size']['default'];
            configEnableSlbHeader['enabled'] = configs['protocol-response-header']['default'];
            configEnableRequestId['enabled'] = configs['request-id-enable']['default'];
            configAccessByLua['lua-command'] = configs[accessbyluakey]['default'];
            configRewriteByLua['lua-command'] = configs[rewritebyluakey]['default'];
            configSetCookieEnable['enabled'] = configs['log-set-cookie-value']['default'];
            configInitByLua['lua-command'] = configs[initbyluakey]['default'];
            configSetByLua = configs[setbyluakey]['default'];
            configAddHeader = configs[addheaderkey]['default'];
            configHideHeader = configs[hideheaderkey]['default'];
            configConditionRedirect = configs[redirectkey]['default'];
            configKeepAliveTime['upstream-keep-alive-timeout'] = configs[upstreamkeepalivekey]['default'];
            configKeepAliveCount['upstream-keep-alive-count'] = configs[keepalivecountkey]['default'];
            configClientKeepAliveTime['client-keep-alive-timeout'] = configs[keepalivekey]['default'];
            configGzip = {
                enabled: configs['gzip']['enabled'],
                'gzip-min-length': configs['gzip']['min-length']['default'],
                'gzip-types': configs['gzip']['types']['default'],
                'gzip-buffer-count': configs['gzip']['buffer']['count']['default'],
                'gzip-buffer-size': configs['gzip']['buffer']['size']['default'],
                'gzip-comp-level': configs['gzip']['compress']['default']
            };
            configLargeClientBufferSize = {
                'large-client-header-buffers-count': config['large-client-header']['count']['default'],
                'large-client-header-buffers-size': config['large-client-header']['size']['default']
            };
            configBuffersize = {
                'enabled': configs['server-proxy-buffer-size-rule']['enabled'],
                'proxy-buffer-size': configs['server-proxy-buffer-size-rule']['proxy-buffer-size']['default'],
                'proxy-buffers-size': configs['server-proxy-buffer-size-rule']['proxy-buffers']['size']['default'],
                'proxy-buffers-count': configs['server-proxy-buffer-size-rule']['proxy-buffers']['count']['default'],
                'proxy-busy-buffers-size': configs['server-proxy-buffer-size-rule']['proxy-busy-buffers-size']['default']
            };
            configHsts = {
                enabled: configs['enable-hsts']['enabled'],
                'hsts-max-age': configs['enable-hsts']['hsts-max-age']['default']
            };
            configHttp2 = {
                'enabled': config['server-http2-config-rule'].enabled,
                'http2-body-preread-size': config['server-http2-config-rule']['http2-body-preread-size']["default"],
                'http2-chunk-size': config['server-http2-config-rule']['http2-chunk-size']["default"],
                'http2-idle-timeout': config['server-http2-config-rule']['http2-idle-timeout']["default"],
                'http2-max-concurrent-streams': config['server-http2-config-rule']['http2-max-concurrent-streams']["default"],
                'http2-max-field-size': config['server-http2-config-rule']['http2-max-field-size']["default"],
                'http2-max-header-size': config['server-http2-config-rule']['http2-max-header-size']["default"],
                'http2-max-requests': config['server-http2-config-rule']['http2-max-requests']["default"],
                'http2-recv-buffer-size': config['server-http2-config-rule']['http2-recv-buffer-size']["default"],
                'http2-recv-timeout': config['server-http2-config-rule']['http2-recv-timeout']["default"]
            };
            configErrorPage = {
                enabled: configs['error-page']['default']
            };
            configSsl = {
                'ssl-prefer-server-ciphers': configs['ssl-config']['ssl-prefer-server-ciphers'],
                'ssl-ecdh-curve': configs['ssl-config']['ssl-ecdh-curve'],
                'ssl-ciphers': configs['ssl-config']['ssl-ciphers'],
                'ssl-buffer-size': configs['ssl-config']['ssl-buffer-size'],
                'ssl-protocol': configs['ssl-config']['ssl-protocol']
            };
            configDefaultSsl = {
                'ssl-prefer-server-ciphers': configs[defaultsslkey]['ssl-prefer-server-ciphers'],
                'ssl-ecdh-curve': configs[defaultsslkey]['ssl-ecdh-curve'],
                'ssl-ciphers': configs[defaultsslkey]['ssl-ciphers'],
                'ssl-buffer-size': configs[defaultsslkey]['ssl-buffer-size'],
                'ssl-protocol': configs[defaultsslkey]['ssl-protocol']
            };
            configDefautlServer = {
                'proxy-protocol': configs[defaultlistenkey]['proxy-protocol'],
                'http2': configs[defaultlistenkey]['http2'],
                'backlog': configs[defaultlistenkey]['backlog']['default']
            };
            configFavicon = configs[enablefaviconkey];
            configErrorPageEnable = {
                enabled: configs['group-error-page-enable']['default']
            };
            configInterceptEnable = {
                enabled: configs['request-intercept-for-ip-black-list-rule']['enabled'],
                'reject-code': configs['request-intercept-for-ip-black-list-rule']['reject-code'],
                'global-list-enable': configs['request-intercept-for-ip-black-list-rule']['global-list-enable'],
                'reject-message': configs['request-intercept-for-ip-black-list-rule']['reject-message']
            };
            configSourceRegion = {
                enabled: configs['header-x-ctrip-source-region']['default']
            };
            configDownloadImageEnable = configs[enabledownloadimagekey];
            configRequestBodyBufferEnable['enabled'] = configs['proxy-request-buffer-enable']['default'];
            configPageIdEnable['enabled'] = configs['page-id']['default'];
            configHeaderSizeLogEnable = {
                'enabled': configs['log-large-header-size']['enabled'],
                'header-size-log-large-headers': configs['log-large-header-size']['header-size-log-large-headers'],
                'header-size-integer': configs['log-large-header-size']['header-size-integer']['default']
            };
            configServerNameHash = {
                'server-name-hash-max-size': config[servernamehashkey]['server-name-hash-max-size']['default'],
                'server-name-hash-bucket-size': config[servernamehashkey]['server-name-hash-bucket-size']['default']
            };
            configWafRule = {
                enabled: configs['waf-rule']['default']
            };
            configAccessControl = {
                'enabled': configs['access-control']['enabled'],
                'access-control-allow-list': '*',
                'access-control-deny-list': '*'
            };
            //******** 组装 page rules ********

            // parent rules result
            var rule1 =
                getDefaultRule(configMaxBodySize,
                    globalRules,
                    'CLIENT_MAX_BODY_SIZE');
            var rule2 =
                getDefaultRule(configRequestTimeout,
                    globalRules,
                    'PROXY_READ_TIMEOUT');
            var rule3 =
                getDefaultRule(configClientBodyBufferSize,
                    globalRules,
                    'CLIENT_BODY_BUFFER_SIZE');

            var rule5 =
                getDefaultRule(configEnableSlbHeader,
                    globalRules,
                    'PROTOCOL_RESPONSE_HEADER');

            var rule6 =
                getDefaultRule(configKeepAliveTime,
                    globalRules,
                    'UPSTREAM_KEEP_ALIVE_TIMEOUT');

            var rule7 =
                getDefaultRule(configEnableRequestId,
                    globalRules,
                    'REQUEST_ID_ENABLE');

            var rule8 =
                getDefaultRule(configAccessByLua,
                    globalRules,
                    'ACCESS_BY_LUA');

            var rule9 =
                getDefaultRule(configRewriteByLua,
                    globalRules,
                    'REWRITE_BY_LUA');
            var rule10 =
                getDefaultRule(configInitByLua,
                    globalRules,
                    'INIT_BY_LUA');
            var rule11 =
                getDefaultRule(configSetByLua,
                    globalRules,
                    'SET_BY_LUA');
            var rule12 =
                getDefaultRule(configAddHeader,
                    globalRules,
                    'ADD_HEADER');

            var rule13 =
                getDefaultRule(configHideHeader,
                    globalRules,
                    'HIDE_HEADER');

            var rule15 =
                getDefaultRule(configConditionRedirect,
                    globalRules,
                    'CONDITION_REDIRECT');

            var rule14 =
                getDefaultRule(configGzip,
                    globalRules,
                    'GZIP');

            var rule16 =
                getDefaultRule(configBuffersize,
                    globalRules,
                    'SERVER_PROXY_BUFFER_SIZE_RULE');

            var rule17 =
                getDefaultRule(configLargeClientBufferSize,
                    globalRules,
                    'LARGE_CLIENT_HEADER');

            var rule18 =
                getDefaultRule(configClientKeepAliveTime,
                    globalRules,
                    'KEEP_ALIVE_TIMEOUT');

            var rule19 =
                getDefaultRule(configHsts,
                    globalRules,
                    'ENABLE_HSTS');


            var rule20 =
                getDefaultRule(configHttp2,
                    globalRules,
                    'SERVER_HTTP2_CONFIG_RULE');
            var rule21 =
                getDefaultRule(configErrorPage,
                    globalRules,
                    'ERROR_PAGE');
            var rule22 =
                getDefaultRule(configSsl,
                    globalRules,
                    'SSL_CONFIG');
            var rule23 =
                getDefaultRule(configDefautlServer,
                    globalRules,
                    'DEFAULT_LISTEN_RULE');
            var rule24 =
                getDefaultRule(configKeepAliveCount,
                    globalRules,
                    'UPSTREAM_KEEP_ALIVE_COUNT');

            var rule25 =
                getDefaultRule(configDefaultSsl,
                    globalRules,
                    'DEFAULT_SSL_CONFIG');
            var rule26 =
                getDefaultRule(configFavicon,
                    globalRules,
                    'FAVICON_RULE');
            var rule27 =
                getDefaultRule(configErrorPageEnable,
                    globalRules,
                    'GROUP_ERROR_PAGE_ENABLE');

            var rule28 =
                getDefaultRule(configWebsocketEnable,
                    globalRules,
                    'SOCKET_IO_ENABLED');

            var rule29 =
                getDefaultRule(configSetCookieEnable,
                    globalRules,
                    'LOG_SET_COOKIE_VALUE');

            var rule30 =
                getDefaultRule(configAddHeader,
                    globalRules,
                    'SET_REQUEST_HEADER');

            var rule31 =
                getDefaultRule(configInterceptEnable,
                    globalRules,
                    'REQUEST_INTERCEPT_FOR_IP_BLACK_LIST_RULE');

            var rule32 =
                getDefaultRule(configDownloadImageEnable,
                    globalRules,
                    'DEFAULT_DOWNLOAD_IMAGE');

            var rule33 =
                getDefaultRule(configHeaderSizeLogEnable,
                    globalRules,
                    'LOG_LARGE_HEADER_SIZE');

            var rule34 =
                getDefaultRule(configPageIdEnable,
                    globalRules,
                    'PAGE_ID');

            var rule35 =
                getDefaultRule(configRequestBodyBufferEnable,
                    globalRules,
                    'PROXY_REQUEST_BUFFER_ENABLE');

            var rule36 =
                getDefaultRule(configSourceRegion,
                    globalRules,
                    'HEADER_X_CTRIP_SOURCE_REGION');

            var rule37 =
                getDefaultRule(configAccessControl,
                    globalRules,
                    'ACCESS_CONTROL');

            var rule38 = getDefaultRule(configServerNameHash,
                globalRules,
                'SERVER_NAME_HASH');
            var rule39 =
                getDefaultRule(configWafRule,
                    globalRules,
                    'WAF_RULE');
            pagerules[maxbodysizekey] = rule1;
            pagerules[requesttimeoutkey] = rule2;
            pagerules[clientbuffersizekey] = rule3;
            pagerules[enableslbheaderkey] = rule5;
            pagerules[upstreamkeepalivekey] = rule6;
            pagerules[enablerequestidkey] = rule7;
            pagerules[accessbyluakey] = rule8;
            pagerules[rewritebyluakey] = rule9;
            pagerules[initbyluakey] = rule10;
            pagerules[setbyluakey] = rule11;
            pagerules[addheaderkey] = rule12;
            pagerules[hideheaderkey] = rule13;

            pagerules[gzipsettingkey] = rule14;
            pagerules[redirectkey] = rule15;

            pagerules[enableserverproxybuffersizekey] = rule16;
            pagerules[largeclientbuffersizekey] = rule17;
            pagerules[keepalivekey] = rule18;
            pagerules[enablehstskey] = rule19;
            pagerules[enablehttp2key] = rule20;
            pagerules[enableerrorpagekey] = rule21;
            pagerules[sslkey] = rule22;
            pagerules[defaultlistenkey] = rule23;
            pagerules[keepalivecountkey] = rule24;
            pagerules[defaultsslkey] = rule25;
            pagerules[enablefaviconkey] = rule26;
            pagerules[enableerrorpageenablekey] = rule27;
            pagerules[socketioenabledkey] = rule28;
            pagerules[addrequestheaderkey] = rule30;
            pagerules[setcookieenablekey] = rule29;
            pagerules[enableinterceptkey] = rule31;
            pagerules[enabledownloadimagekey] = rule32;
            pagerules[enableheaderlongkey] = rule33;
            pagerules[setpageidenablekey] = rule34;
            pagerules[proxurequestbufferingkey] = rule35;
            pagerules[enablesourceregionkey] = rule36;
            pagerules[servernamehashkey] = rule38;
            pagerules[wafrulekey] = rule39;
            if (rule37 && rule37.length > 0) {
                var value = rule37[0].value['enabled'];
                if (value != undefined) {
                    switch (value) {
                        case true: {
                            rule37[0].value['enabled'] = 1;
                            delete rule37[0].value['access-control-deny-list'];
                            break;
                        }
                        case false: {
                            rule37[0].value['enabled'] = 2;
                            delete rule37[0].value['access-control-allow-list'];
                            break;
                        }
                        default: {
                            rule37[0].value['enabled'] = 0;
                            break;
                        }
                    }
                }
                pagerules[enableaccesscontrolkey] = rule37;
            }
            // init setbylua items
            var s = $.extend(true, [], rule11);
            $scope.setLuas = _.map(s, function (v) {
                v.editable = v.target == 'self';
                v.key = v['value']['lua-var'];
                v.value = v['value']['lua-command'];
                v.isnewlyadded = false;
                return v;
            });

            var t = $.extend(true, [], rule12);
            t = _.reject(t, function (a) {
                return a.target != 'self';
            });
            $scope.addedHeders = _.map(t, function (v) {
                v.editable = v.target == 'self';
                v.key = v['value']['header-key'];
                v.value = v['value']['header-value'];
                v.isnewlyadded = false;
                return v;
            });

            var r = $.extend(true, [], rule30);
            r = _.reject(r, function (a) {
                return a.target != 'self';
            });
            $scope.addedRequestHeders = _.map(r, function (v) {
                v.editable = v.target == 'self';
                v.key = v['value']['header-key'];
                v.value = v['value']['header-value'];
                v.isnewlyadded = false;
                return v;
            });

            var m = $.extend(true, [], rule13);
            m = _.reject(m, function (a) {
                return a.target != 'self';
            });
            $scope.hideHeaders = _.map(m, function (v) {
                v.editable = v.target == 'self';
                v.key = v['value']['header-key'];
                v.isnewlyadded = false;
                return v;
            });

            var n = $.extend(true, [], rule15);
            $scope.redirects = _.map(n, function (v) {
                v.editable = v.target == 'self';
                v.target = v['value']['target'];
                v.condition = v['value']['condition'];
                v.code = v['value']['response-code'];
                v.isnewlyadded = false;
                return v;
            });

            pageRulesSnapShot = $.extend(true, {}, pagerules);
        });

        $http.get('/api/auth/current/user').success(
            function (response) {
                if (response && !response.code) {
                    $scope.query.user = response['name'];
                    $scope.query.email = response.mail;
                }
            }
        );
    };

    function getDefaultRule(config, defaults, type) {
        //var type = 'CLIENT_MAX_BODY_SIZE';
        var defaultRules = defaults[type];

        var result = [];

        if (defaultRules) {
            for (var l = 0; l < defaultRules.length; l++) {
                result.push({
                    canDelete: true,
                    target: 'self',
                    type: type,
                    id: defaultRules[l].id,
                    value: getAttributeValue(defaultRules[l])
                });
            }
        } else {
            if (config instanceof Array) {
                for (var m = 0; m < config.length; m++) {
                    var hasConfig = _.find(result, function (v) {
                        return v.target == 'config';
                    });
                    if (!hasConfig) {
                        result.push({
                            target: 'config',
                            type: type,
                            value: config[m]
                        });
                    }
                }
            } else {
                var hasConfig = _.find(result, function (v) {
                    return v.target == 'config';
                });
                if (!hasConfig) {
                    result.push({
                        target: 'config',
                        type: type,
                        value: config
                    });
                }
            }
        }
        return result;
    }

    function getNeedAddedRules() {
        var online = $scope.slb['online'];
        var offline = $scope.slb['offline'];

        if (!online && !offline) {
            return;
        }
        var onlinerules = online['rule-set'];
        var offlinerules = offline['rule-set'];

        var onlinerulesobj = _.indexBy(onlinerules, 'id');
        var result = [];
        // offline 没有, online有.
        var offlineRuleIds = _.pluck(offlinerules, 'id');
        var onlineRuleIds = _.pluck(onlinerules, 'id');
        for (var i = 0; i < onlineRuleIds.length; i++) {
            var ruleId = onlineRuleIds[i];
            if (offlineRuleIds.indexOf(ruleId) == -1) {
                result.push(onlinerulesobj[ruleId]);
            }
        }

        return result;
    }

    function getNeedDeleteRules() {
        var online = $scope.slb['online'];
        var offline = $scope.slb['offline'];

        if (!online && !offline) {
            return;
        }
        var onlinerules = online['rule-set'];
        var offlinerules = offline['rule-set'];

        var offlinerulesobj = _.indexBy(offlinerules, 'id');

        var result = [];
        // offline 有, online没有.
        var offlineRuleIds = _.pluck(offlinerules, 'id');
        var onlineRuleIds = _.pluck(onlinerules, 'id');
        for (var i = 0; i < offlineRuleIds.length; i++) {
            var ruleId = offlineRuleIds[i];
            if (onlineRuleIds.indexOf(ruleId) == -1) {
                result.push(offlinerulesobj[ruleId]);
            }
        }

        return result;
    }

    function getNeedUpdateRules() {
        var online = $scope.slb['online'];
        var offline = $scope.slb['offline'];

        if (!online && !offline) {
            return;
        }
        var onlinerules = online['rule-set'];
        var offlinerules = offline['rule-set'];

        var result = [];
        var offlineRuleIds = _.pluck(offlinerules, 'id');
        var onlineRuleIds = _.pluck(onlinerules, 'id');
        var sameRuleIds = _.intersection(offlineRuleIds, onlineRuleIds);

        var onlinerulesobj = _.indexBy(onlinerules, 'id');
        var offlinerulesobj = _.indexBy(offlinerules, 'id');

        _.map(sameRuleIds, function (v) {
            var onlineRule = onlinerulesobj[v];
            var offlineRule = offlinerulesobj[v];

            var same = compareTwoRule(onlineRule, offlineRule);
            if (!same) result.push(onlineRule);
        });

        return result;
    }

    function compareTwoRule(left, right) {
        var result = false;

        if (left['rule-type'] != right['rule-type']) return result;

        if (left && right) {
            var att1 = left['attributes'];
            var att2 = right['attributes'];

            if (att1 && att2) {
                var att1json = JSON.parse(att1);
                var att2json = JSON.parse(att2);
                result = compareTwoRuleAttributes(att1json, att2json);
            } else if (!att1 && !att2) {
                result = true;
            }
        } else if (!left && !right) {
            result = true;
        }

        return result;
    }

    function compareTwoPageRule(left, right) {
        var result = false;

        if (left && right) {
            var key1 = _.keys(left);
            var key2 = _.keys(right);
            if (key1 && key2) {
                var keys = _.union(key1, key2);
                for (var i = 0; i < keys.length; i++) {
                    var aRules = left[keys[i]];
                    var bRules = right[keys[i]];
                    if (aRules && bRules) {
                        if (aRules.length != bRules.length) {
                            result = false;
                            return result;
                        }
                        result = compareTwoPageRuleList(aRules, bRules) && compareTwoPageRuleList(bRules, aRules);
                        if (!result) {
                            return result;
                        }
                    } else if (!aRules && !bRules) {
                        result = true;
                    }
                }
            }
        } else if (!left && !right) {
            result = true;
        } else {
            return false;
        }

        return result;
    }

    function compareTwoRuleAttributes(a, b) {
        var result = false;

        var aKeys = _.keys(a);
        var bKeys = _.keys(b);
        var abKeys = _.union(aKeys, bKeys);

        for (var j = 0; j < abKeys.length; j++) {
            var c = a[abKeys[j]];
            var d = b[abKeys[j]];

            var ctype = typeof c;
            var dtype = typeof d;
            if (ctype != dtype) {
                result = false;
                break;
            }

            if (ctype == 'object') {
                result = compareTwoRuleAttributes(c, d);
                if (result == false) break;
            } else {
                if (c != d) {
                    result = false;
                    break
                } else {
                    result = true;
                }
            }
        }

        return result;
    }

    function compareRuleVersion(offline, online) {
        var offlineRules;
        var onlineRules;

        if (!online) return 'deactivated';
        onlineRules = online['rule-set'];

        if (offline) {
            offlineRules = offline['rule-set'];

            //  有offline 并且offline是因为rule引起的
            if (!offlineRules && !onlineRules) return 'activated';

            if (!(offlineRules && onlineRules)) return 'tobeactivated';

            var result = compareTwoRuleList(offlineRules, onlineRules) && compareTwoRuleList(onlineRules, offlineRules);
            if (result) return 'activated';
            return 'tobeactivated';
        } else {
            return 'activated';
        }

    }

    function compareTwoRuleList(lefts, rights) {
        var result = false;
        for (var i = 0; i < lefts.length; i++) {
            var left = lefts[i];
            var rightHas = _.find(rights, function (v) {
                return compareTwoRule(left, v);
            });

            if (rightHas) {
                result = true;
            } else {
                result = false;
                break;
            }
        }
        return result;
    }

    function compareTwoPageRuleList(lefts, rights) {
        var result = false;
        if ((!lefts && !rights) || (lefts.length == 0 && rights.length == 0)) return true;
        for (var i = 0; i < lefts.length; i++) {
            var left = lefts[i];
            var rightHas = _.find(rights, function (v) {
                return comparePageRule(left, v);
            });

            if (rightHas) {
                result = true;
            } else {
                result = false;
                break;
            }
        }
        return result;
    }

    function comparePageRule(left, right) {
        var result = false;
        if (!left || !right) return result;

        if (left['type'] != right['type'] || left.target != right.target) return result;

        if (left && right) {
            var att1json = left['value'];
            var att2json = right['value'];

            if (att1json && att2json) {
                result = compareTwoRuleAttributes(att1json, att2json);
            } else if (!att1json && !att2json) {
                result = true;
            }
        } else if (!left && !right) {
            result = true;
        }

        return result;
    }


    function getAttributeValue(rule) {
        var att = JSON.parse(rule['attributes']);
        return att;
    }

    function mapRuleValues(rs) {
        var ts = $.extend(true, [], rs);
        return _.map(ts, function (v) {
            v.value = v['attributes'];
            return v;
        });
    };

    $scope.applyHashData = function (hashData) {
        $scope.env = hashData.env;
        $scope.query.env = hashData.env;
        $scope.query.slbId = hashData.slbId;
    };

    $scope.hashChanged = function (hashData) { $scope.resource = H.resource;
        systemdefault = H.resource['slb-rule']['slb_rule_operationLogApp_configmap']['systemdefault'];
        envdefault = H.resource['slb-rule']['slb_rule_operationLogApp_configmap']['envdefault'];
        slbdefault = H.resource['slb-rule']['slb_rule_operationLogApp_configmap']['slbdefault'];
        parentdefault = H.resource['slb-rule']['slb_rule_operationLogApp_configmap']['parentdefault'];

        $scope.applyHashData(hashData);
        $scope.loadData();
    };
    H.addListener("operationLogApp", $scope, $scope.hashChanged);
});
angular.bootstrap(document.getElementById("operation-log-area"), ['operationLogApp']);
