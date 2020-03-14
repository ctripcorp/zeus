var headerInfoApp = angular.module('headerInfoApp', ["angucomplete-alt"]);
headerInfoApp.controller('headerInfoController', function ($scope, $http) {

    $scope.resource = H.resource;
    var resource= $scope.resource;
    $scope.query = {};
    $scope.generateLink = function (a) {
        var link = '';
        switch (a) {
            case 'list': {

                link = "/portal/tools#?env=" + G.env;
                break;
            }
            case 'url': {
                link = "/portal/tools/visiturl#?env=" + G.env;
                break;
            }
            case 'test': {
                link = "/portal/tools/test#?env=" + G.env;
                break;
            }
            case 'http-https': {
                link = "/portal/tools/redirect/redirects#?env=" + G.env;
                break;
            }
            case 'bug': {
                link = "/portal/tools/problem#?env=" + G.env;
                break;
            }
            case 'verify': {
                link = "/portal/tools/verify#?env=" + G.env;
                break;
            }
            case 'slb-migration': {
                link = "/portal/tools/smigration/migrations#?env=" + G.env;
                break;
            }
            case 'slb-sharding': {
                link = "/portal/tools/sharding/shardings#?env=" + G.env;
                break;
            }
            case 'vs-migration': {
                link = "/portal/tools/vmigration/migrations#?env=" + G.env;
                break;
            }
            case 'vs-merge': {
                link = "/portal/tools/vs/merges#?env=" + G.env;
                break;
            }
            case 'vs-seperate': {
                link = "/portal/tools/vs/seperates#?env=" + G.env;
                break;
            }
            case 'dr': {
                link = "/portal/tools/dr/drs#?env=" + G.env;
                break;
            }
            default:
                break;
        }
        return link;
    }
    $scope.hashChanged = function (hashData) { $scope.resource= H.resource;
        if (hashData.env) {
            $scope.env = hashData.env;
        }
    };
    H.addListener("headerInfoApp", $scope, $scope.hashChanged);
});
angular.bootstrap(document.getElementById("header-area"), ['headerInfoApp']);

