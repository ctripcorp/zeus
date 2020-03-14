//AppInfoComponent: show a app info
var appInfoApp = angular.module('appInfoApp', ['http-auth-interceptor']);
appInfoApp.controller('appInfoController', function ($scope, $http, $q) {

    $scope.resource = H.resource;
    var resource = $scope.resource;

    $scope.users = {};
    $scope.data = {};
    $scope.query = {
        appId: '',
        user: '',
        rewrite: {}
    };

    // Table Init Area
    $scope.initGroupTable = function (tableName, toolbarName, env) {
        var resource = $scope.resource;
        $('#' + tableName).bootstrapTable({
            toolbar: "#" + toolbarName,
            columns: [[
                {
                    field: 'id',
                    title: 'ID',
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    formatter: function (value, row, index) {
                        return '<a target="_blank" title="' + value + '" href="/portal/group#?env=' + env + '&groupId=' + value + '">' + value + '</a>';
                    }
                },
                {
                    field: 'name',
                    title: 'Name',
                    align: 'left',
                    valign: 'middle',
                    width: '250px',
                    sortable: true,
                    formatter: function (value, row, index) {
                        return '<a style="text-decoration: none; margin-left: 5px; word-break: break-all" target="_blank" title="' + value + '" href="/portal/group#?env=' + env + '&groupId=' + row.id + '">' + value + '</a>';
                    }
                },

                {
                    field: 'paths',
                    title: 'Path | Priority | SSL | VS | SLB',
                    align: 'left',
                    valign: 'middle',
                    sortName: 'pathOrder',
                    width: '400px',
                    sortable: true,
                    formatter: function (value, row, index) {
                        var result = '';
                        $.each(value, function (i, v) {
                            var pathStr = '';
                            var priorityStr = '';
                            var sslStr = '';
                            var vsIdStr = '';
                            var slbIdStr = '';
                            if (v.path || v.name) {
                                var pathItems = [];
                                if (v.path) {
                                    pathItems.push(v.path);
                                }
                                if (v.name) {
                                    pathItems.push(v.name);
                                }
                                var tempStr = '<div style="padding:0;line-height:34px;white-space: nowrap;overflow:hidden;color:#196eaa;font-weight:bold;" class="path-item-cell col-md-6';
                                if (pathItems.length > 1) {
                                    tempStr += ' path-multi-item-container';
                                }
                                tempStr += '">';
                                if (v.path) {
                                    tempStr += '<div title="' + v.path + '">' + ellipseString(v.path, 25) + '</div>';
                                }
                                if (v.name) {
                                    tempStr += '<div title="' + v.name + '">' + ellipseString(v.name, 25) + '</div>';
                                }
                                tempStr += '</div>';

                                priorityStr = '<span class="">' + v.priority + '</span>';
                                sslStr = '<span class="">' + (v.ssl ? 'https' : 'http') + '</span>';
                                vsIdStr = '<a class="" target="_blank" href="' + '/portal/vs#?env=' + env + '&vsId=' + v.vsId + '" title="' + v.domain + '">' + v.vsId + '</a>';

                                var slbArray = [];
                                for (var index = 0; index < v.slbId.length; index++) {
                                    var temp = '<a class="" target="_blank" href="' + '/portal/slb#?env=' + env + '&slbId=' + v.slbId[index] + '">' + v.slbId[index] + '</a>';
                                    slbArray.push(temp);
                                }
                                slbIdStr += slbArray.join(';');


                                tempStr +=
                                    '<div style="width: 50%;" class="path-item-cell path-item-list col-md-6">' +
                                    '<div class="col-md-3 path-item-list">' + priorityStr + '</div>' +
                                    '<div class="col-md-3 path-item-list">' + sslStr + '</div>' +
                                    '<div class="col-md-3 path-item-list">' + vsIdStr + '</div>' +
                                    '<div class="col-md-3 path-item-list">' + slbIdStr + '</div>' +
                                    '</div>';
                                result = result + '<div class="row" style="margin:0;">' + tempStr + '</div>';
                            }
                        });
                        result = '<div class="row" style="margin:0; width: 360px">' + result + '</div>';
                        return result;
                    }
                },
                {
                    field: 'idc',
                    title: 'IDC',
                    width: '80px',
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    formatter: function (value, row, index) {
                        return value;
                    }
                },
                {
                    field: 'member-count',
                    title: 'Members',
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    formatter: function (value, row, index) {
                        if (!value)
                            return "-";
                        return "<a target='_blank' href='" + G[env].urls.webinfo + "/?Keyword=" + row.name + "'>" + value + "</a>";
                    }
                },
                {
                    field: 'qps',
                    title: 'QPS',
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    formatter: function (value, row, index) {
                        return value ? '<a target="_blank" href="/portal/group/traffic' + H.generateHashStr({
                            env: env,
                            groupId: row.id
                        }) + '">' + Math.floor(value) + '</a>' : '-';
                    }
                },
                {
                    field: 'status',
                    title: 'Status',
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    events: operateEvents,
                    formatter: function (value, row, index) {
                        var str = "";
                        switch (value) {
                            case "deactivated":
                                str = (angular.equals(resource, {}) ? "<span class='status-gray'>未激活</span>": resource.app.js.msg1);
                                break;
                            case "activated":
                                str = (angular.equals(resource, {}) ? "<span class='status-green'>已激活</span>": resource.app.js.msg2);
                                break;
                            case "toBeActivated":
                                str = (angular.equals(resource, {}) ? "<span class=' status-yellow'>有变更(<a tag='": resource.app.js.msg3) + env + "' class='diffGroup " + env + "'>Diff</a>)</span>";
                                break;
                            default:
                                str = "-";
                                break;
                        }
                        return str;
                    }
                },
                {
                    field: 'grouphealthy',
                    title: 'Healthy',
                    align: 'center',
                    valign: 'middle',
                    sortable: true,
                    formatter: function (value, row, index) {
                        if (row.isvgroup) {
                            return (angular.equals(resource, {}) ? '<a><span title="VGroup不涉及健康检测">-</span></a>': resource.app.js.msg4);
                        }
                        var str = '';
                        switch (value) {
                            case "healthy":
                                str = '<a href="/portal/group#?env=' + env + '&groupId=' + row.id + (angular.equals(resource, {}) ? '"><span class="fa fa-circle status-green" title="完全健康"></span></a>': resource.app.js.msg5);
                                break;
                            case "broken":
                                str = '<a href="/portal/group#?env=' + env + '&groupId=' + row.id + (angular.equals(resource, {}) ? '"><span class="fa fa-circle status-red" title="全部失败"></span></a>': resource.app.js.msg6);
                                break;
                            case "unhealthy":
                                str = '<a href="/portal/group#?env=' + env + '&groupId=' + row.id + (angular.equals(resource, {}) ? '"><span class="fa fa-circle status-yellow" title="存在部分问题Server"></span></a>': resource.app.js.msg7);
                                break;
                            default:
                                str = "-";
                                break;
                        }
                        return str;
                    }
                }
            ], []],
            search: true,
            showRefresh: true,
            showColumns: true,
            classes: "table-bordered  table-hover table-striped table",
            minimumCountColumns: 2,
            idField: 'id',
            sidePagination: 'client',
            formatLoadingMessage: function () {
                return (angular.equals(resource, {}) ? "<img class='loading-img' src='/static/img/loading_100.gif' /> 正在加载当前Application关联的Groups": resource.app.js.msg8);
            },
            formatNoMatches: function () {
                return (angular.equals(resource, {}) ? '<i class="fa status-alert fa-exclamation-triangle" aria-hidden="true"></i> 该Application没有关联任何Group': resource.app.js.msg9);
            }
        });
    };
    $scope.initRewriteTable = function (tableName, toolbarName, env) {
        var resource = $scope.resource;
        $('#' + tableName).bootstrapTable({
            toolbar: "#" + toolbarName,
            columns: [[
                {
                    field: 'id',
                    title: 'ID',
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    width: '30px',
                    formatter: function (value, row, index) {
                        return '<a target="_blank" title="' + value + '" href="/portal/group#?env=' + env + '&groupId=' + value + '">' + value + '</a>';
                    }
                },
                {
                    field: 'name',
                    title: 'Name',
                    align: 'left',
                    valign: 'middle',
                    width: '250px',
                    sortable: true,
                    formatter: function (value, row, index) {
                        return '<a style="text-decoration: none; margin-left: 5px; word-break: break-all" target="_blank" title="' + value + '" href="/portal/group#?env=' + env + '&groupId=' + row.id + '">' + value + '</a>';
                    }
                },
                {
                    field: 'paths',
                    title: 'Path',
                    align: 'left',
                    valign: 'middle',
                    sortName: 'pathOrder',
                    sortable: true,
                    formatter: function (value, row, index) {
                        var result = '';
                        $.each(value, function (i, v) {
                            if (v.path) {
                                result = result + '<div class="row" style="margin:0;">' + ellipseString(v.path, 25) + '</div>';
                            }
                        });
                        result = '<div class="row" style="margin:0; width: 360px">' + result + '</div>';
                        return result;
                    }
                },
                {
                    field: 'paths',
                    title: '(angular.equals(resource, {}) ? 现有rewrite: resource.app.js.msg34)',
                    align: 'left',
                    valign: 'middle',
                    sortName: 'pathOrder',
                    width: '400px',
                    sortable: true,
                    formatter: function (value, row, index) {
                        var str = '';
                        for (var i = 0; i < value.length; i++) {
                            if (value[i].rewrite) {
                                str += '<div>' + value[i].rewrite + '</div>';
                            }
                        }

                        return str ? str : '-';
                    }
                },
                {
                    field: 'idc',
                    title: 'IDC',
                    width: '80px',
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    formatter: function (value, row, index) {
                        return value;
                    }
                }
            ], []],
            classes: "table-bordered  table-hover table-striped table",
            minimumCountColumns: 2,
            idField: 'id',
            formatLoadingMessage: function () {
                return (angular.equals(resource, {}) ? "<img class='loading-img' src='/static/img/loading_100.gif' /> 正在加载当前Application关联的Groups": resource.app.js.msg8);
            },
            formatNoMatches: function () {
                return (angular.equals(resource, {}) ? '<i class="fa status-alert fa-exclamation-triangle" aria-hidden="true"></i> 该Application没有关联任何Group': resource.app.js.msg9);
            }
        });
    };
    var currentenv = '';
    window.operateEvents = {
        'click .diffGroup': function (e, value, row) {
            var env = $(e.target).attr('tag');
            getGroupDataByVersion(row, env);
        }
    };
    $('.confirmActivateGroup').on('click', function () {
        $('#activateGroupModal').modal('hide');
        $('#confirmActivateGroup').modal('show');
    });
    $('.doubleConfirmActivateGroup').on('click', function () {
        $('#confirmActivateGroup').modal('hide');
    });

    // Activate Group Area
    $scope.activateGroup = function () {
        var loading = (angular.equals(resource, {}) ? "<img src='/static/img/spinner.gif' /> 正在激活": resource.app.js.msg12);
        $('#operationConfrimModel').modal('show').find(".modal-body").html(loading);

        var url = G[currentenv].urls.api + '/api/activate/group';
        var param = {
            groupId: $scope.currentGroupId
        };
        var request = {
            method: 'GET',
            url: url,
            params: param
        };

        $scope.processRequest(request, $('#operationConfrimModel'), (angular.equals(resource, {}) ? "激活Group": resource.app.js.msg13), (angular.equals(resource, {}) ? "激活Group成功,GroupId:": resource.app.js.msg14) + $scope.currentGroupId);
    };

    function getGroupDataByVersion(row, env) {
        var baseText = '';
        var NewText = '';
        var groupId = row.id;
        var url = G[env].urls.api;
        $q.all([
            $http.get(url + "/api/group?groupId=" + groupId + "&mode=online").then(
                function successCallback(res) {
                    if (res.data.code) {
                        $scope.onlineGroupData = "No online version!!!";
                    } else {
                        $scope.onlineGroupData = res.data;
                    }
                },
                function errorCallback(res) {
                    if (res.status == 400 && res.data.message == "Group cannot be found.") {
                        $scope.onlineGroupData = "No online version!!!";
                    }
                }
            ),

            $http.get(url + "/api/group?groupId=" + groupId).success(
                function (res) {
                    $scope.tobeActivatedGroupData = res;
                }
            )
        ]).then(
            function () {
                $scope.confirmActivateText = (angular.equals(resource, {}) ? "线上版本与当前版本比较": resource.app.js.msg15);
                var target = document.getElementById('fileDiffForm1');
                NewText = JSON.stringify(U.sortObjectFileds($scope.tobeActivatedGroupData), null, "\t");
                baseText = JSON.stringify(U.sortObjectFileds($scope.onlineGroupData), null, "\t");
                var ptext = (angular.equals(resource, {}) ? '线下版本': resource.app.js.msg16) + ($scope.onlineGroupData ? $scope.onlineGroupData.version : '-');
                var ctext = (angular.equals(resource, {}) ? "线上版本": resource.app.js.msg17) + $scope.tobeActivatedGroupData.version;
                diffTwoSlb(target, baseText, NewText, ptext, ctext);
                $('#diffVSDiv').modal('show');
            }
        );

    }

    $scope.showLink = function (type) {
        var env = $scope.env;
        if (env && G[env].urls[type]) return true;
        return false;
    };
    // Web links generator
    $scope.generateMonitorUrl = function (type) {
        var startTime = new Date().getTime() - 60 * 1000;
        var endTime = new Date().getTime();
        startTime = $.format.date(startTime, 'yyyy-MM-dd_HH:mm:ss');
        endTime = $.format.date(endTime, 'yyyy-MM-dd_HH:mm:ss');

        var url = '';
        var appId = $scope.appinfoResponse ? $scope.appinfoResponse['app-id'] : '-';

        switch (type) {
            case 'hickwall': {
                if ($scope.env) {
                    url = G[$scope.env].urls.hickwall + '/d/CfHEabRZk/slb-app?orgId=6&var-group_appid=' + appId;
                }
                break;
            }
            case 'cat': {
                if ($scope.env) {
                    url = G[$scope.env].urls.cat + '/cat/r/t?op=view&domain=' + appId;
                }
                break;
            }
            case 'es': {
                if ($scope.env) {
                    url = G[$scope.env].urls.es + "?query=group_appid%3D'" + appId + "'";
                }
                break;
            }
            case 'cms': {
                if ($scope.env) {
                    url = G[$scope.env].urls.cms + '/#/app/?q=%7B%22appId$wildcard$%22:%22' + appId + '%22%7D';
                }
                break;
            }
            case 'webinfo': {
                var e = G[$scope.env];
                if (e && e.urls.webinfo) {
                    return e.urls.webinfo + '/#/relation/application/' + appId;
                } else {
                    return '/portal/404';
                }
            }
            case 'clog': {
                if ($scope.env && G[$scope.env].urls.clog) {
                    url = G[$scope.env].urls.clog + '?fromDate=' + startTime + '~toDate=' + endTime + '~app=' + appId;
                }
            }
            default: {
                url = '';
                break;
            }

        }
        return url;
    };

    $scope.getCdPath = function (appId) {
        var cdng = G.cd;
        var env = $scope.env;
        var prefix = "test";
        if (env == 'pro') {
            prefix = "prod";
        }
        return cdng + '#/app/' + appId + '/' + prefix + '/detail?env=' + prefix;
    };

    // Focus Area
    $scope.getFocusObject = function () {
        if ($scope.query == undefined) return undefined;
        var f = _.find($scope.tags, function (item) {
            return item.trim().toLowerCase() == 'user_' + $scope.query.user;
        });
        return f;
    };
    $scope.toggleFocus = function () {
        var f = $scope.getFocusObject();
        if (!f) $scope.addFocus("user_" + $scope.query.user, "focus");
        else $scope.removeFocus("user_" + $scope.query.user, "focus");
    };
    $scope.getFocusCss = function () {
        var f = $scope.getFocusObject();
        if (f == undefined) return "fa fa-eye-slash ";
        return "fa fa-eye ";
    };
    $scope.getFocusText = function () {
        var f = $scope.getFocusObject();
        if (!f) return (angular.equals(resource, {}) ? "关注": resource.app.js.msg35);
        return (angular.equals(resource, {}) ? "取消关注": resource.app.js.msg36);
    };
    $scope.addFocus = function (tagName, type) {
        var request = {
            method: 'GET',
            url: G.baseUrl + "/api/tagging?type=app&tagName=" + tagName + "&targetId=" + $scope.query.appId
        };
        $http(request).success(
            function (res) {
                H.setData({"timeStamp": new Date().getTime()});
            });

    };
    $scope.removeFocus = function (tagName, type) {
        var request = {
            method: 'GET',
            url: G.baseUrl + "/api/untagging?type=app&tagName=" + tagName + "&targetId=" + $scope.query.appId
        };
        $http(request).success(
            function (res) {
                H.setData({"timeStamp": new Date().getTime()});
            });
    };
    $scope.generateAppLink = function (appId) {
        if ($scope.env) {
            return G[G.env].urls.cms + '#/app/?q=%7B%22appId$wildcard$%22:%22' + appId + '%22%7D';
        }
    };

    // Data Loading Area
    $scope.loadData = function () {
        $scope.appinfoResponse = {};
        $scope.tags = [];
        var environments = {
            'pro': {
                groupsurl: G['pro'].urls.api + '/api/groups',
                statsticsurl: G['pro'].urls.api + '/api/statistics/groups',
                diagnoseurl: G['pro'].urls.api + '/api/diagnose/group/query',
                resourceUrl: G['pro'].urls.api + '/api/auth/user/resources'
            },
            'uat': {
                groupsurl: G['uat'].urls.api + '/api/groups',
                statsticsurl: G['uat'].urls.api + '/api/statistics/groups',
                diagnoseurl: G['uat'].urls.api + '/api/diagnose/group/query',
                resourceUrl: G['uat'].urls.api + '/api/auth/user/resources'
            },
            'fws': {
                groupsurl: G['fws'].urls.api + '/api/groups',
                statsticsurl: G['fws'].urls.api + '/api/statistics/groups',
                diagnoseurl: G['fws'].urls.api + '/api/diagnose/group/query',
                resourceUrl: G['fws'].urls.api + '/api/auth/user/resources'
            },
            'fra-aws': {
                groupsurl: G['fra-aws'].urls.api + '/api/groups',
                statsticsurl: G['fra-aws'].urls.api + '/api/statistics/groups',
                diagnoseurl: G['fra-aws'].urls.api + '/api/diagnose/group/query',
                resourceUrl: G['fra-aws'].urls.api + '/api/auth/user/resources'
            },
            'sin-aws': {
                groupsurl: G['sin-aws'].urls.api + '/api/groups',
                statsticsurl: G['sin-aws'].urls.api + '/api/statistics/groups',
                diagnoseurl: G['sin-aws'].urls.api + '/api/diagnose/group/query',
                resourceUrl: G['sin-aws'].urls.api + '/api/auth/user/resources'
            },
            'ymq-aws': {
                groupsurl: G['ymq-aws'].urls.api + '/api/groups',
                statsticsurl: G['ymq-aws'].urls.api + '/api/statistics/groups',
                diagnoseurl: G['ymq-aws'].urls.api + '/api/diagnose/group/query',
                resourceUrl: G['ymq-aws'].urls.api + '/api/auth/user/resources'
            }
        };
        // summarize the requests
        var groupsRequests = [];
        var stasticsRequests = [];
        var diagnoseRequests = [];
        var resourcesRequests = [];

        var groupsResponse = {};
        var stasticsResponse = {};
        var diagnoseResponse = {};
        var resourceResponse = {};

        $.each(_.keys(environments), function (index, item) {
            var groupsurl = environments[item].groupsurl;
            var statsticurl = environments[item].statsticsurl;
            var diagnoseurl = environments[item].diagnoseurl;
            var resourceUrl = environments[item].resourceUrl;

            var groupsparam = {
                appId: $scope.query.appId,
                type: 'extended'
            };
            var request1 = {
                method: 'GET',
                url: groupsurl,
                params: groupsparam
            };
            var request2 = {
                method: 'GET',
                url: statsticurl
            };
            var request3 = {
                method: 'GET',
                url: diagnoseurl,
                params: groupsparam
            };
            var request4 = {
                method: 'GET',
                url: resourceUrl
            };
            var r1 = $http(request1).success(
                function (res, code) {
                    if (code != 200) {
                        groupsResponse[item] = [];
                        exceptionNotify((angular.equals(resource, {}) ? "出错了!!": resource.app.js.msg18), (angular.equals(resource, {}) ? "从": resource.app.js.msg19) + item + (angular.equals(resource, {}) ? "加载AppId=": resource.app.js.msg20) + $scope.query.appId + (angular.equals(resource, {}) ? "的Groups失败了，失败原因：Code=": resource.app.js.msg21) + code);
                    } else {
                        groupsResponse[item] = res;
                    }
                }
            );
            var r2 = $http(request2).success(
                function (res, code) {
                    if (code != 200) {
                        stasticsResponse[item] = [];
                        exceptionNotify((angular.equals(resource, {}) ? "出错了!!": resource.app.js.msg18), (angular.equals(resource, {}) ? "从": resource.app.js.msg19) + item + (angular.equals(resource, {}) ? "加载AppId=": resource.app.js.msg20) + $scope.query.appId + (angular.equals(resource, {}) ? "的Groups Statstics，失败原因Code=": resource.app.js.msg22) + code);
                    } else {
                        stasticsResponse[item] = res;
                    }
                }
            );
            var r3 = $http(request3).success(
                function (res, code) {
                    if (code != 200) {
                        diagnoseResponse[item] = [];
                    } else {
                        diagnoseResponse[item] = res;
                    }
                }
            );
            var r4 = $http(request4).success(function (res, code) {
                if (code != 200) {
                    resourceResponse[item] = [];
                } else {
                    resourceResponse[item] = res;
                }
            });
            diagnoseRequests.push(r3);
            groupsRequests.push(r1);
            stasticsRequests.push(r2);
            resourcesRequests.push(r4);
        });

        var otherRequests = [];
        var appParam = {
            appId: $scope.query.appId
        };
        var appRequest = {
            method: 'GET',
            url: G.baseUrl + '/api/apps',
            params: appParam
        };
        otherRequests.push(
            $http(appRequest).success(
                function (response) {
                    if (response['apps'] && response['apps'].length > 0) {
                        $scope.appinfoResponse = response['apps'][0];
                    } else {
                        $scope.appinfoResponse = {
                            'app-id': 'UNKNOWN',
                            'chinese-name': 'UNKOWN',
                            'sbu': 'UNKNOW',
                            'owner': 'UNKNOWN'
                        };
                    }
                }
            )
        );

        var tagsParam = {
            type: 'app',
            targetId: $scope.query.appId
        };
        var tagsRequest = {
            method: 'GET',
            url: G.baseUrl + '/api/tags',
            params: tagsParam
        };
        otherRequests.push(
            $http(tagsRequest).success(
                function (res) {
                    $scope.tags = res.tags;
                }
            )
        );
        otherRequests.push(
            $http.get(G.baseUrl + '/api/auth/current/user').success(
                function (response) {
                    if (response && !response.code) {
                        $scope.query.user = response['name'];
                        $scope.query.email = response.mail;
                    }
                }
            )
        );
        otherRequests.push(
            $http.get('/api/auth/users').success(
                function (response) {
                    if (response && !response.code) {
                        $scope.users = _.indexBy(response['users'], function (item) {
                            return item['user-name'];
                        })
                    }
                }
            )
        );

        // send the query
        $q.all(
            groupsRequests.concat(stasticsRequests).concat(otherRequests).concat(diagnoseRequests).concat(resourcesRequests)
        ).then(function () {
            var appOwner = $scope.appinfoResponse['owner'];
            $scope.appinfoResponse['name'] = appOwner;

            var ownerChinese = $scope.users[appOwner];
            if (ownerChinese) {
                ownerChinese = ownerChinese['chinese-name'] || appOwner;
            }
            $scope.appinfoResponse['owner'] = ownerChinese;
            $scope.reloadTable(groupsResponse, stasticsResponse, diagnoseResponse, resourceResponse);
        });
    };
    $scope.gc = [];
    $scope.go1 = [];
    $scope.go2 = [];
    $scope.go3 = [];
    $scope.go4 = [];
    $scope.go5 = [];
    $scope.go6 = [];

    $scope.reloadTable = function (groupsResponse, stasticsResponse, diagnoseResponse, resourcesResonse) {
        var c = $scope.env;
        var o1 = $scope.otherEnv1;
        var o2 = $scope.otherEnv2;
        var o3 = $scope.otherEnv3;
        var o4 = $scope.otherEnv4;
        var o5 = $scope.otherEnv5;

        var table = "app-groups-table-" + c;
        var table_toolbar = "appInfo-groupList-toolbar-" + c;

        var table1 = "app-groups-table-" + o1;
        var table1_toolbar = "appInfo-groupList-toolbar-" + o1;

        var table2 = "app-groups-table-" + o2;
        var table2_toolbar = "appInfo-groupList-toolbar-" + o2;

        var table3 = "app-groups-table-" + o3;
        var table3_toolbar = "appInfo-groupList-toolbar-" + o3;

        var table4 = "app-groups-table-" + o4;
        var table4_toolbar = "appInfo-groupList-toolbar-" + o4;

        var table5 = "app-groups-table-" + o5;
        var table5_toolbar = "appInfo-groupList-toolbar-" + o5;

        $scope.initGroupTable(table, table_toolbar, c);
        $scope.initGroupTable(table1, table1_toolbar, o1);
        $scope.initGroupTable(table2, table2_toolbar, o2);
        $scope.initGroupTable(table3, table3_toolbar, o3);
        $scope.initGroupTable(table4, table4_toolbar, o4);
        $scope.initGroupTable(table5, table5_toolbar, o5);


        $scope.initRewriteTable(c, c + "_t", c);
        $scope.initRewriteTable(o1, o1 + "_t", o1);
        $scope.initRewriteTable(o2, o2 + "_t", o2);
        $scope.initRewriteTable(o3, o3 + "_t", o3);
        $scope.initRewriteTable(o4, o4 + "_t", o4);
        $scope.initRewriteTable(o5, o5 + "_t", o5);

        $('#' + c).bootstrapTable('removeAll');
        $('#' + o1).bootstrapTable('removeAll');
        $('#' + o2).bootstrapTable('removeAll');
        $('#' + o3).bootstrapTable('removeAll');
        $('#' + o4).bootstrapTable('removeAll');
        $('#' + o5).bootstrapTable('removeAll');

        $('#' + table3).bootstrapTable('removeAll');
        $('#' + table3).bootstrapTable('removeAll');
        $('#' + table3).bootstrapTable('removeAll');
        $('#' + table3).bootstrapTable('removeAll');
        $('#' + table4).bootstrapTable('removeAll');
        $('#' + table5).bootstrapTable('removeAll');

        $('#' + c).bootstrapTable('removeAll');
        $('#' + o1).bootstrapTable('removeAll');
        $('#' + o2).bootstrapTable('removeAll');
        $('#' + o3).bootstrapTable('removeAll');
        $('#' + o4).bootstrapTable('removeAll');
        $('#' + o5).bootstrapTable('removeAll');

        $('#' + table).bootstrapTable('showLoading');
        $('#' + table1).bootstrapTable('showLoading');
        $('#' + table2).bootstrapTable('showLoading');
        $('#' + table3).bootstrapTable('showLoading');
        $('#' + table4).bootstrapTable('showLoading');
        $('#' + table5).bootstrapTable('showLoading');

        var gc = groupsResponse[c].groups ? groupsResponse[c].groups : [];
        var go1 = groupsResponse[o1].groups ? groupsResponse[o1].groups : [];
        var go2 = groupsResponse[o2].groups ? groupsResponse[o2].groups : [];
        var go3 = groupsResponse[o3].groups ? groupsResponse[o3].groups : [];
        var go4 = groupsResponse[o4].groups ? groupsResponse[o4].groups : [];
        var go5 = groupsResponse[o5].groups ? groupsResponse[o4].groups : [];

        $scope.gc = gc;
        $scope.go1 = go1;
        $scope.go2 = go2;
        $scope.go3 = go3;
        $scope.go4 = go4;
        $scope.go5 = go5;

        $scope.data[c] = gc;
        $scope.data[o1] = go1;
        $scope.data[o2] = go2;
        $scope.data[o3] = go3;
        $scope.data[o4] = go4;
        $scope.data[o5] = go5;

        var sc = stasticsResponse[c]['group-metas'];
        var so1 = stasticsResponse[o1]['group-metas'];
        var so2 = stasticsResponse[o2]['group-metas'];
        var so3 = stasticsResponse[o3]['group-metas'];
        var so4 = stasticsResponse[o4]['group-metas'];
        var so5 = stasticsResponse[o5]['group-metas'];

        var dc = diagnoseResponse[c].length;
        var do1 = diagnoseResponse[o1].length;
        var do2 = diagnoseResponse[o2].length;
        var do3 = diagnoseResponse[o3].length;
        var do4 = diagnoseResponse[o4].length;
        var do5 = diagnoseResponse[o5].length;

        var rc = resourcesResonse[c]['user-resources'];
        var ro1 = resourcesResonse[o1]['user-resources'];
        var ro2 = resourcesResonse[o2]['user-resources'];
        var ro3 = resourcesResonse[o3]['user-resources'];
        var ro4 = resourcesResonse[o4]['user-resources'];
        var ro5 = resourcesResonse[o5]['user-resources'];

        var rc_can = $scope.hasRightForGroups(_.pluck(gc, 'id'), rc);
        var ro1_can = $scope.hasRightForGroups(_.pluck(go1, 'id'), ro1);
        var ro2_can = $scope.hasRightForGroups(_.pluck(go2, 'id'), ro2);
        var ro3_can = $scope.hasRightForGroups(_.pluck(go3, 'id'), ro3);
        var ro4_can = $scope.hasRightForGroups(_.pluck(go4, 'id'), ro4);
        var ro5_can = $scope.hasRightForGroups(_.pluck(go5, 'id'), ro5);

        $scope.query[c] = rc_can;
        $scope.query[o1] = ro1_can;
        $scope.query[o2] = ro2_can;
        $scope.query[o3] = ro3_can;
        $scope.query[o4] = ro4_can;
        $scope.query[o5] = ro5_can;


        // get groups' stastics
        var scMap = {};
        var soMap1 = {};
        var soMap2 = {};
        var soMap3 = {};
        var soMap4 = {};
        var soMap5 = {};

        scMap = _.object(_.map(sc, function (item) {
            return [item['group-id'], item];
        }));
        soMap1 = _.object(_.map(so1, function (item) {
            return [item['group-id'], item];
        }));
        soMap2 = _.object(_.map(so2, function (item) {
            return [item['group-id'], item];
        }));
        soMap3 = _.object(_.map(so3, function (item) {
            return [item['group-id'], item];
        }));
        soMap4 = _.object(_.map(so4, function (item) {
            return [item['group-id'], item];
        }));
        soMap5 = _.object(_.map(so5, function (item) {
            return [item['group-id'], item];
        }));

        var newc = $scope.refactGroupDatas(gc, scMap, c, dc);
        var newo1 = $scope.refactGroupDatas(go1, soMap1, o1, do1);
        var newo2 = $scope.refactGroupDatas(go2, soMap2, o2, do2);
        var newo3 = $scope.refactGroupDatas(go3, soMap3, o3, do3);
        var newo4 = $scope.refactGroupDatas(go4, soMap4, o4, do4);
        var newo5 = $scope.refactGroupDatas(go5, soMap5, o5, do5);

        var c_has_rewrite = $scope.hasRewriteForGroups(newc);
        var o1_has_rewrite = $scope.hasRewriteForGroups(newo1);
        var o2_has_rewrite = $scope.hasRewriteForGroups(newo2);
        var o3_has_rewrite = $scope.hasRewriteForGroups(newo3);
        var o4_has_rewrite = $scope.hasRewriteForGroups(newo4);
        var o5_has_rewrite = $scope.hasRewriteForGroups(newo5);

        $scope.query.rewrite[c] = c_has_rewrite;
        $scope.query.rewrite[o1] = o1_has_rewrite;
        $scope.query.rewrite[o2] = o2_has_rewrite;
        $scope.query.rewrite[o3] = o3_has_rewrite;
        $scope.query.rewrite[o4] = o4_has_rewrite;
        $scope.query.rewrite[o5] = o5_has_rewrite;

        setTimeout(function () {
            $('#' + table).bootstrapTable("load", newc);
            $('#' + table1).bootstrapTable("load", newo1);
            $('#' + table2).bootstrapTable("load", newo2);
            $('#' + table3).bootstrapTable("load", newo3);
            $('#' + table4).bootstrapTable("load", newo4);
            $('#' + table5).bootstrapTable("load", newo5);

            $('#' + c).bootstrapTable("load", newc);
            $('#' + o1).bootstrapTable("load", newo1);
            $('#' + o2).bootstrapTable("load", newo2);
            $('#' + o3).bootstrapTable("load", newo3);
            $('#' + o4).bootstrapTable("load", newo4);
            $('#' + o5).bootstrapTable("load", newo5);

            $('#' + table).bootstrapTable("hideLoading");
            $('#' + table1).bootstrapTable("hideLoading");
            $('#' + table2).bootstrapTable("hideLoading");
            $('#' + table3).bootstrapTable("hideLoading");
            $('#' + table4).bootstrapTable("hideLoading");
            $('#' + table5).bootstrapTable("hideLoading");

            $('#' + c).bootstrapTable("hideLoading");
            $('#' + o1).bootstrapTable("hideLoading");
            $('#' + o2).bootstrapTable("hideLoading");
            $('#' + o3).bootstrapTable("hideLoading");
            $('#' + o4).bootstrapTable("hideLoading");
            $('#' + o5).bootstrapTable("hideLoading");
        }, 1000);
    };

    $scope.hasRightForGroups = function (groups, resources) {
        var indexedResource = _.indexBy(resources, function (v) {
            return v['type'];
        });

        var hasGroup = indexedResource["Group"];

        if (!hasGroup) return false;

        var hasUpdateAndActivated = _.find(hasGroup['data-resources'], function (v) {
            var ops = _.indexBy(v['operations'], 'type');
            var activatedAndUpdate = ops['ACTIVATE'] && ops['UPDATE'];
            var data = v.data == '*' || groups.indexOf(parseInt(v.data)) != -1;
            return activatedAndUpdate && data;
        });

        if (hasUpdateAndActivated) {
            return true;
        }

        return false;
    };

    $scope.hasRewriteForGroups = function (groups) {
        var has = false;
        $.each(groups, function (i, group) {
            var paths = group.paths;
            var hasRewrite = _.find(paths, function (v) {
                return v.rewrite;
            });
            if (hasRewrite) has = true;
        });

        return has;
    };

    $scope.changePath = function (env) {
        var groups = $scope.data[env];
        if (groups && groups.length > 0) {
            var groupIds = _.pluck(groups, 'id');

            var updateRequestBodys = {};
            var getRequests = [];
            var activateRequests = [];
            var activateResults = {};
            // update and activate groups
            var base = G[env]['urls']['api'];
            _.map(groupIds, function (v) {
                var getUrl = base + '/api/group?groupId=' + v;
                var activateUrl = base + '/api/activate/group?groupId=' + v + (angular.equals(resource, {}) ? "&description=urlrewrite大小写转换": resource.app.js.msg23);
                var getRequest = {
                    method: 'GET',
                    url: getUrl
                };
                var activateRequest = {
                    method: 'GET',
                    url: activateUrl
                };
                var r = $http(getRequest).success(function (response, code) {
                    if (code == 200) {
                        response['group-virtual-servers'] = _.map(response['group-virtual-servers'], function (s) {
                            var a = s.path;
                            // ~* ^/fc-ws($|/|\\?)
                            if (a == '~* ^/' || a == '/') {
                                alert('Path=' + a + (angular.equals(resource, {}) ? '. 无需转换！': resource.app.js.msg24));
                                return s;
                            }
                            var c = a.substring(5, a.length - 8);

                            s.rewrite = '"(?i)^/' + c + '(.*)" /' + c + '$1;'
                            return s;
                        });

                        updateRequestBodys[v] = response;
                    }
                });

                getRequests.push(r);

                var a = $http(activateRequest).success(function (response, code) {
                    if (code == 200) {
                        activateResults[v] = 'success';
                    } else {
                        activateResults[v] = 'fail';
                    }
                });
                activateRequests.push(a);
            });


            $q.all(getRequests).then(function () {
                var values = _.values(updateRequestBodys);

                var updateRequests = [];

                var updateResult = [];

                _.map(values, function (v) {
                    var request = {
                        method: 'POST',
                        url: base + (angular.equals(resource, {}) ? '/api/group/update?description=urlrewrite大小写转换': resource.app.js.msg25),
                        data: v
                    };

                    var r = $http(request).success(function (response, code) {
                        if (code == 200) {
                            updateResult[v.id] = 'success';
                        } else {
                            updateResult[v.id] = 'fail';
                        }
                    });

                    updateRequests.push(r);
                });

                $q.all(updateRequests).then(function () {

                    // all succeed?
                    var results = _.values(updateResult);
                    if (results.indexOf('fail') == -1) {
                        // activate
                        $q.all(activateRequests).then(function () {
                            var aresults = _.values(activateResults);
                            if (aresults.indexOf('fail') == -1) {
                                alert((angular.equals(resource, {}) ? '修改成功， 请刷新页面查看': resource.app.js.msg26));
                            } else {
                                alert((angular.equals(resource, {}) ? '修改后激活失败!': resource.app.js.msg27));
                            }
                        });
                    } else {
                        alert((angular.equals(resource, {}) ? '修改失败': resource.app.js.msg28));
                        return;
                    }
                });
            })

        }
    };

    $scope.getAuthUrl = function (env) {
        var user = $scope.query.user;
        var groups = _.pluck($scope.data[env], 'id');
        var targetString = '';
        $.each(groups, function (i, t) {
            targetString += '&targetId=' + t;
        });
        var url = G[env].urls.api + '/api/auth/apply/mail?userName=' + user + '&op=ACTIVATE,UPDATE' + targetString + '&type=Group&env=' + env;
        var request = {
            url: url,
            method: 'GET'
        };
        $http(request).success(function (response, code) {
            if (code == 200) {
                alert((angular.equals(resource, {}) ? '你已经申请更新当前应用的权限，请等待管理员审批!': resource.app.js.msg29));
            }
        });
    };
    $scope.refactGroupDatas = function (gc, map, env, sick) {
        $.each(gc, function (i, group) {
            group.env = env;
            $scope.getGroupMetaData(group);
            $scope.getGroupQPSMetaData(group, map);
            group.paths = [];
            group.pathOrder = 0;
            if (sick && sick > 0) {
                group.sick = true;
            } else {
                group.sick = false;
            }
            var c = 0;
            $.each(group['group-virtual-servers'], function (i, gVs) {
                var o = {
                    path: gVs.path,
                    name: gVs.name,
                    priority: gVs.priority,
                    rewrite: gVs.rewrite,
                    vsId: gVs['virtual-server'].id,
                    slbId: gVs['virtual-server']["slb-ids"],
                    ssl: gVs['virtual-server'].ssl,
                    domain: gVs['virtual-server'].domains[0] == undefined ? "" : gVs['virtual-server'].domains[0].name
                };
                group.paths.push(o);

                //Set path order
                if (c == 0) {
                    if (o.priority >= 0) {
                        group.pathOrder = o.priority * 1000000 + group.id;
                    } else {
                        group.pathOrder = o.priority * 1000000 - group.id;
                    }
                }
                c++;
            })
        });
        return gc;
    };
    $scope.getGroupMetaData = function (group) {
        var g_property = group.properties;
        $.each(g_property, function (i, property) {
            var status = property.name.toLowerCase();
            if (status == "status") {
                group.status = property.value;
                switch (property.value) {
                    case "deactivated":
                        $scope.offline++;
                        break;
                    case "activated":
                        $scope.online++;
                        break;
                    default :
                        $scope.tobeOnline++;
                        break;
                }
            }
            if (status == "healthy") {
                group.grouphealthy = property.value;
            }
            if (status == 'idc') {
                group.idc = property.value;
            }
        });
    };
    $scope.getGroupQPSMetaData = function (group, map) {
        if (map[group.id]) {
            group.qps = map[group.id].qps;
            group['member-count'] = map[group.id]['member-count'];
        }
    };
    $scope.hashChanged = function (hashData) { $scope.resource = H.resource;
        if (hashData.env) {
            $scope.env = hashData.env;
            if (hashData.env == "fws") {
                $scope.otherEnv1 = "pro";
                $scope.otherEnv2 = "uat";
                $scope.otherEnv3 = "fra-aws";
                $scope.otherEnv4 = "sin-aws";
                $scope.otherEnv5 = "ymq-aws";
            }
            if (hashData.env == "uat") {
                $scope.otherEnv1 = "pro";
                $scope.otherEnv2 = "fws";
                $scope.otherEnv3 = "fra-aws";
                $scope.otherEnv4 = "sin-aws";
                $scope.otherEnv5 = "ymq-aws";
            }
            if (hashData.env == "pro") {
                $scope.otherEnv1 = "uat";
                $scope.otherEnv2 = "fws";
                $scope.otherEnv3 = "fra-aws";
                $scope.otherEnv4 = "sin-aws";
                $scope.otherEnv5 = "ymq-aws";
            }
            if (hashData.env == "fra-aws") {
                $scope.otherEnv1 = "pro";
                $scope.otherEnv2 = "uat";
                $scope.otherEnv3 = "fws";
                $scope.otherEnv4 = "sin-aws";
                $scope.otherEnv5 = "ymq-aws";
            }
            if (hashData.env == "sin-aws") {
                $scope.otherEnv1 = "pro";
                $scope.otherEnv2 = "uat";
                $scope.otherEnv3 = "fra-aws";
                $scope.otherEnv4 = "fws";
                $scope.otherEnv5 = "ymq-aws";
            }
            if (hashData.env == "ymq-aws") {
                $scope.otherEnv1 = "pro";
                $scope.otherEnv2 = "uat";
                $scope.otherEnv3 = "fra-aws";
                $scope.otherEnv4 = "fws";
                $scope.otherEnv5 = "sin-aws";
            }

            if (hashData.appId) {
                $scope.query.appId = hashData.appId;
                $scope.loadData();
            }
            if (hashData.rewrite) {
                // init
                $('#rewriteForJavaModel').modal('show');
            }
        }
    };
    H.addListener("appInfoApp", $scope, $scope.hashChanged);

    // Common Functions Area
    function exceptionNotify(title, message, url) {
        var notify = $.notify({
            icon: 'fa fa-exclamation-triangle',
            title: title,
            message: message,
            url: url,
            target: '_self'
        }, {
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
            animate: {
                enter: 'animated fadeInDown',
                exit: 'animated fadeOutUp'
            },
            delay: 60000 * 24,
            spacing: 5,
            z_index: 1031,
            mouse_over: 'pause'
        });
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

    $scope.processRequest = function (request, confirmDialog, operationText, tooltipText) {
        confirmDialog.find(".modal-title").html(operationText);
        var msg = "";
        $http(request).success(
            function (res, code) {
                var errText = '';
                if (code != 200) {
                    msg = res.message;
                    if (!msg) {
                        msg = code;
                    }
                    errText = "<span class='fa fa-times'></span><span style='padding-left: 10px'>" + operationText + (angular.equals(resource, {}) ? "失败</span>": resource.app.js.msg30);
                    confirmDialog.modal('show').find(".modal-title").html(errText);
                    confirmDialog.modal('show').find(".modal-body").html((angular.equals(resource, {}) ? "失败原因:": resource.app.js.msg31) + msg);
                    if (msg.indexOf("overlap") > 0) {
                        // need force update
                        $scope.showForceUpdate = true;
                    }
                }
                else {
                    var successText = "<span class='fa fa-check'></span><span style='padding-left: 10px'>" + operationText + (angular.equals(resource, {}) ? "成功</span>": resource.app.js.msg32);
                    confirmDialog.modal('show').find(".modal-title").html(successText);
                    if (tooltipText) confirmDialog.modal('show').find(".modal-body").html(tooltipText);
                    startTimer(confirmDialog);
                }
            }
        ).error(function (reject) {
            msg = reject.message;
            var errText = "<span class='fa fa-times'></span><span style='padding-left: 10px'>" + operationText + (angular.equals(resource, {}) ? "失败</span>": resource.app.js.msg30);
            confirmDialog.modal('show').find(".modal-title").html(errText);
            confirmDialog.modal('show').find(".modal-body").html((angular.equals(resource, {}) ? "失败原因:": resource.app.js.msg31) + msg);
        });
    };

    function startTimer(dialog) {
        if (dialog.attr('id') == 'deleteGroupConfirmModel') {
            setTimeout(function () {
                dialog.find('#closeProgressWindowBt2').click();
            }, 2000);
        }
        else {
            setTimeout(function () {
                dialog.find('#closeProgressWindowBt').click();
            }, 2000);
        }
    }

    $('#closeProgressWindowBt').click(
        function (e) {
            var hashData = {};
            hashData.timeStamp = new Date().getTime();
            H.setData(hashData);
        }
    );
});
angular.bootstrap(document.getElementById("app-area"), ['appInfoApp']);


var summaryInfoApp = angular.module('summaryInfoApp', ['http-auth-interceptor', 'angucomplete-alt']);
summaryInfoApp.controller('summaryController', function ($scope, $http, $q) {

    $scope.resource = H.resource;
    var resource = $scope.resource;

    $scope.query = {};
    // Auto complete
    $scope.cacheRequestFn = function (str) {
        return {q: str, timestamp: new Date().getTime()};
    };
    $scope.remoteUrl = function () {
        return G.baseUrl + "/api/meta/apps";
    };
    $scope.selectTarget = function (t) {
        if (t) {
            var toId = t.originalObject.id;
            if ($scope.target.id != toId) {
                $scope.$broadcast('angucomplete-alt:clearInput', 'targetSelector');
                var pairs = {};
                pairs['appId'] = toId;
                $scope.target.id = toId;
                H.setData(pairs);
                messageNotify((angular.equals(resource, {}) ? "切换应用:": resource.app.js.MSG33) , (angular.equals(resource, {}) ?"成功切换至应用： ": resource.app.js.msg37) + toId, null);
            }
        }
    };
    $scope.clickTarget = function () {
        $('#targetSelector_value').css('width', '250px');
    };
    $scope.setInputsDisplay = function () {
        $('#targetSelector_value').val($scope.query.buName);
    };
    $scope.hashChanged = function (hashData) { $scope.resource = H.resource;
        if (hashData.env) {
            $scope.env = hashData.env;
        }
        if (hashData.appId) {
            $scope.query.appId = hashData.appId;
        }
        $scope.target = {};
        $scope.target.name = $scope.query.appId;
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

    $scope.resource = H.resource;
    var resource = $scope.resource;

    $scope.query = {};
    $scope.generateLink = function (a) {
        var link = '';
        switch (a) {
            case 'basic': {
                link = "/portal/app#?env=" + G.env + "&appId=" + $scope.query.appId;
                break;
            }
            case 'log': {
                link = "/portal/app/log#?env=" + G.env + "&appId=" + $scope.query.appId;
                break;
            }
            case 'traffic': {
                link = "/portal/app/traffic#?env=" + G.env + "&appId=" + $scope.query.appId;
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
        if (hashData.appId) {
            $scope.query.appId = hashData.appId;
        }
    };
    H.addListener("infoLinksApp", $scope, $scope.hashChanged);
});
angular.bootstrap(document.getElementById("header-area"), ['headerInfoApp']);
