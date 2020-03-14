var slbsQueryApp = angular.module('slbsQueryApp', ["angucomplete-alt", "http-auth-interceptor"]);
slbsQueryApp.controller('slbsQueryController', function ($scope, $http, $rootScope) {
    $scope.query = {
        "idcs": {},
        "zones": {},
        "domains": {},
        "tags": {},
        "statuses": {},
        "slbId": '',
        "slbName": '',
        "hostIp": '',
        "vip": ''
    };
    $scope.data = {
        'tagArr': [],
        "idcs": [],
        "zones": [],
        "statuses": []
    };

    //Load cache
    $scope.cacheRequestFn = function (str) {
        return {q: str, timestamp: new Date().getTime()};
    };
    $scope.remoteSLBsUrl = function () {
        return G.baseUrl + "/api/meta/slbs";
    };
    $scope.remoteVipsUrl = function () {
        return G.baseUrl + "/api/meta/vips";
    };
    $scope.remoteSlbServersUrl = function () {
        return G.baseUrl + "/api/meta/slb-servers";
    };
    $scope.remoteDomainsUrl = function () {
        return G.baseUrl + "/api/meta/domains";
    };

    //Load all select data.
    $scope.dataLoaded = false;
    $scope.env = '';
    $scope.loadData = function (hashData) {
        if ($scope.env == hashData.env && $scope.dataLoaded) return;
        $scope.dataLoaded = true;
        $scope.env = hashData.env;
        $scope.data = {
            'tagArr': [],
            "idcs": [],
            "zones": [],
            "statuses": []
        };

        //Load tags
        $http.get(G.baseUrl + "/api/tags?type=slb").success(function (res) {
            $scope.data.tagArr = _.map(res['tags'], function (val) {
                return {name: val};
            });
        });

        //Load idcs,zones
        $http.get(G.baseUrl + "/api/properties?type=slb").success(function (res) {
            $scope.data.idcs = [];
            $scope.data.zones = [];
            $.each(res.properties, function (i, property) {
                if (property.name == "idc") {
                    // todo: loop temp
                    if (property.value != "日阪") {
                        $scope.data.idcs.push({"idc": property.value});
                    }
                }
                else if (property.name == "zone") $scope.data.zones.push({"zone": property.value});
            });
        });

        //Set status
        $scope.data.statuses = ["已激活", "有变更", "未激活"];
    };


    $scope.getStatusLanguage = function (x) {
        var resource = $scope.resource;

        if (resource && _.keys(resource).length > 0) {
            return resource['slbs']['slbs_slbsQueryApp_Statuses'][x];
        }
    };
    $scope.getZoneLanguage = function (x) {
        var resource = $scope.resource;

        if (resource && _.keys(resource).length > 0) {
            return resource['slbs']['slbs_slbsQueryApp_Zones'][x] || x;
        }
    };
    //Execute query after click query button.
    $scope.executeQuery = function () {
        var hashData = {};

        hashData.idcs = _.values($scope.query.idcs);
        if (hashData.idcs.indexOf('SHARB') != -1) {
            hashData.idcs.push('日阪');
        }


        hashData.zones = _.values($scope.query.zones);
        hashData.domains = _.values($scope.query.domains);
        hashData.tags = _.values($scope.query.tags);
        hashData.statuses = _.values($scope.query.statuses);
        hashData.slbId = $scope.query.slbId || '';
        hashData.slbName = $scope.query.slbName || '';
        hashData.hostIp = $scope.query.hostIp || '';
        hashData.vip = $scope.query.vip || '';
        hashData.pci = _.values($scope.query.pci);
        hashData.timeStamp = new Date().getTime();
        H.setData(hashData);
    };


    //Reset query data after click clear button.
    $scope.clearQuery = function () {
        $scope.query.idcs = {};
        $scope.query.zones = {};
        $scope.query.domains = {};
        $scope.query.tags = {};
        $scope.query.statuses = {};
        $scope.query.slbId = '';
        $scope.query.slbName = '';
        $scope.query.hostIp = '';
        $scope.query.vip = '';
        $scope.setInputsDisplay();
    };

    //Clear selected idcs, zones, tags or statuses.
    $scope.idcClear = function () {
        $scope.query.idcs = {};
    };
    $scope.zoneClear = function () {
        $scope.query.zones = {};
    };
    $scope.tagClear = function () {
        $scope.query.tags = {};
    };
    $scope.statusClear = function () {
        $scope.query.statuses = {};
    };
    $scope.pciClear = function () {
        $scope.query.pci = {};
    };
    $scope.showClear = function (type) {
        if (type == "idc")
            return _.keys($scope.query.idcs).length > 0;
        if (type == "zone")
            return _.keys($scope.query.zones).length > 0;
        if (type == "tag")
            return _.keys($scope.query.tags).length > 0;
        if (type == "status")
            return _.keys($scope.query.statuses).length > 0;
        if (type == "pci")
            return _.keys($scope.query.pci).length > 0;
    };

    // Property Select && Unselect
    $scope.toggleIDC = function (idc) {
        if ($scope.query.idcs[idc]) {
            delete $scope.query.idcs[idc];
        } else {
            $scope.query.idcs[idc] = idc;
        }
    };
    $scope.isSelectedIDC = function (idc) {
        if ($scope.query.idcs[idc]) {
            return "label-info";
        }
    };

    $scope.toggleZone = function (zone) {
        if ($scope.query.zones[zone]) {
            delete $scope.query.zones[zone];
        } else {
            $scope.query.zones[zone] = zone;
        }
    };
    $scope.isSelectedZone = function (zone) {
        if ($scope.query.zones[zone]) {
            return "label-info";
        }
    };

    $scope.toggleTag = function (tag) {
        if ($scope.query.tags[tag]) {
            delete $scope.query.tags[tag];
        } else {
            $scope.query.tags[tag] = tag;
        }
    };
    $scope.isSelectedTag = function (tag) {
        if ($scope.query.tags[tag]) {
            return "label-info";
        }
    };

    $scope.toggleStatus = function (status) {
        if ($scope.query.statuses[status]) {
            delete $scope.query.statuses[status];
        } else {
            $scope.query.statuses[status] = status;
        }
    };
    $scope.isSelectedStatus = function (status) {
        if ($scope.query.statuses[status]) {
            return "label-info";
        }
    };

    $scope.isSelectedPCI = function (pci) {
        if ($scope.query.pci[pci]) {
            return "label-info";
        }
    };
    $scope.togglePCI = function (pci) {
        $scope.query.pci = {};
        $scope.query.pci[pci] = pci;
    };

    // Input changed event
    $scope.slbIdInputChanged = function (o) {
        $scope.query.slbId = o;
    };
    $scope.vipInputChanged = function (o) {
        $scope.query.vip = o;
    };
    $scope.slbServerInputChanged = function (o) {
        $scope.query.hostIp = o;
    };

    // Select input field
    $scope.selectSlbId = function (o) {
        if (o) {
            $scope.query.slbId = o.originalObject.id;
        }
    };
    $scope.selectVIP = function (o) {
        if (o) {
            $scope.query.vip = o.originalObject.name;
        }
    };
    $scope.selectHostIp = function (o) {
        if (o) {
            $scope.query.hostIp = o.originalObject.id;
        }
    };
    $scope.addDomain = function (domain) {
        if (domain) {
            $scope.query.domains[domain.originalObject.name] = domain.originalObject.name;
        }
    };
    $scope.removeDomain = function (domain) {
        delete $scope.query.domains[domain];
    };
    $scope.addTag = function (tag) {
        if (tag) {
            $scope.query.tags[tag.originalObject.name] = tag.originalObject.name;
        }
    };
    $scope.removeTag = function (tag) {
        delete $scope.query.tags[tag];
    };

    //Initial query value by hashData
    $scope.initQueryData = function (hashData) {
        $scope.query.idcs = {};
        if (hashData.idcs) {
            $.each(hashData.idcs.split(","), function (i, val) {
                $scope.query.idcs[val] = val;
            });
        }

        $scope.query.zones = {};
        if (hashData.zones) {
            $.each(hashData.zones.split(","), function (i, val) {
                $scope.query.zones[val] = val;
            })
        }

        $scope.query.statuses = {};
        if (hashData.statuses) {
            $.each(hashData.statuses.split(","), function (i, val) {
                $scope.query.statuses[val] = val;
            })
        }

        $scope.query.domains = {};
        if (hashData.domains) {
            $.each(hashData.domains.split(","), function (i, val) {
                $scope.query.domains[val] = val;
            })
        }

        $scope.query.tags = {};
        if (hashData.tags) {
            $.each(hashData.tags.split(","), function (i, val) {
                $scope.query.tags[val] = val;
            })
        }

        $scope.query.pci = {};
        if (hashData.pci) {
            $.each(hashData.pci.split(","), function (i, val) {
                $scope.query.pci[val] = val;
            })
        }

        $scope.query.slbId = hashData.slbId || '';
        $scope.query.slbName = hashData.slbName || '';
        $scope.query.vip = hashData.vip || '';
        $scope.query.hostIp = hashData.hostIp || '';
    };

    //Init input field while hashChanged
    $scope.setInputsDisplay = function () {
        $('#slbIdSelector_value').val($scope.query.slbId);
        $('#vipSelector_value').val($scope.query.vip);
        $('#hostIpSelector_value').val($scope.query.hostIp);
    };

    //HashChanged
    $scope.hashChanged = function (hashData) {
        $scope.resource = H.resource;
        $scope.resource = H.resource;
        $scope.loadData(hashData);
        $scope.initQueryData(hashData);
        $scope.setInputsDisplay();
    };
    H.addListener("slbsQueryApp", $scope, $scope.hashChanged);
});
angular.bootstrap(document.getElementById("slbs-query-area"), ['slbsQueryApp']);

