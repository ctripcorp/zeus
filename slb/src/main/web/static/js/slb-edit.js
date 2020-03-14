//Slb-edit Component
var slbEditApp = angular.module('slbEditApp', ["http-auth-interceptor"]);
slbEditApp.controller('slbEditController', function ($scope, $http, $q) {
    $scope.data = {
        idc: ['金桥', '欧阳', '福泉', '南通', 'SHARB', 'SHAOYN', 'FRA-AWS', 'SFO-AWS', 'SIN-AWS'],
        zone: ['内网', '外网']
    };
    $scope.query = {
        slbId: ''
    };
    $scope.view = {
        slb: {
            "id": "",
            "name": "",
            "version": "",
            "nginx-bin": "/opt/app/nginx/sbin",
            "nginx-conf": "/opt/app/nginx/conf",
            "nginx-worker-processes": "9",
            "status": "",
            "vips": [
                {"ip": ""}
            ],
            "slb-servers": [
                {"ip": "", "host-name": ""}
            ],
            zone: '',
            idc: '',
            ispci: false
        }
    };
    $scope.model = {
        slb: {}
    };
    $scope.vipsTable = {
        columns: $scope.view.slb.vips,
        add: function (index) {
            this.columns.push({"ip": ''});
            setTimeout(function () {
                $("#editVip" + (index + 1)).hide();
                $("#vips" + (index + 1)).prop('disabled', false);
                $("#vips" + (index + 1)).bootstrapValidation();
            }, 10);
        },
        remove: function (index) {
            this.columns.splice(index, 1);
        },
        edit: function (index) {
            $("#editVip" + (index)).hide();
            $("#vips" + (index)).prop('disabled', false);
            $("#vips" + (index)).bootstrapValidation();
        }
    };
    $scope.slbServersTable = {
        columns: $scope.view.slb['slb-servers'],
        add: function (index) {
            this.columns.push({"ip": '', "host-name": ''});
            setTimeout(function () {
                $("#editSLBServer" + (index + 1)).hide();
                $("#slbServerIp" + (index + 1)).prop('disabled', false);
                $("#slbServerHostName" + (index + 1)).prop('disabled', false);
                $("#slbServerIp" + (index + 1)).bootstrapValidation();
            }, 10);
        },
        remove: function (index) {
            this.columns.splice(index, 1);
        },
        edit: function (index) {
            $("#editSLBServer" + (index)).hide();
            $("#slbServerIp" + (index)).prop('disabled', false);
            $("#slbServerHostName" + (index)).prop('disabled', false);
            $("#slbServerIp" + (index)).bootstrapValidation();
        }
    };

    $scope.clearSLBInfo = function (type) {
        if (type == 'new') {
            $scope.slbInfo.name = "";
            $scope.slbInfo.vips = [
                {"ip": ""}
            ];
            $scope.slbInfo['slb-servers'] = [
                {"ip": "", "host-name": ""}
            ];

            $scope.vipsTable.columns = $scope.slbInfo.vips;
            $scope.slbServersTable.columns = $scope.slbInfo['slb-servers'];
        }

        if (type == 'edit') {
            $scope.getSlbInfo($scope.slbId);
        }
    };
    $('.backLink').click(function () {
        window.history.back();
    });
    $scope.getZoneLanguage = function (x) {
        var resource = $scope.resource;

        if (resource && _.keys(resource).length > 0) {
            return resource['slbs']['slbs_slbsQueryApp_Zones'][x] || x;
        }
    };

    // Property Select && Unselect
    $scope.toggleIDC = function (idc) {
        if ($scope.view.slb.idc == idc) {
            $scope.view.slb.idc = '';
        } else {
            $scope.view.slb.idc = idc;
        }
    };
    $scope.isSelectedIDC = function (idc) {
        if ($scope.view.slb.idc == idc) {
            return "label-info";
        }
    };
    $scope.toggleZone = function (zone) {
        if ($scope.view.slb.zone == zone) {
            $scope.view.slb.zone = '';
        } else {
            $scope.view.slb.zone = zone;
        }
    };
    $scope.isSelectedZone = function (zone) {
        if ($scope.view.slb.zone == zone) {
            return "label-info";
        }
    };

    /**
     * New and update SLB function methods
     * */
    $("#validateAddSLBBtn").click(function (event) {
        var reviewResult = reviewData();
        if (reviewResult) {
            $('#confirmAddSLB').modal({backdrop: 'static'});
        }
    });
    $scope.newSLB = function () {
        var resource = $scope.resource;

        var e = $scope.view.slb;
        e.status = 'Default';

        delete e.id;

        var reason = $scope.query.reason;
        /*    e.properties=[];
            e.properties.push({
                    'pci': e.ispci
            });
            if(e.idc) e.properties.push({
                'idc':e.idc
            });
            if(e.zone)e.properties.push({
                'zone':e.zone
            });*/

        var newRequest = {
            method: 'POST',
            url: G.baseUrl + '/api/slb/new?description=' + reason,
            data: e
        };
        var param1 = {
            type: 'slb',
            pname: 'idc'
        };
        var param2 = {
            type: 'slb',
            pname: 'zone'
        };
        var param3 = {
            type: 'slb',
            pname: 'pci'
        };

        var request1 = {
            method: 'GET',
            url: G.baseUrl + "/api/property/set?description=" + reason,
            params: param1
        };
        var request2 = {
            method: 'GET',
            url: G.baseUrl + "/api/property/set?description=reason" + reason,
            params: param2
        };
        var request3 = {
            method: 'GET',
            url: G.baseUrl + "/api/property/set?description=reason" + reason,
            params: param3
        };

        var loading = "<img src='/static/img/spinner.gif' /> " + resource['slb/new']['slb/new_slbEditDropdownApp_progress']['title'] + "...";
        $('#operationConfrimModel').modal('show').find(".modal-body").html(loading);

        $http(newRequest).success(function (res) {
            if (res.code) {
                var msg = res.message;
                var errText = "<span class='fa fa-times'></span><span style='padding-left: 10px'> " + resource['slb/new']['slb/new_slbEditDropdownApp_progress']['fail'] + "</span>";
                $('#operationConfrimModel').modal('show').find(".modal-title").html(errText);
                $('#operationConfrimModel').modal('show').find(".modal-body").html(msg);
            } else {
                // set properties
                var a, b, c;

                var s = res.id;

                param1.pvalue = e.idc;
                param1.targetId = s;

                param2.pvalue = e.zone;
                param2.targetId = s;

                param3.pvalue = e.ispci ? 'true' : 'false';
                param3.targetId = s;

                var requestArray = [
                    $http(request3).success(function (r) {
                        if (!r.code) c = true;
                    })
                ];

                if (param1.pvalue != undefined && param1.pvalue != '') {
                    requestArray.push(
                        $http(request1).success(function (r) {
                            if (!r.code) a = true;
                        })
                    );
                }
                if (param2.pvalue != undefined && param2.pvalue != '') {
                    requestArray.push(
                        $http(request2).success(function (r) {
                            if (!r.code) b = true;
                        })
                    );
                }

                $q.all(
                    requestArray
                ).then(
                    function () {
                        $('#operationConfrimModel').modal('show').find(".modal-title").html(resource['slb/new']['slb/new_slbEditDropdownApp_progress']['tip'] + "....");
                        $('#operationConfrimModel').modal('show').find(".modal-title").html(resource['slb/new']['slb/new_slbEditDropdownApp_progress']['success']);
                        setTimeout(function () {
                            window.location.href = "/portal/slb" + "#?env=" + G.env + "&slbId=" + s;
                        }, 1000);
                    }
                );
            }
        });
    };
    $("#validateEditSLBBtn").click(function (event) {
        var reviewResult = reviewData();
        if (reviewResult) {
            $('#confirmEditSLB').modal({backdrop: 'static'});
        }
    });
    $scope.updateSLB = function () {
        var resource = $scope.resource;

        var a, b, c;
        var s = $scope.view.slb;

        var description = $scope.query.reason;
        var param1 = {
            type: 'slb',
            targetId: $scope.query.slbId,
            pname: 'zone',
            pvalue: s.zone
        };
        var param2 = {
            type: 'slb',
            targetId: $scope.query.slbId,
            pname: 'idc',
            pvalue: s.idc
        };
        var param3 = {
            type: 'slb',
            targetId: $scope.query.slbId,
            pname: 'pci',
            pvalue: s.ispci ? 'true' : 'false'
        };

        delete s.ispci;
        delete s.idc;
        delete s.zone;

        var req = {
            method: 'POST',
            url: G.baseUrl + '/api/slb/update?description=' + description,
            data: $scope.view.slb
        };
        var loading = "<img src='/static/img/spinner.gif' /> "+resource['slb/new']['slb/new_slbEditDropdownApp_progress']['opedit']+"...";
        $('#operationConfrimModel').modal('show').find(".modal-body").html(loading);
        $http(req).success(function (res) {
            if (res.code) {
                var msg = res.message;
                var errText = "<span class='fa fa-times'></span><span style='padding-left: 10px'> "+resource['slb/new']['slb/new_slbEditDropdownApp_progress']['failedit']+"</span>";
                $('#operationConfrimModel').modal('show').find(".modal-title").html(errText);
                $('#operationConfrimModel').modal('show').find(".modal-body").html(msg);
            } else {
                var request1 = {
                    method: 'GET',
                    url: G.baseUrl + '/api/property/set',
                    params: param1
                };
                var request2 = {
                    method: 'GET',
                    url: G.baseUrl + '/api/property/set',
                    params: param2
                };
                var request3 = {
                    method: 'GET',
                    url: G.baseUrl + '/api/property/set',
                    params: param3
                };

                var t = _.find($scope.model.slb.properties, function (item) {
                    return item.name.toLowerCase() == 'zone';
                });
                var old_zone = t ? t.value : '';
                t = _.find($scope.model.slb.properties, function (item) {
                    return item.name.toLowerCase() == 'idc';
                });
                var old_idc = t ? t.value : '';

                var clearZoneParam = {
                    type: 'SLB',
                    targetId: $scope.query.slbId
                };
                var clearZoneRequest = {
                    method: 'GET',
                    url: G.baseUrl + '/api/property/clear',
                    params: clearZoneParam
                };
                var clearIDCParam = {
                    type: 'SLB',
                    targetId: $scope.query.slbId
                };
                var clearIDCRequest = {
                    method: 'GET',
                    url: G.baseUrl + '/api/property/clear',
                    params: clearIDCParam
                };
                var requestArray = [
                    $http(request3).success(function (r) {
                        if (!r.code) c = true;
                    })
                ];

                if (param1.pvalue != undefined && param1.pvalue != '') {
                    requestArray.push(
                        $http(request1).success(function (r) {
                            if (!r.code) a = true;
                        })
                    );
                } else {
                    if (old_zone && old_zone != '') {
                        clearZoneParam.pname = 'zone';
                        clearZoneParam.pvalue = old_zone;
                        requestArray.push(
                            $http(clearZoneRequest).success(function (r) {
                            })
                        );
                    }
                }

                if (param2.pvalue != undefined && param2.pvalue != '') {
                    requestArray.push(
                        $http(request2).success(function (r) {
                            if (!r.code) b = true;
                        })
                    );
                } else {
                    if (old_idc && old_idc != '') {
                        clearIDCParam.pname = 'idc';
                        clearIDCParam.pvalue = old_idc;
                        requestArray.push(
                            $http(clearIDCRequest).success(function (r) {
                            })
                        );
                    }
                }
                $q.all(
                    requestArray
                ).then(
                    function () {
                        $('#operationConfrimModel').modal('show').find(".modal-title").html(resource['slb/new']['slb/new_slbEditDropdownApp_progress']['tipedit']);
                        $('#operationConfrimModel').modal('show').find(".modal-title").html(resource['slb/new']['slb/new_slbEditDropdownApp_progress']['tipedit']);
                        setTimeout(function () {
                            window.location.href = "/portal/slb" + "#?env=" + G.env + "&slbId=" + s.id;
                        }, 1000);
                    }
                );
            }
        });
    };

    // Load data
    $scope.getSlbInfo = function (isnew) {
        var param = {
            type: 'extended',
            slbId: $scope.query.slbId
        };
        var request = {
            method: 'GET',
            url: G.baseUrl + '/api/slb',
            params: param
        };
        $http(request).success(function (res) {
            $scope.model.slb = $.extend(true, {}, res);

            var e = $.extend(true, {}, res);
            var pci = _.find(e.properties, function (item) {
                return item.name.toLowerCase() == 'pci';
            });
            if (pci) e.ispci = pci.value == "true";

            var zone = _.find(e.properties, function (item) {
                return item.name.toLowerCase() == 'zone';
            });
            if (zone) e.zone = zone.value;

            var idc = _.find(e.properties, function (item) {
                return item.name.toLowerCase() == 'idc';
            });
            if (idc) e.idc = idc.value;

            delete e.tags;
            delete e.properties;
            if (isnew) {
                e['slb-servers'] = [
                    {
                        'host-name': '',
                        ip: ''
                    }
                ];
            }

            if (isnew) {
                e.id = '';
                e.name = e.name + '_copy';
            }
            $scope.view.slb = e;
            $scope.vipsTable.columns = e.vips;
            $scope.slbServersTable.columns = e['slb-servers'];
        });
    };
    $scope.loadData = function (hashData) {
        if (hashData.slbId) {
            $scope.query.slbId = hashData.slbId;
            var location = window.location.pathname;
            var isnew = location.indexOf('new') > 0;
            $scope.getSlbInfo(isnew);
        }
    };
    $scope.hashChanged = function (hashData) { $scope.resource = H.resource;
        $scope.resource = H.resource;
        $scope.loadData(hashData);
    };
    H.addListener("slbEditApp", $scope, $scope.hashChanged);

    // Common Functions
    function reviewData() {
        var result = true;
        $.each($('[data-validator-type="validation"]'), function (i, element) {
            if (!$(element).bootstrapValidation('validate'))
                result = false;
        });
        return result;
    }
});
angular.bootstrap(document.getElementById("slb-edit-area"), ['slbEditApp']);

