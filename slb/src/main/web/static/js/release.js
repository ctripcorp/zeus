/**
 * Created by ygshen on 2016/10/10.
 */

var summaryInfoApp = angular.module('summaryInfoApp', ['http-auth-interceptor', 'angucomplete-alt']);
summaryInfoApp.controller('summaryController', function ($scope, $http, $q) {
    $scope.query = {};
    // Auto complete
    $scope.cacheRequestFn = function (str) {
        return {q: str, timestamp: new Date().getTime()};
    };
    $scope.remoteUrl = function () {
        return G.baseUrl + "/api/meta/groups";
    };
    $scope.selectTarget = function (t) {
        if (t) {
            var toId = t.originalObject.id;
            if ($scope.target.id != toId) {
                $scope.$broadcast('angucomplete-alt:clearInput', 'targetSelector');
                var pairs = {};
                pairs['groupId'] = toId;
                $scope.target.id = toId;
                H.setData(pairs);
                messageNotify("切换Group. ", "成功切换至Group： " + toId, null);
            }
        }
    };
    $scope.setInputsDisplay = function () {
        $('#targetSelector_value').val($scope.query.groupId);
    };
    $scope.getAllTargets = function () {
        $http.get(G.baseUrl + "/api/group?groupId=" + $scope.query.groupId + "&type=info").success(
            function (res) {
                $scope.target = {};
                $scope.target.name = $scope.query.groupId + "/" + res.name;
                $('#targetSelector_value').val($scope.target.name);
            }
        );
    };
    $scope.hashChanged = function (hashData) { $scope.resource = H.resource;
        $scope.resource = H.resource;
        if (hashData.env) {
            $scope.env = hashData.env;
        }
        if (hashData.groupId) {
            $scope.query.groupId = hashData.groupId;
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
            case 'basic': {
                link = "/portal/group#?env=" + G.env + "&groupId=" + $scope.query.groupId;
                break;
            }
            case 'log': {
                link = "/portal/group/log#?env=" + G.env + "&groupId=" + $scope.query.groupId;
                break;
            }
            case 'traffic': {
                link = "/portal/group/traffic#?env=" + G.env + "&groupId=" + $scope.query.groupId;
                break;
            }
            case 'release': {
                link = "/portal/group/release#?env=" + G.env + "&groupId=" + $scope.query.groupId;
                break;
            }
            case 'antibot': {
                link = "/portal/group/antibot#?env=" + G.env + "&groupId=" + $scope.query.groupId;
                break;
            }
            case 'intercept': {
                link = "/portal/group/intercept#?env=" + G.env + "&groupId=" + $scope.query.groupId;
                break;
            }
            case 'conf': {
                link = "/portal/group/conf#?env=" + G.env + "&groupId=" + $scope.query.groupId;
                break;
            }
            default:
                break;
        }
        return link;
    }
    $scope.hashChanged = function (hashData) { $scope.resource = H.resource;
        $scope.resource = H.resource;
        if (hashData.env) {
            $scope.env = hashData.env;
        }
        if (hashData.groupId) {
            $scope.query.groupId = hashData.groupId;
        }
    };
    H.addListener("infoLinksApp", $scope, $scope.hashChanged);
});
angular.bootstrap(document.getElementById("header-area"), ['headerInfoApp']);