var slbsResultApp = angular.module('slbsResultApp', ['http-auth-interceptor']);
slbsResultApp.controller('slbsResultController', function ($scope, $http, $q) {
    var slbsApp;

    $scope.query = {
        "idcs": "",
        "zones": "",
        "domains": "",
        "tags": "",
        "statuses": "",
        "slbId": "",
        "slbName": "",
        "hostIp": "",
        "vip": ""
    };
    $scope.tableOps = {
        slbs: {
            showMoreColumns: false,
            showOperations: false
        }
    };

    $scope.slbs = [];
    $scope.summaryInfo = {};

    var statusMapping = {
        'tobeactivated': '有变更',
        'activated': '已激活',
        'deactivated': '未激活'
    };
    // Wrench tool bar
    $scope.disableOpenSlbs = function () {
        var can = A.canDo('Slb', 'UPDATE', '*');
        return !can;
    };
    $scope.getSlbsShowMore = function () {
        return $scope.tableOps.slbs.showMoreColumns ? 'fa fa-list-alt text-info' : 'fa fa-list-alt font-gray';
    };
    $scope.getSlbsShowOperation = function () {
        return $scope.tableOps.slbs.showOperations ? 'fa fa-wrench text-info' : 'fa fa-wrench font-gray';
    };
    $scope.toggleShowMoreSlbsColumns = function () {
        $scope.tableOps.slbs.showMoreColumns = !$scope.tableOps.slbs.showMoreColumns;
        if ($scope.tableOps.slbs.showMoreColumns) {

        } else {

        }
    };
    $scope.toggleShowSlbsOperations = function () {
        $scope.tableOps.slbs.showOperations = !$scope.tableOps.slbs.showOperations;
    };
    $scope.getSlbsOperationTitle = function () {
        return $scope.tableOps.slbs.showOperations ? '关闭操作' : '打开操作';
    };
    $scope.getSlbsShowMoreTitle = function () {
        return $scope.tableOps.slbs.showOperations ? '显示简略信息' : '显示详细信息';
    };

    $scope.getStatusEnglishName = function (v) {
        var resource = $scope.resource;
        if (!resource || _.keys(resource).length == 0) return;

        return resource['slbs']['slbs_slbsQueryApp_Statuses'][v] || v;
    };
    // Rights
    $scope.showNewSlbBtn = function () {
        return A.canDo('Slb', 'UPDATE', '*') && $scope.tableOps.slbs.showOperations;
    };
    $scope.showAddSlbTagBtn = function () {
        return A.canDo('Slb', 'PROPERTY', '*') && $scope.tableOps.slbs.showOperations;
    };

    // Table init
    $scope.initTable = function () {
        var resource = $scope.resource;
        if (!resource || _.keys(resource).length == 0) return;

        var idTitle = resource['slbs']['slbs_slbsResultApp_ID'];
        var nameTitle = resource['slbs']['slbs_slbsResultApp_Name'];
        var vipsTitle = resource['slbs']['slbs_slbsResultApp_Vips'];
        var idcTitle = resource['slbs']['slbs_slbsResultApp_IDC'];
        var hostsTitle = resource['slbs']['slbs_slbsResultApp_Hosts'];
        var vsTitle = resource['slbs']['slbs_slbsResultApp_VS'];
        var appsTitle = resource['slbs']['slbs_slbsResultApp_Apps'];
        var groupsTitle = resource['slbs']['slbs_slbsResultApp_Groups'];
        var membersTitle = resource['slbs']['slbs_slbsResultApp_Members'];
        var serversTitle = resource['slbs']['slbs_slbsResultApp_Servers'];
        var qpsTitle = resource['slbs']['slbs_slbsResultApp_QPS'];
        var statusTitle = resource['slbs']['slbs_slbsResultApp_Status'];
        var loadingTitle = resource['slbs']['slbs_slbsResultApp_LoadingSlbs'];
        var noSlbsTitle = resource['slbs']['slbs_slbsResultApp_NoSlbs'];
        var statusMap = resource['slbs']['slbs_slbsResultApp_StatusMap'];

        $('#slbList-table').bootstrapTable({
            toolbar: "#slbList-table-toolbar",
            columns: [[
                {
                    field: 'state',
                    checkbox: 'true',
                    align: 'center',
                    valign: 'middle'
                },
                {
                    field: 'id',
                    title: idTitle,
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    formatter: function (value, row, index) {
                        return '<a title="' + value + '"href="/portal/slb#?env=' + G.env + '&slbId=' + row.id + '">' + value + '</a>';
                    }
                },
                {
                    field: 'name',
                    title: nameTitle,
                    align: 'left',
                    valign: 'middle',
                    width: '200px',
                    sortable: true,
                    formatter: function (value, row, index) {
                        return '<a title="' + value + '"href="/portal/slb#?env=' + G.env + '&slbId=' + row.id + '"><span style="word-break: break-all">' + value + '</span></a>';
                    }
                },
                {
                    field: 'vips',
                    title: vipsTitle,
                    align: 'left',
                    valign: 'middle',
                    formatter: function (value, row, index) {
                        var result = "";
                        $.each(value, function (i, v) {
                            result += '<a target="_blank" href="' + G[G.env].urls.webinfo + '?Keyword=' + value[i].ip + '">' + value[i].ip + '</a><br>';
                        });
                        return result;
                    }
                },
                {
                    field: 'idc',
                    title: idcTitle,
                    align: 'left',
                    valign: 'middle',
                    sortable: true
                },
                {
                    title: hostsTitle,
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    sortName: 'slbServersCount',
                    formatter: function (value, row, index) {
                        return row['slb-servers'].length;
                    }
                },
                {
                    field: 'slb-servers',
                    title: 'HostsInfo',
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    sortName: 'slbServersCount',
                    formatter: function (value, row, index) {
                        var result = '';
                        $.each(value, function (i, v) {
                            var ipStr = '<a target="_blank" href="' + G[G.env].urls.cat + '/cat/r/h?domain=100000716&ip=' + value[i].ip + '">' + value[i].ip + '</a>';
                            var qpsStr = '';
                            if (value[i].qps) {
                                qpsStr = '<span class="status-gray">&nbsp;(<a target="_blank" class="status-gray slbServer-qps" href="/portal/slb/traffic' + H.generateHashStr({
                                    env: G.env,
                                    slbId: row.id
                                }) + '">QPS:&nbsp;' + Math.floor(value[i].qps) + '</a>)</span>';
                            } else {
                                qpsStr = '<span class="status-gray">&nbsp;(-)</span>';
                            }
                            result += '<div class="row" style="margin:0; width: 175px">' +
                                '<div class="col-md-6" style="padding:0;">' + ipStr + '</div>' +
                                '<div class="col-md-6" style="padding-left:3px;">' + qpsStr + '</div>' +
                                '</div>';
                        });
                        result = '<div class="row" style="margin:0">' + result + '</div>';
                        return result;
                    }
                },
                {
                    field: 'vsCount',
                    title: vsTitle,
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    formatter: function (value, row, index) {
                        if (!value)
                            return '-';
                        return '<a href="' + '/portal/vses#?env=' + G.env + '&slbId=' + row.id + '">' + value + '</a>';
                    }
                },
                {
                    field: 'appCount',
                    title: appsTitle,
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    formatter: function (value, row, index) {
                        if (!value)
                            return '-';
                        return '<a href="' + '/portal/apps#?env=' + G.env + '&slbId=' + row.id + '">' + value + '</a>';
                    }
                },
                {
                    field: 'groupCount',
                    title: groupsTitle,
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    formatter: function (value, row, index) {
                        if (!value)
                            return '-';
                        return '<a href="' + '/portal/groups#?env=' + G.env + '&slbId=' + row.id + '">' + value + '</a>';
                    }
                },
                {
                    field: 'memberCount',
                    title: membersTitle,
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    formatter: function (value, row, index) {
                        if (!value)
                            return '-';
                        return value;
                    }
                },
                {
                    field: 'serverCount',
                    title: serversTitle,
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    formatter: function (value, row, index) {
                        if (!value)
                            return '-';
                        return value;
                    }
                },
                {
                    field: 'qps',
                    title: qpsTitle,
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    formatter: function (v, row, index) {
                        return v ? '<a target="_blank" href="/portal/slb/traffic' + H.generateHashStr({
                            env: G.env,
                            slbId: row.id
                        }) + '">' + Math.floor(v) + '</a>' : '-';
                    }
                },
                {
                    field: 'status',
                    title: statusTitle,
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    formatter: function (value, row, index) {
                        var vsStatusCss = "";
                        if (!value)
                            return "-";
                        else {
                            switch (statusMapping[value.toLowerCase()]) {
                                case "已激活":
                                    vsStatusCss = 'status-green';
                                    break;
                                case "未激活":
                                    vsStatusCss = 'status-gray';
                                    break;
                                case "有变更":
                                    vsStatusCss = 'status-yellow';
                                    break;
                                default :
                                    vsStatusCss = "status-gray";
                            }
                        }

                        if (value == '有变更')
                            return '<span class="activeVS status-yellow">' + statusMap['tobeactivated'] + '</span>';
                        else
                            return '<span class="' + vsStatusCss + '" ">' + statusMap[value.toLowerCase()] + '</span>';
                    }
                }/*,
                {
                    title: 'Links',
                    align: 'left',
                    valign: 'middle',
                    width: '50px',
                    formatter: function (value, row, index) {
                        var slbId = row.id;
                        var query = 'slb_id:'+slbId;
                        var es = slbsApp.getEsHtml(query);
                        var str = '<div class="">' +es+'</div>'

                        return str;
                    }
                }*/
            ], []],
            sortName: 'qps',
            sortOrder: 'desc',
            data: $scope.slbs,
            classes: "table-bordered  table-hover table-striped table",
            search: true,
            showRefresh: true,
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
                return "<img class='loading-img' src='/static/img/loading_100.gif' /> " + loadingTitle;
            },
            formatNoMatches: function () {
                return '<i class="fa status-alert fa-exclamation-triangle" aria-hidden="true"></i> ' + noSlbsTitle;
            }
        });
    };

    // Table top summary
    $scope.toggleCountSearch = function (status, value) {
        if (value != 0) {
            var hashData = {};
            hashData.env = G.env;
            hashData.statuses = status;
            hashData.timeStamp = new Date().getTime();
            H.setData(hashData);
        }
    };
    $scope.countSearchClass = function (value) {
        var v = value == 0 ? 'link-comment' : '';
        return v;
    };

    // Table ops
    $scope.createSLBTagClick = function () {
        window.open("/portal/slb/new#?env=" + G.env);
    };
    $('#slbList-table').on('check.bs.table uncheck.bs.table check-all.bs.table uncheck-all.bs.table', function () {
        $('#createTag').prop('disabled', !$('#slbList-table').bootstrapTable('getSelections').length);
    });
    $scope.getIdSelections = function () {
        return $.map($('#slbList-table').bootstrapTable('getSelections'), function (row) {
            return row.id;
        });
    };
    $scope.createSlbTag = function (tagName) {
        var slbIds = $scope.getIdSelections();
        var targetIdQueryString = "";
        $.each(slbIds, function (i, val) {
            if (i == slbIds.length - 1)
                targetIdQueryString += "targetId=" + val;
            else {
                targetIdQueryString += "targetId=" + val + "&";
            }
        });

        slbsApp.sendRequest({
            method: 'GET',
            url: G.baseUrl + "/api/tagging?tagName=" + tagName + "&type=slb&" + targetIdQueryString
        }, function (res, code) {
            if (code == 200 || code == 202) {
                $scope.createSLBTagResponseResult = "添加成功";
                setTimeout(function () {
                    $('#createSLBTagResult').modal('hide');
                    $('#createSLBTagDialog').modal('hide');
                }, 500);
            } else {
                $scope.createSLBTagResponseResult = "添加失败";
                $scope.createSLBTagResponseInfo = res.data.message;
            }
        });
    };

    // Query load data
    $scope.loaded = false;
    $scope.bindData = function (data) {
        $scope.slbs = slbsApp.getTableData(data['slbs'], data['slb-stastics']);

        var count = slbsApp.getStasticSummaryData(data['slb-stastics']);

        var status = slbsApp.getStatusSummaryData($scope.slbs);

        var slbServersArray = _.pluck($scope.slbs, 'slbServersCount');

        var summaryInfo = {
            totalSlbCount: $scope.slbs.length || 0,
            totalSlbServerCount: _.reduce(slbServersArray, function (a, b) {
                return a + b;
            }, 0),
            totalVSCount: count['vs-count'],
            totalAppCount: count['app-count'],
            totalGroupCount: count['group-count'],
            totalMemberCount: count['member-count'],
            totalServerCount: count['group-server-count'],
            qps: count['qps'],
            status: [
                {
                    'name': '有变更',
                    'count': status['tobeactivated'] || 0
                },
                {
                    'name': '已激活',
                    'count': status['activated'] || 0
                },
                {
                    'name': '未激活',
                    'count': status['deactivated'] || 0
                }
            ]
        };

        // if hashchange is not cause by slbs summary
        $('.slbs-text').text(summaryInfo.totalSlbCount);
        $('.activate-slb-text').text(status['activated'] || 0);
        $('.tobeactivated-slb-text').text(status['tobeactivated'] || 0);
        $('.deactivated-slb-text').text(status['deactivated'] || 0);
        $('.vs-text').text(summaryInfo.totalVSCount);
        $('.app-text').text(summaryInfo.totalAppCount);
        $('.groups-text').text(summaryInfo.totalGroupCount);
        $('.member-text').text(T.getText(summaryInfo.totalMemberCount));
        $('.server-text').text(T.getText(summaryInfo.totalServerCount));
        $('.qps-text').text(T.getText(count['qps']));

        $scope.summaryInfo = summaryInfo;
    };
    // Hash changed
    $scope.hashChanged = function (hashData) {
        $scope.resource = H.resource;
        $('#slbList-table').bootstrapTable('removeAll');
        $('#slbList-table').bootstrapTable("showLoading");

        // HashData apply
        $scope.env = 'pro';
        if (hashData.env && hashData.env != $scope.env) {
            $scope.env = hashData.env;
            $scope.loaded = false;
        }
        if (hashData.statuses) {
            if (!Array.isArray(hashData.statuses)) {
                hashData.statuses = [hashData.statuses];
            }
            hashData.statuses = _.map(hashData.statuses, function (t) {
                if (t == '已激活') return 'activated';
                if (t == '未激活') return 'deactivated';
                if (t == '有变更') return 'toBeActivated';
                return '-';
            });
        }

        // App initialize
        slbsApp = new SlbsApp(hashData, $http, $q, $scope.env);

        // Get query string
        var query = slbsApp.getQueries();

        // Send Request to the backend
        var out = slbsApp.request(query);
        var promise = out.request;

        promise.then(
            function () {
                $scope.bindData(out.result);
                $('#slbList-table').bootstrapTable("load", $scope.slbs ? $scope.slbs : []);
                $('#slbList-table').bootstrapTable("hideLoading");
            }
        );
        $scope.resource = H.resource;
        // Load table datas
        $scope.initTable();
        // Others
        $('#slbList-table').bootstrapTable('hideColumn', 'slb-servers');
        var p1 = A.canDo("Slb", "ACTIVATE", "*");
        var p2 = A.canDo("Slb", "UPDATE", "*");
        if ((typeof(p1) == 'undefined' || !p1) && (typeof(p2) == 'undefined' || !p2)) {
            $('#slbList-table').bootstrapTable('hideColumn', 'Operation');
        }
        $scope.tableOps.slbs.showMoreColumns = false;
        $scope.tableOps.slbs.showOperations = true;

    };
    H.addListener("slbsResultApp", $scope, $scope.hashChanged);
});
angular.bootstrap(document.getElementById("slbs-result-area"), ['slbsResultApp']);

var slbsSummaryApp = angular.module('slbsSummaryApp', ['http-auth-interceptor']);
slbsSummaryApp.controller('slbsSummaryController', function ($scope, $http, $q) {
    $scope.hashData = {};
    $scope.navigateTo = function (item) {
        var hashData = $scope.hashData;
        hashData.timeStamp = new Date().getTime();

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
            case 'domain':
                break;
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
        hashData.statuses = text;
        return hashData;
    };
    $scope.hashChanged = function (hashData) {
        $scope.resource = H.resource;
        $scope.hashData = hashData;
        $scope.resource = H.resource;
    };

    H.addListener("slbsSummaryApp", $scope, $scope.hashChanged);
});
angular.bootstrap(document.getElementById("apps-summary-area"), ['slbsSummaryApp']);