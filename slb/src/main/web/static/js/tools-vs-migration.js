var headerInfoApp = angular.module('headerInfoApp', ["angucomplete-alt"]);
headerInfoApp.controller('headerInfoController', function ($scope) {
    $scope.resource=  H.resource;
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
            case 'cert-upgrade': {
                link = "/portal/tools/cert/migrations";
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

var selfInfoApp = angular.module('selfInfoApp', ["angucomplete-alt", "http-auth-interceptor", "ngSanitize"]);
selfInfoApp.controller('selfInfoController', function ($scope, $http, $q) {
    $scope.resource = H.resource;
    var resource = $scope.resource;
    var env = 'uat';

    var log_start = new Date().getTime() - 1000 * 60 * 60;
    var log_end = new Date().getTime() + 1000 * 60 * 60;
    var dashboardStartTime = $.format.date(log_start, 'yyyy-MM-dd HH:mm:ss');
    var dashboardEndTime = $.format.date(log_end, 'yyyy-MM-dd HH:mm:ss');

    var switches = {};

    var newMigrationUrl;
    var updateMigrationUrl;
    var deleteMigrationUrl;
    var dateNow = $.format.date(new Date().getTime(), 'yyyy/MM/dd HH:00');

    var migrationStatusColor = {
        new: 'status-gray',
        test: 'status-blue',
        dns: 'status-red',
        monitor: 'status-pink',
        clean: 'status-yellow',
        delete: 'status-green',
        done: 'status-normal'
    };

    var nav = {
        'New': '新建',
        'Activate': '配置发布',
        'Test': '迁移测试',
        'DNS': 'DNS',
        'Monitor': '持续监控',
        'Clean': '配置清理',
        'Delete': '结束迁移',
        'Done': (angular.equals(resource, {}) ? '已完成': resource.tools.vmigrationEdit.js.msg11)
    };
    var keys = {
        'New': 'New',
        'Activate': 'Activate',
        'Test': 'Test',
        'DNS Switch': 'DNS',
        'Monitor': 'Monitor',
        'Clean': 'Clean',
        'Delete': 'Delete',
        'Done': 'Done'
        // 'Roll': (angular.equals(resource, {}) ? '回退': resource.tools.vmigrationEdit.js.msg36)
    };
    var migrationStatus = {
        待发布: 'new',
        正在测试: 'test',
        DNS切换中: 'dns',
        持续监控: 'monitor',
        配置待清理: 'clean',
        迁移待结束: 'delete',
        已完成: 'done'
    };
    var stepOneKey = keys['New'];
    var stepTwoKey = keys['Activate'];
    var stepThreeKey = keys['Test'];
    var stepFourKey = keys['DNS Switch'];
    var stepFiveKey = keys['Monitor'];
    var stepSixKey = keys['Clean'];
    var stepSevenKey = keys['Delete'];
    var stepEightKey = keys['Roll'];
    var stepNineKey = keys['Done'];

    $scope.data = {
        slbsToChoose: {},
        slbs: {},
        vses: {},
        groups: {},
        apps: {},

        // keyed by slb id
        slbVses: {},
        vsesMeta: {},

        vsGroups: {},

        vsesToBeSelected: {},
        user: {},
        migrationName: '',

        nav: nav,
        steps: {},
        migrations: [],
        cleanJobs: {},
        testCheckedVs: {},
        migrationSummary: {},
        keys: [
            stepOneKey, stepTwoKey, stepThreeKey, stepFourKey, stepFiveKey, stepSixKey, stepSevenKey, stepNineKey
        ]
    };

    $scope.origin = {
        vses: {}
    };
    // steps to be saved and used
    $scope.query = {
        hasLoaded: false,
        slbId: '',
        selectedSlbIndex: '',
        slbType: '',
        status: '',
        availableVses: false
    };
    $scope.queryDate = {};
    $scope.revers = true;
    $scope.order = 'qps';

    // Top summary
    $scope.searchMigrationByVs = function (domain) {
        // search the migrations which has domain vses
        var pair = {};
        pair.timeStamp = new Date().getTime();
        pair.domain = domain;

        H.setData(pair);
    };
    $scope.getMigrationSummary = function () {
        var migrations = $scope.data.migrations;

        var map = _.countBy(migrations, function (v) {
            if (v.state) {
                var status = v.status;
                return status;
            } else {
                return 'removed';
            }
        });
        $scope.data.migrationSummary = map;
    };
    $scope.clickStatus = function (status) {
        var pair = {
            status: status,
            timeStamp: new Date().getTime()
        };
        H.setData(pair);
    };
    // Summary info for the list
    $scope.getSummaryInfo = function () {
        var resource = $scope.resource;
        var status = $scope.query.status;
        if ($scope.query.domain) return (angular.equals(resource, {}) ? '共有<span class="highlight">': resource.tools.vmigrationEdit.js.msg1) + $scope.query.count + (angular.equals(resource, {}) ? '个查询结果</span>': resource.tools.vmigrationEdit.js.msg2);

        var statusText = (angular.equals(resource, {}) ? '正在进行中的': resource.tools.vmigrationEdit.js.msg3);
        switch (status) {
            case 'new': {
                statusText = (angular.equals(resource, {}) ? '待发布': resource.tools.vmigrationEdit.js.msg4);
                break;
            }
            case 'test': {
                statusText = (angular.equals(resource, {}) ? '测试中': resource.tools.vmigrationEdit.js.msg5);
                break;
            }
            case 'dns': {
                statusText = (angular.equals(resource, {}) ? 'DNS切换中': resource.tools.vmigrationEdit.js.msg6);
                break;
            }
            case 'minitor': {
                statusText = (angular.equals(resource, {}) ? '持续监控中': resource.tools.vmigrationEdit.js.msg7);
                break;
            }
            case 'clean': {
                statusText = (angular.equals(resource, {}) ? '配置待清理': resource.tools.vmigrationEdit.js.msg8);
                break;
            }
            case 'delete': {
                statusText = (angular.equals(resource, {}) ? '迁移待结束': resource.tools.vmigrationEdit.js.msg9);
                break;
            }
            case 'removed': {
                statusText = (angular.equals(resource, {}) ? '已完成': resource.tools.vmigrationEdit.js.msg11);
                break;
            }
            default: {
                break;
            }
        }
        return (angular.equals(resource, {}) ? '共有<span class="highlight">': resource.tools.vmigrationEdit.js.msg1) + $scope.query.count + (angular.equals(resource, {}) ? '个': resource.tools.vmigrationEdit.js.msg13) + statusText + (angular.equals(resource, {}) ? '</span> 迁移策略': resource.tools.vmigrationEdit.js.msg14);
    };
    // Status:
    $scope.startNewMigration = function () {
        var hasRight = A.canDo('Flow', 'NEW', '*') || A.canDo('Flow', 'FLOW', '*');
        if (!hasRight) {
            alert((angular.equals(resource, {}) ? '还没有权限，权限申请流程正在建设中...': resource.tools.vmigrationEdit.js.msg15));
            return;
        }
        window.location.href = '/portal/tools/vmigration/edit#?env=' + $scope.env;
    };

    $scope.showStep = function (step) {
        var steps = $scope.data.steps;
        var keys = _.keys(steps);
        return keys.indexOf(step) != -1;
    };
    $scope.getMonitorLink = function () {
        var vses = $scope.data.steps.migration['vses'];

        // groups related
        var groupsRequest = {
            url: G.baseUrl + '/api/groups?type=info&vsId=' + vses.join(','),
            method: 'GET'
        };


        $http(groupsRequest).success(function (response, code) {
            var link = '/portal/user/traffic#?env=' + env;

            var groups = _.pluck(response['groups'], 'id');

            var vsIds = vses.join(',');
            var groupIds = groups.join(',');

            if (vses && vses.length > 0) {
                link += '&vses=' + vsIds;
            }
            if (groups && groups.length > 0) {
                link += '&groups=' + groupIds;
            }
            window.open(link, '_blank');
        });

    };
    $scope.showRevert = function () {
        var steps = $scope.data.steps;
        var keys = _.keys(steps);

        return keys.indexOf(stepTwoKey) != -1;
    };

    $scope.getDecodedTime = function (time) {
        return decodeURIComponent(time);
    };

    function buildChart(chartContainer, id, len) {
        var c = 'col-md-6';

        var chartDom = $('<div class="' + c + '" style="">  <div class="portlet" style="height: 453px; max-height: 453px">' +
            '  <div class="panel-collapse collapse in">' +
            ' <div class="portlet-body traffic-chart" style="height: 453px; max-height: 453px" id="' + id + '"></div>' +
            '</div>' +
            '</div></div>');
        chartContainer.append(chartDom);
    };
    $scope.getDateNow = function () {
        var d = new Date();
        // if (d.getMinutes() < 20) {
        //     d = d.setTime(d.getTime() - 1000 * 60 * 60);
        // }
        d = d.setTime(d.getTime() - 1000 * 60 * 60);
        return encodeURIComponent($.format.date(d, 'yyyy-MM-dd HH:mm:00'));
    };

    $scope.refreshCharts = function () {
        $scope.setDateNow();
    };
    $scope.setDateNow = function () {
        var d = new Date();
        d = d.setTime(d.getTime() - 1000 * 60 * 60);
        H.setData({startTime: encodeURIComponent($.format.date(d, 'yyyy-MM-dd HH:00'))});
        $scope.startRollbackWindow();
    };
    $scope.setDate = function () {
        H.setData({startTime: encodeURIComponent($.format.date($scope.queryDate.startTime, 'yyyy-MM-dd HH:00'))});
    };
    $scope.setDateNextHour = function () {
        H.setData({startTime: encodeURIComponent($.format.date(new Date($scope.queryDate.startTime).getTime() + 60 * 1000 * 60, 'yyyy-MM-dd HH:00'))});
        $scope.startRollbackWindow();
    };
    $scope.setDatePreviousHour = function () {
        H.setData({startTime: encodeURIComponent($.format.date(new Date($scope.queryDate.startTime).getTime() - 60 * 1000 * 60, 'yyyy-MM-dd HH:00'))});
        $scope.startRollbackWindow();
    };
    $scope.isOpen = false;
    $scope.openCalendar = function (e) {
        e.preventDefault();
        e.stopPropagation();
        $scope.isOpen = true;
    };
    $scope.getDecodedTime = function (time) {
        return decodeURIComponent(time);
    };

    $scope.startRollbackWindow = function () {
        $('#confirmRollbackTrafficChanges').modal('show');

        var vses = $scope.data.steps.migration['vses'];

        var source = $scope.data.steps.migration['source-slb'];
        var target = $scope.data.steps.migration['target-slb'];

        $scope.query.vses = vses;

        $('.vs-chart-container').empty();
        var str = '';
        $.each(vses, function (index, vs) {
            str += '<div class="vs-chart-container" id="vs-chart-' + vs + '" style="width: 100%;">' +
                '</div>';
        });

        $('.traffic-types-area').append(str);

        var slbs = $scope.data.slbs;
        // draw for each
        $.each(vses, function (index, vs) {
            var vsChartContainer = $('#vs-chart-' + vs);
            var vsTrafficArea = 'vs-traffic-all' + '-' + vs;
            buildChart(vsChartContainer, vsTrafficArea, vses.length);
            setTimeout(function () {
                vsTrafficService.drawVsAllBySlb(vsTrafficArea, vs, function (slbName) {
                    var name = slbs[slbName] ? slbs[slbName].name : "-";
                    if (slbName == source) {
                        return (angular.equals(resource, {}) ? '源 SLB: ': resource.tools.vmigrationEdit.js.msg16) + slbName + '(' + name + ')'
                    } else if (slbName == target) {
                        return (angular.equals(resource, {}) ? '目标 SLB: ': resource.tools.vmigrationEdit.js.msg17) + slbName + '(' + name + ')'
                    } else {
                        return (angular.equals(resource, {}) ? '其它 SLB: ': resource.tools.vmigrationEdit.js.msg18) + slbName + '(' + name + ')'
                    }
                });
            }, 1000);
        });
    };
    $scope.revertChanges = function () {
        $('#confirmRollbackChanges').modal('show');
    };
    $scope.confirmRevert = function () {
        var stage = $scope.data.steps;
        if (!stage) return;

        var hasCleanStep = stage['Clean'] && stage['Clean']['status'];

        var vses = $scope.data.steps.migration['vses'];

        var targetSlb = $scope.data.steps.migration['target-slb'];
        var sourceSlbId = $scope.data.steps.migration['source-slb'];
        // revert changes
        $scope.loadVsDataBeforeUpdate(function () {
            var updateResults = {};
            var activateResults = {};
            var tempResults = {};

            var updateRequests = _.map(vses, function (v) {
                var origin = $scope.origin.vses[v];

                if (hasCleanStep) {
                    origin['slb-ids'].push(parseInt(sourceSlbId));
                } else {
                    var index = origin['slb-ids'].indexOf(parseInt(targetSlb));

                    origin['slb-ids'].splice(index, 1);
                }
                var request = {
                    method: 'POST',
                    url: G.baseUrl + '/api/vs/update',
                    data: origin
                };

                var sendRequest = $http(request).success(function (response, code) {
                    if (response.code && response.message) {
                        updateResults[v] = response;
                    } else {
                        updateResults[v] = {
                            status: 'Success'
                        };
                    }

                    tempResults[v] = [{
                        task: (angular.equals(resource, {}) ? '更新 VS: ': resource.tools.vmigrationEdit.js.msg19) + $scope.data.vses[v].name + '(' + v + ')',
                        order: 1,
                        result: updateResults[v] || {}
                    }];
                });

                return sendRequest;
            });
            $q.all(updateRequests).then(
                function () {
                    // if the requests all passed?
                    var activateRequests = _.map(vses, function (v) {
                        var request = {
                            method: 'GET',
                            url: G.baseUrl + '/api/activate/vs?vsId=' + v
                        };
                        return $http(request).success(function (response, code) {
                            if (response.code && response.message) {
                                activateResults[v] = response;
                            } else {
                                activateResults[v] = {
                                    status: 'Success'
                                }
                            }

                            tempResults[v].push({
                                task: (angular.equals(resource, {}) ? '激活 VS: ': resource.tools.vmigrationEdit.js.msg35) + $scope.data.vses[v].name + '(' + v + ')',
                                order: 0,
                                result: activateResults[v] || {}
                            });
                        });
                    });
                    $q.all(activateRequests).then(
                        function () {
                            $('.content').hideLoading();
                            // update the binding data
                            var resultsArray = _.flatten(_.values(tempResults));
                            var hasFailure = _.filter(resultsArray, function (s) {
                                return _.keys(s.result).length == 0 || s.result['code'];
                            });
                            var failed = hasFailure && hasFailure.length > 0;
                            // update the status

                            var steps = initFirstStepData(vses, sourceSlbId, targetSlb);
                            if (!failed) {
                                saveMigration($scope.data.steps.id, updateMigrationUrl, steps, function (response) {
                                    $scope.data.steps = steps;
                                    alert('Successfully revert');
                                    H.setData({timeStamp: new Date().getTime()});
                                });
                            } else {
                                // record those failed and retry
                                alert('failed to revert');
                            }

                        }
                    );
                }
            );
        }, $('.content'));
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
    // Select slbids
    $scope.cacheRequestFn = function (str) {
        return {q: str, timestamp: new Date().getTime()};
    };
    $scope.remoteSLBsUrl = function () {
        return G.baseUrl + "/api/meta/slbs";
    };
    $scope.slbIdInputChanged = function (o) {
        $scope.query.slbId = o;
    };
    $scope.selectSlbId = function (o) {
        if (o) {
            $scope.query.slbId = o.originalObject.id;
        }
    };

    //First step: create new
    $scope.selectSourceSlb = function () {
        $('#selectSlbDialog').modal('show');
        $scope.query.slbId = '';
        $scope.query.selectedSlbIndex = '';
        $scope.query.slbType = 'source-slb';

        var slbs = $scope.data.slbs;

        // did target slb selected?
        var existingSlbId = $scope.data.steps.migration['target-slb'];
        var choosen = getSlbsForSelect(slbs, existingSlbId);
        $scope.data.slbsToChoose = choosen;
    };
    $scope.selectTargetSlb = function () {
        $('#selectSlbDialog').modal('show');
        $scope.query.slbId = '';
        $scope.query.selectedSlbIndex = '';
        $scope.query.slbType = 'target-slb';

        var slbs = $scope.data.slbs;
        // did target slb selected?
        var existingSlbId = $scope.data.steps.migration['source-slb'];
        var choosen = getSlbsForSelect(slbs, existingSlbId);
        $scope.data.slbsToChoose = choosen;
    };
    $scope.selectedTileClass = function (k) {
        if ($scope.query.selectedSlbIndex == k) return 'tile-selected2';
    };
    $scope.clickSlb = function (key, event) {
        $scope.query.selectedSlbIndex = key;
    };
    $scope.selectSlb = function () {
        var slbType = $scope.query.slbType;
        $scope.data.steps.migration[slbType] = $scope.query.selectedSlbIndex;
    };
    $scope.disableSlbSelection = function () {
        if ($scope.query.selectedSlbIndex) {
            return false;
        }
        return true;
    };
    $scope.showVsesPanel = function () {
        if (!$scope.data.steps[stepOneKey]) return;
        var vses = $scope.data.steps.migration['vses'];
        var source = $scope.data.steps.migration['source-slb'];
        var target = $scope.data.steps.migration['target-slb'];

        var show = source && target && !vses;
        return show;
    };
    $scope.toggleCheckAll = function () {
        var allChecked = $scope.allChecked;
        var dtext = $scope.domainText;

        var cvses = $scope.data.slbVses[$scope.data.steps.migration['source-slb']];

        var values;

        if (dtext) {
            var param1;

            var starter;
            var fArray = dtext.split(':');
            if (fArray.length == 2) {
                starter = fArray[0];
                param1 = fArray[1];
            } else {
                return cvses;
            }
            var paramArray = param1.split('|');

            var param = _.map(paramArray, function (v) {
                if (v) {
                    return v.trim().toLowerCase();
                } else {
                    return [];
                }
            });

            param = _.flatten(param);

            values = _.filter(cvses, function (v) {
                var id = v.id;

                var idOk = false;
                var hasLikeDomainIn = '';
                var tagIn = '';
                var propertyOk = '';
                var domainOk = '';

                var domains = _.map(v.domains, function (s) {
                    return s.toLowerCase();
                });

                hasLikeDomainIn = _.filter(domains, function (s) {
                    var existed = _.filter(param, function (p) {
                        if (s.indexOf(p) != -1) return true;
                        return false;
                    });
                    return existed && existed.length > 0;
                });


                var tags = _.map(v.tags, function (s) {
                    return s.toLowerCase();
                });
                var properties = _.map(_.pluck(_.values(v['properties']), 'value'), function (s) {
                    return s.toLowerCase();
                });

                tagIn = _.intersection(tags, param);
                propertyOk = _.intersection(properties, param);
                idOk = param.indexOf(id.toString()) != -1;
                domainOk = hasLikeDomainIn && hasLikeDomainIn.length > 0;

                if (starter.toUpperCase() == 'TAG') {
                    return tagIn && tagIn.length > 0;
                }

                if (starter.toUpperCase() == 'PROPERTY') {
                    return propertyOk && propertyOk.length > 0;
                }

                if (starter.toUpperCase() == 'DOMAIN') {
                    return domainOk;
                }
                if (starter.toUpperCase() == 'ID') {
                    return idOk;
                }

                return false;
            });

        } else {
            values = cvses;
        }


        if (!allChecked) {
            // uncheck all
            $scope.data.vsesToBeSelected = [];
        } else {
            // check all
            var fs = _.filter(values, function (v) {
                return v.available && v.statusAvailable;
            });

            var ids = _.pluck(fs, 'id');
            $scope.data.vsesToBeSelected = _.object(ids, ids);
        }
    };
    $scope.toggleSelectedVs = function (vsId, checked) {
        if (checked) {
            $scope.data.vsesToBeSelected[vsId] = vsId;
        } else {
            delete  $scope.data.vsesToBeSelected[vsId];
        }
    };
    $scope.isCurrentVsChecked = function (vsId) {
        return $scope.data.vsesToBeSelected[vsId];
    };
    $scope.disableClassFirstToSecond = function () {
        if (_.keys($scope.data.vsesToBeSelected).length == 0) return true;
        if (!hasRight()) return true;
        return false;
    };
    $scope.vsListClass = function () {
        var available = $scope.query.availableVses;
        if (available) {
            return 'fa fa-toggle-on status-green'
        } else {
            return 'fa fa-toggle-off status-gray'
        }
    };
    $scope.checkedVsListClass = function () {

    };

    $scope.vsListContent = function () {
        var resource = $scope.resource;
        var available = $scope.query.availableVses;
        return available ? (angular.equals(resource, {}) ? '可用': resource.tools.vmigrationEdit.js.msg21) : (angular.equals(resource, {}) ? '全部': resource.tools.vmigrationEdit.js.msg22);
    };
    $scope.toggleVses = function () {
        $scope.query.availableVses = !$scope.query.availableVses;
    };
    $scope.getAvailableContent = function () {
        var resource = $scope.resource;
        var status = $scope.query.availableStatus;
        if (status) {
            return (angular.equals(resource, {}) ? '撤销全部可用': resource.tools.vmigrationEdit.js.msg23);
        } else {
            return (angular.equals(resource, {}) ? '设置全部可用': resource.tools.vmigrationEdit.js.msg24);
        }
    };

    function initFirstStepData(vses, sourceSlbId, targetSlbId) {
        var steps = {
            migration: {}
        };
        steps[stepOneKey] = {};

        steps.migration['vses'] = vses;
        steps.migration['source-slb'] = sourceSlbId;
        steps.migration['target-slb'] = targetSlbId;

        steps[stepOneKey]['startTime'] = dateNow;
        steps[stepOneKey]['finishTime'] = dateNow;
        steps[stepOneKey]['status'] = 'Success';
        steps[stepOneKey]['message'] = 'Success';

        var timestamp = new Date().getTime();
        $scope.data.migrationName = "VS_Migration_From_" + sourceSlbId + '_TO_' + targetSlbId + '_' + timestamp;

        steps.status = migrationStatus[(angular.equals(resource, {}) ? '待发布': resource.tools.vmigrationEdit.js.msg4)];
        steps[stepTwoKey] = {};

        return steps;
    }

    $scope.firstToSecondClick = function () {
        // change the data
        var selectedVses = _.keys($scope.data.vsesToBeSelected);
        var sourceSlbId = $scope.data.steps.migration['source-slb'];
        var targetSlbId = $scope.data.steps.migration['target-slb'];

        $scope.data.steps = initFirstStepData(selectedVses, sourceSlbId, targetSlbId);

        var url = $scope.data.steps.id ? updateMigrationUrl : newMigrationUrl;
        saveMigration($scope.data.steps.id, url, $scope.data.steps, function (response) {
            $scope.data.steps.id = response.id;
            H.setData({migrationId: response.id});
        });
    };


    $scope.getConfigActivateClass = function (v) {
        if (!v) return 'status-red';

        var result = v.result;
        if (_.keys(result).length == 0) {
            return 'status-red';
        }
        if (result.code) {
            return 'status-red';
        }
        if (!result) return 'status-red';
        return 'status-green';
    };

    $scope.getCleanFailedCount = function () {
        if (!$scope.data.steps.migration) return;
        var allResults = $scope.data.steps.migration['cleanResult'];
        var failedCount = 0;

        _.mapObject(allResults, function (v, k, item) {
            var failed = _.filter(v, function (f) {
                return f['result']['status'] != 'Success';
            });
            if (failed && failed.length > 0) {
                failedCount++;
            }
        });

        return failedCount;
    };

    $scope.getDeployResultState = function () {
        if (!$scope.data.steps.migration) return;
        var allResults = $scope.data.steps.migration['deployResult'];
        var failedCount = 0;

        _.mapObject(allResults, function (v, k, item) {
            var failed = _.filter(v, function (f) {
                return f['result']['status'] != 'Success';
            });
            if (failed && failed.length > 0) {
                failedCount++;
            }
        });

        return failedCount;
    };
    $scope.exportDomains = function () {
        var name = 'test.txt';

        if (!$scope.data.steps.migration) return;

        var targetVses = $scope.data.steps.migration['vses'];
        if (!targetVses) return;

        var sourceSlbId = $scope.data.steps.migration['source-slb'];
        var targetSlbId = $scope.data.steps.migration['target-slb'];

        var slbs = $scope.data.slbs;
        var vses = $scope.data.vses;


        var sourceVip = slbs[sourceSlbId]['vips'].join('/');
        var targetVip = slbs[targetSlbId]['vips'].join('/');

        var domainText = '';

        _.map(targetVses, function (v) {
            var ds = vses[v].domains;
            var dtext = ds.join('\r\n');

            domainText += dtext;
            domainText += '\r\n\r\n';
        });

        //var data  = document.querySelector('#text').value;
        var result = "From: " + sourceVip + "; To:" + targetVip + '\r\n\r\n';

        result += 'Domains: \r\n';
        result += domainText;


        var urlObject = window.URL || window.webkitURL || window;
        var export_blob = new Blob([result]);
        var save_link = document.createElementNS("http://www.w3.org/1999/xhtml", "a")
        save_link.href = urlObject.createObjectURL(export_blob);
        save_link.download = name;

        var ev = document.createEvent("MouseEvents");
        ev.initMouseEvent("click", true, false, window, 0, 0, 0, 0, 0, false, false, false, false, 0, null);
        save_link.dispatchEvent(ev);
    };

    $scope.getVsDnsCheckFailedCount = function () {
        if (!$scope.data.steps.migration) return;

        var targetSlbId = $scope.data.steps.migration['target-slb'];

        if (!targetSlbId) return;

        var slbs = $scope.data.slbs;

        var targetVip = [];

        if (slbs[targetSlbId]) {
            targetVip = slbs[targetSlbId]['vips'];
        }

        var dnsFailedVsCount = 0;

        var checkResults = $scope.data.steps.migration['dnsResult'];
        _.mapObject(checkResults, function (v, k, item) {
            var vips = _.pluck(v['value'], 'ip');
            vips = _.uniq(vips);
            var u = _.union(vips, targetVip);
            if (u.length != targetVip.length) {
                dnsFailedVsCount++;
            }
            // if there is vips not target vip
        });
        return dnsFailedVsCount;
    };

    $scope.getConfigActivateResult = function (v) {
        if (!v) return;

        var result = v.result;
        if (_.keys(result).length == 0) {
            return (angular.equals(resource, {}) ? '无法获取操作结果': resource.tools.vmigrationEdit.js.msg25)
        }
        if (result.code) {
            var s = (angular.equals(resource, {}) ? '操作失败, 原因：': resource.tools.vmigrationEdit.js.msg26) + result.code;
            var m = (angular.equals(resource, {}) ? '<a>(查看详情)</a>': resource.tools.vmigrationEdit.js.msg27);

            return s + m;
        }
        if (!result) return (angular.equals(resource, {}) ? '无法获取操作结果': resource.tools.vmigrationEdit.js.msg25);
        return (angular.equals(resource, {}) ? '成功': resource.tools.vmigrationEdit.js.msg29);
    };

    $scope.showErrorMessage = function (v) {
        if (!v) return;

        var result = v.result;
        if (result.message) return true;
        return false;
    };
    $('.fail-reason-class').click(function (e) {
        e.preventDefault();
        var title = $(this).attr('title');
        alert(title);
    });

    $scope.retryFailedPublish = function () {
        var results = $scope.data.steps.migration['deployResult'];
        // find those failed
        var failed = [];
        _.mapObject(results, function (v, k, item) {
            var successCount = _.filter(v, function (s) {
                return s['result']['status'] && s['result']['status'] == 'Success'
            });

            if (successCount && successCount.length == 2) {
                // do nothing
            } else {
                failed.push(k);
                delete $scope.data.steps.migration['deployResult'][k];
            }
        });

        $scope.secondToThirdClick(failed);
    };
    $scope.secondToThirdClick = function (vs) {
        if (!hasRight()) return true;

        var vses = $scope.data.steps.migration['vses'];
        if (vs) {
            if (vs.length > 0) {
                vses = vs;
            } else {
                alert((angular.equals(resource, {}) ? '没有失败的task，安全推出': resource.tools.vmigrationEdit.js.msg31));
                return;
            }
        }

        // bind vses
        $('.activate-row').showLoading();
        var targetSlb = $scope.data.steps.migration['target-slb'];
        var sourceSlb = $scope.data.steps.migration['source-slb'];
        var vsesDividing = _.chunk(vses, 200);
        var requests = _.map(vsesDividing, function (v) {
            var vsIds = _.map(v, function (vid) {
                return 'vsId=' + vid
            });
            var request = {
                method: 'GET',
                url: G.baseUrl + '/api/flow/vs/migration/bindSlb?sourceSlbId=' + sourceSlb + '&targetSlbId=' + targetSlb + '&' + vsIds.join('&')
            };
            return $http(request).success(function (response, code) {
            });
        });

        $q.all(requests).then(function () {
            $('.activate-row').hideLoading();
            $scope.data.steps.migration['deployResult'] = {};

            getVsMigrationStatus(
                function (failed) {
                    $scope.data.steps[stepTwoKey].status = !failed ? 'Success' : 'Fail';
                    $scope.data.steps[stepTwoKey]['startTime'] = dateNow;
                    $scope.data.steps[stepTwoKey]['finishTime'] = dateNow;

                    saveMigration($scope.data.steps.id, updateMigrationUrl, $scope.data.steps, function (response) {
                        $('.activate-row').hideLoading();

                        if (!failed) {
                            $('#loadingArea').showLoading();
                            $scope.data.steps.status = migrationStatus['正在测试'];
                            $scope.data.steps[stepThreeKey] = {
                                'startTime': dateNow
                            };
                            saveMigration($scope.data.steps.id, updateMigrationUrl, $scope.data.steps, function (response) {
                                $scope.testPassed = false;
                                $scope.startHealthCheck($('#loadingArea'));
                            });
                        }
                    });
                },
                $scope.data.steps.migration['vses'],
                parseInt(targetSlb),
                $scope.data.steps.migration['deployResult'],
                'publish'
            )
        });
    };

    // read all the vses data to determine this update status
    function getVsMigrationStatus(callBack, vsesDoing, targetSlbId, migration, what) {

        var request = {
            url: G.baseUrl + '/api/vses?type=extended&mode=online',
            method: 'GET'
        };

        $http(request).success(function (response, code) {
            if (code == 200) {
                var vses = response['virtual-servers'];
                var vsesObj = _.indexBy(vses, 'id');
                vsesObj = _.mapObject(vsesObj, function (v, k, item) {
                    var ps = _.indexBy(v['properties'], 'name');
                    return {
                        name: v['name'],
                        slbs: v['slb-ids'],
                        status: ps['status'] ? ps['status']['value'] : ''
                    }
                });

                var hasFailed = false;
                _.map(vsesDoing, function (t) {
                    var vs = vsesObj[t];
                    if (vs) {
                        var containsSlb;
                        if (what == 'publish') {
                            containsSlb = vs.slbs.indexOf(targetSlbId) != -1;
                        } else {
                            // clean
                            containsSlb = vs.slbs.indexOf(targetSlbId) == -1;
                        }


                        var activated = vs.status ? vs.status.toLowerCase() == 'activated' : false;

                        var updateResults;
                        var activateResults;
                        var tempResults = [];
                        if (containsSlb && activated) {
                            updateResults = {
                                status: 'Success'
                            };
                            activateResults = {
                                status: 'Success'
                            };
                        } else {
                            hasFailed = true;

                            updateResults = {
                                message: (angular.equals(resource, {}) ? '失败': resource.tools.vmigrationEdit.js.msg32)
                            };
                            activateResults = {
                                message: (angular.equals(resource, {}) ? '失败': resource.tools.vmigrationEdit.js.msg32)
                            };
                        }

                        tempResults = [{
                            task: (angular.equals(resource, {}) ? '更新 VS: ': resource.tools.vmigrationEdit.js.msg19) + vsesObj[t].name + '(' + t + ')',
                            order: 1,
                            result: updateResults
                        }];

                        tempResults.push({
                            task: (angular.equals(resource, {}) ? '激活 VS: ': resource.tools.vmigrationEdit.js.msg35) + vsesObj[t].name + '(' + t + ')',
                            order: 1,
                            result: activateResults
                        });

                        migration[t] = tempResults;
                    }
                });

                callBack(hasFailed);
            }
        });
    };

    $scope.showRetryDeploy = function (k, index) {
        var key;

        if (index == 1) key = stepTwoKey;
        if (index == 2) key = stepSixKey;
        if (index == 3) key = stepEightKey;

        var step = $scope.data.steps[key];
        var passed = step && step['status'].toLowerCase() == 'success';
        if (passed) return false;
        return true;
    };
    $scope.activateHasFailures = function (index) {
        var stepResult;

        if (index == 1) {
            stepResult = _.flatten(_.values($scope.data.steps.migration['deployResult']));
        }
        if (index == 2) {
            stepResult = _.flatten(_.values($scope.data.steps.migration['cleanResult']));
        }
        //
        // if (index == 2) key = stepSixKey;
        // if (index == 3) key = stepEightKey;

        var hasFailure = _.filter(stepResult, function (s) {
            return _.keys(s.result).length == 0 || s.result['code'];
        });
        return hasFailure && hasFailure.length > 0;
    };

    $scope.thirdToFourthClick = function () {
        if (!hasRight()) return true;

        $scope.data.steps[stepThreeKey].status = 'Success';
        $scope.data.steps[stepThreeKey]['endTime'] = dateNow;

        $scope.data.steps.status = migrationStatus[(angular.equals(resource, {}) ? 'DNS切换中': resource.tools.vmigrationEdit.js.msg6)];
        $scope.data.steps[stepFourKey] = {
            "startTime": dateNow
        };
        saveMigration($scope.data.steps.id, updateMigrationUrl, $scope.data.steps, function (response) {
            $scope.dnsPassed = false;
            $('.dns-check-div').showLoading();
            // start the dns checking
            checkDns($scope.data.steps, $('.dns-check-div'));
        });
    };
    $scope.dnsCheck = function () {
        var area = $('.dns-row');
        area.showLoading();
        checkDns($scope.data.steps, area);
    };
    $scope.dnsSwitch = function () {
        var migrationId = $scope.query.migrationId;
        window.open('/portal/tools/vmigration/dns-switch#?env=' + $scope.env + "&migrationId=" + migrationId, '_blank');
    };
    $scope.getDnsClass = function (ip) {
        if (!$scope.data.steps.migration) return;

        var targetSlb = $scope.data.steps.migration['target-slb'];

        var targetVips = [];
        if ($scope.data.slbs[targetSlb]) {
            targetVips = $scope.data.slbs[targetSlb].vips;
        }

        if (targetVips.indexOf(ip) != -1) return 'fa-check-square status-green';

        return 'fa-times status-red';
    };

    $scope.bindHostDialog = function () {
        $('#bindHostDialog').modal('show');
    };
    $scope.getTargetSlbVip = function () {
        var targetSlbId = $scope.data.steps[stepOneKey]['target-slb'];
        var slbs = $scope.data.slbs;
        if (slbs && _.keys(slbs).length > 0) {
            var targetSlbVips = $scope.data.slbs[targetSlbId].vips;
            return targetSlbVips[0];
        }
        return '';

    };
    $scope.disbableDnsPassed = function () {
        var steps = $scope.data.steps;
        return steps[stepFiveKey] || $scope.data.steps[stepNineKey];
    };

    $scope.data.currentDnsSwitchVses = [];
    $scope.startSwitch = function (vsId) {
        $scope.data.currentDnsSwitchVses = [vsId];
        $('#tobeswitcheddialog').modal('show');
    };

    $scope.revertSwitch = function (vsId) {
        $scope.data.currentDnsSwitchVses = [vsId];
        $('#tobeswitchedbackdialog').modal('show');
    };

    $scope.deleteSwitch = function (vsId) {
        $scope.data.currentDnsSwitchVses = [vsId];
        $('#tobedeleteddialog').modal('show');
    };

    $scope.confirmDeleteSwith = function () {
        var vs = $scope.data.currentDnsSwitchVses;
        var vsId = parseInt(vs[0]);
        var targetDnsId;
        var dns = $scope.data.dnsDetails;
        if (!dns || _.keys(dns) == 0) return;
        _.pick(dns, function (v, k, i) {
            var vsIds = v['vs-ids'];
            if (!vsIds) return false;
            var isVs = vsIds.indexOf(vsId) != -1;
            if (isVs) targetDnsId = k;
            return isVs;
        });

        var switchId = targetDnsId;

        removeSwitch(switchId);
    };

    $scope.closeDeleteSwitchDialog = function () {
        reloadDns();
    };

    $scope.startRevertSwitch = function () {
        var vs = $scope.data.currentDnsSwitchVses;
        var vsId = parseInt(vs[0]);
        var targetDnsId;
        var dns = $scope.data.dnsDetails;
        if (!dns || _.keys(dns) == 0) return;
        _.pick(dns, function (v, k, i) {
            var vsIds = v['vs-ids'];
            if (!vsIds) return false;
            var isVs = vsIds.indexOf(vsId) != -1;
            if (isVs) targetDnsId = k;
            return isVs;
        });

        var switchId = targetDnsId;
        rollbackSwitch(switchId);
    };

    $scope.confirmSwitch = function () {
        var tobeswitchedVses = $scope.data.currentDnsSwitchVses;

        // find if there is switch about this vs
        var vsId = parseInt(tobeswitchedVses[0]);
        var targetDnsId;
        var dns = $scope.data.dnsDetails;
        if (dns && _.keys(dns) > 0) {
            _.pick(dns, function (v, k, i) {
                var vsIds = v['vs-ids'];
                if (!vsIds) return false;
                var isVs = vsIds.indexOf(vsId) != -1;
                if (isVs) targetDnsId = k;
                return isVs;
            });
        }
        var switchId = targetDnsId;

        if (!switchId) {
            // no switch about this migration
            createSwith(tobeswitchedVses);
        } else {
            // execute switch
            executeSwith(switchId);
        }
    };

    $scope.getDomainClass = function (domain, vsId) {
        vsId = parseInt(vsId);
        var targetDnsId;
        var dns = $scope.data.dnsDetails;
        if (!dns || _.keys(dns) == 0) return;
        var sw = _.pick(dns, function (v, k, i) {
            var vsIds = v['vs-ids'];
            if (!vsIds) return false;
            var isVs = vsIds.indexOf(vsId) != -1;
            if (isVs) targetDnsId = k;
            return isVs;
        });

        if (!sw || _.keys(sw) == 0) return '';

        sw = sw[targetDnsId];

        var switchStatus = sw['status'];
        var reverting = ['ROLLBACK'];
        var taskStatus = sw['task-status'];
        if (reverting.indexOf(switchStatus) != -1) {
            taskStatus = sw['rollback-task-status'];
        }
        if (!taskStatus) return '';

        var status = taskStatus[domain];
        switch (status) {
            case (angular.equals(resource, {}) ? '已完成': resource.tools.vmigrationEdit.js.msg11):
                return 'status-green fa-check';
            case '已接受':
                return 'status-red fa-handshake-o';
            case '已受理':
                return 'status-red fa-hourglass-half';
            case (angular.equals(resource, {}) ? '已暂停': resource.tools.vmigrationEdit.js.msg42):
                return 'status-red fa-hand-paper-o';
            case '已失败':
                return 'status-red fa-window-close';
        }
    };
    $scope.getDomainStatusText = function (domain, vsId) {
        vsId = parseInt(vsId);
        var targetDnsId;
        var dns = $scope.data.dnsDetails;
        if (!dns || _.keys(dns) == 0) return;
        var sw = _.pick(dns, function (v, k, i) {
            var vsIds = v['vs-ids'];
            if (!vsIds) return false;
            var isVs = vsIds.indexOf(vsId) != -1;
            if (isVs) targetDnsId = k;
            return isVs;
        });

        if (!sw || _.keys(sw) == 0) return '';

        sw = sw[targetDnsId];

        var switchStatus = sw['status'];
        var reverting = ['ROLLBACK'];

        var taskStatus = sw['task-status'];
        if (reverting.indexOf(switchStatus) != -1) {
            taskStatus = sw['rollback-task-status'];
        }
        if (!taskStatus) return '';

        var status = taskStatus[domain];

        if (!status) return;

        if (reverting.indexOf(switchStatus) != -1) {
            status = (angular.equals(resource, {}) ? '回退': resource.tools.vmigrationEdit.js.msg36) + status;
        } else {
            status = (angular.equals(resource, {}) ? '切换': resource.tools.vmigrationEdit.js.msg37) + status;
        }

        return status ? status : '';
    };

    $scope.getSwitchText = function (vsId) {
        vsId = parseInt(vsId);
        var targetDnsId;
        var dns = $scope.data.dnsDetails;
        if (!dns || _.keys(dns) == 0) return;
        var sw = _.pick(dns, function (v, k, i) {
            var vsIds = v['vs-ids'];
            if (!vsIds) return false;
            var isVs = vsIds.indexOf(vsId) != -1;
            if (isVs) targetDnsId = k;
            return isVs;
        });

        if (!sw || _.keys(sw) == 0) return '-';

        sw = sw[targetDnsId];

        var status = sw.status;

        switch (status) {
            case 'CREATED': {
                return (angular.equals(resource, {}) ? 'DNS待切换': resource.tools.vmigrationEdit.js.msg38);
            }
            case 'SWITCH': {
                return (angular.equals(resource, {}) ? '正在切换...': resource.tools.vmigrationEdit.js.msg39);
            }
            case 'FINISHED_SWITCH': {
                return (angular.equals(resource, {}) ? '切换完成': resource.tools.vmigrationEdit.js.msg41);
            }
            case 'DISABLED': {
                return (angular.equals(resource, {}) ? '已暂停': resource.tools.vmigrationEdit.js.msg42);
            }
            case 'ROLLBACK': {
                return (angular.equals(resource, {}) ? '回退中...': resource.tools.vmigrationEdit.js.msg43);
            }
            default:
                return '-'
                break;
        }
    };

    $scope.getSwitchClass = function (vsId) {
        vsId = parseInt(vsId);
        var targetDnsId;
        var dns = $scope.data.dnsDetails;
        if (!dns || _.keys(dns) == 0) return;
        var sw = _.pick(dns, function (v, k, i) {
            var vsIds = v['vs-ids'];
            if (!vsIds) return false;
            var isVs = vsIds.indexOf(vsId) != -1;
            if (isVs) targetDnsId = k;
            return isVs;
        });

        if (!sw || _.keys(sw) == 0) return '';

        sw = sw[targetDnsId];

        if (!sw) return;

        var status = sw.status;

        switch (status) {
            case 'CREATED': {
                return 'status-red';
            }
            case 'SWITCH': {
                return 'status-yellow';
            }
            case 'FINISHED_SWITCH': {
                return 'status-green';
            }
            case 'DISABLED': {
                return 'status-gray';
            }
            case 'ROLLBACK': {
                return 'status-yellow';
            }
            default:
                break;
        }
    };

    $scope.showStartSwitch = function (vsId) {
        vsId = parseInt(vsId);
        var targetDnsId;
        var dns = $scope.data.dnsDetails;
        if (!dns || _.keys(dns) == 0) return true;
        var sw = _.pick(dns, function (v, k, i) {
            var vsIds = v['vs-ids'];
            if (!vsIds) return false;
            var isVs = vsIds.indexOf(vsId) != -1;
            if (isVs) targetDnsId = k;
            return isVs;
        });

        var nothas = !sw || _.keys(sw) == 0;

        if (nothas) return true;

        sw = sw[targetDnsId];

        // has but status is reverted
        var swStatus = sw['status'] == 'CREATED';

        if (swStatus) return true;

        return false;
    };
    $scope.showRevertSwitch = function (vsId) {
        vsId = parseInt(vsId);
        var targetDnsId;
        var dns = $scope.data.dnsDetails;
        if (!dns || _.keys(dns) == 0) return;
        var sw = _.pick(dns, function (v, k, i) {
            var vsIds = v['vs-ids'];
            if (!vsIds) return false;
            var isVs = vsIds.indexOf(vsId) != -1;
            if (isVs) targetDnsId = k;
            return isVs;
        });

        var nothas = !sw || _.keys(sw) == 0;

        if (nothas) return;

        sw = sw[targetDnsId];

        var swStatus = sw['status'] == 'SWITCH' || sw['status'] == 'FINISHED_SWITCH';

        if (!nothas && swStatus) return true;
        return false;
    };

    $scope.showDeleteSwitch = function (vsId) {
        vsId = parseInt(vsId);
        var targetDnsId;
        var dns = $scope.data.dnsDetails;
        if (!dns || _.keys(dns) == 0) return;
        var sw = _.pick(dns, function (v, k, i) {
            var vsIds = v['vs-ids'];
            if (!vsIds) return false;
            var isVs = vsIds.indexOf(vsId) != -1;
            if (isVs) targetDnsId = k;
            return isVs;
        });

        var nothas = !sw || _.keys(sw) == 0;

        if (nothas) return;

        sw = sw[targetDnsId];

        var swStatus = true;

        if (!nothas && swStatus) return true;
        return false;
    };

    $scope.getVses = function (targets) {
        if (!targets) return;
        var vses = $scope.data.vses;

        if (!vses || _.keys(vses).length == 0) return;

        var results = [];
        _.map(targets, function (v) {
            if (vses[v]) results.push(vses[v]);
        });

        return results.sort(function (a, b) {
            return b.qps - a.qps;
        });
    };

    $scope.generateMonitorUrl = function (type, vsId, domains) {
        var dashboard;
        switch (type) {
            case 'dashboard': {
                var env = $scope.env;
                env = env == 'pro' ? 'PROD' : env.toUpperCase();
                dashboard = G.dashboardportal + '/#env=' + env + '&metric-name=slb.req.count&interval=1m&start-time=' + dashboardStartTime + '&end-time=' + dashboardEndTime + '&chart=line&aggregator=sum&ts=1515983913677&tags={"vsid":["' + vsId + '"]}&group-by=[status]';
            }
        }

        if (dashboard) {
            window.open(dashboard, '_blank');
        }
    };

    function updateMigrationWithDnsSwith(dnsId, isRemoving) {
        var migrationId = $scope.query.migrationId;

        var request = {
            method: 'GET',
            url: G.baseUrl + '/api/tools/vs/migrate?migrationId=' + migrationId
        };

        $http(request).success(function (response, code) {
            if (code == 200) {
                var migration = JSON.parse(response['content']);

                if (!migration['DNS']['dns-switch']) {
                    migration['DNS']['dns-switch'] = [dnsId];
                } else {
                    // if existed remove and save
                    migration['DNS']['dns-switch'] = _.reject(migration['DNS']['dns-switch'], function (v) {
                        return v == dnsId;
                    });
                    if (!isRemoving) {
                        migration['DNS']['dns-switch'].push(dnsId);
                    }
                }

                response['content'] = JSON.stringify(migration);

                // update migration
                var saveRequest = {
                    method: 'POST',
                    url: G.baseUrl + '/api/tools/vs/migrate/update',
                    data: response
                };

                $http(saveRequest).success(function (response2, code2) {
                    if (code2 == 200) {
                        reloadDns();
                    } else {
                        alert('Failed to save the migration with id:' + migrationId + ', error message: ' + response2.message);
                    }
                });
            } else {
                alert('Failed to get the migration with id:' + migrationId + ', error message: ' + response.message);
            }
        });
    }


    // 3rd step: health checker
    $scope.redoHealthCheck = function () {
        $('#testArea').showLoading();
        $scope.startHealthCheck($('#testArea'));
    };
    $scope.getHealthCheckUrl = function (group, vs) {

        var targetSlb = $scope.data.steps["migration"]['target-slb'];
        var vip = $scope.data.slbs[targetSlb].vips[0];

        var uri = group.healthUrl;
        var ssl = vs.ssl ? 'HTTPS' : 'HTTP';

        var url = ssl + '://' + vip + uri;
        window.open(url);
    };
    $scope.startHealthCheck = function (area) {
        var vses = $scope.data.steps.migration['vses'];
        var requests = [];
        var targetSlb = $scope.data.steps.migration['target-slb'];
        var vip = $scope.data.slbs[targetSlb].vips[0];
        var healthCheckResults = {};


        var requestData = {
            targets: []
        };

        $.each(vses, function (index, vsId) {
            if (!$scope.data.vses[vsId]) return;
            var ssl = $scope.data.vses[vsId].ssl;
            var domain = $scope.data.vses[vsId].domains[0];
            var vsGroups = $scope.data.vsGroups[vsId];
            if (!vsGroups) {
                return;
            }
            var groups = $scope.data.groups;

            $.each(vsGroups, function (i, v) {
                requestData.targets.push(
                    {
                        "protocol": ssl ? 'HTTPS' : 'HTTP',
                        "ip": vip,
                        "host": domain,
                        "port": ssl ? 443 : 80,
                        "uri": groups[v].healthUrl,
                        "vs-id": vsId,
                        "group-id": v
                    }
                );
            });
        });
        var request = $http({
            method: 'POST',
            url: '/api/tools/check/batch',
            data: requestData
        }).success(function (response, code) {

            var statuses = response['statuses'];

            if (statuses && statuses.length > 0) {
                _.map(statuses, function (s) {
                    var vsId = s['vs-id'];
                    var groupId = s['group-id'];

                    if (!healthCheckResults[vsId]) {
                        healthCheckResults[vsId] = {};
                    }
                    healthCheckResults[vsId][groupId] = s;
                });
            }
        });

        requests.push(request);

        $q.all(requests).then(
            function () {
                area.hideLoading();
                if (_.keys(healthCheckResults).length > 0) {
                    $scope.data.steps.migration['testResult'] = healthCheckResults;

                    var allStatuses = [];
                    $.each(healthCheckResults, function (key, item) {
                        allStatuses = allStatuses.concat(_.values(item));
                    });
                    var hasFailed = _.filter(allStatuses, function (v) {
                        return v.code != 200 && v.code != 302 && v.code != 304;
                    });

                    if (hasFailed && hasFailed.length > 0) {
                        return;
                    }
                    $scope.thirdToFourthClick();
                }
            }
        );
    };
    $scope.getDnsOperationMail = function () {
        if (!$scope.data.steps || !$scope.data.steps[stepOneKey]) {
            return;
        }
        var sourceSlb = $scope.data.steps[stepOneKey]['source-slb'];
        var targetSlb = $scope.data.steps[stepOneKey]['target-slb'];
        var vses = $scope.data.steps[stepOneKey]['vses'];

        if (!$scope.data.slbs || !$scope.data.slbs[sourceSlb] || !$scope.data.slbs[targetSlb]) return;
        var sourceSlbVips = $scope.data.slbs[sourceSlb].vips.join(',');
        var targetSlbVips = $scope.data.slbs[targetSlb].vips.join(',');

        var vsesStr = '';
        $.each(vses, function (i, v) {
            vsesStr += 'Virtual Server Id:' + v + '\n';
            $.each($scope.data.vses[v].domains, function (j, s) {
                vsesStr += s + ';';
            });
            vsesStr += '\n';
        });

        var slbsStr = '源SLB(vips)：' + sourceSlb + '(' + sourceSlbVips + ')\n';
        slbsStr += '目标SLB(vips)：' + targetSlb + '(' + targetSlbVips + ')\n';

        var body = '我们正通过SLB Portal迁移以下域名:\n' + vsesStr + slbsStr + (angular.equals(resource, {}) ? ',请协助切换DNS。 完成后请告知我们。谢谢!': resource.tools.vmigrationEdit.js.msg45);

        return 'mailto:test@Ctrip.com?subject=域名迁移，请协助切换DNS&body=' + encodeURIComponent(body) + '';
    };
    $scope.fourthToFifthClick = function () {
        if (!hasRight()) return true;

        $scope.data.steps[stepFourKey].status = 'Success';
        $scope.data.steps[stepFourKey]['endTime'] = dateNow;

        $scope.monitorPassed = false;
        $scope.data.steps.status = migrationStatus['持续监控'];
        $scope.data.steps[stepFiveKey] = {
            'startTime': dateNow
        };

        saveMigration($scope.data.steps.id, updateMigrationUrl, $scope.data.steps, function (response) {
        });
    };

    $scope.fifthToSixthClick = function () {
        if (!hasRight()) return true;

        $scope.data.steps[stepFiveKey].status = 'Success';
        $scope.data.steps[stepFiveKey]['endTime'] = dateNow;

        $scope.data.steps.status = migrationStatus[(angular.equals(resource, {}) ? '配置待清理': resource.tools.vmigrationEdit.js.msg8)];
        $scope.allDeleted = false;
        $scope.data.steps[stepSixKey] = {
            startTime: dateNow
        };

        saveMigration($scope.data.steps.id, updateMigrationUrl, $scope.data.steps, function (response) {
        });
    };

    $scope.allDeleted = false;

    $scope.retryFailedClean = function () {
        var results = $scope.data.steps.migration['cleanResult'];
        // find those failed
        var failed = [];
        _.mapObject(results, function (v, k, item) {
            var successCount = _.filter(v, function (s) {
                return s['result']['status'] && s['result']['status'] == 'Success'
            });

            if (successCount && successCount.length == 2) {
                // do nothing
            } else {
                failed.push(k);
                delete $scope.data.steps.migration['cleanResult'][k];
            }
        });

        $scope.allowToCleanClick(failed);
    };
    $scope.allowToCleanClick = function (vs) {
        $('.clean-row').showLoading();

        //unbind vses
        if (!hasRight()) return true;

        var vses = $scope.data.steps.migration['vses'];
        if (vs) {
            if (vs.length > 0) {
                vses = vs;
            } else {
                alert((angular.equals(resource, {}) ? '没有失败的task，安全推出': resource.tools.vmigrationEdit.js.msg31));
                return;
            }
        }

        var targetSlb = $scope.data.steps.migration['target-slb'];
        var sourceSlb = $scope.data.steps.migration['source-slb'];
        var vsesDividing = _.chunk(vses, 200);
        var requests = _.map(vsesDividing, function (v) {
            var vsIds = _.map(v, function (vid) {
                return 'vsId=' + vid
            });
            var request = {
                method: 'GET',
                url: G.baseUrl + '/api/flow/vs/migration/unbindSlb?sourceSlbId=' + sourceSlb + '&targetSlbId=' + targetSlb + '&' + vsIds.join('&')
            };
            return $http(request).success(function (response, code) {
            });
        });

        $q.all(requests).then(function () {
            $('.clean-row').hideLoading();

            $scope.data.steps.migration['cleanResult'] = {};

            getVsMigrationStatus(
                function (failed) {
                    var status = failed ? '' : 'Success';
                    $scope.data.steps[stepSixKey].status = status;
                    if (!failed) {
                        $scope.allDeleted = true;
                        $scope.data.steps.status = migrationStatus[(angular.equals(resource, {}) ? '迁移待结束': resource.tools.vmigrationEdit.js.msg9)];
                    }
                    saveMigration($scope.data.steps.id, updateMigrationUrl, $scope.data.steps, function (response) {
                    });
                },
                $scope.data.steps.migration['vses'],
                parseInt(sourceSlb),
                $scope.data.steps.migration['cleanResult'],
                'clean'
            );
        });
    };

    $scope.sixthToSeventhClick = function () {
        $scope.data.steps[stepSixKey]['endTime'] = dateNow;

        $scope.data.steps[stepSevenKey] = {
            startTime: dateNow,
            endTime: dateNow
        };

        saveMigration($scope.data.steps.id, updateMigrationUrl, $scope.data.steps, function (response) {
        });
    };
    $scope.removeMigration = function () {

        $scope.data.steps[stepNineKey] = {};
        $scope.data.steps[stepNineKey]['createdTime'] = dateNow;
        $scope.data.steps[stepNineKey]['endTime'] = dateNow;
        $scope.data.steps.status = migrationStatus[(angular.equals(resource, {}) ? '已完成': resource.tools.vmigrationEdit.js.msg11)];

        saveMigration($scope.data.steps.id, updateMigrationUrl, $scope.data.steps, function (response) {
            deleteMigration($scope.data.steps["id"], function (response) {
                $('#newVsMigrationDialog').modal('hide');
            })
        });
    };

    // Save
    $scope.saveCurrentMigraion = function () {
        $scope.data.mocks.push($scope.data.steps);
        var pair = {};
        pair.timeStamp = new Date().getTime();
        H.setData(pair);
    };
    $scope.openPropertyOfMigration = function (mock) {
        var migrationId = mock.id;
        window.location.href = '/portal/tools/vmigration/edit#?env=' + $scope.env + '&migrationId=' + migrationId;
    };

    // List migrtaions
    $scope.getMigrations = function (input) {
        var slbs = $scope.data.slbs;
        var vses = $scope.data.vses;

        if (!input || input.length == 0 || !slbs || _.keys(slbs).length == 0 || !vses || _.keys(vses).length == 0) return [];

        var map = _.map(input, function (input) {
            var id = input.id;
            var name = input.name;
            var contentJson = JSON.parse(input.content);
            contentJson.id = id;

            var sourceSlbId = contentJson.migration['source-slb'];
            var targetSlbId = contentJson.migration['target-slb'];

            var sourceSlbName = slbs[sourceSlbId] ? slbs[sourceSlbId].name : '-';
            var targetSlbName = slbs[targetSlbId] ? slbs[targetSlbId].name : '-';

            var vsesData = _.map(contentJson.migration['vses'], function (v) {
                return {name: vses[v] ? vses[v].name : '-', id: v};
            });
            var idc = slbs[sourceSlbId] ? slbs[sourceSlbId].idc : '-';

            return {
                id: id,
                name: name,
                idc: idc,
                sourceSlbId: sourceSlbId,
                sourceSlbName: sourceSlbName,
                targetSlbId: targetSlbId,
                targetSlbName: targetSlbName,
                status: contentJson.status,
                state: input.status,
                vses: vsesData,
                content: contentJson
            };
        });
        return map;
    };
    $scope.getMigrationStatusClass = function (migration) {
        var stage = migration.status;
        var state = migration.state;
        if (!state) return 'status-normal';
        return migrationStatusColor[stage];
    };
    $scope.getMigrationStatusText = function (migration) {
        var stage = migration.status;
        var state = migration.state;
        var migrationStatusRevert = _.invert(migrationStatus);
        if (!state) return migrationStatusRevert['done'];
        return migrationStatusRevert[stage];
    };
    $scope.getCurrentContent = function (migration) {
        var content = migration.content;
        var contentJson = JSON.parse(content);
        return contentJson;
    };

    $scope.changeAvailible = function () {
        var status = $scope.query.availableStatus;
        if (status) {
            alert((angular.equals(resource, {}) ? '暂时不支持，请刷新页面重新查询': resource.tools.vmigrationEdit.js.msg46));
            return;
        }

        $scope.query.availableStatus = true;

        var slbVses = $scope.data.slbVses;
        var sourceSlb = $scope.data.steps.migration['source-slb'];

        $scope.data.slbVses[sourceSlb] = _.map(slbVses[sourceSlb], function (v) {
            v.available = true;
            return v;
        });
    };

    $scope.loadData = function () {
        var slbs = {};
        var user = {};
        var slbStastics = {};
        var vses = {};
        var vsesMeta = {};
        var groups = {};
        var apps = {};
        var migrations = [];

        var param = {
            type: 'extended'
        };
        var slbRequest = {
            method: 'GET',
            url: G.baseUrl + '/api/slbs',
            params: param
        };

        var loginUserRequest = {
            method: 'GET',
            url: G.baseUrl + '/api/auth/current/user'
        };

        var slbStasticsRequest = {
            method: 'GET',
            url: G.baseUrl + '/api/statistics/slbs'
        };

        var vsesRequest = {
            method: 'GET',
            url: G.baseUrl + '/api/vses',
            params: param
        };

        var vsesStatsticsRequest = {
            method: 'GET',
            url: G.baseUrl + '/api/statistics/vses'
        };

        var vsGroupsRequest = {
            method: 'GET',
            url: G.baseUrl + '/api/groups'
        };
        var appRequest = {
            method: 'GET',
            url: G.baseUrl + '/api/apps'
        };

        var loadMigrationRequest = {
            method: 'GET',
            url: G.baseUrl + '/api/tools/vs/migrates'
        };

        var switchesRequest = {
            method: 'GET',
            url: G.baseUrl + '/api/flow/dns/switch/list'
        };


        $q.all(
            [
                $http(slbRequest).success(function (response, code) {
                    slbs = _.indexBy(response['slbs'], function (v) {
                        return v.id;
                    });
                }),
                $http(loginUserRequest).success(function (response, code) {
                    user = response;
                }),
                $http(slbStasticsRequest).success(function (response, code) {
                    //$scope.data.slbsStatstics
                    slbStastics = _.indexBy(response['slb-metas'], function (v) {
                        return v['slb-id'];
                    });
                }),
                $http(vsesRequest).success(function (response, code) {
                    vses = response['virtual-servers'];
                }),
                $http(vsesStatsticsRequest).success(function (response, code) {
                    vsesMeta = _.indexBy(response['vs-metas'], function (v) {
                        return v['vs-id'];
                    });
                }),
                $http(vsGroupsRequest).success(function (response, code) {
                    groups = response.groups;
                }),
                $http(appRequest).success(function (response, code) {
                    apps = _.indexBy(response.apps, function (v) {
                        return v['app-id']
                    });
                }),
                $http(loadMigrationRequest).success(function (response, code) {
                    migrations = response.migrations;
                }),
                $http(switchesRequest).success(function (response, code) {
                    switches = _.indexBy(response, 'id');
                })
            ]
        ).then(
            function () {
                $scope.origin.vses = _.indexBy($.extend(true, [], vses), function (v) {
                    return v.id;
                });

                // User
                $scope.data.user = user;


                // SLBS
                $scope.data.slbs = _.mapObject(slbs, function (value, key) {
                    var properties = _.indexBy(value.properties, function (v) {
                        return v.name.toLowerCase();
                    });
                    value.idc = properties['idc'] ? properties['idc'].value : '-';

                    var staticData = slbStastics[key];
                    if (staticData) {
                        value.vsesCount = staticData['vs-count'] || 0;
                        value.qps = staticData['qps'] || 0;
                        value.serverCount = staticData['group-server-count'];
                    }
                    // vips
                    value.vips = _.pluck(value['vips'], 'ip');
                    return value;
                });

                // SLB vses
                var slbVses = {};
                $.each(vses, function (j, v) {
                    var slbIds = v['slb-ids'];
                    var properties = _.indexBy(v['properties'], function (s) {
                        return s.name.toLowerCase();
                    });
                    var status = properties['status'] ? properties['status'].value : '-';
                    var statusAvailable = status == 'activated';
                    var item = {
                        id: v.id,
                        name: v.name,
                        port: v.port,
                        tags: v.tags,
                        properties: properties,
                        statusAvailable: statusAvailable,
                        ssl: v.ssl ? 'https' : 'http',
                        available: slbIds.length == 1,
                        domains: _.pluck(v.domains, 'name')
                    };

                    for (var i = 0; i < slbIds.length; i++) {
                        if (slbVses[slbIds[i]]) {
                            slbVses[slbIds[i]].push(item);
                        } else {
                            slbVses[slbIds[i]] = [item];
                        }
                    }
                });
                $scope.data.slbVses = slbVses;

                // vses

                var dataVses = _.mapObject(vses, function (value, key) {
                    value.domains = _.map(value['domains'], function (v) {
                        return v.name;
                    });
                    value.slbs = _.map(value['slb-ids'], function (s) {
                        return slbs[s];
                    });
                    var qps = 0;
                    var groupCount = 0;
                    var appCount = 0;

                    if (vsesMeta && vsesMeta[value.id]) {
                        qps = vsesMeta[value.id].qps || 0;
                        groupCount = vsesMeta[value.id]['group-count'] || 0;
                        appCount = vsesMeta[value.id]['app-count'] || 0;
                    }
                    value.qps = qps;
                    value.groupCount = groupCount;
                    value.appCount = appCount;

                    return value;
                });

                $scope.data.vses = _.indexBy(dataVses, 'id');

                // vs groups
                var vsGroups = {};
                $.each(groups, function (i, item) {
                    $.each(item['group-virtual-servers'], function (j, v) {
                        var vsId = v['virtual-server'].id;
                        if (vsGroups[vsId]) {
                            vsGroups[vsId].push(item.id);
                        } else {
                            vsGroups[vsId] = [item.id];
                        }
                    });
                });

                $scope.data.vsGroups = vsGroups;

                // Apps
                $scope.data.apps = apps;

                // Groups
                groups = _.indexBy(groups, function (v) {
                    return v.id;
                });
                groups = _.mapObject(groups, function (value, key, object) {
                    var appId = value['app-id']
                    var appChineseName = apps[appId] ? apps[appId]['chinese-name'] : '-';
                    value.appName = appId + '(' + appChineseName + ')';
                    value.healthUrl = value['health-check'] ? (value['health-check']['uri'] || '') : '';
                    return value;
                });
                $scope.data.groups = groups;

                $scope.data.switches = switches;
                $scope.data.migrations = $scope.getMigrations(migrations);

                // Migrations
                $scope.getMigrationSummary();
            }
        );
    };
    $scope.loadVsDataBeforeUpdate = function (callBack, area) {
        area.showLoading();
        var vsesRequest = {
            method: 'GET',
            url: G.baseUrl + '/api/vses'
        };
        $http(vsesRequest).success(function (response, code) {
            var vses = response['virtual-servers'];
            $scope.origin.vses = _.indexBy($.extend(true, [], vses), function (v) {
                return v.id;
            });

            callBack();
        });
    };

    function getSlbsForSelect(allSLbs, choosenSlbId) {
        var result = {};
        if (choosenSlbId) {
            // has selected target slb
            var targetSlb = allSLbs[choosenSlbId];
            var targetSlbIdc = targetSlb.idc;


            // filter those with same idc and different slbs
            result = _.pick(allSLbs, function (value, key, object) {
                var currentIDC = value.idc;

                var notSame = key != choosenSlbId;
                var sameIdc = targetSlbIdc == currentIDC;
                return notSame && sameIdc;
            });

        } else {
            // has not selected
            result = $scope.data.slbs;
        }

        return result;
    }

    //url: '/api/tools/vs/migrate/new',
    function saveMigration(id, url, migration, callBack) {
        var time = new Date().getTime();
        var migrationName = 'Vs_Migrates_CreatedBy_' + $scope.data.user['name'] + '_' + time;
        if (migration.name) {
            migrationName = migration.name;
        }
        var data = {
            name: migrationName,
            content: JSON.stringify(migration)
        };
        if (id) {
            data.id = id;
        }
        var request = {
            url: url,
            method: 'POST',
            data: data
        };
        $http(request).success(function (response, code) {
            callBack(response);
        });
    }

    function checkDns(data, loadingArea) {
        var migratedVses = _.map(data.migration['vses'], function (v) {
            var vses = $scope.data.vses;
            if (vses[v] && vses[v].domains) {
                var domains = _.map($scope.data.vses[v].domains, function (s) {
                    return {
                        name: s
                    };
                });
                return {
                    'vs-id': v,
                    domains: domains
                };
            }
        });
        var request = {
            method: 'POST',
            url: '/api/tools/check/vsvpn?timeout=30000',
            data: {
                vses: _.reject(migratedVses, function (s) {
                    return s == undefined;
                })
            }
        };
        $http(request).success(function (response, data) {
            var results = _.groupBy(response.vses, 'vs-id');
            results = _.mapObject(results, function (value, key) {
                var ds = _.map(value, function (s) {
                    return s.domains;
                });

                return {
                    key: key,
                    value: _.flatten(ds)
                }
            });
            $scope.data.steps.migration.dnsResult = results;
            loadingArea.hideLoading();
        });
    }

    $('#newVsMigrationDialog').on('hidden.bs.modal', function (a, b, c) {
        var pair = {};
        pair.timeStamp = new Date().getTime();
        pair.status = '';

        H.setData(pair);
    });

    function deleteMigration(id, callBack) {
        var request = {
            url: deleteMigrationUrl + '?migrationId=' + id,
            method: 'GET'
        };
        $http(request).success(function (response, code) {
            callBack(response, code);
        });
    }

    var vsTrafficService;

    $scope.hashChanged = function (hashData) { $scope.resource = H.resource;
        env = 'uat';
        if (hashData.env) {
            env = hashData.env;
        }
        var prefix = G[env].urls['api'];
        newMigrationUrl = prefix + '/api/tools/vs/migrate/new';
        updateMigrationUrl = prefix + '/api/tools/vs/migrate/update';
        deleteMigrationUrl = prefix + '/api/tools/vs/migrate/delete';


        var endTime;
        var startTime = new Date(decodeURIComponent(hashData.startTime));
        if (!U.isValidDate(startTime)) {
            startTime = $scope.getDateNow();
            var e = new Date(decodeURIComponent(startTime)).getTime() + 1000 * 60 * 90;
            endTime = encodeURIComponent($.format.date(e, 'yyyy-MM-dd HH:00'));
            H.setData({
                startTime: startTime
            });
        } else {
            var e = new Date(startTime).getTime() + 1000 * 60 * 90;
            endTime = encodeURIComponent($.format.date(e, 'yyyy-MM-dd HH:00'));
        }

        $scope.queryDate.startTime = $.format.date(startTime, 'yyyy-MM-dd HH:mm');
        $scope.queryDate.endTime = endTime;

        try {
            vsTrafficService = VSTraficService.create($http, startTime);
        } catch (ex) {
            // do nothing
        }

        if (hashData.migrationId) {
            $scope.query.migrationId = hashData.migrationId;
        }
        $scope.query.status = hashData.status;
        $scope.query.domain = hashData.domain;
        $scope.searchtext = $scope.query.domain || '';

        $scope.env = env;
        $scope.query.hasLoaded = false;


        if (!$scope.query.migrationId) {
            $scope.data.steps = {};
            $scope.data.steps[stepOneKey] = {};

            // data to store migration metas
            $scope.data.steps.migration = {};
            $scope.data.vsesToBeSelected = {};
        } else {
            var request = {
                method: 'GET',
                url: G.baseUrl + '/api/tools/vs/migrate?migrationId=' + $scope.query.migrationId
            };
            $http(request).success(function (data) {
                var steps = JSON.parse(data.content);
                $scope.data.steps = steps;
                $scope.data.steps[stepOneKey] = {};
                $scope.data.steps.id = data.id;
                $scope.data.steps.name = data.name;
                reloadDns();
            });
        }

        $scope.loadData();
    };

    function hasRight() {
        var hasRight = true;
        var migrationId = $scope.query.migrationId;
        if (migrationId) {
            hasRight = A.canDo('Flow', 'UPDATE', migrationId);
        } else {
            hasRight = A.canDo('Flow', 'NEW', '*') || A.canDo('Flow', 'FLOW', '*');
        }
        return hasRight;
    }

    function reloadDns() {
        var intervalId = $scope.query.intervalId;
        var migrationId = $scope.query.migrationId;
        if (!migrationId) return;

        var migrationRequest = {
            method: 'GET',
            url: G.baseUrl + '/api/tools/vs/migrate?migrationId=' + $scope.query.migrationId
        };

        var dnsListRequest = {
            method: 'GET',
            url: G.baseUrl + '/api/flow/dns/switch/list'
        };

        $http(dnsListRequest).success(function (response2, code2) {
            if (code2 == 200) {
                // index the result by status
                var dnses = _.groupBy(response2, function (v) {
                    return v['status'];
                });

                var needUpdated = _.pick(dnses, function (v, k, item) {
                    var status = k === "SWITCH" || k === "ROLLBACK";


                    if (!status) {
                        if (!$scope.data.dnsDetails) $scope.data.dnsDetails = {};
                        for (var i = 0; i < v.length; i++) {
                            $scope.data.dnsDetails[v[i].id] = v[i];
                        }
                    }
                    return status;
                });

                var ids = [];
                _.mapObject(needUpdated, function (v, k, item) {
                    for (var i = 0; i < v.length; i++) {
                        ids.push(v[i]['id']);
                    }
                });

                // initialize list of dns switch

                $http(migrationRequest).success(function (response3, code3) {
                    if (code3 == 200) {
                        var steps = JSON.parse(response3.content);
                        var dns = steps['DNS']['dns-switch'];
                        var requests = [];
                        if (dns && dns.length > 0) {
                            var cids = _.map(dns, function (v) {
                                return parseInt(v);
                            });

                            ids = _.reject(ids, function (i) {
                                return cids.indexOf(i) == -1;
                            });

                            var tobeupdateddns = [];
                            _.map(cids, function (v) {
                                if (ids.indexOf(v) != -1) tobeupdateddns.push(v);
                            });

                            if (tobeupdateddns.length == 0) return;

                            for (var i = 0; i < tobeupdateddns.length; i++) {
                                var dnsId = tobeupdateddns[i];
                                var request = {
                                    method: 'GET',
                                    url: G.baseUrl + '/api/flow/dns/switch/get?id=' + dnsId
                                };
                                requests.push($http(request).success(function (r, c) {
                                    if (c == 200) {
                                        if (!$scope.data.dnsDetails) $scope.data.dnsDetails = {};

                                        $scope.data.dnsDetails[r.id] = r;
                                    }
                                }))
                            }

                            $q.all(requests).then(function () {

                            });
                        }
                    }
                });
            }
        });


        if (!intervalId) {
            intervalId = setInterval(function () {
                reloadDns();
            }, 30000)
        }

        $scope.query.intervalId = intervalId;
    }

    function createSwith(vses) {
        // create switches
        var sourceSlbId = $scope.data.steps['migration']['source-slb'];
        var targetSlbId = $scope.data.steps['migration']['target-slb'];

        var switchObject = {
            'source-slb-id': sourceSlbId,
            'target-slb-id': targetSlbId,
            'vs-ids': vses
        };

        var query = {
            method: 'POST',
            url: G.baseUrl + '/api/flow/dns/switch/new',
            data: switchObject
        };

        $http(query).success(function (response, code) {
            if (code == 200) {
                // relate this dns switch to target migration
                updateMigrationWithDnsSwith(response.id);
                executeSwith(response.id);
            } else {
                alert((angular.equals(resource, {}) ? '创建DNS切换Flow失败，错误信息：': resource.tools.vmigrationEdit.js.ms4g7) + response.message);
            }
        });
    }

    function executeSwith(switchId) {
        var query = {
            method: 'GET',
            url: G.baseUrl + '/api/flow/dns/switch/execute?id=' + switchId
        };

        $http(query).success(function (response, code) {
            if (code == 200) {
                reloadDns();
            }
        });
    };

    function rollbackSwitch(switchId) {
        var query = {
            method: 'GET',
            url: G.baseUrl + '/api/flow/dns/switch/rollback?id=' + switchId
        };

        $http(query).success(function (response, code) {
            if (code == 200) {
                reloadDns();
            } else {
                alert('Rollback dns switch failed. Message:' + response.message);
            }
        });
    }

    function removeSwitch(switchId) {
        var query = {
            method: 'GET',
            url: G.baseUrl + '/api/flow/dns/switch/delete?id=' + switchId
        };

        $http(query).success(function (response, code) {
            if (code == 200) {
                updateMigrationWithDnsSwith(response.id, true);
                $('#deleteswitchresultdialog').modal('show');
            } else {
                alert('Delete dns switch failed. Message:' + response.message);
            }
        });
    }

    H.addListener("selfInfoApp", $scope, $scope.hashChanged);
});
selfInfoApp.filter('slbIdFilter', function () {
    return function (input, param1) {
        if (!param1 || param1.trim() == "") {
            return input;
        }

        param1 = parseInt(param1);

        return _.pick(input, function (value, key, object) {
            return key == param1;
        });
    }
});
selfInfoApp.filter('domainFilter', function () {
    return function (input, param1) {
        if (!param1 || param1.trim() == "") {
            return input;
        }

        var starter;
        var fArray = param1.split(':');
        if (fArray.length == 2) {
            starter = fArray[0];
            param1 = fArray[1];
        } else {
            return input;
        }
        var paramArray = param1.split('|');

        var param = _.map(paramArray, function (v) {
            if (v) {
                return v.trim().toLowerCase();
            } else {
                return [];
            }
        });

        param = _.flatten(param);

        var values = _.filter(input, function (v) {
            var id = v.id;

            var idOk = false;
            var hasLikeDomainIn = '';
            var tagIn = '';
            var propertyOk = '';
            var domainOk = '';

            var domains = _.map(v.domains, function (s) {
                return s.toLowerCase();
            });

            hasLikeDomainIn = _.filter(domains, function (s) {
                var existed = _.filter(param, function (p) {
                    if (s.indexOf(p) != -1) return true;
                    return false;
                });
                return existed && existed.length > 0;
            });


            var tags = _.map(v.tags, function (s) {
                return s.toLowerCase();
            });
            var properties = _.map(_.pluck(_.values(v['properties']), 'value'), function (s) {
                return s.toLowerCase();
            });

            tagIn = _.intersection(tags, param);
            propertyOk = _.intersection(properties, param);
            idOk = param.indexOf(id.toString()) != -1;
            domainOk = hasLikeDomainIn && hasLikeDomainIn.length > 0;

            if (starter.toUpperCase() == 'TAG') {
                return tagIn && tagIn.length > 0;
            }

            if (starter.toUpperCase() == 'PROPERTY') {
                return propertyOk && propertyOk.length > 0;
            }

            if (starter.toUpperCase() == 'DOMAIN') {
                return domainOk;
            }
            if (starter.toUpperCase() == 'ID') {
                return idOk;
            }

            return false;
        });

        return values;
    }
});

selfInfoApp.filter('availableFilter', function () {
    return function (input, param1) {
        if (!param1) {
            return input;
        }

        return _.filter(input, function (v) {
            return v.statusAvailable == true && v.available;
        });
    }
});

selfInfoApp.filter('stepFilter', function () {
    return function (input) {
        var c = $.extend({}, input, true);
        delete c.status;
        delete c.id;
        return c;
    }
});
selfInfoApp.filter('orderConfigFilter', function () {
    return function (input) {
        return input.sort(function (a, b) {
            return b.order - a.order;
        });
    }
});
selfInfoApp.filter('migrationFilter', function () {
    return function (input, status, scope, domain) {
        var result = input;
        if (domain) {
            // search all the domains contained migration
            result = _.filter(input, function (v) {
                var vses = v.content[scope.data.keys[0]].vses;
                vses = _.map(vses, function (s) {
                    var vs = scope.data.vses[s];
                    var domainsText = vs.domains.join(',');
                    return domainsText;
                });
                var vsesText = vses.join(',');
                return vsesText.indexOf(domain) != -1;
            });
            scope.query.count = result.length || 0;
            return result;
        }
        ;

        if (!status) {
            result = _.filter(input, function (v) {
                return v.state == true;
            });
        } else {
            if (status == 'removed') {
                // only show removed migrations
                result = _.filter(input, function (v) {
                    return v.state == false;
                });
            } else {
                result = _.filter(input, function (v) {
                    var contents = v.content;
                    return contents.status == status && v.state == true;
                });
            }
        }

        scope.query.count = result.length || 0;
        return result;
    }
});
angular.bootstrap(document.getElementById("self-info-area"), ['selfInfoApp']);