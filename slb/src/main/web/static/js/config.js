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
            case 'role': {
                link = "/portal/backend/userroles#?env=" + G.env;
                break;
            }
            case 'config': {
                link = "/portal/backend/config#?env=" + G.env;
                break;
            }
            case 'access': {
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


var configInfoApp = angular.module('configInfoApp', ["http-auth-interceptor"]);
configInfoApp.controller('configInfoController', function ($scope, $http, $q) {
    $scope.data = {
        configs: []
    };
    var configurations = [];

    $scope.query = {};

    $scope.initTables = function () {
        $('#configs').bootstrapTable({
            toolbar: "#config-table-toolbar",
            columns: [[
                {
                    field: 'state',
                    checkbox: 'true',
                    align: 'center',
                    valign: 'middle'
                },

                {
                    field: 'property-key',
                    title: 'Key',
                    align: 'left',
                    valign: 'middle',
                    sortable: true
                },
                {
                    field: 'property-value',
                    title: 'Value',
                    align: 'left',
                    valign: 'middle',
                    sortable: true
                },

                {
                    title: '操作',
                    align: 'center',
                    width: '180px',
                    valign: 'middle',
                    events: operateEvents,
                    formatter: function (value, row, index) {
                        var str = '<div class="row">';
                        str += '<div class="col-md-12 operations">' +
                            '<a class=""><i class="edit fa fa-pencil-square-o status-blue" aria-hidden="true"></i></a> ' +
                            '<a class=""><i class="remove fa fa-minus-circle status-red" aria-hidden="true"></i></a> ';

                        if (index == configurations.length - 1) {
                            str += '<a class=""><i class="add fa fa-plus-circle status-blue" aria-hidden="true"></i></a> ';
                        }

                        str += '</div>';
                        str += '</div>';

                        return str;
                    }
                }
            ], []],
            sortName: 'key',
            sortOrder: 'desc',
            data: [],
            classes: "table-bordered  table-hover table-striped table",
            search: true,
            minimumCountColumns: 2,
            idField: 'key',
            sidePagination: 'client',
            formatLoadingMessage: function () {
                return "<img class='loading-img' src='/static/img/loading_100.gif' /> 正在加载...";
            },
            formatNoMatches: function () {
                return '<i class="fa status-alert fa-exclamation-triangle" aria-hidden="true"></i> 没有任何配置...';
            }
        });
    };
    var selecteRow = '';
    var option;
    window.operateEvents = {
        'click .edit': function (e, value, row) {
            option = 'edit';
            $scope.$apply(function () {
                $scope.query.currentConfig = {
                    propertyKey: row['property-key'],
                    propertyValue: row['property-value']
                };
            });
            $('#newKvDialog').modal('show');
        },
        'click .add': function (e, value, row) {
            option = 'new';

            $scope.$apply(function () {
                $scope.query.currentConfig = {
                    key: '',
                    value: ''
                };
            });
            $('#newKvDialog').modal('show');
        },
        'click .remove': function (e, value, row) {
            // selecteRow = row;
            $scope.$apply(function () {
                $scope.query.currentConfig = {
                    propertyKey: row['property-key'],
                    propertyValue: row['property-value']
                };
            });
            $('#confirmDeleteDialog').modal('show');
        }
    };

    $('#configs').on('check.bs.table uncheck.bs.table check-all.bs.table uncheck-all.bs.table', function () {
        $('#batchEdit').prop('disabled', !$('#configs').bootstrapTable('getSelections').length);
        $('#batchDelete').prop('disabled', !$('#configs').bootstrapTable('getSelections').length);
    });

    $scope.newKvClick = function () {
        $scope.query.batchContent = '';
        $('#batchNewKvDialog').modal('show');
    };

    $scope.saveConfig = function () {
        if (option == 'new') {
            $scope.batchInsertSave();
            return;
        }
        var current = $scope.query.currentConfig;

        $http.post(absolutePathOf("/api/config/batchupdate"), [current]).then(function (response) {
            var resp = response;
            if (200 === resp.status) {
                // update succeed
                $("#newKvDialog").modal("hide");
                $scope.loadData();
            }
            console.log(resp);
        });
    };

    $scope.confirmBatchDelete = function () {
        $('#confirmBatchDeleteDialog').modal("hide");
        $http.get(absolutePathOf("/api/config/batchdelete"), {
            params: {
                key: $scope.rmKeys
            }
        }).then(function (resp) {
            if (200 === resp.status) {
                showMessageDialog("批量删除", "批量删除成功");
            } else {
                showMessageDialog("批量删除失败", resp.statusText);
            }
            $scope.loadData();
        });
    };

    $scope.confirmRemove = function () {
        var chosenConfig = $scope.query.currentConfig;
        var params = {
            key: [chosenConfig.propertyKey]
        };
        $http.get(absolutePathOf("/api/config/batchdelete"), {params: params}).then(function (response) {
            if (200 === response.status) {
                $("#confirmDeleteDialog").modal("hide");
                $scope.loadData();
            } else {
                // todo deal with delete failure situation
            }
        });
    };

    $scope.batchEdit = function () {
        selecteRow = $('#configs').bootstrapTable('getSelections');
        var configs = [];
        _.map(selecteRow, function (v) {
            configs.push(v['property-key'].trim() + "=" + v['property-value'].trim());
        });
        $scope.query.batchContent = configs.join('\n');

        $('#batchEditDialog').modal('show');
    };

    $scope.showBatchError = function () {
        var content = $scope.query.batchContent;
        if (!content) return true;
        var contentArray = content.split('\n');

        var passed = true;
        var map = {};
        for (var i = 0; i < contentArray.length; i++) {
            if (!contentArray[i]) continue;

            var current = contentArray[i].split('=');
            if (current.length === 1 || map[current[0].trim()]) {
                passed = false;
                break;
            } else {
                map[current[0].trim()] = 1;
            }
        }
        return !passed;
    };

    $scope.batchDelete = function () {
        var chosenRows = $("#configs").bootstrapTable("getSelections");
        $scope.rmKeys = _.pluck(chosenRows, "property-key");
        $('#confirmBatchDeleteDialog').modal('show');
    };

    var content2KvPairs = function (content) {
        if (!content) return [];

        var contentArray = content.split('\n');
        return _.map(contentArray, function (kvPairContent) {
            var tokens = kvPairContent.split('=');
            var trimTokens = _.map(tokens, function (token) {
                return token.trim();
            });
            return {
                'property-key': trimTokens[0],
                'property-value': trimTokens[1]
            };
        });
    };

    $scope.batchEditSave = function () {
        var content = $scope.query.batchContent;
        if (!content) {
            return;
        }

        var configs = content2KvPairs(content);
        $http.post(absolutePathOf("/api/config/batchupdate"), configs).then(function (resp) {
            if (200 === resp.status) {
                $("#batchEditDialog").modal("hide");
                $scope.loadData();
            } else {
                showMessageDialog("批量修改失败", resp.statusText);
            }
        });
    };

    $scope.batchInsertSave = function () {
        var content = $scope.query.currentConfig;

        $http.post(absolutePathOf("/api/config/batchinsert"), [content]).then(function (resp) {
            if (200 === resp.status) {
                $("#newKvDialog").modal("hide");
                console.log("batch insert succeed");
                $scope.loadData();
            } else {
                var errorMessage = resp.statusText;
                showMessageDialog("新增配置失败", errorMessage);
            }
        });
    };

    var showMessageDialog = function (title, body) {
        var modalInstance = $("#errorMessageModal");
        modalInstance.find('.modal-title').text(title);
        modalInstance.find('.modal-body').text(body);
        modalInstance.modal("show");
    };

    $scope.loadData = function (hashData) {
        $http.get(absolutePathOf("/api/config/all")).then(function (response) {
            configurations = response.data;
            var code = response.status;
            if (!configurations || configurations.length == 0 || code != 200) {
                configurations = [];
                configurations.push({});
            } else {
                var result = [];
                var arrary = configurations.split('\n');
                for (var i = 0; i < arrary.length; i++) {
                    if (!arrary[i] || !arrary[i].trim()) continue;

                    var firstIndex = arrary[i].indexOf('=');

                    var error = arrary[i] + '  is not in expected format. key=value. Will ignore this';
                    var key = arrary[i].substring(0, firstIndex);
                    var value = arrary[i].substring(firstIndex + 1, arrary[i].length);

                    if (firstIndex == -1 || firstIndex == 0 || !key || !key.trim() || !value || !value.trim()) {
                        alert(error);
                        continue;
                    }
                    result.push({
                        'property-key': key,
                        'property-value': value
                    });

                    configurations = result;
                }
            }

            $('#configs').bootstrapTable('removeAll');
            $('#configs').bootstrapTable("load", configurations);
        });
    };

    var absolutePathOf = function (relativePath) {
        if (relativePath.charAt(0) !== "/") {
            relativePath = "/" + relativePath;
        }
        return G.baseUrl + relativePath;
    };

    $scope.env = '';

    $scope.hashChanged = function (hashData) {
        $scope.env = hashData.env;
        $scope.initTables();
        $scope.loadData(hashData);
    };
    H.addListener("configInfoApp", $scope, $scope.hashChanged);
});
angular.bootstrap(document.getElementById("config-info-area"), ['configInfoApp']);

//
// var queryInfoApp = angular.module('queryInfoApp', ["angucomplete-alt", "http-auth-interceptor"]);
// queryInfoApp.controller('queryInfoController', function ($scope, $http, $q) {
//
//     $scope.query = {};
//
//     $scope.cacheRequestFn = function (str) {
//         return {q: str, timestamp: new Date().getTime()};
//     };
//
//     $scope.remoteKeysUrl = function () {
//         return G.baseUrl + "/api/meta/config/keys";
//     };
//
//     $scope.selectKey = function (o) {
//         if (o) {
//             $scope.query.key = o.originalObject.name;
//         }
//     };
//
//     $scope.keyInputChanged = function (o) {
//         $scope.query.key = o;
//     };
//
//     $scope.executeQuery=function () {
//         var pair ={
//             timeStamp: new Date().getTime()
//         };
//
//         if($scope.query.key){
//             pair.key =$scope.query.key;
//         }
//
//         if($scope.query.value){
//             pair.value =$scope.query.value;
//         }
//
//         H.setData(pair);
//     };
//
//     $scope.clearQuery=function () {
//         $scope.query={};
//     };
//
//     $scope.applyHashData = function (hashData) {
//         if (hashData.key) {
//             $scope.query.key = hashData.key;
//         } else {
//             $scope.query.key = '';
//         }
//
//         $scope.env = hashData.env || 'pro';
//
//         if (hashData.value) {
//             $scope.query.value = hashData.value;
//         } else {
//             $scope.query.value = '';
//         }
//
//         // set value for auto-complete
//         $('#keySelector_value').val($scope.query.key);
//     };
//
//     $scope.env = '';
//
//     $scope.hashChanged = function (hashData) {
//         $scope.applyHashData(hashData);
//     };
//     H.addListener("queryInfoApp", $scope, $scope.hashChanged);
// });
// angular.bootstrap(document.getElementById("query-info-area"), ['queryInfoApp']);