var trafficsResultApp = angular.module("trafficsResultApp", ['http-auth-interceptor']);
trafficsResultApp.controller("trafficsResultController", function ($scope, $http, $q) {
    $scope.resource = H.resource;
    var resource= $scope.resource;
    var drsApplication;
    $scope.data = {};
    $scope.query = {};
    $scope.initTable = function () {
        var resource = $scope.resource;
        $('#drs-table').bootstrapTable({
            toolbar: "#table-toolbar",
            columns: [[
                {
                    field: 'state',
                    checkbox: true,
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
                        return '<a href="/portal/tools/dr/edit#?env=' + $scope.env + '&drId=' + row.id + '&page=tool">' + value + '</a>';
                    }
                },
                {
                    field: 'name',
                    title: 'Name',
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    formatter: function (value, row, index) {
                        return '<a href="/portal/tools/dr/edit#?env=' + $scope.env + '&drId=' + row.id + '&page=tool">' + value + '</a>';
                    }
                },
                {
                    field: 'appName',
                    title: 'App',
                    align: 'left',
                    valign: 'middle',
                    formatter: function (value, row, index) {
                        return '<a href="/portal/app#?env=' + $scope.env + '&appId=' + row['appId'] + '">' + value + '</a>';
                    }
                },
                {
                    field: 'config',
                    title: (angular.equals(resource, {}) ? 'DR配置': resource.tools.drs.js.msg1),
                    align: 'left',
                    valign: 'middle',
                    width: '210px',
                    formatter: function (value, row, index) {
                        if(value && value.length==0){
                            return '<a class="status-blue" href="/portal/dr#?env=' + $scope.env + '&drId=' + row.id + (angular.equals(resource, {}) ? '">未配置切换规则, 点此查看详情</a>': resource.tools.drs.js.msg2);
                        }
                        var hasHybridConfig = _.filter(value, function (v) {
                            return v.to==undefined;
                        });
                        if (hasHybridConfig && hasHybridConfig.length>0) {
                            return '<a class="status-blue" href="/portal/dr#?env=' + $scope.env + '&drId=' + row.id + (angular.equals(resource, {}) ? '">复杂规则切换，点此查看详情</a>': resource.tools.drs.js.msg3);
                        }

                        var str = '<div class="">';
                        $.each(value, function (i, item) {
                            str += '<div class="">' +
                                '<div><span class="">' + item.from +
                                '</span><i class="fa fa-arrow-right target-arrow"></i>';

                            $.each(item.to, function (j, item2) {
                                str += '<span>' + item2.to + '</span><b class="status-red">(' + item2.weight + '%)</b>';
                            });

                            str += '</div> ';
                            str += '</div>';
                        });
                        str += '</div>';

                        return str;
                    }
                },
                {
                    field: 'bu',
                    title: 'BU',
                    width: '120px',
                    align: 'left',
                    valign: 'middle',
                    formatter: function (value, row, index) {
                        return '<a href="/portal/bu#?env=' + $scope.env + '&buName=' + value + '">' + value + '</a>';
                    }
                },
                {
                    field: 'appowner',
                    title: 'App Owner',
                    align: 'left',
                    valign: 'middle',
                    formatter: function (value, row, index) {
                        return '<a href="mailto:'+value+'">' + value + '</a>';
                    }
                },
                {
                    field: 'ownerText',
                    title: 'Operators',
                    width: '150px',
                    align: 'left',
                    valign: 'middle',
                    formatter: function (value, row, index) {
                        var str = '<div>';
                        $.each(value, function (i, item) {
                            str += '<div class="pull-left" style="padding-right: 5px"><a href="mailto:' + item + '">' + item;
                            if (i != value.length - 1) str += ',';
                            str += '</a></div>';
                        });
                        str += '</div>';
                        return str;
                    }
                },
                {
                    field: 'status',
                    title: 'Status',
                    align: 'left',
                    valign: 'middle',
                    events: operateEvents,
                    formatter: function (value, row, index) {
                        var obj = {
                            activated: {
                                'class': 'status-green',
                                'text': (angular.equals(resource, {}) ? '已激活': resource.tools.drs.js.msg4)
                            },
                            deactivated: {
                                'class': 'status-gray',
                                'text': (angular.equals(resource, {}) ? '未激活': resource.tools.drs.js.msg5)
                            },
                            toBeActivated: {
                                'class': 'status-yellow',
                                'text': (angular.equals(resource, {}) ? '有变更': resource.tools.drs.js.msg6)
                            }
                        };
                        if (!obj[value]) return '-';
                        var str = '<span class="' + obj[value].class + '">' + obj[value].text;


                        if (value == 'toBeActivated') {
                            str += "(<a class='diffDr' data-toggle='modal' data-target='#activateVGroupModal'>Diff</a>)";
                        }
                        str += '</span>';
                        return str;
                    }
                }
            ], []],
            sortName: 'id',
            sortOrder: 'desc',
            data: $scope.data.view,
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
                return (angular.equals(resource, {}) ? "<img class='loading-img' src='/static/img/loading_100.gif' /> 正在加载 DR信息": resource.tools.drs.js.msg7);
            },
            formatNoMatches: function () {
                return (angular.equals(resource, {}) ? '<i class="fa status-alert fa-exclamation-triangle" aria-hidden="true"></i> 没有找到匹配的 DR': resource.tools.drs.js.msg8);
            }
        });
        $('#drs-table').bootstrapTable("showLoading");
    };
    window.operateEvents = {
        'click .diffDr': function (e, value, row, index) {
            popActivateGroupDialog(row);
        }
    };
    // Area: Right Area
    $scope.showNewTrafficBt = function () {
        return A.canDo('Dr', 'UPDATE', '*');
    };
    $scope.showAddTrafficTag = function () {
        return A.canDo('Dr', 'PROPERTY', '*');
    };
    $('#createTraffic').click(function () {
        window.location.href = "/portal/tools/dr/edit#?env=" + G.env+'&page=tool';
    });
    $('#drs-table').on('check.bs.table uncheck.bs.table ' +
        'check-all.bs.table uncheck-all.bs.table', function () {
        $('#createTag').prop('disabled', !$('#drs-table').bootstrapTable('getSelections').length);
    });

    function popActivateGroupDialog(row) {
        var drId = row.id;
        $scope.tobeActivatedDrId = drId;
        $q.all([
            $http.get(G.baseUrl + "/api/dr?drId=" + drId + "&mode=online").then(
                function successCallback(res) {
                    if (res.data.code) {
                        $scope.onlineGroupData = "No online version!!!";
                    } else {
                        $scope.onlineGroupData = res.data;
                    }
                },
                function errorCallback(res) {
                    if (res.status == 400 && res.data.message == "DR cannot be found.") {
                        $scope.onlineGroupData = "No online version!!!";
                    }
                }
            ),

            $http.get(G.baseUrl + "/api/dr?drId=" + drId).success(
                function (res) {
                    $scope.tobeActivatedGroupData = res;
                }
            )
        ]).then(
            function () {
                $scope.confirmActivateText = (angular.equals(resource, {}) ? "线上版本与当前版本比较": resource.tools.drs.js.msg9);

                if (row.status == "activated") {
                    $scope.confirmActivateText = (angular.equals(resource, {}) ? "线上已是最新版本,确认是否强制重新激活": resource.tools.drs.js.msg11);

                    $('.fileViewHead').removeClass("status-red-important").addClass("status-red-important");
                }
                else {
                    $('.fileViewHead').removeClass("status-red-important");
                }

                var baseText = JSON.stringify(U.sortObjectFileds($scope.onlineGroupData), null, "\t");
                var newText = JSON.stringify(U.sortObjectFileds($scope.tobeActivatedGroupData), null, "\t");

                var base = difflib.stringAsLines(baseText);
                var newtxt = difflib.stringAsLines(newText);

                var sm = new difflib.SequenceMatcher(base, newtxt);
                var opcodes = sm.get_opcodes();
                var diffoutputdiv = byId("diffOutputVGroup");

                diffoutputdiv.innerHTML = "";

                diffoutputdiv.appendChild(diffview.buildView({
                    baseTextLines: base,
                    newTextLines: newtxt,
                    opcodes: opcodes,
                    baseTextName: (angular.equals(resource, {}) ? "线上Dr版本(版本号:" : resource.tools.drs.js.msg12)+ $scope.onlineGroupData.version + ")",
                    newTextName: (angular.equals(resource, {}) ? "更新后Dr版本(版本号:": resource.tools.drs.js.msg13) + $scope.tobeActivatedGroupData.version + ")",
                    viewType: 0
                }));
            }
        );

    }

    $('#addTagBt').click(function (e) {
        e.preventDefault();
        var validate = reviewData($('#createTrafficTagDialog'));
        if (!validate) return;
        $('#createTrafficTagDialog').modal('hide');
        $scope.createTrafficTag($('#tagNameText').val().trim());
    });
    $scope.createTrafficTag = function (tagName) {
        var policyIds = getIdSelections();
        var targetIdQueryString = "";
        $.each(policyIds, function (i, val) {
            if (i == policyIds.length - 1)
                targetIdQueryString += "targetId=" + val;
            else {
                targetIdQueryString += "targetId=" + val + "&";
            }
        });

        var request = {
            url: G.baseUrl + "/api/tagging?tagName=" + tagName + "&type=dr&" + targetIdQueryString,
            method: 'GET',
            params: {}
        };

        $('#operationConfrimModel').modal("show").find(".modal-title").html((angular.equals(resource, {}) ? "在 DR  上打Tag": resource.tools.drs.js.msg14));
        $('#operationConfrimModel').modal("show").find(".modal-body").html((angular.equals(resource, {}) ? "正在打Tag.. <img src='/static/img/spinner.gif' />": resource.tools.drs.js.msg15));
        // update traffic policy
        $scope.processRequest(request, $('#operationConfrimModel'), '更新DR', '打Tag成功');
    };

    // Area: Taging
    function getIdSelections() {
        return $.map($('#drs-table').bootstrapTable('getSelections'), function (row) {
            return row.id
        });
    }

    function reviewData(id) {
        var result = true;
        $.each(id.find('[data-validator-type="validation"]'), function (i, element) {
            if (!$(element).bootstrapValidation('validate'))
                result = false;
        });
        return result;
    };
    $scope.activateVGroup = function () {
        var confirmDialog = $('#activateGroupResultConfirmDialog');
        var loading = (angular.equals(resource, {}) ? "<img src='/static/img/spinner.gif' /> 正在激活": resource.tools.drs.js.msg16);
        confirmDialog.modal('show').find(".modal-body").html(loading);
        confirmDialog.modal("show").find(".modal-title").html((angular.equals(resource, {}) ? "正在激活": resource.tools.drs.js.msg17));

        $('#confirmActivateVGroup').modal('hide');
        var request = {
            method: 'GET',
            url: G.baseUrl + "/api/activate/dr?drId=" + $scope.tobeActivatedDrId
        };
        var msg = "";
        $http(request).success(
            function (res) {
                if (res == undefined || res.code != undefined) {
                    msg = res.message;
                    var errText = (angular.equals(resource, {}) ? "<span class='fa fa-times'></span><span style='padding-left: 10px'> 激活失败</span>": resource.tools.drs.js.msg18);
                    confirmDialog.modal('show').find(".modal-title").html(errText);
                    confirmDialog.modal('show').find(".modal-body").html((angular.equals(resource, {}) ? "失败原因:" : resource.tools.drs.js.msg25)+ msg);
                }
                else {
                    var successText = (angular.equals(resource, {}) ? "<span class='fa fa-check'></span><span style='padding-left: 10px'>激活成功</span>": resource.tools.drs.js.msg19);
                    confirmDialog.modal('show').find(".modal-title").html(successText);
                    confirmDialog.modal('show').find(".modal-body").html((angular.equals(resource, {}) ? "激活成功": resource.tools.drs.js.msg21));
                    setTimeout(function () {
                        confirmDialog.find('#closeActivateConfrimBt').click();
                    }, 2000);
                }
            }
        ).error(function (reject) {
            msg = reject.message;
            var errText = (angular.equals(resource, {}) ? "<span class='fa fa-times'></span><span style='padding-left: 10px'>激活失败</span>": resource.tools.drs.js.msg22);
            confirmDialog.modal('show').find(".modal-title").html(errText);
            confirmDialog.modal('show').find(".modal-body").html((angular.equals(resource, {}) ? "失败原因:" : resource.tools.drs.js.msg25)+ msg);
        });
    }
    var byId = function (id) {
        return document.getElementById(id);
    };
    $scope.loadData = function (hashData) {
        var a = $scope.getDrQueryParam(hashData);
        var param = a[0];
        var queryString = a[1];

        var drsPromise = drsApplication.getAllDRsView(param, queryString);
        drsPromise.then(function (data) {
            $scope.data.view = data;

            var countObj = _.countBy(data, 'status');
            $scope.data.summary = countObj;

            var countAppObj = _.countBy(data, 'appId');
            $scope.data.appSummary = countObj;

            $('.activated-text').text(countObj['activated'] || 0);
            $('.tobeactivated-text').text(countObj['toBeActivated'] || 0);
            $('.deactivated-text').text(countObj['deactivated'] || 0);
            $('.apps-text').text(_.keys(countAppObj).length || 0);
            $('.groups-text').text();

            $('#drs-table').bootstrapTable('load', data);
            $('#drs-table').bootstrapTable("hideLoading");
        });
    };
    $('#closeActivateConfrimBt, #closeNewTagConfrimBt').click(function (e) {
        var hashData = {};
        hashData.timeStamp = new Date().getTime();
        H.setData(hashData);
    });
    $scope.getAppCount = function () {
        var v = $scope.data.appSummary;
        return _.reduce(_.values(v), function (a, b) {
            return a + b;
        });
    };
    $scope.getDrQueryParam = function (hashData) {
        var param = {};
        var queryString = '';

        // If there is only one env hashdata in the url
        if (_.keys(hashData).length == 1 && hashData.env) {
            return [param, queryString];
        } else {
            var id = hashData.trafficId;
            if (id) {
                param.drId = id;
            }

            var name = hashData.trafficName;
            if (name) {
                param.fuzzyName = name;
            }


            var groupId = hashData.groupId;
            if (groupId) {
                param.groupId = groupId;
            }

            var m = {
                已激活: 'activated',
                有变更: 'toBeActivated',
                未激活: 'deactivated'
            };

            var status = hashData.policyStatus;
            if (status) {
                if (queryString) {
                    queryString += '&';
                }
                queryString += 'anyProp=';
                var statusArray = status.split(',')
                for (var i = 0; i < statusArray.length; i++) {
                    queryString += 'status:' + m[statusArray[i]];
                    if (i != statusArray.length - 1) {
                        queryString += ',';
                    }
                }
            }


            var appId = hashData.appId;
            if (appId) {
                if (queryString) {
                    queryString += '&';
                }
                queryString += 'anyProp=appId:' + appId;
            }


            var bus = hashData.policyBues;
            if (bus) {
                if (queryString) {
                    queryString += '&';
                }
                queryString += 'anyProp=';
                var busArray = bus.split(',')
                for (var i = 0; i < busArray.length; i++) {
                    queryString += 'SBU:' + busArray[i];
                    if (i != busArray.length - 1) {
                        queryString += ',';
                    }
                }
            }

            var tags = hashData.policyTags;
            if (tags) {
                param.anyTag = tags;
            }
        }

        return [param, queryString];
    };

    $scope.processRequest = function (request, confirmDialog, operationText, tooltipText) {
        $scope.forceUpdateRequest = request;
        if (!$scope.forceUpdateRequest.params['force']) {
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
                    errText = "<span class='fa fa-times'></span><span style='padding-left: 10px'>" + operationText + (angular.equals(resource, {}) ? "失败</span>": resource.tools.drs.js.msg23);
                    confirmDialog.modal('show').find(".modal-title").html(errText);
                    confirmDialog.modal('show').find(".modal-body").html((angular.equals(resource, {}) ? "失败原因:" : resource.tools.drs.js.msg25)+ msg);
                    $scope.showForceUpdate = true;
                }
                else {
                    var successText = "<span class='fa fa-check'></span><span style='padding-left: 10px'>" + operationText + (angular.equals(resource, {}) ? "成功</span>": resource.tools.drs.js.msg24);
                    confirmDialog.modal('show').find(".modal-title").html(successText);
                    if (tooltipText) confirmDialog.modal('show').find(".modal-body").html(tooltipText);
                    startTimer(confirmDialog);
                }
            }
        ).error(function (reject) {
            msg = reject.message;
            var errText = "<span class='fa fa-times'></span><span style='padding-left: 10px'>" + operationText + (angular.equals(resource, {}) ? "失败</span>": resource.tools.drs.js.msg23);
            confirmDialog.modal('show').find(".modal-title").html(errText);
            confirmDialog.modal('show').find(".modal-body").html((angular.equals(resource, {}) ? "失败原因:" : resource.tools.drs.js.msg25)+ msg);
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

    $('.closeProgressWindowBt').click(
        function (e) {
            var hashData = {};
            hashData.timeStamp = new Date().getTime();
            H.setData(hashData);
        }
    );

    $scope.hashChanged = function (hashData) { $scope.resource= H.resource;
        var env = 'uat';

        if (hashData.env) {
            $scope.env = hashData.env;
            env = hashData.env;
        }
        $scope.initTable();
        drsApplication = AppDrsApplication.create($http, $q, env);
        // Load data
        $scope.loadData(hashData);
    };
    H.addListener("trafficsResultApp", $scope, $scope.hashChanged);
});
angular.bootstrap(document.getElementById("traffics-result-area"), ['trafficsResultApp']);
