/**
 * Created by ygshen on 2017/2/22.
 */
var summaryInfoApp = angular.module('summaryInfoApp', ['http-auth-interceptor', 'angucomplete-alt']);
summaryInfoApp.controller('summaryController', function ($scope, $http, $q) {
    $scope.query = {};
    // Auto complete
    $scope.cacheRequestFn = function (str) {
        return {q: str, timestamp: new Date().getTime()};
    };
    $scope.remoteUrl = function () {
        return G.baseUrl + "/api/meta/policies";
    };
    $scope.selectTarget = function (t) {
        if (t) {
            var toId = t.originalObject.id;
            if ($scope.target.id != toId) {
                $scope.$broadcast('angucomplete-alt:clearInput', 'targetSelector');
                var pairs = {};
                pairs['policyId'] = toId;
                $scope.target.id = toId;
                H.setData(pairs);
                messageNotify("切换Policy. ", "成功切换至Traffic Policy： " + toId, null);
            }
        }
    };
    $scope.clickTarget = function () {
        $('#targetSelector_value').css('width', '250px');
    };
    $scope.setInputsDisplay = function () {
        $('#targetSelector_value').val($scope.query.policyId);
    };
    $scope.getAllTargets = function () {
        $http.get(G.baseUrl + "/api/policy?policyId=" + $scope.query.policyId).success(
            function (res) {
                $scope.target = {};
                $scope.target.name = $scope.query.policyId + "/" + res.name;
                $('#targetSelector_value').val($scope.target.name);
            }
        );
    };
    $scope.hashChanged = function (hashData) { $scope.resource = H.resource;
        if (hashData.env) {
            $scope.env = hashData.env;
        }
        if (hashData.policyId) {
            $scope.query.policyId = hashData.policyId;
            $scope.getAllTargets();
        }
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
    $scope.query = {};
    $scope.generateLink = function (a) {
        var link = '';
        switch (a) {
            case 'basic': {
                link = "/portal/policy#?env=" + G.env + "&policyId=" + $scope.query.policyId;
                break;
            }
            case 'log': {
                link = "/portal/policy/log#?env=" + G.env + "&policyId=" + $scope.query.policyId;
                break;
            }
            case 'traffic': {
                link = "/portal/policy/traffic#?env=" + G.env + "&policyId=" + $scope.query.policyId;
                break;
            }
            case 'conf': {
                link = "/portal/policy/conf#?env=" + G.env + "&policyId=" + $scope.query.policyId;
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
        if (hashData.policyId) {
            $scope.query.policyId = hashData.policyId;
        }
    };
    H.addListener("infoLinksApp", $scope, $scope.hashChanged);
});
angular.bootstrap(document.getElementById("header-area"), ['headerInfoApp']);

//ReleaseComponent: Release
var confApp = angular.module('confApp', ['http-auth-interceptor']);
confApp.controller('confController', function ($scope, $http, $q) {
    $scope.dataLoaded = false;
    $scope.data = {
        vsConf: {},
        upStreamConf: {},
        vsSlbs: {},
        slbs: {},
        vses: {},
        groups: {},
        policy: {}
    };

    //Type Info Page Switcher
    $scope.infoTypes = {
        currentIndex: 0,
        current: '',
        links: []
    };
    $scope.generateVSLinkText = function (x) {
        var f = _.find($scope.data.vses, function (item) {
            return item.id == x;
        });
        if (f) return x + "(" + f.name + ")";
    };
    $scope.generateSLBLinkText = function (x) {
        var f = _.find($scope.data.slbs, function (item) {
            return item.id == x;
        });
        if (f) return x + "(" + f.name + ")";
    };
    $scope.isActivateSlb = function (index) {
        if (index == 0) return 'active';
    }
    $scope.getSlbsByVs = function (vs) {
        return $scope.data.vsSlbs[vs];
    };

    $scope.isCurrentInfoPage = function (link) {
        return $scope.infoTypes.current == link ? 'current' : '';
    };
    $scope.setInfoType = function (t) {
        H.setData({infoType: t});
    };
    $scope.showInfo = function (type) {
        return type == $scope.infoTypes.current;
    };
    $scope.changeSlb = function (vs, slb, event) {
        var target = event.currentTarget;
        if (!$(target).hasClass('active')) {
            $(target).addClass('active');
            $.each($(target).siblings(), function (i, item) {
                $(item).removeClass('active');
            });
        }
        var details = $('#vs_' + vs).find('.tab-pane');

        $.each(details, function (i, item) {
            $(item).removeClass('active');
        });
        $('#panel_' + vs + '_' + slb).addClass('active');
        setTimeout(function () {
            $.each($scope.divList, function (i, item) {
                item.refresh();
            });
        }, 1000);
    };
    $scope.getGroupConfFromVsConf = function (vs, policypath, grouppath, groupIds) {
        if (!vs) return undefined;
        var s = clean_lines(vs);

        var policyIndex = s.indexOf('location ' + grouppath);
        var indexArray = [policyIndex];
        for(var i=0; i<groupIds.length;i++){
            indexArray.push(s.indexOf('location @group_' + groupIds[i]));
        }
        /*$.each(groupIds, function (i, item) {
            var policyGroupIndex = s.indexOf('location @group_' + item);
            if (policyGroupIndex != -1) {
                indexArray.push(policyGroupIndex);
            }
        });*/

        var c = '';
        /*(var k=0;k<indexArray.length;k++){*/
        $.each(indexArray, function (i, index) {
            if (index != -1) {
                var a = [];
                $.each(s, function (i, item) {
                    if (item.startWith('location ')) {
                        a.push(i);
                    }
                });
                a = a.sort(function (b, c) {
                    return b - c;
                });

                var t = [];

                var i = a.indexOf(index);
                if (i != a.length - 1) {
                    for (var j = a[i]; j < a[i + 1]; j++) {
                        t.push(s[j]);
                    }
                } else {
                    for (var j = index; j < s.length - 2; j++) {
                        t.push(s[j]);
                    }
                }

                var p = join_opening_bracket(t);
                p = perform_indentation(p);

                for (var k = p.length - 1; k >= 0; k--) {
                    if (p[k] == '}')
                        p[k]+='\n';
                    else break;
                }
                c += p.join("\n");
            }
        });
        if (c && c != '') return c;
        return undefined;
    };

    $scope.getGroupUpstreamFromVsConf = function (vs, groupId) {
        var path = "upstream backend_" + groupId;
        if (!vs) return undefined;
        var s = clean_lines(vs.trim());
        var index = s.indexOf(path);

        if (index != -1) {
            var a = [];
            $.each(s, function (i, item) {
                if (item.startWith('upstream backend_')) {
                    a.push(i);
                }
            });
            a = a.sort(function (b, c) {
                return b - c;
            });

            var t = [];

            var i = a.indexOf(index);
            if (i != a.length - 1) {
                for (var j = a[i]; j < a[i + 1]; j++) {
                    t.push(s[j]);
                }
            } else {
                for (var j = index; j < s.length; j++) {
                    t.push(s[j]);
                }
            }

            var p = join_opening_bracket(t);
            p = perform_indentation(p);
            p = p.join("\n").toString().trim('\n');

            return p;
        }
        return '';
    };
    String.prototype.endWith = function (s) {
        if (s == null || s == "" || this.length == 0 || s.length > this.length)
            return false;
        if (this.substring(this.length - s.length) == s)
            return true;
        else
            return false;
        return true;
    }
    //Build Data Modal
    $scope.dataLoaded = false;
    $scope.env = '';
    $scope.query = {
        policyId: ''
    };
    $scope.divList = [];
    $scope.loadData = function (hashData) {
        var confRequests = [];
        if ($scope.query.policyId == hashData.policyId && $scope.env == hashData.env && $scope.dataLoaded) {
            $scope.tabInfoChanged(hashData);
            setTimeout(function () {
                $.each($scope.divList, function (i, item) {
                    item.refresh();
                });
            }, 1000);
            return;
        }
        $('.content-area').showLoading();
        $('.CodeMirror').remove();
        $scope.dataLoaded = true;
        $scope.env = hashData.env;

        var param = {
            policyId: $scope.query.policyId
        };
        var request = {
            method: 'GET',
            url: G.baseUrl + '/api/policy',
            params: param
        };

        var param3 = {
            type: 'info'
        };
        var request3 = {
            method: 'GET',
            url: G.baseUrl + '/api/vses'
        };
        var request5 = {
            method: 'GET',
            url: G.baseUrl + '/api/groups'
        };
        var request4 = {
            method: 'GET',
            url: G.baseUrl + '/api/slbs',
            params: param3
        };

        $q.all(
            [
                $http(request).success(
                    function (res) {
                        $scope.data.policy = res;
                    }
                ),
                $http(request3).success(
                    function (res) {
                        $scope.data.vses = _.indexBy(res['virtual-servers'], 'id');
                    }
                ),
                $http(request4).success(
                    function (res) {
                        $scope.data.slbs = _.indexBy(res['slbs'], 'id');
                    }
                ),
                $http(request5).success(
                    function (res) {
                        $scope.data.groups = _.indexBy(res['groups'], 'id');
                    }
                )
            ]
        ).then(
            function () {
                $scope.data.vsConf = [];
                $scope.infoTypes.links = [];

                var vsSlbs = {};
                var slbs = [];

                $.each($scope.data.policy['policy-virtual-servers'], function (i, item) {
                    var vsId = item['virtual-server'].id;
                    vsSlbs[vsId] = $scope.data.vses[vsId]['slb-ids'];
                    slbs = slbs.concat(vsSlbs[vsId]);
                });

                $scope.infoTypes.links = _.map(vsSlbs, function (num, key) {
                    return key;
                });

                $scope.data.vsSlbs = vsSlbs;

                $.each(_.keys(vsSlbs), function (i, item) {
                    var vId = item;
                    $scope.data.vsConf[vId] = {};
                    $scope.data.upStreamConf[vId] = {};

                    $.each(vsSlbs[vId], function (j, item2) {
                        var param2 = {
                            vsId: vId,
                            slbId: item2
                        };
                        var request2 = {
                            method: 'GET',
                            url: G.baseUrl + '/api/nginx/conf',
                            params: param2
                        };
                        confRequests.push(
                            $http(request2).success(
                                function (res) {
                                    $scope.data.vsConf[vId][item2] = res['server-conf'];
                                    $scope.data.upStreamConf[vId][item2] = res['upstream-conf'];
                                }
                            )
                        );
                    });
                });
            }
        ).finally(
            function () {
                $q.all(confRequests).then(
                    function () {
                        if (!hashData.infoType) {
                            hashData.infoType = $scope.infoTypes.links[0];
                        }
                        var list = [];
                        var groupIds = [];

                        $.each(_.keys($scope.data.vsConf), function (i, key) {
                            var vsLevel = $scope.data.vsConf[key];
                            $.each(_.keys(vsLevel), function (j, key2) {
                                var confEdit = CodeMirror.fromTextArea(document.getElementsByName('nginxInput_' + key + '_' + key2)[0], {
                                    lineNumbers: true,
                                    mode: "nginx",
                                    lineWrapping: true,
                                    readOnly: true
                                });
                                var policypath;

                                var p = _.find($scope.data.policy['policy-virtual-servers'], function (item) {
                                    return item['virtual-server'].id == key;
                                });
                                if (p) policypath = p.path;

                                var c = _.reject($scope.data.groups, function (item) {
                                    var v = _.find($scope.data.policy.controls, function (r) {
                                        return r['group'].id == item.id;
                                    });
                                    if (v) return false;
                                    return true;
                                });
                                var d = _.find(c, function (item) {
                                    var e = item['group-virtual-servers'];
                                    var g = _.find(e, function (h) {
                                        return h['virtual-server'].id == parseInt(key) && h['virtual-server']['slb-ids'].indexOf(parseInt(key2)) != -1;
                                    });
                                    if (g) return true;
                                    return false;
                                });
                                var grouppath;
                                if (d) {
                                    var k = _.find(d['group-virtual-servers'], function (j) {
                                        return j['virtual-server'].id == key;
                                    });
                                    grouppath = k.path;
                                }

                                groupIds = [];

                                $.each($scope.data.policy['controls'], function (i, item) {
                                    groupIds.push(item['group'].id);
                                });

                                if (grouppath && policypath) {
                                    var text = $scope.getGroupConfFromVsConf($scope.data.vsConf[key][key2], policypath, grouppath, groupIds);
                                    if (text == undefined) {
                                        text = '当前Policy在SLB上没有配置信息生成，请先激活GROUP后再查看。';
                                    }
                                    if (confEdit) {
                                        confEdit.setValue(text);
                                    }
                                    list.push(confEdit);
                                }
                            });
                        });

                        $.each(_.keys($scope.data.upStreamConf), function (i, key) {
                            var vsLevel = $scope.data.upStreamConf[key];
                            $.each(_.keys(vsLevel), function (j, key2) {
                                var confEdit = CodeMirror.fromTextArea(document.getElementsByName('nginxInput2_' + key + '_' + key2)[0], {
                                    lineNumbers: true,
                                    mode: "nginx",
                                    lineWrapping: true,
                                    readOnly: true
                                });
                                var text = '';
                                $.each(groupIds, function (i, item) {
                                    if (text) {
                                      text += '\n';
                                    }
                                    text += $scope.getGroupUpstreamFromVsConf($scope.data.upStreamConf[key][key2], item);
                                });

                                if (text == undefined) {
                                    text = '当前Policy在SLB上没有配置信息生成，请先激活GROUP后再查看。';
                                }
                                if (confEdit) {
                                    confEdit.setValue(text);
                                }
                                list.push(confEdit);
                            });
                        });

                        $scope.divList = list;
                        $scope.tabInfoChanged(hashData);
                        setTimeout(function () {
                            $.each(list, function (i, item) {
                                item.refresh();
                            });
                        }, 1000);
                        $('.content-area').hideLoading();
                    }
                );
            }
        );
    };
    $scope.tabInfoChanged = function (hashData) {
        var t = hashData.infoType;
        var tls = $scope.infoTypes.links;
        $scope.infoTypes.current = t;
        $scope.infoTypes.currentIndex = _.indexOf(tls, t);
    };
    $scope.hashChanged = function (hashData) { $scope.resource = H.resource;
        if (hashData.policyId) $scope.query.policyId = hashData.policyId;
        $scope.loadData(hashData);
    };
    H.addListener("releaseApp", $scope, $scope.hashChanged);

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
angular.bootstrap(document.getElementById("conf-area"), ['confApp']);
