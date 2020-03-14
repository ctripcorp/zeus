var headerInfoApp = angular.module('headerInfoApp', ["angucomplete-alt"]);
headerInfoApp.controller('headerInfoController', function ($scope, $http) {
    $scope.resource=  H.resource;
    var resource = $scope.resource;
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
    $scope.hashChanged = function (hashData) { $scope.resource = H.resource;
        if (hashData.env) {
            $scope.env = hashData.env;
        }
    };
    H.addListener("headerInfoApp", $scope, $scope.hashChanged);
});
angular.bootstrap(document.getElementById("header-area"), ['headerInfoApp']);


var selfInfoApp = angular.module('selfInfoApp', ["ngSanitize", "angucomplete-alt", "http-auth-interceptor"]);
selfInfoApp.controller('selfInfoController', function ($scope, $http, $q) {
    $scope.resource=  H.resource;
    var resource = $scope.resource;
    $scope.data = {};

    var flowService;
    var vsservice;

    var table = $('#merges-table');

    $scope.startNewMigration = function () {
        // var hasRight = A.canDo('Flow', 'NEW', '*') || A.canDo('Flow', 'FLOW', '*');
        // if (!hasRight) {
        //     alert('还没有权限，权限申请流程正在建设中...');
        //     return;
        // }

        window.location.href = '/portal/tools/vs/merge-edit#?env=' + $scope.env;
    };
    $scope.initTable = function (data) {
        var resource = $scope.resource;
        data = data || [];
        table.bootstrapTable({
            toolbar: "#toolbar",
            columns: [[
                {
                    field: 'id',
                    title: 'ID',
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    formatter: function (value, row, index) {
                        return '<a class="editVS" target="_self" title="' + value + '" ' +
                            'href="/portal/tools/vs/merge-edit#?env=' + G.env + '&id=' + value + '">' + value + '(' + row.name + ')</a>';
                    }
                },
                {
                    field: 'create-time',
                    title: (angular.equals(resource, {}) ? '创建日期': resource.tools.merges.js.msg1),
                    align: 'left',
                    valign: 'middle',
                    sortable: true
                },
                {
                    title: (angular.equals(resource, {}) ? '待合并VS': resource.tools.merges.js.msg2),
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    formatter: function (value, row, index) {
                        var originVs = row.getSourceVses();
                        var str = '';
                        $.each(originVs, function (i, v) {
                            str += '<div><a class="editVS"' +
                                ' target="_self" ' +
                                'title="' + v.name + '"' +
                                ' href="/portal/vs#?env=' + G.env + '&vsId=' + i + '">VSID: ' + i + '(' + v.name + ')</a></div>';
                        });
                        return str;
                    }
                },
                {
                    title: (angular.equals(resource, {}) ? '目标VS': resource.tools.merges.js.msg3),
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    formatter: function (value, row, index) {
                        var targetVs = row.getTargetVs();
                        var vsKeyArray = _.keys(targetVs);
                        var vsValueArray = _.values(targetVs);

                        var v = vsValueArray[0];
                        var k = vsKeyArray[0];

                        return '<div><a class="editVS"' +
                            ' target="_self" ' +
                            'title="' + v.name + '"' +
                            ' href="/portal/vs#?env=' + G.env + '&vsId=' + k + '">VSID: ' + k + '(' + v.name + ')</a></div>';
                    }
                },
                {
                    field: 'status',
                    title: (angular.equals(resource, {}) ? '状态': resource.tools.merges.js.msg11),
                    width: '120px',
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    formatter: function (value, row, index) {
                        var sclass = 'status-green';
                        return '<span class="' + sclass + '">' + row.status + '</span>';
                    }
                },
                {
                    title: (angular.equals(resource, {}) ? '操作': resource.tools.merges.js.msg12),
                    align: 'left',
                    valign: 'middle',
                    width: '80px',
                    events: operateEvents,
                    sortable: true,
                    formatter: function (value, row, index) {
                        var str = '<div class="operation"><a class="pull-left" href="/portal/tools/vs/merge-edit#?env=' +
                            '' + $scope.env + '&id=' + row.id + (angular.equals(resource, {}) ? '">编辑</a>': resource.tools.merges.js.msg4);

                        if (row.status == '完成清理') {
                            str += (angular.equals(resource, {}) ? ' <a class="pull-right remove-split">删除</a></div>': resource.tools.merges.js.msg6);
                        }

                        return str;
                    }
                }

            ], []],
            sortName: 'id',
            sortOrder: 'desc',
            showRefresh: true,
            search: true,
            showColumns: true,
            data: data,
            classes: "table table-bordered  table-hover table-striped",
            minimumCountColumns: 2,
            pagination: true,
            idField: 'id',
            pageSize: 20,
            sidePagination: 'client',
            pageList: [20, 40, 80, 200],
            resizable: true,
            resizeMode: 'overflow',
            formatLoadingMessage: function () {
                return (angular.equals(resource, {}) ? "<img class='loading-img' src='/static/img/loading_100.gif' /> 正在加载 所有VS合并记录": resource.tools.merges.js.msg5);
            },
            formatNoMatches: function () {
                return (angular.equals(resource, {}) ? '<i class="fa status-alert fa-exclamation-triangle" aria-hidden="true"></i> 没有找到匹配的VS合并记录': resource.tools.merges.js.msg7);
            }
        });

        table.bootstrapTable('showLoading');
    };
    var currentId;
    window.operateEvents = {
        'click .remove-split': function (e, value, row, index) {
            currentId = row.id;
            $('#deleteSplitDialog').modal('show');
        }
    };
    $scope.confirmRemoveSplit = function () {
        $('#progressDialog').modal('show').find('#progress').html((angular.equals(resource, {}) ? "正在删除.. <img src='/static/img/spinner.gif' />": resource.tools.merges.js.msg8));

        var promise = flowService.deleteMerge(currentId);
        promise.success(function (response, code) {
            if (code == 200) {
                $('#progressDialog').modal('show').find('#progress').html((angular.equals(resource, {}) ? "删除成功": resource.tools.merges.js.msg9));
                startTimer($('#progressDialog'));
            } else {
                $('#progressDialog').modal('show').find('#progress').html((angular.equals(resource, {}) ? "<span class='warning-important'>删除失败</span>": resource.tools.merges.js.msg10));
            }
        });
    };

    $('.closeProgressWindowBt').click(
        function (e) {
            var hashData = {};
            hashData.timeStamp = new Date().getTime();
            H.setData(hashData);
        }
    );

    function startTimer(dialog) {
        setTimeout(function () {
            dialog.find('.closeProgressWindowBt').click();
        }, 2000);
    }

    $scope.loadData = function () {
        var promise = flowService.listMerges();
        promise.success(function (response, code) {
            if (code != 200) {
                alert('Failed to list all of the vs splits with error message: ' + response.message);
                return;
            }

            var vsIds = [];

            for (var i = 0; i < response.length; i++) {
                if (response[i] && response[i]['source-vs-id']) {
                    vsIds = vsIds.concat(response[i]['source-vs-id']);
                }
                if (response[i] && response[i]['new-vs-id']) {
                    vsIds.push(response[i]['new-vs-id']);
                }
            }

            var promise = [];

            var lists = [];

            response.forEach(function (v) {
                const obj = $.extend(true, {}, vsMergeObj);
                const responseTmp = v;

                promise.push(vsservice.getVsByIds(vsIds).then(function (data) {
                    var vses = {};
                    $.each(vsIds, function (i, r) {
                        if (data.vses && data.vses[r]) {
                            vses[r] = data.vses[r];
                        } else {
                            vses[r] = {id: r, name: "已下线"};
                        }
                    });
                    obj.toMerge(responseTmp, vses);

                    return obj;
                }));
            });

            $q.all(promise).then(function (data) {
                lists = data;

                $scope.data.results = lists;
                $scope.data.mergesCounts = _.countBy(lists, function (v) {
                    return v.status;
                });
                $scope.data.total = lists.length;

                table.bootstrapTable('hideLoading');
                table.bootstrapTable('load', lists);
            })
        });
    };

    $scope.hashChanged = function (hashData) { $scope.resource = H.resource;
        vsservice = VsesService.create($http, $q);
        flowService = VsSeperateService.create($http);

        var env = 'uat';
        // env
        if (hashData.env) {
            env = hashData.env;
        }
        $scope.env = env;
        $scope.initTable([]);
        $scope.loadData();
    };

    H.addListener("selfInfoApp", $scope, $scope.hashChanged);
});
angular.bootstrap(document.getElementById("self-info-area"), ['selfInfoApp']);