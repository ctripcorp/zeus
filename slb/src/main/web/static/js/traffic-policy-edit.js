/**
 * Created by ygshen on 2017/2/9.
 */
//Group-edit Component
var trafficEditApp = angular.module('trafficEditApp', ["angucomplete-alt", 'http-auth-interceptor']);
trafficEditApp.controller('trafficEditController', function ($scope, $http, $q) {
    var alert;
    var userPolicyApp;
    var supportedV = [
        '.NET 转 Java',
        '应用迁移',
        '大服务拆分',
        '多版本测试',
        '其它'
    ];
    // Scope data Area
    $scope.query = {
        groupid: '',
        policyId: '',
        fromId: ''
    };
    $scope.trafficInfo = {
        "name": "",
        "controls": [],
        "policy-virtual-servers": []
    };
    $scope.policyTargets = [];
    $scope.selectedTarget = {};

    $scope.currentweight = 50;
    $scope.groups = {};
    $scope.vses = {};
    $scope.apps = {};
    $scope.loginUserInfo = {};

    $scope.sameVses = [];
    $scope.diffVses = [];

    $scope.suggestPriority = 1000;
    $scope.realPriority = 0;
    $scope.supportedVsesStr = '';
    $scope.error = {
        code: '',
        message: ''
    };

    $scope.showGroupsError = false;

    // VS ID Dropdown Area
    $scope.remoteGroupsUrl = function () {
        return G.baseUrl + "/api/meta/groups";
    };
    $scope.selectGroupId = function (o) {
        if (o) {
            $scope.query.groupid = o.originalObject.id;
            $('.query-id').text($scope.query.groupid + '(' + $scope.groups[$scope.query.groupid].name + ')');
        }
    };
    $scope.cacheRequestFn = function (str) {
        return {q: str, timestamp: new Date().getTime()};
    };

    $scope.getTargetLanguage = function (x) {
        var resource = $scope.resource;
        if (!resource || _.keys(resource).length == 0) return;

        var map = resource['policynew']['policynew_trafficEditApp_typemap'];
        return map[x];
    };

    $scope.getErrorMessageLanguage=function (v) {

    };
    // New Controls Area
    $scope.suggestGroups = [];
    $scope.showSuggestArea = function () {
        if ($scope.trafficInfo.controls) {
            return $scope.trafficInfo.controls.length > 0;
        } else {
            return false;
        }
    };
    $scope.popAddNewControl = function () {
        $scope.query.groupid = '';
        $('#groupIdSelector_value').val('');
        /*$('input[name="wighttext"]').val('');*/
        $('#addNewTrafficPolicyModal').modal('show');
        $scope.error = {
            code: '',
            message: ''
        };
        if ($scope.trafficInfo.controls.length > 0) {
            var suggests = [];
            var sameVses = $scope.sameVses;
            var vsIds = [];
            var temp = _.pluck(sameVses, 'vsId');
            $.each(temp, function (i, item) {
                vsIds.push(parseInt(item));
            });
            var vsIdString = '';
            if (vsIds && vsIds.length > 0) vsIdString = vsIds.join();
            if (vsIdString != '') {
                var param = {
                    vsId: vsIdString,
                    type: 'extended'
                };
                var request = {
                    method: 'GET',
                    url: G.baseUrl + '/api/groups',
                    params: param
                };
                $http(request).success(function (res, code) {
                    if (code == 200) {
                        if (res.total != 0) {
                            suggests = _.reject(res.groups, function (item) {
                                var v = _.find($scope.trafficInfo.controls, function (r) {
                                    return r.group.id == item.id;
                                });
                                if (v != undefined) return true;
                                else return false;
                            });
                            var policyvses = _.map($scope.trafficInfo['policy-virtual-servers'], function (r) {
                                return r['virtual-server'].id;
                            });

                            suggests = _.reject(suggests, function (item) {
                                var groupvses = _.map(item['group-virtual-servers'], function (r) {
                                    return r['virtual-server'].id;
                                });
                                var v = _.find(policyvses, function (t) {
                                    if (groupvses.indexOf(t) == -1) return true;
                                    return false;
                                });
                                if (v) return true;
                                return false;
                            });

                            var vsesPaths = {};
                            var v = [];
                            $.each($scope.trafficInfo.controls, function (i, item) {
                                var id = item.group.id;
                                var c = _.filter($scope.groups[id]['group-virtual-servers'], function (r) {
                                    return vsIds.indexOf(r['virtual-server'].id) != -1;
                                });

                                v = v.concat(c);
                            });
                            if (v) {
                                $.each(v, function (i, v) {
                                    if (!vsesPaths[v['virtual-server'].id]) vsesPaths[v['virtual-server'].id] = [v.path.toLowerCase()];
                                    else vsesPaths[v['virtual-server'].id].push(v.path.toLowerCase());
                                });
                            }
                            // refine the suggests collection
                            suggests = _.reject(suggests, function (item) {
                                var v = _.find(item['group-virtual-servers'], function (s) {
                                    var vsId = s['virtual-server'].id;
                                    return vsesPaths[vsId] != undefined;
                                });
                                if (v) {
                                    var r = _.find(vsesPaths[v['virtual-server'].id], function (t) {
                                        return t.startsWith(v.path.toLowerCase()) || v.path.toLowerCase().startsWith(t);
                                    });

                                    if (r) return false;
                                    else return true;
                                }
                                return true;
                            });

                            $.each(suggests, function (i, group) {
                                var s = _.find(group.properties, function (item) {
                                    return item.name == 'status';
                                });
                                if (s) group.status = s.value;
                                var appId = group['app-id'];
                                var appName = $scope.apps[appId] ? $scope.apps[appId]['chinese-name'] : '-';
                                group.app = appId + '(' + appName + ')';
                                group.weight = 50;
                                group.paths = [];
                                group.pathOrder = 0;
                                var c = 0;
                                $.each(group['group-virtual-servers'], function (i, gVs) {
                                    var o = {
                                        path: gVs.path,
                                        priority: gVs.priority,
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
                            $scope.suggestGroups = suggests;
                            $('#suggest-groups').bootstrapTable('load', suggests);
                        }
                    } else {

                    }
                });
            }
        }
    };
    $scope.addNewControl = function (from) {
        var resource = $scope.resource;
        var errorMap = resource['policynew']['policynew_trafficEditApp_addNewTrafficPolicyModal'];

        $scope.error = {
            code: '',
            message: ''
        };
        var selectedGroupId = $scope.query.groupid;
        var weight = $scope.currentweight;

        if (from == 1) {
            var selected = $('#suggest-groups').find('tr.selected');
            if (selected.length == 0) {
                alert(alert['请至少选择一个推荐的Group']);
                return;
            } else {
                var c = selected[0];
                var d = $(c).find('.weight-control')[0];
                if (!$(d).val() || $(d).val() < 0) {
                    alert(alert['推荐的Group Weight不能小于0']);
                    return;
                }
                weight = $(d).val();
            }
        }
        if (!selectedGroupId || !weight) {
            $scope.error = {
                code: errorMap['验证错误']||'验证错误',
                message: errorMap['Group Id 和 Weight 都是必填字段'] ||'Group Id 和 Weight 都是必填字段'
            };
            return;
        }

        // Area: already added?
        var existed = _.find($scope.trafficInfo.controls, function (item) {
            return item.group.id == selectedGroupId;
        });
        if (existed) {
            $scope.error = {
                code: errorMap['重复'] || '重复',
                message: errorMap['当前Group已经在当前Policy中不能重复添加'] || '当前Group已经在当前Policy中不能重复添加'
            };
            return;
        }

        // Existing group and this group compare
        var existingControls = $scope.trafficInfo.controls;
        var existingGroupVses = {};
        $.each(existingControls, function (i, item) {
            existingGroupVses[item.group.id] = $scope.abstractGroupVsPaths($scope.groups[item.group.id])
        });

        var currentGroupVses = {};
        var currentGroupPaths = $scope.abstractGroupVsPaths($scope.groups[selectedGroupId]);
        currentGroupVses[selectedGroupId] = currentGroupPaths;
        var temp = $scope.validateControl(currentGroupVses, existingGroupVses);

        if (!temp) {
            $scope.error = {
                code: errorMap['验证出错'],
                message: errorMap['当前要添加的Group与Traffic Policy的Virtual Server不存在相同的Virtual Server， 不允许当前操作']
            };
            return;
        }
        $('#addNewTrafficPolicyModal').modal('hide');

        // Go to save and reload
        $scope.trafficInfo.controls.push(
            {
                group: {
                    id: selectedGroupId
                },
                weight: parseInt(weight)
            }
        );
        $scope.reloadTable($scope.trafficInfo);
        $scope.reloadGraphic($scope.trafficInfo);
    };
    $scope.saveNewControl = function () {
        $('#failed-div').text('');
        $('#success-div').text('');
        var v = true;
        var failedInputs = [];
        $.each($('.path-validate'), function (i, item) {
            if ($(item).attr('disabled')) return;
            var va = $(item).val();
            if (va && va.trim() != undefined) {
            } else {
                failedInputs.push(item);
                v = false;
            }
        });
        if (!v) {
            $.each(failedInputs, function (i, item) {
                $(item).focus();

                $(item).addClass('alert-border');
            });
            return;
        }

        var wcs = $('#controls-table').find('.weight-control');
        var a = _.filter(wcs, function (b) {
            var bvalue = $(b).val();

            if (bvalue == undefined) {
                $(b).addClass('alert-border');
                return true;
            }

            var c;
            try {
                c = parseInt(bvalue);
            } catch (e) {

            }
            if (isNaN(c)) {
                $(b).addClass('alert-border');
                $(b).focus();
                return true;
            }

            return false;
        });
        if (a && a.length > 0) return;


        var e = true;
        var f = _.filter($('.weight-control'), function (item) {
            var g = $(item).val();
            if (g < 0) return true;
            else return false;
        });

        e = f && f.length > 0 ? false : true;
        if (!e) {
            $scope.showGroupsError = true;
            return;
        }

        if (!$scope.selectedTarget.key) {
            $('.targetwarning').removeClass('hide');
            return;
        }
        var r = reviewData($('#previewarea'));
        var controlsValidate = $scope.trafficInfo.controls.length > 0;
        if (r && controlsValidate) {
            // set the controls weight again
            $.each($scope.trafficInfo.controls, function (i, item) {
                var h = $('#group_' + item.group.id).val();
                item.weight = h;
            });

            var post = $scope.getPostBody();
            var requestUrl = '';
            var successmessage = '';
            if ($scope.query.policyId) {
                requestUrl = G.baseUrl + '/api/policy/update?description=' + $scope.query.reason;
                successmessage = "更新Traffic Policy 成功";
            } else {
                requestUrl = G.baseUrl + '/api/policy/new?description=' + $scope.query.reason;
                successmessage = "新增 Policy 成功";
            }

            var request = {
                method: 'POST',
                data: post,
                url: requestUrl
            };
            var loading = "<img src='/static/img/spinner.gif' /> 正在保存设置";
            $('#confirmAddTrafficPolicyResult').modal('show').find(".modal-body").html(loading);
            $('#confirmAddTrafficPolicyResult').modal("show").find(".modal-title").html("正在保存设置");

            $scope.processRequest(request, $('#confirmAddTrafficPolicyResult'), "保存设置", successmessage);
        } else {
            if (!controlsValidate) $scope.showGroupsError = true;
            return;
        }
    };
    $scope.forceUpdatePolicy = function () {
        $scope.showForceUpdate = false;
        var loading = "<img src='/static/img/spinner.gif' /> 正在强制保存设置";
        $('#confirmAddTrafficPolicyResult').modal('show').find(".modal-body").html(loading);
        $('#confirmAddTrafficPolicyResult').modal("show").find(".modal-title").html("正在强制保存设置");

        if ($scope.forceUpdateRequest) {

            $scope.processRequest($scope.forceUpdateRequest, $('#confirmAddTrafficPolicyResult'), "强制保存设置", '成功保存设置');
        }
    };
    $scope.resetChanges = function () {
        window.location.reload();
    };
    $scope.validateControl = function (currentGroupVses, existingGroupVses) {
        var isnew = $scope.query.policyId == '' || $scope.query.policyId == undefined;

        var cVses = [];
        var cloneCurrentGroupVses = $.extend(true, {}, currentGroupVses);
        $.each(_.keys(cloneCurrentGroupVses), function (i, item) {
            cVses = cVses.concat(_.pluck(cloneCurrentGroupVses[item], 'vsId'));
        });
        cVses = _.unique(cVses);

        var sames = _.pluck($scope.sameVses, 'vsId');

        if (!isnew) {
            // edit
            if (cVses.length < sames.length) return false;

            var cs = _.filter(sames, function (item) {
                var v = _.find(cVses, function (t) {
                    return t == item;
                });
                if (!v) return true;
                else return false;
            });

            if (cs && cs.length > 0) return false;
            return true;
        } else {
            if (sames.length == 0) return true;

            var cs = _.filter(sames, function (item) {
                var v = _.find(cVses, function (t) {
                    return t == item;
                });
                if (v) return true;
                else return false;
            });

            if (cs && cs.length > 0) return true;
            return false;
        }
    };

    // Policy target area
    $scope.toggleTarget = function (target) {
        var k = target.key;
        if ($scope.selectedTarget.key == target.key) {
            $scope.selectedTarget = {
                key: ''
            };
        } else {
            $scope.selectedTarget = target;
            $('.targetwarning').removeClass('hide').addClass('hide');
        }
    };
    $scope.isSelectedTarget = function (target) {
        if ($scope.selectedTarget.key == target.key) {
            return "label-info";
        }
    };
    $scope.addTagsForGroups = function (groups, target) {
        $.each(groups, function (index, group) {
            var param = {
                type: 'group',
                pname: target,
                pvalue: 'true',
                targetId: group
            };
            var request = {
                method: 'GET',
                url: G.baseUrl + "/api/property/set",
                params: param
            };

            $http(request).success(function (code, response) {

            });
        });
    };

    var getTagsAndPolicyForPolicy = function (target, controls, vses, issoa) {
        // summarize the tags and properties
        var pname = 'target';
        var properties = [];
        var tags = [];

        properties.push({
            name: pname,
            value: target
        });

        properties.push(
            {
                name: 'chanel',
                value: 'policy-new'
            }
        );

        var idcs = _.map(vses, function (r) {
            var vsId = r['virtual-server'].id;
            var vs = $scope.vses[vsId];
            var u = _.find(vs.properties, function (t) {
                return t.name.toLowerCase() == 'idc';
            });
            if (u) return u.value;
            return '-';
        });
        idcs = _.uniq(idcs);

        idcs = idcs.join(',');
        properties.push({
            name: 'idc',
            value: idcs
        });

        var v = _.map(controls, function (item) {
            var id = item.group.id;
            var owner;
            var bu;

            var o = _.find($scope.groups[id].tags, function (y) {
                return y.toLowerCase().indexOf('owner_') != -1;
            });
            if (o) owner = o.substring(6, o.length);
            else owner = '-';

            var p = _.find($scope.groups[id].properties, function (z) {
                return z.name.toLowerCase() == 'sbu';
            });
            if (p) bu = p.value;
            else bu = '-';

            return {
                group: id,
                owner: owner,
                bu: bu
            }
        });
        var users = [];
        var bus = [];
        if (v && v.length != 0) {
            users = _.uniq(_.pluck(v, 'owner'));
            bus = _.uniq(_.pluck(v, 'bu'));
        }
        $.each(users, function (i, item) {
            if (item == '-') return;
            tags.push('owner_' + item);
        });

        $.each(bus, function (i, item) {
            if (item == '-') return;
            properties.push({
                name: 'SBU',
                value: item
            });
        });

        tags.push('user_' + $scope.loginUserInfo.name);

        if (issoa) {
            tags.push('soa');
        }


        return {
            tags: tags,
            properties: properties
        }
    };

    // Help methods Area
    $scope.getGroupPaths = function (group) {
        var paths = [];
        var c = 0;
        $.each(group['group-virtual-servers'], function (i, gVs) {
            var o = {
                path: gVs.path,
                priority: gVs.priority,
                vsId: gVs['virtual-server'].id,
                slbId: gVs['virtual-server']["slb-ids"],
                ssl: gVs['virtual-server'].ssl,
                domain: gVs['virtual-server'].domains[0] == undefined ? "" : gVs['virtual-server'].domains[0].name
            };
            paths.push(o);

            //Set path order
            if (c == 0) {
                if (o.priority >= 0) {
                    group.pathOrder = o.priority * 1000000 + group.id;
                } else {
                    group.pathOrder = o.priority * 1000000 - group.id;
                }
            }
            c++;
        });
        return paths;
    };
    $scope.getGroupIdc = function (group) {
        var c = _.find(group.properties, function (item) {
            return item.name == 'idc';
        });
        if (c) {
            return c.value;
        }
        return '-';
    };
    $scope.getGroupSameAndDiffrentVsPaths = function (all) {
        var result = {
            same: [],
            diffs: []
        };
        var keys = _.keys(all);
        var values = [];
        $.each(keys, function (i, key) {
            values = values.concat(all[key]);
        });
        var len = keys.length;

        if (len == 1) {
            $.each(values, function (i, item) {
                item.priority = item.priority + 1;
            });
            result.same = values;
        } else {
            var countByItems = _.countBy(values, function (item) {
                return item.vsId;
            });

            var vsIds = _.map(countByItems, function (num, key) {
                if (num == len) return key;
            });

            vsIds = _.reject(vsIds, function (item) {
                return item == undefined;
            });

            var diffs = _.difference(_.keys(countByItems), _.values(vsIds));
            $.each(diffs, function (i, item) {
                result.diffs.push({vsId: item});
            });
            if ($scope.diffVses && $scope.diffVses.length > 0) {
                var v = _.filter($scope.diffVses, function (r) {
                    var t = _.find(result.diffs, function (s) {
                        return s.vsId == r.vsId;
                    });
                    if (t) return false;
                    return true;
                });

                result.diffs = result.diffs.concat(v);
            }
            $.each(vsIds, function (i, item) {
                var v = _.filter(values, function (t) {
                    return t.vsId == item;
                });
                result.same.push(
                    {
                        vsId: item,
                        path: _.pluck(v, 'path'),
                        available: true,
                        priority: $scope.abstractVsPriority(v)
                    }
                );
            });
        }
        return result;
    };
    $scope.abstractVsPriority = function (array) {
        var result = _.max(_.pluck(array, 'priority')) + 1;
        return result;
    };
    $scope.abstractGroupVsPaths = function (group) {
        var groupvses = group['group-virtual-servers'];
        var vsPaths = _.map(groupvses, function (item) {
            var vsId = item['virtual-server'].id;
            //var setted = $('#path_vs_' + vsId).val();
            //var path = setted ? setted.trim() : item.path;
            return {
                'vsId': vsId,
                'path': item.path,
                priority: item['priority']
            }
        });
        return vsPaths;
    };
    $scope.showErrorMessage = function () {
        var show = false;
        if ($scope.error.code) {
            show = true;
        }
        return show;
    };
    $scope.getPostBody = function () {
        var body = $.extend(true, {}, $scope.trafficInfo);
        // delete those unusable properties
        $.each(body.controls, function (index, item) {
            delete item.group.name;
            delete item.group.app;
            delete item.group.idc;
            delete item.group.paths;
            delete item.status;
        });

        body['policy-virtual-servers'] = _.reject(body['policy-virtual-servers'], function (i) {
            var v = _.find($scope.diffVses, function (item) {
                return item.vsId == i['virtual-server'].id;
            });
            if (v) return true;
            else return false;
        });

        $.each(body['policy-virtual-servers'], function (i, item) {
            item.path = $('#path_vs_' + item['virtual-server'].id).val().trim();
            item.priority = $('#priority_vs_' + item['virtual-server'].id).val().trim();
        });

        var ts = _.countBy(body.controls, function (item) {
            var group = $scope.groups[item.group.id];
            var ps = group['tags'];
            if (ps.indexOf('soa') != -1) {
                return 'soa';
            } else {
                return 'site';
            }
        });
        var isSoa = false;
        if (ts['soa'] && ts['soa'] == body.controls.length) isSoa = true;

        var pvalue = $scope.selectedTarget.value;
        if (originTarget) {
            pvalue = originTarget;
        }
        var tagsandproperties = getTagsAndPolicyForPolicy(pvalue, body.controls, body['policy-virtual-servers'], isSoa);

        body.properties = tagsandproperties.properties;
        body.tags = tagsandproperties.tags;

        return body;
    };

    function reviewData(id) {
        var result = true;
        $.each(id.find('[data-validator-type="validation"]'), function (i, element) {
            if (!$(element).bootstrapValidation('validate'))
                result = false;
        });
        return result;
    };

    // Reload tables area
    $scope.reloadTable = function (policy, deletevs, addedvs) {
        var isnew = $scope.query.policyId == "" || $scope.query.policyId == undefined;

        var existingControls = policy.controls;
        var existingGroupVses = {};
        $.each(existingControls, function (i, item) {
            existingGroupVses[item.group.id] = $scope.abstractGroupVsPaths($scope.groups[item.group.id])
        });

        var t = $scope.getGroupSameAndDiffrentVsPaths(existingGroupVses);
        $scope.sameVses = t.same;

        if (deletevs) {
            var e = _.find($scope.diffVses, function (item) {
                return item.vsId == deletevs.vsId;
            });
            if (!e) $scope.diffVses.push(deletevs);
        } else {
            if (addedvs) {
                $scope.diffVses = _.reject($scope.diffVses, function (item) {
                    return item.vsId == addedvs.vsId;
                });
            } else {
                $scope.diffVses = t.diffs;
            }
        }

        // summarize the traffic policy groups and virtual servers
        var policyGroups = policy;
        $.each(policyGroups.controls, function (i, item) {
            var group = item.group;
            var id = group.id;
            var appId = $scope.groups[id]['app-id'];
            var appName = $scope.apps[appId] ? $scope.apps[appId]['chinese-name'] : '-';
            group.name = $scope.groups[id].name;
            group.type = $scope.groups[id].type;
            group.appId = $scope.groups[id]['app-id'];
            group.paths = $scope.getGroupPaths($scope.groups[id]);
            group.idc = $scope.getGroupIdc($scope.groups[id]);
            group.app = appId + '(' + appName + ')';
            var g = $scope.groups[id].properties;
            var v = _.find(g, function (r) {
                return r.name == 'status';
            });
            if (v) item.status = v.value.toLowerCase();
        });

        if (isnew) {
            var vs = [];
            $.each($scope.sameVses, function (i, item) {
                vs.push({
                    'virtual-server': {
                        id: item.vsId
                    },
                    path: item.path,
                    priority: item.priority
                });
            });
            policyGroups['policy-virtual-servers'] = vs;
        } else {
            var vs = [];
            $.each($scope.sameVses, function (i, item) {
                var f = _.find(policyGroups['policy-virtual-servers'], function (r) {
                    return r['virtual-server'].id == item.vsId;
                });
                if (!f) {
                    $scope.diffVses.push({
                        vsId: item.vsId,
                        path: item.path
                    });
                    policyGroups['policy-virtual-servers'].push(
                        {
                            'virtual-server': {
                                id: item.vsId
                            },
                            path: item.path,
                            priority: item.priority
                        }
                    );
                }
            });
        }
        // 从当前Groups相同的Vs里面找出不在当前Policy control里面的Vs
        var existing_vses = [];
        $.each(policyGroups['policy-virtual-servers'], function (index, item) {
            var itemavaliable = false;
            var available = _.find($scope.diffVses, function (v) {
                return v.vsId == item['virtual-server'].id;
            });
            if (!available) itemavaliable = true;
            existing_vses.push(
                {
                    vsId: item['virtual-server'].id,
                    path: item.path,
                    priority: item.priority,
                    available: itemavaliable
                }
            );
        });
        $('#vs-table').bootstrapTable("load", existing_vses);
        $('#controls-table').bootstrapTable("load", policyGroups.controls);

        $('#controls-table').bootstrapTable("hideLoading");
        $('#vs-table').bootstrapTable('hideLoading');
    };
    $scope.reloadGraphic = function (policy) {
        var policy_clone = $.extend({}, true, policy);

        var domains = [];
        var idc = '-';
        var pollicyname = '-';

        var diffs = $scope.diffVses;
        policy_clone['policy-virtual-servers'] = _.reject(policy_clone['policy-virtual-servers'], function (item) {
            var v = _.find(diffs, function (r) {
                return r.vsId == item['virtual-server'].id;
            });
            if (v == undefined) return false;
            return true;
        });
        var vses = policy_clone['policy-virtual-servers'];

        var result = _.map(vses, function (item) {
                var vsId = item['virtual-server'].id;
                var path = '-';
                if (_.isString(item.path)) path = item.path;

                if (path == '/' || path == '~* ^/') path = path;
                else {
                    // standard paths?
                    if (path.startsWith('~* ^/') && path.endsWith('($|/|\\?)')) {
                        path = path.substring(5, path.length - 8);
                    } else {
                        if (!path.endsWith('($|/|\\?)')) {
                            path = path.substring(5, path.length);
                        }
                    }
                }

                var vs = $scope.vses[vsId];
                if (vs) {
                    var p = _.find(vs.properties, function (r) {
                        return r.name == 'idc';
                    });
                    if (p) idc = p.value;

                    var protocol = vs.ssl ? 'https://' : 'http://';
                    domains = _.map(vs.domains, function (t) {
                        return protocol + t.name + '/' + path;
                    });
                }
                pollicyname = policy_clone.name;

                var totalWeight = _.reduce(_.pluck(policy_clone.controls, 'weight'), function (memo, num) {
                    return memo + num;
                });
                var groups = _.map(policy_clone.controls, function (s) {
                    var groupidc = '-';
                    var a = _.find($scope.groups[s.group.id].properties, function (t) {
                        return t.name == 'idc';
                    });
                    if (a) groupidc = a.value;

                    var appid = $scope.groups[s.group.id]['app-id'];

                    var props = _.indexBy($scope.groups[s.group.id].properties, function (v) {
                        return v.name.toLowerCase();
                    });

                    return {
                        idc: groupidc,
                        name: $scope.groups[s.group.id].name,
                        id: s.group.id,
                        language: props['language'] ? props['language'].value : '未知',
                        weight: T.getPercent(s.weight, totalWeight),
                        'app-id': appid,
                        'app-name': $scope.apps[appid] ? $scope.apps[appid]['chinese-name'] : '-'
                    };
                });

                return {
                    vs: {
                        id: vsId,
                        domains: domains,
                        idc: idc
                    },
                    policy: {
                        id: policy_clone.id || '-',
                        name: pollicyname
                    },
                    groups: groups
                }
            }
        );

        var canvas = $('.graphics .diagram');
        canvas.html('');
        var diagramOnly = true;
        var noOperation = true;
        var http = $http;
        var env = $scope.env;
        var scopegroups = $scope.groups;
        var scopeapps = $scope.apps;
        var dashboardUrl = G[G.env].urls.dashboard + "/data";

        /*   TrafficPolicyGraphics.draw(result, $('.graphics .diagram'));*/
        var c = {
            '+policy_clone.id+': policy_clone
        };
        userPolicyApp.drawListOfPolicyGraphics(c, result, http, env, scopegroups, scopeapps, dashboardUrl, function (id) {
            }, function (id) {
            },
            function (id) {
                window.open('/portal/policy/traffic#?env=' + env + '&policyId=' + id, '_blank');
            }, function (id) {
                window.open('/portal/policy/traffic#?env=' + env + '&policyId=' + id, '_blank');
            },
            canvas,
            diagramOnly,
            noOperation
        );

    };
    // Load Data area
    var originTarget;
    $scope.loadData = function () {
        $scope.selectedTarget = {
            key: ''
        };
        $scope.policyTargets = [
            {
                key: 'dotnet2java',
                value: '.NET 转 Java'
            },
            {
                key: 'appmigration',
                value: '应用迁移'
            },
            {
                key: 'bigservice',
                value: '大服务拆分'
            },
            {
                key: 'versiontest',
                value: '多版本测试'
            },
            {
                key: 'vmtodocker',
                value: 'VM转容器'
            },
            {
                key: 'others',
                value: '其它'
            }
        ];
        var groupfrom = $scope.query.fromId;
        var isnew = $scope.query.policyId == undefined || $scope.query.policyId == '';
        $scope.error = {
            code: '',
            message: ''
        };
        var groupsRequest = {
            url: G.baseUrl + '/api/groups?type=extended&groupType=all',
            method: 'GET'
        };
        var vsesRequest = {
            url: G.baseUrl + '/api/vses?type=extended',
            method: 'GET'
        };
        var appsRequest = {
            method: 'GET',
            url: G.baseUrl + '/api/apps'
        };
        var loginUserRequest = {
            method: 'GET',
            url: G.baseUrl + '/api/auth/current/user'
        };
        $q.all([
            $http(appsRequest).success(
                function (response) {
                    if (response['apps']) {
                        $scope.apps = _.indexBy(response['apps'], function (item) {
                            return item['app-id'];
                        });
                    }
                }
            ),

            $http(groupsRequest).success(
                function (response, code) {
                    if (code == 200) {
                        var groups = response['groups'];
                        $scope.groups = _.indexBy(groups, 'id');
                    }
                }
            ),
            $http(vsesRequest).success(
                function (response, code) {
                    if (code == 200) {
                        var vses = response['virtual-servers'];
                        $scope.vses = _.indexBy(vses, 'id');
                    }
                }
            ),
            $http(loginUserRequest).success(
                function (response, code) {
                    if (code == 200) {
                        $scope.loginUserInfo = response;
                    }
                }
            )
        ]).then(
            function () {
                if (isnew) {
                    $scope.trafficInfo = {
                        "name": "",
                        "controls": [],
                        "policy-virtual-servers": []
                    };
                    if (groupfrom != undefined && groupfrom != '') {
                        // create from an existing group
                        $scope.trafficInfo.controls.push({
                            group: {
                                id: parseInt(groupfrom)
                            },
                            weight: 50
                        });

                        var vsesExisting = _.map($scope.groups[groupfrom]['group-virtual-servers'], function (item) {
                            return {
                                'virtual-server': {
                                    id: item['virtual-server'].id
                                },
                                path: item.path,
                                priority: item.priority
                            }
                        });
                        $scope.trafficInfo['policy-virtual-servers'] = vsesExisting;

                        $scope.reloadTable($scope.trafficInfo);
                        $scope.reloadGraphic($scope.trafficInfo);
                    }
                    $('#controls-table').bootstrapTable("hideLoading");
                    $('#vs-table').bootstrapTable('hideLoading');
                } else {
                    var policyRequest = {
                        url: G.baseUrl + '/api/policy?policyId=' + $scope.query.policyId + '&type=extended',
                        method: 'GET'
                    };
                    $http(policyRequest).success(
                        function (response, code) {
                            if (code == 200) {
                                var t;
                                var v = _.find(response.properties, function (r) {
                                    return r.name == 'target';
                                });
                                if (v) {
                                    if (supportedV[v.value]) {
                                        t = supportedV[v.value];
                                    } else {
                                        originTarget = v.value;
                                        t = '其它';
                                    }
                                }

                                if (t) {
                                    $scope.selectedTarget = _.find($scope.policyTargets, function (r) {
                                        return r.value == t;
                                    });
                                } else {
                                    $scope.selectedTarget = _.find($scope.policyTargets, function (r) {
                                        return r.value == '其它';
                                    });
                                }
                                delete response.properties;
                                $scope.trafficInfo = response;

                                $scope.reloadTable(response);
                                $scope.reloadGraphic(response);
                            }
                        }
                    );
                }
            }
        );
    };
    $scope.initTable = function () {
        var resource = $scope.resource;
        var controltable = resource['policynew']['policynew_trafficEditApp_group']['table'];
        var idtitle = controltable['groupid'];
        var nametitle = controltable['groupname'];
        var apptitle = controltable['app'];
        var pathtitle = controltable['path'];
        var idctitle = controltable['idc'];
        var weighttitle = controltable['weight'];
        var statustitle = controltable['status'];
        var operationtitle = controltable['operation'];
        var loadingtitle = controltable['loading'];
        var nodatatitle = controltable['nodata'];
        var statusmapp = resource['policynew']['policynew_trafficEditApp_status'];
        $('#controls-table').bootstrapTable({
            toolbar: "#toolbar",
            columns: [[
                {
                    field: 'group',
                    title: idtitle,
                    align: 'left',
                    valign: 'middle',
                    formatter: function (value, row, index) {
                        var uri = value.type === 'VGROUP' ? '/portal/vgroup' : '/portal/group';
                        return '<a target="blank" href="' + uri + '#?env=' + $scope.env + '&groupId=' + value.id + '">' + value.id + '</a>';
                    }
                },
                {
                    field: 'group',
                    title: nametitle,
                    align: 'left',
                    width: '500px',
                    resetWidth: true,
                    valign: 'middle',
                    formatter: function (value, row, index) {
                        var uri = value.type === 'VGROUP' ? '/portal/vgroup' : '/portal/group';
                        return '<a style="word-break:break-all" target="blank" href="' + uri + '#?env=' + $scope.env + '&groupId=' + value.id + '">' + value.name + '</a>';
                    }
                },
                {
                    field: 'group',
                    title: apptitle,

                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    formatter: function (value, row, index) {
                        return '<a target="blank" href="/portal/app#?env=' + G.env + '&appId=' + value.appId + '">' + value.app + '</a>';
                    }
                },
                {
                    field: 'group',
                    title: pathtitle,
                    width: '700px',
                    align: 'left',
                    valign: 'middle',
                    formatter: function (value, row, index) {
                        value = value.paths;
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
                                    "<div class='col-md-5' style='padding:0;line-height:34px;white-space: nowrap;overflow:hidden;color:#196eaa;font-weight:bold;' title='" + v.path + "'>" +
                                    pathStr +
                                    "</div>" +
                                    '<div class="col-md-7" style="padding:0;line-height:34px;">' +
                                    '<div class="col-md-3" style="padding:0;line-height:34px;">' + priorityStr + '</div>' +
                                    '<div class="col-md-2" style="padding:0;line-height:34px;">' + sslStr + '</div>' +
                                    '<div class="col-md-4" style="padding:0;line-height:34px;">' + vsIdStr + '</div>' +
                                    '<div class="col-md-3" style="padding:0;line-height:34px;">' + slbIdStr + '</div>' +
                                    '</div>';
                                // if current vs path is not common in each groups highlight it
                                var linethroughcss = '';
                                if ($scope.diffVses && $scope.diffVses.length > 0) {
                                    var illegal = _.find($scope.diffVses, function (item) {
                                        return item['vsId'] == v.vsId;
                                    });
                                    if (illegal) {
                                        linethroughcss = 'diff-div';
                                    }
                                }
                                result = result + '<div class="row ' + linethroughcss + '"' + ' style="margin:0;">' + tempStr + '</div>';
                            }
                        });

                        result = '<div class="row" style="margin:0">' + result + '</div>';
                        return result;
                    },
                    sortable: true
                },
                {
                    field: 'group',
                    title: idctitle,
                    align: 'left',
                    valign: 'middle',
                    width: '200px',
                    events: operateEvents,
                    formatter: function (value, row, index) {
                        return value.idc;
                    }
                },
                {
                    field: 'weight',
                    title: weighttitle,
                    align: 'center',
                    valign: 'middle',
                    events: operateEvents,
                    formatter: function (value, row, index) {
                        return '<input id="group_' + row.group.id + '" type="text" class="form-group weight-control"  value="' + value + '" />';
                    }
                },
                {
                    field: 'status',
                    title: statustitle,
                    align: 'left',
                    valign: 'middle',
                    width: '200px',
                    events: operateEvents,
                    formatter: function (value, row, index) {
                        value = value.toLowerCase();
                        var str = '';
                        switch (value) {
                            case "deactivated":
                                str = "<span class='status-gray'>" + statusmapp['未激活'] + "</span>";
                                break;
                            case "activated":
                                str = "<span class='status-green'>" + statusmapp['已激活'] + "</span>";
                                break;
                            case "tobeactivated":
                                str = "<span class='diffGroup status-yellow'>" + statusmapp['有变更'] + "(<a class=''>Diff</a>)</span>";
                                break;
                            default:
                                str = "-";
                                break;
                        }
                        return str;
                    },
                    sortable: true
                },
                {
                    field: 'Operation',
                    title: operationtitle,
                    align: 'center',
                    events: operateEvents,
                    valign: 'middle',
                    formatter: function (value, row, index) {
                        var isnew = $scope.query.policyId == '' || $scope.query.policyId == undefined;
                        var len = isnew ? 0 : 1;
                        var candelete = $('#controls-table').data()['bootstrap.table'].data.length > len;
                        if (candelete) {
                            return '<button type="button" class=" remove-group btn btn-info waves-effect waves-light" aria-label="Left Align"><span class="fa fa-minus"></span></button>';
                        } else {
                            return '-';
                        }
                    }
                }], []],
            classes: "table-bordered  table-hover table-striped table",
            showRefresh: true,
            search: true,
            showRefresh: false,
            showColumns: true,
            minimumCountColumns: 2,
            pagination: false,
            pageSize: 20,
            resizable: true,
            resizeMode: 'overflow',
            sidePagination: 'client',
            pageList: [20, 40, 80, 200],
            responseHandler: "responseHandler",
            formatLoadingMessage: function () {
                return "<img class='loading-img' src='/static/img/loading_100.gif' /> " + loadingtitle;
            },
            formatNoMatches: function () {
                return '<i class="fa status-alert fa-exclamation-triangle" aria-hidden="true"></i> ' + nodatatitle;
            }
        });


        var vstable=resource['policynew']['policynew_trafficEditApp_preview']['table'];
        var vspathtitle = vstable['path'];
        var vsidtitle = vstable['vs'];
        var prioritytitle = vstable['priority'];
        var operationtitle = vstable['operation'];

        var loadingtitlevs = vstable['loading'];
        var loadingtitlevsnodata = vstable['nodata'];
        $('#vs-table').bootstrapTable({
            toolbar: "#toolbar2",
            columns: [[
                {
                    title: vspathtitle,
                    field: 'path',
                    align: 'left',
                    width: '400px',
                    valign: 'middle',
                    events: operateEvents,
                    formatter: function (value, row, index) {
                        var disabled = '';
                        if (row.available != undefined && row.available == false) disabled = 'disabled';

                        var v;
                        if (_.isString(value)) v = value;
                        else {
                            v = $('#path_vs_' + row.vsId).val();
                        }

                        if (v) {
                            return "<input " + disabled + " id='path_vs_" + row.vsId + "'" + "class='form-control path-validate' placeholder='请输入期望的PATH' type='text' value='" + v + "' data-validator-type='validation' data-validator='required' />";
                        } else {
                            return '<input ' + disabled + ' id="path_vs_' + row.vsId + '" class="form-control path-validate" type="text" placeholder="请输入期望的PATH" data-validator-type="validation" data-validator="required" />';
                        }
                    }
                },
                {
                    title: vsidtitle,
                    field: 'vsId',
                    align: 'left',
                    resetWidth: true,
                    valign: 'middle',
                    formatter: function (value, row, index) {
                        var currentClass = '';
                        if (row.available != undefined && row.available == false) currentClass = 'diff-item';
                        return '<a class="path-main-item ' + currentClass + '" href="/portal/vs?#env=' + G.env + '&vsId=' + value + '">' + value + '(' + $scope.vses[value].name + ')</a>';
                    }
                },
                {
                    field: 'priority',
                    title: prioritytitle,
                    align: 'left',
                    valign: 'middle',
                    events: operateEvents,
                    formatter: function (value, row, index) {
                        var currentClass = '';
                        if (row.available != undefined && row.available == false) currentClass = 'diff-item';
                        return '<input data-validator-type="validation" data-validator="required" id="priority_vs_' + row.vsId + '" class="form-control priority-validate"  value="' + parseInt(value) + '"/>'
                    }
                },
                {
                    title: operationtitle,
                    field: 'Operation',
                    align: 'center',
                    events: operateEvents,
                    valign: 'middle',
                    formatter: function (value, row, index) {
                        var str = '';
                        var hasadding = false;
                        if (row.available != undefined && row.available == false) {
                            str += '<button style="margin-right: 5px" title="revert back" type="button" class=" add-vs btn btn-info waves-effect waves-light" aria-label="Left Align"><span class="fa fa-plus"></span></button>';
                            hasadding = true;
                        }
                        if ($scope.sameVses.length - $scope.diffVses.length < 1) {
                            if (!hasadding) {
                                str += '-';
                            }
                        } else {
                            str += '<button title="remove" type="button" class=" remove-vs btn btn-info waves-effect waves-light" aria-label="Left Align"><span class="fa fa-minus"></span></button>';
                        }
                        return str;
                    }
                }], []],
            sortName: 'id',
            sortOrder: 'desc',
            data: $scope.sameVses,
            classes: "table-bordered  table-hover table-striped table",
            showRefresh: false,
            minimumCountColumns: 2,
            idField: 'id',
            pageSize: 20,
            pagination: false,
            resizeMode: 'overflow',
            sidePagination: 'client',
            pageList: [20, 40, 80, 200],
            responseHandler: "responseHandler",
            formatLoadingMessage: function () {
                return "<img class='loading-img' src='/static/img/loading_100.gif' /> "+loadingtitlevs;
            },
            formatNoMatches: function () {
                return '<i class="fa status-alert fa-exclamation-triangle" aria-hidden="true"></i> '+loadingtitlevsnodata;
            }
        });
        $('#suggest-groups').bootstrapTable({
            toolbar: '#suggest-toolbar',
            columns: [[{
                field: 'state',
                radio: true,
                rowspan: 2,
                align: 'center',
                valign: 'middle'
            },
                {
                    field: 'id',
                    title: 'ID',
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    formatter: function (value, row, index) {
                        return value;
                    }
                },
                {
                    field: 'name',
                    title: 'Name',
                    align: 'left',
                    resetWidth: true,
                    valign: 'middle',
                    width: '400px',
                    sortable: true,
                    formatter: function (value, row, index) {
                        return value;
                    }
                },
                {
                    field: 'paths',
                    title: 'Path | Priority | SSL | VS | SLB',
                    width: '800px',
                    align: 'left',
                    valign: 'middle',
                    sortName: 'pathOrder',
                    sortable: true,
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
                    field: 'app',
                    title: 'App',
                    align: 'left',
                    valign: 'middle',
                    width: '500px',
                    sortable: true,
                    formatter: function (value, row, index) {
                        return value;
                    }
                },
                {
                    field: 'group-servers',
                    title: 'Members',
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
                    field: 'status',
                    title: 'Status',
                    align: 'left',
                    valign: 'middle',
                    width: '200px',
                    events: operateEvents,
                    formatter: function (value, row, index) {
                        var str = "";
                        switch (value) {
                            case "deactivated":
                                str = "<span class='status-gray'>未激活</span>";
                                break;
                            case "activated":
                                str = "<span class='status-green'>已激活</span>";
                                break;
                            case "toBeActivated":
                                str = "<span class='diffGroup status-yellow'>有变更(<a class=''>Diff</a>)</span>";
                                break;
                            default:
                                str = "-";
                                break;
                        }
                        return str;
                    },
                    sortable: true
                },
                {
                    field: 'weight',
                    title: 'Weight',
                    align: 'left',
                    valign: 'middle',
                    formatter: function (value, row, index) {
                        return '<input type="text" class="form-control weight-control" value="' + value + '"/>';
                    },
                    sortable: true
                }
            ], []],
            classes: "table-bordered  table-hover table-striped table",
            minimumCountColumns: 2,
            showRefresh: true,
            search: true,
            showColumns: true,
            pagination: true,
            idField: 'id',
            pageSize: 20,
            resizable: true,
            resizeMode: 'overflow',
            sidePagination: 'client',
            pageList: [20, 40, 80, 200],
            responseHandler: "responseHandler",
            formatLoadingMessage: function () {
                return "<img class='loading-img' src='/static/img/loading_100.gif' /> 正在加载 Groups";
            },
            formatNoMatches: function () {
                return '<i class="fa status-alert fa-exclamation-triangle" aria-hidden="true"></i> 没有找到匹配的 Groups';
            }
        });
    };
    $('#suggest-groups').on('check.bs.table uncheck.bs.table', function () {
        var row = $('#suggest-groups').bootstrapTable('getSelections');
        $scope.query.groupid = row[0].id;
    });

    window.operateEvents = {
        'click .diffGroup': function (e, value, row, index) {
            getGroupDataByVersion(row);
        },
        'blur .priority-validate': function (e, value, row, index) {
            var vsId = row.vsId;
            var oldvalue = parseInt(row.priority);
            var newvalue = $('#priority_vs_' + vsId).val();

            try {
                newvalue = parseInt(newvalue);
                if (newvalue - oldvalue < 0) {
                    alert(alert['Policy的Virtual Server Priority 和 Groups的最大Priority至少要高100!否则不允许创建']);
                    return;
                }
            } catch (ex) {
                alert(alert['格式不正确！']);
                return;
            }
        },
        'blur .path-validate': function (e, value, row, index) {
            var vsId = row.vsId;
            var newValue = $('#path_vs_' + vsId).val();
            var old = _.find($scope.trafficInfo['policy-virtual-servers'], function (item) {
                return item['virtual-server'].id == vsId;
            });
            if (newValue == old.path) return;

            else {
                if (newValue == undefined) return

                old.path = newValue;
                $scope.reloadGraphic($scope.trafficInfo);
            }
        },
        'blur .weight-control': function (e, value, row, index) {
            var groupId = row.group.id;
            var newValue = $('#group_' + groupId).val();
            var old = _.find($scope.trafficInfo.controls, function (item) {
                return item.group.id == groupId;
            });
            var w = parseInt(newValue);
            if (isNaN(w)) return;

            old.weight = w;
            $scope.reloadGraphic($scope.trafficInfo);
        },
        'click .remove-group': function (e, value, row, index) {
            var groupId = row.group.id;
            $scope.trafficInfo.controls = _.reject($scope.trafficInfo.controls, function (item) {
                return item.group.id == groupId;
            });
            $scope.reloadTable($scope.trafficInfo);
            $scope.reloadGraphic($scope.trafficInfo);
        },
        'click .remove-vs': function (e, value, row, index) {
            var vsId = row.vsId;

            $scope.trafficInfo['policy-virtual-servers'] = _.reject($scope.trafficInfo['policy-virtual-servers'], function (item) {
                return item['virtual-server'].id == vsId;
            });
            $scope.reloadTable($scope.trafficInfo, {vsId: vsId});
            $scope.reloadGraphic($scope.trafficInfo);
            $scope.trafficInfo['policy-virtual-servers'] = _.reject($scope.trafficInfo['policy-virtual-servers'], function (item) {
                return item['virtual-server'].id == vsId;
            });
        },
        'click .add-vs': function (e, value, row, index) {

            var priority = row.priority;
            var vsId = row.vsId;
            var f = _.find($scope.trafficInfo['policy-virtual-servers'], function (r) {
                return r['virtual-server'].id == vsId;
            });
            if (!f) {
                $scope.trafficInfo['policy-virtual-servers'].push(
                    {
                        'virtual-server': {
                            id: vsId
                        },
                        path: $('#path_vs_' + vsId).val(),
                        priority: priority
                    }
                );
            }
            $scope.reloadTable($scope.trafficInfo, undefined, {vsId: vsId});
            $scope.reloadGraphic($scope.trafficInfo);
        }
    };

    $scope.policyNameChanged = function () {
        $scope.reloadGraphic($scope.trafficInfo);
    };
    $scope.hashChanged = function (hashData) { $scope.resource = H.resource;
        $scope.resource = H.resource;
        alert = $scope.resource['policynew']['policynew_trafficEditApp_alert'];
        $scope.suggestGroups = [];
        $scope.env = 'pro';
        if ($scope.env != hashData.env) {
            $scope.env = hashData.env;
        }

        var resourcefile = H.resource['policynew']['policynew_trafficEditApp_diagram'];

        userPolicyApp = new UserPolicyApp(hashData, $http, $q, $scope.env, resourcefile);
        if (hashData.policyId) {
            $scope.query.policyId = hashData.policyId;
        }
        if (hashData.groupId) {
            $scope.query.fromId = hashData.groupId;
        }

        $scope.forceUpdateRequest = {};
        $scope.showGroupsError = false;
        $scope.loadData();
        $scope.initTable();
        $('#controls-table').bootstrapTable("showLoading");
        $('#vs-table').bootstrapTable('showLoading');
    };
    H.addListener("trafficEditApp", $scope, $scope.hashChanged);
    $scope.showForceUpdate = false;
    $scope.forceUpdateRequest = {};
    $scope.processRequest = function (request, confirmDialog, operationText, tooltipText) {
        $scope.forceUpdateRequest = $.extend(true, {}, request);
        if (!$scope.forceUpdateRequest.params) {
            $scope.forceUpdateRequest.params = {force: true};
        } else {
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
                    $('.policy-id-text').text(res.id);
                    confirmDialog.modal('show').find(".modal-title").html(successText);
                    if (tooltipText) confirmDialog.modal('show').find(".modal-body").html(tooltipText);
                    // add tag for groups related to this policy
                    var groups = _.map(request.data.controls, function (v) {
                        return v.group.id;
                    });
                    var pvalue = $scope.selectedTarget.value;
                    $scope.addTagsForGroups(groups, pvalue);
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

    function getGroupDataByVersion(row) {

        var resource = $scope.resource;

        var m = resource['policynew']['policynew_trafficEditApp_diffVSDiv'];

        var baseText = '';
        var NewText = '';
        var groupId;

        if (row.group) {
            groupId = row.group.id;
        } else {
            groupId = row.id;
        }
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
                $scope.confirmActivateText = m['线上版本与当前版本比较'];
                var target = document.getElementById('fileDiffForm1');
                NewText = JSON.stringify(U.sortObjectFileds($scope.tobeActivatedGroupData), null, "\t");
                baseText = JSON.stringify(U.sortObjectFileds($scope.onlineGroupData), null, "\t");
                var ptext = m['offline'] + ($scope.onlineGroupData ? $scope.onlineGroupData.version : '-');
                var ctext = m['online'] + $scope.tobeActivatedGroupData.version;
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

    $('.closeProgressWindowBt').click(function (e) {
        if ($('.policy-id-text').text() == '') {
            return;
        }
        window.location.href = "/portal/policy" + "#?env=" + G.env + "&policyId=" + $('.policy-id-text').text();
    });

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
angular.bootstrap(document.getElementById("traffic-edit-area"), ['trafficEditApp']);
