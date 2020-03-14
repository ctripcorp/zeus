//Group-edit Component
var groupEditApp = angular.module('groupEditApp', ["angucomplete-alt", 'http-auth-interceptor']);
groupEditApp.controller('groupEditController', function ($scope, $http) {
    var start = "~* ^";
    var end = "($|/|\\?)";
    $scope.namePrefix = '@N_';
    $scope.target = {
        id: '',
        pair: {}
    };
    $scope.context = {
        targetIdName: 'groupId',
        targetNameArr: ['app-id', 'id', 'name'],
        targetsUrl: '/api/meta/groups',
        targetsName: 'groups'
    };
    $scope.query = {
        groupId: '',
        vsId: '',
        targetGroupId: ''
    };
    // What this v-group target at?
    $scope.view = {
        targetGroup: {
            id: '',
            name: '',
            'app-id': '',
            'vses': []
        }
    };

    $scope.addNewVirtualServer = function () {
        $scope.popEditVirtualServer({
            'virtual-server': {
                id: '',
                name: '',
                path: '',
                priority: '',
                redirect: '',
                'custom-conf': ''
            }
        })
    };

    // Auto complete area
    $scope.cacheRequestFn = function (str) {
        return {q: str, timestamp: new Date().getTime()};
    };
    $scope.remoteVsesUrl = function () {
        return G.baseUrl + "/api/meta/vses";
    };

    $scope.remoteGroupUrl = function () {
        return G.baseUrl + "/api/meta/groups";
    };
    $scope.selectGroupTarget = function (t) {
        if (t) {
            var toId = t.originalObject.id;
            var timestamp = new Date().getTime();
            var pairs = {};

            if ($scope.target.id != toId) {
                $scope.target.id = toId;
                pairs['targetGroupId'] = toId;
                pairs["timestamp"] = timestamp;

                $scope.target.pair = pairs;
            }
            $('#targetSelector').val(t.originalObject.id);
        }
    };
    $scope.saveTargetGroup = function () {
        $scope.target.id = $('.selected-target-id').text();
        H.setData($scope.target.pair);
    };

    $scope.selectVsId = function (o) {
        if (o) {
            $scope.currentVs['virtual-server']['id'] = o.originalObject.id;
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

    /**
     * New A Virtual Group Area
     * **/
    // Edit Group Virtual Server Area
    $scope.currentVs;
    var isEditVS;
    var preVsId;
    $scope.popEditVirtualServer = function (vs) {
        if(vs["virtual-server"].id){
            // edit
            isEditVS = true;
            preVsId=vs["virtual-server"].id;
        }else{
            isEditVS = false;
            preVsId='';
        }

        var showPath = "";
        var e = {
            "virtual-server": {
                "id": vs["virtual-server"].id.toString()
            },
            "name": vs.name,
            "path": vs.path,
            "priority": vs.priority,
            "redirect": vs.redirect,
            "custom-conf": vs["custom-conf"]
        };
        $scope.vsBindingDisabled = true;
        var showSuffix = $scope.showSuffixText(e.path);
        if (e.path && showSuffix) {
            // Regular path
            showPath = e.path.substring(4, e.path.lastIndexOf(end));
            $('#path-row-id').removeClass('path-row-illegal');
            $('#path-row-id').addClass('paths-row');
        } else {
            showPath = e.path;
            $('#path-row-id').removeClass('paths-row');
            $('#path-row-id').addClass('path-row-illegal');
        }
        e.showPath = showPath;
        e.showNamePrefix = $scope.shouldShowNamePrefix(e.name);
        e.showName = $scope.getPureNameForEditing(e.name);
        $scope.currentVs = e;
        if ($scope.currentVs.redirect) {
            if ($scope.currentVs.redirect.indexOf('http://') != -1) {
                $('#sh').prop('checked', true);
                $('#hs').prop('checked', false);
            } else {
                $('#hs').prop('checked', true);
                $('#sh').prop('checked', false);
            }
        }
        if ($scope.currentVs['custom-conf'] == 'return 307 "https://$host$request_uri";' || $scope.currentVs['custom-conf'] == 'return 307 "http://$host$request_uri";') {
            $('#c307').prop('checked', true);
            $('#c302').prop('checked', false);
        } else {
            $('#c302').prop('checked', true);
            $('#c307').prop('checked', false);
        }

        $('#groupEditVsPriority0').prop("disabled", true);
        $('#groupEditVsPath0').prop("disabled", true);
        $('#pathEditRootSetting0').prop("disabled", true);
        $('#EditGroupPriority').show();
        $('#editGroupRewrite').show();
        $('#editVsPath').show();
        $('#editVsId').show();

        $('#editVirtualServerModel').modal("show").find(".field-description").html('');
    };
    $scope.confirmEditPriority = function () {
        $('#groupEditVsPriority0').prop("disabled", false);
        $('#editGroupPriority').hide();
    }
    $scope.confirmEditRedirect = function () {
        $('#groupEditVsRewrite0').prop("disabled", false);
        $('#editGroupRewrite').hide();
    }
    $scope.confirmEditPath = function () {
        $('#groupEditVsPath0').prop("disabled", false);
        $('#pathEditRootSetting0').prop("disabled", false);
        $('#editVsPath').hide();
    }
    $scope.confirmEditName = function () {
        $('#groupEditVsName0').prop("disabled", false);
        $('#editVsName').hide();
    }
    $scope.confirmEditVs = function () {
        $scope.vsBindingDisabled = false;
        $('#editVsId').hide();
    }
    $scope.confirmEditCustomConf = function () {
        $('#groupEditCustomConf').prop("disabled", false);
        $('#editCustomConfPath').hide();
    }
    $('#saveEditVirtualServer').click(function () {

        $('[data-validator-type="validation"]').bootstrapValidation("reset");
        $('[data-validator-type="validation"]').bootstrapValidation();

        var selectedVsId = $('#vsEdit0_value').val().trim();
        if (selectedVsId == undefined || selectedVsId == "") {
            alert("VS Id是必填项！");
            return;
        }
        if (!$('#hs').prop('checked') && !$('#sh').prop('checked')) {
            alert('请选择Redirect方式');
            return;
        }

        var path = $('#groupEditVsPath0').val();
        if (!path) {
            alert('Path必须');
            return;
        }

        if ($('#hs').prop('checked')) $scope.currentVs.redirect = '"(?i)(.*)" https://$host$1;';
        if ($('#sh').prop('checked')) $scope.currentVs.redirect = '"(?i)(.*)" http://$host$1;';


        if ($('#c307').prop('checked')) {
            if ($('#hs').prop('checked')) {
                $scope.currentVs['custom-conf'] = 'return 307 "https://$host$request_uri";';
            } else {
                $scope.currentVs['custom-conf'] = 'return 307 "http://$host$request_uri";';
            }
        }

        if ($('#c302').prop('checked')) {
            if ($scope.currentVs['custom-conf'] == 'return 307 "https://$host$request_uri";' || $scope.currentVs['custom-conf'] == 'return 307 "http://$host$request_uri";') {
                delete $scope.currentVs['custom-conf'];
            }
        }

        var reviewResult = reviewDataZone($('.edit-vs-table'));
        if (reviewResult) {
            $('#editVirtualServerModel').modal("hide");
            $('#doubleConfirmEditGroupVirtualServerModal').modal("show");
        }
    });
    $scope.confirmEditGroupVirtualServer = function () {
        // replace the group vs
        var c = $scope.currentVs;
        var showSuffix = $scope.showSuffixText(c.path);
        if (showSuffix && c.showPath) {
            c.path = start + c.showPath + end;
        } else {
            c.path = c.showPath;
        }
        c.name = c.showName ? (c.showNamePrefix ? $scope.namePrefix + c.showName : c.showName) : null;
        if(isEditVS){
            var index = _.findIndex($scope.view.targetGroup.vses, function (item) {
                return item['virtual-server'].id == preVsId;
            });
            if(index!=-1){
                $scope.view.targetGroup.vses.splice(index, 1);
                $scope.view.targetGroup.vses.push(c);
            }
        }else{
            $scope.view.targetGroup.vses.push(c);
        }
    };
    // Save New Virtual Group Area
    $("#validateAddGroupBtn").click(function (event) {
        var reviewResult = reviewData();
        if (reviewResult) {
            $('#confirmAddGroup').modal({backdrop: 'static'});
        }
    });
    $scope.saveNewVirtualGroup = function () {
        $scope.newgroupinfo = {};
        $scope.newgroupinfo.name = $scope.view.targetGroup.name;
        $scope.newgroupinfo['app-id'] = $scope.view.targetGroup['app-id'];

        $scope.newgroupinfo['group-virtual-servers'] = [];
        var validationPassed = true;
        // for virtual server field
        $.each($scope.view.targetGroup.vses, function (i, val) {
            var path = "";
            if (val.showPath && val.showPath != '~* ^/') {
                path = start + val.showPath + end;
            }
            else {
                path = val.path;
            }
            if (val["virtual-server"].id == "" || val["virtual-server"].id == undefined) {
                alert("Virtual Server id 为必填字段");
                validationPassed = false;
            }

            var obj = {
                'path': path.replace(/\\\"/g, '"'),
                'name': val.name,
                'rewrite': val.rewrite == undefined ? "" : val.rewrite.replace(/\\\"/g, '"'),
                'virtual-server': val['virtual-server'],
                'priority': val.priority,
                'redirect': val.redirect == undefined ? "" : val.redirect.replace(/\\\"/g, '"'),
                'custom-conf': val['custom-conf']
            };
            $scope.newgroupinfo['group-virtual-servers'].push(obj);
        });

        if (!validationPassed) return;
        var req = {
            method: 'POST',
            url: G.baseUrl + '/api/vgroup/new',
            data: $scope.newgroupinfo
        };

        $scope.groupUpdateRequest = req;
        $('#newGroupDialog').modal('show').find(".modal-body").html("正在创建.. <img src='/static/img/spinner.gif' />");
        $('#newGroupDialog').modal('show').find(".created-group-id").text('');
        $http(req).success(
            function (res) {
                if (res.code) {
                    $('#newGroupDialog').modal('show').find(".modal-body").html("<span class='fa fa-times'></span> 创建失败。 失败原因:" + res.message);
                    $scope.showForceUpdateBt = true;
                }
                else {
                    $('#newGroupDialog').modal('show').find(".modal-body").html("<span class='fa fa-check'></span> 创建成功");
                    $('#newGroupDialog').modal('show').find(".created-group-id").text(res.id);

                    setTimeout(function () {
                        $scope.createGroupDismiss();
                    }, 2000);
                }
            }
        ).error(
            function (reject) {
                $('#newGroupDialog').find(".modal-body").html("<span class='fa fa-times'></span> 创建失败。 失败原因:" + reject);
            }
        );
    };
    $scope.showSuffixText = function (path) {
        if (path == undefined || path == "") return true;
        return path.startWith(start) && path.endWith(end);
    };
    $scope.getShowPath = function (path) {
        var showPath = "";
        var showSuffix = $scope.showSuffixText(path);
        if (showSuffix && path) {
            // Regular path
            showPath = path.substring(4, path.lastIndexOf(end));
        } else {
            showPath = path;
        }
        return showPath;
    };
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
    $scope.toggleEditRootPath = function () {
        var c = $('#pathEditRootSetting0').is(':checked');
        if (c) {
            $scope.currentVs.path = "~* ^/";
            $scope.currentVs.showPath = "~* ^/";
            $('#groupEditVsPath0').prop("disabled", true);
        } else {
            $('#groupEditVsPath0').prop("disabled", false);
            $scope.currentVs.path = $scope.view.targetGroup.path;
            $scope.currentVs.showPath = $scope.getShowPath($scope.view.targetGroup.path);
        }
    };

    /**Remove A Virtual Server Area**/
    $scope.popRemoveVirtualServer = function (vs) {
        var index = _.findIndex($scope.view.targetGroup.vses, function (item) {
            return item['virtual-server'].id == vs['virtual-server'].id && item.path == vs['path'] && item.priority == vs.priority;
        });
        $scope.view.targetGroup.vses.splice(index, 1);
    };
    /* Update a Virtual Group Area
    * **/
    $scope.confirmAddPriority = function () {
        $('#groupVsPriority0').prop("disabled", false);
        $('#NewGroupPriority').hide();
    }
    $scope.confirmAddRewrite = function () {
        $('#groupNewVsRewrite0').prop("disabled", false);
        $('#newGroupRewrite').hide();
    }

    $("#validateEditGroupBtn").click(function (event) {
        var reviewResult = reviewDataZone($('#inputarea'));
        if (reviewResult) {
            $('#confirmEditGroup').modal({backdrop: 'static'});
        }
    });
    $scope.resetChanges = function () {
        window.location.reload();
    };
    $scope.editVirtualGroup = function () {
        $scope.newgroupinfo = {};
        $scope.newgroupinfo.id = $scope.view.targetGroup.id;
        $scope.newgroupinfo.name = $scope.view.targetGroup.name;
        $scope.newgroupinfo.version = $scope.view.targetGroup.version;
        $scope.newgroupinfo['app-id'] = $scope.view.targetGroup['app-id'];
        var validationPassed = true;
        $scope.newgroupinfo['group-virtual-servers'] = [];
        $.each($scope.view.targetGroup.vses, function (i, val) {
            var path = "";
            if (val.showPath && val.showPath != '~* ^/') {
                path = start + val.showPath + end;
            }
            else {
                path = val.path;
            }
            if (val["virtual-server"].id == "" || val["virtual-server"].id == undefined) {
                alert("Virtual Server id 为必填字段");
                validationPassed = false;
            } else {
                val["virtual-server"].id = parseInt(val["virtual-server"].id);
            }

            var obj = {
                'path': path.replace(/\\\"/g, '"'),
                'name': val.name,
                'virtual-server': val['virtual-server'],
                'priority': val.priority,
                'redirect': val.redirect == undefined ? "" : val.redirect.replace(/\\\"/g, '"'),
                'custom-conf': val['custom-conf']
            };
            $scope.newgroupinfo['group-virtual-servers'].push(obj);
        });

        if (!validationPassed) return;
        // re-org the group info
        var req = {
            method: 'POST',
            url: G.baseUrl + '/api/vgroup/update',
            data: $scope.newgroupinfo
        };
        $scope.groupUpdateRequest = req;
        $('#newGroupDialog').modal('show').find(".modal-body").html("正在更新.. <img src='/static/img/spinner.gif' />");
        $('#newGroupDialog').modal('show').find(".created-group-id").text('');

        var confirmDialog = $('#newGroupDialog');
        $http(req).success(
            function (res, code) {
                var errText = '';
                if (code != 200) {
                    errText = "<span class='fa fa-times'></span><span style='padding-left: 10px'>更新失败</span>";
                    confirmDialog.modal('show').find(".modal-title").html(errText);
                    confirmDialog.modal('show').find(".modal-body").html("失败原因:" + res.message);
                    $scope.showForceUpdateBt = true;
                }
                else {
                    var successText = "<span class='fa fa-check'></span><span style='padding-left: 10px'>更新成功</span>";
                    confirmDialog.modal('show').find(".modal-body").html(successText);
                    confirmDialog.modal('show').find(".created-group-id").text(res.id);
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

    $('.backLink').click(function () {
        window.history.back();
    });
    $scope.showForceUpdateBt = false;
    $scope.groupUpdateRequest;
    $scope.forceCreateGroupDismiss = function () {
        var req = $scope.groupUpdateRequest;

        if (req.params == undefined) {
            req.params = {
                force: true
            };
        }
        else {
            req.params.force = true;
        }
        $('#newGroupForceDialog').modal('show').find(".modal-body").html("正在强制更新.. <img src='/static/img/spinner.gif' />");
        $('#newGroupForceDialog').modal('show').find(".created-group-id").text('');
        $http(req).success(
            function (res) {
                if (res.code) {
                    $('#newGroupForceDialog').modal('show').find(".modal-body").html("<span class='fa fa-times'></span> 操作失败。 失败原因:" + res.message);
                    if (res.message.indexOf("overlap") > 0) {
                        $scope.showForceUpdateBt = true;
                    }
                }
                else {
                    $scope.showForceUpdateBt = false;
                    $('#newGroupForceDialog').modal('show').find(".modal-body").html("<span class='fa fa-check'></span> 操作成功");
                    $('#newGroupForceDialog').modal('show').find(".created-group-id").text(res.id);

                    setTimeout(function () {
                        $scope.createGroupDismiss();
                    }, 2000);
                }
            }
        ).error(
            function (reject) {
                $('#newGroupForceDialog').find(".modal-body").html("<span class='fa fa-times'></span> 操作失败。 失败原因:" + reject);
            }
        );
    };
    $scope.createGroupDismiss = function () {
        if ($('.created-group-id').text() == '') {
            return;
        }
        window.location.href = "/portal/vgroup" + "#?env=" + G.env + "&groupId=" + $('.created-group-id').text();
    }
    $scope.groupData = {};
    $scope.getGroupInfo = function () {
        var groupId = $scope.query.groupId;
        var param = {
            groupId: groupId
        };
        var request = {
            method: 'GET',
            params: param,
            url: G.baseUrl + '/api/vgroup'
        };
        $http(request).success(
            function (response) {
                $scope.groupData = $.extend(true, {}, response);
                $scope.view.targetGroup.id = response.id;
                $scope.view.targetGroup.version = response.version;
                $scope.view.targetGroup.name = response.name;
                $scope.view.targetGroup['app-id'] = response['app-id'];
                $scope.view.targetGroup.vses = response['group-virtual-servers'];
            }
        );
    };
    $scope.getTargetGroup = function () {
        var param = {
            groupId: $scope.query.targetGroupId
        };
        var request = {
            method: 'GET',
            url: G.baseUrl + '/api/group',
            params: param
        };
        $http(request).success(function (response) {
            $('.main-container').hideLoading();
            $scope.view.targetGroup.name = response.name + '_virtual';
            $scope.view.targetGroup['app-id'] = response['app-id'];

            var v = _.find(response['group-virtual-servers'], function (item) {
                return item['virtual-server'].id == $scope.query.vsId;
            });

            if (!v) {
                v = {
                    path: '',
                    priority: '1000',
                    'virtual-server': {
                        id: $scope.query.vsId
                    }
                };
            }

            $scope.view.targetGroup.vses = [v];
        });
    };
    $scope.getVs = function (vsId) {
        $http.get(G.baseUrl + "/api/vs?vsId=" + vsId).success(
            function (res) {
                $('.main-container').hideLoading();

                res.id = res.id.toString();
                $scope.view.targetGroup.vses.push(
                    {
                        path: '',
                        priority: 1000,
                        'virtual-server': res
                    }
                );
            }
        );
    };

    $scope.loadData = function (hashData) {
        $scope.query.groupId = hashData.groupId;
        $scope.query.vsId = hashData.vsId;
        $scope.query.targetGroupId = hashData.targetGroupId;

        if ($scope.env != hashData.env) {
            $scope.env = hashData.env;
        }
        $scope.view.targetGroup.vses = [];
        // Edit and existing v-group
        if ($scope.query.groupId != undefined && $scope.query.groupId != "") {
            $scope.getGroupInfo(hashData.groupId);
            return;
        }
        // New a v-group
        if ($scope.query.vsId != undefined && $scope.query.vsId !== "") {
            if ($scope.query.targetGroupId) {
                $scope.getTargetGroup($scope.query.targetGroupId);
            } else {
                $scope.getVs($scope.query.vsId);
            }
            return;
        }
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

    $scope.hashChanged = function (hashData) { $scope.resource = H.resource;
        if (hashData.env) {
            $scope.env = hashData.env;
        }
        var n = $scope.context.targetIdName;
        if (hashData[n]) {
            $scope.target.id = hashData[n];
        } else {
            $scope.target.id = null;
            $scope.target.name = "下拉选择Group进行克隆";
        }
        $scope.loadData(hashData);
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
