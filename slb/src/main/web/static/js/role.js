/**
 * Created by ygshen on 2016/10/20.
 */
var infoLinksApp = angular.module('infoLinksApp', ["angucomplete-alt", "http-auth-interceptor"]);
infoLinksApp.controller('infoLinksController', function ($scope) {

    $scope.data = {
        current: 'Roles信息',
        links: ['用户信息', 'Roles信息'],
        hrefs: {
            '用户信息': '/portal/users',
            'Roles信息': '/portal/roles'
        }
    };

    $scope.isCurrentInfoPage = function (link) {
        return $scope.data.current == link ? 'current' : '';
    };

    $scope.generateInfoLink = function (link) {
        return $scope.data.hrefs[link] + "#?env=" + G.env;
    };

    $scope.hashChanged = function (hashData) { $scope.resource = H.resource;
        if (hashData.env) {
            $scope.env = hashData.env;
        }
    };
    H.addListener("infoLinksApp", $scope, $scope.hashChanged);
});
angular.bootstrap(document.getElementById("info-links-area"), ['infoLinksApp']);

var roleEditInfoApp = angular.module('roleEditInfoApp', ["http-auth-interceptor","angucomplete-alt"]);
roleEditInfoApp.controller('roleEditInfoController', function ($scope, $http, $q) {
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
     *
     *
     /*** AUTH Related
     * A: Auth
     /** Clean Related
     * M: MAINTENANCE
     /** Conf Related
     * C: Conf
     /** Lock Related
     * L: Lock
     / ** SyncError Related
     * S: SyncError
     * / ** IP Related
     * I: OP_SERVER
     **/
    $scope.query={
        slb:{
            id:''
        },
        vs:{
            id:''
        },
        group:{
            id:''
        },
        policy:{
            id:''
        },
        role:{
        },
        auth:{
            id:'*'
        },
        sync:{
            id:'*'
        },
        conf:{
            id:'*'
        },
        lock:{
            id:'*'
        },
        clean:{
            id:'*'
        },
        ip:{
            id:'*'
        }
    };
    $scope.data={
        systemAccounts:[
            'slbvisitor',
            'slbadmin',
            'ops',
            'superadmin'
        ],
        slb_ops:[
            {id:'ACTIVATE', name:'激活SLB 上线'},
            {id:'DEACTIVATE', name:'下线SLB'},
            {id:'UPDATE', name:'更新SLB属性'},
            {id:'DELETE', name:'删除SLB'},
            {id:'READ', name:'查询SLB信息'},
            {id:'PROPERTY', name:'给SLB 打Tag或者Property'},
            {id:'SYNC', name:'调用SLB接口向CMS同步数据'},
            {id:'ADMIN_INFO', name:'TBD'},
            {id:'WAF', name:'上传WAF包'}
        ],
        vs_ops:[
            {id:'ACTIVATE', name:'激活 VS 上线'},
            {id:'DEACTIVATE', name:'下线 VS'},
            {id:'UPDATE', name:'更新VS属性'},
            {id:'DELETE', name:'删除VS'},
            {id:'READ', name:'查询VS信息'},
            {id:'PROPERTY', name:'给VS 打Tag或者Property'},
            {id:'SYNC', name:'调用SLB接口向CMS同步数据'},
            {id:'CERT', name:'上传证书'}
        ],
        group_ops:[
            {id:'ACTIVATE', name:'激活 GROUP 上线'},
            {id:'DEACTIVATE', name:'下线 GROUP'},
            {id:'UPDATE', name:'更新GROUP属性'},
            {id:'DELETE', name:'删除GROUP'},
            {id:'READ', name:'查询GROUP信息'},
            {id:'PROPERTY', name:'给GROUP 打Tag或者Property'},
            {id:'SYNC', name:'调用SLB接口向CMS同步数据'},
            {id:'OP_MEMBER', name:'Member拉入拉出'},
            {id:'OP_PULL', name:'Group拉入拉出'},
            {id:'OP_HEALTH_CHECK', name:'健康检测拉入拉出'}
        ],
        policy_ops:[
            {id:'ACTIVATE', name:'激活 POLICY 上线'},
            {id:'DEACTIVATE', name:'下线 POLICY'},
            {id:'UPDATE', name:'POLICY'},
            {id:'DELETE', name:'删除 POLICY'},
            {id:'READ', name:'查询 POLICY 信息'},
            {id:'PROPERTY', name:'给 POLICY 打Tag或者Property'},
            {id:'FORCE', name:'POLICY的Force操作'}
        ],
        auth_ops:[
            {id:'AUTH',name: '为其他用户赋予Authorize权限'}
        ],
        clean_ops:[
            {id:'MAINTENANCE',name: '启动Clean Job的权限'}
        ],
        conf_ops:[
            {id:'MAINTENANCE',name: '查看Conf文件的权限'}
        ],
        lock_ops:[
            {id:'MAINTENANCE',name: '释放锁的权限'}
        ],
        sync_ops:[
            {id:'MAINTENANCE',name: '同步错误的权限'}
        ],
        ip_ops:[
            {id:'OP_SERVER',name: '操作改网段机器的拉入与拉出'}
        ]
    };
    $scope.role={};
    $scope.slbs={};
    $scope.vses={};
    $scope.groups={};
    $scope.policies={};
    $scope.optionPanelStatusBool=false;

    // Rights Area
    $scope.showAddSlbBt= function () {
        return A.canDo("Auth", "AUTH", '*') && $scope.optionPanelStatusBool;
    };$scope.showRemoveSlbBt= function () {
        return A.canDo("Auth", "AUTH", '*') && $scope.optionPanelStatusBool;
    };$scope.showAddVsBt= function () {
        return A.canDo("Auth", "AUTH", '*') && $scope.optionPanelStatusBool;
    };$scope.showRemoveVsBt= function () {
        return A.canDo("Auth", "AUTH", '*') && $scope.optionPanelStatusBool;
    };$scope.showAddGroupBt= function () {
        return A.canDo("Auth", "AUTH", '*') && $scope.optionPanelStatusBool;
    };$scope.showRemoveGroupBt= function () {
        return A.canDo("Auth", "AUTH", '*') && $scope.optionPanelStatusBool;
    };$scope.showRemovePolicyBt= function () {
        return A.canDo("Auth", "AUTH", '*') && $scope.optionPanelStatusBool;
    };
    $scope.showAddPolicyBt= function () {
        return A.canDo("Auth", "AUTH", '*') && $scope.optionPanelStatusBool;
    };
    $scope.showAddConfBt= function () {
        return A.canDo("Auth", "AUTH", '*') && $scope.optionPanelStatusBool;
    };$scope.showRemoveConfBt= function () {
        return A.canDo("Auth", "AUTH", '*') && $scope.optionPanelStatusBool;
    };$scope.showAddCleanBt= function () {
        return A.canDo("Auth", "AUTH", '*') && $scope.optionPanelStatusBool;
    };$scope.showRemoveCleanBt= function () {
        return A.canDo("Auth", "AUTH", '*') && $scope.optionPanelStatusBool;
    };$scope.showAddSyncBt= function () {
        return A.canDo("Auth", "AUTH", '*') && $scope.optionPanelStatusBool;
    };$scope.showRemoveSyncBt= function () {
        return A.canDo("Auth", "AUTH", '*') && $scope.optionPanelStatusBool;
    };$scope.showAddLockBt= function () {
        return A.canDo("Auth", "AUTH", '*') && $scope.optionPanelStatusBool;
    };$scope.showRemoveLockBt= function () {
        return A.canDo("Auth", "AUTH", '*') && $scope.optionPanelStatusBool;
    };$scope.showAddIpBt= function () {
        return A.canDo("Auth", "AUTH", '*') && $scope.optionPanelStatusBool;
    };$scope.showRemoveIpBt= function () {
        return A.canDo("Auth", "AUTH", '*') && $scope.optionPanelStatusBool;
    };$scope.showAddAuthBt= function () {
        return A.canDo("Auth", "AUTH", '*') && $scope.optionPanelStatusBool;
    };$scope.showRemoveAuthBt= function () {
        return A.canDo("Auth", "AUTH", '*') && $scope.optionPanelStatusBool;
    };$scope.showUpdateBt= function () {
        return A.canDo("Auth", "AUTH", '*') && $scope.optionPanelStatusBool  && $scope.isSystemRole();
    };$scope.showDeleteBt= function () {
        return A.canDo("Auth", "AUTH", '*') && $scope.optionPanelStatusBool && $scope.isSystemRole();
    };$scope.showCloneBt= function () {
        return A.canDo("Auth", "AUTH", '*') && $scope.optionPanelStatusBool;
    };
    $scope.isSystemRole= function () {
        var r= $scope.role['role-name'];
        if(!r) return true;
        else{
            return $scope.data.systemAccounts.indexOf(r.toLowerCase())==-1;
        }
    }

    // Auto-Complete Area
    $scope.cacheRequestFn = function (str) {
        return {q: str, timestamp: new Date().getTime()};
    };
    $scope.remoteRolesUrl= function () {
        return G.baseUrl + "/api/auth/roles";
    };
    $scope.remoteSLBsUrl = function() {
        return G.baseUrl + "/api/meta/slbs";
    };
    $scope.remoteVSUrl = function() {
        return G.baseUrl + "/api/meta/vses";
    };
    $scope.remoteGroupsUrl = function() {
        return G.baseUrl + "/api/meta/groups";
    };
    $scope.remotePolicyUrl = function() {
        return G.baseUrl + "/api/meta/policies";
    };
    $scope.selectRoleId= function (t) {
        if (t) {
            var id = t.originalObject.id;
            $scope.query.role.id=id;
            $('.add-role-alert').html('');
        }
    };
    $scope.selectSlbId= function (t) {
        if (t) {
            var id = t.originalObject.id;
            $scope.query.slb.id=id;
            $('.add-slb-alert').html('');
        }
    };
    $scope.selectVSId= function (t) {
        if (t) {
            var id = t.originalObject.id;
            $scope.query.vs.id=id;
            $('.add-vs-alert').html('');
        }
    };
    $scope.selectGroupId= function (t) {
        if (t) {
            var id = t.originalObject.id;
            $scope.query.group.id=id;
            $('.add-group-alert').html('');
        }
    };
    $scope.selectPolicyId= function (t) {
        if (t) {
            var id = t.originalObject.id;
            $scope.query.policy.id=id;
            $('.add-policy-alert').html('');
        }
    };

    // Operation Panel
    $scope.toggleOptionPanel= function () {
        H.setData({'openPanel': !$scope.optionPanelStatusBool});
    };
    $scope.getOptionPanelText= function () {
        if(!$scope.optionPanelStatusBool) return "打开操作面板";
        return "收起操作面板";
    };
    $scope.getOptionPanelCss= function () {
        if(!$scope.optionPanelStatusBool) {
            return "fa fa-arrows panel-close";
        } else {
            return "fa fa-arrows-alt panel-open";
        }
    };

    // Focus Area
    $scope.getFocusObject= function () {
        /*    if($scope.query==undefined) return undefined;
         var f = _.find($scope.information.extendedGroup.tags, function (item) {
         return item.trim().toLowerCase()=='user_'+$scope.query.role;
         });
         return f;*/
    };
    $scope.toggleFocus = function () {
    };
    $scope.getFocusCss = function () {
        var f= $scope.getFocusObject();
        if(f==undefined) return "fa fa-eye-slash status-unfocus";
        return "fa fa-eye status-focus";
    };
    $scope.getFocusText= function () {
        var f= $scope.getFocusObject();
        if(!f) return "关注";
        return "取消关注";
    };
    $scope.addFocus = function (tagName, type) {
        var request = {
            method: 'GET',
            url: G.baseUrl + "/api/tagging?type=group&tagName=" + tagName + "&targetId=" + $scope.information.extendedGroup.id
        };
        $http(request).success(
            function (res) {
                H.setData({"timeStamp":new Date().getTime()});
            });

    };
    $scope.removeFocus = function (tagName, type) {
        var request = {
            method: 'GET',
            url: G.baseUrl + "/api/untagging?type=group&tagName=" + tagName + "&targetId=" + $scope.information.extendedGroup.id
        };
        $http(request).success(
            function (res) {
                H.setData({"timeStamp":new Date().getTime()});
            });
    };
    // Top bar management Area
    $scope.popRemoveRoleDialog= function () {
       $('#confirmRemoveRoleDialog').find('.to-be-removed-roles').html('');
        var id=$scope.query.role['id'];
        var name=$scope.query.role['role-name'];
        var desc=$scope.query.role['discription'];

        var str='<tr><td>'+id+'</td><td>'+name+'</td><td>'+desc+'</td></tr>';
        $('#confirmRemoveRoleDialog').modal('show').find('.to-be-removed-roles').append(str);
    };
    $scope.confirmRemoveRole= function () {
        $scope.removeSucceed=false;
        var param ={
            roleId: $scope.query.role['id']
        };
        var request={
            method:'GET',
            url: G.baseUrl+'/api/auth/role/delete',
            params: param
        };
        $scope.processRequest(request,$('#operationResultDialog'),'成功删除Role','Role删除成功');
    };

    //SLB Management Area
    var selectedRemoveSlbItems=[];
    var selectedEditSlbItems=[];
    window.SlbOperationEvent={
        'click .deleteSlbOp': function (e, value, row) {
            $scope.popRemoveSlbDialog(row);
        },
        'click .updateSlbOp': function (e, value, row) {
            $scope.popEditSlbDialog(row);
        }
    };
    window.SlbChangeRightEvent={
        'click .slbReadOp': function(e,value,row){
            $scope.toggleSlbRightClick(row.data,"READ",value);
        },
        'click .slbUpdateOp': function(e,value,row){
            $scope.toggleSlbRightClick(row.data,"UPDATE",value);
        },
        'click .slbDeleteOp': function(e,value,row){
            $scope.toggleSlbRightClick(row.data,"DELETE",value);
        },
        'click .slbPropertyOp': function(e,value,row){
            $scope.toggleSlbRightClick(row.data,"PROPERTY",value);
        },
        'click .slbSyncOp': function(e,value,row){
            $scope.toggleSlbRightClick(row.data,"SYNC",value);
        },
        'click .slbActivateOp': function(e,value,row){
            $scope.toggleSlbRightClick(row.data,"ACTIVATE",value);
        },
        'click .slbDeactivateOp': function(e,value,row){
            $scope.toggleSlbRightClick(row.data,"DEACTIVATE",value);
        },
        'click .slbAdminOp': function(e,value,row){
            $scope.toggleSlbRightClick(row.data,"ADMIN_INFO",value);
        },
        'click .slbWafOp': function(e,value,row){
            $scope.toggleSlbRightClick(row.data,"WAF",value);
        }
    };
    $scope.toggleSlbRightClick= function (id, op,v) {
        var request={
            method: 'POST',
            url: G.baseUrl+'/api/auth/role/update'
        };
        var c=v!=undefined;
        var b = $scope.query.role;
        var r = _.findIndex(b['data-resources'], function (item) {
            return item.data==id && item['resource-type'].toLowerCase()=='slb';
        });
        if(r!=-1){
            var o = b['data-resources'][r].operations;
            var i = _.findIndex(o, function (item) {
                return item.type.toLowerCase()==op.toLowerCase();
            });

            // Splice those empty typed element
            var j = _.findIndex(o, function (item) {
                return item.type=='';
            });
            if(j!=-1) o.splice(j,1);

            if(c){
                // Turn off the right
                o.splice(i,1);
            }else{
                // Turn on the right
                o.push({type:op});
            }

            // Save the changes
            request.data=b;
            var txt=c?"成功收回 "+op+" 权限":"成功赋予 "+op+" 权限";
            $scope.processRequest(request,$('#operationResultDialog'),'更新User',txt);
        }
    };
    $scope.addNewSlbClick= function () {
        $scope.popEditSlbDialog();
    };
    $scope.batchRemoveSlbClick= function () {
        $scope.popRemoveSlbDialog();
    };
    $scope.saveNewSlbClick= function () {
        var request={
            method: 'POST',
            url: G.baseUrl+'/api/auth/role/update'
        };
        if($scope.query.slb.id){
            if($scope.getSlbSelectedItems().length==0){
                $('.add-slb-auth-alert').html('(请至少选择一种权限!)');
                return;
            }
            $('#newSlbDialog').modal('hide');
            var b =$scope.query.role;
            if(selectedEditSlbItems.length>0){
                var r = _.findIndex(b['data-resources'], function (item) {
                    return item.data==selectedEditSlbItems[0].data && item['resource-type'].toLowerCase()=='slb';
                });
                b['data-resources'][r]=={};
                b['data-resources'][r]={
                    data: $scope.query.slb.id,
                    'resource-type':'Slb',
                    operations:$scope.getSlbSelectedItems()
                };
                selectedEditSlbItems=[];
            }
            else{
                if(!b['data-resources']){
                    b['data-resources']=[];
                }
                b['data-resources'].push(
                    {
                        'resource-type':'Slb',
                        data: $scope.query.slb.id,
                        operations: $scope.getSlbSelectedItems()
                    }
                );
            }
            request.data=b;
            $scope.processRequest(request,$('#operationResultDialog'),'更新User Roles','更新成功！');
        }
        else{
            $('.add-slb-alert').html('<p  style="color:red; margin-left: 200px">请选择要关联的SLB</p>');
        }
    };
    $scope.popRemoveSlbDialog= function (row) {
        $('.to-be-removed-slbs').html('');
        selectedRemoveSlbItems=[];
        var str='';
        if(row){
            selectedRemoveSlbItems.push(row);
        }
        else{
            selectedRemoveSlbItems = $('#slb-table').bootstrapTable('getSelections');
        }
        $.each(selectedRemoveSlbItems, function (i, item) {
            str+='<tr><td>'+item.data+'</td>';
            var d='';
            var t= _.find($scope.slbs,function (item3) {
                return item3.id==item.data;
            });
            if(t) d=t.name;
            else d='-';
            str+='<td>'+d+'</td></tr>';
        });
        $('#confirmRemoveSlbsDialog').modal('show').find('.to-be-removed-slbs').append(str);
    };
    $scope.confirmRemoveSlb= function () {
        var request={
            method: 'POST',
            url: G.baseUrl+'/api/auth/role/update'
        };
        var b = $scope.query.role;
        var remaining=_.filter(b['data-resources'], function (item) {
            return _.find(selectedRemoveSlbItems, function (item2) {
                    return item.data==item2.data && item['resource-type'].toLowerCase()=='group';;
                })==undefined;
        });
        b['data-resources']=remaining;
        request.data=b;
        $scope.processRequest(request,$('#operationResultDialog'),'更新 User','更新成功！');
    };
    $scope.popEditSlbDialog= function (row) {
        $scope.query.slb.id=undefined;
        selectedEditSlbItems=[];
        $('.check-all-slbs-bt').prop('checked',false);

        $('#newSlbDialog').modal('show').find('.add-slb-alert').html('');
        $('.add-slb-auth-alert').html('');
        $("#role-selector-table").bootstrapTable("uncheckAll");
        $('#slbIdSelector_value').val('');

        if(row){
            $('#slbIdSelector_value').val(row.data.toString());
            var selectedRowsArray=[];
            var item=row;
            $scope.query.slb.id=item.data;
            if(item.data=='*'){
                $('.check-all-slbs-bt').prop('checked',true);
            }
            if(item.R){
                selectedRowsArray.push(item.R.type);
            }
            if(item.D){
                selectedRowsArray.push(item.D.type);
            }
            if(item.A){
                selectedRowsArray.push(item.A.type);
            }
            if(item.U){
                selectedRowsArray.push(item.U.type);
            }
            if(item.E){
                selectedRowsArray.push(item.E.type);
            }
            if(item.P){
                selectedRowsArray.push(item.P.type);
            }
            if(item.S){
                selectedRowsArray.push(item.S.type);
            }
            if(item.I){
                selectedRowsArray.push(item.I.type);
            }
            if(item.W){
                selectedRowsArray.push(item.W.type);
            }
            $("#role-selector-table").bootstrapTable("checkBy", {field:"id", values:selectedRowsArray})
            selectedEditSlbItems=[row];
        }
    };
    $scope.getSlbSelectedItems= function () {
        var result=[];
        var slbRoles =  $('#role-selector-table').bootstrapTable('getSelections');
        $.each(slbRoles, function (i, item) {
            result.push({type:item.id});
        });
        return result;
    };
    $scope.checkAllSlbs= function () {
        var c =$('.check-all-slbs-bt').is(':checked');
        if(c){
            $scope.query.slb.id='*';
            $('#slbIdSelector_value').val('*');
        }
    };

    // VS Management Area
    var selectedRemoveVsItems=[];
    var selectedEditVsItems=[];
    window.VSOperationEvent={
        'click .deleteVSOp': function (e, value, row) {
            $scope.popRemoveVSDialog(row);
        },
        'click .updateVSOp': function (e, value, row) {
            $scope.popEditVSDialog(row);
        }
    };
    window.VsChangeRightEvent={
        'click .vsReadOp': function(e,value,row){
            $scope.toggleVSRightClick(row.data,"READ",value);
        },
        'click .vsUpdateOp': function(e,value,row){
            $scope.toggleVSRightClick(row.data,"UPDATE",value);
        },
        'click .vsDeleteOp': function(e,value,row){
            $scope.toggleVSRightClick(row.data,"DELETE",value);
        },
        'click .vsPropertyOp': function(e,value,row){
            $scope.toggleVSRightClick(row.data,"PROPERTY",value);
        },
        'click .vsSyncOp': function(e,value,row){
            $scope.toggleVSRightClick(row.data,"SYNC",value);
        },
        'click .vsActivateOp': function(e,value,row){
            $scope.toggleVSRightClick(row.data,"ACTIVATE",value);
        },
        'click .vsDeactivateOp': function(e,value,row){
            $scope.toggleVSRightClick(row.data,"DEACTIVATE",value);
        },
        'click .vsCertOp': function(e,value,row){
            $scope.toggleVSRightClick(row.data,"CERT",value);
        }
    };
    $scope.toggleVSRightClick= function (id, op,v) {
        var request={
            method: 'POST',
            url: '/api/auth/role/update'
        };
        var c=v!=undefined;
        var b = $scope.query.role;
        var r = _.findIndex(b['data-resources'], function (item) {
            return item.data==id && item['resource-type'].toLowerCase()=='vs';
        });
        if(r!=-1){
            var o = b['data-resources'][r].operations;
            var i = _.findIndex(o, function (item) {
                return item.type.toLowerCase()==op.toLowerCase();
            });

            // Splice those empty typed element
            var j = _.findIndex(o, function (item) {
                return item.type=='';
            });
            if(j!=-1) o.splice(j,1);

            if(c){
                // Turn off the right
                o.splice(i,1);
            }else{
                // Turn on the right
                o.push({type:op});
            }

            // Save the changes
            request.data=b;
            var txt=c?"成功收回 "+op+" 权限":"成功赋予 "+op+" 权限";
            $scope.processRequest(request,$('#operationResultDialog'),'更新User',txt);
        }
    };
    $scope.addNewVsClick= function () {
        $scope.popEditVSDialog();
    };
    $scope.batchRemoveVsClick= function () {
        $scope.popRemoveVSDialog();
    };
    $scope.saveNewVSClick= function () {
        var request={
            method: 'POST',
            url: G.baseUrl+'/api/auth/role/update'
        };
        if($scope.query.vs.id){
            if($scope.getVSSelectedItems().length==0){
                $('.add-vs-auth-alert').html('(请至少选择一种权限!)');
                return;
            }
            $('#newVSDialog').modal('hide');
            var b =$scope.query.role;
            if(selectedEditVsItems.length>0){
                var r = _.findIndex(b['data-resources'], function (item) {
                    return item.data==selectedEditVsItems[0].data && item['resource-type'].toLowerCase()=='vs';
                });
                b['data-resources'][r]=={};
                b['data-resources'][r]={
                    data: $scope.query.vs.id,
                    'resource-type':'Vs',
                    operations:$scope.getVSSelectedItems()
                };
                selectedEditVsItems=[];
            }
            else{
                if(!b['data-resources']){
                    b['data-resources']=[];
                }

                b['data-resources'].push(
                    {
                        'resource-type':'Vs',
                        data: $scope.query.vs.id,
                        operations: $scope.getVSSelectedItems()
                    }
                );
            }

            request.data=b;
            $scope.processRequest(request,$('#operationResultDialog'),'更新User','更新成功！');
        }
        else{
            $('.add-vs-alert').html('<p  style="color:red; margin-left: 200px">请选择要关联的VS</p>');
        }
    };
    $scope.popRemoveVSDialog= function (row) {
        $('.to-be-removed-vses').html('');
        selectedRemoveVsItems=[];
        var str='';
        if(row){
            selectedRemoveVsItems.push(row);
        }
        else{
            selectedRemoveVsItems = $('#vs-table').bootstrapTable('getSelections');
        }
        $.each(selectedRemoveVsItems, function (i, item) {
            str+='<tr><td>'+item.data+'</td>';
            var d='';
            var t= _.find($scope.vses,function (item3) {
                return item3.id==item.data;
            });
            if(t) d= t.name;
            d='-';
            str+='<td>'+d+'</td></tr>';
        });
        $('#confirmRemoveVSDialog').modal('show').find('.to-be-removed-vses').append(str);
    };
    $scope.confirmRemoveVS= function () {
        var request={
            method: 'POST',
            url: G.baseUrl+'/api/auth/role/update'
        };
        var b = $scope.query.role;
        var remaining=_.filter(b['data-resources'], function (item) {
            return _.find(selectedRemoveVsItems, function (item2) {
                    return item.data==item2.data && item['resource-type'].toLowerCase()=='vs';
                })==undefined;
        });
        b['data-resources']=remaining;
        request.data=b;
        $scope.processRequest(request,$('#operationResultDialog'),'更新 User','更新成功！');
    };
    $scope.popEditVSDialog= function (row) {
        $scope.query.vs.id=undefined;
        selectedEditVsItems=[];
        $('.check-all-vses-bt').prop('checked',false);
        $('#newVSDialog').modal('show').find('.add-vs-alert').html('');
        $('.add-vs-auth-alert').html('');
        $("#vs-role-selector-table").bootstrapTable("uncheckAll");
        $('#vsIdSelector_value').val('');
        if(row){
            $('#vsIdSelector_value').val(row.data.toString());
            var selectedRowsArray=[];
            var item=row;
            $scope.query.vs.id=item.data;
            if(item.data=='*'){
                $('.check-all-vses-bt').prop('checked',true);
            }
            if(item.R){
                selectedRowsArray.push(item.R.type);
            }
            if(item.D){
                selectedRowsArray.push(item.D.type);
            }
            if(item.A){
                selectedRowsArray.push(item.A.type);
            }
            if(item.U){
                selectedRowsArray.push(item.U.type);
            }
            if(item.E){
                selectedRowsArray.push(item.E.type);
            }
            if(item.P){
                selectedRowsArray.push(item.P.type);
            }
            if(item.S){
                selectedRowsArray.push(item.S.type);
            }
            if(item.C){
                selectedRowsArray.push(item.C.type);
            }
            $("#vs-role-selector-table").bootstrapTable("checkBy", {field:"id", values:selectedRowsArray})
            selectedEditVsItems=[row];
        }
    };
    $scope.getVSSelectedItems= function () {
        var result=[];
        var slbRoles =  $('#vs-role-selector-table').bootstrapTable('getSelections');
        $.each(slbRoles, function (i, item) {
            result.push({type:item.id});
        });
        return result;
    };
    $scope.checkAllVses= function () {
        var c =$('.check-all-vses-bt').is(':checked');
        if(c){
            $scope.query.vs.id='*';
            $('#vsIdSelector_value').val('*');
        }
    };

    // Group Management Area
    var selectedRemoveGroupItems=[];
    var selectedRemovePolicyItems=[];
    var selectedEditGroupItems=[];
    var selectedEditPolicyItems=[];
    window.GroupOperationEvent={
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
    window.GroupChangeRightEvent={
        'click .groupReadOp': function(e,value,row){
            $scope.toggleGroupRightClick(row.data,"READ",value);
        },
        'click .groupUpdateOp': function(e,value,row){
            $scope.toggleGroupRightClick(row.data,"UPDATE",value);
        },
        'click .groupDeleteOp': function(e,value,row){
            $scope.toggleGroupRightClick(row.data,"DELETE",value);
        },
        'click .groupPropertyOp': function(e,value,row){
            $scope.toggleGroupRightClick(row.data,"PROPERTY",value);
        },
        'click .groupSyncOp': function(e,value,row){
            $scope.toggleGroupRightClick(row.data,"SYNC",value);
        },
        'click .groupActivateOp': function(e,value,row){
            $scope.toggleGroupRightClick(row.data,"ACTIVATE",value);
        },
        'click .groupDeactivateOp': function(e,value,row){
            $scope.toggleGroupRightClick(row.data,"DEACTIVATE",value);
        },
        'click .groupMemberOp': function(e,value,row){
            $scope.toggleGroupRightClick(row.data,"OP_MEMBER",value);
        },
        'click .groupPullOp': function(e,value,row){
            $scope.toggleGroupRightClick(row.data,"OP_PULL",value);
        },
        'click .groupHealthOp': function(e,value,row){
            $scope.toggleGroupRightClick(row.data,"OP_HEALTH_CHECK",value);
        }
    };
    $scope.toggleGroupRightClick= function (id, op,v) {
        var request={
            method: 'POST',
            url: G.baseUrl+'/api/auth/role/update'
        };
        var c=v!=undefined;
        var b = $scope.query.role;
        var r = _.findIndex(b['data-resources'], function (item) {
            return item.data==id && item['resource-type'].toLowerCase()=='group';
        });
        if(r!=-1){
            var o = b['data-resources'][r].operations;
            var i = _.findIndex(o, function (item) {
                return item.type.toLowerCase()==op.toLowerCase();
            });

            // Splice those empty typed element
            var j = _.findIndex(o, function (item) {
                return item.type=='';
            });
            if(j!=-1) o.splice(j,1);

            if(c){
                // Turn off the right
                o.splice(i,1);
            }else{
                // Turn on the right
                o.push({type:op});
            }

            // Save the changes
            request.data=b;
            var txt=c?"成功收回 "+op+" 权限":"成功赋予 "+op+" 权限";
            $scope.processRequest(request,$('#operationResultDialog'),'更新User',txt);
        }
    };
    $scope.addNewGroupClick= function () {
        $scope.popEditGroupDialog();
    };
    $scope.addNewPolicyClick= function () {
        $scope.popEditPolicyDialog();
    };
    $scope.batchRemoveGroupClick= function (){
        $scope.popRemoveGroupDialog();
    };
    $scope.saveNewGroupClick= function (){
        var request={
            method: 'POST',
            url: G.baseUrl+'/api/auth/role/update'
        };
        if($scope.query.group.id){
            if($scope.getGroupSelectedItems().length==0){
                $('.add-group-auth-alert').html('(请至少选择一种权限!)');
                return;
            }
            $('#newGroupDialog').modal('hide');
            var b =$scope.query.role;
            if(selectedEditGroupItems.length>0){
                var r = _.findIndex(b['data-resources'], function (item) {
                    return item.data==selectedEditGroupItems[0].data;
                });
                b['data-resources'][r]=={};
                b['data-resources'][r]={
                    data: $scope.query.group.id,
                    'resource-type':'Group',
                    operations:$scope.getGroupSelectedItems()
                };
                selectedEditGroupItems=[];
            }
            else{
                if(!b['data-resources']){
                    b['data-resources']=[];
                }

                b['data-resources'].push(
                    {
                        'resource-type':'Group',
                        data: $scope.query.group.id,
                        operations: $scope.getGroupSelectedItems()
                    }
                );
            }

            request.data=b;
            $scope.processRequest(request,$('#operationResultDialog'),'更新User','更新成功！');
        }
        else{
            $('.add-group-alert').html('<p  style="color:red; margin-left: 200px">请选择要关联的 Group</p>');
        }
    };
    $scope.saveNewPolicyClick= function (){
        var request={
            method: 'POST',
            url: G.baseUrl+'/api/auth/role/update'
        };
        if($scope.query.policy.id){
            if($scope.getPolicySelectedItems().length==0){
                $('.add-policy-auth-alert').html('(请至少选择一种权限!)');
                return;
            }
            $('#newPolicyDialog').modal('hide');
            var b =$scope.query.role;
            if(selectedEditPolicyItems.length>0){
                var r = _.findIndex(b['data-resources'], function (item) {
                    return item.data==selectedEditPolicyItems[0].data;
                });
                b['data-resources'][r]=={};
                b['data-resources'][r]={
                    data: $scope.query.policy.id,
                    'resource-type':'Policy',
                    operations:$scope.getPolicySelectedItems()
                };
                selectedEditPolicyItems=[];
            }
            else{
                if(!b['data-resources']){
                    b['data-resources']=[];
                }

                b['data-resources'].push(
                    {
                        'resource-type':'Policy',
                        data: $scope.query.policy.id,
                        operations: $scope.getPolicySelectedItems()
                    }
                );
            }

            request.data=b;
            $scope.processRequest(request,$('#operationResultDialog'),'更新Role','更新成功！');
        }
        else{
            $('.add-policy-alert').html('<p  style="color:red; margin-left: 200px">请选择要关联的 Policy</p>');
        }
    };
    $scope.popRemoveGroupDialog= function (row) {
        $('.to-be-removed-groups').html('');
        selectedRemoveGroupItems=[];
        var str='';
        if(row){
            selectedRemoveGroupItems.push(row);
        }
        else{
            selectedRemoveGroupItems = $('#group-table').bootstrapTable('getSelections');
        }
        $.each(selectedRemoveGroupItems, function (i, item) {
            str+='<tr><td>'+item.data+'</td>';
            var d='';
            var t= _.find($scope.groups,function (item3) {
                return item3.id==item.data;
            });
            if(t) d= t.name;
            else d='-';
            str+='<td>'+d+'</td></tr>';
        });
        $('#confirmRemoveGroupDialog').modal('show').find('.to-be-removed-groups').append(str);
    };
    $scope.popRemovePolicyDialog= function (row) {
        $('.to-be-removed-policy').html('');
        selectedRemovePolicyItems=[];
        var str='';
        if(row){
            selectedRemovePolicyItems.push(row);
        }
        else{
            selectedRemovePolicyItems = $('#policy-table').bootstrapTable('getSelections');
        }
        $.each(selectedRemovePolicyItems, function (i, item) {
            str+='<tr><td>'+item.data+'</td>';
            var d='';
            var t= _.find($scope.policies,function (item3) {
                return item3.id==item.data;
            });
            if(t) d= t.name;
            else d='-';
            str+='<td>'+d+'</td></tr>';
        });
        $('#confirmRemovePolicyDialog').modal('show').find('.to-be-removed-policy').append(str);
    };
    $scope.confirmRemoveGroup= function () {
        var request={
            method: 'POST',
            url: G.baseUrl+'/api/auth/role/update'
        };
        var b = $scope.query.role;
        var remaining=_.filter(b['data-resources'], function (item) {
            return _.find(selectedRemoveGroupItems, function (item2) {
                    return item.data==item2.data && item['resource-type'].toLowerCase()=='group';
                })==undefined;
        });
        b['data-resources']=remaining;
        request.data=b;
        $scope.processRequest(request,$('#operationResultDialog'),'更新 User','更新成功！');
    };
    $scope.confirmRemovePolicy= function () {
        var request={
            method: 'POST',
            url: G.baseUrl+'/api/auth/role/update'
        };
        var b = $scope.query.role;
        var remaining=_.filter(b['data-resources'], function (item) {
            return _.find(selectedRemovePolicyItems, function (item2) {
                    return item.data==item2.data && item['resource-type'].toLowerCase()=='policy';
                })==undefined;
        });
        b['data-resources']=remaining;
        request.data=b;
        $scope.processRequest(request,$('#operationResultDialog'),'更新 User','更新成功！');
    };
    $scope.popEditGroupDialog= function (row) {
        $scope.query.group.id=undefined;
        selectedEditGroupItems=[];
        $('.check-all-groups-bt').prop('checked',false);
        $('#newGroupDialog').modal('show').find('.add-group-alert').html('');
        $('.add-group-auth-alert').html('');
        $("#group-role-selector-table").bootstrapTable("uncheckAll");
        $('#groupIdSelector_value').val('');
        if(row){
            $('#groupIdSelector_value').val(row.data.toString());
            var selectedRowsArray=[];
            var item=row;
            $scope.query.group.id=item.data;
            if(item.data=='*'){
                $('.check-all-groups-bt').prop('checked',true);
            }
            if(item.R){
                selectedRowsArray.push(item.R.type);
            }
            if(item.D){
                selectedRowsArray.push(item.D.type);
            }
            if(item.A){
                selectedRowsArray.push(item.A.type);
            }
            if(item.U){
                selectedRowsArray.push(item.U.type);
            }
            if(item.E){
                selectedRowsArray.push(item.E.type);
            }
            if(item.P){
                selectedRowsArray.push(item.P.type);
            }
            if(item.S){
                selectedRowsArray.push(item.S.type);
            }
            if(item.M){
                selectedRowsArray.push(item.M.type);
            }
            if(item.L){
                selectedRowsArray.push(item.L.type);
            }
            if(item.H){
                selectedRowsArray.push(item.H.type);
            }

            $("#group-role-selector-table").bootstrapTable("checkBy", {field:"id", values:selectedRowsArray})
            selectedEditGroupItems=[row];
        }
    };
    $scope.popEditPolicyDialog= function (row) {
        $scope.query.policy.id=undefined;
        selectedEditPolicyItems=[];
        $('.check-all-policy-bt').prop('checked',false);
        $('#newPolicyDialog').modal('show').find('.add-policy-alert').html('');
        $('.add-policy-auth-alert').html('');
        $("#policy-role-selector-table").bootstrapTable("uncheckAll");
        $('#policyIdSelector_value').val('');
        if(row){
            $('#policyIdSelector_value').val(row.data.toString());
            var selectedRowsArray=[];
            var item=row;
            $scope.query.policy.id=item.data;
            if(item.data=='*'){
                $('.check-all-policy-bt').prop('checked',true);
            }
            if(item.R){
                selectedRowsArray.push(item.R.type);
            }
            if(item.D){
                selectedRowsArray.push(item.D.type);
            }
            if(item.A){
                selectedRowsArray.push(item.A.type);
            }
            if(item.U){
                selectedRowsArray.push(item.U.type);
            }
            if(item.E){
                selectedRowsArray.push(item.E.type);
            }
            if(item.P){
                selectedRowsArray.push(item.P.type);
            }

            if(item.F){
                selectedRowsArray.push(item.F.type);
            }

            $("#policy-role-selector-table").bootstrapTable("checkBy", {field:"id", values:selectedRowsArray})
            selectedEditPolicyItems=[row];
        }
    };
    $scope.getGroupSelectedItems= function () {
        var result=[];
        var slbRoles =  $('#group-role-selector-table').bootstrapTable('getSelections');
        $.each(slbRoles, function (i, item) {
            result.push({type:item.id});
        });
        return result;
    };
    $scope.getPolicySelectedItems= function () {
        var result=[];
        var slbRoles =  $('#policy-role-selector-table').bootstrapTable('getSelections');
        $.each(slbRoles, function (i, item) {
            result.push({type:item.id});
        });
        return result;
    };
    $scope.checkAllGroups= function () {
        var c =$('.check-all-groups-bt').is(':checked');
        if(c){
            $scope.query.group.id='*';
            $('#groupIdSelector_value').val('*');
        }
    };
    $scope.checkAllPloicy= function () {
        var c =$('.check-all-policy-bt').is(':checked');
        if(c){
            $scope.query.policy.id='*';
            $('#policyIdSelector_value').val('*');
        }
    };

    // Conf Management Area
    var selectedRemoveConfItems=[];
    var selectedEditConfItems=[];
    window.ConfOperationEvent={
        'click .deleteConfOp': function (e, value, row) {
            $scope.popRemoveConfDialog(row);
        },
        'click .updateConfOp': function (e, value, row) {
            $scope.popEditConfDialog(row);
        }
    };
    window.ConfChangeRightEvent={
        'click .confOp': function(e,value,row){
            $scope.toggleConfRightClick(row.data,"MAINTENANCE",value);
        }
    };
    $scope.toggleConfRightClick= function (id, op,v) {
        var request={
            method: 'POST',
            url: '/api/auth/role/update'
        };
        var c=v!=undefined;
        var b = $scope.query.role;
        var r = _.findIndex(b['data-resources'], function (item) {
            return item.data==id && item['resource-type'].toLowerCase()=='conf';
        });
        if(r!=-1){
            var o = b['data-resources'][r].operations;
            var i = _.findIndex(o, function (item) {
                return item.type.toLowerCase()==op.toLowerCase();
            });

            // Splice those empty typed element
            var j = _.findIndex(o, function (item) {
                return item.type=='';
            });
            if(j!=-1) o.splice(j,1);

            if(c){
                // Turn off the right
                o.splice(i,1);
            }else{
                // Turn on the right
                o.push({type:op});
            }

            // Save the changes
            request.data=b;
            var txt=c?"成功收回 "+op+" 权限":"成功赋予 "+op+" 权限";
            $scope.processRequest(request,$('#operationResultDialog'),'更新User',txt);
        }
    };
    $scope.addNewConfClick= function () {
        $scope.popEditConfDialog();
    };
    $scope.batchRemoveConfClick= function (){
        $scope.popRemoveConfDialog();
    };
    $scope.saveNewConfClick= function (){
        var request={
            method: 'POST',
            url: G.baseUrl+'/api/auth/role/update'
        };
        if($scope.query.conf.id){
            if($scope.getConfSelectedItems().length==0){
                $('.add-conf-auth-alert').html('(请至少选择一种权限!)');
                return;
            }
            $('#newConfDialog').modal('hide');
            var b =$scope.query.role;
            if(selectedEditConfItems.length>0){
                var r = _.findIndex(b['data-resources'], function (item) {
                    return item.data==selectedEditConfItems[0].data && item['resource-type'].toLowerCase()=='conf';
                });
                b['data-resources'][r]=={};
                b['data-resources'][r]={
                    data: $scope.query.conf.id,
                    'resource-type':'Conf',
                    operations:$scope.getConfSelectedItems()
                };
                selectedEditConfItems=[];
            }
            else{
                if(!b['data-resources']){
                    b['data-resources']=[];
                }

                b['data-resources'].push(
                    {
                        'resource-type':'Conf',
                        data: $scope.query.conf.id,
                        operations: $scope.getConfSelectedItems()
                    }
                );
            }

            request.data=b;
            $scope.processRequest(request,$('#operationResultDialog'),'更新User','更新成功！');
        }
        else{
            $('.add-conf-alert').html('<p  style="color:red; margin-left: 200px">请选择要关联的Conf ID</p>');
        }
    };
    $scope.popRemoveConfDialog= function (row) {
        $('.to-be-removed-confs').html('');
        selectedRemoveConfItems=[];
        var str='';
        if(row){
            selectedRemoveConfItems.push(row);
        }
        else{
            selectedRemoveConfItems = $('#conf-table').bootstrapTable('getSelections');
        }
        $.each(selectedRemoveConfItems, function (i, item) {
            str+='<tr><td>'+item.data+'</td>';
        });
        $('#confirmRemoveConfDialog').modal('show').find('.to-be-removed-confs').append(str);
    };
    $scope.confirmRemoveConf= function () {
        var request={
            method: 'POST',
            url: G.baseUrl+'/api/auth/role/update'
        };
        var b = $scope.query.role;
        var remaining=_.filter(b['data-resources'], function (item) {
            return _.find(selectedRemoveConfItems, function (item2) {
                    return item.data==item2.data && item['resource-type'].toLowerCase()=='conf';
                })==undefined;
        });
        b['data-resources']=remaining;
        request.data=b;
        $scope.processRequest(request,$('#operationResultDialog'),'更新 User','更新成功！');
    };
    $scope.popEditConfDialog= function (row) {
        $scope.query.conf.id=undefined;
        selectedEditConfItems=[];
        $('#newConfDialog').modal('show').find('.add-conf-alert').html('');
        $('.add-conf-auth-alert').html('');
        if(row){
            var selectedRowsArray=[];
            var item=row;
            $scope.query.conf.id=item.data;

            if(item.M){
                selectedRowsArray.push(item.M.type);
            }

            $("#conf-role-selector-table").bootstrapTable("checkBy", {field:"id", values:selectedRowsArray})
            selectedEditConfItems=[row];
        }
    };
    $scope.getConfSelectedItems= function () {
        var result=[];
        var slbRoles =  $('#conf-role-selector-table').bootstrapTable('getSelections');
        $.each(slbRoles, function (i, item) {
            result.push({type:item.id});
        });
        return result;
    };

    // Clean Management Area
    var selectedRemoveCleanItems=[];
    var selectedEditCleanItems=[];
    window.CleanOperationEvent={
        'click .deleteCleanOp': function (e, value, row) {
            $scope.popRemoveCleanDialog(row);
        },
        'click .updateCleanOp': function (e, value, row) {
            $scope.popEditCleanDialog(row);
        }
    };
    window.CleanChangeRightEvent={
        'click .cleanOp': function(e,value,row){
            $scope.toggleCleanRightClick(row.data,"MAINTENANCE",value);
        }
    };
    $scope.toggleCleanRightClick= function (id, op,v) {
        var request={
            method: 'POST',
            url: G.baseUrl+'/api/auth/role/update'
        };
        var c=v!=undefined;
        var b = $scope.query.role;
        var r = _.findIndex(b['data-resources'], function (item) {
            return item.data==id && item['resource-type'].toLowerCase()=='clean';
        });
        if(r!=-1){
            var o = b['data-resources'][r].operations;
            var i = _.findIndex(o, function (item) {
                return item.type.toLowerCase()==op.toLowerCase();
            });

            // Splice those empty typed element
            var j = _.findIndex(o, function (item) {
                return item.type=='';
            });
            if(j!=-1) o.splice(j,1);

            if(c){
                // Turn off the right
                o.splice(i,1);
            }else{
                // Turn on the right
                o.push({type:op});
            }

            // Save the changes
            request.data=b;
            var txt=c?"成功收回 "+op+" 权限":"成功赋予 "+op+" 权限";
            $scope.processRequest(request,$('#operationResultDialog'),'更新User',txt);
        }
    };
    $scope.addNewCleanClick= function () {
        $scope.popEditCleanDialog();
    };
    $scope.batchRemoveCleanClick= function (){
        $scope.popRemoveCleanDialog();
    };
    $scope.saveNewCleanClick= function (){
        var request={
            method: 'POST',
            url: G.baseUrl+'/api/auth/role/update'
        };
        if($scope.query.clean.id){
            if($scope.getCleanSelectedItems().length==0){
                $('.add-clean-auth-alert').html('(请至少选择一种权限!)');
                return;
            }
            $('#newCleanDialog').modal('hide');
            var b =$scope.query.role;
            if(selectedEditCleanItems.length>0){
                var r = _.findIndex(b['data-resources'], function (item) {
                    return item.data==selectedEditCleanItems[0].data && item['resource-type'].toLowerCase()=='clean';
                });
                b['data-resources'][r]=={};
                b['data-resources'][r]={
                    data: $scope.query.clean.id,
                    'resource-type':'Clean',
                    operations:$scope.getCleanSelectedItems()
                };
                selectedEditCleanItems=[];
            }
            else{
                if(!b['data-resources']){
                    b['data-resources']=[];
                }

                b['data-resources'].push(
                    {
                        'resource-type':'Clean',
                        data: $scope.query.clean.id,
                        operations: $scope.getCleanSelectedItems()
                    }
                );
            }

            request.data=b;
            $scope.processRequest(request,$('#operationResultDialog'),'更新User','更新成功！');
        }
        else{
            $('.add-clean-alert').html('<p  style="color:red; margin-left: 200px">请选择要关联的Clean ID</p>');
        }
    };
    $scope.popRemoveCleanDialog= function (row) {
        $('.to-be-removed-cleans').html('');
        selectedRemoveCleanItems=[];
        var str='';
        if(row){
            selectedRemoveCleanItems.push(row);
        }
        else{
            selectedRemoveCleanItems = $('#clean-table').bootstrapTable('getSelections');
        }
        $.each(selectedRemoveCleanItems, function (i, item) {
            str+='<tr><td>'+item.data+'</td>';
        });
        $('#confirmRemoveCleanDialog').modal('show').find('.to-be-removed-cleans').append(str);
    };
    $scope.confirmRemoveClean= function () {
        var request={
            method: 'POST',
            url: G.baseUrl+'/api/auth/role/update'
        };
        var b = $scope.query.role;
        var remaining=_.filter(b['data-resources'], function (item) {
            return _.find(selectedRemoveCleanItems, function (item2) {
                    return item.data==item2.data && item['resource-type'].toLowerCase()=='clean';
                })==undefined;
        });
        b['data-resources']=remaining;
        request.data=b;
        $scope.processRequest(request,$('#operationResultDialog'),'更新 User','更新成功！');
    };
    $scope.popEditCleanDialog= function (row) {
        $scope.query.clean.id=undefined;
        selectedEditCleanItems=[];
        $('#newCleanDialog').modal('show').find('.add-clean-alert').html('');
        $('.add-clean-auth-alert').html('');
        if(row){
            var selectedRowsArray=[];
            var item=row;
            $scope.query.clean.id=item.data;

            if(item.M){
                selectedRowsArray.push(item.M.type);
            }

            $("#clean-role-selector-table").bootstrapTable("checkBy", {field:"id", values:selectedRowsArray})
            selectedEditCleanItems=[row];
        }
    };
    $scope.getCleanSelectedItems= function () {
        var result=[];
        var slbRoles =  $('#clean-role-selector-table').bootstrapTable('getSelections');
        $.each(slbRoles, function (i, item) {
            result.push({type:item.id});
        });
        return result;
    };

    // Sync Management Area
    var selectedRemoveSyncItems=[];
    var selectedEditSyncItems=[];
    window.SyncOperationEvent={
        'click .deleteSyncOp': function (e, value, row) {
            $scope.popRemoveSyncDialog(row);
        },
        'click .updateSyncOp': function (e, value, row) {
            $scope.popEditSyncDialog(row);
        }
    };
    window.SyncChangeRightEvent={
        'click .syncOp': function(e,value,row){
            $scope.toggleSyncRightClick(row.data,"MAINTENANCE",value);
        }
    };
    $scope.toggleSyncRightClick= function (id, op,v) {
        var request={
            method: 'POST',
            url: '/api/auth/role/update'
        };
        var c=v!=undefined;
        var b = $scope.query.role;
        var r = _.findIndex(b['data-resources'], function (item) {
            return item.data==id && item['resource-type'].toLowerCase()=='syncerror';
        });
        if(r!=-1){
            var o = b['data-resources'][r].operations;
            var i = _.findIndex(o, function (item) {
                return item.type.toLowerCase()==op.toLowerCase();
            });

            // Splice those empty typed element
            var j = _.findIndex(o, function (item) {
                return item.type=='';
            });
            if(j!=-1) o.splice(j,1);

            if(c){
                // Turn off the right
                o.splice(i,1);
            }else{
                // Turn on the right
                o.push({type:op});
            }

            // Save the changes
            request.data=b;
            var txt=c?"成功收回 "+op+" 权限":"成功赋予 "+op+" 权限";
            $scope.processRequest(request,$('#operationResultDialog'),'更新User',txt);
        }
    };
    $scope.addNewSyncClick= function () {
        $scope.popEditSyncDialog();
    };
    $scope.batchRemoveSyncClick= function (){
        $scope.popRemoveSyncDialog();
    };
    $scope.saveNewSyncClick= function (){
        var request={
            method: 'POST',
            url: G.baseUrl+'/api/auth/role/update'
        };
        if($scope.query.clean.id){
            if($scope.getSyncSelectedItems().length==0){
                $('.add-sync-auth-alert').html('(请至少选择一种权限!)');
                return;
            }
            $('#newSyncDialog').modal('hide');
            var b =$scope.query.role;
            if(selectedEditSyncItems.length>0){
                var r = _.findIndex(b['data-resources'], function (item) {
                    return item.data==selectedEditSyncItems[0].data && item['resource-type'].toLowerCase()=='syncerror';
                });
                b['data-resources'][r]=={};
                b['data-resources'][r]={
                    data: $scope.query.sync.id,
                    'resource-type':'SyncError',
                    operations:$scope.getSyncSelectedItems()
                };
                selectedEditSyncItems=[];
            }
            else{
                if(!b['data-resources']){
                    b['data-resources']=[];
                }

                b['data-resources'].push(
                    {
                        'resource-type':'SyncError',
                        data: $scope.query.sync.id,
                        operations: $scope.getSyncSelectedItems()
                    }
                );
            }

            request.data=b;
            $scope.processRequest(request,$('#operationResultDialog'),'更新User','更新成功！');
        }
        else{
            $('.add-sync-alert').html('<p  style="color:red; margin-left: 200px">请选择要关联的 Sync ID</p>');
        }
    };
    $scope.popRemoveSyncDialog= function (row) {
        $('.to-be-removed-syncs').html('');
        selectedRemoveSyncItems=[];
        var str='';
        if(row){
            selectedRemoveSyncItems.push(row);
        }
        else{
            selectedRemoveSyncItems = $('#sync-table').bootstrapTable('getSelections');
        }
        $.each(selectedRemoveSyncItems, function (i, item) {
            str+='<tr><td>'+item.data+'</td>';
        });
        $('#confirmRemoveSyncDialog').modal('show').find('.to-be-removed-syncs').append(str);
    };
    $scope.confirmRemoveSync= function () {
        var request={
            method: 'POST',
            url: G.baseUrl+'/api/auth/role/update'
        };
        var b = $scope.query.role;
        var remaining=_.filter(b['data-resources'], function (item) {
            return _.find(selectedRemoveSyncItems, function (item2) {
                    return item.data==item2.data && item['resource-type'].toLowerCase()=='syncerror';
                })==undefined;
        });
        b['data-resources']=remaining;
        request.data=b;
        $scope.processRequest(request,$('#operationResultDialog'),'更新 User','更新成功！');
    };
    $scope.popEditSyncDialog= function (row) {
        $scope.query.sync.id=undefined;
        selectedEditSyncItems=[];
        $('#newSyncDialog').modal('show').find('.add-sync-alert').html('');
        $('.add-sync-auth-alert').html('');
        if(row){
            var selectedRowsArray=[];
            var item=row;
            $scope.query.sync.id=item.data;

            if(item.M){
                selectedRowsArray.push(item.M.type);
            }

            $("#sync-role-selector-table").bootstrapTable("checkBy", {field:"id", values:selectedRowsArray})
            selectedEditSyncItems=[row];
        }
    };
    $scope.getSyncSelectedItems= function () {
        var result=[];
        var slbRoles =  $('#sync-role-selector-table').bootstrapTable('getSelections');
        $.each(slbRoles, function (i, item) {
            result.push({type:item.id});
        });
        return result;
    };

    // Auth Management Area
    var selectedRemoveAuthItems=[];
    var selectedEditAuthItems=[];
    window.AuthOperationEvent={
        'click .deleteAuthOp': function (e, value, row) {
            $scope.popRemoveAuthDialog(row);
        },
        'click .updateAuthOp': function (e, value, row) {
            $scope.popEditAuthDialog(row);
        }
    };
    window.AuthChangeRightEvent={
        'click .authOp': function(e,value,row){
            $scope.toggleAuthRightClick(row.data,"AUTH",value);
        }
    };
    $scope.toggleAuthRightClick= function (id, op,v) {
        var request={
            method: 'POST',
            url: '/api/auth/role/update'
        };
        var c=v!=undefined;
        var b = $scope.query.role;
        var r = _.findIndex(b['data-resources'], function (item) {
            return item.data==id && item['resource-type'].toLowerCase()=='auth';
        });
        if(r!=-1){
            var o = b['data-resources'][r].operations;
            var i = _.findIndex(o, function (item) {
                return item.type.toLowerCase()==op.toLowerCase();
            });

            // Splice those empty typed element
            var j = _.findIndex(o, function (item) {
                return item.type=='';
            });
            if(j!=-1) o.splice(j,1);

            if(c){
                // Turn off the right
                o.splice(i,1);
            }else{
                // Turn on the right
                o.push({type:op});
            }

            // Save the changes
            request.data=b;
            var txt=c?"成功收回 "+op+" 权限":"成功赋予 "+op+" 权限";
            $scope.processRequest(request,$('#operationResultDialog'),'更新User',txt);
        }
    };
    $scope.addNewAuthClick= function () {
        $scope.popEditAuthDialog();
    };
    $scope.batchRemoveAuthClick= function (){
        $scope.popRemoveAuthDialog();
    };
    $scope.saveNewAuthClick= function (){
        var request={
            method: 'POST',
            url: G.baseUrl+'/api/auth/role/update'
        };
        if($scope.query.auth.id){
            if($scope.getAuthSelectedItems().length==0){
                $('.add-auth-auth-alert').html('(请至少选择一种权限!)');
                return;
            }
            $('#newAuthDialog').modal('hide');
            var b =$scope.query.role;
            if(selectedEditAuthItems.length>0){
                var r = _.findIndex(b['data-resources'], function (item) {
                    return item.data==selectedEditAuthItems[0].data;
                });
                b['data-resources'][r]=={};
                b['data-resources'][r]={
                    data: $scope.query.auth.id,
                    'resource-type':'Auth',
                    operations:$scope.getAuthSelectedItems()
                };
                selectedEditAuthItems=[];
            }
            else{
                if(!b['data-resources']){
                    b['data-resources']=[];
                }

                b['data-resources'].push(
                    {
                        'resource-type':'Auth',
                        data: $scope.query.auth.id,
                        operations: $scope.getAuthSelectedItems()
                    }
                );
            }

            request.data=b;
            $scope.processRequest(request,$('#operationResultDialog'),'更新User','更新成功！');
        }
        else{
            $('.add-auth-alert').html('<p  style="color:red; margin-left: 200px">请选择要关联的 Auth ID</p>');
        }
    };
    $scope.popRemoveAuthDialog= function (row) {
        $('.to-be-removed-auths').html('');
        selectedRemoveAuthItems=[];
        var str='';
        if(row){
            selectedRemoveAuthItems.push(row);
        }
        else{
            selectedRemoveAuthItems = $('#auth-table').bootstrapTable('getSelections');
        }
        $.each(selectedRemoveAuthItems, function (i, item) {
            str+='<tr><td>'+item.data+'</td>';
        });
        $('#confirmRemoveAuthDialog').modal('show').find('.to-be-removed-auths').append(str);
    };
    $scope.confirmRemoveAuth= function () {
        var request={
            method: 'POST',
            url: G.baseUrl+'/api/auth/role/update'
        };
        var b = $scope.query.role;
        var remaining=_.filter(b['data-resources'], function (item) {
            return _.find(selectedRemoveAuthItems, function (item2) {
                    return item.data==item2.data && item['resource-type'].toLowerCase()=='auth';
                })==undefined;
        });
        b['data-resources']=remaining;
        request.data=b;
        $scope.processRequest(request,$('#operationResultDialog'),'更新 User','更新成功！');
    };
    $scope.popEditAuthDialog= function (row) {
        $scope.query.auth.id=undefined;
        selectedEditAuthItems=[];
        $('#newAuthDialog').modal('show').find('.add-auth-alert').html('');
        $('.add-auth-auth-alert').html('');
        if(row){
            var selectedRowsArray=[];
            var item=row;
            $scope.query.auth.id=item.data;

            if(item.M){
                selectedRowsArray.push(item.M.type);
            }

            $("#auth-role-selector-table").bootstrapTable("checkBy", {field:"id", values:selectedRowsArray})
            selectedEditAuthItems=[row];
        }
    };
    $scope.getAuthSelectedItems= function () {
        var result=[];
        var slbRoles =  $('#auth-role-selector-table').bootstrapTable('getSelections');
        $.each(slbRoles, function (i, item) {
            result.push({type:item.id});
        });
        return result;
    };

    // Lock Management Area
    var selectedRemoveLockItems=[];
    var selectedEditLockItems=[];
    window.LockOperationEvent={
        'click .deleteLockOp': function (e, value, row) {
            $scope.popRemoveLockDialog(row);
        },
        'click .updateLockOp': function (e, value, row) {
            $scope.popEditLockDialog(row);
        }
    };
    window.LockChangeRightEvent={
        'click .lockOp': function(e,value,row){
            $scope.toggleLockRightClick(row.data,"MAINTENANCE",value);
        }
    };
    $scope.toggleLockRightClick= function (id, op,v) {
        var request={
            method: 'POST',
            url: '/api/auth/role/update'
        };
        var c=v!=undefined;
        var b = $scope.query.role;
        var r = _.findIndex(b['data-resources'], function (item) {
            return item.data==id && item['resource-type'].toLowerCase()=='lock';
        });
        if(r!=-1){
            var o = b['data-resources'][r].operations;
            var i = _.findIndex(o, function (item) {
                return item.type.toLowerCase()==op.toLowerCase();
            });

            // Splice those empty typed element
            var j = _.findIndex(o, function (item) {
                return item.type=='';
            });
            if(j!=-1) o.splice(j,1);

            if(c){
                // Turn off the right
                o.splice(i,1);
            }else{
                // Turn on the right
                o.push({type:op});
            }

            // Save the changes
            request.data=b;
            var txt=c?"成功收回 "+op+" 权限":"成功赋予 "+op+" 权限";
            $scope.processRequest(request,$('#operationResultDialog'),'更新User',txt);
        }
    };
    $scope.addNewLockClick= function () {
        $scope.popEditLockDialog();
    };
    $scope.batchRemoveLockClick= function (){
        $scope.popRemoveLockDialog();
    };
    $scope.saveNewLockClick= function (){
        var request={
            method: 'POST',
            url: G.baseUrl+'/api/auth/role/update'
        };
        if($scope.query.lock.id){
            if($scope.getLockSelectedItems().length==0){
                $('.add-lock-auth-alert').html('(请至少选择一种权限!)');
                return;
            }
            $('#newLockDialog').modal('hide');
            var b =$scope.query.role;
            if(selectedEditLockItems.length>0){
                var r = _.findIndex(b['data-resources'], function (item) {
                    return item.data==selectedEditLockItems[0].data && item['resource-type'].toLowerCase()=='lock';
                });
                b['data-resources'][r]=={};
                b['data-resources'][r]={
                    data: $scope.query.lock.id,
                    'resource-type':'Lock',
                    operations:$scope.getLockSelectedItems()
                };
                selectedEditLockItems=[];
            }
            else{
                if(!b['data-resources']){
                    b['data-resources']=[];
                }

                b['data-resources'].push(
                    {
                        'resource-type':'Lock',
                        data: $scope.query.lock.id,
                        operations: $scope.getLockSelectedItems()
                    }
                );
            }

            request.data=b;
            $scope.processRequest(request,$('#operationResultDialog'),'更新User','更新成功！');
        }
        else{
            $('.add-lock-alert').html('<p  style="color:red; margin-left: 200px">请选择要关联的 Lock ID</p>');
        }
    };
    $scope.popRemoveLockDialog= function (row) {
        $('.to-be-removed-locks').html('');
        selectedRemoveLockItems=[];
        var str='';
        if(row){
            selectedRemoveLockItems.push(row);
        }
        else{
            selectedRemoveLockItems = $('#lock-table').bootstrapTable('getSelections');
        }
        $.each(selectedRemoveLockItems, function (i, item) {
            str+='<tr><td>'+item.data+'</td>';
        });
        $('#confirmRemoveLockDialog').modal('show').find('.to-be-removed-locks').append(str);
    };
    $scope.confirmRemoveLock= function () {
        var request={
            method: 'POST',
            url: G.baseUrl+'/api/auth/role/update'
        };
        var b = $scope.query.role;
        var remaining=_.filter(b['data-resources'], function (item) {
            return _.find(selectedRemoveLockItems, function (item2) {
                    return item.data==item2.data && item['resource-type'].toLowerCase()=='lock';
                })==undefined;
        });
        b['data-resources']=remaining;
        request.data=b;
        $scope.processRequest(request,$('#operationResultDialog'),'更新 User','更新成功！');
    };
    $scope.popEditLockDialog= function (row) {
        $scope.query.lock.id=undefined;
        selectedEditLockItems=[];
        $('#newLockDialog').modal('show').find('.add-lock-alert').html('');
        $('.add-lock-auth-alert').html('');
        if(row){
            var selectedRowsArray=[];
            var item=row;
            $scope.query.lock.id=item.data;

            if(item.M){
                selectedRowsArray.push(item.M.type);
            }

            $("#lock-role-selector-table").bootstrapTable("checkBy", {field:"id", values:selectedRowsArray})
            selectedEditLockItems=[row];
        }
    };
    $scope.getLockSelectedItems= function () {
        var result=[];
        var slbRoles =  $('#lock-role-selector-table').bootstrapTable('getSelections');
        $.each(slbRoles, function (i, item) {
            result.push({type:item.id});
        });
        return result;
    };

    // IP Management Area
    var selectedRemoveIpItems=[];
    var selectedEditIpItems=[];
    window.IPOperationEvent={
        'click .deleteIpOp': function (e, value, row) {
            $scope.popRemoveIpDialog(row);
        },
        'click .updateIpOp': function (e, value, row) {
            $scope.popEditIpDialog(row);
        }
    };
    window.IPChangeRightEvent={
        'click .ipOp': function(e,value,row){
            $scope.toggleIpRightClick(row.data,"OP_SERVER",value);
        }
    };
    $scope.toggleIpRightClick= function (id, op,v) {
        var request={
            method: 'POST',
            url: '/api/auth/role/update'
        };
        var c=v!=undefined;
        var b = $scope.query.role;
        var r = _.findIndex(b['data-resources'], function (item) {
            return item.data==id && item['resource-type'].toLowerCase()=='ip';
        });
        if(r!=-1){
            var o = b['data-resources'][r].operations;
            var i = _.findIndex(o, function (item) {
                return item.type.toLowerCase()==op.toLowerCase();
            });

            // Splice those empty typed element
            var j = _.findIndex(o, function (item) {
                return item.type=='';
            });
            if(j!=-1) o.splice(j,1);

            if(c){
                // Turn off the right
                o.splice(i,1);
            }else{
                // Turn on the right
                o.push({type:op});
            }

            // Save the changes
            request.data=b;
            var txt=c?"成功收回 "+op+" 权限":"成功赋予 "+op+" 权限";
            $scope.processRequest(request,$('#operationResultDialog'),'更新User',txt);
        }
    };
    $scope.addNewIpClick= function () {
        $scope.popEditIpDialog();
    };
    $scope.batchRemoveIpClick= function (){
        $scope.popRemoveIpDialog();
    };
    $scope.saveNewIpClick= function (){
        var request={
            method: 'POST',
            url: G.baseUrl+'/api/auth/role/update'
        };
        if($scope.query.ip.id){
            if($scope.getIpSelectedItems().length==0){
                $('.add-ip-auth-alert').html('(请至少选择一种权限!)');
                return;
            }
            $('#newIpDialog').modal('hide');
            var b =$scope.query.role;
            if(selectedEditIpItems.length>0){
                var r = _.findIndex(b['data-resources'], function (item) {
                    return item.data==selectedEditIpItems[0].data && item['resource-type'].toLowerCase()=='ip';
                });
                b['data-resources'][r]=={};
                b['data-resources'][r]={
                    data: $scope.query.ip.id,
                    'resource-type':'Ip',
                    operations:$scope.getIpSelectedItems()
                };
                selectedEditIpItems=[];
            }
            else{
                if(!b['data-resources']){
                    b['data-resources']=[];
                }

                b['data-resources'].push(
                    {
                        'resource-type':'Ip',
                        data: $scope.query.ip.id,
                        operations: $scope.getIpSelectedItems()
                    }
                );
            }

            request.data=b;
            $scope.processRequest(request,$('#operationResultDialog'),'更新User','更新成功！');
        }
        else{
            $('.add-ip-alert').html('<p  style="color:red; margin-left: 200px">请选择要关联的 IP/Subnet</p>');
        }
    };
    $scope.popRemoveIpDialog= function (row) {
        $('.to-be-removed-ips').html('');
        selectedRemoveIpItems=[];
        var str='';
        if(row){
            selectedRemoveIpItems.push(row);
        }
        else{
            selectedRemoveIpItems = $('#ip-table').bootstrapTable('getSelections');
        }
        $.each(selectedRemoveIpItems, function (i, item) {
            str+='<tr><td>'+item.data+'</td>';
        });
        $('#confirmRemoveIpDialog').modal('show').find('.to-be-removed-ips').append(str);
    };
    $scope.confirmRemoveIp= function () {
        var request={
            method: 'POST',
            url: G.baseUrl+'/api/auth/role/update'
        };
        var b = $scope.query.role;
        var remaining=_.filter(b['data-resources'], function (item) {
            return _.find(selectedRemoveIpItems, function (item2) {
                    return item.data==item2.data && item['resource-type'].toLowerCase()=='ip';
                })==undefined;
        });
        b['data-resources']=remaining;
        request.data=b;
        $scope.processRequest(request,$('#operationResultDialog'),'更新 User','更新成功！');
    };
    $scope.popEditIpDialog= function (row) {
        $scope.query.ip.id=undefined;
        selectedEditIpItems=[];
        $('#newIpDialog').modal('show').find('.add-ip-alert').html('');
        $('.add-ip-auth-alert').html('');
        if(row){
            var selectedRowsArray=[];
            var item=row;
            $scope.query.ip.id=item.data;

            if(item.M){
                selectedRowsArray.push(item.M.type);
            }

            $("#ip-role-selector-table").bootstrapTable("checkBy", {field:"id", values:selectedRowsArray})
            selectedEditIpItems=[row];
        }
    };
    $scope.getIpSelectedItems= function () {
        var result=[];
        var slbRoles =  $('#ip-role-selector-table').bootstrapTable('getSelections');
        $.each(slbRoles, function (i, item) {
            result.push({type:item.id});
        });
        return result;
    };

    // Progress dialog Area
    $scope.processRequest = function (request, confirmDialog, operationText, tooltipText) {
        confirmDialog.find(".modal-title").html(operationText);
        var msg = "";
        $http(request).success(
            function (res,code) {
                var errText='';
                if (code!=200) {
                    msg = res.message;
                    if(!msg){
                        msg=code;
                    }
                     errText = "<span class='fa fa-times'></span><span style='padding-left: 10px'>" + operationText + "失败</span>";
                    confirmDialog.modal('show').find(".modal-title").html(errText);
                    confirmDialog.modal('show').find(".modal-body").html("失败原因:" + msg);
                }
                else {
                    var successText = "<span class='fa fa-check'></span><span style='padding-left: 10px'>" + operationText + "成功</span>";
                    confirmDialog.modal('show').find(".modal-title").html(successText);
                    if (tooltipText)confirmDialog.modal('show').find(".modal-body").html(tooltipText);
                    if(tooltipText=='Role删除成功'){
                        setTimeout(function () {
                            window.location='/portal/roles#?env='+ G.env;
                        },1000);
                    }
                    else{
                        startTimer(confirmDialog);
                    }
                }
            }
        ).error(function (reject) {
                msg = reject.message;
                var errText = "<span class='fa fa-times'></span><span style='padding-left: 10px'>" + operationText + "失败</span>";
                confirmDialog.modal('show').find(".modal-title").html(errText);
                confirmDialog.modal('show').find(".modal-body").html("失败原因:" + msg);
            });
    };
    function startTimer(dialog){
        setTimeout(function () {
            dialog.find('.closeProgressWindowBt').click();
        },1000);
    }
    $('.closeProgressWindowBt').click(
        function (e) {
            var hashData = {};
            hashData.timeStamp = new Date().getTime();
            H.setData(hashData);
        }
    );

    // Loading Area
    $scope.initTable= function () {
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
                    formatter: function(value, row, index, field){
                        return '<a target="_blank" href="/portal/group#?env='+ G.env+'&groupId='+value+'">'+value+'</a>';
                    }
                },
                {
                    field: 'name',
                    title: 'Name',
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    formatter: function(value, row, index, field){
                        if(row.data!='*'){
                            value=value.substring(0, value.length/2)+'...';
                            return '<a target="_blank" href="/portal/group#?env='+ G.env+'&groupId='+row.data+'">'+value+'</a>';
                        }
                        else{
                            return '-';
                        }
                    }
                },
                {
                    field: 'R',
                    title: 'Read',
                    events:GroupChangeRightEvent,
                    align: 'center',
                    valign: 'middle',
                    formatter: function(value, row, index, field){
                        return $scope.formatter('groupReadOp',value);
                    }
                },
                {
                    field: 'U',
                    title: 'Update',
                    events:GroupChangeRightEvent,
                    align: 'center',
                    valign: 'middle',
                    formatter: function(value, row, index, field){
                        return $scope.formatter('groupUpdateOp',value);
                    }
                },
                {
                    field: 'E',
                    title: 'Delete',
                    events:GroupChangeRightEvent,
                    align: 'center',
                    valign: 'middle',
                    formatter: function(value, row, index, field){
                        return $scope.formatter('groupDeleteOp',value);
                    }
                },

                {
                    field: 'P',
                    title: 'PROPERTY',
                    events:GroupChangeRightEvent,
                    align: 'center',
                    valign: 'middle',
                    formatter: function(value, row, index, field){
                        return $scope.formatter('groupPropertyOp',value);
                    }
                },

                {
                    field: 'A',
                    title: 'Activate',
                    events:GroupChangeRightEvent,
                    align: 'center',
                    valign: 'middle',
                    formatter: function(value, row, index, field){
                        return $scope.formatter('groupActivateOp',value);
                    }
                },
                {
                    field: 'D',
                    title: 'Deactivate',
                    events:GroupChangeRightEvent,
                    align: 'center',
                    valign: 'middle',
                    formatter: function(value, row, index, field){
                        return $scope.formatter('groupDeactivateOp',value);
                    }
                },
                {
                    field: 'F',
                    title: 'Force',
                    events:GroupChangeRightEvent,
                    align: 'center',
                    valign: 'middle',
                    formatter: function(value, row, index, field){
                        return $scope.formatter('groupDeactivateOp',value);
                    }
                },

                {
                    field: 'operate',
                    width: '120px',
                    title: 'Operation',
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    events:GroupOperationEvent,
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
            data: $scope.role.policies,
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
                return  "<img class='loading-img' src='/static/img/loading_100.gif' /> 正在加载 Traffic Policy";
            },
            formatNoMatches: function () {
                return '<i class="fa status-alert fa-exclamation-triangle" aria-hidden="true"></i> 没有找到匹配的 Traffic Policy';
            }
        });
        $('#group-table').bootstrapTable({
            search: true,
            showRefresh: true,
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
                    formatter: function(value, row, index, field){
                        return '<a target="_blank" href="/portal/group#?env='+ G.env+'&groupId='+value+'">'+value+'</a>';
                    }
                },
                {
                    field: 'name',
                    title: 'Name',
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    formatter: function(value, row, index, field){
                        if(row.data!='*'){
                            value=value.substring(0, value.length/2)+'...';
                            return '<a target="_blank" href="/portal/group#?env='+ G.env+'&groupId='+row.data+'">'+value+'</a>';
                        }
                        else{
                            return '-';
                        }
                    }
                },
                {
                    field: 'R',
                    title: 'Read',
                    events:GroupChangeRightEvent,
                    align: 'center',
                    valign: 'middle',
                    formatter: function(value, row, index, field){
                        return $scope.formatter('groupReadOp',value);
                    }
                },
                {
                    field: 'U',
                    title: 'Update',
                    events:GroupChangeRightEvent,
                    align: 'center',
                    valign: 'middle',
                    formatter: function(value, row, index, field){
                        return $scope.formatter('groupUpdateOp',value);
                    }
                },
                {
                    field: 'E',
                    title: 'Delete',
                    events:GroupChangeRightEvent,
                    align: 'center',
                    valign: 'middle',
                    formatter: function(value, row, index, field){
                        return $scope.formatter('groupDeleteOp',value);
                    }
                },

                {
                    field: 'P',
                    title: 'PROPERTY',
                    events:GroupChangeRightEvent,
                    align: 'center',
                    valign: 'middle',
                    formatter: function(value, row, index, field){
                        return $scope.formatter('groupPropertyOp',value);
                    }
                },
                {
                    field: 'S',
                    title: 'SYNC',
                    events:GroupChangeRightEvent,
                    align: 'center',
                    valign: 'middle',
                    formatter: function(value, row, index, field){
                        return $scope.formatter('groupSyncOp',value);
                    }
                },

                {
                    field: 'A',
                    title: 'Activate',
                    events:GroupChangeRightEvent,
                    align: 'center',
                    valign: 'middle',
                    formatter: function(value, row, index, field){
                        return $scope.formatter('groupActivateOp',value);
                    }
                },
                {
                    field: 'D',
                    title: 'Deactivate',
                    events:GroupChangeRightEvent,
                    align: 'center',
                    valign: 'middle',
                    formatter: function(value, row, index, field){
                        return $scope.formatter('groupDeactivateOp',value);
                    }
                },

                {
                    field: 'M',
                    title: 'Member',
                    align: 'center',
                    events:GroupChangeRightEvent,
                    valign: 'middle',
                    formatter: function(value, row, index, field){
                        return $scope.formatter('groupMemberOp',value);
                    }
                },
                {
                    field: 'L',
                    title: 'Pull',
                    align: 'center',
                    valign: 'middle',
                    events:GroupChangeRightEvent,
                    formatter: function(value, row, index, field){
                        return $scope.formatter('groupPullOp',value);
                    }
                },
                {
                    field: 'H',
                    title: 'Raise',
                    align: 'center',
                    events:GroupChangeRightEvent,
                    valign: 'middle',
                    formatter: function(value, row, index, field){
                        return $scope.formatter('groupHealthOp',value);
                    }
                },
                {
                    field: 'operate',
                    width: '120px',
                    title: 'Operation',
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    events:GroupOperationEvent,
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
            data: $scope.role.groups,
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
                return  "<img class='loading-img' src='/static/img/loading_100.gif' /> 正在加载 Groups";
            },
            formatNoMatches: function () {
                return '<i class="fa status-alert fa-exclamation-triangle" aria-hidden="true"></i> 没有找到匹配的 Groups';
            }
        });
        $('#vs-table').bootstrapTable({
            toolbar: "#user-vs-toolbar",
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
                    formatter: function(value, row, index, field){
                        return '<a target="_blank" href="/portal/vs#?env='+ G.env+'&vsId='+value+'">'+value+'</a>';
                    }
                },
                {
                    field: 'name',
                    title: 'Name',
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    formatter: function(value, row, index, field){
                        if(row.data!='*'){
                            value=value.substring(0, value.length/2)+'...';
                            return '<a target="_blank" href="/portal/vs#?env='+ G.env+'&vsId='+row.data+'">'+value+'</a>';
                        }
                        else{
                            return '-';
                        }
                    }
                },
                {
                    field: 'R',
                    title: 'Read',
                    align: 'center',
                    events:VsChangeRightEvent,
                    valign: 'middle',
                    formatter: function(value, row, index, field){
                        return $scope.formatter('vsReadOp',value);
                    }
                },
                {
                    field: 'U',
                    title: 'Update',
                    events:VsChangeRightEvent,
                    align: 'center',
                    valign: 'middle',
                    formatter: function(value, row, index, field){
                        return $scope.formatter('vsUpdateOp',value);
                    }
                },
                {
                    field: 'E',
                    title: 'Delete',
                    events:VsChangeRightEvent,
                    align: 'center',
                    valign: 'middle',
                    formatter: function(value, row, index, field){
                        return $scope.formatter('vsDeleteOp',value);
                    }
                },
                {
                    field: 'A',
                    title: 'Activate',
                    events:VsChangeRightEvent,
                    align: 'center',
                    valign: 'middle',
                    formatter: function(value, row, index, field){
                        return $scope.formatter('vsActivateOp',value);
                    }
                },
                {
                    field: 'D',
                    title: 'Deactivate',
                    events:VsChangeRightEvent,
                    align: 'center',
                    valign: 'middle',
                    formatter: function(value, row, index, field){
                        return $scope.formatter('vsDeactivateOp',value);
                    }
                },
                {
                    field: 'P',
                    title: 'PROPERTY',
                    events:VsChangeRightEvent,
                    align: 'center',
                    valign: 'middle',
                    formatter: function(value, row, index, field){
                        return $scope.formatter('vsPropertyOp',value);
                    }
                },
                {
                    field: 'S',
                    title: 'SYNC',
                    events:VsChangeRightEvent,
                    align: 'center',
                    valign: 'middle',
                    formatter: function(value, row, index, field){
                        return $scope.formatter('vsSyncOp',value);
                    }
                },

                {
                    field: 'C',
                    title: 'Cert',
                    events:VsChangeRightEvent,
                    align: 'center',
                    valign: 'middle',
                    formatter: function(value, row, index, field){
                        return $scope.formatter('vsCertOp',value);
                    }
                },
                {
                    field: 'operate',
                    width: '120px',
                    title: 'Operation',
                    align: 'left',
                    valign: 'middle',
                    events:VSOperationEvent,
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
            search: true,
            showRefresh: true,
            showColumns: true,
            data: $scope.role.vses,
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
                return  "<img class='loading-img' src='/static/img/loading_100.gif' /> 正在加载 VSES";
            },
            formatNoMatches: function () {
                return '<i class="fa status-alert fa-exclamation-triangle" aria-hidden="true"></i> 没有找到匹配的 VSES';
            }
        });
        $('#slb-table').bootstrapTable({
            search: true,
            showRefresh: true,
            showColumns: true,
            toolbar: "#user-slb-toolbar",
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
                    formatter: function(value, row, index, field){
                        return '<a target="_blank" href="/portal/vs#?env='+ G.env+'&vsId='+value+'">'+value+'</a>';
                    }
                },
                {
                    field: 'name',
                    title: 'Name',
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    formatter: function(value, row, index, field){
                        if(row.data!='*'){
                            value=value.substring(0, value.length/2)+'...';
                            return '<a target="_blank" href="/portal/slb#?env='+ G.env+'&slbId='+row.data+'">'+value+'</a>';
                        }
                        else{
                            return '-';
                        }
                    }
                },
                {
                    field: 'R',
                    title: 'Read',
                    align: 'center',
                    valign: 'middle',
                    events:SlbChangeRightEvent,
                    formatter: function(value, row, index, field){
                        return $scope.formatter('slbReadOp',value);
                    }
                },
                {
                    field: 'U',
                    title: 'Update',
                    align: 'center',
                    events:SlbChangeRightEvent,
                    valign: 'middle',
                    formatter: function(value, row, index, field){
                        return $scope.formatter('slbUpdateOp',value);
                    }
                },
                {
                    field: 'E',
                    title: 'Delete',
                    events:SlbChangeRightEvent,
                    align: 'center',
                    valign: 'middle',
                    formatter: function(value, row, index, field){
                        return $scope.formatter('slbDeleteOp',value);
                    }
                },
                {
                    field: 'A',
                    title: 'Activate',
                    events:SlbChangeRightEvent,
                    align: 'center',
                    valign: 'middle',
                    formatter: function(value, row, index, field){
                        return $scope.formatter('slbActivateOp',value);
                    }
                },
                {
                    field: 'D',
                    title: 'Deactivate',
                    events:SlbChangeRightEvent,
                    align: 'center',
                    valign: 'middle',
                    formatter: function(value, row, index, field){
                        return $scope.formatter('slbDeactivateOp',value);
                    }
                },
                {
                    field: 'P',
                    title: 'PROPERTY',
                    events:SlbChangeRightEvent,
                    align: 'center',
                    valign: 'middle',
                    formatter: function(value, row, index, field){
                        return $scope.formatter('slbPropertyOp',value);
                    }
                },
                {
                    field: 'S',
                    title: 'SYNC',
                    events:SlbChangeRightEvent,
                    align: 'center',
                    valign: 'middle',
                    formatter: function(value, row, index, field){
                        return $scope.formatter('slbSyncOp',value);
                    }
                },

                {
                    field: 'I',
                    title: 'ADMIN',
                    align: 'center',
                    events:SlbChangeRightEvent,
                    valign: 'middle',
                    formatter: function(value, row, index, field){
                        return $scope.formatter('slbAdminOp',value);
                    }
                },
                {
                    field: 'W',
                    title: 'WAF',
                    events:SlbChangeRightEvent,
                    align: 'center',
                    valign: 'middle',
                    formatter: function(value, row, index, field){
                        return $scope.formatter('slbWafOp',value);
                    }
                },
                {
                    field: 'operate',
                    width: '120px',
                    title: 'Operation',
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    events:SlbOperationEvent,
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
            data: $scope.role.slbs,
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
                return  "<img class='loading-img' src='/static/img/loading_100.gif' /> 正在加载 SLBS";
            },
            formatNoMatches: function () {
                return '<i class="fa status-alert fa-exclamation-triangle" aria-hidden="true"></i> 没有找到匹配的 SLBS';
            }
        });
        $('#auth-table').bootstrapTable({
            search: true,
            showRefresh: true,
            showColumns: true,
            toolbar: "#user-auth-toolbar",
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
                    sortable: true
                },
                {
                    field: 'M',
                    title: 'Auth',
                    align: 'center',
                    width:'510px',
                    valign: 'middle',
                    events:AuthChangeRightEvent,
                    formatter: function(value, row, index, field){
                        return $scope.formatter('authOp',value);
                    }
                },
                {
                    field: 'operate',
                    width: '120px',
                    title: 'Operation',
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    events:AuthOperationEvent,
                    formatter: function () {
                        var p1 = "";
                        var str = '<button data-toggle="tooltip" type="button" class="btn btn-info deleteAuthOp  ' + p1 + '" title="删除" aria-label="Left Align"><span class="fa fa-minus"></span></button>';
                        str += '  <button data-toggle="tooltip" title="修改" type="button" class="btn btn-info updateAuthOp  ' + p1 + '" aria-label="Left Align">'
                        + '<span class="fa fa-edit"></span>' +
                        '</button>';
                        return str;
                    }
                }

            ], []],
            data: $scope.role.auth,
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
                return  "<img class='loading-img' src='/static/img/loading_100.gif' /> 正在加载 Auths";
            },
            formatNoMatches: function () {
                return '<i class="fa status-alert fa-exclamation-triangle" aria-hidden="true"></i> 没有找到匹配的 Auths';
            }
        });
        $('#clean-table').bootstrapTable({
            search: true,
            showRefresh: true,
            showColumns: true,
            toolbar: "#user-clean-toolbar",
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
                    sortable: true
                },
                {
                    width:'510px',

                    field: 'M',
                    title: 'Clean',
                    align: 'center',
                    valign: 'middle',
                    events:CleanChangeRightEvent,
                    formatter: function(value, row, index, field){
                        return $scope.formatter('cleanOp',value);
                    }
                },
                {
                    field: 'operate',
                    width: '120px',
                    title: 'Operation',
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    events:CleanOperationEvent,
                    formatter: function () {
                        var p1 = "";
                        var str = '<button data-toggle="tooltip" type="button" class="btn btn-info deleteCleanOp  ' + p1 + '" title="删除" aria-label="Left Align"><span class="fa fa-minus"></span></button>';
                        str += '  <button data-toggle="tooltip" title="修改" type="button" class="btn btn-info updateCleanOp  ' + p1 + '" aria-label="Left Align">'
                        + '<span class="fa fa-edit"></span>' +
                        '</button>';
                        return str;
                    }
                }

            ], []],
            data: $scope.role.clean,
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
                return  "<img class='loading-img' src='/static/img/loading_100.gif' /> 正在加载 Cleans";
            },
            formatNoMatches: function () {
                return '<i class="fa status-alert fa-exclamation-triangle" aria-hidden="true"></i> 没有找到匹配的 Cleans';
            }
        });
        $('#conf-table').bootstrapTable({
            search: true,
            showColumns: true,
            toolbar: "#user-conf-toolbar",
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
                    sortable: true
                },
                {
                    width:'510px',

                    field: 'M',
                    title: 'Conf',
                    align: 'center',
                    valign: 'middle',
                    events:ConfChangeRightEvent,
                    formatter: function(value, row, index, field){
                        return $scope.formatter('confOp',value);
                    }
                },
                {
                    field: 'operate',
                    width: '120px',
                    title: 'Operation',
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    events:ConfOperationEvent,
                    formatter: function () {
                        var p1 = "";
                        var str = '<button data-toggle="tooltip" type="button" class="btn btn-info deleteConfOp  ' + p1 + '" title="删除" aria-label="Left Align"><span class="fa fa-minus"></span></button>';
                        str += '  <button data-toggle="tooltip" title="修改" type="button" class="btn btn-info updateConfOp  ' + p1 + '" aria-label="Left Align">'
                        + '<span class="fa fa-edit"></span>' +
                        '</button>';
                        return str;
                    }
                }

            ], []],
            data: $scope.role.conf,
            classes: "table-bordered  table-hover table-striped table",
            showRefresh: true,
            minimumCountColumns: 2,
            pageSize: 20,
            sidePagination: 'client',
            pageList: [20, 40, 80, 200],
            resizable: true,
            resizeMode: 'overflow',
            responseHandler: "responseHandler",
            idField: 'ip',
            formatLoadingMessage: function () {
                return  "<img class='loading-img' src='/static/img/loading_100.gif' /> 正在加载 Confs";
            },
            formatNoMatches: function () {
                return '<i class="fa status-alert fa-exclamation-triangle" aria-hidden="true"></i> 没有找到匹配的 Confs';
            }
        });
        $('#lock-table').bootstrapTable({
            search: true,
            showRefresh: true,
            showColumns: true,
            toolbar: "#user-lock-toolbar",
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
                    sortable: true
                },
                {
                    width:'510px',

                    field: 'M',
                    title: 'Lock',
                    align: 'center',
                    valign: 'middle',
                    events:LockChangeRightEvent,
                    formatter: function(value, row, index, field){
                        return $scope.formatter('lockOp',value);
                    }
                },
                {
                    field: 'operate',
                    width: '120px',
                    title: 'Operation',
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    events:LockOperationEvent,
                    formatter: function () {
                        var p1 = "";
                        var str = '<button data-toggle="tooltip" type="button" class="btn btn-info deleteLockOp  ' + p1 + '" title="删除" aria-label="Left Align"><span class="fa fa-minus"></span></button>';
                        str += '  <button data-toggle="tooltip" title="修改" type="button" class="btn btn-info updateLockOp  ' + p1 + '" aria-label="Left Align">'
                        + '<span class="fa fa-edit"></span>' +
                        '</button>';
                        return str;
                    }
                }

            ], []],
            data: $scope.role.lock,
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
                return  "<img class='loading-img' src='/static/img/loading_100.gif' /> 正在加载 Lock";
            },
            formatNoMatches: function () {
                return '<i class="fa status-alert fa-exclamation-triangle" aria-hidden="true"></i> 没有找到匹配的 Lock';
            }
        });
        $('#sync-table').bootstrapTable({
            search: true,
            showRefresh: true,
            showColumns: true,
            toolbar: "#user-sync-toolbar",
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
                    sortable: true
                },
                {
                    width:'510px',

                    field: 'M',
                    title: 'SyncError',
                    align: 'center',
                    valign: 'middle',
                    events:SyncChangeRightEvent,
                    formatter: function(value, row, index, field){
                        return $scope.formatter('syncOp',value);
                    }
                },
                {
                    field: 'operate',
                    width: '120px',
                    title: 'Operation',
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    events:SyncOperationEvent,
                    formatter: function () {
                        var p1 = "";
                        var str = '<button data-toggle="tooltip" type="button" class="btn btn-info deleteSyncOp  ' + p1 + '" title="删除" aria-label="Left Align"><span class="fa fa-minus"></span></button>';
                        str += '  <button data-toggle="tooltip" title="修改" type="button" class="btn btn-info updateSyncOp  ' + p1 + '" aria-label="Left Align">'
                        + '<span class="fa fa-edit"></span>' +
                        '</button>';
                        return str;
                    }
                }

            ], []],
            data: $scope.role.sync,
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
                return  "<img class='loading-img' src='/static/img/loading_100.gif' /> 正在加载 Sync Error";
            },
            formatNoMatches: function () {
                return '<i class="fa status-alert fa-exclamation-triangle" aria-hidden="true"></i> 没有找到匹配的 Sync Error';
            }
        });
        $('#ip-table').bootstrapTable({
            search: true,
            showRefresh: true,
            showColumns: true,
            toolbar: "#user-ip-toolbar",
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
                    sortable: true
                },
                {
                    width:'510px',

                    field: 'M',
                    title: 'IP',
                    align: 'center',
                    valign: 'middle',
                    events:IPChangeRightEvent,
                    formatter: function(value, row, index, field){
                        return $scope.formatter('ipOp',value);
                    }
                },
                {
                    field: 'operate',
                    width: '120px',
                    title: 'Operation',
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    events:IPOperationEvent,
                    formatter: function () {
                        var p1 = "";
                        var str = '<button data-toggle="tooltip" type="button" class="btn btn-info deleteIpOp  ' + p1 + '" title="删除" aria-label="Left Align"><span class="fa fa-minus"></span></button>';
                        str += '  <button data-toggle="tooltip" title="修改" type="button" class="btn btn-info updateIpOp  ' + p1 + '" aria-label="Left Align">'
                        + '<span class="fa fa-edit"></span>' +
                        '</button>';
                        return str;
                    }
                }

            ], []],
            data: $scope.role.ip,
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
                return  "<img class='loading-img' src='/static/img/loading_100.gif' /> 正在加载 IP/Subnet";
            },
            formatNoMatches: function () {
                return '<i class="fa status-alert fa-exclamation-triangle" aria-hidden="true"></i> 没有找到匹配的 IP/Subnet';
            }
        });
        // Role selector in the pop up window
        $('#role-selector-table').bootstrapTable({
            search: true,
            showRefresh: true,
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
                return  "<img class='loading-img' src='/static/img/loading_100.gif' /> 正在加载 Type";
            },
            formatNoMatches: function () {
                return '<i class="fa status-alert fa-exclamation-triangle" aria-hidden="true"></i> 没有找到匹配的 Type';
            }
        });
        $('#vs-role-selector-table').bootstrapTable({
            search: true,
            showRefresh: true,
            showColumns: true,
            toolbar: "#vs-role-selector-toolbar",
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
                return  "<img class='loading-img' src='/static/img/loading_100.gif' /> 正在加载 Type";
            },
            formatNoMatches: function () {
                return '<i class="fa status-alert fa-exclamation-triangle" aria-hidden="true"></i> 没有找到匹配的 Type';
            }
        });
        $('#group-role-selector-table').bootstrapTable({
            search: true,
            showRefresh: true,
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
                return  "<img class='loading-img' src='/static/img/loading_100.gif' /> 正在加载 Type";
            },
            formatNoMatches: function () {
                return '<i class="fa status-alert fa-exclamation-triangle" aria-hidden="true"></i> 没有找到匹配的 Type';
            }
        });
        $('#policy-role-selector-table').bootstrapTable({
            search: true,
            showRefresh: true,
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
                return  "<img class='loading-img' src='/static/img/loading_100.gif' /> 正在加载 Type";
            },
            formatNoMatches: function () {
                return '<i class="fa status-alert fa-exclamation-triangle" aria-hidden="true"></i> 没有找到匹配的 Type';
            }
        });
        $('#auth-role-selector-table').bootstrapTable({
            search: true,
            showRefresh: true,
            showColumns: true,
            toolbar: "#auth-role-selector-toolbar",
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
            data: $scope.data.auth_ops,
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
                return  "<img class='loading-img' src='/static/img/loading_100.gif' /> 正在加载 Type";
            },
            formatNoMatches: function () {
                return '<i class="fa status-alert fa-exclamation-triangle" aria-hidden="true"></i> 没有找到匹配的 Type';
            }
        });
        $('#sync-role-selector-table').bootstrapTable({
            search: true,
            showRefresh: true,
            showColumns: true,
            toolbar: "#sync-role-selector-toolbar",
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
            data: $scope.data.sync_ops,
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
                return  "<img class='loading-img' src='/static/img/loading_100.gif' /> 正在加载 Type";
            },
            formatNoMatches: function () {
                return '<i class="fa status-alert fa-exclamation-triangle" aria-hidden="true"></i> 没有找到匹配的 Type';
            }
        });
        $('#clean-role-selector-table').bootstrapTable({
            search: true,
            showRefresh: true,
            showColumns: true,
            toolbar: "#clean-role-selector-toolbar",
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
            data: $scope.data.clean_ops,
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
                return  "<img class='loading-img' src='/static/img/loading_100.gif' /> 正在加载 Type";
            },
            formatNoMatches: function () {
                return '<i class="fa status-alert fa-exclamation-triangle" aria-hidden="true"></i> 没有找到匹配的 Type';
            }
        });
        $('#conf-role-selector-table').bootstrapTable({
            search: true,
            showRefresh: true,
            showColumns: true,
            toolbar: "#conf-role-selector-toolbar",
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
            data: $scope.data.conf_ops,
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
                return  "<img class='loading-img' src='/static/img/loading_100.gif' /> 正在加载 Type";
            },
            formatNoMatches: function () {
                return '<i class="fa status-alert fa-exclamation-triangle" aria-hidden="true"></i> 没有找到匹配的 Type';
            }
        });
        $('#ip-role-selector-table').bootstrapTable({
            search: true,
            showRefresh: true,
            showColumns: true,
            toolbar: "#ip-role-selector-toolbar",
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
            data: $scope.data.ip_ops,
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
                return  "<img class='loading-img' src='/static/img/loading_100.gif' /> 正在加载 Type";
            },
            formatNoMatches: function () {
                return '<i class="fa status-alert fa-exclamation-triangle" aria-hidden="true"></i> 没有找到匹配的 Type';
            }
        });
        $('#lock-role-selector-table').bootstrapTable({
            search: true,
            showRefresh: true,
            showColumns: true,
            toolbar: "#lock-role-selector-toolbar",
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
            data: $scope.data.lock_ops,
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
                return  "<img class='loading-img' src='/static/img/loading_100.gif' /> 正在加载 Type";
            },
            formatNoMatches: function () {
                return '<i class="fa status-alert fa-exclamation-triangle" aria-hidden="true"></i> 没有找到匹配的 Type';
            }
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
    };
    $scope.formatter= function (className,value) {
        var str='';
        var p1 = (A.canDo("Auth", "AUTH", '*')  && $scope.optionPanelStatusBool)? "" : "hide";

        if (p1 == "hide") {
            if (value) {
                str = "<span class='fa fa-check status-green' title='有权限'></span>";
            }
            else {
                str = "<span class='fa fa-times status-red' title='无权限'></span>";
            }
        }
        else
        {
            if (value) {
                str = "<span class='"+className+" status-green fa fa-check-square ' title='有权限'></span>";
            }
            else {
                str = "<span class='"+className+" fa fa-square-o' title='无权限'></span>";
            }
        }
        return str;
    };
    $scope.reloadTable= function () {
        var p1= A.canDo("Auth","AUTH", '*') && $scope.optionPanelStatusBool;
        if(!p1){
            $('#group-table').bootstrapTable('hideColumn', 'operate');
            $('#vs-table').bootstrapTable('hideColumn', 'operate');
            $('#slb-table').bootstrapTable('hideColumn', 'operate');
            $('#conf-table').bootstrapTable('hideColumn', 'operate');
            $('#clean-table').bootstrapTable('hideColumn', 'operate');
            $('#sync-table').bootstrapTable('hideColumn', 'operate');
            $('#ip-table').bootstrapTable('hideColumn', 'operate');
            $('#lock-table').bootstrapTable('hideColumn', 'operate');
            $('#auth-table').bootstrapTable('hideColumn', 'operate');
            $('#policy-table').bootstrapTable('hideColumn', 'operate');
        }
        else{
            $('#group-table').bootstrapTable('showColumn', 'operate');
            $('#vs-table').bootstrapTable('showColumn', 'operate');
            $('#slb-table').bootstrapTable('showColumn', 'operate');
            $('#conf-table').bootstrapTable('showColumn', 'operate');
            $('#clean-table').bootstrapTable('showColumn', 'operate');
            $('#sync-table').bootstrapTable('showColumn', 'operate');
            $('#ip-table').bootstrapTable('showColumn', 'operate');
            $('#lock-table').bootstrapTable('showColumn', 'operate');
            $('#auth-table').bootstrapTable('showColumn', 'operate');
            $('#policy-table').bootstrapTable('showColumn', 'operate');
        }
        $('#group-table').bootstrapTable("load", $scope.role.groups);
        $('#vs-table').bootstrapTable("load", $scope.role.vses);
        $('#slb-table').bootstrapTable("load", $scope.role.slbs);
        $('#conf-table').bootstrapTable("load", $scope.role.conf);
        $('#clean-table').bootstrapTable("load", $scope.role.clean);
        $('#ip-table').bootstrapTable("load", $scope.role.ip);
        $('#lock-table').bootstrapTable("load", $scope.role.lock);
        $('#sync-table').bootstrapTable("load", $scope.role.sync);
        $('#auth-table').bootstrapTable("load", $scope.role.auth);
        $('#policy-table').bootstrapTable("load", $scope.role.policies);

        $('#role-selector-table').bootstrapTable("load", $scope.data.slb_ops);
        $('#vs-role-selector-table').bootstrapTable("load", $scope.data.vs_ops);
        $('#group-role-selector-table').bootstrapTable("load", $scope.data.group_ops);
        $('#policy-role-selector-table').bootstrapTable("load", $scope.data.policy_ops);

        $('#conf-role-selector-table').bootstrapTable("load", $scope.data.conf_ops);
        $('#clean-role-selector-table').bootstrapTable("load", $scope.data.clean_ops);
        $('#ip-role-selector-table').bootstrapTable("load", $scope.data.ip_ops);
        $('#lock-role-selector-table').bootstrapTable("load", $scope.data.lock_ops);
        $('#sync-role-selector-table').bootstrapTable("load", $scope.data.sync_ops);
        $('#auth-role-selector-table').bootstrapTable("load", $scope.data.auth_ops);

        $('#group-table').bootstrapTable("hideLoading");
        $('#vs-table').bootstrapTable("hideLoading");
        $('#slb-table').bootstrapTable("hideLoading");
        $('#conf-table').bootstrapTable("hideLoading");
        $('#clean-table').bootstrapTable("hideLoading");
        $('#ip-table').bootstrapTable("hideLoading");
        $('#lock-table').bootstrapTable("hideLoading");
        $('#auth-table').bootstrapTable("hideLoading");
        $('#sync-table').bootstrapTable("hideLoading");
        $('#policy-table').bootstrapTable("hideLoading");
    };
    $scope.loadData= function (hashData) {
        var roleId = hashData.roleId;
        var param ={
            roleId:roleId
        };
        var request = {
            method: 'GET',
            url: G.baseUrl+'/api/auth/role',
            params:param
        };

        var param2 ={
            type:'info'
        };
        var request2={
            method: 'GET',
            url: G.baseUrl+'/api/slbs',
            params:param2
        };

        var request3={
            method: 'GET',
            url: G.baseUrl+'/api/vses',
            params:param2
        };
        var request4={
            method: 'GET',
            url: G.baseUrl+'/api/groups',
            params:param2
        };
        var request5={
            method: 'GET',
            url: G.baseUrl+'/api/policies'
        };

        $q.all(
            [
                $http(request).success(
                    function (response) {
                        $scope.role=response;
                    }
                ),
                $http(request).success(
                    function (response) {
                        $scope.query.role= response;
                    }
                ),
                $http(request2).success(
                    function (response) {
                        $scope.slbs=response.slbs;
                    }
                ),
                $http(request3).success(
                    function (response) {
                        $scope.vses=response['virtual-servers'];
                    }
                ),
                $http(request4).success(
                    function (response) {
                        $scope.groups=response.groups;
                    }
                ),
                $http(request5).success(
                    function (response) {
                        if(response.total>0){
                            $scope.policies=response['traffic-policies'];
                        }
                    }
                )
            ]).then(
            function () {
                $scope.role.slbs= _.filter($scope.role['data-resources'], function (item) {
                    return item['resource-type'].toUpperCase()=='SLB';
                });
                $.each($scope.role.slbs, function (i, item) {
                    item.A= _.find(item.operations, function (item2) {
                        return item2.type=='ACTIVATE';
                    });

                    item.D= _.find(item.operations, function (item2) {
                        return item2.type=='DEACTIVATE';
                    });

                    item.U= _.find(item.operations, function (item2) {
                        return item2.type=='UPDATE';
                    });
                    item.E= _.find(item.operations, function (item2) {
                        return item2.type=='DELETE';
                    });
                    item.R= _.find(item.operations, function (item2) {
                        return item2.type=='READ';
                    });
                    item.P= _.find(item.operations, function (item2) {
                        return item2.type=='PROPERTY';
                    });
                    item.S= _.find(item.operations, function (item2) {
                        return item2.type=='SYNC';
                    });
                    item.I= _.find(item.operations, function (item2) {
                        return item2.type=='ADMIN_INFO';
                    });
                    item.W= _.find(item.operations, function (item2) {
                        return item2.type=='WAF';
                    });
                    var t= _.find($scope.slbs,function (item3) {
                        return item3.id==item.data;
                    });
                    if(t) item.name= t.name;
                    else item.name='-';
                });

                $scope.role.vses= _.filter($scope.role['data-resources'], function (item) {
                    return item['resource-type'].toUpperCase()=='VS';
                });
                $.each($scope.role.vses, function (i, item) {
                    item.A= _.find(item.operations, function (item2) {
                        return item2.type=='ACTIVATE';
                    });

                    item.D= _.find(item.operations, function (item2) {
                        return item2.type=='DEACTIVATE';
                    });

                    item.U= _.find(item.operations, function (item2) {
                        return item2.type=='UPDATE';
                    });
                    item.E= _.find(item.operations, function (item2) {
                        return item2.type=='DELETE';
                    });
                    item.R= _.find(item.operations, function (item2) {
                        return item2.type=='READ';
                    });
                    item.P= _.find(item.operations, function (item2) {
                        return item2.type=='PROPERTY';
                    });
                    item.S= _.find(item.operations, function (item2) {
                        return item2.type=='SYNC';
                    });
                    item.C= _.find(item.operations, function (item2) {
                        return item2.type=='CERT';
                    });

                    var vs= _.find($scope.vses,function (item3) {
                        return item3.id==item.data;
                    });
                    if(vs) item.name=vs.name;
                    else item.name='-';
                });

                $scope.role.groups= _.filter($scope.role['data-resources'], function (item) {
                    return item['resource-type'].toUpperCase()=='GROUP';
                });
                $.each($scope.role.groups, function (i, item) {
                    item.A= _.find(item.operations, function (item2) {
                        return item2.type=='ACTIVATE';
                    });

                    item.D= _.find(item.operations, function (item2) {
                        return item2.type=='DEACTIVATE';
                    });

                    item.U= _.find(item.operations, function (item2) {
                        return item2.type=='UPDATE';
                    });
                    item.E= _.find(item.operations, function (item2) {
                        return item2.type=='DELETE';
                    });
                    item.R= _.find(item.operations, function (item2) {
                        return item2.type=='READ';
                    });
                    item.P= _.find(item.operations, function (item2) {
                        return item2.type=='PROPERTY';
                    });
                    item.S= _.find(item.operations, function (item2) {
                        return item2.type=='SYNC';
                    });
                    item.M= _.find(item.operations, function (item2) {
                        return item2.type=='OP_MEMBER';
                    });
                    item.L= _.find(item.operations, function (item2) {
                        return item2.type=='OP_PULL';
                    });
                    item.H= _.find(item.operations, function (item2) {
                        return item2.type=='OP_HEALTH_CHECK';
                    });
                    var t= _.find($scope.groups,function (item3) {
                        return item3.id==item.data;
                    });
                    if(t) item.name= t.name;
                    else item.name='-';
                });

                $scope.role.auth=_.filter($scope.role['data-resources'], function (item) {
                    return item['resource-type'].toUpperCase()=='AUTH';
                });
                $.each($scope.role.auth, function (i, item) {
                    item.M= _.find(item.operations, function (item2) {
                        return item2.type=='AUTH';
                    });
                });
                $scope.role.clean=_.filter($scope.role['data-resources'], function (item) {
                    return item['resource-type'].toUpperCase()=='CLEAN';
                });
                $.each($scope.role.clean, function (i, item) {
                    item.M= _.find(item.operations, function (item2) {
                        return item2.type=='MAINTENANCE';
                    });
                });
                $scope.role.conf=_.filter($scope.role['data-resources'], function (item) {
                    return item['resource-type'].toUpperCase()=='CONF';
                });
                $.each($scope.role.conf, function (i, item) {
                    item.M= _.find(item.operations, function (item2) {
                        return item2.type=='MAINTENANCE';
                    });
                });
                $scope.role.lock=_.filter($scope.role['data-resources'], function (item) {
                    return item['resource-type'].toUpperCase()=='LOCK';
                });
                $.each($scope.role.lock, function (i, item) {
                    item.M= _.find(item.operations, function (item2) {
                        return item2.type=='MAINTENANCE';
                    });
                });
                $scope.role.sync=_.filter($scope.role['data-resources'], function (item) {
                    return item['resource-type'].toUpperCase()=='SYNCERROR';
                });
                $.each($scope.role.sync, function (i, item) {
                    item.M= _.find(item.operations, function (item2) {
                        return item2.type=='MAINTENANCE';
                    });
                });
                $scope.role.ip=_.filter($scope.role['data-resources'], function (item) {
                    return item['resource-type'].toUpperCase()=='IP';
                });
                $.each($scope.role.ip, function (i, item) {
                    item.M= _.find(item.operations, function (item2) {
                        return item2.type=='OP_SERVER';
                    });
                });
                $scope.role.policies=_.filter($scope.role['data-resources'], function (item) {
                    return item['resource-type'].toUpperCase()=='POLICY';
                });
                $.each($scope.role.policies, function (i, item) {
                    item.A= _.find(item.operations, function (item2) {
                        return item2.type=='ACTIVATE';
                    });

                    item.D= _.find(item.operations, function (item2) {
                        return item2.type=='DEACTIVATE';
                    });

                    item.U= _.find(item.operations, function (item2) {
                        return item2.type=='UPDATE';
                    });
                    item.E= _.find(item.operations, function (item2) {
                        return item2.type=='DELETE';
                    });
                    item.R= _.find(item.operations, function (item2) {
                        return item2.type=='READ';
                    });
                    item.P= _.find(item.operations, function (item2) {
                        return item2.type=='PROPERTY';
                    });
                    item.F= _.find(item.operations, function (item2) {
                        return item2.type=='FORCE';
                    });

                    var t= _.find($scope.policies,function (item3) {
                        return item3.id==item.data;
                    });
                    if(t) item.name= t.name;
                    else item.name='-';
                });
                $scope.reloadTable();
            }
        );
    }
    $scope.hashChanged = function (hashData) { $scope.resource = H.resource;
        if (hashData.env) {
            $scope.env = hashData.env;
        }
        else{
           $scope.env = 'pro';
        }

        if(typeof(hashData.openPanel) == "undefined") {
            $scope.optionPanelStatusBool = false;
        } else {
            $scope.optionPanelStatusBool = hashData.openPanel=="true" ? true : false;
        }
        $scope.initTable();

        $('#group-table').bootstrapTable("removeAll");
        $('#vs-table').bootstrapTable("removeAll");
        $('#slb-table').bootstrapTable("removeAll");
        $('#user-role-table').bootstrapTable("removeAll");
        $('#conf-table').bootstrapTable("removeAll");
        $('#clean-table').bootstrapTable("removeAll");
        $('#sync-table').bootstrapTable("removeAll");
        $('#ip-table').bootstrapTable("removeAll");
        $('#lock-table').bootstrapTable("removeAll");
        $('#auth-table').bootstrapTable("removeAll");
        $('#policy-table').bootstrapTable("removeAll");


        $('#group-table').bootstrapTable("showLoading");
        $('#vs-table').bootstrapTable("showLoading");
        $('#slb-table').bootstrapTable("showLoading");
        $('#user-role-table').bootstrapTable("showLoading");
        $('#conf-table').bootstrapTable("showLoading");
        $('#clean-table').bootstrapTable("showLoading");
        $('#sync-table').bootstrapTable("showLoading");
        $('#ip-table').bootstrapTable("showLoading");
        $('#lock-table').bootstrapTable("showLoading");
        $('#auth-table').bootstrapTable("showLoading");
        $('#policy-table').bootstrapTable("showLoading");
        $scope.loadData(hashData);
    };
    H.addListener("userEditInfoApp", $scope, $scope.hashChanged);
});
angular.bootstrap(document.getElementById("role-info-area"), ['roleEditInfoApp']);

var headerInfoApp = angular.module('headerInfoApp', ["angucomplete-alt"]);
headerInfoApp.controller('headerInfoController', function ($scope, $http) {
    $scope.query = {};
    $scope.generateLink = function (a) {
        var link = '';
        switch (a) {
            case 'user':
            {
                link = "/portal/users#?env=" + G.env;
                break;
            }
            case 'role':
            {
                link = "/portal/userroles#?env=" + G.env;
                break;
            }
            case 'access':
            {
                link = "/portal/useraccess#?env=" + G.env;
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

