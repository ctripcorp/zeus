//Group-edit Component
var groupEditApp = angular.module('groupEditApp', ["angucomplete-alt", 'http-auth-interceptor']);
groupEditApp.controller('groupEditController', function ($scope, $http) {

    var start = "~* ^";
    var end = "($|/|\\?)";
    $scope.namePrefix = '@N_';

    $scope.groupInfo = {
        "id": "",
        "name": "",
        "app-id": "",
        "version": "",
        "ssl": false,
        "group-virtual-servers": [
            {
                "virtual-server": {
                    "id": ""
                },
                "priority": "",
                "path": "",
                "name": "",
                "rewrite": ""
            }
        ],
        "health-check": {
            "timeout": "5000",
            "intervals": "5000",
            "fails": "3",
            "passes": "1",
            "uri": ""
        },
        "load-balancing-method": {
            "type": "roundrobin",
            "value": ""
        },
        "group-servers": [
            {
                "ip": "",
                "host-name": "",
                "port": "80",
                "weight": "5",
                "max-fails": "0",
                "fail-timeout": "0"
            }
        ]
    };

    // VS ID Dropdown
    $scope.remoteVsesUrl = function () {
        return G.baseUrl + "/api/meta/vses";
    };
    $scope.cacheRequestFn = function (str) {
        return {q: str, timestamp: new Date().getTime()};
    };
    $scope.selectVsId = function (o) {
        if (o) {
            this.$parent.col['virtual-server']['id'] = o.originalObject.id;
        } else {
        }
    };
    $scope.searchVses = function () {
        var vses = [];
        $http.get(G.baseUrl + "/api/vses").success(
            function (response) {
                if (response['total'] > 0) {
                    $.each(response['virtual-servers'], function (i, val) {
                        vses.push(val);
                    });
                }
                $scope.vses = vses;
            }
        );
    };

    $scope.groupServersTable = {
        columns: $scope.groupInfo['group-servers'],
        add: function (index) {
            this.columns.push({
                "ip": "",
                "host-name": "",
                "port": "80",
                "weight": "5",
                "max-fails": "0",
                "fail-timeout": "0"
            });
            setTimeout(function () {
                $("#editGroupServer" + (index + 1)).hide();
                $("#groupServerIp" + (index + 1)).prop('disabled', false);
                $("#groupServerHostName" + (index + 1)).prop('disabled', false);
                $("#groupServerPort" + (index + 1)).prop('disabled', false);
                $("#groupServerWeight" + (index + 1)).prop('disabled', false);
                $("#groupServerMaxFails" + (index + 1)).prop('disabled', false);
                $("#groupServerFailTimeout" + (index + 1)).prop('disabled', false);

                $("#groupServerIp" + (index + 1)).bootstrapValidation();
            }, 10);
        },
        remove: function (index) {
            this.columns.splice(index, 1);
        },
        edit: function (index) {
            $("#editGroupServer" + (index)).hide();
            $("#groupServerIp" + (index)).prop('disabled', false);
            $("#groupServerHostName" + (index)).prop('disabled', false);
            $("#groupServerPort" + (index)).prop('disabled', false);
            $("#groupServerWeight" + (index)).prop('disabled', false);
            $("#groupServerMaxFails" + (index)).prop('disabled', false);
            $("#groupServerFailTimeout" + (index)).prop('disabled', false);

            $("#groupServerIp" + (index)).bootstrapValidation();
        }
    };
    $scope.editVirtualServerIndex;
    $scope.groupVirtualServersTable = {
        selectedColumn: [],
        addedColumns: [],
        initColumns: {
            "virtual-server": {
                "id": ""
            },
            "priority": "",
            "path": start + end,
            "rewrite": ""
        },
        columns: _.sortBy($scope.groupInfo['group-virtual-servers'], function (item) {
            return item['virtual-server'].id;
        }),
        add: function (vs, index) {
            var c = {
                "virtual-server": {
                    "id": ""
                },
                "priority": vs.priority,
                "path": vs.path,
                "name": vs.name,
                "rewrite": vs.rewrite
            };
            //this.columns.push(c);
            c.showPath = $scope.getShowPath(c.path);
            c.showName = $scope.getPureNameForEditing(name);
            c.showNamePrefix = $scope.shouldShowNamePrefix(name);
            this.addedColumns = [];
            this.addedColumns.push(c);

            $('#addVirtualServerModel').modal("show");
        },
        remove: function (index) {
            $scope.editVirtualServerIndex = index;
            $('#doubleConfirmRemoveGroupVirtualServerModal').modal("show");
        },
        edit: function (vs, index) {
            var showPath = "";
            var e = {
                "virtual-server": {
                    "id": vs["virtual-server"].id
                },
                "priority": vs.priority,
                "path": vs.path,
                "name": vs.name,
                "rewrite": vs.rewrite
            };

            $('#confirmEditGroupVsDialog').modal("show");
            $scope.editVirtualServerIndex = index;
            $scope.vsBindingDisabled = true;
            $scope.groupVirtualServersTable.selectedColumn = [];

            e.showPath = $scope.getShowPath(e.path);
            e.showName = $scope.getPureNameForEditing(vs.name);
            e.showNamePrefix = $scope.shouldShowNamePrefix(vs.name);

            $scope.groupVirtualServersTable.selectedColumn.push(e);

            $('#groupEditVsPriority0').prop("disabled", true);
            $('#groupEditVsRewrite0').prop("disabled", true);
            $('#groupEditVsPath0').prop("disabled", true);
            $('#pathEditRootSetting0').prop("disabled", true);
            $('#EditGroupPriority').show();
            $('#editGroupRewrite').show();
            $('#editVsPath').show();
            $('#editVsId').show();

            $('#editVirtualServerModel').modal("show");
        }
    };

    $scope.showSuffixText = function (path) {
        if (path == undefined || path == "") return true;
        return path.startWith(start) && path.endWith(end);
    }

    $scope.shouldShowNamePrefix = function (name) {
        if (!name) {
            return true;
        }
        return name.startWith($scope.namePrefix);
    };
    $scope.getPureNameForEditing = function (name) {
        if (!name) {
            return '';
        }
        return $scope.shouldShowNamePrefix(name) ? name.substring($scope.namePrefix.length) : name;
    };

    $scope.confirmAddPriority = function () {
        $('#groupVsPriority0').prop("disabled", false);
        $('#NewGroupPriority').hide();
    }
    $scope.confirmAddRewrite = function () {
        $('#groupNewVsRewrite0').prop("disabled", false);
        $('#newGroupRewrite').hide();
    }
    $scope.confirmAddName = function () {
        $('#groupNewVsName0').prop("disabled", false);
        $('#newGroupName').hide();
    }

    $scope.confirmEditPriority = function () {
        $('#groupEditVsPriority0').prop("disabled", false);
        $('#EditGroupPriority').hide();
    }
    $scope.confirmEditName = function () {
        $('#groupEditVsName0').prop("disabled", false);
        $('#editGroupName').hide();
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
    $scope.toggleAddRootPath = function () {
        var c = $('#pathAddRootSetting0').is(':checked');
        if (c) {
            $scope.groupVirtualServersTable.addedColumns[0].path = "~* ^/";
            $scope.groupVirtualServersTable.addedColumns[0].showPath = "~* ^/";
            $scope.groupVirtualServersTable.addedColumns[0].priority = -1000;
            $('#groupNewVsPath0').prop("disabled", true);
        }
        else {
            $('#groupNewVsPath0').prop("disabled", false);
            $scope.groupVirtualServersTable.addedColumns[0].priority = 1000;
            $scope.groupVirtualServersTable.addedColumns[0].path = $scope.groupVirtualServersTable.columns[0].path;
            $scope.groupVirtualServersTable.addedColumns[0].showPath = $scope.getShowPath($scope.groupVirtualServersTable.columns[0].path);
        }
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
    $scope.toggleEditRootPath = function () {
        var c = $('#pathEditRootSetting0').is(':checked');
        if (c) {
            $scope.groupVirtualServersTable.selectedColumn[0].path = "~* ^/";
            $scope.groupVirtualServersTable.selectedColumn[0].showPath = "~* ^/";
            $scope.groupVirtualServersTable.selectedColumn[0].priority = -1000;
            $('#groupEditVsPath0').prop("disabled", true);
        }
        else {
            $('#groupEditVsPath0').prop("disabled", false);
            $scope.groupVirtualServersTable.selectedColumn[0].priority = 1000;
            $scope.groupVirtualServersTable.selectedColumn[0].path = $scope.groupVirtualServersTable.columns[$scope.editVirtualServerIndex].path;
            $scope.groupVirtualServersTable.selectedColumn[0].showPath = $scope.getShowPath($scope.groupVirtualServersTable.columns[$scope.editVirtualServerIndex].path);
        }
    };
    $scope.confirmRemoveGroupVirtualServer = function () {
        $scope.groupVirtualServersTable.columns.splice($scope.editVirtualServerIndex, 1);
    }
    $('#saveEditVirtualServer').click(function () {
        $('[data-validator-type="validation"]').bootstrapValidation("reset");
        $('[data-validator-type="validation"]').bootstrapValidation();
        var reviewResult = reviewDataZone($('.edit-vs-table'));
        if (reviewResult) {
            $('#editVirtualServerModel').modal("hide");
            $('#doubleConfirmEditGroupVirtualServerModal').modal("show");
        }
    });
    $("#saveNewVirtualServer").click(function (event) {
        $('[data-validator-type="validation"]').bootstrapValidation("reset");
        $('[data-validator-type="validation"]').bootstrapValidation();
        var reviewResult = reviewDataZone($('.add-vs-dv'));
        if (reviewResult) {
            $('#addVirtualServerModel').modal("hide");
            $('#doubleConfirmAddGroupVirtualServerModal').modal("show");
        }
    });
    $scope.confirmEditGroupVirtualServer = function () {
        // replace the group vs
        var c = $scope.groupVirtualServersTable.selectedColumn[0];
        var showSuffix = $scope.showSuffixText(c.path);
        if (c.showPath && showSuffix) {
            c.path = start + c.showPath + end;
        }
        else {
            c.path = c.showPath;
        }
        c.name = c.showName ? (c.showNamePrefix ? $scope.namePrefix + c.showName : c.showName) : null;
        $scope.groupVirtualServersTable.columns.splice($scope.editVirtualServerIndex, 1);
        $scope.groupVirtualServersTable.columns.push(c);
    };
    $scope.confirmAddGroupVirtualServer = function () {
        var c = $scope.groupVirtualServersTable.addedColumns[0];
        var showSuffix = $scope.showSuffixText(c.path);
        if (c.showPath && showSuffix) {
            c.path = start + c.showPath + end;
        }
        else {
            c.path = c.showPath;
        }
        c.name = c.showName ? (c.showNamePrefix ? $scope.namePrefix + c.showName : c.showName) : null;
        $scope.groupVirtualServersTable.columns.push(c);
    }

    $scope.focusGField = function (field) {
        var resource = $scope.resource;
        var i = resource['groupnew_groupEditApp_info'];

        switch (field) {
            case "path": {
                $('.field-description').html("<ul class='operation-warnning-ul'> " + i['path'] +
                    "<li>" + i['li1'] + "</li>" +
                    "<li>" + i['li2'] + "</li>" +
                    "<li>" + i['li3'] + "</li>");
                break;
            }
            case "name": {
                $('.field-description').html("<span>"+i["name"]+"</span>");
                break;
            }
            case "rewrite": {
                $('.field-description').html('<span>'+i["rewrite"]+'</span>');
                break;
            }
            case "priority": {
                $('.field-description').html('<span>'+i["priority"]+'</span>');
                break;
            }
        }
    }

    $scope.clearGroupInfo = function (type) {
        if (type == 'new') {
            $scope.groupInfo['name'] = "";
            $scope.groupInfo['app-id'] = "";
            $scope.groupInfo['group-virtual-servers'] = [
                {
                    "virtual-server": {
                        "id": ""
                    },
                    "priority": "",
                    "path": "",
                    "rewrite": ""
                }
            ];
            $scope.groupVirtualServersTable.columns = $scope.groupInfo['group-virtual-servers'];
            $scope.groupInfo['health-check'].uri = "";
            $scope.groupInfo['group-servers'] = [
                {
                    "ip": "",
                    "host-name": "",
                    "port": "80",
                    "weight": "5",
                    "max-fails": "0",
                    "fail-timeout": "0"
                }
            ];
            $scope.groupServersTable.columns = $scope.groupInfo['group-servers'];
        }
        if (type == 'edit') {
            $scope.getGroupInfo($scope.groupId);
        }
    };

    function reviewDataZone(id) {
        var result = true;
        $.each(id.find('[data-validator-type="validation"]'), function (i, element) {
            if (!$(element).bootstrapValidation('validate'))
                result = false;
        });
        return result;
    }

    function reviewData() {
        var result = true;
        $.each($('[data-validator-type="validation"]'), function (i, element) {
            if (!$(element).bootstrapValidation('validate'))
                result = false;
        });
        return result;
    }

    $("#validateAddGroupBtn").click(function (event) {
        var reviewResult = reviewData();
        if (reviewResult) {
            $('#confirmAddGroup').modal({backdrop: 'static'});
        }
    });
    $("#validateEditGroupBtn").click(function (event) {
        var reviewResult = reviewData();
        if (reviewResult) {
            $('#confirmEditGroup').modal({backdrop: 'static'});
        }
    });
    $('.backLink').click(function () {
        window.history.back();
    });
    $scope.showForceUpdateBt = false;
    $scope.groupUpdateRequest;

    $scope.updateGroup = function () {
        $scope.showForceUpdateBt = false;
        // re-org the group info
        var req = {
            method: 'POST',
            url: G.baseUrl + '/api/group/update?description=' + $scope.query.reason,
            data: $scope.groupInfo
        };

        // if has duplicated groupservers
        var members = $scope.groupInfo['group-servers'];
        var ipc = _.pluck(members, 'ip');
        if (_.uniq(ipc).length != ipc.length) {
            alert("Members' server shall not share the same ip!");
            return;
        }

        $scope.groupUpdateRequest = req;

        $('#newGroupDialog').modal('show').find(".modal-body").html("正在更新.. <img src='/static/img/spinner.gif' />");
        $('#newGroupDialog').modal('show').find(".created-group-id").text('');
        $http(req).success(
            function (res, code) {
                if (code != 200) {
                    $('#newGroupDialog').modal('show').find(".modal-body").html("<span class='fa fa-times'></span> 更新失败。 失败原因:" + res.message);
                    $scope.showForceUpdateBt = true;
                } else {
                    $('#newGroupDialog').modal('show').find(".modal-body").html("<span class='fa fa-check'></span> 更新成功");
                    $('#newGroupDialog').modal('show').find(".created-group-id").text(res.id);

                    setTimeout(function () {
                        $scope.createGroupDismiss();
                    }, 2000);
                }
            }
        ).error(
            function (reject) {
                $('#newGroupDialog').find(".modal-body").html("<span class='fa fa-times'></span> 更新失败。 失败原因:" + reject);
            }
        );
    };
    $scope.newGroup = function () {
        var resource = $scope.resource;
        var p = resource['groupnew']['groupnew_groupEditApp_progress'];
        $scope.showForceUpdateBt = false;
        delete  $scope.groupInfo.id;
        $scope.groupInfo['group-virtual-servers'] = [];
        var validationPassed = true;
        // for virtual server field
        $.each($scope.groupVirtualServersTable.columns, function (i, val) {
            var path = "";
            if (val.showPath && val.showPath != '~* ^/' && !val.showPath.startWith('~* ^/')) {
                path = start + val.showPath + end;
            }
            else {
                path = val.path;
            }
            if (val["virtual-server"].id == "" || val["virtual-server"].id == undefined) {
                alert(p["Virtual Server id 为必填字段"]);
                validationPassed = false;
            }

            var obj = {
                'path': path.replace(/\\\"/g, '"'),
                'name': val.showName ? (val.showNamePrefix ? $scope.namePrefix + val.showName : val.showName) : null,
                'rewrite': val.rewrite == undefined ? "" : val.rewrite.replace(/\\\"/g, '"'),
                'virtual-server': val['virtual-server'],
                'priority': val.priority
            };
            $scope.groupInfo['group-virtual-servers'].push(obj);
        });

        // for group server field
        $.each($scope.groupServersTable.columns, function (i, val) {

            if ($scope.groupInfo['group-servers'][i].ip == undefined || $scope.groupInfo['group-servers'][i]["host-name"] == undefined
                || $scope.groupInfo['group-servers'][i]["port"] == undefined || $scope.groupInfo['group-servers'][i]["weight"] == undefined) {
                alert(p["Host IP、Name、Port、Weight不能为空"]);
                validationPassed = false;
            }
            $scope.groupInfo['group-servers'][i].ip = val["ip"].trim();
            $scope.groupInfo['group-servers'][i]["host-name"] = val["host-name"].trim();
            $scope.groupInfo['group-servers'][i]["port"] = val["port"];
            $scope.groupInfo['group-servers'][i]["weight"] = val["weight"];
        });

        if (!validationPassed) return;

        var req = {
            method: 'POST',
            url: G.baseUrl + '/api/group/new?description=' + $scope.query.reason,
            data: $scope.groupInfo
        };
        $scope.groupUpdateRequest = req;

        var members = $scope.groupInfo['group-servers'];
        var ipc = _.pluck(members, 'ip');
        if (_.uniq(ipc).length != ipc.length) {
            alert("Members' server shall not share the same ip!");
            return;
        }

        $('#newGroupDialog').modal('show').find(".modal-body").html(p["正在创建.."] + " <img src='/static/img/spinner.gif' />");
        $('#newGroupDialog').modal('show').find(".created-group-id").text('');
        $http(req).success(
            function (res, code) {
                if (code != 200) {
                    $('#newGroupDialog').modal('show').find(".modal-body").html("<span class='fa fa-times'></span> " + p["创建失败。 失败原因:"] + res.message);
                    $scope.showForceUpdateBt = true;
                } else {
                    $('#newGroupDialog').modal('show').find(".modal-body").html("<span class='fa fa-check'></span> " + p["更新成功"]);
                    $('#newGroupDialog').modal('show').find(".created-group-id").text(res.id);

                    setTimeout(function () {
                        $scope.createGroupDismiss();
                    }, 2000);
                }
            }
        ).error(
            function (reject) {
                $('#newGroupDialog').find(".modal-body").html("<span class='fa fa-times'></span> " + p["更新失败。 失败原因:"] + reject);
            }
        );

    };
    $scope.forceCreateGroupDismiss = function () {

        var resource = $scope.resource;
        var p = resource['groupnew']['groupnew_groupEditApp_progress'];

        $scope.showForceUpdateBt = false;
        var req = $scope.groupUpdateRequest;
        req.params = {
            force: true
        };

        $('#newGroupDialog').modal('show').find(".modal-body").html(p['正在强制更新..'] + " <img src='/static/img/spinner.gif' />");
        $('#newGroupDialog').modal('show').find(".created-group-id").text('');
        $http(req).success(
            function (res, code) {
                if (code != 200) {
                    $('#newGroupDialog').modal('show').find(".modal-body").html("<span class='fa fa-times'></span> " + p["创建失败。 失败原因:"] + res.message);
                    $scope.showForceUpdateBt = true;
                } else {
                    $('#newGroupDialog').modal('show').find(".modal-body").html("<span class='fa fa-check'></span> " + p["更新成功"]);
                    $('#newGroupDialog').modal('show').find(".created-group-id").text(res.id);

                    setTimeout(function () {
                        $scope.createGroupDismiss();
                    }, 2000);
                }
            }
        ).error(
            function (reject) {
                $('#newGroupDialog').find(".modal-body").html("<span class='fa fa-times'></span> " + p["更新失败。 失败原因:"] + reject);
            }
        );
    };

    $scope.createGroupDismiss = function () {
        if ($('.created-group-id').text() == '') {
            return;
        }
        window.location.href = "/portal/group" + "#?env=" + G.env + "&groupId=" + $('.created-group-id').text();
    }
    $scope.getGroupInfo = function (groupId, clone) {
        $http.get(G.baseUrl + "/api/group?groupId=" + groupId).success(
            function (res) {
                $scope.groupInfo = res;

                if (window.location.pathname == "/portal/group/new") {
                    // copy group
                    $scope.groupInfo.id = "";
                    $scope.groupInfo.name = res.name + "_copy";
                    $scope.groupInfo['health-check'].uri = "";
                }

                $.each($scope.groupInfo['group-virtual-servers'], function (i, val) {
                    val["virtual-server"]["id"] = val["virtual-server"]["id"].toString();
                });
            }
        ).then(
            function () {
                if (!clone) {
                    $scope.groupServersTable.columns = $scope.groupInfo['group-servers'];
                }
                else {
                    if ($scope.groupServersTable.columns[0].ip == "" || $scope.groupServersTable.columns[0].ip == undefined) $scope.groupServersTable.columns = $scope.groupInfo['group-servers'];
                }
                $scope.groupVirtualServersTable.columns = $scope.groupInfo['group-virtual-servers'];
            }
        );
    };
    $scope.showHealthUrl = function () {
        return $scope.groupInfo['health-check'] != undefined;
    }
    $scope.getVs = function (vsId) {
        $http.get(G.baseUrl + "/api/vs?vsId=" + vsId).success(
            function (res) {
                res.id = res.id.toString();
                $scope.groupVirtualServersTable.columns = [{
                    "virtual-server": res,
                    "priority": "",
                    "path": "",
                    "rewrite": ""
                }];
            }
        );
    };

    var vsId;
    $scope.hashChanged = function (hashData) { $scope.resource = H.resource;
        $scope.resource = H.resource;
        if ($scope.env != hashData.env) {
            //$scope.searchVses();
            $scope.env = hashData.env;
        }

        if (hashData.groupId == null || hashData.groupId == "") {
            // new group
            $scope.groupInfo = {
                "id": "",
                "name": "",
                "app-id": "",
                "version": "",
                "ssl": false,
                "group-virtual-servers": [
                    {
                        "virtual-server": {
                            "id": ""
                        },
                        "priority": "",
                        "path": "",
                        "rewrite": "",
                        "showPath": ""
                    }
                ],
                "health-check": {
                    "timeout": "3000",
                    "intervals": "5000",
                    "fails": "3",
                    "passes": "1",
                    "uri": ""
                },
                "load-balancing-method": {
                    "type": "roundrobin",
                    "value": ""
                },
                "group-servers": [
                    {
                        "ip": "",
                        "host-name": "",
                        "port": "80",
                        "weight": "5",
                        "max-fails": "0",
                        "fail-timeout": "0"
                    }
                ]
            };
            $scope.groupServersTable.columns = $scope.groupInfo['group-servers'];
            $scope.groupVirtualServersTable.columns = [];
        }
        else {
            var clone = false;
            // edit or copy group
            $scope.groupId = hashData.groupId;
            if (window.location.pathname == "/portal/group/new") {
                // clone group
                clone = true;
            }
            $scope.getGroupInfo(hashData.groupId, clone);
        }

        //add group
        if (hashData.vsId) {
            vsId = hashData.vsId;
            $scope.getVs(hashData.vsId);
        }
    };
    H.addListener("groupEditApp", $scope, $scope.hashChanged);
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
});
angular.bootstrap(document.getElementById("group-edit-area"), ['groupEditApp']);

var groupEditDropdownApp = angular.module('groupEditDropdownApp', ["angucomplete-alt", "http-auth-interceptor"]);
groupEditDropdownApp.controller('groupEditDropdownController', function ($scope, $http) {
    $scope.context = {
        targetIdName: 'groupId',
        targetNameArr: ['app-id', 'id', 'name'],
        targetsUrl: '/api/meta/groups',
        targetsName: 'groups'
    };

    $scope.target = {
        id: null,
        name: ''
    };
    $scope.targets = {};

    $scope.cacheRequestFn = function (str) {
        return {q: str, timestamp: new Date().getTime()};
    };

    $scope.remoteUrl = function () {
        return G.baseUrl + "/api/meta/groups";
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
            }
        );
    };

    $scope.targetCloneGroupId;
    $scope.selectTarget = function (t) {
        if (t) {
            var toId = t.originalObject.id;
            if ($scope.target.id != toId) {
                var timestamp = new Date().getTime();
                var pairs = {};
                pairs[$scope.context.targetIdName] = toId;
                pairs["timestamp"] = timestamp;

                $scope.targetCloneGroupId = pairs;
            }
            $('#targetSelector').val(t.originalObject.id);
        }
    };
    $('#confirmClone').click(
        function () {
            H.setData($scope.targetCloneGroupId);
        }
    );
    $scope.hashChanged = function (hashData) { $scope.resource = H.resource;
        $scope.resource = H.resource;
        if (hashData.env) {
            $scope.env = hashData.env;
        }

        $scope.getAllTargets();
        var n = $scope.context.targetIdName;
        if (hashData[n]) {
            $scope.target.id = hashData[n];
        } else {
            $scope.target.id = null;
            $scope.target.name = "下拉选择Group进行克隆";
        }
    };
    H.addListener("groupEditDropdownApp", $scope, $scope.hashChanged);
});
angular.bootstrap(document.getElementById("group-edit-dropdown-area"), ['groupEditDropdownApp']);