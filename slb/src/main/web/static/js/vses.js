//VsesQuery Component: VsesQueryController
var vsesQueryApp = angular.module('vsesQueryApp', ["angucomplete-alt", "http-auth-interceptor"]);
vsesQueryApp.controller('vsesQueryController', function ($scope, $http) {
    $scope.query = {
        "tags": {},
        "statuses": {},
        "ssl": {},
        "zones": {},
        "domains": {},
        "slbId": "",
        "vsId": "",
        "vsName": ""
    };

    $scope.data = {
        "statuses": [],
        "ssl": [],
        "zones": [],
        'vsArr': [],
        'domainArr': [],
        'tagArr': []
    };

    //Load cache
    $scope.cacheRequestFn = function (str) {
        return {q: str, timestamp: new Date().getTime()};
    };

    $scope.remoteVsesUrl = function () {
        return G.baseUrl + "/api/meta/vses";
    };

    $scope.remoteSLBsUrl = function () {
        return G.baseUrl + "/api/meta/slbs";
    };

    $scope.remoteDomainsUrl = function () {
        return G.baseUrl + "/api/meta/domains";
    };

    //Load all select datas
    $scope.dataLoaded = false;
    $scope.env = '';
    $scope.loadData = function (hashData) {
        if ($scope.env == hashData.env && $scope.dataLoaded) return;
        $scope.dataLoaded = true;
        $scope.env = hashData.env;
        $scope.data = {
            "statuses": [],
            "ssl": [],
            'vsArr': [],
            'domainArr': [],
            'tagArr': []
        };

        //Load vs ids,names
        $http.get(G.baseUrl + "/api/vses").success(function (res) {
            var vsArr = [];
            _.each(res['virtual-servers'], function (val) {
                vsArr.push({
                    id: val.id,
                    name: val.name
                });
            });
            $scope.data.vsArr = vsArr;
        });

        //Load slb ids, names
        $http.get(G.baseUrl + "/api/slbs?type=info").success(function (res) {
            var slbArr = [];
            _.each(res['slbs'], function (val) {
                slbArr.push({
                    id: val.id,
                    name: val.name
                });
            });
            $scope.data.slbArr = slbArr;
        });

        //Load domains
        $http.get(G.baseUrl + "/api/vses").success(function (res) {
            var domains = {};
            _.each(res['virtual-servers'], function (val) {
                _.each(val.domains, function (val) {
                    domains[val.name] = 1;
                });
            });
            $scope.data.domainArr = _.map(_.keys(domains), function (val) {
                return {name: val};
            });
        });

        //Load tags
        $http.get(G.baseUrl + "/api/tags?type=vs").success(function (res) {
            $scope.data.tagArr = _.map(res['tags'], function (val) {
                return {name: val};
            });
        });

        //Set status
        $scope.data.statuses = ["已激活", "有变更", "未激活"];
        $scope.data.ssl = ["HTTP", "HTTPS"];
        $scope.data.zones = ["内网", "外网"];
    };

    $scope.getStatusLanguage = function (x) {
        var resource = $scope.resource;

        if (resource && _.keys(resource).length > 0) {
            return resource['vses']['slbs_vsesQueryApp_statuses'][x];
        }
    };

    $scope.getZoneLanguage = function (x) {
        var resource = $scope.resource;

        if (resource && _.keys(resource).length > 0) {
            return resource['vses']['slbs_vsesQueryApp_zones'][x];
        }
    };
    //Execute query after click query button.
    $scope.executeQuery = function () {
        var hashData = {};
        hashData.slbId = $scope.query.slbId || "";
        hashData.vsId = $scope.query.vsId || "";
        hashData.vsName = $scope.query.vsName || "";
        hashData.ssl = _.values($scope.query.ssl);
        hashData.domains = _.values($scope.query.domains);
        hashData.tags = _.values($scope.query.tags);
        hashData.statuses = _.values($scope.query.statuses);
        hashData.zones = _.values($scope.query.zones);
        hashData.timeStamp = new Date().getTime();
        H.setData(hashData);
    };

    //Reset query data after click clear button.
    $scope.clearQuery = function () {
        $scope.query.ssl = {};
        $scope.query.domains = {};
        $scope.query.tags = {};
        $scope.query.statuses = {};
        $scope.query.slbId = '';
        $scope.query.vsId = '';
        $scope.query.vsName = '';
        $scope.setInputsDisplay();
    };

    $scope.statusClear = function () {
        $scope.query.statuses = {};
    };
    $scope.sslClear = function () {
        $scope.query.ssl = {};
    };
    $scope.zoneClear = function () {
        $scope.query.zones = {};
    };
    $scope.showClear = function (type) {
        if (type == "status")
            return _.keys($scope.query.statuses).length > 0;
        if (type == "ssl")
            return _.keys($scope.query.ssl).length > 0;
        if (type == "zone")
            return _.keys($scope.query.zones).length > 0;
    };

    //Property Select & UnSelect
    $scope.toggleSSL = function (v) {
        if ($scope.query.ssl[v] == undefined) {
            $scope.query.ssl[v] = v;
        }
        else delete $scope.query.ssl[v];
    };
    $scope.isSelectedSSL = function (v) {
        if ($scope.query.ssl[v] == v) {
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
    $scope.toggleZones = function (zone) {
        if ($scope.query.zones[zone]) {
            delete $scope.query.zones[zone];
        } else {
            $scope.query.zones[zone] = zone;
        }
    };

    $scope.isSelectedStatus = function (status) {
        if ($scope.query.statuses[status]) {
            return "label-info";
        }
    };
    $scope.isSelectedZone = function (zone) {
        if ($scope.query.zones[zone]) {
            return "label-info";
        }
    };
    // Input changed event
    $scope.vsIdInputChanged = function (o) {
        $scope.query.vsId = o;
    };
    $scope.slbIdInputChanged = function (o) {
        $scope.query.slbId = o;
    };

    // Select input field
    $scope.selectVSId = function (o) {
        if (o) {
            $scope.query.vsId = o.originalObject.id;
        } else {
        }
    };
    $scope.selectVSName = function (o) {
        if (o) {
            $scope.query.vsName = o.originalObject.name.split(":")[0];
        }
    };
    $scope.selectSLBId = function (o) {
        if (o) {
            $scope.query.slbId = o.originalObject.id;
        }
    };
    $scope.addDomain = function (domain) {
        if (domain) {
            $scope.query.domains[domain.originalObject.name] = domain.originalObject.id;
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
        $scope.query.statuses = {};
        if (hashData.statuses) {
            $.each(hashData.statuses.split(","), function (i, val) {
                $scope.query.statuses[val] = val;
            })
        }

        $scope.query.ssl = {};
        if (hashData.ssl) {
            $.each(hashData.ssl.split(","), function (i, val) {
                $scope.query.ssl[val] = val;
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

        $scope.query.zones = {};
        if (hashData.zones) {
            $.each(hashData.zones.split(","), function (i, val) {
                $scope.query.zones[val] = val;
            })
        }

        $scope.query.slbId = hashData.slbId || '';
        $scope.query.vsId = hashData.vsId || '';
        $scope.query.vsName = hashData.vsName || '';
    };

    //Init input field while hashChanged
    $scope.setInputsDisplay = function () {
        $('#slbIdSelector_value').val($scope.query.slbId);
        $('#vsIdSelector_value').val($scope.query.vsId);
        $('#vsNameSelector_value').val($scope.query.vsName);
    };

    $scope.hashChanged = function (hashData) {
        $scope.resource = H.resource;
        $scope.resource = H.resource;

        $scope.loadData(hashData);
        $scope.initQueryData(hashData);
        $scope.setInputsDisplay();
    };

    H.addListener("slbsQueryApp", $scope, $scope.hashChanged);

});
angular.bootstrap(document.getElementById("vses-query-area"), ['vsesQueryApp']);

var vsesResultApp = angular.module('vsesResultApp', ['http-auth-interceptor']);
vsesResultApp.controller('vsesResultController', function ($scope, $http, $q) {
        $scope.query = {
            "tags": "",
            "statuses": "",
            "ssl": "",
            "domains": "",
            "slbId": "",
            "vsId": "",
            "vsName": ""
        };
        $scope.tableOps = {
            vses: {
                showMoreColumns: false,
                showOperations: false
            }
        };

        //Table DataSource
        $scope.vses = [];
        $scope.summaryInfo = {};

        $scope.getStatusEnglishName = function (v) {
            var resource = $scope.resource;
            if (!resource || _.keys(resource).length == 0) return;

            return resource['slbs']['slbs_slbsQueryApp_Statuses'][v] || v;
        };
        $scope.getStatusEnglishName = function (v) {
            var resource = $scope.resource;
            if (!resource || _.keys(resource).length == 0) return;

            return resource['vses']['slbs_vsesQueryApp_statuses'][v] || v;
        };

        // Partial show
        $scope.disableOpenVses = function () {
            var can = A.canDo('Vs', 'UPDATE', '*');
            return !can;
        };
        $scope.getVsesShowMore = function () {
            return $scope.tableOps.vses.showMoreColumns ? 'fa fa-list-alt text-info' : 'fa fa-list-alt font-gray';
        };
        $scope.getVsesShowOperation = function () {
            return $scope.tableOps.vses.showOperations ? 'fa fa-wrench text-info' : 'fa fa-wrench font-gray';
        };
        $scope.toggleShowMoreVsesColumns = function () {
            $scope.tableOps.vses.showMoreColumns = !$scope.tableOps.vses.showMoreColumns;
            if ($scope.tableOps.vses.showMoreColumns) {
                $('#table').bootstrapTable('showColumn', 'slbvips');
            } else {
                $('#table').bootstrapTable('hideColumn', 'slbvips');
            }
        };
        $scope.toggleShowVsesOperations = function () {
            $scope.tableOps.vses.showOperations = !$scope.tableOps.vses.showOperations;
        };
        $scope.getVsesOperationTitle = function () {
            return $scope.tableOps.vses.showOperations ? '关闭操作' : '打开操作';
        };
        $scope.getVsesShowMoreTitle = function () {
            return $scope.tableOps.vses.showOperations ? '显示简略信息' : '显示详细信息';
        };

        //whether to show the buttons
        $scope.showNewVsBtn = function () {
            return A.canDo('Vs', 'UPDATE', '*') && $scope.tableOps.vses.showOperations;
        };
        $scope.showAddVsTag = function () {
            return A.canDo('Vs', 'PROPERTY', '*') && $scope.tableOps.vses.showOperations;
        };

        $scope.initTable = function () {
            var resource = $scope.resource;
            if (!resource || _.keys(resource).length == 0) return;
            var t = resource['vses']['slbs_vsesResultApp_table'];
            var idtext = t['id'];
            var domaintext = t['domains'];
            var ssltext = t['ssl'];
            var idctext = t['idc'];
            var appstext = t['apps'];
            var groupstext = t['groups'];
            var memberstext = t['members'];
            var serverstext = t['servers'];
            var qpstext = t['qps'];
            var statustext = t['status'];

            var statusMap = resource['vses']['slbs_vsesResultApp_StatusMap'];

            $('#table').bootstrapTable({
                toolbar: "#toolbar",
                columns: [[{
                    field: 'state',
                    checkbox: true,
                    align: 'center',
                    valign: 'middle'
                },
                    {
                        field: 'id',
                        title: idtext,
                        align: 'left',
                        valign: 'middle',
                        sortable: true,
                        formatter: function (value, row, index) {
                            return '<a class="editVS" target="_self" title="' + value + '" href="/portal/vs#?env=' + G.env + '&vsId=' + value + '" style="text-decoration: none; margin-left: 5px; word-break: break-all">' + value + '</a>';
                        }
                    },
                    {
                        field: 'domains',
                        title: domaintext,
                        align: 'left',
                        valign: 'middle',
                        sortable: true,
                        formatter: function (value, row, index) {
                            var domains = "";
                            domains += '<div style="width: auto;">';
                            $.each(value, function (i, val) {
                                domains += '<a class="editVS" target="_self" title="' + val.name + '" href="/portal/vs#?env=' + G.env + '&vsId=' + row.id + '" style="text-decoration: none; margin-left: 5px">' + val.name + '</a><br>'
                            });
                            domains += '</div>'
                            return domains;
                        }
                    },
                    {
                        field: 'ssl',
                        title: ssltext,
                        align: 'left',
                        valign: 'middle',
                        sortable: true,
                        formatter: function (value, row, index) {
                            if (value)
                                return "Https";
                            else
                                return "Http";
                        }
                    },
                    {
                        field: 'slbvips',
                        title: 'SLB | VIPs',
                        align: 'left',
                        width: '200px',
                        valign: 'middle',
                        visible: false,
                        formatter: function (value, row, index) {
                            var result = '';
                            $.each(value, function (i, v) {
                                result += '<div class="">' +
                                    '<div class="col-md-2"><a style="color: #196eaa;font-weight:bold" target="_blank" href="' + '/portal/slb#?env=' + G.env + '&slbId=' + v.slbid + '">' + v.slbid + '</a></div>' +
                                    '<div class="col-md-10">';
                                result += '<ul style="float: left;list-style: none;">';
                                $.each(v.vips, function (j, k) {
                                    result += '<li>';
                                    result += '<a class="pull-left" target="_blank" href="' + G[G.env].urls.webinfo + '?Keyword=' + k.ip + '">' + k.ip + '</a>';
                                    result += '</li>';
                                });
                                result += '</ul>';
                                result += '</div>' +
                                    '</div>';
                            });

                            return result;
                        }
                    },
                    {
                        field: 'idc',
                        title: idctext,
                        align: 'left',
                        valign: 'middle',
                        sortable: true,
                        formatter: function (value, row, index) {
                            return value;
                        }
                    },
                    {
                        field: 'appCount',
                        title: appstext,
                        align: 'left',
                        valign: 'middle',
                        sortable: true,
                        formatter: function (value, row, index) {
                            if (!value)
                                return "-";
                            return '<a href="' + '/portal/apps#?env=' + G.env + '&vsId=' + row.id + '">' + value + '</a>';
                        }
                    },
                    {
                        field: 'groupCount',
                        title: groupstext,
                        align: 'left',
                        valign: 'middle',
                        sortable: true,
                        formatter: function (value, row, index) {
                            if (!value)
                                return "-";
                            return '<a href="' + '/portal/groups#?env=' + G.env + '&vsId=' + row.id + '">' + value + '</a>';
                        }
                    },
                    {
                        field: 'memberCount',
                        title: memberstext,
                        align: 'left',
                        valign: 'middle',
                        sortable: true,
                        formatter: function (value, row, index) {
                            if (!value)
                                return "-";
                            return '<div style="width:5px;">' + value + '</div>';
                        }
                    },
                    {
                        field: 'serverCount',
                        title: serverstext,
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
                        title: qpstext,
                        align: 'left',
                        valign: 'middle',
                        sortable: true,
                        formatter: function (value, row, index) {
                            return value ? '<a target="_blank" href="/portal/vs/traffic' + H.generateHashStr({
                                env: G.env,
                                vsId: row.id
                            }) + '">' + Math.floor(value) + '</a>' : '-';
                        }
                    },
                    {
                        field: 'vsStatus',
                        title: statustext,
                        align: 'left',
                        valign: 'middle',
                        sortable: true,
                        events: operateEvents,
                        formatter: function (value, row, index) {
                            var vsStatusCss = "";
                            if (!value)
                                return "-";
                            else {
                                switch (value) {
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
                                return '<span class="activeVS status-yellow">' + statusMap["有变更"] + '(<a data-toggle="modal" data-target="#activateVSModal">Diff</a>)</span>';
                            else
                                return '<span class="' + vsStatusCss + '" ">' + statusMap[value.toLowerCase()] + '</span>';
                        }
                    }/*,
                {
                    field: 'hickwall',
                    title: 'Links',
                    align: 'left',
                    valign: 'middle',
                    width: '25px',
                    events: operateEvents,
                    formatter: function (value, row, index) {
                        if($scope.env.toLowerCase()!='pro') return '-';
                        var domains = _.pluck(row.domains,'name');
                        domains = domains.join(' OR ');
                        var esLink = G.es;
                        var idc=row.idc.split(',')[0]||'-';

                        var mapping = {
                            金桥: 'SHAJQ',
                            欧阳: 'SHAOY',
                            南通: 'NTGXH',
                            福泉: 'SHAFQ',
                            金钟: 'SHAJZ'
                        };

                        var str = '<div class="">' +
                            '<div class="system-link">' +
                            '<a class="pull-left es" title="ES" target="_blank" href="' + esLink + '?query=domain:(' + domains + ') AND idc:'+mapping[idc]+'">ES</a>' +
                            '</div>'
                            '</div>';

                        return str;
                    }
                }*/], []],
                sortName: 'qps',
                sortOrder: 'desc',
                showRefresh: true,
                search: true,
                showColumns: true,
                data: $scope.vses,
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
                    return "<img class='loading-img' src='/static/img/loading_100.gif' /> " + t['loading'];
                },
                formatNoMatches: function () {
                    return '<i class="fa status-alert fa-exclamation-triangle" aria-hidden="true"></i> ' + t['nodata'];
                }
            });
        }
        // Summary information methods
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

        window.operateEvents = {
            'click .activeVS': function (e, value, row, index) {
                $scope.currentVSId = row.id;
                $scope.confirmActivateText = "线上版本与当前版本比较";
                if (row.vsStatus == "已激活") {
                    $scope.confirmActivateText = "线上已是最新版本,确认是否强制重新激活";
                    $('.fileViewHead').removeClass("status-red-important").addClass("status-red-important");
                    ;
                } else {
                    $('.fileViewHead').removeClass("status-red-important");
                }

                $scope.getVSData(row.id);

                setTimeout(function () {

                    var baseText = JSON.stringify(U.sortObjectFileds($scope.onlineVSData), null, "\t");
                    var newText = JSON.stringify(U.sortObjectFileds($scope.tobeActivatedVSData), null, "\t");

                    var base = difflib.stringAsLines(baseText);
                    var newtxt = difflib.stringAsLines(newText);

                    var sm = new difflib.SequenceMatcher(base, newtxt);
                    var opcodes = sm.get_opcodes();
                    var diffoutputdiv = document.getElementById("diffOutput");

                    diffoutputdiv.innerHTML = "";
                    $scope.onlineVsVersion = $scope.onlineVSData.version;
                    if (!$scope.onlineVSData.version)
                        $scope.onlineVsVersion = "无";

                    diffoutputdiv.appendChild(diffview.buildView({
                        baseTextLines: base,
                        newTextLines: newtxt,
                        opcodes: opcodes,
                        baseTextName: "线上VS版本(版本号:" + $scope.onlineVsVersion + ")",
                        newTextName: "更新后VS版本(版本号:" + $scope.tobeActivatedVSData.version + ")",
                        viewType: 0
                    }));

                }, 500);

            }
        };

        $('.confirmActivateVs').on('click', function () {
            $('#activateVSModal').modal('hide');
            $('#confirmActivateVS').modal('show');
        });
        $('.doubleConfirmActivateVs').on('click', function () {
            $('#confirmActivateVS').modal('hide');
        });

        $scope.getVSData = function (vsId) {
            $http.get(G.baseUrl + "/api/vs?vsId=" + vsId + "&mode=online").then(
                function (res) {
                    if (res.status == 200 || res.status == 202) {
                        $scope.onlineVSData = res.data;
                    } else {
                        if (res.status == 400 && res.data.message == "Virtual server cannot be found.") {
                            $scope.onlineVSData = "No online version!!!";
                        }
                    }
                }
            );

            $http.get(G.baseUrl + "/api/vs?vsId=" + vsId).success(
                function (res) {
                    $scope.tobeActivatedVSData = res;
                }
            );
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
                        if (msg.indexOf("overlap") > 0) {
                            // need force update
                            $scope.showForceUpdate = true;
                        }
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
        $scope.activateVS = function () {
            var loading = "<img src='/static/img/spinner.gif' /> 正在激活";
            $('#operationConfrimModel').modal('show').find(".modal-body").html(loading);
            var request = {
                method: 'GET',
                url: G.baseUrl + "/api/activate/vs?vsId=" + $scope.currentVSId
            };

            $scope.processRequest(request, $('#operationConfrimModel'), "激活VS", "激活VS成功,VsId:" + $scope.currentVSId);
        };

        $scope.generateDomainLink = function (domain) {
            return G[G.env].urls.webinfo + "?Keyword=" + domain;
        };

        //create tag event
        $('#table').on('check.bs.table uncheck.bs.table check-all.bs.table uncheck-all.bs.table', function () {
            $('#createTag').prop('disabled', !$('#table').bootstrapTable('getSelections').length);
        });

        function getIdSelections() {
            return $.map($('#table').bootstrapTable('getSelections'), function (row) {
                return row.id
            });
        }

        $scope.createVSTag = function (tagName) {
            var vsIds = getIdSelections();
            var targetIdQueryString = "";
            $.each(vsIds, function (i, val) {
                if (i == vsIds.length - 1)
                    targetIdQueryString += "targetId=" + val;
                else {
                    targetIdQueryString += "targetId=" + val + "&";
                }
            });

            $http.get(G.baseUrl + "/api/tagging?tagName=" + tagName + "&type=vs&" + targetIdQueryString).then(
                function (res) {
                    if (res.status == 200 || res.status == 202) {
                        $scope.createVSTagResponseResult = "添加成功";
                        setTimeout(function () {
                            $('#createVSTagResult').modal('hide');
                            $('#createVSTagDialog').modal('hide');
                        }, 500);
                    } else {
                        $scope.createVSTagResponseResult = "添加失败";
                        $scope.createVSTagResponseInfo = res.data.message;
                    }
                }
            );
        };

        //create VS event
        $('#createVS').click(function () {
            window.open("/portal/vs/new#?env=" + G.env);
        });

        //spell query string
        $scope.getHashData = function (str, seperator) {
            if (str == "" || str == undefined) {
                return [];
            }
            else return str.split(',');
        };
        $scope.spellTagsQueryString = function (queryString, tags) {
            if (queryString.endsWith('?')) {
                queryString += "anyTag=";
            }
            else {
                queryString += "&anyTag=";
            }
            $.each(tags, function (i, val) {
                queryString += val + ",";
            });
            queryString = queryString.substr(0, queryString.lastIndexOf(','));
            return queryString;
        };
        $scope.getRealpValues = function (pValues) {
            var realpValus = [];
            $.each(pValues, function (i, val) {
                switch (val) {
                    case '已激活':
                        realpValus.push('activated');
                        break;
                    case '有变更':
                        realpValus.push('toBeActivated');
                        break;
                    case '未激活':
                        realpValus.push('deactivated');
                }
            });

            return realpValus;
        };
        $scope.spellPropertyQueryString = function (queryString, pValues, pName) {
            if (pName == "status" && pValues.length > 0) {
                pValues = $scope.getRealpValues(pValues);
            }

            if (queryString.endsWith('?')) {
                queryString += "anyProp=";
            }
            else {
                queryString += "&anyProp=";
            }
            $.each(pValues, function (i, val) {
                queryString += (pName + ":" + val + ",");
            });
            queryString = queryString.substr(0, queryString.lastIndexOf(','));
            return queryString;
        };
        $scope.spellQueryString = function (queryString, key, singleValue) {
            if (queryString.endsWith('?')) {
                queryString += key + "=" + singleValue;
            }
            else {
                queryString += "&" + key + "=" + singleValue;
            }
            return queryString;
        };
        $scope.spellQueryData = function (hashData) {
            var queryString = "";

            if (hashData.slbId) {
                queryString = $scope.spellQueryString(queryString, "slbId", hashData.slbId);
            }

            if (hashData.vsId) {
                queryString = $scope.spellQueryString(queryString, "id", hashData.vsId);
            }

            if (hashData.vsName) {
                queryString = $scope.spellQueryString(queryString, "fuzzyName", hashData.vsName);
            }

            if (hashData.ssl && hashData.ssl != "HTTP,HTTPS") {
                if (hashData.ssl == "HTTP")
                    queryString += "&ssl=false";
                if (hashData.ssl == "HTTPS")
                    queryString += "&ssl=true";
            }

            if (hashData.domains) {
                queryString += "&domain=";
                $.each(hashData.domains.split(','), function (i, val) {
                    if (i == hashData.domains.split(',').length - 1)
                        queryString += val;
                    else
                        queryString += val + ",";
                });
            }
            var tags = $scope.getHashData(hashData.tags, ",");
            if (tags.length > 0) {
                queryString += $scope.spellTagsQueryString(queryString, tags);
            }

            var statuses = $scope.getHashData(hashData.statuses, ",");
            if (statuses.length > 0) {
                queryString = $scope.spellPropertyQueryString(queryString, statuses, "status");
            }

            var zones = $scope.getHashData(hashData.zones, ",");
            if (zones.length > 0) {
                queryString = $scope.spellPropertyQueryString(queryString, zones, "zone");
            }

            return G.baseUrl + "/api/vses?type=extended" + queryString;
        };

        $scope.loadData = function (queryString) {
            $scope.initTable();
            var p1 = A.canDo("Vs", "ACTIVATE", "*");
            var p2 = A.canDo("Vs", "UPDATE", "*");

            if ((typeof(p1) == 'undefined' || !p1) && (typeof(p2) == 'undefined' || !p2)) {
                $('#table').bootstrapTable('hideColumn', 'Operation');
            }

            $scope.vses = [];

            $('#table').bootstrapTable('removeAll');
            $('#table').bootstrapTable("showLoading");
            $q.all(
                [
                    $http.get(G.baseUrl + "/api/slbs").success(
                        function (res) {
                            $scope.slbVipsMap = {};
                            $.each(res.slbs, function (i, val) {
                                $scope.slbVipsMap[val.id] = val.vips;
                            })
                        }
                    ),
                    $http.get(G.baseUrl + "/api/statistics/vses").success(
                        function (res) {
                            $scope.vsStatistics = res['vs-metas'];
                            $scope.vsStatisticsMap = {};
                            $.each($scope.vsStatistics, function (i, val) {
                                $scope.vsStatisticsMap[val['vs-id']] = val;
                            });
                        }
                    ),
                    $http.get(queryString).success(function (res) {
                        $scope.queryResult = res;
                        $scope.vses = res['virtual-servers'] || [];
                    }),
                    $http.get(G.baseUrl + "/api/slbs?type=info").success(
                        function (res) {
                            $scope.slbsInfoMap = {};
                            if (res.total && res.total > 0) {
                                $.each(res.slbs, function (i, val) {
                                    $scope.slbsInfoMap[val.id] = val;
                                });
                            }
                        }
                    )
                ]
            ).then(
                function () {
                    if ($scope.queryResult.code) {
                        exceptionNotify("出错了!!", "加载VSES 失败了， 失败原因" + $scope.queryResult.message, null);
                        return;
                    } else {
                        setTimeout(
                            function () {
                                $('.alert-danger').remove();
                            },
                            1000
                        );
                    }
                    var totalVSCount = $scope.vses.length || 0, totalAppCount = 0, totalQps = 0, totalGroupCount = 0,
                        totalServerCount = 0, totalMemberCount = 0;
                    var toBeActivatedCount = 0, activatedCount = 0, deactivatedCount = 0;
                    $.each($scope.vses, function (i, val) {
                        val.slbvips = [];
                        var slbs = val['slb-ids'];
                        $.each(slbs, function (i, item) {
                            val.slbvips.push({
                                slbid: item,
                                vips: $scope.slbVipsMap[item]
                            })
                        });
                        if ($scope.vsStatisticsMap[val.id]) {
                            var newMember = $scope.vsStatisticsMap[val.id];
                            val.appCount = 0;
                            if (newMember['app-count']) {
                                val.appCount = newMember['app-count'];
                            }
                            val.groupCount = newMember['group-count'];
                            val.memberCount = newMember['member-count'];
                            val.serverCount = newMember['group-server-count'];
                            val.qps = newMember['qps'];

                            totalAppCount += val.appCount;
                            totalGroupCount += val.groupCount;
                            totalMemberCount += val.memberCount;
                            totalServerCount += val.serverCount;
                            totalQps += val.qps;
                        }


                        if (val.properties) {
                            var propertiesMap = {};
                            $.each(val.properties, function (i, val) {
                                propertiesMap[val.name] = val;
                            });

                            if (propertiesMap['status']) {
                                switch (propertiesMap['status'].value) {
                                    case "activated":
                                        val.vsStatus = "已激活";
                                        activatedCount++;
                                        break;
                                    case "deactivated":
                                        val.vsStatus = "未激活";
                                        deactivatedCount++;
                                        break;
                                    case "toBeActivated":
                                        val.vsStatus = "有变更";
                                        toBeActivatedCount++;
                                        break;
                                    default :
                                        val.vsStatus = "unKnown";
                                }
                            }
                        }

                        var e = _.find(val.properties, function (r) {
                            return r.name == 'idc';
                        });
                        if (e && e.value) val.idc = e.value;
                        else val.idc = '-';
                    });

                    $scope.summaryInfo.totalVSCount = totalVSCount;
                    $scope.summaryInfo.status = [];
                    $scope.summaryInfo.status.push(
                        {
                            'name': '有变更',
                            'count': toBeActivatedCount
                        }
                    );
                    $scope.summaryInfo.status.push(
                        {
                            'name': '已激活',
                            'count': activatedCount
                        }
                    );
                    $scope.summaryInfo.status.push(
                        {
                            'name': '未激活',
                            'count': deactivatedCount
                        }
                    );

                    $scope.summaryInfo.totalAppCount = totalAppCount;
                    $scope.summaryInfo.totalGroupCount = totalGroupCount;
                    $scope.summaryInfo.totalMemberCount = totalMemberCount;
                    $scope.summaryInfo.totalServerCount = totalServerCount;

                    $('.vses-text').text(totalVSCount);
                    $('.activate-vs-text').text(activatedCount);
                    $('.tobeactivated-vs-text').text(toBeActivatedCount);
                    $('.deactivated-vs-text').text(deactivatedCount);
                    $('.qps-text').text(T.getText(totalQps));
                    $('.app-text').text(totalAppCount);
                    $('.groups-text').text(totalGroupCount);
                    $('.member-text').text(T.getText(totalMemberCount));
                    $('.server-text').text(T.getText(totalServerCount));

                    $('#table').bootstrapTable("load", $scope.vses ? $scope.vses : []);
                    $('#table').bootstrapTable("hideLoading");
                }
            );
        };

        $scope.hashChanged = function (hashData) {
            $scope.resource = H.resource;
            $scope.resource = H.resource;
            $scope.env = G.env;
            var queryString = $scope.spellQueryData(hashData);

            $scope.loadData(queryString);
            $scope.tableOps.vses.showOperations = true;
            $scope.tableOps.vses.showMoreColumns = false;
        };
        H.addListener("vsesResultApp", $scope, $scope.hashChanged);

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
    }
);
angular.bootstrap(document.getElementById("vses-result-area"), ['vsesResultApp']);


var vsesSummaryApp = angular.module('vsesSummaryApp', ['http-auth-interceptor']);
vsesSummaryApp.controller('vsesSummaryController', function ($scope, $http, $q) {
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
        $scope.resource = H.resource;
        $scope.hashData = hashData;
    };

    H.addListener("slbsSummaryApp", $scope, $scope.hashChanged);
});
angular.bootstrap(document.getElementById("apps-summary-area"), ['vsesSummaryApp']);

