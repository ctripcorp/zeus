//InfoLinksComponent: info links
var headerInfoApp = angular.module('headerInfoApp', ["angucomplete-alt"]);
headerInfoApp.controller('headerInfoController', function ($scope, $http) {
    $scope.query = {};
    $scope.generateLink = function (a) {
        var link = '';
        switch (a) {
            case 'basic':
            {
                link = "/portal/statistics#?env=" + G.env;
                break;
            }
            case 'policy':
            {
                link = "/portal/statistics/statistics-netjava#?env=" + G.env;
                break;
            }
            case 'rule': {
                link = "/portal/statistics/rule#?env=" + G.env;
                break;
            }
            case 'dashboard':
            {
                link = "/portal/statistics/dashboard#?env=" + G.env;
                break;
            }
            case 'hc':
            {
                link = "/portal/statistics/statistics-hc#?env=" + G.env;
                break;
            }
            case 'abtest':
            {
                link = "/portal/statistics/statistics-normalpolicy#?env=" + G.env;
                break;
            }
            case 'pie':
            {
                link = "/portal/statistics/charts#?env=" + G.env;
                break;
            }
            case 'traffic':
            {
                link = "/portal/statistics/traffic#?env=" + G.env;

                break;
            }
            case 'bu-traffic':
            {
                link = "/portal/statistics/butraffic#?env=" + G.env + '&bu=All';
                break;
            }
            case 'version':
            {
                link = "/portal/statistics/release#?env=" + G.env;
                break;
            }
            case 'health':
            {
                link = "/portal/statistics/slbhealth#?env=" + G.env;
                break;
            }
            case 'log':
            {
                link = "/portal/statistics/opslog#?env=" + G.env;
                break;
            }
            case 'database':
            {
                link = "/portal/statistics/dbtraffic#?env=" + G.env;
                break;
            }
            case 'deploy':
            {
                link = "/portal/statistics/deployment#?env=" + G.env;
                break;
            }
            case 'ctripprogress':
            {
                link = "/portal/statistics/statistics-ctrip-netjava#?env=" + G.env;
                break;
            }
            case 'cert':
            {
                link = "/portal/statistics/certificates#?env=" + G.env;
                break;
            }
            case 'ctriplanguage':
            {
                link = "/portal/statistics/statistics-ctrip-language#?env=" + G.env;
                break;
            }
            case 'comments':
            {
                link = "/portal/statistics/statistics-feedback#?env=" + G.env;
                break;
            }
            case 'unhealthy':
            {
                link = "/portal/statistics/statistics-unhealthy#?env=" + G.env;
                break;
            }
            case 'rule':
            {
                link = "/portal/statistics/rule#?env=" + G.env;
                break;
            }
            default:
                break;
        }
        return link;
    }
    $scope.hashChanged = function (hashData) {
        if (hashData.env) {
            $scope.env = hashData.env;
        }
        if (hashData.appId) {
            $scope.query.appId = hashData.appId;
        }
    };
    H.addListener("infoLinksApp", $scope, $scope.hashChanged);
});
angular.bootstrap(document.getElementById("header-area"), ['headerInfoApp']);

var queryApp = angular.module('queryApp', ["angucomplete-alt", "http-auth-interceptor"]);
queryApp.controller('queryController', function ($scope, $http) {
    $scope.query = {};
    $scope.data = {};

    $scope.cacheRequestFn = function (str) {
        return {q: str, timestamp: new Date().getTime()};
    };
    $scope.remoteVsesUrl = function () {
        return G.baseUrl + "/api/meta/vses";
    };

    $scope.remoteDomainsUrl = function () {
        return G.baseUrl + "/api/meta/domains";
    };

    $scope.addDomain = function (o) {
        if (o) {
            $scope.query.domain = o.originalObject.id;
        }
    };

    $scope.vsIdInputChanged = function (o) {
        $scope.query.vsId = o;
    };

    $scope.selectVsId = function (o) {
        if (o) {
            $scope.query.vsId = o.originalObject.id;
        }
    };

    //Execute query after click query button.
    $scope.executeQuery = function () {
        var hashData = {};
        hashData.timeStamp = new Date().getTime();

        var vsId = $scope.query.vsId;
        var domain = $scope.query.domain;
        var expireTime = $scope.query.expireTime;

        if (vsId) hashData.vsId = vsId;
        if (domain) hashData.domain = domain;
        if (expireTime) hashData.expireTime = expireTime;

        H.setData(hashData);
    };

    //Reset query data after click clear button.
    $scope.clearQuery = function () {
        $scope.query.vsId = '';
        $scope.query.domain = '';
        $scope.query.expireTime = '';
        $scope.setInputsDisplay();
    };

    $scope.setInputsDisplay = function () {
        if($scope.query.vsId){
            $('#vsIdSelector_value').val($scope.query.vsId);
        }
        if($scope.query.domain){
            $('#domainSelector_value').val($scope.query.domain);
        }
    };

    $scope.applyHashData = function (hashData) {
        if(hashData.vsId){
            $scope.query.vsId = hashData.vsId;
        }
        if(hashData.domain){
            $scope.query.domain = hashData.domain;
        }
        if(hashData.expireTime){
            $scope.query.expireTime = $.format.date(hashData.expireTime, 'yyyy-MM-dd');
        }

        $scope.setInputsDisplay();
    };

    $scope.hashChanged = function (hashData) {
        $scope.applyHashData(hashData);
    };
    H.addListener("queryApp", $scope, $scope.hashChanged);
});
angular.bootstrap(document.getElementById("query-area"), ['queryApp']);

