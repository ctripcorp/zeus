var headerInfoApp = angular.module('headerInfoApp', ["angucomplete-alt"]);
headerInfoApp.controller('headerInfoController', function ($scope, $http) {
    $scope.resource = H.resource;
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
    $scope.resource = H.resource;
    var resource= $scope.resource;
    $scope.data = {};

    var flowService;
    var vsservice;

    var table = $('#sperate-table');

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
                            'href="/portal/tools/vs/seperate-edit#?env=' + G.env + '&id=' + value + '">' + value + '(' + row.name + ')</a>';
                    }
                },
                {
                    field: 'create-time',
                    title: (angular.equals(resource, {}) ? '创建日期': resource.tools.vseparates.js.msg1),
                    align: 'left',
                    valign: 'middle',
                    sortable: true
                },
                {
                    title: (angular.equals(resource, {}) ? '待拆分VS': resource.tools.vseparates.js.msg2),
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    formatter: function (value, row, index) {
                        var originVs = row.getOriginVsData();
                        var id = originVs.id;
                        var name = originVs.name;
                        return '<a class="editVS"' +
                            ' target="_self" ' +
                            'title="' + name + '"' +
                            ' href="/portal/vs#?env=' + G.env + '&vsId=' + id + '">VSID：' + id + '(' + name + ')</a>';
                    }
                },
                {
                    title: (angular.equals(resource, {}) ? '目标VS': resource.tools.vseparates.js.msg3),
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    formatter: function (value, row, index) {
                        var targetVs = row.getCreatedVs();
                        var str = '';
                        $.each(targetVs, function (i, v) {
                            str += '<div><a class="editVS"' +
                                ' target="_self" ' +
                                'title="' + v.name + '"' +
                                ' href="/portal/vs#?env=' + G.env + '&vsId=' + i + '">VSID：' + i + '(' + v.name + ')</a></div>';
                        });
                        return str;

                        //
                        // var str = '';
                        // if (targetVs && _.keys(targetVs) > 0) {
                        //     $.each(_.keys(targetVs), function (i, v) {
                        //         str += '<div><a class="editVS"' +
                        //             ' target="_blank" ' +
                        //             'title="' + value + '"' +
                        //             ' href="/portal/vs#?env=' + G.env + '&vsId=' + v.id + '">' + v.id + '</a></div>';
                        //     });
                        // } else {
                        //     str = '-';
                        // }
                        //
                        // return str;
                    }
                },
                {
                    field: 'status',
                    title: (angular.equals(resource, {}) ? '状态': resource.tools.vseparates.js.msg4),
                    width: '120px',
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    formatter: function (value, row, index) {
                        var sclass = 'status-green';
                        var text = value;
                        if (text.indexOf('失败') != -1) sclass = 'status-red';
                        else if (text.indexOf('正在') != -1) sclass = 'status-yellow';

                        return '<span class="' + sclass + '">' + resource.tools.vseparates.js[text] + '</span>';
                    }
                },
                {
                    title: (angular.equals(resource, {}) ? '操作': resource.tools.vseparates.js.msg5),
                    align: 'left',
                    valign: 'middle',
                    width: '80px',
                    events: operateEvents,
                    sortable: true,
                    formatter: function (value, row, index) {
                        var str = '<div class="operation"><a class="pull-left" href="/portal/tools/vs/seperate-edit#?env=' +
                            '' + $scope.env + '&id=' + row.id + (angular.equals(resource, {}) ? '">编辑</a>': resource.tools.vseparates.js.msg6) +
                            (angular.equals(resource, {}) ? ' <a class="pull-right remove-split">删除</a></div>': resource.tools.vseparates.js.msg7);
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
            resizeMode: 'overflow', formatLoadingMessage: function () {
                return (angular.equals(resource, {}) ? "<img class='loading-img' src='/static/img/loading_100.gif' /> 正在加载 所有VS拆分": resource.tools.vseparates.js.msg8);
            },
            formatNoMatches: function () {
                return (angular.equals(resource, {}) ? '<i class="fa status-alert fa-exclamation-triangle" aria-hidden="true"></i> 没有找到匹配的VS拆分记录': resource.tools.vseparates.js.msg9);
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
        $('#progressDialog').modal('show').find('#progress').html((angular.equals(resource, {}) ? "正在删除.. <img src='/static/img/spinner.gif' />": resource.tools.vseparates.js.msg10));

        var promise = flowService.deleteSplit(currentId);
        promise.success(function (response, code) {
            if (code == 200) {
                $('#progressDialog').modal('show').find('#progress').html((angular.equals(resource, {}) ? "删除成功": resource.tools.vseparates.js.msg11));
                startTimer($('#progressDialog'));
            } else {
                $('#progressDialog').modal('show').find('#progress').html((angular.equals(resource, {}) ? "<span class='warning-important'>删除失败</span>": resource.tools.vseparates.js.msg12));
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

    $scope.startNewSeperate = function () {
        // var hasRight = A.canDo('Flow', 'NEW', '*') || A.canDo('Flow', 'FLOW', '*');
        // if (!hasRight) {
        //     alert('还没有权限，权限申请流程正在建设中...');
        //     return;
        // }

        window.location.href = '/portal/tools/vs/seperate-edit#?env=' + $scope.env;
    };

    function startTimer(dialog) {
        setTimeout(function () {
            dialog.find('.closeProgressWindowBt').click();
        }, 2000);
    }

    $scope.loadData = function () {
        $scope.data.vsSeperates = {};
        var promise = flowService.listSplits();
        promise.success(function (response, code) {
            var results = [];

            if (code != 200) {
                alert('Failed to list all of the vs splits with error message: ' + response.message);
                return;
            }
            var promises = [];
            $.each(response, function (i, v) {
                var obj = $.extend(true, {}, vsSeperateObj);

                var entity = obj.toEntity(v);
                var vsIds = v['new-vs-ids'].concat(v['source-vs-id']);
                promises.push(getVsesDetail(vsIds, entity, v['source-vs-id']).then(function (data) {
                    results.push(data);
                }));
            });
            $q.all(promises).then(function () {
                $scope.data.results = results;

                $scope.data.seperateCounts = _.countBy(results, function (v) {
                    return v.status;
                });
                table.bootstrapTable('hideLoading');
                table.bootstrapTable('load', results);
            })
        });
    };

    function getVsesDetail(vsIds, entity, sVsId) {

        return vsservice.getVsByIds(vsIds).then(function (data) {
            var vsData = {};
            vsIds.forEach(function (id) {
                if (data.vses && data.vses[id] != undefined) {
                    vsData[id] = data.vses[id]
                } else {
                    vsData[id] = {id: id, name: (angular.equals(resource, {}) ? "已下线": resource.tools.vseparates.js.msg13)}
                }
            });
            entity.setCreatedVs(vsData);
            if (data.vses && data.vses[sVsId] != undefined) {
                entity.setOriginVsData(data.vses[sVsId])
            } else {
                entity.setOriginVsData({id: sVsId, name: (angular.equals(resource, {}) ? "已下线": resource.tools.vseparates.js.msg13)})
            }

            return entity;
        });
    }

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