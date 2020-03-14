//InfoLinksComponent: info links
var summaryInfoApp = angular.module('summaryInfoApp', ['http-auth-interceptor', 'angucomplete-alt']);
summaryInfoApp.controller('summaryController', function ($scope, $http, $q) {
    $scope.resource = H.resource;
   var resource = $scope.resource;
    $scope.query = {};
    // Auto complete
    $scope.cacheRequestFn = function (str) {
        return {q: str, timestamp: new Date().getTime()};
    };
    $scope.remoteUrl = function () {
        return G.baseUrl + "/api/meta/apps";
    };
    $scope.selectTarget = function (t) {
        if (t) {
            var toId = t.originalObject.id;
            if ($scope.target.id != toId) {
                $scope.$broadcast('angucomplete-alt:clearInput', 'targetSelector');
                var pairs = {};
                pairs['appId'] = toId;
                $scope.target.id = toId;
                H.setData(pairs);
                messageNotify((angular.equals(resource, {}) ? "切换应用:": resource.appTraffic.js.msg1), (angular.equals(resource, {}) ? "成功切换至应用： ": resource.appTraffic.js.msg2) + toId, null);
            }
        }
    };
    $scope.clickTarget= function () {
        $('#targetSelector_value').css('width','250px');
    };
    $scope.setInputsDisplay = function () {
        $('#targetSelector_value').val($scope.query.buName);
    };
    $scope.hashChanged = function (hashData) { $scope.resource = H.resource;
        if (hashData.env) {
            $scope.env = hashData.env;
        }
        if (hashData.appId) {
            $scope.query.appId = hashData.appId;
        }
        $scope.target = {};
        $scope.target.name = $scope.query.appId;
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


//InfoLinksComponent: info links
var headerInfoApp = angular.module('headerInfoApp', ["angucomplete-alt"]);
headerInfoApp.controller('headerInfoController', function ($scope, $http) {
$scope.resource = H.resource;
   var resource = $scope.resource;
    $scope.query = {};
    $scope.generateLink = function (a) {
        var link = '';
        switch (a) {
            case 'basic':
            {
                link = "/portal/app#?env=" + G.env + "&appId=" + $scope.query.appId;
                break;
            }
            case 'log':
            {
                link = "/portal/app/log#?env=" + G.env + "&appId=" + $scope.query.appId;
                break;
            }
            case 'traffic':
            {
                link = "/portal/app/traffic#?env=" + G.env + "&appId=" + $scope.query.appId;
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

//TrafficComponent: Traffic
var trafficApp = angular.module('trafficApp', ['ui.bootstrap', 'ui.bootstrap.datetimepicker','http-auth-interceptor']);
trafficApp.controller('trafficController', function ($scope, $http, $filter) {
$scope.resource = H.resource;
   var resource = $scope.resource;
    echarts.registerTheme('macarons');

    $scope.paramName = "appId";
    $scope.target = "apps";
    $scope.targetType = "APP";
    $scope.targetLink = '/portal/app';
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
        links: ['请求数','5xx请求','500请求',  '502请求', '504请求','4xx请求', '404请求', '非200请求', '慢请求']
    };
    $scope.chartsContainer = {
        container: null,
        longContainer: null,
        chartContainerWidth: null
    };

    //Area 1
    $scope.isCurrentTrafficPage = function (link) {
        return $scope.trafficTypes.current == link ? 'current' : '';
    };
    $scope.generateTargetLink = function () {
        return $scope.targetLink + "#?env=" + G.env + "&" + $scope.paramName + "=" + $scope[$scope.paramName];
    };
    $scope.getTargetInfo = function () {
        $http.get(G.baseUrl + "/api/"+$scope.target+"?" + $scope.paramName+ "=" + $scope[$scope.paramName]).success(
            function (res) {
                if(res && res.total && res.total > 0 && res['apps'])
                    res = res['app-infos'][0];
                $scope.targetInfo = " " + res['app-id'] + " / " + res['app-name'] + "";
            }
        );
    };

    //Area 2: set startDate
    $scope.refreshCharts = function () {
        $scope.loadAllCharts();
    };
    $scope.getDateNow = function () {
        var d = new Date();
        if(d.getMinutes() < 20) {
            d = d.setTime(d.getTime() - 1000*60*60);
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
    $scope.setTrafficType= function (t) {
        var time = $.format.date(new Date($scope.queryDate.startTime),'yyyy-MM-dd HH:mm:00');
        var startTime = encodeURIComponent(time);
        H.setData({startTime: startTime, trafficType:t});
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

    $scope.loadAllCharts = function () {
        $scope.startTime = new Date($scope.queryDate.startTime);
        $scope.endTime = new Date($scope.startTime.getTime() + 1000 * 60 * 90);
        $scope.queryId = $scope[$scope.paramName];

        $scope.loadTrafficCharts(0);
        $scope.load5xxCharts(1);
        $scope.load500Charts(2);
        $scope.load502Charts(3);
        $scope.load504Charts(4);
        $scope.load4xxCharts(5);
        $scope.load404Charts(6);
        $scope.loadNone200Charts(7);
        $scope.loadSlowCharts(8);
    };

    $scope.buildChart = function (chartContainer, title, config, needMark, groupByCount, groupByLinkFun) {
        var chartDom = $('<div class="col-md-6">  <div class="portlet">' +
        '  <div class="panel-collapse collapse in">' +
        ' <div class="portlet-body traffic-chart"></div>' +
        '</div>' +
        '</div></div>');
        chartContainer.append(chartDom);
        var chart = newChart(chartDom.get(0).getElementsByClassName('portlet-body')[0]);
        config.withCredentials=false;
        $http(config).success(
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
    $scope.loadTrafficCharts = function (index) {
        var resource = $scope.resource;
        var chartContainer = $('#chart-' + $scope.trafficTypes.links[index]);
        chartContainer.empty();

        var params = {
            'metric-name': 'slb.req.count',
            'start-time': $.format.date($scope.startTime, 'yyyy-MM-dd HH:mm'),
            'end-time': $.format.date($scope.endTime, 'yyyy-MM-dd HH:mm'),
            'tags': '{"group_appid":["' + $scope.queryId + '"]}',
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
        $scope.buildChart(chartContainer, (angular.equals(resource, {}) ? "总流量统计": resource.appTraffic.js.msg4), config, true, -1);

        params = _.clone(params);
        config = _.clone(config);
        params['group-by'] = '[status]';
        config.params = params;
        $scope.buildChart(chartContainer, (angular.equals(resource, {}) ? "状态码分布": resource.appTraffic.js.msg5), config, false, -1);

        params = _.clone(params);
        config = _.clone(config);
        params['group-by'] = '[cost]';
        config.params = params;
        $scope.buildChart(chartContainer, (angular.equals(resource, {}) ? "请求耗时分布": resource.appTraffic.js.msg6), config, false, -1);

        params = _.clone(params);
        config = _.clone(config);
        params['group-by'] = '[group_id]';
        config.params = params;
        $scope.buildChart(chartContainer, (angular.equals(resource, {}) ? "流量 Top10 Group": resource.appTraffic.js.msg7), config, false, 10, $scope.groupTrafficLinkFun);

        params = _.clone(params);
        config = _.clone(config);
        params['group-by'] = '[domain,request_uri]';
        config.params = params;
        $scope.buildChart(chartContainer, (angular.equals(resource, {}) ? "流量 Top10 Url": resource.appTraffic.js.msg8), config, false, 10);

        params = _.clone(params);
        config = _.clone(config);
        params['group-by'] = '[group_server]';
        config.params = params;
        $scope.buildChart(chartContainer, (angular.equals(resource, {}) ? "流量 GroupServer 分布": resource.appTraffic.js.msg9), config, false, -1);
    };
    $scope.load5xxCharts = function (index) {
       var resource = $scope.resource;
        var chartContainer = $('#chart-' + $scope.trafficTypes.links[index]);
        chartContainer.empty();

        var params = {
            'metric-name': 'slb.req.count',
            'start-time': $.format.date($scope.startTime, 'yyyy-MM-dd HH:mm'),
            'end-time': $.format.date($scope.endTime, 'yyyy-MM-dd HH:mm'),
            'tags': '{"group_appid":["' + $scope.queryId + '"],"status":["5*"]}',
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
        $scope.buildChart(chartContainer, (angular.equals(resource, {}) ? "5xx错误请求": resource.appTraffic.js.msg11), config, true, -1);

        params = _.clone(params);
        config = _.clone(config);
        params['group-by'] = '[status]';
        config.params = params;
        $scope.buildChart(chartContainer, (angular.equals(resource, {}) ? "5xx错误请求状态码分布": resource.appTraffic.js.msg12), config, false, -1);

        params = _.clone(params);
        config = _.clone(config);
        params['group-by'] = '[group_id]';
        config.params = params;
        $scope.buildChart(chartContainer, (angular.equals(resource, {}) ? "5xx错误请求 Top10 Group": resource.appTraffic.js.msg13), config, false, 10, $scope.groupTrafficLinkFun);

        params = _.clone(params);
        config = _.clone(config);
        params['group-by'] = '[domain,request_uri]';
        config.params = params;
        $scope.buildChart(chartContainer, (angular.equals(resource, {}) ? "5xx错误请求 Top10 Url": resource.appTraffic.js.msg14), config, false, 10);

        params = _.clone(params);
        config = _.clone(config);
        params['group-by'] = '[group_server]';
        config.params = params;
        $scope.buildChart(chartContainer, (angular.equals(resource, {}) ? "5xx错误请求 Top10 GroupServer": resource.appTraffic.js.msg15), config, false, 10);
    };
    $scope.load500Charts = function (index) {
           var resource = $scope.resource;

        var chartContainer = $('#chart-' + $scope.trafficTypes.links[index]);
        chartContainer.empty();

        var params = {
            'metric-name': 'slb.req.count',
            'start-time': $.format.date($scope.startTime, 'yyyy-MM-dd HH:mm'),
            'end-time': $.format.date($scope.endTime, 'yyyy-MM-dd HH:mm'),
            'tags': '{"group_appid":["' + $scope.queryId + '"],"status":["500"]}',
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
        $scope.buildChart(chartContainer, (angular.equals(resource, {}) ? "500错误请求": resource.appTraffic.js.msg16), config, true, -1);

        params = _.clone(params);
        config = _.clone(config);
        params['group-by'] = '[group_id]';
        config.params = params;
        $scope.buildChart(chartContainer, (angular.equals(resource, {}) ? "500错误请求 Top10 Group": resource.appTraffic.js.msg17), config, false, 10, $scope.groupTrafficLinkFun);

        params = _.clone(params);
        config = _.clone(config);
        params['group-by'] = '[domain,request_uri]';
        config.params = params;
        $scope.buildChart(chartContainer, (angular.equals(resource, {}) ? "500错误请求 Top10 Url": resource.appTraffic.js.msg18), config, false, 10);

        params = _.clone(params);
        config = _.clone(config);
        params['group-by'] = '[group_server]';
        config.params = params;
        $scope.buildChart(chartContainer, (angular.equals(resource, {}) ? "500错误请求 Top10 GroupServer": resource.appTraffic.js.msg19), config, false, 10);
    };
    $scope.load502Charts = function (index) {
           var resource = $scope.resource;

        var chartContainer = $('#chart-' + $scope.trafficTypes.links[index]);
        chartContainer.empty();

        var params = {
            'metric-name': 'slb.req.count',
            'start-time': $.format.date($scope.startTime, 'yyyy-MM-dd HH:mm'),
            'end-time': $.format.date($scope.endTime, 'yyyy-MM-dd HH:mm'),
            'tags': '{"group_appid":["' + $scope.queryId + '"],"status":["502"]}',
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
        $scope.buildChart(chartContainer, (angular.equals(resource, {}) ? "502错误请求": resource.appTraffic.js.msg21), config, true, -1);

        params = _.clone(params);
        config = _.clone(config);
        params['group-by'] = '[group_id]';
        config.params = params;
        $scope.buildChart(chartContainer, (angular.equals(resource, {}) ? "502错误请求 Top10 Group": resource.appTraffic.js.msg22), config, false, 10, $scope.groupTrafficLinkFun);

        params = _.clone(params);
        config = _.clone(config);
        params['group-by'] = '[domain,request_uri]';
        config.params = params;
        $scope.buildChart(chartContainer, (angular.equals(resource, {}) ? "502错误请求 Top10 Url": resource.appTraffic.js.msg23), config, false, 10);

        params = _.clone(params);
        config = _.clone(config);
        params['group-by'] = '[group_server]';
        config.params = params;
        $scope.buildChart(chartContainer, (angular.equals(resource, {}) ? "502错误请求 Top10 GroupServer": resource.appTraffic.js.msg24), config, false, 10);
    };
    $scope.load504Charts = function (index) {
               var resource = $scope.resource;
        var chartContainer = $('#chart-' + $scope.trafficTypes.links[index]);
        chartContainer.empty();

        var params = {
            'metric-name': 'slb.req.count',
            'start-time': $.format.date($scope.startTime, 'yyyy-MM-dd HH:mm'),
            'end-time': $.format.date($scope.endTime, 'yyyy-MM-dd HH:mm'),
            'tags': '{"group_appid":["' + $scope.queryId + '"],"status":["504"]}',
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
        $scope.buildChart(chartContainer, (angular.equals(resource, {}) ? "504错误请求": resource.appTraffic.js.msg25), config, true, -1);

        params = _.clone(params);
        config = _.clone(config);
        params['group-by'] = '[group_id]';
        config.params = params;
        $scope.buildChart(chartContainer, (angular.equals(resource, {}) ? "504错误请求 Top10 Group": resource.appTraffic.js.msg26), config, false, 10, $scope.groupTrafficLinkFun);

        params = _.clone(params);
        config = _.clone(config);
        params['group-by'] = '[domain,request_uri]';
        config.params = params;
        $scope.buildChart(chartContainer, (angular.equals(resource, {}) ? "504错误请求 Top10 Url": resource.appTraffic.js.msg27), config, false, 10);

        params = _.clone(params);
        config = _.clone(config);
        params['group-by'] = '[group_server]';
        config.params = params;
        $scope.buildChart(chartContainer, (angular.equals(resource, {}) ? "504错误请求 Top10 GroupServer": resource.appTraffic.js.msg28), config, false, 10);
    };
    $scope.load4xxCharts = function (index) {
           var resource = $scope.resource;

        var chartContainer = $('#chart-' + $scope.trafficTypes.links[index]);
        chartContainer.empty();

        var params = {
            'metric-name': 'slb.req.count',
            'start-time': $.format.date($scope.startTime, 'yyyy-MM-dd HH:mm'),
            'end-time': $.format.date($scope.endTime, 'yyyy-MM-dd HH:mm'),
            'tags': '{"group_appid":["' + $scope.queryId + '"],"status":["4*"]}',
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
        $scope.buildChart(chartContainer, (angular.equals(resource, {}) ? "4xx错误请求": resource.appTraffic.js.msg29), config, true, -1);

        params = _.clone(params);
        config = _.clone(config);
        params['group-by'] = '[status]';
        config.params = params;
        $scope.buildChart(chartContainer, (angular.equals(resource, {}) ? "4xx错误请求状态码分布": resource.appTraffic.js.msg31), config, false, -1);

        params = _.clone(params);
        config = _.clone(config);
        params['group-by'] = '[group_id]';
        config.params = params;
        $scope.buildChart(chartContainer, (angular.equals(resource, {}) ? "4xx错误请求 Top10 Group": resource.appTraffic.js.msg32), config, false, 10, $scope.groupTrafficLinkFun);

        params = _.clone(params);
        config = _.clone(config);
        params['group-by'] = '[domain,request_uri]';
        config.params = params;
        $scope.buildChart(chartContainer, (angular.equals(resource, {}) ? "4xx错误请求 Top10 Url": resource.appTraffic.js.msg33), config, false, 10);

        params = _.clone(params);
        config = _.clone(config);
        params['group-by'] = '[group_server]';
        config.params = params;
        $scope.buildChart(chartContainer, (angular.equals(resource, {}) ? "4xx错误请求 Top10 GroupServer": resource.appTraffic.js.msg34), config, false, 10);
    };
    $scope.load404Charts = function (index) {
           var resource = $scope.resource;

        var chartContainer = $('#chart-' + $scope.trafficTypes.links[index]);
        chartContainer.empty();

        var params = {
            'metric-name': 'slb.req.count',
            'start-time': $.format.date($scope.startTime, 'yyyy-MM-dd HH:mm'),
            'end-time': $.format.date($scope.endTime, 'yyyy-MM-dd HH:mm'),
            'tags': '{"group_appid":["' + $scope.queryId + '"],"status":["404"]}',
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
        $scope.buildChart(chartContainer, (angular.equals(resource, {}) ? "404错误请求": resource.appTraffic.js.msg35), config, true, -1);

        params = _.clone(params);
        config = _.clone(config);
        params['group-by'] = '[group_id]';
        config.params = params;
        $scope.buildChart(chartContainer, (angular.equals(resource, {}) ? "404错误请求 Top10 Group": resource.appTraffic.js.msg36), config, false, 10, $scope.groupTrafficLinkFun);

        params = _.clone(params);
        config = _.clone(config);
        params['group-by'] = '[domain,request_uri]';
        config.params = params;
        $scope.buildChart(chartContainer, (angular.equals(resource, {}) ? "404错误请求 Top10 Url": resource.appTraffic.js.msg37), config, false, 10);

        params = _.clone(params);
        config = _.clone(config);
        params['group-by'] = '[group_server]';
        config.params = params;
        $scope.buildChart(chartContainer, (angular.equals(resource, {}) ? "404错误请求 Top10 GroupServer": resource.appTraffic.js.msg38), config, false, 10);
    };
    $scope.loadNone200Charts = function (index) {
           var resource = $scope.resource;

        var chartContainer = $('#chart-' + $scope.trafficTypes.links[index]);
        chartContainer.empty();

        var params = {
            'metric-name': 'slb.req.count',
            'start-time': $.format.date($scope.startTime, 'yyyy-MM-dd HH:mm'),
            'end-time': $.format.date($scope.endTime, 'yyyy-MM-dd HH:mm'),
            'tags': '{"group_appid":["' + $scope.queryId + '"],"status":["009","400","401","403","404","405","406","408","409","411","412","413","414","415","416","429","430","499","500","501","502","503","504","505","520"]}',
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
        $scope.buildChart(chartContainer, (angular.equals(resource, {}) ? "非200错误请求": resource.appTraffic.js.msg39), config, true, -1);

        params = _.clone(params);
        config = _.clone(config);
        params['group-by'] = '[status]';
        config.params = params;
        $scope.buildChart(chartContainer, (angular.equals(resource, {}) ? "非200错误请求状态码分布": resource.appTraffic.js.msg41), config, false, -1);

        params = _.clone(params);
        config = _.clone(config);
        params['group-by'] = '[group_id]';
        config.params = params;
        $scope.buildChart(chartContainer, (angular.equals(resource, {}) ? "非200错误请求 Top10 Group": resource.appTraffic.js.msg42), config, false, 10, $scope.groupTrafficLinkFun);

        params = _.clone(params);
        config = _.clone(config);
        params['group-by'] = '[domain,request_uri]';
        config.params = params;
        $scope.buildChart(chartContainer, (angular.equals(resource, {}) ? "非200错误请求 Top10 Url": resource.appTraffic.js.msg43), config, false, 10);

        params = _.clone(params);
        config = _.clone(config);
        params['group-by'] = '[group_server]';
        config.params = params;
        $scope.buildChart(chartContainer, (angular.equals(resource, {}) ? "非200错误请求 Top10 GroupServer": resource.appTraffic.js.msg44), config, false, 10);
    };
    $scope.loadSlowCharts = function (index) {
           var resource = $scope.resource;

        var chartContainer = $('#chart-' + $scope.trafficTypes.links[index]);
        chartContainer.empty();

        var params = {
            'metric-name': 'slb.req.count',
            'start-time': $.format.date($scope.startTime, 'yyyy-MM-dd HH:mm'),
            'end-time': $.format.date($scope.endTime, 'yyyy-MM-dd HH:mm'),
            'tags': '{"group_appid":["' + $scope.queryId + '"],"cost":["10~20s"]}',
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
        $scope.buildChart(chartContainer, (angular.equals(resource, {}) ? "10~20s慢请求": resource.appTraffic.js.msg45), config, true, -1);

        params = _.clone(params);
        config = _.clone(config);
        params.tags = '{"vsid":["' + $scope.queryId + '"],"cost":["20~30s","30~50s","50~100s",">100s"]}';
        params['group-by'] = '';
        config.params = params;
        $scope.buildChart(chartContainer, (angular.equals(resource, {}) ? ">20s慢请求": resource.appTraffic.js.msg46), config, true, -1);

        params = _.clone(params);
        config = _.clone(config);
        params.tags = '{"vsid":["' + $scope.queryId + '"],"cost":["10~20s"]}';
        params['group-by'] = '[group_id]';
        config.params = params;
        $scope.buildChart(chartContainer, (angular.equals(resource, {}) ? "10~20s慢请求 Top10 Group": resource.appTraffic.js.msg47), config, false, 10, $scope.groupTrafficLinkFun);

        params = _.clone(params);
        config = _.clone(config);
        params.tags = '{"vsid":["' + $scope.queryId + '"],"cost":["20~30s","30~50s","50~100s",">100s"]}';
        params['group-by'] = '[group_id]';
        config.params = params;
        $scope.buildChart(chartContainer, (angular.equals(resource, {}) ? ">20s慢请求 Top10 Group": resource.appTraffic.js.msg48), config, false, 10, $scope.groupTrafficLinkFun);

        params = _.clone(params);
        config = _.clone(config);
        params.tags = '{"group_id":["' + $scope.queryId + '"],"cost":["10~20s"]}';
        params['group-by'] = '[group_server]';
        config.params = params;
        $scope.buildChart(chartContainer, (angular.equals(resource, {}) ? "10~20s慢请求 Top10 GroupServer": resource.appTraffic.js.msg49), config, false, 10);

        params = _.clone(params);
        config = _.clone(config);
        params.tags = '{"group_id":["' + $scope.queryId + '"],"cost":["20~30s","30~50s","50~100s",">100s"]}';
        params['group-by'] = '[group_server]';
        config.params = params;
        $scope.buildChart(chartContainer, (angular.equals(resource, {}) ? ">20s慢请求 Top10 GrouServer": resource.appTraffic.js.msg51), config, false, 10);

        params = _.clone(params);
        config = _.clone(config);
        params.tags = '{"vsid":["' + $scope.queryId + '"],"cost":["10~20s"]}';
        params['group-by'] = '[domain,request_uri]';
        config.params = params;
        $scope.buildChart(chartContainer, (angular.equals(resource, {}) ? "10~20s慢请求 Top10 Url": resource.appTraffic.js.msg52), config, false, 10);

        params = _.clone(params);
        config = _.clone(config);
        params.tags = '{"vsid":["' + $scope.queryId + '"],"cost":["20~30s","30~50s","50~100s",">100s"]}';
        params['group-by'] = '[domain,request_uri]';
        config.params = params;
        $scope.buildChart(chartContainer, (angular.equals(resource, {}) ? ">20s慢请求 Top10 Url": resource.appTraffic.js.msg53), config, false, 10);
    };

    $scope.hashChanged = function (hashData) { $scope.resource = H.resource;
        if (hashData.env) {
            $scope.env = hashData.env;
        }
        var startTime = new Date(decodeURIComponent(hashData.startTime));
        var trafficType='';
        if (!U.isValidDate(startTime)) {
            startTime =  $scope.getDateNow();
        } else {
            $scope.queryDate.startTime = $.format.date(startTime,'yyyy-MM-dd HH:mm:00');
            $scope.queryDate.endTime = new Date(startTime.getTime() + 1000 * 60 * 90);
        }

        var t = hashData.trafficType;
        var tls = $scope.trafficTypes.links;
        if (!_.contains(tls, t)) {
            trafficType = $scope.getTrafficType($scope.trafficTypes.current);
            H.setData({startTime: startTime,trafficType:trafficType});
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
