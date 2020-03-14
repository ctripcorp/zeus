/**
 * Created by ygshen on 2017/2/9.
 */
//GroupsQuery Component: GroupsQueryController
var trafficsQueryApp = angular.module('trafficsQueryApp', ["angucomplete-alt", "http-auth-interceptor"]);
trafficsQueryApp.controller('trafficsQueryController', function ($scope, $http) {
    $scope.query = {
        "trafficid": "",
        "trafficname": "",
        states: {},
        target: {},
        bus: {}
    };
    $scope.data = {
        trafficArr: [],
        vsArr: [],
        vsArr: [],
        statusArr: [],
        trafficForArr: [],
        buArr: []
    };

    //Load cache Area
    $scope.cacheRequestFn = function (str) {
        return {q: str, timestamp: new Date().getTime()};
    };
    $scope.getStatusLanguage = function (x) {
        var resource = $scope.resource;

        if (resource) {
            return resource['polices']['policies_trafficsQueryApp_statusmapping'][x];
        }
    };
    $scope.getTypeLanguage = function (x) {
        var resource = $scope.resource;

        if (resource) {
            return resource['polices']['policies_trafficsQueryApp_typemapping'][x];
        }
    };
    $scope.remoteGroupsUrl = function () {
        return G.baseUrl + "/api/meta/groups";
    };
    $scope.remoteTrafficsUrl = function () {
        return G.baseUrl + "/api/meta/policies";
    };
    $scope.remoteVsesUrl = function () {
        return G.baseUrl + "/api/meta/vses";
    };
    $scope.remoteAppsUrl = function () {
        return G.baseUrl + "/api/meta/apps";
    };
    //Load Query Data Area
    $scope.dataLoaded = false;
    $scope.loadData = function (hashData) {
        if ($scope.env == hashData.env && $scope.dataLoaded) return;
        $scope.dataLoaded = true;
        $scope.env = hashData.env;

        // refresh the query data object
        $scope.data = {
            trafficArr: [],
            vsArr: [],
            groupArr: [],
            tagArr: [],
            buArr: []
        };

        $http.get(G.baseUrl + "/api/tags?type=policy").success(function (res) {
            $scope.data.tagArr = _.map(res['tags'], function (val) {
                return {name: val};
            });
        });
        //Load BUES and status Arr
        $http.get(G.baseUrl + '/api/apps').success(
            function (response) {
                var bues = [];
                $.each(response['apps'], function (index, property) {
                    property.sbu = property.sbu.replace('用户研究&设计', '用户研究与设计');
                    if ($scope.buNameValidation(property.sbu) && bues.indexOf(property.sbu) == -1) {
                        bues.push(property.sbu);
                    }
                });
                $scope.data.buArr = bues.sortBu();
            }
        );

        $scope.data.statusArr = ["已激活", "有变更", "未激活"];
        $scope.data.trafficForArr = ['.NET 转 Java', '应用迁移', '大服务拆分', '多版本测试', 'VM转容器', '其它'];
    };
    $scope.buNameValidation = function (pname) {
        var illegals = ['%', '�', '/', '(', '^', 'э', ')', ';', '}', '-', ':'];
        var needBreak = false;

        $.each(pname.split(''), function (i, v) {
            if (illegals.indexOf(v) > 0) needBreak = true;
        });

        if (needBreak) return false;
        return true;
    };
    $scope.showMoreBU = false;
    $scope.multiTagsClass = function () {
        return $scope.showMoreBU ? '' : 'multi-tags-collapse';
    };
    $scope.collapseBtnClass = function () {
        return $scope.showMoreBU ? 'fa fa-chevron-down' : 'fa fa-chevron-left';
    };
    $scope.toggleShowMoreBU = function () {
        $scope.showMoreBU = !$scope.showMoreBU;
    };
    $scope.buClear = function () {
        $scope.query.bues = [];
    };
    $scope.toggleBu = function (bu) {
        if ($scope.query.bues[bu]) {
            delete $scope.query.bues[bu];
        } else {
            $scope.query.bues[bu] = bu;
        }
    };
    $scope.isSelectedBu = function (bu) {
        if ($scope.query.bues[bu]) {
            return "label-info";
        }
    };
    // Input changed event
    $scope.trafficIdInputChanged = function (o) {
        $scope.query.trafficid = o;
    };
    $scope.vsIdInputChanged = function (o) {
        $scope.query.vsid = o;
    };
    $scope.groupIdInputChanged = function (o) {
        $scope.query.groupid = o;
    };
    // Select input field
    $scope.selectTrafficId = function (o) {
        if (o) {
            $scope.query.trafficid = o.originalObject.id;
        }
    };
    $scope.selectVsId = function (o) {
        if (o) {
            $scope.query.vsid = o.originalObject.id;
        } else {
        }
    };
    $scope.selectGroupId = function (o) {
        if (o) {
            $scope.query.groupid = o.originalObject.id;
        } else {
        }
    };
    $scope.selectAppId = function (o) {
        if (o) {
            $scope.query.appid = o.originalObject.id;
        }
    };

    $scope.toggleStatus = function (status) {
        if ($scope.query.states[status]) {
            delete $scope.query.states[status];
        } else {
            $scope.query.states[status] = status;
        }
    };
    $scope.isSelectedStatus = function (status) {
        if ($scope.query.states[status]) {
            return "label-info";
        }
    };
    $scope.statusClear = function () {
        $scope.query.states = [];
    };

    $scope.toggleTarget = function (target) {
        if ($scope.query.target[target]) {
            delete $scope.query.target[target];
        } else {
            $scope.query.target[target] = target;
        }
    };
    $scope.isSelectedTarget = function (target) {
        if ($scope.query.target[target]) {
            return "label-info";
        }
    };
    $scope.targetClear = function () {
        $scope.query.target = {};
    };

    $scope.selectTag = function (tag) {
        if (tag) {
            $scope.query.tags[tag.originalObject.name] = tag.originalObject.name;
        }
    };

    $scope.showClear = function (type) {
        if (type == "status") {
            return _.keys($scope.query.states).length > 0 ? "link-show" : "link-hide";
        }
        if (type == "target") {
            return _.keys($scope.query.target).length > 0 ? "link-show" : "link-hide";
        }
        if (type == "bu") {
            return _.keys($scope.query.bues).length > 0 ? "link-show" : "link-hide";
        }
    };
    $scope.clearQuery = function () {
        $scope.query.states = {};
        $scope.query.trafficid = "";
        $scope.query.trafficname = "";
        $scope.query.vsid = "";
        $scope.query.groupid = "";
        $scope.query.target = {};
        $scope.query.bues = {};
        $scope.query.appid = "";
        $scope.setInputsDisplay();
    };
    $scope.executeQuery = function () {
        var hashData = {};
        hashData.trafficId = $scope.query.trafficid || "";
        hashData.trafficName = $scope.query.trafficname || "";
        hashData.vsId = $scope.query.vsid || "";
        hashData.groupId = $scope.query.groupid || "";
        hashData.appId = $scope.query.appid || "";
        hashData.policyTags = _.values($scope.query.tags);
        hashData.policyStatus = _.values($scope.query.states);
        hashData.policyTarget = _.values($scope.query.target);
        hashData.policyBues = _.values($scope.query.bues);

        hashData.timeStamp = new Date().getTime();
        H.setData(hashData);
    };
    //Init input field while hashChanged
    $scope.setInputsDisplay = function () {
        $('#trafficIdSelector_value').val($scope.query.trafficid);
        $('#vsIdSelector_value').val($scope.query.vsid);
        $('#groupIdSelector_value').val($scope.query.groupid);
        $('#appIdSelector_value').val($scope.query.appid);
    };
    $scope.applyHashData = function (hashData) {
        $scope.query.trafficname = hashData.trafficName;
        $scope.query.vsid = hashData.vsId;
        $scope.query.appid = hashData.appId;
        $scope.query.groupid = hashData.groupId;
        $scope.query.tags = {};

        if (hashData.policyTags) {
            $.each(hashData.policyTags.split(","), function (i, val) {
                $scope.query.tags[val] = val;
            })
        }
        $scope.query.states = {};
        if (hashData.policyStatus) {
            $.each(hashData.policyStatus.split(","), function (i, val) {
                $scope.query.states[val] = val;
            })
        }
        $scope.query.target = {};
        if (hashData.policyTarget) {
            $.each(hashData.policyTarget.split(","), function (i, val) {
                $scope.query.target[val] = val;
            })
        }

        $scope.query.bues = {};
        if (hashData.policyBues) {
            $.each(hashData.policyBues.split(","), function (i, val) {
                $scope.query.bues[val] = val;
            });
        }
    };
    $scope.hashChanged = function (hashData) { $scope.resource = H.resource;
        $scope.resource = H.resource;
        $scope.loadData(hashData);
        $scope.applyHashData(hashData);
        $scope.setInputsDisplay();
    };
    H.addListener("groupsQueryApp", $scope, $scope.hashChanged);
});
angular.bootstrap(document.getElementById("traffics-query-area"), ['trafficsQueryApp']);

