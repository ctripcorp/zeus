//InfoLinksComponent: info links
var headerInfoApp = angular.module('headerInfoApp', []);
headerInfoApp.controller('headerInfoController', function ($scope, $http) {
    $scope.query = {};
    $scope.generateLink = function (a) {
        var link = '';
        switch (a) {
            case 'hc': {
                link = "/portal/statistics/statistics-hc#?env=" + G.env;
                break;
            }
            case 'rule': {
                link = "/portal/statistics/rule#?env=" + G.env;
                break;
            }
            case 'basic':
            {
                link = "/portal/statistics#?env=" + G.env;
                break;
            }
            case 'pie':
            {
                link = "/portal/statistics/charts#?env=" + G.env;
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
            case 'ctriplanguage':
            {
                link = "/portal/statistics/statistics-ctrip-language#?env=" + G.env;
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

var releaseApp = angular.module('slbHealthQueryApp', ['http-auth-interceptor',"angucomplete-alt"]);
releaseApp.controller('slbHealthQueryController', function ($scope, $http, $q) {
    $scope.slbs = {};
    $scope.query = {
        "slbIds":{},
        "slbName":''
    };

    //Load cache
    $scope.cacheRequestFn = function(str){
        return {q: str, timestamp: new Date().getTime()};
    };
    $scope.remoteSLBsUrl = function() {
        return G.baseUrl + "/api/meta/slbs";
    };

    // select event
    $scope.selectSlbId = function (o) {
        if(o){
            $scope.query.slbIds[o.originalObject.id] = o.originalObject.name ;
        }
    };
    $scope.removeSlb = function (slb) {
        delete $scope.query.slbIds[slb];
    };

    // changed
    $scope.hashChanged = function (hashData) { $scope.resource = H.resource;
        $scope.resource = H.resource;
        if (hashData.env) {
            $scope.env = hashData.env;
        }
    };
    H.addListener("healthApp", $scope, $scope.hashChanged);
});
angular.bootstrap(document.getElementById("health-query-area"), ['slbHealthQueryApp']);

(function (slb, $, undefined) {
    slb.getDatas = function (callBack,slbIds) {
        $.ajax({
            xhrFields: {
                withCredentials: true
            },
            url: G.baseUrl + '/api/slbs?type=info',
            type: "GET",
            dataType: "json",
            timeout: 99000,
            beforeSend: function () {
                $('#slb-health-area').showLoading();
            },
            error: function (xhr, status, error) {
                console.log(error);
            },
            success: function (slbs) {
                callBack(slbs,slbIds);
            },
            complete: function () {

            }
        });
    };
    slb.getSlbHealth = function (slbs,slbIds) {
        $.ajax({
            xhrFields: {
                withCredentials: true
            },
            url: G.baseUrl + '/api/status/check/slbs',
            type: "GET",
            dataType: "json",
            timeout: 99000,
            beforeSend: function () {
            },
            error: function (xhr, status, error) {
            },
            success: function (status) {
                if(slbIds){
                    var c = status['slb-group-check-failure-entities'];
                    if(c){
                        var s = _.filter(c, function (r) {
                            return slbIds.indexOf(r['slb-id'].toString())!=-1;
                        });
                        status['slb-group-check-failure-entities']=[];
                        status['slb-group-check-failure-entities']=s;
                    }
                }

                slb.drawChart(slbs, status);
            },
            complete: function () {
                $('#slb-health-area').hideLoading();
            }
        });
    };
    slb.drawChart = function (l, r) {
        var slbs = l.slbs;
        var chartData = r['slb-group-check-failure-entities'];

        var newChartData = _.sortBy(chartData, function (term) {
            return -(_.reduce(term['failure-counts'], function (memo, num) {
                return memo + num;
            }, 0));
        });
        $.each(newChartData, function (i, item) {
            var name = 'UNKNOWN';
            var slb = _.find(slbs, function (t) {
                return t.id == item['slb-id'];
            });
            if (slb) {
                name = slb.name;
            }
            item['slb-name'] == name;
            var template = '<div class="col-md-3"><div class="portlet"> ' +
                '<div class="portlet-heading" id="portlet-head-' + item["slb-id"] + '">' +
                '<h3 class="portlet-title"><a class="text-dark" href="/portal/groups#?env='+ G.env+'&slbId='+ item['slb-id']+'&groupHealthy=healthCheckHealthy:健康拉出">' +
                item['slb-id'] + '(' + name + ')' +
                '</a></h3>' +
                '</div>' +
                '<div class="panel-collapse collapse in">' +
                '  <div class="c3 portlet-chart-container" id="portlet-body-' + item["slb-id"] + '" ></div>' +
                '</div>' +
                '</div>' +
                '</div>';

            $('#slb-health-area').append(template);
            var columArray = ['Group Pulled Out Count'];
            columArray = columArray.concat(item['failure-counts']);
            var colors = ['rgb(199, 196, 18)', 'rgb(3, 169, 244)', 'rgb(46, 157, 80)'];
            var index = _.random(0, 3);
            var chart = c3.generate({
                bindto: '#portlet-body-' + item["slb-id"],
                data: {
                    columns: [
                        columArray
                    ],
                    type: 'bar',
                    //labels: true,
                    colors: {
                        健康状况: colors[index]
                    }
                },
                bar: {
                    width: {
                        ratio: 0.5 // this makes bar width 50% of length between ticks
                    }
                    // or
                    //width: 100 // this makes bar width 100px
                },
                size: {
                    width: 380,
                    height:300
                }

            });
            chart.load({});
        });
    };

    $('#executeQuery').click(function (e) {
        var selectedSlbs =[];
        var items = $('.selected-slbs span');
        $.each(items, function (i, t) {
            selectedSlbs.push($(t).attr('tag'));
        });

        if(selectedSlbs){
            $('#slb-health-area').html('');
            slb.draw(selectedSlbs);
        }
    });
    slb.draw = function (slbIds) {
        slb.getDatas(slb.getSlbHealth,slbIds);
    }
})(window.slb = window.slb || {}, $, undefined);