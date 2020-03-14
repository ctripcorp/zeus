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


//InfoLinksComponent: info links
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


var operationLogApp = angular.module('operationLogApp', ['http-auth-interceptor', 'ngSanitize']);
operationLogApp.controller('operationLogController', function ($scope, $http, $q) {
    var pageRulesSnapShot = [];

    var pagerules = [];

    $scope.query = {
        ruleId: -1,
        index: -1
    };

    $scope.slb = {
        online: '',
        offline: ''
    };

    $scope.condition = {
        composit: {},
        action: {},

        type: '',
        target: '',
        'function': '',
        value: ''
    };

    $scope.relations = {
        并且关系: 'and',
        或者关系: 'or'
    };
    var parentRules = {};
    $scope.parentRules = parentRules;

    // Table area
    $scope.canActivateRule = function () {
        var offline = $scope.slb.offline;
        if (!offline) return false;
        return A.canDo("Slb", "ACTIVATE", offline.id);
    };


    $scope.showNewRuleBt = function () {
        var offline = $scope.slb.offline;
        if (!offline) return false;
        return A.canDo("Slb", "UPDATE", offline.id);
    };

    $scope.createNewRule = function () {
        $scope.condition = {
            composit: {},
            action: {},

            type: '',
            target: '',
            'function': '',
            value: ''
        };
        $('#newRuleModel').modal('show');
    };

    // Condition area
    $scope.newConditionClick = function (type) {
        var condition;
        switch (type) {
            case 'header':
            case 'cookie':
            case 'arg': {
                condition = {
                    target: type,
                    'function': '=',
                    "key": '',
                    "value": ''
                };
                break;
            }
            case 'uri':
            case 'url': {
                condition = {
                    target: type,
                    'function': '=',
                    "value": ''
                };
                break;
            }
            case 'countheader':
            case 'countcookie': {
                condition = {
                    target: type,
                    'function': '=',
                    count: ''
                };
                break;
            }

           /* case 'cip': {
                $scope.query.cip=true;

                return;
            }*/

            case 'cip': {
                condition = {
                    target: type,
                    'function': '=',
                    ip: ''
                };
                break;
            }

            case 'method': {
                condition = {
                    target: type,
                    'function': '=',
                    method: 'GET'
                };
                break;
            }
            default:
                break;
        }

        if (!$scope.condition.composit[type]) {
            $scope.condition.composit[type] = [];
        }

        $scope.condition.composit[type].push(condition);
    };

    $scope.deleteRow = function (index, type) {
        $scope.condition.composit[type].splice(index, 1);
    };

    $scope.showEmptyConitionMsg = function () {
        var composits = $scope.condition.composit;
        if (!composits) return false;

        return _.keys(composits) == 0;
    };

    $scope.showConditionActionError = function () {
        var condition = $scope.condition;
        if (!condition) return;
        var action = condition.action;
        return _.keys(action) == 0;
    }

    $scope.showConditionJoin = function () {
        var composits = $scope.condition.composit;
        if (!composits) return false;

        var v = _.flatten(_.values(composits));

        return v.length > 1;
    };

    $scope.isSelectedRelation = function (k, v) {
        if ($scope.condition.type == v) {
            return "label-info";
        }
    };

    $scope.toggleRelation = function (k, v) {
        $scope.condition.type = v;
    };

    $scope.checkNull = function (composit) {
        composit['value'] == 'nil' ? composit['value'] = '' : composit['value'] = 'nil';
    };
    $scope.changeFunction = function (composit, key) {
        composit[key] = '';
    };

    // Action area
    $scope.newActionClick = function (type) {
        var action = {};
        switch (type) {
            case 'reject': {
                action = {
                    type: type,
                    message: '',
                    code: '433'
                };
                break;
            }
            case 'flag': {
                action = {
                    type: type,
                    headers: [
                        {
                            key: '',
                            value: ''
                        }
                    ]
                };
                break;
            }
            case 'redirect': {
                action = {
                    type: type,
                    target: '',
                    code: '301'
                };
                break;
            }
            case 'proxypass': {
                action = {
                    type: type,
                    target: ''
                };
                break;
            }
            default:
                break;
        }

        $scope.condition.action = action;
    };

    $scope.addHeaderRow = function () {
        var action = $scope.condition.action;
        if (!action) return;
        var headers = action.headers;
        if (!headers) return;
        headers.push({
            key: '',
            value: ''
        });
    };

    $scope.deleteHeaderRow = function (index) {
        var action = $scope.condition.action;
        if (!action) return;
        var headers = action.headers;
        if (!headers) return;
        headers.splice(index, 1);

    };

    $scope.showAddHeaderBt = function (index) {
        var action = $scope.condition.action;
        if (!action) return;
        var headers = action.headers;
        if (!headers) return;

        return index === headers.length - 1;
    };

    $scope.showRemoveHeaderBt = function (index) {
        var action = $scope.condition.action;
        if (!action) return;
        var headers = action.headers;
        if (!headers) return;

        return headers.length != 1;
    };

    $scope.showDeleteComposite = function () {
        var compiste = $scope.condition.composit;
        var typeCount = _.keys(compiste).length > 1;

        if (typeCount) {
            return true;
        } else {
            var eachCompisteCount = _.values(compiste);
            return _.flatten(eachCompisteCount).length > 1 || eachCompisteCount.length > 1;
        }

        return false;
    };

    $scope.disableAddCondition = function () {
        var condition = $scope.condition;

        var composit = condition['composit'];
        var type = condition['type'];

        var action = condition['action'];

        var passed = true;

        // Action
        var actionsLength = _.keys(action).length > 0;
        if (!actionsLength) {
            passed = false;
        } else {
            for (var k in action) {
                var v = action[k];
                if (v) {
                    if (typeof v == "string") {
                        passed = v != undefined && v != "";
                    } else if (typeof v == "number") {
                        passed = v != undefined && v != "" && v > 0;
                    } else if (typeof v == "object") {
                        if (k == "headers") {
                            for (var i = 0; i < v.length; i++) {
                                passed = v[i]['key'] && v[i]['value'] && validateKey(v[i]['key']);
                                if (!passed) return true;
                            }
                        }
                    }
                } else {
                    passed = false;
                }

                if (!passed) return true;
            }
        }
        if (!passed) return true;

        // Composite
        var compositLength = _.keys(composit).length > 0;
        if (!compositLength) {
            passed = false;
        } else {
            var compositValue = _.flatten(_.values(composit));

            if (compositValue.length > 1 && !type) {
                return true;
            }

            for (var i = 0; i < compositValue.length; i++) {
                var v = compositValue[i];
                for (var vs in v) {
                    if (v[vs]) {
                        if (typeof v[vs] == "string") {
                            passed = v[vs] != undefined && v[vs].length > 0;
                        } else if (typeof v[vs] == "number") {
                            passed = v[vs] != undefined && v[vs] > 0;
                        }
                        if (passed && vs == 'ip' && (v['function'] == '=' || v['function'] == '!=')) {
                            passed = validateIp(v[vs]);
                        }
                        if (passed && vs == 'key') {
                            passed = validateKey(v[vs]);
                        }
                    } else {
                        passed = false;
                    }

                    if (!passed) return true;
                }
            }
        }

        if (!passed) return true;

        var join = condition.type;
        if ((!join || join == 'self') && _.flatten(_.values(composit)).length > 1) return true;

        return false;
    };

    $scope.saveCondition = function () {
        var ruleId = $scope.query.ruleId;
        var slbId = $scope.query.slbId;
        var index = $scope.query.index;
        var condition = $scope.condition;

        var composit = condition.composit;
        var compositValues = _.flatten(_.values(composit));

        var result = {};
        if (compositValues.length == 1) {
            result = getCondition(compositValues[0]);
            result.type = 'self';
        } else {
            // multiple composits
            result.type = condition.type;
            result.composit = [];
            for (var i = 0; i < compositValues.length; i++) {
                var c = compositValues[i];
                result.composit.push(getCondition(c));
            }
        }

        var att = {
            'condition': result,
            'condition-action': condition.action
        };


        var rule = {
            'target-type': 'Slb',
            'target-id': slbId,
            'rule-type': 'REQUEST_INTERCEPT_RULE',
            'name': 'rule_for_slb_' + slbId,
            'attributes': angular.toJson(att)
        };

        if (ruleId > 0) {
            rule.id = ruleId;
        }

        var pagerules = $scope.pagerules;

        if (index >= 0) {
            for (var i = 0; i < pagerules.length; i++) {
                if (index == i) {
                    pagerules[i] = rule;
                }
            }
        } else {
            pagerules.push(rule);
        }
    };

    $scope.showConditionActionText = function () {
        var condition = $scope.condition;
        var action = condition['action'];
        if (!action) return false;

        var type = action.type;
        return type;
    };

    $scope.getConditionActionText = function () {
        var condition = $scope.condition;
        var action = condition['action'];
        if (!action) return;

        var type = action.type.toLowerCase();
        if (!type) return;

        var text = '';
        switch (type) {
            case 'reject': {
                text = '拒绝请求并返回错误信息';
                break;
            }
            case 'flag': {
                text = '标记请求并转发';
                break;
            }
            case 'redirect': {
                text = '转发请求到新的地址';
                break;
            }
            case 'proxypass': {
                text = '透传到新的地址';
                break;
            }
            default: {
                text = '';
                break;
            }
        }
        return text;
    };

    $scope.showHelpMsg = function () {

    };

    function getCondition(c) {
        var result = {
            type: 'self'
        };
        var ctype = c['target'];
        switch (ctype) {
            case 'header':
            case 'cookie':
            case 'arg': {
                var key = c['key'];
                var value = c['value'];
                result.target = '$' + ctype + '_' + key;
                result.function = c['function'];
                result.value = value;
                break;
            }
            case 'uri':
            case 'url': {
                result.target = ctype;
                result.function = c['function'];
                result.value = c['value'];
                break;
            }
            case 'countheader':
            case 'countcookie': {
                var count = c['count'];
                result.target = '$' + ctype;
                result.function = c['function'];
                result.value = count;
                break;
            }
            case 'cip': {
                result.target = '$' + ctype;
                result.function = c['function'];
                result.value = c['ip'];
                break;
            }
            case 'method': {
                result.target = ctype;
                result.function = c['function'];
                result.value = c['method'];
                break;
            }
            default: {
                break;
            }
        }

        return result;
    }

    // Table show area
    $scope.translateCondition = function (rule) {
        if (!rule || !rule['attributes']) return;
        var attribute = JSON.parse(rule['attributes']);

        var condition = attribute['condition'];
        if (!condition) return;

        var type = condition['type'];
        var target = condition['target'];
        var func = condition['function'];
        var value = condition['value'];


        var str = '';
        if (type == 'self') {
            str += getConditionText(target, func, value);
        } else {
            var comps = condition['composit'];
            for (var i = 0; i < comps.length; i++) {
                var com = comps[i];
                str += getConditionText(com.target, com.function, com.value);
                if (i != comps.length - 1) {
                    var t = type.toLowerCase() == 'and' ? ', 并且' : ', 或者';
                    str += '<div>' + t + '</div>'
                }
            }
        }

        return str;
    };

    $scope.translateActionType = function (rule) {
        if (!rule) return;
        var attribute = JSON.parse(rule['attributes']);
        if (!attribute || !attribute['condition-action']) return;

        var action = attribute['condition-action'];
        var str = '';

        var type = action['type'];
        if (!type) return;

        type = type.toLowerCase();
        if ('reject' == type) {
            str = '<div>拒绝请求</div>';
        } else if ('flag' == type) {
            str = '<div>标识请求</div>';
        } else if ('redirect' == type) {
            str = '<div>Redirect跳转</div>';
        } else if ('proxypass' == type) {
            str += '<div>定向转发</div>';
        }

        return str;
    };

    $scope.translateAction = function (rule) {
        if (!rule) return;
        var attribute = JSON.parse(rule['attributes']);
        if (!attribute || !attribute['condition-action']) return;

        var action = attribute['condition-action'];
        var str = '';
        var type = action['type'];
        var code = action['code'];
        var message = action['message'];

        var target = action['target'];

        if (!type) return;

        type = type.toLowerCase();
        if ('reject' == type) {
            str += '<div>' +
                '<div>' +
                '返回状态码:<b class="function">' + code + '</b>, 错误信息:<b class="function">' + message + '</b>' +
                '</div>' +
                '</div>';
        } else if ('flag' == type) {
            var headers = action['headers'];
            str += '<div>';
            for (var i = 0; i < headers.length; i++) {
                str += '<div>' +
                    '标记新的Header,Header键:<b class="function">' + headers[i].key + '</b>; Header值:<b class="function">' + headers[i].value + '</b>' +
                    '</div>';
            }
            str += '</div>';
        } else if ('redirect' == type) {
            str += '<div>返回状态码:<b class="function">' + code + '</b>, 目标地址:<b class="function">' + target + '</b></div>';
        } else if ('proxypass' == type) {
            str += '<div>目标地址:<b class="function">' + target + '</b></div>';
        }

        str += '</div>';
        return str;
    };

    function getConditionText(target, func, value) {
        var map = {
            '~': '正则等于',
            '~*': '忽略大小写正则等于',
            '!~': '正则不等于',
            '!~*': '忽略大小写正则不等于'
        };
        func = map[func] ? map[func] : func;

        var str = '';
        str += '<div class="condition">';
        if (target.indexOf('$header') == 0) {
            var headerKey = target.substring(8, target.length);
            var headerValue = value;
            str += '<div>当请求Header中存在: 键' + headerKey + ' <b class="function">' + func + '</b> ' + headerValue + '</div>';
        } else if (target.indexOf('uri') == 0) {
            str += '<div>当请求URI' + target.substring(4, target.length) + ' <b class="function">' + func + '</b> ' + value + '</div>';
        } else if (target.indexOf('url') == 0) {
            str += '<div>当请求URL' + target.substring(4, target.length) + ' <b class="function">' + func + '</b> ' + value + '</div>';
        } else if (target.indexOf('$cookie') == 0) {
            var cookieKey = target.substring(8, target.length);
            var cookieValue = value;
            str += '<div>当请求Cookie中存在: 键' + cookieKey + ' <b class="function">' + func + '</b> ' + cookieValue + '</div>';
        } else if (target.indexOf('$arg') == 0) {
            var argKey = target.substring(5, target.length);
            var argValue = value;
            str += '<div>当请求请求参数中存在: 键' + argKey + ' <b class="function">' + func + '</b> ' + argValue + '</div>';
        } else if (target == '$countcookie') {
            var cookieCount = value;
            str += '<div>当请求中Cookie个数 <b class="function">' + func + '</b> ' + cookieCount + '个</div>';
        } else if (target == '$countheader') {
            var headerCount = value;
            str += '<div>当请求中Header个数 <b class="function">' + func + '</b> ' + headerCount + '个</div>';
        } else if (target == '$cip') {
            var ip = value;
            str += '<div>当请求源IP <b class="function">' + func + '</b> ' + ip + '</div>';
        }
        else if (target == 'method') {
            str += '<div>当请求Method <b class="function">' + func + '</b> ' + value + '</div>';
        }
        str += '</div>';
        return str;
    }

    // Table operation
    $scope.moveUp = function (index, rule) {
        var pagerules = $scope.pagerules;
        var cindex = index - 1;
        if (cindex < 0) return;

        // swap
        var t = pagerules[cindex];
        pagerules[cindex] = pagerules[index];
        pagerules[index] = t;
    };

    $scope.moveDown = function (index, rule) {
        var pagerules = $scope.pagerules;
        var cindex = index + 1;
        if (cindex > pagerules.length - 1) return;

        // swap
        var t = pagerules[cindex];
        pagerules[cindex] = pagerules[index];
        pagerules[index] = t;
    };

    $scope.editRule = function (index, rule) {
        if (rule.id != undefined && rule.id != "" && rule.id > 0) {
            $scope.query.ruleId = rule.id;
        }
        $scope.query.index = index;

        var result = {};
        var attributes = JSON.parse(rule['attributes']);
        var condition = attributes['condition'];
        var action = attributes['condition-action'];


        // Composit
        var type = condition.type;
        var target = condition.target;
        var functions = condition.function;
        var value = condition.value;
        var composit = _.map(condition.composit, function (v) {
            var t = v.target;

            return getViewData(v, t, getTarget(v.target));
        });

        if (composit && composit.length > 0) {
            result['composit'] = _.groupBy(composit, function (v) {
                return v.target;
            });
            result['type'] = type;
        } else {
            result = {
                type: type,
                composit: {}
            };
            var t = getTarget(target);
            var viewData = getViewData({
                target: target,
                type: type,
                'function': functions,
                value: value
            }, target, t);
            result['composit'][t] = [
                viewData
            ];
        }

        // Action
        result['action'] = action;
        $scope.condition = result;
        $('#newRuleModel').modal('show');
    };

    $('#newRuleModel').on('hide.bs.modal', function () {
        $scope.query.ruleId = -1;
        $scope.query.index = -1;
        $scope.query.ruleId = -1;
    });

    $scope.deleteRule = function (index) {
        var pagerules = $scope.pagerules;
        pagerules.splice(index, 1);
    };

    function getTarget(target) {
        if (target.toLowerCase().indexOf('$header_') == 0) {
            return 'header';
        } else if (target.toLowerCase().indexOf('uri') == 0) {
            return 'uri';
        } else if (target.toLowerCase().indexOf('url') == 0) {
            return 'url';
        } else if (target.toLowerCase().indexOf('$cookie_') == 0) {
            return 'cookie';
        } else if (target.toLowerCase().indexOf('$arg_') == 0) {
            return 'arg';
        } else if (target.toLowerCase() == '$countheader') {
            return 'countheader';
        } else if (target.toLowerCase() == '$countcookie') {
            return 'countcookie';
        } else if (target.toLowerCase() == '$cip') {
            return 'cip';
        }
        return target;
    }

    function getViewData(obj, oldTarget, newTarget) {
        if (newTarget == 'header' || newTarget == 'cookie' || newTarget == 'arg') {
            var index = oldTarget.indexOf('_');
            obj.key = oldTarget.substring(index + 1);
        } else if (newTarget == 'uri' || newTarget == 'url') {
            obj.value = obj.value;
        } else if (newTarget == 'countcookie' || newTarget == 'countheader') {
            obj.count = obj.value;
        } else if (newTarget == 'cip') {
            obj.ip = obj.value;
        }

        obj.target = newTarget;
        return obj;
    }

    // Save result
    $scope.showSuccessMsg = function () {
        var result = $scope.query.saveResult;
        var show = false;
        if (result && _.keys(result).length > 0) {
            var update = result['update'];
            var deleted = result['delete'];

            if (update != undefined) {
                show = update == true;
            }
            if (deleted != undefined) {
                show = deleted == true;
            }
        } else if (_.keys(result).length == 0) {
            return show;
        }
        return show;
    };

    $scope.getErrorMsg = function () {
        var result = $scope.query.saveResult;
        var content = '<div>';
        if (result && _.keys(result).length > 0) {
            var update = result['update'];
            var deleted = result['delete'];

            if (update != undefined) {
                if (update == true) {
                    content += '<div class="status-green">更新：成功</div>';
                } else {
                    content += '<div>更新失败信息 ' + update.message + '</div>';
                }
            }
            if (deleted != undefined) {
                if (deleted == true) {
                    content += '<div class="status-green">删除：成功</div>';
                } else {
                    content += '<div>删除失败信息 ' + deleted.message + '</div>';
                }
            }
        }
        content += '</div>';
        return content;
    };

    // Help doc
    var helps = {

    };
    $scope.getHelpLink = function (type) {
        var url = helps[type];
        if (url) {
            window.open(url, '_blank');
        }
    };
    // Top bar
    $scope.getStatusClass = function () {
        var offlineGroup = $scope.slb.offline;
        var onlineGroup = $scope.slb.online;

        if (!offlineGroup && !onlineGroup) return;

        var compareRules = compareRuleVersion(offlineGroup, onlineGroup);

        switch (compareRules) {
            case 'activated':
                return 'status-green';
            case 'tobeactivated':
                return 'status-yellow';

            case 'deactivated':
                return 'status-red';

            default:
                break;
        }
    };

    $scope.getStatusText = function () {
        var offlineGroup = $scope.slb.offline;
        var onlineGroup = $scope.slb.online;

        if (!offlineGroup && !onlineGroup) return;

        var compareRules = compareRuleVersion(offlineGroup, onlineGroup);

        var resource = $scope.resource;
        if(!resource || _.keys(resource).length==0) return;

        switch (compareRules) {
            case 'activated':
                return resource['slb-rule']['slb_rule_operationLogApp_statusmap']['配置已生效'];
            case 'tobeactivated':
                return resource['slb-rule']['slb_rule_operationLogApp_statusmap']['配置有变更'];
            case 'deactivated':
                return resource['slb-rule']['slb_rule_operationLogApp_statusmap']['配置未生效'];

            default:
                break;
        }
    };

    $scope.showActivateBt = function () {
        var offline = $scope.slb.offline;
        var online = $scope.slb.online;

        if (!offline && !online) return;

        var compareRules = compareRuleVersion(offline, online);

        switch (compareRules) {
            case 'activated':
                return false;
            case 'tobeactivated':
                return true;

            case 'deactivated':
                return true;
            default:
                return false;
        }
    };

    $scope.confirmActivateText = '线上版本与当前版本比';

    $scope.activateRule = function () {
        // if save is disabled
        var saveIsDisabled = $scope.disableSaving();
        if (!saveIsDisabled) {
            alert("存在更改未保存，请先点击由此保存后激活！");
            return;
        }
        var offline = $scope.slb.offline;
        var online = $scope.slb.online;

        if (online.version != undefined && online.version == offline.version) {
            $scope.confirmActivateText = '线上已是最新版本,确认是否强制重新激活';
        }
        var baseText = JSON.stringify(U.sortObjectFileds(online), null, "\t");
        var newText = JSON.stringify(U.sortObjectFileds(offline), null, "\t");
        var baseVersion = '线上Slb版本(版本' + online.version + ")";
        var newVersion = '更新后Slb版本(版本' + offline.version + ")";
        var diffoutputdiv = document.getElementById("diffOutput");
        diffTwoGroup(diffoutputdiv, baseText, newText, baseVersion, newVersion);
        $('#activateGroupModal').modal('show');
    };

    $scope.activateGroup = function () {
        var offline = $scope.slb.offline;
        $('#operationConfrimModel').modal("show").find(".modal-title").html("激活Slb");
        $('#operationConfrimModel').modal("show").find(".modal-body").html("正在激活.. <img src='/static/img/spinner.gif' />");
        var param = {
            slbId: offline.id
        };
        var req = {
            method: 'GET',
            url: G.baseUrl + "/api/activate/slb",
            params: param
        };
        $scope.processRequest(req, $('#operationConfrimModel'), "激活Slb", "激活成功");
    };

    $scope.forceActivateGroup = function () {
        $scope.showForceUpdate = false;
        $('#operationConfrimModel').modal("show").find(".modal-body").html("正在Force激活.. <img src='/static/img/spinner.gif' />");
        $scope.processRequest($scope.forceUpdateRequest, $('#operationConfrimModel'), "激活Slb", "激活成功");
    };

    $scope.activateBtShow = function () {
        var offline = $scope.slb.offline;
        if (!offline) return false;
        return A.canDo("Slb", "ACTIVATE", offline.id);
    };

    $scope.applyGroupUpdateRequest = function () {
        if ($scope.hasApply) {
            alert('你已经申请更新当前应用的权限，请等待管理员审批! 如已经收到审批通过邮件，请刷新当前页面获得最新状态!');
            return;
        }

        var offline = $scope.slb.offline;

        var slbId = offline.id;
        var user = $scope.query.user;
        var env = $scope.env;

        var mailLink = G[env].urls.api + '/api/auth/apply/mail?userName=' + user + '&op=ACTIVATE&targetId=' + slbId + '&type=Slb&env=' + env;

        var request = {
            url: mailLink,
            method: 'GET'
        };
        $http(request).success(function (response, code) {
            if (code == 200) {
                alert('你已经申请更新当前应用的权限，请等待管理员审批!');
                $scope.hasApply = true;
            }
        });
    };

    $scope.activateGroupTitleClass = function () {
        var offline = $scope.slb.offline;
        var online = $scope.slb.online;

        try {
            if (online.version != undefined && online.version == offline.version) {
                return "status-red-important";
            }
        } catch (e) {

        }
    };

    $scope.forceUpdateRequest;
    $scope.processRequest = function (request, confirmDialog, operationText, tooltipText) {
        confirmDialog.find(".modal-title").html(operationText);
        $scope.forceUpdateRequest = $.extend(true, {}, request);
        if (!$scope.forceUpdateRequest.params) {
            $scope.forceUpdateRequest.params = {
                force: true
            }
        } else {
            $scope.forceUpdateRequest.params.force = true;
        }

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
                    confirmDialog.modal('show').find(".modal-title").html(successText);
                    if (tooltipText) confirmDialog.modal('show').find(".modal-body").html(tooltipText);
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

    $('.closeProgressWindowBt').click(
        function (e) {
            window.location.reload(true);
        }
    );

    function diffTwoGroup(targetDiv, baseText, newText, baseVersion, newVersion) {
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

    // Rights
    $scope.showChangeRule = function () {
        // can do group update?
        var slbId = $scope.query.slbId;
        if (!slbId) return false;
        var update = A.canDo("Slb", "UPDATE", slbId);
        var activate = A.canDo("Slb", "ACTIVATE", slbId);
        var op = update && activate;
        return !op;
    };

    $scope.getAuthLink = function () {
        var slbId = $scope.query.slbId;
        var url1 = G[$scope.env].urls.api
            + '/api/auth/apply/mail?userName=' +
            $scope.query.user +
            '&targetId=' + slbId
            + '&op=ACTIVATE,UPDATE,READ&type=Slb&env=' + $scope.env;

        var request = {
            method: 'GET',
            url: url1
        };
        $http(request).success(
            function () {
                alert('权限申请已经发出，请等待SLB Team审批通过。');
            }
        );
    };

    $scope.disableSaving = function () {
        var result = true;
        var pagerules = $scope.pagerules;
        if (pagerules && pageRulesSnapShot && pagerules.length >= 0 && pageRulesSnapShot.length >= 0) {
            result = compareRules(pageRulesSnapShot, pagerules);
        }
        return result;
    };

    $scope.saveSettings = function () {
        var disabled = $scope.disableSaving();
        if (disabled) return;
        var slbId = $scope.query.slbId;

        var set = {
            'target-id': slbId,
            'target-type': 'Slb',
            'rules': []
        };

        var setRequests;
        var deleteRequests;
        var request2;
        var request;

        var newrules = $scope.pagerules;
        var oldrules = pageRulesSnapShot;

        var oldIds = _.sortBy(_.reject(_.pluck(oldrules, 'id'), function (v) {
            return v == undefined;
        }));

        var newIds = _.sortBy(_.reject(_.pluck(newrules, 'id'), function (v) {
            return v == undefined;
        }));

        var deletedids = _.difference(oldIds, newIds);
        if (newIds.length == 0) {
            deletedids = oldIds;
        }
        if (deletedids.length > 0) {
            var params = _.map(deletedids, function (v) {
                return 'ruleId=' + v;
            });
            var url = G.baseUrl + '/api/rule/delete?' + params.join('&') + "&description=删除已有反爬规则";
            request2 = {
                url: url,
                method: 'GET'
            };

            deleteRequests = true;
        }

        // newrules not to be deleted are to be set
        for (var i = 0; i < newrules.length; i++) {
            delete newrules[i]['$$hashKey'];
        }
        set.rules = newrules;

        if (set.rules.length > 0) {
            request = {
                url: G.baseUrl + '/api/rule/set?description=保存新的反爬规则',
                method: 'POST',
                data: set
            };
            setRequests = true;
        }

        if (!deleteRequests && !setRequests) return;

        if (deleteRequests && setRequests) {
            $http(request2).success(function (response, code) {
                if (code == 200) {
                    $scope.query.saveResult['delete'] = true;
                } else {
                    $scope.query.saveResult['delete'] = response;
                }

                $http(request).success(function (response2, code2) {
                    if (code2 == 200) {
                        $scope.query.saveResult['update'] = true;
                    } else {
                        $scope.query.saveResult['update'] = response2;
                    }

                    $('#saveSettingsModel').modal("show");
                    setTimeout(function () {
                        var pair = {
                            timeStamp: new Date().getTime()
                        };
                        $('#saveSettingsModel').modal("hide");
                        H.setData(pair);
                    }, 2000);
                });
            });
        } else if (deleteRequests) {
            $http(request2).success(function (response, code) {
                if (code == 200) {
                    $scope.query.saveResult['delete'] = true;
                } else {
                    $scope.query.saveResult['delete'] = response;
                }

                $('#saveSettingsModel').modal("show");
                setTimeout(function () {
                    var pair = {
                        timeStamp: new Date().getTime()
                    };
                    $('#saveSettingsModel').modal("hide");
                    H.setData(pair);
                }, 2000);
            });
        } else {
            $http(request).success(function (response, code) {
                if (code == 200) {
                    $scope.query.saveResult['update'] = true;
                } else {
                    $scope.query.saveResult['update'] = response;
                }

                $('#saveSettingsModel').modal("show");
                setTimeout(function () {
                    var pair = {
                        timeStamp: new Date().getTime()
                    };
                    $('#saveSettingsModel').modal("hide");
                    H.setData(pair);
                }, 2000);
            });
        }

        setTimeout(function () {

        }, 2000);

    };

    $scope.reloadPage = function () {
        var pair = {
            timeStamp: new Date().getTime()
        };
        H.setData(pair);
    };

    // Validation
    $scope.isIpValid = function (func, ip) {
        if (!ip || ip.trim().length == 0) return false;
        var valid = ['=', '!='];
        if (valid.indexOf(func) != -1) {
            return !validateIp(ip);
        }
    };
    $scope.isKeyValid = function (key) {
        if (!key || key.trim().length == 0) return false;

        return !validateKey(key);
    };
    // Common methods
    $scope.loadData = function () {
        $scope.query.saveResult = {};
        var slbId = $scope.query.slbId;

        // Group
        var online = {
            method: 'GET',
            url: G.baseUrl + '/api/slb?slbId=' + slbId + '&mode=online'
        };

        var offline = {
            method: 'GET',
            url: G.baseUrl + '/api/slb?slbId=' + slbId
        };

        // Groups
        var onlineRequest = $http(online).success(function (response, code) {
            if (code == 200) {
                $scope.slb['online'] = response;
            }
        });

        var offlineRequest = $http(offline).success(function (response, code) {
            if (code == 200) {
                $scope.slb['offline'] = response;
            }
        });

        // Configs
        $q.all([onlineRequest, offlineRequest]).then(function () {
            var offline = $scope.slb['offline'];
            var online = $scope.slb['online'];
            pagerules = _.filter(offline['rule-set'], function (v) {
                return v['rule-type'] == 'REQUEST_INTERCEPT_RULE';
            });

            pageRulesSnapShot = $.extend(true, [], pagerules);
            $scope.pagerules = pagerules;
        });

        $http.get('/api/auth/current/user').success(
            function (response) {
                if (response && !response.code) {
                    $scope.query.user = response['name'];
                    $scope.query.email = response.mail;
                }
            }
        );
    };

    function compareRules(left, right) {
        var same = true;

        if (left && right) {
            if (left.length == right.length) {
                for (var i = 0; i < left.length; i++) {
                    var lRule = left[i];
                    var rRule = right[i];
                    // compare to rule is same
                    var c = compareRule(lRule, rRule);
                    if (!c) {
                        return false;
                    }
                }
            } else {
                same = false;
            }
        } else if (!left && !right) {
            same = true;
        } else {
            same = false;
        }

        return same;
    }

    function compareRule(left, right) {
        var result = false;

        if (left['rule-type'] != right['rule-type']) return result;

        if (left && right) {
            var att1 = left['attributes'];
            var att2 = right['attributes'];

            if (att1 && att2) {
                var att1json = JSON.parse(att1);
                var att2json = JSON.parse(att2);
                result = compareTwoRuleAttributes(att1json, att2json);
            } else if (!att1 && !att2) {
                result = true;
            }
        } else if (!left && !right) {
            result = true;
        }

        return result;
    }

    function compareTwoRuleAttributes(a, b) {
        var result = false;

        var aKeys = _.keys(a);
        var bKeys = _.keys(b);
        var abKeys = _.union(aKeys, bKeys);

        for (var j = 0; j < abKeys.length; j++) {
            var c = a[abKeys[j]];
            var d = b[abKeys[j]];

            var ctype = typeof c;
            var dtype = typeof d;
            if (ctype != dtype) {
                result = false;
                break;
            }

            if (ctype == 'object') {
                result = compareTwoRuleAttributes(c, d);
                if (result == false) break;
            } else {
                if (c != d) {
                    result = false;
                    break
                } else {
                    result = true;
                }
            }
        }

        return result;
    }

    function compareRuleVersion(offline, online) {
        var offlineRules;
        var onlineRules;

        if (!online) return 'deactivated';
        onlineRules = online['rule-set'];

        if (offline) {
            offlineRules = offline['rule-set'];

            //  有offline 并且offline是因为rule引起的
            if (!offlineRules && !onlineRules) return 'activated';

            if (!(offlineRules && onlineRules)) return 'tobeactivated';

            var result = compareTwoRuleList(offlineRules, onlineRules) && compareTwoRuleList(onlineRules, offlineRules);
            if (result) return 'activated';
            return 'tobeactivated';
        } else {
            return 'activated';
        }

    }

    function compareTwoRuleList(lefts, rights) {
        var result = false;
        // id order of left and right
        var leftIds = _.pluck(lefts, 'id');
        var rightIds = _.pluck(rights, 'id');


        if (leftIds.join(',') == rightIds.join(',')) {
            for (var i = 0; i < lefts.length; i++) {
                var left = lefts[i];
                var rightHas = _.find(rights, function (v) {
                    return compareRule(left, v);
                });

                if (rightHas) {
                    result = true;
                } else {
                    result = false;
                    break;
                }
            }
        }
        return result;
    }

    function validateIp(ip) {
        var regv4 = /^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$/;
        var regv6 = /^\s*((([0-9A-Fa-f]{1,4}:){7}([0-9A-Fa-f]{1,4}|:))|(([0-9A-Fa-f]{1,4}:){6}(:[0-9A-Fa-f]{1,4}|((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3})|:))|(([0-9A-Fa-f]{1,4}:){5}(((:[0-9A-Fa-f]{1,4}){1,2})|:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3})|:))|(([0-9A-Fa-f]{1,4}:){4}(((:[0-9A-Fa-f]{1,4}){1,3})|((:[0-9A-Fa-f]{1,4})?:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){3}(((:[0-9A-Fa-f]{1,4}){1,4})|((:[0-9A-Fa-f]{1,4}){0,2}:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){2}(((:[0-9A-Fa-f]{1,4}){1,5})|((:[0-9A-Fa-f]{1,4}){0,3}:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){1}(((:[0-9A-Fa-f]{1,4}){1,6})|((:[0-9A-Fa-f]{1,4}){0,4}:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3}))|:))|(:(((:[0-9A-Fa-f]{1,4}){1,7})|((:[0-9A-Fa-f]{1,4}){0,5}:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3}))|:)))(%.+)?\s*$/;
        return regv4.test(ip) || regv6.test(ip);
    }

    function validateKey(key) {
        if (!key || key.trim().length == 0) {
            return false;
        }
        var reg = /^[0-9|a-z|A-Z|\-|_]*$/g
        return reg.test(key);
    }

    $scope.applyHashData = function (hashData) {
        $scope.env = hashData.env;
        $scope.query.env = hashData.env;
        $scope.query.slbId = hashData.slbId;

        var startTime = new Date().getTime();
        startTime = new Date(startTime - 60 * 1000 * 60);
        $scope.query.startTime = $.format.date(startTime, 'yyyy-MM-dd HH:mm:00');
    };

    $scope.hashChanged = function (hashData) { $scope.resource = H.resource;
        $scope.resource = H.resource;
        $scope.applyHashData(hashData);
        $scope.loadData();
    };
    H.addListener("operationLogApp", $scope, $scope.hashChanged);
});
angular.bootstrap(document.getElementById("operation-log-area"), ['operationLogApp']);

