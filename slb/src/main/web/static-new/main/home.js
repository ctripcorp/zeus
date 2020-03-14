//InfoLinksComponent: info links
var headerInfoApp = angular.module('headerInfoApp', ["angucomplete-alt"]);
headerInfoApp.controller('headerInfoController', function ($scope, $http) {
    $scope.query = {};
    $scope.generateLink = function (a) {
        var link = '';
        switch (a) {
            case 'basic': {

                link = "/portal/user#?env=" + G.env;
                break;
            }
            case 'home': {
                link = "/portal/user-home#?env=" + G.env;
                break;
            }
            case 'access': {
                link = "/portal/user/user-access#?env=" + G.env;
                break;
            }
            case 'log': {
                link = "/portal/user/log#?env=" + G.env;

                break;
            }
            case 'policy': {
                link = "/portal/user/user-trafficpolicy#?env=" + G.env;
                break;
            }
            case 'AB': {
                link = "/portal/user/user-normalpolicy#?env=" + G.env;
                break;
            }
            case 'drs': {
                link = "/portal/user/user-drs#?env=" + G.env;
                break;
            }
            case 'unhealthy': {
                link = "/portal/user/user-unhealthy#?env=" + G.env;
                break;
            }
            default:
                break;
        }
        link += '&userId=' + $scope.query.userId;
        return link;
    };
    $scope.loadData = function () {
        if ($scope.query.userId) return;
        var url = '/api/auth/current/user';
        var request = {
            method: 'GET',
            url: url
        };
        $http(request).success(
            function (res) {
                $scope.query.userId = res.name;
            }
        );
    };
    $scope.hashChanged = function (hashData) {
        $scope.resource = H.resource;
        if (hashData.env) {
            $scope.env = hashData.env;
        }
        if (hashData.userId) {
            $scope.query.userId = hashData.userId;
        } else {
            $scope.loadData();
        }
    };
    H.addListener("infoLinksApp", $scope, $scope.hashChanged);
});
angular.bootstrap(document.getElementById("header-area"), ['headerInfoApp']);


