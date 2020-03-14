var _trafficParam = {
    'metric-name': 'slb.req.count',
    'start-time': '',
    'end-time': '',
    // '{"group_id":["' + $scope.queryId + '"]}
    'tags': '',
    'group-by': '',
    'interval': '1m',
    'chart': 'line',
    'aggregator': 'count',
    'downsampler': 'count'
};

var _trafficLoader = {
    _param: '',
    _url: '',
    _remoteCall: '',

    init: function (param, url) {
        this._param = param;
        if (url) this._url = url;
        return this;
    },
    request: function (_remoteCall) {
        var url = this._url;
        var param = this._param;

        var promise = _remoteCall.getData(url, param);
        return promise;
    },
    drawChart: function (area, title, des, start, data, limit, xAxisFunc) {
        _trafficChart.init(area, title, des, start, data, limit, xAxisFunc).build();
    }
};

var _trafficChart = {

    // Chart area
    area: '',

    // chart title
    title: '',

    // chart description
    description: '',

    // chart start time point
    startTime: '',

    // point to point interval
    interval: 60000,

    // need to show a circle in each point
    markable: false,

    // only get the top limited rows of the chart
    limited: -1,

    // Function will be called when '查看分组' has been clicked
    navFn: '',

    // chart data: data that return from dashboard
    chartData: '',

    init: function (_area, _title, _desc, _startTime, _chartData, _limit, _xAxisFunc) {
        this.area = _area;
        this.title = _title;
        this.description = _desc;
        this.startTime = new Date(_startTime).getTime();
        this.chartData = _chartData;
        this.limited = _limit || -1;
        this.xAxisFunc = _xAxisFunc;
        return this;
    },
    build: function () {
        loadChartData(this.area,
            this.chartData,
            this.title,
            this.description,
            this.startTime,
            this.interval,
            this.markable,
            this.limited,
            this.navFn,
            this.xAxisFunc
        );
    }
};