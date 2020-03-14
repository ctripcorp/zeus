var headerInfoApp = angular.module('headerInfoApp', ["angucomplete-alt"]);
headerInfoApp.controller('headerInfoController', function ($scope, $http) {
    $scope.query = {};
    $scope.generateLink = function (a) {
        var link = '';
        switch (a) {
            case 'basic': {
                link = "/portal/statistics#?env=" + G.env;
                break;
            }
            case 'policy': {
                link = "/portal/statistics/statistics-netjava#?env=" + G.env;
                break;
            }
            case 'dashboard': {
                link = "/portal/statistics/dashboard#?env=" + G.env;
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
    $scope.hashChanged = function (hashData) {
        $scope.resource = H.resource;
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

var statisticsApp = angular.module('statisticsApp', ['ui.bootstrap', 'ui.bootstrap.datetimepicker', 'http-auth-interceptor']);
statisticsApp.controller('statisticsController', function ($scope, $http, $filter, $q) {
    var hourSeconds = 1000 * 60 * 60;
    var cats = {
        '法兰克福': 'http://cat.fraaws.tripws.com/',
        '美西': 'http://cat.sfoaws.tripws.com/',
        '新家坡': 'http://cat.sinaws.tripws.com/'
    };
    $scope.data = {
        todaymax: {},
        qps: {
            total: 0
        },
        slbs: {},
        vses: {},
        groups: {},
        apps: {},
        envs: {
            '生产': G['pro']['urls'].api,
            '法兰克福': G['fra-aws']['urls'].api,
            '美西': G['sfo-aws']['urls'].api,
            '新家坡': G['sin-aws']['urls'].api
        },
        times: {
            '当前': 0,
            '-7天': -7 * 24 * hourSeconds,
            '-1天': -24 * hourSeconds,
            '+1天': 24 * hourSeconds,
            '+7天': 7 * 24 * hourSeconds
        }
    };

    var healthyMapping = {
        'healthy': '健康',
        'unhealthy': '不健康',
        'broken': 'Broken'
    };

    $scope.query = {
        showLevel: 'list',
        env: '生产'
    };

    $scope.showChart = function (type) {
        var prods = ['reload', 'qps', 'd-qps'];
        var foreigns = ['all-qps'];
        if ($scope.query.env == '生产') return prods.indexOf(type) != -1;
        else return foreigns.indexOf(type) != -1;
    }
    $scope.toggleShow = function (type) {
        $scope.query.env = type;
        H.setData({timeStamp: new Date().getTime(), cenv: type})
    };

    $scope.showClass = function (type) {
        if (type == $scope.query.env) return 'selected';
        return '';
    };

    $scope.getHealthLinks = function (k) {
        var invertHealthyMap = _.invert(healthyMapping);
        var env = $scope.env;

        var healthy = invertHealthyMap[k];
        window.open('/portal/groups#?env=' + env + '&groupType=Group,V-Group&groupHealthy=healthy:' + healthy, '_blank');
    };

    $scope.getBrokenText = function (k) {
        var invertHealthyMap = _.invert(healthyMapping);
        var healthy = invertHealthyMap[k];
        if (healthy == 'broken') return true;
    };

    $scope.loadData = function (hashData) {
        var resource = $scope.resource;
        if (!resource || _.keys(resource).length == 0) return;

        var slbsQuery = {
            method: 'GET',
            url: host + '/api/slbs?type=extended'
        };

        var vsesQuery = {
            method: 'GET',
            url: host + '/api/vses?type=extended'
        };

        var groupsQuery = {
            method: 'GET',
            url: host + '/api/groups?type=extended'
        };

        var appsQuery = {
            method: 'GET',
            url: host + '/api/apps'
        };

        var statisticsQuery = {
            method: 'GET',
            url: host + '/api/statistics/slbs'
        };


        setTimeout(function () {
            if (env == '生产') {
                loadLastRequestDay();
                loadTodayRequests();
            } else {
                loadLastRequestDayCat(cats[env]);
            }
        }, 2000);

        $http(slbsQuery).success(function (response, code) {

            if (code == 200) {
                var slbData = _.mapObject(_.indexBy(response['slbs'], 'id'), function (v, k, item) {
                    _.map(v.properties, function (p) {
                        if (p['name']) {
                            v[p['name']] = p['value'];
                        }
                    });
                    return v;
                });
                var resultByZone = [];
                _.mapObject(_.countBy(_.values(slbData), 'zone'), function (v, k, item) {
                    resultByZone.push({
                        name: k,
                        y: v
                    });
                });

                var resultByIdc = [];
                _.mapObject(_.countBy(_.values(slbData), 'idc'), function (v, k, item) {
                    resultByIdc.push({
                        name: k,
                        y: v
                    });
                });

                $scope.data.slbs['zone'] = _.countBy(_.values(slbData), 'zone');
                $scope.data.slbs['zoneCount'] = _.keys(slbData).length;

                $scope.data.slbs['idc'] = _.countBy(_.values(slbData), 'idc');

                slbs(resultByZone, 'zone', 'SLB(Total：' + _.keys(slbData).length + ')');
                slbs(resultByIdc, 'idc', resource['coredashboard']['coredashboard_statisticsApp_slbcount']);
                var mapper = {};
                var mapperServerCount = {};
                var mapperZone = {};
                var mapperProtocol = {
                    443: 'HTTPS'
                };

                _.mapObject(slbData, function (v, k, item) {
                    var server = v['slb-servers'][0]['ip'];
                    mapper[k] = server;
                    mapperServerCount[k] = v['slb-servers'].length;
                    if (!mapperZone[v['zone']]) mapperZone[v['zone']] = [];
                    mapperZone[v['zone']].push(k);
                });


                setTimeout(function () {
                    if (env == '生产') {
                        loadSlbReload(mapper);
                    } else {

                    }
                }, 2000);


                setTimeout(function () {
                    if (env == '生产') {
                        loadMemberOp()
                    } else {
                        loadMemberOpCat(cats[env]);
                    }
                }, 2000);


                setTimeout(function () {
                    loadQps(mapperServerCount)
                }, 2000);

                setTimeout(function () {
                    loadZoneQps(mapperZone)
                }, 2000);

                setTimeout(function () {
                    loadProtocolQps(mapperProtocol)
                }, 2000);

                setTimeout(function () {
                    loadIdcQps()
                }, 2000);

                setTimeout(function () {
                    load4xx5xx()
                }, 2000);

                $http(vsesQuery).success(function (response, code) {
                    if (code == 200) {
                        var vsesData = _.mapObject(_.indexBy(response['virtual-servers'], 'id'), function (v, k, item) {
                            _.map(v.properties, function (p) {
                                if (p['name']) {
                                    v[p['name']] = p['value'];
                                }
                            });

                            var slbZones = _.uniq(_.map(v['slb-ids'], function (s) {
                                var zone = slbData[s]['zone'];
                                if (!zone) {
                                    zone == '未知';
                                }
                                return zone;
                            }));

                            v['slb-zone'] = slbZones.sort().join(',');

                            v['domain-count'] = v['domains'].length;
                            return v;
                        });

                        var vsesResult = [];
                        var vsesByZone = [];
                        var domainsByZoneTemp = {};
                        var domainsByZone = [];
                        _.mapObject(_.countBy(vsesData, function (v) {
                            if (v['ssl']) return 'Https';
                            return 'Http';
                        }), function (v, k, item) {
                            vsesResult.push({
                                'name': k,
                                'y': v
                            });
                        });


                        _.mapObject(_.countBy(vsesData, function (vd) {
                            return vd['slb-zone']
                        }), function (v, k, item) {
                            vsesByZone.push({
                                'name': k,
                                'y': v
                            });
                        });


                        var domainMap = {};
                        var domainCount = 0;
                        _.map(_.values(vsesData), function (v) {
                            if (!domainsByZoneTemp[v['slb-zone']]) {
                                domainsByZoneTemp[v['slb-zone']] = 0;
                            }
                            var len = 0;
                            _.map(v.domains, function (vd) {
                                if (!domainMap[vd.name]) {
                                    len++;
                                    domainMap[vd.name] = vd.name;
                                }
                            });
                            domainCount += len;
                            domainsByZoneTemp[v['slb-zone']] += len;
                        });

                        _.mapObject(domainsByZoneTemp, function (v, k, item) {
                            domainsByZone.push({
                                'name': k,
                                'y': v
                            });
                        });

                        $scope.data.vses['ssl'] = _.countBy(vsesData, function (v) {
                            if (v['ssl']) return 'Https';
                            return 'Http';
                        });
                        $scope.data.vses['zone'] = domainsByZoneTemp;
                        $scope.data.vses['zoneCount'] = domainCount;
                        slbs(vsesResult, 'http-https', 'VS(Total:' + _.keys(vsesData).length + ')');
                        slbs(vsesByZone, 'zone-area', resource['coredashboard']['coredashboard_statisticsApp_vscount'] + '(Total:' + _.keys(vsesData).length + ')');
                        slbs(domainsByZone, 'domains', resource['coredashboard']['coredashboard_statisticsApp_domaincount'] + '(Total:' + count + ')');
                    }
                });

                $http(groupsQuery).success(function (response, code) {
                    if (code == 200) {
                        var groupsData = _.mapObject(_.indexBy(response['groups'], 'id'), function (v, k, item) {
                            _.map(v.properties, function (p) {
                                if (p['name']) {
                                    if (p['name'] == 'healthy') {
                                        v[p['name']] = healthyMapping[p['value']] || p['value'];
                                    } else {
                                        v[p['name']] = p['value'];
                                    }

                                }
                            });
                            return v;
                        });

                        var resultByHealthy = [];
                        _.mapObject(_.countBy(groupsData, 'healthy'), function (v, k, item) {
                            resultByHealthy.push({
                                name: k,
                                y: v
                            });
                        });

                        $scope.data.groups['healthy'] = _.countBy(groupsData, 'healthy');
                        $scope.data.groups['count'] = _.keys(groupsData).length;

                        slbs(resultByHealthy, 'group-area', resource['coredashboard']['coredashboard_statisticsApp_healthycount'] + '(Total:' + _.keys(groupsData).length + ')');
                    }
                });
            }
        });

        $http(appsQuery).success(function (response, code) {
            if (code == 200) {
                $scope.data.apps['count'] = response['total'];
            }
        });

        $http(statisticsQuery).success(function (response, code) {
            if (code == 200) {
                var statisticsData = _.indexBy(response['slb-metas'], 'slb-id');
                console.log(statisticsData);
            }
        });
    };

    $scope.getLastDayQps = function (v) {
        return Math.round((v / (1024 * 1024 * 1024)) * 1000) / 1000;
    };

    $scope.getTodayQps = function (v) {
        return Math.round((v / (1024 * 1024)) * 1000) / 1000;
    };

    $scope.getLastDay = function () {
        return $.format.date(new Date().getTime() - hourSeconds * 24, 'yyyy-MM-dd')
    };
    $scope.getToday = function () {
        return $.format.date(new Date().getTime(), 'yyyy-MM-dd')
    };
    var ctime = '';
    $scope.toggleTime = function (v) {
        var d = new Date(start_time);
        if (v == '当前') {
            d = new Date();
        }
        ctime = v;

        var today = $.format.date(d, 'yyyy-MM-dd 00:00:00');
        var factor = $scope.data.times[v];

        var n = new Date(today).getTime() + factor;
        if (n > new Date().getTime()) {
            alert('查询日期超过当前时间，不合法');
            return;
        }

        H.setData({start: n, end: n + hourSeconds * 24});
    };

    $scope.isSelectedTime = function (v) {
        if (ctime == v) {
            return "selected";
        }
    };

    function loadSlbReload(slbServerMapping) {

        var resource = $scope.resource;
        if (!resource || _.keys(resource).length == 0) return;

        var startTime = $scope.startTime;
        var endTime = $scope.endTime;

        var ips = _.map(_.values(slbServerMapping), function (v) {
            return '"' + v + '"';
        });

        var ipsrevert = _.invert(slbServerMapping);
        var params = {
            'metric-name': 'slb.method.req.count',
            'start-time': $.format.date(startTime, 'yyyy-MM-dd HH:mm'),
            'end-time': $.format.date(endTime, 'yyyy-MM-dd HH:mm'),
            'tags': '{"method":["reload"],"hostip":[' + ips.join(',') + ']}',
            'interval': '1h',
            'chart': 'line',
            'aggregator': 'sum',
            'downsampler': 'sum'
        };
        params['group-by'] = '[hostip]';

        var config = {
            'url': G[G.env].urls.dashboard + "/data",
            'method': 'GET',
            'params': params
        };
        config.withCredentials = false;

        $http(config).success(
            function (res, code) {
                // group by slb
                var result = {};
                if (code != 200) {
                    alert('Failed to get slb reload data');
                    return;
                }
                var data = res['time-series-group-list'];

                _.map(data, function (v) {
                    var ip = v['time-series-group']['hostip'];
                    var key = ipsrevert[ip];
                    if (key) result[key] = v['data-points']['data-points'];
                });

                result = _.mapObject(result, function (v, k, item) {
                    v = _.map(v, function (s) {
                        if (s == null || s == 'null') s = 0;
                        return s;
                    });
                    return v;
                });

                // total
                var times = _.reduce(_.flatten(_.values(result)), function (a, b) {
                    return a + b;
                });

                var keys = sortObjectKeys(result);
                var show = [];
                var len = 0;
                for (var i = 0; i < keys.length; i++) {
                    var v = keys[i];
                    len = result[v].length;
                    var ob = {
                        'name': v,
                        data: result[v],
                        type: 'column',
                        maxPointWidth: 30
                    };

                    if (i > 10) {
                        ob.visible = false;
                    }
                    show.push(ob);
                }
                var categories = [];
                var s = start_time;
                for (var i = 1; i < len + 1; i++) {
                    var step = i * hourSeconds;
                    categories.push($.format.date(new Date(s + step), 'HH:mm'));
                }
                column(show, 'reload', resource['coredashboard']['coredashboard_statisticsApp_reloadcount'] + '（' + 'total:' + times + '）', resource['coredashboard']['coredashboard_statisticsApp_eachreloadcount'], resource['coredashboard']['coredashboard_statisticsApp_count3'], categories, true)

            }
        );
    };

    function loadMemberOp() {
        var resource = $scope.resource;
        if (!resource || _.keys(resource).length == 0) return;

        var ops = [
            '/api/op/pullIn',
            '/api/op/pullOut',
            '/api/op/upMember',
            '/api/op/downMember',
            '/api/op/upServer',
            '/api/op/downServer',
            '/api/op/raise',
            '/api/op/fall',
        ];
        var opm = _.map(ops, function (v) {
            return '"' + v + '"';
        });
        var startTime = $scope.startTime;
        var endTime = $scope.endTime;

        var params = {
            'metric-name': 'slb.api.req.count',
            'start-time': $.format.date(startTime, 'yyyy-MM-dd HH:mm'),
            'end-time': $.format.date(endTime, 'yyyy-MM-dd HH:mm'),
            'tags': '{"uri":[' + opm.join(',') + ']}',
            'interval': '1h',
            'chart': 'line',
            'aggregator': 'sum',
            'downsampler': 'sum'
        };
        params['group-by'] = '[uri]';

        var config = {
            'url': G[G.env].urls.dashboard + "/data",
            'method': 'GET',
            'params': params
        };
        config.withCredentials = false;

        $http(config).success(
            function (res, code) {
                // group by slb
                var result = {};
                if (code != 200) {
                    alert('Failed to get slb reload data');
                    return;
                }
                var data = res['time-series-group-list'];

                _.map(data, function (v) {
                    var key = v['time-series-group']['uri'];
                    if (key) result[key] = v['data-points']['data-points'];
                });

                result = _.mapObject(result, function (v, k, item) {
                    v = _.map(v, function (s) {
                        if (s == null || s == 'null') s = 0;
                        return s;
                    });
                    return v;
                });

                // total
                var times = _.reduce(_.flatten(_.values(result)), function (a, b) {
                    return a + b;
                });

                var keys = sortObjectKeys(result);
                var show = [];
                var len = 0;
                for (var i = 0; i < keys.length; i++) {
                    var v = keys[i];
                    len = result[v].length;
                    var ob = {
                        'name': v,
                        data: result[v],
                        type: 'column',
                        maxPointWidth: 30
                    };

                    if (i > 10) {
                        ob.visible = false;
                    }
                    show.push(ob);
                }


                var categories = [];
                var s = start_time;
                for (var i = 1; i < len + 1; i++) {
                    var step = i * hourSeconds;
                    categories.push($.format.date(new Date(s + step), 'HH:mm'));
                }
                column(show, 'pull', resource['coredashboard']['coredashboard_statisticsApp_pullcount'] + '（' + times + '）', resource['coredashboard']['coredashboard_statisticsApp_pullcount2'], resource['coredashboard']['coredashboard_statisticsApp_count3'], categories, true)
            }
        );
    };

    function loadQps(mapperServerCount) {
        var resource = $scope.resource;
        if (!resource || _.keys(resource).length == 0) return;

        var startTime = $scope.startTime;
        var endTime = $scope.endTime;

        var params = {
            'metric-name': 'slb.req.count',
            'start-time': $.format.date(startTime, 'yyyy-MM-dd HH:mm'),
            'end-time': $.format.date(endTime, 'yyyy-MM-dd HH:mm'),
            'interval': '1h',
            'chart': 'line',
            'aggregator': 'sum',
            'downsampler': 'sum'
        };
        params['group-by'] = '[slb_id]';

        var config = {
            'url': G[G.env].urls.dashboard + "/data",
            'method': 'GET',
            'params': params
        };
        config.withCredentials = false;

        $http(config).success(
            function (res, code) {
                // group by slb
                var result = {};
                if (code != 200) {
                    alert('Failed to get slb reload data');
                    return;
                }
                var data = res['time-series-group-list'];

                _.map(data, function (v) {
                    var key = v['time-series-group']['slb_id'];
                    if (key) result[key] = v['data-points']['data-points'];
                });

                var max = 0;
                result = _.mapObject(result, function (v, k, item) {
                    var slbServerCount = mapperServerCount[k];

                    v = _.map(v, function (s) {
                        if (slbServerCount) {
                            if (s == null || s == 'null') {
                                s = 0;
                            } else {
                                s = Math.floor(s / (60 * 60 * slbServerCount));
                            }
                        } else {
                            s = 0;
                        }

                        return s;
                    });

                    return v;
                });

                // total


                var maxes = [];
                var vs = _.values(result);

                for (var i = 0; i < vs[0].length; i++) {
                    var f = [];
                    for (var j = 0; j < vs.length; j++) {
                        f.push(vs[j][i]);
                    }
                    max = Math.max.apply(null, f);
                    var maxIndex = f.indexOf(max);

                    maxes.push([maxIndex, max]);
                }


                var tmax = 0;
                var ttime = 0;
                _.map(maxes, function (v) {
                    if (v[1] > tmax) {
                        tmax = v[1];
                        ttime = v[0];
                    }
                });

                var keys = sortObjectKeys(result);
                var show = [];
                var len = 0;
                for (var i = 0; i < keys.length; i++) {
                    var v = keys[i];
                    len = result[v].length;
                    var ob = {
                        'name': v,
                        data: result[v]
                    };

                    if (i > 20) {
                        ob.visible = false;
                    }
                    show.push(ob);
                }

                var categories = [];
                var s = start_time;
                for (var i = 1; i < len + 1; i++) {
                    var step = i * hourSeconds;
                    categories.push($.format.date(new Date(s + step), 'HH:mm'));
                }

                line(show, 'qps', resource['coredashboard']['coredashboard_statisticsApp_eachqps'] + '（' + resource['coredashboard']['coredashboard_statisticsApp_maxqps'] + ':' + categories[ttime] + ', Max Value：' + Math.round((tmax / 1000) * 100) / 100 + 'K）', resource['coredashboard']['coredashboard_statisticsApp_maxqps2'], categories);
            }
        );
    }

    function loadZoneQps(mapper) {
        var resource = $scope.resource;
        if (!resource || _.keys(resource).length == 0) return;

        var startTime = $scope.startTime;
        var endTime = $scope.endTime;

        var params = {
            'metric-name': 'slb.req.count',
            'start-time': $.format.date(startTime, 'yyyy-MM-dd HH:mm'),
            'end-time': $.format.date(endTime, 'yyyy-MM-dd HH:mm'),
            'interval': '1h',
            'chart': 'line',
            'aggregator': 'sum',
            'downsampler': 'sum'
        };
        params['group-by'] = '[slb_id]';

        var config = {
            'url': G[G.env].urls.dashboard + "/data",
            'method': 'GET',
            'params': params
        };
        config.withCredentials = false;

        $http(config).success(
            function (res, code) {
                // group by slb
                var result = {};
                if (code != 200) {
                    alert('Failed to get slb reload data');
                    return;
                }
                var data = res['time-series-group-list'];

                _.map(data, function (v) {
                    var key = v['time-series-group']['slb_id'];
                    _.mapObject(mapper, function (mv, mk, mitem) {
                        if (mv.indexOf(key) != -1) {
                            if (!result[mk]) result[mk] = [];
                            result[mk].push(v['data-points']['data-points']);
                        }
                    });
                });

                result = _.mapObject(result, function (rv, rk, ritem) {
                    var f = [];
                    var vs = _.values(rv);

                    for (var i = 0; i < vs[0].length; i++) {
                        var r = 0;
                        for (var j = 0; j < vs.length; j++) {
                            r += vs[j][i];
                        }

                        f.push(r / (60 * 60));
                    }

                    rv = f;
                    return rv;
                });

                console.log(result);
                var show = [];
                var len = 0;
                _.mapObject(result, function (v, k, item) {
                    len = v.length;
                    show.push({
                        'name': k,
                        data: v
                    });
                });
                var categories = [];
                var s = start_time;
                for (var i = 1; i < len + 1; i++) {
                    var step = i * hourSeconds;
                    categories.push($.format.date(new Date(s + step), 'HH:mm'));
                }

                line(show, 'zone-qps', resource['coredashboard']['coredashboard_statisticsApp_zoneqps'], resource['coredashboard']['coredashboard_statisticsApp_eachzoneqps'], categories);
            }
        );
    }

    function loadProtocolQps(mapper) {
        var resource = $scope.resource;
        if (!resource || _.keys(resource).length == 0) return;

        var startTime = $scope.startTime;
        var endTime = $scope.endTime;

        var params = {
            'metric-name': 'slb.req.count',
            'start-time': $.format.date(startTime, 'yyyy-MM-dd HH:mm'),
            'end-time': $.format.date(endTime, 'yyyy-MM-dd HH:mm'),
            'interval': '1h',
            'chart': 'line',
            'aggregator': 'sum',
            'downsampler': 'sum'
        };
        params['group-by'] = '[port]';

        var config = {
            'url': G[G.env].urls.dashboard + "/data",
            'method': 'GET',
            'params': params
        };
        config.withCredentials = false;

        $http(config).success(
            function (res, code) {
                // group by slb
                var result = {};
                if (code != 200) {
                    alert('Failed to get slb reload data');
                    return;
                }
                var data = res['time-series-group-list'];

                _.map(data, function (v) {
                    var key = v['time-series-group']['port'];
                    _.mapObject(mapper, function (mv, mk, mitem) {
                        // if (mv.indexOf(key) != -1) {
                        //     if (!result[mk]) result[mk] = [];
                        //     result[mk].push(v['data-points']['data-points']);
                        // }

                        if (mk == key) {
                            if (!result[mv]) result[mv] = [];

                            result[mv].push(v['data-points']['data-points']);
                        } else {
                            if (!result['HTTP']) result['HTTP'] = [];
                            result['HTTP'].push(v['data-points']['data-points']);
                        }
                    });
                });

                result = _.mapObject(result, function (rv, rk, ritem) {
                    var f = [];
                    var vs = _.values(rv);

                    for (var i = 0; i < vs[0].length; i++) {
                        var r = 0;
                        for (var j = 0; j < vs.length; j++) {
                            r += vs[j][i];
                        }

                        f.push(r / (60 * 60));
                    }

                    rv = f;
                    return rv;
                });

                console.log(result);
                var show = [];
                var len = 0;
                _.mapObject(result, function (v, k, item) {
                    len = v.length;
                    show.push({
                        'name': k,
                        data: v
                    });
                });
                var categories = [];
                var s = start_time;
                for (var i = 1; i < len + 1; i++) {
                    var step = i * hourSeconds;
                    categories.push($.format.date(new Date(s + step), 'HH:mm'));
                }

                line(show, 'out-http-https-qps', resource['coredashboard']['coredashboard_statisticsApp_protocolqps'], resource['coredashboard']['coredashboard_statisticsApp_protocolqpsall'], categories);
            }
        );
    }

    function loadIdcQps() {
        var resource = $scope.resource;
        if (!resource || _.keys(resource).length == 0) return;

        var startTime = $scope.startTime;
        var endTime = $scope.endTime;

        var params = {
            'metric-name': 'slb.req.count',
            'start-time': $.format.date(startTime, 'yyyy-MM-dd HH:mm'),
            'end-time': $.format.date(endTime, 'yyyy-MM-dd HH:mm'),
            'interval': '1h',
            'chart': 'line',
            'aggregator': 'sum',
            'downsampler': 'sum'
        };
        params['group-by'] = '[idc]';

        var config = {
            'url': G[G.env].urls.dashboard + "/data",
            'method': 'GET',
            'params': params
        };
        config.withCredentials = false;

        $http(config).success(
            function (res, code) {
                // group by slb
                var result = {};
                if (code != 200) {
                    alert('Failed to get slb reload data');
                    return;
                }
                var data = res['time-series-group-list'];

                _.map(data, function (v) {
                    var key = v['time-series-group']['idc'];
                    if (!result[key]) result[key] = [];
                    result[key].push(v['data-points']['data-points']);
                });

                result = _.mapObject(result, function (rv, rk, ritem) {
                    var f = [];
                    var vs = _.values(rv);

                    for (var i = 0; i < vs[0].length; i++) {
                        var r = 0;
                        for (var j = 0; j < vs.length; j++) {
                            r += vs[j][i];
                        }

                        f.push(r / (60 * 60));
                    }

                    rv = f;
                    return rv;
                });

                var keys = sortObjectKeys(result);
                var show = [];
                var len = 0;
                for (var i = 0; i < keys.length; i++) {
                    var v = keys[i];
                    len = result[v].length;
                    var ob = {
                        'name': v,
                        data: result[v]
                    };

                    if (i > 20) {
                        ob.visible = false;
                    }
                    show.push(ob);
                }

                var categories = [];
                var s = start_time;
                for (var i = 1; i < len + 1; i++) {
                    var step = i * hourSeconds;
                    categories.push($.format.date(new Date(s + step), 'HH:mm'));
                }

                line(show, 'idc-qps', resource['coredashboard']['coredashboard_statisticsApp_idcqps'], resource['coredashboard']['coredashboard_statisticsApp_idcqpsall'], categories);
            }
        );
    }

    function loadLastRequestDay() {
        var params = {
            'metric-name': 'slb.req.count',
            'start-time': $.format.date(new Date().getTime() - hourSeconds * 24, 'yyyy-MM-dd 00:00'),
            'end-time': $.format.date(new Date().getTime(), 'yyyy-MM-dd 00:00'),
            'interval': '1d',
            'chart': 'line',
            'aggregator': 'sum',
            'downsampler': 'sum'
        };

        var config = {
            'url': G[G.env].urls.dashboard + "/data",
            'method': 'GET',
            'params': params
        };
        config.withCredentials = false;

        $http(config).success(
            function (res, code) {
                // group by slb
                var result = {};
                if (code != 200) {
                    alert('Failed to get slb reload data');
                    return;
                }
                var data = res['time-series-group-list'];
                console.log(data);
                var points = data[0]['data-points'];
                if (points) {
                    var ds = points['data-points'][0];
                    $scope.data.qps.total = ds;
                }
            }
        );
    }

    function loadMemberOpCat(cat) {
        var url = G['fws']['urls']['api'] + '/api/statistics/call/dashboard';
        var params = {
            op: 'history',
            ip: 'All',
            cat: cat,
            date: $.format.date(new Date().getTime() - hourSeconds * 24, 'yyyyMMdd'),
            reportType: 'day',
            type: 'URL',
            domain: '100000716',
            forceDownload: 'json'
        };

        var config = {
            'url': url,
            'method': 'GET',
            'params': params
        };
        config.withCredentials = false;

        $http(config).success(
            function (res, code) {
                var categories = [];

                var s = start_time;
                for (var i = 1; i < res['/api/op/pullOut'].length + 1; i++) {
                    var step = i * hourSeconds;
                    categories.push($.format.date(new Date(s + step), 'HH:mm'));
                }
                var show = [{
                    name: '/api/op/pullIn',
                    data: _.map(res['/api/op/pullIn'], function (v) {
                        return parseInt(v);
                    })
                },

                    {
                        name: '/api/op/pullOut',
                        data: _.map(res['/api/op/pullOut'], function (v) {
                            return parseInt(v);
                        })
                    }, {
                        name: '/api/op/raise',
                        data: _.map(res['/api/op/raise'], function (v) {
                            return parseInt(v);
                        })
                    }, {
                        name: '/api/op/raise',
                        data: _.map(res['/api/op/fall'], function (v) {
                            return parseInt(v);
                        })
                    }, {
                        name: '/api/op/raise',
                        data: _.map(res['/api/op/memberUp'], function (v) {
                            return parseInt(v);
                        })
                    }, {
                        name: '/api/op/raise',
                        data: _.map(res['/api/op/memberDown'], function (v) {
                            return parseInt(v);
                        })
                    }];
                column(show, 'pull', '单日拉入拉出次数', '当日拉入拉出次数', '次数', categories, true)
            }
        );
    }

    function loadLastRequestDayCat(cat) {
        var url = G['fws']['urls']['api'] + '/api/statistics/dist/dashboard';
        var params = {
            op: 'history',
            ip: 'All',
            cat: cat,
            date: $.format.date(new Date().getTime() - hourSeconds * 24, 'yyyyMMdd'),
            reportType: 'day',
            type: 'SLBReq.Dist',
            domain: '100000716',
            forceDownload: 'json'
        };

        var config = {
            'url': url,
            'method': 'GET',
            'params': params
        };
        config.withCredentials = false;

        $http(config).success(
            function (res, code) {
                var categories = [];
                var s = start_time;
                for (var i = 1; i < res['counts'].length + 1; i++) {
                    var step = i * hourSeconds;
                    categories.push($.format.date(new Date(s + step), 'HH:mm'));
                }
                var show = [{
                    name: '全部SLB',
                    data: _.map(res['counts'], function (v) {
                        return parseInt(v);
                    })
                }];
                $scope.data.qps.total = parseInt(res.total);
                line(show, 'all-qps', 'QPS', '', categories);
            }
        );
    }


    function loadTodayRequests() {
        var now = new Date();
        var hour = now.getHours();
        var minute = now.getMinutes();
        var second = now.getSeconds();

        var back = hour * 60 * 60 * 1000 + minute * 60 * 1000 + second * 1000;

        var lastdaybegining = new Date(now - back);
        var requests = [];
        var results = [];

        for (var i = 0; i < hour; i++) {
            var params = {
                'metric-name': 'slb.req.count',
                'start-time': $.format.date(lastdaybegining, 'yyyy-MM-dd HH:mm'),
                'end-time': $.format.date(new Date(lastdaybegining.getTime() + 60 * 60 * 1000), 'yyyy-MM-dd HH:mm'),
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
            config.withCredentials = false;

            lastdaybegining = new Date(lastdaybegining.getTime() + 60 * 60 * 1000);


            requests.push(
                $http(config).success(
                    function (res, code) {
                        var data = res['time-series-group-list'];
                        console.log(data);
                        if (code == 200) {
                            var points = data[0]['data-points'];
                            if (points) {
                                var max = Math.max.apply(null, points['data-points']);
                                var index = points['data-points'].indexOf(max);
                                var time = new Date(points['base-time']).getTime() + index * 60 * 1000;
                                results.push([$.format.date(time, 'yyyy-MM-dd HH:mm'), max]);
                                // get the max value
                            }
                        }
                    }
                ));
        }

        $q.all(requests).then(function () {
            console.log(results);

            var max = 0;
            var time;
            for (var i = 0; i < results.length; i++) {
                if (results[i][1] >= max) {
                    max = results[i][1];
                    time = results[i][0];
                }
            }
            $scope.data.todaymax = [time, max];
        });
    }

    function load4xx5xx() {
        var resource = $scope.resource;
        if (!resource || _.keys(resource).length == 0) return;

        var startTime = $scope.startTime;
        var endTime = $scope.endTime;

        var params = {
            'metric-name': 'slb.req.count',
            'start-time': $.format.date(startTime, 'yyyy-MM-dd HH:mm'),
            'end-time': $.format.date(endTime, 'yyyy-MM-dd HH:mm'),
            'tags': '{"status":["4*","5*"]}',
            'interval': '1h',
            'chart': 'line',
            'aggregator': 'sum',
            'downsampler': 'sum'
        };
        params['group-by'] = '[status]';

        var config = {
            'url': G[G.env].urls.dashboard + "/data",
            'method': 'GET',
            'params': params
        };
        config.withCredentials = false;

        $http(config).success(
            function (res, code) {
                // group by slb
                var result = {};
                if (code != 200) {
                    alert('Failed to get slb reload data');
                    return;
                }
                var data = res['time-series-group-list'];

                _.map(data, function (v) {
                    var key = v['time-series-group']['status'];
                    if (!result[key]) result[key] = [];
                    result[key].push(v['data-points']['data-points']);
                });

                result = _.mapObject(result, function (v, k, item) {
                    v = _.map(v, function (s) {
                        if (s == null || s == 'null') s = 0;
                        return s;
                    });
                    return v;
                });


                var show = [];
                var len = _.values(result)[0][0].length;
                _.mapObject(result, function (v, k, item) {
                    show.push({
                        'name': k,
                        data: v[0]
                    });
                });

                var categories = [];
                var s = start_time;
                for (var i = 1; i < len + 1; i++) {
                    var step = i * hourSeconds;
                    categories.push($.format.date(new Date(s + step), 'HH:mm'));
                }
                line(show, '4xx-5xx-qps', resource['coredashboard']['coredashboard_statisticsApp_4xx'], resource['coredashboard']['coredashboard_statisticsApp_4xxall'], categories);
            }
        );
    }

                noData: "No Data"
    function column(data, area, title, subTitle, ytitle, cs, label) {
        var loading = {
            credits: {
                enabled: false
            },
            title: title
        };
        Highcharts.chart(area, loading);
        var chart = {
            chart: {
                type: 'column'
            },
            title: {
                text: title,
                style: {
                    fontSize: '15px',
                    color: '#FF00FF',
                    fontWeight: 'bold'
                }
            },
            plotOptions: {
                series: {
                    cursor: 'pointer',
                    dataLabels: {
                        enabled: label,
                        formatter: function () {
                            if (this.y == 0) return '';
                            return this.y;
                        },
                        crop: false,
                        overflow: 'none',
                        style: {
                            color: 'black',
                            fontSize: '15px'
                        }
                    }
                },
                column: {
                    borderWidth: 0,
                    depth: 25
                }
            },
            subtitle: {
                text: subTitle
            },
            lang: {
                noData: "No Data"
            },
            credits: {
                enabled: false
            },

            xAxis: {
                categories: cs
            },
            yAxis: {
                min: 0,
                title: {
                    text: ytitle
                }
            },

            series: data
        };
        Highcharts.chart(area, chart)
    };

    function line(data, area, title, subtitle, cs) {
        var loading = {
            credits: {
                enabled: false
            },
            title: title
        };
        Highcharts.chart(area, loading);
        var chart = {
            title: {
                text: title
            },
            subtitle: {
                text: subtitle
            },
            yAxis: {
                title: {
                    text: '请求数'
                }
            },
            legend: {
                layout: 'horizontal'
            },
            tooltip: {
                shared: true,
                valueDecimals: 2,
                pointFormatter: function () {
                    return this.series.name + ' : <b>' + (Math.round(this.y / 1000 * 100) / 100) + ' K</b><br/>';
                }
            },
            xAxis: {
                categories: cs
            },
            credits: {
                enabled: false
            },
            series: data,
            responsive: {
                rules: [{
                    condition: {
                        maxWidth: 500
                    },
                    chartOptions: {
                        legend: {
                            layout: 'horizontal',
                            align: 'center',
                            verticalAlign: 'bottom'
                        }
                    }
                }]
            }
        };
        Highcharts.chart(area, chart)
    }

    function sortObjectKeys(object) {
        var sortable = _.pairs(object);

        sortable.sort(function (a, b) {
            var v1 = _.reduce(a[1], function (m, n) {
                return m + n;
            }, 0);
            var v2 = _.reduce(b[1], function (m, n) {
                return m + n;
            }, 0);
            return v2 - v1;
        });


        var result = [];
        _.map(sortable, function (s) {
            result.push(s[0]);
        });
        return result;
    }

    var start_time;
    var end_time;

    $scope.hashChanged = function (hashData) {
        $scope.resource = H.resource;
        start_time = hashData.start;
        end_time = hashData.end;
        $scope.env = hashData.env || 'pro';
        $scope.query.env = hashData.cenv || '生产';

        var d = new Date();
        var s = start_time ? d.setTime(start_time) : d.setTime(new Date($.format.date(new Date(), 'yyyy-MM-dd 00:00:00')).getTime());
        var e = end_time ? d.setTime(end_time) : d.setTime(new Date($.format.date(new Date().getTime() + hourSeconds * 24, 'yyyy-MM-dd 00:00:00')).getTime());

        start_time = s;
        end_time = e;

        $scope.startTime = $.format.date(s, 'yyyy-MM-dd HH:mm:00');
        $scope.endTime = $.format.date(e, 'yyyy-MM-dd HH:mm:00');

        $scope.loadData(hashData);
    };
    H.addListener("statisticsApp", $scope, $scope.hashChanged);
});
angular.bootstrap(document.getElementById("statistics-area"), ['statisticsApp']);
