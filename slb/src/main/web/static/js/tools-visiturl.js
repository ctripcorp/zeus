var headerInfoApp = angular.module('headerInfoApp', ["angucomplete-alt"]);
headerInfoApp.controller('headerInfoController', function ($scope, $http) {
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

var toolsVisitUrlApp = angular.module('toolsVisitUrlApp', ["angucomplete-alt", "http-auth-interceptor"]);
toolsVisitUrlApp.controller('toolsVisitUrlController', function ($scope, $http, $q) {
    $scope.resource = H.resource;
    var resource = $scope.resource;

    $scope.query = {
        url: '',
        checkhost: 'false'
    };
    $scope.env = 'pro';
    // 2 minutes timeout setting
    var checkUrlTimeout = 1200;
    // Current URL is not an SLB url
    var nonSLBError = (angular.equals(resource, {}) ? "URL 探测成功，但没有任何应用接收该URL。 可能的原因：": resource.tools.visitUrl.js.msg1);
    // Current Url could not be resolved
    $scope.checkPassed = undefined;
    $scope.data = {};
    $scope.information = {};
    $scope.checkresult = {
        'status': undefined,
        'result': {
            group: {},
            app: {}
        }
    };


    $scope.check = function () {
        var resource = $scope.resource;

        $scope.checkresult = {};
        var host = $scope.host;

        var param = {
            url: $scope.query.url.trim(),
            timeout: checkUrlTimeout
        };

        if (host) {

            var curl = param.url;
            // replace host with user set host

            var reg = /(http|https)(:\/\/)((\w|\.)+)(\/.*)/ig;

            var m = reg.exec(curl);
            if (m && m.length > 3) {
                var p = m[1] + m[2] + host.trim();
                if (m[5]) p += m[5];

                param.url = curl.replace(reg, p);

                param.host = m[3];
            }
        }

        var request = {
            method: 'GET',
            url: G['fws'].urls.api + '/api/tools/visit',
            params: param
        };

        var groupId = '';
        var env = '';
        $('.check-result-area').showLoading();
        $http(request).success(function (response, rcode) {
            groupId = response.group;
            env = response.env;
            if (groupId < 0 || groupId == undefined || groupId == '') {
                $scope.checkPassed = false;
                var hostIp = response['host-ip'];
                if (hostIp == '' || hostIp == undefined) {
                    hostIp = (angular.equals(resource, {}) ? '无（Ping 无效）': resource.tools.visitUrl.js.msg2);
                    nonSLBError = (angular.equals(resource, {}) ? "<b>URL 探测成功，但没有任何应用接收该URL</b>. DNS解析该域名请求到VIP: <b><a target='_blank' href='": resource.tools.visitUrl.js.msg3) + G[G.env].urls.webinfo + "?Keyword=" + response['host-ip'] + "'> " + hostIp + (angular.equals(resource, {}) ? "</a></b>, 请判断域名是否真实存在": resource.tools.visitUrl.js.msg4);
                }
                else {
                    nonSLBError = (angular.equals(resource, {}) ? "<b>URL 探测成功，但没有任何应用接收该URL</b>. DNS解析该域名请求到VIP: <b><a target='_blank' href='": resource.tools.visitUrl.js.msg3) + G[G.env].urls.webinfo + "?Keyword=" + response['host-ip'] + "'> " + hostIp + (angular.equals(resource, {}) ? "</a></b>.</br> 请确认(是/否?)属于期望应用的VIP. <br />": resource.tools.visitUrl.js.msg6) +
                        "<ul class='visit-url-tool-error'>" +
                        (angular.equals(resource, {}) ? "<li>否,可能的原因：": resource.tools.visitUrl.js.msg7) +
                        "<ul>" +
                        (angular.equals(resource, {}) ? "<li>你尚未申请SLB 应用, 请通过<a href='http://www.ctrip.com/app/applyonce/' target='_blank'>cdng</a> 申请所需要的App。</li>": resource.tools.visitUrl.js.msg8) +
                        (angular.equals(resource, {}) ? "<li>已经申请应用，但尚未切换DNS到SLB设备。请联系OPS Team协助切换DNS。</li>": resource.tools.visitUrl.js.msg9) +
                        (angular.equals(resource, {}) ? "<li>URL 地址不正确</li>": resource.tools.visitUrl.js.msg10) +
                        "</ul>" +
                        (angular.equals(resource, {}) ? "<li>是：SLB 配置问题，请联系<a href='mailto:slb@test.com?subject=SLB产品咨询&body=请添加咨询问题列表:%E2%80%A8%E2%80%A8%E2%80%A8%E2%80%A8'>框架研发部SLB Team</a>提供支持</li>": resource.tools.visitUrl.js.msg11) +
                        "</ul>";
                }
                $('.error-message').html(nonSLBError);
            } else {
                $scope.checkPassed = true;
                $scope.getGroupInfo(groupId, env);
            }
            $('#check-result-area').hideLoading();
        });
    };
    $scope.generateGroupIdLink = function (group) {
        var env = 'uat';
        var id = '';

        if (group) {
            env = group.env || 'uat';
            if (env == 'prod_fraaws') {
                env = 'fra-aws';
            }

            id = group.id;
        }
        return "/portal/group#?env=" + env + "&groupId=" + id;
    };
    $scope.getGroupInfo = function (groupId, env) {
        var envUrl = G.baseUrl;
        env = env.toLowerCase();
        switch (env) {
            case 'uat':
                envUrl = G['uat'].urls.api;
                break;
            case 'fat':
                env = 'fws';
                envUrl = G['fws'].urls.api;
                break;
            case 'prod_private':
                env = 'pro';
                envUrl = G['pro'].urls.api;
                break;
            case 'prod_fraaws':
                env = 'fra-aws';
                envUrl = G['fra-aws'].urls.api;
                break;
            default :
                break;
        }
        $q.all(
            [
                $http.get(envUrl + "/api/group?type=EXTENDED&groupId=" + groupId).success(
                    function (res) {
                        // Group meta data
                        res.env = env;
                        $scope.information.extendedGroup = res;
                    }
                ),

                $http.get(envUrl + "/api/group?groupId=" + groupId).success(
                    function (res) {
                        $scope.information.offlineGroup = res;
                    }
                )
            ]
        ).then(
            function () {
                var appId = $scope.information.offlineGroup['app-id'];
                // Get the apps information
                $scope.getAppsMetaData(appId, envUrl, env);
            }
        );
    };
    $scope.getAppsMetaData = function (appId, envUrl, env) {
        var params = {
            "appId": appId
        };
        var request = {
            method: 'GET',
            url: envUrl + '/api/apps',
            params: params
        };

        $http(request).success(function (res) {
            var response = res["apps"];
            if (response != undefined) {
                var response = res["apps"][0];

                $scope.information.appdata['id'] = appId;
                $scope.information.appdata['appname'] = response["chinese-name"];
                $scope.information.appdata['applink'] = "/portal/app#?env=" + env + "&appId=" + appId;
                $scope.information.appdata['bu'] = response.sbu;
                $scope.information.appdata["owner"] = response.owner;
                $scope.information.appdata["email"] = response["owner-email"];
            }
            else {
                $scope.information.appdata['id'] = appId;
                $scope.information.appdata['appname'] = "Unknown";
                $scope.information.appdata['bu'] = "Unknown";
                $scope.information.appdata["owner"] = "Unknown";
                $scope.information.appdata["email"] = "Unknown";
            }
        });
    };
    $scope.getToolTipText = function () {
        var resource = $scope.resource;
        if ($scope.query.url != undefined) return (angular.equals(resource, {}) ? '检测结果如下': resource.tools.visitUrl.js.msg12);
        else return (angular.equals(resource, {}) ? ' 请输入你要检测的URL并点击右侧检测按钮': resource.tools.visitUrl.js.msg13);
    };

$scope.hashChanged = function (hashData) { $scope.resource = H.resource;
        $scope.checkPassed = undefined;
        $('.error-message').html('');
        $scope.information = {
            'offlineGroup': {},
            'extendedGroup': {},
            'appdata': {}
        };

        if (hashData.env) {
            $scope.env = hashData.env;
        }
        $scope.host = hashData.host;

        if (hashData.url) {
            var url = hashData.url.toLowerCase();
            if (!url.startsWith('http')) {
                url = 'http://' + url
            }
            $scope.query.url = url;

            $scope.check();
        } else {
            $scope.query.url = undefined;
            $scope.checkresult.status = undefined;
        }
    };
    H.addListener("toolsVisitUrlApp", $scope, $scope.hashChanged);
});
angular.bootstrap(document.getElementById("tools-area"), ['toolsVisitUrlApp']);

var toolsQueryUrlApp = angular.module('toolsQueryUrlApp', ["angucomplete-alt", "http-auth-interceptor"]);
toolsQueryUrlApp.controller('toolsQueryUrlController', function ($scope, $http, $q) {
    $scope.query = {
        url: '',
        checkhost: false,
        ishostable: false,
        env: 'pro'
    };
    $scope.data = {};

    var selectedIndex;
    var envs = [
        'pro',
        'uat',
        'fws',
        'fra-aws'
    ];

    $scope.toggleEnv = function (x) {
        var c = $scope.query.env;
        if (x != c) {
            $scope.query.host = '';
            $scope.query.env = x;
        }
    };
    $scope.isCurrentENV = function (x) {
        var i = $scope.query.env == x;
        return i ? 'btn-info' : 'btn-default';
    };

    $scope.applyHashData = function (hashData) {
        if (hashData.url) {
            $scope.query.url = hashData.url;
        }
        if (hashData.host) {
            $scope.query.host = hashData.host;
            $scope.query.checkhost = true;
        }
    };

    $scope.clearQuery = function () {
        $scope.query.url = '';
        var hashData = {};
        hashData.url = '';
        hashData.host = '';
        $scope.query.checkhost = false;
        hashData.timeStamp = new Date().getTime();
        H.setData(hashData);
    };
    $scope.executeQuery = function () {
        var host = $scope.query.host;

        var checkhost = $scope.query.checkhost;
        if (!checkhost) host = '';
        var hashData = {};

        var url =  $scope.query.url || "";
        if(url){
            hashData.url = encodeURIComponent(url);
        }
        hashData.timeStamp = new Date().getTime();
        if (host) {
            hashData.host = host;
        } else {
            hashData.host = '';
        }

        var review = reviewData($('#urlzone'));
        if (!review) return;
        else {
            H.setData(hashData);
        }
    };

    $scope.disableUrlCheck = function () {
        var url = $scope.query.url;
        var host = $scope.query.host;
        var checkhost = $scope.query.checkhost;

        var isHostOk = false;
        if (checkhost) {
            isHostOk = !host;
        }

        return !url || isHostOk;
    };
    $scope.showHostArea = function () {
        return $scope.query.checkhost;
    };
    $scope.editHostClick = function () {
        $('#hostzone').removeAttr('disabled');
    };
    $scope.isHostChecked = function () {
        var isChecked = $scope.query.host;
        return isChecked;
    };

    $scope.$watch($scope.query.checkhost, function (newVal) {

    });
    $scope.$watch('query.url', function (newVal) {
        // if the url is valid
        if (!newVal) {
            $scope.query.ishostable = false;

            return;
        }

        // if url start with http and https
        var url = newVal.toString().toLowerCase();

        if (!url.startsWith('http')) {
            url = 'http://' + url
        }

        // get the host of the url
        var reg = /(http|https)(:\/\/)((\w|\d|\.)+)(\/)*/ig;
        var result = reg.exec(url);

        var domain;
        if (result) domain = result[3];

        if (domain) {
            // search the domain related slb.
            // all envs slbs for this domain
            var slbs = {};
            var allSlbs = {};
            var envs = ['pro', 'uat', 'fws', 'fra-aws'];

            var promises = [];
            $.each(envs, function (i, e) {
                var request = $http({
                    method: 'GET',
                    url: G[e]['urls']['api'] + '/api/slbs?domain=' + domain
                }).success(function (response, code) {
                    if (code == 200 && response['slbs'] && response['slbs'].length > 0) {
                        slbs[e] = _.map(response['slbs'], function (v) {
                            return {
                                id: v.id,
                                name: v.name,
                                vip: _.pluck(v.vips, 'ip')
                            }
                        });
                    }
                });

                var request2 = $http({
                    method: 'GET',
                    url: G[e]['urls']['api'] + '/api/slbs'
                }).success(function (response, code) {
                    if (code == 200 && response['slbs'] && response['slbs'].length > 0) {
                        allSlbs[e] = _.map(response['slbs'], function (v) {
                            return {
                                id: v.id,
                                name: v.name,
                                vip: _.pluck(v.vips, 'ip')
                            }
                        });
                    }
                });
                promises.push(request);
                promises.push(request2);
            });

            $q.all(promises).then(function () {
                $scope.data.allSlbs = allSlbs;
                var es = _.keys(slbs);
                if (es.length > 0) {
                    $scope.data.envs = es;
                    $scope.query.env = es[0];
                    $scope.data.slbs = _.flatten(_.values(slbs));
                } else {
                    $scope.data.envs = envs;
                    $scope.query.env = envs[0];
                    $scope.data.slbs = allSlbs[envs[0]];
                }
                var host = $scope.query.host;
                if (host) {
                    $.each($scope.data.slbs, function (i, s) {
                        if (s.vip.indexOf(host) != -1) {
                            selectedIndex = i;
                        }
                    });
                }
            });
        }
    });
    $scope.$watch('query.env', function (newVal) {
        $scope.data.slbs = $scope.data.allSlbs[newVal];
    });

    $scope.clickTile = function (slb, index) {
        //tile-selected
        selectedIndex = index;

        var ips = slb.vip;

        var hosts = _.find(ips, function (v) {
            return v.indexOf('10.') == 0;
        });

        var host = hosts || ips[0];

        $scope.query.host = host;
    };
    $scope.selectedTileClass = function (slb, index) {
        return selectedIndex == index ? 'tile-selected2' : '';
    };
$scope.hashChanged = function (hashData) { $scope.resource = H.resource;
        if (hashData.env) {
            $scope.env = hashData.env;
        }
        $scope.applyHashData(hashData);
    };

    function reviewData(id) {
        var result = true;
        $.each(id.find('[data-validator-type="validation"]'), function (i, element) {
            if (!$(element).bootstrapValidation('validate'))
                result = false;
        });
        return result;
    };
    H.addListener("toolsQueryUrlApp", $scope, $scope.hashChanged);
});
angular.bootstrap(document.getElementById("query-area"), ['toolsQueryUrlApp']);


