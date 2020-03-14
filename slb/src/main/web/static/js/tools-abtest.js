/**
 * Created by ygshen on 2017/4/28.
 */
//InfoLinksComponent: info links
var headerInfoApp = angular.module('headerInfoApp', []);
headerInfoApp.controller('headerInfoController', function ($scope, $http) {
    $scope.resource = H.resource;
    var resource= $scope.resource;
    $scope.query = {};
    $scope.generateLink = function (a) {
        var link = '';
        switch (a) {
            case 'list': {

                link = "/portal/tools#?env=" + G.env;
                break;
            }
            case 'url': {
                link = "/portal/tools/visiturl#?env=" + G.env;
                break;
            }
            case 'test': {
                link = "/portal/tools/test#?env=" + G.env;
                break;
            }
            case 'http-https': {
                link = "/portal/tools/redirect/redirects#?env=" + G.env;
                break;
            }
            case 'bug': {
                link = "/portal/tools/problem#?env=" + G.env;
                break;
            }
            case 'verify': {
                link = "/portal/tools/verify#?env=" + G.env;
                break;
            }
            case 'slb-migration': {
                link = "/portal/tools/smigration/migrations#?env=" + G.env;
                break;
            }
            case 'slb-sharding': {
                link = "/portal/tools/sharding/shardings#?env=" + G.env;
                break;
            }
            case 'vs-migration': {
                link = "/portal/tools/vmigration/migrations#?env=" + G.env;
                break;
            }
            case 'vs-merge': {
                link = "/portal/tools/vs/merges#?env=" + G.env;
                break;
            }
            case 'vs-seperate': {
                link = "/portal/tools/vs/seperates#?env=" + G.env;
                break;
            }
            case 'dr': {
                link = "/portal/tools/dr/drs#?env=" + G.env;
                break;
            }
            case 'cert-upgrade': {
                link = "/portal/tools/cert/migrations";
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
    };
    H.addListener("headerInfoApp", $scope, $scope.hashChanged);
});
angular.bootstrap(document.getElementById("header-area"), ['headerInfoApp']);

var toolsQueryUrlApp = angular.module('toolsQueryUrlApp', ["ngSanitize", "http-auth-interceptor", "ng-code-mirror"]);
toolsQueryUrlApp.controller('toolsQueryUrlController', function ($scope, $rootScope, $http, $q) {
$scope.resource = H.resource;
    var resource= $scope.resource;
    $scope.data = {
        requestMethods: [{id: 1, name: "GET"}, {id: 2, name: "POST"}, {id: 3, name: "PUT"}],
        bodyModes: ['form-data', 'x-www-form-urlencoded', 'raw'],
        requestHeaders: [{
            key: '',
            value: '',
            type: 'text'
        }],
        requestBodyFormData: [{
            key: '',
            value: '',
            type: 'text'
        }],
        requestBodyEncodeFormData: [{
            key: '',
            value: ''

        }],
        responseArray: [],
        responseObject: {},
        renderClasses: ['Pretty', 'Raw'],
        renderFormats: []
    };
    $scope.show = {
        showResponse: false
    };
    $scope.query = {
        appId: '',
        policyId: '',
        showResult: false,
        uri: '',
        host: '',
        url: '',
        defaultBodyFormat: 'form-data'
    };
    $scope.model = {
        policy: {},
        groups: {},
        vses: {},
        controls: [],
        apps: {}
    };
    $scope.view = {
        urls: []
    };
    $scope.responses = [];
    $scope.selectedMethod = undefined;
    $scope.addHeaders = false;

    $scope.cacheRequestFn = function (str) {
        return {q: str, timestamp: new Date().getTime()};
    };
    $scope.remoteAppsUrl = function () {
        return G.baseUrl + "/api/meta/apps";
    };

    $scope.selectAppId = function (o) {
        if (o) {
            $scope.query.appId = o.originalObject.id;
            $scope.changeAppIdSelection();
        }
    };
    $scope.changeAppIdSelection = function () {
        // GET Policy related to current appid
    };

    $scope.getAppIDC = function (g) {
        var ps = _.find(g.properties, function (item) {
            return item.name.toLowerCase() == 'idc';
        });
        if (ps) return ps.value;
        return 'unknown';
    };
    $scope.getAppBU = function (g) {
        var ps = _.find(g.properties, function (item) {
            return item.name.toLowerCase() == 'sbu';
        });
        if (ps) return ps.value;
        return 'unknown';
    };
    $scope.getAppName = function (g) {
        var appId = g['app-id'];
        var appName = $scope.model.apps[appId] ? $scope.model.apps[appId]['chinese-name'] : 'unknown';

        return appId + '(' + appName + ')';
    };
    $scope.updateRequestMethod = function (v) {
        $('#red').removeClass('alert-border');
    };

    $scope.showRequestBody = function () {
        return $scope.selectedMethod && $scope.selectedMethod.id != 1;
    };

    $scope.setHeadersClick = function () {
        $scope.addHeaders = !$scope.addHeaders;
    };
    $scope.showRequestHeaders = function () {
        return $scope.addHeaders;
    };

    $scope.getRequestHeaderCount = function () {
        var l = _.filter($scope.data.requestHeaders, function (item) {
            return item.key && item.value;
        });
        return l.length;
    };
    $scope.addNewHeaderPair = function () {
        $scope.data.requestHeaders.push({
            key: '',
            value: ''
        })
    };
    $scope.deleteHeaderPair = function (index) {
        if ($scope.data.requestHeaders.length == 1) return;
        $scope.data.requestHeaders.splice(index, 1);
    };
    $scope.chooseTarget = function (host) {
        $scope.query.host = host;
    };
    $scope.isSelectedBodyFormat = function (type) {
        if (type != $scope.query.defaultBodyFormat) return '';
        return 'active';
    };
    $scope.toggleDefaultBodyFormat = function (type) {
        $scope.query.defaultBodyFormat = type;
    };
    $scope.addNewBodyPair = function (type) {
        switch (type) {
            case 'form': {
                $scope.data.requestBodyFormData.push({
                    key: '',
                    value: '',
                    type: 'text'
                });
                break;
            }
            case 'encode': {
                $scope.data.requestBodyEncodeFormData.push({
                    key: '',
                    value: ''
                });
                break;
            }
            default:
                break;
        }
    };
    $scope.deletePair = function (index) {
        if ($scope.data.requestBodyFormData.length == 1) return;
        $scope.data.requestBodyFormData.splice(index, 1);
    };
    $scope.deleteEncodePair = function (index) {
        if ($scope.data.requestBodyEncodeFormData.length == 1) return;
        $scope.data.requestBodyEncodeFormData.splice(index, 1);
    };

    $scope.getDropDownItems = function () {
        var result = $scope.query.host;

        if (!result) {
            result = $scope.view.urls[0];
        }
        return result;
    };
    $scope.sendQuery = function () {
        $scope.show.showResponse = true;
        $('#uri').removeClass('alert-border');
        $('#red').removeClass('alert-border');

        var host = $scope.query.host;
        var uri = $scope.query.uri;
        var method = $scope.selectedMethod;
        var body = '';

        var api = G['fws'].urls.api+'/api/tools/butest';
        var default_type = 'text';
        var default_format = 'json';

        var methodsWithBody = ["POST", "PUT", "PATCH", "DELETE", "LINK", "UNLINK"];

        if (!method) {
            $('#red').removeClass('alert-border').addClass('alert-border');
            return;
        }
        method = method.name;
        $('.response-body-area').showLoading();


        // params in the url
        var url;
        var params = [];

        if (uri) {
            // get params
            var p = uri.split('?');
            if (p && p.length > 1) {
                var q = p[1].split('&');
                for (var i = 0; i < q.length; i++) {
                    var s = q[i];
                    var t = s.split('=');
                    params.push(
                        {
                            key: t[0],
                            value: t[1]
                        }
                    );
                }
            }
            url = encodeUrl(host + (p[0].startsWith('/') ? p[0] : '/' + p[0]));
        } else {
            url = host;
        }
        $scope.query.url = url;

        if (methodsWithBody.indexOf(method) != -1) {
            var isForm = $scope.query.defaultBodyFormat == 'form-data';
            var isEncode = $scope.query.defaultBodyFormat == 'x-www-form-urlencoded';
            var isRaw = $scope.query.defaultBodyFormat == 'raw';

            var paramsBodyData = new FormData();
            var urlEncodedBodyData = '';
            var rawBodyData = $scope.operationReason;
            var hasFormData = false;

            if (isForm) {
                $.each($scope.data.requestBodyFormData, function (i, item) {
                    if (item.type == 'text') {
                        if (item.key && item.value) {
                            paramsBodyData.append(item.key, item.value);
                            hasFormData = true;
                        }
                    } else {
                        var e = document.getElementById("form" + item.key);
                        if (item.key && e.files[0]) {
                            paramsBodyData.append(item.key, e.files[0]);
                            hasFormData = true;
                        }
                    }
                });
            }
            if (isEncode) {
                $.each($scope.data.requestBodyEncodeFormData, function (i, item2) {
                    var key = item2.key;
                    var value = item2.value;
                    if (key && value) {
                        key = encodeURIComponent(key.trim());
                        key = key.replace(/%20/g, '+');

                        value = encodeURIComponent(value.trim());
                        value = value.replace(/%20/g, '+');
                    }

                    urlEncodedBodyData += key + "=" + value + "&";
                });
                urlEncodedBodyData = urlEncodedBodyData.substr(0, urlEncodedBodyData.length - 1);
            }

            body = {
                formData: isForm && hasFormData ? paramsBodyData : undefined,
                encodeData: isEncode ? urlEncodedBodyData : undefined,
                raw: isRaw ? rawBodyData : ''
            };
        }

        // Body
        if (isRaw) body = rawBodyData;
        if (isEncode) body = urlEncodedBodyData;
        if (isForm && hasFormData) body = paramsBodyData;


        if (methodsWithBody.indexOf(method) != -1 && !body) {
            $('#requestBody').removeClass('alert-border').addClass('alert-border');
            return;
        }

        var ch = _.map($scope.data.requestHeaders, function (v) {
                if (v.key && v.value) {
                    return {
                        key: v.key,
                        value: v.value
                    }
                }
            }
        );

        if (ch.length == 1 && ch[0] == undefined) ch = [];
        var requestData = {
            method: method,
            url: url,
            params: params,
            body: body,
            'custom-headers': _.filter(ch, function (v) {
                return v != undefined;
            })
        };

        var c = [];
        $.each($scope.model.controls, function (i, item) {
            var groupId = item.id;

            var vip = getCurrentGroupVip(groupId);

            requestData.cookie = 'slbshardingcookieflag=' + groupId;
            requestData.vip = vip;

            // get current
            var appId = $scope.model.groups[groupId]['app-id'];
            var appName = appId + '(' + $scope.model.apps[appId]['chinese-name'] + ')';

            var p = new postman(api, requestData, default_type, default_format, appName, groupId, function (startTime, app, groupId, response) {
                    if (response.readyState == 4) {
                        var endTime = new Date().getTime();
                        response.timecost = endTime - startTime;
                        response.app = app;
                        response.groupId = groupId;
                        response.groupName = $scope.model.groups[groupId].name;
                        response.resHeaders = response.getAllResponseHeaders();
                        c.push(response);
                        if (c.length > 1) {
                            $scope.$broadcast('done', c);
                        }
                    }
                }
            );
            p.sendRequest();
        });
        $scope.$on('done', function (v) {
            $scope.$apply(function () {
                c = $.extend([], true, c);
                /* c[0].responseNewText = c[0].responseText;
                 c[1].responseNewText = c[1].responseText;*/
                $scope.data.responseArray = c;

                $scope.data.responseObject = _.indexBy(c, function (i) {
                    return i.groupId;
                });
            });
            $('.response-body-area').hideLoading();
        });
    };

    function getCurrentGroupVip(groupId) {
        // 这个域名在这个Group中的哪个VS
        var host = $scope.query.host;
        var reg = /(http|https):(\/\/)(.*)/gi;
        var match = reg.exec(host);

        var protocol;
        var host;
        if (match && match.length == 4) {
            protocol = match[1];
            host = match[3];

            var isSSL = protocol.toLowerCase() == 'https';

            var group = $scope.model.groups[groupId];
            var f = _.find(group['group-virtual-servers'], function (s) {
                var sslOk = s['virtual-server'].ssl == isSSL;
                var domains = _.pluck(s['virtual-server'].domains, 'name');
                var hostOk = domains.indexOf(host) != -1;

                return sslOk && hostOk;
            });

            if (f) {
                var vsId = f['virtual-server'].id;
                return $scope.model.vses[vsId]['vip'];
            }
            return '';
        }

        return '';
    }

    $scope.showScreen = function (type, app) {
        switch (type) {
            case "failed": {
                if (app.status == 0) {
                    return true;
                }
                break;
            }
            case "success": {
                if (app.status != 0) {
                    return true;
                }
                break;
            }
            default :
                break;
        }
    };

    $scope.defaultClass = 'Pretty';
    $scope.defaultFormat = 'XML';
    $scope.getRenderClass = function (klass) {
        return klass == $scope.defaultClass ? 'active' : '';
    };
    $scope.getRenderFormat = function (format) {
        return format == $scope.defaultFormat ? 'active' : '';
    };

    $scope.toggleRenderClass = function (klass) {
        $scope.defaultClass = klass;
    };
    $scope.toggleRenderFormat = function (format) {
        $scope.defaultFormat = format;
    };

    $scope.diffResults = function (dataArray) {
        var m = _.map(dataArray, function (item) {
            return {
                appId: item.app,
                response: item.responseText
            };
        });

        var baseText = JSON.stringify(U.sortObjectFileds(m[0].response), null, "\t");
        var newText = JSON.stringify(U.sortObjectFileds(m[1].response), null, "\t");
        var baseVersion = m[0].appId;
        var newVersion = m[1].appId;
        var diffoutputdiv = document.getElementById('diffOutput');
        diffTwoSlbs(diffoutputdiv, baseText, newText, baseVersion, newVersion);

        $('#resultCompareDialog').modal('show');
    };

    function diffTwoSlbs(targetDiv, baseText, newText, baseVersion, newVersion) {
        var base = difflib.stringAsLines(baseText);
        var newtxt = difflib.stringAsLines(newText);
        var sm = new difflib.SequenceMatcher(base, newtxt);
        var opcodes = sm.get_opcodes();

        targetDiv.innerHTML = "";

        targetDiv.appendChild(diffview.buildView({
            baseTextLines: base,
            newTextLines: newtxt,
            opcodes: opcodes,
            baseTextName: baseVersion,
            newTextName: newVersion,
            viewType: 0
        }));
    }

    $scope.loadData = function () {
        var id = $scope.query.policyId;
        $scope.data.responseArray = [];
        var params = {
            policyId: id
        };
        var request = {
            method: 'GET',
            url: G.baseUrl + '/api/policy',
            params: params
        };

        var groupsRequest = {
            method: 'GET',
            url: G.baseUrl + '/api/groups?type=extended&groupType=all'
        };
        var vsesRequest = {
            method: 'GET',
            url: G.baseUrl + '/api/vses?type=extended'
        };
        var slbsRequest = {
            method: 'GET',
            url: G.baseUrl + '/api/slbs'
        };
        var appsRequest = {
            method: 'GET',
            url: G.baseUrl + '/api/apps'
        };

        var requestArray = [
            $http(request).success(
                function (response, code) {
                    if (code == 200) {
                        $scope.model.policy = response;
                    }
                }
            ),

            $http(groupsRequest).success(
                function (response, code) {
                    if (code == 200) {
                        $scope.model.groups = _.indexBy(response.groups, function (v) {
                            return v.id;
                        });
                    }
                }
            ),
            $http(vsesRequest).success(
                function (response, code) {
                    if (code == 200) {
                        $scope.model.vses = _.indexBy(response['virtual-servers'], function (v) {
                            return v.id;
                        });
                    }
                }
            ),
            $http(appsRequest).success(
                function (response, code) {
                    if (code == 200) {
                        $scope.model.apps = _.indexBy(response['apps'], function (v) {
                            return v['app-id'];
                        });
                    }
                }
            ),
            $http(slbsRequest).success(function (response, code) {
                // vs vip
                $scope.model.slbs = _.indexBy(response['slbs'], 'id');
            })
        ];

        $q.all(requestArray).then(
            function () {
                var groups = _.map($scope.model.policy.controls, function (w) {
                    return $scope.model.groups[w.group.id];
                });
                var vses = $scope.model.vses;

                $scope.model.controls = groups;

                url.vses = vses;
                url.groups = groups;
                var urls = url.getUrl();

                $scope.view.urls = urls;
                $scope.query.host = urls[0];

                // vses intranet vips
                $scope.model.vses = _.mapObject($scope.model.vses, function (v, k, item) {
                    v.vip = '';
                    // get an vip from slb ids' vip array
                    var slbId = v['slb-ids'][0];
                    var slbs = $scope.model.slbs;
                    var slb = slbs[slbId];
                    var vips = slb['vips'];
                    var vipObj = _.find(vips, function (s) {
                        return s.ip.indexOf('10.') == 0;
                    });
                    if (vipObj) {
                        v.vip = vipObj.ip;
                    } else {
                        v.vip = vips[0].ip;
                    }
                    return v;
                });
            }
        );
    };
    $scope.hashChanged = function (hashData) { $scope.resource = H.resource;
        var resource = $scope.resource;
        if (hashData.env) {
            $scope.env = hashData.env;
        }
        if (hashData.policyId) {
            $scope.query.policyId = hashData.policyId;
        } else {
            alert((angular.equals(resource, {}) ? '你还没有选择任何应用': resource.tools.test.js.msg1));
            return;
            /* $('#PolicyIdSelectorDialog').modal('show');*/
        }
        $scope.loadData();
    };
    H.addListener("toolsQueryUrlApp", $scope, $scope.hashChanged);
    var url = {
        groups: [],
        vses: {},
        getUrl: function () {
            var groups = this.groups;
            var vses = this.vses;

            // 根据group分析对应的hosts
            if (!groups || groups.length == 0) return undefined;

            var n = _.map(groups, function (group) {
                var gs = _.indexBy(group['group-virtual-servers'], function (item) {
                    return item['virtual-server'].id;
                });
                return _.keys(gs);
            });
            var q = [];
            $.each(n, function (j, array) {
                q = q.concat(array);
            });
            n = _.uniq(q);
            var r = _.map(n, function (p) {
                var vs = vses[p];
                var vsdomains = vs.domains;
                var ssl = vs.ssl;

                var hosts = _.map(vsdomains, function (q) {
                    return ssl ? ('https://' + q.name) : ('http://' + q.name);
                });

                return hosts;
            });

            var result = [];
            $.each(r, function (k, barray) {
                result = result.concat(barray);
            });
            return _.uniq(result);
        }
    };
    var postman = function (httpapi, requestData, type, format, app, groupId, renderCallBack, showLoadingCallBack) {
        this.api = httpapi;
        this.type = type;
        this.format = format;
        this.requestData = JSON.stringify(requestData);
        this.app = app;
        this.groupId = groupId;
        this.render = renderCallBack;
        this.loading = showLoadingCallBack;
    };

    postman.prototype.sendRequest = function () {
        var api = this.api;

        // Prepare
        var xhr = new XMLHttpRequest();
        xhr.open('POST', api, true);

        var render = this.render;
        var app = this.app;
        var groupId = this.groupId;
        xhr.onreadystatechange = function (event) {
            render(startTime, app, groupId, event.target);
        };

        // Body send
        var startTime = new Date().getTime();
        if (this.requestData) {
            xhr.send(this.requestData);
        }
        else xhr.send();
    };

});
angular.bootstrap(document.getElementById("query-area"), ['toolsQueryUrlApp']);