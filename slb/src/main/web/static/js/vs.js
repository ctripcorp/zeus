//VSInfoComponent: show a vs info
var vsInfoApp = angular.module('vsInfoApp', ['angucomplete-alt', 'ngSanitize', 'ui-rangeSlider', 'http-auth-interceptor']);
vsInfoApp.controller('vsInfoController', function ($scope, $http, $q) {
    var log_start = new Date().getTime() - 1000 * 60 * 60;
    var log_end = new Date().getTime() + 1000 * 60 * 60;
    var dashboardStartTime = $.format.date(log_start, 'yyyy-MM-dd HH:mm:ss');
    var dashboardEndTime = $.format.date(log_end, 'yyyy-MM-dd HH:mm:ss');
    var resource;
    $scope.query = {
        vsId: '',
        user: {},
        showextended: '',
        canarySuccess: false
    };
    $scope.model = {
        currentlogs: [],
        offlinevs: {},
        extendedvs: {},
        onlinevs: {},
        vsgroups: [],
        slbs: {},
        vses: {},
        apps: {},
        groups: {},
        slbsObjects: {},
        vsstastics: {}
    };
    $scope.view = {
        offlinevs: {},
        extendedvs: {},
        onlinevs: {},
        vsgroups: [],
        vshistoryversions: [],
        vsstastics: {}
    };
    $scope.allGroups = [];
    $scope.groups = [];
    $scope.vgroups = [];
    $scope.diffVersions = {};
    $scope.tableOps = {
        vsDomain: {
            showMoreColumns: false,
            showOperations: false
        },
        vsGroups: {
            showMoreColumns: false,
            showOperations: false
        },
        vsCerts: {
            showMoreColumns: false,
            showOperations: false
        },
        policy: {
            showMoreColumns: false,
            showOperations: false
        }
    };

    // Right area
    $scope.showUpdateVsBt = function () {
        return A.canDo("Vs", "UPDATE", $scope.query.vsId) && $scope.query.showextended;
    };
    $scope.showActivateVsBt = function () {
        return A.canDo("Vs", "ACTIVATE", $scope.query.vsId) && $scope.query.showextended;
    };
    $scope.showAddVsTagBt = function () {
        return A.canDo("Vs", "PROPERTY", $scope.query.vsId) && $scope.query.showextended;
    };
    $scope.showAddVsPropertyBt = function () {
        return A.canDo("Vs", "PROPERTY", $scope.query.vsId) && $scope.query.showextended;
    };
    $scope.showDeactivateVsBt = function () {
        return A.canDo("Vs", "DEACTIVATE", $scope.query.vsId) && $scope.query.showextended;
    };
    $scope.showDeleteVsBt = function () {
        return A.canDo("Vs", "DELETE", $scope.query.vsId) && $scope.query.showextended;
    };
    $scope.showCloneVsBt = function () {
        return A.canDo("Vs", "UPDATE", $scope.query.vsId) && $scope.query.showextended;
    };
    $scope.showAddGroupBt = function () {
        var wrenchOp = A.canDo("Vs", "UPDATE", $scope.query.vsId) && $scope.tableOps.vsGroups.showOperations;
        if (!wrenchOp) return false;
        else return true;
    };
    $scope.showAddCertBt = function () {
        var wrenchOp = A.canDo("Vs", "UPDATE", $scope.query.vsId) && $scope.tableOps.vsCerts.showOperations;
        if (!wrenchOp) return false;
        else return true;
    };
    $scope.showAddVGroupBt = function () {
        return A.canDo("Group", "UPDATE", $scope.query.vsId) && A.canDo("Vs", "UPDATE", $scope.query.vsId) && $scope.query.showextended;
    };
    $scope.showUpdateVsDomainBt = function () {
        var wrenchOp = A.canDo("Vs", "UPDATE", $scope.query.vsId) && $scope.tableOps.vsDomain.showOperations;
        if (!wrenchOp) return false;
        else return true;
    };
    $scope.navigateTo = function (type) {
        var url = '';
        switch (type) {

            case 'groups': {
                url = '/portal/groups#?env=' + G.env + '&vsId=' + $scope.query.vsId + '&&groupType=Group';
                break;
            }
            case 'activated': {
                url = '/portal/groups#?env=' + G.env + '&vsId=' + $scope.query.vsId + '&groupStatus=已激活&groupType=Group';
                break;
            }
            case 'tobeactivated': {
                url = '/portal/groups#?env=' + G.env + '&vsId=' + $scope.query.vsId + '&groupStatus=有变更&groupType=Group';
                break;
            }
            case 'deactivated': {
                url = '/portal/groups#?env=' + G.env + '&vsId=' + $scope.query.vsId + '&groupStatus=未激活&groupType=Group';
                break;
            }
            case 'healthy': {
                url = '/portal/groups#?env=' + G.env + '&vsId=' + $scope.query.vsId + '&groupHealthy=healthy:healthy&groupType=Group';
                break;
            }
            case 'unhealthy': {
                url = '/portal/groups#?env=' + G.env + '&vsId=' + $scope.query.vsId + '&groupHealthy=healthy:unhealthy&groupType=Group';
                break;
            }
            case 'broken': {
                url = '/portal/groups#?env=' + G.env + '&vsId=' + $scope.query.vsId + '&groupHealthy=healthy:Broken&groupType=Group';
                break;
            }

            case 'qps': {
                url = '/portal/vs/traffic#?env=' + G.env + '&vsId=' + $scope.query.vsId + '&startTime=2017-02-21%2017%3A00&trafficType=请求数';
                break;
            }
            default : {
                url = '';
                break;
            }
        }
        window.location.href = url;
    };
    $scope.showHttpsRedirect = function () {
        var vs = $scope.model.extendedvs;
        if (!vs || _.keys(vs).length == 0) return false;
        if (vs.ssl) return false;

        return true;
        // var ps = _.indexBy(vs['properties'],'name');
        // return ps
    };
    $scope.getCName = function (vs) {
        if (!vs || _.keys(vs).length == 0) return;

        var slbs = $scope.model.slbsObjects;
        var results = {};

        var slbids = vs['slb-ids'];
        for (var i = 0; i < slbids.length; i++) {
            var slb = slbs[slbids[i]];
            var properties = _.indexBy(slb['properties'], 'name');
            results[slbids[i]] = (properties && properties['ops-cname']) ? properties['ops-cname']['value'] : '-';
        }

        return results;
    };

    $scope.showRedirect = function () {
        var vs = $scope.model.extendedvs;
        if (!vs || _.keys(vs).length == 0) return false;
        if (vs.ssl) return false;


        var ps = _.indexBy(vs['properties'], 'name');
        var v = ps['relatedredirectid'];

        return v;
    };
    $scope.getRedirectId = function () {
        var vs = $scope.model.extendedvs;
        if (!vs || _.keys(vs).length == 0) return;
        if (vs.ssl) return;


        var ps = _.indexBy(vs['properties'], 'name');
        var v = ps['relatedredirectid'];

        if (v) return v.value;
    };

    $scope.getNamedGroupUrl=function (env, vsId) {
        var vs = $scope.model.extendedvs;

        if(!env || !vsId || !vs){
            return;
        }

        var ps = _.indexBy(vs['properties'], 'name');
        var v = ps['REGION_TRAFFIC_GROUP_ID'];

        //REGION_TRAFFIC_GROUP_ID
        if(!v || !v.value){
            return '/portal/group/delegate#?env='+env+'&vsId='+vsId;
        }
        return '/portal/group/delegate#?env='+env+'&groupId='+v.value+"&vsId="+vsId;
    };

    $scope.showDelegate = function () {
        var env = $scope.env;
        if (!env) return false;

        var e = env.toLowerCase();

        switch (e) {
            case 'fws':
            case 'uat':
            case 'pro': {
                return false;
            }
            default:
                return true;
        }
    };

    // Partial show more and show ops
    $scope.disableOpenVsDomain = function () {
        var can = A.canDo('Vs', 'UPDATE', $scope.model.offlinevs.id);
        return !can;
    };
    $scope.getVsDomainShowOperation = function () {
        return $scope.tableOps.vsDomain.showOperations ? 'fa fa-wrench text-info' : 'fa fa-wrench font-gray';
    };
    $scope.toggleShowVsDomainOperations = function () {
        $scope.tableOps.vsDomain.showOperations = !$scope.tableOps.vsDomain.showOperations;
        var can = A.canDo('Vs', 'UPDATE', $scope.model.offlinevs.id);
        if (can) {
            if ($scope.tableOps.vsDomain.showOperations) {
                $('#vs-domains-table').bootstrapTable('showColumn', 'Operation');
                $('#vs-domains-table').bootstrapTable('showColumn', 'Operation');
            }
            else {
                $('#vs-domains-table').bootstrapTable('hideColumn', 'Operation');
                $('#vs-domains-table').bootstrapTable('hideColumn', 'Operation');
            }
        }
    };
    $scope.getVsDomainOperationTitle = function () {
        return $scope.tableOps.vsDomain.showOperations ? operationmap ? operationmap['关闭操作'] : '关闭操作' : operationmap ? operationmap['打开操作'] : '打开操作';
    };
    $scope.getVsDomainShowMoreTitle = function () {
        return $scope.tableOps.vsDomain.showOperations ? operationmap ? operationmap['简略'] : '简略' : operationmap ? operationmap['详细'] : '详细';
    };

    $scope.getVsGroupsShowMore = function () {
        return $scope.tableOps.vsGroups.showMoreColumns ? 'fa fa-list-alt text-info' : 'fa fa-list-alt font-gray';
    };
    $scope.toggleShowMoreVsGroupsColumns = function () {
        $scope.tableOps.vsGroups.showMoreColumns = !$scope.tableOps.vsGroups.showMoreColumns;
        if ($scope.tableOps.vsGroups.showMoreColumns) {
            $('#vs-groups-table').bootstrapTable('showColumn', 'qps');
            $('#vs-groups-table').bootstrapTable('showColumn', 'paths');
            $('#vs-groups-table').bootstrapTable('showColumn', 'group-servers');
        } else {
            $('#vs-groups-table').bootstrapTable('hideColumn', 'qps');
            $('#vs-groups-table').bootstrapTable('hideColumn', 'paths');
            $('#vs-groups-table').bootstrapTable('hideColumn', 'group-servers');
        }
    };
    $scope.getVsGroupsShowMoreTitle = function () {
        return $scope.tableOps.vsGroups.showOperations ? operationmap ? operationmap['简略'] : '简略' : operationmap ? operationmap['详细'] : '详细';
    };

    $scope.disableOpenVsGroups = function () {
        var can = A.canDo('Vs', 'UPDATE', $scope.model.offlinevs.id);
        return !can;
    };
    $scope.getVsGroupsShowOperation = function () {
        return $scope.tableOps.vsGroups.showOperations ? 'fa fa-wrench text-info' : 'fa fa-wrench font-gray';
    };
    $scope.toggleShowVsGroupsOperations = function () {
        var can = A.canDo('Vs', 'UPDATE', $scope.model.offlinevs.id);
        if (can) {
            $scope.tableOps.vsGroups.showOperations = !$scope.tableOps.vsGroups.showOperations;
        }
    };
    $scope.getVsGroupsOperationTitle = function () {
        return $scope.tableOps.vsGroups.showOperations ? operationmap ? operationmap['关闭操作'] : '关闭操作' : operationmap ? operationmap['打开操作'] : '打开操作';
    };

    $scope.disableOpenVsCerts = function () {
        var can = A.canDo('Vs', 'UPDATE', $scope.model.offlinevs.id);
        return !can;
    };
    $scope.getVsCertsShowOperation = function () {
        return $scope.tableOps.vsCerts.showOperations ? 'fa fa-wrench text-info' : 'fa fa-wrench font-gray';
    };
    $scope.toggleShowVsCertsOperations = function () {
        var can = A.canDo('Vs', 'UPDATE', $scope.model.offlinevs.id);
        if (can) {
            $scope.tableOps.vsCerts.showOperations = !$scope.tableOps.vsCerts.showOperations;
            if ($scope.tableOps.vsCerts.showOperations) {
                $('#certificates-table').bootstrapTable('showColumn', 'Operation');
            } else {
                $('#certificates-table').bootstrapTable('hideColumn', 'Operation');
            }
        }
    };
    $scope.getVsCertsOperationTitle = function () {
        return $scope.tableOps.vsCerts.showOperations ? operationmap ? operationmap['关闭操作'] : '关闭操作' : operationmap ? operationmap['打开操作'] : '打开操作';
    };

    // Partial show
    $scope.getVsPolicyShowMore = function () {
        return $scope.tableOps.policy.showMoreColumns ? 'fa fa-list-alt text-info' : 'fa fa-list-alt font-gray';
    };
    $scope.toggleShowMoreVsPolicyColumns = function () {
        $scope.tableOps.policy.showMoreColumns = !$scope.tableOps.policy.showMoreColumns;
        if ($scope.tableOps.policy.showMoreColumns) {
            $('#vs-policy-table').bootstrapTable('showColumn', 'paths');
            $('#vs-policy-table').bootstrapTable('showColumn', 'target');
            $('#vs-policy-table').bootstrapTable('showColumn', 'controls1');
            $('#vs-policy-table').bootstrapTable('hideColumn', 'controls2');
        } else {
            $('#vs-policy-table').bootstrapTable('hideColumn', 'paths');
            $('#vs-policy-table').bootstrapTable('hideColumn', 'target');
            $('#vs-policy-table').bootstrapTable('hideColumn', 'controls1');
            $('#vs-policy-table').bootstrapTable('showColumn', 'controls2');
        }
    };
    $scope.getPolicyOperationTitle = function () {
        return $scope.tableOps.policy.showOperations ? operationmap ? operationmap['显示简略信息'] : '显示简略信息' : operationmap ? operationmap['显示详细信息'] : '显示详细信息';
    };

    // show tags and porperties
    $scope.showMoreTags = false;
    $scope.showMoreProperties = false;
    $scope.multiTagsClass = function () {
        return $scope.showMoreTags ? '' : 'multi-tags-collapse';
    };
    $scope.collapseTagBtnClass = function () {
        return $scope.showMoreTags ? 'fa fa-chevron-down' : 'fa fa-chevron-left';
    };
    $scope.toggleShowMoreTags = function () {
        $scope.showMoreTags = !$scope.showMoreTags;
    };

    $scope.multiPropertiesClass = function () {
        return $scope.showMoreProperties ? '' : 'multi-tags-collapse';
    };
    $scope.collapsePropertiesBtnClass = function () {
        return $scope.showMoreProperties ? 'fa fa-chevron-down' : 'fa fa-chevron-left';
    };
    $scope.toggleShowMoreProperties = function () {
        $scope.showMoreProperties = !$scope.showMoreProperties;
    };


    // Auto Complete Cached
    $scope.cacheRequestFn = function (str) {
        return {q: str, timestamp: new Date().getTime()};
    };
    $scope.remoteUrl = function () {
        return G.baseUrl + "/api/meta/vses";
    };

    /**Top bar Area
     * Activate, Deactivate, DELETE
     * Option Panel Area
     * Focus Area
     * Status Area
     * Tagging
     * Property
     * Diff
     * **/

        //WEBINFO,HICKWALL,CAT links
    var mapping = {
            金桥: 'SHAJQ',
            欧阳: 'SHAOY',
            南通: 'NTGXH',
            福泉: 'SHAFQ',
            金钟: 'SHAJZ',
            SHAOYN: 'SHAOYN',
            日坂: 'SHARB',
            SHARB: 'SHARB'
        };
    $scope.showLinks = function (type) {
        var env = $scope.env;
        if (G[env] && G[env].urls[type]) return true;
        return false;
    };
    $scope.generateMonitorUrl = function (type) {
        if ($scope.model.extendedvs.domains && $scope.model.extendedvs.domains.length > 0) {
            var vsId = $scope.model.extendedvs.id;
            var domains = $scope.model.extendedvs.domains[0].name;

            var esDomains = _.map(_.pluck($scope.model.extendedvs.domains, 'name'), function (v) {
                return "'" + v + "'";
            });
            var esDomainsText = esDomains.join('%20OR%20');

            var vsProperties = _.indexBy($scope.model.extendedvs.properties, function (v) {
                return v.name;
            });

            var idc = vsProperties['idc'] ? vsProperties['idc'].value : '-';
            var idcText = '';
            if (idc !== '-') {
                var idcArray = _.map(idc.split(','), function (i) {
                    return "idc%3D'" + mapping[i] + "'";
                });

                idcText = '(' + idcArray.join('%20OR%20') + ')';
            }
            switch (type) {
                case 'hickwall':
                    return G[$scope.env] ? G[$scope.env].urls.hickwall + '/d/vT4LobgZk/slb-vs?orgId=6&var-vsid=' + vsId : '';
                case 'es': {
                    if (G[$scope.env].urls.es) {
                        return G[$scope.env].urls.es + '?query=domain%3D(' + esDomainsText + ')%20AND%20' + idcText;
                    } else {
                        return '-';
                    }
                }
                case 'dashboard': {
                    var env = $scope.env;
                    env = env == 'pro' ? 'PROD' : env.toUpperCase();
                    var dashboard = G.dashboardportal + '/#env=' + env + '&metric-name=slb.req.count&interval=1m&start-time=' + dashboardStartTime + '&end-time=' + dashboardEndTime + '&chart=line&aggregator=sum&ts=1515983913677&tags={"vsid":["' + vsId + '"]}&group-by=[status]';
                    return dashboard;
                }
                default:
                    return '';
            }
        }
        return '-';
    };
    // Deactivate and Remove Area
    $scope.disableDeactivateVs = function () {
        var status = $scope.getVsStatusProperty();
        if (status == "deactivated") {
            return true;
        }
        return false;
    };
    $scope.disableRemoveVs = function () {
        var status = $scope.getVsStatusProperty();
        if (status == "activated" || status == "tobeactivated" || !status) {
            return true;
        }
        return false;
    };

    $scope.activateVsTitleClass = function () {
        try {
            if ($scope.model.onlinevs.data.version != undefined && $scope.model.onlinevs.data.version == $scope.model.extendedvs.version) {
                return "status-red-important";
            }
        } catch (e) {

        }
    };
    $scope.confirmActivateText = '';
    $scope.activateVsClick = function () {
        var resource = $scope.resource;
        if (!resource || _.keys(resource).length == 0) return;

        var statusMap = resource['vs']['vs_vsInfoApp_status'];
        if ($scope.model.onlinevs.data.version != undefined && $scope.model.onlinevs.data.version == $scope.model.extendedvs.version) {
            $scope.confirmActivateText = statusMap['线上已是最新版本,确认是否强制重新激活'];
        } else {
            $scope.confirmActivateText = statusMap[$scope.confirmActivateText];
        }
        var baseText = JSON.stringify(U.sortObjectFileds($scope.model.onlinevs.data), null, "\t");
        var newText = JSON.stringify(U.sortObjectFileds($scope.model.offlinevs), null, "\t");
        var baseVersion = statusMap['线上VS版本(版本'] + $scope.model.onlinevs.data.version + ")";
        var newVersion = statusMap['更新后VS版本(版本'] + $scope.model.offlinevs.version + ")";
        var diffoutputdiv = document.getElementById('diffOutput');
        diffTwoVses(diffoutputdiv, baseText, newText, baseVersion, newVersion);

        $('#activateVSModal').modal('show');
    };
    $scope.activateVS = function () {
        var resource = $scope.resource;
        if (!resource || _.keys(resource).length == 0) return;
        var statusMap = resource['vs']['vs_vsInfoApp_status'];
        $('#confirmActivateVS').modal('hide');
        $('#operationConfrimModel').modal("show").find(".modal-title").html(statusMap["激活VS"]);
        $('#operationConfrimModel').modal("show").find(".modal-body").html(statusMap["正在激活.."] + " <img src='/static/img/spinner.gif' />");
        var param = {
            vsId: $scope.query.vsId
        };
        var req = {
            method: 'GET',
            url: G.baseUrl + '/api/activate/vs?description=' + $scope.query.user,
            params: param
        };
        $scope.processRequest(req, $('#operationConfrimModel'), statusMap["激活VS"], statusMap["激活成功"]);
    };
    $scope.activateBtShow = function () {
        return A.canDo("Vs", "ACTIVATE", $scope.query.vsId);
    };

    $scope.deactivateVsAction = function () {
        var status = $scope.getVsStatusProperty();
        if (status != "deactivated") {
            if ($scope.model.vsgroups && $scope.model.vsgroups.length > 0) {
                $('#discardDeactivateVSModal').modal('show');
            } else {
                $('#allowDeactivateVSModal').modal('show');
            }
        }
    };
    $scope.deactivateVs = function () {
        $('#allowDeactivateVSModal').modal('hide');
        $('#deactivateVSModalConfirm').modal('show');
    };
    $scope.confirmDeactivateVs = function () {
        var resource = $scope.resource;
        if (!resource || _.keys(resource).length == 0) return;
        var statusMap = resource['vs']['vs_vsInfoApp_status'];

        $('#deactivateVSModalConfirm').modal('hide');

        var loading = "<img src='/static/img/spinner.gif' /> " + statusMap["正在下线"];
        $('#operationConfrimModel').modal('show').find(".modal-body").html(loading);
        var param = {
            vsId: $scope.query.vsId
        };
        var request = {
            method: 'GET',
            url: G.baseUrl + '/api/deactivate/vs?description=' + $scope.query.deactivatereason,
            params: param
        };
        $scope.processRequest(request, $('#operationConfrimModel'), statusMap["下线VS"], statusMap["下线VS成功, VSId:"] + $scope.query.vsId, "");
    };

    $scope.deleteVsAction = function () {
        var status = $scope.getVsStatusProperty();
        if (status == "deactivated") {
            if ($scope.model.vsgroups && $scope.model.vsgroups.length > 0) {
                $('#discardDeleteVSModal').modal('show');
            } else {
                $('#allowDeleteVSModal').modal('show');
            }
        }
    };
    $scope.deleteVs = function () {
        $('#allowDeleteVSModal').modal('hide');
        $('#deleteVSModalConfirm').modal('show');
    };
    $scope.confirmDeleteVs = function () {
        var resource = $scope.resource;
        if (!resource || _.keys(resource).length == 0) return;
        var statusMap = resource['vs']['vs_vsInfoApp_status'];

        $('#deleteVSModalConfirm').modal('hide');
        var loading = "<img src='/static/img/spinner.gif' /> " + statusMap["正在删除"];
        $('#operationConfrimModel').modal('show').find(".modal-body").html(loading);
        var param = {
            vsId: $scope.query.vsId
        };
        var request = {
            method: 'GET',
            url: G.baseUrl + '/api/vs/delete?description=' + $scope.query.deleteslbreason,
            params: param
        };
        $http(request).success(
            function (res) {
                if (res == undefined || res.code != undefined) {
                    var msg = res.message;
                    var errText = "<span class='fa fa-times'></span><span style='padding-left: 10px'> " + statusMap["删除失败"] + "</span>";
                    $('#operationConfrimModel').modal('show').find(".modal-title").html(errText);
                    $('#operationConfrimModel').modal('show').find(".modal-body").html(statusMap["失败原因"] + ":" + msg);
                }
                else {
                    var successText = "<span class='fa fa-check'></span><span style='padding-left: 10px'>" + statusMap["删除成功"] + "</span>";
                    $('#operationConfrimModel').modal('show').find(".modal-title").html(successText);
                    $('#operationConfrimModel').modal('show').find(".modal-body").html(statusMap["删除成功"]);

                    setTimeout(
                        function () {
                            window.location.href = "/portal/vses#?env=" + $scope.env;
                        },
                        1000
                    );
                }
            }
        );
    };

    // Option panel Area
    $scope.toggleOptionPanel = function () {
        $scope.query.showextended = !$scope.query.showextended;
        var p = A.canDo("Vs", "UPDATE", $scope.query.vsId) && $scope.query.showextended;
        if (p) {
            $scope.tableOps.vsDomain.showOperations = true;
            $('#vs-domains-table').bootstrapTable('showColumn', 'Operation');

            $scope.tableOps.vsGroups.showOperations = true;
            $('#vs-groups-table').bootstrapTable('showColumn', 'Operation');

            $scope.tableOps.vsCerts.showOperations = true;
            $('#certificates-table').bootstrapTable('showColumn', 'Operation');
        } else {
            $scope.tableOps.vsDomain.showOperations = false;
            $('#vs-domains-table').bootstrapTable('hideColumn', 'Operation');

            $scope.tableOps.vsGroups.showOperations = false;
            $('#vs-groups-table').bootstrapTable('hideColumn', 'Operation');

            $scope.tableOps.vsCerts.showOperations = false;
            $('#certificates-table').bootstrapTable('hideColumn', 'Operation');
        }
    };
    $scope.getOptionPanelCss = function () {
        if (!$scope.query.showextended) {
            return "fa fa-arrows panel-close";
        } else {
            return "fa fa-arrows-alt panel-open";
        }
    };
    $scope.getOptionPanelText = function () {
        if (!$scope.query.showextended) return operationmap ? operationmap['打开操作面板'] : '打开操作面板';
        return operationmap ? operationmap['收起操作面板'] : '收起操作面板';
    };
    // Focus Area
    $scope.getFocusObject = function () {
        if ($scope.query == undefined) return undefined;
        var f = _.find($scope.model.extendedvs.tags, function (item) {
            return item.trim().toLowerCase() == 'user_' + $scope.query.user;
        });
        return f;
    };
    $scope.toggleFocus = function (event) {
        var target = event.currentTarget;
        var f = $scope.getFocusObject();
        if (!f) $scope.addFocus("user_" + $scope.query.user, target);
        else $scope.removeFocus("user_" + $scope.query.user, target);
    };
    $scope.getFocusCss = function () {
        var f = $scope.getFocusObject();
        if (f == undefined) return "fa fa-eye-slash status-unfocus";
        return "fa fa-eye status-focus";
    };
    $scope.getFocusText = function () {
        var f = $scope.getFocusObject();
        if (!f) return operationmap ? operationmap["关注"] : "关注";
        return operationmap ? operationmap["取消关注"] : "取消关注";
    };
    $scope.addFocus = function (tagName, target) {
        $(target).showLoading();
        var param = {
            type: 'vs',
            tagName: tagName,
            targetId: $scope.query.vsId
        };
        var request = {
            method: 'GET',
            url: G.baseUrl + '/api/tagging?description=focus',
            params: param
        };
        $http(request).success(
            function (response) {
                $(target).hideLoading();
                $(target).html(operationmap ? operationmap["取消关注"] : "取消关注");
                $(target).parent('span').removeClass('fa fa-eye-slash status-unfocus').addClass("fa fa-eye status-focus");
                if (!$scope.model.extendedvs.tags) $scope.model.extendedvs.tags = [];
                var f = _.find($scope.model.extendedvs.tags, function (item) {
                    return item.trim().toLowerCase() == tagName.toLowerCase();
                });
                if (!f) {
                    $scope.model.extendedvs.tags.push(tagName);
                }
            }
        );
    };
    $scope.removeFocus = function (tagName, target) {
        $(target).showLoading();
        var param = {
            type: 'vs',
            tagName: tagName,
            targetId: $scope.query.vsId
        };
        var request = {
            method: 'GET',
            url: G.baseUrl + '/api/untagging?description=unfocus',
            params: param
        };
        $http(request).success(
            function (response) {
                $(target).hideLoading();
                if (!$scope.model.extendedvs.tags) $scope.model.extendedvs.tags = [];
                $(target).html(operationmap ? operationmap["关注"] : "关注");
                $(target).parent('span').removeClass('fa fa-eye status-focus').addClass("fa fa-eye-slash status-unfocus");
                var index = $scope.model.extendedvs.tags.indexOf(tagName);
                if (index != -1) {
                    $scope.model.extendedvs.tags.splice(index, 1);
                }
            }
        );
    };
    // Status Area
    $scope.statusText = function () {
        var resource = $scope.resource;
        if (!resource || _.keys(resource).length == 0) return;

        var statusMap = resource['vs']['vs_vsInfoApp_status'];

        var s = $scope.getVsStatusProperty();
        switch (s) {
            case "activated":
                return statusMap['已激活'];
            case "deactivated":
                return statusMap['未激活'];
            case "tobeactivated":
                return statusMap['有变更']
            default:
                return statusMap['未知'];
        }
    };
    $scope.statusClass = function () {
        var s = $scope.getVsStatusProperty();
        switch (s) {
            case 'deactivated':
                return 'fa fa-circle status-deactivated';
            case 'activated':
                return 'fa fa-circle status-activated';
            case 'tobeactivated':
                return 'fa fa-circle status-tobeActivated';
            default :
                return 'fa fa-circle status-deactivated';
        }
    };
    // Tagging
    $scope.showDialog = function (value, type) {
        switch (type) {
            case 'deleteTag' : {
                $scope.currentTagName = value;
                $('#deleteVSTag').modal({backdrop: 'static'});
                break;
            }
            case 'deleteProp' : {
                if ($scope.canDeleteTag(value)) {
                    $scope.currentProperty = value;
                    $('#deleteVSProp').modal({backdrop: 'static'});
                }
                break;
            }
            case 'addTag' : {
                $('#addVSTag').modal({backdrop: 'static'});
                break;
            }
            case 'addProp' : {
                $('#addVSProp').modal({backdrop: 'static'});
            }
        }
    };
    $scope.canDeleteTag = function (p) {
        var pName = p.name.toLowerCase();
        if (pName == "status" || pName == "idc" || pName == "zone" || pName == "pci") {
            return false;
        }
        return true;
    };
    $scope.addTag = function (tagName, type) {
        var resource = $scope.resource;
        if (!resource || _.keys(resource).length == 0) return;

        var statusMap = resource['vs']['vs_vsInfoApp_operations'];

        var param = {
            type: 'vs',
            tagName: tagName,
            targetId: $scope.query.vsId
        };
        var request = {
            method: 'GET',
            url: G.baseUrl + '/api/tagging?description=' + $scope.query.tagreason,
            params: param
        };
        $('#addVSTag').modal('hide');
        $scope.processRequest(request, $('#operationConfrimModel'), '', statusMap['新增Tag']);
    };
    $scope.deleteTag = function (tagName, type) {
        var resource = $scope.resource;
        if (!resource || _.keys(resource).length == 0) return;

        var statusMap = resource['vs']['vs_vsInfoApp_operations'];
        var param = {
            type: 'vs',
            tagName: tagName,
            targetId: $scope.query.vsId
        };
        var request = {
            method: 'GET',
            url: G.baseUrl + '/api/untagging?description=' + $scope.query.deletetagreason,
            params: param
        };
        $('#deleteVSTag').modal('hide');
        $scope.processRequest(request, $('#operationConfrimModel'), '', statusMap['删除Tag']);
    };
    $('#addTagBt').click(function (e) {
        e.preventDefault();
        var validate = reviewData($('#addVSTag'));
        if (!validate) return;
        $scope.addTag($('#tagNameInput').val().trim());
    });
    $('#addPropertyBt').click(function (e) {
        e.preventDefault();
        var validate = reviewData($('#addVSProp'));
        if (!validate) return;
        $scope.addProperty({'name': $('#pname').val().trim(), 'value': $('#pvalue').val().trim()});
    });
    // Property
    $scope.addProperty = function (prop) {
        var resource = $scope.resource;
        if (!resource || _.keys(resource).length == 0) return;

        var statusMap = resource['vs']['vs_vsInfoApp_operations'];
        var param = {
            type: 'vs',
            pname: prop.name,
            pvalue: prop.value,
            targetId: $scope.query.vsId
        };
        var request = {
            method: 'GET',
            url: G.baseUrl + '/api/property/set?description=' + $scope.query.propertyreason,
            params: param
        };
        $('#addVSProp').modal("hide");
        if (prop.name == 'status') return;
        $scope.processRequest(request, $('#operationConfrimModel'), "", statusMap["添加Property"]);
    };
    $scope.deleteProperty = function (prop) {
        var resource = $scope.resource;
        if (!resource || _.keys(resource).length == 0) return;

        var statusMap = resource['vs']['vs_vsInfoApp_operations'];
        var param = {
            type: 'vs',
            pname: prop.name,
            pvalue: prop.value,
            targetId: $scope.query.vsId
        };
        var request = {
            method: 'GET',
            url: G.baseUrl + '/api/property/clear?description=' + $scope.query.deletepropertyreason,
            params: param
        };
        $('#deleteVSProp').modal('hide');
        $scope.processRequest(request, $('#operationConfrimModel'), "", statusMap["删除Property"]);
    };
    // Diff
    $scope.confirmDiffButtonDisable = true;
    $scope.targetDiffVsId;
    $scope.selectDiffVsId = function (t) {
        if (t) {
            var toId = t.originalObject.id;
            $scope.targetDiffVsId = toId;
            $scope.confirmDiffButtonDisable = false;
        }
    };
    $scope.confirmSelectVsId = function () {
        var resource = $scope.resource;
        if (!resource || _.keys(resource).length == 0) return;

        var statusMap = resource['vs']['vs_vsInfoApp_operations'];

        if ($scope.targetDiffVsId) {
            // has selected the current targetId
            $scope.confirmActivateText = statusMap["当前VsId"] + ':' + $scope.query.vsId + ". " + statusMap["对比VsId"] + ":" + $scope.targetDiffVsId;

            $http.get(G.baseUrl + "/api/vs?vsId=" + $scope.targetDiffVsId).success(
                function (res) {
                    var baseText = JSON.stringify(U.sortObjectFileds($scope.model.offlinevs), null, "\t");
                    var newText = JSON.stringify(U.sortObjectFileds(res), null, "\t");

                    var base = difflib.stringAsLines(baseText);
                    var newtxt = difflib.stringAsLines(newText);

                    var sm = new difflib.SequenceMatcher(base, newtxt);
                    var opcodes = sm.get_opcodes();
                    var diffoutputdiv = document.getElementById("diffOutput3");

                    diffoutputdiv.innerHTML = "";

                    diffoutputdiv.appendChild(diffview.buildView({
                        baseTextLines: base,
                        newTextLines: newtxt,
                        opcodes: opcodes,
                        baseTextName: statusMap["当前VsId"] + ': ' + $scope.query.vsId,
                        newTextName: statusMap["对比VsId"] + ':' + res.id,
                        viewType: 0
                    }));
                    $('#diffSeperateVses').modal('show');
                }
            );
        }
        else {
            alert(statusMap["请选择要比较的Vs"]);
        }
    };
    /**Basic Information Area
     * Version
     * History
     * VIPS
     * Tags and Properties
     * */
    $scope.vsSSLText = function () {
        if ($scope.model.extendedvs.ssl) return 'HTTPS';
        return 'HTTP';
    };
    $scope.vsVersionClass = function () {
        var v = $scope.getVsStatusProperty();
        switch (v) {
            case "activated":
                return "status-green link-comment";
            case "deactivated":
                return "status-red activeGroup";
            case "tobeactivated":
                return "status-yellow activeGroup";
            default:
                return "status-gray";
                break;
        }
    };
    $scope.vsVersionText = function () {
        var resource = $scope.resource;
        if (!resource || _.keys(resource).length == 0) return;

        var statusMap = resource['vs']['vs_vsInfoApp_status'];

        var s = $scope.getVsStatusProperty();
        if (s) s = s.toLowerCase();
        var v = $scope.model.onlinevs.data == undefined ? "unknownText" : $scope.model.onlinevs.data.version;
        switch (s) {
            case "activated":
                return statusMap["已是线上版本"];
            case "deactivated":
                return statusMap["无线上版本，点击上线"];
            case "tobeactivated":
                return statusMap["线上版本"] + v + "; " + statusMap["点击Diff"];
            default:
                return statusMap["未知"];
                break;
        }
    };
    // Vips
    $scope.vips = '';
    $scope.getVsVipText = function () {
        var ids = _.map($scope.view.extendedvs['slb-ids'], function (item) {
            var id = item.id;
            var slbvips = $scope.model.slbsObjects[id] ? $scope.model.slbsObjects[id].vips : '';
            var r = _.pluck(slbvips, 'ip');
            return r.join(',');
        });
        if (ids.length > 0) {
            $scope.vips = ids.join(',');
        }
    };
    $scope.getSlbNameWithID = function (id) {
        return $scope.model.slbsObjects[id] ? $scope.model.slbsObjects[id].name : '';
    };
    // History Area
    $scope.diffVersions = {};
    $scope.vshistoryversions = function () {
        $scope.diffVersions = {};
        var c = $scope.model.extendedvs.version;
        $scope.diffVersions[c] = c;

        $.each($scope.view.vshistoryversions, function (i, val) {
            if (val != c)
                $('#historyVersion' + val).removeClass('label-info');
        });
        $('#historyVersion' + c).addClass('label-info');
        $('#historyVersionModel').modal('show');
    };
    $scope.diffVsBetweenVersions = function () {
        var resource = $scope.resource;
        if (!resource || _.keys(resource).length == 0) return;

        var statusMap = resource['vs']['vs_vsInfoApp_operations'];

        var diffVersions = _.keys($scope.diffVersions);
        if (diffVersions.length < 2) {
            alert(statusMap["请选择两个版本进行diff"]);
        }
        $scope.confirmActivateText = statusMap["版本号"] + ':' + diffVersions[0] + statusMap["VS 版本号"] + ":" + diffVersions[1];
        var v1 = '';
        var v2 = '';
        $q.all(
            [
                $http.get(G.baseUrl + "/api/archive/vs?vsId=" + $scope.query.vsId + "&version=" + diffVersions[0]).success(
                    function (res) {
                        v1 = res;
                    }
                ),
                $http.get(G.baseUrl + "/api/archive/vs?vsId=" + $scope.query.vsId + "&version=" + diffVersions[1]).success(
                    function (res) {
                        v2 = res;
                    }
                )
            ]
        ).then(
            function () {
                var baseText = JSON.stringify(U.sortObjectFileds(v1), null, "\t");
                var newText = JSON.stringify(U.sortObjectFileds(v2), null, "\t");

                var base = difflib.stringAsLines(baseText);
                var newtxt = difflib.stringAsLines(newText);

                var sm = new difflib.SequenceMatcher(base, newtxt);
                var opcodes = sm.get_opcodes();
                var diffoutputdiv = document.getElementById("diffOutput3");

                diffoutputdiv.innerHTML = "";

                diffoutputdiv.appendChild(diffview.buildView({
                    baseTextLines: base,
                    newTextLines: newtxt,
                    opcodes: opcodes,
                    baseTextName: statusMap["版本号"] + ':' + diffVersions[0],
                    newTextName: statusMap["版本号"] + ':' + diffVersions[1],
                    viewType: 0
                }));
                $('#historyVersionModel').modal('hide');
                $('#diffSeperateVses').modal('show');
            }
        );
    };
    $scope.showDiffHistory = function () {
        return _.keys($scope.diffVersions).length != 2;
    };
    $scope.toggleVersion = function (x) {
        if ($scope.diffVersions[x]) {
            $scope.diffVersions = _.omit($scope.diffVersions, x);
            $('#historyVersion' + x).removeClass('label-info');
        } else {
            if (_.keys($scope.diffVersions).length < 2) {
                $scope.diffVersions[x] = x;
                $('#historyVersion' + x).addClass('label-info');
            } else {
                $('#historyVersion' + _.keys($scope.diffVersions)[1]).removeClass('label-info');
                $scope.diffVersions = _.omit($scope.diffVersions, _.keys($scope.diffVersions)[1]);
                $scope.diffVersions[x] = x;
                $('#historyVersion' + x).addClass('label-info');
            }
        }
    };
    $scope.isSelectedVersion = function (x) {
        if ($scope.diffVersions[x]) {
            return "label-info";
        }
    };

    // Tagging Area
    $scope.showExtendedTag = function () {
        if ($scope.model.extendedvs.tags == undefined) return false;
        var u_tag = _.filter($scope.model.extendedvs.tags, function (m) {
            return m.trim().toLowerCase().startWith("user_");
        });
        var p = u_tag.length == $scope.model.extendedvs.tags.length;
        var t = $scope.model.extendedvs.tags == undefined || $scope.model.extendedvs.tags.length == 0;
        if (t || p) return false;
        return true;
    };
    $scope.showCurrentTag = function (t) {
        var x = !t.trim().toLowerCase().startWith('user_');
        return x;
    };
    $scope.showRemoveTagBt = function (v) {
        if (v.toLowerCase().startWith('owner') || v.toLowerCase().startWith('user')) return false;
        return A.canDo("Slb", "PROPERTY", $scope.model.extendedvs.id) && $scope.query.showextended;
    };
    // Property
    $scope.showExtendedProperty = function () {
        return !($scope.model.extendedvs.properties == undefined || $scope.model.extendedvs.properties.length == 0);
    };
    $scope.showRemoveProperty = function (v) {
        var v = v.name;
        if (v.toLowerCase() == "idc" || v.toLowerCase() == "zone" || v.toLowerCase() == "status" || v.toLowerCase() == "pci") return false;
        return A.canDo("Vs", "PROPERTY", $scope.model.extendedvs.id) && $scope.query.showextended;
    };
    $scope.showLogs = function () {
        return $scope.model.currentlogs.length > 0;
    };
    /**VS Domains Area
     * */
    $scope.toBeRemovedDomains = {};
    window.vsDomainsRemoveEvent = {
        'click .vsRemoveDomain': function (e, value, row) {
            var toBeRemovedDomains = [row.name];
            $scope.toBeRemovedDomains = toBeRemovedDomains;
            var str = '';
            $('.to-be-removed-domains').children().remove();

            if (toBeRemovedDomains != undefined && toBeRemovedDomains.length > 0) {
                $.each(toBeRemovedDomains, function (i, val) {
                    str += "<tr>";
                    str += "<td><span class='ip'>" + val + "</span></td>";
                    str += "</tr>";
                });
            }
            $('.to-be-removed-domains').append(str);
            $('#removeDomainDialog').modal('show');
        }
    };
    $('#vs-domains-table').on('check.bs.table uncheck.bs.table check-all.bs.table uncheck-all.bs.table', function () {
        $('#removeDomain').prop('disabled', !$('#vs-domains-table').bootstrapTable('getSelections').length);
    });
    $scope.getIdSelections = function () {
        return $.map($('#vs-domains-table').bootstrapTable('getSelections'), function (row) {
            return row.name;
        });
    };
    $scope.newDomains = [{"name": ''}];
    $scope.domainsTable = {
        columns: $scope.newDomains,
        add: function (index) {
            this.columns.push({"name": ''});
        },
        remove: function (index) {
            this.columns.splice(index, 1);
        }
    };
    $scope.addDomainToVs = function () {
        var addDomains = _.pluck($scope.newDomains, "name");
        var param = {
            vsId: $scope.query.vsId,
            domain: addDomains.join()
        };
        var request = {
            method: 'GET',
            url: G.baseUrl + '/api/vs/addDomain?description=' + $scope.query.adddomainreason,
            params: param
        };
        $('#addDomainDialog').modal('hide');
        var loading = "<img src='/static/img/spinner.gif' /> 正在往VS中新增指定的Domain";
        $('#operationConfrimModel').modal('show').find(".modal-body").html(loading);

        $scope.processRequest(request, $('#operationConfrimModel'), "新增Domain列表", "新增成功");
    };
    $scope.removeDomainFromVs = function () {
        var removeDomains = $scope.toBeRemovedDomains.join();
        var param = {
            vsId: $scope.query.vsId,
            domain: removeDomains
        };
        var request = {
            method: 'GET',
            url: G.baseUrl + '/api/vs/removeDomain?description=' + $scope.query.removedomainreason,
            params: param
        };
        $('#removeDomainDialog').modal('hide');
        $('#addDomainDialog').modal('hide');
        var loading = "<img src='/static/img/spinner.gif' /> 正在从当前VS中删除指定的Domain";
        $('#operationConfrimModel').modal('show').find(".modal-body").html(loading);
        $scope.processRequest(request, $('#operationConfrimModel'), "下线Domain", "下线成功");
    };
    $('#removeDomain').click(
        function () {
            var toBeRemovedDomains = _.toArray($scope.getIdSelections());
            $scope.toBeRemovedDomains = toBeRemovedDomains;
            var str = '';
            $('.to-be-removed-domains').children().remove();

            if (toBeRemovedDomains != undefined && toBeRemovedDomains.length > 0) {
                $.each(toBeRemovedDomains, function (i, val) {
                    str += "<tr>";
                    str += "<td><span class='ip'>" + val + "</span></td>";
                    str += "</tr>";
                });
            }
            $('.to-be-removed-domains').append(str);
            $('#removeDomainDialog').modal('show');
        }
    );
    /**Vs related groups*/
    $scope.members = {};
    $scope.getGroupMetaData = function (group) {
        var g_servers = group["group-servers"];
        $.each(g_servers, function (i, g) {
            if ($scope.members[g.ip] == undefined) {
                $scope.sCount++;
                $scope.members[g.ip] = "";
            }
        });
        // get the group status
        var g_property = group.properties;
        $.each(g_property, function (i, property) {
            var status = property.name.toLowerCase();
            if (status == "status") {
                group.status = property.value;
                switch (property.value) {
                    case "deactivated":
                        $scope.offline++;
                        break;
                    case "activated":
                        $scope.online++;
                        break;
                    default :
                        $scope.tobeOnline++;
                        break;
                }
            }
            if (status == "healthy") {
                group.grouphealthy = property.value;
            }
            if (status == "idc") {
                group.idc = property.value;
            }
        });
    };
    $scope.getGroupQPSMetaData = function (group) {
        if ($scope.groupStatisticsMap[group.id]) {
            group.qps = $scope.groupStatisticsMap[group.id].qps;
            group['member-count'] = $scope.groupStatisticsMap[group.id]['member-count'];
            $scope.mCount += $scope.groupStatisticsMap[group.id]['member-count'];
        }
    };
    $scope.getAppInfoMetaData = function (val) {
        if ($scope.appInfoMap[val['app-id']]) {
            if (!$scope.appsCollection[val['app-id']]) $scope.appsCollection[val['app-id']] = 1;
            else $scope.appsCollection[val['app-id']]++;

            val['app-name'] = $scope.appInfoMap[val['app-id']]['chinese-name'];
        }
    };

    // VS SSL Management
    $scope.showSslRow = function () {
        var certificates = $scope.certificates;
        if (!certificates) return false;

        return _.keys(certificates).length > 0;
    };

    function responseHandler(res) {
        $.each(res.rows, function (i, row) {
            row.state = $.inArray(row.id, selections) !== -1;
        });
        return res;
    };
    $scope.onlineGroupData;
    $scope.tobeActivatedGroupData;
    $scope.confirmActivateText = "当前版本与线上版本比较";
    window.operateEvents = {};
    $scope.activateGroupTitleClass = function () {
        try {
            if ($scope.information.onlineGroup.data.version != undefined && $scope.information.onlineGroup.data.version == $scope.information.group.version) {
                return "status-red-important";
            }
        } catch (e) {

        }
    };

    $scope.onlineVGroupData;
    $scope.tobeActivatedVGroupData;

    $('#vs-groups-table').on('check.bs.table uncheck.bs.table ' +
        'check-all.bs.table uncheck-all.bs.table', function () {
        $('#createTag').prop('disabled', !$('#groups-data-table').bootstrapTable('getSelections').length);
    });
    var byId = function (id) {
        return document.getElementById(id);
    };
    $('#createGroup').click(function () {
        window.location.href = "/portal/group/new#?env=" + G.env;
    });
    $scope.activateGroup = function () {
        var confirmDialog = $('#activateGroupResultConfirmDialog');
        var loading = "<img src='/static/img/spinner.gif' /> 正在激活";
        confirmDialog.modal('show').find(".modal-body").html(loading);
        confirmDialog.modal("show").find(".modal-title").html("正在激活");

        $('#confirmActivateGroup').modal('hide');
        var request = {
            method: 'GET',
            url: G.baseUrl + "/api/activate/group?groupId=" + $scope.tobeActivatedGroupData.id + "&description=" + $scope.query.user
        };
        var msg = "";
        $http(request).success(
            function (res) {
                if (res == undefined || res.code != undefined) {
                    msg = res.message;
                    var errText = "<span class='fa fa-times'></span><span style='padding-left: 10px'> 激活失败</span>";
                    confirmDialog.modal('show').find(".modal-title").html(errText);
                    confirmDialog.modal('show').find(".modal-body").html("失败原因:" + msg);
                }
                else {
                    var successText = "<span class='fa fa-check'></span><span style='padding-left: 10px'>激活成功</span>";
                    confirmDialog.modal('show').find(".modal-title").html(successText);
                    confirmDialog.modal('show').find(".modal-body").html("激活成功");
                    setTimeout(function () {
                        confirmDialog.find('#closeActivateConfrimBt').click();
                    }, 2000);
                }
            }
        ).error(function (reject) {
            msg = reject.message;
            var errText = "<span class='fa fa-times'></span><span style='padding-left: 10px'>激活失败</span>";
            confirmDialog.modal('show').find(".modal-title").html(errText);
            confirmDialog.modal('show').find(".modal-body").html("失败原因:" + msg);
        });
    };
    $scope.activateVGroup = function () {
        var confirmDialog = $('#activateGroupResultConfirmDialog');
        var loading = "<img src='/static/img/spinner.gif' /> 正在激活";
        confirmDialog.modal('show').find(".modal-body").html(loading);
        confirmDialog.modal("show").find(".modal-title").html("正在激活");

        $('#confirmActivateVGroup').modal('hide');
        var request = {
            method: 'GET',
            url: G.baseUrl + "/api/activate/group?groupId=" + $scope.tobeActivatedVGroupData.id
        };
        var msg = "";
        $http(request).success(
            function (res) {
                if (res == undefined || res.code != undefined) {
                    msg = res.message;
                    var errText = "<span class='fa fa-times'></span><span style='padding-left: 10px'> 激活失败</span>";
                    confirmDialog.modal('show').find(".modal-title").html(errText);
                    confirmDialog.modal('show').find(".modal-body").html("失败原因:" + msg);
                }
                else {
                    var successText = "<span class='fa fa-check'></span><span style='padding-left: 10px'>激活成功</span>";
                    confirmDialog.modal('show').find(".modal-title").html(successText);
                    confirmDialog.modal('show').find(".modal-body").html("激活成功");
                    setTimeout(function () {
                        confirmDialog.find('#closeActivateConfrimBt').click();
                    }, 2000);
                }
            }
        ).error(function (reject) {
            msg = reject.message;
            var errText = "<span class='fa fa-times'></span><span style='padding-left: 10px'>激活失败</span>";
            confirmDialog.modal('show').find(".modal-title").html(errText);
            confirmDialog.modal('show').find(".modal-body").html("失败原因:" + msg);
        });
    }
    $('#closeActivateConfrimBt').click(function (e) {
        var hashData = {};
        hashData.timeStamp = new Date().getTime();
        H.setData(hashData);
    });
    $scope.toggleCountSearch = function (stateText, value) {
        if (value != 0) {
            //window.location.href="/portal/groups#?env="+ G.env+"&groupStatus="+stateText;
            var hashData = {};
            hashData.env = G.env;
            hashData.groupStatus = stateText;
            hashData.timeStamp = new Date().getTime();
            H.setData(hashData);
        }
    };
    $scope.countSearchClass = function (value) {
        var v = value == 0 ? 'link-comment' : '';
        return v;
    };

    function getSingleGroup(groupId) {
        $http.get(G.baseUrl + "/api/group?groupId=" + groupId + "&mode=online").then(
            function successCallback(res) {
                if (res.data.code) {
                    $scope.onlineGroupData = "No online version!!!";
                } else {
                    $scope.onlineGroupData = res.data;
                }
            },
            function errorCallback(res) {
                if (res.status == 400 && res.data.message == "Group cannot be found.") {
                    $scope.onlineGroupData = "No online version!!!";
                }
            }
        );

        $http.get(G.baseUrl + "/api/group?groupId=" + groupId).success(
            function (res) {
                $scope.tobeActivatedGroupData = res;
            }
        );
    }

    function getSingleVGroup(groupId) {
        $http.get(G.baseUrl + "/api/vgroup?groupId=" + groupId + "&mode=online").then(
            function successCallback(res) {
                if (res.data.code) {
                    $scope.onlineVGroupData = "No online version!!!";
                } else {
                    $scope.onlineVGroupData = res.data;
                }
            },
            function errorCallback(res) {
                if (res.status == 400) {
                    $scope.onlineVGroupData = "No online version!!!";
                }
            }
        );

        $http.get(G.baseUrl + "/api/vgroup?groupId=" + groupId).success(
            function (res) {
                $scope.tobeActivatedVGroupData = res;
            }
        );
    }

    $scope.initTable = function () {
        var ta = $scope.resource['vs']['vs_vsInfoApp_domains']['table'];
        $('#vs-domains-table').bootstrapTable({
            toolbar: "#vsInfo-domainList-toolbar",
            columns: [[
                {
                    field: 'state',
                    checkbox: true,
                    rowspan: 2,
                    align: 'center',
                    valign: 'middle'
                },
                {
                    field: 'name',
                    title: ta['domains'],
                    align: 'left',
                    valign: 'middle',
                    sortable: true
                },
                {
                    field: 'Operation',
                    title: ta['operation'],
                    align: 'center',
                    valign: 'middle',
                    width: '120px',
                    events: vsDomainsRemoveEvent,
                    formatter: function vsDomainsRemoveFormatter() {
                        var p = A.canDo("Vs", "UPDATE", $scope.query.vsId) ? "" : "hide";
                        return '<button type="button" class="btn btn-little btn-info vsRemoveDomain ' + p + '" title="删除" aria-label="Left Align" ><span class="fa fa-minus"></span></button>';
                    }
                },
                {
                    title: ta['links'],
                    align: 'center',
                    width: '300px',
                    valign: 'middle',
                    formatter: function (value, row, index) {
                        var env = $scope.env;
                        env == 'pro' ? 'PROD' : env.toUpperCase();
                        var vsId = $scope.query.vsId;
                        var domain = row.name;

                        var vsProperties = _.indexBy($scope.model.extendedvs.properties, function (v) {
                            return v.name;
                        });

                        var idc = vsProperties['idc'] ? vsProperties['idc'].value : '-';

                        var idcText = '';
                        if (idc !== '-') {
                            var idcArray = _.map(idc.split(','), function (i) {
                                return "idc%3D'" + vsApp.convertIDC(i) + "'";
                            });

                            idcText = '(' + idcArray.join('%20OR%20') + ')';
                        }

                        var query = "domain%3D'" + domain + "'%20AND%20" + idcText;
                        var esLink = vsApp.getEsHtml(query);

                        var str = '<div>';
                        var dashboard = G.dashboardportal + '/#env=' + env + '&metric-name=slb.req.count&interval=1m&start-time=' + dashboardStartTime + '&end-time=' + dashboardEndTime + '&chart=line&aggregator=sum&ts=1515983913677&tags={"vsid":["' + vsId + '"],"domain":["' + domain + '"]}&group-by=[status]';

                        if (G[env]['urls'].dashboard) {
                            str += '<div class="system-link">' +
                                '<a class="pull-left dashboard" title="Dashboard" target="_blank" href=\'' + dashboard + '\'>Dashboard</a>' +
                                '</div>';
                        }

                        if (G[env]['urls']['hickwall']) {
                            var hickwallLink = G[$scope.env] ? G[$scope.env].urls.hickwall + '/d/Y7xzaxgWz/slb-vs-domain?orgId=6&var-vsid=' + vsId + '&var-domain=' + row.name : '';
                            str += '<div class="system-link">' +
                                '<a class="pull-left hickwall" title="Hickwall" target="_blank" href=\'' + hickwallLink + '\'>Hickwall</a>' +
                                '</div>';
                        }

                        if (G[env]['urls']['es']) {
                            str += esLink;
                        }

                        if (str == '<div>') str = '-';
                        str += '</div>';

                        return str;
                    }
                }
            ], []],
            search: true,
            showRefresh: true,
            showColumns: true,
            data: $scope.model.extendedvs.domains,
            sortOrder: 'desc',
            classes: "table-bordered  table-hover table-striped table",
            minimumCountColumns: 2,
            idField: 'id',
            sidePagination: 'client',
            formatLoadingMessage: function () {
                return "<img class='loading-img' src='/static/img/loading_100.gif' /> " + ta['loading'];
            },
            formatNoMatches: function () {
                return '<i class="fa status-alert fa-exclamation-triangle" aria-hidden="true"></i> ' + ta['nodata'];
            }
        });

        var ta2 = $scope.resource['vs']['vs_vsInfoApp_groups']['table'];
        var statusMap = $scope.resource['vs']['vs_vsInfoApp_status'];
        $('#vs-groups-table').bootstrapTable({
            toolbar: "#vsInfo-groupList-toolbar",
            columns: [[{
                field: 'state',
                checkbox: true,
                rowspan: 2,
                align: 'center',
                valign: 'middle'
            },
                {
                    field: 'id',
                    title: ta2['id'],
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    width: '100px',
                    formatter: function (value, row, index) {
                        var groupPageUrl = "";
                        if (row["isvgroup"] != undefined) {
                            groupPageUrl = '/portal/vgroup#?env=' + G.env + '&groupId=' + value;
                            return '<span class="fa  fa-magic status-gray" title="Virtual Group"><a title="' + value + '" href="' + groupPageUrl + '">' + value + '</a></span>';
                        }
                        else {
                            groupPageUrl = '/portal/group#?env=' + G.env + '&groupId=' + value;
                            return '<a title="' + value + '" href="' + groupPageUrl + '">' + value + '</a>';
                        }
                    }
                },
                {
                    field: 'name',
                    title: ta2['name'],
                    align: 'left',
                    resetWidth: true,
                    valign: 'middle',
                    width: '400px',
                    sortable: true,
                    formatter: function (value, row, index) {
                        var groupPageUrl = "";
                        if (row["isvgroup"] != undefined) {
                            groupPageUrl = '/portal/vgroup#?env=' + G.env + '&groupId=' + row.id;
                        }
                        else {
                            groupPageUrl = '/portal/group#?env=' + G.env + '&groupId=' + row.id;
                        }
                        return '<a title="' + value + '" href="' + groupPageUrl + '"><div style="word-break: break-all">' + value + '</div></a>';
                    }
                },
                {
                    field: 'paths',
                    title: ta2['path'],
                    width: '580px',
                    align: 'left',
                    valign: 'middle',
                    sortName: 'pathOrder',
                    sortable: true,
                    visible: false,
                    formatter: function (value, row, index) {
                        var result = '';
                        var currentVsId = parseInt($scope.query.vsId);
                        var current = _.find(value, function (item) {
                            return item.vsId == currentVsId
                        });
                        var index = _.findIndex(value, function (r) {
                            return r.vsId == currentVsId;
                        });
                        var nclass = 'path-main-item';
                        if (current) {
                            result += $scope.generatePathLink(nclass, current);
                        }
                        nclass = 'path-sub-item';
                        $.each(value, function (i, item) {
                            if (index != i) {
                                result += $scope.generatePathLink(nclass, item);
                            }
                        });
                        return result;
                    }
                },
                {
                    field: 'app-name',
                    title: ta2['app'],
                    align: 'left',
                    valign: 'middle',
                    width: '300px',
                    sortable: true,
                    formatter: function (value, row, index) {
                        if (value == undefined) value = row['app-id'];
                        else value = row['app-id'] + '(' + value + ')'
                        return "<a style='word-break:break-all' title='" + value + "' target='_blank' href='/portal/app#?env=" + G.env + "&appId=" + row['app-id'] + "'>" + value + "</a>";
                    }
                },
                {
                    field: 'idc',
                    title: ta2['idc'],
                    align: 'left',
                    valign: 'middle',
                    width: '150px',
                    sortable: true
                },
                {
                    field: 'group-servers',
                    title: ta2['members'],
                    align: 'left',
                    valign: 'middle',
                    visible: false,
                    formatter: function (value, row, index) {
                        return value ? value.length : 0;
                    },
                    sortable: true
                },
                {
                    field: 'qps',
                    title: ta2['qps'],
                    align: 'left',
                    valign: 'middle',
                    visible: false,
                    formatter: function (value, row, index) {
                        return value ? '<a target="_blank" href="/portal/group/traffic' + H.generateHashStr({
                            env: G.env,
                            groupId: row.id
                        }) + '">' + Math.floor(value) + '</a>' : '-';
                    },
                    sortable: true
                },
                {
                    field: 'status',
                    title: ta2['status'],
                    align: 'left',
                    valign: 'middle',
                    width: '100px',
                    formatter: function (value, row, index) {


                        var str = "";
                        switch (value) {
                            case "deactivated":
                                str = "<span class='status-gray'>" + statusMap["未激活"] + "</span>";
                                break;
                            case "activated":
                                str = "<span class='status-green'>" + statusMap["已激活"] + "</span>";
                                break;
                            case "toBeActivated":
                                if (row.isvgroup) {
                                    str = "<span class='diffVGroup status-yellow'>" + statusMap["有变更"] + "</span>";
                                }
                                else {
                                    str = "<span class='diffGroup status-yellow'>" + statusMap["有变更"] + "</span>";
                                }

                                break;
                            default:
                                str = "-";
                                break;
                        }
                        return str;
                    },
                    events: operateEvents,
                    sortable: true
                },
                {
                    field: 'grouphealthy',
                    title: ta2['health'],
                    valign: 'middle',
                    align: 'center',
                    width: '100px',
                    formatter: function (value, row, index) {
                        if (row.isvgroup) {
                            return '<a><span title="VGroup不涉及健康检测">-</span></a>';
                        }
                        var str = '';
                        switch (value) {
                            case "healthy":
                                str = '<a href="/portal/group#?env=' + G.env + '&groupId=' + row.id + '"><span class="fa fa-circle status-green" title="完全健康"></span></a>';
                                break;
                            case "broken":
                                str = '<a href="/portal/group#?env=' + G.env + '&groupId=' + row.id + '"><span class="fa fa-circle status-red" title="全部失败"></span></a>';
                                break;
                            case "unhealthy":
                                str = '<a href="/portal/group#?env=' + G.env + '&groupId=' + row.id + '"><span class="fa fa-circle status-yellow" title="存在部分问题Server"></span></a>';
                                break;
                            default:
                                str = "-";
                                break;
                        }
                        return str;
                    },
                    sortable: true
                },
                {
                    title: ta2['links'],
                    align: 'center',
                    width: '250px',
                    valign: 'middle',
                    formatter: function (value, row, index) {
                        var env = $scope.env;
                        env == 'pro' ? 'PROD' : env.toUpperCase();
                        var groupId = row.id;

                        var query = "group_id%3D'" + groupId + "'";


                        var str = '<div>';
                        var dashboard = G.dashboardportal + '/#env=' + env + '&metric-name=slb.req.count&interval=1m&start-time=' + dashboardStartTime + '&end-time=' + dashboardEndTime + '&chart=line&aggregator=sum&ts=1515983913677&tags={"group_id":["' + row.id + '"]}&group-by=[status]';

                        if (G[env]['urls'].dashboard) {
                            str += '<div class="system-link">' +
                                '<a class="pull-left dashboard" title="Dashboard" target="_blank" href=\'' + dashboard + '\'>Dashboard</a>' +
                                '</div>';
                        }

                        if (G[env]['urls']['hickwall']) {
                            var hickwallLink = G[$scope.env] ? G[$scope.env].urls.hickwall + '/d/Cah2BxgWz/slb-group?var-group_id=' + row.id : '';
                            str += '<div class="system-link">' +
                                '<a class="pull-left hickwall" title="Hickwall" target="_blank" href=\'' + hickwallLink + '\'>Hickwall</a>' +
                                '</div>';
                        }

                        var esLink = vsApp.getEsHtml(query);

                        if (G[env]['urls']['es']) {
                            str += '<div style="text-align: center">' +
                                esLink +
                                '</div>';
                        }

                        str += '</div>';

                        return str;
                    }
                }
            ], []],
            search: true,
            showRefresh: true,
            showColumns: true,
            sortName: 'qps',
            sortOrder: 'desc',
            data: $scope.allGroups,
            classes: "table-bordered  table-hover table-striped table",
            minimumCountColumns: 2,
            pagination: true,
            idField: 'id',
            pageSize: 20,
            resizable: true,
            resizeMode: 'overflow',
            sidePagination: 'client',
            pageList: [20, 40, 80, 200],
            responseHandler: "responseHandler",
            formatLoadingMessage: function () {
                return "<img class='loading-img' src='/static/img/loading_100.gif' /> " + ta2['loading'];
            },
            formatNoMatches: function () {
                return '<i class="fa status-alert fa-exclamation-triangle" aria-hidden="true"></i> ' + ta2['nodata'];
            }
        });
        var ta3 = $scope.resource['vs']['vs_vsInfoApp_policies']['table'];

        $('#vs-policy-table').bootstrapTable({
            toolbar: "#vsInfo-PolicyList-toolbar",
            columns: [[
                {
                    field: 'id',
                    title: ta3['id'],
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    formatter: function (value, row, index) {
                        var trafficPageUrl = "";
                        trafficPageUrl = '/portal/policy#?env=' + G.env + '&policyId=' + value;
                        return '<a title="' + value + '" href="' + trafficPageUrl + '">' + value + '</a>';
                    }
                },
                {
                    field: 'name',
                    title: ta3['name'],
                    align: 'left',
                    resetWidth: true,
                    valign: 'middle',
                    width: '400px',
                    sortable: true,
                    formatter: function (value, row, index) {
                        var trafficPageUrl = '/portal/policy#?env=' + G.env + '&policyId=' + row.id;
                        return '<a title="' + value + '" href="' + trafficPageUrl + '"><div style="word-break: break-all">' + value + '</div></a>';
                    }
                },
                {
                    field: 'paths',
                    title: ta3['path'],
                    width: '600px',
                    align: 'left',
                    valign: 'middle',
                    sortName: 'pathOrder',
                    sortable: true,
                    visible: false,
                    formatter: function (value, row, index) {
                        var result = '';
                        $.each(value, function (i, v) {
                            var p = v.path;
                            if (v.path.length > 25) {
                                p = v.path.substring(0, 22);
                                p = p + "...";
                            }
                            var pathStr = '';
                            var priorityStr = '';
                            var sslStr = '';
                            var vsIdStr = '';
                            var slbIdStr = '';
                            if (v.path) {
                                pathStr = '<span class="">' + p + '</span>';
                                priorityStr = '&nbsp;<span class="">' + v.priority + '</span>';
                                sslStr = '&nbsp;<span class="">' + (v.ssl ? 'https' : 'http') + '</span>';
                                vsIdStr = '&nbsp;<a class="" target="_blank" href="' + '/portal/vs#?env=' + G.env + '&vsId=' + v.vsId + '" title="' + v.domain + '">' + v.vsId + '</a>';
                                slbIdStr += '(';
                                $.each(v.slbId, function (i, slbId) {
                                    if (i == v.slbId.length - 1) {
                                        slbIdStr += '<a class="" target="_blank" href="' + '/portal/slb#?env=' + G.env + '&slbId=' + slbId + '">' + slbId + '</a>';
                                    } else {
                                        slbIdStr += '<a class="" target="_blank" href="' + '/portal/slb#?env=' + G.env + '&slbId=' + slbId + '">' + slbId + '</a>,';
                                    }
                                });
                                slbIdStr += ')';

                                var tempStr = "" +
                                    "<div class='col-md-6' style='padding:0;line-height:34px;white-space: nowrap;overflow:hidden;color:#196eaa;font-weight:bold;' title='" + v.path + "'>" +
                                    pathStr +
                                    "</div>" +
                                    '<div class="col-md-6" style="padding:0;line-height:34px;">' +
                                    '<div class="col-md-3" style="padding:0;line-height:34px;">' + priorityStr + '</div>' +
                                    '<div class="col-md-3" style="padding:0;line-height:34px;">' + sslStr + '</div>' +
                                    '<div class="col-md-3" style="padding:0;line-height:34px;">' + vsIdStr + '</div>' +
                                    '<div class="col-md-3" style="padding:0;line-height:34px;">' + slbIdStr + '</div>' +
                                    '</div>';
                                result = result + '<div class="row" style="margin:0;">' + tempStr + '</div>';
                            }
                        });
                        result = '<div class="row" style="margin:0">' + result + '</div>';
                        return result;
                    }
                },
                {
                    field: 'controls2',
                    title: ta3['group'],
                    align: 'left',
                    valign: 'middle',
                    events: operateEvents,
                    visible: true,
                    formatter: function (value, row, index) {
                        var result = '';

                        $.each(value, function (i, v) {
                            var p = v.groupName;
                            /* if (v.groupName.length > 25) {
                             p = v.groupName.substring(0, 22);
                             p = p + "...";
                             }*/
                            var appStr = '';
                            var appId = $scope.model.groups[v.group.id]['app-id'];
                            appStr += appId;
                            var app = $scope.model.apps[appId];
                            if (app) {
                                appStr += '(' + app['chinese-name'] + ')';
                            } else {
                                appStr += '(-)';
                            }
                            var link = (v.group.type === 'VGROUP' ? '/portal/vgroup' : '/portal/group') + '#?env=' + $scope.env + '&groupId=' + v.group.id;
                            var appLink = '/portal/app#?env=' + $scope.env + '&appId=' + appId;

                            result += '<div style="padding-top: 10px"><a href="' + link + '">' + v.group.id + '</a>' + '/' + '<a href="' + link + '">' + p + '</a>' + '/' + '<a href="' + appLink + '">' + appStr + '</a></div>';

                        });
                        result = '<div class="row" style="margin:0">' + result + '</div>';
                        return result;
                    },
                    sortable: true
                },
                {
                    field: 'controls1',
                    title: ta3['controls'],
                    align: 'left',
                    width: '600px',
                    valign: 'middle',
                    events: operateEvents,
                    visible: false,
                    formatter: function (value, row, index) {
                        var result = '';
                        $.each(value, function (i, v) {
                            var p = v.groupName;
                            if (v.groupName.length > 25) {
                                p = v.groupName.substring(0, 22);
                                p = p + "...";
                            }
                            var groupStr = '<span class="">' + p + '</span>';
                            var idcStr = '&nbsp;<span class="">' + v.idc + '</span>';
                            var statusStr = '&nbsp;<span class="">' + v.status + '</span>';
                            var ownerStr = 'N/A';
                            if (v.owner != 'N/A')
                                ownerStr = '&nbsp;<a class="" target="_blank" href="' + '/portal/user#?env=' + G.env + '&userId=' + v.owner + '" title="' + v.owner + '">' + v.owner + '</a>';

                            var tempStr =
                                "<div class='col-md-6' style='padding:0;line-height:34px;white-space: nowrap;overflow:hidden;' title='" + v.groupName + "'><a title='" + v.groupName + "' target='blank' href='/portal/group#?env=" + G.env + "&groupId=" + v.group.id + "'>" +
                                "<div class='col-md-3'>" + v.group.id + "</div>" +
                                "<div class='col-md-9'>" + groupStr + "</div>" +
                                "</a></div>" +
                                '<div class="col-md-6" style="padding:0;line-height:34px;">' +
                                '<div class="col-md-4" style="padding:0;line-height:34px;">' + idcStr + '</div>' +
                                '<div class="col-md-4" style="padding:0;line-height:34px;">' + statusStr + '</div>' +
                                '<div class="col-md-4" style="padding:0;line-height:34px;">' + ownerStr + '</div>' +
                                '</div>';
                            result = result + '<div class="row" style="margin:0;">' + tempStr + '</div>';
                        });
                        result = '<div class="row" style="margin:0">' + result + '</div>';
                        return result;
                    },
                    sortable: true
                },
                {
                    field: 'target',
                    title: ta3['target'],
                    align: 'left',
                    valign: 'middle',
                    width: '200px',
                    visible: false,
                    sortable: true
                },
                {
                    field: 'idc',
                    title: ta3['idc'],
                    align: 'left',
                    valign: 'middle',
                    sortable: true
                },
                {
                    field: 'status',
                    title: ta3['status'],
                    align: 'left',
                    events: operateEvents,
                    valign: 'middle',
                    width: '152px',
                    formatter: function (value, row, index) {
                        var str = "";
                        switch (value) {
                            case "deactivated":
                                str = "<span class='status-gray'>" + statusMap["未激活"] + "</span>";
                                break;
                            case "activated":
                                str = "<span class='status-green'>" + statusMap["已激活"] + "</span>";
                                break;
                            case "toBeActivated":
                                str = "<span class='diffTraffic status-yellow'>" + statusMap["有变更"] + "(<a data-toggle='modal' data-target='#activateVSModal'>Diff</a>)</span>";
                                break;
                            default:
                                str = "-";
                                break;
                        }
                        return str;
                    },
                    sortable: true
                },
                {
                    title: ta3['links'],
                    align: 'center',
                    width: '130px',
                    valign: 'middle',
                    formatter: function (value, row, index) {
                        var esLink = vsApp.getEsHtml('policy_name:' + row.id);
                        var str = '<div style="text-align: center">' +
                            esLink +
                            '</div>'
                        return str;
                    }
                }
            ], []],
            search: true,
            showRefresh: true,
            showColumns: true,
            sortName: 'id',
            sortOrder: 'desc',
            data: $scope.traffics,
            classes: "table-bordered  table-hover table-striped table",
            minimumCountColumns: 2,
            pagination: true,
            idField: 'id',
            pageSize: 20,
            resizable: true,
            resizeMode: 'overflow',
            sidePagination: 'client',
            pageList: [20, 40, 80, 200],
            responseHandler: "responseHandler",
            formatLoadingMessage: function () {
                return "<img class='loading-img' src='/static/img/loading_100.gif' /> " + ta3['loading'];
            },
            formatNoMatches: function () {
                return '<i class="fa status-alert fa-exclamation-triangle" aria-hidden="true"></i> ' + ta3['nodata'];
            }
        });
        var ta4 = $scope.resource['vs']['vs_vsInfoApp_certs']['table'];
        $('#certificates-table').bootstrapTable({
            toolbar: "#cookie-List-toolbar",
            columns: [[
                {
                    field: 'cid',
                    title: ta4['cid'],
                    align: 'left',
                    sortable: true,
                    formatter: function (value, row, index) {
                        return value;
                    }
                },
                {
                    field: 'status',
                    title: ta4['status'],
                    align: 'left',
                    resetWidth: true,
                    valign: 'middle',
                    sortable: true,
                    formatter: function (value, row, index) {
                        value = value.toLowerCase();
                        if (value == 'activated') {
                            return '<span class="status-green">在线</span>';
                        } else if (value == 'canary') {
                            return '<span class="status-red">正在灰度</span>';
                        }
                    }
                },
                {
                    field: 'Operation',
                    width: '200px',
                    title: 'Operation',
                    align: 'left',
                    sortable: true,
                    events: certificateEvent,
                    formatter: function (value, row, index) {
                        var str = '';
                        if (row.status == 'CANARY') {
                            str += '<button class="btn-xs btn-info canary-test"><i class="fa fa-certificate " style="padding-right:5px"></i>继续灰度</button>';
                            str += '<button class="btn-xs btn-info canary-revert" style="margin-left: 10px;"><i class="fa fa-reply" style="padding-right:5px"></i>回退证书</button>';
                        } else {
                            str += '<button class="btn-xs btn-danger activate-slb-bt"><i class="fa fa-th-large " style="padding-right:5px"></i>重新激活SLB</button>';
                        }
                        return str;
                    }
                }
            ], []],
            search: true,
            showRefresh: true,
            showColumns: true,
            sortName: 'status',
            sortOrder: 'asc',
            data: [],
            classes: "table-bordered  table-hover table-striped table",
            minimumCountColumns: 2,
            pagination: true,
            idField: 'id',
            pageSize: 20,
            resizable: true,
            resizeMode: 'overflow',
            sidePagination: 'client',
            pageList: [20, 40, 80, 200],
            responseHandler: "responseHandler",
            formatLoadingMessage: function () {
                return "<img class='loading-img' src='/static/img/loading_100.gif' /> " + ta4['loading'];
            },
            formatNoMatches: function () {
                return '<i class="fa status-alert fa-exclamation-triangle" aria-hidden="true"></i> ' + ta4['nodata'];
            }
        });
    };

    $scope.generatePathLink = function (nclass, v) {
        var result = '';
        var priorityStr = '';
        var sslStr = '';
        var vsIdStr = '';
        var slbIdStr = '';

        if (v.path || v.name) {
            var pathItems = [];
            if (v.path) {
                pathItems.push(v.path);
            }
            if (v.name) {
                pathItems.push(v.name);
            }
            var tempStr = '<div style="width: 50%;" class="path-item-cell ' + nclass;
            if (pathItems.length > 1) {
                tempStr += ' path-multi-item-container';
            }
            tempStr += '">';
            if (v.path) {
                tempStr += '<div title="' + v.path + '">' + ellipseString(v.path, 25) + '</div>';
            }
            if (v.name) {
                tempStr += '<div title="' + v.name + '">' + ellipseString(v.name, 25) + '</div>';
            }
            tempStr += '</div>';

            priorityStr = '&nbsp;<span class="">' + v.priority + '</span>';
            sslStr = '&nbsp;<span class="">' + (v.ssl ? 'https' : 'http') + '</span>';
            vsIdStr = '&nbsp;<a class="" target="_blank" href="' + '/portal/vs#?env=' + G.env + '&vsId=' + v.vsId + '" title="' + v.domain + '">' + v.vsId + '</a>';
            slbIdStr = $scope.generateSlbIds(v.slbId);

            tempStr +=
                '<div style="width: 50%;" class="path-item-cell path-item-list">' +
                '<div class="col-md-3 path-item-list">' + priorityStr + '</div>' +
                '<div class="col-md-3 path-item-list">' + sslStr + '</div>' +
                '<div class="col-md-3 path-item-list">' + vsIdStr + '</div>' +
                '<div class="col-md-3 path-item-list">' + slbIdStr + '</div>' +
                '</div>';
            result = result + '<div class="row" style="margin:0;display:table;width:100%;">' + tempStr + '</div>';
        }
        result = '<div class="row" style="margin:0">' + result + '</div>';
        return result;
    }
    $scope.generateSlbIds = function (slbIds) {
        var slbIdStr = '(';
        $.each(slbIds, function (i, slbId) {
            if (i == slbIds.length - 1) {
                slbIdStr += '<a class="" target="_blank" href="' + '/portal/slb#?env=' + G.env + '&slbId=' + slbId + '">' + slbId + '</a>';
            } else {
                slbIdStr += '<a class="" target="_blank" href="' + '/portal/slb#?env=' + G.env + '&slbId=' + slbId + '">' + slbId + '</a>,';
            }
        });
        slbIdStr += ')';
        return slbIdStr;
    }
    $scope.reloadTable = function () {
        var p = A.canDo("Vs", "UPDATE", $scope.query.vsId) && $scope.query.showextended;
        if (!p) {
            $('#vs-domains-table').bootstrapTable('hideColumn', 'Operation');
        } else {
            $('#vs-domains-table').bootstrapTable('showColumn', 'Operation');
        }
        $('#vs-domains-table').bootstrapTable("load", $scope.view.extendedvs['domains'] ? $scope.view.extendedvs['domains'] : []);

        var p2 = A.canDo("Group", "UPDATE", '*') && $scope.query.showextended;
        if (!p2) {
            $('#vs-groups-table').bootstrapTable('hideColumn', 'Operation');
        } else {
            $('#vs-groups-table').bootstrapTable('showColumn', 'Operation');
        }
        $('#vs-groups-table').bootstrapTable("load", $scope.allGroups);

        $('#vs-policy-table').bootstrapTable("load", $scope.traffics);

        // $('.vs-basic-info').hideLoading();
        $('#vs-domains-table').bootstrapTable('hideLoading');
        $('#vs-groups-table').bootstrapTable('hideLoading');
        $('#vs-policy-table').bootstrapTable('hideLoading');
    };

    $scope.reloadCertificates = function () {
        var certificates = $scope.certificates;
        var vsId = $scope.query.vsId;

        var certArray = [];

        _.mapObject(certificates[vsId], function (v, k, item) {
            certArray.push({
                cid: v,
                status: k
            });
        });
        $('#certificates-table').bootstrapTable('load', certArray);

        var p = A.canDo("Vs", "UPDATE", $scope.query.vsId) && $scope.query.showextended;
        if (!p) {
            $('#certificates-table').bootstrapTable('hideColumn', 'Operation');
        } else {
            $('#certificates-table').bootstrapTable('showColumn', 'Operation');
        }

        $('#certificates-table').bootstrapTable('hideLoading');
    };

    $scope.disableUploadCert = function () {
        var certificates = $scope.certificates;
        var vsId = $scope.query.vsId;

        if (!certificates || !vsId) return;

        var hasCanary = certificates[vsId] && certificates[vsId]['CANARY'];

        var canary = hasCanary != undefined;

        return canary;
    };

    $scope.addNewCertificate = function () {
        $scope.canaryIps = [];

        $scope.certdog.certName = '';

        $scope.certdog.uploadCanarySuccess = false;
        $scope.certdog.canarySuccess = false;
        $scope.certdog.allUploadCanarySuccess = false;
        $scope.certdog.canaryAllSuccess = false;

        $scope.query.canaryPassed = false;
        $scope.query.allCanaryPassed = false;
        $('#uploadCertDialog').modal('show');
    };

    // status watch dog for certificate
    $scope.canaryIps = {};
    $scope.certificates = {};
    $scope.certificateRange = {
        max: 25,
        disabled: false
    };

    $scope.certdog = {
        uploadCanarySuccess: '',
        allUploadCanarySuccess: '',
        canarySuccess: '',
        canaryAllSuccess: '',

        certName: ''
    };

    // query.canaryPassed 灰度通过允许批量
    // query.allCanaryPassed  全部灰度完成，全部生效允许
    window.certificateEvent = {
        'click .canary-test': function (e, value, row, index) {
            var promises = getCanaryIps();
            promises.success(function (response, code) {
                if (!response.code) {
                    var machines = response;
                    $scope.canaryIps = machines;

                    $scope.certdog.certName = row.cid;

                    $scope.certdog.uploadCanarySuccess = true;
                    $scope.certdog.canarySuccess = true;
                    $scope.certdog.allUploadCanarySuccess = false;
                    $scope.certdog.canaryAllSuccess = false;

                    $scope.query.canaryPassed = false;
                    $scope.query.allCanaryPassed = false;
                    $scope.query.loadCertPassed = false;

                    $('#uploadCertDialog').modal('show');
                }
            });
        },
        'click .canary-revert': function (e, value, row, index) {
            $('#revertCertDialog').modal('show');
        },
        'click .activate-slb-bt': function (e, value, row, index) {
            // todo: one vs might has more than 1 slb, so here not compare the versions, just activate
            $('#confirmActivateSLB').modal('show');
        }
    };

    $('#uploadCertDialog').on('hidden.bs.modal', function () {
        H.setData({timeStamp: new Date().getTime()});
    });

    $scope.revertCertChanges = function () {
        var vsId = $scope.query.vsId;
        revertCertificateFunc(vsId);
    };
    // First time upload certificate
    $scope.uploadCertificate = function () {
        var percentage = $scope.certificateRange.max;
        var cid = $scope.certdog.certName.trim();
        var vsId = $scope.query.vsId;

        loadCertificateFunc(cid, vsId, function () {
            $('#uploadFailSpan').html('');
            uploadCertificateFunc(percentage, function () {
                $scope.certdog.uploadCanarySuccess = true;
                $('#uploadFailSpan').html('');
                $scope.canaryCert();
            }, function (message) {
                $('#uploadFailSpan').html('<span class="status-red">灰度CID失败: ' + message + '</span>');
            });
        }, function (message) {
            $('#uploadFailSpan').html('<span class="status-red">生效灰度证书失败! 错误: ' + message + '</span>');
        });
    };

    // First time canary range of machines
    $scope.canaryCert = function () {
        activateSlb(function () {
            $scope.certdog.canarySuccess = true;
            $('#uploadFailSpan').html('');
        }, function (code) {
            $scope.certdog.canarySuccess = false;
            $('#uploadFailSpan').html('<span class="status-red">生效灰度证书失败! 错误码: ' + code + '</span>');
        })
    };

    // Upload certificate to all machines
    $scope.batchCanary = function () {
        uploadCertificateFunc(100, function () {
            $('#uploadFailSpan').html('');
            $scope.certdog.allUploadCanarySuccess = true;
            $scope.canaryAllCert();
        }, function (message) {
            $('#uploadFailSpan').html('批量更新证书失败，错误信息:' + message);
        }, true);
    };

    // Canary all of the machines
    $scope.canaryAllCert = function () {
        activateSlb(function () {
            $('#uploadFailSpan').html('');
            $scope.certdog.canaryAllSuccess = true;
        }, function (code) {
            $scope.certdog.canaryAllSuccess = false;
            $('#uploadFailSpan').html('<span class="status-red">批量生效证书失败! 错误码: ' + code + '</span>');
        })
    };

    $scope.activateSlb = function () {
        return activateSlb(function () {
            $('#uploadFailSpan').html('<span class="status-green">激活SLB成功</span>');
        }, function (message) {
            $('#uploadFailSpan').html('批量更新证书失败，错误信息:' + message);
        });
    };

    $scope.getMachineStatus = function () {
        var uploadCanarySuccess = $scope.certdog.uploadCanarySuccess;
        var canarySuccess = $scope.certdog.canarySuccess;

        var uploadAllCanarySuccess = $scope.certdog.allUploadCanarySuccess;
        var canaryAllSuccess = $scope.certdog.canaryAllSuccess;

        var text = '';

        if (uploadCanarySuccess) {
            text = '已更新证书';
        }
        if (canarySuccess) {
            text = '已安装新证书';
        }
        if (uploadAllCanarySuccess) {
            text = '已更新证书';
        }
        if (canaryAllSuccess) {
            text = '已安装新证书';
        }
        return text;
    };

    $scope.activateSLBClick = function () {
        var slbIds = _.pluck($scope.view.extendedvs['slb-ids'], 'id');
        var url = G.baseUrl + '/api/activate/slb';

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
        $('#confirmActivateSLB').modal('hide');

        $('#operationConfrimModel').modal("show").find(".modal-title").html("激活VS");
        $('#operationConfrimModel').modal("show").find(".modal-body").html("正在激活.. <img src='/static/img/spinner.gif' />");

        $scope.processRequest(request, $('#operationConfrimModel'), "激活SLB", "激活成功");
    };

    function getCanaryIps() {
        var certificates = $scope.certificates;
        var vsId = $scope.query.vsId;
        var certId = certificates[vsId]['canary'];

        var param = {
            vsId: vsId,
            cid: certId
        };
        var request = {
            method: 'GET',
            url: G.baseUrl + '/api/cert/vs/canary/ips',
            params: param
        };
        return $http(request).success(function (response, code) {
            return response;
        });
    };

    function getAllIps(loading) {
        var slbs = $scope.model.slbsObjects;
        var vsSlbIds = $scope.view.extendedvs['slb-ids'];

        var slbServers = [];

        $.each(vsSlbIds, function (i, a) {
            var id = loading ? a : a.id;
            slbServers = slbServers.concat(_.pluck(slbs[id]['slb-servers'], 'ip'));
        });

        return slbServers;
    };

    function uploadCertificateFunc(percentage, nextFunc, failFunc, batch) {
        percentage = percentage / 100;

        // call api to upload cid
        var vsId = $scope.query.vsId;
        var certId = $scope.certdog.certName;

        var param = {};
        var url = G.baseUrl + '/api/cert/canary';
        if (batch) {
            url = G.baseUrl + '/api/cert/activate';
            param = {
                vsId: vsId,
                percent: percentage
            }
        } else {
            url = G.baseUrl + '/api/cert/canary';
            param = {
                vsId: vsId,
                cid: certId,
                percent: percentage
            };
        }
        var request = {
            method: 'GET',
            url: url,
            params: param
        };
        $http(request).success(function (response, status) {
            if (status == 200) {
                if (nextFunc) {
                    nextFunc();
                }
                if (batch) {
                    $scope.canaryIps = getAllIps();
                } else {
                    $scope.canaryIps = response;
                }
            } else {
                if (failFunc) {
                    failFunc(response.message);
                }
            }
        });
    }

    function loadCertificateFunc(cid, vsId, nextFunc, failFunc) {
        var param = {
            vsId: vsId,
            cid: cid
        };
        var url = G.baseUrl + '/api/cert/load';

        var request = {
            method: 'GET',
            url: url,
            params: param
        };
        $http(request).success(function (response, status) {
                if (status == 200) {
                    if (nextFunc) nextFunc();
                } else {
                    if (failFunc) failFunc(response.message);
                }
            }
        );
    }

    function revertCertificateFunc(vsId) {
        $('#revertCertDialog').modal('hide');
        var param = {
            vsId: vsId
        };
        var url = G.baseUrl + '/api/cert/rollback';
        var request = {
            method: 'GET',
            url: url,
            params: param
        };
        $http(request).success(function (response, status) {
                if (status == 200) {
                    $('#operationConfrimModel').modal('show').find('.modal-body').html('<span class="success-important">' +
                        '回退证书成功' +
                        '</span>');
                    startTimer($('#operationConfrimModel'));
                } else {
                    $('#operationConfrimModel').modal('show').find('.modal-body').html('<span class="warning-important">' +
                        '回退证书失败，错误信息.' + response.message + ',请确认线上已经存在可用证书' +
                        '</span>');
                }
            }
        );
    }

    function activateSlb(next, fail) {
        var slbIds = _.pluck($scope.view.extendedvs['slb-ids'], 'id');
        var url = G.baseUrl + '/api/activate/slb';

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

    window.DiffVersionEvent = {
        'click .diff-version-bt': function (e, value, row) {
            getVsDataByVersion(row);
        }
    };
    window.ViewEventDetailEvent = {
        'click .statusclass': function (e, value, row) {
            $('.operation-detail-div').html('');
            var statusText = '';
            var statusCss = '';
            var type = row.type;
            var target = row['target-id'];
            var operation = row['operation'];
            var user = row['user-name'];
            var success = row['success'];
            var userip = row['client-ip'];
            var exception = '-';
            if (row['err-msg']) {
                exception = row['err-msg'];
            }
            if (success == true) {
                statusCss = 'status-green';
                statusText = '成功';
            } else {
                statusCss = 'status-red';
                statusText = '失败';
            }

            var op_url = '-';
            var op_param = '-';

            var data = row['data'];
            if (data) {
                if (IsJsonString(data)) {
                    var t = JSON.parse(data);
                    op_url = t.uri;
                    op_param = t.query;
                }
            }
            var dateTime = row['date-time'];

            var str = '<div class="" style="margin: 0 auto">' +
                '<table class="table ng-scope ng-table operation-detail-table">' +
                ' <tr><td><b>状态:</b></td>' +
                ' <td class="' + statusCss + '" style="font-weight: bold">' + statusText + '</td>' +
                ' </tr>' +
                ' <tr><td><b>类型:</b></td>' +
                ' <td>' + type + '</td>' +
                ' </tr>' +
                ' <tr><td><b>ID:</b></td>' +
                ' <td>' + target + '</td>' +
                ' </tr>' +
                ' <tr><td><b>操作:</b></td>' +
                ' <td>' + operation + '</td>' +
                ' </tr>' +
                ' <tr><td><b>操作URL:</b></td>' +
                ' <td>' + op_url + '</td>' +
                ' </tr>' +
                ' <tr><td><b>操作参数:</b></td>' +
                ' <td>' + op_param + '</td>' +
                ' </tr>' +
                ' <tr><td><b>操作人:</b></td>' +
                ' <td>' + user + '</td>' +
                ' </tr>' +
                ' <tr><td><b>操作人IP:</b></td>' +
                ' <td>' + userip + '</td>' +
                ' </tr>' +
                ' <tr><td><b>操作时间:</b></td>' +
                ' <td>' + dateTime + '</td>' +
                ' </tr>' +
                ' <tr><td><b>Exception:</b></td>' +
                ' <td><div style="width: 60%">' + exception + '</div></td>' +
                ' </tr>' +
                '</table>' +
                '</div>';
            $('.operation-detail-div').append(str);
            $('#output-div').modal('show');
        }
    };

    function getVsDataByVersion(row) {
        var currentVersion = row['vs-version'];
        var id = row['target-id'];

        var c = currentVersion;
        var p = currentVersion - 1;

        if (row['operation'] == 'activate') {
            var gd = JSON.parse(row['data']);
            var gd_datas = gd['vs-datas'];
            var gd_sort = _.sortBy(gd_datas, 'version');
            p = gd_sort[0].version;
        }
        var param0 = {
            vsId: id,
            version: c
        };
        var param1 = {
            vsId: id,
            version: p
        };

        var request0 = {
            method: 'GET',
            url: G.baseUrl + '/api/archive/vs',
            params: param0
        };
        var request1 = {
            method: 'GET',
            url: G.baseUrl + '/api/archive/vs',
            params: param1
        };

        var compareData = {};
        $q.all(
            [
                $http(request0).success(
                    function (resp) {
                        compareData.current = resp;
                    }
                ),
                $http(request1).success(
                    function (resp) {
                        compareData.previous = resp;
                    }
                )
            ]
        ).then(
            function () {
                var baseText = JSON.stringify(U.sortObjectFileds(compareData.previous), null, "\t");
                var NewText = JSON.stringify(U.sortObjectFileds(compareData.current), null, "\t");
                var target = document.getElementById('slbfileDiffForm');
                var ptext = "变更前版本" + p;
                var ctext = "变更后版本" + c;
                if (row['operation'] == 'activate') {
                    ptext = '线上版本' + p;
                    ctext = "线下版本" + c;
                }
                diffTwoVS(target, baseText, NewText, ptext, ctext);
                $('#slbdiffVSDiv').modal('show');
            }
        );
    };
    $scope.loadData = function (hashData) {
        var queryString = G.baseUrl + "/api/groups?type=EXTENDED&vsId=" + $scope.query.vsId;
        var vqueryString = G.baseUrl + "/api/vgroups?type=EXTENDED&vsId=" + $scope.query.vsId;
        var trafficsqueryString = G.baseUrl + "/api/policies?type=EXTENDED&vsId=" + $scope.query.vsId;

        $scope.vgroups = [];
        $scope.agroups = [];
        $scope.traffics = [];

        var vsId = hashData.vsId;
        var param0 = {
            mode: 'online',
            vsId: vsId
        };
        var param1 = {
            type: 'extended',
            vsId: vsId
        };
        var param2 = {
            vsId: vsId
        };
        var param3 = {
            type: 'info'
        };
        var param4 = {
            targetId: vsId,
            type: 'VS',
            count: 3
        };

        var vsOnlineRequest = {
            method: 'GET',
            url: G.baseUrl + '/api/vs',
            params: param0
        };
        var vsOfflineRequest = {
            method: 'GET',
            url: G.baseUrl + '/api/vs',
            params: param2
        };
        var vsExtendedRequest = {
            method: 'GET',
            url: G.baseUrl + '/api/vs',
            params: param1
        };

        var currentUserRequest = {
            method: 'GET',
            url: '/api/auth/current/user'
        };
        var slbsRequest = {
            method: 'GET',
            url: G.baseUrl + '/api/slbs?type=extended'
        };
        var vsCurrentLogs = {
            method: 'GET',
            url: G.baseUrl + '/api/logs',
            params: param4
        };
        var vsesStasticsRequest = {
            method: 'GET',
            url: G.baseUrl + '/api/statistics/vses'
        };
        var vsesRequest = {
            method: 'GET',
            url: G.baseUrl + '/api/vses'
        };
        var appsRequest = {
            method: 'GET',
            url: G.baseUrl + '/api/apps'
        };
        var groupsRequest = {
            method: 'GET',
            url: G.baseUrl + '/api/groups?type=extended&groupType=all'
        };
        var policyRequest = {
            method: 'GET',
            url: trafficsqueryString
        };
        var certificateParam = {
            vsId: $scope.query.vsId
        };
        var certificateRequest = {
            method: 'GET',
            url: G.baseUrl + '/api/cert/query',
            params: certificateParam
        };

        var requests = [
            $http(vsesStasticsRequest).success(
                function (response) {
                    $scope.model.vsstastics = _.find(response['vs-metas'], function (item) {
                        return item['vs-id'] == vsId;
                    });
                    $scope.view.vsstastics = $.extend(true, {}, $scope.model.vsstastics);

                    if ($scope.view.vsstastics) {
                        $scope.view.vsstastics['qps'] = $scope.view.vsstastics['qps'] == undefined ? 0 : T.getText($scope.view.vsstastics['qps']);
                    }
                }
            ),
            $http(vsesRequest).success(
                function (response) {
                    $scope.model.vses = _.indexBy(response['virtual-servers'], 'id');
                }
            ), $http(groupsRequest).success(
                function (response) {
                    $scope.model.groups = _.indexBy(response['groups'], 'id');
                }
            ), $http(appsRequest).success(
                function (response) {
                    $scope.model.apps = _.indexBy(response['apps'], 'app-id');
                }
            ),
            $http(vsCurrentLogs).success(
                function (response) {
                    if (response['operation-log-datas']) {
                        $scope.model.currentlogs = response['operation-log-datas'];
                    }
                }
            ),
            $http.get(vqueryString).success(
                function (res) {
                    if (res.groups) {
                        $scope.vgroups = res.groups;
                    } else {
                        $scope.vgroups = [];
                    }
                }
            ),
            $http.get(queryString).success(
                function (res) {
                    if (res.groups) {
                        $scope.groups = res.groups;
                    } else {
                        $scope.groups = [];
                    }
                }
            ),
            $http.get(G.baseUrl + "/api/status/groups?type=EXTENDED&vsId=" + $scope.query.vsId).success(
                function (response) {
                    $scope.groupStatusMap = {};
                    $.each(response['group-statuses'], function (i, val) {
                        $scope.groupStatusMap[val['group-id']] = val;
                    });
                }
            ),
            $http.get(G.baseUrl + "/api/apps").success(
                function (response) {
                    $scope.appInfoMap = {};
                    $.each(response['apps'], function (i, val) {
                        $scope.appInfoMap[val['app-id']] = val;
                    });
                }
            ),
            $http.get(G.baseUrl + "/api/statistics/groups").success(
                function (response) {
                    $scope.groupStatisticsMap = {};
                    $.each(response["group-metas"], function (index, meta) {
                        $scope.groupStatisticsMap[meta['group-id']] = meta;
                    });
                }
            ),

            $http(vsOnlineRequest).success(
                function (response) {
                    if (response.code != undefined) {
                        $scope.model.onlinevs = {"data": "No Online Data", "version": "Unknow"};
                    }
                    else {
                        $scope.model.onlinevs = {"data": response};
                    }
                    $scope.view.onlinevs = $.extend(true, {}, response);
                }
            ),
            $http(vsOfflineRequest).success(
                function (response) {
                    $scope.model.offlinevs = response;
                    $scope.view.offlinevs = $.extend(true, {}, response);
                }
            ),
            $http(vsExtendedRequest).success(
                function (response) {
                    $scope.model.extendedvs = response;
                    $scope.view.extendedvs = $.extend(true, {}, response);
                    $scope.view.vshistoryversions = _.range(1, $scope.model.extendedvs.version + 1);
                }
            ),
            $http(currentUserRequest).success(
                function (response) {
                    $scope.query.user = response.name;
                }
            ),
            $http(slbsRequest).success(
                function (response) {
                    var slbs = response['slbs'];
                    $scope.model.slbs = _.invert(_.object(_.map(slbs, _.values)));
                    $scope.model.slbsObjects = _.indexBy(slbs, 'id');
                }
            ),
            $http(policyRequest).success(
                function (response) {
                    if (response['traffic-policies']) {
                        $scope.traffics = response['traffic-policies'];
                    } else {
                        $scope.traffics = [];
                    }
                }
            )
        ];
        // if this
        $q.all(requests).then(
            function () {

                if ($scope.model.offlinevs.code) {
                    exceptionNotify("出错了!!", "加载Virtual Server ID=" + hashData.vsId + "失败了， 失败原因" + $scope.model.offlinevs.message, null);
                    return;
                } else {
                    setTimeout(
                        function () {
                            $('.alert-danger').remove();
                        },
                        1000
                    );
                }
                var slbs = getAllIps(true);
                $scope.query.allSlbs = slbs;
                $.each($scope.model.currentlogs, function (i, item) {
                    var data = item.data;
                    var isJson = IsJsonString(data);

                    var version = '-';

                    if (isJson) {
                        data = JSON.parse(data);
                        if (data['vs-datas'] && data['vs-datas'].length > 0) {
                            version = data['vs-datas'][0].version;
                        }
                    }
                    item['vs-version'] = version;
                });

                var slbIds = [];
                $.each($scope.view.extendedvs['slb-ids'], function (i, item) {
                    slbIds.push({
                        id: item,
                        name: $scope.model.slbs[item]
                    });
                });
                $scope.view.extendedvs['slb-ids'] = slbIds;

                $scope.getVsVipText();

                var v = _.find($scope.view.extendedvs.properties, function (r) {
                    return r.name == 'idc';
                });
                if (v) $scope.view.extendedvs.idc = v.value;

                var isssl = $scope.view.extendedvs.ssl;

                // status and healthy
                var groupStatusCountedItem = _.countBy($scope.groups, function (item) {
                    var v = _.find(item.properties, function (r) {
                        return r.name == 'status';
                    });
                    if (v) {
                        return v.value.toLowerCase();
                    } else return '';
                });
                var groupHealthyCountedItem = _.countBy($scope.groups, function (item) {
                    var v = _.find(item.properties, function (r) {
                        return r.name == 'healthy';
                    });
                    if (v) {
                        return v.value.toLowerCase();
                    } else return '';
                });

                $scope.view.vsstastics['status'] = {
                    'activated': groupStatusCountedItem['activated'] | 0,
                    'tobeactivated': groupStatusCountedItem['tobeactivated'] | 0,
                    'deactivated': groupStatusCountedItem['deactivated'] | 0
                };
                $scope.view.vsstastics['healthy'] = {
                    'healthy': groupHealthyCountedItem['healthy'] | 0,
                    'unhealthy': groupHealthyCountedItem['unhealthy'] | 0,
                    'broken': groupHealthyCountedItem['broken'] | 0
                };


                $scope.groupsCount += $scope.groups.length;

                // get v-groups
                $.each($scope.groups, function (i, group) {
                    $scope.getGroupMetaData(group);
                    $scope.getAppInfoMetaData(group);
                    $scope.getGroupQPSMetaData(group);

                    group.paths = [];
                    group.pathOrder = 0;
                    var c = 0;
                    $.each(group['group-virtual-servers'], function (i, gVs) {
                        var o = {
                            name: gVs.name,
                            path: gVs.path,
                            priority: gVs.priority,
                            vsId: gVs['virtual-server'].id,
                            slbId: gVs['virtual-server']["slb-ids"],
                            ssl: gVs['virtual-server'].ssl,
                            domain: gVs['virtual-server'].domains && gVs['virtual-server'].domains[0] != undefined ? gVs['virtual-server'].domains[0].name : ''
                        };
                        group.paths.push(o);

                        //Set path order
                        if (c == 0) {
                            if (o.priority >= 0) {
                                group.pathOrder = o.priority * 1000000 + group.id;
                            } else {
                                group.pathOrder = o.priority * 1000000 - group.id;
                            }
                        }
                        c++;
                    })
                });

                $scope.groupsCount += ($scope.vgroups == undefined ? 0 : $scope.vgroups.length);
                // get v-groups
                $.each($scope.vgroups, function (i, group) {
                    $scope.getGroupMetaData(group);
                    $scope.getAppInfoMetaData(group);
                    group.isvgroup = true;
                    group.groupstatus = 1;
                    group.paths = [];
                    group.pathOrder = 0;
                    var c = 0;
                    $.each(group['group-virtual-servers'], function (i, gVs) {
                        var o = {
                            name: gVs.name,
                            path: gVs.path,
                            priority: gVs.priority,
                            vsId: gVs['virtual-server'].id,
                            slbId: gVs['virtual-server']["slb-ids"],
                            ssl: gVs['virtual-server'].ssl,
                            domain: gVs['virtual-server'].domains && gVs['virtual-server'].domains[0] != undefined ? gVs['virtual-server'].domains[0].name : ''
                        };
                        group.paths.push(o);

                        //Set path order
                        if (c == 0) {
                            if (o.priority >= 0) {
                                group.pathOrder = o.priority * 1000000 + group.id;
                            } else {
                                group.pathOrder = o.priority * 1000000 - group.id;
                            }
                        }
                        c++;
                    })
                });
                $scope.allGroups = $scope.groups.concat($scope.vgroups);

                $scope.aCount = _.reduce(_.values($scope.appsCollection), function (a, b) {
                    return a + b;
                });
                if (!$scope.aCount) $scope.aCount = 0;

                var vses_temp = {}, groups_temp = {}, apps_temp = {}, members_temp = {}, groups_server = {};
                $.each($scope.traffics, function (index, current) {
                    // Traffic status fields
                    current.paths = [];
                    current.status = '';

                    var statusProperty = _.find(current.properties, function (item) {
                        return item.name == 'status';
                    });
                    if (statusProperty) {
                        current.status = statusProperty.value;
                    }
                    var targetProperty = _.find(current.properties, function (item) {
                        return item.name == 'target';
                    });
                    if (targetProperty) {
                        current.target = targetProperty.value;
                    }
                    var idcProperty = _.find(current.properties, function (item) {
                        return item.name == 'idc';
                    });
                    if (idcProperty) {
                        current.idc = idcProperty.value;
                    }
                    // Traffic paths
                    var vs = current['policy-virtual-servers'];
                    $.each(vs, function (c_index, c_item) {
                        var vsId = c_item['virtual-server'].id;
                        var c = {
                            path: c_item.path,
                            priority: c_item.priority,
                            vsId: vsId,
                            ssl: '',
                            slbId: []
                        };
                        if ($scope.model.vses[vsId]) {
                            c.ssl = $scope.model.vses[vsId].ssl ? 'https' : 'http';
                            c.slbId = $scope.model.vses[vsId]['slb-ids'] ? $scope.model.vses[vsId]['slb-ids'] : [];
                        }
                        current.paths.push(c);
                        vses_temp[vsId] = vsId;
                    });

                    // traffic groups and apps
                    var groups = current.controls;
                    $.each(groups, function (index, item) {
                        var id = item.group.id;
                        groups_temp[id] = id;

                        item.group.type = $scope.model.groups[id].type;
                        item.groupName = $scope.model.groups[id].name;
                        var idc_property = _.find($scope.model.groups[id].properties, function (c) {
                            return c.name == 'idc';
                        });
                        var status_property = _.find($scope.model.groups[id].properties, function (c) {
                            return c.name == 'status';
                        });
                        if (idc_property) {
                            item.idc = idc_property.value;
                        }
                        if (status_property) {
                            item.status = getGroupStatus(status_property.value);
                        }
                        var appId = $scope.model.groups[id]['app-id'];
                        apps_temp[appId] = appId;
                        var appName = "-";
                        var sbu = "-";
                        var owner = "-";
                        if ($scope.model.apps[appId]) {
                            appName = $scope.model.apps[appId]['chinese-name'];
                            sbu = $scope.model.apps[appId]['sbu'];
                            owner = $scope.model.apps[appId]['owner'];
                        }
                        item.app = appId + '(' + appName + ')';
                        item.bu = sbu;
                        item.owner = owner;

                        members_temp += $scope.model.groups[id]['group-servers'] ? $scope.model.groups[id]['group-servers'].length : 0;
                        $.each($scope.model.groups[id]['group-servers'], function (i, t) {
                            groups_server[t.ip + '/' + t['host-name']] = t;
                        });
                    });

                    current.controls1 = current.controls;
                    current.controls2 = current.controls;
                });

                // if this request is ssl
                if (isssl) {
                    // send request to load certificates
                    $http(certificateRequest).success(function (response, status) {

                        if (status == 200) {
                            $scope.certificates = response;
                            $scope.reloadCertificates();
                        }

                        $scope.reloadCertificates();
                    });
                }
                $scope.reloadTable();
            }
        );
    };

    function getGroupStatus(val) {
        switch (val) {
            case 'activated':
                return '已激活';
            case 'toBeActivated':
                return '有变更';
            case 'deactivated':
                return '未激活';
            default:
                return val;
        }
    }

    // Common Methods
    $scope.getVsStatusProperty = function () {
        var grpStatus = undefined;
        if (!$scope.model.extendedvs.properties) return grpStatus;

        var p = _.find($scope.model.extendedvs.properties, function (item) {
            return item.name.toLowerCase().trim() == "status";
        });
        if (p) {
            grpStatus = p.value.toLowerCase().trim();
        }
        return grpStatus;
    };
    $scope.processRequest = function (request, confirmDialog, operationText, tooltipText) {
        confirmDialog.find(".modal-title").html(operationText);
        var msg = "";
        $http(request).success(
            function (res, code) {
                var errText = '';
                if (code != 200) {
                    msg = res.message;
                    if (!msg) {
                        msg = code;
                    }
                    errText = "<span class='fa fa-times'></span><span style='padding-left: 10px'>" + operationText + "失败</span>";
                    confirmDialog.modal('show').find(".modal-title").html(errText);
                    confirmDialog.modal('show').find(".modal-body").html("失败原因:" + msg);
                    if (msg.indexOf("overlap") > 0) {
                        // need force update
                        $scope.showForceUpdate = true;
                    }
                }
                else {
                    var successText = "<span class='fa fa-check'></span><span style='padding-left: 10px'>" + operationText + "成功</span>";
                    confirmDialog.modal('show').find(".modal-title").html(successText);
                    if (tooltipText) confirmDialog.modal('show').find(".modal-body").html(tooltipText);
                    startTimer(confirmDialog);
                }
            }
        ).error(function (reject) {
            msg = reject.message;
            var errText = "<span class='fa fa-times'></span><span style='padding-left: 10px'>" + operationText + "失败</span>";
            confirmDialog.modal('show').find(".modal-title").html(errText);
            confirmDialog.modal('show').find(".modal-body").html("失败原因:" + msg);
        });
    };
    $('.closeProgressWindowBt').click(
        function (e) {
            var hashData = {};
            hashData.timeStamp = new Date().getTime();
            H.setData(hashData);
        }
    );

    function startTimer(dialog) {
        if (dialog.attr('id') == 'deleteGroupConfirmModel') {
            setTimeout(function () {
                dialog.find('.closeProgressWindowBt2').click();
            }, 2000);
        }
        else {
            setTimeout(function () {
                dialog.find('.closeProgressWindowBt').click();
            }, 2000);
        }
    }

    function reviewData(id) {
        var result = true;
        $.each(id.find('[data-validator-type="validation"]'), function (i, element) {
            if (!$(element).bootstrapValidation('validate'))
                result = false;
        });
        return result;
    };

    function diffTwoVses(targetDiv, baseText, newText, baseVersion, newVersion) {
        var base = difflib.stringAsLines(baseText);
        var newtxt = difflib.stringAsLines(newText);
        var sm = new difflib.SequenceMatcher(base, newtxt);
        var opcodes = sm.get_opcodes();

        targetDiv.innerHTML = "";

        targetDiv.appendChild(diffview.buildView({
            baseTextLines: base,
            newTextLines: newtxt,
            opcodes: opcodes,
            baseTextName: baseVersion,
            newTextName: newVersion,
            viewType: 0
        }));
    }

    $scope.getOperationText = function (x) {
        var resource = $scope.resource;
        if (!resource || _.keys(resource).length == 0) return;
        return resource['log']['log_operationLogApp_opmapping'][x];
    };
    var vsApp;
    var operationmap;
    $scope.hashChanged = function (hashData) {
        $scope.resource = H.resource;
        $scope.resource = H.resource;
        resource = H.resource;
        operationmap = resource['vs']['vs_vsInfoApp_operation'];

        $scope.tobeOnline = 0;
        $scope.online = 0;
        $scope.offline = 0;
        $scope.mCount = 0;
        $scope.sCount = 0;
        $scope.groupsCount = 0;
        $scope.aCount = 0;

        $scope.appsCollection = {};
        $scope.members = {};

        $scope.initTable();

        // init the wreches
        $scope.tableOps.vsDomain.showOperations = false;
        $scope.tableOps.vsGroups.showOperations = false;
        $scope.tableOps.vsCerts.showOperations = false;

        $scope.tableOps.vsDomain.showMoreColumns = false;
        $scope.tableOps.vsGroups.showMoreColumns = false;

        $('#vs-groups-table').bootstrapTable('removeAll');
        $('#vs-domains-table').bootstrapTable('removeAll');
        // $('.vs-basic-info').showLoading();
        $('#vs-domains-table').bootstrapTable('showLoading');
        $('#vs-groups-table').bootstrapTable('showLoading');
        $('#vs-policy-table').bootstrapTable('showLoading');
        $('#certificates-table').bootstrapTable('showLoading');

        $scope.env = 'pro';
        if (hashData.env) {
            $scope.env = hashData.env;
        }
        vsApp = new VsApp($scope.env);

        if (hashData.vsId) {
            $scope.query.vsId = hashData.vsId;
            $scope.loadData(hashData);
        }
    };
    H.addListener("vsInfoApp", $scope, $scope.hashChanged);

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
                align: 'center'
            },
            offset: {
                x: 0,
                y: 0
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

    String.prototype.endWith = function (s) {
        if (s == null || s == "" || this.length == 0 || s.length > this.length)
            return false;
        if (this.substring(this.length - s.length) == s)
            return true;
        else
            return false;
        return true;
    }
    String.prototype.startWith = function (s) {
        if (s == null || s == "" || this.length == 0 || s.length > this.length)
            return false;
        if (this.substr(0, s.length) == s)
            return true;
        else
            return false;
        return true;
    }

    function IsJsonString(str) {
        try {
            JSON.parse(str);
        } catch (e) {
            return false;
        }
        return true;
    }

    function diffTwoVS(targetDiv, baseText, newText, baseVersion, newVersion) {
        var base = difflib.stringAsLines(baseText);
        var newtxt = difflib.stringAsLines(newText);
        var sm = new difflib.SequenceMatcher(base, newtxt);
        var opcodes = sm.get_opcodes();

        targetDiv.innerHTML = "";

        targetDiv.appendChild(diffview.buildView({
            baseTextLines: base,
            newTextLines: newtxt,
            opcodes: opcodes,
            baseTextName: baseVersion,
            newTextName: newVersion,
            viewType: 0
        }));
    }
});
angular.bootstrap(document.getElementById("vs-info-area"), ['vsInfoApp']);

var summaryInfoApp = angular.module('summaryInfoApp', ['http-auth-interceptor', 'angucomplete-alt']);
summaryInfoApp.controller('summaryController', function ($scope, $http, $q) {
    $scope.query = {};
    // Auto complete
    $scope.cacheRequestFn = function (str) {
        return {q: str, timestamp: new Date().getTime()};
    };
    $scope.remoteUrl = function () {
        return G.baseUrl + "/api/meta/vses";
    };
    $scope.selectTarget = function (t) {
        if (t) {
            var toId = t.originalObject.id;
            if ($scope.target.id != toId) {
                $scope.$broadcast('angucomplete-alt:clearInput', 'targetSelector');
                var pairs = {};
                pairs['vsId'] = toId;
                $scope.target.id = toId;
                H.setData(pairs);
                messageNotify("切换VS. ", "成功切换至VS： " + toId, null);
            }
        }
    };
    $scope.clickTarget = function () {
        $('#targetSelector_value').css('width', '250px');
    };
    $scope.setInputsDisplay = function () {
        $('#targetSelector_value').val($scope.query.vsId);
    };
    $scope.getAllTargets = function () {
        $http.get(G.baseUrl + "/api/vs?vsId=" + $scope.query.vsId + "&type=info").success(
            function (res) {
                $scope.target = {};
                $scope.target.name = $scope.query.vsId + "/" + res.name;
                $('#targetSelector_value').val($scope.target.name);
            }
        );
    };
    $scope.hashChanged = function (hashData) {
        $scope.resource = H.resource;
        if (hashData.env) {
            $scope.env = hashData.env;
        }
        if (hashData.vsId) {
            $scope.query.vsId = hashData.vsId;
            $scope.getAllTargets();
        }
    };
    H.addListener("summaryInfoApp", $scope, $scope.hashChanged);

    function messageNotify(title, message, url) {
        var notify = $.notify({
            icon: '',
            title: title,
            message: message,
            url: url,
            target: '_self'
        }, {
            type: 'success',
            allow_dismiss: true,
            newest_on_top: true,
            placement: {
                from: 'top',
                align: 'center'
            },
            offset: {
                x: 0,
                y: 0
            },
            animate: {
                enter: 'animated fadeInDown',
                exit: 'animated fadeOutUp'
            },
            delay: 1000,
            spacing: 5,
            z_index: 1031,
            mouse_over: 'pause'
        });
    }
});
angular.bootstrap(document.getElementById("summary-area"), ['summaryInfoApp']);

//InfoLinksComponent: info links
var headerInfoApp = angular.module('headerInfoApp', ["angucomplete-alt"]);
headerInfoApp.controller('headerInfoController', function ($scope, $http) {
    $scope.query = {};
    $scope.generateLink = function (a) {
        var link = '';
        switch (a) {
            case 'basic': {
                link = "/portal/vs#?env=" + G.env + "&vsId=" + $scope.query.vsId;
                break;
            }
            case 'rule': {
                link = "/portal/vs/rule#?env=" + G.env + "&vsId=" + $scope.query.vsId;
                break;
            }
            case 'log': {
                link = "/portal/vs/log#?env=" + G.env + "&vsId=" + $scope.query.vsId;
                break;
            }
            case 'traffic': {
                link = "/portal/vs/traffic#?env=" + G.env + "&vsId=" + $scope.query.vsId;
                break;
            }
            case 'conf': {
                link = "/portal/vs/conf#?env=" + G.env + "&vsId=" + $scope.query.vsId;
                break;
            }
            case 'intercept': {
                link = "/portal/vs/intercept#?env=" + G.env + "&vsId=" + $scope.query.vsId;
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
        if (hashData.vsId) {
            $scope.query.vsId = hashData.vsId;
        }
    };
    H.addListener("infoLinksApp", $scope, $scope.hashChanged);
});
angular.bootstrap(document.getElementById("header-area"), ['headerInfoApp']);