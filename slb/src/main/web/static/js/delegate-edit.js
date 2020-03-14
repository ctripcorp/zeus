var groupEditApp = angular.module('groupEditApp', ["angucomplete-alt", 'http-auth-interceptor']);
groupEditApp.controller('groupEditController', function ($scope, $http, $q) {
    $scope.query = {
        'selectedslbids': {}
    };
    $scope.data = {
        'group': {
            "properties": [],
            "group-servers": [],
            "ssl": false,
            "app-id": "999999",
            "group-virtual-servers": [{
                "path": "",
                "virtual-server": {
                    "id": ''
                },
                "priority": ''
            }],
            "health-check": {
                "timeout": 3000,
                "uri": "",
                "intervals": 5000,
                "fails": 3,
                "passes": 1
            },

            "load-balancing-method": {
                "type": "roundrobin",
                "value": "default"
            },
            "name": ""
        },
        "slbs": "",
        "vs": ""
    };

    $scope.loadData = function () {
        var vsId = $scope.query.vsId;
        var groupId = $scope.query.groupId;

        var vsrequest = {
            method: "GET",
            url: G.baseUrl + '/api/vs?vsId=' + vsId + "&type=extended"
        };

        var slbsRequest = {
            method: 'GET',
            url: G['pro']['urls']['api'] + '/api/slbs?type=extended'
        };

        var allslbsrequest = {
            method: 'GET',
            url: G['pro']['urls']['api'] + '/api/slbs?type=extended'
        };

        var grouprequest = {
            method: 'GET',
            url: G.baseUrl + '/api/group?type=extended'
        };

        if (groupId) {
            grouprequest.url += '&groupId=' + groupId;

            $http(grouprequest).success(function (response, code) {
                if (code == 200) {
                    $scope.data.group = response;
                    // queries
                    $scope.query.vsId = response['group-virtual-servers'][0]['virtual-server'].id;

                    var slbps = _.filter(response['properties'], function (v) {
                        return v['name'] == 'slbs';
                    });

                    var ps = {};
                    _.map(slbps[0]['value'].split(','), function (v) {
                        ps[v] = v;
                    });

                    $scope.query.selectedslbids = ps;
                }
            });
        }

        $http(vsrequest).success(function (response, code) {
            if (code == 200) {
                $scope.data.vs = response;
                var domains = _.pluck(response['domains'], 'name');
                var domainstext = _.map(domains, function (v) {
                    return 'domain=' + v;
                }).join('&');

                slbsRequest.url += "&" + domainstext;

                $http(slbsRequest).success(function (response2, code2) {
                    if (code2 == 200) {
                        var slbs = [];
                        slbs = response2.total > 0 ? response2['slbs'] : [];
                        $scope.data.slbs = _.map(slbs, function (s) {
                            var ps = _.indexBy(s['properties'], 'name');
                            s.idc = ps['idc'] ? ps['idc']['value'] : '未知';
                            s.zone = ps['zone'] ? ps['zone']['value'] : '未知';
                            s.vips = _.pluck(s['vips'], 'ip');
                            return s;
                        });
                        if (slbs.length == 0) {
                            // get all the slbs
                            $http(allslbsrequest).success(function (response3, code3) {
                                if (code3 == 200) {
                                    slbs = response3.total > 0 ? response3['slbs'] : [];
                                    $scope.data.slbs = _.map(slbs, function (s) {
                                        var ps = _.indexBy(s['properties'], 'name');
                                        s.idc = ps['idc'] ? ps['idc']['value'] : '未知';
                                        s.zone = ps['zone'] ? ps['zone']['value'] : '未知';
                                        s.vips = _.pluck(s['vips'], 'ip');
                                        return s;
                                    });
                                }
                            });
                        }
                    }
                });
            }
        });
    };

    $scope.getDomains = function () {
        var vs = $scope.data.vs;
        if (!vs) return;

        return _.pluck(vs['domains'], 'name').join('/');
    };

    $scope.toggleSlb = function (slbId) {
        if ($scope.query.selectedslbids[slbId]) {
            delete $scope.query.selectedslbids[slbId];
        } else {
            $scope.query.selectedslbids[slbId] = slbId;
        }
    };

    $scope.removeSlb = function (slbId) {
        delete  $scope.query.selectedslbids[slbId];
    };

    $scope.getTileClass = function (slbId) {
        return $scope.query.selectedslbids[slbId] ? 'tile-selected' : '';
    };

    $scope.getSlbInfo = function (v) {
        var slbs = $scope.data.slbs;
        if (slbs && slbs.length > 0) {
            var slb = _.filter(slbs, function (s) {
                return s['id'] == v;
            });
            if (slb && slb.length > 0) {
                return v + '(' + slb[0]['idc'] + '/' + slb[0]['zone'] + ')';
            }
        }
    };

    $scope.disableSaving = function () {
        var name = $scope.data.group.name;
        var slbs = $scope.query.selectedslbids;

        if (!name) return true;

        var l = slbs && _.keys(slbs).length > 0;
        if (!l) return true;

        return false;
    };

    $scope.saveGroup = function () {
        var slbids = _.keys($scope.query.selectedslbids);
        var slbs = _.indexBy($scope.data.slbs, 'id');

        var slectedslbs = _.map(slbids, function (v) {
            v = parseInt(v);
            var vips = slbs[v]['vips'];
            return _.map(vips, function (s) {
                return {
                    "port": 80,
                    "ip": s,
                    "host-name": s,
                    "weight": 5,
                    "max-fails": 0,
                    "fail-timeout": 30
                }
            });
        });

        var upstreams = _.flatten(slectedslbs);
        $scope.data.group['group-servers'] = upstreams;

        var vsId = $scope.query.vsId;
        $scope.data.group['group-virtual-servers'][0]['virtual-server'].id = vsId;
        $scope.data.group['group-virtual-servers'][0].path = '~* ^/';
        $scope.data.group['properties'] = _.reject($scope.data.group['properties'], function (v) {
            return v['name'] == 'slbs';
        });

        $scope.data.group['properties'].push({
            "name": "slbs",
            "value": slbids.join(',')
        });

        var request = {
            method: 'POST',
            url: G.baseUrl + '/api/group/delegate/new',
            data: $scope.data.group
        };

        $http(request).success(function (response, code) {
            if (code == 200) {
                $scope.query.groupId = response.id;

                // pull in the members
                var pullRequest = {
                    method: 'GET',
                    url: G.baseUrl + '/api/op/pullIn?groupId=' + response.id + "&batch=true"
                };
                $http(pullRequest).success(function (response2, code2) {
                    if (code2 == 200) {
                        $scope.query.saveresult = "Successfully create the group and pull in servers. Click ok to configure traffic!";
                        $('#operationConfrimModel').modal('show');
                    } else {
                        alert('Failed to pullin group members. error message:' + response2.message)
                    }
                });
            } else {
                alert('Failed to save group data. error message:' + response.message)
            }
        });
    };

    $scope.confirmSave = function () {
        $('#operationConfrimModel').modal('hide');
        H.setData({timeStamp: new Date().getTime()});
    }

    $scope.redirectToConfig = function () {
        var groupId = $scope.query.groupId;
        var env = $scope.env;
        window.open("/portal/group/delegate-rule#?groupId=" + groupId + "&env=" + env, '_blank');
    };

    $scope.hashChanged = function (hashData) {
        $scope.resource = H.resource;
        if (hashData.env) {
            $scope.env = hashData.env;
        }
        if (hashData.vsId) {
            $scope.query.vsId = hashData.vsId;
        }

        if (hashData.groupId) {
            $scope.query.groupId = hashData.groupId;
        }

        $scope.loadData();
    };

    H.addListener("groupEditApp", $scope, $scope.hashChanged);
});
groupEditApp.filter('slbFilter', function () {
    return function (input, param1) {
        if (!param1 || param1.trim() == "") {
            return input;
        }

        var values = _.filter(input, function (v) {
            return v['id'] == param1;
        });

        return values;
    }
});
angular.bootstrap(document.getElementById("group-edit-area"), ['groupEditApp']);