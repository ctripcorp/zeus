//InfoLinksComponent: info links
var headerInfoApp = angular.module('headerInfoApp', ["angucomplete-alt"]);
headerInfoApp.controller('headerInfoController', function ($scope, $http) {
    $scope.query = {};
    $scope.generateLink = function (a) {
        var link = '';
        switch (a) {
            case 'basic': {
                link = "/portal/slb#?env=" + G.env + "&slbId=" + $scope.query.slbId;
                break;
            }
            case 'rule': {
                link = "/portal/slb/rule#?env=" + G.env + "&slbId=" + $scope.query.slbId;
                break;
            }
            case 'log': {
                link = "/portal/slb/log#?env=" + G.env + "&slbId=" + $scope.query.slbId;
                break;
            }
            case 'traffic': {
                link = "/portal/slb/traffic#?env=" + G.env + "&slbId=" + $scope.query.slbId;
                break;
            }
            case 'conf': {
                link = "/portal/slb/conf#?env=" + G.env + "&slbId=" + $scope.query.slbId;
                break;
            }
            case 'intercept': {
                link = "/portal/slb/intercept#?env=" + G.env + "&slbId=" + $scope.query.slbId;
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
        if (hashData.slbId) {
            $scope.query.slbId = hashData.slbId;
        }
    };
    H.addListener("infoLinksApp", $scope, $scope.hashChanged);
});
angular.bootstrap(document.getElementById("header-area"), ['headerInfoApp']);

var summaryInfoApp = angular.module('summaryInfoApp', ['http-auth-interceptor', 'angucomplete-alt']);
summaryInfoApp.controller('summaryController', function ($scope, $http, $q) {
    $scope.query = {};
    // Auto complete
    $scope.cacheRequestFn = function (str) {
        return {q: str, timestamp: new Date().getTime()};
    };
    $scope.remoteUrl = function () {
        return G.baseUrl + "/api/meta/slbs";
    };
    $scope.selectTarget = function (t) {
        if (t) {
            var toId = t.originalObject.id;
            if ($scope.target.id != toId) {
                $scope.$broadcast('angucomplete-alt:clearInput', 'targetSelector');
                var pairs = {};
                pairs['slbId'] = toId;
                $scope.target.id = toId;
                H.setData(pairs);
                messageNotify("切换SLB. ", "成功切换至SLB： " + toId, null);
            }
        }
    };
    $scope.clickTarget = function () {
        $('#targetSelector_value').css('width', '250px');
    };
    $scope.setInputsDisplay = function () {
        $('#targetSelector_value').val($scope.query.slbId);
    };
    $scope.getAllTargets = function () {
        $http.get(G.baseUrl + "/api/slb?slbId=" + $scope.query.slbId + "&type=info").success(
            function (res) {
                $scope.target = {};
                $scope.target.name = $scope.query.slbId + "/" + res.name;
                $('#targetSelector_value').val($scope.target.name);
            }
        );
    };
    $scope.hashChanged = function (hashData) { $scope.resource = H.resource;
        if (hashData.env) {
            $scope.env = hashData.env;
        }
        if (hashData.slbId) {
            $scope.query.slbId = hashData.slbId;
            $scope.getAllTargets();
        }
    };
    H.addListener("summaryInfoApp", $scope, $scope.hashChanged);

    function messageNotify(title, message, url) {
        var notify = $.notify({
            icon: '',
            title: title,
            message: message,
            url: url,
            target: '_self'
        }, {
            type: 'success',
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
            delay: 1000,
            spacing: 5,
            z_index: 1031,
            mouse_over: 'pause'
        });
    }
});
angular.bootstrap(document.getElementById("summary-area"), ['summaryInfoApp']);


//TrafficComponent: Traffic
var trafficApp = angular.module('trafficApp', ['ui.bootstrap', 'ui.bootstrap.datetimepicker', 'http-auth-interceptor']);
trafficApp.controller('trafficController', function ($scope, $http, $q) {
    echarts.registerTheme('macarons');

    $scope.paramName = "slbId";
    $scope.target = "slb";
    $scope.targetType = "SLB";
    $scope.targetLink = '/portal/slb';
    $scope.targetInfo = '';

    $scope.queryDate = {
        startTime: '',
        endTime: ''
    };
    $scope.queryId = '';
    $scope.startTime = new Date();
    $scope.endTime = new Date();


    $scope.trafficTypes = {
        currentIndex: 0,
        current: '请求数',
        links: ['请求数', '5xx请求', '500请求', '502请求', '504请求', '4xx请求', '404请求', '非200请求', '慢请求', '被拦截请求']
    };
    $scope.chartsContainer = {
        container: null,
        longContainer: null,
        chartContainerWidth: null
    };


    $scope.getDisplay = function (x) {
        var resource = $scope.resource;
        if (resource) {
            return resource['traffic']['metricsmap'][x];
        }
    };
    $('#time-zone input').datepicker({
        timepicker: true,
        autoclose: false
    });

    //Area 1
    $scope.isCurrentTrafficPage = function (link) {
        return $scope.trafficTypes.current == link ? 'current' : '';
    };
    $scope.generateTargetLink = function () {
        return $scope.targetLink + "#?env=" + G.env + "&" + $scope.paramName + "=" + $scope[$scope.paramName];
    };
    $scope.getTargetInfo = function () {
        $http.get(G.baseUrl + "/api/" + $scope.target + "?" + $scope.paramName + "=" + $scope[$scope.paramName]).success(
            function (res) {
                $scope.targetInfo = " " + res.id + " / " + res.name + "";
            }
        );
    };

    //Area 2: set startDate
    $scope.refreshCharts = function () {
        $scope.loadAllCharts();
    };
    $scope.getDateNow = function () {
        var d = new Date();
        if (d.getMinutes() < 20) {
            d = d.setTime(d.getTime() - 1000 * 60 * 60);
        }
        return encodeURIComponent($.format.date(d, 'yyyy-MM-dd HH:00'));
    };

    $scope.setDateNow = function () {
        var d = new Date();
        d = d.setTime(d.getTime() - 1000 * 60 * 60);
        H.setData({startTime: encodeURIComponent($.format.date(d, 'yyyy-MM-dd HH:mm:00'))});
    };
    $scope.setDate = function () {
        H.setData({startTime: encodeURIComponent($.format.date(new Date($scope.queryDate.startTime).getTime(), 'yyyy-MM-dd HH:mm:00'))});
    };
    $scope.setDateNextHour = function () {
        H.setData({startTime: encodeURIComponent($.format.date(new Date($scope.queryDate.startTime).getTime() + 60 * 1000 * 60, 'yyyy-MM-dd HH:mm:00'))});
    };
    $scope.setDatePreviousHour = function () {
        H.setData({startTime: encodeURIComponent($.format.date(new Date($scope.queryDate.startTime).getTime() - 60 * 1000 * 60, 'yyyy-MM-dd HH:mm:00'))});
    };
    $scope.getTrafficType = function (t) {
        return t;
    };
    $scope.setTrafficType = function (t) {
        var time = $.format.date(new Date($scope.queryDate.startTime), 'yyyy-MM-dd HH:mm:00');
        var startTime = encodeURIComponent(time);
        H.setData({startTime: startTime, trafficType: t});
    };

    //Area 3: init chartsContainer
    $scope.buildChartsContainer = function () {
        if ($scope.chartsContainer.container) return;
        var c = $('.charts-container');
        var lc = $('.charts-long-container');
        var chartContainerWidth = c.width();
        var lcWidth = chartContainerWidth * $scope.trafficTypes.links.length;
        lc.width(lcWidth);
        $('.chart-container').each(function () {
            $(this).width(chartContainerWidth);
        });

        $scope.chartsContainer.container = c;
        $scope.chartsContainer.longContainer = lc;
        $scope.chartsContainer.chartContainerWidth = chartContainerWidth;
    };
    $scope.scrollChartsContainer = function () {
        $scope.chartsContainer.container.scrollLeft($scope.chartsContainer.chartContainerWidth * $scope.trafficTypes.currentIndex);
    };

    //Area 4: Load Page Data
    $scope.init = function () {
        $scope.getTargetInfo();
        if ($scope.startTime.getTime() != new Date($scope.queryDate.startTime).getTime() || $scope.queryId != $scope[$scope.paramName]) {
            $scope.loadAllCharts();
        }
        $scope.scrollChartsContainer();
    };

    var resource;

    $scope.loadAllCharts = function () {
        $scope.startTime = new Date($scope.queryDate.startTime);
        $scope.endTime = new Date($scope.startTime.getTime() + 1000 * 60 * 90);
        $scope.queryId = $scope[$scope.paramName];

        var groupTimeout = 0;
        $scope.loadTrafficCharts(0);

        setTimeout(function () {
            $scope.load5xxCharts(1);
        }, groupTimeout);

        setTimeout(function () {
            $scope.loadNone200Charts(7);
        }, groupTimeout);

        setTimeout(function () {
            $scope.load404Charts(6);
        }, groupTimeout);
        setTimeout(function () {
            $scope.load4xxCharts(5);
        }, groupTimeout);
        setTimeout(function () {
            $scope.load504Charts(4);
        }, groupTimeout);
        setTimeout(function () {
            $scope.load502Charts(3);
        }, groupTimeout);
        setTimeout(function () {
            $scope.load500Charts(2);
        }, groupTimeout);

        setTimeout(function () {
            $scope.loadSlowCharts(8);
        }, groupTimeout);

        setTimeout(function () {
            $scope.loadInterceptCharts(9);
        }, groupTimeout);
    };
    $scope.buildChart = function (chartContainer, title, config, needMark, groupByCount, groupByLinkFun) {
        var chartDom = $('<div class="col-md-6">  <div class="portlet">' +
            '  <div class="panel-collapse collapse in">' +
            ' <div class="portlet-body traffic-chart"></div>' +
            '</div>' +
            '</div></div>');
        chartContainer.append(chartDom);
        var chart = newChart(chartDom.get(0).getElementsByClassName('portlet-body')[0]);
        config.withCredentials = false;
        return $http(config).success(
            function (res) {
                loadChartData(chart, res, title, "", new Date($scope.startTime).getTime(), 60000, needMark, groupByCount, groupByLinkFun);
            }
        );
    };
    $scope.groupTrafficLinkFun = function (id) {
        return '/portal/group/traffic' + H.generateHashStr({
            env: G.env,
            groupId: id,
            startTime: H.getParam('startTime'),
            trafficType: H.getParam('trafficType')
        });
    };
    $scope.slbServerCatLinkFun = function (ip) {
        return G[G.env].urls.cat + '/cat/r/h?domain=100000716&ip=' + ip;
    };
    var chartTimeout = 0;
    $scope.loadTrafficCharts = function (index) {
        var chartContainer = $('#chart-' + $scope.trafficTypes.links[index]);
        chartContainer.empty();

        var params = {
            'metric-name': 'slb.req.count',
            'start-time': $.format.date($scope.startTime, 'yyyy-MM-dd HH:mm'),
            'end-time': $.format.date($scope.endTime, 'yyyy-MM-dd HH:mm'),
            'tags': '{"slb_id":["' + $scope.queryId + '"]}',
            'interval': '1m',
            'chart': 'line',
            'aggregator': 'sum',
            'downsampler': 'sum'
        };
        var config = {
            'url': G[G.env].urls.dashboard + "/data",
            'method': 'GET',
            'params': params
        };

        var v1 = $scope.buildChart(chartContainer, resource["traffic"]["slbtitlemap"]["总流量统计"], config, true, -1);

        params = _.clone(params);
        config = _.clone(config);
        params['group-by'] = '[hostip]';
        config.params = params;
        var v2 = $scope.buildChart(chartContainer, resource["traffic"]["slbtitlemap"]["流量分SLB服务器统计"], config, false, -1, $scope.slbServerCatLinkFun);

        params = _.clone(params);
        config = _.clone(config);
        params['group-by'] = '[status]';
        config.params = params;
        var v3 = $scope.buildChart(chartContainer, resource["traffic"]["slbtitlemap"]["状态码分布"], config, false, -1);

        params = _.clone(params);
        config = _.clone(config);
        params['group-by'] = '[cost]';
        config.params = params;
        var v4 = $scope.buildChart(chartContainer, resource["traffic"]["slbtitlemap"]["请求耗时分布"], config, false, -1);

        params = _.clone(params);
        config = _.clone(config);
        params['group-by'] = '[group_id]';
        config.params = params;
        var v5 = $scope.buildChart(chartContainer, resource["traffic"]["slbtitlemap"]["流量 Top10 Group"], config, false, 10, $scope.groupTrafficLinkFun);

        $q.all([v1, v2]).then(function () {
            $q.all([v3, v4]).then(function () {
                $q.all([v4, v5]).then(function () {
                    console.log("done");
                })
            })
        })
    };
    $scope.load5xxCharts = function (index) {
        var chartContainer = $('#chart-' + $scope.trafficTypes.links[index]);
        chartContainer.empty();

        var params = {
            'metric-name': 'slb.req.count',
            'start-time': $.format.date($scope.startTime, 'yyyy-MM-dd HH:mm'),
            'end-time': $.format.date($scope.endTime, 'yyyy-MM-dd HH:mm'),
            'tags': '{"slb_id":["' + $scope.queryId + '"],"status":["5*"]}',
            'interval': '1m',
            'chart': 'line',
            'aggregator': 'sum',
            'downsampler': 'sum'
        };
        var config = {
            'url': G[G.env].urls.dashboard + "/data",
            'method': 'GET',
            'params': params
        };

        setTimeout(function () {
            $scope.buildChart(chartContainer, resource["traffic"]["slbtitlemap"]["5xx错误请求"], config, true, -1);

            params = _.clone(params);
            config = _.clone(config);
            params['group-by'] = '[status]';
            config.params = params;
            $scope.buildChart(chartContainer, resource["traffic"]["slbtitlemap"]["5xx错误请求状态码分布统计"], config, false, -1, $scope.slbServerCatLinkFun);

        }, chartTimeout);


        setTimeout(function () {
            params = _.clone(params);
            config = _.clone(config);
            params['group-by'] = '[hostip]';
            config.params = params;
            $scope.buildChart(chartContainer, resource["traffic"]["slbtitlemap"]["5xx错误请求分SLB服务器统计"], config, false, -1, $scope.slbServerCatLinkFun);

            params = _.clone(params);
            config = _.clone(config);
            params['group-by'] = '[group_id]';
            config.params = params;
            $scope.buildChart(chartContainer, resource["traffic"]["slbtitlemap"]["5xx错误请求 Top10 Group"], config, false, 10, $scope.groupTrafficLinkFun);

        }, chartTimeout);


        setTimeout(function () {
            params = _.clone(params);
            config = _.clone(config);
            params['group-by'] = '[domain,request_uri]';
            config.params = params;
            $scope.buildChart(chartContainer, resource["traffic"]["slbtitlemap"]["5xx错误请求 Top10 Url"], config, false, 10);
        }, chartTimeout);
    };
    $scope.load500Charts = function (index) {
        var chartContainer = $('#chart-' + $scope.trafficTypes.links[index]);
        chartContainer.empty();

        var params = {
            'metric-name': 'slb.req.count',
            'start-time': $.format.date($scope.startTime, 'yyyy-MM-dd HH:mm'),
            'end-time': $.format.date($scope.endTime, 'yyyy-MM-dd HH:mm'),
            'tags': '{"slb_id":["' + $scope.queryId + '"],"status":["500"]}',
            'interval': '1m',
            'chart': 'line',
            'aggregator': 'sum',
            'downsampler': 'sum'
        };
        var config = {
            'url': G[G.env].urls.dashboard + "/data",
            'method': 'GET',
            'params': params
        };

        setTimeout(function () {
            $scope.buildChart(chartContainer, resource["traffic"]["slbtitlemap"]["500错误请求"], config, true, -1);

            params = _.clone(params);
            config = _.clone(config);
            params['group-by'] = '[hostip]';
            config.params = params;
            $scope.buildChart(chartContainer, resource["traffic"]["slbtitlemap"]["500错误请求分SLB服务器统计"], config, false, -1, $scope.slbServerCatLinkFun);
        }, chartTimeout);


        setTimeout(function () {
            params = _.clone(params);
            config = _.clone(config);
            params['group-by'] = '[group_id]';
            config.params = params;
            $scope.buildChart(chartContainer, resource["traffic"]["slbtitlemap"]["500错误请求 Top10 Group"], config, false, 10, $scope.groupTrafficLinkFun);

            params = _.clone(params);
            config = _.clone(config);
            params['group-by'] = '[domain,request_uri]';
            config.params = params;
            $scope.buildChart(chartContainer, resource["traffic"]["slbtitlemap"]["500错误请求 Top10 Url"], config, false, 10);
        }, chartTimeout);

    };
    $scope.load502Charts = function (index) {
        var chartContainer = $('#chart-' + $scope.trafficTypes.links[index]);
        chartContainer.empty();

        var params = {
            'metric-name': 'slb.req.count',
            'start-time': $.format.date($scope.startTime, 'yyyy-MM-dd HH:mm'),
            'end-time': $.format.date($scope.endTime, 'yyyy-MM-dd HH:mm'),
            'tags': '{"slb_id":["' + $scope.queryId + '"],"status":["502"]}',
            'interval': '1m',
            'chart': 'line',
            'aggregator': 'sum',
            'downsampler': 'sum'
        };
        var config = {
            'url': G[G.env].urls.dashboard + "/data",
            'method': 'GET',
            'params': params
        };

        setTimeout(function () {
            $scope.buildChart(chartContainer, resource["traffic"]["slbtitlemap"]["502错误请求"], config, true, -1);

            params = _.clone(params);
            config = _.clone(config);
            params['group-by'] = '[hostip]';
            config.params = params;
            $scope.buildChart(chartContainer, resource["traffic"]["slbtitlemap"]["502错误请求分SLB服务器统计"], config, false, -1, $scope.slbServerCatLinkFun);

        }, chartTimeout);

        setTimeout(function () {
            params = _.clone(params);
            config = _.clone(config);
            params['group-by'] = '[group_id]';
            config.params = params;
            $scope.buildChart(chartContainer, resource["traffic"]["slbtitlemap"]["502错误请求 Top10 Group"], config, false, 10, $scope.groupTrafficLinkFun);

            params = _.clone(params);
            config = _.clone(config);
            params['group-by'] = '[domain,request_uri]';
            config.params = params;
            $scope.buildChart(chartContainer, resource["traffic"]["slbtitlemap"]["502错误请求 Top10 Url"], config, false, 10);
        }, chartTimeout);

    };
    $scope.load504Charts = function (index) {
        var chartContainer = $('#chart-' + $scope.trafficTypes.links[index]);
        chartContainer.empty();

        var params = {
            'metric-name': 'slb.req.count',
            'start-time': $.format.date($scope.startTime, 'yyyy-MM-dd HH:mm'),
            'end-time': $.format.date($scope.endTime, 'yyyy-MM-dd HH:mm'),
            'tags': '{"slb_id":["' + $scope.queryId + '"],"status":["504"]}',
            'interval': '1m',
            'chart': 'line',
            'aggregator': 'sum',
            'downsampler': 'sum'
        };
        var config = {
            'url': G[G.env].urls.dashboard + "/data",
            'method': 'GET',
            'params': params
        };

        setTimeout(function () {
            $scope.buildChart(chartContainer, resource["traffic"]["slbtitlemap"]["504错误请求"], config, true, -1);

            params = _.clone(params);
            config = _.clone(config);
            params['group-by'] = '[hostip]';
            config.params = params;
            $scope.buildChart(chartContainer, resource["traffic"]["slbtitlemap"]["504错误请求分SLB服务器统计"], config, false, -1, $scope.slbServerCatLinkFun);

        }, chartTimeout);

        setTimeout(function () {
            params = _.clone(params);
            config = _.clone(config);
            params['group-by'] = '[group_id]';
            config.params = params;
            $scope.buildChart(chartContainer, resource["traffic"]["slbtitlemap"]["504错误请求 Top10 Group"], config, false, 10, $scope.groupTrafficLinkFun);

            params = _.clone(params);
            config = _.clone(config);
            params['group-by'] = '[domain,request_uri]';
            config.params = params;
            $scope.buildChart(chartContainer, resource["traffic"]["slbtitlemap"]["504错误请求 Top10 Url"], config, false, 10);
        }, chartTimeout);

    };
    $scope.load4xxCharts = function (index) {
        var chartContainer = $('#chart-' + $scope.trafficTypes.links[index]);
        chartContainer.empty();

        var params = {
            'metric-name': 'slb.req.count',
            'start-time': $.format.date($scope.startTime, 'yyyy-MM-dd HH:mm'),
            'end-time': $.format.date($scope.endTime, 'yyyy-MM-dd HH:mm'),
            'tags': '{"slb_id":["' + $scope.queryId + '"],"status":["4*"]}',
            'interval': '1m',
            'chart': 'line',
            'aggregator': 'sum',
            'downsampler': 'sum'
        };
        var config = {
            'url': G[G.env].urls.dashboard + "/data",
            'method': 'GET',
            'params': params
        };

        setTimeout(function () {
            $scope.buildChart(chartContainer, resource["traffic"]["slbtitlemap"]["4xx错误请求"], config, true, -1);

            params = _.clone(params);
            config = _.clone(config);
            params['group-by'] = '[status]';
            config.params = params;
            $scope.buildChart(chartContainer, resource["traffic"]["slbtitlemap"]["4xx错误请求状态码分布统计"], config, false, -1, $scope.slbServerCatLinkFun);
        }, chartTimeout);

        setTimeout(function () {
            params = _.clone(params);
            config = _.clone(config);
            params['group-by'] = '[hostip]';
            config.params = params;
            $scope.buildChart(chartContainer, resource["traffic"]["slbtitlemap"]["4xx错误请求分SLB服务器统计"], config, false, -1, $scope.slbServerCatLinkFun);

            params = _.clone(params);
            config = _.clone(config);
            params['group-by'] = '[group_id]';
            config.params = params;
            $scope.buildChart(chartContainer, resource["traffic"]["slbtitlemap"]["4xx错误请求 Top10 Group"], config, false, 10, $scope.groupTrafficLinkFun);

        }, chartTimeout);


        setTimeout(function () {
            params = _.clone(params);
            config = _.clone(config);
            params['group-by'] = '[domain,request_uri]';
            config.params = params;
            $scope.buildChart(chartContainer, resource["traffic"]["slbtitlemap"]["4xx错误请求 Top10 Url"], config, false, 10);
        }, chartTimeout);

    };
    $scope.load404Charts = function (index) {
        var chartContainer = $('#chart-' + $scope.trafficTypes.links[index]);
        chartContainer.empty();

        var params = {
            'metric-name': 'slb.req.count',
            'start-time': $.format.date($scope.startTime, 'yyyy-MM-dd HH:mm'),
            'end-time': $.format.date($scope.endTime, 'yyyy-MM-dd HH:mm'),
            'tags': '{"slb_id":["' + $scope.queryId + '"],"status":["404"]}',
            'interval': '1m',
            'chart': 'line',
            'aggregator': 'sum',
            'downsampler': 'sum'
        };
        var config = {
            'url': G[G.env].urls.dashboard + "/data",
            'method': 'GET',
            'params': params
        };

        setTimeout(function () {
            $scope.buildChart(chartContainer, resource["traffic"]["slbtitlemap"]["404错误请求"], config, true, -1);

            params = _.clone(params);
            config = _.clone(config);
            params['group-by'] = '[hostip]';
            config.params = params;
            $scope.buildChart(chartContainer, resource["traffic"]["slbtitlemap"]["404错误请求分SLB服务器统计"], config, false, -1, $scope.slbServerCatLinkFun);

        }, chartTimeout);

        setTimeout(function () {
            params = _.clone(params);
            config = _.clone(config);
            params['group-by'] = '[group_id]';
            config.params = params;
            $scope.buildChart(chartContainer, resource["traffic"]["slbtitlemap"]["404错误请求 Top10 Group"], config, false, 10, $scope.groupTrafficLinkFun);

            params = _.clone(params);
            config = _.clone(config);
            params['group-by'] = '[domain,request_uri]';
            config.params = params;
            $scope.buildChart(chartContainer, resource["traffic"]["slbtitlemap"]["404错误请求 Top10 Url"], config, false, 10);
        }, chartTimeout);


    };
    $scope.loadNone200Charts = function (index) {
        var chartContainer = $('#chart-' + $scope.trafficTypes.links[index]);
        chartContainer.empty();

        var params = {
            'metric-name': 'slb.req.count',
            'start-time': $.format.date($scope.startTime, 'yyyy-MM-dd HH:mm'),
            'end-time': $.format.date($scope.endTime, 'yyyy-MM-dd HH:mm'),
            'tags': '{"slb_id":["' + $scope.queryId + '"],"status":["009","400","401","403","404","405","406","408","409","411","412","413","414","415","416","429","430","499","500","501","502","503","504","505","520"]}',
            'interval': '1m',
            'chart': 'line',
            'aggregator': 'sum',
            'downsampler': 'sum'
        };
        var config = {
            'url': G[G.env].urls.dashboard + "/data",
            'method': 'GET',
            'params': params
        };

        setTimeout(function () {
            $scope.buildChart(chartContainer, resource["traffic"]["slbtitlemap"]["非200错误请求"], config, true, -1);

            params = _.clone(params);
            config = _.clone(config);
            params['group-by'] = '[status]';
            config.params = params;
            $scope.buildChart(chartContainer, resource["traffic"]["slbtitlemap"]["非200错误请求状态码分布"], config, false, -1);

        }, chartTimeout);

        setTimeout(function () {
            params = _.clone(params);
            config = _.clone(config);
            params['group-by'] = '[hostip]';
            config.params = params;
            $scope.buildChart(chartContainer, resource["traffic"]["slbtitlemap"]["非200错误请求分SLB服务器统计"], config, false, -1, $scope.slbServerCatLinkFun);

            params = _.clone(params);
            config = _.clone(config);
            params['group-by'] = '[group_id]';
            config.params = params;
            $scope.buildChart(chartContainer, resource["traffic"]["slbtitlemap"]["非200错误请求 Top10 Group"], config, false, 10, $scope.groupTrafficLinkFun);

        }, chartTimeout);

        setTimeout(function () {
            params = _.clone(params);
            config = _.clone(config);
            params['group-by'] = '[domain,request_uri]';
            config.params = params;
            $scope.buildChart(chartContainer, resource["traffic"]["slbtitlemap"]["非200错误请求 Top10 Url"], config, false, 10);
        }, chartTimeout);

    };
    $scope.loadSlowCharts = function (index) {
        var chartContainer = $('#chart-' + $scope.trafficTypes.links[index]);
        chartContainer.empty();

        var params = {
            'metric-name': 'slb.req.count',
            'start-time': $.format.date($scope.startTime, 'yyyy-MM-dd HH:mm'),
            'end-time': $.format.date($scope.endTime, 'yyyy-MM-dd HH:mm'),
            'tags': '{"slb_id":["' + $scope.queryId + '"],"cost":["10~20s"]}',
            'interval': '1m',
            'chart': 'line',
            'aggregator': 'sum',
            'downsampler': 'sum'
        };
        var config = {
            'url': G[G.env].urls.dashboard + "/data",
            'method': 'GET',
            'params': params
        };

        setTimeout(function () {
            $scope.buildChart(chartContainer, resource["traffic"]["slbtitlemap"]["10~20s慢请求"], config, true, -1);

            params = _.clone(params);
            config = _.clone(config);
            params.tags = '{"slb_id":["' + $scope.queryId + '"],"cost":["20~30s","30~50s","50~100s",">100s"]}';
            params['group-by'] = '';
            config.params = params;
            $scope.buildChart(chartContainer, resource["traffic"]["slbtitlemap"][">20s慢请求"], config, true, -1);

        }, chartTimeout);

        setTimeout(function () {
            params = _.clone(params);
            config = _.clone(config);
            params.tags = '{"slb_id":["' + $scope.queryId + '"],"cost":["10~20s"]}';
            params['group-by'] = '[hostip]';
            config.params = params;
            $scope.buildChart(chartContainer, resource["traffic"]["slbtitlemap"]["10~20s慢请求SLB服务器分布"], config, false, -1, $scope.slbServerCatLinkFun);

            params = _.clone(params);
            config = _.clone(config);
            params.tags = '{"slb_id":["' + $scope.queryId + '"],"cost":["20~30s","30~50s","50~100s",">100s"]}';
            params['group-by'] = '[hostip]';
            config.params = params;
            $scope.buildChart(chartContainer, resource["traffic"]["slbtitlemap"][">20s慢请求SLB服务器分布"], config, false, -1, $scope.slbServerCatLinkFun);

        }, chartTimeout);
        setTimeout(function () {
            params = _.clone(params);
            config = _.clone(config);
            params.tags = '{"slb_id":["' + $scope.queryId + '"],"cost":["10~20s"]}';
            params['group-by'] = '[group_id]';
            config.params = params;
            $scope.buildChart(chartContainer, resource["traffic"]["slbtitlemap"]["10~20s慢请求 Top10 Group"], config, false, 10, $scope.groupTrafficLinkFun);

            params = _.clone(params);
            config = _.clone(config);
            params.tags = '{"slb_id":["' + $scope.queryId + '"],"cost":["20~30s","30~50s","50~100s",">100s"]}';
            params['group-by'] = '[group_id]';
            config.params = params;
            $scope.buildChart(chartContainer, resource["traffic"]["slbtitlemap"][">20s慢请求 Top10 Group"], config, false, 10, $scope.groupTrafficLinkFun);

        }, chartTimeout);

        setTimeout(function () {
            params = _.clone(params);
            config = _.clone(config);
            params.tags = '{"slb_id":["' + $scope.queryId + '"],"cost":["10~20s"]}';
            params['group-by'] = '[domain,request_uri]';
            config.params = params;
            $scope.buildChart(chartContainer, "10~20s慢请求 Top10 Url", config, false, 10);

            params = _.clone(params);
            config = _.clone(config);
            params.tags = '{"slb_id":["' + $scope.queryId + '"],"cost":["20~30s","30~50s","50~100s",">100s"]}';
            params['group-by'] = '[domain,request_uri]';
            config.params = params;
            $scope.buildChart(chartContainer, ">20s慢请求 Top10 Url", config, false, 10);
        }, chartTimeout);
    };

    $scope.loadInterceptCharts = function (index) {
        var chartContainer = $('#chart-' + $scope.trafficTypes.links[index]);
        chartContainer.empty();

        var params = {
            'metric-name': 'slb.req.count',
            'start-time': $.format.date($scope.startTime, 'yyyy-MM-dd HH:mm'),
            'end-time': $.format.date($scope.endTime, 'yyyy-MM-dd HH:mm'),
            'tags': '{"slb_id":["' + $scope.queryId + '"],"intercept_status":[1]}',
            'interval': '1m',
            'chart': 'line',
            'aggregator': 'sum',
            'downsampler': 'sum'
        };

        var config = {
            'url': G[G.env].urls.dashboard + "/data",
            'method': 'GET',
            'params': params
        };
        $scope.buildChart(chartContainer, resource["traffic"]["slbtitlemap"]["被拦截的请求"], config, true, -1);
    };
    $scope.hashChanged = function (hashData) { $scope.resource = H.resource;
        $scope.resource = H.resource;
        resource =  H.resource;
        if (hashData.env) {
            $scope.env = hashData.env;
        }
        var startTime = new Date(decodeURIComponent(hashData.startTime));
        var trafficType = '';
        if (!U.isValidDate(startTime)) {
            startTime = $scope.getDateNow();
        } else {
            $scope.queryDate.startTime = $.format.date(startTime, 'yyyy-MM-dd HH:mm:00');
            $scope.queryDate.endTime = new Date(startTime.getTime() + 1000 * 60 * 90);
        }

        var t = hashData.trafficType;
        var tls = $scope.trafficTypes.links;
        if (!_.contains(tls, t)) {
            trafficType = $scope.getTrafficType($scope.trafficTypes.current);
            H.setData({startTime: startTime, trafficType: trafficType});
            return;
        } else {
            $scope.trafficTypes.current = t;
            $scope.trafficTypes.currentIndex = _.indexOf(tls, t);
        }
        if (hashData[$scope.paramName]) {
            $scope[$scope.paramName] = hashData[$scope.paramName];
            $scope.buildChartsContainer();
            $scope.init();
        }
    };
    H.addListener("trafficApp", $scope, $scope.hashChanged);
});
angular.bootstrap(document.getElementById("traffic-area"), ['trafficApp']);