//GroupLogComponent: group log
var releaseLogApp = angular.module('releaseLogApp', ['http-auth-interceptor']);
releaseLogApp.controller('releaseLogController', function ($scope, $http, $q) {
    $scope.paramName = "groupId";
    $scope.target = "group";
    $scope.tableId = '#release-log-table';
    $scope.data = {
        apps: '',
        group: '',
        groupName: '',
        owners: '',
        app: '',
        appId: ''
    };

    $scope.query={
        groupId:''
    };
    $scope.generateTargetLink = function () {
        return $scope.targetLink + "#?env=" + G.env + "&" + $scope.paramName + "=" + $scope[$scope.paramName];
    };
    $scope.initialize = function (groupId, getReleaseInfoFunc) {
        var params = {
            "groupId": groupId,
            'type': 'extended'
        };
        var request = {
            method: 'GET',
            url: G.baseUrl + '/api/group',
            params: params
        };

        var appsRequest = {
            method: 'GET',
            url: G.baseUrl + '/api/apps'
        };
        var appsRequest = $http(appsRequest).success(function (response, code) {
            $scope.data.apps = _.indexBy(response.apps, function (v) {
                return v['app-id'];
            });
        });

        var groupsRequest = $http(request).success(function (res) {
            $scope.data.group = res;
            var appId = res['app-id'];
            // call get tars information func
            var properties = _.indexBy(res['properties'], function (v) {
                return v.name.toLowerCase();
            });
            var cmsGroupId = properties['cmsgroupid'] ? properties['cmsgroupid'].value : undefined;
            cmsGroupId = cmsGroupId || groupId;
            getReleaseInfoFunc(cmsGroupId, appId);
        });

        $q.all([appsRequest, groupsRequest]).then(function () {
            var apps = $scope.data.apps;
            var group = $scope.data.group;

            var groupName = group ? group.name : '-';
            var appId = group ? group['app-id'] : '-';
            var appName = (apps && apps[appId]) ? appId+'('+apps[appId]['chinese-name']+')' : '-';

            var owners = _.map(group.tags, function (v) {
                v = v.toLowerCase();
                if (v.indexOf('owner_') != -1) return v.substring(6, v.length)
            });
            owners = _.reject(owners,function (v) {
                return v==undefined;
            });

            $scope.data.appId = appId;
            $scope.data.app = appName;
            $scope.data.owners = owners;
            $scope.data.groupName = groupName;
        });

    }
    $scope.publishHistories = [];
    var getTarsReleaseInfo = function (groupId, appId) {
        $scope.publishHistories = [];
        $('.release-summary').html("");
        var queryString = G[G.env].urls.tars + "/api/v1/applications/" + appId + "/deployments?group_id=" + groupId + "&format=jsonp";
        $.ajax({
            url: queryString,
            dataType: "jsonp",
            xhrFields: {
                withCredentials: false
            },
            success: function (res) {
                if (res.count > 0) {
                    $.each(res.results, function (index, item) {
                        $scope.publishHistories.push({
                            id: item.id,
                            owner: item.created_by,
                            time: item.config.created_at,
                            packagelocation: item.package.origin,
                            status: item.status,
                            category: item.category,
                            appid: item.package.application,
                            groupid: item.group.group_id
                        });
                    });
                }
                var s = _.filter($scope.publishHistories, function (item) {
                    return item.status == 'SUCCESS';
                }).length;
                var f = _.filter($scope.publishHistories, function (item) {
                    return item.status == 'FAILURE';
                }).length;

                var r = _.filter($scope.publishHistories, function (item) {
                    return item.status == 'REVOKED';
                }).length;

                var o = $scope.publishHistories.length - s - f - r;
                if (!$scope.publishHistories) {
                    $scope.publishHistories = [];
                }
                $($scope.tableId).bootstrapTable("load", $scope.publishHistories);
                $($scope.tableId).bootstrapTable("hideLoading");
                if ($scope.publishHistories.length > 0) {
                    var str = '当前应用共发布过 <b>' + $scope.publishHistories.length + '</b>次。其中成功 <b>' + s + ' </b>次,　失败 <b>' + f + ' </b>次, 回退 <b>' + r + ' </b>次, 正在发布 <b>' + o + '</b> 次';
                    $('.release-summary').html(str);
                }
            },
            error: function (err) {
                $scope.publishHistories = [];
                $($scope.tableId).bootstrapTable("load", $scope.publishHistories);
                $($scope.tableId).bootstrapTable("hideLoading");
            }
        });
    };
    $scope.initTable = function () {
        var table = $scope.resource["grouprelease"]["release_releaseLogApp_result"]["table"];
        $($scope.tableId).bootstrapTable({
            toolbar: "#release-log-toolbar",
            columns: [[
                {
                    field: 'id',
                    title: table.id,
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    formatter: function (value, row) {
                        var url = G[G.env].urls.tars + "#/view/deployments/single?app=" + row.appid + "&deployment=" + value;
                        return "<a href='" + url + "'>" + value + "</a>";
                    }
                },
                {
                    field: 'owner',
                    title: table.owner,
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    formatter: function (value, row) {
                        if (value)
                            return "<a href='/portal/user#?env=" + $scope.env + "&userId=" + value + "'>" + value + "</a>";
                        return '-';
                    }
                },
                {
                    field: 'time',
                    title: table.releasetime,
                    align: 'left',
                    valign: 'middle',
                    width: '460px',
                    sortable: true
                },
                {
                    field: 'category',
                    title: table.releasetype,
                    align: 'left',
                    valign: 'middle',
                    width: '460px',
                    sortable: true,
                    formatter: function (value, row) {
                        if(value=='scaleout') return '<i class="fa fa-expand" style="padding-right: 10px"></i><span>扩容发布</span>';

                        else{
                          return '<i class="fa fa-bullhorn" style="padding-right: 10px"></i><span>手动发布</span>';
                        }
                    }
                },
                {
                    field: 'status',
                    title: table.releaseresult,
                    align: 'center',
                    valign: 'middle',
                    sortable: true,
                    width: '170px',
                    events: releaseStatusEvent,
                    formatter: function (value, row) {
                        var icon = '';
                        var statusclass = '';
                        switch (value) {
                            case "SUCCESS": {
                                icon = table['success'];
                                statusclass = 'btn-info';
                                break;
                            }
                            case "FAILURE": {
                                icon = table['fail'];
                                statusclass = 'btn-danger';
                                break;
                            }
                            case "REVOKED": {
                                statusclass = 'btn-warning';
                                icon = table['revoke'];
                                break;
                            }
                            default: {
                                statusclass = 'btn-info';
                                icon = table['releasing'];

                                break;
                            }
                        }

                        return "<button type='button' class='btn " + statusclass + " status'>" + icon + "</button>";
                    }
                },
                {
                    field: 'packagelocation',
                    title: table.releasepackage,
                    align: 'center',
                    valign: 'middle',
                    width: '120px',
                    formatter: function (value) {
                        return '<a href="' + value + '" title="Download"><i class="fa fa-download" aria-hidden="true"></i></a>';
                    }
                }
            ], []],
            showRefresh: true,
            search: true,
            showColumns: true,
            data: $scope.publishHistories,
            classes: "table-bordered  table-hover table-striped table",
            minimumCountColumns: 2,
            pageSize: 20,
            sidePagination: 'client',
            pageList: [10, 20, 30],
            responseHandler: "responseHandler",
            idField: 'id',
            formatLoadingMessage: function () {
                return "<img class='loading-img' src='/static/img/loading_100.gif' /> ";
            },
            formatNoMatches: function () {
                return '<i class="fa status-alert fa-exclamation-triangle" aria-hidden="true"></i> No Result！';
            }
        });
    };

    window.releaseStatusEvent = {
        'click .status': function (e, value, row) {
            var table = $scope.resource["grouprelease"]["release_releaseLogApp_result"]["table"];

            $('.release-history-div').html('');
            var time = row.time;
            var owner = '-';
            owner = row.owner ? row.owner : '-';
            var url = G[G.env].urls.tars + "#/view/deployments/single?app=" + row.appid + "&deployment=" + row.id;
            var id = "<a href='" + url + "'>" + row.id + "</a>";
            var packagelocation = '<a href="' + row.packagelocation + '" title="Download"><i class="fa fa-download" aria-hidden="true"></i></a>';
            var status = row.status;
            var statusClass = '';

            switch (status) {
                case "SUCCESS":
                    status = '成功';
                    statusClass = 'status-green';
                    break;
                case "FAILURE":
                    status = '失败';
                    statusClass = 'status-red';
                    break;
                case "REVOKED":
                    status = '已回退';
                    statusClass = 'status-revoked';
                    break;
                default:
                    status = '正在发布';
                    statusClass = 'status-gray';
                    break;
            }

            var str = '<div class="" style="margin: 0 auto">' +
                '<table class="table ng-scope ng-table release-table">' +
                ' <tr><td><b>'+table['releasetype']+':</b></td>' +
                ' <td class="' + statusClass + '" style="font-weight: bold">' + status + '</td>' +
                ' </tr>' +

                ' <tr><td><b>'+table["id"]+':</b></td>' +
                ' <td><a>' + id + '</a></td>' +
                ' </tr>' +
                ' <tr><td><b>'+table["owner"]+':</b></td>' +
                ' <td>' + owner + '</td>' +
                ' </tr>' +
                ' <tr><td><b>'+table["releasetime"]+':</b></td>' +
                ' <td>' + time + '</td>' +
                ' </tr>' +
                ' <tr><td><b>'+table["releasepackage"]+':</b></td>' +
                ' <td>' + packagelocation + '</td>' +
                ' </tr>'
            '</table>' +
            '</div>';
            $('.release-history-div').append(str);
            $('#output-div').modal('show');
        }
    };
    $scope.hashChanged = function (hashData) { $scope.resource = H.resource;
        $scope.resource = H.resource;
        if (hashData.env) {
            $scope.env = hashData.env;
        }
        if (hashData.groupId) {
            $scope.query.groupId = hashData.groupId;
        }
        if (hashData[$scope.paramName]) {
            $scope.initTable();
            $($scope.tableId).bootstrapTable("removeAll");
            $($scope.tableId).bootstrapTable("showLoading");
            $scope[$scope.paramName] = hashData[$scope.paramName];
            $scope.initialize(hashData[$scope.paramName], getTarsReleaseInfo);
        }
    };
    H.addListener("releaseLogApp", $scope, $scope.hashChanged);
});
angular.bootstrap(document.getElementById("release-log-area"), ['releaseLogApp']);