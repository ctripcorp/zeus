var summaryInfoApp = angular.module('summaryInfoApp', ['http-auth-interceptor', 'angucomplete-alt']);
summaryInfoApp.controller('summaryController', function ($scope, $http, $q) {
    $scope.query = {};
    // Auto complete
    $scope.cacheRequestFn = function (str) {
        return {q: str, timestamp: new Date().getTime()};
    };
    $scope.remoteUrl = function () {
        return G.baseUrl + "/api/meta/groups";
    };
    $scope.selectTarget = function (t) {
        if (t) {
            var toId = t.originalObject.id;
            if ($scope.target.id != toId) {
                $scope.$broadcast('angucomplete-alt:clearInput', 'targetSelector');
                var pairs = {};
                pairs['groupId'] = toId;
                $scope.target.id = toId;
                H.setData(pairs);
                messageNotify("切换Group. ", "成功切换至Group： " + toId, null);
            }
        }
    };
    $scope.clickTarget = function () {
        $('#targetSelector_value').css('width', '250px');
    };
    $scope.setInputsDisplay = function () {
        $('#targetSelector_value').val($scope.query.groupId);
    };
    $scope.getAllTargets = function () {
        $http.get(G.baseUrl + "/api/group?groupId=" + $scope.query.groupId + "&type=info").success(
            function (res) {
                $scope.target = {};
                $scope.target.name = $scope.query.groupId + "/" + res.name;
                $('#targetSelector_value').val($scope.target.name);
            }
        );
    };
    $scope.hashChanged = function (hashData) { $scope.resource = H.resource;
        if (hashData.env) {
            $scope.env = hashData.env;
        }
        if (hashData.groupId) {
            $scope.query.groupId = hashData.groupId;
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
                link = "/portal/group#?env=" + G.env + "&groupId=" + $scope.query.groupId;
                break;
            }
            case 'log': {
                link = "/portal/group/log#?env=" + G.env + "&groupId=" + $scope.query.groupId;
                break;
            }
            case 'traffic': {
                link = "/portal/group/traffic#?env=" + G.env + "&groupId=" + $scope.query.groupId;
                break;
            }
            case 'intercept': {
                link = "/portal/group/intercept#?env=" + G.env + "&groupId=" + $scope.query.groupId;
                break;
            }
            case 'antibot': {
                link = "/portal/group/antibot#?env=" + G.env + "&groupId=" + $scope.query.groupId;
                break;
            }
            case 'release': {
                link = "/portal/group/release#?env=" + G.env + "&groupId=" + $scope.query.groupId;
                break;
            }
            case 'conf': {
                link = "/portal/group/conf#?env=" + G.env + "&groupId=" + $scope.query.groupId;
                break;
            }
            case 'rule': {
                link = "/portal/group/rule#?env=" + G.env + "&groupId=" + $scope.query.groupId;
                break;
            }
            default:
                break;
        }
        return link;
    }
    $scope.showDelegate = function () {
        var env = $scope.env;
        if (!env) return false;

        switch (env) {
            case 'uat':
            case 'fws':
            case 'pro':
                return false;

            default:
                return true;
        }
    };
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


//ReleaseComponent: Release
var confApp = angular.module('confApp', ['http-auth-interceptor']);
confApp.controller('confController', function ($scope, $http, $q) {
    $scope.dataLoaded = false;
    $scope.data = {
        vsConf: {},
        upStreamConf: {},
        vsSlbs: {},
        slbs: {},
        vses: {}
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
    $scope.getGroupConfFromVsConf = function (vs, gvs, groupId) {
        if (!vs) return undefined;
        var s = clean_lines(vs.trim());

        var expectedLocationStarts = [];
        if (gvs.path) {
            expectedLocationStarts.push('location ' + gvs.path);
        }
        if (gvs.name) {
            expectedLocationStarts.push('location ' + gvs.name);
        }
        expectedLocationStarts.push('location @group_' + groupId);

        var indexArray = [];
        for (var i = 0; i < s.length; i++) {
            var isTargetLocation = _.some(expectedLocationStarts, function (start) {
                return s[i] === start;;
            });
            if (isTargetLocation) indexArray.push(i)
        }

        if (indexArray.length === 0) return;

        var c = '';
        $.each(indexArray, function (i, index) {
            if (index === -1) {
                return;
            }

            var t = [];
            var openBrackets = 0, foundOpenBracket = false;
            for (var j = index; j < s.length; j++) {
                var line = s[j];
                t.push(line);

                if (line.endsWith('{')) {
                  ++openBrackets;
                  foundOpenBracket = true;
                } else if (line.endsWith('}') || line.endsWith('};')) {
                  --openBrackets;
                }

                if (foundOpenBracket && openBrackets === 0) {
                    break;
                }
            }

            var isPolicyGroup = _.some(t, function (line) {
                return line.indexOf('set $policy_name policy_') !== -1;
            });
            if (isPolicyGroup) {
                return;
            }
            var notCurrentGroup =  _.some(t, function(line) {
                // Not my backend.
                return line.indexOf('backend_') !== -1 && line.indexOf('backend_' + groupId) === -1;
            });
            if (notCurrentGroup) return;
            var isPathLocation = s[index].indexOf(gvs.path) !== -1;
            if (gvs.name && isPathLocation) {
                // A group with both name and path, and we are processing the path location.
                // If the path location doesn't execute the name of the current GVS binding,
                // it means the conf should belong to another group with the same path and a different name.
                notCurrentGroup =  _.some(t, function(line) {
                    return line.indexOf('ngx.exec') !== -1 && line.indexOf(gvs.name) === -1;
                });
            }
            if (notCurrentGroup) return;

            var p = join_opening_bracket(t);
            p = perform_indentation(p);
            if (c) {
                c += '\n';
            }
            c += p.join("\n");
        });
        return c;
    };
    function escapeRegExp(str) {
        return str.replace(/[\-\[\]\/\{\}\(\)\*\+\?\.\\\^\$\|]/g, "\\$&");
    }
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
        return undefined;
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
        groupId: ''
    };
    $scope.divList = [];
    $scope.loadData = function (hashData) {
        var url = G.baseUrl + '/api/group';
        var locationPath = window.location.pathname;
        if (locationPath.indexOf('vgroup') != -1) {
            url = G.baseUrl + '/api/vgroup';
        }
        var confRequests = [];
        if ($scope.groupId == hashData.groupId && $scope.env == hashData.env && $scope.dataLoaded) {
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
        $scope.groupId = hashData.groupId;

        var param = {
            groupId: $scope.groupId
        };
        var request = {
            method: 'GET',
            url: url,
            params: param
        };

        var param3 = {
            type: 'info'
        };
        var request3 = {
            method: 'GET',
            url: G.baseUrl + '/api/vses',
            params: param3
        };
        var request4 = {
            method: 'GET',
            url: G.baseUrl + '/api/slbs',
            params: param3
        };
        // $('.content-area').showLoading();
        $http(request).success(
            function (res) {
                $scope.data.vsConf = [];
                $scope.infoTypes.links = [];
                var vsSlbs = {};
                var slbs = [];
                if (res.code) {
                    exceptionNotify("失败了", "加载Config信息失败", null);
                } else {
                    setTimeout(
                        function () {
                            $('.alert-danger').remove();
                        },
                        1000
                    );
                }

                $.each(res['group-virtual-servers'], function (i, item) {
                    var k = item['virtual-server'].id;
                    vsSlbs[k] = item['virtual-server']['slb-ids'];
                    slbs = slbs.concat(item['virtual-server']['slb-ids']);
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

                confRequests.push($http(request3).success(
                    function (res) {
                        $.each(_.keys(vsSlbs), function (i, item2) {
                            var f = _.find(res['virtual-servers'], function (r) {
                                return r.id == item2;
                            });
                            $scope.data.vses[item2] = f;
                        });
                    }
                ));

                confRequests.push($http(request4).success(
                    function (res) {
                        $.each(slbs, function (i, item2) {
                            var f = _.find(res['slbs'], function (r) {
                                return r.id == item2;
                            });
                            $scope.data.slbs[item2] = f;
                        });
                    }
                ));

                $q.all(confRequests).then(
                    function () {
                        if (!hashData.infoType) {
                            hashData.infoType = $scope.infoTypes.links[0];
                        }
                        var list = [];
                        $.each(_.keys($scope.data.vsConf), function (i, key) {
                            var vsLevel = $scope.data.vsConf[key];
                            $.each(_.keys(vsLevel), function (j, key2) {
                                var confEdit = CodeMirror.fromTextArea(document.getElementsByName('nginxInput_' + key + '_' + key2)[0], {
                                    lineNumbers: true,
                                    mode: "nginx",
                                    lineWrapping: true,
                                    readOnly: true
                                });
                                var gvs = _.find(res['group-virtual-servers'], function (r) {
                                    return r['virtual-server'].id == key && r['virtual-server']['slb-ids'].indexOf(parseInt(key2)) != -1;
                                });

                                var text = $scope.getGroupConfFromVsConf($scope.data.vsConf[key][key2], gvs, $scope.query.groupId);
                                if (text == undefined) {
                                    text = '当前GROUP在SLB上没有配置信息生成，请先激活GROUP后再查看。';
                                }
                                if (confEdit) {
                                    confEdit.setValue(text);
                                }
                                list.push(confEdit);
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
                                var text = $scope.getGroupUpstreamFromVsConf($scope.data.upStreamConf[key][key2], $scope.query.groupId);
                                if (text == undefined) {
                                    text = '当前GROUP在SLB上没有配置信息生成，请先激活GROUP后再查看。';
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
        if (hashData.groupId) $scope.query.groupId = hashData.groupId;
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