var slbEditDropdownApp = angular.module('slbEditDropdownApp', ["angucomplete-alt", "http-auth-interceptor"]);
slbEditDropdownApp.controller('slbEditDropdownController', function ($scope, $http) {

    $scope.context = {
        targetIdName: 'slbId',
        targetNameArr: ['id', 'name'],
        targetsUrl: '/api/meta/slbs',
        targetsName: 'slbs'
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
        return G.baseUrl + "/api/meta/slbs";
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
                if ($scope.target.id) {
                    if ($scope.targets[$scope.target.id])
                        $scope.target.name = $scope.targets[$scope.target.id].id + "/" + $scope.targets[$scope.target.id].name;
                    else {
                        $http.get(G.baseUrl + "/api/slb?slbId=" + $scope.target.id + "&type=info").success(
                            function (res) {
                                $scope.target.name = $scope.target.id + "/" + res.name;
                            }
                        );
                    }
                }
            }
        );
    };

    $scope.selectTarget = function (t) {
        if (t) {
            var toId = t.originalObject.id;
            if ($scope.target.id != toId) {
                $scope.$broadcast('angucomplete-alt:clearInput', 'targetSelector');
                var pairs = {};
                pairs[$scope.context.targetIdName] = toId;
                H.setData(pairs);
            }
        }
    };

    $('#confirmClone').click(
        function () {
            var timestamp = new Date().getTime();
            H.setData({'timestamp': timestamp});
        }
    );

    $scope.cancelClone = function () {
        H.setData({"slbId": ""});
    };

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
            $scope.target.name = "下拉选择slb进行克隆";
        }
    };
    H.addListener("slbEditDropdownApp", $scope, $scope.hashChanged);
});
angular.bootstrap(document.getElementById("slb-edit-dropdown-area"), ['slbEditDropdownApp']);
