/**
 * Created by ygshen on 2016/10/17.
 */
var userEditInfoApp = angular.module('userEditInfoApp', ["http-auth-interceptor","angucomplete-alt"]);
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
     **/
    $scope.query={
        role:{
            id:'',
            'role-name':''
        },
        slb:{
            id:''
        },
        vs:{
            id:''
        },
        group:{
            id:''
        },  policy:{
            id:''
        },
        user:{
            id:'',
            'user-name':'',
            email:'',
            bu:'',
            roles:[],
            'data-resources':[]
        },
        queryType:'',
        env:'uat',
        slbType:'',
        slbOps:[],
        slbIds:[],
        userName:''
    };
    $scope.data={
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

        opType:[
            '正在申请的权限',
            '已有的权限'
        ]
    };
    $scope.user={};
    $scope.slbs={};
    $scope.vses={};
    $scope.groups={};
    $scope.apps={};
    // btn behavior
    $scope.showAppvoveBt= function () {
        return $scope.query.queryType==$scope.data.opType[0];
    };
    $scope.canUserApprove= function () {
        return !A.canDo("Auth", "AUTH", '*');
    };
    $scope.showErrorMessage= function () {
        return !A.canDo("Auth", "AUTH", '*');
    };
    $scope.approveClick= function () {
        var can = A.canDo("Auth", "AUTH", '*');
        if(!can) return;

        var type=$scope.query.slbType;
        var ids = $scope.query.slbIds;
        var ops= _.map($scope.query.slbOps, function (v) {
            return v.toUpperCase();
        });

        var params={
            userName: $scope.query.user['user-name'],
            type: type,
            targetId:ids,
            op:ops.join(';')
        };

        var request={
            method: 'GET',
            url: G.baseUrl+'/api/auth/apply',
            params: params
        };
        $scope.processRequest(request,$('#operationResultDialog'),'批准用户请求','批准成功');
    };
    $scope.denyClick= function () {
        alert('正在建设中.....');
    };
    // type switch
    $scope.toggleOpType= function (type) {
        var hashData = {};
        hashData.timeStamp = new Date().getTime();
        if($scope.query.queryType == type){
            return;
        }else{
            hashData.queryType=type;
        }
        H.setData(hashData);
    };
    $scope.opTypeClass= function (type) {
        if(type==$scope.query.queryType) return 'label-info';
    };

    // Show rights area or not
    $scope.showTypesArea= function (type) {
        if($scope.query.slbType.toLowerCase().trim()==type) return true;
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
        $('#dr-table').bootstrapTable({
            search: true,
            showRefresh: true,
            showColumns: true,
            toolbar: "#user-dr-toolbar",
            columns: [[
                {
                    field: 'data',
                    title: 'ID',
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    formatter: function(value, row, index, field){
                        return '<a target="_blank" href="/portal/dr/edit#?env='+ G.env+'&drId='+value+'">'+value+'</a>';
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
                            return '<a target="_blank" href="/portal/dr/edit#?env='+ G.env+'&drId='+row.data+'">'+value+'</a>';
                        }
                        else{
                            return '*';
                        }
                    }
                },
                {
                    field: 'R',
                    title: 'Read',
                    align: 'center',
                    valign: 'middle',
                    formatter: function(value, row, index, field){
                        return $scope.formatter('groupReadOp',value);
                    }
                },
                {
                    field: 'U',
                    title: 'Update',
                    align: 'center',
                    valign: 'middle',
                    formatter: function(value, row, index, field){
                        return $scope.formatter('groupUpdateOp',value);
                    }
                },
                {
                    field: 'E',
                    title: 'Delete',
                    align: 'center',
                    valign: 'middle',
                    formatter: function(value, row, index, field){
                        return $scope.formatter('groupDeleteOp',value);
                    }
                },

                {
                    field: 'P',
                    title: 'PROPERTY',
                    align: 'center',
                    valign: 'middle',
                    formatter: function(value, row, index, field){
                        return $scope.formatter('groupPropertyOp',value);
                    }
                },

                {
                    field: 'A',
                    title: 'Activate',
                    align: 'center',
                    valign: 'middle',
                    formatter: function(value, row, index, field){
                        return $scope.formatter('groupActivateOp',value);
                    }
                },
                {
                    field: 'D',
                    title: 'Deactivate',
                    align: 'center',
                    valign: 'middle',
                    formatter: function(value, row, index, field){
                        return $scope.formatter('groupDeactivateOp',value);
                    }
                },
                {
                    field: 'F',
                    title: 'Force',
                    align: 'center',
                    valign: 'middle',
                    formatter: function(value, row, index, field){
                        return $scope.formatter('groupDeactivateOp',value);
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
                return  "<img class='loading-img' src='/static/img/loading_100.gif' /> 正在加载 DR";
            },
            formatNoMatches: function () {
                return '<i class="fa status-alert fa-exclamation-triangle" aria-hidden="true"></i> 没有找到匹配的 DR';
            }
        });
        $('#policy-table').bootstrapTable({
            search: true,
            showRefresh: true,
            showColumns: true,
            toolbar: "#user-policy-toolbar",
            columns: [[
                {
                    field: 'data',
                    title: 'ID',
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    formatter: function(value, row, index, field){
                        return '<a target="_blank" href="/portal/policy#?env='+ G.env+'&policyId='+value+'">'+value+'</a>';
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
                            return '<a target="_blank" href="/portal/policy#?env='+ G.env+'&policyId='+row.data+'">'+value+'</a>';
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
                    formatter: function(value, row, index, field){
                        return $scope.formatter('groupReadOp',value);
                    }
                },
                {
                    field: 'U',
                    title: 'Update',
                    align: 'center',
                    valign: 'middle',
                    formatter: function(value, row, index, field){
                        return $scope.formatter('groupUpdateOp',value);
                    }
                },
                {
                    field: 'E',
                    title: 'Delete',
                    align: 'center',
                    valign: 'middle',
                    formatter: function(value, row, index, field){
                        return $scope.formatter('groupDeleteOp',value);
                    }
                },

                {
                    field: 'P',
                    title: 'PROPERTY',
                    align: 'center',
                    valign: 'middle',
                    formatter: function(value, row, index, field){
                        return $scope.formatter('groupPropertyOp',value);
                    }
                },

                {
                    field: 'A',
                    title: 'Activate',
                    align: 'center',
                    valign: 'middle',
                    formatter: function(value, row, index, field){
                        return $scope.formatter('groupActivateOp',value);
                    }
                },
                {
                    field: 'D',
                    title: 'Deactivate',
                    align: 'center',
                    valign: 'middle',
                    formatter: function(value, row, index, field){
                        return $scope.formatter('groupDeactivateOp',value);
                    }
                },
                {
                    field: 'F',
                    title: 'Force',
                    align: 'center',
                    valign: 'middle',
                    formatter: function(value, row, index, field){
                        return $scope.formatter('groupDeactivateOp',value);
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
                return  "<img class='loading-img' src='/static/img/loading_100.gif' /> 正在加载 Traffic Policy";
            },
            formatNoMatches: function () {
                return '<i class="fa status-alert fa-exclamation-triangle" aria-hidden="true"></i> 没有找到匹配的 Traffic Policy';
            }
        });
        $('#group-table').bootstrapTable({
            search: true,
            showRefresh: false,
            showColumns: true,
            toolbar: "#user-group-toolbar",
            columns: [[
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
                            return '<a target="_blank" href="/portal/group#?env='+ G.env+'&groupId='+row.data+'">'+value+'</a>';
                        }
                        else{
                            return '-';
                        }
                    }
                },
                {
                    field: 'app',
                    title: 'App',
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    formatter: function(value, row, index, field){
                        if(row.data!='*'){
                            return '<a target="_blank" href="/portal/group#?env='+ G.env+'&groupId='+row.data+'">'+value+'</a>';
                        }
                        else{
                            return '-';
                        }
                    }
                },
                {
                    field: 'bu',
                    title: 'BU',
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    formatter: function(value, row, index, field){
                        if(row.data!='*'){
                            return '<a target="_blank" href="/portal/group#?env='+ G.env+'&groupId='+row.data+'">'+value+'</a>';
                        }
                        else{
                            return '-';
                        }
                    }
                },


                {
                    field: 'owner',
                    title: 'IsOwner?',
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    formatter: function(value, row, index, field){
                        if($scope.query.userName==value){
                            return '是';
                        }return '否';
                    }
                },

                {
                    field: 'R',
                    title: 'Read',
                    align: 'center',
                    valign: 'middle',
                    formatter: function(value, row, index, field){
                        return $scope.formatter('groupReadOp',value);
                    }
                },
                {
                    field: 'U',
                    title: 'Update',
                    align: 'center',
                    valign: 'middle',
                    formatter: function(value, row, index, field){
                        return $scope.formatter('groupUpdateOp',value);
                    }
                },
                {
                    field: 'E',
                    title: 'Delete',
                    align: 'center',
                    valign: 'middle',
                    formatter: function(value, row, index, field){
                        return $scope.formatter('groupDeleteOp',value);
                    }
                },

                {
                    field: 'P',
                    title: 'PROPERTY',
                    align: 'center',
                    valign: 'middle',
                    formatter: function(value, row, index, field){
                        return $scope.formatter('groupPropertyOp',value);
                    }
                },
                {
                    field: 'S',
                    title: 'SYNC',
                    align: 'center',
                    valign: 'middle',
                    formatter: function(value, row, index, field){
                        return $scope.formatter('groupSyncOp',value);
                    }
                },

                {
                    field: 'A',
                    title: 'Activate',
                    align: 'center',
                    valign: 'middle',
                    formatter: function(value, row, index, field){
                        return $scope.formatter('groupActivateOp',value);
                    }
                },
                {
                    field: 'D',
                    title: 'Deactivate',
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
                    formatter: function(value, row, index, field){
                        return $scope.formatter('groupPullOp',value);
                    }
                },
                {
                    field: 'H',
                    title: 'Raise',
                    align: 'center',
                    valign: 'middle',
                    formatter: function(value, row, index, field){
                        return $scope.formatter('groupHealthOp',value);
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
                return  "<img class='loading-img' src='/static/img/loading_100.gif' /> 正在加载 Groups";
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
                    valign: 'middle',
                    formatter: function(value, row, index, field){
                        return $scope.formatter('vsReadOp',value);
                    }
                },
                {
                    field: 'U',
                    title: 'Update',
                    align: 'center',
                    valign: 'middle',
                    formatter: function(value, row, index, field){
                        return $scope.formatter('vsUpdateOp',value);
                    }
                },
                {
                    field: 'E',
                    title: 'Delete',
                    align: 'center',
                    valign: 'middle',
                    formatter: function(value, row, index, field){
                        return $scope.formatter('vsDeleteOp',value);
                    }
                },
                {
                    field: 'A',
                    title: 'Activate',
                    align: 'center',
                    valign: 'middle',
                    formatter: function(value, row, index, field){
                        return $scope.formatter('vsActivateOp',value);
                    }
                },
                {
                    field: 'D',
                    title: 'Deactivate',
                    align: 'center',
                    valign: 'middle',
                    formatter: function(value, row, index, field){
                        return $scope.formatter('vsDeactivateOp',value);
                    }
                },
                {
                    field: 'P',
                    title: 'PROPERTY',
                    align: 'center',
                    valign: 'middle',
                    formatter: function(value, row, index, field){
                        return $scope.formatter('vsPropertyOp',value);
                    }
                },
                {
                    field: 'S',
                    title: 'SYNC',
                    align: 'center',
                    valign: 'middle',
                    formatter: function(value, row, index, field){
                        return $scope.formatter('vsSyncOp',value);
                    }
                },

                {
                    field: 'C',
                    title: 'Cert',
                    align: 'center',
                    valign: 'middle',
                    formatter: function(value, row, index, field){
                        return $scope.formatter('vsCertOp',value);
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
                return  "<img class='loading-img' src='/static/img/loading_100.gif' /> 正在加载 VSES";
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
                    field: 'data',
                    title: 'ID',
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    formatter: function(value, row, index, field){
                        return '<a target="_blank" href="/portal/slb#?env='+ G.env+'&slbId='+value+'">'+value+'</a>';
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
                    formatter: function(value, row, index, field){
                        return $scope.formatter('slbReadOp',value);
                    }
                },
                {
                    field: 'U',
                    title: 'Update',
                    align: 'center',
                    valign: 'middle',
                    formatter: function(value, row, index, field){
                        return $scope.formatter('slbUpdateOp',value);
                    }
                },
                {
                    field: 'E',
                    title: 'Delete',
                    align: 'center',
                    valign: 'middle',
                    formatter: function(value, row, index, field){
                        return $scope.formatter('slbDeleteOp',value);
                    }
                },
                {
                    field: 'A',
                    title: 'Activate',
                    align: 'center',
                    valign: 'middle',
                    formatter: function(value, row, index, field){
                        return $scope.formatter('slbActivateOp',value);
                    }
                },
                {
                    field: 'D',
                    title: 'Deactivate',
                    align: 'center',
                    valign: 'middle',
                    formatter: function(value, row, index, field){
                        return $scope.formatter('slbDeactivateOp',value);
                    }
                },
                {
                    field: 'P',
                    title: 'PROPERTY',
                    align: 'center',
                    valign: 'middle',
                    formatter: function(value, row, index, field){
                        return $scope.formatter('slbPropertyOp',value);
                    }
                },
                {
                    field: 'S',
                    title: 'SYNC',
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
                    valign: 'middle',
                    formatter: function(value, row, index, field){
                        return $scope.formatter('slbAdminOp',value);
                    }
                },
                {
                    field: 'W',
                    title: 'WAF',
                    align: 'center',
                    valign: 'middle',
                    formatter: function(value, row, index, field){
                        return $scope.formatter('slbWafOp',value);
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
                return  "<img class='loading-img' src='/static/img/loading_100.gif' /> 正在加载 SLBS";
            },
            formatNoMatches: function () {
                return '<i class="fa status-alert fa-exclamation-triangle" aria-hidden="true"></i> 没有找到匹配的 SLBS';
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
                    formatter: function(value, row, index, field){
                        return '<a href="/portal/userrole#?env='+ G.env+'&roleId='+value+'">'+value+'</a>';
                    }

                },
                {
                    field: 'role-name',
                    title: 'Name',
                    align: 'left',
                    valign: 'middle',
                    formatter: function(value, row, index, field){
                        return '<a href="/portal/userrole#?env='+ G.env+'&roleId='+row.id+'">'+value+'</a>';
                    }
                },
                {
                    field: 'discription',
                    title: 'Description',
                    align: 'left',
                    valign: 'middle'
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
                return  "<img class='loading-img' src='/static/img/loading_100.gif' /> 正在加载 Roles";
            },
            formatNoMatches: function () {
                return '<i class="fa status-alert fa-exclamation-triangle" aria-hidden="true"></i> 没有找到匹配的 Roles';
            }
        });
    };

    $scope.formatter= function (className,value) {
        var str='';
        if (value) {
            str = "<span class='fa fa-check status-green' title='有权限'></span>";
        }
        else {
            str = "<span class='fa fa-times status-red' title='无权限'></span>";
        }
        return str;
    };
    $scope.reloadTable= function () {
        $('#policy-table').bootstrapTable("load", $scope.user.policies);
        $('#dr-table').bootstrapTable("load", $scope.user.drs);
        $('#group-table').bootstrapTable("load", $scope.user.groups);
        $('#vs-table').bootstrapTable("load", $scope.user.vses);
        $('#slb-table').bootstrapTable("load", $scope.user.slbs);
        $('#user-role-table').bootstrapTable("load", $scope.user.roles);

        $('#dr-table').bootstrapTable("hideLoading");
        $('#policy-table').bootstrapTable("hideLoading");
        $('#group-table').bootstrapTable("hideLoading");
        $('#vs-table').bootstrapTable("hideLoading");
        $('#slb-table').bootstrapTable("hideLoading");
        $('#user-role-table').bootstrapTable("hideLoading");
    };
    $scope.loadData= function (hashData) {
        var userName = '';
        var slbType='';
        var slbIds=[];
        var slbOps=[];
        if(hashData.userName){
            userName = hashData.userName.trim();
            $scope.query.userName=userName;
        }
        if(!userName){
            alert('URL中没有提供要查询的用户名');
            return;
        }

        if(hashData.type){
            slbType=hashData.type;
            $scope.query.slbType=slbType;
        }else{
            alert('URL中没有提供要查询的 SLB 种类。');
            return;
        }
        if(hashData.targetId){
            if(Array.isArray(hashData.targetId)){
                slbIds=hashData.targetId;
            }else{
                slbIds=[hashData.targetId];
            }
            $scope.query.slbIds=slbIds;
        }else{
            alert('URL中没有提供要查询的 SLB IDS。');
            return;
        }
        if(hashData.op){
            slbOps=hashData.op.split(',');
            $scope.query.slbOps=slbOps;
        }else{
            alert('URL中没有提供要查询的 SLB 操作类型。');
            return;
        }
        if(hashData.env){
            $scope.query.env = hashData.env;
        }

        if(hashData.queryType){
           $scope.query.queryType = hashData.queryType;
        }else{
            $scope.query.queryType = $scope.data.opType[0];
        }

        var param ={
            userName:userName
        };
        var request = {
            method: 'GET',
            url: G.baseUrl+'/api/auth/user',
            params:param
        };

        var param2 ={
            type:'extended'
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
        var request6={
            method: 'GET',
            url: G.baseUrl+'/api/apps'
        };
        var request5={
            method: 'GET',
            url: G.baseUrl+'/api/policies',
            params:param2
        };
        var request7={
            method: 'GET',
            url: G.baseUrl+'/api/drs',
            params:param2
        };

        var requestArray=[
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
                    $scope.policies=response['traffic-policies'];
                }
            ),
            $http(request6).success(
                function (response) {
                    $scope.apps= _.indexBy(response['apps'], function (item) {
                        return item['app-id'];
                    });
                }
            ),
            $http(request7).success(
                function (response) {
                    $scope.drs= _.indexBy(response['drs'], function (item) {
                        return item['id'];
                    });
                }
            )
        ];

        if($scope.query.queryType==$scope.data.opType[1]){
            requestArray.push(
                $http(request).success(
                function (response) {
                    $scope.user=response;
                    $scope.query.user=  $.extend(true, {}, response);
                    if(!$scope.query.user.roles){
                        $scope.query.user.roles=[];
                    }
                }
            ));
        }else{
            // come to a new array based on the pointed objects
            requestArray.push(
                $http(request).success(
                    function (response) {
                        $scope.query.user=  $.extend(true, {}, response);
                        $scope.user=response;

                        var ops = _.map(slbOps, function (item) {
                            return {
                                'type':item.toUpperCase()
                            };
                        });

                        $scope.user['data-resources'] = _.map(slbIds, function (item) {
                            return {
                                'resource-type': slbType.toUpperCase(),
                                'data': item,
                                'operations':ops
                            }
                        });

                        if(!$scope.query.user.roles){
                            $scope.query.user.roles=[];
                        }
                    }
                ));
        }

        $q.all(requestArray).then(
            function () {
                $scope.user.roles=!$scope.user.roles?[]:$scope.user.roles;
                $scope.user.slbs= _.filter($scope.user['data-resources'], function (item) {
                    return item['resource-type'].toUpperCase()=='SLB';
                });
                $.each($scope.user.slbs, function (i, item) {
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

                $scope.user.vses= _.filter($scope.user['data-resources'], function (item) {
                    return item['resource-type'].toUpperCase()=='VS';
                });
                $.each($scope.user.vses, function (i, item) {
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

                $scope.user.groups= _.filter($scope.user['data-resources'], function (item) {
                    return item['resource-type'].toUpperCase()=='GROUP';
                });
                $.each($scope.user.groups, function (i, item) {
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

                    if(t) {
                        var appid= t['app-id'];
                        var appname = $scope.apps[appid]?$scope.apps[appid]['chinese-name']:'-';

                        item.app=appid+'('+appname+')';
                    }
                    else item.app='-';

                    if(t) {
                        var ps = _.indexBy(t['properties'], function (v) {
                            return v.name.toLowerCase();
                        });
                        item.bu =ps['sbu']? ps['sbu'].value:'-';
                    }else{
                        item.bu='-';
                    }

                    if(t) {
                        var ps = t['tags'];
                        var b = _.find(ps, function (a) {
                            return a.startsWith('owner_');
                        });
                        if(b){
                            item.owner = b.substring(6, b.length);
                        }else{
                          item.owner='-';
                        }

                    }else{
                        item.owner='-';
                    }
                });

                $scope.user.policies= _.filter($scope.user['data-resources'], function (item) {
                    return item['resource-type'].toUpperCase()=='POLICY';
                });
                $.each($scope.user.policies, function (i, item) {
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

                $scope.user.drs= _.filter($scope.user['data-resources'], function (item) {
                    return item['resource-type'].toUpperCase()=='DR';
                });
                $.each($scope.user.drs, function (i, item) {
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
                    var t= _.find($scope.drs,function (item3) {
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

        $scope.initTable();

        $('#policy-table').bootstrapTable("removeAll");
        $('#dr-table').bootstrapTable("removeAll");
        $('#group-table').bootstrapTable("removeAll");
        $('#vs-table').bootstrapTable("removeAll");
        $('#slb-table').bootstrapTable("removeAll");
        $('#user-role-table').bootstrapTable("removeAll");

        $('#policy-table').bootstrapTable("showLoading");
        $('#dr-table').bootstrapTable("showLoading");
        $('#group-table').bootstrapTable("showLoading");
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

