﻿var headerInfoApp = angular.module('headerInfoApp', ["angucomplete-alt"]);
headerInfoApp.controller('headerInfoController', function ($scope) {
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
            case 'cert-upgrade': {
                link = "/portal/tools/cert/migrations#?env=" + G.env;
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

            default:
                break;
        }
        return link;
    }
    $scope.hashChanged = function (hashData) {
        if (hashData.env) {
            $scope.env = hashData.env;
        }
    };
    H.addListener("headerInfoApp", $scope, $scope.hashChanged);
});
angular.bootstrap(document.getElementById("header-area"), ['headerInfoApp']);

var selfInfoApp = angular.module('selfInfoApp', ["angucomplete-alt", "http-auth-interceptor", "ngSanitize"]);
selfInfoApp.controller('selfInfoController', function ($scope, $http, $q) {
    var log_start = new Date().getTime() - 1000 * 60 * 60;
    var log_end = new Date().getTime() + 1000 * 60 * 60;
    var dashboardStartTime = $.format.date(log_start, 'yyyy-MM-dd HH:mm:ss');
    var dashboardEndTime = $.format.date(log_end, 'yyyy-MM-dd HH:mm:ss');
    var nav = {
        'New': '新建',
        'Activate': '灰度Virtual Server',
        'Test': '测试',
        'Done': '已完成'
    };
    var keys = {
        'New': 'New',
        'Activate': 'Activate',
        'Test': 'Test',
        'Done': 'Done'
    };
    var stepOneKey = keys['New'];
    var stepTwoKey = keys['Activate'];
    var stepThreeKey = keys['Test'];
    var stepFourKey = keys['Done'];

    var toBeCanaryStatus = "toBeCanary";
    var isInCanaryStatus = "canarySuccess";
    var canaryDoneStatus = "canaryDone";

    $scope.data = {
        nav: nav,
        steps: {},
        certs: {}
    };
    $scope.query = {
        upgradeId: ''
    };
    $scope.certdog = {
        uploadCanarySuccess: '',
        allUploadCanarySuccess: '',
        canarySuccess: '',
        canaryAllSuccess: '',

        certName: ''
    };
    $scope.stepClass = function (v, index) {
        var steps = $scope.data.steps;
        if (!steps || _.keys(steps).length == 0) return;

        if (!steps[stepOneKey] && index == 0) {
            return 'btn activate-nav';
        }
        var values = _.keys(steps);

        // if steps has the value
        var hasStep = values.indexOf(v) != -1;
        if (hasStep) return 'btn activate-nav';
        return 'btn diable-nav';
    };

    $scope.showOldIdError = function (id) {
        return validateId(id);
    };

    $scope.showNewIdError = function (id) {
        return validateId(id);
    };

    $scope.disableFirstToSecond = function () {
        var steps = $scope.data.steps;

        if (steps && steps[stepOneKey]) {
            var oldId = steps[stepOneKey]['old-id'];
            var newId = steps[stepOneKey]['new-id'];

            if (validateId(oldId) || validateId(newId)) return true;
        }

        return false;
    };

    $scope.firstToSecond = function () {
        var oldId = $scope.data.steps[stepOneKey]['old-id'];
        var newId = $scope.data.steps[stepOneKey]['new-id'];
        var cs=[];
        var certsRequest1 = {
            method: 'GET',
            url: G.baseUrl+'/api/cert/certs/get?certId='+oldId
        };

        var certsRequest2 = {
            method: 'GET',
            url: G.baseUrl+'/api/cert/certs/get?certId='+newId
        };


        var certsQuery1 = $http(certsRequest1).success(function (response, code) {
            if (code == 200) {
                cs.push(response)
                //$scope.data.certs = _.indexBy(response, 'id');
            }
        });

        var certsQuery2 = $http(certsRequest2).success(function (response, code) {
            if (code == 200) {
                cs.push(response)
                //$scope.data.certs = _.indexBy(response, 'id');
            }
        });

        $q.all([certsQuery1,certsQuery2]).then(function (response, date) {
            if(cs.length!=2){
                alert('新证书或者旧证书查找不到，请确保ID是存在的证书ID');
                return;
            }
            $scope.data.certs = _.indexBy(cs, 'id');

            var vses = $scope.data.vses;

            var oldId = $scope.data.steps[stepOneKey]['old-id'];
            var newId = $scope.data.steps[stepOneKey]['new-id'];
            if (!oldId || !newId) return;

            // use cid to get vses
            var param = {
                certId: oldId
            };

            var request = {
                url: G.baseUrl+'/api/cert/query/vs',
                method: 'GET',
                params: param
            };

            var data = $scope.data.steps;
            data[stepTwoKey] = {};

            $http(request).success(function (response, code) {
                if (code == 200) {
                    certVses["old"] = _.map(response[oldId], function (v) {
                        return {
                            id: v,
                            name: vses[v]['name']
                        };
                    });
                    certVses["new"] = _.map(response[newId], function (v) {
                        return {
                            id: v,
                            name: vses[v]['name']
                        };
                    });

                    _.map(response[oldId], function (v) {
                        data[stepTwoKey][v] = {
                            "status": toBeCanaryStatus
                        }
                    });
                }


                $('#virtualServersTable').bootstrapTable("load", _.flatten(_.values(certVses)));
                createUpgrade(data);
            });
        })
    };

    $scope.showSecondStep = function () {
        var steps = $scope.data.steps;
        if (!steps || _.keys(steps).length == 0) return false;

        return steps['status'] == stepTwoKey || steps['status'] == stepThreeKey || steps['status'] == stepFourKey;
    };

    $('#virtualServersTable').on('check.bs.table uncheck.bs.table ' +
        'check-all.bs.table uncheck-all.bs.table', function () {
        $('#batchCanary').prop('disabled', !$('#virtualServersTable').bootstrapTable('getSelections').length);
        $('#batchActive').prop('disabled', !$('#virtualServersTable').bootstrapTable('getSelections').length);
    });

    $scope.startCanary = function (percentage) {
        var vsId = selectedVsId;
        var cid = selectedCid;
        var upgradeId = $scope.query.upgradeId;
        var steps = $scope.data.steps;
        canaryCertificateFunc(vsId, cid, percentage, function (ips) {
            // save current vs has been canary
            steps[stepTwoKey][vsId] = {
                "status": isInCanaryStatus,
                "canaryIps": ips
            };
            updateUpgrade(upgradeId, steps, stepTwoKey);
            // reload
        }, function (ips) {
            // canary failed
            // alert('Failed to canary certificate');
            steps[stepTwoKey][vsId] = {
                "status": isInCanaryStatus,
                "canaryIps": ips
            };
            updateUpgrade(upgradeId, steps, stepTwoKey);
        });
    };

    $scope.batchCanary = function () {
        var vsId = selectedVsId;
        var certId = selectedCid;

        canaryCertificateFunc(vsId, certId, 100, function (canaryIps) {
            $scope.data.steps[stepTwoKey][vsId]['canaryIps'] = canaryIps;
            $scope.canaryAllCert(vsId);
        }, function (message) {
            alert('批量更新证书失败，错误信息:' + message);
        }, true);
    };

    $scope.canaryAllCert = function (vsId) {
        var upgradeId = $scope.data.steps.id;

        activateSlb(function () {
            $scope.data.steps[stepTwoKey][vsId]['status'] = canaryDoneStatus;
            // update
            updateUpgrade(upgradeId, $scope.data.steps, stepTwoKey);
        }, function (code) {
            alert('批量生效证书失败! 错误码: ' + code);
        }, vsId);
    };

    $scope.batchUpgrade = function () {

    };

    $scope.batchActivate = function () {

    };

    $scope.showSecondToThird = function () {
        var upgrade = $scope.data.steps;
        if (!upgrade || _.keys(upgrade).length == 0) return false;

        var status = upgrade.status;
        if(status!=stepTwoKey) return false;
        var show = true;
        _.mapObject(upgrade[stepTwoKey], function (v, k, item) {
            if (v['status'] == canaryDoneStatus) {
                return v;
            } else {
                show = false;
            }
        });


        return show;
    };

    $scope.secondToThird = function () {
        // update status to be done
        var steps = $scope.data.steps;
        steps[stepThreeKey] = true;

        updateUpgrade(steps.id, steps, stepThreeKey);
    };

    $scope.showThirdStep = function () {
        var steps = $scope.data.steps;
        if (!steps) return false;

        return steps[stepThreeKey] && !steps[stepFourKey];
    };

    $scope.showForthStep = function () {
        var steps = $scope.data.steps;
        if (!steps) return false;

        return steps[stepFourKey];
    };




    $scope.completeClick = function () {
        var steps = $scope.data.steps;
        steps[stepFourKey] = true;

        updateUpgrade(steps.id, steps, 'Done');
    };

    function validateId(id) {
        if (id == undefined || id == '') return true;
        id = id.trim();
        try {
            if (isNaN(parseInt(id)) || parseInt(id) <= 0) return true;
        } catch (e) {
            return true;
        }
    }

    function activateSlb(next, fail, vsId) {
        var vses = $scope.data.vses;
        var slbIds = vses[vsId]['slb-ids'];

        var url = G.baseUrl+'/api/activate/slb';

        $.each(slbIds, function (i, v) {
            if (i == 0) {
                url += '?slbId=' + v;
            } else {
                url += '&slbId=' + v
            }
        });

        var request = {
            method: 'GET',
            url: url
        };

        $('#uploadCertHead').showLoading();

        $http(request).success(function (response, code) {
            $('#uploadCertHead').hideLoading();
            if (code == 200) {
                next();
            } else {
                fail(response.message);
            }
        });
    };


    // 创建新的upgrade
    function createUpgrade(data) {
        if (!data) {
            alert('Failed to save upgrade');
            return;
        }
        var request = {
            method: 'POST',
            url: G.baseUrl+'/api/tools/cert/upgrade/new',
            data: {
                'name': 'cert-upgrade-' + new Date().getTime(),
                'status': 'Activate',
                'content': JSON.stringify(data)
            }
        };
        $http(request).success(function (response, code) {
            if (code == 200) {
                // reload page
                H.setData({
                    upgradeId: response.id,
                    timeStamp: new Date().getTime()
                });
            }
        });
    };

    function updateUpgrade(id, data, status) {
        if (!data) {
            alert('Failed to save upgrade');
            return;
        }

        var request = {
            method: 'POST',
            url: G.baseUrl+'/api/tools/cert/upgrade/update',
            data: {
                'id': id,
                'status': status,
                'content': JSON.stringify(data)
            }
        };
        $http(request).success(function (response, code) {
            if (code != 200) {
                alert('Failed to update cert upgrade');
                return;
            } else {
                H.setData({
                    upgradeId: response.id,
                    timeStamp: new Date().getTime()
                });
            }
        });
    };


    var statusTextMapping = {
        'tobeactivated': '有变更',
        'activated': '已激活',
        'deactivated': '未激活'
    };

    var statusClassMapping = {
        'tobeactivated': 'status-yellow',
        'activated': 'status-green',
        'deactivated': 'status-red'
    };

    function getStatusText(status) {
        return statusTextMapping[status];
    }

    function getStatusClass(status) {
        return statusClassMapping[status];
    }

    var certVses = {};
    $scope.loadData = function (id) {
        var vsesRequest = {
            method: 'GET',
            url: G.baseUrl+'/api/vses?type=extended'
        };
        var slbsRequest = {
            method: 'GET',
            url: G.baseUrl+'/api/slbs?type=extended'
        };

        var vsesQuery = $http(vsesRequest).success(function (response, code) {
            if (code == 200) {
                $scope.data.vses = _.indexBy(response['virtual-servers'], 'id');
            }
        });

        var slbsQuery = $http(slbsRequest).success(function (response, code) {
            if (code == 200) {
                $scope.data.slbs = _.indexBy(response['slbs'], 'id');
            }
        });


        var certId = $scope.query.certId;

        $q.all([vsesQuery, slbsQuery]).then(function () {
            var vses = $scope.data.vses;

            if (!id) {
                $scope.data.steps = {};
                $scope.data.steps[stepOneKey] = {
                    'old-id': certId,
                    'new-id': ''
                };
            } else {
                var request = {
                    method: 'GET',
                    url: G.baseUrl+'/api/tools/cert/upgrade?upgradeId=' + id
                };
                $http(request).success(function (data) {
                    var steps = JSON.parse(data.content);
                    $scope.data.steps = steps;
                    $scope.data.steps.id = data.id;
                    $scope.data.steps.name = data.name;
                    $scope.data.steps.status = data.status;

                    var cIdOld = $scope.data.steps[stepOneKey]['old-id'];
                    var cIdNew = $scope.data.steps[stepOneKey]['new-id'];

                    // 用了老的Cert的VS
                    var url = G.baseUrl+'/api/cert/query/vs?certId=' + cIdOld + "&certId=" + cIdNew;
                    var request2 = {
                        method: 'GET',
                        url: url
                    };
                    $http(request2).success(function (response, code) {
                        if (code == 200) {
                            certVses['old'] = _.map(response[cIdOld], function (v) {
                                return {
                                    id: v,
                                    name: vses[v]['name']
                                };
                            });

                            certVses['new'] = _.map(response[cIdNew], function (v) {
                                return {
                                    id: v,
                                    name: vses[v]['name']
                                };
                            });

                            $('#virtualServersTable').bootstrapTable("load", _.flatten(_.values(certVses)));
                        }
                    });
                });
            }
        });
    };

    function initTable() {
        $('#virtualServersTable').bootstrapTable({
            toolbar: "#vsToolBar",
            columns: [[
                {
                    field: 'state',
                    checkbox: 'true',
                    align: 'center',
                    valign: 'middle'
                },
                {
                    field: 'id',
                    title: 'ID',
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    formatter: function (value, row, index) {
                        return '<a href="/portal/vs#?vsId=' + row.id + '">' + value + '</a>';
                    }
                },
                {
                    field: 'name',
                    title: 'Name',
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    formatter: function (value, row, index) {
                        return '<a href="/portal/vs#?vsId=' + row.id + '">' + value + '</a>';
                    }
                },

                // {
                //     field: 'status',
                //     title: 'Status',
                //     align: 'left',
                //     valign: 'middle',
                //     width: '200px',
                //     sortable: true,
                //     formatter: function (value, row, index) {
                //         var v = getStatusText(value);
                //         var c = getStatusClass(value);
                //         return '<span class="' + c + '">' + v + '</span>'
                //     }
                // },

                {
                    title: 'Status',
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    formatter: function (value, row, index) {
                        var steps = $scope.data.steps;
                        if (!steps[stepTwoKey] || !steps[stepTwoKey][row.id]) {
                            return '-';
                        }
                        var data = steps[stepTwoKey][row.id];
                        if (data.status == toBeCanaryStatus) return '未灰度';
                        if (data.status == isInCanaryStatus) return '灰度中';
                        if (data.status == canaryDoneStatus) return '升级完成';
                    }
                },

                {
                    title: 'Operation',
                    align: 'center',
                    valign: 'middle',
                    width: '200px',
                    events: operateEvents,
                    sortable: true,
                    formatter: function (value, row, index) {
                        // is this vs use old
                        var str = '<div class="actions">';
                        var steps = $scope.data.steps;
                        if (!steps[stepTwoKey] || !steps[stepTwoKey][row.id]) {
                            return '-';
                        }

                        if (steps[stepTwoKey][row.id]['status'] == toBeCanaryStatus || steps[stepTwoKey][row.id]['status'] == isInCanaryStatus) {
                            str += '<a class="btn btn-info btn-xs canary">灰度</a>';
                        } else if (steps[stepTwoKey][row.id]['status'] == canaryDoneStatus) {
                            str += '<a class="btn btn-danger btn-xs activate">生效</a>';
                        }
                        str += '</div>';
                        return str;
                    }
                }
            ], []],
            sortName: 'id',
            sortOrder: 'desc',
            data: [],
            classes: "table-bordered  table-hover table-striped table",
            idField: 'id',
            resizable: true,
            resizeMode: 'overflow',
            pageSize: 200,
            sidePagination: 'client',
            pageList: [20, 40, 80, 200],
            formatLoadingMessage: function () {
                return "<img class='loading-img' src='/static/img/loading_100.gif' /> 正在加载全部证书升级流程";
            },
            formatNoMatches: function () {
                return '<i class="fa status-alert fa-exclamation-triangle" aria-hidden="true"></i> 没有找到匹配的证书升级流程';
            }
        });
    };

    var selectedVsId;
    var selectedCid;
    window.operateEvents = {
        'click .activate': function (e, value, row, index) {
            selectedVsId = row.id;
            $scope.query.vsId = row.id;
            activateSlb(function () {
                alert('生效证书成功');
            }, function (code) {
                alert('生效证书失败! 错误码: ' + code);
            }, row.id);
        },
        'click .canary': function (e, value, row, index) {
            var certId = $scope.data.steps[stepOneKey]['new-id'];
            selectedVsId = row.id;
            $scope.$apply(function () {
                $scope.query.vsId = row.id;
                $('#uploadCertDialog').modal('show');
            });
            selectedCid = certId;
            // var promises = getCanaryIps(row.id, certId);
            // promises.success(function (response, code) {
            //     //todo: fake
            //     if (code == 200) {
            //         $scope.canaryIps = response;
            //
            //     } else {
            //         alert('Failed t get canary ips');
            //     }
            // });
        }
    };

    $scope.showCanaryArea=function () {
        var vsId = $scope.query.vsId;
        return $scope.data.steps['Activate']?$scope.data.steps['Activate'][vsId]['status']=='toBeCanary':false;
    };

    $scope.getMachineStatus = function () {

    };

    function canaryCertificateFunc(vsId, certId, percentage, nextFunc, failFunc, batch) {
        percentage = percentage / 100;

        var param = {};
        var url = G.baseUrl+'/api/cert/canary';
        if (batch) {
            url = G.baseUrl+'/api/cert/activate';
            param = {
                vsId: vsId,
                certId: certId,
                percent: percentage
            }
        } else {
            param = {
                vsId: vsId,
                certId: certId,
                percent: percentage
            };
        }
        var request = {
            method: 'GET',
            url: url,
            params: param
        };
        $http(request).success(function (response, status) {
            var canaryIps = [];
            if (status == 200) {
                if (batch) {
                    $scope.canaryIps = getAllIps();
                    canaryIps = $scope.canaryIps;
                } else {
                    // save in canary ips
                    canaryIps = response;
                    $scope.canaryIps = response;
                }
                if (nextFunc) {
                    nextFunc(canaryIps);
                }
            } else {
                if (batch) {
                    canaryIps = getAllIps();
                } else {
                    // save in canary ips
                    //canaryIps = response;
                    //$scope.canaryIps = response;
                }
                if (nextFunc) {
                    //todo: test code
                    canaryIps.push("127.0.0.1");
                    nextFunc(canaryIps);
                }
                //
                // if (failFunc) {
                //     failFunc(response.message);
                // }
            }
        });
    }

    function getAllIps() {
        var slbs = $scope.data.slbs;
        var vses = $scope.data.vses;

        var currentVsId = $scope.query.vsId;
        var vs = vses[currentVsId];
        var slbsIds = vs['slb-ids'];

        var slbServers = [];

        $.each(slbsIds, function (i, a) {
            slbServers = slbServers.concat(_.pluck(slbs[a]['slb-servers'], 'ip'));
        });

        return slbServers;
    };

    function getCanaryIps(vsId, certId) {
        var param = {
            vsId: vsId,
            cid: certId
        };
        var request = {
            method: 'GET',
            url: G.baseUrl+'/api/cert/vs/canary/ips',
            params: param
        };
        return $http(request).success(function (response, code) {
            return response;
        });
    };

    function applyHashData(hashData) {
        if (hashData && hashData.upgradeId) {
            $scope.query.upgradeId = hashData.upgradeId;
        } else {
            $scope.query.upgradeId = '';
        }

        if (hashData && hashData.certId) {
            $scope.query.certId = hashData.certId;
        } else {
            $scope.query.certId = '';
        }
    }

    $scope.hashChanged = function (hashData) {
        $scope.env = hashData.env ? hashData.env : 'pro';
        initTable();
        applyHashData(hashData);

        var id = $scope.query.upgradeId;
        $scope.loadData(id);
    };

    H.addListener("selfInfoApp", $scope, $scope.hashChanged);
});
angular.bootstrap(document.getElementById("self-info-area"), ['selfInfoApp']);