var trafficsResultApp = angular.module("trafficsResultApp", ['http-auth-interceptor']);
trafficsResultApp.controller("trafficsResultController", function ($scope, $http, $q) {
    $scope.loaded = false;
    $scope.data = {
        "traffics": "",
        "trafficCount": 0
    };
    $scope.traffics = [];
    $scope.vses = {};
    $scope.groups = {};
    $scope.apps = {};
    $scope.onlinePolicyData = {};
    $scope.tobeActivatedPolicyData = {};
    $scope.tableOps = {
        policy: {
            showMoreColumns: false,
            showOperations: false
        }
    };
    // Partial show
    $scope.disableOpenPolicy = function () {
        var can = A.canDo('Policy', 'UPDATE', '*');
        return !can;
    };
    $scope.getPolicyShowMore = function () {
        return $scope.tableOps.policy.showMoreColumns ? 'fa fa-list-alt text-info' : 'fa fa-list-alt font-gray';
    };
    $scope.getPolicyShowOperation = function () {
        return $scope.tableOps.policy.showOperations ? 'fa fa-wrench text-info' : 'fa fa-wrench font-gray';
    };
    $scope.toggleShowMorePolicyColumns = function () {
        $scope.tableOps.policy.showMoreColumns = !$scope.tableOps.policy.showMoreColumns;
        if ($scope.tableOps.policy.showMoreColumns) {
            $scope.showMoreGroupsColumn = true;
            $('#traffics-data-table').bootstrapTable('showColumn', 'paths');
            $('#traffics-data-table').bootstrapTable('showColumn', 'target');
            $('#traffics-data-table').bootstrapTable('showColumn', 'controls1');
            $('#traffics-data-table').bootstrapTable('hideColumn', 'controls2');
        } else {
            $scope.showMoreGroupsColumn = false;
            $('#traffics-data-table').bootstrapTable('hideColumn', 'paths');
            $('#traffics-data-table').bootstrapTable('hideColumn', 'target');
            $('#traffics-data-table').bootstrapTable('hideColumn', 'controls1');
            $('#traffics-data-table').bootstrapTable('showColumn', 'controls2');
        }
    };
    $scope.toggleShowPolicyOperations = function () {
        $scope.tableOps.policy.showOperations = !$scope.tableOps.policy.showOperations;
    };
    $scope.getPolicyOperationTitle = function () {
        return $scope.tableOps.policy.showOperations ? '关闭操作' : '打开操作';
    };
    $scope.getPolicyShowMoreTitle = function () {
        return $scope.tableOps.policy.showOperations ? '显示简略信息' : '显示详细信息';
    };

    // Area: Right Area
    $scope.showNewTrafficBt = function () {
        return A.canDo('Policy', 'UPDATE', '*') && $scope.tableOps.policy.showOperations;
    };
    $scope.showAddTrafficTag = function () {
        return A.canDo('Policy', 'PROPERTY', '*') && $scope.tableOps.policy.showOperations;
    };

    // Area: Taging
    function getIdSelections() {
        return $.map($('#traffics-data-table').bootstrapTable('getSelections'), function (row) {
            return row.id
        });
    }

    $('#addTagBt').click(function (e) {
        e.preventDefault();
        var validate = reviewData($('#createTrafficTagDialog'));
        if (!validate) return;
        $('#createTrafficTagDialog').modal('hide');
        $scope.createTrafficTag($('#tagNameText').val().trim());
    });

    function reviewData(id) {
        var result = true;
        $.each(id.find('[data-validator-type="validation"]'), function (i, element) {
            if (!$(element).bootstrapValidation('validate'))
                result = false;
        });
        return result;
    };

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
            url: G.baseUrl + "/api/tagging?tagName=" + tagName + "&type=policy&" + targetIdQueryString,
            method: 'GET',
            params: {}
        };

        $('#operationConfrimModel').modal("show").find(".modal-title").html("在 Traffic Policy 上打Tag");
        $('#operationConfrimModel').modal("show").find(".modal-body").html("正在打Tag.. <img src='/static/img/spinner.gif' />");
        // update traffic policy
        $scope.processRequest(request, $('#operationConfrimModel'), '更新Traffic Policy', '打Tag成功');
    };

    // Area: Table definition and table formatters
    $scope.initTable = function () {
        var resource = $scope.resource;
        var t = resource['polices']['policies_trafficsResultApp_table'];
        var idtitle = t['id'];
        var nametitle = t['name'];
        var groupapptitle = t['groupapp'];
        var idctitle = t['idc'];
        var statustitle = t['status'];
        var pathtitle = t['path'];
        var grouptitle = t['group'];
        var targettitle = t['target'];
        var progresstitle = t['progress'];
        var loadingtext = t['loading'];
        var nodatatext = t["nodata"];
        var statusmapping=resource['polices']['policies_trafficsQueryApp_statusmapping'];
        var targetMap = resource['polices']['policies_trafficsQueryApp_typemapping'];

        $('#traffics-data-table').bootstrapTable({
            toolbar: "#toolbar",
            columns: [[{
                field: 'state',
                checkbox: true,
                rowspan: 2,
                align: 'center',
                valign: 'middle'
            },
                {
                    field: 'id',
                    title: idtitle,
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    formatter: function (value, row, index) {
                        var trafficPageUrl = "";
                        trafficPageUrl = '/portal/policy#?env=' + G.env + '&policyId=' + value;
                        return '<a title="' + value + '" href="' + trafficPageUrl + '">' + value + '</a>';
                    }
                },
                {
                    field: 'name',
                    title: nametitle,
                    align: 'left',
                    resetWidth: true,
                    valign: 'middle',
                    width: '400px',
                    sortable: true,
                    formatter: function (value, row, index) {
                        var trafficPageUrl = '/portal/policy#?env=' + G.env + '&policyId=' + row.id;
                        return '<a title="' + value + '" href="' + trafficPageUrl + '"><div style="word-break: break-all">' + value + '</div></a>';
                    }
                },
                {
                    field: 'paths',
                    title: pathtitle,
                    width: '600px',
                    align: 'left',
                    valign: 'middle',
                    sortName: 'pathOrder',
                    sortable: true,
                    visible: false,
                    formatter: function (value, row, index) {
                        var result = '';
                        $.each(value, function (i, v) {
                            var p = v.path;
                            if (v.path.length > 25) {
                                p = v.path.substring(0, 22);
                                p = p + "...";
                            }
                            var pathStr = '';
                            var priorityStr = '';
                            var sslStr = '';
                            var vsIdStr = '';
                            var slbIdStr = '';
                            if (v.path) {
                                pathStr = '<span class="">' + p + '</span>';
                                priorityStr = '&nbsp;<span class="">' + v.priority + '</span>';
                                sslStr = '&nbsp;<span class="">' + (v.ssl ? 'https' : 'http') + '</span>';
                                vsIdStr = '&nbsp;<a class="" target="_blank" href="' + '/portal/vs#?env=' + G.env + '&vsId=' + v.vsId + '" title="' + v.domain + '">' + v.vsId + '</a>';
                                slbIdStr += '(';
                                $.each(v.slbId, function (i, slbId) {
                                    if (i == v.slbId.length - 1) {
                                        slbIdStr += '<a class="" target="_blank" href="' + '/portal/slb#?env=' + G.env + '&slbId=' + slbId + '">' + slbId + '</a>';
                                    } else {
                                        slbIdStr += '<a class="" target="_blank" href="' + '/portal/slb#?env=' + G.env + '&slbId=' + slbId + '">' + slbId + '</a>,';
                                    }
                                });
                                slbIdStr += ')';

                                var tempStr = "" +
                                    "<div class='col-md-6' style='padding:0;line-height:34px;white-space: nowrap;overflow:hidden;color:#196eaa;font-weight:bold;' title='" + v.path + "'>" +
                                    pathStr +
                                    "</div>" +
                                    '<div class="col-md-6" style="padding:0;line-height:34px;">' +
                                    '<div class="col-md-3" style="padding:0;line-height:34px;">' + priorityStr + '</div>' +
                                    '<div class="col-md-3" style="padding:0;line-height:34px;">' + sslStr + '</div>' +
                                    '<div class="col-md-3" style="padding:0;line-height:34px;">' + vsIdStr + '</div>' +
                                    '<div class="col-md-3" style="padding:0;line-height:34px;">' + slbIdStr + '</div>' +
                                    '</div>';
                                result = result + '<div class="row" style="margin:0;">' + tempStr + '</div>';
                            }
                        });
                        result = '<div class="row" style="margin:0">' + result + '</div>';
                        return result;
                    }
                },
                {
                    field: 'controls2',
                    title: groupapptitle,
                    align: 'left',
                    valign: 'middle',
                    events: operateEvents,
                    visible: true,
                    formatter: function (value, row, index) {
                        var result = '';

                        $.each(value, function (i, v) {
                            var relatedappid = getGroupsRelatedAppId(v.group.id);
                            var newClass = relatedappid ? 'new-policy' : '';
                            var p = v.groupName;
                            var appStr = '';
                            var appId = $scope.groups[v.group.id]['app-id'];
                            appStr += appId;
                            var app = $scope.apps[appId];
                            if (app) {
                                appStr += '(' + app['chinese-name'] + ')';
                            } else {
                                appStr += '(-)';
                            }
                            var link = (v.group.type === 'VGROUP' ? '/portal/vgroup' : '/portal/group') + '#?env=' + $scope.env + '&groupId=' + v.group.id;
                            var appLink = '/portal/app#?env=' + $scope.env + '&appId=' + appId;

                            result += '<div class="row" style="padding-top: 9px"><div class="col-md-11"><a href="' + link + '">' + v.group.id + '</a>' + '/' + '<a href="' + link + '">' + p + '</a>' + '/' + '<a href="' + appLink + '">' + appStr + '</a>' +
                                '</div>' +
                                '<h6 class="status-red col-md-1 ' + newClass + '">' + v.weight + '%</h6>' +
                                '</div>';
                        });
                        result = '<div class="row" style="margin:0">' + result + '</div>';
                        return result;
                    },
                    sortable: true
                },
                {
                    field: 'controls1',
                    title: grouptitle,
                    align: 'left',
                    width: '600px',
                    valign: 'middle',
                    events: operateEvents,
                    visible: false,
                    formatter: function (value, row, index) {
                        var result = '';
                        $.each(value, function (i, v) {
                            var p = v.groupName;
                            if (v.groupName.length > 25) {
                                p = v.groupName.substring(0, 22);
                                p = p + "...";
                            }
                            var groupStr = '<span class="">' + p + '</span>';
                            var idcStr = '&nbsp;<span class="">' + v.idc + '</span>';
                            var statusStr = '&nbsp;<span class="">' + v.status + '</span>';
                            var ownerStr = 'N/A';
                            if (v.owner != 'N/A')
                                ownerStr = '&nbsp;<a class="" target="_blank" href="' + '/portal/user#?env=' + G.env + '&userId=' + v.owner + '" title="' + v.owner + '">' + v.owner + '</a>';

                            var tempStr =
                                "<div class='col-md-6' style='padding:0;line-height:34px;white-space: nowrap;overflow:hidden;' title='" + v.groupName + "'><a title='" + v.groupName + "' target='blank' href='/portal/group#?env=" + G.env + "&groupId=" + v.group.id + "'>" +
                                "<div class='col-md-3'>" + v.group.id + "</div>" +
                                "<div class='col-md-9'>" + groupStr + "</div>" +
                                "</a></div>" +
                                '<div class="col-md-6" style="padding:0;line-height:34px;">' +
                                '<div class="col-md-4" style="padding:0;line-height:34px;">' + idcStr + '</div>' +
                                '<div class="col-md-4" style="padding:0;line-height:34px;">' + statusStr + '</div>' +
                                '<div class="col-md-4" style="padding:0;line-height:34px;">' + ownerStr + '</div>' +
                                '</div>';
                            result = result + '<div class="row" style="margin:0;">' + tempStr + '</div>';
                        });
                        result = '<div class="row" style="margin:0">' + result + '</div>';
                        return result;
                    },
                    sortable: true
                },
                {
                    field: 'target',
                    title: targettitle,
                    align: 'left',
                    valign: 'middle',
                    width: '200px',
                    visible: false,
                    sortable: true,
                    formatter: function (value, row, index) {
                        return targetMap[value];
                    }
                },
                {
                    field: 'idc',
                    title: idctitle,
                    align: 'left',
                    valign: 'middle',
                    sortable: true
                },
                {
                    title: progresstitle,
                    align: 'left',
                    valign: 'middle',
                    visible: false,
                    sortable: true,
                    formatter: function (value, row, index) {
                        var str = '<button type="button" class="btn btn-block btn-sm btn-success">Block Button</button>';
                        console.log(row);
                    }
                },
                {
                    field: 'status',
                    title: statustitle,
                    align: 'left',
                    events: operateEvents,
                    valign: 'middle',
                    width: '152px',
                    formatter: function (value, row, index) {
                        var str = "";
                        switch (value) {
                            case "deactivated":
                                str = "<span class='status-gray'>"+statusmapping['未激活']+"</span>";
                                break;
                            case "activated":
                                str = "<span class='status-green'>"+statusmapping['已激活']+"</span>";
                                break;
                            case "toBeActivated":
                                str = "<span class='diffTraffic status-yellow'>"+statusmapping['有变更']+"(<a data-toggle='modal' data-target='#activateVSModal'>Diff</a>)</span>";
                                break;
                            default:
                                str = "-";
                                break;
                        }
                        return str;
                    },
                    events: operateEvents,
                    sortable: true
                }
            ], []],
            search: true,
            showRefresh: true,
            showColumns: true,
            sortName: 'id',
            sortOrder: 'desc',
            data: $scope.traffics,
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
                return "<img class='loading-img' src='/static/img/loading_100.gif' /> " + loadingtext;
            },
            formatNoMatches: function () {
                return '<i class="fa status-alert fa-exclamation-triangle" aria-hidden="true"></i> ' + nodatatext;
            }
        });
    };

    function getGroupsRelatedAppId(groupId) {
        var groups = $scope.groups;
        var cgroup = groups[groupId];
        var properties = cgroup.properties;
        var indexProperties = _.indexBy(properties, function (v) {
            return v.name;
        });
        if (indexProperties && indexProperties.relatedappid) {
            return indexProperties.relatedappid.value;
        }
        return '';
    }

    function responseHandler(res) {
        $.each(res.rows, function (i, row) {
            row.state = $.inArray(row.id, selections) !== -1;
        });
        return res;
    };
    window.operateEvents = {
        'click .diffTraffic': function (e, value, row, index) {
            var resouce = $scope.resource;
            var r = resouce['polices']['policies_trafficsResultApp_activate'];

            var online = r['online'];
            var offline = r['offline'];
            $scope.currentPolicyId = row.id;
            $scope.confirmActivateText = r['title'];

            $scope.getPolicyById(row.id);

            setTimeout(function () {
                var baseText = JSON.stringify(U.sortObjectFileds($scope.onlinePolicyData), null, "\t");
                var newText = JSON.stringify(U.sortObjectFileds($scope.tobeActivatedPolicyData), null, "\t");
                var base = difflib.stringAsLines(baseText);
                var newtxt = difflib.stringAsLines(newText);

                var sm = new difflib.SequenceMatcher(base, newtxt);
                var opcodes = sm.get_opcodes();
                var diffoutputdiv = document.getElementById("diffOutput");

                diffoutputdiv.innerHTML = "";
                $scope.onlinePolicyVersion = $scope.onlinePolicyData.version;
                if (!$scope.onlinePolicyData.version)
                    $scope.onlinePolicyVersion = "无";

                diffoutputdiv.appendChild(diffview.buildView({
                    baseTextLines: base,
                    newTextLines: newtxt,
                    opcodes: opcodes,
                    baseTextName: online + "(version:" + $scope.onlinePolicyData.version + ")",
                    newTextName: offline + "(version:" + $scope.tobeActivatedPolicyData.version + ")",
                    viewType: 0
                }));

            }, 500);
        },
        'click .diffGroup': function (e, value, row, index) {
            var target = $(e.target).attr('tag');
            getGroupDataByVersion(target);
        }
    };

    function getGroupDataByVersion(target) {
        var baseText = '';
        var NewText = '';
        var groupId = target;


        $q.all([
            $http.get(G.baseUrl + "/api/group?groupId=" + groupId + "&mode=online").then(
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

            $http.get(G.baseUrl + "/api/group?groupId=" + groupId).success(
                function (res) {
                    $scope.tobeActivatedGroupData = res;
                }
            )
        ]).then(
            function () {
                $scope.confirmActivateText = "线上版本与当前版本比较";
                var target = document.getElementById('fileDiffForm1');
                NewText = JSON.stringify(U.sortObjectFileds($scope.tobeActivatedGroupData), null, "\t");
                baseText = JSON.stringify(U.sortObjectFileds($scope.onlineGroupData), null, "\t");
                var ptext = '线下版本' + ($scope.onlineGroupData ? $scope.onlineGroupData.version : '-');
                var ctext = "线上版本" + $scope.tobeActivatedGroupData.version;
                diffTwoSlb(target, baseText, NewText, ptext, ctext);
                $('#diffVSDiv').modal('show');
            }
        );
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

    $('.confirmActivatePolicy').on('click', function () {
        $('#activateVSModal').modal('hide');
        $('#confirmActivatePolicy').modal('show');
    });

    $scope.activatePolicy = function () {
        var loading = "<img src='/static/img/spinner.gif' /> 正在激活";
        $('#operationConfrimModel').modal('show').find(".modal-body").html(loading);
        var param = {
            policyId: $scope.currentPolicyId
        };
        var request = {
            method: 'GET',
            url: G.baseUrl + "/api/activate/policy",
            params: param
        };

        $scope.processRequest(request, $('#operationConfrimModel'), "激活Traffic Policy", "激活Traffic Policy成功,Id:" + $scope.currentPolicyId);
    };
    $scope.forceActivatePolicy = function () {
        $scope.showForceUpdate = false;
        $('#operationConfrimModel').modal("show").find(".modal-title").html("Force 变更");
        $('#operationConfrimModel').modal("show").find(".modal-body").html("正在进行Force变更.. <img src='/static/img/spinner.gif' />");

        $scope.processRequest($scope.forceUpdateRequest, $('#operationConfrimModel'), "激活Traffic Policy", "激活Traffic Policy成功,Id:" + $scope.currentPolicyId);
    };
    $scope.getPolicyById = function (id) {
        $http.get(G.baseUrl + "/api/policy?policyId=" + id + "&mode=online").then(
            function (res) {
                if (res.status == 200 || res.status == 202) {
                    $scope.onlinePolicyData = res.data;
                } else {
                    $scope.onlinePolicyData = "No online version!!!";
                }
            }
        );

        $http.get(G.baseUrl + "/api/policy?policyId=" + id).success(
            function (res) {
                $scope.tobeActivatedPolicyData = res;
            }
        );
    };
    $('#traffics-data-table').on('check.bs.table uncheck.bs.table ' +
        'check-all.bs.table uncheck-all.bs.table', function () {
        $('#createTag').prop('disabled', !$('#traffics-data-table').bootstrapTable('getSelections').length);
    });

    // Create Group Area
    $('#createTraffic').click(function () {
        window.location.href = "/portal/policy/new#?env=" + G.env;
    });
    $scope.getTrafficsQueryString = function (hashData) {
        var queryString = G.baseUrl + "/api/policies?type=EXTENDED";
        // If there is only one env hashdata in the url
        if (_.keys(hashData).length == 1 && hashData.env) {
        } else {
            var groups = hashData.policyBues;
            if (Array.isArray(groups)) {
                groups = _.map(groups, function (s) {
                    if (s == '用户研究与设计') s = '用户研究%26设计';
                    return s;
                });
            } else {
                if (groups == '用户研究与设计') groups = '用户研究%26设计';
            }
            queryString = SpellQueryString(queryString, hashData.trafficId, "policyId");
            queryString = SpellQueryString(queryString, hashData.groupName, "policyName");
            queryString = SpellQueryString(queryString, hashData.vsId, "vsId");
            queryString = SpellQueryString(queryString, hashData.groupId, "groupId");
            queryString = SpellPropertyQueryString(queryString, {"status": hashData.policyStatus});
            queryString = SpellPropertyQueryString(queryString, {"target": hashData.policyTarget});
            queryString = SpellPropertyQueryString(queryString, {"SBU": groups});
            queryString = SpellPropertyQueryString(queryString, {"last": hashData.last});
            var tags = [];
            if (hashData.policyTags == "" || hashData.policyTags == undefined) tags = [];
            else tags = hashData.policyTags.split(',');
            if (tags.length > 0) {
                queryString = SpellTagsQueryString(queryString, tags);
            }
        }
        return queryString;
    };

    function SpellQueryString(queryString, property, tag) {
        if (property) {

            var v = '';
            if (typeof property == 'object') {
                // it only is array
                v = property.join(',');
            } else {
                v = property.split(":")[0];
            }

            if (queryString.endsWith('?')) {
                queryString += (tag + "=");
            }
            else {
                queryString += ("&" + tag + "=");
            }
            return queryString + v;
        }
        else return queryString;
    }

    function SpellTagsQueryString(queryString, tags) {
        if (tags.length == 0 || tags[0] == "") return queryString;

        var query = "anyTag=";
        $.each(tags, function (i, tag) {
            query += tag + ",";
        });

        // trim last ','
        var lastSymIndex = query.lastIndexOf(',');
        query = query.substr(0, lastSymIndex);

        if (queryString.endsWith('?')) {
            queryString += query;
        }
        else {
            queryString += ("&" + query);
        }
        return queryString;
    }

    function SpellPropertyQueryString(queryString, property) {
        if (property != undefined && _.keys(property).length > 0) {
            var query = "anyProp=";
            var keys = _.keys(property);

            $.each(keys, function (i, key) {
                if (property[key]) {
                    var v = property[key].split(',');
                    $.each(v, function (index, unit) {
                        query += key + ":" + getRealpValues(unit) + ",";
                    });
                }
            });
            // trim last ','
            var lastSymIndex = query.lastIndexOf(',');
            if (lastSymIndex != -1) {
                query = query.substr(0, lastSymIndex);

                if (queryString.endsWith('?')) {
                    queryString += query;
                }
                else {
                    queryString += ("&" + query);
                }
            }
            return queryString;
        }
        else return queryString;
    }

    // Get Groups Data Area
    $scope.loadData = function (hashData) {
        if (hashData.env) {
            $scope.env = hashData.env;
        }
        var trafficsqueryString;
        var appIdsQuery;
        var appGroups;

        if (hashData.appId) {
            appIdsQuery = {
                method: 'GET',
                url: G.baseUrl + '/api/groups?type=info&appId=' + hashData.appId
            };
        }

        // send the request
        if (appIdsQuery) {
            $http(appIdsQuery).success(function (response, code) {
                appGroups = _.pluck(response.groups, 'id');
                if (!hashData.groupId) {
                    if (appGroups.length > 0) {
                        hashData.groupId = appGroups;
                    }
                }

                trafficsqueryString = $scope.getTrafficsQueryString(hashData);
                initData(trafficsqueryString);
            });
        } else {
            trafficsqueryString = $scope.getTrafficsQueryString(hashData);
            initData(trafficsqueryString);
        }
    };

    function initData(trafficsqueryString) {
        var queryLogics = [
            $http.get(trafficsqueryString).success(
                function (res) {
                    $scope.trafficQueryResult = res;
                    if (res['traffic-policies']) {
                        $scope.traffics = res['traffic-policies'];
                    } else {
                        $scope.traffics = [];
                    }
                }
            ),
            $http.get(G.baseUrl + '/api/vses').success(
                function (res) {
                    var vses = res['virtual-servers'];
                    $scope.vses = _.indexBy(vses, 'id');
                }
            ),
            $http.get(G.baseUrl + '/api/groups?type=extended&groupType=all').success(
                function (res) {
                    var groups = res['groups'];
                    $scope.groups = _.indexBy(groups, 'id');
                }
            ),
            $http.get(G.baseUrl + '/api/apps').success(
                function (res) {
                    var apps = res['apps'];
                    $scope.apps = _.indexBy(apps, 'app-id');
                }
            )
        ];
        $q.all(queryLogics).then(
            function () {
                var groups_temp = {};
                var vses_temp = {};
                var apps_temp = {};
                var groups_server = {};
                var members_temp = 0;
                if ($scope.groupQueryResult && $scope.groupQueryResult.code) {
                    exceptionNotify("出错了!!", "加载Traffic Groups 失败了， 失败原因" + $scope.groupQueryResult.message, null);
                    return;
                } else {
                    setTimeout(
                        function () {
                            $('.alert-danger').remove();
                        },
                        1000
                    );
                }
                $.each($scope.traffics, function (index, current) {
                    // Traffic status fields
                    current.paths = [];
                    current.status = '';

                    var statusProperty = _.find(current.properties, function (item) {
                        return item.name == 'status';
                    });
                    if (statusProperty) {
                        current.status = statusProperty.value;
                    }
                    var targetProperty = _.find(current.properties, function (item) {
                        return item.name == 'target';
                    });
                    if (targetProperty) {
                        current.target = targetProperty.value;
                    }
                    var idcProperty = _.find(current.properties, function (item) {
                        return item.name == 'idc';
                    });
                    if (idcProperty) {
                        current.idc = idcProperty.value;
                    }
                    // Traffic paths
                    var vs = current['policy-virtual-servers'];
                    var c = {
                        path: '',
                        priority: '',
                        ssl: '',
                        vsId: '',
                        slbId: []
                    };
                    $.each(vs, function (c_index, c_item) {
                        var vsId = c_item['virtual-server'].id;
                        c.path = c_item.path;
                        c.priority = c_item.priority;
                        c.vsId = vsId;
                        if ($scope.vses[vsId]) {
                            c.ssl = $scope.vses[vsId].ssl ? 'https' : 'http';
                            c.slbId = $scope.vses[vsId]['slb-ids'] ? $scope.vses[vsId]['slb-ids'] : [];
                        }
                        current.paths.push(c);
                        vses_temp[vsId] = vsId;
                    });

                    // traffic groups and apps
                    var groups = current.controls;
                    $.each(groups, function (index, item) {
                        var id = item.group.id;
                        groups_temp[id] = id;

                        item.group.type = $scope.groups[id].type;
                        item.groupName = $scope.groups[id].name;
                        var idc_property = _.find($scope.groups[id].properties, function (c) {
                            return c.name == 'idc';
                        });
                        var status_property = _.find($scope.groups[id].properties, function (c) {
                            return c.name == 'status';
                        });
                        if (idc_property) {
                            item.idc = idc_property.value;
                        }
                        if (status_property) {
                            item.status = getGroupStatus(status_property.value);
                        }
                        var appId = $scope.groups[id]['app-id'];
                        apps_temp[appId] = appId;
                        var appName = "-";
                        var sbu = "-";
                        var owner = "-";
                        if ($scope.apps[appId]) {
                            appName = $scope.apps[appId]['chinese-name'];
                            sbu = $scope.apps[appId]['sbu'];
                            owner = $scope.apps[appId]['owner'];
                        }
                        item.app = appId + '(' + appName + ')';
                        item.bu = sbu;
                        item.owner = owner;

                        members_temp += $scope.groups[id]['group-servers'] ? $scope.groups[id]['group-servers'].length : 0;
                        $.each($scope.groups[id]['group-servers'], function (i, t) {
                            groups_server[t.ip + '/' + t['host-name']] = t;
                        });
                    });

                    current.controls1 = current.controls;
                    current.controls2 = current.controls;
                });
                $scope.trafficsCount += $scope.traffics.length;
                // if hashchange is not cause by slbs summary
                if (!$scope.loaded) {
                    var groupCount = _.keys(groups_temp).length;
                    var appCount = _.keys(apps_temp).length;
                    var vsCount = _.keys(vses_temp).length;
                    var serverCount = _.keys(groups_server).length;
                    var memberCount = members_temp;

                    var countItem = _.countBy($scope.traffics, function (item) {
                        var v = _.find(item.properties, function (item2) {
                            return item2.name == 'status';
                        });
                        if (v) return v.value.toLowerCase();
                        else return 'unknown';
                    });
                    var activateCount = countItem['activated'] | 0;
                    var deactivateCount = countItem['deactivated'] | 0;
                    ;
                    var changedCount = countItem['tobeactivated'] | 0;
                    ;

                    $scope.summaryInfo = {
                        activate: activateCount,
                        deactivated: deactivateCount,
                        tobeactivated: changedCount
                    };
                    $('.activated-text').text(activateCount);
                    if (activateCount > 0) {
                        $('.activated-text').prop('href', $scope.navigateTo('activated'));
                    }
                    $('.deactivated-text').text(deactivateCount);
                    if (deactivateCount > 0) {
                        $('.deactivated-text').prop('href', $scope.navigateTo('deactivated'));
                    }
                    $('.tobeactivated-text').text(changedCount);
                    if (changedCount > 0) {
                        $('.tobeactivated-text').prop('href', $scope.navigateTo('tobeactivated'));
                    }

                    $('.policies-text').text($scope.trafficsCount);
                    $('.groups-text').text(groupCount);
                    $('.apps-text').text(appCount);

                    $('.vses-text').text(vsCount);
                    $('.servers-text').text(serverCount);
                    $('.members-text').text(memberCount);

                    $scope.loaded = true;
                }

                $scope.reloadTable();
            }
        );
    }

    $scope.navigateTo = function (item) {
        var url = '';
        switch (item) {
            case 'activated': {
                url = '/portal/policies#?env=' + G.env + '&policyStatus=已激活';
                break;
            }
            case 'tobeactivated': {
                url = '/portal/policies#?env=' + G.env + '&policyStatus=有变更';
                break;
            }
                ;
            case 'deactivated': {
                url = '/portal/policies#?env=' + G.env + '&policyStatus=未激活';
                break;
            }
            case 'domain':
                break;
            case 'apps':
                break;
            case 'groups':
                break;
            default:
                break;
        }
        return url;
    };
    $scope.reloadTable = function () {
        var p1 = A.canDo("Group", "ACTIVATE", "*");
        var p2 = A.canDo("Group", "UPDATE", "*");
        if (!p1 || !p2) {
            $('#traffics-data-table').bootstrapTable('hideColumn', 'Operation');
        }
        $('#traffics-data-table').bootstrapTable("load", $scope.traffics);
        $('#traffics-data-table').bootstrapTable("hideLoading");
    };

    $scope.hashChanged = function (hashData) { $scope.resource = H.resource;
        $scope.resource = H.resource;
        $scope.showMoreGroupsColumn = false;
        $scope.loaded = false;
        $scope.traffics = [];
        $scope.forceUpdateRequest = {};
        $scope.showForceUpdate = false;
        $scope.trafficsCount = 0;
        // reset global data
        $scope.initTable();
        $('#traffics-data-table').bootstrapTable("removeAll");
        $('#traffics-data-table').bootstrapTable("showLoading");
        $scope.loadData(hashData);
        $scope.tableOps.policy.showMoreColumns = false;
        $scope.tableOps.policy.showOperations = true;
    };
    $scope.showForceUpdate = false;
    $scope.forceUpdateRequest = {};
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
                    errText = "<span class='fa fa-times'></span><span style='padding-left: 10px'>" + operationText + "失败</span>";
                    confirmDialog.modal('show').find(".modal-title").html(errText);
                    confirmDialog.modal('show').find(".modal-body").html("失败原因:" + msg);
                    $scope.showForceUpdate = true;
                }
                else {
                    var successText = "<span class='fa fa-check'></span><span style='padding-left: 10px'>" + operationText + "成功</span>";
                    confirmDialog.modal('show').find(".modal-title").html(successText);
                    if (tooltipText) confirmDialog.modal('show').find(".modal-body").html(tooltipText);
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
    H.addListener("trafficsResultApp", $scope, $scope.hashChanged);
    var byId = function (id) {
        return document.getElementById(id);
    };

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

    function getGroupStatus(val) {
        switch (val) {
            case 'activated':
                return '已激活';
            case 'toBeActivated':
                return '有变更';
            case 'deactivated':
                return '未激活';
            default:
                return val;
        }
    }

    function getRealpValues(val) {
        switch (val) {
            case '已激活':
                return 'activated';
            case '有变更':
                return 'toBeActivated';
            case '未激活':
                return 'deactivated';
            case '健康拉出':
                return 'unhealthy';
            case '发布拉出':
                return 'unhealthy';
            case 'Member拉出':
                return 'unhealthy';
            case 'Server拉出':
                return 'unhealthy';
            case 'Broken':
                return 'broken';
            default:
                return val;
        }
    }
});
angular.bootstrap(document.getElementById("traffics-result-area"), ['trafficsResultApp']);

var vsesSummaryApp = angular.module('vsesSummaryApp', ['http-auth-interceptor']);
vsesSummaryApp.controller('vsesSummaryController', function ($scope, $http, $q) {
    $scope.hashData = {};

    $scope.generateStatusText = function (hashData, text) {
        hashData.statuses = text;
        return hashData;
    };
    $scope.hashChanged = function (hashData) { $scope.resource = H.resource;
        $scope.resource = H.resource;
        $scope.hashData = hashData;
    };

    H.addListener("slbsSummaryApp", $scope, $scope.hashChanged);
});
angular.bootstrap(document.getElementById("apps-summary-area"), ['vsesSummaryApp']);