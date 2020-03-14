/**
 * Created by ygshen on 2016/10/17.
 */
var infoLinksApp = angular.module('infoLinksApp', ["angucomplete-alt", "http-auth-interceptor"]);
infoLinksApp.controller('infoLinksController', function ($scope, $http, $q) {

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

var rolesInfoApp = angular.module('rolesInfoApp', ["http-auth-interceptor"]);
rolesInfoApp.controller('rolesInfoController', function ($scope, $http, $q) {
    $scope.data={
        systemAccounts:[
            'slbvisitor',
            'slbadmin',
            'ops',
            'superadmin'
        ]
    }
    $scope.roles={};
    $scope.newRole={
        'role-name':'',
        discription:''
    };
    $scope.query={
        slbvisitor:{},
        currentrole:{}
    };

    // Roles operation Area
    window.RoleOperationEvent={
        'click .deleteRoleOp': function (e, value, row) {
            $scope.popRemoveRoleDialog(row['id'],row['role-name'],row['discription']);
        },
        'click .updateRoleOp': function (e, value, row) {
            $scope.popUpdateRole(row['id'],row['role-name'],row['discription']);
        }
    };
    $scope.saveNewRole= function () {
        $('.role-create-message').html("");
        var n = $scope.newRole['role-name'].trim().toLowerCase();
        var d = $scope.newRole['discription'].trim().toLowerCase();
        if(n=='' || n==undefined){
            $('.role-create-message').html("Role name is required");
            return;
        }
        if($scope.data.systemAccounts.indexOf(n)!=-1){
            $('.role-create-message').html("Role名字:"+n+" 是系统账号，不允许重名!");
            return;
        }
        var role ={
            'role-name':n,
            'discription': d,
            'data-resources':$scope.query.slbvisitor['data-resources']
        };

        $('#newRoleDialog').modal('hide');
        var request={
            method:'POST',
            url: G.baseUrl+'/api/auth/role/new',
            data: role
        };
        var txt='成功创建 Role';
        $scope.processRequest(request,$('#operationResultDialog'),'创建新的Role',txt);
    };
    $scope.popRemoveRoleDialog= function (id,name,desc) {
        $('.to-be-removed-roles').html('');
        selectedRoleId=id;
        var str='<tr><td>'+id+'</td><td>'+name+'</td><td>'+desc+'</td></tr>';
        $('#confirmRemoveRoleDialog').modal('show').find('.to-be-removed-roles').append(str);
    };
    $scope.confirmRemoveRole= function () {
        var param ={
            roleId: selectedRoleId
        };
        var request={
            method:'GET',
            url: G.baseUrl+'/api/auth/role/delete',
            params: param
        };
        $scope.processRequest(request,$('#operationResultDialog'),'成功删除Role','Role删除成功');
    };
    var selectedRoleId;
    $scope.popUpdateRole= function (id,name,desc) {
        selectedRoleId=id;
        $('.role-create-message').html("");

        var c = $scope.data.systemAccounts.indexOf(name.toLowerCase())==-1;

        $('.role-name-txt').val(name);
        $('.role-dec-txt').val(desc);

        $scope.newRole['role-name']=name;
        $scope.newRole['discription']=desc;

        if(c){
            $('.role-name-txt').prop('disabled',false);
            $('#updateRoleDialog').modal('show');
        } else{
            $('.role-name-txt').prop('disabled',true);
            $('#updateRoleDialog').modal('show');
        }
    };
    $scope.updateRole= function () {
        var param = {
            roleId:selectedRoleId
        };
        var request={
            method:'GET',
            url: G.baseUrl+'/api/auth/role',
            params: param
        };

        $http(request).success(function (resp) {
            var n = resp;
            n['role-name']=$scope.newRole['role-name'];
            n['discription']=$scope.newRole['discription'];
            var request2={
                method:'POST',
                url: G.baseUrl+'/api/auth/role/update',
                data: n
            };
            $scope.processRequest(request2,$('#operationResultDialog'),'创建新的Role','更新Role成功');
        });
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
        },2000);
    }
    $('.closeProgressWindowBt').click(
        function (e) {
            var hashData = {};
            hashData.timeStamp = new Date().getTime();
            H.setData(hashData);
        }
    );
    $scope.initTable = function() {
        $('#roles-table').bootstrapTable({
            toolbar: "#roles-toolbar",
            columns: [[
                {
                    field: 'id',
                    title: 'ID',
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    formatter: function (value, row, index) {
                        return '<a title="'+ value +'"href="/portal/userrole#?env=' + G.env + '&roleId=' + row.id + '">' + value + '</a>';
                    }
                },
                {
                    field: 'role-name',
                    title: 'Name',
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    formatter: function (value, row, index) {
                        return '<a title="'+ value +'"href="/portal/userrole#?env=' + G.env + '&roleId=' + row.id + '">' + value + '</a>';
                    }
                },
                {
                    field: 'discription',
                    title: 'Description',
                    align: 'left',
                    valign: 'middle'
                }/*,
                {
                    field: 'ops',
                    title: 'Operations',
                    align: 'left',
                    width: '150px',
                    valign: 'middle',
                    events:RoleOperationEvent,
                    formatter: function (value, row, index, field) {
                        var str='';
                        var r = row['role-name'].toLowerCase();
                        if($scope.data.systemAccounts.indexOf(r)==-1){
                            str = '<button data-toggle="tooltip" type="button" class="btn btn-info deleteRoleOp"  title="删除" aria-label="Left Align"><span class="fa fa-minus"></span></button>';
                            str += '  <button data-toggle="tooltip" title="修改" type="button" class="btn btn-info updateRoleOp" aria-label="Left Align">'
                            + '<span class="fa fa-edit"></span>' +
                            '</button>';
                        }else{
                            str += '  <button data-toggle="tooltip" title="修改" type="button" class="btn btn-info updateRoleOp" aria-label="Left Align">'
                            + '<span class="fa fa-edit"></span>' +
                            '</button>';
                        }
                        return str;
                    }
                }*/
            ], []],
            sortName:'id',
            sortOrder:'desc',
            data: $scope.roles,
            classes: "table-bordered  table-hover table-striped table",
            search: true,
            showRefresh: false,
            showColumns: true,
            minimumCountColumns: 2,
            pagination: true,
            idField: 'id',
            resizable: true,
            resizeMode: 'overflow',
            pageSize: 20,
            sidePagination: 'client',
            pageList: [20, 40, 80, 200],
            formatLoadingMessage: function () {
                return  "<img class='loading-img' src='/static/img/loading_100.gif' /> 正在加载 Roles信息";
            },
            formatNoMatches: function () {
                return '<i class="fa status-alert fa-exclamation-triangle" aria-hidden="true"></i> 没有找到匹配的 Roles';
            }
        });
    };
    $scope.loadData= function () {
        var request={
            method: 'GET',
            url: G.baseUrl+'/api/auth/roles'
        };
        $http(request).success(
            function (resp) {
                $scope.roles=resp.roles;
                $('#roles-table').bootstrapTable("load", $scope.roles ? $scope.roles : []);
                $('#roles-table').bootstrapTable("hideLoading");
            }
        );

        var param ={
            roleName:'slbVisitor'
        };
        request={
            method: 'GET',
            url: G.baseUrl+'/api/auth/role',
            params:param
        };
        $http(request).success(
            function (resp) {
                $scope.query.slbvisitor=resp;
            }
        );
    };
    $scope.env = '';
    $scope.hashChanged = function (hashData) { $scope.resource = H.resource;
        $scope.env =hashData.env;
        $scope.initTable();
        $('#roles-table').bootstrapTable("removeAll");
        $('#roles-table').bootstrapTable("showLoading");
        $scope.loadData();
    };
    H.addListener("rolesInfoApp", $scope, $scope.hashChanged);
});
angular.bootstrap(document.getElementById("roles-info-area"), ['rolesInfoApp']);

var headerInfoApp = angular.module('headerInfoApp', ["angucomplete-alt"]);
headerInfoApp.controller('headerInfoController', function ($scope, $http) {
    $scope.query = {};
    $scope.generateLink = function (a) {
        var link = '';
        switch (a) {
            case 'rights': {
                link = "/portal/backend/rights#?env=" + G.env;
                break;
            }
            case 'users': {
                link = "/portal/backend/users#?env=" + G.env;
                break;
            }
            case 'config': {
                link = "/portal/backend/config#?env=" + G.env;
                break;
            }
            case 'role':
            {
                link = "/portal/backend/userroles#?env=" + G.env;
                break;
            }
            case 'access':
            {
                link = "/portal/backend/useraccess#?env=" + G.env;
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