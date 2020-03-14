/**
 * Created by ygshen on 2016/10/17.
 */
var userEditInfoApp = angular.module('userEditInfoApp', ["http-auth-interceptor", "angucomplete-alt"]);
userEditInfoApp.controller('userEditInfoController', function ($scope, $http, $q) {

    /** Group Related:
     * A:ACTIVATE
     * D: DEACTIVATE
     * U: UPDATE
     * E: DELETE
     * R: READ
     * P: PROPERTY
     * S: SYNC
     * M: OP_MEMBER
     * L: OP_PULL
     * H: OP_HEALTH_CHECK
     * F: FORCE
     **/
    /** SLB Related:
     * A:ACTIVATE
     * D: DEACTIVATE
     * U: UPDATE
     * E: DELETE
     * R: READ
     * P: PROPERTY
     * S: SYNC
     * I: ADMIN_INFO
     * W: WAF
     * F: FORCE
     **/
    /** VS Related:
     * A:ACTIVATE
     * D: DEACTIVATE
     * U: UPDATE
     * E: DELETE
     * R: READ
     * P: PROPERTY
     * S: SYNC
     * C: CERT
     * F: FORCE
     **/
    $scope.query = {
        role: {
            id: '',
            'role-name': ''
        },
        slb: {
            id: ''
        },
        vs: {
            id: ''
        },
        group: {
            id: ''
        },
        policy: {
            id: ''
        },
        dr: {
            id: ''
        },
        user: {
            id: '',
            'user-name': '',
            email: '',
            bu: '',
            roles: [],
            'data-resources': []
        },
        authUserName: ''
    };
    $scope.data = {
        slb_ops: [
            {id: 'ACTIVATE', name: '激活SLB 上线'},
            {id: 'DEACTIVATE', name: '下线SLB'},
            {id: 'UPDATE', name: '更新SLB属性'},
            {id: 'DELETE', name: '删除SLB'},
            {id: 'READ', name: '查询SLB信息'},
            {id: 'PROPERTY', name: '给SLB 打Tag或者Property'},
            {id: 'SYNC', name: '调用SLB接口向CMS同步数据'},
            {id: 'ADMIN_INFO', name: 'TBD'},
            {id: 'WAF', name: '上传WAF包'},
            {id: 'FORCE', name: 'SLB Force 操作'}
        ],
        vs_ops: [
            {id: 'ACTIVATE', name: '激活 VS 上线'},
            {id: 'DEACTIVATE', name: '下线 VS'},
            {id: 'UPDATE', name: '更新VS属性'},
            {id: 'DELETE', name: '删除VS'},
            {id: 'READ', name: '查询VS信息'},
            {id: 'PROPERTY', name: '给VS 打Tag或者Property'},
            {id: 'SYNC', name: '调用SLB接口向CMS同步数据'},
            {id: 'CERT', name: '上传证书'},
            {id: 'FORCE', name: 'VS Force操作'}
        ],
        group_ops: [
            {id: 'ACTIVATE', name: '激活 GROUP 上线'},
            {id: 'DEACTIVATE', name: '下线 GROUP'},
            {id: 'UPDATE', name: '更新GROUP属性'},
            {id: 'DELETE', name: '删除GROUP'},
            {id: 'READ', name: '查询GROUP信息'},
            {id: 'PROPERTY', name: '给GROUP 打Tag或者Property'},
            {id: 'SYNC', name: '调用SLB接口向CMS同步数据'},
            {id: 'OP_MEMBER', name: 'Member拉入拉出'},
            {id: 'OP_PULL', name: 'Group拉入拉出'},
            {id: 'OP_HEALTH_CHECK', name: '健康检测拉入拉出'},
            {id: 'FORCE', name: 'Group Force操作'}
        ],
        policy_ops: [
            {id: 'ACTIVATE', name: '激活 POLICY 上线'},
            {id: 'DEACTIVATE', name: '下线 POLICY'},
            {id: 'UPDATE', name: 'POLICY'},
            {id: 'DELETE', name: '删除 POLICY'},
            {id: 'READ', name: '查询 POLICY 信息'},
            {id: 'PROPERTY', name: '给 POLICY 打Tag或者Property'},
            {id: 'FORCE', name: 'POLICY的Force操作'}
        ],
        dr_ops: [
            {id: 'ACTIVATE', name: '激活 DR 上线'},
            {id: 'DEACTIVATE', name: '下线 DR'},
            {id: 'UPDATE', name: 'DR'},
            {id: 'DELETE', name: '删除 DR'},
            {id: 'READ', name: '查询 DR 信息'},
            {id: 'PROPERTY', name: '给 DR 打Tag或者Property'},
            {id: 'FORCE', name: 'DR的Force操作'}
        ]
    };
    $scope.user = {};
    $scope.slbs = {};
    $scope.vses = {};
    $scope.groups = {};
    $scope.roles = {};

    $scope.optionPanelStatusBool = false;

    // Alt-Complete Area
    $scope.cacheRequestFn = function (str) {
        return {q: str, timestamp: new Date().getTime()};
    };
    $scope.remoteRolesUrl = function () {
        return G.baseUrl + "/api/auth/roles";
    };
    $scope.remoteSLBsUrl = function () {
        return G.baseUrl + "/api/meta/slbs";
    };
    $scope.remoteVSUrl = function () {
        return G.baseUrl + "/api/meta/vses";
    };
    $scope.remoteGroupsUrl = function () {
        return G.baseUrl + "/api/meta/groups";
    };
    $scope.remotePolicyUrl = function () {
        return G.baseUrl + "/api/meta/policies";
    };
    $scope.remoteDrUrl = function () {
        return G.baseUrl + "/api/meta/drs";
    };
    $scope.selectRoleId = function (t) {
        if (t) {
            var id = t.originalObject.id;
            $scope.query.role.id = id;
            $('.add-role-alert').html('');
        }
    };
    $scope.selectSlbId = function (t) {
        if (t) {
            var id = t.originalObject.id;
            $scope.query.slb.id = id;
            $('.add-slb-alert').html('');
        }
    };
    $scope.selectVSId = function (t) {
        if (t) {
            var id = t.originalObject.id;
            $scope.query.vs.id = id;
            $('.add-vs-alert').html('');
        }
    };
    $scope.selectGroupId = function (t) {
        if (t) {
            var id = t.originalObject.id;
            $scope.query.group.id = id;
            $('.add-group-alert').html('');
        }
    };
    $scope.selectPolicyId = function (t) {
        if (t) {
            var id = t.originalObject.id;
            $scope.query.policy.id = id;
            $('.add-policy-alert').html('');
        }
    };
    $scope.selectDrId = function (t) {
        if (t) {
            var id = t.originalObject.id;
            $scope.query.dr.id = id;
            $('.add-dr-alert').html('');
        }
    };

    // Show Area
    $scope.showAddRoleBt = function () {
        return A.canDo("Auth", "AUTH", '*') && $scope.optionPanelStatusBool;
    };
    $scope.showRemoveRoleBt = function () {
        return A.canDo("Auth", "AUTH", '*') && $scope.optionPanelStatusBool;
    };
    $scope.showAddSlbBt = function () {
        return A.canDo("Auth", "AUTH", '*') && $scope.optionPanelStatusBool;
    };
    $scope.showAddAuthBt = function () {
        return A.canDo("Auth", "AUTH", '*') && $scope.optionPanelStatusBool;
    };
    $scope.showRemoveSlbBt = function () {
        return A.canDo("Auth", "AUTH", '*') && $scope.optionPanelStatusBool;
    };
    $scope.showRemoveAuthBt = function () {
        return A.canDo("Auth", "AUTH", '*') && $scope.optionPanelStatusBool;
    };
    $scope.showAddVsBt = function () {
        return A.canDo("Auth", "AUTH", '*') && $scope.optionPanelStatusBool;
    };
    $scope.showRemoveVsBt = function () {
        return A.canDo("Auth", "AUTH", '*') && $scope.optionPanelStatusBool;
    };
    $scope.showAddGroupBt = function () {
        return A.canDo("Auth", "AUTH", '*') && $scope.optionPanelStatusBool;
    };
    $scope.showRemoveGroupBt = function () {
        return A.canDo("Auth", "AUTH", '*') && $scope.optionPanelStatusBool;
    };

    $scope.showRemovePolicyBt = function () {
        return A.canDo("Auth", "AUTH", '*') && $scope.optionPanelStatusBool;
    };
    $scope.showAddPolicyBt = function () {
        return A.canDo("Auth", "AUTH", '*') && $scope.optionPanelStatusBool;
    };

    $scope.showRemoveDrBt = function () {
        return A.canDo("Auth", "AUTH", '*') && $scope.optionPanelStatusBool;
    };
    $scope.showAddDrBt = function () {
        return A.canDo("Auth", "AUTH", '*') && $scope.optionPanelStatusBool;
    };

    // Operation Panel
    $scope.toggleOptionPanel = function () {
        H.setData({'openPanel': !$scope.optionPanelStatusBool});
    };
    $scope.getOptionPanelText = function () {
        if (!$scope.optionPanelStatusBool) return "打开操作面板";
        return "收起操作面板";
    };
    $scope.getOptionPanelCss = function () {
        if (!$scope.optionPanelStatusBool) {
            return "fa fa-arrows panel-close";
        } else {
            return "fa fa-arrows-alt panel-open";
        }
    };
    // Focus Area
    $scope.getFocusObject = function () {
        /*    if($scope.query==undefined) return undefined;
            var f = _.find($scope.information.extendedGroup.tags, function (item) {
                return item.trim().toLowerCase()=='user_'+$scope.query.user;
            });
            return f;*/
    };
    $scope.toggleFocus = function () {

    };
    $scope.getFocusCss = function () {
        var f = $scope.getFocusObject();
        if (f == undefined) return "fa fa-eye-slash status-unfocus";
        return "fa fa-eye status-focus";
    };
    $scope.getFocusText = function () {
        var f = $scope.getFocusObject();
        if (!f) return "关注";
        return "取消关注";
    };
    $scope.addFocus = function (tagName, type) {
        var request = {
            method: 'GET',
            url: G.baseUrl + "/api/tagging?type=group&tagName=" + tagName + "&targetId=" + $scope.information.extendedGroup.id
        };
        $http(request).success(
            function (res) {
                H.setData({"timeStamp": new Date().getTime()});
            });

    };
    $scope.removeFocus = function (tagName, type) {
        var request = {
            method: 'GET',
            url: G.baseUrl + "/api/untagging?type=group&tagName=" + tagName + "&targetId=" + $scope.information.extendedGroup.id
        };
        $http(request).success(
            function (res) {
                H.setData({"timeStamp": new Date().getTime()});
            });
    };
    // Role Management Area
    window.RoleManagmentEvent = {
        'click .deleteRoleOp': function (e, value, row) {
            $scope.popRemoveRoleDialog(row);
        },
        'click .changeRoleOp': function (e, value, row) {
            $scope.popEditRoleDialog(row);
        }
    };
    $scope.addNewRoleClick = function () {
        $scope.popEditRoleDialog();
        $('#roleIdSelector_value').val('');
    };
    $scope.batchRemoveRole = function () {
        $scope.popRemoveRoleDialog();
    };
    $scope.saveNewUserRole = function () {
        var request = {
            method: 'POST',
            url: G.baseUrl + '/api/auth/user/update'
        };
        if ($scope.query.role.id) {
            $('#newRoleDialog').modal('hide');
            var b = $scope.query.user;
            if (selectedEditRoleItems.length > 0) {
                var r = _.findIndex(b.roles, function (item) {
                    return item.id == selectedEditRoleItems[0].id;
                });
                b.roles[r] == {};
                b.roles[r] = {id: $scope.query.role.id};
                selectedEditRoleItems = [];
            }
            else {
                var roleId = $scope.query.role.id;
                b.roles.push(
                    {
                        id: roleId,
                        'role-name': $scope.roles[roleId]['role-name']
                    }
                );
            }

            request.data = b;
            $scope.processRequest(request, $('#operationResultDialog'), '更新User Roles', '更新成功！');
        }
        else {
            $('.add-role-alert').html('<p  style="color:red; margin-left: 200px">请选择要关联的Role</p>');
        }
    };
    var selectedRemoveRoleItems = [];
    var selectedEditRoleItems = [];
    $scope.popRemoveRoleDialog = function (row) {
        $('.to-be-removed-roles').html('');
        selectedRemoveRoleItems = [];
        var str = '';
        if (row) {
            selectedRemoveRoleItems.push(row);
        }
        else {
            selectedRemoveRoleItems = $('#user-role-table').bootstrapTable('getSelections');
        }
        $.each(selectedRemoveRoleItems, function (i, item) {
            str += '<tr><td>' + item.id + '</td>';
            str += '<td>' + item['role-name'] + '</td>';
            str += '<td>' + item['discription'] + '</td></tr>';
        });
        $('#confirmRemoveRolesDialog').modal('show').find('.to-be-removed-roles').append(str);
    };
    $scope.confirmRemoveRole = function () {
        var request = {
            method: 'POST',
            url: G.baseUrl + '/api/auth/user/update'
        };
        var b = $scope.query.user;
        var remaining = _.filter(b.roles, function (item) {
            return _.find(selectedRemoveRoleItems, function (item2) {
                return item.id == item2.id;
            }) == undefined;
        });
        b.roles = remaining;
        request.data = b;
        $scope.processRequest(request, $('#operationResultDialog'), '更新User Roles', '更新成功！');
    };
    $scope.popEditRoleDialog = function (row) {
        selectedEditSlbItems = [];
        $('#newRoleDialog').modal('show').find('.add-role-alert').html('');
        $('.add-slb-auth-alert').html('');
        if (row) {
            $('#roleIdSelector_value').val(row.id.toString());
            selectedEditRoleItems = [row];
        }
    };

    //Auth Management Area
    window.AuthOperationEvent = {
        'click .deleteAuthOp': function (e, value, row) {
            $scope.popRemoveAuthDialog(row);
        }
    };
    $scope.addNewAuthClick = function () {
        $scope.popEditAuthDialog();
    };
    $scope.popEditAuthDialog = function () {
        $('#newAuthDialog').modal('show');
    };
    $scope.saveNewAuthClick = function () {
        var request = {
            method: 'POST',
            url: G.baseUrl + '/api/auth/user/update'
        };
        $('#newAuthDialog').modal('hide');
        var b = $scope.query.user;
        var authExisted = _.find(b['data-resources'], function (v) {
            return v['resource-type'] && v['resource-type'].toLowerCase() == 'auth';
        });
        if (!authExisted) {
            b['data-resources'].push({
                    "operations": [{
                        "type": "AUTH"
                    }],
                    "data": "*",
                    "resource-type": "Auth"
                }
            );

            request.data = b;
            $scope.processRequest(request, $('#operationResultDialog'), '更新User Roles', '更新成功！');
        }
    };
    var selectedRemoveAuthItems = [];
    $scope.popRemoveAuthDialog = function (row) {
        $('.to-be-removed-auths').html('');
        selectedRemoveAuthItems = [];
        var str = '';
        if (row) {
            selectedRemoveAuthItems.push(row);
        }
        else {
            selectedRemoveAuthItems = $('#auth-table').bootstrapTable('getSelections');
        }
        $.each(selectedRemoveAuthItems, function (i, item) {
            str += '<tr><td>' + item.data + '</td>';
            str += '<td>' + $scope.query.user['chinese-name'] + '</td></tr>';
        });
        $('#confirmRemoveAuthsDialog').modal('show').find('.to-be-removed-auths').append(str);
    };
    $scope.confirmRemoveAuth = function () {
        var request = {
            method: 'POST',
            url: G.baseUrl + '/api/auth/user/update'
        };
        var b = $scope.query.user;
        var remaining = _.filter(b['data-resources'], function (item) {
            return _.find(selectedRemoveAuthItems, function (item2) {
                return item['resource-type'].toLowerCase() == 'auth';
            }) == undefined;
        });
        b['data-resources'] = remaining;
        request.data = b;
        $scope.processRequest(request, $('#operationResultDialog'), '更新 User', '更新成功！');
    };
    //SLB Management Area
    window.SlbOperationEvent = {
        'click .deleteSlbOp': function (e, value, row) {
            $scope.popRemoveSlbDialog(row);
        },
        'click .updateSlbOp': function (e, value, row) {
            $scope.popEditSlbDialog(row);
        }
    };
    window.SlbChangeRightEvent = {
        'click .slbReadOp': function (e, value, row) {
            $scope.toggleSlbRightClick(row.data, "READ", value);
        },
        'click .slbUpdateOp': function (e, value, row) {
            $scope.toggleSlbRightClick(row.data, "UPDATE", value);
        },
        'click .slbDeleteOp': function (e, value, row) {
            $scope.toggleSlbRightClick(row.data, "DELETE", value);
        },
        'click .slbPropertyOp': function (e, value, row) {
            $scope.toggleSlbRightClick(row.data, "PROPERTY", value);
        },
        'click .slbSyncOp': function (e, value, row) {
            $scope.toggleSlbRightClick(row.data, "SYNC", value);
        },
        'click .slbActivateOp': function (e, value, row) {
            $scope.toggleSlbRightClick(row.data, "ACTIVATE", value);
        },
        'click .slbDeactivateOp': function (e, value, row) {
            $scope.toggleSlbRightClick(row.data, "DEACTIVATE", value);
        },
        'click .slbAdminOp': function (e, value, row) {
            $scope.toggleSlbRightClick(row.data, "ADMIN_INFO", value);
        },
        'click .slbWafOp': function (e, value, row) {
            $scope.toggleSlbRightClick(row.data, "WAF", value);
        },
        'click .slbForceOp': function (e, value, row) {
            $scope.toggleSlbRightClick(row.data, "FORCE", value);
        }
    };
    $scope.toggleSlbRightClick = function (id, op, v) {
        var request = {
            method: 'POST',
            url: G.baseUrl + '/api/auth/user/update'
        };
        var c = v != undefined;
        var b = $scope.query.user;
        var r = _.findIndex(b['data-resources'], function (item) {
            return item.data == id && item['resource-type'].toLowerCase() == 'slb';
        });
        if (r != -1) {
            var o = b['data-resources'][r].operations;
            var i = _.findIndex(o, function (item) {
                return item.type.toLowerCase() == op.toLowerCase();
            });

            // Splice those empty typed element
            var j = _.findIndex(o, function (item) {
                return item.type == '';
            });
            if (j != -1) o.splice(j, 1);

            if (c) {
                // Turn off the right
                o.splice(i, 1);
            } else {
                // Turn on the right
                o.push({type: op});
            }

            // Save the changes
            request.data = b;
            var txt = c ? "成功收回 " + op + " 权限" : "成功赋予 " + op + " 权限";
            $scope.processRequest(request, $('#operationResultDialog'), '更新User', txt);
        }
    };
    $scope.addNewSlbClick = function () {
        $scope.popEditSlbDialog();
    };
    $scope.batchRemoveSlbClick = function () {
        $scope.popRemoveSlbDialog();
    };
    $scope.saveNewSlbClick = function () {
        var request = {
            method: 'POST',
            url: G.baseUrl + '/api/auth/user/update'
        };
        if ($scope.query.slb.id) {
            if ($scope.getSlbSelectedItems().length == 0) {
                $('.add-slb-auth-alert').html('(请至少选择一种权限!)');
                return;
            }
            $('#newSlbDialog').modal('hide');
            var b = $scope.query.user;
            if (selectedEditSlbItems.length > 0) {
                var r = _.findIndex(b['data-resources'], function (item) {
                    return item.data == selectedEditSlbItems[0].data && item['resource-type'].toLowerCase() == 'slb';
                });
                b['data-resources'][r] == {};
                b['data-resources'][r] = {
                    data: $scope.query.slb.id,
                    'resource-type': 'Slb',
                    operations: $scope.getSlbSelectedItems()
                };
                selectedEditSlbItems = [];
            }
            else {
                if (!b['data-resources']) {
                    b['data-resources'] = [];
                }

                b['data-resources'].push(
                    {
                        'resource-type': 'Slb',
                        data: $scope.query.slb.id,
                        operations: $scope.getSlbSelectedItems()
                    }
                );
            }

            request.data = b;
            $scope.processRequest(request, $('#operationResultDialog'), '更新User Roles', '更新成功！');
        }
        else {
            $('.add-slb-alert').html('<p  style="color:red; margin-left: 200px">请选择要关联的SLB</p>');
        }
    };
    var selectedRemoveSlbItems = [];
    var selectedEditSlbItems = [];
    $scope.popRemoveSlbDialog = function (row) {
        $('.to-be-removed-slbs').html('');
        selectedRemoveSlbItems = [];
        var str = '';
        if (row) {
            selectedRemoveSlbItems.push(row);
        }
        else {
            selectedRemoveSlbItems = $('#slb-table').bootstrapTable('getSelections');
        }
        $.each(selectedRemoveSlbItems, function (i, item) {
            str += '<tr><td>' + item.data + '</td>';
            var d = '';
            var t = _.find($scope.slbs, function (item3) {
                return item3.id == item.data;
            });
            if (t) d = t.name;
            else d = '-';
            str += '<td>' + d + '</td></tr>';
        });
        $('#confirmRemoveSlbsDialog').modal('show').find('.to-be-removed-slbs').append(str);
    };
    $scope.confirmRemoveSlb = function () {
        var request = {
            method: 'POST',
            url: G.baseUrl + '/api/auth/user/update'
        };
        var b = $scope.query.user;
        var remaining = _.filter(b['data-resources'], function (item) {
            return _.find(selectedRemoveSlbItems, function (item2) {
                return item.data == item2.data && item['resource-type'].toLowerCase() == 'slb';
            }) == undefined;
        });
        b['data-resources'] = remaining;
        request.data = b;
        $scope.processRequest(request, $('#operationResultDialog'), '更新 User', '更新成功！');
    };
    $scope.popEditSlbDialog = function (row) {
        $scope.query.slb.id = undefined;
        selectedEditSlbItems = [];
        $('.check-all-slbs-bt').prop('checked', false);

        $('#newSlbDialog').modal('show').find('.add-slb-alert').html('');
        $('.add-slb-auth-alert').html('');
        $("#role-selector-table").bootstrapTable("uncheckAll");
        $('#slbIdSelector_value').val('');

        if (row) {
            $('#slbIdSelector_value').val(row.data.toString());
            var selectedRowsArray = [];
            var item = row;
            $scope.query.slb.id = item.data;
            if (item.data == '*') {
                $('.check-all-slbs-bt').prop('checked', true);
            }
            if (item.R) {
                selectedRowsArray.push(item.R.type);
            }
            if (item.D) {
                selectedRowsArray.push(item.D.type);
            }
            if (item.A) {
                selectedRowsArray.push(item.A.type);
            }
            if (item.U) {
                selectedRowsArray.push(item.U.type);
            }
            if (item.E) {
                selectedRowsArray.push(item.E.type);
            }
            if (item.P) {
                selectedRowsArray.push(item.P.type);
            }
            if (item.S) {
                selectedRowsArray.push(item.S.type);
            }
            if (item.I) {
                selectedRowsArray.push(item.I.type);
            }
            if (item.W) {
                selectedRowsArray.push(item.W.type);
            }
            if (item.F) {
                selectedRowsArray.push(item.F.type);
            }
            $("#role-selector-table").bootstrapTable("checkBy", {field: "id", values: selectedRowsArray})
            selectedEditSlbItems = [row];
        }
    };
    $scope.getSlbSelectedItems = function () {
        var result = [];
        var slbRoles = $('#role-selector-table').bootstrapTable('getSelections');
        $.each(slbRoles, function (i, item) {
            result.push({type: item.id});
        });
        return result;
    };
    $scope.checkAllSlbs = function () {
        var c = $('.check-all-slbs-bt').is(':checked');
        if (c) {
            $scope.query.slb.id = '*';
            $('#slbIdSelector_value').val('*');
        }
    };

    // VS Management Area
    window.VSOperationEvent = {
        'click .deleteVSOp': function (e, value, row) {
            $scope.popRemoveVSDialog(row);
        },
        'click .updateVSOp': function (e, value, row) {
            $scope.popEditVSDialog(row);
        }
    };
    window.VsChangeRightEvent = {
        'click .vsReadOp': function (e, value, row) {
            $scope.toggleVSRightClick(row.data, "READ", value);
        },
        'click .vsUpdateOp': function (e, value, row) {
            $scope.toggleVSRightClick(row.data, "UPDATE", value);
        },
        'click .vsDeleteOp': function (e, value, row) {
            $scope.toggleVSRightClick(row.data, "DELETE", value);
        },
        'click .vsPropertyOp': function (e, value, row) {
            $scope.toggleVSRightClick(row.data, "PROPERTY", value);
        },
        'click .vsSyncOp': function (e, value, row) {
            $scope.toggleVSRightClick(row.data, "SYNC", value);
        },
        'click .vsActivateOp': function (e, value, row) {
            $scope.toggleVSRightClick(row.data, "ACTIVATE", value);
        },
        'click .vsDeactivateOp': function (e, value, row) {
            $scope.toggleVSRightClick(row.data, "DEACTIVATE", value);
        },
        'click .vsCertOp': function (e, value, row) {
            $scope.toggleVSRightClick(row.data, "CERT", value);
        },
        'click .vsForceOp': function (e, value, row) {
            $scope.toggleVSRightClick(row.data, "FORCE", value);
        }
    };
    $scope.toggleVSRightClick = function (id, op, v) {
        var request = {
            method: 'POST',
            url: G.baseUrl + '/api/auth/user/update'
        };
        var c = v != undefined;
        var b = $scope.query.user;
        var r = _.findIndex(b['data-resources'], function (item) {
            return item.data == id && item['resource-type'].toLowerCase() == 'vs';
        });
        if (r != -1) {
            var o = b['data-resources'][r].operations;
            var i = _.findIndex(o, function (item) {
                return item.type.toLowerCase() == op.toLowerCase();
            });

            // Splice those empty typed element
            var j = _.findIndex(o, function (item) {
                return item.type == '';
            });
            if (j != -1) o.splice(j, 1);

            if (c) {
                // Turn off the right
                o.splice(i, 1);
            } else {
                // Turn on the right
                o.push({type: op});
            }

            // Save the changes
            request.data = b;
            var txt = c ? "成功收回 " + op + " 权限" : "成功赋予 " + op + " 权限";
            $scope.processRequest(request, $('#operationResultDialog'), '更新User', txt);
        }
    };
    $scope.addNewVsClick = function () {
        $scope.popEditVSDialog();
    };
    $scope.batchRemoveVsClick = function () {
        $scope.popRemoveVSDialog();
    };
    $scope.saveNewVSClick = function () {
        var request = {
            method: 'POST',
            url: G.baseUrl + '/api/auth/user/update'
        };
        if ($scope.query.vs.id) {
            if ($scope.getVSSelectedItems().length == 0) {
                $('.add-vs-auth-alert').html('(请至少选择一种权限!)');
                return;
            }
            $('#newVSDialog').modal('hide');
            var b = $scope.query.user;
            if (selectedEditVsItems.length > 0) {
                var r = _.findIndex(b['data-resources'], function (item) {
                    return item.data == selectedEditVsItems[0].data && item['resource-type'].toLowerCase() == 'vs';
                });
                b['data-resources'][r] == {};
                b['data-resources'][r] = {
                    data: $scope.query.vs.id,
                    'resource-type': 'Vs',
                    operations: $scope.getVSSelectedItems()
                };
                selectedEditVsItems = [];
            }
            else {
                if (!b['data-resources']) {
                    b['data-resources'] = [];
                }

                b['data-resources'].push(
                    {
                        'resource-type': 'Vs',
                        data: $scope.query.vs.id,
                        operations: $scope.getVSSelectedItems()
                    }
                );
            }

            request.data = b;
            $scope.processRequest(request, $('#operationResultDialog'), '更新User', '更新成功！');
        }
        else {
            $('.add-vs-alert').html('<p  style="color:red; margin-left: 200px">请选择要关联的VS</p>');
        }
    };
    var selectedRemoveVsItems = [];
    var selectedEditVsItems = [];
    $scope.popRemoveVSDialog = function (row) {
        $('.to-be-removed-vses').html('');
        selectedRemoveVsItems = [];
        var str = '';
        if (row) {
            selectedRemoveVsItems.push(row);
        }
        else {
            selectedRemoveVsItems = $('#vs-table').bootstrapTable('getSelections');
        }
        $.each(selectedRemoveVsItems, function (i, item) {
            str += '<tr><td>' + item.data + '</td>';
            var d = '';
            var t = _.find($scope.vses, function (item3) {
                return item3.id == item.data;
            });
            if (t) d = t.name;
            d = '-';
            str += '<td>' + d + '</td></tr>';
        });
        $('#confirmRemoveVSDialog').modal('show').find('.to-be-removed-vses').append(str);
    };
    $scope.confirmRemoveVS = function () {
        var request = {
            method: 'POST',
            url: G.baseUrl + '/api/auth/user/update'
        };
        var b = $scope.query.user;
        var remaining = _.filter(b['data-resources'], function (item) {
            return _.find(selectedRemoveVsItems, function (item2) {
                return item.data == item2.data && item['resource-type'].toLowerCase() == 'vs';
            }) == undefined;
        });
        b['data-resources'] = remaining;
        request.data = b;
        $scope.processRequest(request, $('#operationResultDialog'), '更新 User', '更新成功！');
    };
    $scope.popEditVSDialog = function (row) {
        $scope.query.vs.id = undefined;
        selectedEditVsItems = [];
        $('.check-all-vses-bt').prop('checked', false);
        $('#newVSDialog').modal('show').find('.add-vs-alert').html('');
        $('.add-vs-auth-alert').html('');
        $("#vs-role-selector-table").bootstrapTable("uncheckAll");
        $('#vsIdSelector_value').val('');
        if (row) {
            $('#vsIdSelector_value').val(row.data.toString());
            var selectedRowsArray = [];
            var item = row;
            $scope.query.vs.id = item.data;
            if (item.data == '*') {
                $('.check-all-vses-bt').prop('checked', true);
            }
            if (item.R) {
                selectedRowsArray.push(item.R.type);
            }
            if (item.D) {
                selectedRowsArray.push(item.D.type);
            }
            if (item.A) {
                selectedRowsArray.push(item.A.type);
            }
            if (item.U) {
                selectedRowsArray.push(item.U.type);
            }
            if (item.E) {
                selectedRowsArray.push(item.E.type);
            }
            if (item.P) {
                selectedRowsArray.push(item.P.type);
            }
            if (item.S) {
                selectedRowsArray.push(item.S.type);
            }

            if (item.C) {
                selectedRowsArray.push(item.C.type);
            }
            if (item.F) {
                selectedRowsArray.push(item.F.type);
            }

            $("#vs-role-selector-table").bootstrapTable("checkBy", {field: "id", values: selectedRowsArray})
            selectedEditVsItems = [row];
        }
    };
    $scope.getVSSelectedItems = function () {
        var result = [];
        var slbRoles = $('#vs-role-selector-table').bootstrapTable('getSelections');
        $.each(slbRoles, function (i, item) {
            result.push({type: item.id});
        });
        return result;
    };
    $scope.checkAllVses = function () {
        var c = $('.check-all-vses-bt').is(':checked');
        if (c) {
            $scope.query.vs.id = '*';
            $('#vsIdSelector_value').val('*');
        }
    };

    // Group Management Area
    window.GroupOperationEvent = {
        'click .deletePolicyOp': function (e, value, row) {
            $scope.popRemovePolicyDialog(row);
        },
        'click .updatePolicyOp': function (e, value, row) {
            $scope.popEditPolicyDialog(row);
        },
        'click .deleteGroupOp': function (e, value, row) {
            $scope.popRemoveGroupDialog(row);
        },
        'click .updateGroupOp': function (e, value, row) {
            $scope.popEditGroupDialog(row);
        }
    };
    window.GroupChangeRightEvent = {
        'click .groupReadOp': function (e, value, row) {
            $scope.toggleGroupRightClick(row.data, "READ", value);
        },
        'click .groupUpdateOp': function (e, value, row) {
            $scope.toggleGroupRightClick(row.data, "UPDATE", value);
        },
        'click .groupDeleteOp': function (e, value, row) {
            $scope.toggleGroupRightClick(row.data, "DELETE", value);
        },
        'click .groupPropertyOp': function (e, value, row) {
            $scope.toggleGroupRightClick(row.data, "PROPERTY", value);
        },
        'click .groupSyncOp': function (e, value, row) {
            $scope.toggleGroupRightClick(row.data, "SYNC", value);
        },
        'click .groupActivateOp': function (e, value, row) {
            $scope.toggleGroupRightClick(row.data, "ACTIVATE", value);
        },
        'click .groupDeactivateOp': function (e, value, row) {
            $scope.toggleGroupRightClick(row.data, "DEACTIVATE", value);
        },
        'click .groupMemberOp': function (e, value, row) {
            $scope.toggleGroupRightClick(row.data, "OP_MEMBER", value);
        },
        'click .groupPullOp': function (e, value, row) {
            $scope.toggleGroupRightClick(row.data, "OP_PULL", value);
        },
        'click .groupHealthOp': function (e, value, row) {
            $scope.toggleGroupRightClick(row.data, "OP_HEALTH_CHECK", value);
        },
        'click .groupForceOp': function (e, value, row) {
            $scope.toggleGroupRightClick(row.data, "FORCE", value);
        }
    };
    $scope.toggleGroupRightClick = function (id, op, v) {
        var request = {
            method: 'POST',
            url: G.baseUrl + '/api/auth/user/update'
        };
        var c = v != undefined;
        var b = $scope.query.user;
        var r = _.findIndex(b['data-resources'], function (item) {
            return item.data == id && item['resource-type'].toLowerCase() == 'group';
        });
        if (r != -1) {
            var o = b['data-resources'][r].operations;
            var i = _.findIndex(o, function (item) {
                return item.type.toLowerCase() == op.toLowerCase();
            });

            // Splice those empty typed element
            var j = _.findIndex(o, function (item) {
                return item.type == '';
            });
            if (j != -1) o.splice(j, 1);

            if (c) {
                // Turn off the right
                o.splice(i, 1);
            } else {
                // Turn on the right
                o.push({type: op});
            }

            // Save the changes
            request.data = b;
            var txt = c ? "成功收回 " + op + " 权限" : "成功赋予 " + op + " 权限";
            $scope.processRequest(request, $('#operationResultDialog'), '更新User', txt);
        }
    };
    $scope.addNewGroupClick = function () {
        $scope.popEditGroupDialog();
    };
    $scope.batchRemoveGroupClick = function () {
        $scope.popRemoveGroupDialog();
    };
    $scope.saveNewGroupClick = function () {
        var request = {
            method: 'POST',
            url: G.baseUrl + '/api/auth/user/update'
        };
        if ($scope.query.group.id) {
            if ($scope.getGroupSelectedItems().length == 0) {
                $('.add-group-auth-alert').html('(请至少选择一种权限!)');
                return;
            }
            $('#newGroupDialog').modal('hide');
            var b = $scope.query.user;
            if (selectedEditGroupItems.length > 0) {
                var r = _.findIndex(b['data-resources'], function (item) {
                    return item.data == selectedEditGroupItems[0].data && item['resource-type'].toLowerCase() == 'group';
                });
                b['data-resources'][r] == {};
                b['data-resources'][r] = {
                    data: $scope.query.group.id,
                    'resource-type': 'Group',
                    operations: $scope.getGroupSelectedItems()
                };
                selectedEditGroupItems = [];
            }
            else {
                if (!b['data-resources']) {
                    b['data-resources'] = [];
                }

                b['data-resources'].push(
                    {
                        'resource-type': 'Group',
                        data: $scope.query.group.id,
                        operations: $scope.getGroupSelectedItems()
                    }
                );
            }

            request.data = b;
            $scope.processRequest(request, $('#operationResultDialog'), '更新User', '更新成功！');
        }
        else {
            $('.add-group-alert').html('<p  style="color:red; margin-left: 200px">请选择要关联的VS</p>');
        }
    };
    var selectedRemoveGroupItems = [];
    var selectedEditGroupItems = [];
    $scope.popRemoveGroupDialog = function (row) {
        $('.to-be-removed-groups').html('');
        selectedRemoveGroupItems = [];
        var str = '';
        if (row) {
            selectedRemoveGroupItems.push(row);
        }
        else {
            selectedRemoveGroupItems = $('#group-table').bootstrapTable('getSelections');
        }
        $.each(selectedRemoveGroupItems, function (i, item) {
            str += '<tr><td>' + item.data + '</td>';
            var d = '';
            var t = _.find($scope.groups, function (item3) {
                return item3.id == item.data;
            });
            if (t) d = t.name;
            else d = '-';
            str += '<td>' + d + '</td></tr>';
        });
        $('#confirmRemoveGroupDialog').modal('show').find('.to-be-removed-groups').append(str);
    };
    $scope.confirmRemoveGroup = function () {
        var request = {
            method: 'POST',
            url: G.baseUrl + '/api/auth/user/update'
        };
        var b = $scope.query.user;
        var remaining = _.filter(b['data-resources'], function (item) {
            return _.find(selectedRemoveGroupItems, function (item2) {
                return item.data == item2.data && item['resource-type'].toLowerCase() == 'group';
            }) == undefined;
        });
        b['data-resources'] = remaining;
        request.data = b;
        $scope.processRequest(request, $('#operationResultDialog'), '更新 User', '更新成功！');
    };
    $scope.popEditGroupDialog = function (row) {
        $scope.query.group.id = undefined;
        selectedEditGroupItems = [];
        $('.check-all-groups-bt').prop('checked', false);
        $('#newGroupDialog').modal('show').find('.add-group-alert').html('');
        $('.add-group-auth-alert').html('');
        $("#group-role-selector-table").bootstrapTable("uncheckAll");
        $('#groupIdSelector_value').val('');
        if (row) {
            $('#groupIdSelector_value').val(row.data.toString());
            var selectedRowsArray = [];
            var item = row;
            $scope.query.group.id = item.data;
            if (item.data == '*') {
                $('.check-all-groups-bt').prop('checked', true);
            }
            if (item.R) {
                selectedRowsArray.push(item.R.type);
            }
            if (item.D) {
                selectedRowsArray.push(item.D.type);
            }
            if (item.A) {
                selectedRowsArray.push(item.A.type);
            }
            if (item.U) {
                selectedRowsArray.push(item.U.type);
            }
            if (item.E) {
                selectedRowsArray.push(item.E.type);
            }
            if (item.P) {
                selectedRowsArray.push(item.P.type);
            }
            if (item.S) {
                selectedRowsArray.push(item.S.type);
            }
            if (item.M) {
                selectedRowsArray.push(item.M.type);
            }
            if (item.L) {
                selectedRowsArray.push(item.L.type);
            }
            if (item.H) {
                selectedRowsArray.push(item.H.type);
            }
            if (item.F) {
                selectedRowsArray.push(item.F.type);
            }
            $("#group-role-selector-table").bootstrapTable("checkBy", {field: "id", values: selectedRowsArray})
            selectedEditGroupItems = [row];
        }
    };
    $scope.getGroupSelectedItems = function () {
        var result = [];
        var slbRoles = $('#group-role-selector-table').bootstrapTable('getSelections');
        $.each(slbRoles, function (i, item) {
            result.push({type: item.id});
        });
        return result;
    };
    $scope.checkAllGroups = function () {
        var c = $('.check-all-groups-bt').is(':checked');
        if (c) {
            $scope.query.group.id = '*';
            $('#groupIdSelector_value').val('*');
        }
    };

    // Policy Management Area
    window.PolicyOperationEvent = {
        'click .deletePolicyOp': function (e, value, row) {
            $scope.popRemovePolicyDialog(row);
        },
        'click .updatePolicyOp': function (e, value, row) {
            $scope.popEditPolicyDialog(row);
        }
    };
    window.PolicyChangeRightEvent = {
        'click .policyReadOp': function (e, value, row) {
            $scope.togglePolicyRightClick(row.data, "READ", value);
        },
        'click .policyUpdateOp': function (e, value, row) {
            $scope.togglePolicyRightClick(row.data, "UPDATE", value);
        },
        'click .policyDeleteOp': function (e, value, row) {
            $scope.togglePolicyRightClick(row.data, "DELETE", value);
        },
        'click .policyPropertyOp': function (e, value, row) {
            $scope.togglePolicyRightClick(row.data, "PROPERTY", value);
        },
        'click .policyActivateOp': function (e, value, row) {
            $scope.togglePolicyRightClick(row.data, "ACTIVATE", value);
        },
        'click .policyDeactivateOp': function (e, value, row) {
            $scope.togglePolicyRightClick(row.data, "DEACTIVATE", value);
        },
        'click .policyForceOp': function (e, value, row) {
            $scope.togglePolicyRightClick(row.data, "FORCE", value);
        }
    };
    $scope.togglePolicyRightClick = function (id, op, v) {
        var request = {
            method: 'POST',
            url: G.baseUrl + '/api/auth/user/update'
        };
        var c = v != undefined;
        var b = $scope.query.user;
        var r = _.findIndex(b['data-resources'], function (item) {
            return item.data == id && item['resource-type'].toLowerCase() == 'policy';
        });
        if (r != -1) {
            var o = b['data-resources'][r].operations;
            var i = _.findIndex(o, function (item) {
                return item.type.toLowerCase() == op.toLowerCase();
            });

            // Splice those empty typed element
            var j = _.findIndex(o, function (item) {
                return item.type == '';
            });
            if (j != -1) o.splice(j, 1);

            if (c) {
                // Turn off the right
                o.splice(i, 1);
            } else {
                // Turn on the right
                o.push({type: op});
            }

            // Save the changes
            request.data = b;
            var txt = c ? "成功收回 " + op + " 权限" : "成功赋予 " + op + " 权限";
            $scope.processRequest(request, $('#operationResultDialog'), '更新User', txt);
        }
    };
    $scope.addNewPolicyClick = function () {
        $scope.popEditPolicyDialog();
    };
    $scope.batchRemovePolicyClick = function () {
        $scope.popRemovePolicyDialog();
    };
    $scope.saveNewPolicyClick = function () {
        var request = {
            method: 'POST',
            url: G.baseUrl + '/api/auth/user/update'
        };
        if ($scope.query.policy.id) {
            if ($scope.getPolicySelectedItems().length == 0) {
                $('.add-policy-auth-alert').html('(请至少选择一种权限!)');
                return;
            }
            $('#newPolicyDialog').modal('hide');
            var b = $scope.query.user;
            if (selectedEditPolicyItems.length > 0) {
                var r = _.findIndex(b['data-resources'], function (item) {
                    return item.data == selectedEditPolicyItems[0].data && item['resource-type'].toLowerCase() == 'policy';
                });
                b['data-resources'][r] == {};
                b['data-resources'][r] = {
                    data: $scope.query.policy.id,
                    'resource-type': 'Policy',
                    operations: $scope.getPolicySelectedItems()
                };
                selectedEditPolicyItems = [];
            }
            else {
                if (!b['data-resources']) {
                    b['data-resources'] = [];
                }

                b['data-resources'].push(
                    {
                        'resource-type': 'Policy',
                        data: $scope.query.policy.id,
                        operations: $scope.getPolicySelectedItems()
                    }
                );
            }

            request.data = b;
            $scope.processRequest(request, $('#operationResultDialog'), '更新User', '更新成功！');
        }
        else {
            $('.add-policy-alert').html('<p  style="color:red; margin-left: 200px">请选择要关联的 Traffic Policy</p>');
        }
    };
    var selectedRemovePolicyItems = [];
    var selectedEditPolicyItems = [];
    $scope.popRemovePolicyDialog = function (row) {
        $('.to-be-removed-policy').html('');
        selectedRemovePolicyItems = [];
        var str = '';
        if (row) {
            selectedRemovePolicyItems.push(row);
        }
        else {
            selectedRemovePolicyItems = $('#policy-table').bootstrapTable('getSelections');
        }
        $.each(selectedRemovePolicyItems, function (i, item) {
            str += '<tr><td>' + item.data + '</td>';
            var d = '';
            var t = _.find($scope.policies, function (item3) {
                return item3.id == item.data;
            });
            if (t) d = t.name;
            else d = '-';
            str += '<td>' + d + '</td></tr>';
        });
        $('#confirmRemovePolicyDialog').modal('show').find('.to-be-removed-policy').append(str);
    };
    $scope.confirmRemovePolicy = function () {
        var request = {
            method: 'POST',
            url: G.baseUrl + '/api/auth/user/update'
        };
        var b = $scope.query.user;
        var remaining = _.filter(b['data-resources'], function (item) {
            return _.find(selectedRemovePolicyItems, function (item2) {
                return item.data == item2.data && item['resource-type'].toLowerCase() == 'policy';
            }) == undefined;
        });

        b['data-resources'] = remaining;
        request.data = b;
        $scope.processRequest(request, $('#operationResultDialog'), '更新 User', '更新成功！');
    };
    $scope.popEditPolicyDialog = function (row) {
        $scope.query.policy.id = undefined;
        selectedEditGroupItems = [];
        $('.check-all-policy-bt').prop('checked', false);
        $('#newPolicyDialog').modal('show').find('.add-policy-alert').html('');
        $('.add-policy-auth-alert').html('');
        $("#policy-role-selector-table").bootstrapTable("uncheckAll");
        $('#policyIdSelector_value').val('');
        if (row) {
            $('#policyIdSelector_value').val(row.data.toString());
            var selectedRowsArray = [];
            var item = row;
            $scope.query.policy.id = item.data;
            if (item.data == '*') {
                $('.check-all-policy-bt').prop('checked', true);
            }
            if (item.R) {
                selectedRowsArray.push(item.R.type);
            }
            if (item.D) {
                selectedRowsArray.push(item.D.type);
            }
            if (item.A) {
                selectedRowsArray.push(item.A.type);
            }
            if (item.U) {
                selectedRowsArray.push(item.U.type);
            }
            if (item.E) {
                selectedRowsArray.push(item.E.type);
            }
            if (item.P) {
                selectedRowsArray.push(item.P.type);
            }
            if (item.F) {
                selectedRowsArray.push(item.F.type);
            }


            $("#policy-role-selector-table").bootstrapTable("checkBy", {field: "id", values: selectedRowsArray})
            selectedEditPolicyItems = [row];
        }
    };
    $scope.getPolicySelectedItems = function () {
        var result = [];
        var slbRoles = $('#policy-role-selector-table').bootstrapTable('getSelections');
        $.each(slbRoles, function (i, item) {
            result.push({type: item.id});
        });
        return result;
    };
    $scope.checkAllPolicy = function () {
        var c = $('.check-all-policy-bt').is(':checked');
        if (c) {
            $scope.query.policy.id = '*';
            $('#policyIdSelector_value').val('*');
        }
    };

    // Policy Management Area
    window.DrOperationEvent = {
        'click .deleteDrOp': function (e, value, row) {
            $scope.popRemoveDrDialog(row);
        },
        'click .updateDrOp': function (e, value, row) {
            $scope.popEditDrDialog(row);
        }
    };
    window.DrChangeRightEvent = {
        'click .drReadOp': function (e, value, row) {
            $scope.toggleDrRightClick(row.data, "READ", value);
        },
        'click .drUpdateOp': function (e, value, row) {
            $scope.toggleDrRightClick(row.data, "UPDATE", value);
        },
        'click .drDeleteOp': function (e, value, row) {
            $scope.toggleDrRightClick(row.data, "DELETE", value);
        },
        'click .drPropertyOp': function (e, value, row) {
            $scope.toggleDrRightClick(row.data, "PROPERTY", value);
        },
        'click .drActivateOp': function (e, value, row) {
            $scope.toggleDrRightClick(row.data, "ACTIVATE", value);
        },
        'click .drDeactivateOp': function (e, value, row) {
            $scope.toggleDrRightClick(row.data, "DEACTIVATE", value);
        },
        'click .drForceOp': function (e, value, row) {
            $scope.toggleDrRightClick(row.data, "FORCE", value);
        }
    };
    $scope.toggleDrRightClick = function (id, op, v) {
        var request = {
            method: 'POST',
            url: G.baseUrl + '/api/auth/user/update'
        };
        var c = v != undefined;
        var b = $scope.query.user;
        var r = _.findIndex(b['data-resources'], function (item) {
            return item.data == id && item['resource-type'].toLowerCase() == 'dr';
        });
        if (r != -1) {
            var o = b['data-resources'][r].operations;
            var i = _.findIndex(o, function (item) {
                return item.type.toLowerCase() == op.toLowerCase();
            });

            // Splice those empty typed element
            var j = _.findIndex(o, function (item) {
                return item.type == '';
            });
            if (j != -1) o.splice(j, 1);

            if (c) {
                // Turn off the right
                o.splice(i, 1);
            } else {
                // Turn on the right
                o.push({type: op});
            }

            // Save the changes
            request.data = b;
            var txt = c ? "成功收回 " + op + " 权限" : "成功赋予 " + op + " 权限";
            $scope.processRequest(request, $('#operationResultDialog'), '更新User', txt);
        }
    };
    $scope.addNewDrClick = function () {
        $scope.popEditDrDialog();
    };
    $scope.batchRemoveDrClick = function () {
        $scope.popRemoveDrDialog();
    };
    $scope.saveNewDrClick = function () {
        var request = {
            method: 'POST',
            url: G.baseUrl + '/api/auth/user/update'
        };
        if ($scope.query.dr.id) {
            if ($scope.getDrSelectedItems().length == 0) {
                $('.add-dr-auth-alert').html('(请至少选择一种权限!)');
                return;
            }
            $('#newDrDialog').modal('hide');
            var b = $scope.query.user;
            if (selectedEditPolicyItems.length > 0) {
                var r = _.findIndex(b['data-resources'], function (item) {
                    return item.data == selectedEditPolicyItems[0].data && item['resource-type'].toLowerCase() == 'dr';
                });
                b['data-resources'][r] == {};
                b['data-resources'][r] = {
                    data: $scope.query.dr.id,
                    'resource-type': 'dr',
                    operations: $scope.getDrSelectedItems()
                };
                selectedEditDrItems = [];
            }
            else {
                if (!b['data-resources']) {
                    b['data-resources'] = [];
                }

                b['data-resources'].push(
                    {
                        'resource-type': 'Dr',
                        data: $scope.query.dr.id,
                        operations: $scope.getDrSelectedItems()
                    }
                );
            }

            request.data = b;
            $scope.processRequest(request, $('#operationResultDialog'), '更新User', '更新成功！');
        }
        else {
            $('.add-dr-alert').html('<p  style="color:red; margin-left: 200px">请选择要关联的 DR</p>');
        }
    };
    var selectedRemoveDrItems = [];
    var selectedEditDrItems = [];
    $scope.popRemoveDrDialog = function (row) {
        $('.to-be-removed-dr').html('');
        selectedRemoveDrItems = [];
        var str = '';
        if (row) {
            selectedRemoveDrItems.push(row);
        }
        else {
            selectedRemoveDrItems = $('#dr-table').bootstrapTable('getSelections');
        }
        $.each(selectedRemoveDrItems, function (i, item) {
            str += '<tr><td>' + item.data + '</td>';
            var d = '';
            var t = _.find($scope.user.drs, function (item3) {
                return item3.data == item.data;
            });
            if (t) d = t.name;
            else d = '-';
            str += '<td>' + d + '</td></tr>';
        });
        $('#confirmRemoveDrDialog').modal('show').find('.to-be-removed-dr').append(str);
    };
    $scope.confirmRemoveDr = function () {
        var request = {
            method: 'POST',
            url: G.baseUrl + '/api/auth/user/update'
        };
        var b = $scope.query.user;
        var remaining = _.filter(b['data-resources'], function (item) {
            return _.find(selectedRemoveDrItems, function (item2) {
                return item.data == item2.data && item['resource-type'].toLowerCase() == 'dr';
            }) == undefined;
        });

        b['data-resources'] = remaining;
        request.data = b;
        $scope.processRequest(request, $('#operationResultDialog'), '更新 User', '更新成功！');
    };
    $scope.popEditDrDialog = function (row) {
        $scope.query.dr.id = undefined;
        selectedEditDrItems = [];
        $('.check-all-dr-bt').prop('checked', false);
        $('#newDrDialog').modal('show').find('.add-dr-alert').html('');
        $('.add-dr-auth-alert').html('');
        $("#dr-role-selector-table").bootstrapTable("uncheckAll");
        $('#drIdSelector_value').val('');
        if (row) {
            $('#drIdSelector_value').val(row.data.toString());
            var selectedRowsArray = [];
            var item = row;
            $scope.query.dr.id = item.data;
            if (item.data == '*') {
                $('.check-all-dr-bt').prop('checked', true);
            }
            if (item.R) {
                selectedRowsArray.push(item.R.type);
            }
            if (item.D) {
                selectedRowsArray.push(item.D.type);
            }
            if (item.A) {
                selectedRowsArray.push(item.A.type);
            }
            if (item.U) {
                selectedRowsArray.push(item.U.type);
            }
            if (item.E) {
                selectedRowsArray.push(item.E.type);
            }
            if (item.P) {
                selectedRowsArray.push(item.P.type);
            }
            if (item.F) {
                selectedRowsArray.push(item.F.type);
            }


            $("#dr-role-selector-table").bootstrapTable("checkBy", {field: "id", values: selectedRowsArray})
            selectedEditDrItems = [row];
        }
    };
    $scope.getDrSelectedItems = function () {
        var result = [];
        var slbRoles = $('#dr-role-selector-table').bootstrapTable('getSelections');
        $.each(slbRoles, function (i, item) {
            result.push({type: item.id});
        });
        return result;
    };
    $scope.checkAllDr = function () {
        var c = $('.check-all-dr-bt').is(':checked');
        if (c) {
            $scope.query.dr.id = '*';
            $('#drIdSelector_value').val('*');
        }
    };

    // Progress dialog Area
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
            dialog.find('.closeProgressWindowBt').click();
        }, 1000);
    }

    $('.closeProgressWindowBt').click(
        function (e) {
            var hashData = {};
            hashData.timeStamp = new Date().getTime();
            H.setData(hashData);
        }
    );

    // Loading Area
    $scope.initTable = function () {
        $('#policy-table').bootstrapTable({
            search: true,
            showRefresh: true,
            showColumns: true,
            toolbar: "#user-policy-toolbar",
            columns: [[
                {
                    checkbox: 'true',
                    align: 'center',
                    valign: 'middle'
                },
                {
                    field: 'data',
                    title: 'ID',
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    formatter: function (value, row, index, field) {
                        return '<a target="_blank" href="/portal/policy#?env=' + G.env + '&policyId=' + value + '">' + value + '</a>';
                    }
                },
                {
                    field: 'name',
                    title: 'Name',
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    formatter: function (value, row, index, field) {
                        if (row.data != '*') {
                            value = value.substring(0, value.length / 2) + '...';
                            return '<a target="_blank" href="/portal/policy#?env=' + G.env + '&policyId=' + row.data + '">' + value + '</a>';
                        }
                        else {
                            return '-';
                        }
                    }
                },
                {
                    field: 'R',
                    title: 'Read',
                    events: PolicyChangeRightEvent,
                    align: 'center',
                    valign: 'middle',
                    formatter: function (value, row, index, field) {
                        return $scope.formatter('policyReadOp', value);
                    }
                },
                {
                    field: 'U',
                    title: 'Update',
                    events: PolicyChangeRightEvent,
                    align: 'center',
                    valign: 'middle',
                    formatter: function (value, row, index, field) {
                        return $scope.formatter('policyUpdateOp', value);
                    }
                },
                {
                    field: 'E',
                    title: 'Delete',
                    events: PolicyChangeRightEvent,
                    align: 'center',
                    valign: 'middle',
                    formatter: function (value, row, index, field) {
                        return $scope.formatter('policyDeleteOp', value);
                    }
                },

                {
                    field: 'P',
                    title: 'PROPERTY',
                    events: PolicyChangeRightEvent,
                    align: 'center',
                    valign: 'middle',
                    formatter: function (value, row, index, field) {
                        return $scope.formatter('policyPropertyOp', value);
                    }
                },

                {
                    field: 'A',
                    title: 'Activate',
                    events: PolicyChangeRightEvent,
                    align: 'center',
                    valign: 'middle',
                    formatter: function (value, row, index, field) {
                        return $scope.formatter('policyActivateOp', value);
                    }
                },
                {
                    field: 'D',
                    title: 'Deactivate',
                    events: PolicyChangeRightEvent,
                    align: 'center',
                    valign: 'middle',
                    formatter: function (value, row, index, field) {
                        return $scope.formatter('policyDeactivateOp', value);
                    }
                },
                {
                    field: 'F',
                    title: 'Force',
                    events: PolicyChangeRightEvent,
                    align: 'center',
                    valign: 'middle',
                    formatter: function (value, row, index, field) {
                        return $scope.formatter('policyForceOp', value);
                    }
                },

                {
                    field: 'operate',
                    width: '120px',
                    title: 'Operation',
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    events: PolicyOperationEvent,
                    formatter: function () {
                        var p1 = "";
                        var str = '<button data-toggle="tooltip" type="button" class="btn btn-info deletePolicyOp  ' + p1 + '" title="删除" aria-label="Left Align"><span class="fa fa-minus"></span></button>';
                        str += '  <button data-toggle="tooltip" title="修改" type="button" class="btn btn-info updatePolicyOp  ' + p1 + '" aria-label="Left Align">'
                            + '<span class="fa fa-edit"></span>' +
                            '</button>';
                        return str;
                    }
                }

            ], []],
            data: $scope.user.policies,
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
                return "<img class='loading-img' src='/static/img/loading_100.gif' /> 正在加载 Traffic Policy";
            },
            formatNoMatches: function () {
                return '<i class="fa status-alert fa-exclamation-triangle" aria-hidden="true"></i> 没有找到匹配的 Traffic Policy';
            }
        });
        $('#dr-table').bootstrapTable({
            search: true,
            showRefresh: true,
            showColumns: true,
            toolbar: "#user-dr-toolbar",
            columns: [[
                {
                    checkbox: 'true',
                    align: 'center',
                    valign: 'middle'
                },
                {
                    field: 'data',
                    title: 'ID',
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    formatter: function (value, row, index, field) {
                        return '<a target="_blank" href="/portal/dr#?env=' + G.env + '&drId=' + value + '">' + value + '</a>';
                    }
                },
                {
                    field: 'name',
                    title: 'Name',
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    formatter: function (value, row, index, field) {
                        if (row.data != '*') {
                            return '<a target="_blank" href="/portal/dr#?env=' + G.env + '&drId=' + row.data + '">' + value + '</a>';
                        }
                        else {
                            return '-';
                        }
                    }
                },
                {
                    field: 'R',
                    title: 'Read',
                    events: DrChangeRightEvent,
                    align: 'center',
                    valign: 'middle',
                    formatter: function (value, row, index, field) {
                        return $scope.formatter('drReadOp', value);
                    }
                },
                {
                    field: 'U',
                    title: 'Update',
                    events: DrChangeRightEvent,
                    align: 'center',
                    valign: 'middle',
                    formatter: function (value, row, index, field) {
                        return $scope.formatter('drUpdateOp', value);
                    }
                },
                {
                    field: 'E',
                    title: 'Delete',
                    events: DrChangeRightEvent,
                    align: 'center',
                    valign: 'middle',
                    formatter: function (value, row, index, field) {
                        return $scope.formatter('drDeleteOp', value);
                    }
                },

                {
                    field: 'P',
                    title: 'PROPERTY',
                    events: DrChangeRightEvent,
                    align: 'center',
                    valign: 'middle',
                    formatter: function (value, row, index, field) {
                        return $scope.formatter('drPropertyOp', value);
                    }
                },

                {
                    field: 'A',
                    title: 'Activate',
                    events: DrChangeRightEvent,
                    align: 'center',
                    valign: 'middle',
                    formatter: function (value, row, index, field) {
                        return $scope.formatter('drActivateOp', value);
                    }
                },
                {
                    field: 'D',
                    title: 'Deactivate',
                    events: DrChangeRightEvent,
                    align: 'center',
                    valign: 'middle',
                    formatter: function (value, row, index, field) {
                        return $scope.formatter('drDeactivateOp', value);
                    }
                },
                {
                    field: 'F',
                    title: 'Force',
                    events: DrChangeRightEvent,
                    align: 'center',
                    valign: 'middle',
                    formatter: function (value, row, index, field) {
                        return $scope.formatter('drForceOp', value);
                    }
                },

                {
                    field: 'operate',
                    width: '120px',
                    title: 'Operation',
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    events: DrOperationEvent,
                    formatter: function () {
                        var p1 = "";
                        var str = '<button data-toggle="tooltip" type="button" class="btn btn-info deleteDrOp  ' + p1 + '" title="删除" aria-label="Left Align"><span class="fa fa-minus"></span></button>';
                        str += '  <button data-toggle="tooltip" title="修改" type="button" class="btn btn-info updateDrOp  ' + p1 + '" aria-label="Left Align">'
                            + '<span class="fa fa-edit"></span>' +
                            '</button>';
                        return str;
                    }
                }

            ], []],
            data: $scope.user.drs,
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
                return "<img class='loading-img' src='/static/img/loading_100.gif' /> 正在加载 DR";
            },
            formatNoMatches: function () {
                return '<i class="fa status-alert fa-exclamation-triangle" aria-hidden="true"></i> 没有找到匹配的 Dr';
            }
        });

        $('#group-table').bootstrapTable({
            search: true,
            showRefresh: false,
            showColumns: true,
            toolbar: "#user-group-toolbar",
            columns: [[
                {
                    checkbox: 'true',
                    align: 'center',
                    valign: 'middle'
                },
                {
                    field: 'data',
                    title: 'ID',
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    formatter: function (value, row, index, field) {
                        return '<a target="_blank" href="/portal/group#?env=' + G.env + '&groupId=' + value + '">' + value + '</a>';
                    }
                },
                {
                    field: 'name',
                    title: 'Name',
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    formatter: function (value, row, index, field) {
                        if (row.data != '*') {
                            value = value.substring(0, value.length / 2) + '...';
                            return '<a target="_blank" href="/portal/group#?env=' + G.env + '&groupId=' + row.data + '">' + value + '</a>';
                        }
                        else {
                            return '-';
                        }
                    }
                },
                {
                    field: 'R',
                    title: 'Read',
                    events: GroupChangeRightEvent,
                    align: 'center',
                    valign: 'middle',
                    formatter: function (value, row, index, field) {
                        return $scope.formatter('groupReadOp', value);
                    }
                },
                {
                    field: 'U',
                    title: 'Update',
                    events: GroupChangeRightEvent,
                    align: 'center',
                    valign: 'middle',
                    formatter: function (value, row, index, field) {
                        return $scope.formatter('groupUpdateOp', value);
                    }
                },
                {
                    field: 'E',
                    title: 'Delete',
                    events: GroupChangeRightEvent,
                    align: 'center',
                    valign: 'middle',
                    formatter: function (value, row, index, field) {
                        return $scope.formatter('groupDeleteOp', value);
                    }
                },

                {
                    field: 'P',
                    title: 'PROPERTY',
                    events: GroupChangeRightEvent,
                    align: 'center',
                    valign: 'middle',
                    formatter: function (value, row, index, field) {
                        return $scope.formatter('groupPropertyOp', value);
                    }
                },
                {
                    field: 'S',
                    title: 'SYNC',
                    events: GroupChangeRightEvent,
                    align: 'center',
                    valign: 'middle',
                    formatter: function (value, row, index, field) {
                        return $scope.formatter('groupSyncOp', value);
                    }
                },

                {
                    field: 'A',
                    title: 'Activate',
                    events: GroupChangeRightEvent,
                    align: 'center',
                    valign: 'middle',
                    formatter: function (value, row, index, field) {
                        return $scope.formatter('groupActivateOp', value);
                    }
                },
                {
                    field: 'D',
                    title: 'Deactivate',
                    events: GroupChangeRightEvent,
                    align: 'center',
                    valign: 'middle',
                    formatter: function (value, row, index, field) {
                        return $scope.formatter('groupDeactivateOp', value);
                    }
                },

                {
                    field: 'M',
                    title: 'Member',
                    align: 'center',
                    events: GroupChangeRightEvent,
                    valign: 'middle',
                    formatter: function (value, row, index, field) {
                        return $scope.formatter('groupMemberOp', value);
                    }
                },
                {
                    field: 'L',
                    title: 'Pull',
                    align: 'center',
                    valign: 'middle',
                    events: GroupChangeRightEvent,
                    formatter: function (value, row, index, field) {
                        return $scope.formatter('groupPullOp', value);
                    }
                },
                {
                    field: 'H',
                    title: 'Raise',
                    align: 'center',
                    events: GroupChangeRightEvent,
                    valign: 'middle',
                    formatter: function (value, row, index, field) {
                        return $scope.formatter('groupHealthOp', value);
                    }
                },
                {
                    field: 'F',
                    title: 'Force',
                    align: 'center',
                    events: GroupChangeRightEvent,
                    valign: 'middle',
                    formatter: function (value, row, index, field) {
                        return $scope.formatter('groupForceOp', value);
                    }
                },
                {
                    field: 'operate',
                    width: '120px',
                    title: 'Operation',
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    events: GroupOperationEvent,
                    formatter: function () {
                        var p1 = "";
                        var str = '<button data-toggle="tooltip" type="button" class="btn btn-info deleteGroupOp  ' + p1 + '" title="删除" aria-label="Left Align"><span class="fa fa-minus"></span></button>';
                        str += '  <button data-toggle="tooltip" title="修改" type="button" class="btn btn-info updateGroupOp  ' + p1 + '" aria-label="Left Align">'
                            + '<span class="fa fa-edit"></span>' +
                            '</button>';
                        return str;
                    }
                }

            ], []],
            data: $scope.user.groups,
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
                return "<img class='loading-img' src='/static/img/loading_100.gif' /> 正在加载 Groups";
            },
            formatNoMatches: function () {
                return '<i class="fa status-alert fa-exclamation-triangle" aria-hidden="true"></i> 没有找到匹配的 Groups';
            }
        });
        $('#vs-table').bootstrapTable({
            toolbar: "#user-vs-toolbar",
            search: true,
            showRefresh: false,
            showColumns: true,
            columns: [[
                {
                    checkbox: 'true',
                    align: 'center',
                    valign: 'middle'
                },
                {
                    field: 'data',
                    title: 'ID',
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    formatter: function (value, row, index, field) {
                        return '<a target="_blank" href="/portal/vs#?env=' + G.env + '&vsId=' + value + '">' + value + '</a>';
                    }
                },
                {
                    field: 'name',
                    title: 'Name',
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    formatter: function (value, row, index, field) {
                        if (row.data != '*') {
                            value = value.substring(0, value.length / 2) + '...';
                            return '<a target="_blank" href="/portal/vs#?env=' + G.env + '&vsId=' + row.data + '">' + value + '</a>';
                        }
                        else {
                            return '-';
                        }
                    }
                },
                {
                    field: 'R',
                    title: 'Read',
                    align: 'center',
                    events: VsChangeRightEvent,
                    valign: 'middle',
                    formatter: function (value, row, index, field) {
                        return $scope.formatter('vsReadOp', value);
                    }
                },
                {
                    field: 'U',
                    title: 'Update',
                    events: VsChangeRightEvent,
                    align: 'center',
                    valign: 'middle',
                    formatter: function (value, row, index, field) {
                        return $scope.formatter('vsUpdateOp', value);
                    }
                },
                {
                    field: 'E',
                    title: 'Delete',
                    events: VsChangeRightEvent,
                    align: 'center',
                    valign: 'middle',
                    formatter: function (value, row, index, field) {
                        return $scope.formatter('vsDeleteOp', value);
                    }
                },
                {
                    field: 'A',
                    title: 'Activate',
                    events: VsChangeRightEvent,
                    align: 'center',
                    valign: 'middle',
                    formatter: function (value, row, index, field) {
                        return $scope.formatter('vsActivateOp', value);
                    }
                },
                {
                    field: 'D',
                    title: 'Deactivate',
                    events: VsChangeRightEvent,
                    align: 'center',
                    valign: 'middle',
                    formatter: function (value, row, index, field) {
                        return $scope.formatter('vsDeactivateOp', value);
                    }
                },
                {
                    field: 'P',
                    title: 'PROPERTY',
                    events: VsChangeRightEvent,
                    align: 'center',
                    valign: 'middle',
                    formatter: function (value, row, index, field) {
                        return $scope.formatter('vsPropertyOp', value);
                    }
                },
                {
                    field: 'S',
                    title: 'SYNC',
                    events: VsChangeRightEvent,
                    align: 'center',
                    valign: 'middle',
                    formatter: function (value, row, index, field) {
                        return $scope.formatter('vsSyncOp', value);
                    }
                },

                {
                    field: 'C',
                    title: 'Cert',
                    events: VsChangeRightEvent,
                    align: 'center',
                    valign: 'middle',
                    formatter: function (value, row, index, field) {
                        return $scope.formatter('vsCertOp', value);
                    }
                },
                {
                    field: 'F',
                    title: 'Force',
                    events: VsChangeRightEvent,
                    align: 'center',
                    valign: 'middle',
                    formatter: function (value, row, index, field) {
                        return $scope.formatter('vsForceOp', value);
                    }
                },
                {
                    field: 'operate',
                    width: '120px',
                    title: 'Operation',
                    align: 'left',
                    valign: 'middle',
                    events: VSOperationEvent,
                    sortable: true,
                    formatter: function () {
                        var p1 = "";
                        var str = '<button data-toggle="tooltip" type="button" class="btn btn-info deleteVSOp  ' + p1 + '" title="删除" aria-label="Left Align"><span class="fa fa-minus"></span></button>';
                        str += '  <button data-toggle="tooltip" title="修改" type="button" class="btn btn-info updateVSOp  ' + p1 + '" aria-label="Left Align">'
                            + '<span class="fa fa-edit"></span>' +
                            '</button>';
                        return str;
                    }
                }

            ], []],
            data: $scope.user.vses,
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
                return "<img class='loading-img' src='/static/img/loading_100.gif' /> 正在加载 VSES";
            },
            formatNoMatches: function () {
                return '<i class="fa status-alert fa-exclamation-triangle" aria-hidden="true"></i> 没有找到匹配的 VSES';
            }
        });
        $('#slb-table').bootstrapTable({
            toolbar: "#user-slb-toolbar",
            search: true,
            showRefresh: false,
            showColumns: true,
            columns: [[
                {
                    checkbox: 'true',
                    align: 'center',
                    valign: 'middle'
                },
                {
                    field: 'data',
                    title: 'ID',
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    formatter: function (value, row, index, field) {
                        return '<a target="_blank" href="/portal/slb#?env=' + G.env + '&slbId=' + value + '">' + value + '</a>';
                    }
                },
                {
                    field: 'name',
                    title: 'Name',
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    formatter: function (value, row, index, field) {
                        if (row.data != '*') {
                            value = value.substring(0, value.length / 2) + '...';
                            return '<a target="_blank" href="/portal/slb#?env=' + G.env + '&slbId=' + row.data + '">' + value + '</a>';
                        }
                        else {
                            return '-';
                        }
                    }
                },
                {
                    field: 'R',
                    title: 'Read',
                    align: 'center',
                    valign: 'middle',
                    events: SlbChangeRightEvent,
                    formatter: function (value, row, index, field) {
                        return $scope.formatter('slbReadOp', value);
                    }
                },
                {
                    field: 'U',
                    title: 'Update',
                    align: 'center',
                    events: SlbChangeRightEvent,
                    valign: 'middle',
                    formatter: function (value, row, index, field) {
                        return $scope.formatter('slbUpdateOp', value);
                    }
                },
                {
                    field: 'E',
                    title: 'Delete',
                    events: SlbChangeRightEvent,
                    align: 'center',
                    valign: 'middle',
                    formatter: function (value, row, index, field) {
                        return $scope.formatter('slbDeleteOp', value);
                    }
                },
                {
                    field: 'A',
                    title: 'Activate',
                    events: SlbChangeRightEvent,
                    align: 'center',
                    valign: 'middle',
                    formatter: function (value, row, index, field) {
                        return $scope.formatter('slbActivateOp', value);
                    }
                },
                {
                    field: 'D',
                    title: 'Deactivate',
                    events: SlbChangeRightEvent,
                    align: 'center',
                    valign: 'middle',
                    formatter: function (value, row, index, field) {
                        return $scope.formatter('slbDeactivateOp', value);
                    }
                },
                {
                    field: 'P',
                    title: 'PROPERTY',
                    events: SlbChangeRightEvent,
                    align: 'center',
                    valign: 'middle',
                    formatter: function (value, row, index, field) {
                        return $scope.formatter('slbPropertyOp', value);
                    }
                },
                {
                    field: 'S',
                    title: 'SYNC',
                    events: SlbChangeRightEvent,
                    align: 'center',
                    valign: 'middle',
                    formatter: function (value, row, index, field) {
                        return $scope.formatter('slbSyncOp', value);
                    }
                },

                {
                    field: 'I',
                    title: 'ADMIN',
                    align: 'center',
                    events: SlbChangeRightEvent,
                    valign: 'middle',
                    formatter: function (value, row, index, field) {
                        return $scope.formatter('slbAdminOp', value);
                    }
                },
                {
                    field: 'W',
                    title: 'WAF',
                    events: SlbChangeRightEvent,
                    align: 'center',
                    valign: 'middle',
                    formatter: function (value, row, index, field) {
                        return $scope.formatter('slbWafOp', value);
                    }
                },
                {
                    field: 'F',
                    title: 'Force',
                    events: SlbChangeRightEvent,
                    align: 'center',
                    valign: 'middle',
                    formatter: function (value, row, index, field) {
                        return $scope.formatter('slbForceOp', value);
                    }
                },
                {
                    field: 'operate',
                    width: '120px',
                    title: 'Operation',
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    events: SlbOperationEvent,
                    formatter: function () {
                        var p1 = "";
                        var str = '<button data-toggle="tooltip" type="button" class="btn btn-info deleteSlbOp  ' + p1 + '" title="删除" aria-label="Left Align"><span class="fa fa-minus"></span></button>';
                        str += '  <button data-toggle="tooltip" title="修改" type="button" class="btn btn-info updateSlbOp  ' + p1 + '" aria-label="Left Align">'
                            + '<span class="fa fa-edit"></span>' +
                            '</button>';
                        return str;
                    }
                }

            ], []],
            data: $scope.user.slbs,
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
                return "<img class='loading-img' src='/static/img/loading_100.gif' /> 正在加载 SLBS";
            },
            formatNoMatches: function () {
                return '<i class="fa status-alert fa-exclamation-triangle" aria-hidden="true"></i> 没有找到匹配的 SLBS';
            }
        });
        $('#auth-table').bootstrapTable({
            toolbar: "#user-auth-toolbar",
            search: true,
            showRefresh: false,
            showColumns: true,
            columns: [[
                {
                    checkbox: 'true',
                    align: 'center',
                    valign: 'middle'
                },
                {
                    field: 'user',
                    title: 'User',
                    align: 'left',
                    valign: 'middle'
                },
                {
                    field: 'data',
                    title: 'Data',
                    align: 'left',
                    valign: 'middle'
                },
                {
                    field: 'operate',
                    width: '120px',
                    title: 'Operation',
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    events: AuthOperationEvent,
                    formatter: function () {
                        var p1 = "";
                        var str = '<button data-toggle="tooltip" type="button" class="btn btn-info deleteAuthOp  ' + p1 + '" title="删除" aria-label="Left Align"><span class="fa fa-minus"></span></button>';
                        return str;
                    }
                }

            ], []],
            data: $scope.user.slbs,
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
                return "<img class='loading-img' src='/static/img/loading_100.gif' /> 正在加载 Auth";
            },
            formatNoMatches: function () {
                return '<i class="fa status-alert fa-exclamation-triangle" aria-hidden="true"></i> 没有找到匹配的 Auth权限';
            }
        });
        $('#user-role-table').bootstrapTable({
            toolbar: "#user-roles-toolbar",
            search: true,
            showRefresh: false,
            showColumns: true,
            columns: [[
                {
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
                    formatter: function (value, row, index, field) {
                        return '<a href="/portal/userrole#?env=' + G.env + '&roleId=' + value + '">' + value + '</a>';
                    }

                },
                {
                    field: 'role-name',
                    title: 'Name',
                    align: 'left',
                    valign: 'middle',
                    formatter: function (value, row, index, field) {
                        return '<a href="/portal/userrole#?env=' + G.env + '&roleId=' + row.id + '">' + value + '</a>';
                    }
                },
                {
                    field: 'discription',
                    title: 'Description',
                    align: 'left',
                    valign: 'middle'
                },
                {
                    field: 'operate',
                    width: '120px',
                    title: 'Operation',
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    events: RoleManagmentEvent,
                    formatter: function () {
                        var p1 = "";
                        var str = '<button data-toggle="tooltip" type="button" class="btn btn-info deleteRoleOp  ' + p1 + '" title="删除" aria-label="Left Align"><span class="fa fa-minus"></span></button>';
                        str += '  <button data-dismiss="modal" data-toggle="tooltip" title="修改" type="button" class="btn btn-info changeRoleOp  ' + p1 + '" aria-label="Left Align">'
                            + '<span class="fa fa-edit"></span>' +
                            '</button>';
                        return str;
                    }
                }
            ], []],
            data: $scope.user.roles,
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
                return "<img class='loading-img' src='/static/img/loading_100.gif' /> 正在加载 Roles";
            },
            formatNoMatches: function () {
                return '<i class="fa status-alert fa-exclamation-triangle" aria-hidden="true"></i> 没有找到匹配的 Roles';
            }
        });
        $('#role-selector-table').bootstrapTable({
            search: true,
            showRefresh: false,
            showColumns: true,
            toolbar: "#role-selector-toolbar",
            columns: [[
                {
                    checkbox: 'true',
                    align: 'center',
                    valign: 'middle'
                },
                {
                    field: 'id',
                    title: 'Name',
                    align: 'left',
                    valign: 'middle',
                    sortable: true
                },
                {
                    field: 'name',
                    title: 'Description',
                    align: 'left',
                    valign: 'middle'
                }
            ], []],
            data: $scope.data.slb_ops,
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
        $('#vs-role-selector-table').bootstrapTable({
            toolbar: "#vs-role-selector-toolbar",
            search: true,
            showRefresh: false,
            showColumns: true,
            columns: [[
                {
                    checkbox: 'true',
                    align: 'center',
                    valign: 'middle'
                },
                {
                    field: 'id',
                    title: 'Name',
                    align: 'left',
                    valign: 'middle',
                    sortable: true
                },
                {
                    field: 'name',
                    title: 'Description',
                    align: 'left',
                    valign: 'middle'
                }
            ], []],
            data: $scope.data.vs_ops,
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
        $('#group-role-selector-table').bootstrapTable({
            search: true,
            showRefresh: false,
            showColumns: true,
            toolbar: "#group-role-selector-toolbar",
            columns: [[
                {
                    checkbox: 'true',
                    align: 'center',
                    valign: 'middle'
                },
                {
                    field: 'id',
                    title: 'Name',
                    align: 'left',
                    valign: 'middle',
                    sortable: true
                },
                {
                    field: 'name',
                    title: 'Description',
                    align: 'left',
                    valign: 'middle'
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
                return "<img class='loading-img' src='/static/img/loading_100.gif' /> 正在加载 Type";
            },
            formatNoMatches: function () {
                return '<i class="fa status-alert fa-exclamation-triangle" aria-hidden="true"></i> 没有找到匹配的 Type';
            }
        });
        $('#policy-role-selector-table').bootstrapTable({
            search: true,
            showRefresh: false,
            showColumns: true,
            toolbar: "#policy-role-selector-toolbar",
            columns: [[
                {
                    checkbox: 'true',
                    align: 'center',
                    valign: 'middle'
                },
                {
                    field: 'id',
                    title: 'Name',
                    align: 'left',
                    valign: 'middle',
                    sortable: true
                },
                {
                    field: 'name',
                    title: 'Description',
                    align: 'left',
                    valign: 'middle'
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
        $('#dr-role-selector-table').bootstrapTable({
            toolbar: "#dr-role-selector-toolbar",
            columns: [[
                {
                    checkbox: 'true',
                    align: 'center',
                    valign: 'middle'
                },
                {
                    field: 'id',
                    title: 'Name',
                    align: 'left',
                    valign: 'middle',
                    sortable: true
                },
                {
                    field: 'name',
                    title: 'Description',
                    align: 'left',
                    valign: 'middle'
                }
            ], []],
            data: $scope.data.dr_ops,
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
        $('#user-role-table').on('check.bs.table uncheck.bs.table ' +
            'check-all.bs.table uncheck-all.bs.table', function () {
            $('#batch-remove-role').prop('disabled', !$('#user-role-table').bootstrapTable('getSelections').length);
        });
        $('#aut-table').on('check.bs.table uncheck.bs.table ' +
            'check-all.bs.table uncheck-all.bs.table', function () {
            $('#batch-remove-auth').prop('disabled', !$('#auth-table').bootstrapTable('getSelections').length);
        });
        $('#slb-table').on('check.bs.table uncheck.bs.table ' +
            'check-all.bs.table uncheck-all.bs.table', function () {
            $('#batch-remove-slbs').prop('disabled', !$('#slb-table').bootstrapTable('getSelections').length);
        });
        $('#vs-table').on('check.bs.table uncheck.bs.table ' +
            'check-all.bs.table uncheck-all.bs.table', function () {
            $('#batch-remove-veses').prop('disabled', !$('#vs-table').bootstrapTable('getSelections').length);
        });
        $('#group-table').on('check.bs.table uncheck.bs.table ' +
            'check-all.bs.table uncheck-all.bs.table', function () {
            $('#batch-remove-groups').prop('disabled', !$('#group-table').bootstrapTable('getSelections').length);
        });
        $('#policy-table').on('check.bs.table uncheck.bs.table ' +
            'check-all.bs.table uncheck-all.bs.table', function () {
            $('#batch-remove-policy').prop('disabled', !$('#policy-table').bootstrapTable('getSelections').length);
        });

        $('#dr-table').on('check.bs.table uncheck.bs.table ' +
            'check-all.bs.table uncheck-all.bs.table', function () {
            $('#batch-remove-dr').prop('disabled', !$('#dr-table').bootstrapTable('getSelections').length);
        });
    };
    $scope.formatter = function (className, value) {
        var str = '';
        var p1 = (A.canDo("Auth", "AUTH", '*') && $scope.optionPanelStatusBool) ? "" : "hide";

        if (p1 == "hide") {
            if (value) {
                str = "<span class='fa fa-check status-green' title='有权限'></span>";
            }
            else {
                str = "<span class='fa fa-times status-red' title='无权限'></span>";
            }
        }
        else {
            if (value) {
                str = "<span class='" + className + " fa fa-check-square status-green' title='有权限'></span>";
            }
            else {
                str = "<span class='" + className + " fa fa-square-o' title='无权限'></span>";
            }
        }
        return str;
    };
    $scope.reloadTable = function () {
        var p1 = A.canDo("Auth", "AUTH", '*') && $scope.optionPanelStatusBool;
        if (!p1) {
            $('#group-table').bootstrapTable('hideColumn', 'operate');
            $('#vs-table').bootstrapTable('hideColumn', 'operate');
            $('#slb-table').bootstrapTable('hideColumn', 'operate');
            $('#user-role-table').bootstrapTable('hideColumn', 'operate');
            $('#policy-table').bootstrapTable('hideColumn', 'operate');
            $('#dr-table').bootstrapTable('hideColumn', 'operate');
        }
        else {
            $('#group-table').bootstrapTable('showColumn', 'operate');
            $('#vs-table').bootstrapTable('showColumn', 'operate');
            $('#slb-table').bootstrapTable('showColumn', 'operate');
            $('#user-role-table').bootstrapTable('showColumn', 'operate');
            $('#policy-table').bootstrapTable('showColumn', 'operate');
            $('#dr-table').bootstrapTable('showColumn', 'operate');
        }
        $('#policy-table').bootstrapTable("load", $scope.user.policies);
        $('#dr-table').bootstrapTable("load", $scope.user.drs);
        $('#group-table').bootstrapTable("load", $scope.user.groups);
        $('#vs-table').bootstrapTable("load", $scope.user.vses);
        $('#slb-table').bootstrapTable("load", $scope.user.slbs);
        $('#user-role-table').bootstrapTable("load", $scope.user.roles);
        $('#auth-table').bootstrapTable("load", $scope.user.auths);
        $('#role-selector-table').bootstrapTable("load", $scope.data.slb_ops);
        $('#vs-role-selector-table').bootstrapTable("load", $scope.data.vs_ops);
        $('#group-role-selector-table').bootstrapTable("load", $scope.data.group_ops);
        $('#policy-role-selector-table').bootstrapTable("load", $scope.data.policy_ops);

        $('#dr-table').bootstrapTable("hideLoading");
        $('#policy-table').bootstrapTable("hideLoading");
        $('#group-table').bootstrapTable("hideLoading");
        $('#vs-table').bootstrapTable("hideLoading");
        $('#slb-table').bootstrapTable("hideLoading");
        $('#user-role-table').bootstrapTable("hideLoading");
    };
    $scope.loadData = function (hashData) {
        var userId = hashData.userId;
        var param = {
            userId: userId
        };
        var request = {
            method: 'GET',
            url: G.baseUrl + '/api/auth/user',
            params: param
        };

        var param2 = {
            type: 'info'
        };
        var request2 = {
            method: 'GET',
            url: G.baseUrl + '/api/slbs',
            params: param2
        };

        var request3 = {
            method: 'GET',
            url: G.baseUrl + '/api/vses',
            params: param2
        };
        var request4 = {
            method: 'GET',
            url: G.baseUrl + '/api/groups',
            params: param2
        };
        var request5 = {
            method: 'GET',
            url: G.baseUrl + '/api/policies',
            params: param2
        };
        var request6 = {
            method: 'GET',
            url: G.baseUrl + '/api/drs',
            params: param2
        };
        var request7 = {
            method: 'GET',
            url: G.baseUrl + '/api/auth/roles'
        };

        $q.all(
            [
                $http(request).success(
                    function (response) {
                        $scope.user = response;
                        $scope.query.user = $.extend(true, {}, response);
                        if (!$scope.query.user.roles) {
                            $scope.query.user.roles = [];
                        }
                    }
                ),
                $http(request2).success(
                    function (response) {
                        $scope.slbs = response.slbs;
                    }
                ),
                $http(request3).success(
                    function (response) {
                        $scope.vses = response['virtual-servers'];
                    }
                ),
                $http(request4).success(
                    function (response) {
                        $scope.groups = response.groups;
                    }
                ),
                $http(request5).success(
                    function (response) {
                        $scope.policies = response['traffic-policies'];
                    }
                ),
                $http(request6).success(
                    function (response) {
                        $scope.drs = response['drs'];
                    }
                ),
                $http(request7).success(
                    function (response) {
                        $scope.roles = _.indexBy(response['roles'], 'id');
                    }
                )
            ]).then(
            function () {
                $scope.user.roles = !$scope.user.roles ? [] : $scope.user.roles;

                $scope.user.auths = _.filter($scope.user['data-resources'], function (item) {
                    return item['resource-type'].toUpperCase() == 'AUTH';
                });
                $.each($scope.user.auths, function (i, item) {
                    item.data = '*';
                    item.user = $scope.query.user['chinese-name'];
                });
                $scope.user.slbs = _.filter($scope.user['data-resources'], function (item) {
                    return item['resource-type'].toUpperCase() == 'SLB';
                });
                $.each($scope.user.slbs, function (i, item) {
                    item.A = _.find(item.operations, function (item2) {
                        return item2.type == 'ACTIVATE';
                    });

                    item.D = _.find(item.operations, function (item2) {
                        return item2.type == 'DEACTIVATE';
                    });

                    item.U = _.find(item.operations, function (item2) {
                        return item2.type == 'UPDATE';
                    });
                    item.E = _.find(item.operations, function (item2) {
                        return item2.type == 'DELETE';
                    });
                    item.R = _.find(item.operations, function (item2) {
                        return item2.type == 'READ';
                    });
                    item.P = _.find(item.operations, function (item2) {
                        return item2.type == 'PROPERTY';
                    });
                    item.S = _.find(item.operations, function (item2) {
                        return item2.type == 'SYNC';
                    });
                    item.I = _.find(item.operations, function (item2) {
                        return item2.type == 'ADMIN_INFO';
                    });
                    item.W = _.find(item.operations, function (item2) {
                        return item2.type == 'WAF';
                    });
                    item.F = _.find(item.operations, function (item2) {
                        return item2.type == 'FORCE';
                    });
                    var t = _.find($scope.slbs, function (item3) {
                        return item3.id == item.data;
                    });
                    if (t) item.name = t.name;
                    else item.name = '-';
                });

                $scope.user.vses = _.filter($scope.user['data-resources'], function (item) {
                    return item['resource-type'].toUpperCase() == 'VS';
                });
                $.each($scope.user.vses, function (i, item) {
                    item.A = _.find(item.operations, function (item2) {
                        return item2.type == 'ACTIVATE';
                    });

                    item.D = _.find(item.operations, function (item2) {
                        return item2.type == 'DEACTIVATE';
                    });

                    item.U = _.find(item.operations, function (item2) {
                        return item2.type == 'UPDATE';
                    });
                    item.E = _.find(item.operations, function (item2) {
                        return item2.type == 'DELETE';
                    });
                    item.R = _.find(item.operations, function (item2) {
                        return item2.type == 'READ';
                    });
                    item.P = _.find(item.operations, function (item2) {
                        return item2.type == 'PROPERTY';
                    });
                    item.S = _.find(item.operations, function (item2) {
                        return item2.type == 'SYNC';
                    });
                    item.C = _.find(item.operations, function (item2) {
                        return item2.type == 'CERT';
                    });
                    item.F = _.find(item.operations, function (item2) {
                        return item2.type == 'FORCE';
                    });

                    var vs = _.find($scope.vses, function (item3) {
                        return item3.id == item.data;
                    });
                    if (vs) item.name = vs.name;
                    else item.name = '-';
                });

                $scope.user.groups = _.filter($scope.user['data-resources'], function (item) {
                    return item['resource-type'].toUpperCase() == 'GROUP';
                });
                $.each($scope.user.groups, function (i, item) {
                    item.A = _.find(item.operations, function (item2) {
                        return item2.type == 'ACTIVATE';
                    });

                    item.D = _.find(item.operations, function (item2) {
                        return item2.type == 'DEACTIVATE';
                    });

                    item.U = _.find(item.operations, function (item2) {
                        return item2.type == 'UPDATE';
                    });
                    item.E = _.find(item.operations, function (item2) {
                        return item2.type == 'DELETE';
                    });
                    item.R = _.find(item.operations, function (item2) {
                        return item2.type == 'READ';
                    });
                    item.P = _.find(item.operations, function (item2) {
                        return item2.type == 'PROPERTY';
                    });
                    item.S = _.find(item.operations, function (item2) {
                        return item2.type == 'SYNC';
                    });
                    item.M = _.find(item.operations, function (item2) {
                        return item2.type == 'OP_MEMBER';
                    });
                    item.L = _.find(item.operations, function (item2) {
                        return item2.type == 'OP_PULL';
                    });
                    item.H = _.find(item.operations, function (item2) {
                        return item2.type == 'OP_HEALTH_CHECK';
                    });
                    item.F = _.find(item.operations, function (item2) {
                        return item2.type == 'FORCE';
                    });
                    var t = _.find($scope.groups, function (item3) {
                        return item3.id == item.data;
                    });
                    if (t) item.name = t.name;
                    else item.name = '-';
                });

                $scope.user.policies = _.filter($scope.user['data-resources'], function (item) {
                    return item['resource-type'].toUpperCase() == 'POLICY';
                });
                $.each($scope.user.policies, function (i, item) {
                    item.A = _.find(item.operations, function (item2) {
                        return item2.type == 'ACTIVATE';
                    });

                    item.D = _.find(item.operations, function (item2) {
                        return item2.type == 'DEACTIVATE';
                    });

                    item.U = _.find(item.operations, function (item2) {
                        return item2.type == 'UPDATE';
                    });
                    item.E = _.find(item.operations, function (item2) {
                        return item2.type == 'DELETE';
                    });
                    item.R = _.find(item.operations, function (item2) {
                        return item2.type == 'READ';
                    });
                    item.P = _.find(item.operations, function (item2) {
                        return item2.type == 'PROPERTY';
                    });
                    item.F = _.find(item.operations, function (item2) {
                        return item2.type == 'FORCE';
                    });
                    var t = _.find($scope.policies, function (item3) {
                        return item3.id == item.data;
                    });
                    if (t) item.name = t.name;
                    else item.name = '-';
                });

                $scope.user.drs = _.filter($scope.user['data-resources'], function (item) {
                    return item['resource-type'].toUpperCase() == 'DR';
                });
                $.each($scope.user.drs, function (i, item) {
                    item.A = _.find(item.operations, function (item2) {
                        return item2.type == 'ACTIVATE';
                    });

                    item.D = _.find(item.operations, function (item2) {
                        return item2.type == 'DEACTIVATE';
                    });

                    item.U = _.find(item.operations, function (item2) {
                        return item2.type == 'UPDATE';
                    });
                    item.E = _.find(item.operations, function (item2) {
                        return item2.type == 'DELETE';
                    });
                    item.R = _.find(item.operations, function (item2) {
                        return item2.type == 'READ';
                    });
                    item.P = _.find(item.operations, function (item2) {
                        return item2.type == 'PROPERTY';
                    });
                    item.F = _.find(item.operations, function (item2) {
                        return item2.type == 'FORCE';
                    });
                    var t = _.find($scope.drs, function (item3) {
                        return item3.id == item.data;
                    });
                    if (t) item.name = t.name;
                    else item.name = '-';
                });
                $scope.reloadTable();
            }
        );
    }
    $scope.hashChanged = function (hashData) { $scope.resource = H.resource;
        if (hashData.env) {
            $scope.env = hashData.env;
        }
        else {
            $scope.env = 'pro';
        }

        if (typeof(hashData.openPanel) == "undefined") {
            $scope.optionPanelStatusBool = false;
        } else {
            $scope.optionPanelStatusBool = hashData.openPanel == "true" ? true : false;
        }
        $scope.initTable();

        $('#group-table').bootstrapTable("removeAll");
        $('#dr-table').bootstrapTable("removeAll");
        $('#policy-table').bootstrapTable("removeAll");
        $('#vs-table').bootstrapTable("removeAll");
        $('#slb-table').bootstrapTable("removeAll");
        $('#user-role-table').bootstrapTable("removeAll");

        $('#group-table').bootstrapTable("showLoading");
        $('#dr-table').bootstrapTable("showLoading");
        $('#policy-table').bootstrapTable("showLoading");
        $('#vs-table').bootstrapTable("showLoading");
        $('#slb-table').bootstrapTable("showLoading");
        $('#user-role-table').bootstrapTable("showLoading");
        $scope.loadData(hashData);
    };
    H.addListener("userEditInfoApp", $scope, $scope.hashChanged);
});
angular.bootstrap(document.getElementById("user-info-area"), ['userEditInfoApp']);
//InfoLinksComponent: info links
var headerInfoApp = angular.module('headerInfoApp', ["angucomplete-alt"]);
headerInfoApp.controller('headerInfoController', function ($scope, $http) {
    $scope.query = {};
    $scope.generateLink = function (a) {
        var link = '';
        switch (a) {
            case 'user': {
                link = "/portal/users#?env=" + G.env;
                break;
            }
            case 'role': {
                link = "/portal/userroles#?env=" + G.env;
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
        if (hashData.slbId) {
            $scope.query.slbId = hashData.slbId;
        }
    };
    H.addListener("infoLinksApp", $scope, $scope.hashChanged);
});
angular.bootstrap(document.getElementById("header-area"), ['headerInfoApp']);