var resultApp = angular.module('resultApp', ['http-auth-interceptor']);
resultApp.controller('resultController', function ($scope, $http, $q) {
    $scope.query = {};
    $scope.data = {
        cert: {
            type: 'local',
            domains: [],
            cert: '',
            key: ''
        }
    };

    $scope.addNewCert = function () {
        $('#addCertDialog').modal('show');
    };

    $scope.disableAddCert = function () {
        var cert = $scope.data.cert;
        if (!cert) return true;

        var type = cert.type;
        if (type == "local") {
            var keyOk = cert.key && cert.key.name.toLowerCase().endsWith('.key');
            var crtOk = cert.key && cert.cert.name.toLowerCase().endsWith('.crt');
            return !(cert.domains.length > 0 && cert.key && cert.cert && keyOk && crtOk)
        } else {
            return !(cert.domains.length > 0 && cert.cid);
        }
    };

    $scope.addNewDomain = function (domain) {
        if (!domain || !domain.trim()) {
            alert('域名不能为空');
            return;
        }
        domain = domain.toLowerCase();
        var domains = _.map($scope.data.cert.domains, function (v) {
            return v.toLowerCase();
        });
        if (domains.indexOf(domain) == -1) {
            $scope.data.cert.domains.push(domain);
            $scope.query.domain = '';
        }
    };

    $scope.removeDomain = function (domain) {
        if (!domain) return;
        var domains = $scope.data.cert.domains;
        var index = domains.indexOf(domain);
        if (index != -1) {
            domains.splice(index, 1);
        }
    };

    $scope.showFormatError = function (file, expected) {
        if (!file) return false;
        var name = file.name.toLowerCase();
        return !name.endsWith('.' + expected);
    };
    $scope.uploadFile = function () {
        var cert = $scope.data.cert;
        if (!cert) return;

        var type = cert.type;

        var key = new FormData();
        key.append('domain', cert.domains);
        if (type == 'local') {
            key.append('key', cert.key);
            key.append('cert', cert.cert);

        } else {
            key.append('cid', cert.cid);
        }

        var query = '';
        for (var i = 0; i < cert.domains.length; i++) {
            if (i != 0) {
                query += '&domain=' + cert.domains[i];
            } else {
                query += 'domain=' + cert.domains[i];
            }
        }

        $.ajax({
            url: G.baseUrl + "/api/cert/certs/add?" + query,
            data: key,
            type: "Post",
            dataType: "json",
            cache: false,//上传文件无需缓存
            processData: false,//用于对data参数进行序列化处理 这里必须false
            contentType: false, //必须
            beforeSend: function () {
                $('#addCertDialog').modal('hide');
                processing();
            },
            success: function (result) {
                succeed();
            },
            error: function (xhr, status, error) {
                fail(error);
            }
        });
    };

    function loadData(hashData) {
        var domain = hashData.domain;
        var vsId = hashData.vsId;
        var expireTime = hashData.expireTime;

        var params = {};
        if (domain) {
            params.domain = domain;
        }
        if (vsId) {
            params.vsId = vsId;
        }
        if (expireTime) {
            params.expireTime = expireTime;
        }

        var vsesRequest = {
            url: G.baseUrl + '/api/vses?type=info',
            method: 'GET'
        };

        var request = {
            method: 'GET',
            params: params,
            url: G.baseUrl + '/api/cert/certs/all'
        };

        var vses;
        var certs;

        var query2 = $http(vsesRequest).success(function (response, code) {
            if (code == 200) {
                vses = _.indexBy(response['virtual-servers'], 'id');
            }
        });

        var query1 = $http(request).success(function (response, code) {
            if (code == 200) {
                certs = response;
            }
        });

        $q.all([query1, query2]).then(function () {
            certs = _.map(certs, function (v) {
                v.domains = v.domain ? v.domain.split('|') : [];
                v.vses = _.map(v['vs-ids'], function (s) {
                    return {
                        id: s,
                        name: vses[s] ? vses[s].name : '-'
                    }
                });
                v.cid = v['cid'] || '-';

                return v;
            });

            if(vsId){
                certs = _.filter(certs, function (v) {
                    var vses = _.pluck(v['vses'], 'id');
                    return vses.indexOf(parseInt(vsId))!=-1;
                });
            }

            if (domain) {
                certs = _.filter(certs, function (v) {
                   var domains = _.map(v['domains'],function (s) {
                       return s.toLowerCase();
                   });

                    return domains.indexOf(domain.toLowerCase())!=-1;
                });
            }

            if(expireTime){
                certs = _.filter(certs, function (v) {
                    var etime = v['expire-time'];
                    if(!etime) return true;

                    return (new Date(etime).getTime() -  new Date(expireTime).getTime())<=0;
                });
            }



            $scope.data.certs = certs;
            $('#list-table').bootstrapTable("hideLoading")

            $('#list-table').bootstrapTable("load", certs);
        });
    };

    function processing() {
        $('#progressDialog').modal('show').find('.modal-body').html('<span class=""><img src="/static/img/spinner.gif">正在上传</span>');
    }

    function succeed() {
        $('#progressDialog').modal('show').find('.modal-body').html('<span class="fa fa-check status-green">上传成功</span>');
        setTimeout(function () {
            close();
            H.setData({timeStamp: new Date().getTime()})
        }, 2000);
    }

    function close() {
        $('#progressDialog').modal('hide');
    }

    function fail(err) {
        $('#progressDialog').modal('show').find('.modal-body').html('<span class="fa fa-times status-red">上传失败,错误信息: ' + err + '</span>');
        setTimeout(function () {
            close();
        }, 2000);
    }

    function initTable() {
        $('#list-table').bootstrapTable({
            toolbar: "#table-toolbar",
            columns: [[
                {
                    field: 'state',
                    checkbox: 'true',
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
                        return '<a href="/portal/statistics/certificate#?env=' + env + '&certId=' + value + '">' + value + '</a>';
                    }
                },
                {
                    field: 'cid',
                    title: 'CID',
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    formatter: function (value, row, index) {
                        return value;
                    }
                },
                {
                    field: 'domains',
                    title: 'Domain',
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    formatter: function (value, row, index) {
                        if (!value) return '-';
                        var str = '<div>';
                        for (var i = 0; i < value.length; i++) {
                            str += '<div>' + value[i] + '</div>'
                        }
                        str += '</div>';
                        return str;
                    }
                },
                {
                    field: 'vses',
                    title: 'Virtual Servers',
                    align: 'left',
                    valign: 'middle',
                    events: operateEvents,
                    sortable: true,
                    formatter: function (value, row, index) {
                        if (!value || value.length == 0) return '-';
                        var str = '<div>';
                        for (var i = 0; i < value.length; i++) {
                            str += '<div><a href="/portal/vs#?env=' + env + '&vsId=' + value[i].id + '">' + value[i].name + '</a></div>';

                            if (i > 3) {
                                str += '<a class="link-more">点此查看全部...</a>';
                            }
                        }
                        str += '</div>';
                        return str;
                    }
                },
                {
                    field: 'issue-time',
                    title: 'Release Time',
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    formatter: function (value, row, index) {
                        return value;
                    }
                },
                {
                    field: 'expire-time',
                    title: 'Expire Time',
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    formatter: function (value, row, index) {
                        return value;
                    }
                },
                {
                    title: 'Operations',
                    align: 'center',
                    valign: 'middle',
                    sortable: true,
                    formatter: function (value, row, index) {
                        var str = '<div>';
                        str += '<a class="upgrade btn btn-info btn-xs fa fa-paper-plane-o" href="/portal/tools/cert/edit#?env=' + env + '&certId=' + row.id + '">升级证书</a>';
                        str += '</div>';
                        return str;
                    }
                }
            ], []],
            sortName: 'id',
            sortOrder: 'desc',
            data: [],
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
                return "<img class='loading-img' src='/static/img/loading_100.gif' /> 正在加载所有证书";
            },
            formatNoMatches: function () {
                return '<i class="fa status-alert fa-exclamation-triangle" aria-hidden="true"></i> 没有找到匹配的证书';
            }
        });
    };
    window.operateEvents = {
        'click .link-more': function (e, value, row) {
            $scope.query.currentVses = value;
            $('#viewVsesDialog').modal('show');
        }
    };

    var env;
    $scope.hashChanged = function (hashData) {
        env = hashData.env || 'pro';
        initTable();
        $('#list-table').bootstrapTable("showLoading")

        loadData(hashData);
    };
    H.addListener("resultApp", $scope, $scope.hashChanged);
});
resultApp.directive('fileModel', ['$parse', function ($parse) {
    return {
        restrict: 'A',
        link: function (scope, element, attrs) {
            var model = $parse(attrs.fileModel);
            var modelSetter = model.assign;

            element.bind('change', function () {
                scope.$apply(function () {
                    modelSetter(scope, element[0].files[0]);
                });
            });
        }
    };
}]);
angular.bootstrap(document.getElementById("result-area"), ['resultApp']);