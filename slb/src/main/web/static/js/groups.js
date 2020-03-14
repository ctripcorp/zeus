//GroupsQuery Component: GroupsQueryController
var groupsQueryApp = angular.module('groupsQueryApp', ["angucomplete-alt", "http-auth-interceptor"]);
groupsQueryApp.controller('groupsQueryController', function ($scope, $http) {
    $scope.query = {
        "groupid": "",
        "groupname": "",
        "slbname": "",
        "vsname": "",
        "vsid": "",
        "slbid": "",
        "appid": "",
        "hostipsearch": "",
        "tags": {},
        "states": {},
        "ssl": {},
        "grouptypes": {},
        "grouphealths": {},
        "grouplanguages": {},
        "groupsoa": {},
        "bues": {},
        "domains": {}
    };
    $scope.data = {
        groupArr: [],
        slbArr: [],
        vsArr: [],
        appArr: [],
        hostIpArr: [],
        tagArr: [],
        domainArr: [],
        buArr: [],
        statusArr: [],
        sslArr: [],
        groupTypeArr: [],
        healthArr: [],
        arrowMore: true
    };

    $scope.getStatusLanguage = function (x) {
        var resource = $scope.resource;

        if (resource) {
            return resource['groups']['groups_groupsQueryApp_statuses'][x];
        }
    };

    $scope.getSoaLanguage = function (x) {
        var resource = $scope.resource;

        if (resource) {
            return resource['groups']['groups_groupsQueryApp_soas'][x];
        }
    };

    $scope.getHealthyLanguage = function (x) {
        var resource = $scope.resource;

        if (resource) {
            return resource['groups']['groups_groupsQueryApp_healthymap'][x];
        }
    };

    //Load cache Area
    $scope.cacheRequestFn = function (str) {
        return {q: str, timestamp: new Date().getTime()};
    };

    $scope.remoteGroupsUrl = function () {
        return G.baseUrl + "/api/meta/groups";
    };

    $scope.remoteSLBsUrl = function () {
        return G.baseUrl + "/api/meta/slbs";
    };

    $scope.remoteVsesUrl = function () {
        return G.baseUrl + "/api/meta/vses";
    };

    $scope.remoteAppsUrl = function () {
        return G.baseUrl + "/api/meta/apps";
    };

    $scope.remoteServersUrl = function () {
        return G.baseUrl + "/api/meta/servers";
    };

    $scope.remoteDomainsUrl = function () {
        return G.baseUrl + "/api/meta/domains";
    };

    //Load Query Data Area
    $scope.dataLoaded = false;
    $scope.loadData = function (hashData) {
        if ($scope.env == hashData.env && $scope.dataLoaded) return;
        $scope.dataLoaded = true;
        $scope.env = hashData.env;

        // refresh the query data object
        $scope.data = {
            groupArr: [],
            slbArr: [],
            vsArr: [],
            appArr: [],
            hostIpArr: [],
            tagArr: [],
            domainArr: [],
            buArr: [],
            statusArr: [],
            sslArr: [],
            groupTypeArr: [],
            arrowMore: true
        };

        //Load tagsArr
        $http.get(G.baseUrl + "/api/tags?type=group").success(function (res) {
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
                $scope.data.buArr = bues;
            }
        );

        // Load status data
        //Set status
        $scope.data.statusArr = ["已激活", "有变更", "未激活"];
        $scope.data.sslArr = ["HTTP", "HTTPS"];
        $scope.data.languages = ["Java", ".NET"];
        $scope.data.groupSoaArr = ["是"];
        $scope.data.groupTypeArr = ["Group", "V-Group"];
        $scope.data.healthArr = {
            healthCheckHealthy: "健康拉出",
            pullHealthy: "发布拉出",
            memberHealthy: "Member拉出",
            serverHealthy: "Server拉出",
            healthy: "Broken"
        };
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
    $scope.statusClear = function () {
        $scope.query.states = [];
    };
    $scope.SSLClear = function () {
        $scope.query.ssl = {};
    };
    $scope.GroupTypeClear = function () {
        $scope.query.grouptypes = {};
    };
    $scope.GroupHealthyClear = function () {
        $scope.query.grouphealths = {};
    };
    $scope.GroupLanguageClear = function () {
        $scope.query.grouplanguages = {};
    };
    $scope.GroupSOAClear = function () {
        $scope.query.groupsoa = {};
    };


    $scope.showClear = function (type) {
        if (type == "bu") {
            return _.keys($scope.query.bues).length > 0 ? "link-show" : "link-hide";
        }
        if (type == "status") {
            return _.keys($scope.query.states).length > 0 ? "link-show" : "link-hide";
        }
        if (type == "ssl") {
            return _.keys($scope.query.ssl).length > 0 ? "link-show" : "link-hide";
        }
        if (type == "groupType") {
            return _.keys($scope.query.grouptypes).length > 0 ? "link-show" : "link-hide";
        }
        if (type == "grouphealth") {
            return _.keys($scope.query.grouphealths).length > 0 ? "link-show" : "link-hide";
        }
        if (type == "grouplanguage") {
            return _.keys($scope.query.grouplanguages).length > 0 ? "link-show" : "link-hide";
        }
        if (type == "groupSOA") {
            return _.keys($scope.query.groupsoa).length > 0 ? "link-show" : "link-hide";
        }
    };

    // Input changed event
    $scope.groupIdInputChanged = function (o) {
        $scope.query.groupid = o;
    };
    $scope.slbIdInputChanged = function (o) {
        $scope.query.slbid = o;
    };
    $scope.vsIdInputChanged = function (o) {
        $scope.query.vsid = o;
    };
    $scope.appIdInputChanged = function (o) {
        $scope.query.appid = o;
    };
    $scope.groupServerInputChanged = function (o) {
        $scope.query.hostip = o;
    };

    // Select input field
    $scope.selectGroupId = function (o) {
        if (o) {
            $scope.query.groupid = o.originalObject.id;
        }
    };
    $scope.selectGroupName = function (o) {
        if (o) {
            $scope.query.groupname = o.originalObject.name.split("|")[1];
        }
    };
    $scope.selectSlbId = function (o) {
        if (o) {
            $scope.query.slbid = o.originalObject.id;
        } else {
        }
    };
    $scope.selectVsId = function (o) {
        if (o) {
            $scope.query.vsid = o.originalObject.id;
        } else {
        }
    };
    $scope.selectTag = function (tag) {
        if (tag) {
            $scope.query.tags[tag.originalObject.name] = tag.originalObject.name;
        }
    };
    $scope.removeTag = function (tag) {
        delete $scope.query.tags[tag];
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

    $scope.toggleSSL = function (ssl) {
        if ($scope.query.ssl[ssl]) {
            delete $scope.query.ssl[ssl];
        } else {
            $scope.query.ssl[ssl] = ssl;
        }
    };
    $scope.isSelectedSSL = function (ssl) {
        if ($scope.query.ssl[ssl]) {
            return "label-info";
        }
    };

    $scope.isSelectedGroupType = function (t) {
        if ($scope.query.grouptypes[t]) {
            return "label-info";
        }
    };
    $scope.toggleGroupType = function (t) {
        if ($scope.query.grouptypes[t]) {
            delete $scope.query.grouptypes[t];
        } else {
            $scope.query.grouptypes[t] = t;
        }
    };

    $scope.isSelectedHealthy = function (t, v) {
        if ($scope.query.grouphealths[t] && $scope.query.grouphealths[t] == v) {
            return "label-info";
        }
    };
    $scope.toggleGroupHealthy = function (t, v) {
        if ($scope.query.grouphealths[t]) {
            delete $scope.query.grouphealths[t];
        } else {
            $scope.query.grouphealths[t] = v;
        }
    };

    $scope.isSelectedLanguage = function (t) {
        if ($scope.query.grouplanguages[t]) {
            return "label-info";
        }
    };
    $scope.toggleGroupLanguage = function (t) {
        if ($scope.query.grouplanguages[t]) {
            delete $scope.query.grouplanguages[t];
        } else {
            $scope.query.grouplanguages[t] = t;
        }
    };

    $scope.isSelectedGroupSOA = function (t) {
        if ($scope.query.groupsoa[t]) {
            return "label-info";
        }
    };
    $scope.toggleGroupSOA = function (t) {
        if ($scope.query.groupsoa[t]) {
            delete $scope.query.groupsoa[t];
        } else {
            $scope.query.groupsoa[t] = t;
        }
    };

    $scope.selectAppId = function (o) {
        if (o) {
            $scope.query.appid = o.originalObject.id;
        }
    };
    $scope.selectHostIp = function (o) {
        if (o) {
            $scope.query.hostip = o.originalObject.id;
        }
    };
    $scope.selectDomain = function (domain) {
        if (domain) {
            $scope.query.domains[domain.originalObject.name] = domain.originalObject.id;
        }
    };
    $scope.removeDomain = function (domain) {
        delete $scope.query.domains[domain];
    };
    $scope.clearQuery = function () {
        $scope.query.groupid = "";
        $scope.query.groupname = "";
        $scope.query.slbname = "";
        $scope.query.vsname = "";
        $scope.query.slbid = "";
        $scope.query.vsid = "";
        $scope.query.appid = "";
        $scope.query.hostip = "";

        $scope.query.bues = {};
        $scope.query.tags = {};
        $scope.query.states = {};
        $scope.query.ssl = {};
        $scope.query.grouptypes = {};
        $scope.query.grouphealths = {};
        $scope.query.domains = {};
        $scope.query.grouplanguages = {};
        $scope.setInputsDisplay();
    };
    $scope.executeQuery = function () {
        var hashData = {};
        hashData.groupId = $scope.query.groupid || "";
        hashData.groupName = $scope.query.groupname || "";
        hashData.slbId = $scope.query.slbid || "";
        hashData.vsId = $scope.query.vsid || "";
        hashData.appId = $scope.query.appid || "";
        hashData.hostIp = $scope.query.hostip || "";
        hashData.domains = _.values($scope.query.domains);
        hashData.ssl = _.values($scope.query.ssl);
        hashData.groupType = _.values($scope.query.grouptypes);
        var keys = _.keys($scope.query.grouphealths);
        var h = '';
        $.each(keys, function (i, item) {
            if (i < keys.length - 1) {
                h += item + ':' + $scope.query.grouphealths[item] + ',';
            } else {
                h += item + ':' + $scope.query.grouphealths[item];
            }
        });
        hashData.groupHealthy = h;
        hashData.groupLanguages = _.values($scope.query.grouplanguages);
        hashData.groupSOA = _.values($scope.query.groupsoa);

        hashData.groupStatus = _.values($scope.query.states);
        hashData.groupBues = _.values($scope.query.bues);
        hashData.groupTags = _.values($scope.query.tags);
        hashData.timeStamp = new Date().getTime();
        H.setData(hashData);
    };
    //Init input field while hashChanged
    $scope.setInputsDisplay = function () {
        $('#groupIdSelector_value').val($scope.query.groupid);
        $('#slbIdSelector_value').val($scope.query.slbid);
        $('#groupNameSelector_value').val($scope.query.groupname);
        $('#vsIdSelector_value').val($scope.query.vsid);
        $('#appIdSelector_value').val($scope.query.appid);
        $('#hostIpSelector_value').val($scope.query.hostip);
    };
    $scope.applyHashData = function (hashData) {
        $scope.query.groupname = hashData.groupName;
        $scope.query.groupid = hashData.groupId;
        $scope.query.slbid = hashData.slbId;
        $scope.query.vsid = hashData.vsId;
        $scope.query.appid = hashData.appId;
        $scope.query.hostip = hashData.hostIp;
        $scope.query.states = {};
        if (hashData.groupLanguages) {
            $.each(hashData.groupLanguages.split(","), function (i, val) {
                $scope.query.grouplanguages[val] = val;
            })
        }
        if (hashData.groupSOA) {
            $.each(hashData.groupSOA.split(","), function (i, val) {
                $scope.query.groupsoa[val] = val;
            })
        }
        if (hashData.groupStatus) {
            $.each(hashData.groupStatus.split(","), function (i, val) {
                $scope.query.states[val] = val;
            })
        }

        $scope.query.ssl = {};
        if (hashData.ssl) {
            $.each(hashData.ssl.split(","), function (i, val) {
                $scope.query.ssl[val] = val;
            })
        }

        $scope.query.grouptypes = {};
        if (hashData.groupType) {
            $.each(hashData.groupType.split(","), function (i, val) {
                $scope.query.grouptypes[val] = val;
            })
        }

        $scope.query.grouphealths = {};
        if (hashData.groupHealthy) {
            $.each(hashData.groupHealthy.split(","), function (i, val) {
                var temp = val.split(':');
                var k = temp[0];
                var v = temp[1];
                $scope.query.grouphealths[k] = v;
            })
        }


        $scope.query.tags = {};
        if (hashData.groupTags) {
            $.each(hashData.groupTags.split(","), function (i, val) {
                $scope.query.tags[val] = val;
            })
        }

        $scope.query.bues = {};
        if (hashData.groupBues) {
            $.each(hashData.groupBues.split(","), function (i, val) {
                $scope.query.bues[val] = val;
            });
        }

        $scope.query.domains = {};
        if (hashData.domains) {
            $.each(hashData.domains.split(","), function (i, val) {
                $scope.query.domains[val] = val;
            })
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
angular.bootstrap(document.getElementById("groups-query-area"), ['groupsQueryApp']);

var groupsResultApp = angular.module("groupsResultApp", ['http-auth-interceptor']);
groupsResultApp.controller("groupsResultController", function ($scope, $http, $q) {
    $scope.data = {
        "groups": "",
        "groupCount": 0,
        "membersCount": 0,
        "serversCount": 0,
        "groupStatusCount": []
    };
    $scope.query = {};
    $scope.groups = [];
    $scope.vgroups = [];
    $scope.allGroups = [];
    $scope.tableOps = {
        groups: {
            showMoreColumns: false,
            showOperations: false
        }
    };

    var currentUser;
    // Partial show
    $scope.disableOpenGroups = function () {
        var can = A.canDo('Group', 'UPDATE', '*');
        return !can;
    };
    $scope.getGroupsShowMore = function () {
        return $scope.tableOps.groups.showMoreColumns ? 'fa fa-list-alt text-info' : 'fa fa-list-alt font-gray';
    };
    $scope.getGroupsShowOperation = function () {
        return $scope.tableOps.groups.showOperations ? 'fa fa-wrench text-info' : 'fa fa-wrench font-gray';
    };
    $scope.toggleShowMoreGroupsColumns = function () {
        $scope.tableOps.groups.showMoreColumns = !$scope.tableOps.groups.showMoreColumns;
        if ($scope.tableOps.groups.showMoreColumns) {
            $('#groups-data-table').bootstrapTable('showColumn', 'paths');
        } else {
            $('#groups-data-table').bootstrapTable('hideColumn', 'paths');
        }
    };
    $scope.toggleShowGroupsOperations = function () {
        $scope.tableOps.groups.showOperations = !$scope.tableOps.groups.showOperations;
    };
    $scope.getGroupsOperationTitle = function () {
        return $scope.tableOps.groups.showOperations ? '关闭操作' : '打开操作';
    };
    $scope.getGroupsShowMoreTitle = function () {
        return $scope.tableOps.groups.showOperations ? '显示简略信息' : '显示详细信息';
    };

    // Area: Right Area
    $scope.showNewGroupBt = function () {
        return A.canDo('Group', 'UPDATE', '*') && $scope.tableOps.groups.showOperations;
    };
    $scope.showAddTagBt = function () {
        return A.canDo('Group', 'PROPERTY', '*') && $scope.tableOps.groups.showOperations;
    };

    // Area: Table definition and table formatters
    $scope.initTable = function () {
        var resource = $scope.resource;
        var table = resource['groups']['groups_groupsResultApp_table'];


        var idtitle = table['id'];
        var nametitle = table['name'];
        var pathtitle = table['paths'];
        var apptitle = table['app'];
        var idctitle = table['idc'];
        var memberstitle = table['members'];
        var qpstitle = table['qps'];
        var healthytitle = table['healthy'];
        var statustitle = table['status'];
        var loadingtitle = table['loading'];
        var nodatatitle = table['nodata'];

        var statusmap = resource['groups']['groups_groupsResultApp_StatusMap'];

        $('#groups-data-table').bootstrapTable({
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
                        var groupPageUrl = "";
                        if (row["isvgroup"] != undefined) {
                            groupPageUrl = '/portal/vgroup#?env=' + G.env + '&groupId=' + value;
                            return '<span class="fa  fa-magic status-gray" title="Virtual Group"><a title="' + value + '" href="' + groupPageUrl + '">' + value + '</a></span>';
                        }
                        else {
                            groupPageUrl = '/portal/group#?env=' + G.env + '&groupId=' + value;
                            return '<a title="' + value + '" href="' + groupPageUrl + '">' + value + '</a>';
                        }
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
                        var groupPageUrl = "";
                        if (row["isvgroup"] != undefined) {
                            groupPageUrl = '/portal/vgroup#?env=' + G.env + '&groupId=' + row.id;
                        }
                        else {
                            groupPageUrl = '/portal/group#?env=' + G.env + '&groupId=' + row.id;
                        }
                        return '<a title="' + value + '" href="' + groupPageUrl + '"><div style="word-break: break-all">' + value + '</div></a>';
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
                                var tempStr = '<div style="width: 50%;" class="path-item-cell path-main-item';
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

                                priorityStr = '&nbsp;<span class="">' + v.priority + '</span>';
                                sslStr = '&nbsp;<span class="">' + (v.ssl ? 'https' : 'http') + '</span>';
                                vsIdStr = '&nbsp;<a class="" target="_blank" href="' + '/portal/vs#?env=' + G.env + '&vsId=' + v.vsId + '" title="' + v.domain + '">' + v.vsId + '</a>';
                                slbIdStr += '(';
                                $.each(v.slbId, function (i, slbId) {
                                    if (i === v.slbId.length - 1) {
                                        slbIdStr += '<a class="" target="_blank" href="' + '/portal/slb#?env=' + G.env + '&slbId=' + slbId + '">' + slbId + '</a>';
                                    } else {
                                        slbIdStr += '<a class="" target="_blank" href="' + '/portal/slb#?env=' + G.env + '&slbId=' + slbId + '">' + slbId + '</a>,';
                                    }
                                });
                                slbIdStr += ')';

                                tempStr +=
                                    '<div style="width: 50%;" class="path-item-cell path-item-list">' +
                                    '<div class="col-md-3 path-item-list">' + priorityStr + '</div>' +
                                    '<div class="col-md-3 path-item-list">' + sslStr + '</div>' +
                                    '<div class="col-md-3 path-item-list">' + vsIdStr + '</div>' +
                                    '<div class="col-md-3 path-item-list">' + slbIdStr + '</div>' +
                                    '</div>';
                                result = result + '<div class="row" style="margin:0;display:table;width:100%;">' + tempStr + '</div>';
                            }
                        });
                        result = '<div class="row" style="margin:0">' + result + '</div>';
                        return result;
                    }
                },
                {
                    field: 'app-name',
                    title: apptitle,
                    align: 'left',
                    valign: 'middle',
                    width: '300px',
                    sortable: true,
                    formatter: function (value, row, index) {
                        if (value == undefined) value = row['app-id'];
                        else value = row['app-id'] + '(' + value + ')'
                        return "<a style='word-break:break-all' title='" + value + "' target='_blank' href='/portal/app#?env=" + G.env + "&appId=" + row['app-id'] + "'>" + value + "</a>";
                    }
                },
                {
                    field: 'idc',
                    title: idctitle,
                    align: 'left',
                    resetWidth: true,
                    valign: 'middle',
                    sortable: true,
                    formatter: function (value, row, index) {
                        return value;
                    }
                },
                {
                    field: 'group-servers',
                    title: memberstitle,
                    align: 'left',
                    valign: 'middle',
                    formatter: function (value, row, index) {
                        if (value) {
                            return value.length;
                        } else {
                            return '-';
                        }
                    },
                    sortable: true
                },
                {
                    field: 'qps',
                    title: qpstitle,
                    align: 'left',
                    valign: 'middle',
                    formatter: function (value, row, index) {
                        return value ? '<a target="_blank" href="/portal/group/traffic' + H.generateHashStr({
                            env: G.env,
                            groupId: row.id
                        }) + '">' + Math.floor(value) + '</a>' : '-';
                    },
                    sortable: true
                },
                {
                    field: 'status',
                    title: statustitle,
                    align: 'left',
                    valign: 'middle',
                    formatter: function (value, row, index) {
                        var str = "";
                        switch (value) {
                            case "deactivated":
                                str = "<span class='status-gray'>"+statusmap['未激活']+"</span>";
                                break;
                            case "activated":
                                str = "<span class='status-green'>"+statusmap['已激活']+"</span>";
                                break;
                            case "toBeActivated":
                                if (row.isvgroup) {
                                    str = "<span class='diffVGroup status-yellow'>"+statusmap['有变更']+"(<a class='' data-toggle='modal' data-target='#activateVGroupModal'>Diff</a>)</span>";
                                }
                                else {
                                    str = "<span class='diffGroup status-yellow'>"+statusmap['有变更']+"(<a class='' data-toggle='modal' data-target='#activateGroupModal'>Diff</a>)</span>";
                                }
                                break;
                            default:
                                str = "-";
                                break;
                        }
                        return str;
                    },
                    events: operateEvents,
                    sortable: true
                },
                {
                    field: 'grouphealthy',
                    title: healthytitle,
                    valign: 'middle',
                    align: 'center',
                    formatter: function (value, row, index) {
                        if (row.isvgroup) {
                            return '<a><span title="VGroup不涉及健康检测">-</span></a>';
                        }
                        var str = '';
                        switch (value) {
                            case "healthy":
                                str = '<a href="/portal/group#?env=' + G.env + '&groupId=' + row.id + '"><span class="fa fa-circle status-green"></span></a>';
                                break;
                            case "broken":
                                str = '<a href="/portal/group#?env=' + G.env + '&groupId=' + row.id + '"><span class="fa fa-circle status-red"></span></a>';
                                break;
                            case "unhealthy":
                                str = '<a href="/portal/group#?env=' + G.env + '&groupId=' + row.id + '"><span class="fa fa-circle status-yellow"></span></a>';
                                break;
                            default:
                                str = "-";
                                break;
                        }
                        return str;
                    },
                    sortable: true
                }], []],
            search: true,
            showRefresh: true,
            showColumns: true,
            sortName: 'qps',
            sortOrder: 'desc',
            data: $scope.allGroups,
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
                return "<img class='loading-img' src='/static/img/loading_100.gif' /> "+loadingtitle;
            },
            formatNoMatches: function () {
                return '<i class="fa status-alert fa-exclamation-triangle" aria-hidden="true"></i> '+nodatatitle;
            }
        });
    };

    function responseHandler(res) {
        $.each(res.rows, function (i, row) {
            row.state = $.inArray(row.id, selections) !== -1;
        });
        return res;
    };

    // Activate Groups Area
    $scope.onlineGroupData;
    $scope.tobeActivatedGroupData;
    $scope.confirmActivateText = "当前版本与线上版本比较";
    window.operateEvents = {
        'click .activeGroup': function (e, value, row, index) {
            popActivateGroupDialog(row);
        },
        'click .activeVGroup': function (e, value, row, index) {
            popActivateVGroupDialog(row);
        },
        'click .diffGroup': function (e, value, row, index) {
            popActivateGroupDialog(row);
        },
        'click .diffVGroup': function (e, value, row, index) {
            popActivateVGroupDialog(row);
        }
    };
    $scope.activateGroupTitleClass = function () {
        try {
            if ($scope.information.onlineGroup.data.version != undefined && $scope.information.onlineGroup.data.version == $scope.information.group.version) {
                return "status-red-important";
            }
        } catch (e) {

        }
    };

    function popActivateGroupDialog(row) {
        var resource = $scope.resource;
        var pop = resource['groups']['groups_groupsResultApp_activatedialog'];

        var groupId = row.id;
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

                var online = pop['线上Group版本'];
                var offline = pop['更新后Group版本'];

                $scope.confirmActivateText = pop["线上版本与当前版本比较"];

                if (row.status == "activated") {
                    $scope.confirmActivateText = pop["线上已是最新版本,确认是否强制重新激活"];

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
                var diffoutputdiv = byId("diffOutput");

                diffoutputdiv.innerHTML = "";

                diffoutputdiv.appendChild(diffview.buildView({
                    baseTextLines: base,
                    newTextLines: newtxt,
                    opcodes: opcodes,
                    baseTextName: online + "(Version:" + $scope.onlineGroupData.version + ")",
                    newTextName: offline + "(Version:" + $scope.tobeActivatedGroupData.version + ")",
                    viewType: 0
                }));
            }
        );

    }

    $scope.onlineVGroupData;
    $scope.tobeActivatedVGroupData;

    function popActivateVGroupDialog(row) {
        var resource = $scope.resource;
        var pop = resource['groups']['groups_groupsResultApp_activatedialog'];
        var online = pop['线上Group版本'];
        var offline = pop['更新后Group版本'];
        getSingleVGroup(row.id);
        $scope.confirmActivateText = pop["线上版本与当前版本比较"];
        if (row.status == "activated") {
            $scope.confirmActivateText = pop["线上已是最新版本,确认是否强制重新激活"];

            $('.fileViewHead').removeClass("status-red-important").addClass("status-red-important");
        }
        else {
            $('.fileViewHead').removeClass("status-red-important");
        }

        setTimeout(function () {
            var baseText = JSON.stringify(U.sortObjectFileds($scope.onlineVGroupData), null, "\t");
            var newText = JSON.stringify(U.sortObjectFileds($scope.tobeActivatedVGroupData), null, "\t");

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
                baseTextName: online + "(version:" + $scope.onlineVGroupData.version + ")",
                newTextName: offline + "(version:" + $scope.tobeActivatedVGroupData.version + ")",
                viewType: 0
            }));

        }, 500);
    }

    $scope.activateGroup = function () {
        var resource = $scope.resource;
        var pop = resource['groups']['groups_groupsResultApp_activatedialog'];
        var activating = pop['正在激活'];
        var activatefail = pop['激活失败'];
        var activatesuccess = pop['激活成功'];
        var failreason = pop['失败原因'];
        var confirmDialog = $('#activateGroupResultConfirmDialog');
        var loading = "<img src='/static/img/spinner.gif' /> " + activating;
        confirmDialog.modal('show').find(".modal-body").html(loading);
        confirmDialog.modal("show").find(".modal-title").html(activating);

        $('#confirmActivateGroup').modal('hide');
        var request = {
            method: 'GET',
            url: G.baseUrl + "/api/activate/group?groupId=" + $scope.tobeActivatedGroupData.id
        };
        var msg = "";
        $http(request).success(
            function (res) {
                if (res == undefined || res.code != undefined) {
                    msg = res.message;
                    var errText = "<span class='fa fa-times'></span><span style='padding-left: 10px'> '+activatefail+'</span>";
                    confirmDialog.modal('show').find(".modal-title").html(errText);
                    confirmDialog.modal('show').find(".modal-body").html(failreason + msg);
                }
                else {
                    var successText = "<span class='fa fa-check'></span><span style='padding-left: 10px'>'+activatesuccess+'</span>";
                    confirmDialog.modal('show').find(".modal-title").html(successText);
                    confirmDialog.modal('show').find(".modal-body").html(activatesuccess);
                    setTimeout(function () {
                        confirmDialog.find('#closeActivateConfrimBt').click();
                    }, 2000);
                }
            }
        ).error(function (reject) {
            msg = reject.message;
            var errText = "<span class='fa fa-times'></span><span style='padding-left: 10px'>" + activatefail + "</span>";
            confirmDialog.modal('show').find(".modal-title").html(errText);
            confirmDialog.modal('show').find(".modal-body").html(failreason + msg);
        });
    };
    $scope.activateVGroup = function () {
        var confirmDialog = $('#activateGroupResultConfirmDialog');
        var loading = "<img src='/static/img/spinner.gif' /> 正在激活";
        confirmDialog.modal('show').find(".modal-body").html(loading);
        confirmDialog.modal("show").find(".modal-title").html("正在激活");

        $('#confirmActivateVGroup').modal('hide');
        var request = {
            method: 'GET',
            url: G.baseUrl + "/api/activate/group?groupId=" + $scope.tobeActivatedVGroupData.id
        };
        var msg = "";
        $http(request).success(
            function (res) {
                if (res == undefined || res.code != undefined) {
                    msg = res.message;
                    var errText = "<span class='fa fa-times'></span><span style='padding-left: 10px'> 激活失败</span>";
                    confirmDialog.modal('show').find(".modal-title").html(errText);
                    confirmDialog.modal('show').find(".modal-body").html("失败原因:" + msg);
                }
                else {
                    var successText = "<span class='fa fa-check'></span><span style='padding-left: 10px'>激活成功</span>";
                    confirmDialog.modal('show').find(".modal-title").html(successText);
                    confirmDialog.modal('show').find(".modal-body").html("激活成功");
                    setTimeout(function () {
                        confirmDialog.find('#closeActivateConfrimBt').click();
                    }, 2000);
                }
            }
        ).error(function (reject) {
            msg = reject.message;
            var errText = "<span class='fa fa-times'></span><span style='padding-left: 10px'>激活失败</span>";
            confirmDialog.modal('show').find(".modal-title").html(errText);
            confirmDialog.modal('show').find(".modal-body").html("失败原因:" + msg);
        });
    }
    $('#closeActivateConfrimBt, #closeNewTagConfrimBt').click(function (e) {
        var hashData = {};
        hashData.timeStamp = new Date().getTime();
        H.setData(hashData);
    });
    $('#groups-data-table').on('check.bs.table uncheck.bs.table ' +
        'check-all.bs.table uncheck-all.bs.table', function () {
        $('#createTag').prop('disabled', !$('#groups-data-table').bootstrapTable('getSelections').length);
    });

    // Create Group Area
    $('#createGroup').click(function () {
        window.location.href = "/portal/group/new#?env=" + G.env;
    });

    // Group tag and properties Area
    $scope.addTag = function (tagName) {
        // get the selected groups
        var selected = $('#groups-data-table').bootstrapTable('getSelections');

        var ids = [];
        $.each(selected, function (i, item) {
            ids.push(item.id);
        });

        var parameters = {
            'type': 'group',
            'tagName': tagName,
            'targetId': _.values(ids)
        };

        var request = {
            method: 'GET',
            url: G.baseUrl + "/api/tagging",
            params: parameters
        };

        // call tagging api
        $http(request).success(
            function () {
                $('#createGroupTagDialog').modal("hide");
                $('#operationResultModal').modal('show').find(".modal-title").text("新建Tag结果:");
                $('#operationResultModal').modal('show').find(".modal-body").text("成功给GroupId:" + _.values(ids) + ". 添加了Tag: " + tagName);

                setTimeout(function () {
                    $('#closeNewTagConfrimBt').click();
                }, 1000);

            }
        ).error(function (reject) {
            $('#operationResultModal').modal('show').find(".modal-title").text("新建Tag结果:")
            $('#operationResultModal').modal('show').find(".modal-body").text("添加Tag失败,失败原因:" + reject.message);
        });
    };
    $('#addTagBt').click(function (e) {
        e.preventDefault();
        var validate = reviewData($('#createGroupTagDialog'));
        if (!validate) return;
        $scope.addTag($('#tagNameText').val().trim());
    });

    function reviewData(id) {
        var result = true;
        $.each(id.find('[data-validator-type="validation"]'), function (i, element) {
            if (!$(element).bootstrapValidation('validate'))
                result = false;
        });
        return result;
    };
    $scope.confirmResultClick = function () {

    }

    // Summary information methods Area
    $scope.toggleCountSearch = function (stateText, value) {
        if (value != 0) {
            //window.location.href="/portal/groups#?env="+ G.env+"&groupStatus="+stateText;
            var hashData = {};
            hashData.env = G.env;
            hashData.groupStatus = stateText;
            hashData.timeStamp = new Date().getTime();
            H.setData(hashData);
        }
    };
    $scope.toggleHealthySearch = function (stateText, value) {
        if (value != 0) {
            //window.location.href="/portal/groups#?env="+ G.env+"&groupStatus="+stateText;
            var hashData = {};
            hashData.env = G.env;
            hashData.groupHealthy = 'healthy:' + stateText;
            hashData.timeStamp = new Date().getTime();
            H.setData(hashData);
        }
    };
    $scope.countSearchClass = function (value) {
        var v = value == 0 ? 'link-comment' : '';
        return v;
    };

    function getSingleVGroup(groupId) {
        $http.get(G.baseUrl + "/api/vgroup?groupId=" + groupId + "&mode=online").then(
            function successCallback(res) {
                if (res.data.code) {
                    $scope.onlineVGroupData = "No online version!!!";
                } else {
                    $scope.onlineVGroupData = res.data;
                }
            },
            function errorCallback(res) {
                if (res.status == 400) {
                    $scope.onlineVGroupData = "No online version!!!";
                }
            }
        );

        $http.get(G.baseUrl + "/api/vgroup?groupId=" + groupId).success(
            function (res) {
                $scope.tobeActivatedVGroupData = res;
            }
        );
    }

    $scope.showHealthAndCountSummarize = true;

    $scope.getGroupsQueryString = function (hashData) {
        var queryString = G.baseUrl + "/api/groups?type=EXTENDED";
        var domains;
        var ssls;
        var tags;
        // If there is only one env hashdata in the url
        if (_.keys(hashData).length == 1 && hashData.env) {
            $scope.query.showGroups = false;
            return '';
        } else {
            $scope.query.showGroups = true;
            var groups = hashData.groupBues;
            if (Array.isArray(groups)) {
                groups = _.map(groups, function (s) {
                    if (s == '用户研究与设计') s = '用户研究%26设计';
                    return s;
                });
            } else {
                if (groups == '用户研究与设计') groups = '用户研究%26设计';
            }
            queryString = SpellQueryString(queryString, hashData.groupId, "groupId");
            queryString = SpellQueryString(queryString, hashData.groupName, "fuzzyName");
            queryString = SpellQueryString(queryString, hashData.vsId, "vsId");
            queryString = SpellQueryString(queryString, hashData.slbId, "slbId");
            queryString = SpellQueryString(queryString, hashData.appId, "appId");
            queryString = SpellQueryString(queryString, hashData.hostIp, "ip");

            queryString = SpellPropertyQueryString(queryString, {"language": hashData.groupLanguages});
            queryString = SpellPropertyQueryString(queryString, {"status": hashData.groupStatus});
            queryString = SpellPropertyQueryString(queryString, {"SBU": groups});
            queryString = SpellPropertyQueryString(queryString, {"migrationDoneWeek": hashData.migrationDoneWeek});
            if (hashData.groupHealthy) {
                queryString = SpellMultiplePropertyQueryString(queryString, hashData.groupHealthy);
            }
            if (hashData.groupTags == "" || hashData.groupTags == undefined) tags = [];
            else tags = hashData.groupTags.split(',');
            if (tags.length > 0) {
                queryString = SpellTagsQueryString(queryString, tags);
            }

            var soaTags = [];
            if (hashData.groupSOA == "" || hashData.groupSOA == undefined) soaTags = [];
            else soaTags = hashData.groupSOA.split(',');
            if (soaTags.length > 0) {
                soaTags = ['soa'];
                queryString = SpellTagsQueryString(queryString, soaTags);
            }

            // Domain
            if (hashData.domains == "" || hashData.domains == undefined) domains = [];
            else domains = hashData.domains.split(',');
            if (domains.length > 0) {
                queryString = SpellCollectionQueryString(queryString, domains, "domain");
            }

            // Group ssl
            if (hashData.ssl == "" || hashData.ssl == undefined) ssls = [];
            else ssls = hashData.ssl.split(',');
            var convertedSSL = [];
            $.each(ssls, function (i, s) {
                if (s == "HTTP") {
                    convertedSSL.push("false");
                }
                else {
                    convertedSSL.push("true");
                }
            });
            if (convertedSSL.length > 0) {
                queryString = SpellCollectionQueryString(queryString, convertedSSL, "ssl");
            }
        }
        return queryString;
    };
    $scope.getVGroupsQueryString = function (hashData) {
        var domains;
        var ssls;
        var tags;
        var queryString = G.baseUrl + "/api/vgroups?type=EXTENDED";
        // If there is only one env hashdata in the url
        if (_.keys(hashData).length == 1 && hashData.env) {
            return '';
        } else {
            var groups = hashData.groupBues;
            if (Array.isArray(groups)) {
                groups = _.map(groups, function (s) {
                    if (s == '用户研究与设计') s = '用户研究%26设计';
                    return s;
                });
            } else {
                if (groups == '用户研究与设计') groups = '用户研究%26设计';
            }
            queryString = SpellQueryString(queryString, hashData.groupId, "groupId");
            queryString = SpellQueryString(queryString, hashData.groupName, "fuzzyName");
            queryString = SpellQueryString(queryString, hashData.vsId, "vsId");
            queryString = SpellQueryString(queryString, hashData.slbId, "slbId");
            queryString = SpellQueryString(queryString, hashData.appId, "appId");
            queryString = SpellQueryString(queryString, hashData.hostIp, "ip");
            if (hashData.groupHealthy) {
                queryString = SpellMultiplePropertyQueryString(queryString, hashData.groupHealthy);
            }
            // for group status
            queryString = SpellPropertyQueryString(queryString, {"status": hashData.groupStatus});
            queryString = SpellPropertyQueryString(queryString, {"SBU": groups});


            if (hashData.groupTags == "" || hashData.groupTags == undefined) tags = [];
            else tags = hashData.groupTags.split(',');
            if (tags.length > 0) {
                queryString = SpellTagsQueryString(queryString, tags);
            }

            // Domain
            if (hashData.domains == "" || hashData.domains == undefined) domains = [];
            else domains = hashData.domains.split(',');
            if (domains.length > 0) {
                queryString = SpellCollectionQueryString(queryString, domains, "domain");
            }

            // Group ssl
            if (hashData.ssl == "" || hashData.ssl == undefined) ssls = [];
            else ssls = hashData.ssl.split(',');
            var convertedSSL = [];
            $.each(ssls, function (i, s) {
                if (s == "HTTP") {
                    convertedSSL.push("false");
                }
                else {
                    convertedSSL.push("true");
                }
            });
            if (convertedSSL.length > 0) {
                queryString = SpellCollectionQueryString(queryString, convertedSSL, "ssl");
            }
        }
        return queryString;
    };

    function SpellQueryString(queryString, property, tag) {
        if (property) {
            var v = property.split(":")[0];
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

    function SpellMultiplePropertyQueryString(queryString, property) {
        var temp = {};
        if (property != undefined) {
            var t = property.split(',');
            $.each(t, function (i, item) {
                var v = item.split(':');
                temp[v[0]] = v[1];
            });

            var query = "anyProp=";
            var keys = _.keys(temp);

            $.each(keys, function (i, key) {
                if (temp[key]) {
                    var v2 = temp[key];
                    query += key + ":" + getRealpValues(v2) + ",";
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

    function getRealpValues(val) {
        switch (val) {
            case 'Java':
                return 'java';
            case '.NET':
                return '.net';
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

    function SpellCollectionQueryString(queryString, tags, tag) {
        if (queryString.endsWith('?')) {
            queryString += (tag + "=");
        }
        else {
            queryString += "&" + tag + "=";
        }
        $.each(tags, function (i, val) {
            queryString += val + ",";
        });
        queryString = queryString.substr(0, queryString.lastIndexOf(','));
        return queryString;
    }

    $scope.members = {};
    $scope.getGroupMetaData = function (group) {
        var g_servers = group["group-servers"];
        $.each(g_servers, function (i, g) {
            if ($scope.members[g.ip] == undefined) {
                $scope.sCount++;
                $scope.members[g.ip] = "";
            }
        });
        // get the group status
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
        });
    };
    $scope.getGroupQPSMetaData = function (group) {
        if ($scope.groupStatisticsMap[group.id]) {
            group.qps = $scope.groupStatisticsMap[group.id].qps;
            group['member-count'] = $scope.groupStatisticsMap[group.id]['member-count'];
            $scope.mCount += $scope.groupStatisticsMap[group.id]['member-count'];
        }
    };
    $scope.healthStatsticData = {};
    $scope.getGroupsHealthStastics = function (groups) {
        $scope.healthStatsticData = _.countBy(groups, function (item) {
            var str;
            if (item.properties) {
                var h = _.find(item.properties, function (p) {
                    return p.name == "healthy";
                });
                if (h) {
                    switch (h.value) {
                        case "healthy":
                            str = 'healthy';
                            break;
                        case "unhealthy":
                            str = 'unhealthy';
                            break;
                        case "broken":
                            str = 'broken';
                            break;
                        default:
                            str = '-';
                            break;
                    }
                    return str;
                }
            }
        });

        if (!$scope.healthStatsticData.healthy) $scope.healthStatsticData.healthy = 0;
        if (!$scope.healthStatsticData.unhealthy) $scope.healthStatsticData.unhealthy = 0;
        if (!$scope.healthStatsticData.broken) $scope.healthStatsticData.broken = 0;

    };
    $scope.getAppInfoMetaData = function (val) {
        if ($scope.appInfoMap[val['app-id']]) {
            val['app-name'] = $scope.appInfoMap[val['app-id']]['chinese-name'];
        }
    };

    $scope.showGroupsResult = function () {
        return $scope.query.showGroups;
    };
    $scope.getGroupsProperties = function () {

    };
    // Get Groups Data Area
    $scope.loadData = function (hashData) {
        $('.groups-text').text(0);
        $('.activate-group-text').text(0);
        $('.tobeactivated-group-text').text(0);
        $('.deactivated-group-text').text(0);
        $('.healthy-group-text').text(0);
        $('.unhealthy-group-text').text(0);
        $('.broken-group-text').text(0);
        $('.server-text').text(0);
        $('.qps-text').text(0);
        $('.member-text').text(0);
        $scope.showHealthAndCountSummarize = true;
        $scope.groups = [];
        $scope.vgroups = [];

        var groupsqueryString;
        var vgroupsqueryString;
        var queryLogics = [];

        groupsqueryString = $scope.getGroupsQueryString(hashData);
        vgroupsqueryString = $scope.getVGroupsQueryString(hashData);

        if (!groupsqueryString && !vgroupsqueryString) {
            // first time loading
            $('#groups-data-table').bootstrapTable("hideLoading");
            // init top summary
            var pairs = {
                healthy: ['healthy', 'unhealthy', 'broken'],
                status: ['activated', 'deactivated', 'tobeactivated']
            };

            var results = {};
            var queries = _.map(_.keys(pairs), function (v) {
                // /api/property/query/targets?type=group&pname=healthy&pvalue=healthy
                var innerMap = _.map(pairs[v], function (s) {
                    var url = G.baseUrl + '/api/property/query/targets?type=group&pname=' + v + '&pvalue=' + s;
                    var request = {
                        method: 'GET',
                        url: url
                    };
                    return $http(request).success(function (res, code) {
                        if (!results[v]) {
                            results[v] = {};
                            results[v][s] = res.targets;
                        } else {
                            results[v][s] = res.targets;
                        }
                    });
                });
                return innerMap;
            });
            queries = _.flatten(queries);

            var groupsStasticsRequest = $http({
                method: 'GET',
                url: G.baseUrl + '/api/statistics/groups'
            }).success(function (response, code) {
                results['statistics'] = response;
            });

            var groupsInfoQuery = $http({
                method: 'GET',
                url: G.baseUrl + '/api/groups?type=info'
            }).success(function (response, code) {
                results['groups'] = response;
            });

            var vgroupsInfoQuery = $http({
                method: 'GET',
                url: G.baseUrl + '/api/vgroups?type=info'
            }).success(function (response, code) {
                results['vgroups'] = response;
            });

            queries = queries.concat(groupsStasticsRequest).concat(groupsInfoQuery).concat(vgroupsInfoQuery);

            $q.all(queries).then(
                function () {

                    var serverCount = _.reduce(_.pluck(results['statistics']['group-metas'], 'group-server-count'), function (a, b) {
                        return a + b;
                    });
                    var memberCount = _.reduce(_.pluck(results['statistics']['group-metas'], 'member-count'), function (a, b) {
                        return a + b;
                    });

                    var qps = _.reduce(_.pluck(results['statistics']['group-metas'], 'qps'), function (a, b) {
                        return a + b;
                    });

                    var groupIds = _.pluck(results['groups']['groups'], 'id');
                    var vgroupIds = _.pluck(results['vgroups']['groups'], 'id');

                    groupIds = groupIds.concat(vgroupIds);

                    // not in groups'
                    var activated = _.intersection(results['status']['activated'], groupIds);
                    var deactivated = _.intersection(results['status']['deactivated'], groupIds);
                    var tobeactivated = _.intersection(results['status']['tobeactivated'], groupIds);
                    var healthy = _.intersection(results['healthy']['healthy'], groupIds);
                    var unhealthy = _.intersection(results['healthy']['unhealthy'], groupIds);
                    var broken = _.intersection(results['healthy']['broken'], groupIds);

                    $('.groups-text').text(results['statistics']['group-metas'].length);
                    $('.activate-group-text').text(activated.length);
                    $('.tobeactivated-group-text').text(tobeactivated.length);
                    $('.deactivated-group-text').text(deactivated.length);
                    $('.healthy-group-text').text(healthy.length);
                    $('.unhealthy-group-text').text(unhealthy.length);
                    $('.broken-group-text').text(broken.length);
                    $('.server-text').text(T.getText(serverCount));
                    $('.member-text').text(T.getText(memberCount));
                    $('.qps-text').text(T.getText(qps));
                }
            );

            return;
        }
        var groupType = hashData.groupType;
        var gs;
        if (groupType != undefined) {
            gs = groupType.toLowerCase().split(',');
        }
        if (gs == undefined || (Array.isArray(groupType) && groupType.length == 2)) {
            // groups and v-groups
            queryLogics = [
                $http.get(groupsqueryString).success(
                    function (res) {
                        $scope.groupQueryResult = res;
                        if (res.groups) {
                            $scope.groups = res.groups;
                        } else {
                            $scope.groups = [];
                        }
                    }
                ),

                $http.get(G.baseUrl + "/api/apps").success(
                    function (response) {
                        $scope.appInfoMap = {};
                        $.each(response['apps'], function (i, val) {
                            $scope.appInfoMap[val['app-id']] = val;
                        });
                    }
                ),
                $http.get(G.baseUrl + "/api/statistics/groups").success(
                    function (response) {
                        $scope.groupStatisticsMap = {};
                        $.each(response["group-metas"], function (index, meta) {
                            $scope.groupStatisticsMap[meta['group-id']] = meta;
                        });
                    }
                ),
                $http.get(vgroupsqueryString).success(
                    function (res) {
                        $scope.vgroupQueryResult = res;

                        if (res.groups) {
                            $scope.vgroups = res.groups;
                        } else {
                            $scope.vgroups = [];
                        }
                    }
                )
            ];
        } else {
            if (gs.indexOf('v-group') > -1) {
                // v-group
                $scope.showHealthAndCountSummarize = false;
                queryLogics = [
                    $http.get(vgroupsqueryString).success(
                        function (res) {
                            if (res.groups) {
                                $scope.vgroups = res.groups;
                            } else {
                                $scope.vgroups = [];
                            }
                        }
                    ),
                    $http.get(G.baseUrl + "/api/apps").success(
                        function (response) {
                            $scope.appInfoMap = {};
                            $.each(response['apps'], function (i, val) {
                                $scope.appInfoMap[val['app-id']] = val;
                            });
                        }
                    ),
                ];
            }
            if (gs.indexOf('group') > -1) {
                // Group
                $scope.showHealthAndCountSummarize = true;
                queryLogics = [
                    $http.get(groupsqueryString).success(
                        function (res) {
                            if (res.groups) {
                                $scope.groups = res.groups;
                            } else {
                                $scope.groups = [];
                            }
                        }
                    ),

                    $http.get(G.baseUrl + "/api/apps").success(
                        function (response) {
                            $scope.appInfoMap = {};
                            $.each(response['apps'], function (i, val) {
                                $scope.appInfoMap[val['app-id']] = val;
                            });
                        }
                    ),
                    $http.get(G.baseUrl + "/api/statistics/groups").success(
                        function (response) {
                            $scope.groupStatisticsMap = {};
                            $.each(response["group-metas"], function (index, meta) {
                                $scope.groupStatisticsMap[meta['group-id']] = meta;
                            });
                        }
                    )
                ];
            }
        }

        // send the request
        $q.all(queryLogics).then(
            function () {
                if ($scope.groupQueryResult && $scope.groupQueryResult.code) {
                    exceptionNotify("出错了!!", "加载Groups 失败了， 失败原因" + $scope.groupQueryResult.message, null);
                    return;
                } else {
                    setTimeout(
                        function () {
                            $('.alert-danger').remove();
                        },
                        1000
                    );
                }
                if ($scope.vgroupQueryResult && $scope.vgroupQueryResult.code) {
                    exceptionNotify("出错了!!", "加载VGroups 失败了， 失败原因" + $scope.vgroupQueryResult.message, null);
                    return;
                } else {
                    setTimeout(
                        function () {
                            $('.alert-danger').remove();
                        },
                        1000
                    );
                }
                $scope.groupsCount += $scope.groups.length;
                $scope.groupsCount += ($scope.vgroups == undefined ? 0 : $scope.vgroups.length);

                $.each($scope.groups, function (i, group) {
                    $scope.getGroupMetaData(group);

                    $scope.getAppInfoMetaData(group);
                    $scope.getGroupQPSMetaData(group);
                    group.paths = [];
                    group.pathOrder = 0;
                    var c = 0;
                    $.each(group['group-virtual-servers'], function (i, gVs) {
                        var o = {
                            name: gVs.name,
                            path: gVs.path,
                            priority: gVs.priority,
                            vsId: gVs['virtual-server'].id,
                            slbId: gVs['virtual-server']["slb-ids"],
                            ssl: gVs['virtual-server'].ssl,
                            domain: gVs['virtual-server'].domains && gVs['virtual-server'].domains[0] != undefined ?
                                gVs['virtual-server'].domains[0].name : ""
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

                    var e = _.find(group.properties, function (r) {
                        return r.name == 'idc';
                    });
                    if (e && e.value) group.idc = e.value;
                    else group.idc = '-';
                });
                $scope.allGroups = $scope.allGroups.concat($scope.groups);

                // get v-groups
                $.each($scope.vgroups, function (i, group) {
                    $scope.getGroupMetaData(group);
                    $scope.getAppInfoMetaData(group);
                    group.isvgroup = true;
                    group.groupstatus = 1;
                    group.paths = [];
                    group.pathOrder = 0;
                    var c = 0;
                    $.each(group['group-virtual-servers'], function (i, gVs) {
                        var o = {
                            name: gVs.name,
                            path: gVs.path,
                            priority: gVs.priority,
                            vsId: gVs['virtual-server'].id,
                            slbId: gVs['virtual-server']["slb-ids"],
                            ssl: gVs['virtual-server'].ssl,
                            domain: gVs['virtual-server'].domains && gVs['virtual-server'].domains[0] != undefined ?
                                gVs['virtual-server'].domains[0].name : ""
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
                var qps_count = _.reduce(_.pluck($scope.allGroups, 'qps'), function (left, right) {
                    return left + right;
                });
                $scope.allGroups = $scope.allGroups.concat($scope.vgroups);
                $scope.getGroupsHealthStastics($scope.allGroups);

                $('.groups-text').text($scope.groupsCount);
                $('.activate-group-text').text($scope.online);
                $('.tobeactivated-group-text').text($scope.tobeOnline);
                $('.deactivated-group-text').text($scope.offline);
                $('.healthy-group-text').text($scope.healthStatsticData.healthy);
                $('.unhealthy-group-text').text($scope.healthStatsticData.unhealthy);
                $('.broken-group-text').text($scope.healthStatsticData.broken);
                $('.server-text').text(T.getText($scope.sCount));
                $('.member-text').text(T.getText($scope.mCount));
                $('.qps-text').text(T.getText(qps_count));

                $scope.reloadTable();
            }
        );
    };
    $scope.reloadTable = function () {
        var p1 = A.canDo("Group", "ACTIVATE", "*");
        var p2 = A.canDo("Group", "UPDATE", "*");
        if (!p1 || !p2) {
            $('#groups-data-table').bootstrapTable('hideColumn', 'Operation');
        }
        $('#groups-data-table').bootstrapTable("load", $scope.allGroups);
        $('#groups-data-table').bootstrapTable("hideLoading");
    };
    $scope.hashChanged = function (hashData) { $scope.resource = H.resource;
        $scope.resource = H.resource;
        $scope.allGroups = [];
        $scope.tobeOnline = 0;
        $scope.online = 0;
        $scope.offline = 0;
        $scope.mCount = 0;
        $scope.sCount = 0;
        $scope.groupsCount = 0;
        $scope.members = {};
        // reset global data
        $scope.initTable();
        $('#groups-data-table').bootstrapTable("removeAll");
        $('#groups-data-table').bootstrapTable("showLoading");
        // get current login user
        var userRequest = {
            method: 'GET',
            url: '/api/auth/current/user'
        };
        $http(userRequest).success(
            function (response, code) {
                currentUser = response.name;
                $scope.loadData(hashData);
            }
        );
        $scope.tableOps.groups.showMoreColumns = false;
        $scope.tableOps.groups.showOperations = true;
    };

    H.addListener("groupsResultApp", $scope, $scope.hashChanged);
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
});
angular.bootstrap(document.getElementById("groups-result-area"), ['groupsResultApp']);

var groupsSummaryApp = angular.module('groupsSummaryApp', ['http-auth-interceptor']);
groupsSummaryApp.controller('groupsSummaryController', function ($scope, $http, $q) {
    $scope.hashData = {};
    $scope.navigateTo = function (item) {
        var hashData = $scope.hashData;
        hashData.timeStamp = new Date().getTime();
        hashData.groupType = 'Group';

        switch (item) {
            case 'activated': {
                hashData = $scope.generateStatusText(hashData, '已激活');
                break;
            }
            case 'tobeactivated': {
                hashData = $scope.generateStatusText(hashData, '有变更');
                break;
            }
                ;
            case 'deactivated': {
                hashData = $scope.generateStatusText(hashData, '未激活');
                break;
            }
            case 'healthy': {
                hashData = $scope.generateHealthyText(hashData, 'healthy');
                break;
            }
            case 'unhealthy': {
                hashData = $scope.generateHealthyText(hashData, 'unhealthy');
                break;
            }
            case 'broken': {
                hashData = $scope.generateHealthyText(hashData, 'Broken');
                break;
            }

            case 'apps':
                break;
            case 'groups':
                break;
            default:
                break;
        }
        H.setData(hashData);
    };
    $scope.generateStatusText = function (hashData, text) {
        hashData.groupStatus = text;
        hashData.groupType = "Group,V-Group";
        return hashData;
    };
    $scope.generateHealthyText = function (hashData, text) {
        hashData.groupHealthy = 'healthy:' + text;
        hashData.groupType = 'Group,V-Group';

        return hashData;
    };
    $scope.hashChanged = function (hashData) { $scope.resource = H.resource;
        $scope.resource = H.resource;

        $scope.hashData = hashData;
    };

    H.addListener("slbsSummaryApp", $scope, $scope.hashChanged);
});
angular.bootstrap(document.getElementById("apps-summary-area"), ['groupsSummaryApp']);