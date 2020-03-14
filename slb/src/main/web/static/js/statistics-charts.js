//InfoLinksComponent: info links
var headerInfoApp = angular.module('headerInfoApp', ["angucomplete-alt"]);
headerInfoApp.controller('headerInfoController', function ($scope, $http) {
    $scope.query = {};
    $scope.generateLink = function (a) {
        var link = '';
        switch (a) {
            case 'basic':
            {
                link = "/portal/statistics#?env=" + G.env;
                break;
            }
            case 'rule': {
                link = "/portal/statistics/rule#?env=" + G.env;
                break;
            }
            case 'pie':
            {
                link = "/portal/statistics/charts#?env=" + G.env;
                break;
            }
            case 'cert': {
                link = "/portal/statistics/certificates#?env=" + G.env;
                break;
            }
            case 'hc': {
                link = "/portal/statistics/statistics-hc#?env=" + G.env;
                break;
            }
            case 'policy':
            {
                link = "/portal/statistics/statistics-netjava#?env=" + G.env;
                break;
            }
            case 'abtest':
            {
                link = "/portal/statistics/statistics-normalpolicy#?env=" + G.env;
                break;
            }
            case 'traffic':
            {
                link = "/portal/statistics/traffic#?env=" + G.env;

                break;
            }
            case 'bu-traffic':
            {
                link = "/portal/statistics/butraffic#?env=" + G.env + '&bu=All';
                break;
            }
            case 'version':
            {
                link = "/portal/statistics/release#?env=" + G.env;
                break;
            }
            case 'health':
            {
                link = "/portal/statistics/slbhealth#?env=" + G.env;
                break;
            }
            case 'log':
            {
                link = "/portal/statistics/opslog#?env=" + G.env;
                break;
            }
            case 'database':
            {
                link = "/portal/statistics/dbtraffic#?env=" + G.env;
                break;
            }
            case 'deploy':
            {
                link = "/portal/statistics/deployment#?env=" + G.env;
                break;
            }
            case 'ctripprogress':
            {
                link = "/portal/statistics/statistics-ctrip-netjava#?env=" + G.env;
                break;
            }
            case 'comments':
            {
                link = "/portal/statistics/statistics-feedback#?env=" + G.env;
                break;
            }
            case 'unhealthy':
            {
                link = "/portal/statistics/statistics-unhealthy#?env=" + G.env;
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

//StatisticsComponent: Statistics
var statisticsApp = angular.module('statisticsApp', ['ui.bootstrap', 'ui.bootstrap.datetimepicker', 'http-auth-interceptor']);
statisticsApp.controller('statisticsController', function ($scope, $http, $filter, $q) {

    $scope.idcData = {
        'qpsData': [],
        'groupData': []
    };
    $scope.slbData = {
        'qpsData': [],
        'groupData': []
    };
    $scope.sbuData = {
        'qpsData': [],
        'groupData': []
    };
    $scope.zoneData = {
        'qpsData': [],
        'groupData': []
    };

    var othertext;
    $scope.resetData = function () {
        $scope.idcData = {
            'qpsData': [],
            'groupData': []
        };
        $scope.slbData = {
            'qpsData': [],
            'groupData': []
        };
        $scope.sbuData = {
            'qpsData': [],
            'groupData': []
        };
        $scope.zoneData = {
            'qpsData': [],
            'groupData': []
        };
    };

    $scope.getOtherValue = function (arr) {
        var sumValue = 0, otherValue = 0;
        $.each(arr, function (i, val) {
            sumValue += val.value;
        });

        var top10Result = _.sortBy(arr, 'value').slice(-10);
        $.each(top10Result, function (i, val) {
            otherValue = sumValue - val.value;
        });
        return Math.floor(otherValue);
    };

    $scope.loadData = function () {
        $scope.resetData();
        $q.all(
            [
                $http.get(G.baseUrl + "/api/statistics/idc").success(
                    function (res) {
                        if (res['idc-metas']) {
                            $.each(res['idc-metas'], function (i, val) {
                                $scope.idcData.qpsData.push(
                                    {
                                        value: Math.floor(val.qps),
                                        name: val.idc
                                    }
                                );
                                $scope.idcData.groupData.push(
                                    {
                                        value: val['group-count'],
                                        name: val.idc
                                    }
                                );
                            })
                        }
                    }
                ),
                $http.get(G.baseUrl + "/api/statistics/sbu").success(
                    function (res) {
                        if (res['sbu-metas']) {
                            $.each(res['sbu-metas'], function (i, val) {
                                $scope.sbuData.qpsData.push(
                                    {
                                        value: Math.floor(val.qps),
                                        name: val.sbu
                                    }
                                );
                                $scope.sbuData.groupData.push(
                                    {
                                        value: val['group-count'],
                                        name: val.sbu
                                    }
                                );
                            });

                            var sortByQpsResult = _.sortBy($scope.sbuData.qpsData, 'value');
                            $scope.sbuData.qpsData = sortByQpsResult.slice(-10);
                            $scope.sbuData.qpsData.push(
                                {
                                    value: $scope.getOtherValue(sortByQpsResult),
                                    name: othertext
                                }
                            );


                            var sortByGroupCountResult = _.sortBy($scope.sbuData.groupData, 'value');
                            $scope.sbuData.groupData = sortByGroupCountResult.slice(-10);
                            $scope.sbuData.groupData.push(
                                {
                                    value: $scope.getOtherValue(sortByGroupCountResult),
                                    name: othertext
                                }
                            );
                        }
                    }
                ),
                $http.get(G.baseUrl + "/api/slbs?type=info").success(
                    function (res) {
                        $scope.slbsInfoMap = {};
                        if (res.total && res.total > 0) {
                            $.each(res['slbs'], function (i, val) {
                                $scope.slbsInfoMap[val.id] = val;
                            });
                        }
                    }
                ),
            ]
        ).then(
            function () {
                $q.all(
                    [
                        $http.get(G.baseUrl + "/api/statistics/slbs").success(
                            function (res) {
                                $scope.slbStatisticsMap = {};
                                if (res['slb-metas']) {
                                    $.each(res['slb-metas'], function (i, val) {
                                        $scope.slbStatisticsMap[val['slb-id']] = val;
                                        $scope.slbData.qpsData.push(
                                            {
                                                value: val.qps,
                                                name: $scope.slbsInfoMap[val['slb-id']] ?
                                                val['slb-id'] + "/" + $scope.slbsInfoMap[val['slb-id']].name : val['slb-id']
                                            }
                                        );
                                        $scope.slbData.groupData.push(
                                            {
                                                value: val['group-count'],
                                                name: $scope.slbsInfoMap[val['slb-id']] ?
                                                val['slb-id'] + "/" + $scope.slbsInfoMap[val['slb-id']].name : val['slb-id']
                                            }
                                        );
                                    });
                                    var sortByQpsResult = _.sortBy($scope.slbData.qpsData, 'value');
                                    $scope.slbData.qpsData = sortByQpsResult.slice(-10);
                                    $scope.slbData.qpsData.push(
                                        {
                                            value: $scope.getOtherValue(sortByQpsResult),
                                            name: othertext
                                        }
                                    );

                                    var sortByGroupCountResult = _.sortBy($scope.slbData.groupData, 'value');
                                    $scope.slbData.groupData = sortByGroupCountResult.slice(-10);
                                    $scope.slbData.groupData.push(
                                        {
                                            value: $scope.getOtherValue(sortByGroupCountResult),
                                            name: othertext
                                        }
                                    );
                                }
                            }
                        ),
                        $http.get(G.baseUrl + "/api/slbs?anyProp=zone:内网").success(
                            function (res) {
                                $scope.privateSlbs = res['slbs'];
                                var privateQps = 0;
                                var privateGroups = 0;
                                $.each($scope.privateSlbs, function (i, val) {
                                    if ($scope.slbStatisticsMap[val.id]) {
                                        privateQps += $scope.slbStatisticsMap[val.id].qps;
                                        privateGroups += $scope.slbStatisticsMap[val.id]['group-count'];
                                    }
                                });
                                $scope.zoneData.qpsData.push(
                                    {
                                        value: Math.floor(privateQps),
                                        name: '内网'
                                    }
                                );
                                $scope.zoneData.groupData.push(
                                    {
                                        value: privateGroups,
                                        name: '内网'
                                    }
                                );
                            }
                        ),
                        $http.get(G.baseUrl + "/api/slbs?anyProp=zone:外网").success(
                            function (res) {
                                $scope.publicSlbs = res['slbs'];
                                var publicQps = 0;
                                var publicGroups = 0;
                                $.each($scope.publicSlbs, function (i, val) {
                                    if ($scope.slbStatisticsMap[val.id]) {
                                        publicQps += $scope.slbStatisticsMap[val.id].qps;
                                        publicGroups += $scope.slbStatisticsMap[val.id]['group-count'];
                                    }
                                });
                                $scope.zoneData.qpsData.push(
                                    {
                                        value: Math.floor(publicQps),
                                        name: '外网'
                                    }
                                );
                                $scope.zoneData.groupData.push(
                                    {
                                        value: publicGroups,
                                        name: '外网'
                                    }
                                );
                            }
                        )
                    ]
                ).then(
                    function () {
                        $scope.buildChartsContainer();
                        $scope.init();
                    }
                )
            }
        );
    };

    echarts.registerTheme('macarons');

    $scope.paramName = "buName";
    $scope.target = "apps";
    $scope.targetType = "BU";
    $scope.targetLink = '/portal/bu';
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
        links: ['请求数']
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
        $scope.targetInfo = " " + $scope[$scope.paramName] + " ";
    };

    //Area 2: set startDate
    $scope.refreshCharts = function () {
        $scope.loadAllCharts();
    };

    $scope.isOpen = false;
    $scope.openCalendar = function (e) {
        e.preventDefault();
        e.stopPropagation();
        $scope.isOpen = true;
    };
    $scope.setTrafficType = function (t) {
        H.setData({trafficType: t});
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
        $scope.loadAllCharts();
        $scope.scrollChartsContainer();
    };

    $scope.loadAllCharts = function () {
        $scope.queryId = $scope[$scope.paramName];
        $scope.loadTrafficCharts(0);
    };

    $scope.buildChart = function (chartContainer, data, title, type) {
        var context=""
        var chartDom = $('<div class="col-md-6">  <div class="portlet">' +
        '  <div class="panel-collapse collapse in">' +
        ' <div class="portlet-body traffic-chart"></div>' +
        '</div>' +
        '</div></div>');
        chartContainer.append(chartDom);
        var chart = newChart(chartDom.get(0).getElementsByClassName('portlet-body')[0]);

        chart.on('click', function (params) {
            switch (type) {
                case 'sbu':
                {
                    if (params.seriesName) {
                        if (params.seriesName == 'QPS') {
                            var buName = params.data.name;
                            if (buName == othertext)
                                window.open("/portal/bus#?env=" + G.env);
                            else
                                window.open("/portal/bu#?env=" + G.env + "&buName=" + buName);

                        }
                        if (params.seriesName == 'Group数量') {
                            var buName = params.data.name;
                            if (buName == othertext)
                                window.open("/portal/groups#?env=" + G.env);
                            else
                                window.open("/portal/groups#?env=" + G.env + "&groupBues=" + buName);
                        }
                    }
                    break;
                }
                case 'slb':
                {
                    if (params.seriesName) {
                        if (params.seriesName == 'QPS') {
                            var slbName = params.data.name;
                            if (slbName == othertext)
                                window.open("/portal/slbs#?env=" + G.env);
                            else
                                window.open("/portal/slb#?env=" + G.env + "&slbId=" + slbName.split("/")[0]);
                        }
                        if (params.seriesName == 'Group数量') {
                            var slbName = params.data.name;
                            if (slbName == othertext)
                                window.open("/portal/groups#?env=" + G.env);
                            else
                                window.open("/portal/groups#?env=" + G.env + "&slbId=" + slbName.split("/")[0]);
                        }
                    }
                    break;
                }
                case 'idc':
                {
                    if (params.seriesName) {
                        if (params.seriesName == 'QPS') {
                            var idcName = params.data.name;
                            window.open("/portal/slbs#?env=" + G.env + "&idcs=" + idcName);
                        }
                        if (params.seriesName == 'Group数量') {
                            window.open("/portal/groups#?env=" + G.env);
                        }
                    }
                    break;
                }
                case 'zone':
                {
                    if (params.seriesName) {
                        if (params.seriesName == 'QPS') {
                            var zoneName = params.data.name;
                            window.open("/portal/slbs#?env=" + G.env + "&zones=" + zoneName);
                        }
                        if (params.seriesName == 'Group数量') {
                            window.open("/portal/groups#?env=" + G.env);
                        }
                    }
                    break;
                }
            }
        });

        loadSimplePieChartData(chart, data, title);
    };
    $scope.groupTrafficLinkFun = function (id) {
        return '/portal/bu/traffic' + H.generateHashStr({
                env: G.env,
                groupId: id,
                startTime: H.getParam('startTime'),
                trafficType: H.getParam('trafficType')
            });
    };
    $scope.loadTrafficCharts = function (index) {
        var sbucontext= $scope.resource["statisticscharts"]["charts_statisticsApp_sbu"];
        var idccontext= $scope.resource["statisticscharts"]["charts_statisticsApp_idc"];
        var zonecontext= $scope.resource["statisticscharts"]["charts_statisticsApp_zone"];
        var slbcontext= $scope.resource["statisticscharts"]["charts_statisticsApp_slb"];

        var chartContainer = $('#chart-' + $scope.trafficTypes.links[index]);
        chartContainer.empty();

        var chartDom = $('<div class="col-md-12" ><b style="font-size: 28px">'+sbucontext["title"]+':</b></div><br>');
        chartContainer.append(chartDom);
        $scope.buildChart(chartContainer, $scope.sbuData.qpsData, sbucontext["charttitle"], "sbu");
        $scope.buildChart(chartContainer, $scope.sbuData.groupData, sbucontext["grouptitle"], "sbu");

        chartDom = $('<div class="col-md-12"><b style="font-size: 28px">'+slbcontext["title"]+':</b></div><br>');
        chartContainer.append(chartDom);
        $scope.buildChart(chartContainer, $scope.slbData.qpsData, slbcontext["charttitle"], "slb");
        $scope.buildChart(chartContainer, $scope.slbData.groupData, sbucontext["grouptitle"], "slb");

        chartDom = $('<div class="col-md-12" ><b style="font-size: 28px">'+idccontext["title"]+':</b></div><br>');
        chartContainer.append(chartDom);
        $scope.buildChart(chartContainer, $scope.idcData.qpsData, idccontext["charttitle"], "idc");
        $scope.buildChart(chartContainer, $scope.idcData.groupData, idccontext["grouptitle"], "idc");

        chartDom = $('<div class="col-md-12" ><b style="font-size: 28px">'+zonecontext["title"]+':</b></div><br>');
        chartContainer.append(chartDom);
        $scope.buildChart(chartContainer, $scope.zoneData.qpsData, zonecontext["charttitle"], "zone");
        $scope.buildChart(chartContainer, $scope.zoneData.groupData, zonecontext["grouptitle"], "zone");
    };

    $scope.hashChanged = function (hashData) { $scope.resource = H.resource;
        $scope.resource = H.resource;
        othertext = $scope.resource['statisticscharts']['charts_statisticsApp_other'];
        if (hashData.env) {
            $scope.env = hashData.env;
        }
        $scope.loadData();
    };

    H.addListener("statisticsApp", $scope, $scope.hashChanged);

});
angular.bootstrap(document.getElementById("statistics-area"), ['statisticsApp']);
