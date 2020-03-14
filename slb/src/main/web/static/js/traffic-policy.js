/**
 * Created by ygshen on 2017/2/15.
 */
var summaryInfoApp = angular.module('summaryInfoApp', ['http-auth-interceptor', 'angucomplete-alt']);
summaryInfoApp.controller('summaryController', function ($scope, $http, $q) {
    $scope.query = {};
    // Auto complete
    $scope.cacheRequestFn = function (str) {
        return {q: str, timestamp: new Date().getTime()};
    };
    $scope.remoteUrl = function () {
        return G.baseUrl + "/api/meta/policies";
    };
    $scope.selectTarget = function (t) {
        if (t) {
            var toId = t.originalObject.id;
            if ($scope.target.id != toId) {
                $scope.$broadcast('angucomplete-alt:clearInput', 'targetSelector');
                var pairs = {};
                pairs['policyId'] = toId;
                $scope.target.id = toId;
                H.setData(pairs);
                messageNotify("切换Policy. ", "成功切换至Traffic Policy： " + toId, null);
            }
        }
    };
    $scope.clickTarget = function () {
        $('#targetSelector_value').css('width', '250px');
    };
    $scope.setInputsDisplay = function () {
        $('#targetSelector_value').val($scope.query.policyId);
    };
    $scope.getAllTargets = function () {
        $http.get(G.baseUrl + "/api/policy?policyId=" + $scope.query.policyId).success(
            function (res) {
                $scope.target = {};
                $scope.target.name = $scope.query.policyId + "/" + res.name;
                $('#targetSelector_value').val($scope.target.name);
            }
        );
    };
    $scope.hashChanged = function (hashData) {
        $scope.resource = H.resource;
        if (hashData.env) {
            $scope.env = hashData.env;
        }
        if (hashData.policyId) {
            $scope.query.policyId = hashData.policyId;
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
                link = "/portal/policy#?env=" + G.env + "&policyId=" + $scope.query.policyId;
                break;
            }
            case 'log': {
                link = "/portal/policy/log#?env=" + G.env + "&policyId=" + $scope.query.policyId;
                break;
            }
            case 'traffic': {
                link = "/portal/policy/traffic#?env=" + G.env + "&policyId=" + $scope.query.policyId;
                break;
            }
            case 'conf': {
                link = "/portal/policy/conf#?env=" + G.env + "&policyId=" + $scope.query.policyId;
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
        if (hashData.policyId) {
            $scope.query.policyId = hashData.policyId;
        }
    };
    H.addListener("infoLinksApp", $scope, $scope.hashChanged);
});
angular.bootstrap(document.getElementById("header-area"), ['headerInfoApp']);


var policyInfoApp = angular.module('policyInfoApp', ['angucomplete-alt', 'http-auth-interceptor', 'ngSanitize']);
policyInfoApp.controller('policyInfoController', function ($scope, $http, $q) {
    $scope.currentweight = 50;
    //slbInfo data
    $scope.query = {
        policyId: '',
        user: {},
        showextended: false
    };
    $scope.model = {
        groups: [],
        vses: [],
        apps: [],
        currentlogs: [],
        policies: {},
        offlinepolicy: {},
        extendedpolicy: {},
        onlinepolicy: {},
        currentlogs: [],
        newpolicylogs: [],
        users: [],
        usersByChineseName: {}
    };
    $scope.view = {
        groups: [],
        vses: [],
        policies: {},
        policyhistoryversions: [],
        onlinepolicy: {},
        offlinepolicy: {},
        extendedpolicy: {}
    };

    $scope.changevs = {};
    $scope.tableOps = {
        policyVs: {
            showMoreColumns: false,
            showOperations: false
        },
        policyGroup: {
            showMoreColumns: false,
            showOperations: false
        }
    };
    $scope.data = {
        policy_ops: [
            {id: 'ACTIVATE', name: '激活 POLICY 上线'},
            {id: 'DEACTIVATE', name: '下线 POLICY'},
            {id: 'UPDATE', name: 'POLICY'},
            {id: 'DELETE', name: '删除 POLICY'},
            {id: 'READ', name: '查询 POLICY 信息'},
            {id: 'PROPERTY', name: '给 POLICY 打Tag或者Property'}
        ]
    };
    $scope.getTargetPolicy = function (p) {
        var resource = $scope.resource;
        if (!resource || _.keys(resource).length == 0) return;

        return resource['polices']['policies_trafficsQueryApp_typemapping'][p];
    };

    // Partial show op and summary
    $scope.disableOpenPolicyVs = function () {
        var can = A.canDo('Policy', 'UPDATE', $scope.model.offlinepolicy.id);
        return !can;
    };
    $scope.getPolicyVsShowOperation = function () {
        return $scope.tableOps.policyVs.showOperations ? 'fa fa-wrench text-info' : 'fa fa-wrench font-gray';
    };
    $scope.toggleShowPolicyVsOperations = function () {
        $scope.tableOps.policyVs.showOperations = !$scope.tableOps.policyVs.showOperations;
        var can = A.canDo('Policy', 'UPDATE', $scope.model.offlinepolicy.id);
        if (can) {
            if ($scope.tableOps.policyVs.showOperations) {
                $('#vs-table').bootstrapTable('showColumn', 'Operation');
            }
            else {
                $('#vs-table').bootstrapTable('hideColumn', 'Operation');
            }
        }
    };
    $scope.getPolicyGroupOperationTitle = function () {
        return $scope.tableOps.policyVs.showOperations ? '关闭操作' : '打开操作';
    };

    $scope.applyBtClick = function () {
        $("#group-role-selector-table").bootstrapTable("uncheckAll");
        // check current user's right
        var resources = $scope.query.resource;
        resources = _.groupBy(resources, 'type');
        resources = resources['Policy'];
        var groupAccesses = _.indexBy(resources[0]['data-resources'], 'data');
        var thisGroupAccess = groupAccesses[$scope.query.policyId];
        var allGroupAccess = groupAccesses['*'];
        var allAccesses = [];
        if (thisGroupAccess) {
            allAccesses = allAccesses.concat(thisGroupAccess.operations);
        }
        if (allGroupAccess) {
            allAccesses = allAccesses.concat(allGroupAccess.operations);
        }
        allAccesses = _.uniq(_.pluck(allAccesses, 'type'));
        var data = $scope.data.policy_ops;
        data = _.map(data, function (v) {
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
            var mailLink = G.baseUrl + '/api/auth/apply/mail?userName=' + $scope.query.user + '&op=' + op + '&type=Policy&targetId=' + $scope.query.policyId + '&env=' + $scope.env;
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

    $scope.getPolicyVsShowMore = function () {
        return $scope.tableOps.policyVs.showMoreColumns ? 'fa fa-list-alt text-info' : 'fa fa-list-alt font-gray';
    };
    $scope.toggleShowMorePolicyVsColumns = function () {
        $scope.tableOps.policyVs.showMoreColumns = !$scope.tableOps.policyVs.showMoreColumns;
        if ($scope.tableOps.policyVs.showMoreColumns) {
            $('#vs-table').bootstrapTable('showColumn', 'domains');
        } else {
            $('#vs-table').bootstrapTable('hideColumn', 'domains');
        }
    };
    $scope.getPolicyVsShowMoreTitle = function () {
        return $scope.tableOps.policyVs.showOperations ? '简略' : '详细';
    };

    $scope.disableOpenPolicyGroup = function () {
        var can = A.canDo('Policy', 'UPDATE', $scope.model.offlinepolicy.id);
        return !can;
    };
    $scope.getPolicyGroupShowOperation = function () {
        return $scope.tableOps.policyGroup.showOperations ? 'fa fa-wrench text-info' : 'fa fa-wrench font-gray';
    };
    $scope.toggleShowPolicyGroupOperations = function () {
        $scope.tableOps.policyGroup.showOperations = !$scope.tableOps.policyGroup.showOperations;
        var can = A.canDo('Policy', 'UPDATE', $scope.model.offlinepolicy.id);
        if (can) {
            if ($scope.tableOps.policyGroup.showOperations) {
                $('#controls-table').bootstrapTable('showColumn', 'Operation');
            }
            else {
                $('#controls-table').bootstrapTable('hideColumn', 'Operation');
            }
        }
    };
    $scope.getPolicyGroupOperationTitle = function () {
        return $scope.tableOps.policyGroup.showOperations ? '关闭操作' : '打开操作';
    };

    $scope.getPolicyGroupShowMore = function () {
        return $scope.tableOps.policyGroup.showMoreColumns ? 'fa fa-list-alt text-info' : 'fa fa-list-alt font-gray';
    };
    $scope.toggleShowMorePolicyGroupColumns = function () {
        $scope.tableOps.policyGroup.showMoreColumns = !$scope.tableOps.policyGroup.showMoreColumns;
        if ($scope.tableOps.policyGroup.showMoreColumns) {
            $('#controls-table').bootstrapTable('showColumn', 'grouppath');
        } else {
            $('#controls-table').bootstrapTable('hideColumn', 'grouppath');
        }
    };
    $scope.getPolicyGroupShowMoreTitle = function () {
        return $scope.tableOps.policyGroup.showOperations ? '简略' : '详细';
    };
    //Rights Area
    $scope.showUpdatePolicyBt = function () {
        return A.canDo("Policy", "UPDATE", $scope.query.policyId) && $scope.query.showextended;
    };
    $scope.showActivatePolicyBt = function () {
        return A.canDo("Policy", "ACTIVATE", $scope.query.policyId) && $scope.query.showextended;
    };
    $scope.showAddPOLICYTagBt = function () {
        return A.canDo("Policy", "PROPERTY", $scope.query.policyId) && $scope.query.showextended;
    };
    $scope.showAddPOLICYPropertyBt = function () {
        return A.canDo("Policy", "PROPERTY", $scope.query.policyId) && $scope.query.showextended;
    };
    $scope.showDeactivatePolicyBt = function () {
        return A.canDo("Policy", "DEACTIVATE", $scope.query.policyId) && $scope.query.showextended;
    };
    $scope.showDeletePolicyBt = function () {
        return A.canDo("Policy", "DELETE", $scope.query.policyId) && $scope.query.showextended;
    };
    $scope.showAddGroupBt = function () {
        var wrenchOp = A.canDo("Policy", "UPDATE", $scope.query.policyId) && $scope.tableOps.policyGroup.showOperations;
        if (!wrenchOp) return false;
        else return true;
    }
    $scope.showLogs = function () {
        return $scope.model.currentlogs.length > 0;
    };
    // Dropdown Area
    $scope.remoteGroupsUrl = function () {
        return G.baseUrl + "/api/meta/groups";
    };
    $scope.selectGroupId = function (o) {
        if (o) {
            $scope.query.groupid = o.originalObject.id;
        }
    };
    $scope.cacheRequestFn = function (str) {
        return {q: str, timestamp: new Date().getTime()};
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
    $scope.activateBtShow = function () {
        return A.canDo("Policy", "ACTIVATE", $scope.query.policyId);
    };

    // Links
    $scope.showEs = function () {
        var env = $scope.env;
        if (env && env.toLowerCase() == 'fws') return false;
        return true;
    };
    //WEBINFO,HICKWALL,CAT links
    $scope.generateMonitorUrl = function (type) {
        if (!G[$scope.env]) return;
        switch (type) {
            case 'es': {
                if (G[$scope.env].urls.es) {
                    return G[$scope.env].urls.es + "?query=policy_name%3D'" + $scope.query.policyId + "'";
                } else {
                    return '-';
                }
            }
            default:
                return '';
        }
    };
    // Auto Complete caching
    $scope.cacheRequestFn = function (str) {
        return {q: str, timestamp: new Date().getTime()};
    };
    $scope.remoteUrl = function () {
        return G.baseUrl + "/api/meta/policies";
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
    $scope.forceUpdateRequest;
    $scope.disableDeactivatePolicy = function () {
        var status = $scope.getPolicyStatusProperty();
        if (status == "deactivated") {
            return true;
        }
        return false;
    };
    $scope.disableRemovePolicy = function () {
        var status = $scope.getPolicyStatusProperty();
        if (status == "activated" || status == "tobeactivated" || !status) {
            return true;
        }
        return false;
    };
    $scope.confirmActivateText = '线上版本与当前版本比';
    $scope.activatePolicyClick = function () {
        if ($scope.model.onlinepolicy.data.version != undefined && $scope.model.onlinepolicy.data.version == $scope.model.extendedpolicy.version) {
            $scope.confirmActivateText = '线上已是最新版本,确认是否强制重新激活';
        }
        var baseText = JSON.stringify(U.sortObjectFileds($scope.model.onlinepolicy.data), null, "\t");
        var newText = JSON.stringify(U.sortObjectFileds($scope.model.offlinepolicy), null, "\t");
        var baseVersion = '线上Policy版本(版本' + $scope.model.onlinepolicy.data.version + ")";
        var newVersion = '更新后Policy版本(版本' + $scope.model.offlinepolicy.version + ")";
        var diffoutputdiv = document.getElementById('diffOutput');
        diffTwoPolicies(diffoutputdiv, baseText, newText, baseVersion, newVersion);
        $('#activatePolicyModal').modal('show');
    };
    $scope.activatePOLICY = function () {
        $('#confirmActivatePolicy').modal('hide');
        $('#operationConfrimModel').modal("show").find(".modal-title").html("激活Traffic Policy");
        $('#operationConfrimModel').modal("show").find(".modal-body").html("正在激活.. <img src='/static/img/spinner.gif' />");
        var param = {
            policyId: $scope.query.policyId
        };
        var req = {
            method: 'GET',
            url: G.baseUrl + '/api/activate/policy?description=' + $scope.query.user,
            params: param
        };
        $scope.processRequest(req, $('#operationConfrimModel'), "激活Policy", "激活成功");
    };
    $scope.forceActivatePolicy = function () {
        $scope.showForceUpdate = false;
        $('#operationConfrimModel').modal("show").find(".modal-title").html("强制变更");
        $('#operationConfrimModel').modal("show").find(".modal-body").html("正在进行强制变更.. <img src='/static/img/spinner.gif' />");
        if ($scope.forceUpdateRequest) {
            $scope.processRequest($scope.forceUpdateRequest, $('#operationConfrimModel'), "强制变更", "成功");
        }
    };
    $scope.deactivatePolicyAction = function () {
        var status = $scope.getPolicyStatusProperty();
        if (status != "deactivated") {
            $('#allowDeactivatePolicyModal').modal('show');
        }
    };
    $scope.deactivatePolicy = function () {
        $('#allowDeactivatePolicyModal').modal('hide');
        $('#deactivatePolicyModalConfirm').modal('show');
    };
    $scope.confirmDeactivatePolicy = function () {
        $('#deactivatePolicyModalConfirm').modal('hide');
        var loading = "<img src='/static/img/spinner.gif' /> 正在下线";
        $('#operationConfrimModel').modal('show').find(".modal-body").html(loading);
        var param = {
            policyId: $scope.query.policyId
        };
        var request = {
            method: 'GET',
            url: G.baseUrl + '/api/deactivate/policy?description=' + $scope.query.deactivatereason,
            params: param
        };
        $scope.processRequest(request, $('#operationConfrimModel'), "下线 Traffic Policy", "下线Traffic Policy成功, Traffic Policy Id:" + $scope.query.policyId, "");
    };
    $scope.activatePolicyTitleClass = function () {
        try {
            if ($scope.model.onlinepolicy.data.version != undefined && $scope.model.onlinepolicy.data.version == $scope.model.extendedpolicy.version) {
                return "status-red-important";
            }
        } catch (e) {

        }
    };
    $scope.deletePolicyAction = function () {
        var status = $scope.getPolicyStatusProperty();
        if (status == "deactivated") {
            $('#allowDeletePolicyModal').modal('show');
        }
    };
    $scope.deletePolicy = function () {
        $('#allowDeletePolicyModal').modal('hide');
        $('#deletePolicyModalConfirm').modal('show');
    };
    $scope.confirmDeletePolicy = function () {
        // delete the tags related to the groups
        var groups = _.map($scope.model.offlinepolicy.controls, function (item) {
            var pname = $scope.view.extendedpolicy.targetFor;
            var param = {
                type: 'group',
                pname: pname,
                pvalue: 'true',
                targetId: item.group.id
            };
            var request = {
                method: 'GET',
                url: G.baseUrl + "/api/property/clear",
                params: param
            };

            return $http(request);
        });

        $q.all(groups).then();

        $('#deletePolicyModalConfirm').modal('hide');
        var loading = "<img src='/static/img/spinner.gif' /> 正在删除";
        $('#operationConfrimModel2').modal('show').find(".modal-body").html(loading);
        var param = {
            policyId: $scope.query.policyId
        };
        var request = {
            method: 'GET',
            url: G.baseUrl + '/api/policy/delete?description=' + $scope.query.deletereason,
            params: param
        };
        $scope.processRequest(request, $('#operationConfrimModel2'), "删除Traffic Policy", "删除Traffic Policy成功, PolicyId:" + $scope.query.policyId, "");
    };

    // Diff
    $scope.confirmDiffButtonDisable = true;
    $scope.targetDiffPolicyId;
    $scope.selectDiffSlbId = function (t) {
        if (t) {
            var toId = t.originalObject.id;
            $scope.targetDiffPolicyId = toId;
            $scope.confirmDiffButtonDisable = false;
        }
    };
    $scope.confirmSelectPolicyId = function () {
        if ($scope.confirmDiffButtonDisable) {
            // has selected the current targetId
            $scope.confirmActivateText = '当前 Traffic Policy Id:' + $scope.query.policyId + ". 对比 Traffic Policy Id:" + $scope.targetDiffPolicyId;

            $http.get(G.baseUrl + "/api/policy?policyId=" + $scope.targetDiffPolicyId).success(
                function (res) {
                    var baseText = JSON.stringify(U.sortObjectFileds($scope.model.offlinepolicy), null, "\t");
                    var newText = JSON.stringify(U.sortObjectFileds(res, null, "\t"));

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
                        baseTextName: '当前 Traffic Policy Id: ' + $scope.query.policyId,
                        newTextName: '对比 Traffic Policy Id:' + res.id,
                        viewType: 0
                    }));
                    $('#diffSeperatePolicies').modal('show');
                }
            );
        }
        else {
            alert("请选择要比较的 Traffic Policy");
        }
    };

    // A/B test
    $scope.startABTestLink = function () {
        var link = '/portal/tools/test#?env=' + $scope.env + '&';
        var p = $scope.model.extendedpolicy;
        link += 'policyId=' + p.id;
        window.open(link, '_self');
    };
    // Option panel Area
    $scope.toggleOptionPanel = function () {
        $scope.query.showextended = !$scope.query.showextended;

        var p = A.canDo("Policy", "UPDATE", $scope.query.policyId) && $scope.query.showextended;
        if (p) {
            $scope.tableOps.policyVs.showOperations = true;
            $scope.tableOps.policyGroup.showOperations = true;
            $('#controls-table').bootstrapTable('showColumn', 'Operation');
            $('#vs-table').bootstrapTable('showColumn', 'Operation');
        } else {
            $scope.tableOps.policyVs.showOperations = false;
            $scope.tableOps.policyGroup.showOperations = false;
            $('#controls-table').bootstrapTable('hideColumn', 'Operation');
            $('#vs-table').bootstrapTable('hideColumn', 'Operation');
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
        var f = _.find($scope.model.extendedpolicy.tags, function (item) {
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
            type: 'policy',
            tagName: tagName,
            targetId: $scope.query.policyId
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
                if (!$scope.model.extendedpolicy.tags) $scope.model.extendedpolicy.tags = [];
                var f = _.find($scope.model.extendedpolicy.tags, function (item) {
                    return item.trim().toLowerCase() == tagName.toLowerCase();
                });
                if (!f) {
                    $scope.model.extendedpolicy.tags.push(tagName);
                }
            }
        );
    };
    $scope.removeFocus = function (tagName, target) {
        var resource = $scope.resource;
        if (!resource || _.keys(resource).length == 0) return;
        $(target).showLoading();

        var param = {
            type: 'policy',
            tagName: tagName,
            targetId: $scope.query.policyId
        };
        var request = {
            method: 'GET',
            url: G.baseUrl + '/api/untagging?description=unfocus',
            params: param
        };
        $http(request).success(
            function (response) {
                $(target).hideLoading();

                $(target).html(resource['slb']['slb_slbInfoApp_opmap']["关注"]);
                $(target).parent('span').removeClass('fa fa-eye').addClass("fa fa-eye-slash");
                var index = $scope.model.extendedpolicy.tags.indexOf(tagName);
                if (index != -1) {
                    $scope.model.extendedpolicy.tags.splice(index, 1);
                }
            }
        );
    };

    // Status Area
    $scope.statusText = function () {
        var resource = $scope.resource;
        if (!resource || _.keys(resource).length == 0) return;


        var s = $scope.getPolicyStatusProperty();
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
        var s = $scope.getPolicyStatusProperty();
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
                if ($scope.canDeleteTag(value)) {
                    $scope.currentTagName = value;
                    $('#deletePOLICYTag').modal({backdrop: 'static'});
                }
                break;
            }
            case 'deleteProp' : {
                if ($scope.canDeleteProperty(value)) {
                    $scope.currentProperty = value;
                    $('#deletePOLICYProp').modal({backdrop: 'static'});
                }
                break;
            }
            case 'addTag' : {
                $('#addPOLICYTag').modal({backdrop: 'static'});
                break;
            }
            case 'addProp' : {
                $('#addPOLICYProp').modal({backdrop: 'static'});
            }
        }
    };
    $scope.canDeleteTag = function (t) {
        if (!$scope.query.showextended) return;
        if (t == 'activate' || t == 'deactive' || t.startWith('owner')) return false;
        return true;
    };
    $scope.canDeleteProperty = function (p) {
        if (!$scope.query.showextended) return;
        var pName = p.name.toLowerCase();
        if (pName == "apptype" || pName == "idc" || pName == "zone" || pName == "status" || pName == "sbu") {
            return false;
        }
        return true;
    };

    $scope.addTag = function (tagName, type) {
        var param = {
            type: 'policy',
            tagName: tagName,
            targetId: $scope.query.policyId
        };
        var request = {
            method: 'GET',
            url: G.baseUrl + '/api/tagging?description' + $scope.query.tagreason,
            params: param
        };
        $('#addPOLICYTag').modal('hide');
        $scope.processRequest(request, $('#operationConfrimModel'), '', '新增Tag');
    };
    $scope.deleteTag = function (tagName, type) {
        var param = {
            type: 'policy',
            tagName: tagName,
            targetId: $scope.query.policyId
        };
        var request = {
            method: 'GET',
            url: G.baseUrl + '/api/untagging?description=' + $scope.query.deletetagreason,
            params: param
        };
        $('#deletePOLICYTag').modal('hide');
        $scope.processRequest(request, $('#operationConfrimModel'), '', "删除Tag");
    };
    $('#addTagBt').click(function (e) {
        e.preventDefault();
        var validate = reviewData($('#addPOLICYTag'));
        if (!validate) return;
        $scope.addTag($('#tagNameInput').val().trim());
    });
    $('#addPropertyBt').click(function (e) {
        e.preventDefault();
        var validate = reviewData($('#addPOLICYBProp'));
        if (!validate) return;
        $scope.addProperty({'name': $('#pname').val().trim(), 'value': $('#pvalue').val().trim()});
    });
    // Property
    $scope.addProperty = function (prop) {
        var param = {
            type: 'policy',
            pname: prop.name,
            pvalue: prop.value,
            targetId: $scope.query.policyId
        };
        var request = {
            method: 'GET',
            url: G.baseUrl + '/api/property/set?description=' + $scope.query.propertyreason,
            params: param
        };
        $('#addPOLICYProp').modal("hide");
        if (prop.name == 'status') return;
        $scope.processRequest(request, $('#operationConfrimModel'), "", "添加Property");
    };
    $scope.deleteProperty = function (prop) {
        var param = {
            type: 'policy',
            pname: prop.name,
            pvalue: prop.value,
            targetId: $scope.query.policyId
        };
        var request = {
            method: 'GET',
            url: G.baseUrl + '/api/property/clear?description=' + $scope.query.deletepropertyreason,
            params: param
        };
        $('#deletePOLICYProp').modal('hide');
        $scope.processRequest(request, $('#operationConfrimModel'), "", "删除Property");
    };

    // Diff
    function diffTwoPolicies(targetDiv, baseText, newText, baseVersion, newVersion) {
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
        if ($scope.targetDiffSlbId) {
            // has selected the current targetId
            $scope.confirmActivateText = '当前SlbId:' + $scope.query.slbId + ". 对比SlbId:" + $scope.targetDiffSlbId;

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
                        baseTextName: '当前SlbId: ' + $scope.query.slbId,
                        newTextName: '对比SlbId:' + res.id,
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
    $scope.policyVersionClass = function () {
        var v = $scope.getPolicyStatusProperty();
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
    $scope.policyVersionText = function () {
        var resource = $scope.resource;
        if (!resource || _.keys(resource).length == 0) return;

        var statusMap = resource['vs']['vs_vsInfoApp_status'];


        var s = $scope.getPolicyStatusProperty();
        if (s) s = s.toLowerCase();
        var v = 'UNKNOWN';
        v = $scope.model.onlinepolicy.data == undefined ? "UNKNOWN" : $scope.model.onlinepolicy.data.version;
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

    // Tagging Area
    $scope.showExtendedTag = function () {
        if ($scope.model.extendedpolicy.tags == undefined) return false;
        var u_tag = _.filter($scope.model.extendedpolicy.tags, function (m) {
            return m.trim().toLowerCase().startWith("user_");
        });
        var p = u_tag.length == $scope.model.extendedpolicy.tags.length;
        var t = $scope.model.extendedpolicy.tags == undefined || $scope.model.extendedpolicy.tags.length == 0;
        if (t || p) return false;
        return true;
    };
    $scope.showCurrentTag = function (t) {
        var x = !t.trim().toLowerCase().startWith('user_');
        return x;
    };
    $scope.showRemoveTagBt = function (v) {
        if (v.toLowerCase().startWith('owner') || v.toLowerCase().startWith('user')) return false;
        return A.canDo("Policy", "PROPERTY", $scope.model.extendedpolicy.id) && $scope.query.showextended;
    };
    // Property
    $scope.showExtendedProperty = function () {
        return !($scope.view.extendedpolicy.properties == undefined || $scope.view.extendedpolicy.properties.length == 0);
    };
    $scope.showRemoveProperty = function (v) {
        var v = v.name;
        if (v.toLowerCase() == "idc" || v.toLowerCase() == "zone" || v.toLowerCase() == "status" || v.toLowerCase() == "pci") return false;

        return A.canDo("Policy", "PROPERTY", $scope.model.extendedpolicy.id) && $scope.query.showextended;
    };

    // History Area
    $scope.diffVersions = {};
    $scope.getHistoryClick = function () {
        $scope.diffVersions = {};
        var c = $scope.model.offlinepolicy.version;
        $scope.diffVersions[c] = c;


        $('#historyVersion' + c).addClass('label-info');
        $('#historyVersionModel').modal('show');
        $.each($scope.view.historyversions, function (i, val) {
            if (val != c)
                $('#historyVersion' + val).removeClass('label-info');
        });
    };
    $scope.diffVsBetweenVersions = function () {
        var diffVersions = _.keys($scope.diffVersions);
        if (diffVersions.length < 2) {
            alert("请选择两个版本进行diff");
        }
        $scope.confirmActivateText = '版本号:' + diffVersions[0] + " VS 版本号:" + diffVersions[1];
        var v1 = '';
        var v2 = '';
        $q.all(
            [
                $http.get(G.baseUrl + "/api/archive/policy?policyId=" + $scope.query.policyId + "&version=" + diffVersions[0]).success(
                    function (res) {
                        v1 = res;
                    }
                ),
                $http.get(G.baseUrl + "/api/archive/policy?policyId=" + $scope.query.policyId + "&version=" + diffVersions[1]).success(
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
                    baseTextName: '版本号:' + diffVersions[0],
                    newTextName: '版本号:' + diffVersions[1],
                    viewType: 0
                }));
                $('#historyVersionModel').modal('hide');
                $('#diffSeperatePolicies').modal('show');
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

    // Common Methods Area
    $scope.getPolicyStatusProperty = function () {
        var grpStatus = '';
        if (!$scope.model.extendedpolicy.properties) return grpStatus;

        var p = _.find($scope.model.extendedpolicy.properties, function (item) {
            return item.name.toLowerCase().trim() == "status";
        });
        if (p) {
            grpStatus = p.value.toLowerCase().trim();
        }
        return grpStatus;
    };
    $scope.getPolicyOwnerProperty = function () {
        var owner = '-';
        var bu = '-';
        var policy = $scope.model.extendedpolicy;
        var v = _.map(policy.controls, function (item) {
            var id = item.group.id;
            var owner;
            var username;
            var bu;

            var o = _.find($scope.model.groups[id].tags, function (y) {
                return y.toLowerCase().indexOf('owner_') != -1;
            });
            if (o) owner = o.substring(6, o.length);
            username = owner;

            if ($scope.model.users[owner]) {
                owner = $scope.model.users[owner]['chinese-name'] || owner;
            }
            var p = _.find($scope.model.groups[id].properties, function (z) {
                return z.name.toLowerCase() == 'sbu';
            });
            if (p) bu = p.value;
            if (!bu) bu = 'unknown';

            return {
                group: id,
                owner: owner,
                username: username,
                bu: bu
            }
        });

        var users = [];
        var bus = [];

        if (v && v.length != 0) {
            users = _.uniq(_.pluck(v, 'owner'));
            bus = _.uniq(_.pluck(v, 'bu'));
        }
        return {
            user: _.without(users, 'unknown'),
            bu: _.without(bus, 'unknown')
        }
    };
    $scope.getPolicyTarget = function () {
        var policy = $scope.model.extendedpolicy;

        var v = _.find(policy.properties, function (r) {
            return r.name == 'target';
        });
        if (v) return v.value;
        return '-';
    };
    $scope.processRequest = function (request, confirmDialog, operationText, tooltipText) {
        $scope.forceUpdateRequest = $.extend(true, {}, request);
        if (!$scope.forceUpdateRequest.params) {
            $scope.forceUpdateRequest.params = {force: true};
        } else {
            $scope.forceUpdateRequest.params['force'] = true;
        }
        confirmDialog.find(".modal-title").html(operationText);
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
    $('.closeProgressWindowBt').click(
        function (e) {
            var hashData = {};
            hashData.timeStamp = new Date().getTime();
            H.setData(hashData);
        }
    );
    $('.closeProgressWindowBt2').click(
        function (e) {
            if ($('#operationConfrimModel2').find('.fa-check').length > 0) {
                // Success
                window.location.href = "/portal/policies#?env=" + G.env + "&timeStamp=" + new Date().getDate();
            }
            if ($('#operationConfrimModel2').find('.fa-times') > 0) {
                // Success
            }

        }
    );

    function startTimer(dialog) {
        if (dialog.attr('id') == 'operationConfrimModel2') {
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

    // Traffic Policy Area
    $scope.initTable = function () {
        var resource = $scope.resource;
        if (!resource || _.keys(resource).length == 0) return;

        $('#controls-table').bootstrapTable({
            toolbar: "#toolbar",
            columns: [[
                {
                    field: 'group',
                    title: 'Group ID',
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    formatter: function (value, row, index) {
                        var id = value.id;
                        var uri = value.type === 'VGROUP' ? '/portal/vgroup' : '/portal/group';
                        return '<a href="' + uri + '#?env=' + G.env + '&groupId=' + id + '">' + id + '</a>';
                    }
                },
                {
                    field: 'group',
                    title: 'Group Name',
                    align: 'left',
                    resetWidth: true,
                    valign: 'middle',
                    sortable: true,
                    formatter: function (value, row, index) {
                        var name = value.name;
                        var uri = value.type === 'VGROUP' ? '/portal/vgroup' : '/portal/group';
                        return '<a href="' + uri + '#?env=' + G.env + '&groupId=' + value.id + '">' + name + '</a>';
                    }
                },
                {
                    field: 'grouppath',
                    title: 'Path | Priority | SSL | VS | SLB',
                    align: 'left',
                    valign: 'middle',
                    visible: false,
                    formatter: function (value, row, index) {
                        value = value.paths;
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
                                // if current vs path is not common in each groups highlight it
                                var linethroughcss = '';
                                if ($scope.diffVses && $scope.diffVses.length > 0) {
                                    var illegal = _.find($scope.diffVses, function (item) {
                                        return item['vsId'] == v.vsId;
                                    });
                                    if (illegal) {
                                        linethroughcss = 'diff-div';
                                    }
                                }
                                result = result + '<div class="row ' + linethroughcss + '"' + ' style="margin:0;">' + tempStr + '</div>';
                            }
                        });

                        result = '<div class="row" style="margin:0">' + result + '</div>';
                        return result;
                    },
                    sortable: true
                },
                {
                    field: 'group',
                    title: 'App',
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    formatter: function (value, row, index) {
                        return '<a href="/portal/app#?env=' + G.env + '&appId=' + value.appId + '">' + value.app + '</a>';
                    }
                },
                {
                    field: 'group',
                    title: 'IDC',
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    formatter: function (value, row, index) {
                        return value.idc;
                    }
                },
                {
                    field: 'weight',
                    title: 'Weight',
                    align: 'left',
                    width: '100px',
                    valign: 'middle',
                    events: operateEvents,
                    formatter: function (value, row, index) {
                        var totalWeight = _.reduce(
                            _.pluck($('#controls-table').bootstrapTable('getData'), 'weight'),
                            function (m, n) {
                                return m + n;
                            },
                            0
                        );
                        var percentWeight = new Number(Math.abs(value / totalWeight) * 100).toFixed(1);

                        var v = '<span class="group_weight_span" id="group_weight_' + row.group.id + '">' + percentWeight + '%</span>';
                        v += '<input type="text" class="hide group_weight_edit" id="group_weight_edit' + row.group.id + '" value="' + value + '" />';
                        return v;
                    },
                    sortable: true
                },
                {
                    field: 'status',
                    title: 'Status',
                    align: 'left',
                    valign: 'middle',
                    events: operateEvents,
                    formatter: function (value, row, index) {
                        var str = "";
                        value = value.toLowerCase();
                        var resource = $scope.resource;
                        if (!resource || _.keys(resource).length == 0) return;

                        switch (value) {
                            case "deactivated":
                                str = "<span class='status-gray'>" + resource['slb']['slb_slbInfoApp_sum_deactivated'] + "</span>";
                                break;
                            case "activated":
                                str = "<span class='status-green'>" + resource['slb']['slb_slbInfoApp_sum_activated'] + "</span>";
                                break;
                            case "tobeactivated":
                                str = "<span class=' status-yellow'>" + resource['slb']['slb_slbInfoApp_sum_tobeactivated'] + "(<a class='diffGroupVersion'>Diff</a>)</span>";
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
                    field: 'Operation',
                    title: 'Operation',
                    align: 'center',
                    width: '150px',
                    events: operateEvents,
                    valign: 'middle',
                    formatter: function (value, row, index) {
                        var o = A.canDo("Policy", "UPDATE", $scope.query.policyId) ? "" : "hide";
                        var p = '';
                        if ($scope.bindinGroups.length > 1) {
                            p += '<button type="button" class=" remove-group btn btn-info waves-effect waves-light ' + o + '" aria-label="Left Align"><span class="fa fa-minus"></span></button>';
                        }
                        p += '<button style="margin-left:5px" type="button" class="o btn btn-info update-group-bt ' + o + '" aria-label="Left Align"><span class="fa fa-edit"></span></button>';

                        return p;
                    }
                },
                {
                    title: 'Links',
                    align: 'center',
                    width: '115px',
                    valign: 'middle',
                    formatter: function (value, row, index) {
                        var groupId = row.group.id;
                        var query = "group_id%3D'" + groupId + "'";
                        var esLink = userPolicyApp.getEsHtml(query);
                        var str = '<div style="text-align: center">' +
                            esLink +
                            '</div>'
                        return str;
                    }
                }], []],
            search: true,
            showRefresh: true,
            showColumns: true,
            classes: "table-bordered  table-hover table-striped table",
            minimumCountColumns: 2,
            pagination: false,
            idField: 'id',
            pageSize: 20,
            resizable: true,
            resizeMode: 'overflow',
            sidePagination: 'client',
            pageList: [20, 40, 80, 200],
            responseHandler: "responseHandler",
            formatLoadingMessage: function () {
                return "<img class='loading-img' src='/static/img/loading_100.gif' /> " + resource['policy']['policy_policyInfoApp_grouptable']['loading'];
            },
            formatNoMatches: function () {
                return '<i class="fa status-alert fa-exclamation-triangle" aria-hidden="true"></i> ' + resource['policy']['policy_policyInfoApp_grouptable']['nodata'];
            }
        });
        $('#vs-table').bootstrapTable({
            toolbar: "#toolbar2",
            columns: [[
                {
                    field: 'path',
                    title: 'PATH',
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    events: operateEvents,
                    formatter: function (value, row, index) {
                        var currentClass = '';
                        if (row.available == false) currentClass = 'diff-item';

                        return '<span class="path-main-item ' + currentClass + '">' + value + '</span>';
                    }
                },
                {
                    field: 'virtual-server',
                    title: 'Virtual Server',
                    align: 'left',
                    resetWidth: true,
                    valign: 'middle',
                    sortable: true,
                    formatter: function (value, row, index) {
                        var currentClass = '';
                        if (row.available == false) currentClass = 'diff-item';
                        return '<a class="path-main-item ' + currentClass + '" href="/portal/vs#?env=' + G.env + '&vsId=' + value.id + '">' + value.id + '(' + $scope.model.vses[value.id].name + ')</a>';
                    }
                },
                {
                    field: 'idc',
                    title: 'IDC',
                    align: 'left',
                    width: '150px',
                    valign: 'middle',
                    sortable: true,
                    formatter: function (value, row, index) {
                        return value;
                    }
                },
                {
                    field: 'priority',
                    title: 'Priority',
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    formatter: function (value, row, index) {
                        var currentClass = '';
                        if (row.available == false) currentClass = 'diff-item';

                        return '<span class="' + currentClass + '">' + parseInt(value) + '</span>';
                    }
                },
                {
                    field: 'domains',
                    title: 'Domains',
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    visible: false,
                    formatter: function (value, row, index) {
                        var vsId = row['virtual-server'].id;
                        var domains = _.pluck($scope.model.vses[vsId].domains, "name");

                        var str = "";
                        $.each(domains, function (i, v) {
                            str += "<a title='" + v.name + "' target='_blank' href='/portal/vs#?env=" + G.env + "&vsId=" + vsId + "'>" + v + "</a><br>";
                        });
                        return str;
                    }
                },
                {
                    field: 'Operation',
                    title: 'Operation',
                    align: 'center',
                    valign: 'middle',
                    width: '150px',
                    sortable: true,
                    events: operateEvents,
                    formatter: function (value, row, index) {
                        var o = A.canDo("Policy", "UPDATE", $scope.query.policyId) ? "" : "hide";
                        var str = '';
                        if (row.available == false) {
                            str += '<button title="生效" type="button" class="fa fa-plus add-vs btn btn-info waves-effect waves-light ' + o + '" aria-label="Left Align"></button>';
                        }
                        else {
                            if ($scope.model.offlinepolicy['policy-virtual-servers'].length == 1) {
                                return '-';
                            }
                            else {
                                str += '<button type="button" class=" remove-vs btn btn-info waves-effect waves-light ' + o + '" aria-label="Left Align"><span class="fa fa-minus"></span></button>';
                            }
                        }
                        if (row.available) {
                            str += '<button style="margin-left:5px" type="button" class="o btn btn-info update-vs-bt ' + o + '" aria-label="Left Align"><span class="fa fa-edit"></span></button>';
                        }

                        return str;
                    }
                }
            ], []],
            search: true,
            showRefresh: true,
            showColumns: true,
            sortName: 'id',
            sortOrder: 'desc',
            classes: "table-bordered  table-hover table-striped table",
            idField: 'id',
            pageSize: 20,
            pagination: false,
            minimumCountColumns: 2,
            resizeMode: 'overflow',
            sidePagination: 'client',
            pageList: [20, 40, 80, 200],
            responseHandler: "responseHandler",
            formatLoadingMessage: function () {
                return "<img class='loading-img' src='/static/img/loading_100.gif' /> " + resource['policy']['policy_policyInfoApp_vstable']['loading'];
            },
            formatNoMatches: function () {
                return '<i class="fa status-alert fa-exclamation-triangle" aria-hidden="true"></i> ' + resource['policy']['policy_policyInfoApp_vstable']['nodata'];
            }
        });
        $('#suggest-groups').bootstrapTable({
            toolbar: '#suggest-toolbar',
            columns: [[{
                field: 'state',
                radio: true,
                rowspan: 2,
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
                        return value;
                    }
                },
                {
                    field: 'name',
                    title: 'Name',
                    align: 'left',
                    resetWidth: true,
                    valign: 'middle',
                    sortable: true,
                    formatter: function (value, row, index) {
                        return value;
                    }
                },
                {
                    field: 'paths',
                    title: 'Path | Priority | SSL | VS | SLB',
                    width: '650px',
                    align: 'left',
                    valign: 'middle',
                    sortName: 'pathOrder',
                    sortable: true,
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
                                    '<div class="col-md-4" style="padding:0;line-height:34px;">' + priorityStr + '</div>' +
                                    '<div class="col-md-3" style="padding:0;line-height:34px;">' + sslStr + '</div>' +
                                    '<div class="col-md-2" style="padding:0;line-height:34px;">' + vsIdStr + '</div>' +
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
                    field: 'app-name',
                    title: 'App',
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    formatter: function (value, row, index) {
                        if (value == undefined) value = row['app-id'];
                        else value = row['app-id'] + '(' + value + ')'
                        return "<a style='word-break:break-all' title='" + value + "' target='_blank' href='/portal/app#?env=" + G.env + "&appId=" + row['app-id'] + "'>" + value + "</a>";
                    }
                },
                {
                    field: 'group-servers',
                    title: 'Members',
                    align: 'left',
                    valign: 'middle',
                    formatter: function (value, row, index) {
                        if (value) {
                            return value.length;
                        } else {
                            return '-';
                        }
                    },
                    sortable: true
                },
                {
                    field: 'status',
                    title: 'Status',
                    align: 'left',
                    valign: 'middle',
                    events: operateEvents,
                    formatter: function (value, row, index) {
                        var str = "";
                        switch (value) {
                            case "deactivated":
                                str = "<span class='status-gray'>未激活</span>";
                                break;
                            case "activated":
                                str = "<span class='status-green'>已激活</span>";
                                break;
                            case "toBeActivated":
                                str = "<span class=' status-yellow'>有变更(<a class='diffGroupVersion'>Diff</a>)</span>";
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
                    field: 'weight',
                    title: 'Weight',
                    align: 'left',
                    valign: 'middle',
                    formatter: function (value, row, index) {
                        return '<input type="text" class="form-control weight-control" value="' + value + '"/>';
                    },
                    sortable: true
                }
            ], []],
            classes: "table-bordered  table-hover table-striped table",
            minimumCountColumns: 2,
            showRefresh: true,
            search: true,
            showColumns: true,
            pagination: true,
            idField: 'id',
            pageSize: 20,
            resizable: true,
            resizeMode: 'overflow',
            sidePagination: 'client',
            pageList: [20, 40, 80, 200],
            responseHandler: "responseHandler",
            formatLoadingMessage: function () {
                return "<img class='loading-img' src='/static/img/loading_100.gif' /> 正在加载 Groups";
            },
            formatNoMatches: function () {
                return '<i class="fa status-alert fa-exclamation-triangle" aria-hidden="true"></i> 没有找到匹配的 Groups';
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
                    title: '权限',
                    align: 'left',
                    valign: 'middle',
                    sortable: true
                },
                {
                    field: 'name',
                    title: '描述',
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
                            return '<span class="status-red">已有权限</span>';
                        }
                        return '-';
                    }
                }
            ], []],
            data: $scope.data.policy_ops,
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
                return "<img class='loading-img' src='/static/img/loading_100.gif' /> 正在加载 Type";
            },
            formatNoMatches: function () {
                return '<i class="fa status-alert fa-exclamation-triangle" aria-hidden="true"></i> 没有找到匹配的 Type';
            }
        });
    };
    window.DiffVersionEvent = {
        'click .diff-version-bt': function (e, value, row) {
            getPolicyDataByVersion(row);
        }

    };

    function getGroupDataByVersion(row) {
        var baseText = '';
        var NewText = '';
        var groupId;

        if (row.group) {
            groupId = row.group.id;
        } else {
            groupId = row.id;
        }
        $q.all([
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
            ),

            $http.get(G.baseUrl + "/api/group?groupId=" + groupId).success(
                function (res) {
                    $scope.tobeActivatedGroupData = res;
                }
            )
        ]).then(
            function () {
                $scope.confirmActivateText = "线上版本与当前版本比较";
                var target = document.getElementById('fileDiffForm1');
                NewText = JSON.stringify(U.sortObjectFileds($scope.tobeActivatedGroupData), null, "\t");
                baseText = JSON.stringify(U.sortObjectFileds($scope.onlineGroupData), null, "\t");
                var ptext = '线下版本' + ($scope.onlineGroupData ? $scope.onlineGroupData.version : '-');
                var ctext = "线上版本" + $scope.tobeActivatedGroupData.version;
                diffTwoSlb(target, baseText, NewText, ptext, ctext);
                $('#diffVSDiv').modal('show');
            }
        );
    }

    function getPolicyDataByVersion(row) {
        var currentVersion = row['policy-version'];
        var id = row['target-id'];

        var c = currentVersion;
        var p = currentVersion - 1;

        if (row['operation'] == 'activate') {
            var gd = JSON.parse(row['data']);
            var gd_datas = gd['policy-datas'];
            var gd_sort = _.sortBy(gd_datas, 'version');
            p = gd_sort[0].version;
        }

        var param0 = {
            policyId: id,
            version: c
        };
        var param1 = {
            policyId: id,
            version: p
        };

        var request0 = {
            method: 'GET',
            url: G.baseUrl + '/api/archive/policy',
            params: param0
        };
        var request1 = {
            method: 'GET',
            url: G.baseUrl + '/api/archive/policy',
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
                var target = document.getElementById('fileDiffForm');

                var ptext = "变更前版本" + p;
                var ctext = "变更后版本" + c;
                if (row['operation'] == 'activate') {
                    ptext = '线上版本' + p;
                    ctext = "线下版本" + c;
                }
                diffTwoSlb(target, baseText, NewText, ptext, ctext);
                $('#diffVSDiv').modal('show');
            }
        );
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
                ' <td style="word-break: break-all">' + exception + '</td>' +
                ' </tr>' +
                '</table>' +
                '</div>';
            $('.operation-detail-div').append(str);
            $('#output-div').modal('show');
        }
    };
    $('#suggest-groups').on('check.bs.table uncheck.bs.table', function () {
        var row = $('#suggest-groups').bootstrapTable('getSelections');
        $scope.query.groupid = row[0].id;
    });

    $scope.toberemovedgroup = undefined;
    $scope.tobeaddedvs = undefined;
    $scope.toberemovedvs = undefined;
    $scope.tobeupdatedvs = undefined;
    window.operateEvents = {
        'blur .group_weight_edit': function (e, value, row, index) {
            var groupId = row.group.id;
            var weight = row.weight;
            var newValue = $('#group_weight_edit' + groupId).val();
            if (newValue == undefined) return;
            var policyClone = $.extend({}, true, $scope.model.extendedpolicy);
            var f = _.find(policyClone['controls'], function (item) {
                return item.group.id == groupId;
            });
            if (f) f.weight = parseInt(newValue);
            $scope.reloadGraphic(policyClone);
        },
        'click .update-vs-bt': function (e, value, row, index) {
            var vsId = row['virtual-server'].id;
            var path = row.path;
            var priority = row.priority;
            $scope.currentpriority = priority;
            $scope.currentpath = path;
            $('#updateVsIdSelector_value').val(vsId);
            $('#priorityid').val(priority);
            $('#path').val(path);
            $scope.tobeupdatedvs = row;
            $('#updateTrafficPolicyVSModal').modal('show');
        },
        'click .remove-group': function (e, value, row, index) {
            // ask to remove or not
            $scope.toberemovedgroup = row;
            $('#confirmRemovePolicy').modal('show');
        },
        'click .update-group-bt': function (e, value, row, index) {
            // ask to remove or not
            $scope.updatedrow = row;
            $scope.currentweight = row.weight;
            $scope.query.groupid = row.group.id;
            $('#updatewighttext').val(row.weight);
            $('#updateTrafficPolicyModal').modal('show').find('#updateGroupIdSelector_value').val(row.group.id.toString()).find('#updatewighttext').val(row.weight);
        },
        'click .remove-vs': function (e, value, row, index) {
            $scope.toberemovedvs = row;
            $('#confirmRemoveVSDialog').modal('show');
        },
        'click .add-vs': function (e, value, row, index) {
            $scope.tobeaddedvs = row;
            var vsId = row['virtual-server'].id;
            var t = $(e.target);
            if (t.attr('class').indexOf('fa-floppy-o') != -1) {
                var c = $('#path_vs_' + vsId).val() != undefined && $('#path_vs_' + vsId).val().trim() != '';
                if (c) {
                    $('#confirmAddVSDialog').modal('show');
                } else {
                    $('#path_vs_' + vsId).addClass('alert-border');
                    return;
                }
            }
            else {
                $scope.diffVses = _.reject($scope.diffVses, function (v) {
                    return v.vsId == vsId;
                });
                var p = $.extend(true, {}, $scope.view.extendedpolicy);
                var targetVs = {
                    'virtual-server': {
                        id: vsId
                    },
                    path: ''
                };
                p['policy-virtual-servers'].push(
                    targetVs
                );
                $scope.reloadGraphic(p);
                var policyGroups = $.extend(true, {}, $scope.view.extendedpolicy);
                $.each(policyGroups.controls, function (i, item) {
                    var group = item.group;

                    var id = group.id;
                    var status = _.find($scope.model.groups[id].properties, function (t) {
                        return t.name == 'status';
                    });
                    group.name = $scope.model.groups[id].name;
                    group.type = $scope.model.groups[id].type;
                    group.app = $scope.model.groups[id]['app-id'];
                    group.paths = $scope.getGroupPaths($scope.model.groups[id]);
                    group.idc = $scope.getGroupIdc($scope.model.groups[id]);
                    group.status = status ? status.value.toLowerCase() : '-';
                    item.status = status ? status.value.toLowerCase() : '-';

                    item.grouppath = group;
                });

                $('#controls-table').bootstrapTable('load', policyGroups.controls);

                t.attr('class', 'fa fa-floppy-o add-vs btn btn-danger waves-effect waves-light');

                var parent = $(e.target).parent();
                var siblings = $(parent).siblings();
                $.each(siblings, function (i, item) {
                    var child = $(item).children()[0];
                    if (child) {
                        child.remove();
                    }
                    if (i == 0) {
                        var d = $('<input placeholder="请输入 Path " class="form-control path-validate" id="path_vs_' + vsId + '" type="text" />');
                        $(d).blur(function (e) {
                            targetVs = {
                                'virtual-server': {
                                    id: vsId
                                },
                                path: $('#path_vs_' + vsId).val()
                            };
                            var index = _.findIndex(p['policy-virtual-servers'], function (r) {
                                return r['virtual-server'].id == vsId;
                            });

                            p['policy-virtual-servers'].splice(index, 1);
                            p['policy-virtual-servers'].push(targetVs);
                            $scope.reloadGraphic(p);
                        });
                        $(item).append(d);
                    }
                    if (i == 1) {
                        $(item).append('<a class="path-main-item " href="/portal/vs#?env=' + G.env + '&vsId=' + vsId + '">' + vsId + '(' + $scope.model.vses[vsId].name + ')</a>');
                    }
                    if (i == 3) {
                        $(item).append('<span>' + row.priority + '</span>');
                    }
                });
            }
        },
        'click .diffGroupVersion': function (e, value, row) {
            getGroupDataByVersion(row);
        }
    };
    // Remove Policy Virtual Server
    $scope.confirmRemoveVirtualServer = function () {
        $('#confirmRemoveVSDialog').modal('hide');
        var currentRow = $scope.toberemovedvs;
        var currentVsId = currentRow['virtual-server'].id;
        var index = _.findIndex($scope.view.offlinepolicy['policy-virtual-servers'], function (item) {
            return item['virtual-server'].id == currentVsId;
        });
        $scope.model.offlinepolicy['policy-virtual-servers'].splice(index, 1);
        var newPolicy = $.extend({}, true, $scope.model.offlinepolicy);
        var request = {
            url: G.baseUrl + '/api/policy/update?description=remove-vs-from-policy',
            method: 'POST',
            data: newPolicy,
            params: {}
        };
        $scope.processRequest(request, $('#operationConfrimModel'), '更新Traffic Policy', '更新成功');
    };

    // New Policy Virtual Server
    $scope.confirmAddVirtualServer = function () {
        var currentRow = $scope.tobeaddedvs;
        currentRow.path = $('#path_vs_' + currentRow['virtual-server'].id).val().trim();
        $('#confirmAddVSDialog').modal('hide');
        var newPolicy = $.extend({}, true, $scope.model.offlinepolicy);
        delete currentRow.available;
        newPolicy['policy-virtual-servers'].push(currentRow);

        $('#operationConfrimModel').modal("show").find(".modal-title").html("更新Traffic Policy");
        $('#operationConfrimModel').modal("show").find(".modal-body").html("正在更新.. <img src='/static/img/spinner.gif' />");
        // update traffic policy
        var request = {
            url: G.baseUrl + '/api/policy/update?description=addvstopolicy',
            method: 'POST',
            data: newPolicy,
            params: {}
        };
        $scope.processRequest(request, $('#operationConfrimModel'), '更新Traffic Policy', '更新成功');
    };
    // New Controls Area
    $scope.showSuggestArea = function () {
        if ($scope.model.extendedpolicy.controls) {
            return $scope.model.extendedpolicy.controls.length > 0;
        } else {
            return false;
        }
    };
    $scope.popAddNewControl = function () {
        $scope.query.groupid = '';
        $('#groupIdSelector_value').val('');
        $('#addNewTrafficPolicyModal').modal('show');
        $scope.error = {
            code: '',
            message: ''
        };
        if ($scope.model.extendedpolicy.controls.length > 0) {
            var suggests = [];
            var sameVses = $scope.sameVses;
            var vsIds = [];
            var temp = _.pluck(sameVses, 'vsId');
            $.each(temp, function (i, item) {
                vsIds.push(parseInt(item));
            });
            var vsIdString = '';
            if (vsIds && vsIds.length > 0) vsIdString = vsIds.join();
            if (vsIdString != '') {
                var param = {
                    vsId: vsIdString,
                    type: 'extended'
                };
                var request = {
                    method: 'GET',
                    url: G.baseUrl + '/api/groups?groupType=all',
                    params: param
                };
                $http(request).success(function (res, code) {
                    if (code == 200) {
                        if (res.total != 0) {
                            suggests = _.reject(res.groups, function (item) {
                                var v = _.find($scope.model.extendedpolicy.controls, function (r) {
                                    return r.group.id == item.id;
                                });
                                if (v != undefined) return true;
                                else return false;
                            });
                            var policyvses = _.map($scope.model.extendedpolicy['policy-virtual-servers'], function (r) {
                                return r['virtual-server'].id;
                            });

                            suggests = _.reject(suggests, function (item) {
                                var groupvses = _.map(item['group-virtual-servers'], function (r) {
                                    return r['virtual-server'].id;
                                });
                                var v = _.find(policyvses, function (t) {
                                    if (groupvses.indexOf(t) == -1) return true;
                                    return false;
                                });
                                if (v) return true;
                                return false;
                            });

                            var vsesPaths = {};
                            var v = [];
                            $.each($scope.model.extendedpolicy.controls, function (i, item) {
                                var id = item.group.id;
                                var c = _.filter($scope.model.groups[id]['group-virtual-servers'], function (r) {
                                    return vsIds.indexOf(r['virtual-server'].id) != -1;
                                });

                                v = v.concat(c);
                            });
                            if (v) {
                                $.each(v, function (i, v) {
                                    if (!vsesPaths[v['virtual-server'].id]) vsesPaths[v['virtual-server'].id] = [v.path.toLowerCase()];
                                    else vsesPaths[v['virtual-server'].id].push(v.path.toLowerCase());
                                });
                            }
                            // refine the suggests collection
                            suggests = _.reject(suggests, function (item) {
                                var v = _.find(item['group-virtual-servers'], function (s) {
                                    var vsId = s['virtual-server'].id;
                                    return vsesPaths[vsId] != undefined;
                                });
                                if (v) {
                                    var r = _.find(vsesPaths[v['virtual-server'].id], function (t) {
                                        return t.startsWith(v.path.toLowerCase()) || v.path.toLowerCase().startsWith(t);
                                    });

                                    if (r) return false;
                                    else return true;
                                }
                                return true;
                            });

                            $.each(suggests, function (i, group) {
                                var app = _.find($scope.model.apps, function (r) {
                                    return r['app-id'] == group['app-id'];
                                });
                                if (app)
                                    group['app-name'] = app['chinese-name'];
                                else group['app-name'] = '-';
                                var s = _.find(group.properties, function (item) {
                                    return item.name == 'status';
                                });
                                if (s) group.status = s.value;

                                group.weight = 50;
                                group.paths = [];
                                group.pathOrder = 0;
                                var c = 0;
                                $.each(group['group-virtual-servers'], function (i, gVs) {
                                    var o = {
                                        path: gVs.path,
                                        priority: gVs.priority,
                                        vsId: gVs['virtual-server'].id,
                                        slbId: gVs['virtual-server']["slb-ids"],
                                        ssl: gVs['virtual-server'].ssl,
                                        domain: gVs['virtual-server'].domains[0] == undefined ? "" : gVs['virtual-server'].domains[0].name
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
                            $scope.suggestGroups = suggests;
                            $('#suggest-groups').bootstrapTable('load', suggests);
                        }
                    } else {

                    }
                });
            }
        }
    };
    $scope.addNewControl = function (from) {
        $scope.error = {
            code: '',
            message: ''
        };
        var selectedGroupId = $scope.query.groupid;
        var weight = $scope.currentweight;
        if (from == 0) {
            if (!reviewData($('#suggest1'))) {
                return;
            }
        }
        if (from == 1) {
            var selected = $('#suggest-groups').find('tr.selected');
            if (selected.length == 0) {
                alert('请至少选择一个推荐的Group');
                return;
            } else {
                var c = selected[0];
                var d = $(c).find('.weight-control')[0];
                if (!$(d).val() || $(d).val() < 0) {
                    alert('推荐的Group Weight不能小于0');
                    return;
                }
                var e = parseInt($(d).val());
                if (isNaN(e)) {
                    alert('推荐的Group Weight必须是整数');
                    return;
                }

                weight = $(d).val();
            }
        }
        $scope.currentweight = weight;
        if (!selectedGroupId || !weight) {
            $scope.error = {
                code: '验证错误',
                message: 'Group Id 和 Weight 都是必填字段'
            };
            return;
        }

        // Area: already added?
        var existed = _.find($scope.view.extendedpolicy.controls, function (item) {
            return item.group.id == selectedGroupId;
        });
        if (existed) {
            $scope.error = {
                code: '重复',
                message: '当前Group, GroupId: ' + selectedGroupId + ', 已经在当前Policy中不能重复添加'
            };
            return;
        }

        // Existing group and this group compare
        var existingControls = $scope.view.extendedpolicy.controls;
        var existingGroupVses = {};
        $.each(existingControls, function (i, item) {
            existingGroupVses[item.group.id] = $scope.abstractGroupVsPaths($scope.model.groups[item.group.id])
        });

        var currentGroupVses = {};
        var currentGroupPaths = $scope.abstractGroupVsPaths($scope.model.groups[selectedGroupId]);
        currentGroupVses[selectedGroupId] = currentGroupPaths;
        var temp = $scope.validateControl(currentGroupVses, existingGroupVses);

        if (!temp) {
            $scope.error = {
                code: '验证出错',
                message: '当前Group与现有Traffic Policy 不存在相同的Virtual Server'
            };
            return;
        }
        $('#addNewTrafficPolicyModal').modal('hide');

        $('#confirmAddPolicy').modal('show');
    };
    $scope.updateControl = function () {
        $scope.error = {
            code: '',
            message: ''
        };
        var weight = $scope.currentweight;
        if (!weight || $('#updateGroupIdSelector_value').val().trim() == '') {
            $scope.error = {
                code: '验证错误',
                message: ' Weight和Group都是必填字段'
            };
            return;
        }
        var a = parseInt(weight);
        if (isNaN(a)) {
            $scope.error = {
                code: '验证错误',
                message: ' Weight必须是>=0的整数'
            };
            return;
        }

        if (a < 0) {
            $scope.error = {
                code: '验证错误',
                message: 'Weight必须是>=0的整数'
            };
            return;
        }

        var existingControls = $scope.model.offlinepolicy.controls;
        var existingGroupVses = {};
        $.each(existingControls, function (index, current) {
            var e = $scope.abstractGroupVsPaths($scope.model.groups[current.group.id]);
            existingGroupVses[current.group.id] = e;
        });

        var currentGroupVses = {};
        var currentGroupPaths = $scope.abstractGroupVsPaths($scope.model.groups[$scope.query.groupid]);
        currentGroupVses[$scope.query.groupid] = currentGroupPaths;

        var currentRow = $scope.updatedrow;
        var changed = ($scope.query.groupid != undefined && $scope.query.groupid != currentRow.group.id) || $scope.currentweight.toString().trim() != currentRow.weight.toString();
        if (!changed) {
            $('#updateTrafficPolicyModal').modal('hide');
            return;
        }
        if (!$scope.validateControl(currentGroupVses, existingGroupVses)) {
            $scope.error = {
                code: '验证错误',
                message: "当前要添加的Group与现有Traffic Policy不存在相同的Virtual Server"
            };
            return;
        }
        $('#updateTrafficPolicyModal').modal('hide');
        $('#confirmUpdatePolicy').modal('show');
    };
    $scope.confirmAddGroup = function () {
        $('#confirmAddPolicy').modal('hide');
        var selectedGroupId = $scope.query.groupid;
        var weight = $scope.currentweight;
        var control = {
            group: {
                id: parseInt(selectedGroupId)
            },
            weight: weight
        };

        var newPolicy = $.extend({}, true, $scope.model.offlinepolicy);
        newPolicy.controls.push(control);

        $('#operationConfrimModel').modal("show").find(".modal-title").html("更新Traffic Policy");
        $('#operationConfrimModel').modal("show").find(".modal-body").html("正在更新.. <img src='/static/img/spinner.gif' />");
        // update traffic policy
        var request = {
            url: G.baseUrl + '/api/policy/update?description=addgrouptopolicy',
            method: 'POST',
            data: newPolicy,
            params: {}
        };
        $scope.processRequest(request, $('#operationConfrimModel'), '更新Traffic Policy', '更新成功');
    };
    $scope.confirmUpdateGroup = function () {
        $('#confirmUpdatePolicy').modal('hide');
        var currentRow = $scope.updatedrow;
        var newPolicy = $.extend({}, true, $scope.model.offlinepolicy);
        var f = _.find(newPolicy.controls, function (item) {
            return item.group.id == currentRow.group.id;
        });
        if (f) {
            f.group.id = $scope.query.groupid;
            f.weight = $scope.currentweight;
        }
        $('#operationConfrimModel').modal("show").find(".modal-title").html("更新Traffic Policy");
        $('#operationConfrimModel').modal("show").find(".modal-body").html("正在更新.. <img src='/static/img/spinner.gif' />");
        // update traffic policy
        var request = {
            url: G.baseUrl + '/api/policy/update?description=' + $scope.query.updategroupreason,
            method: 'POST',
            data: newPolicy,
            params: {}
        };
        $scope.processRequest(request, $('#operationConfrimModel'), '更新Traffic Policy', '更新成功');
    };
    $scope.validateControl = function (currentGroupVses, existingGroupVses) {
        var cloneCurrentGroupVses = $.extend(true, {}, currentGroupVses);

        var currentVses = $scope.model.extendedpolicy['policy-virtual-servers'];
        var sameVses = _.map(currentVses, function (item) {
            return item['virtual-server'].id;
        });

        var cVses = [];
        $.each(_.keys(cloneCurrentGroupVses), function (i, item) {
            cVses = cVses.concat(_.pluck(cloneCurrentGroupVses[item], 'vsId'));
        });
        cVses = _.unique(cVses);

        if (cVses.length < sameVses.length) return false;

        var cs = _.filter(sameVses, function (item) {
            var v = _.find(cVses, function (t) {
                return t == item;
            });
            if (!v) return true;
            else return false;
        });

        if (cs && cs.length > 0) return false;
        return true;
    };
    $scope.validateGroupVsCollection = function (cloneExistingGroupVses, cloneCurrentGroupVses, groupId) {
        var error = '';
        var keys = _.keys(cloneExistingGroupVses);
        $.each(cloneCurrentGroupVses[groupId], function (index, item) {
            delete item.priority;
        });
        $.each(keys, function (index, key) {
            $.each(cloneExistingGroupVses[key], function (t, item) {
                delete item.priority;
            });
            var sames = _.filter(cloneExistingGroupVses[key], function (obj) {
                return _.findWhere(cloneCurrentGroupVses[groupId], obj);
            });
            if (sames && sames.length > 0) {
                if (sames.length != $scope.sameVses.length) {
                    error = '与现有Group的Virtual Server个数不等，不允许添加 ';
                }
            } else {
                error += '与已添加的Group: ' + key + '(' + $scope.model.groups[key].name + '), 不存在共同的域名或者PATH. ';
            }
        });
        return error;
    }
    $scope.showErrorMessage = function () {
        var show = false;
        if ($scope.error && $scope.error.code) {
            show = true;
        }
        return show;
    };
    $scope.confirmRemoveGroup = function () {
        var row = $scope.toberemovedgroup;
        var currentGroupId = row.group.id;
        var index = _.findIndex($scope.view.offlinepolicy.controls, function (item) {
            return item.group.id == currentGroupId;
        });
        $scope.model.offlinepolicy.controls.splice(index, 1);
        var newPolicy = $.extend({}, true, $scope.model.offlinepolicy);

        $('#confirmRemovePolicy').modal('hide');
        $('#operationConfrimModel').modal("show").find(".modal-title").html("更新Traffic Policy");
        $('#operationConfrimModel').modal("show").find(".modal-body").html("正在更新.. <img src='/static/img/spinner.gif' />");
        // update traffic policy
        var request = {
            url: G.baseUrl + '/api/policy/update?description=' + $scope.query.deletegroupreason,
            method: 'POST',
            data: newPolicy,
            params: {}
        };
        $scope.processRequest(request, $('#operationConfrimModel'), '更新Traffic Policy', '更新成功');
    }
    $scope.updateVirtualServer = function () {
        var newpriority = $scope.currentpriority;
        var newpath = $scope.currentpath;
        if (reviewData($('#updateTrafficPolicyVSModal')) == false) return;
        $('#updateTrafficPolicyVSModal').modal('hide');
        if (newpriority == $scope.tobeupdatedvs.priority && newpath.toLowerCase().trim() == $scope.tobeupdatedvs.path) return;
        $('#confirmUpdateVs').modal('show');
    };
    $scope.confirmUpdateVS = function () {
        var newpriority = $scope.currentpriority;
        var newpath = $scope.currentpath;

        var vsId = $scope.tobeupdatedvs['virtual-server'].id;
        $('#confirmUpdateVs').modal('hide');

        var newPolicy = $.extend({}, true, $scope.model.offlinepolicy);
        var f = _.find(newPolicy['policy-virtual-servers'], function (item) {
            return item['virtual-server'].id == vsId;
        });

        if (f) {
            f.path = newpath.trim();
            f.priority = newpriority;
        }
        $('#operationConfrimModel').modal("show").find(".modal-title").html("更新Traffic Policy");
        $('#operationConfrimModel').modal("show").find(".modal-body").html("正在更新.. <img src='/static/img/spinner.gif' />");
        // update traffic policy
        var request = {
            url: G.baseUrl + '/api/policy/update?description=updatevsofpolicy',
            method: 'POST',
            data: newPolicy,
            params: {}
        };
        $scope.processRequest(request, $('#operationConfrimModel'), '更新Traffic Policy', '更新成功');
    };
    $scope.showActionPanel = function (vs) {
        if ($scope.changevs[vs]) {
            return false;
        }
        return true;
    };
    $scope.showRevertPanel = function (vs) {
        if ($scope.changevs[vs]) {
            return true;
        }
        return false;
    };
    $scope.resetVsChange = function () {
        var hashData = {};
        hashData.timeStamp = new Date().getTime();
        H.setData(hashData);
    };
    $scope.batchEditControlWeight = function () {
        var resource = $scope.resource;
        if (!resource || _.keys(resource).length == 0) return;

        var t = $('#batch-edit-weight');
        if ($(t).hasClass('edit')) {
            $.each($('.group_weight_span'), function (i, item) {
                var v = $(item).attr('id');
                $(item).siblings('input').removeClass('hide');
                $(item).removeClass('hide').addClass('hide');
            });
        } else {
            // save
            $('#batchUpdateGroupWeightDialog').modal('show');
        }

        if ($(t).hasClass('edit')) {
            $(t).text(resource['policy']['policy_policyInfoApp_save']);
            $(t).removeClass('edit').addClass('add');
        } else {
            $(t).removeClass('add').addClass('edit');
        }
    };

    $scope.batchUpdateControlWeight = function () {
        $('#batchUpdateGroupWeightDialog').modal('hide');
        var t = $('#batch-edit-weight');
        $(t).removeClass('add').addClass('edit');
        $(t).text('批量改权重');

        var newPolicy = $.extend({}, true, $scope.model.offlinepolicy);

        $.each($('.group_weight_edit'), function (i, item) {
            var v = $(item).attr('id');
            var groupId = v.substring(17, v.length);
            var id = parseInt(groupId);
            var f = _.find(newPolicy.controls, function (g) {
                return g.group.id == id;
            });
            if (f) f.weight = $(item).val();
        });

        var request = {
            url: G.baseUrl + '/api/policy/update?description=' + $scope.query.changeweightreason,
            method: 'POST',
            data: newPolicy,
            params: {}
        };
        $scope.processRequest(request, $('#operationConfrimModel'), '更新Traffic Policy', '更新成功');
    };
    // Load Data Area
    $scope.loadData = function (hashData) {
        var policyId = hashData.policyId;
        var groupsParam = {
            type: 'extended'
        };
        var vsesParam = {
            type: 'info'
        };

        var param0 = {
            mode: 'online',
            policyId: policyId
        };
        var param1 = {
            type: 'extended',
            policyId: policyId
        };

        var param3 = {
            targetId: policyId,
            type: 'policy',
            count: 3
        };
        var param4 = {
            "type": 'extended'
        };
        var param5 = {
            targetId: policyId,
            type: 'policy',
            op: 'new'
        };
        var policyOnlineRequest = {
            method: 'GET',
            url: G.baseUrl + '/api/policy',
            params: param0
        };
        var policyExtendedRequest = {
            method: 'GET',
            url: G.baseUrl + '/api/policy',
            params: param1
        };
        var currentUserRequest = {
            method: 'GET',
            url: G.baseUrl + '/api/auth/current/user'
        };
        var groupsRequest = {
            method: 'GET',
            url: G.baseUrl + '/api/groups?groupType=all',
            params: groupsParam
        };
        var vsesRequest = {
            method: 'GET',
            url: G.baseUrl + '/api/vses',
            params: param4
        };
        var policyCurrentLogs = {
            method: 'GET',
            url: G.baseUrl + '/api/logs',
            params: param3
        };
        var policyNewLogs = {
            method: 'GET',
            url: G.baseUrl + '/api/logs',
            params: param5
        };
        var usersRequest = {
            method: 'GET',
            url: G.baseUrl + '/api/auth/users'
        };
        var appsRequest = {
            method: 'GET',
            url: G.baseUrl + '/api/apps'
        };
        var userResourcesRequest = {
            method: 'GET',
            url: G.baseUrl + '/api/auth/user/resources'
        };
        $q.all(
            [
                $http(appsRequest).success(
                    function (response) {
                        if (response['apps']) {
                            $scope.model.apps = _.indexBy(response['apps'], function (item) {
                                return item['app-id'];
                            });
                        }
                    }
                ),
                $http(policyCurrentLogs).success(
                    function (response) {
                        if (response['operation-log-datas']) {
                            $scope.model.currentlogs = response['operation-log-datas'];
                        }
                    }
                ),
                $http(vsesRequest).success(
                    function (response) {
                        var vses = response['virtual-servers'];
                        if (vses) {
                            $scope.model.vses = _.indexBy(vses, 'id');
                        }
                    }
                ),

                $http(groupsRequest).success(
                    function (response) {
                        var groups = response['groups'];
                        if (groups) {
                            $scope.model.groups = _.indexBy(groups, 'id');
                        }
                    }
                ),

                $http(policyCurrentLogs).success(
                    function (response) {
                        if (response['operation-log-datas']) {
                            $scope.model.currentlogs = response['operation-log-datas'];
                        }
                    }
                ),

                $http(policyOnlineRequest).success(
                    function (response) {
                        if (response.code != undefined) {
                            $scope.model.onlinepolicy = {"data": "No Online Data", "version": "Unknow"};
                        }
                        else {
                            $scope.model.onlinepolicy = {"data": response};
                        }
                        $scope.view.onlinepolicy = $.extend(true, {}, response);
                    }
                ),
                $http(policyExtendedRequest).success(
                    function (response) {
                        $scope.model.extendedpolicy = response;
                        $scope.view.extendedpolicy = $.extend(true, {}, response);

                        $scope.view.policyhistoryversions = _.range(1, $scope.model.extendedpolicy.version + 1);

                        $scope.model.offlinepolicy = $.extend(true, {}, response);
                        $scope.model.offlinepolicy = _.omit($scope.model.offlinepolicy, "properties");
                        $scope.model.offlinepolicy = _.omit($scope.model.offlinepolicy, "tags");
                        $scope.view.offlinepolicy = $.extend(true, {}, $scope.model.offlinepolicy);
                        $scope.view.historyversions = _.range(1, $scope.model.offlinepolicy.version + 1);
                    }
                ),
                $http(policyNewLogs).success(
                    function (response) {
                        $scope.model.newpolicylogs = response['operation-log-datas'];
                    }
                ),
                $http(currentUserRequest).success(
                    function (response) {
                        $scope.query.user = response.name;
                    }
                ),
                $http(usersRequest).success(
                    function (response) {
                        if (response && response.total > 0) {
                            $scope.model.users = _.indexBy(response.users, function (item) {
                                return item['user-name']
                            });
                            $scope.model.usersByChineseName = _.indexBy(response.users, 'chinese-name');

                        }
                    }
                ),
                $http(userResourcesRequest).success(
                    function (response) {
                        $scope.query.resource = response['user-resources'];
                    }
                )
            ]
        ).then(
            function () {
                if ($scope.model.extendedpolicy.code) {
                    // current request is not a valid one
                    exceptionNotify("出错了", "加载 Policy失败了，ID=" + hashData.policyId + "失败了，失败原因" + $scope.model.extendedpolicy.code + "::" + $scope.model.extendedpolicy.message);
                    return;
                } else {
                    setTimeout(
                        function () {
                            $('.alert-danger').remove();
                        },
                        1000
                    );
                }
                $.each($scope.model.currentlogs, function (i, item) {
                    var data = item.data;
                    var isJson = IsJsonString(data);

                    var version = '-';

                    if (isJson) {
                        data = JSON.parse(data);
                        if (data['policy-datas'] && data['policy-datas'].length > 0) {
                            version = data['policy-datas'][0].version;
                        }
                    }
                    item['policy-version'] = version;
                });
                $scope.view.extendedpolicy.status = $scope.getPolicyStatusProperty();
                $scope.view.extendedpolicy.owner = $scope.getPolicyOwnerProperty();
                $scope.view.extendedpolicy.targetFor = $scope.getPolicyTarget();
                $scope.reloadTable($scope.view.extendedpolicy);
                $scope.reloadGraphic($scope.view.extendedpolicy);

                if (hashData.diff) {
                    $scope.activatePolicyClick();
                }
            }
        )
    };
    $scope.getOwnerNameByChineseName = function (chineseName) {
        return $scope.model.usersByChineseName[chineseName] ? $scope.model.usersByChineseName[chineseName]['user-name'] : 'unknown';
    };
    $scope.getGroupPaths = function (group) {
        var paths = [];
        var c = 0;
        $.each(group['group-virtual-servers'], function (i, gVs) {
            var o = {
                path: gVs.path,
                priority: gVs.priority,
                vsId: gVs['virtual-server'].id,
                slbId: gVs['virtual-server']["slb-ids"],
                ssl: gVs['virtual-server'].ssl,
                domain: gVs['virtual-server'].domains[0] == undefined ? "" : gVs['virtual-server'].domains[0].name
            };
            paths.push(o);

            //Set path order
            if (c == 0) {
                if (o.priority >= 0) {
                    group.pathOrder = o.priority * 1000000 + group.id;
                } else {
                    group.pathOrder = o.priority * 1000000 - group.id;
                }
            }
            c++;
        });
        return paths;
    };
    $scope.getGroupIdc = function (group) {
        var c = _.find(group.properties, function (item) {
            return item.name == 'idc';
        });
        if (c) {
            return c.value;
        }
        return '-';
    };
    $scope.abstractGroupVsPaths = function (group) {
        var vsPaths = [];
        var groupVirtualServers = group['group-virtual-servers'];
        $.each(groupVirtualServers, function (index, item) {
            var vs = {
                vsId: item['virtual-server'].id,
                path: item['path'],
                priority: item.priority
            };
            vsPaths.push(vs);
        });
        return vsPaths;
    };
    $scope.getGroupSameAndDiffrentVsPaths = function (all) {
        var result = {
            same: [],
            diffs: []
        };
        var keys = _.keys(all);
        var values = _.values(all);
        var len = keys.length;

        if (len != 0) {
            var temp = [];
            $.each(values, function (i, item) {
                temp = temp.concat(item);
            });

            var vsCollection = {};
            $.each(temp, function (i, item) {
                if (!vsCollection[item.vsId]) vsCollection[item.vsId] = [item];
                else vsCollection[item.vsId].push(item);
            });

            $.each(_.keys(vsCollection), function (index, item) {
                var vsId = item;

                if (vsCollection[item].length == len) {
                    result.same.push({
                        vsId: vsId,
                        available: true,
                        priority: $scope.abstractVsPriority(vsCollection[vsId])
                    });
                }
                else {
                    result.diffs.push({
                        vsId: vsId
                    });

                    if ($scope.diffVses && $scope.diffVses.length > 0) {
                        var v = _.filter($scope.diffVses, function (r) {
                            var t = _.find(result.diffs, function (s) {
                                return s.vsId == r.vsId;
                            });
                            if (t) return false;
                            return true;
                        });
                        result.diffs = result.diffs.concat(v);
                    }
                }
            });
        }
        return result;
    };
    $scope.abstractVsPriority = function (array) {
        var result = _.max(_.pluck(array, 'priority')) + 1;
        return result;
    };
    $scope.reloadTable = function (policy) {
        $('#controls-table').bootstrapTable('removeAll');
        $('#vs-table').bootstrapTable('removeAll');
        var p1 = A.canDo("Policy", "UPDATE", $scope.query.policyId) && $scope.query.showextended;
        if (!p1) {
            $('#controls-table').bootstrapTable('hideColumn', 'Operation');
            $('#vs-table').bootstrapTable('hideColumn', 'Operation');
        }
        var existingControls = policy.controls;
        var existingGroupVses = {};
        $.each(existingControls, function (index, current) {
            var e = $scope.abstractGroupVsPaths($scope.model.groups[current.group.id]);
            existingGroupVses[current.group.id] = e;
        });
        var t = $scope.getGroupSameAndDiffrentVsPaths(existingGroupVses);

        $scope.sameVses = t.same;
        $scope.diffVses = t.diffs;

        // summarize the traffic policy groups and virtual servers
        var policyGroups = $.extend(true, {}, policy);
        $.each(policyGroups.controls, function (i, item) {
            var group = item.group;
            var id = group.id;
            var appId = $scope.model.groups[id]['app-id'];
            group.appId = appId;
            var appName = $scope.model.apps[appId] ? $scope.model.apps[appId]['chinese-name'] : '-';
            var status = _.find($scope.model.groups[id].properties, function (t) {
                return t.name == 'status';
            });
            group.name = $scope.model.groups[id].name;
            group.app = appId + '(' + appName + ')';
            group.type = $scope.model.groups[id].type;
            group.paths = $scope.getGroupPaths($scope.model.groups[id]);
            group.idc = $scope.getGroupIdc($scope.model.groups[id]);
            var g = $scope.model.groups[id].properties;
            var v = _.find(g, function (r) {
                return r.name == 'status';
            });
            if (v) item.status = v.value.toLowerCase();
            item.grouppath = group;
        });
        $scope.bindinGroups = policyGroups['controls'];

        // 从当前Groups相同的Vs里面找出不在当前Policy control里面的Vs
        var existing_vses = policyGroups['policy-virtual-servers'] || [];
        var f = _.filter($scope.sameVses, function (item) {
            var e = _.find(existing_vses, function (i) {
                return item.vsId == i['virtual-server'].id;
            });
            if (e) return false;
            else return true;
        });


        if (f) {
            $.each(f, function (i, item) {
                existing_vses.push(
                    {
                        'virtual-server': {
                            id: item.vsId
                        },
                        path: 'N/A',
                        priority: item.priority,
                        available: false
                    }
                );
                $scope.diffVses.push({
                    vsId: item.vsId,
                    path: item.path
                });
            });
        }

        $.each(existing_vses, function (i, item) {
            var vsId = item['virtual-server'].id;
            var idc = '-';
            var e = _.find($scope.model.vses[vsId].properties, function (r) {
                return r.name == 'idc';
            });
            if (e && e.value) idc = e.value;
            item.idc = idc;
        });
        $('#controls-table').bootstrapTable("load", policyGroups.controls);
        $('#vs-table').bootstrapTable("load", existing_vses);

        $('#controls-table').bootstrapTable("hideLoading");
        $('#vs-table').bootstrapTable('hideLoading');
    };
    $scope.reloadGraphic = function (policy) {
        var policy_clone = $.extend({}, true, policy);

        var domains = [];
        var idc = '-';
        var pollicyname = '-';

        var diffs = $scope.diffVses;
        policy_clone['policy-virtual-servers'] = _.reject(policy_clone['policy-virtual-servers'], function (item) {
            var v = _.find(diffs, function (r) {
                return r.vsId == item['virtual-server'].id;
            });
            if (v == undefined) return false;
            return true;
        });
        var vses = policy_clone['policy-virtual-servers'];

        var result = _.map(vses, function (item) {
                var vsId = item['virtual-server'].id;
                var path = item.path.trim();
                if (path == '/' || path == '~* ^/') path = path;
                else {
                    // standard paths?
                    if (path.startsWith('~* ^/') && path.endsWith('($|/|\\?)')) {
                        path = path.substring(5, path.length - 8);
                    } else {
                        if (!path.endsWith('($|/|\\?)')) {
                            path = path.substring(5, path.length);
                        }
                    }
                }


                var slbops = [];
                var soaops = [];
                var reg = /(.*)\/(\(xml\|json\|bjjson\|x-protobuf\))\/(.*)/g;
                var m = reg.exec(path);
                if (m) {
                    var s = m[1];
                    var t = m[3];
                    if (s) {
                        path = s;
                    }
                    if (t) {
                        var l = t.length;
                        var start = t[0] == '(' ? 1 : 0;
                        var end = start == 0 ? l : l - 1;
                        soaops = t.substring(start, end).split('|');
                    }
                }

                var reg2 = /(.*)\/\((.*)\)/g
                var n = reg2.exec(path);
                if (n) {
                    var u = n[1];
                    var v = n[2];
                    if (u) path = u;
                    if (v) slbops = v.split('|');
                }

                var vs = $scope.model.vses[vsId];
                if (vs) {
                    var p = _.find(vs.properties, function (r) {
                        return r.name == 'idc';
                    });
                    if (p) idc = p.value;

                    var protocol = vs.ssl ? 'https://' : 'http://';
                    domains = _.map(vs.domains, function (t) {
                        return protocol + t.name + '/' + path;
                    });
                }
                pollicyname = policy_clone.name;

                var totalWeight = _.reduce(_.pluck(policy_clone.controls, 'weight'), function (memo, num) {
                    return memo + num;
                });
                var groups_temp = _.map(policy_clone.controls, function (s) {
                    var groupidc = '-';
                    var a = _.find($scope.model.groups[s.group.id].properties, function (t) {
                        return t.name == 'idc';
                    });
                    if (a) groupidc = a.value;

                    var groupdeployidc = '-';
                    var b = _.find($scope.model.groups[s.group.id].properties, function (t) {
                        return t.name == 'idc_code';
                    });
                    if (b) groupdeployidc = b.value;

                    var appid = $scope.model.groups[s.group.id]['app-id'];
                    var props = _.indexBy($scope.model.groups[s.group.id].properties, function (v) {
                        return v.name.toLowerCase();
                    });

                    return {
                        name: $scope.model.groups[s.group.id].name,
                        id: s.group.id,
                        language: props['language'] ? props['language'].value : '未知',
                        weight: T.getPercent(s.weight, totalWeight),
                        idc: groupidc,
                        idc_code: G['idcs'][groupdeployidc] || groupdeployidc,

                        'app-id': appid,
                        'app-name': $scope.model.apps[appid] ? $scope.model.apps[appid]['chinese-name'] : '-'
                    };
                });

                return {
                    vs: {
                        id: vsId,
                        domains: domains,
                        idc: idc,
                        soaops: soaops,
                        slbops: slbops
                    },
                    policy: {
                        id: $scope.model.extendedpolicy.id,
                        name: pollicyname
                    },
                    groups: groups_temp
                }
            }
        );

        var canvas = $('.graphics .diagram');
        canvas.html('');
        var diagramOnly = false;
        var noOperation = true;
        var http = $http;
        var env = $scope.env;
        var scopegroups = $scope.model.groups;
        var scopeapps = $scope.model.apps;
        var dashboardUrl = G[G.env].urls.dashboard + "/data";

        /*   TrafficPolicyGraphics.draw(result, $('.graphics .diagram'));*/
        var c = {
            '+policy_clone.id+': policy_clone
        };
        userPolicyApp.drawListOfPolicyGraphics(c, result, http, env, scopegroups, scopeapps, dashboardUrl, function (id) {
            }, function (id) {
            },
            function (id) {
                window.open('/portal/policy/traffic#?env=' + env + '&policyId=' + id, '_blank');
            }, function (id) {
                window.open('/portal/policy/traffic#?env=' + env + '&policyId=' + id, '_blank');
            }, canvas, diagramOnly, noOperation
        )
    };

    $scope.init = function () {
        $scope.view = {
            "onlinepolicy": {
                "version": "未知",
                "name": "未知",
                "id": '未知'
            },
            "offlinepolicy": {
                "version": "未知",
                "name": "未知",
                "id": '未知'
            },
            "extendedpolicy": {
                "tags": ["未知"],
                "properties": [{"name": "idc", "value": "未知"}, {"name": "status", "value": "未知"}],
                "version": '未知',
                "createdtime": '',
                "name": "未知",
                "id": '未知'
            }
        }
    };
    $scope.getOperationText = function (x) {
        var resource = $scope.resource;
        if (!resource || _.keys(resource).length == 0) return;
        return resource['log']['log_operationLogApp_opmapping'][x];
    };
    var userPolicyApp = '';
    $scope.hashChanged = function (hashData) {
        $scope.resource = H.resource;
        $scope.resource = H.resource;
        $scope.forceUpdatePolicy = false;
        $scope.suggestGroups = [];
        $scope.forceUpdateRequest = undefined;
        $scope.changevs = {};
        $scope.sameVses = [];
        $scope.diffVses = [];
        $scope.bindinGroups = [];
        $scope.toberemovedgroup = undefined;
        $scope.tobeupdatedvs = undefined;
        $scope.updatedrow = {};
        $scope.error = {
            code: '',
            message: ''
        };
        if (hashData.env) {
            $scope.env = hashData.env;
        }
        if (hashData.policyId) {
            $scope.query.policyId = hashData.policyId;
            $scope.loadData(hashData);
        }

        userPolicyApp = new UserPolicyApp(hashData, $http, $q, $scope.env, $scope.resource['policynew']["policynew_trafficEditApp_diagram"]);
        $scope.initTable();
        // init the wreches
        $scope.tableOps.policyGroup.showOperations = false;
        $scope.tableOps.policyVs.showOperations = false;

        $scope.tableOps.policyGroup.showMoreColumns = false;
        $scope.tableOps.policyVs.showMoreColumns = false;
        $('#controls-table').bootstrapTable('showLoading');
        $('#vs-table').bootstrapTable('showLoading');
    };
    H.addListener("policyInfoApp", $scope, $scope.hashChanged);

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
})
;
angular.bootstrap(document.getElementById("policy-info-area"), ['policyInfoApp']);