var selfInfoApp = angular.module('selfInfoApp', ["angucomplete-alt", "http-auth-interceptor", 'ngSanitize']);
selfInfoApp.controller('selfInfoController', function ($scope, $http, $q) {
    $scope.resource = H.resource;
    $scope.data={};
    $scope.query={};

    var resource = $scope.resource;

    $scope.initData = function (resource) {
        $scope.key = angular.equals(resource, {}) ? '全部': resource.userHome.js.home.msg1;
        $scope.query = {};
        $scope.data = {
            appGroups: {
                error: {}
            },
            homegroups: {},
            filters: [
                {
                    key: angular.equals(resource, {}) ? '全部': resource.userHome.js.home.msg1,
                    keyword: 'all'
                },
                {
                    key: angular.equals(resource, {}) ? '服务器操作': resource.userHome.js.home.msg2,
                    keyword: 'member'
                },
                {
                    key: angular.equals(resource, {}) ? '健康检测': resource.userHome.js.home.msg3,
                    keyword: 'health'
                },
                {
                    key: angular.equals(resource, {}) ? '流量策略': resource.userHome.js.home.msg4,
                    keyword: 'traffic'
                },
                {
                    key: angular.equals(resource, {}) ? '工具': resource.userHome.js.home.msg5,
                    keyword: 'tool'
                },
                {
                    key: angular.equals(resource, {}) ? '其它': resource.userHome.js.home.msg6,
                    keyword: 'other'
                }
            ],
            categories: ''
        };

        $scope.categories = [
            {
                type: 'member',
                name: angular.equals(resource, {}) ? '服务器拉入、拉出': resource.userHome.js.home.msg7,
                keyword: 'member,all'
            }, {
                type: 'memberop',
                name: angular.equals(resource, {}) ? '服务器拉入、拉出日志': resource.userHome.js.home.msg8,
                keyword: 'member,all'
            },

            {
                type: 'healthchange',
                name: angular.equals(resource, {}) ? '修改健康检测URL': resource.userHome.js.home.msg9,
                keyword: 'health,all'
            }, {
                type: 'healthyop',
                name: angular.equals(resource, {}) ? '健康检测拉入、拉出日志': resource.userHome.js.home.msg21,
                keyword: 'health,all'
            },
            {
                type: 'apprewrite',
                name: angular.equals(resource, {}) ? 'Java应用URL转小写': resource.userHome.js.home.msg24,
                keyword: 'traffic,all'
            },
            {
                type: 'netjava',
                name: angular.equals(resource, {}) ? 'NET转JAVA流量灰度、拉出日志': resource.userHome.js.home.msg233,
                keyword: 'traffic,all'
            }, {
                type: 'normaltraffic',
                name: angular.equals(resource, {}) ? 'angular.equals(resource, {}) ? 应用迁移: resource.navs.navs_app_policy流量灰度': resource.userHome.js.home.msg22,
                keyword: 'traffic,all'
            }, {
                type: 'endtraffic',
                name: angular.equals(resource, {}) ? '结束流量灰度': resource.userHome.js.home.msg236,
                keyword: 'traffic,all'
            }, {
                type: 'dr',
                name: angular.equals(resource, {}) ? '配置angular.equals(resource, {}) ? 应用DR策略: resource.navs.navs_dr': resource.userHome.js.home.msg25,
                keyword: 'traffic,all'
            }, {
                type: 'urlcheck',
                name: angular.equals(resource, {}) ? 'URL探测(查询URL被哪个App处理)': resource.userHome.js.home.msg237,
                keyword: 'tool,all'
            }, {
                type: 'errorpage',
                name: angular.equals(resource, {}) ? '外网错误页配置': resource.userHome.js.home.msg238,
                keyword: 'tool,all'
            }, {
                type: 'healthchecker',
                name: angular.equals(resource, {}) ? '应用健康检查(验证健康检测URL可访问性)': resource.userHome.js.home.msg239,
                keyword: 'health,all'
            }, {
                type: 'grouptraffic',
                name: angular.equals(resource, {}) ? '应用流量监控': resource.userHome.js.home.msg13,
                keyword: 'traffic,all'
            }, {
                type: 'questionModal',
                name: angular.equals(resource, {}) ? '问题反馈': resource.userHome.js.home.msg23,
                keyword: 'other,all'
            }
        ];

        $scope.data.categories = $scope.categories;
    }

    $scope.initCategories = function(resource) {
        $scope.key = angular.equals(resource, {}) ? '全部': resource.userHome.js.home.msg1;
        $scope.data.filters = [
            {
                key: angular.equals(resource, {}) ? '全部': resource.userHome.js.home.msg1,
                keyword: 'all'
            },
            {
                key: angular.equals(resource, {}) ? '服务器操作': resource.userHome.js.home.msg2,
                keyword: 'member'
            },
            {
                key: angular.equals(resource, {}) ? '健康检测': resource.userHome.js.home.msg3,
                keyword: 'health'
            },
            {
                key: angular.equals(resource, {}) ? '流量策略': resource.userHome.js.home.msg4,
                keyword: 'traffic'
            },
            {
                key: angular.equals(resource, {}) ? '工具': resource.userHome.js.home.msg5,
                keyword: 'tool'
            },
            {
                key: angular.equals(resource, {}) ? '其它': resource.userHome.js.home.msg6,
                keyword: 'other'
            }
        ];


        $scope.categories = [
            {
                type: 'member',
                name: angular.equals(resource, {}) ? '服务器拉入、拉出': resource.userHome.js.home.msg7,
                keyword: 'member,all'
            }, {
                type: 'memberop',
                name: angular.equals(resource, {}) ? '服务器拉入、拉出日志': resource.userHome.js.home.msg8,
                keyword: 'member,all'
            },

            {
                type: 'healthchange',
                name: angular.equals(resource, {}) ? '修改健康检测URL': resource.userHome.js.home.msg9,
                keyword: 'health,all'
            }, {
                type: 'healthyop',
                name: angular.equals(resource, {}) ? '健康检测拉入、拉出日志': resource.userHome.js.home.msg21,
                keyword: 'health,all'
            },
            {
                type: 'apprewrite',
                name: angular.equals(resource, {}) ? 'Java应用URL转小写': resource.userHome.js.home.msg24,
                keyword: 'traffic,all'
            },
            {
                type: 'netjava',
                name: angular.equals(resource, {}) ? 'NET转JAVA流量灰度、拉出日志': resource.userHome.js.home.msg233,
                keyword: 'traffic,all'
            }, {
                type: 'normaltraffic',
                name: angular.equals(resource, {}) ? 'angular.equals(resource, {}) ? 应用迁移: resource.navs.navs_app_policy流量灰度': resource.userHome.js.home.msg22,
                keyword: 'traffic,all'
            }, {
                type: 'endtraffic',
                name: angular.equals(resource, {}) ? '结束流量灰度': resource.userHome.js.home.msg236,
                keyword: 'traffic,all'
            }, {
                type: 'dr',
                name: angular.equals(resource, {}) ? '配置angular.equals(resource, {}) ? 应用DR策略: resource.navs.navs_dr': resource.userHome.js.home.msg25,
                keyword: 'traffic,all'
            }, {
                type: 'urlcheck',
                name: angular.equals(resource, {}) ? 'URL探测(查询URL被哪个App处理)': resource.userHome.js.home.msg237,
                keyword: 'tool,all'
            }, {
                type: 'errorpage',
                name: angular.equals(resource, {}) ? '外网错误页配置': resource.userHome.js.home.msg238,
                keyword: 'tool,all'
            }, {
                type: 'healthchecker',
                name: angular.equals(resource, {}) ? '应用健康检查(验证健康检测URL可访问性)': resource.userHome.js.home.msg239,
                keyword: 'health,all'
            }, {
                type: 'grouptraffic',
                name: angular.equals(resource, {}) ? '应用流量监控': resource.userHome.js.home.msg13,
                keyword: 'traffic,all'
            }, {
                type: 'questionModal',
                name: angular.equals(resource, {}) ? '问题反馈': resource.userHome.js.home.msg23,
                keyword: 'other,all'
            }
        ];

        $scope.data.categories = $scope.categories;
    }

    // filter
    $scope.toggleFilter = function (filter) {
        $scope.key = filter.key;

        var word = filter.keyword;

        // filter those with same keyword
        $scope.data.categories = _.filter($scope.categories, function (v) {
            return v.keyword.indexOf(word) != -1;
        });
    };

    $scope.isSelectedTool = function (t) {
        var isSelected = $scope.key == t.key;
        return isSelected ? 'selected-tool' : '';
    };

    $scope.cacheRequestFn = function (str) {
        return { q: str, timestamp: new Date().getTime() };
    };

    $scope.remoteAppsUrl = function () {
        return G.baseUrl + "/api/meta/apps";
    };

    $scope.selectAppId = function (o) {
        if (o) {
            var appId = o.originalObject.id;
            $scope.homeAppId = appId;
            $scope.query.appId = appId;

            // groups of current appid
            var promise = groupsService.getGroupsByAppId(appId, 'extended');
            promise.then(function (data) {
                if (data && _.keys(data).length > 0) {
                    $scope.data.homegroups = _.values(data);
                    $('#homeGroupsDialog').modal('show');
                    H.setData({ appId: appId, timeStamp: new Date().getTime() });
                }
            });
        }
    };

    $scope.selectMemberAppId = function (o) {
        if (o) {
            var appId = o.originalObject.id;
            $scope.appid = appId;
            $scope.searchAppGroup(appId);
        }
    };
    $scope.selectRewriteAppId = function (o) {
        if (o) {
            var appId = o.originalObject.id;
            $scope.appid = appId;
            window.open('/portal/app#?env=' + $scope.env + '&appId=' + appId + '&rewrite=true', '_blank');
        }
    };

    $scope.popDialog = function (v) {
        if (v == 'urlcheck') {
            window.open('/portal/tools/visiturl#?env=' + $scope.env, '_blank');
            return;
        }

        $scope.appid = '';
        $scope.data.appGroups = {
            error: {}
        };
        $('#appIdMemberSelector_value').val('');
        $('#appIdRewriteSelector_value').val('');
        $('#appIdHealthySelector_value').val('');
        $('#appIdHealthChangeSelector_value').val('');
        $('#appIdNetJavaSelector_value').val('');
        $('#appIdNormalSelector_value').val('');
        $('#appIdEndSelector_value').val('');
        $('#appIdDrSelector_value').val('');
        $('#appIdGroupTrafficSelector_value').val('');
        $('#appIdMemberOpSelector_value').val('');
        $('#appIdHealthyOpSelector_value').val('');
        $scope.query = {};
        $('#' + v).modal('show');
    };

    var docs = {
        'rewrite': '',
        'group': '',
        'activate-group': '',
        'url-rewrite': '',
        'domain-remove': '',
        'health-modify': '',
        'health-check': '',
        'member': '',
        'url-check': '',
        'netjava': '',
        'normaltraffic': '',
        'endtraffic': '',
        'dr': '',
        'home': '',
        '499': '',
        '504': '',
        '413': '',
        'nohttpreponse': '',
        'isssl': '',
        'httptiaohttps': '',
        'error-page': ''
    };

    $scope.startNetJavaPolicy = function (appId) {
        window.open('/portal/user/user-trafficpolicy#?env=' + $scope.env + '&appId=' + appId, '_blank');
    };

    $scope.startEndPolicy = function (appId) {
        window.open('/portal/user/user-normalpolicy#?env=' + $scope.env + '&appId=' + appId + '&type=deactivate', '_blank');
    };
    $scope.startDr = function (appId) {
        window.open('/portal/user/user-edit-dr#?env=' + $scope.env + '&appId=' + appId, '_blank');
    };

    $scope.startNormalPolicy = function (appId) {
        window.open('/portal/user/user-normalpolicy#?env=' + $scope.env + '&appId=' + appId, '_blank');
    };

    $scope.getDocumentUrl = function (type) {
        return docs[type];
    };

    $scope.documentLink = function ($event, type) {
        var url = docs[type];
        window.open(url, '_blank');
        $event.stopPropagation();
    };


    $scope.searchAppGroup = function (app) {
        $scope.data.appGroups['error'][app] = '';

        var promise = groupsService.getGroupsByAppId(app, 'extended');
        var promise2 = appService.getAppsByAppId(app);

        $q.all([promise2, promise]).then(function (data) {
            var apps = data[0];
            if (data[1] && _.keys(data[1]).length > 0) {
                $scope.data.appGroups[app] = _.indexBy(_.map(data[1], function (v) {
                    v['app-id'] = apps[v['app-id']];
                    return v;
                }), 'id');
            } else {
                $scope.data.appGroups['error'][app] = angular.equals(resource, {}) ? '找不到 APP:': resource.userHome.js.home.msg33 + app + angular.equals(resource, {}) ? '对用的SLB Groups. 请确认APPID存在，并且已经通过cndg申请了应用的访问入口！！': resource.userHome.js.home.msg43
            }

        });
    };

    $scope.getAppName = function (groups) {
        var values = _.values(groups);

        if (groups && values.length > 0) {
            var group = values[0];
            return group['app-id']['chinese-name'];
        }
    };

    $scope.getUserEmail = function (groups) {
        var values = _.values(groups);

        if (groups && values.length > 0) {
            var group = values[0];
            var owners = group['app-id']['owner'];

            return owners ? owners['email'] : '';
        }
    };

    $scope.getAppOwners = function (groups) {
        var values = _.values(groups);

        if (groups && values.length > 0) {
            var group = values[0];
            var owners = group['app-id']['owner'];

            return owners ? owners['chinese-name'] : '';
        }
    };

    $scope.getAppBu = function (groups) {
        var values = _.values(groups);

        if (groups && values.length > 0) {
            var group = values[0];
            var sbu = group['app-id']['sbu'];

            return sbu;
        }
    };


    $scope.getGroupClass = function (properties) {
        var state = '';

        if (properties) {
            state = properties['status'];
        }

        switch (state) {
            case "activated":
                return "status-green";
            case "deactivated":
                return "status-red";
            case "tobeactivated":
                return "status-yellow";
            default:
                return "status-gray";
                break;
        }
    };

    $scope.getGroupStatusText = function (properties) {
        var val = properties ? properties['status'] : '';

        switch (val) {
            case 'activated':
                return 'angular.equals(resource, {}) ? 已激活: resource.summaryInfoApp.html.token1';
            case 'toBeActivated':
                return 'angular.equals(resource, {}) ? 有变更: resource.summaryInfoApp.html.token2';
            case 'deactivated':
                return 'angular.equals(resource, {}) ? 未激活: resource.summaryInfoApp.html.token3';
            default:
                return val;
        }
    };

    $scope.getGroupIDCText = function (properties) {
        var val = properties ? properties['idc'] : angular.equals(resource, {}) ? '未知': resource.userHome.js.home.msg53;

        return val;
    };

    $scope.getGroupBu = function (properties) {
        var val = properties ? properties['sbu'] : angular.equals(resource, {}) ? '未知': resource.userHome.js.home.msg53;

        return val;
    };

    $scope.chooseGroup = function (v) {
        $scope.query.groupId = v;
    };

    $scope.isSelectedGroup = function (v) {
        var selected = v == $scope.query.groupId;

        if (selected && v) return 'tile-selected';
    };

    $scope.applyHashData = function (hashData) {
        $scope.env = 'pro';

        if (hashData.env) {
            $scope.env = hashData.env;
        }

        if (hashData.appId) {
            $scope.query.appId = hashData.appId;
            $scope.homeAppId = hashData.appId;
            $('#appIdSelector_value').val(hashData.appId);
        }
    };

    $scope.getAppData = function () {
        if(!$scope.data) return;
        var app = $scope.data.app;
        if (!app) return '';

        var id = app['app-id'];
        var chineseName = app['chinese-name'];

        var owner = app['owner'] ? app['owner']['chinese-name'] : '';
        var mail = app['owner'] ? app['owner']['email'] : '';

        var sbu = app['sbu'];


        return id + '(' + chineseName + '), ' + 'Owner:' + owner + ',' + ' SBU:' + sbu;
    };

    $scope.loadData = function () {
        var appId = $scope.query.appId;
        if (!appId) return;

        var promise = appService.getAppsByAppId(appId);

        promise.then(function (data) {
            var apps = _.values(data);
            if (apps && apps.length > 0) $scope.data.app = apps[0];
        });
    };

    var groupsService;
    var appService;

    $scope.hashChanged = function (hashData) {
        $scope.resource = H.resource;
        $scope.key = angular.equals(resource, {}) ? '全部': resource.userHome.js.home.msg1;
        $scope.initCategories(H.resource);
        $scope.applyHashData(hashData);
        groupsService = GroupsService.create($http, $q);
        appService = AppService.create($http, $q);
        $scope.loadData();
    };

    H.addListener("selfInfoApp", $scope, $scope.hashChanged);

    function exceptionNotify(title, message, url) {
        var notify = $.notify({
            icon: 'fa fa-exclamation-triangle',
            title: title,
            message: message,
            url: url,
            target: '_self'
        }, {
            type: 'danger',
            allow_dismiss: true,
            newest_on_top: true,
            placement: {
                from: 'top',
                align: 'center'
            },
            offset: {
                x: 0,
                y: 0
            },
            animate: {
                enter: 'animated fadeInDown',
                exit: 'animated fadeOutUp'
            },
            delay: 60000 * 24,
            spacing: 5,
            z_index: 1031,
            mouse_over: 'pause'
        });
    }
});
angular.bootstrap(document.getElementById("self-info-area"), ['selfInfoApp']);
