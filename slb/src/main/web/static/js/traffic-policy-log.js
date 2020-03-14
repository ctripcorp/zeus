/**
 * Created by ygshen on 2017/2/22.
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
    $scope.clickTarget= function () {
        $('#targetSelector_value').css('width','250px');
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
    $scope.hashChanged = function (hashData) { $scope.resource = H.resource;
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
            case 'basic':
            {
                link = "/portal/policy#?env=" + G.env + "&policyId=" + $scope.query.policyId;
                break;
            }
            case 'log':
            {
                link = "/portal/policy/log#?env=" + G.env + "&policyId=" + $scope.query.policyId;
                break;
            }
            case 'traffic':
            {
                link = "/portal/policy/traffic#?env=" + G.env + "&policyId=" + $scope.query.policyId;
                break;
            }
            case 'conf':
            {
                link = "/portal/policy/conf#?env=" + G.env + "&policyId=" + $scope.query.policyId;
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
        if (hashData.policyId) {
            $scope.query.policyId = hashData.policyId;
        }
    };
    H.addListener("infoLinksApp", $scope, $scope.hashChanged);
});
angular.bootstrap(document.getElementById("header-area"), ['headerInfoApp']);

//TrafficComponent: Traffic
var opsLogQueryApp = angular.module('opsLogQueryApp', ['ui.bootstrap', 'ui.bootstrap.datetimepicker','angucomplete-alt','http-auth-interceptor','ngSanitize']);
opsLogQueryApp.controller('opsLogQueryController', function ($scope, $http, $filter) {
    $scope.data={
        policyops: {},
        opstates:{},
        builtinusers:{},
        ctripusers:{}
    };
    $scope.query={
        startTime:'',
        endTime:'',
        policyops:{},
        builtinusers:{},
        ctripusers:{},
        status:{}
    };
    $scope.queryDate = {
        startTime: '',
        endTime: ''
    };
    // Calendar area
    $scope.startTime = new Date();
    $scope.endTime = new Date();
    $scope.getOperationTextLanguage = function (x) {
        var resource = $scope.resource;

        if (resource) {
            return resource['log']['log_opsLogQueryApp_opmapping'][x];
        }
    };
    $scope.getStatusMapping = function (x) {
        var resource = $scope.resource;

        if (resource) {
            return resource['log']['log_opsLogQueryApp_statusmapping'][x];
        }
    };

    $scope.setDateNow = function () {
        var d = new Date();
        d = d.setTime(d.getTime() - 3*12*30*24*1000*60*60);

        H.setData({startTime: encodeURIComponent($.format.date(d, 'yyyy-MM-dd HH:mm:00'))});
        d = new Date();
        var e = d.setTime(d.getTime() + 1000*60*60);
        H.setData({endTime: encodeURIComponent($.format.date(e, 'yyyy-MM-dd HH:mm:00'))});
    };
    $('#time-zone input').datepicker({
        timepicker:true,
        autoclose: false, //这里最好设置为false
    });
    $scope.openStartCalendar = function (e) {
        e.preventDefault();
        e.stopPropagation();
        $scope.isStartOpen = true;
    };
    $scope.openEndCalendar = function (e) {
        e.preventDefault();
        e.stopPropagation();
        $scope.isEndOpen = true;
    };

    //Load cache
    $scope.policyOpsClear= function () {
        $scope.query.policyops={};
    };
    $scope.statusClear= function () {
        $scope.query.status={};
    };
    $scope.userClear= function () {
        $scope.query.ctripusers={};
    };
    $scope.builtinUserClear= function () {
        $scope.query.builtinusers={};
    };

    $scope.showClear = function (type) {
        if (type == "policy") {
            return _.keys($scope.query.policyops).length > 0 ? "link-show" : "link-hide";
        }
        if (type == "status") {
            return _.keys($scope.query.status).length > 0 ? "link-show" : "link-hide";
        }
        if (type == "user") {
            return _.keys($scope.query.users).length > 0 ? "link-show" : "link-hide";
        }
        if (type == "builtinuser") {
            return _.keys($scope.query.builtinusers).length > 0 ? "link-show" : "link-hide";
        }
    };

    // Query cretia events
    $scope.toggleStatus= function (x) {
        if ($scope.query.status[x]) {
            delete $scope.query.status[x];
        } else {
            $scope.query.status[x] = x;
        }
    };

    $scope.togglePolicyOps= function (x) {
        if ($scope.query.policyops[x]) {
            if(x=='activate'){
                delete $scope.query.policyops['activateSlb'];
            } if(x=='new'){
                delete $scope.query.policyops['add'];
            }
            delete $scope.query.policyops[x];
        } else {
            var t=x;
            if(x=='activate'){
                t+=','+'activateSlb';
            }
            if(x=='new'){
                t+=','+'add';
            }
            $scope.query.policyops[x]=t;
        }
    };

    $scope.toggleBuiltInUser= function (x) {
        if ($scope.query.builtinusers[x]) {
            delete $scope.query.builtinusers[x];
        } else {
            $scope.query.builtinusers[x] = x;
        }
    };
    $scope.toggleUser= function (x) {
        if ($scope.query.ctripusers[x]) {
            delete $scope.query.ctripusers[x];
        } else {
            $scope.query.ctripusers[x] = x;
        }
    };
    $scope.isSelectedPolicyOps= function (x) {
        if ($scope.query.policyops[x]) {
            return "label-info";
        }
    };

    $scope.isSelectedStatus= function (target) {
        if ($scope.query.status[target]) {
            return "label-info";
        }
    };

    $scope.isSelectedUser= function (target) {
        if ($scope.query.ctripusers[target]) {
            return "label-info";
        }
    };
    $scope.isSelectedBuiltinUser= function (target) {
        if ($scope.query.builtinusers[target]) {
            return "label-info";
        }
    };
    $scope.dataLoaded = false;
    $scope.env = '';
    $scope.loadData= function (hashData) {
        if($scope.env == hashData.env && $scope.dataLoaded) return;
        $scope.data.policyops={
            'new':'新建',
            update:'更新',
            activate: '激活',
            deactivate:'下线',
            'delete':'删除'
        };
        $scope.data.builtinusers =['healthChecker','releaseSys','opSys'];
        $scope.data.opstates=['成功','失败'];
    };
    $scope.applyHashData= function (hashData) {
        $scope.query.startTime=new Date(hashData.startTime);
        if(hashData.endTime){
            $scope.query.endTime=new Date(hashData.endTime);
        }
        else{
            $scope.query.endTime=new Date($scope.query.startTime.getTime() + 1000 * 60 * 90);
        }

        $scope.startTime = $.format.date($scope.query.startTime, 'yyyy-MM-dd HH:mm:00');
        $scope.endTime = $.format.date($scope.query.endTime, 'yyyy-MM-dd HH:mm:00');


        $scope.query.policyops={};
        if (hashData.policyOperations) {
            $.each(hashData.policyOperations.split(","), function (i, val) {
                $scope.query.policyops[val] = val;
            })
        }

        $scope.query.status={};
        if (hashData.logStatus) {
            $.each(hashData.logStatus.split(","), function (i, val) {
                $scope.query.status[val] = val;
            })
        }
        $scope.query.builtinusers={};
        if (hashData.logBuiltinUser) {
            $.each(hashData.logBuiltinUser.split(","), function (i, val) {
                $scope.query.builtinusers[val] = val;
            })
        }

        $scope.query.ctripusers={};
        if (hashData.logUsers) {
            $.each(hashData.logUsers.split(","), function (i, val) {
                $scope.query.ctripusers[val] = val;
            })
        }
    };
    $scope.clearQuery= function () {
        var start;
        var end;
        var d = new Date();
        d = d.setTime(d.getTime() - 3 * 12 * 30 * 24 * 1000 * 60 * 60);
        start = $.format.date(d, 'yyyy-MM-dd HH:mm:00');

        d = new Date();
        var e = d.setTime(d.getTime() + 1000 * 60 * 90);
        end = $.format.date(e, 'yyyy-MM-dd HH:mm:00');

        $scope.query.policyops={};
        $scope.query.status={};
        $scope.query.ctripusers={};
        $scope.query.builtinusers={};
        $scope.startTime=start;
        $scope.endTime=end;
    };

    $scope.executeQuery= function () {
        var hashData={};
        hashData.startTime = encodeURIComponent($.format.date(new Date($scope.startTime), 'yyyy-MM-dd HH:mm:00'));
        hashData.endTime = encodeURIComponent($.format.date(new Date($scope.endTime), 'yyyy-MM-dd HH:mm:00'));
        hashData.policyOperations=_.values($scope.query.policyops);
        hashData.logStatus= _.values($scope.query.status);
        hashData.logUsers= _.values($scope.query.ctripusers);
        hashData.logBuiltinUser= _.values($scope.query.builtinusers);
        hashData.timeStamp = new Date().getTime();
        H.setData(hashData);
    };

    $scope.hashChanged = function (hashData) { $scope.resource = H.resource;
        $scope.resource = H.resource;
        var startTime = hashData.startTime;
        var endTime = hashData.endTime;

        if(!startTime){
            startTime= new Date().getTime()-3*365*24*60*60*1000;
        }else{
            startTime = new Date(startTime).getTime();
        }
        if(!endTime){
            endTime = new Date().getTime();
        }else{
            endTime = new Date(endTime).getTime();
        }

        startTime = $.format.date(new Date(startTime), 'yyyy-MM-dd HH:mm:00');
        endTime = $.format.date(new Date(endTime), 'yyyy-MM-dd HH:mm:00');

        startTime =new Date(decodeURIComponent(startTime.replace(/-/g, '/')));
        endTime =new Date(decodeURIComponent(endTime.replace(/-/g, '/')));

        hashData.startTime = startTime;
        hashData.endTime = endTime;


        $scope.loadData(hashData);
        $scope.applyHashData(hashData);
    };
    H.addListener("trafficApp", $scope, $scope.hashChanged);
});
angular.bootstrap(document.getElementById("ops-query-area"), ['opsLogQueryApp']);


var operationLogApp = angular.module('operationLogApp', ['http-auth-interceptor','ngSanitize']);
operationLogApp.controller('operationLogController', function($scope, $http, $q){
    $(document).ready(function () {
        setInterval(function(){
            $(".tooltip-msg").fadeOut(200).fadeIn(200);
        },5000);
    });
    $scope.initTable = function() {
        var resource = $scope.resource;

        var table = resource['log']['log_operationLogApp_table'];
        var time = table['time'];
        var targetid = table['policyid'];
        var version = table['version'];
        var operation = table['operation'];
        var operationmsg = table['operationmsg'];
        var user = table['user'];
        var clientip = table['clientip'];
        var failreason = table['failreason'];
        var status = table['status'];
        var loadingtext = table['loadingtextpolicy'];
        var norecordtext = table['norecordtextpolicy'];
        var successtext = table['成功'];
        var failtext = table['失败'];
        $("#operation-log-table").bootstrapTable({
            toolbar: '.op-log-toolbar',
            columns: [[
                {
                    field: 'target-id',
                    title: targetid,
                    align: 'left',
                    resetWidth: true,
                    valign: 'middle',
                    sortable: true,
                    formatter: function (value, row, index) {
                        var  str='<a href="/portal/policy#?env='+ G.env+'&policyId='+value+'">'+value+'</a>';
                        return str;
                    }
                },
                {
                    field: 'policy-version',
                    title: version,
                    align: 'left',
                    valign: 'middle',
                    events: DiffVersionEvent,
                    formatter: function (value, row, index) {
                        if(value!='-' && value!=1){
                            var str='<div>' +
                                '<span class="pull-left">'+value+'</span>' +
                                '<a class="pull-right diff-version-bt" title="对比"><span class="fa fa-search"></span><span class="status-yellow" style="margin-left: 10px">(Diff)</span></a>'+
                                '</div>';
                            return str;
                        }else{
                            return value;
                        }
                    },
                    sortable: true
                },
                {
                    field: 'operation',
                    title: operation,
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    formatter: function (value, row, index) {
                        return $scope.getOperationText(value);
                    }
                },
                {
                    field: 'description',
                    title: operationmsg,
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    formatter: function (value, row, index) {
                        return value;
                    }
                },
                {
                    field: 'user-name',
                    title: user,
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    formatter: function (value, row, index) {
                        return "<a href='/portal/user#?env="+$scope.env+"&userId="+value+"'>"+value+"</a>";
                    }
                },
                {
                    field: 'client-ip',
                    title: clientip,
                    align: 'left',
                    valign: 'middle',
                    formatter: function (value, row, index) {
                        return value;
                    },
                    sortable: true
                },
                {
                    field: 'datelong',
                    title: time,
                    align: 'left',
                    valign: 'middle',
                    width: '200px',
                    formatter: function (value, row, index) {
                        var d = new Date(value);
                        return parseDateTime(d,true);
                    },
                    sortable: true
                },
                {
                    field: 'err-msg',
                    title: failreason,
                    align: 'left',
                    valign: 'middle',
                    events: ViewEventDetailEvent,
                    formatter: function (value, row, index) {
                        if (value) {
                            var str = '<div><span style="word-break: break-all" class="logs-error-message-collapse pull-left wrench-text">' +
                                value +
                                '</span><span title="" class="expander pull-right" style="cursor: pointer"><i class="status-blue fa fa-chevron-down wrench"></i></span></div>';
                            return str;
                        }
                        return '-';
                    },
                    sortable: true
                },
                {
                    field: 'success',
                    title: status,
                    align: 'center',
                    valign: 'middle',
                    events: ViewEventDetailEvent,
                    formatter: function (value, row, index) {
                        if (value) return "<button type='button'' class='btn btn-info statusclass'>"+successtext+"</button>";
                        return "<button type='button'' class='btn btn-danger statusclass'>"+failtext+"</button>";
                    },
                    sortable: true
                }
            ], []],
            search: true,
            showRefresh: true,
            showColumns: true,
            sortName: 'date-time',
            sortOrder: 'desc',
            data: $scope.data,
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
                return  "<img class='loading-img' src='/static/img/loading_100.gif' /> "+loadingtext;
            },
            formatNoMatches: function () {
                return '<i class="fa status-alert fa-exclamation-triangle" aria-hidden="true"></i> '+norecordtext;
            }
        });
    };
    window.DiffVersionEvent = {
        'click .diff-version-bt': function (e, value, row) {
            getPolicyDataByVersion(row);
        }
    };
    window.ViewEventDetailEvent={
        'click .expander': function (e, value, row) {
            var target = e.currentTarget;
            target = $(target);
            var current =target;
            var firstSpan = target.parent('div').find('.wrench-text');
            var wrench = current.find('.wrench');

            if (firstSpan) {
                var hasTextCollapse = $(firstSpan).hasClass('logs-error-message-collapse');
                var hasWrenchCollapse = $(wrench).hasClass('fa-chevron-down');
                if (hasTextCollapse){
                    $(firstSpan).removeClass('logs-error-message-collapse');
                } else{
                    $(firstSpan).addClass('logs-error-message-collapse');
                }
                if(hasWrenchCollapse){
                    $(wrench).removeClass('fa-chevron-down').addClass('fa-chevron-up')
                }else{
                    $(wrench).removeClass('fa-chevron-up').addClass('fa-chevron-down')
                }
            }
        },
        'click .statusclass': function(e,value,row){
            var resource = $scope.resource;
            $('.operation-detail-div').html('');
            var statusText='';
            var statusCss = '';
            var type= row.type;
            var target = row['target-id'];
            var operation = row['operation'];
            var user = row['user-name'];
            var success= row['success'];
            var userip= row['client-ip'];
            var exception='-';
            if(row['err-msg']){
                exception =row['err-msg'];
            }
            if(success==true){
                statusCss='status-green';
                statusText='成功';
            }else{
                statusCss='status-red';
                statusText='失败';
            }

            var op_url='-';
            var op_param ='-';

            var data=row['data'];
            if(data){
                if(IsJsonString(data)){
                    var t = JSON.parse(data);
                    op_url= t.uri;
                    op_param= t.query;
                }
            }
            var dateTime =row['date-time'];

            var state = resource['log']['log_operationLogApp_detail']['state'];
            var typetext = resource['log']['log_operationLogApp_detail']['type'];
            var id = resource['log']['log_operationLogApp_detail']['id'];
            var op = resource['log']['log_operationLogApp_detail']['op'];
            var opurl = resource['log']['log_operationLogApp_detail']['opurl'];
            var opparam = resource['log']['log_operationLogApp_detail']['opparam'];
            var oper = resource['log']['log_operationLogApp_detail']['oper'];
            var opip = resource['log']['log_operationLogApp_detail']['opip'];
            var optime = resource['log']['log_operationLogApp_detail']['optime'];
            var failreason = resource['log']['log_operationLogApp_detail']['reason'];

            var str='<div class="" style="margin: 0 auto">' +
                '<table class="table ng-scope ng-table operation-detail-table">' +
                ' <tr><td><b>'+state+':</b></td>' +
                ' <td class="'+statusCss+'" style="font-weight: bold">'+statusText+'</td>' +
                ' </tr>' +
                ' <tr><td><b>'+typetext+':</b></td>' +
                ' <td>'+type+'</td>' +
                ' </tr>' +
                ' <tr><td><b>'+id+':</b></td>' +
                ' <td>'+target+'</td>' +
                ' </tr>' +
                ' <tr><td><b>'+op+':</b></td>' +
                ' <td>'+operation+'</td>' +
                ' </tr>' +
                ' <tr><td><b>'+opurl+':</b></td>' +
                ' <td>'+op_url+'</td>' +
                ' </tr>' +
                ' <tr><td><b>'+opparam+':</b></td>' +
                ' <td>'+op_param+'</td>' +
                ' </tr>' +
                ' <tr><td><b>'+oper+':</b></td>' +
                ' <td>'+user+'</td>' +
                ' </tr>' +
                ' <tr><td><b>'+opip+':</b></td>' +
                ' <td>'+userip+'</td>' +
                ' </tr>' +
                ' <tr><td><b>'+optime+':</b></td>' +
                ' <td>'+dateTime+'</td>' +
                ' </tr>' +
                ' <tr><td style="width: 10%"><b>'+failreason+':</b></td>' +
                ' <td style="word-break: break-all">'+exception+'</td>' +
                ' </tr>' +
                '</table>'+
                '</div>';
            $('.operation-detail-div').append(str);
            $('#output-div').modal('show');
        }
    };
    function getPolicyDataByVersion(row) {
        var currentVersion = row['policy-version'];
        var id= row['target-id'];

        var c = currentVersion;
        var p = currentVersion-1;

        if(row['operation']=='activate'){
            var gd=JSON.parse(row['data']);
            var gd_datas=gd['policy-datas'];
            var gd_sort= _.sortBy(gd_datas,'version');
            p=gd_sort[0].version;
        }

        var param0={
            policyId: id,
            version: c
        };
        var param1={
            policyId: id,
            version: p
        };

        var request0={
            method: 'GET',
            url: G.baseUrl+'/api/archive/policy',
            params: param0
        };
        var request1={
            method: 'GET',
            url: G.baseUrl+'/api/archive/policy',
            params: param1
        };

        var compareData={};
        $q.all(
            [
                $http(request0).success(
                    function (resp) {
                        compareData.current=resp;
                    }
                ),
                $http(request1).success(
                    function (resp) {
                        compareData.previous=resp;
                    }
                )
            ]
        ).then(
            function () {
                var baseText= JSON.stringify(U.sortObjectFileds(compareData.previous), null, "\t");
                var NewText= JSON.stringify(U.sortObjectFileds(compareData.current), null, "\t");
                var target=document.getElementById('fileDiffForm');

                var resource = $scope.resource;

                var before = resource['log']['log_operationLogApp_diff']['before'];
                var after = resource['log']['log_operationLogApp_diff']['after'];
                var online = resource['log']['log_operationLogApp_diff']['online'];
                var offline = resource['log']['log_operationLogApp_diff']['offline'];

                var ptext = before + p;
                var ctext = after + c;
                if (row['operation'] == 'activate') {
                    ptext = online + p;
                    ctext = offline + c;
                }
                diffTwoSlb(target,baseText,NewText,ptext,ctext);
                $('#diffVSDiv').modal('show');
            }
        );
    };

    $scope.getLogsHeader = function () {
        var resource = $scope.resource;
        var d = $scope.data;
        if (resource) {
            var prefix = resource['log']['log_operationLogApp_header_prefix'];
            var suffix = resource['log']['log_operationLogApp_header_suffix'];

            return '<span>' + prefix + '<b>' + d.length + '</b>' + suffix + '</span>'
        }
    };

    $scope.loadData= function (hashData) {
        var policyId=hashData.policyId;

        if(hashData.env){
            $scope.env=hashData.env;
        }
        var status=hashData.logStatus;
        var policyops= hashData.policyOperations;
        var ctripuser = hashData.logUsers;
        var builtinuser = hashData.logBuiltinUser;

        var start =new Date(decodeURIComponent(hashData.startTime));
        var end = new Date(decodeURIComponent(hashData.endTime));

        var opsArray=[];
        var statusArray=[];
        var userArray=[];
        if(policyops){
            opsArray=policyops.split(',');
        }
        if(status){
            var s =status.split(',');
            $.each(s, function (i, item) {
                if(item=='成功') statusArray.push('true');
                else{
                    statusArray.push('false');
                }
            });
        }
        if(ctripuser){
            var w= ctripuser.split(',');
            userArray = userArray.concat(w);
        }
        if(builtinuser){
            var v= builtinuser.split(',');
            userArray=userArray.concat(v);
        }


        $scope.data=[];
        var param = {
            fromDate: parseDateTime(start),
            toDate:  parseDateTime(end),
            type:'POLICY',
            targetId:policyId
        };

        var opsObj={
            name: 'op',
            value:opsArray
        };
        var statusObj={
            name: 'success',
            value:statusArray
        };
        var userObj={
            name: 'user',
            value:userArray
        };

        var union =[opsObj,statusObj,userObj];
        var filter = _.filter(union, function (item) {
            return item.value.length>0;
        });

        $scope.requestArray=[];
        var httpArray=[];

        if(filter.length==0){
            var request={
                method: 'GET',
                url: G.baseUrl+'/api/logs',
                params: param
            };
            httpArray=[
                $http(request).success(function (response) {
                    $scope.queryResult=response;
                    $scope.data=response['operation-log-datas']
                })
            ];
        }else{
            var t=getList(filter);
            $.each(t, function (i, item) {
                param = {
                    fromDate: parseDateTime(start),
                    toDate:   parseDateTime(end),
                    type:'POLICY',
                    targetId:policyId
                };

                var last = item.lastIndexOf('&');
                item=item.substring(0,last);
                var q = item.split('&');
                $.each(q, function (j, item2) {
                    var s = item2.split('=');
                    param[s[0]]=s[1];
                });
                var request={
                    method: 'GET',
                    url: G.baseUrl+'/api/logs',
                    params: param
                };
                httpArray.push(
                    $http(request).success(function (response) {
                        $scope.queryResult=response;
                        if(response['operation-log-datas'] && response['operation-log-datas'].length>0){
                            $scope.data=$scope.data.concat(response['operation-log-datas']);
                        }
                    })
                );
            });
        }
        $q.all(httpArray).then(
            function () {
                if($scope.queryResult.code){
                    exceptionNotify("出错了!!","加载Log 失败了， 失败原因"+ $scope.queryResult.message,null);
                    return;
                }else{
                    setTimeout(
                        function () {
                            $('.alert-danger').remove();
                        },
                        1000
                    );
                }

                $.each($scope.data, function (i, item) {
                    var data = item.data;
                    var isJson = IsJsonString(data);

                    var targetIp=['-'];
                    var version='-';

                    if(!isJson){
                        var regit = /ip=\[(.*)\]/g;
                        var match = regit.exec(data);
                        if(match) {
                            var v = match[1];
                            if(v){
                                targetIp = v.split(',');
                            }
                        }
                    }else{
                        data = JSON.parse(data);
                        targetIp=data.ips;
                        if(data['policy-datas'] && data['policy-datas'].length>0){
                            version=data['policy-datas'][0].version;
                        }
                    }
                    item['target-ip']=targetIp;
                    item['policy-version']=version;
                    item['description']=data['description'];
                    var date = item['date-time'].replace(/-/g, '/');
                    item['datelong']=new Date(date).getTime();
                });
                if(!$scope.data){
                    $scope.data=[];
                }
                $('#operation-log-table').bootstrapTable("load", $scope.data);
                $('#operation-log-table').bootstrapTable("hideLoading");
            }
        );
    };
    var parseDateTime= function (date,secondBool) {
        var year = date.getFullYear();

        var month = date.getMonth()+1;
        if(month<10) month='0'+month;

        var day = date.getDate();
        if(day<10) day='0'+day;

        var hour = date.getHours();
        if(hour<10) hour='0'+hour;

        var minute = date.getMinutes();
        if(minute<10) minute='0'+minute;

        if(!secondBool){
            return year+'-'+month+'-'+day+'_'+hour+':'+minute+':'+'00';
        }else{
            var second = date.getSeconds();
            if(second<10) second='0'+second;

            return year+'/'+month+'/'+day+' '+hour+':'+minute+':'+second;
        }
    };
    $scope.getOperationText= function (text) {
        var t = $scope.resource['log']['log_operationLogApp_opmapping'][text];
        return t;
    };
    $scope.hashChanged = function(hashData){
        $scope.resource = H.resource;
        $scope.initTable();
        $('#operation-log-table').bootstrapTable("removeAll");
        $('#operation-log-table').bootstrapTable("showLoading");
        $scope.loadData(hashData);
    };
    H.addListener("operationLogApp", $scope, $scope.hashChanged);
    // Common functions
    function exceptionNotify(title, message, url){
        var notify = $.notify({
            icon: 'fa fa-exclamation-triangle',
            title: title,
            message: message,
            url:url,
            target: '_self'
        },{
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
            spacing:5,
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
    function getList(a){
        var strs=[];
        var n=1;
        for(var i=0;i< a.length;i++){
            n=n*a[i].value.length;
        }
        for(var j=0;j<n;j++){
            var c='';
            for(var k=0;k< a.length;k++){
                var index= parseInt(Math.random()*a[k].value.length);
                c+=a[k].name+'='+a[k].value[index]+'&';
            }
            if(strs.indexOf(c)!=-1){
                n++;
            }else{
                strs.push(c);
            }
        }
        return strs;
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
angular.bootstrap(document.getElementById("operation-log-area"), ['operationLogApp']);

