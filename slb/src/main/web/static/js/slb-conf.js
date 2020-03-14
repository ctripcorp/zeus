var headerInfoApp = angular.module('headerInfoApp', ["angucomplete-alt"]);
headerInfoApp.controller('headerInfoController', function ($scope, $http) {
    $scope.query = {};
    $scope.generateLink = function (a) {
        var link = '';
        switch (a) {
            case 'basic': {
                link = "/portal/slb#?env=" + G.env + "&slbId=" + $scope.query.slbId;
                break;
            }
            case 'log': {
                link = "/portal/slb/log#?env=" + G.env + "&slbId=" + $scope.query.slbId;
                break;
            }
            case 'rule': {
                link = "/portal/slb/rule#?env=" + G.env + "&slbId=" + $scope.query.slbId;
                break;
            }
            case 'traffic': {
                link = "/portal/slb/traffic#?env=" + G.env + "&slbId=" + $scope.query.slbId;
                break;
            }
            case 'conf': {
                link = "/portal/slb/conf#?env=" + G.env + "&slbId=" + $scope.query.slbId;
                break;
            }
            case 'intercept': {
                link = "/portal/slb/intercept#?env=" + G.env + "&slbId=" + $scope.query.slbId;
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
        if (hashData.slbId) {
            $scope.query.slbId = hashData.slbId;
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
        return G.baseUrl + "/api/meta/slbs";
    };
    $scope.selectTarget = function (t) {
        if (t) {
            var toId = t.originalObject.id;
            if ($scope.target.id != toId) {
                $scope.$broadcast('angucomplete-alt:clearInput', 'targetSelector');
                var pairs = {};
                pairs['slbId'] = toId;
                $scope.target.id = toId;
                H.setData(pairs);
                messageNotify("切换SLB. ", "成功切换至SLB： " + toId, null);
            }
        }
    };
    $scope.clickTarget = function () {
        $('#targetSelector_value').css('width', '250px');
    };
    $scope.setInputsDisplay = function () {
        $('#targetSelector_value').val($scope.query.slbId);
    };
    $scope.getAllTargets = function () {
        $http.get(G.baseUrl + "/api/slb?slbId=" + $scope.query.slbId + "&type=info").success(
            function (res) {
                $scope.target = {};
                $scope.target.name = $scope.query.slbId + "/" + res.name;
                $('#targetSelector_value').val($scope.target.name);
            }
        );
    };
    $scope.hashChanged = function (hashData) { $scope.resource = H.resource;
        if (hashData.env) {
            $scope.env = hashData.env;
        }
        if (hashData.slbId) {
            $scope.query.slbId = hashData.slbId;
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
        conf: ''
    };

    //Build Data Modal
    $scope.env = 'pro';
    $scope.loadData = function (hashData) {
        $('.CodeMirror').remove();

        $scope.env = hashData.env;
        $scope.slbId = hashData.slbId;

        var param = {
            slbId: $scope.slbId
        };
        var request = {
            method: 'GET',
            url: G.baseUrl + '/api/nginx/nginxconf',
            params: param
        };

        var confRequests = [
            $http(request).success(function (response, code) {
                if (code == 200) {
                    $scope.data.conf = response.message;
                }
            })
        ];


        $('.content-area').showLoading();

        $q.all(confRequests).then(
            function () {
                var text = $scope.data.conf;
                if (!text) {
                    text = "当前SLB上没有配置信息生成。";
                }
                var confEdit = CodeMirror.fromTextArea(document.getElementsByName('nginxConf')[0], {
                    lineNumbers: true,
                    mode: "nginx",
                    lineWrapping: true,
                    readOnly: true
                });
                text = parseNginxConf(text);
                if (confEdit) {
                    confEdit.setValue(text);
                }
                $('.content-area').hideLoading();
            }
        );
    };
    $scope.hashChanged = function (hashData) { $scope.resource = H.resource;
        $scope.loadData(hashData);
    };
    H.addListener("releaseApp", $scope, $scope.hashChanged);
});
angular.bootstrap(document.getElementById("conf-area"), ['confApp']);
