/**
 * Created by ygshen on 2017/5/16.
 */
function UserPolicyApp(hashData, $http, $q, env, resourcefile) {
    this.hashData = hashData;
    this.$q = $q;
    this.env = env;
    // Dependency Service
    this.paramService = new ParamService(hashData);
    this.requestService = new ApiService($http);
    this.queryService = new QueryService();
    this.resourcefile = resourcefile;
    if (env) {
        this.esService = new ESService(G[env].urls.es);
    }
};


UserPolicyApp.prototype = new App();

UserPolicyApp.prototype.getEsHtml = function (query) {
    var esLink = this.esService.getESLink(this.env, query);
    if (esLink == '-') {
        return '<div class="system-link">' +
            '<span class="pull-left">-</span>' +
            '</div>';
    }

    return '<div class="system-link">' +
        '<a class="pull-left es" title="ES" target="_blank" href="' + esLink + '">ES</a>' +
        '</div>';
};


UserPolicyApp.prototype.getFilterData = function (datasets, orderKey, filter) {
    var self = this.queryService;
    return self.get(datasets, 'filter', orderKey, '', filter);
};

// Query array
UserPolicyApp.prototype.getSystemData = function (vsesString, groupsString, appString, result) {
    var self = this;

    var collection = [
        this.sendRequest({
            method: 'GET',
            url: vsesString
        }, function (res, code) {

            result.vses = self.getAllData(res['virtual-servers'], 'id');
        }),
        this.sendRequest({
            method: 'GET',
            url: groupsString
        }, function (res, code) {
            result.groups = self.getAllData(res['groups'], 'id');
        }),
        this.sendRequest({
            method: 'GET',
            url: appString
        }, function (res, code) {
            result.apps = self.getAllData(res['apps'], 'app-id');
        })
    ];

    return collection;
};
UserPolicyApp.prototype.getPolicyCollection = function (trafficsFocusedString, trafficsOwnerString, existingPolicyString, result) {
    var self = this;

    var queryCollection = [
        this.sendRequest({
            method: 'GET',
            url: trafficsFocusedString
        }, function (res, code) {

            result.focusedTraffics = self.getAllData(res['traffic-policies'], 'id');
        }),
        this.sendRequest({
            method: 'GET',
            url: trafficsOwnerString
        }, function (res, code) {
            result.ownedTraffics = self.getAllData(res['traffic-policies'], 'id');
        }),
        this.sendRequest({
            method: 'GET',
            url: existingPolicyString
        }, function (res, code) {
            result.existingPolicies = self.getAllData(res['traffic-policies'], 'id');
        })
    ];

    return queryCollection;
};

