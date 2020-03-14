var headerInfoApp = angular.module('headerInfoApp', ["angucomplete-alt"]);
headerInfoApp.controller('headerInfoController', function ($scope, $http) {
    $scope.resource = H.resource;
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
    $scope.resource = H.resource;
    var resource = $scope.resource;
    // Variables
    var vsservice;
    var flowService;
    $scope.query = {
        accept: false
    };
    $scope.data = {};
    var success = 'SUCCESS';
    var doing = 'DOING';
    var fail = 'FAIL';
    var stageTwoName = 'create-and-bind-new-vs';
    var stageThreeName = 'merge-vs';
    var stageFourName = 'clean-vs';
    var stageFiveName = 'rollback';
    var timeout = 5000;
    var intervalStarted = false;
    var revertStarted = false;
    var intervalTask;
    var revertTask;

    // First Step
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
        }
    };

    $scope.saveVsClick = function () {
        // get vs information
        var vsId = $scope.query.vsId;

        vsservice.getVsById(vsId).then(function (data) {
            var d = data;
            if (d.message) {
                alert(d.message);
            } else {
                var valid = vsMergeObj.validateSourceVs(d);
                if (valid.message) {
                    d.message = valid.message;
                }

                var vs = {};
                vs[d.id] = d;

                vsMergeObj.addSourceVs(vs, valid.message);
            }
        });
    };

    $scope.disableMergeName = function () {
        var createStage = vsMergeObj.getStage('created');
        if (createStage) {
            return true;
        }
        return false;
    };

    // Second Step
    $scope.showVsCreation = function () {
        var status = false;
        var createStage = vsMergeObj.getStage('created');
        var rollbackStage = vsMergeObj.getStage(stageFiveName);
        if (createStage && createStage.status == success) {
            status = true;
        }
        if (rollbackStage && rollbackStage.status == success) {
            status = false;
        }
        return status;
    };

    $scope.showFirstToSecond = function () {
        var createStage = vsMergeObj.getStage(stageTwoName);
        if (!createStage) {
            return true;
        }
        return false;
    };


    $scope.popAddVs = function () {
        $scope.query.vsId = '';
        $('#selectVsDialog').modal('show');
        $('#vsIdSelector_value').val('');
    };

    $scope.removeVs = function (vsId) {
        vsMergeObj.removeSourceVs(vsId);
    };

    $scope.getDomainText = function (domains) {
        domains = _.pluck(domains, 'name');
        return domains.join('/');
    };

    $scope.firstStepDisable = function (nameValid) {
        if (nameValid) return true;

        var sourceVses = vsMergeObj.getSourceVses();
        // if there is error vs
        var f = _.pick(sourceVses, function (value, key, object) {
            return value.message;
        });
        if (f && _.keys(f).length > 0) {
            return true;
        }

        if (sourceVses && _.keys(sourceVses).length > 1) return false;
        return true;
    };

    $scope.moveFirstToSecond = function () {
        // create
        var merge = vsMergeObj.toMergeDo();
        var vsIds = merge['source-vs-id'];
        var rollbackStage = vsMergeObj.getStage(stageFiveName);

        var validatePromise = validateVsMerge(vsIds, rollbackStage);
        validatePromise.then(function (data) {
            var code2 = data.status;
            var response2 = data.data;

            if (code2 != 200) {
                vsMergeObj.vsesValid = (angular.equals(resource, {}) ? '当前选择的VS不支持合并，原因:': resource.tools.mergeEdit.js.msg1) + response2.message;
            } else {
                vsMergeObj.vsesValid = '';

                var promise;
                if (rollbackStage) {
                    delete merge.status;
                    promise = flowService.updateMerge(merge);
                } else {
                    promise = flowService.createNewMerge(merge);
                }
                promise.success(function (response, code) {
                    if (code != 200) {
                        $('#errorDomainDialog').modal('show').find('#body-error').html((angular.equals(resource, {}) ? '保存VS合并信息失败了,错误信息: ': resource.tools.mergeEdit.js.msg2) + response.message);
                    } else {
                        // reload the page with merge id
                        $scope.query.id = response.id;
                        var getPromise = getMergeById(response.id);
                        getPromise.then(function () {
                            var ssl = vsMergeObj.getSourceVsSSL();

                            if (!ssl) {
                                vsMergeObj.finishCreation();
                            } else {
                                var cert = certificateReload();
                                cert.then(function () {
                                    if (vsMergeObj.getStage('cert').status == success) {
                                        vsMergeObj.finishCreation();
                                    }
                                });
                            }
                            setData(response.id);
                        });
                    }
                });
            }
        });

    };

    $scope.showBindError = function () {
        var stage = vsMergeObj.getStage(stageTwoName);
        if (stage && stage.status == fail) return true;
    };

    $scope.showActivateError = function () {
        var stage = vsMergeObj.getStage(stageThreeName);
        if (stage && stage.status == fail) return true;
    };

    $scope.showTargetVs = function () {
        var target = vsMergeObj.getTargetVs();
        return target;
    };

    $scope.showCertificateRow = function () {
        var sourceSSL = vsMergeObj.getSourceVsSSL();
        return sourceSSL;
    };

    $scope.stepClass = function (step) {
        var job = vsMergeObj.stages[step];

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
        var job = vsMergeObj.stages[step];

        var defaultText = (angular.equals(resource, {}) ? '未开始': resource.tools.mergeEdit.js.msg3);

        if (!job) return defaultText;

        if (job.status == success) {
            defaultText = (angular.equals(resource, {}) ? '成功': resource.tools.mergeEdit.js.msg4);
        } else if (job.status == doing) {
            defaultText = (angular.equals(resource, {}) ? '正在执行...': resource.tools.mergeEdit.js.msg5);
        } else {
            defaultText = (angular.equals(resource, {}) ? '失败...': resource.tools.mergeEdit.js.msg6);
        }

        return defaultText;
    };

    $scope.showRetry = function () {
        var stages = vsMergeObj.getFailedStages();
        return stages && (stages[stageTwoName] || stages[stageThreeName]);
    };

    $scope.retryFailed = function () {
        var failed = vsMergeObj.getFailedStages();
        if (!failed) return;
        var failedKey = _.keys(failed);
        var key = failedKey[0];

        var id = vsMergeObj.getId();

        var stageName;
        var promise;

        switch (key) {
            case 'create-and-bind-new-vs': {
                stageName = stageTwoName;
                promise = flowService.bindMerge(id);
                break;
            }
            case 'merge-vs': {
                stageName = stageThreeName;
                promise = flowService.mergeVs(id);
                break;
            }
            default:
                break;
        }
        if (promise) {
            vsMergeObj.replaceStage(stageName, {
                status: doing
            });

            promise.success(function (response, code) {
                if (code != 200) {
                    vsMergeObj.replaceStage(stageName, {
                        status: fail,
                        message: response.message
                    });
                }
                setData(id);
            });
        }
    };

    // Third Steps

    $scope.getVsStatusText = function (status) {
        var text = '';
        switch (status) {
            case 'activated': {
                text = (angular.equals(resource, {}) ? '<span class="status-green">已激活</span>': resource.tools.mergeEdit.js.msg7);
                break;
            }
            case 'deactivated': {
                text = (angular.equals(resource, {}) ? '<span class="status-gray">未激活</span>': resource.tools.mergeEdit.js.msg8);
                break;
            }
            case 'tobeactivated': {
                text = (angular.equals(resource, {}) ? '<span class="status-yellow">有变更</span>': resource.tools.mergeEdit.js.msg9);
                break;
            }
            default: {
                text = (angular.equals(resource, {}) ? '<span class="status-gray">未知</span>': resource.tools.mergeEdit.js.msg11);
                break;
            }
        }
        return text;
    }

    $scope.showSecondStep = function () {
        var stage2 = vsMergeObj.getStage(stageTwoName);
        var stage3 = vsMergeObj.getStage(stageThreeName);
        var stage4 = vsMergeObj.getStage(stageFourName);

        if (!stage2 || !stage3 || stage4) return false;

        if (stage2 && stage3 && stage2.status == success && stage3.status == success) return true;

        return false;
    };

    $scope.secondStepDisable = function () {
        return !$scope.query.accept;
    };

    $scope.moveSecondToThird = function () {
        $('#vses').showLoading();
        var promise = flowService.cleanMerge(vsMergeObj.id);
        promise.success(function (response, code) {
            $('#vses').hideLoading();
            if (code !== 200) {
                $('#errorDomainDialog').modal('show').find('#body-error').html((angular.equals(resource, {}) ? '清理数据失败,错误信息: ': resource.tools.mergeEdit.js.msg12) + response.message);
            } else {
                // reload the page with merge id
                $scope.query.id = response.id;
                var getPromise = getMergeById(response.id);
                getPromise.then(function () {
                    setData(response.id);
                });
            }
        });
    };

    $scope.showCleanResult = function () {
        var stage = vsMergeObj.getStage("clean-vs");
        return stage;
    };

    $scope.getCleanClass = function () {
        var stage = vsMergeObj.getStage("clean-vs");
        if (!stage) return;

        if (stage.status == fail) return 'fa fa-times status-red';
        else if (stage.status == success) return 'fa fa-check status-green';
        else return 'fa fa-battery-half status-yellow';
    };

    $scope.getCleanText = function () {
        var stage = vsMergeObj.getStage(stageFourName);
        if (!stage) return;

        if (stage.status == fail) return (angular.equals(resource, {}) ? '<span class="status-red">清理失败，错误信息: ': resource.tools.mergeEdit.js.msg13) + stage.message + '</span>';
        else if (stage.status == success) return (angular.equals(resource, {}) ? '<span class="status-green">清理成功</span>': resource.tools.mergeEdit.js.msg14);
        else return (angular.equals(resource, {}) ? '<span class="status-yellow">正在清理...</span>': resource.tools.mergeEdit.js.msg15)
    };

    $scope.loadData = function () {
        var mergeId = $scope.query.id;
        var promise = getMergeById(mergeId);
        promise.then(function () {
            var ssl = vsMergeObj.getSourceVsSSL();

            var hasBinding = vsMergeObj.getStage(stageTwoName);
            if (hasBinding) {
                vsMergeObj.finishLoading();
            }
            if (ssl) {
                var cert = certificateReload();
                cert.then(function () {
                    var hasCert = vsMergeObj.getStage('cert').status == success;
                    if (hasCert) {
                        // if binding not started
                        if (hasBinding) {
                        }
                        else {
                            vsMergeObj.finishCreation();
                        }
                    }
                });
            }
        });
    };

    $scope.getFinalStatus = function () {
        var stages = vsMergeObj.getFailedStages();
        if (stages && (stages[stageTwoName] || stages[stageThreeName] || stages[stageFourName] || stages[stageFiveName])) {
            return (angular.equals(resource, {}) ? '失败,等待重试!': resource.tools.mergeEdit.js.msg16);
        } else {
            return vsMergeObj.status;
        }
    };

    $scope.getFinalClass = function () {
        var stages = vsMergeObj.getFailedStages();
        if (stages && (stages[stageTwoName] || stages[stageThreeName] || stages[stageFourName] || stages[stageFiveName])) {
            return 'status-red';
        } else {
            return 'status-green'
        }
    };

    $scope.getMonitorLink = function () {
        var sourceVses = vsMergeObj.getSourceVses();
        var targetVses = vsMergeObj.getTargetVs();

        var src = _.keys(sourceVses);
        var target = _.keys(targetVses);

        // vses
        var array = _.uniq(src.concat(target));

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

    $scope.showRevertLink = function () {
        var merge = vsMergeObj.getStage(stageThreeName);
        var rollback = vsMergeObj.getStage(stageFiveName);

        if (merge && (!rollback || rollback.status == fail)) {
            return true;
        }

        return false;
    };

    $scope.showRevertRow = function () {
        var stageClean = vsMergeObj.getStage(stageFourName);
        var stageMerge = vsMergeObj.getStage(stageThreeName);

        var status = false;

        if (stageMerge && stageMerge.status != doing) {
            status = true;
        }

        if (stageClean) {
            status = false;
        }
        return status;
    };

    $scope.showRevertProgress = function () {
        var rollback = vsMergeObj.getStage(stageFiveName);
        return rollback && rollback.status != fail;
    };

    $scope.hasReverting = function () {
        var rollback = vsMergeObj.getStage(stageFiveName);
        if (!rollback) return true;

        return false;
    };

    $scope.revertChanges = function () {
        var id = vsMergeObj.getId();

        var promise = flowService.revertMerge(id);
        promise.success(function (response, code) {
            if (code != 200) {
                alert((angular.equals(resource, {}) ? '回退失败。错误信息: ': resource.tools.mergeEdit.js.msg17) + response.message);
                return;
            }

            vsMergeObj.startRevert();

            vsMergeObj.addStage(stageFiveName, {
                status: doing
            });
        });
    };

    $scope.showRollbackError = function () {
        var rollback = vsMergeObj.getStage(stageFiveName);
        return rollback && rollback.status == fail;
    };

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

        eventWrapper(vsMergeObj);

        vsMergeObj.on('finish-creation', function () {
            var mergeId = $scope.query.id;
            var promise = flowService.bindMerge(mergeId);

            vsMergeObj.addStage(stageTwoName, {
                status: doing,
            });

            promise.success(function (response, code) {
                if (code != 200) {
                    vsMergeObj.addStage(stageTwoName, {
                        status: fail,
                        message: response.message
                    })
                } else {
                    intervalTask = waitingBindingCompletion(mergeId, timeout);
                }
            });
        });
        vsMergeObj.on('finish-loading', function () {
            var mergeId = $scope.query.id;
            if (!intervalTask) {
                intervalTask = waitingBindingCompletion(mergeId, timeout);
                intervalStarted = true;
            }
        });
        vsMergeObj.on('start-revert', function () {
            var mergeId = $scope.query.id;
            if (!revertTask && !revertStarted) {
                revertTask = waitingRevertCompletion(mergeId, timeout);
                revertStarted = true;
            }
        });
        // Data binding
        $scope.data.vsMerge = vsMergeObj;
    };

    function waitingRevertCompletion(mergeId, timeout) {
        return setInterval(function () {
            // if merge is not activated?
            var p = getMergeById(mergeId);
            p.then(function () {
                var rollBackStage = vsMergeObj.getStage('rollback');

                // bind and merge all completed
                if (rollBackStage && rollBackStage.status == success) {
                    if (revertTask) window.clearInterval(revertTask);
                    vsMergeObj.addStage(stageFiveName, {
                        status: success
                    });
                }
            });
        }, timeout);
    }

    function waitingBindingCompletion(mergeId, timeout) {
        return setInterval(function () {
            // if merge is not activated?
            var p = getMergeById(mergeId);
            p.then(function () {
                var bindStage = vsMergeObj.getStage('create-and-bind-new-vs');
                var mergeStage = vsMergeObj.getStage('merge-vs');

                // bind and merge all completed
                if (bindStage && bindStage.status == success && mergeStage && mergeStage.status == success) {
                    if (intervalTask) window.clearInterval(intervalTask);
                    return;
                }

                // bind completed not merge
                var bindDone = bindStage && bindStage.status == success;
                var mergeNotStarted = mergeStage == undefined;
                if (bindDone && mergeNotStarted) {
                    vsMergeObj.addStage(stageThreeName, {
                        status: doing
                    });
                    var promise2 = flowService.mergeVs(mergeId);
                    promise2.success(function (response2, code2) {
                        if (code2 != 200) {
                            vsMergeObj.addStage(stageThreeName, {
                                status: fail,
                                message: response2.message
                            });
                        }
                    });
                }
            });
        }, timeout);
    }

    function certificateReload() {
        return new Promise(function (resevle, reject) {
            if (vsMergeObj.cid) {
                vsMergeObj.replaceStage('cert', {
                    status: success,
                    message: (angular.equals(resource, {}) ? '已经提交证书CID': resource.tools.mergeEdit.js.msg18)
                });
                resevle();
            } else {
                vsMergeObj.replaceStage('cert', {
                    status: fail,
                    message: (angular.equals(resource, {}) ? '未经提交证书CID': resource.tools.mergeEdit.js.msg19)
                });
                reject();
            }

        })
    }

    function getMergeById(id) {
        var promise = flowService.getMergeById(id);

        return promise.then(function (response, code) {
            var code = response.status;
            var response = response.data;

            if (code != 200) {
                $('#errorDomainDialog').modal('show').find('#body-error').html((angular.equals(resource, {}) ? '获取VS合并信息失败了,错误信息: ': resource.tools.mergeEdit.js.msg21) + response.message);
            } else {
                var sources = response['source-vs-id'];
                var target = response['new-vs-id'];

                var vsIds = [];
                if (sources && sources.length > 0) {
                    vsIds = vsIds.concat(sources);
                }
                if (target) {
                    vsIds.push(target);
                }

                // get All the vses detail by vs ids
                return vsservice.getVsByIds(vsIds).then(function (data) {
                    if (data.message) {
                        // api return error
                        alert((angular.equals(resource, {}) ? '获取VS信息失败。错误信息: ': resource.tools.mergeEdit.js.msg22) + data.message);
                    }
                    var vses = data.vses;
                    // 组装
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

                    vsMergeObj.toMerge(response, vses);

                    return vsMergeObj;
                });
            }

        });
    }

    function validateVsMerge(vsIds, rollback) {

        if (rollback) {
            return new Promise(function (resolve, reject) {
                resolve({
                    data: 'success',
                    status: 200
                });
            });
        }
        var promise = flowService.validateMerge(vsIds);
        return promise;
    }

    function setData(id) {
        var pair = {
            timeStamp: new Date().getTime()
        };
        if(id){
            pair.id = id;
        }

        H.setData(pair);
    }

    H.addListener("selfInfoApp", $scope, $scope.hashChanged);
});
angular.bootstrap(document.getElementById("self-info-area"), ['selfInfoApp']);