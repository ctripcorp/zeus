//GroupInfoComponent: show a group info
var groupInfoApp = angular.module('groupInfoApp', ['angucomplete-alt', 'ui.bootstrap', 'ui.bootstrap.datetimepicker', 'http-auth-interceptor']);
groupInfoApp.controller('groupInfoController', function ($scope, $http, $q) {
    var timeout = 120;
    var log_start = new Date().getTime() - 1000 * 60 * 60;
    var log_end = new Date().getTime() + 1000 * 60 * 60;
    var dashboardStartTime = $.format.date(log_start, 'yyyy-MM-dd HH:mm:ss');
    var dashboardEndTime = $.format.date(log_end, 'yyyy-MM-dd HH:mm:ss');

    var unknownText = "未知";
    var start = "~* ^";
    var end = "($|/|\\?)";
    $scope.namePrefix = '@N_';

    var appId;
    $scope.currentUrl = window.location.href;
    $scope.query = {
        cmsGroupId: '',
        groupId: '',
        groupName: '',
        showextended: false,
        user: '',
        email: '',
        right: ''
    };

    $scope.tableOps = {
        health: {
            showMoreColumns: false,
            showOperations: false
        },
        virtualServer: {
            showMoreColumns: false,
            showOperations: false
        }
    };
    $scope.information = {
        users: {},
        vses: {},
        apps: {},
        groups: {},
        currentlogs: [],
        'slbs': {},
        'onlineGroup': {},
        'offlineGroup': {},
        'extendedGroup': {},
        'group-virtual-servers': [],
        'appdata': {},
        'v-groups': [],
        policies: []
    };
    $scope.chart = {
        'startTime': '',
        'endTime': ''
    };
    $scope.data = {
        group_ops: [
            {id: 'ACTIVATE', name: '激活 GROUP 上线'},
            {id: 'DEACTIVATE', name: '下线 GROUP'},
            {id: 'UPDATE', name: '更新GROUP属性'},
            {id: 'DELETE', name: '删除GROUP'},
            {id: 'READ', name: '读权限'},

            {id: 'PROPERTY', name: '给GROUP 打Tag或者Property'},

            {id: 'OP_MEMBER', name: '实例拉入、拉出'},
            {id: 'OP_PULL', name: '发布拉入、拉出'},
            {id: 'OP_HEALTH_CHECK', name: '健康检测拉入、拉出'}
        ]
    };
    // Partial show more and show ops
    $scope.disableOpenHealth = function () {
        var can = A.canDo('Group', 'UPDATE', $scope.information.extendedGroup.id);
        return !can;
    };
    $scope.getHealthShowMore = function () {
        return $scope.tableOps.health.showMoreColumns ? 'fa fa-list-alt text-info' : 'fa fa-list-alt font-gray';
    };
    $scope.getHealthShowOperation = function () {
        return $scope.tableOps.health.showOperations ? 'fa fa-wrench text-info' : 'fa fa-wrench font-gray';
    };
    /*  $scope.toggleShowMoreHealthColumns = function () {
          $scope.tableOps.health.showMoreColumns = !$scope.tableOps.health.showMoreColumns;
          if ($scope.tableOps.health.showMoreColumns) {
              $('#group-health-table').bootstrapTable('showColumn', 'passes');
              $('#group-health-table').bootstrapTable('showColumn', 'fails');
          } else {
              $('#group-health-table').bootstrapTable('hideColumn', 'passes');
              $('#group-health-table').bootstrapTable('hideColumn', 'fails');
          }
      };*/
    $scope.toggleShowHealthOperations = function () {
        var can = A.canDo('Group', 'UPDATE', $scope.information.extendedGroup.id);
        if (can) {
            $scope.tableOps.health.showOperations = !$scope.tableOps.health.showOperations;
            if ($scope.tableOps.health.showOperations)
                $('#group-health-table').bootstrapTable('showColumn', 'operate');
            else
                $('#group-health-table').bootstrapTable('hideColumn', 'operate');
        } else {
            // this dismiss
            var accessLink = '<a class="right-bt-access" href="/portal/group#?env=' + G.env + '&right=update&groupId=' + $scope.query.groupId + '&timeStamp=' + new Date().getTime() + '">点此申请</a>';
            rightNotify('<strong>Oops ...</strong>', '您还没有修改健康检测的权限。 请 ' + accessLink, '');
        }
    };
    $scope.getHealthOperationTitle = function () {
        return $scope.tableOps.health.showOperations ? '关闭操作' : '打开操作';
    };
    $scope.getHealthShowMoreTitle = function () {
        return $scope.tableOps.health.showOperations ? '简略' : '详细';
    };

    $scope.disableOpenVirtualServer = function () {
        var can = A.canDo('Group', 'UPDATE', $scope.information.extendedGroup.id);
        return !can;
    };
    $scope.getVirtualServerShowMore = function () {
        return $scope.tableOps.virtualServer.showMoreColumns ? 'fa fa-list-alt text-info' : 'fa fa-list-alt font-gray';
    };
    $scope.getVirtualServerShowOperation = function () {
        return $scope.tableOps.virtualServer.showOperations ? 'fa fa-wrench text-info' : 'fa fa-wrench font-gray';
    };
    $scope.toggleShowMoreVirtualServerColumns = function () {
        $scope.tableOps.virtualServer.showMoreColumns = !$scope.tableOps.virtualServer.showMoreColumns;
        if ($scope.tableOps.virtualServer.showMoreColumns) {
            $('#group-virtual-server-table').bootstrapTable('showColumn', 'name');
            $('#group-virtual-server-table').bootstrapTable('showColumn', 'rewrite');
            $('#group-virtual-server-table').bootstrapTable('showColumn', 'routes');
            $('#group-virtual-server-table').bootstrapTable('showColumn', 'slbvips');
            $('#group-virtual-server-table').bootstrapTable('showColumn', 'port');
        } else {
            $('#group-virtual-server-table').bootstrapTable('hideColumn', 'name');
            $('#group-virtual-server-table').bootstrapTable('hideColumn', 'rewrite');
            $('#group-virtual-server-table').bootstrapTable('hideColumn', 'routes');
            $('#group-virtual-server-table').bootstrapTable('hideColumn', 'slbvips');
            $('#group-virtual-server-table').bootstrapTable('hideColumn', 'port');
        }
    };
    $scope.toggleShowVirtualServerOperations = function () {
        var can = A.canDo('Group', 'UPDATE', $scope.information.extendedGroup.id);
        if (can) {
            $scope.tableOps.virtualServer.showOperations = !$scope.tableOps.virtualServer.showOperations;
            if ($scope.tableOps.virtualServer.showOperations)
                $('#group-virtual-server-table').bootstrapTable('showColumn', 'operate');
            else
                $('#group-virtual-server-table').bootstrapTable('hideColumn', 'operate');
        } else {
            // this dismiss
            var accessLink = '<a class="right-bt-access" href="/portal/group#?env=' + G.env + '&right=update&groupId=' + $scope.query.groupId + '&timeStamp=' + new Date().getTime() + '">点此申请</a>';
            rightNotify('<strong>Oops ...</strong>', '您还没有修改域名的权限。 请 ' + accessLink, '');
        }
    };
    $scope.getVirtualServerOperationTitle = function () {
        return $scope.tableOps.virtualServer.showOperations ? '关闭操作' : '打开操作';
    };
    $scope.getVirtualServerShowMoreTitle = function () {
        return $scope.tableOps.virtualServer.showOperations ? '显示简略信息' : '显示详细信息';
    };

    // Rights Area
    $scope.showNewVGroupBt = function () {
        return A.canDo("Group", "UPDATE", $scope.information.extendedGroup.id) && $scope.query.showextended;
    }
    $scope.showCloneBt = function () {
        return A.canDo("Group", "UPDATE", $scope.information.extendedGroup.id) && $scope.query.showextended;
    };
    $scope.showUpdateBt = function () {
        return A.canDo("Group", "UPDATE", $scope.information.extendedGroup.id) && $scope.query.showextended;
    };
    $scope.showActivateBt = function () {
        return A.canDo("Group", "ACTIVATE", $scope.information.extendedGroup.id) && $scope.query.showextended;
    };
    $scope.showDeactivateBt = function () {
        return A.canDo("Group", "DEACTIVATE", $scope.information.extendedGroup.id) && $scope.query.showextended;
    };
    $scope.showDeleteBt = function () {
        return A.canDo("Group", "DELETE", $scope.information.extendedGroup.id) && $scope.query.showextended;
    };
    $scope.showNewPolicyBt = function () {
        return A.canDo("Group", "UPDATE", $scope.query.groupId) && $scope.query.showextended;
    };
    $scope.showNewPolicyLink = function () {
        return A.canDo("Group", "UPDATE", $scope.query.groupId);
    };
    $scope.showNewDrLink = function () {
        return A.canDo("Group", "UPDATE", $scope.query.groupId);
    };

    $scope.showPolicy = function () {
        return $scope.information.policies && $scope.information.policies.length > 0;
    };
    $scope.showDr = function () {
        return $scope.information.drs && _.keys($scope.information.drs).length > 0;
    };
    $scope.showVGroupNewBt = function () {
        return A.canDo("Group", "UPDATE", $scope.information.extendedGroup.id) && $scope.query.showextended;
    };
    $scope.showNewBindBt = function () {
        var wrenchOp = A.canDo("Group", "UPDATE", $scope.information.extendedGroup.id) && $scope.tableOps.virtualServer.showOperations;
        if (!wrenchOp) return false;
        else return true;
    };
    $scope.showRemoveBindBt = function () {
        var wrenchOp = A.canDo("Group", "UPDATE", $scope.information.extendedGroup.id) && $scope.tableOps.virtualServer.showOperations;
        if (!wrenchOp) return false;
        else return true;
    };
    $scope.showAddTagBt = function () {
        return A.canDo("Group", "PROPERTY", $scope.information.extendedGroup.id) && $scope.query.showextended;
    };
    $scope.showAddProperty = function () {
        return A.canDo("Group", "PROPERTY", $scope.information.extendedGroup.id) && $scope.query.showextended;
    };
    $scope.showRemoveTagBt = function (v) {
        v = v.toLowerCase();
        if (v.startWith('owner') || v == 'activate' || v == 'deactive') return false;
        return A.canDo("Group", "PROPERTY", $scope.information.extendedGroup.id) && $scope.query.showextended;
    };
    $scope.showRemoveProperty = function (v) {
        v = v.name.toLowerCase();

        if (v == "apptype" || v == "idc" || v == "zone" || v == "sbu" || v == "status" || v == "healthy" || v == 'serverhealthy' || v == 'memberhealthy' || v == 'pullhealthy' || v == 'healthcheckhealthy') return false;
        return A.canDo("Group", "PROPERTY", $scope.information.extendedGroup.id) && $scope.query.showextended;
    };
    $scope.showLogs = function () {
        return $scope.information.currentlogs.length > 0;
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

    // Auto-Complete Area
    $scope.cacheRequestFn = function (str) {
        return {q: str, timestamp: new Date().getTime()};
    };
    $scope.remoteUrl = function () {
        return G.baseUrl + "/api/meta/groups";
    };
    $scope.remoteVsesUrl = function () {
        return G.baseUrl + "/api/meta/vses";
    };
    $scope.selectVsId = function (o) {
        if (o) {
            this.$parent.col['vs-id'] = o.originalObject.id;
            $('#vs0').val(o.originalObject.id);
        } else {
        }
    };

    // Top bar management Area
    $scope.disableDeactivateGroup = function () {
        var status = $scope.getGroupStatusProperty();
        if (status == "deactivated") {
            return true;
        }
        return false;
    };
    $scope.disableRemoveGroup = function () {
        var status = $scope.getGroupStatusProperty();
        if (status == "activated" || status == "tobeactivated") {
            return true;
        }
        return false;
    };

    $scope.activateGroupTitleClass = function () {
        try {
            if ($scope.information.onlineGroup.data.version != undefined && $scope.information.onlineGroup.data.version == $scope.information.extendedGroup.version) {
                return "status-red-important";
            }
        } catch (e) {

        }
    };
    $scope.activateGroupClick = function () {
        var resource = $scope.resource;
        if (!resource || _.keys(resource).length == 0) return;
        $scope.confirmActivateText = resource['slb']['slb_slbInfoApp_activateSLBModal']['线上版本与当前版本比较'];

        if ($scope.information.onlineGroup.data.version != undefined && $scope.information.onlineGroup.data.version == $scope.information.extendedGroup.version) {
            $scope.confirmActivateText = resource['slb']['slb_slbInfoApp_activateSLBModal']["线上已是最新版本,确认是否强制重新激活"];
        }
        var baseText = JSON.stringify(U.sortObjectFileds($scope.information.onlineGroup.data), null, "\t");
        var newText = JSON.stringify(U.sortObjectFileds($scope.information.offlineGroup), null, "\t");
        var baseVersion = resource['slb']['slb_slbInfoApp_activateSLBModal']["线上Group版本"] + '(Version: ' + $scope.information.onlineGroup.data.version + ")";
        var newVersion = resource['slb']['slb_slbInfoApp_activateSLBModal']["更新后Group版本"] + '(Version:' + $scope.information.extendedGroup.version + ")";
        var diffoutputdiv = document.getElementById("diffOutput");
        diffTwoGroup(diffoutputdiv, baseText, newText, baseVersion, newVersion);

        $('#activateGroupModal').modal('show');
    };
    $scope.activateGroup = function () {
        var resource = $scope.resource;
        if (!resource || _.keys(resource).length == 0) return;

        $('#operationConfrimModel').modal("show").find(".modal-title").html(resource['slb']['slb_slbInfoApp_activateSLBModal']["激活Group"]);
        $('#operationConfrimModel').modal("show").find(".modal-body").html(resource['slb']['slb_slbInfoApp_activateSLBModal']["正在激活"] + ".. <img src='/static/img/spinner.gif' />");
        var param = {
            groupId: $scope.information.extendedGroup.id
        };
        var req = {
            method: 'GET',
            url: G.baseUrl + "/api/activate/group?description=" + $scope.query.user,
            params: param
        };
        $scope.processRequest(req, $('#operationConfrimModel'), resource['slb']['slb_slbInfoApp_activateSLBModal']["激活Group"], resource['slb']['slb_slbInfoApp_activateSLBModal']["激活成功"]);
    };

    $scope.forceActivateGroup = function () {
        var resource = $scope.resource;
        if (!resource || _.keys(resource).length == 0) return;
        $scope.showForceUpdate = false;
        $('#operationConfrimModel').modal("show").find(".modal-body").html(resource['slb']['slb_slbInfoApp_activateSLBModal']["正在Force激活"] + ".. <img src='/static/img/spinner.gif' />");
        $scope.processRequest($scope.forceUpdateRequest, $('#operationConfrimModel'), resource['slb']['slb_slbInfoApp_activateSLBModal']["激活Group"],
            resource['slb']['slb_slbInfoApp_activateSLBModal']["激活成功"]);
    };
    $scope.activateBtShow = function () {
        return A.canDo("Group", "ACTIVATE", $scope.information.extendedGroup.id);
    };

    $scope.deactivateGroupAction = function () {
        var status = $scope.getGroupStatusProperty();
        if (status != "deactivated") {
            $scope.loadAllCharts($('#deactivateActivatedGroupModal'));
            $('#deactivateActivatedGroupModal').modal('toggle');
        }
    };
    $scope.deactivateGroup = function () {
        var resource = $scope.resource;
        if (!resource || _.keys(resource).length == 0) return;

        var loading = "<img src='/static/img/spinner.gif' /> " + resource['slb']['slb_slbInfoApp_operationConfrimModel']["正在下线"];
        $('#operationConfrimModel').modal('show').find(".modal-body").html(loading);
        var param = {
            groupId: $scope.information.extendedGroup.id
        };
        var request = {
            method: 'GET',
            url: G.baseUrl + "/api/deactivate/group?description=" + encodeURIComponent($scope.query.deactivatereason),
            params: param
        };
        $('#deactivateGroupModal').modal('hide');
        $scope.processRequest(request, $('#operationConfrimModel'), resource['slb']['slb_slbInfoApp_operationConfrimModel']["下线Group"],
            resource['slb']['slb_slbInfoApp_operationConfrimModel']["下线成功"]
        );
    };
    $scope.deleteGroupAction = function () {
        var status = $scope.getGroupStatusProperty();
        if (status == "deactivated") {
            $('#deleteGroupModal').modal('show');
        }
    };
    $scope.deleteGroup = function () {
        var groupId = $scope.information.extendedGroup.id;

        var param = {
            groupId: groupId
        };
        var request = {
            method: 'GET',
            url: G.baseUrl + "/api/group/delete?description=" + $scope.query.deletereason,
            params: param
        };
        $scope.processRequest(request, $('#deleteGroupConfirmModel'), "删除Group", "删除成功");
    };

    $scope.getIdc = function (idc) {
        return G['idcs'][idc] || idc;
    };

    var helpDoc = {
        "idc": {
            "title": '名词解释',
            "content": '当前访问入口所在IDC.'
        },
        "idc_code": {
            "title": '名词解释',
            "content": 'Group机器实际部署在的IDC'
        }
    };
    $scope.viewDoc = function (type) {

    };

    $scope.showLinks = function (type) {
        var env = $scope.env;
        if (env && G[env].urls[type]) return true;
        return false;
    };
    //WEBINFO,HICKWALL,CAT links
    $scope.generateMonitorUrl = function (type) {
        var startTime = new Date().getTime() - 60 * 1000;
        var endTime = new Date().getTime();
        startTime = $.format.date(startTime, 'yyyy-MM-dd_HH:mm:ss');
        endTime = $.format.date(endTime, 'yyyy-MM-dd_HH:mm:ss');

        var appId = $scope.information.extendedGroup['app-id'];
        var properties = $scope.information.extendedGroup['properties'];
        var slbProperties = _.indexBy(properties, function (v) {
            return v.name.toLowerCase();
        });
        var cmsGroupId = slbProperties['cmsgroupid'] ? slbProperties['cmsgroupid'].value : '';

        switch (type) {
            case 'hickwall':
                return G[$scope.env] ? G[$scope.env].urls.hickwall + '/d/Cah2BxgWz/slb-group?var-group_id=' + $scope.information.extendedGroup.id : '';
            case 'cat':
                return G[$scope.env] ? G[$scope.env].urls.cat + '/cat/r/t?op=view&domain=' + $scope.information.extendedGroup['app-id'] : '';
            case 'webinfo': {
                var e = G[$scope.env];
                if (e && e.urls.webinfo && cmsGroupId) {
                    return e.urls.webinfo + '/#/relation/group/' + cmsGroupId;
                } else {
                    return '/portal/404';
                }
            }
            case 'cms':
                return G[$scope.env] ? G[G.env].urls.cms + '/#/access-group/?q=%257B%22groupId%22:%257B%22value%22:%22' + cmsGroupId + '%22,%22isLike%22:false%257D%257D&orderby=%257B%257D' : '';
            case 'es': {
                if ($scope.env && G[$scope.env].urls.es) {
                    return G[$scope.env].urls.es + "?query=group_id%3D'" + $scope.information.extendedGroup.id + "'";
                    ;
                } else {
                    return '-';
                }
            }
            case 'clog': {
                if ($scope.env && G[$scope.env].urls.clog) {
                    return G[$scope.env].urls.clog + '?fromDate=' + startTime + '~toDate=' + endTime + '~app=' + appId;
                } else {
                    return '-';
                }
            }
            case 'dashboard': {
                var env = $scope.env;
                env = env == 'pro' ? 'PROD' : env.toUpperCase();
                var dashboard = G.dashboardportal + '/#env=' + env + '&metric-name=slb.req.count&interval=1m&start-time=' + dashboardStartTime + '&end-time=' + dashboardEndTime + '&chart=line&aggregator=sum&ts=1515983913677&tags={"group_id":["' + $scope.information.extendedGroup.id + '"]}&group-by=[status]';
                return dashboard;
            }
            case 'tengine': {
                var env = $scope.env;
                return G[env]["urls"]['tengine-es'] + "query=upstream_name%3D'backend_" + $scope.information.extendedGroup.id + "'";
            }
            default:
                return '';
        }
    };

    // Group Name
    $scope.getCdPath = function (appId) {
        var cdng = G.cd;
        var env = $scope.env;
        var prefix = "test";
        if (env == 'pro') {
            prefix = "prod";
        }
        return cdng + '#/app/' + appId + '/' + prefix + '/detail?env=' + prefix;
    };
    $scope.generateGroupNameUrl = function () {
        if ($scope.env) {
            return G[$scope.env].urls.webinfo + '/?Keyword=' + $scope.information.extendedGroup.name;
        } else {
            return '#';
        }
    };
    // Tagging
    $scope.showDialog = function (value, type) {
        switch (type) {
            case 'deleteTag' : {
                if ($scope.canDeleteTag(value)) {
                    $scope.currentTagName = value;
                    $('#deleteGroupTag').modal({backdrop: 'static'});
                }
                break;
            }
            case 'deleteProp' : {
                if ($scope.canDeleteProperty(value)) {
                    $scope.currentProperty = value;
                    $('#deleteGroupProperty').modal({backdrop: 'static'});
                }
                break;
            }
            case 'addTag' : {
                $('#addGroupTag').modal({backdrop: 'static'}).find("#tagNameInput").val("");
                break;
            }
            case 'addPolicy' : {
                window.location.href = '/portal/policy/new#?env=' + G.env + '&groupId=' + $scope.information.extendedGroup.id;
                break;
            }
            case 'addProp' : {
                $('#addGroupProp').modal({backdrop: 'static'}).find(":input").val("");
                break;
            }
        }
    };
    $scope.canDeleteProperty = function (p) {
        if (!$scope.query.showextended) return;
        var pName = p.name.toLowerCase();
        if (pName == "apptype" || pName == "idc" || pName == "zone" || pName == "status" || pName == "sbu" || pName == "healthy" || pName.startWith('owner') || pName == 'serverhealthy' || pName == 'memberhealthy' || pName == 'pullhealthy' || pName == 'healthcheckhealthy') {
            return false;
        }
        return true;
    };
    $scope.canDeleteTag = function (t) {
        if (!$scope.query.showextended) return;
        if (t == 'activate' || t == 'deactive' || t.startWith('owner')) return false;
        return true;
    };

    $scope.deleteTag = function (tagName, type) {
        var param = {
            type: 'group',
            tagName: tagName,
            targetId: $scope.information.extendedGroup.id
        };
        var request = {
            method: 'GET',
            url: G.baseUrl + "/api/untagging?description=" + $scope.query.deletetagreason,
            params: param
        };
        $('#deleteGroupTag').modal('hide');
        $scope.processRequest(request, $('#operationConfrimModel'), "删除Tag");
    };
    $scope.addTag = function (tagName, type) {
        if ($scope.information.extendedGroup.tags == undefined) {
            $scope.information.extendedGroup.tags = [];
        }
        var param = {
            type: 'group',
            tagName: tagName,
            targetId: $scope.information.extendedGroup.id
        };
        var request = {
            method: 'GET',
            url: G.baseUrl + "/api/tagging?description=" + $scope.query.tagreason,
            params: param
        };
        $('#addGroupTag').modal('hide');
        $scope.processRequest(request, $('#operationConfrimModel'), "添加Tag", "添加成功");
    };
    $('#addTagBt').click(function (e) {
        e.preventDefault();
        var validate = reviewData($('#addGroupTag'));
        if (!validate) return;
        $scope.addTag($('#tagNameInput').val().trim());
    });

    // Property
    $scope.addProperty = function (prop) {
        var pname = prop.name;
        var pvalue = prop.value;

        var existed = _.find($scope.information.extendedGroup.properties, function (item) {
            return item.name.toLowerCase() == pname.toLowerCase();
        });
        var param = {
            type: 'group',
            pname: pname,
            pvalue: pvalue,
            targetId: $scope.information.extendedGroup.id
        };
        var request = {
            method: 'GET',
            url: G.baseUrl + "/api/property/set?description=" + $scope.query.propertyreason,
            params: param
        };
        $('#addGroupProp').modal("hide");
        $scope.processRequest(request, $('#operationConfrimModel'), "添加Property", "添加成功");
    };
    $scope.deleteProperty = function (prop) {
        var pname = prop.name;
        var pvalue = prop.value;
        var param = {
            type: 'group',
            pname: pname,
            pvalue: pvalue,
            targetId: $scope.information.extendedGroup.id
        };
        var request = {
            method: 'GET',
            url: G.baseUrl + "/api/property/clear?description=" + $scope.query.deletepropertyreason,
            params: param
        };
        $('#deleteGroupProperty').modal('hide');
        $scope.processRequest(request, $('#operationConfrimModel'), "删除Property", "删除成功");
    };
    $('#addPropertyBt').click(function (e) {
        e.preventDefault();
        var validate = reviewData($('#addGroupProp'));
        if (!validate) return;
        $scope.addProperty({'name': $('#pname').val().trim(), 'value': $('#pvalue').val().trim()});
    });

    // Operation Panel
    $scope.toggleOptionPanel = function () {
        $scope.query.showextended = !$scope.query.showextended;
        var p = A.canDo("Group", "UPDATE", $scope.query.groupId) && $scope.query.showextended;
        if (p) {
            $scope.tableOps.virtualServer.showOperations = true;
            $scope.tableOps.health.showOperations = true;
            $('#group-virtual-server-table').bootstrapTable('showColumn', 'operate');
            $('#group-health-table').bootstrapTable('showColumn', 'operate');
            $('#group-vgroup-table').bootstrapTable('showColumn', 'operate');
        } else {
            $scope.tableOps.virtualServer.showOperations = false;
            $scope.tableOps.health.showOperations = false;

            $('#group-virtual-server-table').bootstrapTable('hideColumn', 'operate');
            $('#group-health-table').bootstrapTable('hideColumn', 'operate');
            $('#group-vgroup-table').bootstrapTable('hideColumn', 'operate');
        }
        $('#optionPanelBt').click();
    };
    $scope.applyBtClick = function () {
        $("#group-role-selector-table").bootstrapTable("uncheckAll");
        // check current user's right
        var resources = $scope.query.resource;
        resources = _.groupBy(resources, 'type');
        resources = resources['Group'];
        var groupAccesses = _.indexBy(resources[0]['data-resources'], 'data');
        var thisGroupAccess = groupAccesses[$scope.query.groupId];
        var allGroupAccess = groupAccesses['*'];
        var allAccesses = [];
        if (thisGroupAccess) {
            allAccesses = allAccesses.concat(thisGroupAccess.operations);
        }
        if (allGroupAccess) {
            allAccesses = allAccesses.concat(allGroupAccess.operations);
        }
        allAccesses = _.uniq(_.pluck(allAccesses, 'type'));
        var data = $scope.data.group_ops;

        var languageresource = $scope.resource;
        if (!languageresource || _.keys(languageresource).length == 0) return;

        data = _.map(data, function (v) {
            v.name = languageresource['group']['group_groupStatusApp_tables']['rights'][v.name] || v.name;

            if (allAccesses.indexOf(v.id) != -1) {
                v.checked = true;
            } else {
                v.checked = false;
            }
            return v;
        });

        $('#group-role-selector-table').bootstrapTable("load", data);
        $('#rightAccessDialog').modal('show');
    };
    $scope.applyUserRight = function () {
        // get those new added
        var selected = $("#group-role-selector-table").bootstrapTable("getSelections");

        var hasNew = _.filter(selected, function (v) {
            return v.checked == false;
        });

        if (hasNew && hasNew.length > 0) {
            // get all of the right user applyed
            var ops = _.pluck(hasNew, 'id');
            var op = ops.join(',');
            var mailLink = G.baseUrl + '/api/auth/apply/mail?userName=' + $scope.query.user + '&op=' + op + '&type=Group&targetId=' + $scope.query.groupId + '&env=' + $scope.env;
            var request = {
                url: mailLink,
                method: 'GET'
            };

            $http(request).success(function (response, code) {
                if (code == 200) {
                    // succeed sending the mail
                    $('#rightAccessConfirmDialog').modal('show');
                }
            });
        } else {
            return;
        }
    };
    $scope.getOptionPanelText = function () {
        var resource = $scope.resource;
        if (!resource || _.keys(resource).length == 0) return;
        if (!$scope.query.showextended) return resource['slb']['slb_slbInfoApp_opmap']["打开操作面板"];
        return resource['slb']['slb_slbInfoApp_opmap']["收起操作面板"];
    };
    $scope.getOptionPanelCss = function () {
        if (!$scope.query.showextended) {
            return "fa fa-arrows panel-close";
        } else {
            return "fa fa-arrows-alt panel-open";
        }
    };

    // Focus Area
    $scope.getFocusObject = function () {
        if ($scope.query == undefined) return undefined;
        var f = _.find($scope.information.extendedGroup.tags, function (item) {
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
        if (f == undefined) return "fa fa-eye-slash ";
        return "fa fa-eye ";
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
        var request = {
            method: 'GET',
            url: G.baseUrl + "/api/tagging?type=group&tagName=" + tagName + "&targetId=" + $scope.information.extendedGroup.id + "&description=focus"
        };
        $http(request).success(
            function (res) {
                $(target).hideLoading();
                $(target).html(resource['slb']['slb_slbInfoApp_opmap']["取消关注"]);
                $(target).parent('span').removeClass('fa fa-eye-slash status-unfocus').addClass("fa fa-eye ");
                if (!$scope.information.extendedGroup.tags) $scope.information.extendedGroup.tags = [];
                var f = _.find($scope.information.extendedGroup.tags, function (item) {
                    return item.trim().toLowerCase() == tagName.toLowerCase();
                });
                if (!f) {
                    $scope.information.extendedGroup.tags.push(tagName);
                }
            });

    };
    $scope.removeFocus = function (tagName, target) {
        var resource = $scope.resource;
        if (!resource || _.keys(resource).length == 0) return;
        $(target).showLoading();
        var request = {
            method: 'GET',
            url: G.baseUrl + "/api/untagging?type=group&tagName=" + tagName + "&targetId=" + $scope.information.extendedGroup.id + "&description=unfocus"
        };
        $http(request).success(
            function (res) {
                $(target).hideLoading();
                if (!$scope.information.extendedGroup.tags) $scope.information.extendedGroup.tags = [];
                $(target).html(resource['slb']['slb_slbInfoApp_opmap']["关注"]);
                $(target).parent('span').removeClass('fa fa-eye ').addClass("fa fa-eye-slash ");
                var index = $scope.information.extendedGroup.tags.indexOf(tagName);
                if (index != -1) {
                    $scope.information.extendedGroup.tags.splice(index, 1);
                }
            });
    };

    // Status area
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
    };
    $scope.statusText = function () {
        var resource = $scope.resource;
        if (!resource || _.keys(resource).length == 0) return;
        var s = $scope.getGroupStatusProperty();
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

        var s = $scope.getGroupStatusProperty();
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

    // Diff
    $scope.confirmDiffButtonDisable = true;
    $scope.targetDiffGroupId;
    $scope.selectDiffGroupId = function (t) {
        if (t) {
            var toId = t.originalObject.id;
            $scope.targetDiffGroupId = toId;
            $scope.confirmDiffButtonDisable = false;
        }
    };
    $scope.confirmSelectGroupId = function () {
        if ($scope.targetDiffGroupId) {
            // has selected the current targetId
            $scope.confirmActivateText = '当前Group Id' + $scope.information.extendedGroup.id + ". 对比Group Id:" + $scope.targetDiffGroupId;
            var diffoutputdiv = document.getElementById("diffOutput3");

            $http.get(G.baseUrl + "/api/group?groupId=" + $scope.targetDiffGroupId).success(
                function (group) {
                    var baseText = JSON.stringify(U.sortObjectFileds($scope.information.offlineGroup), null, "\t");
                    var newText = JSON.stringify(U.sortObjectFileds(group), null, "\t");
                    var baseVersion = '当前Group Id: ' + $scope.information.extendedGroup.id;
                    var newVersion = '对比Group Id:' + group.id;
                    diffTwoGroup(diffoutputdiv, baseText, newText, baseVersion, newVersion);

                    $('#diffSeperateGroups').modal('show');
                }
            );
        }
        else {
            alert("请选择要比较的Group");
        }
    }

    /**Basic Information Area
     * Version
     * History
     * VIPS
     * Tags and Properties
     * */
    $scope.showExtendedTag = function () {
        if ($scope.information.extendedGroup.tags == undefined) return false;
        var u_tag = _.find($scope.information.extendedGroup.tags, function (m) {
            return m.trim().toLowerCase().startWith("user_");
        });
        var p = u_tag && $scope.information.extendedGroup.tags.length == 1;
        var t = $scope.information.extendedGroup.tags == undefined || $scope.information.extendedGroup.tags.length == 0;
        if (t || p) return false;
        return true;
    }
    $scope.showExtendedProperty = function () {
        return !($scope.information.extendedGroup.properties == undefined || $scope.information.extendedGroup.properties.length == 0);
    };
    $scope.showVGroupRow = function () {
        return !($scope.information["v-groups"] == undefined || $scope.information["v-groups"].length == 0);
    };
    $scope.vGroupRowClass = function () {
        if ($scope.information["v-groups"] == undefined || $scope.information["v-groups"].length == 0) {
            return 'hide';
        }
    };
    $scope.getTagShowProperty = function (val) {
        if (!val) return false;

        if (val.toLowerCase().trim().startWith('user_')) {
            return false;
        }
        return true;
    };
    $scope.getBuLinkTarget = function (value) {
        return "/portal/groups#?env=" + G.env + "&groupBues=" + value;
    };
    $scope.getGroupStatusProperty = function () {
        var grpStatus = undefined;
        if (!$scope.information.extendedGroup.properties) return grpStatus;

        var p = _.find($scope.information.extendedGroup.properties, function (item) {
            return item.name.toLowerCase().trim() == "status";
        });
        if (p) {
            grpStatus = p.value.toLowerCase().trim();
        }
        return grpStatus;
    };
    $scope.groupVersionText = function () {
        var resource = $scope.resource;
        if (!resource || _.keys(resource).length == 0) return;

        var statusMap = resource['vs']['vs_vsInfoApp_status'];


        var s = $scope.getGroupStatusProperty();
        if (s) s = s.toLowerCase();

        var v = $scope.information.onlineGroup.data == undefined ? "unknownText" : $scope.information.onlineGroup.data.version;
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
    $scope.groupVersionClass = function () {
        var v = $scope.getGroupStatusProperty();
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
    $scope.showReleaseHistory = function () {
        return $scope.publishHistories.length > 0;
    }
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
    $scope.showDiffHistory = function () {
        return _.keys($scope.diffVersions).length != 2;
    };
    $scope.diffGroupBetweenVersions = function (compare) {
        var resource = $scope.resource;
        if (!resource || _.keys(resource).length == 0) return;


        var diffVersions = _.keys($scope.diffVersions);
        if (compare) {
            diffVersions = $scope.diffVersions;
        }

        if (diffVersions.length < 2) {
            alert("请选择两个版本进行diff");
        }
        var v = resource['slb']['slb_slbInfoApp_op']['版本号'];
        var vc = resource['slb']['slb_slbInfoApp_op']['变更前'];
        var va = resource['slb']['slb_slbInfoApp_op']['变更后'];

        $scope.confirmActivateText = v + ':' + diffVersions[0] + " VS " + v + ": " + diffVersions[1];
        if (compare) {
            $scope.confirmActivateText = vc + ':' + diffVersions[0] + " VS " + va + ": " + diffVersions[1];
        }
        $q.all(
            [
                $http.get(G.baseUrl + "/api/archive/group?groupId=" + $scope.query.groupId + "&version=" + diffVersions[0]).success(
                    function (res) {
                        $scope.groupVersion1 = res;
                    }
                ),
                $http.get(G.baseUrl + "/api/archive/group?groupId=" + $scope.query.groupId + "&version=" + diffVersions[1]).success(
                    function (res) {
                        $scope.groupVersion2 = res;
                    }
                )
            ]
        ).then(
            function () {
                var diffoutputdiv = document.getElementById("diffOutput3");
                var baseText = JSON.stringify(U.sortObjectFileds($scope.groupVersion1), null, "\t");
                var newText = JSON.stringify(U.sortObjectFileds($scope.groupVersion2), null, "\t");
                var baseVersion = v + ':' + diffVersions[0];
                var newVersion = v + ':' + diffVersions[1];
                diffTwoGroup(diffoutputdiv, baseText, newText, baseVersion, newVersion);

                $('#historyVersionModel').modal('hide');
                $('#diffSeperateGroups').modal('show');
            }
        );
    };
    $('#historyVersionBtn').click(
        function () {
            $scope.diffVersions = {};
            $scope.diffVersions[$scope.information.extendedGroup.version] = $scope.information.extendedGroup.version;

            $.each($scope.groupVersions, function (i, val) {
                if (val != $scope.information.extendedGroup.version)
                    $('#historyVersion' + val).removeClass('label-info');
            });
            $('#historyVersion' + $scope.information.extendedGroup.version).addClass('label-info');
            $('#historyVersionModel').modal('show');
        }
    );
    $('#group-virtual-server-table').on('check.bs.table uncheck.bs.table ' +
        'check-all.bs.table uncheck-all.bs.table all.bs.table', function () {
        $('#group-batch-delete-path-bt').prop('disabled', !$('#group-virtual-server-table').bootstrapTable('getSelections').length);
    });
    /* $('#group-policy-table').on('check.bs.table uncheck.bs.table ' +
     'check-all.bs.table uncheck-all.bs.table all.bs.table', function () {
     $('#group-batch-delete-policy-bt').prop('disabled', !$('#group-policy-table').bootstrapTable('getSelections').length);
     });*/
    var selectedVirtualServerObject;
    var selectedPolicyObject;
    window.OperationChangeEvent = {
        'click .deleteVirtualSererBt': function (e, value, row) {
            popRemoveVsFn(row);
        },
        'click .change-virtual-path': function (e, value, row) {
            selectedVirtualServerObject = row;
            $('#askEditVServerModal').find(".btn-info").click();
        },
        'click .remove-policy': function (e, value, row) {
            selectedPolicyObject = row;
            $('.to-be-removed-policy').html('');
            var str = '<tr><td>' + row.id + '</td><td>' + row.name + '</td></tr>';
            $('.to-be-removed-policy').html(str);
            $('#askRemovePolicyModal').modal('show');
        }
    };
    var selectedVGroupObject;
    window.vGroupOperationEvent = {
        'click .deactivate-vgroup-bt': function (e, value, row) {
            selectedVGroupObject = row;
            $('#askDeactivateVGroupModal').find(".btn-info").click();
        },
        'click .remove-vgroup-bt': function (e, value, row) {
            var str = "<tr>";
            str += "<td>" + row.id + "</td>";
            str += "<td>" + row.name + "</td>";
            str += "</tr>";
            selectedVGroupObject = row;
            $('#removeVGroupModal').modal("show").find('.to-be-removed-health').html(str);
        },
        'click .activate-vgroup-bt': function (e, value, row) {
            selectedVGroupObject = row;
        },
        'click .diffGroup': function (e, value, row) {
            selectedVGroupObject = row;
        }
    };
    window.healthCheckOperationEvent = {
        'click .remove-health-bt': function (e, value, row) {
            var str = "<tr>";
            var health = $scope.information.extendedGroup["health-check"];
            str += "<td>" + health.uri + "</td>";
            str += "<td>" + health.intervals + "</td>";
            str += "<td>" + health.fails + "</td>";
            str += "<td>" + health.passes + "</td>";
            str += "</tr>";

            $('#removeHealthCheckModal').modal("show").find('.to-be-removed-health').html(str);

        },
        'click .update-health-bt': function (e, value, row) {
            $scope.popNewHealthCheck();
        }
    };

    function pathFormatter(value, row, index, field) {
        if (!value) {
            return {};
        }
        var width = value.length * 20 + "px";
        return {
            css: {"color": "#196eaa;", "font-weight": "bold", "width": width, "word-break": "break-all"}
        };
    }

    function rewriteFormatter(value, row, index, field) {
        if (value) {
            var width = value.length * 20 + "px";
            return {
                css: {"width": width}
            };
        }
        else {
            return {
                css: {}
            }
        }
    }

    $scope.groupInfo = {};
    $scope.diffVersions = {};
    $scope.vgroups = [];
    // Load Group Data
    $scope.initTable = function () {
        var resource = $scope.resource;
        if (!resource || _.keys(resource).length == 0) return;

        var table1 = resource['group']['group_groupStatusApp_tables']['groupvs'];
        var table2 = resource['group']['group_groupStatusApp_tables']['grouphealth'];
        var table3 = resource['group']['group_groupStatusApp_tables']['groupvgroup'];
        var table4 = resource['group']['group_groupStatusApp_tables']['grouprole'];

        $('#group-virtual-server-table').bootstrapTable({
            toolbar: "#groupInfo-appList-toolbar",
            columns: [[
                {
                    checkbox: 'true',
                    align: 'center',
                    valign: 'middle'
                },
                {
                    field: 'vs',
                    title: table1['vs'],
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    formatter: function (value) {
                        return '<a target="_blank" href="' + '/portal/vs#?env=' + G.env + '&vsId=' + value + '">' + value + '</a>';
                    }
                },
                {
                    field: 'path',
                    title: table1['path'],
                    align: 'left',
                    valign: 'middle',
                    cellStyle: function (value, row, index, field) {
                        return pathFormatter(value, row, index, field);
                    },
                    sortable: true
                },
                {
                    field: 'name',
                    title: table1['name'],
                    align: 'left',
                    valign: 'middle',
                    visible: false,
                    sortable: true
                },
                {
                    field: 'port',
                    title: table1['port'],
                    align: 'left',
                    valign: 'middle',
                    visible: false
                },

                {
                    field: 'rewrite',
                    title: table1['rewrite'],
                    visible: false,
                    align: 'left',
                    valign: 'middle',
                    cellStyle: function (value, row, index, field) {
                        return rewriteFormatter(value, row, index, field);
                    },
                    formatter: function (value) {
                        if (value == undefined || value == "") return "-";
                        else return value;
                    },
                    sortable: true
                },
                {
                    field: 'routes',
                    title: table1['route'],
                    visible: false,
                    align: 'left',
                    valign: 'middle',
                    formatter: function (value, row, index, field) {


                        if (value == undefined || value == "") return "-";
                        else {
                            var headers = _.filter(value, function (v) {
                                return v.type.toLowerCase() == 'header';
                            });
                            // headers
                            var str = '<div>';
                            $.each(headers, function (i, item) {
                                var op = item.op.toLowerCase();
                                if (op == 'regex') op = '正则匹配';
                                str += '<div class=""><span style="font-weight: bold">Header: </span><span class="system-padding-right">' + item.key1 + '</span>' + op + '<span class="system-padding-left">' + item.value1 + '</span></div>';
                            });
                            str += '</div>';
                            return str;
                        }
                    },
                    sortable: true
                },
                {
                    field: 'priority',
                    title: table1['priority'],
                    class: 'shortCol',
                    align: 'left',
                    valign: 'middle',
                    sortable: true
                },
                {
                    field: 'ssl',
                    title: table1['ssl'],
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    formatter: function (v) {
                        return v ? 'https' : 'http';
                    }
                },
                {
                    field: 'slbvips',
                    title: table1['vip'],
                    align: 'left',
                    valign: 'middle',
                    visible: false,
                    formatter: function (value, row, index) {
                        var result = '';
                        $.each(value, function (i, v) {
                            result += '<div class="">';
                            result += "<div class='col-md-2' style='padding:0;line-height:34px;white-space: nowrap;overflow:hidden;color:#196eaa;'>";
                            result += '<a target="_blank" href="' + '/portal/slb#?env=' + G.env + '&slbId=' + v.slbid + '">' + v.slbid + '</a> ';
                            result += '</div>';
                            result += "<div class='col-md-10' style='padding:0;line-height:34px;white-space: nowrap;overflow:hidden;color:#196eaa;'>";
                            var vip = '(';
                            $.each(v.vips, function (j, k) {
                                if (j == v.vips.length - 1) {
                                    vip += '<a target="_blank" href="' + G[G.env].urls.webinfo + '?Keyword=' + k.ip + '">' + k.ip + '</a>';
                                } else {
                                    vip += '<a target="_blank" href="' + G[G.env].urls.webinfo + '?Keyword=' + k.ip + '">' + k.ip + '</a>,';
                                }
                            });
                            vip += ')';
                            result += vip + '</div>';
                            result += '</div>';
                        });
                        return result;
                    },
                    sortable: true
                },
                {
                    field: 'idc',
                    title: table1['idc'],
                    width: '100px',
                    align: 'left',
                    valign: 'middle'
                },
                {
                    field: 'domains',
                    title: table1['domains'],
                    align: 'left',
                    valign: 'middle',
                    width: '100px',
                    formatter: function (value, row) {
                        var str = "";
                        $.each(value, function (i, v) {
                            str += "<a title='" + v.name + "' target='_blank' href='/portal/vs#?env=" + G.env + "&vsId=" + row.vs + "'>" + v.name + "</a><br>";
                        });
                        return str;
                    }
                },
                {
                    field: 'operate',
                    width: '120px',
                    title: 'Operation',
                    align: 'center',
                    valign: 'middle',
                    sortable: true,
                    events: OperationChangeEvent,
                    formatter: function () {
                        var p1 = "";
                        if (!A.canDo("Group", "UPDATE", $scope.information.extendedGroup.id)) p1 = "hide";
                        var str = '<button data-toggle="tooltip" type="button" class="btn-op btn-little btn btn-info deleteVirtualSererBt  ' + p1 + '" title="删除" aria-label="Left Align"><span class="fa fa-minus"></span></button>';
                        str += '  <button data-toggle="tooltip" title="修改" type="button" class="btn-little btn-op btn btn-info change-virtual-path  ' + p1 + '" aria-label="Left Align">'
                            + '<span class="fa fa-edit"></span>' +
                            '</button>';
                        return str;
                    }
                },
                {
                    title: 'Links',
                    align: 'left',
                    valign: 'middle',
                    width: '350px',
                    formatter: function (value, row, index) {
                        var env = $scope.env;
                        env == 'pro' ? 'PROD' : env.toUpperCase();

                        var ssl = row.ssl ? 443 : 80;
                        var hasEs = G[env].urls['es'];
                        if (!hasEs) return '-';

                        var savedDomains = _.pluck(row.domains, 'name');
                        var domains = _.map(_.pluck(row.domains, 'name'), function (v) {
                            return "'" + v + "'";
                        });
                        domains = domains.join('%20OR%20');
                        var idc = row.idc.split(',')[0] || '-';

                        idc = groupApp.convertIDC(idc);
                        var query = "domain%3D(" + domains + ")%20AND%20idc%3D'" + idc + "'%20AND%20group_id%3D'" + $scope.information.offlineGroup.id + "'";
                        var esLink = groupApp.getEsHtml(query);


                        var nginxLink = G[env].urls['tengine-es'] +
                            "query=upstream_name%3D'backend_" + $scope.information.offlineGroup.id + "'%20AND%20domain%3D(" + domains + ")%20AND%20serverPort%3D'" + ssl + "'";

                        var nginx = '<div class="system-link">' +
                            '<a class="pull-left tengine" title="ES" target="_blank" href="' + nginxLink + '">NginxLog</a>' +
                            '</div>';

                        var str = '<div>';


                        var dashboard = G.dashboardportal + '/#env=' + env + '&metric-name=slb.req.count&interval=1m&start-time=' + dashboardStartTime + '&end-time=' + dashboardEndTime + '&chart=line&aggregator=sum&ts=1515983913677&tags={"group_id":["' + $scope.information.extendedGroup.id + '"], "vsid":["' + row.vs + '"]}&group-by=[status]';

                        if (G[env]['urls'].dashboard) {
                            str += '<div class="system-link">' +
                                '<a class="pull-left dashboard" title="Dashboard" target="_blank" href=\'' + dashboard + '\'>Dashboard</a>' +
                                '</div>';
                        }

                        if (G[env]['urls']['hickwall']) {
                            var hickwallLink = G[$scope.env] ? G[$scope.env].urls.hickwall + '/d/9Gw8BxgZk/slb-group-domain?orgId=6&var-group_id=' + $scope.information.extendedGroup.id + '&domain=' + savedDomains[0] : '';
                            str += '<div class="system-link">' +
                                '<a class="pull-left hickwall" title="Hickwall" target="_blank" href=\'' + hickwallLink + '\'>Hickwall</a>' +
                                '</div>';
                        }

                        if (G[env]['urls']['es']) {
                            str += '<div class="system-link">' +
                                esLink +
                                '</div>' +
                                '<div class="system-link">' +
                                nginx +
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
            data: $scope.information["group-virtual-servers"],
            classes: "table-bordered  table-hover table-striped table",
            minimumCountColumns: 2,
            pageSize: 20,
            sidePagination: 'client',
            pageList: [20, 40, 80, 200],
            resizable: true,
            resizeMode: 'overflow',
            responseHandler: "responseHandler",
            idField: 'ip',
            formatLoadingMessage: function () {
                return "<img class='loading-img' src='/static/img/loading_100.gif' /> " + table1['loading'];
            },
            formatNoMatches: function () {
                return '<i class="fa status-alert fa-exclamation-triangle" aria-hidden="true"></i> ' + table1['nodata'];
            }
        });
        $('#group-health-table').bootstrapTable({
            toolbar: "#groupInfo-health-toolbar",
            columns: [[
                {
                    field: 'uri',
                    title: 'URI',
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    formatter: function (value) {
                        return "<b style='color:#196eaa'>" + value + "</b>";
                    }
                },
                {
                    field: 'timeout',
                    class: 'shortCol',
                    title: 'Timeout',
                    align: 'left',
                    valign: 'middle'
                },
                {
                    field: 'intervals',
                    class: 'shortCol',
                    title: 'Intervals',
                    align: 'left',
                    valign: 'middle',
                    sortable: true
                },
                {
                    field: 'fails',
                    class: 'shortCol',
                    title: 'Fails',
                    align: 'left',
                    valign: 'middle',
                    sortable: true
                },
                {
                    field: 'passes',
                    class: 'shortCol',
                    title: 'Passes',
                    align: 'left',
                    valign: 'middle',
                    sortable: true
                },

                {
                    field: 'operate',
                    title: 'Operation',
                    width: '120px',
                    align: 'center',
                    valign: 'middle',
                    sortable: true,
                    events: healthCheckOperationEvent,
                    formatter: function (value, row, index, field) {
                        var p1 = "";
                        if (!A.canDo("Group", "UPDATE", $scope.information.extendedGroup.id)) p1 = "hide";
                        var str = '';
                        if (row.uri != '-') {
                            str += '<button type="button" class="btn btn-little btn-info remove-health-bt ' + p1 + '" aria-label="Left Align" ><span class="fa fa-minus"></span></button>';
                            str += '<button style="margin-left:5px" type="button" class="btn-little btn btn-info update-health-bt ' + p1 + '" aria-label="Left Align" ><span class="fa fa-edit"></span></button>';
                        } else {
                            str += '<button title="修改健康检测" style="" type="button" class="btn-little btn btn-info update-health-bt ' + p1 + '" aria-label="Left Align" ><span class="fa fa-edit"></span></button>';
                        }
                        return str;
                    }
                }
            ], []],
            search: true,
            showRefresh: true,
            showColumns: true,
            data: $scope.information.extendedGroup['health-check'],
            classes: "table-bordered  table-hover table-striped table",
            minimumCountColumns: 2,
            pageSize: 20,
            sidePagination: 'client',
            pageList: [20, 40, 80, 200],
            responseHandler: "responseHandler",
            idField: 'ip',
            formatLoadingMessage: function () {
                return "<img class='loading-img' src='/static/img/loading_100.gif' /> " + table2['loading'];
            },
            formatNoMatches: function () {
                return '<i class="fa status-alert fa-exclamation-triangle" aria-hidden="true"></i> ' + table2['nodata'];
            }
        });
        $('#group-vgroup-table').bootstrapTable({
            toolbar: "#groupInfo-vgroup-toolbar",
            columns: [[
                {
                    field: 'id',
                    title: 'ID',
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    formatter: function (value, row, index) {
                        var groupPageUrl = '/portal/vgroup#?env=' + G.env + '&groupId=' + value;
                        return '<a title="' + value + '" href="' + groupPageUrl + '">' + value + '</a>';
                    }
                },
                {
                    field: 'name',
                    title: 'Name',
                    align: 'left',
                    valign: 'middle',
                    formatter: function (value, row, index) {
                        var groupPageUrl = '/portal/vgroup#?env=' + G.env + '&groupId=' + row.id;
                        return '<a title="' + value + '" href="' + groupPageUrl + '"><div style="word-break: break-all">' + value + '</div></a>';
                    }
                }
            ], []],

            search: true,
            showRefresh: true,
            showColumns: true,
            data: $scope.information['v-groups'],
            classes: "table-bordered  table-hover table-striped table",
            minimumCountColumns: 2,
            pageSize: 20,
            sidePagination: 'client',
            pageList: [20, 40, 80, 200],
            responseHandler: "responseHandler",
            idField: 'ip',
            formatLoadingMessage: function () {
                return "<img class='loading-img' src='/static/img/loading_100.gif' /> " + table3['loading'];
            },
            formatNoMatches: function () {
                return '<i class="fa status-alert fa-exclamation-triangle" aria-hidden="true"></i> ' + table3['nodata'];
            }
        });
        $('#group-role-selector-table').bootstrapTable({
            search: false,
            showRefresh: false,
            showColumns: false,
            toolbar: "#group-role-selector-toolbar",
            columns: [[
                {
                    checkbox: 'true',
                    align: 'center',
                    valign: 'middle',
                    formatter: function (value, row, index) {
                        if (row.checked) {
                            return {disabled: true, checked: true}
                        } else {
                            return {disabled: false, checked: false}
                        }
                    }
                },
                {
                    field: 'id',
                    title: table4['权限'],
                    align: 'left',
                    valign: 'middle',
                    sortable: true
                },
                {
                    field: 'name',
                    title: table4['描述'],
                    align: 'left',
                    valign: 'middle'
                },
                {
                    field: 'checked',
                    title: '',
                    align: 'left',
                    valign: 'middle',
                    formatter: function (value, row, index) {
                        if (value) {
                            return '<span class="status-red">' + table4['已有权限'] + '</span>';
                        }
                        return '-';
                    }
                }
            ], []],
            data: $scope.data.group_ops,
            classes: "table-bordered  table-hover table-striped table",
            minimumCountColumns: 2,
            pageSize: 20,
            sidePagination: 'client',
            pageList: [20, 40, 80, 200],
            resizable: true,
            resizeMode: 'overflow',
            responseHandler: "responseHandler",
            idField: 'ip',
            formatLoadingMessage: function () {
                return "<img class='loading-img' src='/static/img/loading_100.gif' /> " + table4['loading'];
            },
            formatNoMatches: function () {
                return '<i class="fa status-alert fa-exclamation-triangle" aria-hidden="true"></i> ' + table4['nodata'];
            }
        });
    };
    $scope.initRequestHeaders = function (headers) {
        $('#vs-edit-headers').bootstrapTable({
            toolbar: ".vs-edit-headers-tool",
            columns: [[
                {
                    checkbox: 'true',
                    align: 'center',
                    valign: 'middle'
                },
                {
                    field: 'key',
                    title: 'Key',
                    align: 'left',
                    valign: 'middle',
                    sortable: true
                },
                {
                    field: 'op',
                    title: 'Operator',
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    formatter: function (value, row, index) {
                        value = value.toLowerCase();
                        if (value == 'regex') return '正则匹配';
                    }
                },

                {
                    field: 'value',
                    title: 'Value',
                    align: 'left',
                    valign: 'middle',
                    sortable: true
                },
                {
                    field: 'description',
                    title: 'Description',
                    align: 'left',
                    valign: 'middle',
                    formatter: function (value, row, index) {
                        var op = row.op.toLowerCase();
                        if (op == 'regex') op = '正则匹配';
                        var str = row.key + ' ' + op + ' ' + row.value;
                        if (!value) {
                            return str;
                        }
                        return value;
                    }
                },
                {
                    title: 'Operation',
                    align: 'center',
                    valign: 'middle',
                    width: '100px',
                    events: headerEvents,
                    formatter: function (value, row, index) {
                        var str = '<button class="btn btn-xs btn-info edit-header" style="margin-right: 5px"><i class="fa fa-edit"></i></button>' +
                            '<button class="system-margin-left btn btn-xs btn-info remove-header"><i class="fa fa-minus"></i></button>';
                        return str;
                    }
                }
            ], []],
            classes: "table-bordered  table-hover table-striped table",
            minimumCountColumns: 2,
            pageSize: 20,
            data: headers,
            sidePagination: 'client',
            pageList: [20, 40, 80, 200],
            resizable: true,
            resizeMode: 'overflow',
            responseHandler: "responseHandler",
            idField: 'key',
            formatLoadingMessage: function () {
                return "<img class='loading-img' src='/static/img/loading_100.gif' /> 正在加载 Header 信息";
            },
            formatNoMatches: function () {
                return '<i class="fa status-alert fa-exclamation-triangle" aria-hidden="true"></i> 没有找到匹配的 Headers';
            }
        });
        $('#vs-edit-headers').on('check.bs.table uncheck.bs.table ' +
            'check-all.bs.table uncheck-all.bs.table all.bs.table', function () {
            $('#header-remove-bt').prop('disabled', !$('#vs-edit-headers').bootstrapTable('getSelections').length);
        });
    };
    $scope.initNewRequestHeaders = function (headers) {
        $('#vs-add-headers').bootstrapTable({
            toolbar: ".vs-add-headers-tool",
            columns: [[
                {
                    checkbox: 'true',
                    align: 'center',
                    valign: 'middle'
                },
                {
                    field: 'key',
                    title: 'Key',
                    align: 'left',
                    valign: 'middle',
                    sortable: true
                },
                {
                    field: 'op',
                    title: 'Operator',
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    formatter: function (value, row, index) {
                        value = value.toLowerCase();
                        if (value == 'regex') return '正则匹配';
                    }
                },

                {
                    field: 'value',
                    title: 'Value',
                    align: 'left',
                    valign: 'middle',
                    sortable: true
                },
                {
                    field: 'description',
                    title: 'Description',
                    align: 'left',
                    valign: 'middle',
                    formatter: function (value, row, index) {
                        var op = row.op.toLowerCase();
                        if (op == 'regex') op = '正则匹配';
                        var str = row.key + ' ' + op + ' ' + row.value;
                        if (!value) {
                            return str;
                        }
                        return value;
                    }
                },
                {
                    title: 'Operation',
                    align: 'center',
                    valign: 'middle',
                    width: '100px',
                    events: headerEvents,
                    formatter: function (value, row, index) {
                        var str = '<button class="btn btn-xs btn-info add-edit-header" style="margin-right: 5px"><i class="fa fa-edit"></i></button>' +
                            '<button class="system-margin-left btn btn-xs btn-info add-remove-header"><i class="fa fa-minus"></i></button>';
                        return str;
                    }
                }
            ], []],
            classes: "table-bordered  table-hover table-striped table",
            minimumCountColumns: 2,
            pageSize: 20,
            data: headers,
            sidePagination: 'client',
            pageList: [20, 40, 80, 200],
            resizable: true,
            resizeMode: 'overflow',
            responseHandler: "responseHandler",
            idField: 'key',
            formatLoadingMessage: function () {
                return "<img class='loading-img' src='/static/img/loading_100.gif' /> 正在加载 Header 信息";
            },
            formatNoMatches: function () {
                return '<i class="fa status-alert fa-exclamation-triangle" aria-hidden="true"></i> 没有找到匹配的 Headers';
            }
        });
        $('#vs-add-headers').on('check.bs.table uncheck.bs.table ' +
            'check-all.bs.table uncheck-all.bs.table all.bs.table', function () {
            $('#add-header-remove-bt').prop('disabled', !$('#vs-add-headers').bootstrapTable('getSelections').length);
        });
    };
    window.headerEvents = {
        'click .remove-header': function (e, value, row) {
            var key = row.key.toLowerCase();
            var vsId = $scope.editVs.selectedVsId;
            $.each($scope.groupVirtualServersTable.columns, function (i, v) {
                if (v['vs-id'] == vsId.toString()) {
                    v.routes = _.reject(v.routes, function (s) {
                        return s.key.toLowerCase() == key;
                    });
                    $('#vs-edit-headers').bootstrapTable('load', v.routes);
                }
            });
        },
        'click .edit-header': function (e, value, row) {
            $('#error-msg-edit-advanced').html('');
            $scope.editVs.headertype = 'header';
            $scope.editVs.isNewHeader = false;
            $scope.editVs.headerKey = row.key;
            var value = row.value;
            var sValue = value.match(/\^(.*)\$/i);

            $scope.$apply(function () {
                $scope.editVs.options = [
                    {
                        type: 'Header',
                        ops: [
                            '正则匹配'
                        ],
                        selectedop: '正则匹配',
                        key: row.key,
                        value: sValue ? sValue[1] : value
                    }
                ];
            });
            $('#newVsAdvancedHeaderOptionModel').modal('show');
        },
        'click .add-remove-header': function (e, value, row) {
            var key = row.key.toLowerCase();
            var vs = $scope.groupVirtualServersTable.columns;
            var routes = _.reject(vs[0].routes, function (s) {
                return s.key.toLowerCase() == key;
            });
            vs[0].routes = routes;
            $('#vs-add-headers').bootstrapTable('load', routes);
        },
        'click .add-edit-header': function (e, value, row) {
            $('#error-msg-new-advanced').html('');

            $scope.addVs.headertype = 'header';
            $scope.addVs.isNewHeader = false;
            $scope.addVs.headerKey = row.key;
            var value = row.value;
            var sValue = value.match(/\^(.*)\$/i);
            $scope.$apply(function () {
                $scope.addVs.options = [
                    {
                        type: 'Header',
                        ops: [
                            '正则匹配'
                        ],
                        selectedop: '正则匹配',
                        key: row.key,
                        value: sValue ? sValue[1] : value
                    }
                ];
            });
            $('#newAddVsAdvancedHeaderOptionModel').modal('show');
        }
    };

    window.DiffVersionEvent = {
        'click .diff-version-bt': function (e, value, row) {
            getGroupDataByVersion(row);
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
            var reason = '-';
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
                    reason = t['description'];
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
                ' <td><span style="word-break: break-all">' + op_param + '</span></td>' +
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
                ' <tr><td><b>操作原因:</b></td>' +
                ' <td><span style="word-break: break-all">' + reason + '</span></td>' +
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

    function versionChanged(change) {
        var result = false;
        switch (change) {
            case "deactivate":
                result = true;
                break;
            case "activate":
                result = true;
                break;
            case "update":
                result = true;
                break;
            case "bindVs":
                result = true;
                break;
            case "unbindVs":
                result = true;
                break;
            case "addMember":
                result = true;
                break;
            case "updateMember":
                result = true;
                break;
            case "removeMember":
                result = true;
                break;
            case "removeMember":
                result = true;
                break;
        }
        return result;
    }

    function getGroupDataByVersion(row) {
        var currentVersion = row['group-version'];
        var id = row['target-id'];

        var c = currentVersion;
        var p = currentVersion - 1;

        if (row['operation'] == 'activate') {
            var gd = JSON.parse(row['data']);
            var gd_datas = gd['group-datas'];
            var gd_sort = _.sortBy(gd_datas, 'version');
            p = gd_sort[0].version;
        }

        var param0 = {
            groupId: id,
            version: c
        };
        var param1 = {
            groupId: id,
            version: p
        };

        var request0 = {
            method: 'GET',
            url: G.baseUrl + '/api/archive/group',
            params: param0
        };
        var request1 = {
            method: 'GET',
            url: G.baseUrl + '/api/archive/group',
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
                diffTwoGroup(target, baseText, NewText, ptext, ctext);
                $('#slbdiffVSDiv').modal('show');
            }
        );
    };
    $scope.getOperationText = function (x) {
        var resource = $scope.resource;
        if (!resource || _.keys(resource).length == 0) return;
        return resource['log']['log_operationLogApp_opmapping'][x];
    };
    $scope.preLoadCMSData = function () {
        var cmsGroupId = $scope.query.cmsGroupId;

        var param = {
            type: 'info',
            anyProp: 'cmsGroupId:' + cmsGroupId
        };
        var request = {
            method: 'GET',
            url: G.baseUrl + '/api/group',
            params: param
        };

        $http(request).success(function (response, code) {
            if (code == 200 && response.id) {
                var pair = {
                    groupId: response.id,
                    timeStamp: new Date().getTime()
                };

                H.setData(pair);
            } else {
                exceptionNotify("出错了!!", "加载Group, cmsGroupId=" + cmsGroupId + ", 失败了。失败原因: 找不到cmsGroupId为" + cmsGroupId + '的Group，请确认cmsGroupId是存在的。 如果存在请联系SLB Team', null);
                return;
            }
        });
    };
    $scope.preLoadGroupNameData = function () {
        var groupName = $scope.query.groupName;

        var param = {
            type: 'info',
            groupName: groupName
        };
        var request = {
            method: 'GET',
            url: G.baseUrl + '/api/group',
            params: param
        };

        $http(request).success(function (response, code) {
            if (code == 200 && response.id) {
                var pair = {
                    groupId: response.id,
                    timeStamp: new Date().getTime()
                };

                H.setData(pair);
            } else {
                exceptionNotify("出错了!!", "加载Group, groupName=" + groupName + ", 失败了。失败原因: 找不到 Name 为" + groupName + '的Group，请确认name是正确的，并且Group存在。 如果存在请联系SLB Team', null);
                return;
            }
        });
    };
    $scope.loadData = function () {
        var groupId = $scope.query.groupId;
        var param4 = {
            targetId: groupId,
            type: 'Group',
            count: 3
        };
        var groupCurrentLogs = {
            method: 'GET',
            url: G.baseUrl + '/api/logs',
            params: param4
        };

        var param5 = {
            groupId: groupId,
            type: 'extended'
        };
        var groupPolicyRequest = {
            method: 'GET',
            url: G.baseUrl + '/api/policies',
            params: param5
        };
        var vsesReqeust = {
            method: 'GET',
            url: G.baseUrl + '/api/vses?type=extended'
        };

        var usersRequest = {
            method: 'GET',
            url: G.baseUrl + '/api/auth/users'
        };
        var drsRequest = {
            method: 'GET',
            url: G.baseUrl + '/api/drs',
            params: param5
        };
        $('#group-virtual-server-table').bootstrapTable("removeAll");
        $q.all(
            [
                $http(groupCurrentLogs).success(
                    function (response) {
                        if (response['operation-log-datas']) {
                            $scope.information.currentlogs = response['operation-log-datas'];
                        }
                    }
                ),
                $http.get(G.baseUrl + "/api/slbs").success(
                    function (response) {
                        // cache the slbs infromation
                        $.each(response.slbs, function (index, value) {
                            $scope.information.slbs[value.id] = value;
                        });
                    }
                ),
                $http.get(G.baseUrl + "/api/group?type=EXTENDED&groupType=all&groupId=" + groupId).success(
                    function (res) {
                        // TODO: Support VGroup on this page.
                        if (res.type === 'VGROUP') {
                            location.replace(location.href.replace('/portal/group', '/portal/vgroup'));
                            return;
                        }

                        // Group meta data
                        $scope.information.extendedGroup = res;

                        var ps = _.indexBy(res['properties'], 'name');
                        if (ps && ps['status'] && ps['status'].value.toLowerCase() == 'tobeactivated') {
                            introJs().start();
                            $('.introjs-helperNumberLayer').html('');
                        }
                    }
                ),

                $http.get(G.baseUrl + "/api/vgroups?type=extended").success(
                    function (res) {
                        var g = res.groups;
                        $scope.vgroups = g;
                    }
                ),

                $http.get(G.baseUrl + "/api/group?groupId=" + groupId + "&mode=online").success(
                    function (res) {
                        if (res.code != undefined) {
                            $scope.information.onlineGroup = {"data": "No Online Data", "version": "Unknow"};
                        }
                        else {
                            $scope.information.onlineGroup = {"data": res};
                        }
                    }
                ).error(function (reject) {
                    $scope.information.onlineGroup = {"data": "No Online Data", "version": "Unknow"};
                }),

                $http.get(G.baseUrl + "/api/group?groupId=" + groupId).success(
                    function (res) {
                        $scope.information.offlineGroup = res;
                    }
                ),

                $http.get('/api/auth/current/user').success(
                    function (response) {
                        if (response && !response.code) {
                            $scope.query.user = response['name'];
                            $scope.query.email = response.mail;
                        }
                    }
                ),
                $http.get(G.baseUrl + '/api/auth/user/resources').success(
                    function (response) {
                        if (response && !response.code) {
                            $scope.query.resource = response['user-resources'];
                        }
                    }
                ),
                $http(drsRequest).success(function (response) {
                    if (response.total > 0)
                        $scope.information.drs = _.indexBy(response['drs'], 'name');
                    else $scope.information.drs = {};
                }),
                $http(groupPolicyRequest).success(function (response) {
                    if (response.total > 0)
                        $scope.information.policies = response['traffic-policies'];
                    else $scope.information.policies = [];
                }),
                $http(vsesReqeust).success(function (response) {
                    if (response.total > 0)
                        $scope.information.vses = _.indexBy(response['virtual-servers'], 'id');
                    else $scope.information.vses = {};
                }),
                $http(usersRequest).success(
                    function (response) {
                        if (response.total > 0) {
                            $scope.information.users = _.indexBy(response['users'], function (item) {
                                return item['user-name']
                            })
                        }
                    }
                )
            ]
        ).then(
            function () {
                if ($scope.information.extendedGroup.code) {
                    exceptionNotify("出错了!!", "加载Group, GroupId=" + groupId + ", 失败了。失败原因:" + $scope.information.extendedGroup.message, null);
                    return;
                } else {
                    setTimeout(
                        function () {
                            $('.alert-danger').remove();
                        },
                        timeout
                    );
                }
                $.each($scope.information.currentlogs, function (i, item) {
                    var data = item.data;
                    var isJson = IsJsonString(data);

                    var version = '-';

                    if (isJson) {
                        data = JSON.parse(data);
                        if (data['group-datas'] && data['group-datas'].length > 0) {
                            version = data['group-datas'][0].version;
                        }
                    }
                    item['group-version'] = version;

                    item['description'] = data.description;
                });
                var v = _.find($scope.information.extendedGroup.properties, function (e) {
                    return e.name == 'idc';
                });
                var idc_code = _.find($scope.information.extendedGroup.properties, function (e) {
                    return e.name == 'idc_code';
                });
                if (v && v.value) $scope.information.extendedGroup.idc = v.value;
                if (idc_code && idc_code.value) $scope.information.extendedGroup['idc_code'] = idc_code.value;
                else $scope.information.extendedGroup.idc = '-';
                $scope.groupVersions = [];
                $scope.diffVersions[$scope.information.extendedGroup.version] = $scope.information.extendedGroup.version;
                for (var i = 1; i <= $scope.information.extendedGroup.version; i++) {
                    $scope.groupVersions.push(i);
                }
                // Virtual server meta data
                $scope.getVirtualServerMetaData($scope.information.extendedGroup);
                // Get the apps information
                $scope.getAppsMetaData();
                //$scope.getGroupPolicy();
                $scope.loadTable();
            }
        ).finally(
            function () {
                if ($scope.preModifyHealthCheck) {
                    $scope.popNewHealthCheck();
                }
                var hasAccessWindow = $('.right-bt-access');

                if ($scope.query.right && hasAccessWindow && hasAccessWindow.length > 0) {
                    $.notifyClose();

                    var op = '';
                    switch ($scope.query.right) {
                        case 'update': {
                            op = 'READ,UPDATE,ACTIVATE';
                            break;
                        }
                        case 'member': {
                            op = 'OP_MEMBER,READ,UPDATE,ACTIVATE';
                            break;
                        }
                        default:
                            break;
                    }
                    // send the request to apply right
                    var mailLink = G[$scope.env].urls.api + '/api/auth/apply/mail?userName=' + $scope.query.user + '&op=' + op + '&type=Group&targetId=' + $scope.query.groupId + '&env=' + $scope.env;
                    var request = {
                        url: mailLink,
                        method: 'GET'
                    };

                    $http(request).success(function (response, code) {
                        if (code == 200) {
                            // succeed sending the mail
                            successNotify('<strong>发送成功</strong>', '请等待管理员审核通过。 ');
                        }
                    });
                }
            }
        );
    };
    $scope.loadTable = function () {
        var p1 = A.canDo("Group", "UPDATE", $scope.query.groupId) && $scope.query.showextended;

        if (!p1) {
            $('#group-virtual-server-table').bootstrapTable('hideColumn', 'operate');
            $('#group-health-table').bootstrapTable('hideColumn', 'operate');
            $('#group-vgroup-table').bootstrapTable('hideColumn', 'operate');
            /*  $('#group-policy-table').bootstrapTable('hideColumn', 'operate');*/
        }

        else {
            $('#group-virtual-server-table').bootstrapTable('showColumn', 'operate');
            $('#group-health-table').bootstrapTable('showColumn', 'operate');
            $('#group-vgroup-table').bootstrapTable('showColumn', 'operate');
            /*  $('#group-policy-table').bootstrapTable('showColumn', 'operate');*/
        }

        var vses = $scope.information["group-virtual-servers"];
        $('#group-virtual-server-table').bootstrapTable("load", vses);

        $('#group-vgroup-table').bootstrapTable("load", $scope.information["v-groups"]);

        $('#group-health-table').bootstrapTable('hideLoading');
        // $('.group-basic-section').hideLoading();
        $('#group-virtual-server-table').bootstrapTable('hideLoading');
        var heathArr = [];
        if ($scope.information.extendedGroup['health-check']) {
            heathArr.push($scope.information.extendedGroup['health-check']);
        } else {
            heathArr = [
                {
                    uri: '-',
                    timeout: '-',
                    intervals: '-',
                    fails: '-',
                    passes: '-'
                }
            ];
        }
        $('#group-health-table').bootstrapTable("load", heathArr);

        /*    $('#group-policy-table').bootstrapTable("hideLoading");
         $('#group-policy-table').bootstrapTable("load", $scope.information.policies);*/
    };
    $scope.getAppsMetaData = function () {
        var params = {
            "appId": appId
        };
        var request = {
            method: 'GET',
            url: G.baseUrl + '/api/apps',
            params: params
        };

        $http(request).success(function (res) {
            var response = res["apps"];
            if (response != undefined && response.length > 0) {
                var response = res["apps"][0];

                var ownerStr = $scope.information.users[response.owner] ? ($scope.information.users[response.owner]['chinese-name'] || response.owner) : response.owner;

                $scope.information.appdata['id'] = appId;
                $scope.information.appdata['appname'] = response["chinese-name"];
                $scope.information.appdata['applink'] = "/portal/app#?env=" + G.env + "&appId=" + appId;
                $scope.information.appdata['bu'] = response.sbu;
                $scope.information.appdata["owner"] = ownerStr;
                $scope.information.appdata["ownername"] = response.owner;
                $scope.information.appdata["email"] = response["owner-email"];
            }
            else {
                $scope.information.appdata['id'] = appId;
                $scope.information.appdata['appname'] = "Unknown";
                $scope.information.appdata['applink'] = "/portal/app#?env=" + G.env + "&appId=" + appId;
                $scope.information.appdata['bu'] = "Unknown";
                $scope.information.appdata["owner"] = "Unknown";
                $scope.information.appdata["email"] = "Unknown";
            }
        });
    };
    $scope.generatePolicyLink = function (name) {
        var v = _.find($scope.information.policies, function (r) {
            return r.name.toLowerCase().trim() == name.toLowerCase().trim();
        });
        if (v) return '/portal/policy#?env=' + $scope.env + '&policyId=' + v.id;
    };
    $scope.generateDrLink = function (id) {
        return '/portal/dr/edit#?env=' + $scope.env + '&drId=' + id + '&page=dr';
    };
    $scope.getDrConfigType = function (dr) {
        var vproperties = _.groupBy(dr.properties, function (v) {
            return v.value.toLowerCase();
        });

        var category = 'all';
        var partials = vproperties['partial'];
        var alls = vproperties['all'];

        if (partials && alls) {
            category = 'partial';
        }
        if (category == 'all') {
            return dr.name + '(此Group存在全部流量导流策略)'
        } else if (category == 'partial') {
            return dr.name + '(此Group存在部分流量导流策略)'
        }
    };
    $scope.groupVipBindings = {};
    $scope.getVirtualServerMetaData = function (res) {
        $scope.groupVipBindings = {};
        appId = res["app-id"];

        // Assemble the virtual server group information and bind
        var virtualServerInformation = res["group-virtual-servers"];
        $scope.vsCountNumber = virtualServerInformation.length;
        $scope.information["group-virtual-servers"] = [];
        $.each(virtualServerInformation, function (i, vs) {
            var temp = {};
            temp.priority = vs.priority;
            temp.domains = vs["virtual-server"].domains;
            temp.path = vs.path;
            temp.name = vs.name;
            temp.rewrite = vs.rewrite;
            temp.slb = vs["virtual-server"]["slb-ids"];
            temp.port = vs["virtual-server"].port;
            temp.vs = vs["virtual-server"].id;
            temp.ssl = vs["virtual-server"].ssl;
            temp.routes = vs['route-rules'];

            $scope.information["group-virtual-servers"].push(temp);
            temp.slbvips = [];
            $.each(temp.slb, function (i, item) {
                temp.slbvips.push({
                    slbid: item,
                    vips: $scope.information.slbs[item].vips
                })
            });

            var e = _.find($scope.information.vses[vs["virtual-server"].id].properties, function (r) {
                return r.name == 'idc';
            });
            if (e && e.value) temp.idc = e.value;
            else temp.idc = '-';
        });

        var p = $scope.information.extendedGroup["group-virtual-servers"][0].path;
        var vs2Domains = {};
        $.each($scope.information.extendedGroup["group-virtual-servers"], function (index, item) {
            $.each(item["virtual-server"].domains, function (j, v) {
                vs2Domains[v.name] = v.name;
            });
        });

        $scope.information["v-groups"] = _.filter($scope.vgroups, function (item) {
            return $scope.compareGroupVs(item["group-virtual-servers"], vs2Domains, p);
        });
    };
    $scope.compareGroupVs = function (vses1, vses2, path) {
        var passed = false;
        var vpath = vses1[0].path;
        if (vpath == path) {
            $.each(vses1, function (i, item) {
                $.each(item["virtual-server"].domains, function (j, v) {
                    if (vses2[v.name] != undefined) passed = true;
                });
            });
        }
        return passed;
    };

    // Add edit and remove virtual server from a group
    $scope.vsBindingDisabled = true;
    $scope.firstBindingVsRequest;
    $scope.showForceUpdate = false;
    $scope.virtualServersToBeRemoved;
    $scope.groupVirtualServersTable = {
        columns: [],
        add: function (index) {
            this.columns.push({
                "vs-id": "",
                "priority": $scope.information["group-virtual-servers"][0].priority,
                "path": $scope.information["group-virtual-servers"][0].path,
                "rewrite": $scope.information["group-virtual-servers"][0].rewrite
            });
            setTimeout(function () {
                $("#groupVsID" + (index + 1)).bootstrapValidation();
                $("#groupVsPath" + (index + 1)).bootstrapValidation();
                $("#groupVsRewrite" + (index + 1)).bootstrapValidation();
            }, timeout);
        },
        edit: function (index) {
            $("#editGroupVirtualServer").hide();
            $("#groupVsPriority").prop('disabled', false);
            $("#groupVsPath").prop('disabled', false);
            $("#groupVsRewrite").prop('disabled', false);
        },
        remove: function (index) {
            this.columns.splice(index, 1);
        }
    };
    $('#reviewRemoveVsBt').click(function () {
        var reviewResult = reviewData($('#remove-vs-div'));
        if (!reviewResult) {
            return;
        }
        $('#removeVirtualServerModal').modal('hide');
        $('#doubleConfirmRemoveGroupVirtualServerModal').modal('show');
    });
    $scope.confirmRemoveGroupVirtualServer = function () {
        // Delete the target vs from the group
        $('#removeVirtualServerModal').modal("hide");
        var groupId = $scope.information.extendedGroup.id;
        var loading = "<img src='/static/img/spinner.gif' /> 正在从Group中删除指定的VirtualServer";
        $('#operationConfrimModel').modal('show').find(".modal-body").html(loading);
        $('#operationConfrimModel').modal("show").find(".modal-body").html(loading);
        // Ajax call to remove the selected items
        var removedVsIds = [];
        $.each($scope.virtualServersToBeRemoved, function (i, item) {
            removedVsIds.push(item.vs);
        });

        var reason = $('#remove-vs-operation-reason').val().trim();
        var parameters = {
            "groupId": groupId,
            "vsId": removedVsIds.join(','),
            description: reason
        };
        var request = {
            method: 'GET',
            url: G.baseUrl + '/api/group/unbindVs',
            params: parameters
        };
        var confrimText = "<span class='warning-important'>注意：从Group中下线当前VirtualServer后需激活Group方能生效</span>";
        $scope.processRequest(request, $('#operationConfrimModel'), "从Group中删除指定Virtual Server ", confrimText);
    }

    function popRemoveVsFn(row) {
        if ($scope.information["group-virtual-servers"].length == 1) {
            // There is only one virtual server.
            $('#removeOnlyOneVirtualServerModal').modal("show");
            return;
        }
        var str = "";
        $scope.virtualServersToBeRemoved = [];
        $('.to-be-removed-vses').children().remove();
        // get the selected items
        var selectedItems = $('#group-virtual-server-table').bootstrapTable('getSelections');
        if (selectedItems != undefined && selectedItems.length > 0 && row == undefined) {
            // Batch remove
            $.each(selectedItems, function (index, m) {
                $scope.virtualServersToBeRemoved.push({"vs": m.vs, "path": m.path, "name": m.name});
                // $scope.info.membersTobeRemoved.push({"ip": m.ip, "port": m.port});
                str += "<tr>";
                str += "<td><span class='path'>" + (m.path || '-') + "</span></td>";
                str += "<td><span class='name'>" + (m.name || '-') + "</span></td>";
                str += "<td><span class='ssl'>" + m.port + "</span></td>";
                var rewrite = m.rewrite;
                if (rewrite == undefined || rewrite == "") {
                    rewrite = "-";
                }
                str += "<td><span class='rewrite'>" + rewrite + "</span></td>";
                str += "<td><span class='priority'>" + m.priority + "</span></td>";
                str += "<td><span class='ssl'>" + m.ssl + "</span></td>";
                str += "<td><span class='ssl'>" + m.slb + "</span></td>";
                str += "<td><span class='vs'>" + m.vs + "</span></td>";

                var s = "";
                $.each(m.domains, function (i, d) {
                    s += "<span>" + d.name + "</span></br>";
                });
                str += "<td><span class='domain'>" + s + "</span></td>";
                str += "</tr>";
            });
        }
        else {
            if (row) {
                $scope.virtualServersToBeRemoved.push({"vs": row.vs, "path": row.path, "name": row.name});

                str += "<tr>";
                str += "<td><span class='path'>" + (row.path || '-') + "</span></td>";
                str += "<td><span class='name'>" + (row.name || '-') + "</span></td>";
                str += "<td><span class='ssl'>" + row.port + "</span></td>";
                var rewrite = row.rewrite;
                if (rewrite == undefined || rewrite == "") {
                    rewrite = "-";
                }
                str += "<td><span class='rewrite'>" + rewrite + "</span></td>";
                str += "<td><span class='priority'>" + row.priority + "</span></td>";
                str += "<td><span class='ssl'>" + row.ssl + "</span></td>";
                str += "<td><span class='ssl'>" + row.slb + "</span></td>";
                str += "<td><span class='vs'>" + row.vs + "</span></td>";

                var s = "";
                $.each(row.domains, function (i, d) {
                    s += "<span>" + d.name + "</span></br>";
                });
                str += "<td><span class='domain'>" + s + "</span></td>";
                str += "</tr>";
            }
        }
        $('.to-be-removed-vses').append(str);
        $('#removeVirtualServerModal').modal('show');
    }

    $scope.focusGField = function (field) {
        switch (field) {
            case "path": {
                $('.field-description').html("* Path: 配置Group的访问目录。<ul class='operation-warnning-ul'>" +
                    "<li>实例1: <b style='color:red'>~* ^/restapi($|/|\\?)</b>,申请一个虚拟目录是restapi的应用。</li>" +
                    "<li>实例2: <b style='color:red'>空</b>或者<b style='color:red'>/</b>,申请一个根目录的应用。</li>" +
                    "<li>其它: 其它路径请联系<b style='color:red'>" +
                    "<a href='mailto:slb@test.com?subject=SLB产品咨询&body=请添加咨询问题列表: %0A%0A%0A%0A%0AFrom: /portal/group'>框架研发部 SLB Team</a>" +
                    "</b>,申请一个根目录的应用。</li>");
                break;
            }
            case "name": {
                $('.field-description').html("<span>* Name: named location的名称，用于通过自定义脚本进行路由转发。</span>");
                break;
            }
            case "rewrite": {
                $('.field-description').html('<span>* Rewrite: SLB转发规则。 实例: <b  style="color: red">"(?i)^/restapi(.*)" /restapi;"(?i)^/restapi/(.*)" /restapi/$1;"(?i)^/(.*)" /restapi/$1;</b></span>');
                break;
            }

            case "redirect": {
                $('.field-description').html('<span>* Redirect: SLB转发规则。 实例 Http请求转Https:<b  style="color: red">"(?i)(.*)" https://$host$1;</b></span><span>实例 Https请求转Http: <b  style="color: red">"(?i)(.*)" http://$host$1;</b></span>');
                break;
            }
            case "priority": {
                $('.field-description').html('<span>* Priority: Group所在VirtualServer的优先级。 默认: <b  style="color: red">1000</b></span>');
                break;
            }
            case "priority": {
                $('.field-description').html('<span>* Rewrite: Group所在VirtualServer的优先级。 默认: <b  style="color: red">1000</b></span>');
                break;
            }
        }
    }
    $scope.popNewVirtualPath = function () {
        // New Virtual Server binding
        $scope.groupVirtualServersTable.columns = [];
        var path = $scope.information["group-virtual-servers"][0].path;
        var name = $scope.information["group-virtual-servers"][0].name;
        var priority = $scope.information["group-virtual-servers"][0].priority;
        var showPath = $scope.getShowPath(path);
        var showName = $scope.getPureNameForEditing(name);
        var showNamePrefix = $scope.shouldShowNamePrefix(name);

        // new vs
        $scope.groupVirtualServersTable.columns.push(
            {
                "vs-id": "",
                "priority": priority,
                "name": name,
                "showName": showName,
                "showNamePrefix": showNamePrefix,
                "path": path,
                "showPath": showPath,
                "rewrite": $scope.information["group-virtual-servers"][0].rewrite,
                routes: []
            }
        );
        $('#addVirtualServerModel').modal("show").find(".field-description").html('');

        setTimeout(function () {
            $scope.initNewRequestHeaders([]);
            $('#vs-add-headers').bootstrapTable('load', []);
            $scope.addVs.headerCount = $('#vs-add-headers').bootstrapTable('getData').length;

        }, 500);

    };
    $scope.confirmAddGroupVirtualServer = function () {
        $scope.showForceUpdate = false;
        var groupId = $scope.information.extendedGroup.id;
        // Start loading
        var loading = "<img src='/static/img/spinner.gif' /> 正在添加新的Group Virtual Server配置";
        $('#bindingVsConfrimModel').modal('show').find(".modal-body").html(loading);
        // each column stands for a virtual server
        $.each($scope.groupVirtualServersTable.columns, function (i, item) {
            delete item["$$hashKey"];

            item["vs-id"] = parseInt(item["vs-id"]);
            if (item.priority != undefined || item.priority == "") {
                item.priority = parseInt(item.priority);
            }
            if (item.showPath && $scope.showSuffixText(item.path)) {
                item.path = start + item.showPath + end;
            }
            else {
                item.path = item.showPath;
            }
            delete item["showPath"];

            item.name = item.showName ? (item.showNamePrefix ? $scope.namePrefix + item.showName : item.showName) : null;
            delete item.showName;
            delete item.showNamePrefix;

            // for advanced options
            var routes = item['routes'];
            delete item['routes'];
            if (routes && routes.length > 0) {
                item['route-rules'] = _.map(routes, function (item2) {
                    var type = item2.type.toLowerCase();
                    switch (type) {
                        case 'header': {
                            return {
                                type: 'header',
                                key1: item2.key,
                                value1: item2.value,
                                op: 'regex'
                            }
                        }
                        default:
                            break;
                    }
                });
            }
        });

        var opreason = $('#vs-operation-reason').val().trim();
        var param = {
            description: opreason
        };
        var request = {
            method: 'POST',
            url: G.baseUrl + '/api/group/bindVs',
            data: {
                'group-id': groupId,
                'bounds': $scope.groupVirtualServersTable.columns
            },
            params: param
        };

        var confrimText = "<span class='warning-important'>注意:修改Virtual Server信息后需激活Group方能生效</span>";
        $scope.processRequest(request, $('#bindingVsConfrimModel'), "修改Virtual Server信息", confrimText);
    };
    $scope.forceUpdateBinding = function () {
        $scope.showForceUpdate = false;
        var loading = "<img src='/static/img/spinner.gif' /> 正在强制更新Group Virtual Server配置";
        $('#operationConfrimModel').modal('show').find(".modal-body").html(loading);
        var confrimText = "<span class='warning-important'>注意:修改Virtual Server信息后需激活Group方能生效</span>";
        var request = $scope.forceUpdateRequest;

        $scope.processRequest(request, $('#operationConfrimModel'), "强制修改Group Virtual Server信息", confrimText);
    };
    $scope.showSuffixText = function (path) {
        if (!path) return true;
        return path.startWith(start) && path.endWith(end);
    };
    $scope.getShowPath = function (path) {
        var showPath = "";
        var showSuffix = $scope.showSuffixText(path);
        if (path && showSuffix) {
            // Regular path
            showPath = path.substring(4, path.lastIndexOf(end));
        } else {
            showPath = path;
        }
        return showPath;
    }
    $scope.shouldShowNamePrefix = function (name) {
        if (!name) {
            return true;
        }
        return name.startWith($scope.namePrefix);
    };
    $scope.getPureNameForEditing = function (name) {
        if (!name) {
            return '';
        }
        return $scope.shouldShowNamePrefix(name) ? name.substring($scope.namePrefix.length) : name;
    };
    $scope.confirmEditGroupVirtualServer = function () {
        var groupId = $scope.information.extendedGroup.id;
        // Start loading
        var loading = "<img src='/static/img/spinner.gif' /> 正在更新Group Virtual Server配置";
        $('#bindingVsConfrimModel').modal('show').find(".modal-body").html(loading);
        // each column stands for a virtual server
        $.each($scope.groupVirtualServersTable.columns, function (i, item) {
            delete item["$$hashKey"];
            item["vs-id"] = parseInt(item["vs-id"]);
            if (item.priority != undefined) {
                item.priority = parseInt(item.priority);
            }
            if (item.showPath && $scope.showSuffixText(item.path)) {
                item.path = start + item.showPath + end;
            }
            else {
                item.path = item.showPath;
            }
            delete item["showPath"];

            item.name = item.showName ? (item.showNamePrefix ? $scope.namePrefix + item.showName : item.showName) : null;
            delete item.showName;
            delete item.showNamePrefix;

            // for advanced options
            var routes = item['routes'];
            delete item['routes'];
            if (routes && routes.length > 0) {
                item['route-rules'] = _.map(routes, function (item2) {
                    var type = item2.type.toLowerCase();
                    switch (type) {
                        case 'header': {
                            return {
                                type: 'header',
                                key1: item2.key,
                                value1: item2.value,
                                op: 'regex'
                            }
                        }
                        default:
                            break;
                    }
                });
            }
        });
        var params = {
            update: true,
            description: $('#edit-vs-operation-reason').val().trim()
        };
        var request = {
            method: 'POST',
            params: params,
            url: G.baseUrl + '/api/group/bindVs',
            data: {
                "group-id": groupId,
                "bounds": $scope.groupVirtualServersTable.columns
            }
        };

        $scope.firstBindingVsRequest = request;

        var confrimText = "<span class='warning-important'>注意:修改Virtual Server信息后需激活Group方能生效</span>";
        $scope.processRequest(request, $('#bindingVsConfrimModel'), "修改Virtual Server信息", confrimText);
    };
    $scope.popUpdateVirtualPath = function () {
        $scope.vsBindingDisabled = true;
        $('#askEditVServerModal').modal("hide");
        $scope.groupVirtualServersTable.columns = [];
        var path = selectedVirtualServerObject.path;
        var showPath = $scope.getShowPath(path);
        var name = selectedVirtualServerObject.name;
        var showName = $scope.getPureNameForEditing(name);
        var showNamePrefix = $scope.shouldShowNamePrefix(name);

        // update vs
        var routes = _.map(selectedVirtualServerObject['routes'], function (v) {
            if (v.type == 'header') {
                if (v.key1) {
                    v.key = v.key1;
                    delete v.key1;
                }
                if (v.value1) {
                    v.value = v.value1;
                    delete v.value1;
                }
            }
            return v;
        });
        $scope.groupVirtualServersTable.columns.push(
            {
                "vs-id": selectedVirtualServerObject.vs.toString(),
                "priority": selectedVirtualServerObject.priority,
                "name": name,
                "showName": showName,
                "showNamePrefix": showNamePrefix,
                "path": path,
                "showPath": showPath,
                "rewrite": selectedVirtualServerObject.rewrite,
                routes: routes
            }
        );
        $scope.editVs.selectedVsId = selectedVirtualServerObject.vs;

        // load the tables
        var modal = $('#editVirtualServerModel').modal("show");

        setTimeout(function () {
            var vs = _.find($scope.groupVirtualServersTable.columns, function (v) {
                return v['vs-id'] == selectedVirtualServerObject.vs;
            });

            var headers = _.filter(vs['routes'], function (v) {
                return v.type.toLowerCase() == 'header';
            });
            $scope.initRequestHeaders(headers);
            $('#vs-edit-headers').bootstrapTable('load', headers);
            $scope.editVs.headerCount = $('#vs-edit-headers').bootstrapTable('getData').length;

            modal.find('#groupUpdateAction').val("update");
        }, 500);
    };
    $scope.confirmAddName = function () {
        $('#groupNewVsName0').prop("disabled", false);
        $('#newGroupName').hide();
    }
    $scope.confirmAddPriority = function () {
        $('#groupVsPriority0').prop("disabled", false);
        $('#NewGroupPriority').hide();
    }
    $scope.confirmAddRewrite = function () {
        $('#groupNewVsRewrite0').prop("disabled", false);
        $('#newGroupRewrite').hide();
    }
    $scope.confirmEditName = function () {
        $('#groupEditVsName0').prop("disabled", false);
        $('#editGroupName').hide();
    }
    $scope.confirmEditPriority = function () {
        $('#groupEditVsPriority0').prop("disabled", false);
        $('#EditGroupPriority').hide();
    }
    $scope.confirmEditRewrite = function () {
        $('#groupEditVsRewrite0').prop("disabled", false);
        $('#editGroupRewrite').hide();
    }
    $scope.confirmEditPath = function () {
        $('#groupEditVsPath0').prop("disabled", false);
        $('#pathEditRootSetting0').prop("disabled", false);
        $('#editVsPath').hide();
    }
    $scope.confirmEditVs = function () {
        $scope.vsBindingDisabled = false;
        $('#editVsId').hide();
    }
    $scope.setAddedPathAsRoot = function () {
        var c = $('#pathAddRootSetting0').is(':checked');
        if (c) {
            $scope.groupVirtualServersTable.columns[0].path = "~* ^/";
            $scope.groupVirtualServersTable.columns[0].showPath = "~* ^/";
            $scope.groupVirtualServersTable.columns[0].priority = -1000;
            $('#groupNewVsPath0').prop("disabled", true);
        }
        else {
            $('#groupNewVsPath0').prop("disabled", false);
            $scope.groupVirtualServersTable.columns[0].path = $scope.information["group-virtual-servers"][0].path;
            $scope.groupVirtualServersTable.columns[0].priority = 1000;
            $scope.groupVirtualServersTable.columns[0].showPath = $scope.getShowPath($scope.information["group-virtual-servers"][0].path);
        }
    };
    $scope.setEditPathAsRoot = function (p) {
        var c = $('#pathEditRootSetting0').is(':checked');
        if (c) {
            $scope.groupVirtualServersTable.columns[0].path = "~* ^/";
            $scope.groupVirtualServersTable.columns[0].showPath = "~* ^/";
            $scope.groupVirtualServersTable.columns[0].priority = -1000;
            $('#groupEditVsPath0').prop("disabled", true);
        }
        else {
            $('#groupEditVsPath0').prop("disabled", false);
            $scope.groupVirtualServersTable.columns[0].path = selectedVirtualServerObject.path;
            $scope.groupVirtualServersTable.columns[0].priority = 1000;
            $scope.groupVirtualServersTable.columns[0].showPath = $scope.getShowPath(selectedVirtualServerObject.path);
        }
    }
    $scope.editPriorityBtClick = function () {
        var confrimAsk = confirm("Priority建议由系统自动生成，你确定要修改吗?");
        if (confrimAsk)
            $('#groupVsPriority0').prop("disabled", false);
    }
    $scope.editVsBtClick = function () {
        var confrimAsk = confirm("VirtualServerId决定当前Group的域名，你确定要修改吗?");
        if (confrimAsk)
            $scope.editVsBinding2 = false;
    }
    $('#saveEditVirtualServer').click(function () {
        var reviewResult = reviewData($('.edit-vs-table'));
        if (reviewResult) {
            $('#editVirtualServerModel').modal("hide");
            $('#doubleConfirmEditGroupVirtualServerModal').modal("show");
        }
    });
    $("#saveNewVirtualServer").click(function (event) {
        $('[data-validator-type="validation"]').bootstrapValidation("reset");
        $('[data-validator-type="validation"]').bootstrapValidation();
        var reviewResult = reviewData($('.add-vs-dv'));

        if (reviewResult) {
            $('#addVirtualServerModel').modal("hide");
            $('#doubleConfirmAddGroupVirtualServerModal').modal("show");
        }
    });
    $('#group-batch-delete-path-bt').click(
        function (e) {
            e.preventDefault();
            popRemoveVsFn();
        }
    );
    // Edit virtual server advanced mode
    $scope.editVs = {
        hasAdvancedOptions: false,
        isNewHeader: false,
        headerKey: '',
        selectedVsId: '',
        options: [],
        headertype: '',
        headerCount: 0
    };
    $scope.removeSelectedAdvancedOption = function (type) {
        var tabel;
        var data;
        var vsId = $scope.editVs.selectedVsId.toString();
        switch (type) {
            case 'header': {
                tabel = $('#vs-edit-headers');
                break;
            }
            default:
                break;
        }
        var selectedItems = tabel.bootstrapTable('getSelections');
        var keys = _.map(selectedItems, function (v) {
            return v.key;
        });

        // to be removed items
        data = tabel.bootstrapTable("getData");

        data = _.reject(data, function (v) {
            return keys.indexOf(v.key) != -1;
        });

        // reject the data from model
        $.each($scope.groupVirtualServersTable.columns, function (i, item) {
            if (item['vs-id'] == vsId) {
                item.routes = _.reject(item.routes, function (v) {
                    return keys.indexOf(v.key) != -1;
                });
            }
        });
        tabel.bootstrapTable('load', data);
    };
    $scope.popNewVirtualHeader = function () {
        $('#error-msg-edit-advanced').html('');
        $scope.editVs.options = [];
        $scope.editVs.isNewHeader = true;
        $scope.editVs.headertype = 'header';
        $scope.editVs.options.push(
            {
                type: 'Header',
                ops: [
                    '正则匹配'
                ],
                selectedop: '正则匹配',
                key: '',
                value: ''
            }
        );
        $('#newVsAdvancedHeaderOptionModel').modal('show');
    };
    $scope.addNewHeader = function () {
        if ($scope.editVs.headertype == 'header') {
            $scope.editVs.options.push(
                {
                    type: 'Header',
                    ops: [
                        '正则匹配'
                    ],
                    selectedop: '正则匹配',
                    key: '',
                    value: ''
                }
            );
        }
    };
    $scope.removeNewHeader = function (index) {
        $scope.editVs.options.splice(index, 1);
    };

    $scope.saveNewHeader = function () {
        $('#error-msg-edit-advanced').html('');
        var options = $scope.editVs.options;
        var isnew = $scope.editVs.isNewHeader;
        var vsid = $scope.editVs.selectedVsId;

        var result = reviewData($('#newVsAdvancedHeaderOptionModel'));
        if (!result) return;
        // add the added options into the list of origin routes
        var vs = _.find($scope.groupVirtualServersTable.columns, function (v) {
            return v['vs-id'] == vsid.toString();
        });

        var routes = vs.routes;
        var hasEditError = false;
        var editErroMsg = '';
        $.each(options, function (i, item) {
            var key = item.key;
            var value = '^' + item.value + '$';
            var e = key.toLowerCase();
            routes = _.map(_.filter(routes, function (v) {
                return v.type.toLowerCase() == 'header';
            }), function (v) {
                return {
                    key: v.key1 || v.key,
                    value: v.value1 || v.value,
                    type: 'header',
                    op: 'regex'
                }
            });
            var m = _.map(routes, function (v) {
                var s = v.key;
                return s.toLowerCase();
            });
            if (isnew) {
                if (m.indexOf(e) == -1) {
                    // not existed
                    routes.push(
                        {
                            key: key,
                            value: value,
                            type: 'header',
                            op: 'regex'
                        }
                    );
                } else {
                    editErroMsg += '<span>已经存在 Header， Key=' + e + ';</span>';
                    hasEditError = true;
                }
            } else {
                // update
                var current = $scope.editVs.headerKey.toLowerCase();
                var index = m.indexOf(current);
                routes[index] = {
                    type: 'header',
                    key: key,
                    value: value,
                    op: 'regex'
                };
            }
        });
        if (hasEditError) {
            editErroMsg += '; 请通过编辑方式更改。';
            $('#error-msg-edit-advanced').html(editErroMsg);
            return;
        }
        ;
        $.each($scope.groupVirtualServersTable.columns, function (i, item) {
            if (item['vs-id'] == vsid.toString()) item.routes = routes;
        });
        $('#newVsAdvancedHeaderOptionModel').modal('hide');
        $('#vs-edit-headers').bootstrapTable('load', routes);
    };


    // Add virtual server advanced mode
    $scope.addVs = {
        isNewHeader: false,
        headerKey: '',
        options: [],
        headertype: '',
        headerCount: 0
    };
    $scope.popAddNewVirtualHeader = function () {
        $('#error-msg-new-advanced').html('');
        $scope.addVs.options = [];
        $scope.addVs.isNewHeader = true;
        $scope.addVs.headertype = 'header';
        $scope.addVs.options.push(
            {
                type: 'Header',
                ops: [
                    '正则匹配'
                ],
                selectedop: '正则匹配',
                key: '',
                value: ''
            }
        );
        $('#newAddVsAdvancedHeaderOptionModel').modal('show');
    };
    $scope.addAddedNewHeader = function () {
        if ($scope.addVs.headertype == 'header') {
            $scope.addVs.options.push(
                {
                    type: 'Header',
                    ops: [
                        '正则匹配'
                    ],
                    selectedop: '正则匹配',
                    key: '',
                    value: ''
                }
            );
        }
    };
    $scope.removeAddedNewHeader = function (index) {
        $scope.addVs.options.splice(index, 1);
    };

    $scope.saveAddedNewHeader = function () {
        var hasAddError = '';
        var addErrorMsg = '';
        $('#error-msg-new-advanced').html('');
        var options = $scope.addVs.options;
        var isnew = $scope.addVs.isNewHeader;

        var result = reviewData($('#newAddVsAdvancedHeaderOptionModel'));
        if (!result) return;

        // add the added options into the list of origin routes
        var vs = $scope.groupVirtualServersTable.columns[0];

        var routes = vs.routes;

        $.each(options, function (i, item) {
            var key = item.key;
            var value = '^' + item.value + '$';
            var e = key.toLowerCase();
            routes = _.map(_.filter(routes, function (v) {
                return v.type.toLowerCase() == 'header';
            }), function (v) {
                return {
                    key: v.key1 || v.key,
                    value: v.value1 || v.value,
                    type: 'header',
                    op: 'regex'
                }
            });
            var m = _.map(routes, function (v) {
                var s = v.key;
                return s.toLowerCase();
            });
            if (isnew) {
                if (m.indexOf(e) == -1) {
                    // not existed
                    routes.push(
                        {
                            key: key,
                            value: value,
                            type: 'header',
                            op: 'regex'
                        }
                    );
                } else {
                    hasAddError = true;
                    addErrorMsg += '已经存在 Header， Key=' + e;
                }
            } else {
                // update
                var current = $scope.addVs.headerKey.toLowerCase();
                var index = m.indexOf(current);
                routes[index] = {
                    type: 'header',
                    key: key,
                    value: value,
                    op: 'regex'
                };
            }
        });
        if (hasAddError) {
            addErrorMsg += '; 请通过编辑方式更改。';
            $('#error-msg-new-advanced').html(addErrorMsg);
            return;
        }
        $scope.groupVirtualServersTable.columns[0].routes = routes;
        $('#newAddVsAdvancedHeaderOptionModel').modal('hide');
        $('#vs-add-headers').bootstrapTable('load', routes);
    };
    // Health check table actions
    $scope.disableUpdateHealthCheck = function () {
        var groupId = $scope.information.extendedGroup.id;
        if (!A.canDo('Group', 'UPDATE', groupId)) {
            return 'disabled';
        }
    };
    $scope.showGroupUpdateUnavailable = function () {
        var groupId = $scope.information.extendedGroup.id;
        if (!A.canDo('Group', 'UPDATE', groupId)) {
            return true;
        }
    };
    $scope.hasApply = false;
    $scope.timeFieldDisabled = function () {
        /* if($scope.preModifyHealthCheck) return 'disabled';*/
    };
    $scope.applyGroupUpdateRequest = function () {
        if ($scope.hasApply) {
            alert('你已经申请更新当前应用的权限，请等待管理员审批! 如已经收到审批通过邮件，请刷新当前页面获得最新状态!');
            return;
        }
        var groupId = $scope.information.extendedGroup.id;
        var user = $scope.query.user;

        var mailLink = G[$scope.env].urls.api + '/api/auth/apply/mail?userName=' + user + '&op=ACTIVATE,UPDATE&targetId=' + groupId + '&type=Group&env=' + $scope.env;

        var request = {
            url: mailLink,
            method: 'GET'
        };
        $http(request).success(function (response, code) {
            if (code == 200) {
                alert('你已经申请更新当前应用的权限，请等待管理员审批!');
                $scope.hasApply = true;
            }
        });
    };
    $scope.popNewHealthCheck = function () {
        var health = $scope.information.extendedGroup["health-check"];
        if (health == undefined) {
            health = {
                timeout: 2000,
                uri: "",
                intervals: 5000,
                fails: 3,
                passes: 1
            };
        }

        $('#addHealthCheckModal').modal("show");
    };
    $scope.confirmRemoveHealthChecker = function () {
        delete $scope.information.extendedGroup["health-check"];
        // update the group with latest health check url
        delete $scope.information.extendedGroup.properties;
        delete $scope.information.extendedGroup.tags;
        var request = {
            method: 'POST',
            url: G.baseUrl + "/api/group/update?description=" + $scope.query.removeHealthReason,
            data: $scope.information.extendedGroup,
            params: {}
        };
        var loading = "<img src='/static/img/spinner.gif' /> 正在修改HealthCheck信息";
        $('#operationConfrimModel').modal('show').find(".modal-body").html(loading);

        $('#removeHealthCheckModal').modal('hide');
        var confrimText = "<span class='warning-important'>注意:删除健康监测信息后需激活Group方能生效</span>";
        $scope.processRequest(request, $('#operationConfrimModel'), "删除健康监测信息", confrimText);
    };
    $('#saveHealthCheck').click(function (e) {
        var validate = reviewData($('#add-health-table'));
        if (!validate) return;
        $('#addHealthCheckModal').modal("hide");
        $('#doubleConfirmEditHealthCheckModal').modal("show");
    });
    $scope.confirmEditHealthChecker = function () {
        delete $scope.information.extendedGroup.properties;
        delete $scope.information.extendedGroup.tags;
        // update group
        var reasonText = $('#healthy-reason').val().trim();

        var request = {
            method: 'POST',
            url: G.baseUrl + "/api/group/update",
            data: $scope.information.extendedGroup,
            params: {
                description: reasonText
            }
        };
        var loading = "<img src='/static/img/spinner.gif' /> 正在修改HealthCheck信息";
        $('#operationConfrimModel').modal('show').find(".modal-body").html(loading);
        $('#addHealthCheckModal').modal('hide');
        var confrimText = "<span class='warning-important'>注意:修改健康监测信息后需激活Group方能生效</span>";

        $scope.processRequest(request, $('#operationConfrimModel'), "修改健康监测信息", confrimText);
    };

    // Chart area
    $scope.openCalendar = function (e) {
        e.preventDefault();
        e.stopPropagation();
        $scope.isOpen = true;
    };
    $scope.setDateNow = function () {
        var d = new Date();
        d = d.setTime(d.getTime() - 1000 * 60 * 60);
        var time = new Date($.format.date(d, 'yyyy-MM-dd HH:00'));
        $scope.chart.startTime = time;
    };
    $scope.setDateNextHour = function () {
        $scope.chart.startTime = new Date($.format.date($scope.chart.startTime.getTime() + 60 * 1000 * 60, 'yyyy-MM-dd HH:00'));
        $scope.chart.endTime = new Date($scope.chart.startTime.getTime() + 1000 * 60 * 90);
    };
    $scope.setDatePreviousHour = function () {
        $scope.chart.startTime = new Date($.format.date($scope.chart.startTime.getTime() - 60 * 1000 * 60, 'yyyy-MM-dd HH:00'));
        $scope.chart.endTime = new Date($scope.chart.startTime.getTime() + 1000 * 60 * 90);
    };
    $scope.loadAllCharts = function (chartParent) {
        $scope.startTime = $scope.chart.startTime;
        $scope.endTime = new Date($scope.chart.startTime.getTime() + 1000 * 60 * 90);

        $scope.loadTrafficCharts(chartParent);
    };
    $scope.loadTrafficCharts = function (chartParent) {
        var chartContainer = chartParent.find('.chart');
        chartContainer.empty();

        var params = {
            'metric-name': 'slb.req.count',
            'start-time': $.format.date($scope.startTime, 'yyyy-MM-dd HH:mm'),
            'end-time': $.format.date($scope.endTime, 'yyyy-MM-dd HH:mm'),
            'tags': '{"group_id":["' + $scope.information.extendedGroup.id + '"]}',
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

        $scope.buildChart(chartContainer, "Group:" + $scope.information.extendedGroup.name + ",当前1小时流量确认", config, true, -1);
    };
    $scope.deactivateBtEnabled = true;
    $scope.buildChart = function (chartContainer, title, config, needMark, groupByCount, groupByLinkFun) {
        $scope.deactivateBtEnabled = true;
        var chartDom = $('<div class="col-md-12 traffic-chart"></div>');
        chartContainer.append(chartDom);
        var chart = newChart(chartDom.get(0));
        config.withCredentials = false;
        $http(config).success(
            function (res) {
                if (res["result-code"] != undefined && res["result-code"] == 0) {
                    loadChartData(chart, res, title, "", new Date($scope.startTime).getTime(), 60000, needMark, groupByCount, groupByLinkFun);
                }
                else {
                    res = {};
                    res['time-series-group-list'] = [];
                    loadChartData(chart, res, title, "", new Date($scope.startTime).getTime(), 60000, needMark, groupByCount, groupByLinkFun);
                }
            }
        ).finally(
            function () {
                $scope.deactivateBtEnabled = false;
            }
        );
    };
    $scope.queryVGroupData = function () {
        $scope.loadAllCharts($('#deactivateVGroupRequestConfirmModal'));
    };
    $scope.queryGroupData = function () {
        $scope.loadAllCharts($('#deactivateActivatedGroupModal'));
    };


    // Traffic policy Area
    $scope.confirmRemovePolicy = function () {
        $('#askRemovePolicyModal').modal('hide');
        var policyId = selectedPolicyObject.id;

        var param = {
            policyId: policyId
        };
        var request = {
            method: 'GET',
            url: G.baseUrl + '/api/policy/delete',
            params: param
        };
        $('#operationConfrimModel').modal("show").find(".modal-title").html("删除 Traffic Policy");
        $('#operationConfrimModel').modal("show").find(".modal-body").html("正在删除.. <img src='/static/img/spinner.gif' />");
        $scope.processRequest(request, $('#operationConfrimModel'), '删除traffic policy', '删除');
    };

    // Helpers
    $('.closeProgressWindowBt').click(
        function (e) {
            var hashData = {};
            hashData.timeStamp = new Date().getTime();
            // sleep 100 for data change
            setTimeout(function () {
                H.setData(hashData);
            }, timeout);
        }
    );
    $('.closeProgressWindowBt2').click(
        function (e) {
            if ($('#deleteGroupConfirmModel').find('.fa-check').length > 0) {
                // Success
                window.location.href = "/portal/groups#?env=" + G.env + "&timeStamp=" + new Date().getDate();
            }
            if ($('#deleteGroupConfirmModel').find('.fa-times') > 0) {
                // Success
            }

        }
    );

    /**** Common functions****/
    function diffTwoGroup(targetDiv, baseText, newText, baseVersion, newVersion) {
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

    function reviewData(id) {
        var result = true;
        $.each(id.find('[data-validator-type="validation"]'), function (i, element) {
            $(element).bootstrapValidation();
            if (!$(element).bootstrapValidation('validate'))
                result = false;
        });
        return result;
    };

    $scope.forceUpdateRequest;
    $scope.processRequest = function (request, confirmDialog, operationText, tooltipText) {
        confirmDialog.find(".modal-title").html(operationText);
        $scope.forceUpdateRequest = $.extend(true, {}, request);
        if (!$scope.forceUpdateRequest.params) {
            $scope.forceUpdateRequest.params = {
                force: true
            }
        } else {
            $scope.forceUpdateRequest.params.force = true;
        }

        var msg = "";
        var errorcode = "";
        $http(request).success(
            function (res, code) {
                var errText = '';
                if (code != 200) {
                    msg = res.message;
                    errorcode = res.code;
                    if (!msg) {
                        msg = code;
                    }
                    errText = "<span class='fa fa-times'></span><span style='padding-left: 10px'>" + operationText + "失败</span>";
                    confirmDialog.modal('show').find(".modal-title").html(errText);
                    confirmDialog.modal('show').find(".modal-body").html("失败原因:" + msg);
                    $scope.showForceUpdate = true;
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

    $scope.hasActivateright = function () {
        var id = undefined;
        id = $scope.information.extendedGroup.id;
        if (id) {
            return !A.canDo('Group', 'ACTIVATE', id);
        }

        return true;
    };
    $scope.cancelVersionCompare = function (refresh) {
        /*     var pair={
                 timeStamp: new Date().getTime(),
                 compareVersions:false,
                 online:false,
                 offline:false
             };
             H.setData(pair);
             if(refresh){
                 location.reload();
             }*/
    };
    $scope.getActivateRightApplyLink = function () {
        var path = G.baseUrl + '/api/auth/apply/mail?userName=' + $scope.query.user + '&op=ACTIVATE&type=Group&targetId=' + $scope.query.groupId + '&env=' + $scope.env;
        var request = {
            method: 'GET',
            url: path
        };
        $http(request).success(
            function (response, code) {
                alert('权限申请已经发出，请等待审批。');
            }
        );
    };
    var groupApp;
    // Hash Changed
    $scope.hashChanged = function (hashData) {
        $scope.resource = H.resource;
        $scope.resource = H.resource;
        $scope.showForceUpdate = false;
        $scope.forceUpdateRequest = {};
        $scope.env = 'pro';

        if (hashData.compareVersions) {
            $scope.query.compareVersions = true;
        } else {
            $scope.query.compareVersions = false;
        }
        if (hashData.env) {
            $scope.env = hashData.env;
        }
        if (hashData.groupId) {
            $scope.query.groupId = hashData.groupId;
        }
        if (hashData.groupName) {
            $scope.query.groupName = hashData.groupName;
        }
        if (hashData.cmsGroupId) {
            $scope.query.cmsGroupId = hashData.cmsGroupId;
        }
        if (hashData.right) {
            $scope.query.right = hashData.right;
        }
        if ($scope.query.groupId) {
            // ignore cmsGroupId if group id is not undefined
            $scope.query.cmsGroupId = '';
            $scope.query.groupName = '';
        } else {
            if ($scope.query.groupName) {
                $scope.query.cmsGroupId = '';
            }
        }

        groupApp = new GroupApp(hashData, $http, $q, $scope.env);
        $scope.initTable();
        // init the wreches
        $scope.tableOps.health.showOperations = false;
        $scope.tableOps.virtualServer.showOperations = false;

        $scope.tableOps.health.showMoreColumns = false;
        $scope.tableOps.virtualServer.showMoreColumns = false;
        $('#group-virtual-server-table').bootstrapTable("removeAll");
        $('#group-health-table').bootstrapTable("removeAll");
        // $('.group-basic-section').showLoading();
        $('#group-health-table').bootstrapTable('showLoading');
        $('#group-virtual-server-table').bootstrapTable('showLoading');
        /*$('#group-policy-table').bootstrapTable('showLoading');*/
        $scope.preModifyHealthCheck = false;
        if (hashData.preModifyHealthCheck) {
            $scope.preModifyHealthCheck = hashData.preModifyHealthCheck == 'true';
        }

        // load the traffic echarts
        $scope.setDateNow();
        $scope.chart.endTime = new Date($scope.chart.startTime.getTime() + 1000 * 60 * 90);

        if ($scope.query.groupName) {
            $scope.preLoadGroupNameData();
            return;
        }

        if ($scope.query.cmsGroupId) {
            $scope.preLoadCMSData();
            return;
        }

        if ($scope.query.compareVersions) {
            // open the compare different version dialog
            var online = hashData.online;
            var offline = hashData.offline;

            $scope.diffVersions = [online, offline];
            if (online && offline) {
                $scope.diffGroupBetweenVersions(true);
            }
        }
        $scope.loadData();
    };
    H.addListener("groupInfoApp", $scope, $scope.hashChanged);

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

    function rightNotify(title, message, url) {
        var notify = $.notify({
            icon: 'fa fa-lock',
            title: title,
            message: message,
            url: url,
            target: '_self'
        }, {
            type: 'warning',
            allow_dismiss: true,
            newest_on_top: false,
            placement: {
                from: 'bottom',
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
            spacing: 15,
            z_index: 1031,
            mouse_over: 'pause'
        });

        return notify;
    }

    function successNotify(title, message, url) {
        var notify = $.notify({
            icon: 'fa fa-check',
            title: title,
            message: message,
            url: url,
            target: '_self'
        }, {
            type: 'success',
            allow_dismiss: true,
            newest_on_top: false,
            placement: {
                from: 'bottom',
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
            delay: 2000,
            spacing: 5,
            z_index: 1031,
            mouse_over: 'pause'
        });

        return notify;
    }

    function IsJsonString(str) {
        try {
            JSON.parse(str);
        } catch (e) {
            return false;
        }
        return true;
    }

    function diffTwoGroup(targetDiv, baseText, newText, baseVersion, newVersion) {
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

    /***END OF COMMON FUNCTIONS***/
});
angular.bootstrap(document.getElementById("group-info-area"), ['groupInfoApp']);

var groupStatusApp = angular.module('groupStatusApp', ['http-auth-interceptor']);
groupStatusApp.controller('groupStatusController', function ($scope, $http, $q) {
    var healthInitialStatus = [];
    $scope.data = {
        requestHeaders: []
    };
    var log_start = new Date().getTime() - 1000 * 60 * 60;
    var log_end = new Date().getTime() + 1000 * 60 * 60;

    var dashboardStartTime = $.format.date(log_start, 'yyyy-MM-dd HH:mm:ss');
    var dashboardEndTime = $.format.date(log_end, 'yyyy-MM-dd HH:mm:ss');
    var timeout = 120;
    var memberTobeUpdated_bk;
    $scope.groupHealthObjects = {};
    // Show Hide
    $scope.disabledAnchor = function (x) {
        var p1 = A.canDo("Ip", "OP_SERVER", '*');
        var p2 = A.canDo("Group", "OP_MEMBER", groupId);

        var p3 = A.canDo("Group", "OP_PULL", groupId);
        var p4 = A.canDo("Group", "OP_HEALTH_CHECK", groupId);

        var c = '';
        switch (x) {
            case 'server': {
                if (!p1) c = 'disabledAnchor';
                break;
            }
            case 'member': {
                if (!p2) c = 'disabledAnchor';
                break;
            }
                ;
            case 'health': {
                if (!p4) c = 'disabledAnchor';
                break;
            }
                ;
            case 'pull': {
                if (!p3) c = 'disabledAnchor';
                break;
            }
                ;
            default :
                break;
        }
        return c;
    };

    $('#group-status-table').on('check.bs.table uncheck.bs.table ' +
        'check-all.bs.table uncheck-all.bs.table all.bs.table', function () {
        var len = !$('#group-status-table').bootstrapTable('getSelections').length;

        $('#group-batch-delete-bt').prop('disabled', len);

        var p1 = A.canDo("Ip", "OP_SERVER", '*');
        var p2 = A.canDo("Group", "OP_MEMBER", '*');

        var p3 = A.canDo("Group", "OP_PULL", '*');
        var p4 = A.canDo("Group", "OP_HEALTH_CHECK", '*');

        var op = p1 || p2 || p3 || p4;

        $('.batch-operation-text').prop('disabled', len & op);
        $('.batch-operation-icon').prop('disabled', len & op);

    });

    var i = 0;
    $('#group-status-table').on('check-all.bs.table', function () {
        if (i == 0) {
            i++;
            $('#group-status-table').bootstrapTable('togglePagination').bootstrapTable('checkAll').bootstrapTable('togglePagination');
        }
    });
    $('#group-status-table').on('uncheck-all.bs.table', function () {
        i = 0;
    });

    // DATA
    $scope.info = {
        "memberStatus": [],
        "members": [],
        "membersTobeRemoved": [],
        "offlineGroup": {},
        "onlineGroup": {},
        "policies": [],
        "groupHealths": {},
        groupUnheathy: [],
        memberLogs: {}
    };
    $scope.tableOps = {
        member: {
            showMoreColumns: false,
            showOperations: false
        }
    }
    $scope.query = {
        removeReason: ''
    }
    // Load Group status and group information data
    $scope.loadMetaData = function () {
        var parameters = {
            "groupId": groupId
        };
        var logParameter = {
            targetId: groupId,
            op: 'downMember',
            type: 'Group'
        };
        var request = {
            method: 'GET',
            url: G.baseUrl + '/api/status/group',
            params: parameters
        };
        var request2 = {
            method: 'GET',
            url: G.baseUrl + '/api/group',
            params: parameters
        };
        var request3 = {
            method: 'GET',
            url: G.baseUrl + '/api/group?mode=online',
            params: parameters
        };
        var request4 = {
            method: 'GET',
            url: G.baseUrl + '/api/diagnose/group/query',
            params: parameters
        };
        var request5 = {
            method: 'GET',
            url: G.baseUrl + '/api/logs',
            params: logParameter
        };
        var offlineGrpservers = [];
        var onlineGrpservers = [];
        $q.all(
            [
                $http(request).success(function (response) {
                    // bind the data
                    var temp = (response["group-server-statuses"] == undefined ? [] : response["group-server-statuses"]);
                    // sort the member status
                    /*var temp =$scope.info.memberStatus;*/
                    $scope.info.memberStatus = temp;

                    var hasMemberDownInstance = _.filter(temp, function (v) {
                        return v.member == false;
                    });
                    var hasHealthyDownInstance = _.filter(temp, function (v) {
                        return v.healthy == false;
                    });
                    if (hasMemberDownInstance && hasMemberDownInstance.length > 0) {
                        $scope.info.memberStatus = _.sortBy(temp, function (s) {
                            return s.member;
                        });
                    } else {
                        if (hasHealthyDownInstance && hasHealthyDownInstance.length > 0) {
                            $scope.info.memberStatus = _.sortBy(temp, function (s) {
                                return s.healthy;
                            });
                        }
                    }
                }),
                $http(request2).success(function (response) {
                    $scope.info.offlineGroup = response;
                    offlineGrpservers = response['group-servers'];
                }),
                $http(request3).success(function (response) {
                    $scope.info.onlineGroup = response;
                    // online groups
                    if (response['group-servers']) {
                        onlineGrpservers = response['group-servers'];
                    }
                }),
                $http(request4).success(function (response) {
                    $scope.info.groupUnheathy = response;
                }),
                $http(request5).success(function (response) {
                    $scope.info.memberLogs = response;
                })
            ]
        ).then(
            function () {
                healthInitialStatus = [];
                _.each($scope.info.memberStatus, function (v) {
                    var e = _.find(offlineGrpservers, function (item) {
                        return item.ip == v.ip && item.port == v.port;
                    });
                    var f = _.find(onlineGrpservers, function (item) {
                        return item.ip == v.ip && item.port == v.port;
                    });
                    var host;
                    var weight;
                    var port;
                    var ip;
                    if (e) {
                        host = e["host-name"];
                        weight = e.weight;
                        port = e.port;
                        ip = e.ip;
                    }
                    else {
                        host = f["host-name"];
                        weight = f.weight;
                        port = f.port;
                        ip = f.ip;
                    }
                    v.hostName = host;
                    v.weight = weight;

                    var loading = '<img style="height: 20px" src=\'/static/img/spinner.gif\' /> ';
                    healthInitialStatus.push(
                        {
                            ip: ip,
                            hostName: host,
                            port: port,
                            code: loading,
                            timeout: loading,
                            message: loading
                        }
                    );
                });
                // member logs analyse:
                $scope.info.memberLogs = $scope.getMemberOperationLogData($scope.info.memberLogs);

                $scope.getGroupHealthData($scope.info.offlineGroup, function () {
                    $scope.loadTable();
                });
            }
        ).finally(
            function () {
                $scope.loadTable();

                if ($scope.preHealthCheck) {
                    // trigger precheck
                    $scope.popCheckHealthWindow();
                }
            }
        );
    }
    // Member add operation object
    $scope.groupServersTable = {
        add: function (index) {
            var member = {};
            member["host-name"] = "";
            member.ip = "";
            member.port = "80";
            member.weight = "5";
            member["max-fails"] = "0";
            member["fail-timeout"] = "30";

            $scope.info.members.push(member);
        },
        remove: function (index) {
            $scope.info.members.splice(index, 1);
        }
    };

    // Table definition
    function isJson(str) {
        try {
            JSON.parse(str);
        } catch (e) {
            return false;
        }
        return true;
    }

    $scope.initTable = function () {
        var resource = $scope.resource;
        if (!resource || _.keys(resource).length == 0) return;

        $('#group-status-table').bootstrapTable({
            toolbar: "#toolbar-group-status",
            columns: [[
                {
                    checkbox: 'true',
                    align: 'center',
                    valign: 'middle'
                },
                {
                    field: 'ip',
                    title: 'IP',
                    align: 'left',
                    sortable: true,
                    formatter: function (value, row) {
                        return '<a target="_blank" href="' + G[G.env].urls.cat + '/cat/r/t?domain=' + $scope.info.offlineGroup["app-id"] + '&ip=' + value + '">' + value + ' / ' + row.hostName + '</a>';
                    },
                    valign: 'middle'
                },
                {
                    field: 'port',
                    title: 'Port',
                    align: 'center',
                    valign: 'middle',
                    sortable: true
                },
                {
                    field: 'weight',
                    title: 'Weight',
                    align: 'center',
                    valign: 'middle',
                    sortable: true
                },
                {
                    field: 'server',
                    title: 'Server Status',
                    align: 'center',
                    valign: 'middle',
                    events: StatusChangeEvent,
                    sortable: true,
                    formatter: function (value) {
                        var str = "";
                        var p1 = (A.canDo("Ip", "OP_SERVER", groupId) && ($scope.optionPanelState || $scope.tableOps.member.showOperations)) ? "" : "hide";

                        if (p1 == "hide") {
                            if (value == true) {
                                str = "<span class='fa fa-circle status-green' title=' Server在线'></span>";
                            }
                            else {
                                str = "<span class='ServerOp fa fa-circle status-red' title='Server下线'></span>";
                            }
                        }
                        else {
                            if (value == true) {
                                str = "<span class='ServerOp fa fa-toggle-on status-green' title='关停 Server'></span>";
                            }
                            else {
                                str = "<span class='ServerOp fa fa-toggle-on status-red' title='打开 Server'></span>";
                            }
                        }
                        return str;
                    }
                },
                {
                    field: 'member',
                    title: 'Member Status',
                    align: 'center',
                    valign: 'middle',
                    sortable: true,
                    events: StatusChangeEvent,
                    formatter: function (value, row, index) {
                        var ip = row.ip;
                        var str = "";
                        var p1 = (A.canDo("Group", "OP_MEMBER", groupId) && ($scope.optionPanelState || $scope.tableOps.member.showOperations)) ? "" : "hide";
                        if (p1 == "hide") {
                            if (value == true) {
                                str = "<span class='fa fa-circle status-green member-op' title='Member在线'></span>";
                            }
                            else {

                                var reason = $scope.info.memberLogs[ip];
                                var tooltip = 'Member 被拉出';
                                if (reason && reason.length > 0) {
                                    reason = reason[0];
                                    var reasonData = JSON.parse(reason['data']);
                                    var desc = reasonData['description'] || '用户未填写';
                                    tooltip += '' +
                                        '&#13;拉出人：' + reason['user-name'] +
                                        '&#13;拉出时间: ' + reason['date-time'] + '&#13;拉出原因: ' + desc;
                                }
                                str = "<span class='fa fa-circle status-red member-op' title='" + tooltip + "'></span>";
                            }
                        }
                        else {
                            if (value == true) {
                                str = "<span class='MemberOp fa fa-toggle-on status-green' title='拉出 Member'></span>";
                            }
                            else {
                                str = "<span class='MemberOp fa fa-toggle-on status-red' title='拉入 Member'></span>";
                            }
                        }
                        return str;
                    }
                },
                {
                    field: 'pull',
                    title: 'Pull Status',
                    align: 'center',
                    events: StatusChangeEvent,
                    valign: 'middle',
                    sortable: true,
                    formatter: function (value) {
                        var str = "";
                        var p3 = (A.canDo("Group", "OP_PULL", groupId) && ($scope.optionPanelState || $scope.tableOps.member.showOperations)) ? "" : "hide";
                        if (p3 == "hide") {
                            if (value == true) {
                                str = "<span class='fa fa-circle status-green' title='Pull在线'></span>";
                            }
                            else {
                                str = "<span class='fa fa-circle status-red' title='Pull线下'></span>";
                            }
                        }
                        else {
                            if (value == true) {
                                str = "<span class='PullOp fa fa-toggle-on status-green' title='Pull 拉出'></span>";
                            }
                            else {
                                str = "<span class='PullOp fa fa-toggle-on status-red' title='Pull 拉入'></span>";
                            }
                        }

                        return str;
                    }
                },
                {
                    field: 'healthy',
                    title: 'Health Status',
                    align: 'center',
                    valign: 'middle',
                    sortable: true,
                    events: healthEvent,
                    formatter: function (value, row, index) {
                        var str = "";
                        var p3 = (A.canDo("Group", "OP_HEALTH_CHECK", groupId) && ($scope.optionPanelState || $scope.tableOps.member.showOperations)) ? "" : "hide";
                        if (p3 == 'hide') {
                            if (value) {
                                str = '<div title="健康检测成功" port="' + row.port + '" host="' + row.hostName + '" tag="' + row.ip + "/" + row.port + '" data-toggle="tooltip" class="HealthState HealthStateFailure fa fa-circle status-green"> </div>';
                            } else {
                                if ($scope.groupHealthObjects[row.ip + "/" + row.port]) {
                                    str = '<div port="' + row.port + '" host="' + row.hostName + '" data-toggle="tooltip" tag="' + row.ip + "/" + row.port + '" title="健康检测失败，失败原因:' + $scope.groupHealthObjects[row.ip + "/" + row.port].lastFailedCause + '" class=" HealthStateFailure fa fa-circle status-red"></div>';
                                }
                                else {
                                    str = '<div port="' + row.port + '" host="' + row.hostName + '" tag="' + row.ip + "/" + row.port + '" data-toggle="tooltip" title="健康检测失败，失败原因. 未发现" class=" HealthStateFailure fa fa-circle status-red"></div>';
                                }
                            }
                        } else {
                            if (value) {
                                str = '<div title="健康检测Fall" port="' + row.port + '" host="' + row.hostName + '" tag="' + row.ip + "/" + row.port + '" data-toggle="tooltip" class="health-op fa fa-toggle-on status-green"> </div>';
                            } else {
                                str = '<div title="健康检测Raise" port="' + row.port + '" host="' + row.hostName + '" data-toggle="tooltip" tag="' + row.ip + "/" + row.port + '" title="Health Raise" class="health-op  fa fa-toggle-on status-red"></div>';
                            }
                        }
                        return str;
                    }
                },
                {
                    field: 'next-status',
                    title: 'Status',
                    align: 'center',
                    valign: 'middle',
                    sortable: true,
                    formatter: function (value) {
                        var statusStr = "";
                        switch (value) {
                            case "ToBeOffline": {
                                statusStr = '<span class="member-status status-red">' + resource["group"]["group_groupStatusApp_nextstatus"]["待下线"] + '</span>';
                                break;
                            }
                            case "ToBeOnline": {
                                statusStr = '<span class="member-status status-gray">' + resource["group"]["group_groupStatusApp_nextstatus"]["待上线"] + '</span>';
                                break;
                            }
                            case "Online": {
                                statusStr = '<span class="member-status status-green">' + resource["group"]["group_groupStatusApp_nextstatus"]["已上线"] + '</span>';
                                break;
                            }
                            default: {
                                statusStr = "待定...";
                                break;
                            }
                        }
                        return statusStr;
                    }
                },
                {
                    field: 'hickwall',
                    title: 'Links',
                    align: 'center',
                    valign: 'middle',
                    width: '300px',
                    formatter: function slbServersRemoveFormatter(value, row, index) {
                        var env = $scope.env;
                        env == 'pro' ? 'PROD' : env.toUpperCase();

                        var groupId = $scope.info.offlineGroup.id;
                        var appId = $scope.info.offlineGroup['app-id'];
                        var ip = row['ip'];
                        var port = row['port'];
                        var startTime = new Date().getTime() - 60 * 1000;
                        var endTime = new Date().getTime();
                        startTime = $.format.date(startTime, 'yyyy-MM-dd_HH:mm:ss');
                        endTime = $.format.date(endTime, 'yyyy-MM-dd_HH:mm:ss');


                        var dashboard = G.dashboardportal + '/#env=' + env + '&metric-name=slb.req.count&interval=1m&start-time=' + dashboardStartTime + '&end-time=' + dashboardEndTime + '&chart=line&aggregator=sum&ts=1515983913677&tags={"group_id":["' + groupId + '"],"group_server":["' + ip + ':' + port + '"]}&group-by=[status]';


                        var esLink = '';
                        var clogLink = 'fromDate=' + startTime + '~toDate=' + endTime + '~app=' + appId + '~~hostSearch=' + ip;

                        var query = "group_id%3D'" + $scope.info.offlineGroup.id + "'%20AND%20group_server%3D'" + ip + "*'";
                        if (G[env] && G[env].urls.es) {
                            esLink = groupApp.getEsHtml(query);
                        }

                        var nginxLink;
                        if (G[env] && G[env].urls['tengine-es']) {
                            nginxLink = G[env].urls['tengine-es'] + "query=upstream_name%3D'backend_" + groupId + "'%20AND%20upstreamAddr%3D'" + ip + "*'";
                        }

                        var str = '<div style="width: 340px;">';

                        if (G[env] && G[env]['urls']['dashboard']) {
                            str += '<div class="system-link">' +
                                '<a class="pull-left dashboard" title="Dashboard" target="_blank" href=\'' + dashboard + '\'>Dashboard</a>' +
                                '</div>';
                        }
                        if (G[env] && G[env].urls.hickwall) {
                            str += '<div class="system-link">' +
                                '<a class="pull-left hickwall" title="Hickwall" target="_blank" href="' + G[G.env].urls.hickwall + '/d/8GaUPxgZz/slb-server-group?orgId=6&var-group_id=' + $scope.info.offlineGroup.id + '&var-group_server=' + row['ip'] + ':' + row['port'] + '">Hickwall</a>' +
                                '</div>';
                        }

                        if (G[env] && G[env].urls.cat) {
                            str += '<div class="system-link">' +
                                '<a class="pull-right cat" target="_blank" href="' + G[G.env].urls.cat + '/cat/r/h?domain=' + appId + '&ip=' + ip + '">CAT</a>' +
                                '</div>';
                        }
                        if (G[env] && G[env].urls.es) {
                            str += esLink;
                        }

                        if (G[env] && G[env].urls['tengine-es']) {
                            str += '<div class="system-link">' +
                                '<a class="pull-right tengine" target="_blank" href="' + nginxLink + '">NginxLog</a>' +
                                '</div>';
                        }

                        if (G[env] && G[env].urls.clog) {
                            str += '<div class="system-link">' +
                                '<a class="pull-right clog" target="_blank" href="' + G[G.env].urls.clog + clogLink + '">Clog</a>' +
                                '</div>';
                        }


                        str += '</div>';
                        return str;
                    }
                },
                {
                    field: 'operate',
                    title: 'Operation',
                    align: 'center',
                    valign: 'middle',
                    sortable: true,
                    width: '120px',
                    events: StatusChangeEvent,
                    formatter: function (row, value, index) {
                        var str = "-";
                        var p1 = "";
                        if (!A.canDo("Group", "UPDATE", groupId)) {
                            p1 = "hide";
                        }
                        if (value != undefined && value["next-status"] != 'ToBeOffline') {
                            str = '<button type="button" class="btn-op btn btn-info btn-little decrease-member-bt  ' + p1 + '" title="缩容" aria-label="Left Align" ><span class="fa fa-minus"></span></button>';
                            str += '<button data-dismiss="modal" style="margin-left:5px" type="button" class="btn-little btn-op btn btn-info update-member-bt  ' + p1 + '" title="修改" aria-label="Left Align" ><span class="fa fa-edit"></span></button>';
                        }
                        else {
                            str = '<button type="button" class="btn btn-info revert-decrease-bt  ' + p1 + '" title="Undo" aria-label="Left Align" ><span class="fa fa-undo"></span></button>';
                        }
                        return str;
                    }
                }
            ], []],
            search: true,
            showRefresh: true,
            showColumns: true,
            sortName: 'ip',
            sortOrder: 'desc',
            data: $scope.info.memberStatus,
            classes: "table-bordered  table-hover table-striped table",
            minimumCountColumns: 2,
            pagination: true,
            pageSize: 20,
            sidePagination: 'client',
            pageList: [20, 40, 80, 200],
            responseHandler: "responseHandler",
            idField: 'ip',
            formatLoadingMessage: function () {
                return "<img class='loading-img' src='/static/img/loading_100.gif' /> 正在加载 Group Servers 信息";
            },
            formatNoMatches: function () {
                return '<i class="fa status-alert fa-exclamation-triangle" aria-hidden="true"></i> 没有找到匹配的 Group Servers';
            }
        });
        $('#health-check-result-table').bootstrapTable({
            toolbar: "#health-check-result-toolbar",
            columns: [[
                {
                    field: 'ip',
                    title: 'Host',
                    align: 'left',
                    formatter: function (value, row) {
                        return '<a target="_blank" href="' + G[G.env].urls.cat + '/cat/r/t?domain=' + $scope.info.offlineGroup["app-id"] + '&ip=' + value + '">' + value + ' / ' + row.hostName + '</a>';
                    },
                    valign: 'middle'
                },
                {
                    field: 'port',
                    title: 'Port',
                    align: 'center',
                    valign: 'middle',
                    sortable: true
                },
                {
                    field: 'code',
                    title: 'Code',
                    align: 'center',
                    valign: 'middle',
                    sortable: true
                },
                {
                    field: 'timeout',
                    title: 'Response Time',
                    align: 'center',
                    valign: 'middle',
                    sortable: true,
                    formatter: function (value) {
                        if (value == 0) return '504';
                        return value;
                    }
                },
                {
                    field: 'message',
                    title: 'Message',
                    align: 'center',
                    valign: 'middle',
                    sortable: true,
                    formatter: function (value, row, index) {
                        if (row.code == "0") value = "ConnectionTimeout";

                        if (!value) return '-';

                        if (value && value.toLowerCase().trim() == 'ok') {
                            return '<div class="status-green">' + value + '</div>'
                        } else {
                            return '<div class="status-red" >' + value + '</div>'
                        }
                    }
                }
            ], []],

            search: true,
            showRefresh: true,
            showColumns: true,
            pagination: true,
            sortName: 'ip',
            sortOrder: 'desc',
            classes: "table-bordered  table-hover table-striped table",
            minimumCountColumns: 2,
            pageSize: 20,
            pageList: [20, 40, 80, 200],
            responseHandler: "responseHandler",
            idField: 'ip',
            formatLoadingMessage: function () {
                return "<img class='loading-img' src='/static/img/loading_100.gif' /> 健康检测正在运行...";
            },
            formatNoMatches: function () {
                return '<i class="fa status-alert fa-exclamation-triangle" aria-hidden="true"></i> 健康检测失败';
            }
        });
    };
    window.healthEvent = {
        'mouseover .HealthStateFailure': function (e, value, row) {
            $(e.target).tooltip();
        },

        'click .health-op': function (e, value, row) {
            var resource = $scope.resource;
            if(!resource || !resource['group']['group_groupStatusApp_doubleConfirmOperationModal']) return;

            var opstatus = resource['group']['group_groupStatusApp_doubleConfirmOperationModal'];


            var targetClass = e.target.className.indexOf("red");
            var state = targetClass > 0 ? false : true;
            selectedClass = "HealthOp";
            selectedMachine = e;
            showOperationModal(opstatus[$(this).attr('title')], row, "health", state);
        },
        'click .HealthStateFailure': function (e, value, row) {
            var callBack = function () {
                var tag = $(e.target).attr('tag');
                var host = $(e.target).attr('host');
                var port = $(e.target).attr('port');
                if (!$scope.groupHealthObjects[tag]) {
                    tag = {
                        appId: '未知',
                        checkURL: '未知',
                        intervals: '未知',
                        totalFails: '未知',
                        totalPasses: '未知',
                        continuousFails: '未知',
                        lastFailedCause: '未知',
                        lastUnknownCause: '未知'
                    };
                } else {
                    tag = $scope.groupHealthObjects[tag];
                }
                var s = '';
                var h = '';
                if (tag.status) {
                    h = tag.status.toLowerCase();
                }
                var f = '';
                if (row.healthy) s = '<b class="status-green">成功</b>';
                else s = '<b class="status-red">失败</b>';

                switch (h) {
                    case 'unavailable':
                        h = '<b class="status-red">失败</b>';
                        break;
                    case 'available':
                        h = '<b class="status-green">成功</b>';
                        break;
                    default:
                        h = '<b class="status-yellow">初始化中</b>';
                        break;
                }
                if (tag.lastFailedCause == '') {
                    f = '无';
                }
                else {
                    f = tag.lastFailedCause + tag.lastUnknownCause;
                }
                var str = '<div class="" style="margin: 0 auto">' +
                    '<table class="health-table-pop table ng-scope ng-table">' +
                    ' <tr><td><b>AppId:</b></td>' +
                    ' <td>' + tag.appId + '</td>' +
                    ' </tr>' +
                    ' <tr><td><b>GroupId:</b></td>' +
                    ' <td>' + tag.groupId + '</td>' +
                    ' </tr>' +
                    ' <tr><td><b>Member:</b></td>' +
                    ' <td>' + tag.ip + '/' + host + '</td>' +
                    ' </tr>' +
                    ' <tr><td><b>Member端口:</b></td>' +
                    ' <td>' + port + '</td>' +
                    ' </tr>' +
                    ' <tr><td><b>健康监测URL:</b></td>' +
                    ' <td style="word-break: break-all"><a href="' + tag.checkURL + '" target="_blank">' + tag.checkURL + '</a></td>' +
                    ' </tr>' +
                    ' <tr><td><b>请求频率:</b></td>' +
                    ' <td>每 ' + tag.intervals + ' 毫秒一次</td>' +
                    ' </tr>' +
                    ' <tr><td><b>超时设置</b>:</b></td>' +
                    ' <td>' + tag.timeout / 1000 + ' 秒</td>' +
                    ' </tr>' +
                    ' <tr><td><b>总成功次数:</b></td>' +
                    ' <td>' + tag.totalPasses + '</td>' +
                    ' </tr>' +
                    ' <tr><td><b>总失败次数:</b></td>' +
                    ' <td>' + tag.totalFails + '</td>' +
                    ' </tr>' +
                    ' <tr><td><b>持续成功次数:</b></td>' +
                    ' <td>' + tag.continuousPasses + '</td>' +
                    ' </tr>' +
                    ' <tr><td><b>持续失败次数:</b></td>' +
                    ' <td><b class="status-red">' + tag.continuousFails + '</b></td>' +
                    ' </tr>' +
                    ' <tr><td><b>SLB中状态:</b></td>' +
                    ' <td>' + s + '</td>' +
                    ' </tr>' +
                    ' <tr><td><b>健康检测状态:</b></td>' +
                    ' <td>' + h + '</td>' +
                    ' </tr>' +
                    ' <tr><td><b>最后失败原因:</b></td>' +
                    ' <td style="word-break: break-all">' + f + '</td>' +
                    ' </tr>' +
                    ' </table>' +
                    '  </div>';
                $('#HealthCheckerFailResultModal .modal-body').html(str);
                $('#HealthCheckerFailResultModal').modal("show");
            }

            $scope.getGroupHealthData($scope.info.offlineGroup, callBack);
        }
    };

    $('#group-status-table').on('click-row.bs.table', function (e, arg1, arg2, arg3, arg4) {

    });
    // Partial show
    $scope.disableOpenMember = function () {
        var can = A.canDo('Group', 'UPDATE', $scope.info.offlineGroup.id);
        return !can;
    };
    $scope.getMemberShowMore = function () {
        return $scope.tableOps.member.showMoreColumns ? 'fa fa-list-alt text-info' : 'fa fa-list-alt font-gray';
    };
    $scope.getMemberShowOperation = function () {
        return $scope.tableOps.member.showOperations ? 'fa fa-wrench text-info' : 'fa fa-wrench font-gray';
    };
    $scope.toggleShowMoreMemberColumns = function () {
        $scope.tableOps.member.showMoreColumns = !$scope.tableOps.member.showMoreColumns;
    };
    $scope.toggleShowMemberOperations = function ($event) {
        // get event position
        var offset = document.getElementById('members-area').offsetTop;
        window.scrollTo(0, offset - 100);

        var mcan = A.canDo('Group', 'OP_MEMBER', $scope.info.offlineGroup.id);
        if (mcan) {
            $scope.tableOps.member.showOperations = !$scope.tableOps.member.showOperations;

            if ($scope.tableOps.member.showOperations)
                $('#group-status-table').bootstrapTable('showColumn', 'operate');
            else
                $('#group-status-table').bootstrapTable('hideColumn', 'operate');
        } else {
            // this dismiss
            var accessLink = '<a class="right-bt-access" href="/portal/group#?env=' + G.env + '&right=member&groupId=' + groupId + '&timeStamp=' + new Date().getTime() + '">点此申请</a>';
            rightNotify('<strong>Oops ...</strong>', '您还没有服务器拉入、拉出权限。 请 ' + accessLink, '');
        }
    };
    $scope.getMemberOperationTitle = function () {
        return $scope.tableOps.member.showOperations ? '关闭操作' : '打开操作';
    };
    $scope.getMemberShowMoreTitle = function () {
        return $scope.tableOps.member.showOperations ? '显示简略信息' : '显示详细信息';
    };

    // Health checker
    $scope.popCheckHealthWindow = function () {
        var c = $scope.info.offlineGroup;

        var m = $scope.info.memberStatus;

        var healthbody = $scope.info.offlineGroup['health-check'];
        var t = healthbody ? healthbody.timeout : 2000;

        var uri = '';
        var port = 80;
        var ips = [];
        var protocol = 'HTTP';
        var domains = [];
        if (c['health-check']) {
            uri = c['health-check'].uri;
        }
        protocol = c['ssl'] ? 'HTTPS' : 'HTTP';
        $.each(c['group-virtual-servers'], function (i, item) {
            var vsDomains = item['virtual-server'].domains;
            $.each(vsDomains, function (j, item2) {
                if (domains.indexOf(item2.name) == -1) domains.push(item2.name);
            });
        });
        $.each(c['group-servers'], function (i, item) {
            ips.push(item['ip'].trim());
            port = item['port'];
        });
        $scope.healthCheckOriginData = {
            uri: uri,
            hosts: domains,
            defaultHost: domains[0],
            members: m,
            timeout: t,
            ips: ips,
            port: port,
            protocol: protocol
        };

        $scope.healthdata = domains[0];
        $('#health-check-result-table').bootstrapTable('load', healthInitialStatus);
        $('.check-summary').remove();
        $('#HealthCheckDialog').modal('show');

        $scope.startCheck();
    };
    $scope.chooseHealthDomain = function (x) {
        $scope.healthdata = x;
    };
    $scope.startCheck = function () {
        var currentProtocol = $scope.healthCheckOriginData.protocol;
        // if current group is an ssl
        var isSSL = $scope.info.offlineGroup.ssl;
        if (isSSL && currentProtocol == 'HTTPS') {
            currentProtocol = 'HTTPS';
        } else {
            currentProtocol = 'HTTP';
        }
        var param = {
            timeout: 60000,
            uri: $scope.healthCheckOriginData.uri,
            host: $scope.healthdata,
            ips: $scope.healthCheckOriginData.ips.toString(),
            port: $scope.healthCheckOriginData.port,
            protocol: currentProtocol
        };

        var headers = $scope.data.requestHeaders;

        var request = {
            method: 'POST',
            url: G.baseUrl + '/api/tools/check',
            params: param,
            data: headers
        };

        $('#health-check-result-table').bootstrapTable('load', healthInitialStatus);

        $http(request).success(
            function (res) {
                if (!res.code) {
                    var s = res.statuses;
                    var b = [];
                    $.each(s, function (i, item) {
                        b.push(
                            {
                                ip: item.ip,
                                port: item.port,
                                hostName: $scope.getHostNameByIpAndPort(item.ip, item.port),
                                timeout: item.time,
                                code: item.code,
                                message: item.message
                            }
                        );
                    });
                    $scope.statuscollection = _.countBy(b, function (r) {

                        if (!r.message) return 'Connection Timeout';
                        return r.message;
                    });
                    $('#health-check-result-table').bootstrapTable('load', b);
                } else {
                    alert('Health Check Failed with Message: ' + res.message);
                }
                $('#health-check-result-table').bootstrapTable('hideLoading');
            }
        );
    };
    $scope.checkResultClass = function (status) {
        return status.toLowerCase() == 'ok' ? 'status-green' : 'status-red';
    }
    $scope.getHostNameByIpAndPort = function (ip, port) {
        var ms = $scope.info.memberStatus;
        var host = _.find(ms, function (item) {
            return item.ip == ip && item.port == port;
        });
        if (host) return host.hostName;
        else return '-';
    };

    // Add New Member Machine area
    $scope.PopNewMember = function () {
        $scope.ResetNewMember();
        $('#addServerModal').modal('show');
    };
    $('#saveBatchMember').click(function () {
        var review = reviewData($('#addBatchServerModal'));
        if (review) {
            var errorLines = [];
            var machines = [];
            var text = $('.machines-textarea').val().trim();
            var lines = text.split('\n');
            $.each(lines, function (i, line) {
                var reg = /^((([a-z]|[A-Z]|[0-9])*)\/((\d+)\.(\d+)\.(\d+)\.(\d+)\/(\d+))\/(\d+))$/g;
                var match = reg.exec(line.trim());
                if (!match) {
                    errorLines.push(i);
                } else {
                    var s = line.split('/');
                    machines.push({
                        'host-name': s[0],
                        ip: s[1],
                        port: s[2],
                        weight: s[3],
                        'max-fails': 0,
                        'fail-timeout': 30
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
                $scope.info.members = machines;
                $('#addBatchServerModal').modal('hide');
                $('#doubleConfirmAddNewMemberModal').modal('show');
            }
        } else {
            return;
        }
    });
    $scope.ResetNewMember = function () {
        $scope.info.members = [];
        var member = {};
        member["host-name"] = "";
        member.ip = "";
        // Get the existing members' ports
        var ports = _.countBy($scope.info.memberStatus, function (obj) {
            return obj.port;
        });
        var max = _.max(ports);

        var port = 80;

        $.each(_.keys(ports), function (k, v) {
            if (ports[v] == max) {
                port = v;
                return;
            }
        });

        member.port = port;
        member.weight = 5;
        member['max-fails'] = 0;
        member['fail-timeout'] = 30;

        $scope.info.members.push(member);
    }
    $('#saveMember').click(function (e) {
        // add validation to the input fields
        $('[data-validator-type="validation"]').bootstrapValidation("reset");
        $('[data-validator-type="validation"]').bootstrapValidation();
        var validate = reviewData($('#addServerModal'));
        if (!validate) return;
        else {
            $('#addServerModal').modal('hide');
            $('#doubleConfirmAddNewMemberModal').modal('show');
        }
    });
    $('#updateMember').click(function (e) {
        // add validation to the input fields
        $('[data-validator-type="validation"]').bootstrapValidation("reset");
        $('[data-validator-type="validation"]').bootstrapValidation();
        var validate = reviewData($('#updateServerModal'));
        if (!validate) return;
        else {
            $('#updateServerModal').modal('hide');
            $('#doubleConfirmUpdateMemberModal').modal('show');
        }
    });
    $scope.addNewMember = function () {
        var membersData = {
            "group-id": groupId,
            "group-servers": []
        };

        var groupOffline = $scope.info.offlineGroup;
        var members = groupOffline['group-servers'];


        $.each($scope.info.members, function (index, member) {
            membersData["group-servers"].push(member);
        });
        // duplicate determine

        var addedServers = membersData['group-servers'];
        var ipsb = _.pluck(addedServers, 'ip');
        var ipsa = _.pluck(members, 'ip');

        if (_.intersection(ipsa, ipsb).length > 0) {
            alert('Not allow to add same ip different port members!');
            return;
        }

        var message = $scope.query.removeReason;

        var req = {
            method: 'POST',
            url: G.baseUrl + '/api/group/addMember',
            data: membersData,
            params: {
                description: message
            }
        };

        $('#doubleConfirmAddNewMemberModal').modal('hide');
        var loading = "<img src='/static/img/spinner.gif' /> 正在扩容";
        $('#memberOperationConfirmModel').modal('show').find(".modal-body").html(loading);
        $('#memberOperationConfirmModel').modal("show").find(".modal-title").html("正在扩容");

        var confrimText = "<span class='warning-important'>注意:新增Member之后需手动激活当前Group方能生效</span>";
        $scope.processRequest(req, $('#memberOperationConfirmModel'), "扩容", confrimText);
    };
    // Batch operations
    $scope.do = function (x) {
        var ipList = [];
        var selectedItems = $('#group-status-table').bootstrapTable('getSelections');

        $.each(selectedItems, function (i, item) {
            ipList.push(item.ip);
        });

        var ips = ipList.toString();

        var p1 = A.canDo("Ip", "OP_SERVER", '*');
        var p2 = A.canDo("Group", "OP_MEMBER", groupId);

        var p3 = A.canDo("Group", "OP_PULL", groupId);
        var p4 = A.canDo("Group", "OP_HEALTH_CHECK", groupId);


        switch (x) {
            case 'sin': {
                if (!p1) return;
                showOperationModal('批量拉入Server', {ip: ips}, 'server', false);
                break;
            }
            case 'sout': {
                if (!p1) return;
                showOperationModal('批量拉出Server', {ip: ips}, 'server', true);
                break;
            }
            case 'min': {
                if (!p2) return;
                showOperationModal('批量拉入Member', {ip: ips}, 'member', false);
                break;
            }
            case 'mout': {
                if (!p2) return;
                showOperationModal('批量拉出Member', {ip: ips}, 'member', true);
                break;
            }
            case 'pin': {
                if (!p3) return;
                showOperationModal('批量Pull拉入', {ip: ips}, 'pull', false);
                break;
            }
            case 'pout': {
                if (!p3) return;
                showOperationModal('批量Pull拉出', {ip: ips}, 'pull', true);
                break;
            }
            case 'hin': {
                if (!p4) return;
                showOperationModal('批量Health拉入', {ip: ips}, 'health', false);
                break;
            }
            case 'hout': {
                if (!p4) return;
                showOperationModal('批量Health拉出', {ip: ips}, 'health', true);
                break;
            }
            default :
                break;
        }
    };
    // Remove server
    $('#group-batch-delete-bt').click(function () {
        PopRemoveMember();
    });
    $('#remove-machine-bt').click(function () {
        var validate = reviewData($('#remove-machine-message'));
        if (!validate) return;

        $('#RemoveServerModal').modal('hide');
        $('#doubleConfirRemoveMemberModal').modal('show');
    });

    $scope.allowRemoveMember = function () {
        $scope.removeMembers();
    };
    $scope.removeMembers = function () {
        var ips = [];

        $.each($scope.info.membersTobeRemoved, function (i, m) {
            ips.push(m.ip);
        });

        var msg = $('#remove-machine-operation-reason').val().trim();
        var params = {
            "groupId": groupId,
            "ip": ips,
            description: msg
        };
        var request = {
            method: 'GET',
            url: G.baseUrl + '/api/group/removeMember',
            params: params
        };
        $('#RemoveServerModal').modal('hide');
        var loading = "<img src='/static/img/spinner.gif' /> 正在下线";
        $('#memberOperationConfirmModel').modal('show').find(".modal-body").html(loading);

        $('#memberOperationConfirmModel').modal("show").find(".modal-title").html("正在下线");
        var confrimText = "<span class='warning-important'>注意: 删除Member之后需手动激活当前Group方能生效</span>";
        $scope.processRequest(request, $('#memberOperationConfirmModel'), "下线" + ips, confrimText);
    };
    $scope.confirmUpdateMember = function () {
        // update group
        var grp = $scope.info.offlineGroup;
        var p = parseInt(memberTobeUpdated_bk.port);

        var index = _.findIndex(grp["group-servers"], function (item) {
            return item.ip == memberTobeUpdated_bk.ip && item.port == p;
        });
        grp["group-servers"].splice(index, 1);
        var m = {
            "ip": $scope.info.memberTobeUpdated.ip,
            "host-name": $scope.info.memberTobeUpdated.hostName,
            "port": $scope.info.memberTobeUpdated.port,
            "weight": $scope.info.memberTobeUpdated.weight
        };
        grp["group-servers"].push(m);

        var data = {
            "group-id": grp.id,
            "group-servers": grp["group-servers"]
        };
        var url = G.baseUrl + "/api/group/updateMember?description=" + $scope.query.updateserverreason;
        // update group
        var request = {
            method: 'POST',
            url: url,
            data: data,
            params: {}
        };
        $scope.processRequest(request, $('#memberOperationConfirmModel'), "更新Group Member", "更新成功");
    };

    // Group table operation
    function PopRemoveMember(row) {
        var str;
        $scope.info.membersTobeRemoved = [];
        $('.to-be-removed-members').children().remove();
        // get the selected items
        var selectedItems = $('#group-status-table').bootstrapTable('getSelections');

        if (selectedItems != undefined && selectedItems.length > 0 && row == undefined) {
            // Batch remove
            $.each(selectedItems, function (index, m) {
                $scope.info.membersTobeRemoved.push({"ip": m.ip, "port": m.port});
                str += "<tr>";
                str += "<td><span class='ip'>" + m.ip + "</span></td>";
                str += "<td><span class='port'>" + m.port + "</span></td>";
                str += "</tr>";
            });
        }
        else {
            if (row) {
                $scope.info.membersTobeRemoved.push({"ip": row.ip, "port": row.port});
                str += "<tr>";
                str += "<td><span class='ip'>" + row.ip + "</span></td>";
                str += "<td><span class='port'>" + row.port + "</span></td>";
                str += "</tr>";
            }
        }
        $('.to-be-removed-members').append(str);
        $('#RemoveServerModal').modal('show');
    };
    // Server,pull,member and remove
    var selectedMachine;
    var selectedClass;
    window.StatusChangeEvent = {
        'click .MemberOp': function (e, value, row) {
            var resource = $scope.resource;
            if(!resource || !resource['group']['group_groupStatusApp_doubleConfirmOperationModal']) return;

            var opstatus = resource['group']['group_groupStatusApp_doubleConfirmOperationModal'];


            var targetClass = e.target.className.indexOf("red");
            var state = targetClass > 0 ? false : true;
            selectedClass = "MemberOp";
            selectedMachine = e;
            showOperationModal(opstatus[$(this).attr('title')], row, "member", state);
        },
        'click .ServerOp': function (e, value, row) {
            var resource = $scope.resource;
            if(!resource || !resource['group']['group_groupStatusApp_doubleConfirmOperationModal']) return;

            var opstatus = resource['group']['group_groupStatusApp_doubleConfirmOperationModal'];

            var targetClass = e.target.className.indexOf("red");
            var state = targetClass > 0 ? false : true;

            selectedClass = "ServerOp";
            selectedMachine = e;
            showOperationModal(opstatus[$(this).attr('title')], row, "server", state);
        },
        'click .PullOp': function (e, value, row) {
            var resource = $scope.resource;
            if(!resource || !resource['group']['group_groupStatusApp_doubleConfirmOperationModal']) return;

            var opstatus = resource['group']['group_groupStatusApp_doubleConfirmOperationModal'];
            
            var targetClass = e.target.className.indexOf("red");
            var state = targetClass > 0 ? false : true;
            selectedClass = "PullOp";
            selectedMachine = e;
            showOperationModal(opstatus[$(this).attr('title')], row, "pull", state);
        },
        'click .decrease-member-bt': function (e, value, row) {
            PopRemoveMember(row);
        },
        'click .update-member-bt': function (e, value, row) {
            $('#updatedMemberObj').val(row.ip + "/" + row.port);
            // $('#updateMemberTipsModal').modal('show');
            $('#allowUpdateMemberBt').click();
        },
        'click .member-op': function (e, value, row) {
            $('#member-pull').html('');
            var ip = row.ip;
            if (!$scope.info.memberLogs[ip]) return;

            var reason = $scope.info.memberLogs[ip][0];
            var reasonData = JSON.parse(reason['data']);
            var pullStatus = reasonData['success'] ? '<span class="status-green">成功</span>' : '<span class="status-red">' + reasonData["error-message"] + '</span>';

            var reasonTxt = '<div class="row">' +
                '<div class="col-md-4">' +
                '拉出时间:' +
                '</div>' +
                '<div class="col-md-8">' +
                reason['date-time'] +
                '</div>' +
                '</div>' +

                '<div class="row">' +
                '<div class="col-md-4">' +
                '拉出人:' +
                '</div>' +
                '<div class="col-md-8">' +
                reason['user-name'] +
                '</div>' +
                '</div>' +

                '<div class="row">' +
                '<div class="col-md-4">' +
                '拉出者IP:' +
                '</div>' +
                '<div class="col-md-8">' +
                reason['client-ip'] +
                '</div>' +
                '</div>' +

                '<div class="row">' +
                '<div class="col-md-4">' +
                '拉出原因:' +
                '</div>' +
                '<div class="col-md-8">' +
                (reasonData['description'] || '用户未填写') +
                '</div>' +
                '</div>' +
                '<div class="row">' +
                '<div class="col-md-4">' +
                '拉出结果:' +
                '</div>' +
                '<div class="col-md-8">' +
                pullStatus +
                '</div>' +
                '</div>';
            $('#member-pull').append(reasonTxt);
            $('#memberPullOutModel').modal('show');
        },
        'click .revert-decrease-bt': function (e, value, row) {
            $('#removeMemberObj').val(row.ip + "/" + row.port);
            $('#discardRemoveMemberBt').click();
        }
    }

    function showOperationModal(title, row, action, status) {
        var resource = $scope.resource;
        if(!resource || !resource['group']['group_groupStatusApp_doubleConfirmOperationModal']) return;

        var opstatus = resource['group']['group_groupStatusApp_doubleConfirmOperationModal'];

        // Pop up dialog definition
        var $modal = $('#operationModal').modal({show: false});
        var ipShow = '';
        if (row.ip.length > 20) {
            ipShow = row.ip.substring(0, 20) + '...';
        } else {
            ipShow = row.ip;
        }
        var $modalDoubleConfirm = $('#doubleConfirmOperationModal').modal({show: false});

        $modal.find('.modal-title').text(title);
        var text = "";
        var ipcount = row.ip.split(',').length;
        if (status) text = opstatus['outconfirmtext']+ipShow;
        else text = opstatus['inconfirmtext']+ipShow;

        switch (action) {
            case "member": {
                $modal.data('op', "member");
                break;
            }
            case "server": {
                $modal.data('op', "server");
                break;

            }
            case "pull": {
                $modal.data('op', "pull");
                break;
            }
            case "health": {
                $modal.data('op', "health");
                break;
            }
            default :
                break;
        }
        //text += ":" + row.ip;
        $modal.find('.tips').text(text);
        $modal.find('.tips').attr('title', row.ip);
        $modalDoubleConfirm.find('.ip-hidden').val(row.ip);
        $modalDoubleConfirm.find('.op-hidden').val(action);
        $modalDoubleConfirm.find('.status-hidden').val(status);
        $modal.modal('show');
    }

    $scope.operationReason = '';
    $('.confirm-operator').click(function (e) {
        var canDo = reviewData($('#operationModal'));
        if (!canDo) return;
        $('#operationModal').modal('hide');
        $('#doubleConfirmOperationModal').modal('show');
    });
    $('.deny-operator').click(function (e) {
        //$scope.loadMetaData();
    });
    $scope.allowUpdateMember = function () {
        $('#updateServerModal').modal("show");
        var arr = $('#updatedMemberObj').val().split('/');
        var ip = arr[0];
        var port = arr[1];

        memberTobeUpdated_bk = {"ip": ip, "port": port};

        $scope.info.memberTobeUpdated = _.find($scope.info.memberStatus, function (item) {
            return item.ip === ip && item.port == port;
        });
    };
    $scope.discardRemoveMember = function () {
        var arr = $('#removeMemberObj').val().split('/');
        var ip = arr[0];
        var port = arr[1];

        var request = {
            method: 'POST',
            url: G.baseUrl + "/api/group/addMember",
            params: {}
        };
        // find the member information from the online group version
        var m = _.find($scope.info.onlineGroup["group-servers"], function (item) {
            return item.ip == ip && item.port == port;
        });
        if (!m) {
            alert("找不到线上版本没法回退!");
            return;
        }
        request.data = {
            "group-id": $scope.info.offlineGroup.id,
            "group-servers": [m]
        };

        $scope.processRequest(request, $('#memberOperationConfirmModel'), "回退", "回退成功");
    };
    $scope.allowOperation = function () {
        var $modal = $('#doubleConfirmOperationModal').modal({show: false});
        var opreason = $scope.operationReason;

        var status = $modal.find('.status-hidden').val();
        var op = $modal.find('.op-hidden').val();
        var ip = $modal.find('.ip-hidden').val();

        var url = G.baseUrl + "/api/op/";
        var operationText = "";
        switch (op) {
            case "member": {
                operationText = "Member";
                url += (status == "true" ? "downMember" : "upMember");
                break;
            }
            case "server": {
                operationText = "Server";
                url += (status == "true" ? "downServer" : "upServer");
                break;
            }
            case "pull": {
                operationText = "Pull";
                url += (status == "true" ? "pullOut" : "pullIn");
                break;
            }
            case "health": {
                operationText = "Health";
                url += (status == "true" ? "fall" : "raise");
            }
            default:
                break;
        }
        var opText;
        if (status == "true") {
            opText = "拉出";
        }
        else {
            opText = "拉入";
        }
        operationText += opText;

        var parameters = {
            "groupId": groupId,
            "description": opreason
        };
        var ips = ip.split(',');
        $.each(ips, function (i, item) {
            if (i == 0) {
                url += '?ip=' + item;
            } else {
                url += '&ip=' + item;
            }
        });

        var request = {
            method: 'GET',
            url: url,
            params: parameters
        };
        $('#memberOperationConfirmModel').modal("show").find(".modal-body").html("正在" + opText + "<img src='/static/img/spinner.gif' />");
        $('#memberOperationConfirmModel').modal("show").find(".modal-title").html("正在" + opText);

        $scope.processRequest(request, $('#memberOperationConfirmModel'), operationText, "操作成功");
    };
    // Load group status table
    $scope.loadTable = function () {
        var p1 = A.canDo("Group", "UPDATE", groupId) && $scope.optionPanelState;
        if (!p1) {
            $('#group-status-table').bootstrapTable('hideColumn', 'operate');
        }
        else {
            $('#group-status-table').bootstrapTable('showColumn', 'operate');
        }
        $('#group-status-table').bootstrapTable("hideLoading");
        $('#group-status-table').bootstrapTable("load", $scope.info.memberStatus);
    };
    // Rights
    $scope.triggerOptionPanelBt = function (e) {
        $scope.optionPanelState = !$scope.optionPanelState;
        var p = A.canDo("Group", "UPDATE", groupId) && $scope.optionPanelState;
        $scope.tableOps.member.showOperations = !$scope.tableOps.member.showOperations;
        if (p) {
            $('#group-status-table').bootstrapTable('showColumn', 'operate');
        } else {
            $('#group-status-table').bootstrapTable('hideColumn', 'operate');
        }
    };
    $scope.showAddMemberBt = function () {
        var wrenchOp = A.canDo("Group", "UPDATE", groupId) && $scope.tableOps.member.showOperations;
        if (!wrenchOp) return false;
        else return true;
    };
    $scope.showRemoveMemberBt = function () {
        var wrenchOp = A.canDo("Group", "UPDATE", groupId) && $scope.tableOps.member.showOperations;
        if (!wrenchOp) return false;
        else return true;
    };
    $scope.showBatchOperationBt = function () {
        var wrenchOp = A.canDo("Group", "UPDATE", groupId) && $scope.tableOps.member.showOperations;
        if (!wrenchOp) return false;
        else return true;
    };
    // Operation confirm dialogs' click
    $('#MemberOperationSuccess').click(function (e) {
        $('#operationConfrimModel2').modal("hide");
        window.location.reload();
    });
    $('#ConfirmMemberBt').click(function (e) {
        // Reload the backend window
        var hashData = {};
        hashData.timeStamp = new Date().getTime();
        setTimeout(function () {
            H.setData(hashData);
        }, timeout);
    });

    // Common method
    $scope.forceUpdateRequest = {};
    $scope.processRequest = function (request, confirmDialog, operationText, tooltipText) {
        confirmDialog.find(".modal-title").html(operationText);
        $scope.forceUpdateRequest = $.extend(true, {}, request);
        if (!$scope.forceUpdateRequest.params) {
            $scope.forceUpdateRequest.params = {
                force: true
            }
        } else {
            $scope.forceUpdateRequest.params.force = true;
        }
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
                    $scope.showForceUpdate = true;
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

    function startTimer(dialog) {
        setTimeout(function () {
            dialog.find('#ConfirmMemberBt').click();
        }, 2000);
    }

    $scope.getUsedSlbIdForCurrentGroup = function (group) {
        var gs = group['group-virtual-servers'];
        var firstVirtualServer = gs[0];

        var firstVirtualServerSlbIds = firstVirtualServer['virtual-server']['slb-ids'];

        var sorted = firstVirtualServerSlbIds.sort(function (a, b) {
            return a > b;
        });
        return sorted[0];
    }
    // Health checker page
    $scope.getGroupHealthData = function (group, callBack) {
        var request = {
            method: 'GET',
            url: G.baseUrl + '/api/tools/healthcheck/status?groupId=' + group.id
        };
        $http(request).success(function (res) {
            //groupHealthObjects
            var resitems = [];
            if (res) {
                resitems = res.items;

                $.each(resitems, function (i, item) {
                    $scope.groupHealthObjects[item.ip + '/' + item.port] = item;
                });
            } else {
                $scope.groupHealthObjects = {};
            }
            callBack();
        });
    };
    $scope.getMemberOperationLogData = function (data) {

        var result = {};

        try {
            var opslog = data['operation-log-datas'];
            $.each(opslog, function (i, item) {
                // Just care the json data
                if (item.data && isJson(item.data)) {
                    var d = JSON.parse(item.data);
                    var ips = d.ips;
                    $.each(ips, function (j, ip) {
                        if (result[ip]) result[ip].push(item);
                        else result[ip] = [item];
                    });
                }
            });

            // result sorting
            $.each(_.keys(result), function (i, key) {
                result[key].sort(result[key], function (a, b) {
                    var date1 = parseDateTime(new Date(a['date-time']));
                    var date2 = parseDateTime(new Date(b['date-time']));
                    return date1 - date2;
                });
            });
        } catch (e) {

        }
        return result;
    };

    var parseDateTime = function (date, secondBool) {
        var year = date.getFullYear();

        var month = date.getMonth() + 1;
        if (month < 10) month = '0' + month;

        var day = date.getDate();
        if (day < 10) day = '0' + day;

        var hour = date.getHours();
        if (hour < 10) hour = '0' + hour;

        var minute = date.getMinutes();
        if (minute < 10) minute = '0' + minute;

        if (!secondBool) {
            return year + '-' + month + '-' + day + '_' + hour + ':' + minute + ':' + '00';
        } else {
            var second = date.getSeconds();
            if (second < 10) second = '0' + second;

            return year + '/' + month + '/' + day + ' ' + hour + ':' + minute + ':' + second;
        }
    };

    /*Health check*/
    $scope.getRequestHeaderCount = function () {
        var l = _.filter($scope.data.requestHeaders, function (item) {
            return item.key && item.value;
        });
        return l.length;
    };
    $scope.addNewHeaderPair = function () {
        $scope.data.requestHeaders.push({
            key: '',
            value: ''
        })
    };

    $scope.setHeadersClick = function () {
        if ($scope.data.requestHeaders.length == 0) {
            $scope.data.requestHeaders = [{
                key: '',
                value: ''
            }];
        }
    };

    $scope.deleteHeaderPair = function (index) {
        if ($scope.data.requestHeaders.length == 1) return;
        $scope.data.requestHeaders.splice(index, 1);
    };

    function reviewData(id) {
        var result = true;
        $.each(id.find('[data-validator-type="validation"]'), function (i, element) {
            if (!$(element).bootstrapValidation('validate'))
                result = false;
        });
        return result;
    };
    // Hash Changed
    var groupId;
    var groupApp;

    $scope.hashChanged = function (hashData) {
        $scope.resource = H.resource;
        $scope.resource = H.resource;
        $scope.env = 'pro';
        $scope.preHealthCheck = false;
        var memberOp = hashData.memberOp;
        if (hashData.env) {
            $scope.env = hashData.env;
        }


        groupId = hashData.groupId;
        groupApp = new GroupApp(hashData, $http, $q, $scope.env);
        $scope.forceUpdateRequest = {};
        $scope.initTable();

        if (hashData.preHealthCheck) {
            $scope.preHealthCheck = hashData.preHealthCheck == 'true';
        }
        // init the wreches
        $scope.tableOps.member.showOperations = false;
        $scope.tableOps.member.showMoreColumns = false;
        $('#group-status-table').bootstrapTable("removeAll");
        $('#group-status-table').bootstrapTable("showLoading");

        if (memberOp) {
            $scope.toggleShowMemberOperations();
        }
        $scope.loadMetaData();
    };
    H.addListener("groupStatusApp", $scope, $scope.hashChanged);

    function rightNotify(title, message, url) {
        var notify = $.notify({
            icon: 'fa fa-lock',
            title: title,
            message: message,
            url: url,
            target: '_self'
        }, {
            type: 'warning',
            allow_dismiss: true,
            newest_on_top: false,
            placement: {
                from: 'bottom',
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
            spacing: 15,
            z_index: 1031,
            mouse_over: 'pause'
        });

        return notify;
    }
});
angular.bootstrap(document.getElementById("group-status-area"), ['groupStatusApp']);

var summaryInfoApp = angular.module('summaryInfoApp', ['http-auth-interceptor', 'angucomplete-alt']);
summaryInfoApp.controller('summaryController', function ($scope, $http, $q) {
    $scope.query = {};
    // Auto complete
    $scope.cacheRequestFn = function (str) {
        return {q: str, timestamp: new Date().getTime()};
    };
    $scope.remoteUrl = function () {
        return G.baseUrl + "/api/meta/groups";
    };
    $scope.selectTarget = function (t) {
        if (t) {
            var toId = t.originalObject.id;
            if ($scope.target.id != toId) {
                $scope.$broadcast('angucomplete-alt:clearInput', 'targetSelector');
                var pairs = {};
                pairs['groupId'] = toId;
                $scope.target.id = toId;
                H.setData(pairs);
                messageNotify("切换Group. ", "成功切换至Group： " + toId, null);
            }
        }
    };
    $scope.clickTarget = function () {
        $('#targetSelector_value').css('width', '250px');
    };
    $scope.setInputsDisplay = function () {
        $('#targetSelector_value').val($scope.query.groupId);
    };
    $scope.getAllTargets = function () {
        $http.get(G.baseUrl + "/api/group?groupId=" + $scope.query.groupId + "&type=info").success(
            function (res) {
                $scope.target = {};
                $scope.target.name = $scope.query.groupId + "/" + res.name;
                $('#targetSelector_value').val($scope.target.name);
            }
        );
    };
    $scope.hashChanged = function (hashData) {
        $scope.resource = H.resource;
        $scope.resource = H.resource;
        if (hashData.env) {
            $scope.env = hashData.env;
        }
        if (hashData.groupId) {
            $scope.query.groupId = hashData.groupId;
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
                link = "/portal/group#?env=" + G.env + "&groupId=" + $scope.query.groupId;
                break;
            }
            case 'log': {
                link = "/portal/group/log#?env=" + G.env + "&groupId=" + $scope.query.groupId;
                break;
            }
            case 'delegate': {
                link = "/portal/group/delegate-rule#?env=" + G.env + "&groupId=" + $scope.query.groupId;
                break;
            }
            case 'traffic': {
                link = "/portal/group/traffic#?env=" + G.env + "&groupId=" + $scope.query.groupId;
                break;
            }
            case 'release': {
                link = "/portal/group/release#?env=" + G.env + "&groupId=" + $scope.query.groupId;
                break;
            }
            case 'antibot': {
                link = "/portal/group/antibot#?env=" + G.env + "&groupId=" + $scope.query.groupId;
                break;
            }
            case 'conf': {
                link = "/portal/group/conf#?env=" + G.env + "&groupId=" + $scope.query.groupId;
                break;
            }
            case 'rule': {
                link = "/portal/group/rule#?env=" + G.env + "&groupId=" + $scope.query.groupId;
                break;
            }
            case 'intercept': {
                link = "/portal/group/intercept#?env=" + G.env + "&groupId=" + $scope.query.groupId;
                break;
            }
            default:
                break;
        }
        return link;
    };

    $scope.showDelegate = function () {
        var env = $scope.env;
        if (!env) return false;

        switch (env) {
            case 'uat':
            case 'fws':
            case 'pro':
                return false;

            default:
                return true;
        }
    };

    $scope.hashChanged = function (hashData) {
        $scope.resource = H.resource;
        if (hashData.env) {
            $scope.env = hashData.env;
        }
        if (hashData.groupId) {
            $scope.query.groupId = hashData.groupId;
        }
    };
    H.addListener("infoLinksApp", $scope, $scope.hashChanged);
});
angular.bootstrap(document.getElementById("header-area"), ['headerInfoApp']);

$(document).ready(function () {
    $('[data-toggle="tooltip"]').tooltip();
});