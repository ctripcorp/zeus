//VS-edit Component
var vsEditApp = angular.module('vsEditApp', ["angucomplete-alt", 'http-auth-interceptor']);
vsEditApp.controller('vsEditController', function ($scope, $http, $q) {
    $scope.query = {
        slbId: '',
        vsId: '',
        targetVsId: ''
    };
    $scope.view = {
        targetSlbs: [],
        targetVs: {},
        slbs: []
    };
    $scope.vsInfo = {
        "slb-id": "",
        "id": "",
        "name": "",
        "ssl": false,
        "port": "80",
        "domains": [{"name": ''}]
    };

    //Load cache
    $scope.cacheRequestFn = function (str) {
        return {q: str, timestamp: new Date().getTime()};
    };
    $scope.remoteSLBsUrl = function () {
        return G.baseUrl + "/api/meta/slbs";
    };
    $scope.newSlbId = undefined;
    $scope.newSlbName = undefined;
    $scope.selectSlbId = function (o) {
        if (o) {
            $scope.newSlbId = o.originalObject.id;
            $scope.newSlbName = o.originalObject.name;
        }
    };

    $scope.clearVSInfo = function (type) {
        window.location.reload();
    };
    $("#vs-ssl").click(function (event) {
        if ($scope.view.targetVs.ssl) {
            $scope.view.targetVs.port = "80";
        } else {
            $scope.view.targetVs.port = "443";
        }
    });
    $scope.changeVsName = function () {
        var port = '80';
        if ($scope.view.targetVs.port) {
            port = $scope.view.targetVs.port;
        }
        $scope.view.targetVs.name = $("#vs-domain0").val() + "_" + port;
    };

    $('.backLink').click(function () {
        window.history.back();
    });

    /**VS related slbs**/
    var tobeRemoveSlbs = [];
    $scope.popNewSLB = function () {
        $scope.newSlbId = undefined;
        $('#addNewSlbDialog').modal('show');
    };
    $scope.popUpdateSLB = function (row) {
        $('#slbIdSelector2_value').val(row.id.toString());
        $scope.newSlbId = undefined;
        $scope.oldSlbId = row.id;
        $('#updateSlbDialog').modal('show');
    };
    $scope.popDeleteSLB = function (row) {
        tobeRemoveSlbs = [];
        $('.to-be-removed-slbs').html('');
        var str = '';
        if (row) {
            tobeRemoveSlbs.push(row);
            str += '<tr><td>' + row.id + '</td><td>' + row.name + '</td></tr>';
        } else {
            var s = $('#vs-target-slbs-table').bootstrapTable('getSelections');
            $.each(s, function (index, item) {
                tobeRemoveSlbs.push(item);
                str += '<tr><td>' + item.id + '</td><td>' + item.name + '</td></tr>';
            });
        }
        $('.to-be-removed-slbs').append(str);
        $('#removeSlbConfirmdialog').modal('show');
    };
    $scope.confirmRemoveSlb = function () {
        $.each(tobeRemoveSlbs, function (i, item) {
            var index = _.findIndex($scope.view.targetSlbs, function (item2) {
                return item2.id == item.id;
            });
            $scope.view.targetSlbs.splice(index, 1);
            $scope.reloadTable();
        });
    };
    window.operateEvents = {
        'click .deleteSlb': function (e, value, row, index) {
            $scope.popDeleteSLB(row);
        },
        'click .updateSlb': function (e, value, row, index) {
            $scope.popUpdateSLB(row);
        }
    };
    $scope.addNewSlbToVs = function () {
        if (!$scope.newSlbId) {
            alert('请选择你要关联的SLB ID');
        } else {
            var exist = _.find($scope.view.targetSlbs, function (item) {
                return item.id == $scope.newSlbId;
            });
            if (!exist) {
                $scope.view.targetSlbs.push(
                    {
                        id: $scope.newSlbId,
                        name: $scope.newSlbName
                    }
                );
            }
            $scope.reloadTable();
            $('#addNewSlbDialog').modal('hide');
        }
    };
    $scope.updateSlbForVs = function () {
        if (!$scope.newSlbId) {
            alert('请选择你要关联的SLB ID!');
        } else {
            var exist = _.find($scope.view.targetSlbs, function (item) {
                return item.id == $scope.newSlbId;
            });
            if (!exist) {
                var index = _.findIndex($scope.view.targetSlbs, function (item2) {
                    return item2.id == $scope.oldSlbId;
                });
                $scope.view.targetSlbs.splice(index, 1);
                $scope.view.targetSlbs.push(
                    {
                        id: $scope.newSlbId,
                        name: $scope.newSlbName
                    }
                );
            }
            $scope.reloadTable();
            $('#updateSlbDialog').modal('hide');
        }
    };

    /**
     * Domain Related operations area
     * **/
    $scope.domainsTable = {
        columns: $scope.vsInfo.domains,
        add: function (index) {
            this.columns.push({"name": ''});
            setTimeout(function () {
                $("#editDomain" + (index + 1)).hide();
                $("#vsdomains" + (index + 1)).prop('disabled', false);
                $("#vsdomains" + (index + 1)).bootstrapValidation();
            }, 10);
        },
        remove: function (index) {
            this.columns.splice(index, 1);
        },
        edit: function (index) {
            $("#editDomain" + (index)).hide();
            $("#vsdomains" + (index)).prop('disabled', false);
            $("#vsdomains" + (index)).bootstrapValidation();
        }
    };

    /**Save vs area**/
    $("#validateEditVSBtn").click(function (event) {
        var reviewResult = reviewData();
        if (reviewResult) {
            $('#confirmEditVS').modal({backdrop: 'static'});
        }
    });
    $("#validateAddVSBtn").click(function () {
        var reviewResult = reviewData();
        if (reviewResult) {
            $('#confirmAddVS').modal({backdrop: 'static'});
        }
    });
    $scope.newVS = function () {
        var resource = $scope.resource;
        if (!resource || !resource['vsnew']['vsnew_vsEditApp_newVsDialog']) {
            return;
        }

        var progressStatus = resource['vsnew']['vsnew_vsEditApp_newVsDialog'];

        var slbArray = [];
        var slbids = $.extend(true, [], $scope.view.targetSlbs);
        $.each(slbids, function (i, item) {
            slbArray.push(parseInt(item.id));
        });
        $scope.view.targetVs.domains = $scope.domainsTable.columns;
        $scope.view.targetVs['slb-ids'] = slbArray;
        if (!$scope.view.targetVs.port) {
            $scope.view.targetVs.port = 80;
        }
        $scope.view.targetVs.ssl = false;
        if ($('#vs-ssl').prop('checked')) {
            $scope.view.targetVs.ssl = true;
        }

        delete $scope.view.targetVs['slb-id'];
        delete $scope.view.targetVs.id;

        var req = {
            method: 'POST',
            url: G.baseUrl + '/api/vs/new?description=' + $scope.query.reason,
            data: $scope.view.targetVs
        };
        $scope.newVsResponseResult = progressStatus["正在添加"];
        $scope.newVsResponseInfo = progressStatus["正在创建Virtual Server"] + "...";
        $('#confirmAddVS').modal('hide');
        $('#newVsDialog').modal('show');
        $http(req).success(
            function (res) {
                if (!res.code) {
                    $scope.newVsResponseResult = progressStatus["添加成功"];
                    $scope.newVsResponseInfo = progressStatus["正在跳转至查看页面"] + "...";
                    $('#confirmAddVS').modal('hide');
                    $('#newVSDialog').modal('show');
                    setTimeout(function () {
                        window.location.href = "/portal/vs" + "#?env=" + G.env + "&vsId=" + res.id;
                    }, 500);
                } else {
                    $scope.newVsResponseResult = progressStatus["添加失败"];
                    $scope.newVsResponseInfo = res.message;
                }
            }
        );
    };
    $scope.updateVS = function () {
        var resource = $scope.resource;
        if (!resource || !resource['vsnew']['vsnew_vsEditApp_newVsDialog']) {
            return;
        }
        var progressStatus = resource['vsnew']['vsnew_vsEditApp_newVsDialog'];


        var slbArray = [];
        var slbids = $.extend(true, [], $scope.view.targetSlbs);
        $.each(slbids, function (i, item) {
            slbArray.push(parseInt(item.id));
        });
        $scope.view.targetVs.domains = $scope.domainsTable.columns;
        $scope.view.targetVs['slb-ids'] = slbArray;
        if (!$scope.view.targetVs.port) {
            $scope.view.targetVs.port = 80;
        }
        $scope.view.targetVs.ssl = false;
        if ($('#vs-ssl').prop('checked')) {
            $scope.view.targetVs.ssl = true;
        }

        delete $scope.view.targetVs['slb-id'];
        var req = {
            method: 'POST',
            url: G.baseUrl + '/api/vs/update?description=' + $scope.query.reason,
            data: $scope.view.targetVs
        };
        $scope.editVsResponseResult = progressStatus["正在更新"];
        $scope.editVsResponseInfo = progressStatus["正在更新当前Virtual Server"] + "...";
        $('#confirmEditVS').modal('hide');
        $('#editVsDialog').modal('show');
        $http(req).success(
            function (res) {
                if (!res.code) {
                    $scope.editVsResponseResult = progressStatus["更新成功"];
                    $scope.editVsResponseInfo = progressStatus["正在跳转至查看页面"] + "...";
                    setTimeout(function () {
                        window.location.href = "/portal/vs" + "#?env=" + G.env + "&vsId=" + res.id;
                    }, 500);
                } else {
                    $scope.editVsResponseResult = progressStatus["更新失败"];
                    $scope.editVsResponseInfo = res.message;
                }
            }
        );
    };

    // Common Functions
    function reviewData() {
        var result = true;
        $.each($('[data-validator-type="validation"]'), function (i, element) {
            if (!$(element).bootstrapValidation('validate'))
                result = false;
        });
        return result;
    }

    /**Load table and data area
     * **/
    $scope.getSlb = function (slbId) {
        $scope.view.targetSlb = [];
        var param = {
            slbId: slbId
        };
        var request = {
            method: 'GET',
            url: G.baseUrl + '/api/slb',
            params: param
        };
        $http(request).success(
            function (res) {
                $('.main-container').hideLoading();
                res.id = res.id.toString();
                $scope.view.targetSlbs.push({
                    id: res.id,
                    name: res.name
                });
                $scope.reloadTable();
            }
        );
    };
    $scope.getVs = function (vsId) {
        $scope.view.targetVs = {};
        var param = {
            vsId: vsId
        };
        var request = {
            method: 'GET',
            url: G.baseUrl + '/api/vs',
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
        $q.all([
            $http(request).success(
                function (res) {
                    $('.main-container').hideLoading();
                    $scope.view.targetVs = res;
                }
            ),
            $http(request2).success(
                function (res) {
                    $scope.view.slbs = res.slbs;
                }
            )
        ]).then(
            function () {
                var slbs = [];
                $.each($scope.view.targetVs['slb-ids'], function (i, item) {
                    var name = _.find($scope.view.slbs, function (item2) {
                        return item2.id == item;
                    }).name;

                    slbs.push(
                        {
                            id: item,
                            name: name
                        }
                    );
                });
                $scope.view.targetSlbs = slbs;
                $scope.domainsTable.columns = $scope.view.targetVs.domains;
                $scope.reloadTable();
            }
        );

    };
    $scope.loadData = function (hashData) {
        $scope.query.slbId = hashData.slbId;
        $scope.query.targetVsId = hashData.targetVsId;
        $scope.query.vsId = hashData.vsId;

        $scope.view.targetVs.port = 80;
        if ($scope.env != hashData.env) {
            $scope.env = hashData.env;
        }
        if ($scope.query.vsId) {
            // Edit
            $scope.getVs($scope.query.vsId);
            $scope.view.targetSlbs = [];
        } else {
            if (!$scope.query.slbId && !$scope.query.targetVsId) {
                // new vs
                $scope.view.targetSlbs = [];
                $('.main-container').hideLoading();
            } else {
                if ($scope.query.targetVsId) {
                    $scope.getVs($scope.query.targetVsId);

                } else {
                    if ($scope.query.slbId) {
                        $scope.getSlb($scope.query.slbId);
                    }
                }
            }
        }
    };
    $scope.initTable = function () {
        $('#vs-target-slbs-table').bootstrapTable({
            toolbar: ".slb-list-tool",
            columns: [[
                {
                    checkbox: 'true',
                    align: 'center',
                    valign: 'middle'
                },

                {
                    field: 'id',
                    title: 'Slb Id',
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    formatter: function (value, row, index) {
                        return value;
                    }
                },
                {
                    field: 'name',
                    title: 'SLB Name',
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    formatter: function (value, row, index) {
                        return '<span style="word-break: break-all">' + value + '</span>';
                    }
                },
                {
                    field: 'operation',
                    title: 'Operation',
                    align: 'center',
                    valign: 'middle',
                    width: '150px',
                    events: operateEvents,
                    formatter: function (value, row, index) {
                        var str = '<button data-toggle="tooltip" type="button" class="btn deleteSlb" title="删除" aria-label="Left Align"><span class="fa fa-minus"></span></button>';
                        str += '  <button data-toggle="tooltip" title="修改" type="button" class="btn btn-info updateSlb" aria-label="Left Align">'
                            + '<span class="fa fa-edit"></span>' +
                            '</button>';
                        return str;
                    }
                }
            ], []],
            data: $scope.view.targetSlbs,
            sortOrder: 'desc',
            classes: "table-bordered  table-hover table-striped table",
            minimumCountColumns: 2,
            idField: 'id',
            sidePagination: 'client',
            pagination: false,
            pageSize: 10,
            pageList: [10, 30, 50, 100],
            formatLoadingMessage: function () {
                return "<img class='loading-img' src='/static/img/loading_100.gif' /> Loading SLBs";
            },
            formatNoMatches: function () {
                return '<i class="fa status-alert fa-exclamation-triangle" aria-hidden="true"></i> No SLB';
            }
        });
    };
    $('#vs-target-slbs-table').on('check.bs.table uncheck.bs.table ' +
        'check-all.bs.table uncheck-all.bs.table', function () {
        $('#vs-batch-delete-slb-bt').prop('disabled', !$('#vs-target-slbs-table').bootstrapTable('getSelections').length);
    });
    $scope.reloadTable = function () {
        $('#vs-target-slbs-table').bootstrapTable("load", $scope.view.targetSlbs);
    };
    $scope.hashChanged = function (hashData) {
        $scope.resource = H.resource;
        $scope.initTable();
        $('.main-container').showLoading();
        $scope.loadData(hashData);
    };
    H.addListener("vsEditApp", $scope, $scope.hashChanged);

    // Common Functions
    function reviewData() {
        var result = true;
        $.each($('[data-validator-type="validation"]'), function (i, element) {
            if (!$(element).bootstrapValidation('validate'))
                result = false;
        });
        return result;
    }
});
angular.bootstrap(document.getElementById("vs-edit-area"), ['vsEditApp']);

var vsEditDropdownApp = angular.module('vsEditDropdownApp', ["angucomplete-alt", "http-auth-interceptor"]);
vsEditDropdownApp.controller('vsEditDropdownController', function ($scope, $http) {
    $scope.context = {
        targetIdName: 'vsId',
        targetNameArr: ['id', 'name'],
        targetsUrl: '/api/meta/vses',
        targetsName: 'virtual-servers'
    };
    $scope.target = {
        id: '',
        pair: {}
    };
    $scope.targets = {};
    $scope.cacheRequestFn = function (str) {
        return {q: str, timestamp: new Date().getTime()};
    };
    $scope.remoteUrl = function () {
        return G.baseUrl + "/api/meta/vses";
    };

    $scope.getAllTargets = function () {
        var c = $scope.context;
        $http.get(G.baseUrl + c.targetsUrl).success(
            function (res) {
                if (res.length > 0) {
                    $.each(res, function (i, val) {
                        $scope.targets[val.id] = val;
                    });
                }
                if ($scope.target.id) {
                    if ($scope.targets[$scope.target.id])
                        $scope.target.name = $scope.targets[$scope.target.id].id + "/" + $scope.targets[$scope.target.id].name;
                    else {
                        $http.get(G.baseUrl + "/api/vs?vsId=" + $scope.target.id + "&type=info").success(
                            function (res) {
                                $scope.target.name = $scope.target.id + "/" + res.name;
                            }
                        );
                    }
                }
            }
        );
    };

    $scope.cloneVsClick = function () {
        $scope.getAllTargets();
        $('#cloneVSModal').modal('show');
    };
    $scope.saveTargetVs = function () {
        $scope.target.id = $('.selected-target-id').text();
        H.setData($scope.target.pair);
    };
    $scope.selectTarget = function (t) {
        if (t) {
            var toId = t.originalObject.id;
            var timestamp = new Date().getTime();
            var pairs = {};
            if ($scope.target.id != toId) {
                $scope.target.id = toId;
                pairs['targetVsId'] = toId;
                pairs["timestamp"] = timestamp;

                $scope.target.pair = pairs;
            }
            $('#targetSelector').val(t.originalObject.id);
        }
    };

    $scope.hashChanged = function (hashData) {
        $scope.resource = H.resource;
        $scope.resource = H.resource;
        if (hashData.env) {
            $scope.env = hashData.env;
        }
    };
    H.addListener("vsEditDropdownApp", $scope, $scope.hashChanged);
});
angular.bootstrap(document.getElementById("vs-edit-dropdown-area"), ['vsEditDropdownApp']);