// Data Process
UserPolicyApp.prototype.getSummaryData = function (data) {

    var countItem = _.countBy(data, function (item) {
        var v = _.find(item.properties, function (item2) {
            return item2.name == 'status';
        });
        if (v) return v.value.toLowerCase();
        else return 'unknown';
    });
    var activateCount = countItem['activated'] || 0;
    var deactivateCount = countItem['deactivated'] || 0;
    var changedCount = countItem['tobeactivated'] || 0;

    var text = this.resourcefile["共有 "] + _.keys(data).length + this.resourcefile[" 个 策略'"];

    return {
        text: text,
        activate: activateCount,
        deactivate: deactivateCount,
        tobeactivated: changedCount
    };
};
UserPolicyApp.prototype.getGraphicData = function (policy, scopeVses, scopeGroups, scopeApps) {
    var policy_clone = $.extend({}, true, policy);
    var domains = [];
    var idc = '-';
    var pollicyname = '-';
    var vses = policy_clone['policy-virtual-servers'];

    var pId = policy.id;
    var canActivatePolicy = A.canDo('Policy', 'ACTIVATE', pId);
    var canUpdatePolicy = A.canDo('Policy', 'UPDATE', pId);


    var result = _.map(vses, function (item) {
            var vsId = item['virtual-server'].id;
            var path = item.path.trim();
            if (path == '/' || path == '~* ^/') path = path;
            else {
                // standard paths?
                if (path.startsWith('~* ^/') && path.endsWith('($|/|\\?)')) {
                    path = path.substring(5, path.length - 8);
                } else {
                    if (!path.endsWith('($|/|\\?)')) {
                        path = path.substring(5, path.length);
                    } else {
                        path = path.substring(0, path.length - 8);
                    }
                }
            }
            var slbops = [];
            var soaops = [];
            var reg = /(.*)\/(\(xml\|json\|bjjson\|x-protobuf\))\/(.*)/g;
            var m = reg.exec(path);
            if (m) {
                var s = m[1];
                var t = m[3];
                if (s) {
                    path = s;
                }
                if (t) {
                    var l = t.length;
                    var start = t[0] == '(' ? 1 : 0;
                    var end = start == 0 ? l : l - 1;
                    soaops = t.substring(start, end).split('|');
                }
            }

            var reg2 = /(.*)\/\((.*)\)/g
            var n = reg2.exec(path);
            if (n) {
                var u = n[1];
                var v = n[2];
                if (u) path = u;
                if (v) slbops = v.split('|');
            }

            var vs = scopeVses[vsId];
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
            var groups_temp = _.map(policy_clone.controls, function (s) {
                var groupidc = '-';
                var a = _.find(scopeGroups[s.group.id].properties, function (t) {
                    return t.name == 'idc';
                });
                if (a) groupidc = a.value;

                var grouplanguage = '-';
                var b = _.find(scopeGroups[s.group.id].properties, function (t) {
                    return t.name == 'language';
                });
                if (b) grouplanguage = b.value;

                var appid = scopeGroups[s.group.id]['app-id'];
                return {
                    name: scopeGroups[s.group.id].name,
                    id: s.group.id,
                    weight: T.getPercent(s.weight, totalWeight),
                    idc: groupidc,
                    language: grouplanguage,
                    'app-id': appid,
                    'app-name': scopeApps[appid] ? scopeApps[appid]['chinese-name'] : '-'
                };
            });

            var canDeactivateNetGroup = false;
            var netGroupItem = _.find(groups_temp, function (v) {
                return v.language == '.NET';
            });
            if (netGroupItem) {
                canDeactivateNetGroup = A.canDo('Group', 'DEACTIVATE', netGroupItem.id);
            }

            return {
                vs: {
                    id: vsId,
                    domains: domains,
                    idc: idc,
                    soaops: soaops,
                    slbops: slbops
                },
                policy: {
                    id: policy.id,
                    name: pollicyname
                },
                rights: {
                    canActivatePolicy: canActivatePolicy,
                    canUpdatePolicy: canUpdatePolicy,
                    canDeactivateNetGroup: canDeactivateNetGroup
                },
                groups: groups_temp
            }
        }
    );
    return result;
};

// New Policy
UserPolicyApp.prototype.getGroupsByAppId = function (request, env, apps, policies, outer) {
    var diagramresource = this.resourcefile;
    var self = this;
    var send = this.sendRequest(request, function (res, code) {

        var groups = _.values(self.getAllData(res['groups'], 'id'));
        var data = _.map(groups, function (item) {
            var appId = item['app-id'];
            var app = appId + '(' + apps[appId]['chinese-name'] + ')';
            var id = item.id;
            var name = item.name;
            var javailable = false;
            var ravailable = false;
            var savailable = false;
            var pavailable = false;
            var vsavailable = false;

            var idcItem = _.find(item.properties, function (v) {
                return v.name == 'idc';
            });
            var idc = idcItem ? idcItem.value : '-';

            var statusItem = _.find(item.properties, function (v) {
                return v.name == 'status' && v.value.toLowerCase() == 'activated';
            });
            if (statusItem) savailable = true;

            var languageItem = _.find(item.properties, function (v) {
                return v.name.toLowerCase() == 'language' && v.value.toLowerCase() == 'java';
            });
            if (languageItem) javailable = true;

            var relatedItem = _.find(item.properties, function (m) {
                return m.name.toLowerCase() == 'relatedappid'/* && $scope.apps[m.value]*/;
            });
            if (relatedItem) ravailable = true;

            var hasPolicy = _.find(policies, function (b) {
                var gs = _.map(b.controls, function (d) {
                    return d.group.id;
                });
                if (gs.indexOf(id) != -1) return true;
                return false;
            });
            if (!hasPolicy) pavailable = true;

            var groupvses = _.indexBy(item['group-virtual-servers'], function (v) {
                return v.path;
            });
            vsavailable = _.keys(groupvses).length == 1;

            var errorMsg = '';

            if (!(javailable && ravailable && savailable && pavailable && vsavailable)) {
                if (!javailable) errorMsg = diagramresource['当前Group不是Java应用，不可用于.NET转Java.请选择一个Java应用开始一次新的流量切换'];
                else if (!ravailable) errorMsg = diagramresource['当前Group找不到相关联的.NET应用，请联系OPS Team添加关联'];
                else if (!savailable) errorMsg = diagramresource['当前Group尚未激活，请联系OPS Team激活后再创建.net转Java应用'];
                else if (!pavailable) errorMsg = diagramresource["当前Group已经创建过"] + '<a href="/portal/policy#?env=' + env + '&policyId=' + hasPolicy.id + '">' + diagramresource['.NET转Java应用'] + '</a>' + diagramresource['不能再创建了!'];
                else if (!vsavailable) errorMsg = diagramresource['当前Group存在多个Virtual Servers，但是多个Virtual Server之间的Path不相同,请联系OPS Team统一Path'];
            }
            return {
                appId: appId,
                app: app,
                id: id,
                name: name,
                idc: idc,
                status: status,
                available: javailable && ravailable && savailable && pavailable && vsavailable,
                error: errorMsg,
                order: (javailable && ravailable && savailable && pavailable) ? 0 : 1
            }
        });
        var s = _.sortBy(data, 'order');
        outer(s);
    });
    return [send];
};


