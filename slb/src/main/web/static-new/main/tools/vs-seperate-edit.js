var headerInfoApp = angular.module('headerInfoApp', ["angucomplete-alt"]);
headerInfoApp.controller('headerInfoController', function ($scope, $http) {
    $scope.resource =H.resource;
    var resource = $scope.resource;
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
            case 'http-https': {
                link = "/portal/tools/redirect/redirects#?env=" + G.env;
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
    $scope.hashChanged = function (hashData) { $scope.resource = H.resource;
        if (hashData.env) {
            $scope.env = hashData.env;
        }
    };
    H.addListener("headerInfoApp", $scope, $scope.hashChanged);
});
angular.bootstrap(document.getElementById("header-area"), ['headerInfoApp']);

var selfInfoApp = angular.module('selfInfoApp', ["ngSanitize", "angucomplete-alt", "http-auth-interceptor"]);
selfInfoApp.controller('selfInfoController', function ($scope, $http, $q) {
    $scope.resource =H.resource;
    var resource = $scope.resource;
    $scope.data = {};
    $scope.query = {};

    var mapping = {
        金桥: 'SHAJQ',
        欧阳: 'SHAOY',
        南通: 'NTGXH',
        福泉: 'SHAFQ',
        金钟: 'SHAJZ'
    };

    var log_start = new Date().getTime() - 1000 * 60 * 60;
    var log_end = new Date().getTime() + 1000 * 60 * 60;
    var dashboardStartTime = $.format.date(log_start, 'yyyy-MM-dd HH:mm:ss');
    var dashboardEndTime = $.format.date(log_end, 'yyyy-MM-dd HH:mm:ss');

    var table = $('#domainsTable');
    var stages = ['created', 'create-and-bind-new-vs', 'split-vs', 'rollback'];
    var success = 'SUCCESS';
    var doing = 'DOING';
    var fail = 'FAIL';
    var vsservice;
    var flowService;

    var rollbackTask;

    $scope.cacheRequestFn = function (str) {
        return {q: str, timestamp: new Date().getTime()};
    };

    $scope.remoteVsesUrl = function () {
        return G.baseUrl + "/api/meta/vses";
    };

    $scope.selectVsId = function (o) {
        if (o) {
            var id = o.originalObject.id;
            $scope.query.vsId = id;
            vsSeperateObj.setOriginVs(id);
        }
    };

    // $scope.vsIdInputChanged = function (o) {
    //     $scope.query.vsId = o;
    //     vsSeperateObj.setOriginVs(o);
    // };

    // First Step
    var stageOneName = stages[0];
    $scope.showFirstStep = function () {
        var steps = vsSeperateObj.getSteps();
        return steps && steps['select-vs'] && _.keys(steps).length == 1;
    };

    $scope.firstStepDisable = function (nameInValid) {
        var vsId = $scope.query.vsId;
        var seperatable = vsSeperateObj.getOriginVsData();

        if (!seperatable || seperatable.error) return true;

        var vsInValid = vsId == '' || vsId == undefined;

        return nameInValid || vsInValid;
    };

    $scope.getDomainText = function (domains) {
        domains = _.pluck(domains, 'name');
        return domains.join('/');
    };

    $scope.showCreatedError = function () {
        var stage = vsSeperateObj.getStage(stageOneName);
        if (stage && stage.status != success) return true;
    };

    $scope.showBindError = function () {
        var stage = vsSeperateObj.getStage(stageTwoName);
        if (stage && stage.status == fail) return true;
    };

    $scope.showActivateError = function () {
        var stage = vsSeperateObj.getStage(stageThreeName);
        if (stage && stage.status == fail) return true;
    };

    $scope.moveFirstToSecond = function () {
        var splitDo = vsSeperateObj.toEntityDo();
        var promise = flowService.createNewSplit(splitDo);

        // save
        promise.success(function (response, code) {
            saveSplit(response, stageOneName, function () {
                vsSeperateObj.setId(response.id);
                vsSeperateObj.setStage('created', response['created']);
                vsSeperateObj.setStatus(response.status);
                vsSeperateObj.newAddVsStep();

                // reload
                var pair = {
                    id: response.id,
                    timeStamp: new Date().getTime()
                };
                H.setData(pair);
            });
        });
    };

    $scope.$watch('data.vsSeperate.Data.targetVs.total', function (newVal, oldVal) {
        if (!newVal) return;

        if (vsSeperateObj.getId() && vsSeperateObj.status != '已创建') return;

        var max = newVal;

        var orginVs = vsSeperateObj.getOriginVsData();
        if (orginVs.domains) {
            max = orginVs.domains.length;
        }

        if (newVal > max) {
            newVal = max;
        }

        vsSeperateObj.resetTargetVs();
        // add count array to data
        for (var i = 0; i < newVal; i++) {
            vsSeperateObj.setTargetVs([]);
        }
    });

    // Second step
    var stageTwoName = stages[1];
    var stageThreeName = stages[2];

    $scope.showSecondStep = function () {
        var steps = vsSeperateObj.getSteps();
        return steps && steps['add-vs'] && !steps['create-vs'];
    };
    $scope.showSecondStepDiv = function () {
        var steps = vsSeperateObj.getSteps();
        return steps && steps['add-vs'];
    };

    $scope.showVsCountRow = function () {
        var steps = vsSeperateObj.getSteps();
        return !steps['create-vs'];
    };

    $scope.showRemoveVs = function () {
        // if no data has been filled
        var target = vsSeperateObj.getTargetVsData();
        var total = target.total;
        var targetVses = _.flatten(target.vses);
        if (targetVses.length == 0 && total > 2) return true;
        return false;
    };

    $scope.removeVs = function () {
        var targetVsData = vsSeperateObj.getTargetVsData();
        var size = targetVsData.total;
        if (size == 2) return;
        targetVsData.total--;
    };

    var vsIndex;

    $scope.popNewVs = function (index) {
        vsIndex = index;
        // get valid domains
        var domains = vsSeperateObj.getTargetVsDomains();
        if (domains.message) {
            $('#errorDomainDialog').modal('show').find('#body-error').html(domains.message);
            return;
        }
        domains = domains.sort(function (a, b) {
            return a.length - b.length > 0;
        });

        table.bootstrapTable('load', domains);

        $('#selectDomainDialog').modal('show');
    };

    $scope.popEditVs = function (index, domains) {
        vsIndex = index;

        var steps = vsSeperateObj.getSteps();
        if (steps['create-vs']) return;

        // those not included domains
        var vdomains = vsSeperateObj.getTargetVsDomains();
        if (vdomains.message) {
            vdomains = [];
        }
        // current domains
        domains = _.map(domains, function (v) {
            return {domain: v};
        });
        // merge domains
        var merge = vdomains.concat(domains);
        merge = merge.sort(function (a, b) {
            return a.length - b.length > 0;
        });
        table.bootstrapTable("load", merge);
        // select current domains
        table.bootstrapTable("checkBy",
            {field: "domain", values: _.pluck(domains, 'domain')});

        $('#selectDomainDialog').modal('show');
    };

    $scope.saveVsClick = function () {
        var selectedDomains = table.bootstrapTable('getSelections');
        var names = _.pluck(selectedDomains, 'domain');
        // replace indexed vs to the selected
        vsSeperateObj.replaceTargetVs(vsIndex, names);
    };

    $scope.secondStepDisable = function () {
        var target = vsSeperateObj.getTargetVsData();
        if (!target) return;

        // target vs not selected
        var targetVses = _.flatten(target.vses);

        var hasBlank = _.filter(target.vses, function (v) {
            return v.length == 0
        });

        var origin = vsSeperateObj.getOriginVsData();

        if (!origin || !origin.domains) return;
        var originVses = origin.domains.length;


        if (targetVses.length != originVses || hasBlank.length > 0) return true;

        return false;
    };

    $scope.moveSecondToThird = function () {
        var splitDo = vsSeperateObj.toEntityDo();
        var promise = flowService.updateSplit(splitDo);

        // save
        promise.success(function (response, code) {
            saveSplit(response, stageOneName, function () {
                vsSeperateObj.newCreateVsStep();

                var id = response.id;
                // bind vs
                vsSeperateObj.setStage(stages[1],{
                    status: doing
                });

                var bindVsPromise = flowService.bindSplit(id);
                bindVsPromise.success(function (response, code) {
                    saveSplit(response, stageTwoName, function () {
                        // activate vs
                        vsSeperateObj.setStage(stages[2],{
                            status: doing
                        });

                        var activateVsPromise = flowService.activateSplit(id);
                        activateVsPromise.success(function (response, code) {
                            saveSplit(response, stageThreeName, function () {
                                var vsIds = response['new-vs-ids'];
                                vsSeperateObj.setStatus(response.status);
                                getVsesDetail(vsIds);
                                setData();
                            });
                        })
                    });
                });
            });
        });
    };

    $scope.showLinks = function (type) {
        var env = $scope.env;
        if (G[env] && G[env].urls[type]) return true;
        return false;
    };

    $scope.generateMonitorUrl = function (type, vs) {
        var env = $scope.env;
        var cenv = env == 'pro' ? 'PROD' : $scope.env.toUpperCase();

        if (!vs) return;

        var vsId = vs.id;

        var esDomains = _.pluck(vs.domains, 'name');

        var esDomainsText = esDomains.join(' OR ');

        var vsProperties = vs.properties;

        var idc = vsProperties['idc'] ? vsProperties['idc'].value : '-';
        var idcText = '';
        if (idc !== '-') {
            var idcArray = _.map(idc.split(','), function (i) {
                return 'idc:' + mapping[i];
            });

            idcText = '(' + idcArray.join(' OR ') + ')';
        }

        switch (type) {
            case 'dashboard': {
                var dashboard = G.dashboardportal + '/#env=' + cenv +
                    '&metric-name=slb.req.count&interval=1m&start-time=' + dashboardStartTime + '&end-time=' + dashboardEndTime + '&chart=line&aggregator=sum&ts=1515983913677&tags={"vsid":["' + vsId + '"]}&group-by=[status]';
                return dashboard;
            }
            case 'es': {
                if (G[$scope.env].urls.es) {
                    return G[$scope.env].urls.es + '?query=domain:(' + esDomainsText + ') AND ' + idcText;
                } else {
                    return '-';
                }
            }
            case 'hickwall': {
                return G[$scope.env] ? G[$scope.env].urls.hickwall + '/dashboard/db/1/slb_request?tag=domain:' + esDomains : '';
            }
            default:
                break;
        }
    };
    $scope.showTargetVsErrorMessage = function () {
        var steps = vsSeperateObj.getSteps();
        if (steps['create-vs']) return false;
        var target = vsSeperateObj.getTargetVsData();
        var origin = vsSeperateObj.getOriginVsData();

        if (!origin || !origin.domains || !target) return;

        var originVses = origin.domains.length;

        // target vs not selected
        var targetVses = _.flatten(target.vses);

        var hasBlank = _.filter(target.vses, function (v) {
            return v.length == 0
        });
        if (hasBlank.length == 0 && originVses != targetVses.length) {
            return true;
        }
        return false;
    };

    $scope.showRetry = function () {
        var stages = vsSeperateObj.getFailedStages();
        return stages && (stages[stageTwoName] || stages[stageThreeName]);
    };

    $scope.retryFailed = function () {
        var failed = vsSeperateObj.getFailedStages();
        if (!failed) return;
        var failedKey = _.keys(failed);
        var key = failedKey[0];

        var id = vsSeperateObj.getId();

        var stageName;
        var promise;

        switch (key) {
            case 'create-and-bind-new-vs': {
                stageName = stageTwoName;
                promise = flowService.bindSplit(id);
                break;
            }
            case 'split-vs': {
                stageName = stageThreeName;
                promise = flowService.activateSplit(id);
                break;
            }
            default:
                break;
        }
        if (promise) {
            promise.success(function (response, code) {
                saveSplit(response, stageName, function () {
                    var vsIds = response['new-vs-ids'];
                    vsSeperateObj.setStatus(response.status);
                    getVsesDetail(vsIds);
                });
            });
        }
    };
    // Third Step
    $scope.showThirdStep = function () {
        var steps = vsSeperateObj.getSteps();
        return steps['create-vs'];
    };

    $scope.getFinalStatus = function () {
        var steps = vsSeperateObj.getSteps();
        // var s = steps['create-vs'];
        // if (!s) return '-';

        var stages = vsSeperateObj.getFailedStages();
        if (stages && (stages[stageTwoName] || stages[stageThreeName])) {
            return (angular.equals(resource, {}) ? '失败,等待重试!': resource.tools.vseparatesEdit.js.msg1);
        } else {
            return vsSeperateObj.status;
        }
    };

    $scope.getFinalClass = function () {
        var stages = vsSeperateObj.getFailedStages();
        if (stages && (stages[stageTwoName] || stages[stageThreeName])) {
            return 'status-red';
        } else {
            return 'status-green'
        }
    };

    $scope.stepClass = function (step) {
        var job = vsSeperateObj.Stages[step];

        var defaultClass = 'fa fa-battery-empty';

        if (!job) return defaultClass;

        if (job.status == success) {
            defaultClass = 'status-green fa fa-battery-full';
        } else if (job.status == doing) {
            defaultClass = 'status-yellow fa fa-battery-half';
        } else {
            defaultClass = 'status-red fa fa-battery-full';
        }

        return defaultClass;
    };

    $scope.stepText = function (step) {
        var resource = $scope.resource;
        var job = vsSeperateObj.Stages[step];

        var defaultText = (angular.equals(resource, {}) ? '未开始': resource.tools.vseparatesEdit.js.msg2);

        if (!job) return defaultText;

        if (job.status == success) {
            defaultText = (angular.equals(resource, {}) ? '成功': resource.tools.vseparatesEdit.js.msg3);
        } else if (job.status == doing) {
            defaultText = (angular.equals(resource, {}) ? '正在执行': resource.tools.vseparatesEdit.js.msg4);
        } else {
            defaultText = (angular.equals(resource, {}) ? '失败': resource.tools.vseparatesEdit.js.msg5);
        }

        return defaultText;
    };

    $scope.getMonitorLink = function () {
        var sourceVs = vsSeperateObj.getOriginVsData();
        var targetVses = vsSeperateObj.getCreatedVs();

        var src = sourceVs.id.toString();
        var target = _.map(_.keys(targetVses), function (v) {
            return v.toString();
        });

        target.push(src);


        // vses
        var array = _.uniq(target);

        // vses related slb
        var slbsRequest = {
            url: G.baseUrl + '/api/slbs?type=info&vsId=' + array.join(','),
            method: 'GET'
        };

        // vses related groups
        var groupsRequest = {
            url: G.baseUrl + '/api/groups?type=info&vsId=' + array.join(','),
            method: 'GET'
        };

        var results = {};

        var vsesPromise = $http(slbsRequest).success(function (response, code) {
            if (code == 200) {
                results['slbs'] = _.pluck(response['slbs'], 'id');
            }
        });
        var groupsPromise = $http(groupsRequest).success(function (response, code) {
            if (code == 200) {
                results['groups'] = _.pluck(response['groups', 'id']);
            }
        });


        $q.all([vsesPromise, groupsPromise]).then(function () {
            var env = $scope.env;
            var slbs = results['slbs'];
            var groups = results['groups'];

            var vsIds = array.join(',');
            var slbIds = slbs.join(',');
            var groupIds = groups.join(',');

            var link = '/portal/user/traffic#?env=' + env;

            if (array && array.length > 0) {
                link += '&vses=' + vsIds;
            }
            if (slbs && slbs.length > 0) {
                link += '&slbs=' + slbIds;
            }
            if (groups && groups.length > 0) {
                link += '&groups=' + groupIds;
            }
            window.open(link, '_blank');
        });
    };

    // Revert Step
    $scope.showRevert = function () {
        var merge = vsSeperateObj.getStage(stages[2]);
        var rollback = vsSeperateObj.getStage(stages[3]);

        if (merge && (!rollback || rollback.status == fail)) {
            return true;
        }

        return false;
    };

    $scope.showRevertProgress = function () {
        var rollback = vsSeperateObj.getStage(stages[3]);
        return rollback && rollback.status != fail;
    };

    $scope.revertChanges = function () {
        var id = vsSeperateObj.getId();
        var promise = flowService.revertSplit(id);
        promise.success(function (response, code) {
            setData();

            if (code != 200) {
                alert((angular.equals(resource, {}) ? '回退失败。错误信息: ': resource.tools.vseparatesEdit.js.msg6) + response.message);
                return;
            }
            vsSeperateObj.startRevert();

            vsservice.addStage(stages[3], {
                status: doing
            });
        });
    };

    $scope.showRollbackError = function () {
        var rollback = vsSeperateObj.getStage(stages[3]);
        return rollback && rollback.status == fail;
    };

    $scope.loadData = function () {
        var id = $scope.query.id;
        var getPromise = flowService.getSplit(id);
        getPromise.success(function (response, code) {
            if (code != 200) {
                var code = response.code;
                $('#errorDomainDialog').modal('show').find('#body-error').html("Api failed, " +
                    "Code:" + code + ';' +
                    ' Message:' + response.message);
            } else {
                var sourceVsIds = [response['source-vs-id']];
                getOriginVsDetail(sourceVsIds);

                var vsIds = response['new-vs-ids'];

                if (vsIds && vsIds.length > 0) {
                    getVsesDetail(vsIds);
                }

                vsSeperateObj.toEntity(response);

                var rollback = vsSeperateObj.getStage(stages[3]);
                if (!rollback && rollback.status == doing) {
                    vsSeperateObj.startRevert();
                } else if (rollback.status != doing) {
                    window.clearInterval(rollbackTask);
                }
            }
        })
    };

    $scope.initTable = function (data) {
        var resource = $scope.resource;
        data = data || [];
        table.bootstrapTable({
            columns: [[
                {
                    field: 'state',
                    checkbox: true,
                    align: 'center',
                    valign: 'middle'
                },
                {
                    field: 'domain',
                    title: (angular.equals(resource, {}) ? '域名': resource.tools.vseparatesEdit.js.msg7),
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    formatter: function (value, row, index) {
                        return '<a class="editVS" target="_self" title="' + value + '" href="/portal/vs#?env=' + G.env + '&vsId=' + value + '" style="text-decoration: none; margin-left: 5px; word-break: break-all">' + value + '</a>';
                    }
                }], []],
            sortName: 'domain',
            sortOrder: 'desc',
            // search: true,
            data: data,
            classes: "table table-bordered  table-hover table-striped",
            pageSize: 200,
            sidePagination: 'client',
            resizable: true,
            resizeMode: 'overflow',
            formatLoadingMessage: function () {
                return (angular.equals(resource, {}) ? "<img class='loading-img' src='/static/img/loading_100.gif' /> 正在加载 域名列表": resource.tools.vseparatesEdit.js.msg8);
            },
            formatNoMatches: function () {
                return (angular.equals(resource, {}) ? '<i class="fa status-alert fa-exclamation-triangle" aria-hidden="true"></i> 没有找到匹配的 域名列表': resource.tools.vseparatesEdit.js.msg9);
            }
        });

        table.on('check.bs.table uncheck.bs.table check-all.bs.table uncheck-all.bs.table', function () {
            $('#saveVsBt').prop('disabled', !table.bootstrapTable('getSelections').length);
        });
    };

    $scope.getVsStatusText = function (status) {
        var text = '';
        switch (status) {
            case 'activated': {
                text = (angular.equals(resource, {}) ? '<span class="status-green">已激活</span>': resource.tools.vseparatesEdit.js.msg10);
                break;
            }
            case 'deactivated': {
                text = (angular.equals(resource, {}) ? '<span class="status-gray">未激活</span>': resource.tools.vseparatesEdit.js.msg11);
                break;
            }
            case 'tobeactivated': {
                text = (angular.equals(resource, {}) ? '<span class="status-yellow">有变更</span>': resource.tools.vseparatesEdit.js.msg12);
                break;
            }
            default: {
                text = (angular.equals(resource, {}) ? '<span class="status-gray">未知</span>': resource.tools.vseparatesEdit.js.msg13);
                break;
            }
        }
        return text;
    };

    $scope.showOriginVsInfo = function () {
        if (vsSeperateObj) {
            var id = vsSeperateObj.Data.originVs.id;
            return id;
        } else {
            return false;
        }
    };

    function saveSplit(response, stageName, nextStepFn) {
        if (response.code && response.message) {
            var code = response.code;
            $('#errorDomainDialog').modal('show').find('#body-error').html("Api failed, " +
                "Code:" + code + ';' +
                ' Message:' + response.message);
            // set the stage to be failed
            if (!vsSeperateObj[stageName]) {
                vsSeperateObj.setStage(stageName, {
                    status: 'FAIL',
                    message: response.message
                });
            }
        } else {
            var stage = response[stageName];
            if (stage) {
                vsSeperateObj.setStage(stageName, stage);
                if (stage.status && stage.status == success) {
                    nextStepFn();
                }
            }
        }
    }

    function getVsesDetail(vsIds) {
        getVsesFunc(vsIds, function (vses) {
            vsSeperateObj.setCreatedVs(vses);
        }, true);
    }

    function getOriginVsDetail(vsIds) {
        getVsesFunc(vsIds, function (vses) {
            vsSeperateObj.setOriginVsData(_.values(vses)[0]);
        }, true);
    }

    function getVsesFunc(vsIds, nextFun, ignoreValidate) {
        vsservice.getVsByIds(vsIds).then(function (data) {
            // if get vs failed
            if (data.message) {
                nextFun({error: data});
            } else {
                if (ignoreValidate) {
                    var vses = data.vses;
                    vses = _.mapObject(vses, function (v, k, item) {
                        var properties = _.indexBy(v.properties, function (s) {
                            return s.name;
                        });

                        var status = properties['status'] ? properties['status'].value : 'Unknown';
                        var idc = properties['idc'] ? properties['idc'].value : 'Unknown';
                        var zone = properties['zone'] ? properties['zone'].value : 'Unknown';
                        var statistics = data.statistics[k];

                        v.status = status;
                        v.idc = idc;
                        v.zone = zone;
                        v.statistics = statistics;

                        return v;
                    });
                    nextFun(vses);
                    return;
                }
                // if vs validation failed
                var promise = validateVsSeperate(vsIds[0]);
                return promise.success(function (response2, code2) {
                    if (code2 != 200) {
                        nextFun({error: new SlbException("当前VS不能用于拆分, 错误信息:" + response2.message)});
                    } else {
                        var vses = data.vses;
                        vses = _.mapObject(vses, function (v, k, item) {
                            var properties = _.indexBy(v.properties, function (s) {
                                return s.name;
                            });

                            var status = properties['status'] ? properties['status'].value : 'Unknown';
                            var idc = properties['idc'] ? properties['idc'].value : 'Unknown';
                            var zone = properties['zone'] ? properties['zone'].value : 'Unknown';
                            var statistics = data.statistics[k];

                            v.status = status;
                            v.idc = idc;
                            v.zone = zone;
                            v.statistics = statistics;

                            return v;
                        });
                        nextFun(vses);
                    }
                });
            }
        });
    }

    function validateVsSeperate(vsId) {
        var promise = flowService.validateSplit(vsId);
        return promise;
    }

    function waitRollbackComplete(timeout) {
        return setInterval(function () {
            // if merge is not activated?
            $scope.loadData();
        }, timeout);
    };

    function setData() {
        var pair = {
            timeStamp: new Date().getTime()
        };
        H.setData(pair);
    }

    $scope.hashChanged = function (hashData) { $scope.resource = H.resource;
        vsservice = VsesService.create($http, $q);
        flowService = VsSeperateService.create($http);


        var env = 'uat';
        // env
        if (hashData.env) {
            env = hashData.env;
        }
        $scope.env = env;

        var id;
        if (hashData.id) {
            id = hashData.id;
            $scope.query.id = id;
        }

        if (id) {
            $scope.loadData();
        }

        vsSeperateObj.initialize();
        eventWrapper(vsSeperateObj);

        vsSeperateObj.on('select-vs', function () {
            var vsId = vsSeperateObj.getOriginVs();
            getVsesFunc([vsId], function (vses) {
                vsSeperateObj.setOriginVsData(_.values(vses)[0]);
            });
        });

        vsSeperateObj.on('start-revert', function () {
            if (!rollbackTask) {
                rollbackTask = waitRollbackComplete(1000);
            }
        });


        // Data binding
        $scope.data.vsSeperate = vsSeperateObj;


        $scope.initTable();
    };

    H.addListener("selfInfoApp", $scope, $scope.hashChanged);
});
angular.bootstrap(document.getElementById("self-info-area"), ['selfInfoApp']);