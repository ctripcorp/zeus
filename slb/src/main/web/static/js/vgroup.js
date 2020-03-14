//GroupInfoComponent: show a group info
var groupInfoApp = angular.module('groupInfoApp', ['angucomplete-alt', 'ui.bootstrap', 'ui.bootstrap.datetimepicker', 'http-auth-interceptor']);
groupInfoApp.controller('groupInfoController', function ($scope, $http, $q) {
    // Global variables
    var unknownText = "未知";
    var start = "~* ^";
    var end = "($|/|\\?)";
    var appId;
    $scope.currentUrl = window.location.href;
    // query information
    $scope.information = {
        // slbs object
        apps: {},
        'slbs': {},
        'onlineGroup': {},
        'offlineGroup': {},
        'extendedGroup': {},
        'group-virtual-servers': [],
        'group-related': [],
        'appdata': {}
    };
    $scope.chart = {
        'startTime': '',
        'endTime': ''
    };
    // auto-complete
    $scope.remoteUrl = function () {
        return G.baseUrl + "/api/meta/groups";
    };
    // Table area: Virtual Servers and health check tables
    $scope.initTable = function () {
        $('#group-virtual-server-table').bootstrapTable({
            search: true,
            showRefresh: true,
            showColumns: true,
            toolbar: "#groupInfo-appList-toolbar",
            columns: [[
                {
                    checkbox: 'true',
                    align: 'center',
                    valign: 'middle'
                },
                {
                    field: 'path',
                    title: 'Path',
                    align: 'left',
                    valign: 'middle',
                    cellStyle: function (value, row, index, field) {
                        return pathFormatter(value, row, index, field);
                    },
                    sortable: true
                },
                {
                    field: 'name',
                    title: 'Name',
                    align: 'left',
                    valign: 'middle',
                    sortable: true
                },
                {
                    field: 'port',
                    title: 'Port',
                    align: 'left',
                    valign: 'middle'
                },
                {
                    field: 'redirect',
                    title: 'Redirect',
                    align: 'left',
                    valign: 'middle',
                    cellStyle: function (value, row, index, field) {
                        return rewriteFormatter(value, row, index, field);
                    },
                    formatter: function (value) {
                        if (value == undefined || value == "") return "-";
                        else return value;
                    },
                    sortable: true
                },
                {
                    field: 'custom-conf',
                    title: 'Custom Conf',
                    align: 'left',
                    valign: 'middle',
                    formatter: function (value, row, index) {
                        if (!value) return "-";
                        if (value.length <= 50) {
                            return "<pre>" + value + "</pre>";
                        }
                        return "<pre id='custom_conf_short_" + index + "'>" + value.substring(0, 47) + "...</pre>" +
                            "<pre id='custom_conf_full_" + index + "' class='hidden'>" + value + "</pre>" +
                            "<a href='' onclick='angular.element(this).scope().toggleFullCustomConf(" + index + "); return false;'>More</a>";
                    }
                },
                {
                    field: 'priority',
                    title: 'Priority',
                    class: 'shortCol',
                    align: 'left',
                    valign: 'middle',
                    sortable: true
                },
                {
                    field: 'ssl',
                    title: 'SSL',
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    formatter: function (v) {
                        return v ? 'https' : 'http';
                    }
                },
                {
                    field: 'slbvips',
                    title: 'SLB',
                    align: 'left',
                    valign: 'middle',
                    formatter: function (value, row, index) {
                        var result = '';
                        $.each(value, function (i, v) {
                            result += '<div class="">';
                            result += "<div class='col-md-4' style='padding:0;line-height:34px;white-space: nowrap;overflow:hidden;color:#196eaa;'>";
                            result += '<a target="_blank" href="' + '/portal/slb#?env=' + G.env + '&slbId=' + v.slbid + '">' + v.slbid + '</a> ';
                            result += '</div>';

                            result += '</div>';
                        });

                        return result;
                    },
                    sortable: true
                },
                {
                    field: 'vs',
                    title: 'VS',
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    formatter: function (value) {
                        return '<a target="_blank" href="' + '/portal/vs#?env=' + G.env + '&vsId=' + value + '">' + value + '</a>';
                    }
                },
                {
                    field: 'domains',
                    title: 'Domains',
                    align: 'left',
                    valign: 'middle',
                    width: '100px',
                    formatter: function (value) {
                        var str = "<ul class='ul-appdomains'>";
                        $.each(value, function (i, v) {
                            if (G[G.env].urls.cms) {
                                str += "<li><a title='" + v.name + "' target='_blank' href='" + G[G.env].urls.cms + "/#/domain-name/?q=%7B%22name$wildcard$%22:%22" + v.name + "%22%7D'>" + v.name + "</a></li>";
                            } else {
                                str += "<li><span title='" + v.name + "' target='_blank'>" + v.name + "</span></li>";
                            }
                        });
                        str += "</ul>";
                        return str;
                    }
                },
                {
                    field: 'operate',
                    width: '120px',
                    title: 'Operation',
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    events: OperationChangeEvent,
                    formatter: function () {
                        var p1 = "";
                        if (!A.canDo("Group", "UPDATE", $scope.information.extendedGroup.id)) p1 = "hide";
                        var str = '<button data-toggle="tooltip" type="button" class="btn btn-info deleteVirtualSererBt  ' + p1 + '" title="删除" aria-label="Left Align"><span class="fa fa-minus"></span></button>';
                        str += '  <button data-toggle="tooltip" title="修改" type="button" class="btn btn-info change-virtual-path  ' + p1 + '" aria-label="Left Align">'
                            + '<span class="fa fa-edit"></span>' +
                            '</button>';
                        return str;
                    }
                }

            ], []],
            data: $scope.information["group-virtual-servers"],
            classes: "table-bordered  table-hover table-striped table",
            minimumCountColumns: 2,
            pageSize: 20,
            sidePagination: 'client',
            pageList: [20, 40, 80, 200],
            resizable: true,
            resizeMode: 'overflow',
            responseHandler: "responseHandler",
            idField: 'ip'
        });
        $('#group-redirect-to-table').bootstrapTable({
            search: true,
            showRefresh: true,
            showColumns: true,
            toolbar: "#group-redirect-to-toolbar",
            columns: [[
                {
                    field: 'id',
                    title: 'ID',
                    align: 'left',
                    valign: 'middle',
                    formatter: function (value, row, index) {
                        return "<a href='/portal/group#?env=" + G.env + "&groupId=" + value + "'>" + value + "</a>";
                    },
                    sortable: true
                },
                {
                    field: 'name',
                    title: 'Name',
                    align: 'left',
                    valign: 'middle',

                    formatter: function (value, row, index) {
                        return "<a href='/portal/group#?env=" + G.env + "&groupId=" + row.id + "'>" + value + "</a>";
                    },
                    sortable: true
                }
            ], []],
            data: $scope.information["group-related"],
            classes: "table-bordered  table-hover table-striped table",
            minimumCountColumns: 2,
            pageSize: 20,
            sidePagination: 'client',
            pageList: [20, 40, 80, 200],
            responseHandler: "responseHandler",
            idField: 'ip'
        });
    };

    function pathFormatter(value, row, index, field) {
        if (!value) {
            return {};
        }
        var width = value.length * 60 + "px";
        return {
            css: {"color": "#196eaa;", "font-weight": "bold", "width": width, "word-break": "break-all"}
        };
    }

    function rewriteFormatter(value, row, index, field) {
        if (value) {
            var width = value.length * 20 + "px";
            return {
                css: {"width": width}
            };
        }
        else {
            return {
                css: {}
            }
        }
    }

    $('#group-virtual-server-table').on('check.bs.table uncheck.bs.table ' +
        'check-all.bs.table uncheck-all.bs.table', function () {
        $('#group-batch-delete-path-bt').prop('disabled', !$('#group-virtual-server-table').bootstrapTable('getSelections').length);
    });
    var selectedVirtualServerObject;
    window.OperationChangeEvent = {
        'click .deleteVirtualSererBt': function (e, value, row) {
            popRemoveVsFn(row);
        },
        'click .change-virtual-path': function (e, value, row) {
            selectedVirtualServerObject = row;
            $('#askEditVServerModal').modal("show").find(".btn-info").click();
            // $scope.popUpdateVirtualPath();
        }
    };
    window.healthCheckOperationEvent = {
        'click .remove-health-bt': function (e, value, row) {
            var str = "<tr>";
            var health = $scope.information.extendedGroup["health-check"];
            str += "<td>" + health.uri + "</td>";
            str += "<td>" + health.intervals + "</td>";
            str += "<td>" + health.fails + "</td>";
            str += "<td>" + health.passes + "</td>";
            str += "</tr>";

            $('#removeHealthCheckModal').modal("show").find('.to-be-removed-health').html(str);

        },
        'click .start-checker': function (e, value, row) {
            var str = "<ul class='ul-appdomains'>";
            $.each($scope.information["group-virtual-servers"], function (i, vs) {
                var protocol = vs.ssl ? "https://" : "http://";
                var port = vs.port;
                $.each(vs.domains, function (i, domain) {
                    var url = protocol + domain.name + ":" + port + $scope.information.extendedGroup["health-check"].uri;
                    str += "<li><a title='打开" + domain.name + "健康检测地址' target='_blank' href='" + url + "'>" + url + "</a></li>";
                });
            });
            str += "</ul>";

            $('#chooseTargetHealthChecker').modal("show").find('.modal-body').html(str);
        }
    };
    $scope.groupInfo = {};
    $scope.diffVersions = {};
    // Load Group Data
    $scope.loadDataInfo = function (groupId) {
        $('#group-virtual-server-table').bootstrapTable("removeAll");
        $('#group-health-table').bootstrapTable("removeAll");
        $('#group-redirect-to-table').bootstrapTable("removeAll");
        $q.all(
            [
                $http.get(G.baseUrl + '/api/logs?type=Group&count=3&targetId=' + groupId).success(
                    function (response) {
                        if (response['operation-log-datas']) {
                            $scope.information.currentlogs = response['operation-log-datas'];
                        }
                    }
                ),
                $http.get(G.baseUrl + "/api/slbs?type=info").success(
                    function (response) {
                        // cache the slbs infromation
                        $.each(response.slbs, function (index, value) {
                            $scope.information.slbs[value.id] = value.name;
                        });
                    }
                ),
                $http.get(G.baseUrl + "/api/vgroup?type=EXTENDED&groupId=" + groupId).success(
                    function (res) {
                        // Group meta data
                        $scope.information.extendedGroup = res;
                    }
                ),

                $http.get(G.baseUrl + "/api/vgroup?groupId=" + groupId + "&mode=online").success(
                    function (res) {
                        if (res.code != undefined) {
                            $scope.information.onlineGroup = {"data": "No Online Data", "version": "Unknow"};
                        }
                        else {
                            $scope.information.onlineGroup = {"data": res};
                        }
                    }
                ),

                $http.get(G.baseUrl + "/api/vgroup?groupId=" + groupId).success(
                    function (res) {
                        $scope.information.offlineGroup = res;
                    }
                ),
                $http.get(G.baseUrl + "/api/policies?type=EXTENDED&groupId=" + groupId).success(function (response) {
                    if (response.total > 0)
                        $scope.information.policies = response['traffic-policies'];
                    else $scope.information.policies = [];
                }),
                $http.get(G.baseUrl + "/api/apps").success(
                    function (response) {
                        $scope.information.apps = _.indexBy(response.apps, 'app-id');
                    }
                ),
                $http.get('/api/auth/current/user').success(
                    function (response) {
                        $scope.query = {};
                        if (response && !response.code) {
                            $scope.query.user = response['name'];
                            $scope.query.email = response.mail;
                        }
                    }
                )
            ]
        ).then(
            function () {
                $scope.groupVersions = [];
                $scope.diffVersions[$scope.information.extendedGroup.version] = $scope.information.extendedGroup.version;
                for (var i = 1; i <= $scope.information.extendedGroup.version; i++) {
                    $scope.groupVersions.push(i);
                }
                // Virtual server meta data
                $scope.getVirtualServerMetaData($scope.information.extendedGroup);

                $scope.loadTable();
            }
        );
    };
    $scope.getOperationText = function (text) {
        var t = '';
        switch (text) {
            case 'new':
                t = '新建';
                break;
            case 'update':
                t = '更新';
                break;
            case 'delete':
                t = '删除';
                break;
            case 'bindVs':
                t = '绑定新的VS';
                break;
            case 'unbindVs':
                t = '解绑VS';
                break;
            case 'activate':
                t = '激活';
                break;
            case 'deactivate':
                t = '下线';
                break;
            default :
                t = '-';
                break;
        }
        return t;
    };
    $scope.generatePolicyLink = function (name) {
        var v = _.find($scope.information.policies, function (r) {
            return r.name.toLowerCase().trim() == name.toLowerCase().trim();
        });
        if (v) return '/portal/policy#?env=' + $scope.env + '&policyId=' + v.id;
    };
    $scope.getVirtualServerMetaData = function (res) {
        $scope.groupVipBindings = {};
        appId = res["app-id"];

        // Assemble the virtual server group information and bind
        var virtualServerInformation = res["group-virtual-servers"];
        $scope.vsCountNumber = virtualServerInformation.length;
        $.each(virtualServerInformation, function (i, vs) {
            var temp = {};
            temp.priority = vs.priority;
            temp.domains = vs["virtual-server"].domains;
            temp.name = vs.name;
            temp.path = vs.path;
            temp.redirect = vs.redirect;
            temp.slb = vs["virtual-server"]["slb-ids"];
            temp.port = vs["virtual-server"].port;
            temp.vs = vs["virtual-server"].id;
            temp.ssl = vs["virtual-server"].ssl;
            temp["custom-conf"] = vs["custom-conf"];
            $scope.information["group-virtual-servers"].push(temp);
            temp.slbvips = [];
            $.each(temp.slb, function (i, item) {
                temp.slbvips.push({
                    slbid: item,
                    vips: $scope.information.slbs[item].vips
                })
            });
        });

        var p = $scope.information.extendedGroup["group-virtual-servers"][0].path;
        var vs2Domains = {};
        $.each($scope.information.extendedGroup["group-virtual-servers"], function (index, item) {
            $.each(item["virtual-server"].domains, function (j, v) {
                vs2Domains[v.name] = v.name;
            });
        });

        $scope.information["v-groups"] = _.filter($scope.vgroups, function (item) {
            return $scope.compareGroupVs(item["group-virtual-servers"], vs2Domains, p);
        });

        var vs2 = $scope.information["group-virtual-servers"];

        $http.get(G.baseUrl + "/api/groups?domain=" + vs2[0].domains[0].name).success(function (res) {
            if (res.code == undefined) {
                $scope.information["group-related"] = _.filter(res.groups, function (item) {
                    return $scope.compareGroupVirtualServers(vs2, item["group-virtual-servers"]);
                });

                $('#group-redirect-to-table').bootstrapTable("load", $scope.information["group-related"]);
            }
        });

    };
    $scope.compareGroupVirtualServers = function (vses1, vses2) {
        var result = true;
        $.each(vses1, function (i, item) {
            var v = _.find(vses2, function (item2) {
                return $scope.compareGroupVirtualServer(item, item2);
            });
            if (!v) {
                result = false;
                return;
            }
        });
        return result;
    };

    $scope.compareGroupVirtualServer = function (vs1, vs2) {
        if (vs1.domains.length != vs2["virtual-server"].domains.length || vs1.ssl == vs2["virtual-server"].ssl) return false;

        var j1 = true;
        $.each(vs1.domains, function (i, item) {
            var f = _.find(vs2["virtual-server"].domains, function (item2) {
                return item2.name == item.name;
            });
            if (!f) j1 = false;
            return;
        });

        var j2 = true;
        if (vs1.path != vs2.path) j2 = false;

        return j1 & j2;
    };
    $scope.initQueryData = function (hashData) {
        if (hashData.env) {
            $scope.env = hashData.env;
        }
        $scope.currentGroupId = hashData.groupId;
    };

    // show tags and porperties
    $scope.showMoreTags = false;
    $scope.showMoreProperties = false;
    $scope.multiTagsClass = function () {
        return $scope.showMoreTags ? '' : 'multi-tags-collapse';
    };
    $scope.collapseTagBtnClass = function () {
        return $scope.showMoreTags ? 'fa fa-chevron-down' : 'fa fa-chevron-left';
    };
    $scope.toggleShowMoreTags = function () {
        $scope.showMoreTags = !$scope.showMoreTags;
    };

    $scope.multiPropertiesClass = function () {
        return $scope.showMoreProperties ? '' : 'multi-tags-collapse';
    };
    $scope.collapsePropertiesBtnClass = function () {
        return $scope.showMoreProperties ? 'fa fa-chevron-down' : 'fa fa-chevron-left';
    };
    $scope.toggleShowMoreProperties = function () {
        $scope.showMoreProperties = !$scope.showMoreProperties;
    };

    // Rights Area
    $scope.showNewVGroupBt = function () {
        return A.canDo("Group", "UPDATE", $scope.information.extendedGroup.id) && $scope.optionPanelStatusBool;
    }
    $scope.showCloneBt = function () {
        return A.canDo("Group", "UPDATE", $scope.information.extendedGroup.id) && $scope.optionPanelStatusBool;
    };
    $scope.showUpdateBt = function () {
        return A.canDo("Group", "UPDATE", $scope.information.extendedGroup.id) && $scope.optionPanelStatusBool;
    };
    $scope.showActivateBt = function () {
        return A.canDo("Group", "ACTIVATE", $scope.information.extendedGroup.id) && $scope.optionPanelStatusBool;
    };
    $scope.showDeactivateBt = function () {
        return A.canDo("Group", "DEACTIVATE", $scope.information.extendedGroup.id) && $scope.optionPanelStatusBool;
    };
    $scope.showDeleteBt = function () {
        return A.canDo("Group", "DELETE", $scope.information.extendedGroup.id) && $scope.optionPanelStatusBool;
    };
    $scope.showHealthUpdateBt = function () {
        return A.canDo("Group", "UPDATE", $scope.information.extendedGroup.id) && $scope.optionPanelStatusBool;
    };
    $scope.showNewPolicyLink = function () {
        return A.canDo("Group", "UPDATE", $scope.groupId);
    };
    $scope.showPolicy = function () {
        return $scope.information.policies && $scope.information.policies.length > 0;
    };
    $scope.showNewBindBt = function () {
        // DYQ: The GVS editing function of VGroup is buggy. Hide the button for now.
        // return A.canDo("Group", "UPDATE", $scope.information.extendedGroup.id) && $scope.optionPanelStatusBool;
        return false;
    };
    $scope.showRemoveBindBt = function () {
        // DYQ: The GVS editing function of VGroup is buggy. Hide the button for now.
        // return A.canDo("Group", "UPDATE", $scope.information.extendedGroup.id) && $scope.optionPanelStatusBool;
        return false;
    };
    $scope.showAddTagBt = function () {
        return A.canDo("Group", "PROPERTY", $scope.information.extendedGroup.id) && $scope.optionPanelStatusBool;
    };
    $scope.showAddProperty = function () {
        return A.canDo("Group", "PROPERTY", $scope.information.extendedGroup.id) && $scope.optionPanelStatusBool;
    };
    $scope.showRemoveTagBt = function () {
        return A.canDo("Group", "PROPERTY", $scope.information.extendedGroup.id) && $scope.optionPanelStatusBool;
    };
    $scope.showRemoveProperty = function (v) {
        var v = v.name;
        if (v.toLowerCase() == "sbu" || v.toLowerCase() == "status") return false;
        return A.canDo("Group", "PROPERTY", $scope.information.extendedGroup.id) && $scope.optionPanelStatusBool;
    };
    $scope.showLogs = function () {
        return $scope.information.currentlogs && $scope.information.currentlogs.length > 0;
    };

    // Group Status&Version Area
    $scope.getGroupStatusProperty = function () {
        var grpStatus = undefined;
        if (!$scope.information.extendedGroup.properties) return grpStatus;

        var p = _.find($scope.information.extendedGroup.properties, function (item) {
            return item.name.toLowerCase().trim() == "status";
        });
        if (p) {
            grpStatus = p.value.toLowerCase().trim();
        }
        return grpStatus;
    };
    $scope.statusText = function () {
        var s = $scope.getGroupStatusProperty();
        switch (s) {
            case "activated":
                return '已激活';
            case "deactivated":
                return '未激活';
            case "tobeactivated":
                return '有变更';
            default:
                return "未知";
        }
    };
    $scope.groupVersionText = function () {
        var s = $scope.getGroupStatusProperty();
        if (s) s = s.toLowerCase();

        var v = $scope.information.onlineGroup.data == undefined ? "unknownText" : $scope.information.onlineGroup.data.version;
        switch (s) {
            case "activated":
                return "已是线上版本";
            case "deactivated":
                return "无线上版本，点击上线";
            case "tobeactivated":
                return "线上版本" + v + "，点击Diff";
            default:
                return "未知";
                break;
        }
    };
    $scope.statusClass = function () {
        var s = $scope.getGroupStatusProperty();
        switch (s) {
            case 'deactivated':
                return 'fa fa-circle status-deactivated';
            case 'activated':
                return 'fa fa-circle status-activated';
            case 'tobeactivated':
                return 'fa fa-circle status-tobeActivated';
            default :
                return 'fa fa-circle status-deactivated';
        }
    };
    $scope.groupVersionClass = function () {
        var v = $scope.getGroupStatusProperty();
        switch (v) {
            case "activated":
                return "status-green link-comment";
            case "deactivated":
                return "status-red activeGroup";
            case "tobeactivated":
                return "status-yellow activeGroup";
            default:
                return "status-gray";
                break;
        }
    };

    // Group Tags showing Area
    $scope.showExtendedTag = function () {
        if ($scope.information.extendedGroup.tags == undefined) return false;
        var u_tag = _.find($scope.information.extendedGroup.tags, function (m) {
            return m.trim().toLowerCase().startWith("user_");
        });
        var p = u_tag && $scope.information.extendedGroup.tags.length == 1;
        var t = $scope.information.extendedGroup.tags == undefined || $scope.information.extendedGroup.tags.length == 0;
        if (t || p) return false;
        return true;
    }
    $scope.showExtendedProperty = function () {
        return !($scope.information.extendedGroup.properties == undefined || $scope.information.extendedGroup.properties.length == 0);
    }
    $scope.getTagShowProperty = function (val) {
        if (!val) return false;

        if (val.toLowerCase().trim().startWith('user_')) {
            return false;
        }
        return true;
    };
    $scope.getBuLinkTarget = function (value) {
        return "/portal/groups#?env=" + G.env + "&groupBues=" + value;
    };

    // Focus Area
    $scope.getFocusObject = function () {
        if ($scope.query == undefined) return undefined;
        var f = _.find($scope.information.extendedGroup.tags, function (item) {
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
        if (f == undefined) return "fa fa-eye-slash status-unfocus";
        return "fa fa-eye status-focus";
    };
    $scope.getFocusText = function () {
        var f = $scope.getFocusObject();
        if (!f) return "关注";
        return "取消关注";
    };
    $scope.addFocus = function (tagName, type) {
        var request = {
            method: 'GET',
            url: G.baseUrl + "/api/tagging?type=group&tagName=" + tagName + "&targetId=" + $scope.information.extendedGroup.id
        };
        $http(request).success(
            function (res) {
                H.setData({"timeStamp": new Date().getTime()});
            });

    };
    $scope.removeFocus = function (tagName, type) {
        var request = {
            method: 'GET',
            url: G.baseUrl + "/api/untagging?type=group&tagName=" + tagName + "&targetId=" + $scope.information.extendedGroup.id
        };
        $http(request).success(
            function (res) {
                H.setData({"timeStamp": new Date().getTime()});
            });
    };

    $scope.optionPanelStatusBool = false;
    // Operation Panel
    $scope.toggleOptionPanel = function () {
        H.setData({'openPanel': !$scope.optionPanelStatusBool});

        $scope.loadTable();
        //trigger the status app click event
        $('#optionPanelBt').click();
    };
    $scope.getOptionPanelText = function () {
        if (!$scope.optionPanelStatusBool) return "打开操作面板";
        return "收起操作面板";
    };
    $scope.getOptionPanelCss = function () {
        if (!$scope.optionPanelStatusBool) {
            return "fa fa-arrows panel-close";
        } else {
            return "fa fa-arrows-alt panel-open";
        }
    };
    //For history version diff
    $scope.toggleVersion = function (x) {
        if ($scope.diffVersions[x]) {
            $scope.diffVersions = _.omit($scope.diffVersions, x);
            $('#historyVersion' + x).removeClass('label-info');
        } else {
            if (_.keys($scope.diffVersions).length < 2) {
                $scope.diffVersions[x] = x;
                $('#historyVersion' + x).addClass('label-info');
            } else {
                $('#historyVersion' + _.keys($scope.diffVersions)[1]).removeClass('label-info');
                $scope.diffVersions = _.omit($scope.diffVersions, _.keys($scope.diffVersions)[1]);
                $scope.diffVersions[x] = x;
                $('#historyVersion' + x).addClass('label-info');
            }
        }
    };
    $scope.isSelectedVersion = function (x) {
        if ($scope.diffVersions[x]) {
            return "label-info";
        }
    };

    // Diff Group history versions
    $('#historyVersionBtn').click(
        function () {
            $scope.diffVersions = {};
            $scope.diffVersions[$scope.information.extendedGroup.version] = $scope.information.extendedGroup.version;

            $.each($scope.groupVersions, function (i, val) {
                if (val != $scope.information.extendedGroup.version)
                    $('#historyVersion' + val).removeClass('label-info');
            });
            $('#historyVersion' + $scope.information.extendedGroup.version).addClass('label-info');
            $('#historyVersionModel').modal('show');
        }
    );

    $scope.showDiffHistory = function () {
        return _.keys($scope.diffVersions).length != 2;
    };
    $scope.diffGroupBetweenVersions = function () {
        var diffVersions = _.keys($scope.diffVersions);
        if (diffVersions.length < 2) {
            alert("请选择两个版本进行diff");
        }
        $scope.confirmActivateText = '版本号:' + diffVersions[0] + " VS 版本号: " + diffVersions[1];

        $q.all(
            [
                $http.get(G.baseUrl + "/api/archive/group?groupId=" + $scope.groupId + "&version=" + diffVersions[0]).success(
                    function (res) {
                        $scope.groupVersion1 = res;
                    }
                ),
                $http.get(G.baseUrl + "/api/archive/group?groupId=" + $scope.groupId + "&version=" + diffVersions[1]).success(
                    function (res) {
                        $scope.groupVersion2 = res;
                    }
                )
            ]
        ).then(
            function () {
                var diffoutputdiv = byId("diffOutput3");
                var baseText = JSON.stringify(U.sortObjectFileds($scope.groupVersion1), null, "\t");
                var newText = JSON.stringify(U.sortObjectFileds($scope.groupVersion2), null, "\t");
                var baseVersion = '版本号:' + diffVersions[0];
                var newVersion = '版本号:' + diffVersions[1];
                diffTwoGroup(diffoutputdiv, baseText, newText, baseVersion, newVersion);

                $('#historyVersionModel').modal('hide');
                $('#diffSeperateGroups').modal('show');
            }
        );
    };

    // Deactivate and delete group actions
    $scope.deactivateGroupAction = function () {
        var status = $scope.getGroupStatusProperty();
        if (status != "deactivated") {
            $scope.loadAllCharts();
            $('#deactivateActivatedGroupModal').modal('toggle');
        }
    };
    $scope.deleteGroupDisable = function () {
        var status = $scope.getGroupStatusProperty();
        if (status == "activated" || status == "tobeactivated") {
            return true;
        }
        return false;
    };
    $scope.deactivateGroupDisable = function () {
        var status = $scope.getGroupStatusProperty();
        if (status == "deactivated") {
            return true;
        }
        return false;
    };
    $scope.deactivateGroup = function () {
        var loading = "<img src='/static/img/spinner.gif' /> 正在下线";
        $('#operationConfrimModel').modal('show').find(".modal-body").html(loading);
        var request = {
            method: 'GET',
            url: G.baseUrl + "/api/deactivate/group?groupId=" + $scope.information.extendedGroup.id
        };
        $('#deactivateGroupModal').modal('hide');
        $scope.processRequest(request, $('#operationConfrimModel'), "下线Group", "下线成功");
    };
    $scope.deleteGroup = function () {
        var groupId = $scope.information.extendedGroup.id;

        var request = {
            method: 'GET',
            url: G.baseUrl + "/api/vgroup/delete?groupId=" + groupId
        };
        $scope.processRequest(request, $('#deleteGroupConfirmModel'), "删除Group", "删除成功");
    };

    $scope.showLinks = function (type) {
        var env = $scope.env;
        return !!(env && G[env].urls[type]);
    };
    $scope.generateMonitorUrl = function (type) {
        var now = new Date().getTime();
        var logStart = now - 1000 * 60 * 60;
        var logEnd = now + 1000 * 60 * 60;
        var dashboardStartTime = $.format.date(logStart, 'yyyy-MM-dd HH:mm:ss');
        var dashboardEndTime = $.format.date(logEnd, 'yyyy-MM-dd HH:mm:ss');
        var env = $scope.env;
        switch (type) {
            case 'es': {
                if (env && G[env].urls.es) {
                    return G[env].urls.es + "?query=group_id%3D'" + $scope.information.extendedGroup.id + "'";
                } else {
                    return '-';
                }
            }
            case 'dashboard': {
                var dashboardEnv = env === 'pro' ? 'PROD' : env.toUpperCase();
                return G.dashboardportal + '/#env=' + dashboardEnv + '&metric-name=slb.req.count&interval=1m&start-time=' + dashboardStartTime + '&end-time=' + dashboardEndTime + '&chart=line&aggregator=sum&ts=1515983913677&tags={"group_id":["' + $scope.information.extendedGroup.id + '"]}&group-by=[status]';
            }
            case 'tengine': {
                return G[env]["urls"]['tengine-es'] + "query=upstream_name%3D'backend_" + $scope.information.extendedGroup.id + "'";
            }
            default:
                return '';
        }
    };

    // Tagging and property adding
    $scope.showDialog = function (value, type) {
        switch (type) {
            case 'deleteTag' : {
                $scope.currentTagName = value;
                $('#deleteGroupTag').modal({backdrop: 'static'});
                break;
            }
            case 'deleteProp' : {
                if ($scope.canDeleteTag(value)) {
                    $scope.currentProperty = value;
                    $('#deleteGroupProperty').modal({backdrop: 'static'});
                }
                break;
            }
            case 'addTag' : {
                $('#addGroupTag').modal({backdrop: 'static'}).find("#tagNameInput").val("");
                break;
            }
            case 'addProp' : {
                $('#addGroupProp').modal({backdrop: 'static'}).find(":input").val("");
                break;
            }
        }
    };
    $scope.canDeleteTag = function (p) {
        var pName = p.name.toLowerCase();
        if (pName == "status" || pName == "sbu") {
            return false;
        }
        return true;
    };
    $scope.deleteTag = function (tagName, type) {
        var request = {
            method: 'GET',
            url: G.baseUrl + "/api/untagging?type=group&tagName=" + tagName + "&targetId=" + $scope.information.extendedGroup.id
        };
        $('#deleteGroupTag').modal('hide');
        $scope.processRequest(request, $('#operationConfrimModel'), "删除Tag");
    };
    $scope.deleteProperty = function (prop) {
        var pname = prop.name;
        var pvalue = prop.value;

        var request = {
            method: 'GET',
            url: G.baseUrl + "/api/property/clear?type=group&pname=" + pname + "&pvalue=" + pvalue + "&targetId=" + $scope.information.extendedGroup.id
        };
        $('#deleteGroupProperty').modal('hide');
        $scope.processRequest(request, $('#operationConfrimModel'), "删除Property", "删除成功");
    };
    $scope.addTag = function (tagName, type) {
        if ($scope.information.extendedGroup.tags == undefined) {
            $scope.information.extendedGroup.tags = [];
        }

        var request = {
            method: 'GET',
            url: G.baseUrl + "/api/tagging?type=group&tagName=" + tagName + "&targetId=" + $scope.information.extendedGroup.id
        };
        $('#addGroupTag').modal('hide');
        $scope.processRequest(request, $('#operationConfrimModel'), "添加Tag", "添加成功");
    };
    $scope.addProperty = function (prop) {
        var pname = prop.name;
        var pvalue = prop.value;

        var existed = _.find($scope.information.extendedGroup.properties, function (item) {
            return item.name.toLowerCase() == pname.toLowerCase();
        });

        var request = {
            method: 'GET',
            url: G.baseUrl + "/api/property/set?type=group&pname=" + pname + "&pvalue=" + pvalue + "&targetId=" + $scope.information.extendedGroup.id
        };
        $('#addGroupProp').modal("hide");
        $scope.processRequest(request, $('#operationConfrimModel'), "添加Property", "添加成功");
    };
    $('#addTagBt').click(function (e) {
        e.preventDefault();
        var validate = reviewData($('#addGroupTag'));
        if (!validate) return;
        $scope.addTag($('#tagNameInput').val().trim());
    });
    $('#addPropertyBt').click(function (e) {
        e.preventDefault();
        var validate = reviewData($('#addGroupProp'));
        if (!validate) return;
        $scope.addProperty({'name': $('#pname').val().trim(), 'value': $('#pvalue').val().trim()});
    });

    // Diff and activate groups
    $scope.confirmDiffButtonDisable = true;
    $scope.targetDiffGroupId;
    $scope.selectDiffGroupId = function (t) {
        if (t) {
            var toId = t.originalObject.id;
            $scope.targetDiffGroupId = toId;
            $scope.confirmDiffButtonDisable = false;
        }
    };

    $scope.activateBtShow = function () {
        return A.canDo("Group", "ACTIVATE", $scope.information.extendedGroup.id);
    }
    $scope.activateGroupTitleClass = function () {
        try {
            if ($scope.information.onlineGroup.data.version != undefined && $scope.information.onlineGroup.data.version == $scope.information.extendedGroup.version) {
                return "status-red-important";
            }
        } catch (e) {

        }
    };
    $scope.confirmActivateText = '线上版本与当前版本比';
    $scope.activateGroupClick = function () {
        if ($scope.information.onlineGroup.data.version != undefined && $scope.information.onlineGroup.data.version == $scope.information.extendedGroup.version) {
            $scope.confirmActivateText = '线上已是最新版本,确认是否强制重新激活';
        }
        var baseText = JSON.stringify(U.sortObjectFileds($scope.information.onlineGroup.data), null, "\t");
        var newText = JSON.stringify(U.sortObjectFileds($scope.information.offlineGroup), null, "\t");
        var baseVersion = '线上Group版本(版本' + $scope.information.onlineGroup.data.version + ")";
        var newVersion = '更新后Group版本(版本' + $scope.information.extendedGroup.version + ")";
        var diffoutputdiv = byId("diffOutput");
        diffTwoGroup(diffoutputdiv, baseText, newText, baseVersion, newVersion);

        $('#activateGroupModal').modal('show');
    };
    $scope.confirmSelectGroupId = function () {
        if ($scope.targetDiffGroupId) {
            // has selected the current targetId
            $scope.confirmActivateText = '当前Group Id' + $scope.information.extendedGroup.id + ". 对比Group Id:" + $scope.targetDiffGroupId;
            var diffoutputdiv = byId("diffOutput3");

            $http.get(G.baseUrl + "/api/group?groupId=" + $scope.targetDiffGroupId).success(
                function (group) {
                    var baseText = JSON.stringify(U.sortObjectFileds($scope.information.offlineGroup), null, "\t");
                    var newText = JSON.stringify(U.sortObjectFileds(group), null, "\t");
                    var baseVersion = '当前Group Id: ' + $scope.information.extendedGroup.id;
                    var newVersion = '对比Group Id:' + group.id;
                    diffTwoGroup(diffoutputdiv, baseText, newText, baseVersion, newVersion);

                    $('#diffSeperateGroups').modal('show');
                }
            );
        }
        else {
            alert("请选择要比较的Group");
        }
    }
    $scope.activateGroup = function () {
        $('#operationConfrimModel').modal("show").find(".modal-title").html("激活Group");
        $('#operationConfrimModel').modal("show").find(".modal-body").html("正在激活.. <img src='/static/img/spinner.gif' />");
        var req = {
            method: 'GET',
            url: G.baseUrl + "/api/activate/group?groupId=" + $scope.information.extendedGroup.id
        };
        $scope.activateGroupRequest = req;

        $scope.processRequest(req, $('#operationConfrimModel'), "激活Group", "激活成功");
    };

    $scope.forceActivateDismiss = function () {
        $('#operationConfrimModel').modal("show").find(".modal-title").html("激活Group");
        $('#operationConfrimModel').modal("show").find(".modal-body").html("正在激活.. <img src='/static/img/spinner.gif' />");

        $scope.activateGroupRequest.params = {
            force: true
        };
        $scope.showForceUpdate = false;
        $scope.processRequest($scope.activateGroupRequest, $('#operationConfrimModel'), "激活Group", "激活成功");
    };
    // Add edit and remove virtual server from a group
    $scope.virtualServersToBeRemoved;
    $scope.groupVirtualServersTable = {
        columns: [],
        add: function (index) {
            this.columns.push({
                "vs-id": "",
                "priority": $scope.information["group-virtual-servers"][0].priority,
                "path": $scope.information["group-virtual-servers"][0].path,
                "rewrite": $scope.information["group-virtual-servers"][0].rewrite
            });
            setTimeout(function () {
                $("#groupVsID" + (index + 1)).bootstrapValidation();
                $("#groupVsPath" + (index + 1)).bootstrapValidation();
                $("#groupVsRewrite" + (index + 1)).bootstrapValidation();
            }, 10);
        },
        edit: function (index) {
            $("#editGroupVirtualServer").hide();
            $("#groupVsPriority").prop('disabled', false);
            $("#groupVsPath").prop('disabled', false);
            $("#groupVsRewrite").prop('disabled', false);
        },
        remove: function (index) {
            this.columns.splice(index, 1);
        }
    };

    $('#group-batch-delete-path-bt').click(
        function (e) {
            e.preventDefault();
            popRemoveVsFn();
        }
    );
    $scope.remoteVsesUrl = function () {
        return G.baseUrl + "/api/meta/vses";
    };
    $scope.cacheRequestFn = function (str) {
        return {q: str, timestamp: new Date().getTime()};
    };
    $scope.selectVsId = function (o) {
        if (o) {
            this.$parent.col['vs-id'] = o.originalObject.id;
            $('#vs0').val(o.originalObject.id);
        } else {
        }
    };

    $scope.confirmRemoveGroupVirtualServer = function () {
        // Delete the target vs from the group
        $('#removeVirtualServerModal').modal("hide");
        var loading = "<img src='/static/img/spinner.gif' /> 正在从Group中删除指定的VirtualServer";
        $('#bindingVsConfrimModel').modal('show').find(".modal-body").html(loading);
        // Ajax call to remove the selected items
        var removedVsIds = [];
        $.each($scope.virtualServersToBeRemoved, function (i, item) {
            removedVsIds.push(item.vs);
            var index = _.findIndex($scope.information.offlineGroup["group-virtual-servers"], function (t) {
                return t["virtual-server"].id == item.vs;
            });
            $scope.information.offlineGroup["group-virtual-servers"].splice(index, 1);
        });

        var request = {
            method: 'POST',
            url: G.baseUrl + '/api/vgroup/update',
            data: $scope.information.offlineGroup
        };
        $scope.firstBindingVsRequest = request;
        var confrimText = "<span class='warning-important'>注意：从Group中下线当前VirtualServer后需激活Group方能生效</span>";
        $scope.processRequest(request, $('#bindingVsConfrimModel'), "从Group中删除指定Virtual Server ", confrimText);
    }

    function popRemoveVsFn(row) {
        if ($scope.information["group-virtual-servers"].length == 1) {
            // There is only one virtual server.
            $('#removeOnlyOneVirtualServerModal').modal("show");
            return;
        }
        var str = "";
        $scope.virtualServersToBeRemoved = [];
        $('.to-be-removed-vses').children().remove();
        // get the selected items
        var selectedItems = $('#group-virtual-server-table').bootstrapTable('getSelections');
        if (selectedItems != undefined && selectedItems.length > 0 && row == undefined) {
            // Batch remove
            $.each(selectedItems, function (index, m) {
                $scope.virtualServersToBeRemoved.push({"vs": m.vs, "path": m.path});
                // $scope.info.membersTobeRemoved.push({"ip": m.ip, "port": m.port});
                str += "<tr>";
                str += "<td><span class='path'>" + m.path + "</span></td>";
                str += "<td><span class='ssl'>" + m.port + "</span></td>";
                var rewrite = m.redirect;
                if (rewrite == undefined || rewrite == "") {
                    rewrite = "-";
                }
                str += "<td><span class='rewrite'>" + rewrite + "</span></td>";
                str += "<td><span class='priority'>" + m.priority + "</span></td>";
                str += "<td><span class='ssl'>" + m.ssl + "</span></td>";
                str += "<td><span class='ssl'>" + m.slb + "</span></td>";
                str += "<td><span class='vs'>" + m.vs + "</span></td>";

                var s = "";
                $.each(m.domains, function (i, d) {
                    s += "<span>" + d.name + "</span></br>";
                });
                str += "<td><span class='domain'>" + s + "</span></td>";
                str += "</tr>";
            });
        }
        else {
            if (row) {
                $scope.virtualServersToBeRemoved.push({"vs": row.vs, "path": row.path});

                str += "<tr>";
                str += "<td><span class='path'>" + row.path + "</span></td>";
                str += "<td><span class='ssl'>" + row.port + "</span></td>";
                var rewrite = row.redirect;
                if (rewrite == undefined || rewrite == "") {
                    rewrite = "-";
                }
                str += "<td><span class='rewrite'>" + rewrite + "</span></td>";
                str += "<td><span class='priority'>" + row.priority + "</span></td>";
                str += "<td><span class='ssl'>" + row.ssl + "</span></td>";
                str += "<td><span class='ssl'>" + row.slb + "</span></td>";
                str += "<td><span class='vs'>" + row.vs + "</span></td>";

                var s = "";
                $.each(row.domains, function (i, d) {
                    s += "<span>" + d.name + "</span></br>";
                });
                str += "<td><span class='domain'>" + s + "</span></td>";
                str += "</tr>";
            }
        }
        $('.to-be-removed-vses').append(str);
        $('#removeVirtualServerModal').modal('show');
    }

    $scope.focusGField = function (field) {
        switch (field) {
            case "path": {
                $('.field-description').html("<b class='status-red'>应与原Group的Path相同</b>");
                break;
            }
            case "redirect": {
                $('.field-description').html('<span>* Redirect: SLB转发规则。 实例 Http请求转Https:<b  style="color: red">"(?i)(.*)" https://$host$1;</b></span><span>实例 Https请求转Http: <b  style="color: red">"(?i)(.*)" http://$host$1;</b></span>');
                break;
            }
            case "priority": {
                $('.field-description').html('<span>* Rewrite: Group所在VirtualServer的优先级。 默认: <b  style="color: red">1000</b></span>');
                break;
            }
        }
    }

    $scope.vsBindingDisabled = true;
    $("#saveNewVirtualServer").click(function (event) {
        $('[data-validator-type="validation"]').bootstrapValidation("reset");
        $('[data-validator-type="validation"]').bootstrapValidation();
        var reviewResult = reviewData($('.add-vs-dv'));

        if (reviewResult) {
            $('#addVirtualServerModel').modal("hide");
            $('#doubleConfirmAddGroupVirtualServerModal').modal("show");
        }
    });
    $scope.popNewVirtualPath = function () {
        // New Virtual Server binding
        $scope.groupVirtualServersTable.columns = [];
        var path = $scope.information["group-virtual-servers"][0].path;
        var showPath = $scope.getShowPath(path);

        // new vs
        $scope.groupVirtualServersTable.columns.push(
            {
                "vs-id": "",
                "priority": "",
                "path": path,
                "showPath": showPath,
                "redirect": $scope.information["group-virtual-servers"][0].redirect
            }
        );
        $('#addVirtualServerModel').modal("show").find(".field-description").html('');
    };

    $scope.showForceUpdate = false;
    $scope.confirmAddGroupVirtualServer = function () {
        $scope.showForceUpdate = false;
        // Start loading
        var loading = "<img src='/static/img/spinner.gif' /> 正在添加新的Group Virtual Server配置";
        $('#bindingVsConfrimModel').modal('show').find(".modal-body").html(loading);
        // each column stands for a virtual server
        var vs = [];
        $.each($scope.groupVirtualServersTable.columns, function (i, item) {
            delete item["$$hashKey"];

            item["vs-id"] = parseInt(item["vs-id"]);
            if (item.priority != undefined || item.priority == "") {
                item.priority = parseInt(item.priority);
            }
            if ($scope.showSuffixText(item.path)) {
                item.path = start + item.showPath + end;
            }
            else {
                item.path = item.showPath;
            }
            delete item["showPath"];

            vs.push({
                "path": item.path,
                "redirect": item.redirect,
                "priority": item.priority,
                "virtual-server": {
                    "id": item["vs-id"]
                }
            });
        });
        var c = $scope.information.offlineGroup["group-virtual-servers"];
        var f = c.concat(vs);

        $scope.information.offlineGroup["group-virtual-servers"] = f;

        var request = {
            method: 'POST',
            url: G.baseUrl + '/api/vgroup/update',
            data: $scope.information.offlineGroup
        };

        $scope.firstBindingVsRequest = request;

        var confrimText = "<span class='warning-important'>注意:修改Virtual Server信息后需激活Group方能生效</span>";
        $scope.processRequest(request, $('#bindingVsConfrimModel'), "修改Virtual Server信息", confrimText);
    };


    $scope.showSuffixText = function (path) {
        if (!path) return true;
        return path.startWith(start) && path.endWith(end);
    };

    $scope.getShowPath = function (path) {
        var showPath = "";
        var showSuffix = $scope.showSuffixText(path);
        if (path && showSuffix) {
            // Regular path
            showPath = path.substring(4, path.lastIndexOf(end));
        } else {
            showPath = path;
        }
        return showPath;
    }

    $('#saveEditVirtualServer').click(function () {
        /* $('[data-validator-type="validation"]').bootstrapValidation("reset");
         $('[data-validator-type="validation"]').bootstrapValidation();*/
        var reviewResult = reviewData($('.edit-vs-table'));
        if (reviewResult) {
            $('#editVirtualServerModel').modal("hide");
            $('#doubleConfirmEditGroupVirtualServerModal').modal("show");
        }
    });
    $scope.firstBindingVsRequest;
    $scope.confirmEditGroupVirtualServer = function () {
        // Start loading
        var loading = "<img src='/static/img/spinner.gif' /> 正在更新Group Virtual Server配置";
        $('#bindNewVSResultDialog').modal('show').find(".modal-body").html(loading);
        // each column stands for a virtual server
        var vs = [];

        $.each($scope.groupVirtualServersTable.columns, function (i, item) {
            delete item["$$hashKey"];
            item["vs-id"] = parseInt(item["vs-id"]);
            if (item.priority != undefined) {
                item.priority = parseInt(item.priority);
            }
            if ($scope.showSuffixText(item.path)) {
                item.path = start + item.showPath + end;
            }
            else {
                item.path = item.showPath;
            }
            delete item["showPath"];

            vs.push({
                "path": item.path,
                "redirect": item.redirect,
                "priority": item.priority,
                "virtual-server": {
                    "id": item["vs-id"]
                }
            });
        });

        $scope.information.offlineGroup["group-virtual-servers"] = vs;

        var request = {
            method: 'POST',
            url: G.baseUrl + '/api/vgroup/update',
            data: $scope.information.offlineGroup
        };
        $scope.firstBindingVsRequest = request;

        var confrimText = "<span class='warning-important'>注意:修改Virtual Server信息后需激活Group方能生效</span>";
        $scope.processRequest(request, $('#bindNewVSResultDialog'), "修改Virtual Server信息", confrimText);
    };

    $scope.forceBindVSDismiss = function () {
        var loading = "<img src='/static/img/spinner.gif' /> 正在强制更新Group Virtual Server配置";
        $('#operationConfrimModel').modal('show').find(".modal-body").html(loading);
        var confrimText = "<span class='warning-important'>注意:修改Virtual Server信息后需激活Group方能生效</span>";
        var request = $scope.firstBindingVsRequest;
        if (request.params) {
            request.params.force = true;
        }
        else {
            request.params = {force: true};
        }
        $scope.processRequest(request, $('#operationConfrimModel'), "强制更新Virtual Group", "更新成功", confrimText);
    }
    $scope.popUpdateVirtualPath = function () {
        $scope.vsBindingDisabled = true;
        $('#askEditVServerModal').modal("hide");
        $scope.groupVirtualServersTable.columns = [];
        var path = selectedVirtualServerObject.path;
        var showPath = $scope.getShowPath(path);
        // update vs
        $scope.groupVirtualServersTable.columns.push(
            {
                "vs-id": selectedVirtualServerObject.vs.toString(),
                "priority": selectedVirtualServerObject.priority,
                "path": path,
                "showPath": showPath,
                "redirect": selectedVirtualServerObject.redirect
            }
        );

        $('#editVirtualServerModel').modal("show").find('#groupUpdateAction').val("update");
    };

    $scope.confirmAddPriority = function () {
        $('#groupVsPriority0').prop("disabled", false);
        $('#NewGroupPriority').hide();
    }
    $scope.confirmAddRewrite = function () {
        $('#groupNewVsRewrite0').prop("disabled", false);
        $('#newGroupRewrite').hide();
    }
    $scope.confirmEditPriority = function () {
        $('#groupEditVsPriority0').prop("disabled", false);
        $('#EditGroupPriority').hide();
    }
    $scope.confirmEditRewrite = function () {
        $('#groupEditVsRewrite0').prop("disabled", false);
        $('#editGroupRewrite').hide();
    }
    $scope.confirmEditPath = function () {
        $('#groupEditVsPath0').prop("disabled", false);
        $('#pathEditRootSetting0').prop("disabled", false);
        $('#editVsPath').hide();
    }
    $scope.confirmEditVs = function () {
        $scope.vsBindingDisabled = false;
        $('#editVsId').hide();
    }
    $scope.setAddedPathAsRoot = function () {
        var c = $('#pathAddRootSetting0').is(':checked');
        if (c) {
            $scope.groupVirtualServersTable.columns[0].path = "~* ^/";
            $scope.groupVirtualServersTable.columns[0].showPath = "~* ^/";
            $('#groupNewVsPath0').prop("disabled", true);
        }
        else {
            $('#groupNewVsPath0').prop("disabled", false);
            $scope.groupVirtualServersTable.columns[0].path = $scope.information["group-virtual-servers"][0].path;
            $scope.groupVirtualServersTable.columns[0].showPath = $scope.getShowPath($scope.information["group-virtual-servers"][0].path);
        }
    };
    $scope.setEditPathAsRoot = function (p) {
        var c = $('#pathEditRootSetting0').is(':checked');
        if (c) {
            $scope.groupVirtualServersTable.columns[0].path = "~* ^/";
            $scope.groupVirtualServersTable.columns[0].showPath = "~* ^/";
            $('#groupEditVsPath0').prop("disabled", true);
        }
        else {
            $('#groupEditVsPath0').prop("disabled", false);
            $scope.groupVirtualServersTable.columns[0].path = selectedVirtualServerObject.path;
            $scope.groupVirtualServersTable.columns[0].showPath = $scope.getShowPath(selectedVirtualServerObject.path);
        }
    }
    $scope.editPriorityBtClick = function () {
        var confrimAsk = confirm("Priority建议由系统自动生成，你确定要修改吗?");
        if (confrimAsk)
            $('#groupVsPriority0').prop("disabled", false);
    }
    $scope.editVsBtClick = function () {
        var confrimAsk = confirm("VirtualServerId决定当前Group的域名，你确定要修改吗?");
        if (confrimAsk)
            $scope.editVsBinding2 = false;
    }

    // Health check table actions
    $scope.popNewHealthCheck = function () {
        var health = $scope.information.extendedGroup["health-check"];
        if (health == undefined) {
            health = {
                timeout: 2000,
                uri: "",
                intervals: 5000,
                fails: 3,
                passes: 1
            };
        }
        $scope.information.extendedGroup["health-check"] = health;
        $('#addHealthCheckModal').modal("show");
    };
    $scope.confirmRemoveHealthChecker = function () {
        delete $scope.information.extendedGroup["health-check"];
        // update the group with latest health check url
        delete $scope.information.extendedGroup.properties;
        delete $scope.information.extendedGroup.tags;
        var request = {
            method: 'POST',
            url: G.baseUrl + "/api/group/update",
            data: $scope.information.extendedGroup
        };
        var loading = "<img src='/static/img/spinner.gif' /> 正在修改HealthCheck信息";
        $('#operationConfrimModel').modal('show').find(".modal-body").html(loading);

        $('#removeHealthCheckModal').modal('hide');
        var confrimText = "<span class='warning-important'>注意:删除健康监测信息后需激活Group方能生效</span>";
        $scope.processRequest(request, $('#operationConfrimModel'), "删除健康监测信息", confrimText);
    };
    $('#saveHealthCheck').click(function (e) {
        var validate = reviewData($('#add-health-table'));
        if (!validate) return;
        $('#addHealthCheckModal').modal("hide");
        $('#doubleConfirmEditHealthCheckModal').modal("show");
    });
    $scope.confirmEditHealthChecker = function () {
        delete $scope.information.extendedGroup.properties;
        delete $scope.information.extendedGroup.tags;
        // update group
        var request = {
            method: 'POST',
            url: G.baseUrl + "/api/group/update",
            data: $scope.information.extendedGroup
        };
        var loading = "<img src='/static/img/spinner.gif' /> 正在修改HealthCheck信息";
        $('#operationConfrimModel').modal('show').find(".modal-body").html(loading);
        $('#addHealthCheckModal').modal('hide');
        var confrimText = "<span class='warning-important'>注意:修改健康监测信息后需激活Group方能生效</span>";

        $scope.processRequest(request, $('#operationConfrimModel'), "修改健康监测信息", confrimText);
    };

    // GroupVirtualServers table
    $scope.toggleFullCustomConf = function (index) {
        var shortConf = $('#custom_conf_short_' + index);
        var fullConf = $('#custom_conf_full_' + index);
        if (shortConf.hasClass('hidden')) {
            shortConf.removeClass('hidden');
            fullConf.addClass('hidden');
            shortConf.siblings('a').text('More');
        } else {
            shortConf.addClass('hidden');
            fullConf.removeClass('hidden');
            shortConf.siblings('a').text('Less');
        }
    };

    // Chart area
    $scope.openCalendar = function (e) {
        e.preventDefault();
        e.stopPropagation();
        $scope.isOpen = true;
    };
    $scope.setDateNow = function () {
        var d = new Date();
        d = d.setTime(d.getTime() - 1000 * 60 * 60);

        var time = new Date($.format.date(d, 'yyyy-MM-dd HH:00'));
        $scope.chart.startTime = time;
    };
    $scope.setDateNextHour = function () {
        $scope.chart.startTime = new Date($.format.date($scope.chart.startTime.getTime() + 60 * 1000 * 60, 'yyyy-MM-dd HH:00'));
        $scope.chart.endTime = new Date($scope.chart.startTime.getTime() + 1000 * 60 * 90);
    };
    $scope.setDatePreviousHour = function () {
        $scope.chart.startTime = new Date($.format.date($scope.chart.startTime.getTime() - 60 * 1000 * 60, 'yyyy-MM-dd HH:00'));
        $scope.chart.endTime = new Date($scope.chart.startTime.getTime() + 1000 * 60 * 90);
    };
    $scope.loadAllCharts = function () {
        $scope.startTime = $scope.chart.startTime;
        $scope.endTime = new Date($scope.chart.startTime.getTime() + 1000 * 60 * 90);

        $scope.loadTrafficCharts();
    };
    $scope.loadTrafficCharts = function () {
        var chartContainer = $('#chart');
        chartContainer.empty();

        var params = {
            'metric-name': 'slb.req.count',
            'start-time': $.format.date($scope.startTime, 'yyyy-MM-dd HH:mm'),
            'end-time': $.format.date($scope.endTime, 'yyyy-MM-dd HH:mm'),
            'tags': '{"group_id":["' + $scope.information.extendedGroup.id + '"]}',
            'interval': '1m',
            'chart': 'line',
            'aggregator': 'sum',
            'downsampler': 'sum'
        };
        var config = {
            'url': G[G.env].urls.dashboard + "/data",
            'method': 'GET',
            'params': params
        };

        $scope.buildChart(chartContainer, "Group:" + $scope.information.extendedGroup.name + ",当前1小时流量确认", config, true, -1);
    };
    $scope.deactivateBtEnabled = true;
    $scope.buildChart = function (chartContainer, title, config, needMark, groupByCount, groupByLinkFun) {
        $scope.deactivateBtEnabled = true;
        var chartDom = $('<div class="col-md-12 traffic-chart"></div>');
        chartContainer.append(chartDom);
        var chart = newChart(chartDom.get(0));
        config.withCredentials = false;
        $http(config).success(
            function (res) {
                if (res["result-code"] != undefined && res["result-code"] == 0) {
                    loadChartData(chart, res, title, "", new Date($scope.startTime).getTime(), 60000, needMark, groupByCount, groupByLinkFun);
                }
                else {
                    res = {};
                    res['time-series-group-list'] = [];
                    loadChartData(chart, res, title, "", new Date($scope.startTime).getTime(), 60000, needMark, groupByCount, groupByLinkFun);
                }
            }
        ).finally(
            function () {
                $scope.deactivateBtEnabled = false;
            }
        );
    };
    $scope.queryData = function () {
        $scope.loadAllCharts();
    };
    $scope.loadTable = function () {
        $scope.initTable();
        var p1 = A.canDo("Group", "UPDATE", $scope.groupId) && $scope.optionPanelStatusBool;
        // DYQ: The GVS editing function of VGroup is buggy. Hide the operation column for now.
        p1 = false;
        if (!p1) {
            $('#group-virtual-server-table').bootstrapTable('hideColumn', 'operate');
            $('#group-health-table').bootstrapTable('hideColumn', 'operate');
        }
        else {
            $('#group-virtual-server-table').bootstrapTable('showColumn', 'operate');
            $('#group-health-table').bootstrapTable('showColumn', 'operate');
        }
        $('#group-virtual-server-table').bootstrapTable("load", $scope.information["group-virtual-servers"]);
        var heathArr = [];
        if ($scope.information.extendedGroup['health-check']) {
            heathArr.push($scope.information.extendedGroup['health-check']);
        }
        $('#group-health-table').bootstrapTable("load", heathArr);
    };
    $scope.hashChanged = function (hashData) { $scope.resource = H.resource;
        $scope.showForceUpdate = false;
        if (typeof(hashData.openPanel) == "undefined") {
            $scope.optionPanelStatusBool = false;
        } else {
            $scope.optionPanelStatusBool = hashData.openPanel == "true" ? true : false;
        }

        if (hashData.groupId) {
            $scope.groupId = hashData.groupId;
        }
        $scope.initQueryData(hashData);
        $scope.loadDataInfo(hashData.groupId);

        // load the traffic echarts
        $scope.setDateNow();
        $scope.chart.endTime = new Date($scope.chart.startTime.getTime() + 1000 * 60 * 90);
    };
    H.addListener("groupInfoApp", $scope, $scope.hashChanged);
    // Helpers
    var byId = function (id) {
        return document.getElementById(id);
    }
    $('.closeProgressWindowBt').click(
        function (e) {
            var hashData = {};
            hashData.timeStamp = new Date().getTime();
            H.setData(hashData);
        }
    );
    $('.closeProgressWindowBt2').click(
        function (e) {
            if ($('#deleteGroupConfirmModel').find('.fa-check').length > 0) {
                // Success
                window.location.href = "/portal/groups#?env=" + G.env + "&timeStamp=" + new Date().getDate();
            }
            if ($('#deleteGroupConfirmModel').find('.fa-times') > 0) {
                // Success
            }

        }
    );

    /**** Common functions****/

    function diffTwoGroup(targetDiv, baseText, newText, baseVersion, newVersion) {
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

    // Field validation
    function reviewData(id) {
        var result = true;
        $.each(id.find('[data-validator-type="validation"]'), function (i, element) {
            if (!$(element).bootstrapValidation('validate'))
                result = false;
        });
        return result;
    };
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

    String.prototype.endWith = function (s) {
        if (s == null || s == "" || this.length == 0 || s.length > this.length)
            return false;
        if (this.substring(this.length - s.length) == s)
            return true;
        else
            return false;
        return true;
    }

    String.prototype.startWith = function (s) {
        if (s == null || s == "" || this.length == 0 || s.length > this.length)
            return false;
        if (this.substr(0, s.length) == s)
            return true;
        else
            return false;
        return true;
    }
    /***END OF COMMON FUNCTIONS***/
});
angular.bootstrap(document.getElementById("group-info-area"), ['groupInfoApp']);
//InfoLinksComponent: info links
var headerInfoApp = angular.module('headerInfoApp', ["angucomplete-alt"]);
headerInfoApp.controller('headerInfoController', function ($scope, $http) {
    $scope.query = {};
    $scope.generateLink = function (a) {
        var link = '';
        switch (a) {
            case 'basic': {
                link = "/portal/vgroup#?env=" + G.env + "&groupId=" + $scope.query.groupId;
                break;
            }
            case 'log': {
                link = "/portal/vgroup/log#?env=" + G.env + "&groupId=" + $scope.query.groupId;
                break;
            }
            case 'conf': {
                link = "/portal/vgroup/conf#?env=" + G.env + "&groupId=" + $scope.query.groupId;
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
        if (hashData.groupId) {
            $scope.query.groupId = hashData.groupId;
        }
    };
    H.addListener("infoLinksApp", $scope, $scope.hashChanged);
});
angular.bootstrap(document.getElementById("header-area"), ['headerInfoApp']);
$(document).ready(function () {
    $('[data-toggle="tooltip"]').tooltip();
});
