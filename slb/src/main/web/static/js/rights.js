/**
 * Created by ygshen on 2016/10/17.
 */
var infoLinksApp = angular.module('infoLinksApp', ["angucomplete-alt", "http-auth-interceptor"]);
infoLinksApp.controller('infoLinksController', function ($scope, $http, $q) {
    $scope.data = {
        current: '用户信息',
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

    $scope.hashChanged = function (hashData) {
        if (hashData.env) {
            $scope.env = hashData.env;
        }
    };
    H.addListener("infoLinksApp", $scope, $scope.hashChanged);
});
angular.bootstrap(document.getElementById("info-links-area"), ['infoLinksApp']);

var usersInfoApp = angular.module('usersInfoApp', ["http-auth-interceptor","angucomplete-alt"]);
usersInfoApp.controller('usersInfoController', function ($scope, $http, $q) {

    $scope.users={};
    //Load cache
    $scope.cacheRequestFn = function(str){
        return {q: str, timestamp: new Date().getTime()};
    };
    $scope.remoteBusUrl = function() {
        return G.baseUrl + "/api/meta/bus";
    };

    $scope.applyHashData = function (hashData) {
        $scope.query.buName = hashData.buName;
    };

    $scope.selectbuName = function (o) {
        if (o) {
            $scope.newUser.bu = o.originalObject.id;
        }
    };

    // Show or not show
    $scope.showNewUserBt= function () {
        return A.canDo("Auth", "AUTH", '*');
    };
    $('#updateUserBt').click(function (e) {
        e.preventDefault();
        var review = reviewData($('#updateUserDialog'));
        if(review){
            $('#updateUserDialog').modal('hide');
            var data = $scope.updateUser;
            var request={
                method:'POST',
                url: G.baseUrl+'/api/auth/user/update',
                data: data
            };
            var loading = "<img src='/static/img/spinner.gif' /> 正在更新用户";
            $('#operationConfrimModel').modal('show').find(".modal-body").html(loading);
            $('#operationConfrimModel').modal("show").find(".modal-title").html("正在更新用户");

            var confrimText = "<span class='warning-important'>注意:更新成功</span>";
            $scope.processRequest(request, $('#operationConfrimModel'), "更新用户", confrimText);
        }
    });
    $('#saveNewUserBt').click(function (e) {
        e.preventDefault();
        var review = reviewData($('#newUserDialog'));
        if(review){
            $('#newUserDialog').modal('hide');
            var data = $scope.newUser;
            var request={
                method:'POST',
                url: G.baseUrl+'/api/auth/user/new',
                data: data
            };
            var loading = "<img src='/static/img/spinner.gif' /> 正在新建用户";
            $('#operationConfrimModel').modal('show').find(".modal-body").html(loading);
            $('#operationConfrimModel').modal("show").find(".modal-title").html("正在新建用户");

            var confrimText = "<span class='warning-important'>注意:新建成功</span>";
            $scope.processRequest(request, $('#operationConfrimModel'), "新建用户", confrimText);
        }else{
            return;
        }
    });

    function reviewData(id) {
        var result = true;
        $.each(id.find('[data-validator-type="validation"]'), function (i, element) {
            if (!$(element).bootstrapValidation('validate'))
                result = false;
        });
        return result;
    };

    $scope.initTable = function() {
        $('#users-table').bootstrapTable({
            toolbar: "#users-toolbar",
            columns: [[
                {
                    field: 'id',
                    title: 'ID',
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    formatter: function (value, row, index) {
                        return '<a title="'+ value +'"href="/portal/user/usermgr#?env=' + G.env + '&userId=' + row.id + '">' + value + '</a>';
                    }
                },
                {
                    field: 'user-name',
                    title: 'User',
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    formatter: function (value, row, index) {
                        return '<a title="'+ value +'"href="/portal/user/usermgr#?env=' + G.env + '&userId=' + row.id + '">' + value + '</a>';
                    }
                },
                {
                    field: 'email',
                    title: 'Email',
                    align: 'left',
                    valign: 'middle',
                    formatter: function (value, row, index) {
                        if(value)
                        return '<a title="'+ value +'"href="mailto:'+value+'">' + value + '</a>';
                        else return '-';
                    }
                },
                {
                    field: 'bu',
                    title: 'BU',
                    align: 'left',
                    valign: 'middle'
                },
                {
                    field: 'Operation',
                    title: 'Operation',
                    align: 'center',
                    width: '150px',
                    valign: 'middle',
                    events: StatusChangeEvent,
                    formatter: function (value, row, index) {
                        var p1 = A.canDo("Auth", "AUTH", '*');
                        var str='';
                        str+='<button type="button" class="btn btn-info remove-user ' + p1 + '" title="删除" aria-label="Left Align" ><span class="fa fa-minus"></span></button>';
                        str+='<button data-dismiss="modal" style="margin-left:5px" type="button" class="btn btn-info update-user  ' + p1 + '" title="修改" aria-label="Left Align" ><span class="fa fa-edit"></span></button>';
                        return str;
                    }
                }
            ], []],
            sortName:'id',
            sortOrder:'desc',
            data: $scope.users,
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
                return  "<img class='loading-img' src='/static/img/loading_100.gif' /> 正在加载 Users信息";
            },
            formatNoMatches: function () {
                return '<i class="fa status-alert fa-exclamation-triangle" aria-hidden="true"></i> 没有找到匹配的 Users';
            }
        });
    };
    var toberemovedUser;
    window.StatusChangeEvent = {
        'click .remove-user': function (e, value, row) {
            PopRemoveMember(row);
        },
        'click .update-user': function (e, value, row) {
           PopUpdateMember(row);
        }
    };
    function PopUpdateMember(row){
        $scope.updateUser={
            'id': row['id'],
            'user-name': row['user-name'],
            'email': row['email'],
            'bu': row['bu']
        };
        $('#updateuser_name').val($scope.updateUser['user-name']);
        $('#updateuser_email').val($scope.updateUser['email']);
        $('#updateBuNameSelector_value').val(row['bu']);
        $('#updateUserDialog').modal('show');
    };
    function PopRemoveMember(row) {
        toberemovedUser=row.id;
        var str='';
        $('.to-be-removed-members').children().remove();
        if (row) {
            var mail=row.email?row.email:'-';
            str += "<tr>";
            str += "<td><span>" + row['user-name'] + "</span></td>";
            str += "<td><span>" + mail + "</span></td>";
            str += "<td><span>" + row.bu + "</span></td>";
            str += "</tr>";
        }
        $('.to-be-removed-members').append(str);
        $('#RemoveUserModal').modal('show');
    };

    $scope.allowRemoveUser= function () {
        var param={
            userId: toberemovedUser
        };
        var request={
            method: 'GET',
            url: G.baseUrl+'/api/auth/user/delete',
            params: param
        };
        var loading = "<img src='/static/img/spinner.gif' /> 正在删除用户";
        $('#operationConfrimModel').modal('show').find(".modal-body").html(loading);
        $('#operationConfrimModel').modal("show").find(".modal-title").html("正在删除用户");

        var confrimText = "<span class='warning-important'>注意:删除成功</span>";
        $scope.processRequest(request, $('#operationConfrimModel'), "删除用户", confrimText);
    };
    $scope.loadData= function () {
        var request={
            method: 'GET',
            url: G.baseUrl+'/api/auth/users'
        };
        $http(request).success(
            function (resp) {
                $scope.users=resp.users;
                $('#users-table').bootstrapTable("load", $scope.users ? $scope.users : []);
                $('#users-table').bootstrapTable("hideLoading");
            }
        );
    };
    $scope.hashChanged = function (hashData) {
        $scope.initTable();
        $('#users-table').bootstrapTable("removeAll");
        $('#users-table').bootstrapTable("showLoading");
        $scope.loadData();
    };
    H.addListener("usersInfoApp", $scope, $scope.hashChanged);

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
                    if(msg.indexOf("overlap")>0){
                        // need force update
                        $scope.showForceUpdate=true;
                    }
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
        if(dialog.attr('id')=='deleteGroupConfirmModel'){
            setTimeout(function () {
                dialog.find('.closeProgressWindowBt2').click();
            },2000);
        }
        else{
            setTimeout(function () {
                dialog.find('.closeProgressWindowBt').click();
            },2000);
        }
    }
    $('.closeProgressWindowBt').click(
        function (e) {
            $scope.loadData();
        }
    );
});
angular.bootstrap(document.getElementById("users-info-area"), ['usersInfoApp']);


//InfoLinksComponent: info links
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
    $scope.hashChanged = function (hashData) {
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

