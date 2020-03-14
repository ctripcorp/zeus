//SLBInfoComponent: show a slb info
var slbInfoApp = angular.module('slbInfoApp', ['angucomplete-alt', 'ngSanitize', 'http-auth-interceptor']);
slbInfoApp.controller('slbInfoController', function ($scope, $http, $q) {
    //slbInfo data
    var slbApp;
    $scope.query = {
        slbId: '',
        user: {},
        showextended: ''
    };
    $scope.model = {
        currentlogs: [],
        slbs: {},
        offlineslb: {},
        extendedslb: {},
        onlineslb: {},
        slbvses: [],
        vsesstastics: [],
        slbserversstastics: [],
        slbgroups: []
    };
    $scope.view = {
        slbs: {},

        slbhistoryversions: [],
        onlineslb: {},
        offlineslb: {},
        extendedslb: {},
        slbvses: [],
        currentLog: [],
        slbGroupSummary: {},
        slbgroupsHealthySummary: {}
    };
    $scope.tableOps = {
        slbServer: {
            showMoreColumns: false,
            showOperations: false
        },
        slbVs: {
            showMoreColumns: false,
            showOperations: false
        }
    };
    $scope.data={};
    var log_start = new Date().getTime() - 1000 * 60 * 60;
    var log_end = new Date().getTime() + 1000 * 60 * 60;

    var dashboardStartTime = $.format.date(log_start, 'yyyy-MM-dd HH:mm:ss');
    var dashboardEndTime = $.format.date(log_end, 'yyyy-MM-dd HH:mm:ss');
    // SLB servers
    $scope.getSlbServerShowMore = function () {
        return $scope.tableOps.slbServer.showMoreColumns ? 'fa fa-list-alt text-info' : 'fa fa-list-alt font-gray';
    };
    $scope.getSlbServerShowMoreTitle = function () {
        return $scope.tableOps.slbServer.showOperations ? '简略' : '详细';
    };
    $scope.toggleShowMoreSlbServerColumns = function () {
        $scope.tableOps.slbServer.showMoreColumns = !$scope.tableOps.slbServer.showMoreColumns;
        /*   if ($scope.tableOps.slbServer.showMoreColumns) {
         $('#slb-servers-table').bootstrapTable('showColumn', 'hickwall');
         } else {
         $('#slb-servers-table').bootstrapTable('hideColumn', 'hickwall');
         }*/
    };

    $scope.getSlbCName = function (slb) {

        if (!slb) return;

        var properties = _.indexBy(slb['properties'], 'name');
        if (properties && properties['ops-cname']) return properties['ops-cname']['value'];

        return '-';
    };

    $scope.disableOpenSlbServer = function () {
        var can = A.canDo('Slb', 'UPDATE', $scope.model.offlineslb.id);
        return !can;
    };
    $scope.getSlbServerShowOperation = function () {
        return $scope.tableOps.slbServer.showOperations ? 'fa fa-wrench text-info' : 'fa fa-wrench font-gray';
    };
    $scope.toggleShowSlbServerOperations = function () {
        $scope.tableOps.slbServer.showOperations = !$scope.tableOps.slbServer.showOperations;
        var can = A.canDo('Slb', 'UPDATE', $scope.model.offlineslb.id);
        if (can) {
            if ($scope.tableOps.slbServer.showOperations) {
                $('#slb-servers-table').bootstrapTable('showColumn', 'Operation');
            }
            else {
                $('#slb-servers-table').bootstrapTable('hideColumn', 'Operation');
            }
        }
    };
    $scope.getSlbServerOperationTitle = function () {
        return $scope.tableOps.slbServer.showOperations ? '关闭操作' : '打开操作';
    };

    // New slb server
    $scope.showSyncCms = function () {
        return A.canDo('SLB', 'UPDATE', $scope.query.slbId);
    };

    $scope.startCmsSyncing = function () {
        var slbId = $scope.query.slbId;
        // before syncing
        var slbBefore = $scope.model.offlineslb;
        // after syncing

        var request = {
            method: 'GET',
            url: G.baseUrl + '/api/slb/sync/cms?slbId=' + slbId
        };

        $http(request).success(function (response, code) {
            if (code != 200) {
                alert('Failed to get response from cms. error message: ' + (response['message'] || code));
                return;
            }
            var slbAfter = response;


            var slbServersAfter = _.pluck(slbAfter['slb-servers'], 'ip');
            var slbServersBefore = _.pluck(slbBefore['slb-servers'], 'ip');

            var afterIndex = _.indexBy(slbAfter['slb-servers'], 'ip');
            var beforeIndex = _.indexBy(slbBefore['slb-servers'], 'ip');

            var added = [];
            var deleted = [];
            for (var i = 0; i < slbServersAfter.length; i++) {
                var c = slbServersAfter[i];
                if (slbServersBefore.indexOf(c) == -1) {
                    added.push({
                        name: afterIndex[c].name,
                        ip: c
                    });
                }
            }

            for (var i = 0; i < slbServersBefore.length; i++) {
                var c = slbServersBefore[i];
                if (slbServersAfter.indexOf(c) == -1) {
                    deleted.push({
                        name: beforeIndex[c].name,
                        ip: c
                    });
                }
            }

            $scope.data['syncing'] = {
                added: added,
                removed: deleted
            };
        });
    };
    // SLB VSES
    $scope.getSlbVsShowMore = function () {
        return $scope.tableOps.slbVs.showMoreColumns ? 'fa fa-list-alt text-info' : 'fa fa-list-alt font-gray';
    };
    $scope.getSlbVsShowMoreTitle = function () {
        return $scope.tableOps.slbVs.showOperations ? '简略' : '详细';
    };
    $scope.toggleShowMoreSlbVsColumns = function () {
        $scope.tableOps.slbVs.showMoreColumns = !$scope.tableOps.slbVs.showMoreColumns;
        if ($scope.tableOps.slbVs.showMoreColumns) {
            $('#slb-vses-table').bootstrapTable('showColumn', 'slb-ids');
            $('#slb-vses-table').bootstrapTable('showColumn', 'serverCount');
            $('#slb-vses-table').bootstrapTable('showColumn', 'memberCount');
        } else {
            $('#slb-vses-table').bootstrapTable('hideColumn', 'slb-ids');
            $('#slb-vses-table').bootstrapTable('hideColumn', 'serverCount');
            $('#slb-vses-table').bootstrapTable('hideColumn', 'memberCount');
        }
    };

    $scope.disableOpenSlbVs = function () {
        var can = A.canDo('Slb', 'UPDATE', $scope.model.offlineslb.id);
        return !can;
    };
    $scope.getSlbVsShowOperation = function () {
        return $scope.tableOps.slbVs.showOperations ? 'fa fa-wrench text-info' : 'fa fa-wrench font-gray';
    };
    $scope.toggleShowSlbVsOperations = function () {
        var can = A.canDo('Slb', 'UPDATE', $scope.model.offlineslb.id);
        if (can) {
            $scope.tableOps.slbVs.showOperations = !$scope.tableOps.slbVs.showOperations;
        }
    };
    $scope.getSlbVsOperationTitle = function () {
        return $scope.tableOps.slbVs.showOperations ? '关闭操作' : '打开操作';
    };

    //Rights Area
    $scope.showUpdateSlbBt = function () {
        return A.canDo("Slb", "UPDATE", $scope.query.slbId) && $scope.query.showextended;
    };
    $scope.showUpdateSlbServerBt = function () {
        var wrenchOp = A.canDo("Slb", "UPDATE", $scope.query.slbId) && $scope.tableOps.slbServer.showOperations;
        if (!wrenchOp) return false;
        else return true;
    };
    $scope.showActivateSlbBt = function () {
        return A.canDo("Slb", "ACTIVATE", $scope.query.slbId) && $scope.query.showextended;
    };
    $scope.showAddSLBTagBt = function () {
        return A.canDo("Slb", "PROPERTY", $scope.query.slbId) && $scope.query.showextended;
    };
    $scope.showAddSLBPropertyBt = function () {
        return A.canDo("Slb", "PROPERTY", $scope.query.slbId) && $scope.query.showextended;
    };
    $scope.showDeactivateSlbBt = function () {
        return A.canDo("Slb", "DEACTIVATE", $scope.query.slbId) && $scope.query.showextended;
    };
    $scope.showDeleteSlbBt = function () {
        return A.canDo("Slb", "DELETE", $scope.query.slbId) && $scope.query.showextended;
    };
    $scope.showCloneSlbBt = function () {
        return A.canDo("Slb", "UPDATE", $scope.query.slbId) && $scope.query.showextended;
    };
    $scope.showAddVsBt = function () {
        var wrenchOp = A.canDo("Slb", "UPDATE", $scope.query.slbId) && $scope.tableOps.slbVs.showOperations;
        if (!wrenchOp) return false;
        else return true;
    };
    $scope.showLogs = function () {
        return $scope.model.currentlogs.length > 0;
    };

    $scope.navigateTo = function (type) {
        var url = '';
        switch (type) {
            case 'vses': {
                url = '/portal/vses#?env=' + G.env + '&slbId=' + $scope.query.slbId + '&&groupType=Group';
                break;
            }
            case 'groups': {
                url = '/portal/groups#?env=' + G.env + '&slbId=' + $scope.query.slbId + '&&groupType=Group';
                break;
            }
            case 'activated': {
                url = '/portal/groups#?env=' + G.env + '&slbId=' + $scope.query.slbId + '&groupStatus=已激活&groupType=Group';
                break;
            }
            case 'tobeactivated': {
                url = '/portal/groups#?env=' + G.env + '&slbId=' + $scope.query.slbId + '&groupStatus=有变更&groupType=Group';
                break;
            }
            case 'deactivated': {
                url = '/portal/groups#?env=' + G.env + '&slbId=' + $scope.query.slbId + '&groupStatus=未激活&groupType=Group';
                break;
            }
            case 'healthy': {
                url = '/portal/groups#?env=' + G.env + '&slbId=' + $scope.query.slbId + '&groupHealthy=healthy:healthy&groupType=Group';
                break;
            }
            case 'unhealthy': {
                url = '/portal/groups#?env=' + G.env + '&slbId=' + $scope.query.slbId + '&groupHealthy=healthy:unhealthy&groupType=Group';
                break;
            }
            case 'broken': {
                url = '/portal/groups#?env=' + G.env + '&slbId=' + $scope.query.slbId + '&groupHealthy=healthy:Broken&groupType=Group';
                break;
            }

            case 'qps': {
                url = '/portal/slb/traffic#?env=' + G.env + '&slbId=' + $scope.query.slbId + '&startTime=2017-02-21%2017%3A00&trafficType=请求数';
                break;
            }
            default : {
                url = '';
                break;
            }
        }
        window.location.href = url;
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

    // Auto Complete caching
    $scope.cacheRequestFn = function (str) {
        return {q: str, timestamp: new Date().getTime()};
    };
    $scope.remoteUrl = function () {
        return G.baseUrl + "/api/meta/slbs";
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
    // Deactivate and Remove Area
    $scope.disableDeactivateSlb = function () {
        var status = $scope.getSlbStatusProperty();
        if (status == "deactivated") {
            return true;
        }
        return false;
    };
    $scope.disableRemoveSlb = function () {
        var status = $scope.getSlbStatusProperty();
        if (status == "activated" || status == "tobeactivated" || !status) {
            return true;
        }
        return false;
    };

    $scope.activateSlbTitleClass = function () {
        try {
            if ($scope.model.onlineslb.data.version != undefined && $scope.model.onlineslb.data.version == $scope.model.extendedslb.version) {
                return "status-red-important";
            }
        } catch (e) {

        }
    };
    $scope.confirmActivateText = '';
    $scope.activateSlbClick = function () {
        var resource = $scope.resource;
        if (!resource || _.keys(resource).length == 0) return;

        if ($scope.model.onlineslb.data.version != undefined && $scope.model.onlineslb.data.version == $scope.model.extendedslb.version) {
            $scope.confirmActivateText = resource['slb']['slb_slbInfoApp_activateSLBModal']['线上已是最新版本,确认是否强制重新激活'];
        }
        var baseText = JSON.stringify(U.sortObjectFileds($scope.model.onlineslb.data), null, "\t");
        var newText = JSON.stringify(U.sortObjectFileds($scope.model.offlineslb), null, "\t");
        var baseVersion = resource['slb']['slb_slbInfoApp_activateSLBModal']['线上SLB版本'] + '(Version:' + $scope.model.onlineslb.data.version + ")";
        var newVersion = resource['slb']['slb_slbInfoApp_activateSLBModal']['更新后SLB版本'] + '(Version:' + $scope.model.offlineslb.version + ")";
        var diffoutputdiv = document.getElementById('diffOutput');
        diffTwoSlbs(diffoutputdiv, baseText, newText, baseVersion, newVersion);

        $('#activateSLBModal').modal('show');
    };
    $scope.activateSLB = function () {
        var resource = $scope.resource;
        if (!resource || _.keys(resource).length == 0) return;

        $('#confirmActivateSLB').modal('hide');
        $('#operationConfrimModel').modal("show").find(".modal-title").html(resource['slb']['slb_slbInfoApp_operationConfrimModel']['激活Slb']);
        $('#operationConfrimModel').modal("show").find(".modal-body").html(resource['slb']['slb_slbInfoApp_operationConfrimModel']['正在激活'] + ".. <img src='/static/img/spinner.gif' />");
        var param = {
            slbId: $scope.query.slbId
        };
        var req = {
            method: 'GET',
            url: G.baseUrl + '/api/activate/slb?description=' + $scope.query.user,
            params: param
        };
        $scope.processRequest(req, $('#operationConfrimModel'), resource['slb']['slb_slbInfoApp_operationConfrimModel']['激活Slb'],
            resource['slb']['slb_slbInfoApp_operationConfrimModel']['激活成功']);
    };
    $scope.activateBtShow = function () {
        return A.canDo("Slb", "ACTIVATE", $scope.query.slbId);
    };

    $scope.deactivateSlbAction = function () {
        var status = $scope.getSlbStatusProperty();
        if (status != "deactivated") {
            if ($scope.model.slbvses && $scope.model.slbvses.length > 0) {
                $('#discardDeactivateSLBModal').modal('show');
            } else {
                $('#allowDeactivateSLBModal').modal('show');
            }
        }
    };
    $scope.deactivateSlb = function () {
        $('#allowDeactivateSLBModal').modal('hide');
        $('#deactivateSLBModalConfirm').modal('show');
    };
    $scope.confirmDeactivateSlb = function () {
        var resource = $scope.resource;
        if (!resource || _.keys(resource).length == 0) return;

        $('#deactivateSLBModalConfirm').modal('hide');

        var loading = "<img src='/static/img/spinner.gif' /> " + resource['slb']['slb_slbInfoApp_operationConfrimModel']['正在下线'];
        $('#operationConfrimModel').modal('show').find(".modal-body").html(loading);
        var param = {
            slbId: $scope.query.slbId
        };
        var request = {
            method: 'GET',
            url: G.baseUrl + '/api/deactivate/slb?description=' + $scope.query.deactivatereason,
            params: param
        };
        $scope.processRequest(request, $('#operationConfrimModel'), resource['slb']['slb_slbInfoApp_operationConfrimModel']["下线SLB"],
            resource['slb']['slb_slbInfoApp_operationConfrimModel']["下线SLB成功, SlbId"] +
            ":" + $scope.query.slbId, "");
    };

    $scope.deleteSlbAction = function () {
        var status = $scope.getSlbStatusProperty();
        if (status == "deactivated") {
            if ($scope.model.slbvses && $scope.model.slbvses.length > 0) {
                $('#discardDeleteSLBModal').modal('show');
            } else {
                $('#allowDeleteSLBModal').modal('show');
            }
        }
    };
    $scope.deleteSlb = function () {
        $('#allowDeleteSLBModal').modal('hide');
        $('#deleteSLBModalConfirm').modal('show');
    };
    $scope.confirmDeleteSlb = function () {
        var resource = $scope.resource;
        if (!resource || _.keys(resource).length == 0) return;


        $('#deleteSLBModalConfirm').modal('hide');
        var loading = "<img src='/static/img/spinner.gif' /> " + resource['slb']['slb_slbInfoApp_operationConfrimModel']["正在删除"];
        $('#operationConfrimModel').modal('show').find(".modal-body").html(loading);
        var param = {
            slbId: $scope.query.slbId
        };
        var request = {
            method: 'GET',
            url: G.baseUrl + '/api/slb/delete?description=' + $scope.query.deleteslbreason,
            params: param
        };
        $scope.processRequest(request, $('#deleteGroupConfirmModel'), +resource['slb']['slb_slbInfoApp_operationConfrimModel']["删除SLB"],
            resource['slb']['slb_slbInfoApp_operationConfrimModel']["删除SLB成功, SlbId"] +
            ":" + $scope.query.slbId, "");
    };

    // Option panel Area
    $scope.toggleOptionPanel = function () {
        $scope.query.showextended = !$scope.query.showextended;
        var p = A.canDo("Slb", "UPDATE", $scope.query.slbId) && $scope.query.showextended;
        if (p) {
            $scope.tableOps.slbServer.showOperations = true;
            $scope.tableOps.slbVs.showOperations = true;
            $('#slb-vses-table').bootstrapTable('showColumn', 'Operation');
            $('#slb-servers-table').bootstrapTable('showColumn', 'Operation');
        } else {
            $scope.tableOps.slbServer.showOperations = false;
            $scope.tableOps.slbVs.showOperations = false;

            $('#slb-vses-table').bootstrapTable('hideColumn', 'Operation');
            $('#slb-servers-table').bootstrapTable('hideColumn', 'Operation');
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
        var resource = $scope.resource;
        if (!resource || _.keys(resource).length == 0) return;
        if (!$scope.query.showextended) return resource['slb']['slb_slbInfoApp_opmap']["打开操作面板"];
        return resource['slb']['slb_slbInfoApp_opmap']["收起操作面板"];
    };
    // Focus Area
    $scope.getFocusObject = function () {
        if ($scope.query == undefined) return undefined;
        var f = _.find($scope.model.extendedslb.tags, function (item) {
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
        if (f == undefined) return "fa fa-eye-slash";
        return "fa fa-eye";
    };
    $scope.getFocusText = function () {
        var resource = $scope.resource;
        if (!resource || _.keys(resource).length == 0) return;

        var f = $scope.getFocusObject();
        if (!f) return resource['slb']['slb_slbInfoApp_opmap']["关注"];
        return resource['slb']['slb_slbInfoApp_opmap']["取消关注"];
    };
    $scope.addFocus = function (tagName, target) {
        var resource = $scope.resource;
        if (!resource || _.keys(resource).length == 0) return;
        $(target).showLoading();
        var param = {
            type: 'slb',
            tagName: tagName,
            targetId: $scope.query.slbId
        };
        var request = {
            method: 'GET',
            url: G.baseUrl + '/api/tagging?description=focus',
            params: param
        };
        $http(request).success(
            function (response) {
                $(target).hideLoading();
                $(target).html(resource['slb']['slb_slbInfoApp_opmap']["取消关注"]);
                $(target).parent('span').removeClass('fa fa-eye-slash').addClass("fa fa-eye");
                if (!$scope.model.extendedslb.tags) $scope.model.extendedslb.tags = [];
                var f = _.find($scope.model.extendedslb.tags, function (item) {
                    return item.trim().toLowerCase() == tagName.toLowerCase();
                });
                if (!f) {
                    $scope.model.extendedslb.tags.push(tagName);
                }
            }
        );
    };
    $scope.removeFocus = function (tagName, target) {
        $(target).showLoading();

        var param = {
            type: 'slb',
            tagName: tagName,
            targetId: $scope.query.slbId
        };
        var request = {
            method: 'GET',
            url: G.baseUrl + '/api/untagging?description=unfocuse',
            params: param
        };
        $http(request).success(
            function (response) {
                $(target).hideLoading();

                $(target).html(resource['slb']['slb_slbInfoApp_opmap']["关注"]);
                $(target).parent('span').removeClass('fa fa-eye').addClass("fa fa-eye-slash");
                var index = $scope.model.extendedslb.tags.indexOf(tagName);
                if (index != -1) {
                    $scope.model.extendedslb.tags.splice(index, 1);
                }
            }
        );
    };

    // Status Area
    $scope.statusText = function () {
        var resource = $scope.resource;
        if (!resource || _.keys(resource).length == 0) return;

        var s = $scope.getSlbStatusProperty();
        switch (s) {
            case "activated":
                return resource['slb']['slb_slbInfoApp_sum_activated'];
            case "deactivated":
                return resource['slb']['slb_slbInfoApp_sum_deactivated'];
            case "tobeactivated":
                return resource['slb']['slb_slbInfoApp_sum_tobeactivated'];
            default:
                return resource['slb']['slb_slbInfoApp_sum_unknow'];
        }
    };
    $scope.statusClass = function () {
        var s = $scope.getSlbStatusProperty();
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
                $('#deleteSLBTag').modal({backdrop: 'static'});
                break;
            }
            case 'deleteProp' : {
                if ($scope.canDeleteTag(value)) {
                    $scope.currentProperty = value;
                    $('#deleteSLBProp').modal({backdrop: 'static'});
                }
                break;
            }
            case 'addTag' : {
                $('#addSLBTag').modal({backdrop: 'static'});
                break;
            }
            case 'addProp' : {
                $('#addSLBProp').modal({backdrop: 'static'});
            }
        }
    };
    $scope.canDeleteTag = function (p) {
        var pName = p.name.toLowerCase();
        if (!$scope.query.showextended) return;
        if (pName == "idc_code" || pName == "status" || pName == "idc" || pName == "zone" || pName == "pci") {
            return false;
        }
        return true;
    };
    $scope.addTag = function (tagName, type) {
        var param = {
            type: 'slb',
            tagName: tagName,
            targetId: $scope.query.slbId
        };
        var request = {
            method: 'GET',
            url: G.baseUrl + '/api/tagging?description=' + $scope.query.tagreason,
            params: param
        };
        $('#addSLBTag').modal('hide');
        $scope.processRequest(request, $('#operationConfrimModel'), '', '新增Tag');
    };
    $scope.deleteTag = function (tagName, type) {
        var param = {
            type: 'slb',
            tagName: tagName,
            targetId: $scope.query.slbId
        };
        var request = {
            method: 'GET',
            url: G.baseUrl + '/api/untagging',
            params: param
        };
        $('#deleteSLBTag').modal('hide');
        $scope.processRequest(request, $('#operationConfrimModel'), '', "删除Tag");
    };
    $('#addTagBt').click(function (e) {
        e.preventDefault();
        var validate = reviewData($('#addSLBTag'));
        if (!validate) return;
        $scope.addTag($('#tagNameInput').val().trim());
    });
    $('#addPropertyBt').click(function (e) {
        e.preventDefault();
        var validate = reviewData($('#addSLBProp'));
        if (!validate) return;
        $scope.addProperty({'name': $('#pname').val().trim(), 'value': $('#pvalue').val().trim()});
    });
    // Property
    $scope.addProperty = function (prop) {
        var param = {
            type: 'slb',
            pname: prop.name,
            pvalue: prop.value,
            targetId: $scope.query.slbId
        };
        var request = {
            method: 'GET',
            url: G.baseUrl + '/api/property/set?description=' + $scope.query.propertyreason,
            params: param
        };
        $('#addSLBProp').modal("hide");
        if (prop.name == 'status') return;
        $scope.processRequest(request, $('#operationConfrimModel'), "", "添加Property");
    };
    $scope.deleteProperty = function (prop) {
        var param = {
            type: 'slb',
            pname: prop.name,
            pvalue: prop.value,
            targetId: $scope.query.slbId
        };
        var request = {
            method: 'GET',
            url: G.baseUrl + '/api/property/clear?description=' + $scope.query.deletepropertyreason,
            params: param
        };
        $('#deleteSLBProp').modal('hide');
        $scope.processRequest(request, $('#operationConfrimModel'), "", "删除Property");
    };
    // Diff
    $scope.confirmDiffButtonDisable = true;
    $scope.targetDiffSlbId;
    $scope.selectDiffSlbId = function (t) {
        if (t) {
            var toId = t.originalObject.id;
            $scope.targetDiffSlbId = toId;
            $scope.confirmDiffButtonDisable = false;
        }
    };
    $scope.confirmSelectSlbId = function () {
        var resource = $scope.resource;
        if (!resource || _.keys(resource).length == 0) return;

        var currentslbidtext = resource['slb']['slb_slbInfoApp_diffSeperateSlbs']['当前SlbId'];
        var targetslbidtext = resource['slb']['slb_slbInfoApp_diffSeperateSlbs']['对比SlbId'];

        if ($scope.targetDiffSlbId) {
            // has selected the current targetId
            $scope.confirmActivateText = currentslbidtext + ':' + $scope.query.slbId + ". " + targetslbidtext + ":" + $scope.targetDiffSlbId;

            $http.get(G.baseUrl + "/api/slb?slbId=" + $scope.targetDiffSlbId).success(
                function (res) {
                    var baseText = JSON.stringify(U.sortObjectFileds($scope.model.offlineslb), null, "\t");
                    var newText = JSON.stringify(U.sortObjectFileds(_.omit(res, 'virtual-servers')), null, "\t");

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
                        baseTextName: currentslbidtext + ': ' + $scope.query.slbId,
                        newTextName: targetslbidtext + ':' + res.id,
                        viewType: 0
                    }));
                    $('#diffSeperateSlbs').modal('show');
                }
            );
        }
        else {
            alert("请选择要比较的Slb");
        }
    };
    /**Basic Information Area
     * Version
     * History
     * VIPS
     * Tags and Properties
     * */
    $scope.slbVersionClass = function () {
        var v = $scope.getSlbStatusProperty();
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
    $scope.slbVersionText = function () {
        var resource = $scope.resource;
        if (!resource || _.keys(resource).length == 0) return;

        var m = resource['slb']['slb_slbInfoApp_versionmap'];

        var s = $scope.getSlbStatusProperty();
        if (s) s = s.toLowerCase();

        var v = $scope.model.onlineslb.data == undefined ? "unknownText" : $scope.model.onlineslb.data.version;
        switch (s) {
            case "activated":
                return m["已是线上版本"];
            case "deactivated":
                return m["无线上版本，点击上线"];
            case "tobeactivated":
                return m["线上版本"] + v + "，" + m["点击Diff"];
            default:
                return m["未知"];
                break;
        }
    };
    // History Area
    $scope.diffVersions = {};
    $scope.getHistoryClick = function () {
        $scope.diffVersions = {};
        var c = $scope.model.extendedslb.version;
        $scope.diffVersions[c] = c;

        $.each($scope.view.slbhistoryversions, function (i, val) {
            if (val != c)
                $('#historyVersion' + val).removeClass('label-info');
        });
        $('#historyVersion' + c).addClass('label-info');
        $('#historyVersionModel').modal('show');
    };
    $scope.diffSlbBetweenVersions = function () {
        var resource = $scope.resource;
        if (!resource || _.keys(resource).length == 0) return;

        var diffVersions = _.keys($scope.diffVersions);
        if (diffVersions.length < 2) {
            alert("请选择两个版本进行diff");
        }
        var v = resource['slb']['slb_slbInfoApp_op']['版本号'];

        $scope.confirmActivateText = v + ':' + diffVersions[0] + " VS " + v + ":" + diffVersions[1];
        var v1 = '';
        var v2 = '';
        $q.all(
            [
                $http.get(G.baseUrl + "/api/archive/slb?slbId=" + $scope.query.slbId + "&version=" + diffVersions[0]).success(
                    function (res) {
                        v1 = _.omit(res, 'virtual-servers');
                    }
                ),
                $http.get(G.baseUrl + "/api/archive/slb?slbId=" + $scope.query.slbId + "&version=" + diffVersions[1]).success(
                    function (res) {
                        v2 = _.omit(res, 'virtual-servers');
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
                    baseTextName: v + ':' + diffVersions[0],
                    newTextName: v + ':' + diffVersions[1],
                    viewType: 0
                }));
                $('#historyVersionModel').modal('hide');
                $('#diffSeperateSlbs').modal('show');
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
    $scope.showLinks = function (type) {
        var env = $scope.env;
        if (env && G[env].urls[type]) return true;
        return false;
    };
    $scope.isCdNginxValid = function () {
        var slb = $scope.view.extendedslb;
        if (!slb) return;

        var properties = slb['properties'];
        if (!properties) return;

        var nginxCmsGroupId = _.filter(properties, function (v) {
            return v['name'].toLowerCase() == 'nginxcmsgroupid';
        });
        var pci = _.filter(properties, function (v) {
            return v['name'].toLowerCase() == 'pci';
        });
        return nginxCmsGroupId.length > 0 && pci.length > 0;
    };

    //WEBINFO,HICKWALL,CAT links
    $scope.generateMonitorUrl = function (type) {

    };
    // VIPS Area
    $scope.generateVIPLink = function (vip) {
        return G[G.env].urls.webinfo + "/#/route?keyword=" + vip;
    };
    // Tagging Area
    $scope.showExtendedTag = function () {
        if ($scope.model.extendedslb.tags == undefined) return false;
        var u_tag = _.filter($scope.model.extendedslb.tags, function (m) {
            return m.trim().toLowerCase().startWith("user_");
        });
        var p = u_tag.length == $scope.model.extendedslb.tags.length;
        var t = $scope.model.extendedslb.tags == undefined || $scope.model.extendedslb.tags.length == 0;
        if (t || p) return false;
        return true;
    };
    $scope.showCurrentTag = function (t) {
        var x = !t.trim().toLowerCase().startWith('user_');
        return x;
    };
    $scope.showRemoveTagBt = function (v) {
        if (v.toLowerCase().startWith('owner') || v.toLowerCase().startWith('user')) return false;
        return A.canDo("Slb", "PROPERTY", $scope.model.extendedslb.id) && $scope.query.showextended;
    };
    // Property
    $scope.showExtendedProperty = function () {
        return !($scope.model.extendedslb.properties == undefined || $scope.model.extendedslb.properties.length == 0);
    };
    $scope.showRemoveProperty = function (v) {
        var v = v.name;
        if (v.toLowerCase() == "idc_code" || v.toLowerCase() == "idc" || v.toLowerCase() == "zone" || v.toLowerCase() == "status" || v.toLowerCase() == "pci") return false;
        return A.canDo("Slb", "PROPERTY", $scope.model.extendedslb.id) && $scope.query.showextended;
    };

    $scope.summaryInfo = {};

    $('.confirmActivateVs').on('click', function () {
        $('#activateVSModal').modal('hide');
        $('#confirmActivateVS').modal('show');
    });
    $('.doubleConfirmActivateVs').on('click', function () {
        $('#confirmActivateVS').modal('hide');
    });

    /**SLB Servers Area
     * */
    $('#slb-servers-table').on('check.bs.table uncheck.bs.table check-all.bs.table uncheck-all.bs.table', function () {
        $('#removeSlbServer').prop('disabled', !$('#slb-servers-table').bootstrapTable('getSelections').length);
    });
    $scope.getIdSelections = function () {
        return $.map($('#slb-servers-table').bootstrapTable('getSelections'), function (row) {
            return row;
        });
    };
    $scope.newSlbServers = [
        {
            'ip': '',
            'host-name': ''
        }
    ];
    $scope.slbServersTable = {
        columns: $scope.newSlbServers,
        add: function (index) {
            this.columns.push({
                'ip': '',
                'host-name': ''
            });
        },
        remove: function (index) {
            this.columns.splice(index, 1);
        }
    };
    $scope.toBeUpdatedslbInfo = {};
    $('#saveBatchMember').click(function () {
        var review = reviewData($('#addBatchServerModal'));
        if (review) {
            var errorLines = [];
            var machines = [];
            var text = $('.machines-textarea').val().trim();
            var lines = text.split('\n');
            $.each(lines, function (i, line) {
                var reg = /^((([a-z]|[A-Z]|[0-9])*)\/((\d+)\.(\d+)\.(\d+)\.(\d+)))$/g;
                var match = reg.exec(line.trim());
                if (!match) {
                    errorLines.push(i);
                } else {
                    var s = line.split('/');
                    machines.push({
                        'host-name': s[0],
                        ip: s[1]
                    });
                }
            });
            var str = '<p><b class="status-red">红色部分</b>输入格式有误，必须是 机器名/ip/端口号/权重 的格式.请订正后重新扩容</p>';
            if (errorLines.length != 0) {
                $.each(lines, function (i, item) {
                    if (errorLines.indexOf(i) != -1) {
                        str += '<p class="status-red">' + item + '</p>';
                    } else {
                        str += '<p>' + item + '</p>';
                    }
                });
                $('#addServerErrorDialog').modal().find('.modal-body').html(str);
            } else {
                $('#addBatchServerModal').modal('hide');
                $scope.newSlbServers = machines;
                $scope.addSlbServerToSlb();
            }
        } else {
            return;
        }
    });
    $scope.addSlbServerToSlb = function () {
        var resource = $scope.resource;
        if (!resource || _.keys(resource).length == 0) return;

        $scope.toBeUpdatedslbInfo['slb-servers'] = [];
        var servers = {
            'id': $scope.query.slbId,
            'slb-servers': []
        };
        if ($scope.newSlbServers && $scope.newSlbServers.length > 0) {
            servers['slb-servers'] = $scope.newSlbServers;
        }
        var updateSlbReq = {
            method: 'POST',
            url: G.baseUrl + '/api/slb/addServer?description=' + ($scope.query.batchaddserverreason || $scope.query.addserverreason),
            data: servers
        };
        $scope.processRequest(updateSlbReq, $('#operationConfrimModel'), resource['slb']['slb_slbInfoApp_operationConfrimModel']["扩容SLB"],
            resource['slb']['slb_slbInfoApp_operationConfrimModel']["扩容成功"]);
    };
    $('#removeSlbServer').click(
        function () {
            var toBeRemovedSlbServers = _.toArray($scope.getIdSelections());
            var moreThanOneServers = $scope.model.offlineslb && $scope.model.offlineslb['slb-servers'].length > 1;
            if (!moreThanOneServers) {
                $('#discardRemoveSlbServerDialog').modal('show');
                return;
            }
            $scope.toBeRemovedSlbServers = toBeRemovedSlbServers;
            var str = '';
            $('.to-be-removed-slbServers').children().remove();

            if (toBeRemovedSlbServers != undefined && toBeRemovedSlbServers.length > 0) {
                $.each(toBeRemovedSlbServers, function (i, val) {
                    str += "<tr>";
                    str += "<td><span class='ip'>" + val.ip + "</span></td>";
                    str += "<td><span class='ip'>" + val['host-name'] + "</span></td>";
                    str += "</tr>";
                });
            }
            $('.to-be-removed-slbServers').append(str);
            $('#removeSlbServerDialog').modal('show');
        }
    );
    $scope.removeSlbServerFromSlb = function () {
        var resource = $scope.resource;
        if (!resource || _.keys(resource).length == 0) return;

        var ipList = "";
        if ($scope.toBeRemovedSlbServers && $scope.toBeRemovedSlbServers.length > 0) {
            var ipArray = _.pluck($scope.toBeRemovedSlbServers, "ip");
            $.each(ipArray, function (i, val) {
                if (i == ipArray.length - 1)
                    ipList += val;
                else
                    ipList += val + ",";
            })
        }
        var param = {
            'slbId': $scope.query.slbId,
            'ip': ipList
        };
        var updateSlbReq = {
            method: 'GET',
            url: G.baseUrl + '/api/slb/removeServer?description=' + $scope.query.deleteserverreason,
            params: param
        };
        $('#removeSlbServerDialog').modal('hide');
        $scope.processRequest(updateSlbReq, $('#operationConfrimModel'), resource['slb']['slb_slbInfoApp_operationConfrimModel']["缩容 Slb Server"],
            resource['slb']['slb_slbInfoApp_operationConfrimModel']["缩容成功"]
        );
    };

    // SLB Related VSES Area
    $scope.getVSData = function (vsId) {
        $http.get(G.baseUrl + "/api/vs?vsId=" + vsId + "&mode=online").then(
            function (res) {
                if (res.status == 200 || res.status == 202) {
                    $scope.onlineVSData = res.data;
                } else {
                    if (res.status == 400 && res.data.message == "Virtual server cannot be found.") {
                        $scope.onlineVSData = "No online version!!!";
                    }
                }
            }
        );

        $http.get(G.baseUrl + "/api/vs?vsId=" + vsId).success(
            function (res) {
                $scope.tobeActivatedVSData = res;
            }
        );
    };
    $scope.activateVS = function () {
        var loading = "<img src='/static/img/spinner.gif' /> 正在激活";
        $('#operationConfrimModel').modal('show').find(".modal-body").html(loading);
        var request = {
            method: 'GET',
            url: G.baseUrl + "/api/activate/vs?vsId=" + $scope.currentVSId
        };

        $scope.processRequest(request, $('#operationConfrimModel'), "激活VS", "激活VS成功,vsId:" + $scope.currentVSId, "");
    };
    $scope.toBeRemovedSlbServers = [];


    // New slb server
    $scope.showSyncCms = function () {
        return A.canDo('Slb', 'UPDATE', $scope.query.slbId);
    };

    $scope.startCmsSyncing = function () {
        var slbId = $scope.query.slbId;
        // before syncing
        var slbBefore = $scope.model.offlineslb;
        // after syncing

        var request = {
            method: 'GET',
            url: G.baseUrl + '/api/slb/sync/cms?slbId=' + slbId
        };

        $http(request).success(function (response, code) {
            if (code != 200) {
                alert('Failed to get response from cms. error message: ' + (response['message'] || code));
                return;
            }
            var slbAfter = response;


            var slbServersAfter = _.pluck(slbAfter['slb-servers'], 'ip');
            var slbServersBefore = _.pluck(slbBefore['slb-servers'], 'ip');

            var afterIndex = _.indexBy(slbAfter['slb-servers'], 'ip');
            var beforeIndex = _.indexBy(slbBefore['slb-servers'], 'ip');

            var added = [];
            var deleted = [];
            for (var i = 0; i < slbServersAfter.length; i++) {
                var c = slbServersAfter[i];
                if (slbServersBefore.indexOf(c) == -1) {
                    added.push({
                        name: afterIndex[c]['host-name'],
                        ip: c
                    });
                }
            }

            for (var i = 0; i < slbServersBefore.length; i++) {
                var c = slbServersBefore[i];
                if (slbServersAfter.indexOf(c) == -1) {
                    deleted.push({
                        name: beforeIndex[c]['host-name'],
                        ip: c
                    });
                }
            }

            $scope.data['syncing'] = {
                added: added,
                removed: deleted
            };
        });
    };

    // VSES summary Area
    $scope.vsesSummary = {};
    $scope.getVsesSummary = function () {
        var groups_count = 0;
        var apps_count = 0;
        var members_count = 0;
        var servers_count = 0;
        var servers_qps = 0;
        $.each($scope.view.slbvses, function (i, item) {
            if (item.groupCount) {
                groups_count += item.groupCount;
            }
            if (item.appCount) {
                apps_count += item.appCount;
            }
            if (item.memberCount) {
                members_count += item.memberCount;
            }
            if (item.serverCount) {
                servers_count += item.serverCount;
            }
            if (item.qps) {
                servers_qps += item.qps;
            }

        });
        $scope.vsesSummary.a = apps_count;
        $scope.vsesSummary.g = groups_count;
        $scope.vsesSummary.m = members_count;
        $scope.vsesSummary.s = servers_count;
        $scope.vsesSummary.q = Math.floor(servers_qps);
        $scope.vsesSummary.aq = Math.floor(servers_qps / $scope.model.extendedslb['slb-servers'].length);
    };

    // Common Methods Area
    $scope.getSlbStatusProperty = function () {
        var grpStatus = undefined;
        if (!$scope.model.extendedslb.properties) return grpStatus;

        var p = _.find($scope.model.extendedslb.properties, function (item) {
            return item.name.toLowerCase().trim() == "status";
        });
        if (p) {
            grpStatus = p.value.toLowerCase().trim();
        }
        return grpStatus;
    };
    $scope.processRequest = function (request, confirmDialog, operationText, tooltipText) {
        var resource = $scope.resource;
        if (!resource || _.keys(resource).length == 0) return;

        var fail = resource['slb']['slb_slbInfoApp_operationConfrimModel']['失败'];
        var failreason = resource['slb']['slb_slbInfoApp_operationConfrimModel']['失败原因'];
        var success = resource['slb']['slb_slbInfoApp_operationConfrimModel']['成功'];

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
                    errText = "<span class='fa fa-times'></span><span style='padding-left: 10px'>" + operationText + fail + "</span>";
                    confirmDialog.modal('show').find(".modal-title").html(errText);
                    confirmDialog.modal('show').find(".modal-body").html(failreason + ":" + msg);
                    if (msg.indexOf("overlap") > 0) {
                        // need force update
                        $scope.showForceUpdate = true;
                    }
                }
                else {
                    var successText = "<span class='fa fa-check'></span><span style='padding-left: 10px'>" + operationText + success + "</span>";
                    confirmDialog.modal('show').find(".modal-title").html(successText);
                    if (tooltipText) confirmDialog.modal('show').find(".modal-body").html(tooltipText);
                    startTimer(confirmDialog);
                }
            }
        ).error(function (reject) {
            msg = reject.message;
            var errText = "<span class='fa fa-times'></span><span style='padding-left: 10px'>" + operationText + fail + "</span>";
            confirmDialog.modal('show').find(".modal-title").html(errText);
            confirmDialog.modal('show').find(".modal-body").html(failreason + ":" + msg);
        });
    };
    $('.closeProgressWindowBt').click(
        function (e) {
            var hashData = {};
            hashData.timeStamp = new Date().getTime();
            H.setData(hashData);
        }
    );
    $('.closeProgressWindowBt2').click(
        function (e) {
            var hashData = {};
            hashData.timeStamp = new Date().getTime();
            window.location.href = '/portal/slbs#?env=' + $scope.env;
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

    function diffTwoSlbs(targetDiv, baseText, newText, baseVersion, newVersion) {
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

    function getSlbDataByVersion(row) {
        var resource = $scope.resource;
        if (!resource || _.keys(resource).length == 0) return;

        var beforeversion = resource['slb']['slb_slbInfoApp_slbdiffVSDiv']["变更前版本"];
        var afterversion = resource['slb']['slb_slbInfoApp_slbdiffVSDiv']["变更后版本"];
        var onlineversion = resource['slb']['slb_slbInfoApp_slbdiffVSDiv']["线上版本"];
        var offlineversion = resource['slb']['slb_slbInfoApp_slbdiffVSDiv']["线下版本"];


        var currentVersion = row['slb-version'];
        var id = row['target-id'];

        var c = currentVersion;
        var p = currentVersion - 1;

        if (row['operation'] == 'activate') {
            var gd = JSON.parse(row['data']);
            var gd_datas = gd['slb-datas'];
            var gd_sort = _.sortBy(gd_datas, 'version');
            p = gd_sort[0].version;
        }
        var param0 = {
            slbId: id,
            version: c
        };
        var param1 = {
            slbId: id,
            version: p
        };

        var request0 = {
            method: 'GET',
            url: G.baseUrl + '/api/archive/slb',
            params: param0
        };
        var request1 = {
            method: 'GET',
            url: G.baseUrl + '/api/archive/slb',
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

                var ptext = beforeversion + p;
                var ctext = afterversion + c;
                if (row['operation'] == 'activate') {
                    ptext = onlineversion + p;
                    ctext = offlineversion + c;
                }
                diffTwoSlb(target, baseText, NewText, ptext, ctext);
                $('#slbdiffVSDiv').modal('show');
            }
        );
    };
    window.slbServersRemoveEvent = {
        'click .slbRemoveServer': function (e, value, row) {
            var str = '';
            if ($scope.view.extendedslb['slb-servers'].length == 1) {
                $('#discardRemoveSlbServerDialog').modal('show');
                return;
            }
            $('.to-be-removed-slbServers').children().remove();

            if (row && row.ip && row['host-name']) {
                $scope.toBeRemovedSlbServers = [
                    {
                        'ip': row.ip,
                        'host-name': row['host-name']
                    }
                ];
                str += "<tr>";
                str += "<td><span class='ip'>" + row.ip + "</span></td>";
                str += "<td><span class='host-name'>" + row['host-name'] + "</span></td>";
                str += "</tr>";
            }
            $('.to-be-removed-slbServers').append(str);
            $('#removeSlbServerDialog').modal('show');
        }
    };
    $scope.getOperationText = function (x) {
        var resource = $scope.resource;
        if (!resource || _.keys(resource).length == 0) return;
        return resource['log']['log_operationLogApp_opmapping'][x];
    };

    // Bind api data to the scope variables
    $scope.bindData = function (out) {
        var slbserversstastics = out['slb_server'];
        var vsstastics = out['vses_stastics'];
        $scope.model.currentlogs = out['slb_log'];
        $scope.model.slbs = out['slbs_info'];
        $scope.model.onlineslb = out['slb_online'];
        $scope.model.extendedslb = out['slb_extended'];
        $scope.model.offlineslb = _.omit(_.omit($.extend(true, {}, out['slb_extended']), "properties"), 'tags');
        $scope.model.slbvses = out['slb_vses'];
        $scope.model.slbgroups = out['slb_groups'];
        $scope.query.user = out['current_user'].name;

        $scope.view.currentLog = $.extend(true, [], out['slb_log']);
        $scope.view.extendedslb = $.extend(true, {}, out['slb_extended']);
        $scope.view.slbhistoryversions = _.range(1, $scope.model.extendedslb.version + 1);
        $scope.view.slbvses = $.extend(true, [], out['slb_vses']);

        // View: slb object
        $.each($scope.view.extendedslb['slb-servers'], function (i, item) {
            var ip = slbserversstastics[item.ip];
            item.qps = (ip && ip.qps) ? ip.qps : '0';
        });
        $scope.view.extendedslb.status = $scope.getSlbStatusProperty();

        // View: current logs
        $.each($scope.view.currentLog, function (i, item) {
            var data = item.data;
            var isJson = IsJsonString(data);
            var version = '-';
            if (isJson) {
                data = JSON.parse(data);
                if (data['slb-datas'] && data['slb-datas'].length > 0) {
                    version = data['slb-datas'][0].version;
                }
            }
            item['slb-version'] = version;
        });

        // View: slb groups
        var statusCountItems = _.countBy($scope.model.slbgroups, function (item) {
            var v = _.find(item['properties'], function (r) {
                return r.name == 'status';
            });
            if (v) {
                return v.value.toLowerCase();
            } else return '';
        });
        $scope.view.slbGroupSummary['activated'] = statusCountItems['activated'] || 0;
        $scope.view.slbGroupSummary['tobeactivated'] = statusCountItems['tobeactivated'] || 0;
        $scope.view.slbGroupSummary['deactivated'] = statusCountItems['deactivated'] || 0;

        // View: SLB Healthy
        var healthyCountItems = _.countBy($scope.model.slbgroups, function (item) {
            var v = _.find(item['properties'], function (r) {
                return r.name == 'healthy';
            });
            if (v) {
                return v.value.toLowerCase();
            } else return '';
        });
        $scope.view.slbgroupsHealthySummary['healthy'] = healthyCountItems['healthy'] || 0;
        $scope.view.slbgroupsHealthySummary['unhealthy'] = healthyCountItems['unhealthy'] || 0;
        $scope.view.slbgroupsHealthySummary['broken'] = healthyCountItems['broken'] || 0;

        // View: SLB Vses
        $.each($scope.view.slbvses, function (i, item) {
            var s = vsstastics[item.id];
            item.memberCount = s ? s['member-count'] : 0;
            item.serverCount = s ? s['group-server-count'] : 0;
            item.groupCount = s ? s['group-count'] : 0;
            item.appCount = s ? s['app-count'] : 0;
            item.appCount = s ? s['app-count'] : 0;
            item.qps = s ? s['qps'] : 0;
            var e = _.find(item.properties, function (w) {
                return w.name == 'idc';
            });
            var t = _.find(item.properties, function (w) {
                return w.name == 'status';
            });
            item.idc = '';
            item.idc = (e && e.value) ? e.value : '-';
            item.vsStatus = (t && t.value) ? t.value : '-';
        });

        $scope.getVsesSummary();
        $scope.reloadTable();
    };
    $scope.initTable = function () {
        var resource = $scope.resource;

        var title = resource['slb']['slb_slbInfoApp_vstable']['ID'];
        var domaintitle = resource['slb']['slb_slbInfoApp_vstable']['Domain'];
        var ssltitle = resource['slb']['slb_slbInfoApp_vstable']['SSL'];
        var slbtitle = resource['slb']['slb_slbInfoApp_vstable']['SLB'];
        var idctitle = resource['slb']['slb_slbInfoApp_vstable']['IDC'];
        var grouptitle = resource['slb']['slb_slbInfoApp_vstable']['Group'];
        var qpstitle = resource['slb']['slb_slbInfoApp_vstable']['QPS'];
        var statustitle = resource['slb']['slb_slbInfoApp_vstable']['StatusTitle'];
        var linkstitle = resource['slb']['slb_slbInfoApp_vstable']['Links'];
        var serverstitle = resource['slb']['slb_slbInfoApp_vstable']['Servers'];
        var memberstitle = resource['slb']['slb_slbInfoApp_vstable']['Members'];

        var statusMappingTitle = resource['slb']['slb_slbInfoApp_vstable']["Status"];

        $('#slb-vses-table').bootstrapTable({
            toolbar: "#slbInfo-vsList-toolbar",
            columns: [[
                {
                    field: 'id',
                    title: title,
                    align: 'left',
                    valign: 'middle',
                    formatter: function (value, row, index) {
                        var id = '<a class="editVS" target="_blank" title="' + value + '" href="/portal/vs#?env=' + G.env + '&vsId=' + value + '">' + value + '</a>';
                        return id;
                    },
                    sortable: true
                },
                {
                    field: 'domains',
                    title: domaintitle,
                    align: 'left',
                    valign: 'middle',
                    width: '350px',
                    sortable: true,
                    formatter: function (value, row, index) {
                        var domains = "";
                        $.each(value, function (i, val) {

                            domains += '<a class="editVS" target="_blank" title="' + val.name + '" href="/portal/vs#?env=' + G.env + '&vsId=' + row.id + '" style="text-decoration: none; margin-left: 5px; word-break: break-all">' + val.name + '</a><br>'
                        });
                        return domains;
                    }
                },
                {
                    field: 'ssl',
                    title: ssltitle,
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    formatter: function (value, row, index) {
                        if (value)
                            return "Https";
                        else
                            return "Http";
                    }
                },
                {
                    field: 'slb-ids',
                    title: slbtitle,
                    align: 'left',
                    width: '150px',
                    valign: 'middle',
                    sortable: true,
                    visible: false,
                    formatter: function (value, row, index) {
                        var text = '<ul class="slb-list-ul">';
                        var nclass = 'path-main-item';

                        var name = _.find($scope.model.slbs, function (i) {
                            return i.id == $scope.query.slbId;
                        }).name;

                        text += '<li><a class="' + nclass + '" target="_blank" href="/portal/slb#?env=' + G.env + '&slbId=' + $scope.query.slbId + '">' + $scope.query.slbId + '(' + name + ')</a></li>';
                        //var index = _.indexOf(value, parseInt($scope.query.slbId));

                        //value.splice(index, 1);
                        $.each(value, function (i, c) {
                            if (c == $scope.query.slbId) return;

                            nclass = 'path-sub-item';
                            var slb = _.find($scope.model.slbs, function (item) {
                                return item.id == c;
                            });
                            text += '<li><a class="' + nclass + '" target="_blank" href="/portal/slb#?env=' + G.env + '&slbId=' + c + '">' + c + '(' + slb.name + ')</a></li>';
                        });

                        text += '</ul>';
                        return text;
                    }
                },
                {
                    field: 'idc',
                    title: idctitle,
                    align: 'left',
                    width: '100px',
                    valign: 'middle',
                    sortable: true,
                    formatter: function (value, row, index) {
                        return value;
                    }
                },
                {
                    field: 'groupCount',
                    title: grouptitle,
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    formatter: function (value, row, index) {
                        if (!value)
                            return "-";
                        return '<a target="_blank" href="' + '/portal/groups#?env=' + G.env + '&vsId=' + row.id + '">' + value + '</a>';
                    }
                },
                {
                    field: 'memberCount',
                    title: memberstitle,
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    visible: false,

                    formatter: function (value, row, index) {
                        if (!value)
                            return "-";
                        return value;
                    }
                },
                {
                    field: 'serverCount',
                    title: serverstitle,
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    visible: false,

                    formatter: function (value, row, index) {
                        if (!value)
                            return '-';
                        return value;
                    }
                },
                {
                    field: 'qps',
                    title: qpstitle,
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    formatter: function (value, row, index) {
                        return value ? '<a target="_blank" href="/portal/vs/traffic' + H.generateHashStr({
                            env: G.env,
                            vsId: row.id
                        }) + '">' + Math.floor(value) + '</a>' : '-';
                    }
                },
                {
                    field: 'vsStatus',
                    title: statustitle,
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    events: operateEvents,
                    formatter: function (value, row, index) {
                        if (!value)
                            return "-";
                        else {
                            var css = '';
                            var text = '';
                            value = value.toLowerCase();
                            text = statusMappingTitle[value];
                            switch (value) {
                                case "activated":
                                    css = 'status-green';
                                    break;
                                case "deactivated":
                                    css = 'status-gray';
                                    break;
                                case "tobeactivated":
                                    css = 'status-yellow';
                                    break;
                                default :
                                    css = "status-gray";
                            }
                        }
                        if (value == 'tobeactivated')
                            return '<span class="activeVS status-yellow">' + text + '(<a class="activeVS" data-toggle="modal" data-target="#activateVSModal">Diff</a>)</span>';
                        else
                            return '<span class="' + css + '" ">' + text + '</span>';
                    }
                },
                {
                    title: linkstitle,
                    align: 'center',
                    width: '250px',
                    valign: 'middle',
                    formatter: function (value, row, index) {
                        var slbId = $scope.query.slbId;
                        var domains = _.map(_.pluck(row.domains, 'name'), function (v) {
                            return "'" + v + "'";
                        });
                        var domainText = domains.join('%20OR%20');
                        var idc = row.idc.split(',')[0] || '-';
                        idc = slbApp.convertIDC(idc);
                        var query = 'domain%3D(' + domainText + ")%20AND%20idc%3D'" + idc + "'";

                        var vsId = row['id'];
                        var env = $scope.env;
                        env == 'pro' ? 'PROD' : env.toUpperCase();
                        var dashboard = G.dashboardportal + '/#env=' + env + '&metric-name=slb.req.count&interval=1m&start-time=' + dashboardStartTime + '&end-time=' + dashboardEndTime + '&chart=line&aggregator=sum&ts=1515983913677&tags={"slb_id":["' + slbId + '"],"vsid":["' + vsId + '"]}&group-by=[status]';

                        var esLink = slbApp.getEsHtml(query);
                        var str = '<div>';

                        if (G[env]['urls'].dashboard) {
                            str += '<div class="system-link">' +
                                '<a class="pull-left dashboard" title="Dashboard" target="_blank" href=\'' + dashboard + '\'>Dashboard</a>' +
                                '</div>';
                        }

                        var hickwallLink = G[$scope.env] ? G[$scope.env].urls.hickwall + '/d/vT4LobgZk/slb-vs?var-vsid=' + row.id : '';
                        if (hickwallLink) {
                            str += '<div class="system-link">' +
                                '<a class="pull-left hickwall" title="Hickwall" target="_blank" href=\'' + hickwallLink + '\'>Hickwall</a>' +
                                '</div>';
                        }

                        if (G[env]['urls'].es) {
                            str += esLink;
                        }

                        if (str == '<div>') {
                            str += '-';
                        }

                        str += '</div>';

                        return str;
                    }
                }
            ], []],
            sortName: 'qps',
            sortOrder: 'desc',
            data: $scope.view.slbvses,
            classes: "table table-bordered  table-hover table-striped",

            showRefresh: true,
            search: true,
            showColumns: true,
            minimumCountColumns: 2,
            pagination: true,
            idField: 'id',
            pageSize: 20,
            resizable: true,
            resizeMode: 'overflow',
            sidePagination: 'client',
            pageList: [20, 40, 80, 200],
            formatLoadingMessage: function () {
                return "<img class='loading-img' src='/static/img/loading_100.gif' /> 正在加载 Virtual Servers";
            },
            formatNoMatches: function () {
                return '<i class="fa status-alert fa-exclamation-triangle" aria-hidden="true"></i> 没有找到匹配的 Virtual Servers';
            }
        });
        $('#slb-servers-table').bootstrapTable({
            toolbar: "#slbInfo-serverList-toolbar",
            columns: [[
                {
                    field: 'state',
                    checkbox: true,
                    rowspan: 2,
                    align: 'center',
                    valign: 'middle'
                },
                {
                    field: 'ip',
                    title: 'IP',
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    formatter: function (value, row, index) {
                        if (!value)
                            return '-';
                        var link = G[G.env].urls.cat + "/cat/r/h?domain=100000716&ip=" + value;
                        ;
                        return '<a target="_blank" href="' + link + '">' + value + ' / ' + row['host-name'] + '</a>';
                    }
                },
                {
                    field: 'qps',
                    title: 'QPS',
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    formatter: function (value, row, index) {
                        if (!value)
                            return '-';
                        var link = '/portal/slb/traffic' + H.generateHashStr({env: G.env, slbId: $scope.query.slbId});
                        return '<a target="_blank" href="' + link + '">' + value + '</a>';
                    }
                },
                {
                    field: 'hickwall',
                    title: 'Links',
                    align: 'center',
                    valign: 'middle',
                    width: '420px',
                    formatter: function slbServersRemoveFormatter(value, row, index) {
                        var server = row.ip;
                        var slbId = $scope.query.slbId;

                        var query = "slb_id%3D'" + slbId + "'%20AND%20slb_server%3D'" + server + "'";

                        var env = $scope.env;
                        env == 'pro' ? 'PROD' : env.toUpperCase();
                        var dashboard = G.dashboardportal + '/#env=' + env + '&metric-name=slb.req.count&interval=1m&start-time=' + dashboardStartTime + '&end-time=' + dashboardEndTime + '&chart=line&aggregator=sum&ts=1515983913677&tags={"slb_id":["' + slbId + '"],"hostip":["' + server + '"]}&group-by=[status]';

                        var esLink = slbApp.getEsHtml(query);
                        var webInfoLink = slbApp.getWebinfoHtml(env, row['host-name']);

                        var str = '<div>';

                        if (G[env]['urls']['dashboard']) {
                            str += '<div class="system-link">' +
                                '<a class="pull-left dashboard" title="Dashboard" target="_blank" href=\'' + dashboard + '\'>Dashboard</a>' +
                                '</div>';
                        }

                        if (G[env]['urls']['hickwall']) {
                            str += '<div class="system-link">' +
                                '<a class="pull-left hickwall" title="Hickwall" target="_blank" href="' + G['pro'].urls.hickwallpre + '/dashboard/host/' + row['host-name'] + '">Hickwall' +
                                '</div>';
                        }
                        if (G[env]['urls']['cat']) {
                            str += '<div class="system-link">' +
                                '<a class="pull-left cat" target="_blank" href="' + G[G.env].urls.cat + '/cat/r/h?domain=100000716&ip=' + row['ip'] + '">CAT</a>' +
                                '</div>';
                        }

                        if (G[env]['urls']['es']) {
                            str += esLink;
                        }
                        if (G[env]['urls']['webinfo']) {
                            str += webInfoLink;
                        }

                        str += '</div>';


                        return str;
                    }
                },
                {
                    field: 'Operation',
                    title: 'Operation',
                    width: '110px',
                    align: 'center',
                    valign: 'middle',
                    events: slbServersRemoveEvent,
                    formatter: function slbServersRemoveFormatter() {
                        var p = "";
                        p = A.canDo("Slb", "UPDATE", $scope.query.slbId) ? "" : "hide";
                        return '<button type="button" class="btn btn-info btn-little slbRemoveServer ' + p + '" title="删除" aria-label="Left Align" ><span class="fa fa-minus"></span></button>';
                    }
                }
            ], []],
            data: $scope.view.extendedslb['slb-servers'],
            sortName: 'qps',
            sortOrder: 'desc',
            classes: "table-bordered  table-hover table-striped table",
            showRefresh: true,
            showColumns: true,
            showRefresh: true,
            search: true,
            minimumCountColumns: 2,
            idField: 'id',
            pageSize: 20,
            sidePagination: 'client',
            pagination: true,
            pageList: [20, 40, 80, 200],
            formatLoadingMessage: function () {
                return "<img class='loading-img' src='/static/img/loading_100.gif' /> 正在加载 SLB Servers";
            },
            formatNoMatches: function () {
                return '<i class="fa status-alert fa-exclamation-triangle" aria-hidden="true"></i> 没有找到匹配的 SLB Servers';
            }
        });
    };
    window.DiffVersionEvent = {
        'click .diff-version-bt': function (e, value, row) {
            getSlbDataByVersion(row);
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
                statusText = 'Success';
            } else {
                statusCss = 'status-red';
                statusText = 'Fail';
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
    window.operateEvents = {
        'click .activeVS': function (e, value, row) {
            var resource = $scope.resource;
            if (!resource || _.keys(resource).length == 0) return;

            $scope.currentVSId = row.id;
            $scope.confirmActivateText = resource['slb']['slb_slbInfoApp_activateSLBModal']['线上版本与当前版本比较'];
            if (row.vsStatus == "已激活") {
                $scope.confirmActivateText = resource['slb']['slb_slbInfoApp_activateSLBModal']["线上已是最新版本,确认是否强制重新激活"];
                $('.fileViewHead').removeClass("status-red-important").addClass("status-red-important");
                ;
            } else {
                $('.fileViewHead').removeClass("status-red-important");
            }
            $scope.getVSData(row.id);

            setTimeout(function () {

                var baseText = JSON.stringify(U.sortObjectFileds($scope.onlineVSData), null, "\t");
                var newText = JSON.stringify(U.sortObjectFileds($scope.tobeActivatedVSData), null, "\t");

                var base = difflib.stringAsLines(baseText);
                var newtxt = difflib.stringAsLines(newText);

                var sm = new difflib.SequenceMatcher(base, newtxt);
                var opcodes = sm.get_opcodes();
                var diffoutputdiv = document.getElementById("info_diffOutput");

                diffoutputdiv.innerHTML = "";
                $scope.onlineVsVersion = $scope.onlineVSData.version;
                if (!$scope.onlineVSData.version)
                    $scope.onlineVsVersion = resource['slb']['slb_slbInfoApp_activateSLBModal']["无"];

                diffoutputdiv.appendChild(diffview.buildView({
                    baseTextLines: base,
                    newTextLines: newtxt,
                    opcodes: opcodes,
                    baseTextName: resource['slb']['slb_slbInfoApp_activateSLBModal']["线上SLB版本"] + "(Version:" + $scope.onlineVsVersion + ")",
                    newTextName: resource['slb']['slb_slbInfoApp_activateSLBModal']["更新后SLB版本"] + "(Version:" + $scope.tobeActivatedVSData.version + ")",
                    viewType: 0
                }));

            }, 500);
        }
    };
    $scope.reloadTable = function () {
        var p = A.canDo("Slb", "UPDATE", $scope.query.slbId) && $scope.query.showextended;
        if (!p) {
            $('#slb-servers-table').bootstrapTable('hideColumn', 'Operation');
        } else {
            $('#slb-servers-table').bootstrapTable('showColumn', 'Operation');
        }
        $('#slb-servers-table').bootstrapTable("load", $scope.view.extendedslb['slb-servers'] ? $scope.view.extendedslb['slb-servers'] : []);

        var p1 = A.canDo("Vs", "ACTIVATE", "*") && $scope.query.showextended;
        var p2 = A.canDo("Vs", "UPDATE", "*") && $scope.query.showextended;
        if (p1 && p2) {
            $('#slb-vses-table').bootstrapTable('showColumn', 'Operation');
        } else {
            $('#slb-vses-table').bootstrapTable('hideColumn', 'Operation');
        }
        var vses = $scope.view.slbvses ? $scope.view.slbvses : [];
        $('#slb-vses-table').bootstrapTable("load", vses);

        // $('.slb-basic-info').hideLoading();
        $('#slb-servers-table').bootstrapTable('hideLoading');
        $('#slb-servers-table').bootstrapTable('hideLoading');
        $('#slb-vses-table').bootstrapTable("hideLoading");
    };
    $scope.hashChanged = function (hashData) {
        $scope.resource = H.resource;
        $scope.resource = H.resource;
        $('#slb-vses-table').bootstrapTable('removeAll');
        $('#slb-servers-table').bootstrapTable('removeAll');

        $('#slb-vses-table').bootstrapTable('showLoading');
        $('#slb-servers-table').bootstrapTable('showLoading');

        if (hashData.env) {
            $scope.env = hashData.env;
        }
        if (hashData.slbId) {
            $scope.query.slbId = hashData.slbId;
        }

        // Init app
        slbApp = new SlbApp(hashData, $http, $q, $scope.env);

        var query = slbApp.getQueries(hashData);

        // Send Request to the backend
        var out = slbApp.request(query);
        var promise = out.request;

        promise.then(
            function () {
                $scope.bindData(out.result);
            }
        );

        // Table Instance
        $scope.initTable();
        // init the wreches
        $scope.tableOps.slbServer.showOperations = false;
        $scope.tableOps.slbVs.showOperations = false;
        $scope.tableOps.slbServer.showMoreColumns = false;
        $scope.tableOps.slbVs.showMoreColumns = false;
    };
    H.addListener("slbInfoApp", $scope, $scope.hashChanged);
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

    function diffTwoSlb(targetDiv, baseText, newText, baseVersion, newVersion) {
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
angular.bootstrap(document.getElementById("slb-info-area"), ['slbInfoApp']);

var summaryInfoApp = angular.module('summaryInfoApp', ['http-auth-interceptor', 'angucomplete-alt']);
summaryInfoApp.controller('summaryController', function ($scope, $http, $q) {
    $scope.query = {};
    // Auto complete
    $scope.cacheRequestFn = function (str) {
        return {q: str, timestamp: new Date().getTime()};
    };
    $scope.remoteUrl = function () {
        return G.baseUrl + "/api/meta/slbs";
    };
    $scope.selectTarget = function (t) {
        if (t) {
            var toId = t.originalObject.id;
            if ($scope.target.id != toId) {
                $scope.$broadcast('angucomplete-alt:clearInput', 'targetSelector');
                var pairs = {};
                pairs['slbId'] = toId;
                $scope.target.id = toId;
                H.setData(pairs);
                messageNotify("切换SLB. ", "成功切换至SLB： " + toId, null);
            }
        }
    };
    $scope.clickTarget = function () {
        $('#targetSelector_value').css('width', '250px');
    };
    $scope.setInputsDisplay = function () {
        $('#targetSelector_value').val($scope.query.slbId);
    };
    $scope.getAllTargets = function () {
        $http.get(G.baseUrl + "/api/slb?slbId=" + $scope.query.slbId + "&type=info").success(
            function (res) {
                $scope.target = {};
                $scope.target.name = $scope.query.slbId + "/" + res.name;
                $('#targetSelector_value').val($scope.target.name);
            }
        );
    };
    $scope.hashChanged = function (hashData) {
        $scope.resource = H.resource;
        if (hashData.env) {
            $scope.env = hashData.env;
        }
        if (hashData.slbId) {
            $scope.query.slbId = hashData.slbId;
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
                link = "/portal/slb#?env=" + G.env + "&slbId=" + $scope.query.slbId;
                break;
            }
            case 'log': {
                link = "/portal/slb/log#?env=" + G.env + "&slbId=" + $scope.query.slbId;
                break;
            }
            case 'rule': {
                link = "/portal/slb/rule#?env=" + G.env + "&slbId=" + $scope.query.slbId;
                break;
            }
            case 'traffic': {
                link = "/portal/slb/traffic#?env=" + G.env + "&slbId=" + $scope.query.slbId;
                break;
            }
            case 'conf': {
                link = "/portal/slb/conf#?env=" + G.env + "&slbId=" + $scope.query.slbId;
                break;
            }
            case 'intercept': {
                link = "/portal/slb/intercept#?env=" + G.env + "&slbId=" + $scope.query.slbId;
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
        if (hashData.slbId) {
            $scope.query.slbId = hashData.slbId;
        }
    };
    H.addListener("infoLinksApp", $scope, $scope.hashChanged);
});
angular.bootstrap(document.getElementById("header-area"), ['headerInfoApp']);