// Deactivate policy
UserPolicyApp.prototype.getPolicyByAppId = function (request, appid, apps, groups, righturl, outer) {
    var diagramresource = this.resourcefile;
    var self = this;
    var send = this.sendRequest(request, function (res, code) {
        var queryService = new QueryService();
        self.queryService = queryService;

        var appIdGroups = _.filter(_.values(groups), function (t) {
            return t['app-id'] == appid;
        });
        appIdGroups = _.pluck(appIdGroups, 'id');

        var policies = self.getFilterData(res['traffic-policies'], 'id', function () {
            var c = _.values(this);
            return _.filter(c, function (item) {
                var itemGroups = _.map(item.controls, function (d) {
                    return d.group.id;
                });
                var same = _.intersection(itemGroups, appIdGroups);
                if (same && same.length > 0) return true;
                return false;
            });
        });

        var policyVses = _.map(policies, function (item) {
            var vses = _.map(item['policy-virtual-servers'], function (v) {
                return v['virtual-server'].id;
            });
            return vses;
        });

        policyVses = _.flatten(policyVses);

        var policygroups = _.map(policies, function (item) {
            var controls = _.indexBy(item.controls, function (v) {
                return v.group.id;
            });
            return _.keys(controls);
        });

        var allgroups = [];
        $.each(policygroups, function (index, indexVale) {
            allgroups = allgroups.concat(indexVale);
        });
        allgroups = _.uniq(allgroups);

        var groupapp = _.map(allgroups, function (i) {
            var relate = undefined;
            var ps = _.indexBy(groups[i].properties, function (v) {
                return v.name.toLowerCase();
            });
            if (ps['relatedappid']) relate = ps['relatedappid'].value;

            var status = '-';
            if (ps['status']) status = ps['status'].value;

            var idc = '-';
            if (ps['idc']) idc = ps['idc'].value;

            var bu = '-';
            if (ps['sbu']) bu = ps['sbu'].value;

            var language = '-';
            if (ps['language']) language = ps['language'].value;

            var tags = groups[i].tags;

            var owners = [];
            owners = _.map(tags, function (k) {
                k = k.toLowerCase();
                if (k.startsWith('owner_')) return k.substring(6, k.length);
            });
            owners = _.reject(_.uniq(owners), function (item) {
                return item == 'unknown';
            });


            return {
                groupid: groups[i].id,
                groupname: groups[i].name,
                relatedappid: relate,
                status: status,
                idc: idc,
                bu: bu,
                owners: owners,
                language: language,
                appid: groups[i]['app-id'],
                appname: apps[groups[i]['app-id']]['chinese-name']
            }
        });

        // {1:{},2:{}...}
        groupapp = _.object(allgroups, groupapp);


        var t = _.map(policies, function (item) {
            var name = item.name;
            var id = item.id;
            var apps = _.map(item.controls, function (i) {
                var g = i.group.id;
                var r = groupapp[g];
                r.weight = i.weight;
                return r;
            });

            var bu = '-';
            var owner = '-';
            var status = '-';

            var ps = _.indexBy(item.properties, function (j) {
                return j.name.toLowerCase();
            });
            var tags = item.tags;

            bu = ps['sbu'] ? ps['sbu'].value : '-';
            bu = bu.split(',');
            status = ps['status'] ? ps['status'].value.toLowerCase() : undefined;

            owner = _.map(tags, function (k) {
                k = k.toLowerCase();
                if (k.startsWith('owner_')) return k.substring(6, k.length);
            });
            owner = _.reject(_.uniq(owner), function (item) {
                return item == 'unknown';
            });


            var createdTime = item['created-time'];
            var propertyavailable = false;
            var statusavailable = status && (status == 'activated' || status == 'deactivated');
            var java = _.find(apps, function (l) {
                return l.relatedappid != undefined;
            });
            if (java) {
                var controlsGroupByWeight = _.countBy(item.controls, function (v) {
                    return v.weight;
                });
                // fix: If any control's weight is equals to 0. go on
                if (controlsGroupByWeight['0'])
                    propertyavailable = true;
            }


            // Path validation
            var policyPathObj = _.map(item['policy-virtual-servers'], function (v) {
                var path = v.path;
                var vsId = v['virtual-server'].id;

                var temp = vsId + '_' + path;
                return temp.toLocaleLowerCase();
            });

            policyPathObj = policyPathObj.sort();

            var controlPaths = _.map(item.controls, function (v) {
                var groupId = v.group.id;
                var group = groups[groupId];
                var policySamePathGroupVses = _.filter(group['group-virtual-servers'], function (s) {
                    return policyVses.indexOf(s['virtual-server']['id']) != -1;
                });

                var groupPaths = _.map(policySamePathGroupVses, function (s) {
                    var path = s.path;
                    var vsId = s['virtual-server'].id;
                    return vsId + '_' + path;
                });
                return {
                    groupId: groupId,
                    paths: groupPaths
                }
            });

            var pathValidatePassed = true;
            $.each(controlPaths, function (j, t) {
                var currentPath = t.paths.sort();
                if (currentPath.join().toLowerCase() != policyPathObj.join()) {
                    pathValidatePassed = false;
                }
            });

            var rightAvailable = A.canDo('Policy', 'DEACTIVATE', id) && A.canDo('Policy', 'DELETE', id);


            var error;
            var isRightAccess = false;

            if (!statusavailable) {
                error = diagramresource['暂时不支持下线。 因为灰度策略存在未生效变更：'] +
                    '<ul>' +
                    '<li>' + diagramresource['请查看变更内容，并消除变更后再下线。'] + '<a target="_blank" href="/portal/policy#?env=' + self.env + '&policyId=' + id + '&diff=true" class="status-yellow" >' + diagramresource["（查看变更）"] + '</a></li>' +
                    '<li>' + diagramresource['若仍不知如何处理，欢迎联系'] + '<a href="mailto:slb@test.com">SLB Team</a></li>' +
                    '</ul>';
            }
            else {
                if (!java) {
                    error = diagramresource['当前策略关联的应用中不包含要下线的应用。请确认您是否使用该AppId开始过一个新的流量灰度策略。'];
                } else {
                    if (!propertyavailable) {
                        error = diagramresource['暂时不支持下线，因为流量在新老应用上都有。'] +
                            '<ul>' +
                            '<li>' + diagramresource['请查看当前策略的流量比例。确保流量已经100%切到了期望的应用上。'] + '<a target="_blank" href="/portal/policy#?env=' + self.env + '&policyId=' + id + '">' + diagramresource["（查看流量比例）"] + '</a></li>' +
                            '<li>' + diagramresource["若仍不知如何处理，欢迎联系"] + '<a href="mailto:slb@test.com">SLB Team</a></li>' +
                            '</ul>';
                    } else {
                        if (!rightAvailable) {
                            isRightAccess = true;
                            error = diagramresource["用户没有下线并删除策略的权限。"] + '<a class="fast-apply">' + diagramresource["点此快速申请"] + '</a>！';
                        } else {
                            if (!pathValidatePassed) {
                                error = diagramresource["当前策略还不能下线，因为当前策略没有包含所有老应用的流量。SOA应用策略请包含所有Service Operation，请参考:"] + '<a target="_blank" href="http://test.company.com/books/slb/part1-net-to-java/03-change-net2java.html">' + diagramresource["策略Service Operation调整文档"] + '</a>,' + diagramresource["非SOA应用相关，请调整Path 参照"] +
                                    '<a target="_blank" href="http://test.company.com/books/slb/part1-net-to-java/04-change-net2java.html">' + diagramresource["策略Path调整文档"] + '</a>' +
                                    '，' + diagramresource["其他问题请联系"] + '<a href="mailto:slb@test.com">SLB Team</a>';
                            }
                        }
                    }
                }
            }

            return {
                isAccessLink: isRightAccess,
                accessLink: righturl + '&targetId=' + id,
                id: id,
                status: status,
                name: name,
                apps: apps,
                bues: bu,
                owners: owner,
                createdtime: createdTime,
                available: propertyavailable != false && pathValidatePassed != false && statusavailable && rightAvailable,
                error: error
            };
        });
        outer(t);
    });
    return [send];
};
UserPolicyApp.prototype.deactivatePolicy = function (deactivatePolicyRequest, next, action) {
    var send = this.sendRequest(deactivatePolicyRequest, function (res, code) {
        next();
    });
    return [send];
};
UserPolicyApp.prototype.deletePolicy = function (deletePolicyRequest, addLog, action) {
    var diagramresource = this.resourcefile;

    var send = this.sendRequest(deletePolicyRequest, function (res, code) {
        var success = false;
        if (code == 200) {
            success = true;
        }
        var stext = success ? diagramresource['成功'] : diagramresource['失败, 原因- Code:'] + code + ', Message:' + (res.message || '-');
        action(success, diagramresource['删除灰度策略'] + stext, 2);
        addLog();
    });
    return send;
};
UserPolicyApp.prototype.addStatusLog = function (logs, group) {
    var failed = _.find(logs, function (item) {
        return item.success == false;
    });
    var diagramresource = this.resourcefile;


    if (failed) {
        logs.push(
            {
                index: 3,
                success: false,
                text: diagramresource['操作失败:请将错误信息提供给SLB Team提供获取帮助']
            }
        );
    }
    else {
        logs.push(
            {
                index: 3,
                success: true,
                text: diagramresource['操作成功:请到CDNG中下线Group: '] + group.groupname
            }
        );
    }
    return logs;
};

