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
            case 'release': {
                link = "/portal/group/release#?env=" + G.env + "&groupId=" + $scope.query.groupId;
                break;
            }
            case 'conf': {
                link = "/portal/group/conf#?env=" + G.env + "&groupId=" + $scope.query.groupId;
                break;
            }
            case 'delegate': {
                link = "/portal/group/delegate-rule#?env=" + G.env + "&groupId=" + $scope.query.groupId;
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
            case 'rule': {
                link = "/portal/group/rule#?env=" + G.env + "&groupId=" + $scope.query.groupId;
                break;
            }
            default:
                break;
        }
        return link;
    };
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
    $scope.hashChanged = function (hashData) {
        $scope.resource = H.resource;
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
    $scope.hashChanged = function (hashData) {
        $scope.resource = H.resource;
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

var operationLogApp = angular.module('operationLogApp', ['http-auth-interceptor', 'ngSanitize']);
operationLogApp.controller('operationLogController', function ($scope, $http, $q) {
    var ruletype = 'SHARDING_RULE';
    $scope.data = {
        "relation": {
            "and": "AND",
            "or": "OR"
        },

        "group": {},

        "rule": {
            "target-type": "Group",
            "target-id": "",
            "rule-type": ruletype,
            "name": "",
            "attributes": ""
        },

        "cookies": [],
        "headers": [],
        "uris": []
    };

    $scope.query = {
        "awsweight": 0,
        "shanghaiweight": 100,

        "relation": "",
        "uri": {
            "path": "",
            "op": "="
        },
        "header": {
            "key": "",
            "value": "",
            "op": "="
        },
        "cookie": {
            "key": "",
            "value": "",
            "op": "="
        }
    };

    $scope.getStatus = function () {
        var group = $scope.data.group;
        if (!group || _.keys(group).length == 0) return;

        var properties = _.indexBy(group['properties'], 'name');
        var status = properties['status'].value.toLowerCase();
        switch (status) {
            case 'activated':
                return '已激活';
            case 'tobeactivated':
                return '有变更';
            case 'deactivated':
                return '未激活';
            default:
                return status;
        }
    };

    $scope.statusClass = function () {
        var group = $scope.data.group;
        if (!group || _.keys(group).length == 0) return;

        var properties = _.indexBy(group['properties'], 'name');
        var status = properties['status'].value.toLowerCase();
        switch (status) {
            case 'activated':
                return 'status-green';
            case 'tobeactivated':
                return 'status-yellow';
            case 'deactivated':
                return '';
            default:
                return status;
        }
    };

    $scope.showAlert = function () {
        var status = $scope.getStatus();
        if (status == '有变更') return true;

        return false;
    };

    $scope.addRequestUri = function () {
        init();
        $('#uriDialog').modal('show');
    };

    $scope.addRequestHeader = function () {
        init();

        $('#headerDialog').modal('show');
    };

    $scope.addRequestCookie = function () {
        init();

        $('#cookieDialog').modal('show');
    };

    $scope.disableUri = function () {
        var uri = $scope.query.uri;

        if (!uri['path']) return true;

        return false;
    };

    $scope.disableHeader = function () {
        var header = $scope.query.header;
        if (!header['key'] || !header['value']) return true;
        return false;
    };

    $scope.disableCookie = function () {
        var cookie = $scope.query.cookie;
        if (!cookie['key'] || !cookie['value']) return true;
        return false;
    };

    $scope.changeUriFunction = function () {
    };

    $scope.saveUri = function () {
        var uri = $.extend(true, {}, $scope.query.uri);

        var t = $scope.data.uris;

        var p = selecteduri ? selecteduri : uri;
        t = _.reject(t, function (v) {
            return v.path == p.path && v.op == p.op;
        });
        t.push(uri);


        $scope.data.uris = t;

        $('#requesturi').bootstrapTable("load", t);
    };

    $scope.saveHeader = function () {
        var header = $scope.query.header;

        var t = $scope.data.headers;
        var p = selectedheader ? selectedheader : header;

        t = _.reject(t, function (v) {
            return v.key == p.key && v.op == p.op && v.value == p.value;
        });

        t.push(header);
        $scope.data.headers = t;

        $('#requestheader').bootstrapTable("load", t);
    };

    $scope.saveCookie = function () {
        var cookie = $scope.query.cookie;

        var t = $scope.data.cookies;
        var p = selectedcookie ? selectedcookie : cookie;

        t = _.reject(t, function (v) {
            return v.key == p.key && v.op == p.op && v.value == p.value;
        });

        t.push(cookie);
        $scope.data.cookies = t;

        $('#requestcookie').bootstrapTable("load", t);
    };

    $scope.toggleRelation = function (v) {
        $scope.query.relation = v;
    };

    $scope.relationClass = function (v) {
        return $scope.query.relation == v ? 'label label-info' : 'label';
    };

    $scope.saveRule = function () {
        var uris = $scope.data.uris || [];
        var headers = $scope.data.headers || [];
        var cookies = $scope.data.cookies || [];
        var condition = $scope.query.relation;
        var groupId = $scope.query.groupId;
        var shanghaiweight = $scope.query.shanghaiweight;

        var existedRule = $scope.data.rule;
        // save the rule

        var atts = [];
        _.map(uris, function (v) {
            atts.push({
                "type": "self", "target": "uri", "function": v.op, "value": v.path
            });
        });

        _.map(headers, function (v) {
            atts.push({
                "type": "self", "target": "$header_" + v.key, "function": v.op, "value": v.value
            });
        });

        _.map(cookies, function (v) {
            atts.push({
                "type": "self", "target": "$cookie_" + v.key, "function": v.op, "value": v.value
            });
        });


        var rule;
        if (existedRule && existedRule.id) {
            rule = existedRule;
            rule['attributes'] = {
                "condition": {
                    "type": condition.toLowerCase(),
                    "composit": atts
                },
                "percent": shanghaiweight / 100,
                "enable": existedRule['attributes'].enable === false ? false : true
            };
        }
        else {
            rule = {
                "target-type": "Group",
                "target-id": groupId,
                "rule-type": ruletype,
                "name": "delegate_rule_for_group_" + groupId,
                "attributes": {
                    "condition": {
                        "type": condition.toLowerCase(),
                        "composit": atts
                    },
                    "percent": shanghaiweight / 100
                }
            };
        }


        if (atts.length>0 && atts.length < 2){
            rule['attributes']['condition']['type']='and';
        }else if(atts.length==0){
            delete rule['attributes']['condition'];
        }

        rule['attributes'] = JSON.stringify(rule['attributes']);
        var rules = {
            "target-type": "Group",
            "target-id": groupId,
            "rules": [
                rule
            ]
        };

        var request = {
            method: 'POST',
            url: G.baseUrl + '/api/rule/set?description=新建回原上海的Rule',
            data: rules
        };

        var activateRequest = {
            method: 'GET',
            url: G.baseUrl + '/api/activate/group?groupId=' + groupId + "&description=activate"
        };

        $http(request).success(function (response, code) {
            if (code == 200) {
                $http(activateRequest).success(function (response2, code2) {
                    if (code2 == 200) {
                        alert('Save succeed!');
                        H.setData({"timeStamp": new Date().getTime()});
                    } else {
                        alert('Activate failed, reason:' + response2.message);
                    }
                });
            } else {
                // fail to save
                alert('Failed to save rule, error message: ' + response.message);
            }
        });
    };

    $scope.disableSaving = function () {
        var group = $scope.data.group;
        var uris = $scope.data.uris || [];
        var headers = $scope.data.headers || [];
        var cookies = $scope.data.cookies || [];
        var condition = $scope.query.relation;


        if (!group) return;

        var properties = _.indexBy(group['properties'], 'name');
        var status = properties['status'] ? properties['status']['value'] : '-';

        var collections = uris.concat(headers).concat(cookies);

        var result = status.toLowerCase() != 'activated';

        if (result) return result;

        if (collections.length > 1) {
            return !condition;
        }
        return false;
    };

    // Common methods
    $scope.loadData = function () {
        var groupId = $scope.query.groupId;
        var request = {
            url: G.baseUrl + '/api/group?groupId=' + groupId + '&type=extended',
            method: 'GET'
        };
        $http(request).success(function (response, code) {
            if (code != 200) {
                alert('Failed to get group with error message: ' + response.message);
                return;
            }
            $scope.data.group = response;
            var rule = _.find(response['rule-set'], function (v) {
                return v['rule-type'] == ruletype;
            });
            if (!rule || _.keys(rule).length == 0) {
                // no rule
            } else {
                rule.attributes = JSON.parse(rule.attributes);
                $scope.data.rule = rule;
            }

            applyRule($scope.data.rule);
        });
    };

    $scope.applyHashData = function (hashData) {
        $scope.env = hashData.env;
        $scope.query.groupId = hashData.groupId;
    };

    function applyRule(rule) {
        var ruleId = rule.id;

        if (ruleId) {
            // update headers,uris,cookies
            var attributes = rule['attributes'];
            var conditions = attributes['condition'];
            var uris = [];
            var headers = [];
            var cookies = [];
            var conditiontype = '';
            if (conditions) {
                conditiontype = conditions['type'].toLowerCase();
                var composits = conditions['composit'];
                if (composits && composits.length > 0) {
                    for (var i = 0; i < composits.length; i++) {
                        var c = composits[i];
                        if (c['target'].indexOf('$header_') == 0) {
                            // header
                            headers.push({
                                "key": c['target'].substring(8, c['target'].length),
                                "value": c['value'],
                                "op": c['function']
                            });
                        } else if (c['target'].indexOf('$cookie_') == 0) {
                            cookies.push({
                                "key": c['target'].substring(8, c['target'].length),
                                "value": c['value'],
                                "op": c['function']
                            });
                        } else {
                            // uri
                            uris.push({
                                "path": c['value'],
                                "op": c['function']
                            })
                        }
                    }
                }
            }
            // Update percent
            var percent = attributes['percent'] * 100;


            $scope.query.headers = headers;
            $scope.query.uris = uris;
            $scope.query.cookies = cookies;

            $scope.data.headers = headers;
            $scope.data.uris = uris;
            $scope.data.cookies = cookies;
            
            $scope.query.condition = conditiontype;
            $scope.query.relation = conditiontype;
            $scope.query.shanghaiweight = percent;
            $scope.query.awsweight = 100 - percent;


        }
        // For percentage
        $('#aws').html($scope.query.awsweight + '%');
        $('#shanghai').html($scope.query.shanghaiweight + '%');
        var slider = $("#range_02").data("ionRangeSlider");
        if (!slider) {
            $("#range_02").ionRangeSlider({
                min: 0,
                max: 100,
                from: $scope.query.awsweight,
                onChange: function (v) {
                    $scope.query.shanghaiweight = 100 - v.from;
                    $scope.query.awsweight = v.from;
                    $('#aws').html(v.from + '%');
                    $('#shanghai').html((100 - v.from) + '%');
                }
            });
        } else {
            slider.update({
                from: $scope.query.awsweight
            });
        }
        // bind the data into table
        $('#requesturi').bootstrapTable("removeAll");
        $('#requestheader').bootstrapTable("removeAll");
        $('#requestcookie').bootstrapTable("removeAll");

        $('#requesturi').bootstrapTable("load", $scope.query.uris);
        $('#requestheader').bootstrapTable("load", $scope.query.headers);
        $('#requestcookie').bootstrapTable("load", $scope.query.cookies);
    }

    function initTables() {
        $('#requesturi').bootstrapTable({
            toolbar: "#uri",
            columns: [[
                {
                    field: 'state',
                    checkbox: 'true',
                    align: 'center',
                    valign: 'middle'
                },
                {
                    field: 'op',
                    title: "Operation Logic",
                    align: 'left',
                    valign: 'middle',
                    width: '350px',
                    sortable: true,
                    formatter: function (value, row, index) {
                        return value;
                    }
                },
                {
                    field: 'path',
                    title: "URI",
                    align: 'left',
                    valign: 'middle',
                    width: '830px',
                    sortable: true,
                    formatter: function (value, row, index) {
                        return value;
                    }
                },

                {
                    title: "Operation",
                    align: 'center',
                    valign: 'middle',
                    sortable: true,
                    events: operateEvents,
                    formatter: function (v, row, index) {
                        var str = '<div>' +
                            '<button type="button" class="btn-op btn btn-info btn-little edit-uri-bt  " title="修改"><span class="fa fa-edit"></span></button>' +
                            '<button style="margin-left:5px" type="button" class="btn-little btn-op btn btn-info delete-uri-bt  " title="删除" aria-label="Left Align"><span class="fa fa-minus"></span></button>' +
                            '</div>';
                        return str;
                    }
                }
            ], []],
            sortName: 'path',
            sortOrder: 'desc',
            data: [],
            classes: "table-bordered  table-hover table-striped table",
            minimumCountColumns: 2,
            idField: 'id',
            resizable: true,
            resizeMode: 'overflow',
            formatLoadingMessage: function () {
                return "<img class='loading-img' src='/static/img/loading_100.gif' /> Loading URIs";
            },
            formatNoMatches: function () {
                return '<i class="fa status-alert fa-exclamation-triangle" aria-hidden="true"></i> No URIs Specified';
            }
        });

        $('#requestheader').bootstrapTable({
            toolbar: "#header",
            columns: [[
                {
                    field: 'state',
                    checkbox: 'true',
                    align: 'center',
                    valign: 'middle'
                },

                {
                    field: 'key',
                    title: "Header Key",
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    formatter: function (value, row, index) {
                        return value;
                    }
                },

                {
                    field: 'op',
                    title: "Operation Logic",
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    formatter: function (value, row, index) {
                        return value;
                    }
                },


                {
                    field: 'value',
                    title: "Header Value",
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    formatter: function (value, row, index) {
                        return value;
                    }
                },

                {
                    title: "Operation",
                    align: 'center',
                    valign: 'middle',
                    sortable: true,
                    events: operateEvents,
                    formatter: function (v, row, index) {
                        var str = '<div>' +
                            '<button type="button" class="btn-op btn btn-info btn-little edit-header-bt  " title="修改"><span class="fa fa-edit"></span></button>' +
                            '<button style="margin-left:5px" type="button" class="btn-little btn-op btn btn-info delete-header-bt  " title="删除" aria-label="Left Align"><span class="fa fa-minus"></span></button>' +
                            '</div>';
                        return str;
                    }
                }
            ], []],
            sortName: 'key',
            sortOrder: 'desc',
            data: [],
            classes: "table-bordered  table-hover table-striped table",
            minimumCountColumns: 2,
            idField: 'id',
            resizable: true,
            resizeMode: 'overflow',
            formatLoadingMessage: function () {
                return "<img class='loading-img' src='/static/img/loading_100.gif' /> Loading Headers Settings";
            },
            formatNoMatches: function () {
                return '<i class="fa status-alert fa-exclamation-triangle" aria-hidden="true"></i> No Headers Specified';
            }
        });

        $('#requestcookie').bootstrapTable({
            toolbar: "#cookie",
            columns: [[
                {
                    field: 'state',
                    checkbox: 'true',
                    align: 'center',
                    valign: 'middle'
                },

                {
                    field: 'key',
                    title: "Cookie Key",
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    formatter: function (value, row, index) {
                        return value;
                    }
                },

                {
                    field: 'op',
                    title: "Operation Logic",
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    formatter: function (value, row, index) {
                        return value;
                    }
                },


                {
                    field: 'value',
                    title: "Cookie Value",
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    formatter: function (value, row, index) {
                        return value;
                    }
                },

                {
                    title: "Operation",
                    align: 'center',
                    valign: 'middle',
                    sortable: true,
                    events: operateEvents,
                    formatter: function (v, row, index) {
                        var str = '<div>' +
                            '<button type="button" class="btn-op btn btn-info btn-little edit-cookie-bt  " title="修改"><span class="fa fa-edit"></span></button>' +
                            '<button style="margin-left:5px" type="button" class="btn-little btn-op btn btn-info delete-cookie-bt  " title="删除" aria-label="Left Align"><span class="fa fa-minus"></span></button>' +
                            '</div>';
                        return str;
                    }
                }
            ], []],
            sortName: 'key',
            sortOrder: 'desc',
            data: [],
            classes: "table-bordered  table-hover table-striped table",
            minimumCountColumns: 2,
            idField: 'id',
            resizable: true,
            resizeMode: 'overflow',
            formatLoadingMessage: function () {
                return "<img class='loading-img' src='/static/img/loading_100.gif' /> Loading Cookies Settings";
            },
            formatNoMatches: function () {
                return '<i class="fa status-alert fa-exclamation-triangle" aria-hidden="true"></i> No Cookies Specified';
            }
        });


    };

    function init() {
        selecteduri = '';
        selectedheader = '';
        selectedcookie = '';

        $scope.query.uri = {
            "path": "",
            "op": "="
        };
        $scope.query.cookie = {
            "key": "",
            "value": "",
            "op": "="
        };
        $scope.query.header = {
            "key": "",
            "value": "",
            "op": "="
        };
    }

    var selecteduri;
    var selectedheader;
    var selectedcookie;

    window.operateEvents = {
        'click .edit-cookie-bt': function (e, value, row) {
            $scope.$apply(function () {
                selectedcookie = {
                    "key": row.key,
                    "value": row.value,
                    "op": row.op
                };

                $scope.query.cookie = {
                    "key": row.key,
                    "value": row.value,
                    "op": row.op
                };
                $('#headerDialog').modal('show');
            });
        },
        'click .edit-uri-bt': function (e, value, row) {
            $scope.$apply(function () {
                selecteduri = {
                    "path": row.path,
                    "op": row.op
                };

                $scope.query.uri = {
                    "path": row.path,
                    "op": row.op
                };
                $('#uriDialog').modal('show');
            });
        },
        'click .edit-header-bt': function (e, value, row) {
            $scope.$apply(function () {
                selectedheader = {
                    "key": row.key,
                    "value": row.value,
                    "op": row.op
                };

                $scope.query.header = {
                    "key": row.key,
                    "value": row.value,
                    "op": row.op
                };
                $('#headerDialog').modal('show');
            });
        },
        'click .delete-cookie-bt': function (e, value, row) {
            $scope.data.cookies = _.reject($scope.data.cookies, function (v) {
                return v['key'] == row.key && v['value'] == row.value && v['op'] == row.op;
            });

            $('#requestcookie').bootstrapTable("load", $scope.data.cookies);
        },
        'click .delete-uri-bt': function (e, value, row) {
            $scope.data.uris = _.reject($scope.data.uris, function (v) {
                return v['path'] == row.path && v['op'] == row.op;
            });

            $('#requesturi').bootstrapTable("load", $scope.data.uris);
        },
        'click .delete-header-bt': function (e, value, row) {
            $scope.data.headers = _.reject($scope.data.headers, function (v) {
                return v['key'] == row.key && v['value'] == row.value && v['op'] == row.op;
            });

            $('#requestheader').bootstrapTable("load", $scope.data.headers);
        }
    };

    $scope.hashChanged = function (hashData) {
        $scope.resource = H.resource;
        $scope.applyHashData(hashData);
        initTables();
        $scope.loadData();
    };
    H.addListener("operationLogApp", $scope, $scope.hashChanged);
});
angular.bootstrap(document.getElementById("operation-log-area"), ['operationLogApp']);

