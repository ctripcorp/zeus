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
            case 'basic': {
                link = "/portal/statistics#?env=" + G.env;
                break;
            }
            case 'pie': {
                link = "/portal/statistics/charts#?env=" + G.env;
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

//ReleaseComponent: Release
var releaseApp = angular.module('releaseApp', ['ui.bootstrap', 'http-auth-interceptor']);
releaseApp.controller('releaseController', function ($scope, $http, $filter) {
    $scope.dataLoaded = false;
    $scope.data = {
        confsInfo: [],
        commits: {}
    };

    //Type Info Page Switcher
    $scope.infoTypes = {
        currentIndex: 0,
        current: 'NginxConf',
        links: ['NginxConf', 'ReleaseVersion']
    };
    $scope.isCurrentInfoPage = function (link) {
        return $scope.infoTypes.current == link ? 'current' : '';
    };
    $scope.setInfoType = function (t) {
        H.setData({infoType: t});
    };
    $scope.showInfo = function (type) {
        return type == $scope.infoTypes.current;
    };
    $scope.infoTypesHashChanged = function (hashData) {
        var t = hashData.infoType;
        var tls = $scope.infoTypes.links;
        if (!_.contains(tls, t)) {
            $scope.setInfoType($scope.infoTypes.current);
            return;
        } else {
            $scope.infoTypes.current = t;
            $scope.infoTypes.currentIndex = _.indexOf(tls, t);
        }
    };
    //Type Info Page Switcher Over

    //Build Data Modal
    $scope.dataLoaded = false;
    $scope.env = '';
    var nginxLoaded=false;
    var releaseLoaded=false;

    $scope.data = {
        confsInfo: [],
        commits: {}
    };
    $scope.loadData = function (hashData) {
        $scope.env = hashData.env;

        var type = hashData.infoType;

        type = type ? type.toLowerCase() : 'nginxconf';

        if (type == 'nginxconf' && nginxLoaded==false) {
            nginxLoaded=true;
            $('.loading-area').showLoading();
            $http.get(G.baseUrl + "/api/admin/release/confinfos").success(function (res) {
                if (res.code) {
                    exceptionNotify("出错了!!", "加载Release信息失败了， 失败原因" + res.message, null);
                    return;
                } else {
                    setTimeout(
                        function () {
                            $('.alert-danger').remove();
                        },
                        1000
                    );
                }
                var confsInfo = _.map(res['slbs-info'], function (item) {
                    var version = item.version;
                    var innerslb = _.map(item['slb-servers-info'], function (v) {
                        var sick = (v.version != version);
                        v.sick = sick;
                        return v;
                    });
                    item['slb-servers-info'] = innerslb;

                    return item;
                });

                $('.loading-area').hideLoading();
                $scope.data.confsInfo = confsInfo;
            });
        }

        if (type == 'releaseversion' && releaseLoaded==false) {
            releaseLoaded=true;
            $('.loading-area').showLoading();
            $http.get(G.baseUrl + "/api/slbs").success(function (res) {
                var slbData = _.map(res['slbs'], function (v) {
                    return {
                        id: v.id,
                        name: v.name,
                        'slb-servers-info':v['slb-servers']
                    };
                });


                var servers = _.flatten(_.pluck(res['slbs'], 'slb-servers'));
                var slbservers = _.map(servers, function (item) {
                    var ip = item['ip'];
                    var protocol = 'http';
                    var port = '8099';
                    var uri = '/api/admin/release/warinfo';

                    return {
                        "protocol": protocol,
                        "port": port,
                        "ip": ip,
                        "uri": uri
                    }
                });

                var request = {
                    method: 'POST',
                    url: G.baseUrl + '/api/tools/check/slbs/release?timeout=600',
                    data: {
                        'targets': slbservers
                    }
                };

                var slbCommit = {};
                $http(request).success(function (response, code) {
                    $('.loading-area').hideLoading();

                    if (code == 200) {
                        $scope.data.commits = _.countBy(response.statuses, function (v) {
                            return v.message || 'unknown';
                        });

                        slbCommit = _.indexBy(response.statuses, function (t) {
                            return t.ip;
                        });


                        $.each(slbData, function (i, t) {
                            var c = t['slb-servers-info'];
                            $.each(c, function (j, v) {
                                v.commitId = slbCommit[v.ip]?slbCommit[v.ip].message:'unknown';
                            });
                        });

                        $scope.data.commitInfo = slbData;
                    }
                });
            });
        }
    };

    //NginxConf
    $scope.generateSLBLink = function (slb) {
        return '/portal/slb#?env=' + G.env + '&slbId=' + slb.id;
    };
    $scope.slbConfVersionClass = function (conf) {
        return conf.sick ? 'status-red' : 'status-gray';
    };
    $scope.serverConfStatusClass = function (conf) {
        return conf.sick ? 'fa fa-circle status-red' : 'fa fa-circle status-green';
    };
    $scope.serverConfVersionClass = function (conf) {
        return conf.sick ? 'sick-server-conf-version status-red' : 'server-conf-version status-green';
    };
    //NginxConf --Over

    //CommitVersion
    $scope.generateCommitLink = function (commit) {
        return "http://git/framework/slb/commit/" + commit;
    };
    var colors = {};
    $scope.commitVersionStyle = function (commit) {
        if (!commit) return;
        var color = colors[commit];
        var r = Math.floor((Math.random() * 30) + 1);
        var c = commit.substring(0, r);
        if (!color) {
            color = U.string2RGB(c);
            colors[commit] = color;
        }
        return {'color': '#' + color};
    };
    //CommitVersion --Over
    $scope.hashChanged = function (hashData) { $scope.resource = H.resource;
        $scope.loadData(hashData);
        $scope.infoTypesHashChanged(hashData);
    };
    H.addListener("releaseApp", $scope, $scope.hashChanged);

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
                align: 'right'
            },
            offset: {
                x: 0,
                y: 50
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
angular.bootstrap(document.getElementById("release-area"), ['releaseApp']);