// Draw .net to java graphic
UserPolicyApp.prototype.drawListOfPolicyGraphics = function (c, t, $http, env, scopegroups, scopeapps, dashboardurl, activateFn, updateFn, testFn, metricsFn, diagram, diagramOnly, noOperation) {
    var diagramresource = this.resourcefile;

    var diagramZone = $('#diagrams-area');
    if (diagram) {
        diagramZone = diagram;
    }

    $.each(_.values(c), function (i, current) {
        var m = _.map(current.controls, function (t) {
            var gid1 = t.group.id;
            return scopegroups[gid1];
        });

        var policyName = diagramresource['流量切换:'];

        var newappname = '';
        var oldappname = '';

        var policyPropterties = _.indexBy(current.properties, function (v) {
            return v.name.toLowerCase();
        });
        var status = (policyPropterties['status'] && policyPropterties['status'].value) ? policyPropterties['status'].value.toLowerCase() : 'unknow';
        var statusClass = '';
        var statusText = '';
        switch (status) {
            case 'activated': {
                statusClass = 'status-green';
                statusText = diagramresource['已激活'];
                break;
            }
            case 'deactivated': {
                statusClass = 'status-gray';
                statusText = diagramresource['未激活'];
                break;
            }
            case 'tobeactivated': {
                statusClass = 'status-yellow';
                statusText = diagramresource['有变更'];
                break;
            }
            default: {
                break;
            }
        }
        var ptext = '<span class="' + statusClass + '">' + statusText + '</span>';

        var newapp = _.filter(m, function (s) {
            var sProperties = _.indexBy(s.properties, function (v) {
                return v.name.toLowerCase();
            });
            if (sProperties['relatedappid']) return true;
        });
        var oldapp = _.filter(m, function (s) {
            var sProperties = _.indexBy(s.properties, function (v) {
                return v.name.toLowerCase();
            });

            if (!sProperties['relatedappid']) return true;
        });

        var lan = '';
        if (oldapp && oldapp.length > 0) {
            oldapp = oldapp[0];
            var oldappid = oldapp['app-id'];

            var oldps = _.indexBy(oldapp.properties, function (v) {
                return v.name.toLowerCase();
            });

            lan = oldps['language'] ? oldps['language'].value : diagramresource['未知语言'];
            oldappname += lan.toUpperCase() + '/';
            oldappname += scopeapps[oldappid] ? scopeapps[oldappid]['chinese-name'] : '-';
        }

        if (newapp && newapp.length > 0) {
            newapp = newapp[0];
            var newappid = newapp['app-id'];

            var newps = _.indexBy(newapp.properties, function (v) {
                return v.name.toLowerCase();
            });

            lan = newps['language'] ? newps['language'].value : diagramresource['未知语言'];
            newappname += lan.toUpperCase() + '/';
            newappname += scopeapps[newappid] ? scopeapps[newappid]['chinese-name'] : '-';
        }
        if (oldapp && newapp) {
            policyName += oldappname + '----->' + newappname;
        }

        var classname = 'col-md-7';
        var panel = '<div style="cursor: pointer" class="panel panel-default">' +
            '<div class="panel-heading">';
        panel += '<div class="options-bar pull-right" role="group" aria-label="...">' + ptext;

        if (!diagramOnly) {
            if (!noOperation) {
                var activatetext = diagramresource['激活上线'];
                var updatetext = diagramresource["更新"];
                var monitortext = diagramresource["监控"];

                panel +=
                    '<button id="activate_policy_bt_' + i + '" type="button" class="btn btn-info">' + activatetext + '</button>' +
                    '<button id="update_policy_bt_' + i + '" type="button" class="btn btn-warning">' +
                    updatetext +
                    '</button>' +
                    '<button id="traffic_policy_bt_' + i + '" type="button" class="btn btn-info">' +
                    monitortext +
                    '</button>' +
                    '<button id="test_policy_bt_' + i + '" type="button" class="btn btn-info">' +
                    diagramresource["灰度测试"] +
                    '</button>';
            }

        } else {
            classname = 'col-md-12';
        }

        panel += '</div>';

        panel +=
            '<h3 class="panel-title"><a href="/portal/policy#?env=' + env + '&policyId=' + current.id + '" target="_self">' + policyName + '</a></h3>' +
            '</div>';

        panel += '<div class="panel-body" >' +
            '<div class="row">' +
            '<div class="' + classname + '" id="policy_container_' + i + '">' +
            '<div style="" id="policy_' + i + '">' + diagramresource["Policy的信息区域"] + '</div>' +
            '</div>';

        if (!diagramOnly) {
            panel += '<div class="col-md-5" id="policy_container_metrics_' + i + '" style="margin-right: -10px">' +
                '<div style="border-radius: 1px; "  id="policy_metrics_' + i + '">' + diagramresource["监控区域"] + '</div>' +
                '</div>';
        }

        panel += '</div>' +
            '</div>' +
            '</div>';

        diagramZone.append(panel);
        TrafficPolicyGraphics.writePolicy(current.id, t, 'policy_' + i, env, diagramresource);

        /*  $('#policy_container_' + i).click(
              function () {
                  window.location.href = '/portal/policy#?env=' + env + '&policyId=' + current.id;
              }
          );*/
        $('#policy_container_metrics_' + i).click(
            function () {
                window.open('/portal/policy/traffic#?env=' + env + '&policyId=' + current.id, '_blank');
            }
        );

        $('#activate_policy_bt_' + i).click(
            function () {
                var t = activateFn(current.id);
                if (t) {
                    t.apply();
                }
            }
        );

        $('#traffic_policy_bt_' + i).click(
            function () {
                var t = metricsFn(current.id);
                if (t) {
                    t.apply();
                }
            }
        );
        $('#update_policy_bt_' + i).click(
            function () {
                var t = updateFn(current.id);
                if (t) {
                    t.apply();
                }
            }
        );
        $('#test_policy_bt_' + i).click(
            function () {
                window.open('/portal/tools/test#?env=' + env + '&policyId=' + current.id, '_blank');
            }
        );

        var endTime = new Date();

        var startTime = new Date();

        startTime.setHours(endTime.getHours() - 1);

        endTime = $.format.date(endTime, 'yyyy-MM-dd HH:mm');
        startTime = $.format.date(startTime, 'yyyy-MM-dd HH:mm');


        var params = {
            'metric-name': 'slb.req.count',
            'start-time': startTime,
            'end-time': endTime,
            'tags': '{"policy_name":["' + current.id + '"]}',
            'interval': '1m',
            'chart': 'line',
            'aggregator': 'sum',
            'downsampler': 'sum',
            'group-by': '[group_id]'
        };
        var config = {
            'url': dashboardurl,
            'method': 'GET',
            'params': params
        };

        if (diagramOnly) {
            return;
        }
        //$('#policy_metrics_' + i).showLoading();

        var a = TrafficPolicyGraphics.writePolicyMetric('policy_metrics_' + i, [], [], diagramresource);
        a.showLoading();
        config.withCredentials = false;
        $http(config).success(
            function (res) {
                var category = ['-'];
                var series = [{
                    name: '-',
                    data: ['0']
                }];

                var responseSeries = res['time-series-group-list'];
                var r = _.map(responseSeries, function (item) {
                        var groupId = item['time-series-group'] ? item['time-series-group']['group_id'] : '';
                        if (groupId) {
                            var appId = scopegroups[groupId]['app-id'];
                            var ps = _.indexBy(scopegroups[groupId].properties, function (v) {
                                return v.name.toLowerCase();
                            });
                            var idc = ps['idc'] ? ps['idc'].value : '-';
                            var appName = scopeapps[appId] ? scopeapps[appId]['chinese-name'] : 'UNKOWN';
                            var metrics = item['data-points']['data-points'];

                            var language = ps['language'] ? ps['language'].value : diagramresource['未知语言'];

                            startTime = new Date(startTime);
                            return {
                                metrics: {
                                    name: language.toUpperCase() + '/' + idc + '/' + appName,
                                    data: metrics
                                }
                            }
                        }
                    }
                );

                if (r && r.length > 0) {
                    series = _.pluck(r, 'metrics');
                    var xPoints = [];
                    var len = series[0].data.length;
                    for (var j = 0; j < len; j++) {
                        xPoints.push($.format.date(startTime, 'HH:mm'));
                        startTime = new Date(startTime.getTime() + 1 * 60000);
                    }
                    category = xPoints;
                }
                // $('#policy_metrics_' + i).hideLoading();
                var s = TrafficPolicyGraphics.writePolicyMetric('policy_metrics_' + i, category, series, diagramresource);
                s.hideLoading();
            }
        );
    });
}
// Utils
UserPolicyApp.prototype.getGroupVsPathObj = function (group) {
    var groupvses = group['group-virtual-servers'];
    var vsPaths = _.map(groupvses, function (item) {
        var vsId = item['virtual-server'].id;
        return {
            'vsId': vsId,
            'path': item.path,
            priority: item['priority']
        }
    });
    return vsPaths;
};
UserPolicyApp.prototype.getGroupSameAndDiffrentVsPaths = function (all) {
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

        $.each(vsIds, function (i, item) {
            var v = _.filter(values, function (t) {
                return t.vsId == item;
            });
            result.same.push(
                {
                    vsId: item,
                    path: _.pluck(v, 'path')[0],
                    available: true,
                    priority: abstractVsPriority(v)
                }
            );
        });
    }
    return result;
};
UserPolicyApp.prototype.getStatusText = function (status) {
    var diagramresource = this.resourcefile;

    var text = '-';
    switch (status) {
        case 'activated': {
            text = diagramresource['已激活'];
            break;
        }
        case 'deactivated': {
            text = diagramresource['未激活'];
            break;
        }
        case 'tobeactivated': {
            text = diagramresource['有变更'];
            break;
        }
        default:
            break;
    }
    return text;
};
UserPolicyApp.prototype.getStatusClass = function (status) {
    var classes = '-';
    switch (status) {
        case 'activated': {
            classes = 'status-green';
            break;
        }
        case 'deactivated': {
            classes = 'status-red';
            break;
        }
        case 'tobeactivated': {
            classes = 'status-yellow';
            break;
        }
        default:
            break;
    }
    return classes;
};

function abstractVsPriority(array) {
    var result = _.max(_.pluck(array, 'priority')) + 1;
    return result;
};

