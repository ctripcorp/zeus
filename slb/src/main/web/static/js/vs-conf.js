//InfoLinksComponent: info links
var headerInfoApp = angular.module('headerInfoApp', ["angucomplete-alt"]);
headerInfoApp.controller('headerInfoController', function ($scope, $http) {
    $scope.query = {};
    $scope.generateLink = function (a) {
        var link = '';
        switch (a) {
            case 'basic':
            {
                link = "/portal/vs#?env=" + G.env + "&vsId=" + $scope.query.vsId;
                break;
            }
            case 'log':
            {
                link = "/portal/vs/log#?env=" + G.env + "&vsId=" + $scope.query.vsId;
                break;
            }
            case 'rule': {
                link = "/portal/vs/rule#?env=" + G.env + "&vsId=" + $scope.query.vsId;
                break;
            }
            case 'traffic':
            {
                link = "/portal/vs/traffic#?env=" + G.env + "&vsId=" + $scope.query.vsId;
                break;
            }
            case 'conf':
            {
                link = "/portal/vs/conf#?env=" + G.env + "&vsId=" + $scope.query.vsId;
                break;
            }
            case 'intercept': {
                link = "/portal/vs/intercept#?env=" + G.env + "&vsId=" + $scope.query.vsId;
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
        if (hashData.vsId) {
            $scope.query.vsId = hashData.vsId;
        }
    };
    H.addListener("infoLinksApp", $scope, $scope.hashChanged);
});
angular.bootstrap(document.getElementById("header-area"), ['headerInfoApp']);

var summaryInfoApp = angular.module('summaryInfoApp', ['http-auth-interceptor', 'angucomplete-alt']);
summaryInfoApp.controller('summaryController', function ($scope, $http, $q) {
    $scope.query = {};
    // Auto complete
    $scope.cacheRequestFn = function (str) {
        return {q: str, timestamp: new Date().getTime()};
    };
    $scope.remoteUrl = function () {
        return G.baseUrl + "/api/meta/vses";
    };
    $scope.selectTarget = function (t) {
        if (t) {
            var toId = t.originalObject.id;
            if ($scope.target.id != toId) {
                $scope.$broadcast('angucomplete-alt:clearInput', 'targetSelector');
                var pairs = {};
                pairs['vsId'] = toId;
                $scope.target.id = toId;
                H.setData(pairs);
                messageNotify("切换VS. ", "成功切换至VS： " + toId, null);
            }
        }
    };
    $scope.clickTarget= function () {
        $('#targetSelector_value').css('width','250px');
    };
    $scope.setInputsDisplay = function () {
        $('#targetSelector_value').val($scope.query.vsId);
    };
    $scope.getAllTargets = function () {
        $http.get(G.baseUrl + "/api/vs?vsId=" +   $scope.query.vsId + "&type=info").success(
            function (res) {
                $scope.target={};
                $scope.target.name =   $scope.query.vsId  + "/" + res.name;
                $('#targetSelector_value').val($scope.target.name);
            }
        );
    };
    $scope.hashChanged = function (hashData) { $scope.resource = H.resource;
        if (hashData.env) {
            $scope.env = hashData.env;
        }
        if (hashData.vsId) {
            $scope.query.vsId = hashData.vsId;
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

//ReleaseComponent: Release
var releaseApp = angular.module('confApp', ['http-auth-interceptor']);
releaseApp.controller('confController', function ($scope, $http, $q) {
    $scope.dataLoaded = false;
    $scope.data = {
        vsConf: {},
        slbs: {}
    };

    //Type Info Page Switcher
    $scope.infoTypes = {
        currentIndex: 0,
        current: '',
        links: []
    };
    $scope.generateSlbLinkText = function (x) {
        var f = _.find($scope.data.slbs, function (item) {
            return item.id == x;
        });
        if (f) return f.name + "(" + x + ")";
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
    //Build Data Modal
    $scope.dataLoaded = false;
    $scope.env = '';
    $scope.divList = [];
    $scope.loadData = function (hashData) {
        var confRequests = [];
        if ($scope.vsId == hashData.vsId && $scope.env == hashData.env && $scope.dataLoaded) {
            $scope.tabInfoChanged(hashData);
            setTimeout(function () {
                $.each($scope.divList, function (i, item) {
                    item.refresh();
                });
            }, 1000);
            return;
        }
        $('.CodeMirror').remove();
        $scope.dataLoaded = true;
        $scope.env = hashData.env;
        $scope.vsId = hashData.vsId;

        var param = {
            vsId: $scope.vsId
        };
        var request = {
            method: 'GET',
            url: G.baseUrl + '/api/vs',
            params: param
        };

        var param3 = {
            type: 'info'
        };
        var request3 = {
            method: 'GET',
            url: G.baseUrl + '/api/slbs',
            params: param3
        };
        $('.content-area').showLoading();
        $http(request).success(
            function (res) {
                $scope.data.vsConf=[];
                if(res.code){
                    exceptionNotify("失败了","加载Config信息失败",null);
                }else{
                    setTimeout(
                        function () {
                            $('.alert-danger').remove();
                        },
                        1000
                    );
                }
                $scope.infoTypes.links = [];
                var slbIds = res['slb-ids'];
                $scope.infoTypes.links = slbIds;

                $.each(slbIds, function (i, item) {
                    var param2 = {
                        vsId: $scope.vsId,
                        slbId: item
                    };
                    var request2 = {
                        method: 'GET',
                        url: G.baseUrl + '/api/nginx/conf',
                        params: param2
                    };
                    confRequests.push(
                        $http(request2).success(
                            function (res) {
                                $scope.data.vsConf[item] = res['server-conf'];
                            }
                        )
                    );
                });

                confRequests.push($http(request3).success(
                    function (res) {
                        $.each(slbIds, function (i, item2) {
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
                            var text = $scope.data.vsConf[key];
                            if(!text){
                                text="当前VS在SLB上没有配置信息生成，请先激活VS后再查看。";
                            }
                            var confEdit = CodeMirror.fromTextArea(document.getElementsByName('nginxInput' + key)[0], {
                                lineNumbers: true,
                                mode: "nginx",
                                lineWrapping:true,
                                readOnly:true
                            });
                            text = parseNginxConf(text);
                            if (confEdit) {
                                confEdit.setValue(text);
                            }
                            list.push(confEdit);
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
});
angular.bootstrap(document.getElementById("conf-area"), ['confApp']);
