//Global Data
var G = {
    baseUrl: $.cookie("base"),
    env: "pro",
    pro: {
        urls: {
            es: '',
            'tengine-es': '',
            api: $.cookie("base"),
            webinfo: '',
            cat: '',
            dashboard: '',
            clog: '',
            cms: '',
            cdng: '',
            cd: '',
            hc: {
                'jq': '',
                'oy': '',
                'jqpci': '',
                'oypci': '',
                'nt': ''
            },
            db: '',
            tars: '',
            hickwall: '',
            hickwallpre: '',
            altermis: {
                get: '',
                activate: '',
                update: ''
            }
        }
    }
};

//Hash Change Component
var H = function () {
    return {
        data: {},
        resource: {},
        listeners: {},
        addListener: function (name, obj, fun) {
            this.listeners[name] = {obj: obj, fun: fun};
        },
        hashChanged: function () {
            var o = this;
            this.initData(window.location.hash);
            A.init(1);
            $.each(this.listeners, function (name, val) {
                val.obj.$apply(function () {
                    val.fun.call(val.obj, o.data);
                });
            });
            this.pageData();
        },
        initData: function (hashStr) {
            var o = this;
            o.data = {};
            var arr = hashStr.substr(hashStr.indexOf("?") + 1).split("&");
            $.each(arr, function (i, val) {
                try {
                    var pair = val.split("=");
                    var k = decodeURIComponent(pair[0]);
                    var v = decodeURIComponent(pair[1]);
                    if (!o.data[k]) {
                        o.data[k] = v;
                    } else {
                        if (Array.isArray(o.data[k]) && o.data[k].indexOf(v) == -1) {
                            o.data[k].push(v);
                        } else {
                            o.data[k] = [v, o.data[k]];
                        }
                    }

                } catch (e) {
                }
            });
        },
        pageData: function () {
            var t = this;
            // get current language
            var language = 'ch';
            var cookie = $.cookie("_language");
            if (!cookie) {
                $.cookie("_language", language)
            } else {
                language = $.cookie("_language");
            }

            $.ajax({
                url: "/portal/resource/" + language + ".json",
                xhrFields: {
                    withCredentials: true
                },
                beforeSend: function (request) {
                    var cookie = $.cookie("_stok");
                    if (cookie) {
                        cookie = cookie.replace(/ /g, '+');
                    }
                    request.setRequestHeader("_stok", cookie);
                },
                success: function (data, status, xhr) {
                    H.resource = data;
                    $.ajax({
                        url: G.baseUrl + "/api/auth/user/resources",
                        xhrFields: {
                            withCredentials: true
                        },
                        beforeSend: function (request) {
                            var cookie = $.cookie("_stok");
                            if (cookie) {
                                cookie = cookie.replace(/ /g, '+');
                            }
                            request.setRequestHeader("_stok", cookie);
                        },
                        success: function (data, status, xhr) {
                            var o = {};
                            if(!data) return;
                            $.each(data['user-resources'], function (i, v1) {
                                var o1 = o[v1['type']] = {};
                                $.each(v1['data-resources'], function (j, v2) {
                                    var o2 = o1[v2['data']] = {};
                                    $.each(v2['operations'], function (k, v3) {
                                        o2[v3['type']] = 1;
                                    });
                                })
                            });
                            A.data = o;
                            $.each(t.listeners, function (name, val) {
                                val.obj.$apply(function () {
                                    val.fun.call(val.obj, t.data);
                                });
                            });
                        },
                        error: function (err) {
                            A.data = {};
                            $.each(t.listeners, function (name, val) {
                                val.obj.$apply(function () {
                                    val.fun.call(val.obj, t.data);
                                });
                            });
                        }
                    });
                },
                error: function (err) {
                    console.log(err);
                }
            });
        },
        clearData: function () {
            var o = this;
            $.each(o.data, function (name, val) {
                delete o.data[name];
            })
        },
        setData: function (pairs) {
            var o = this;
            $.each(pairs, function (name, val) {
                if (val != '' && val != []) {
                    o.data[name] = val;
                } else {
                    delete o.data[name];
                }
            });
            var hashStr = o.generateHashStr(o.data);
            window.location.hash = hashStr;
        },
        getParam: function (k) {
            var o = this;
            return o.data[k];
        },
        generateHashStr: function (data) {
            var hashStr = "#?"
            var i = 0;
            $.each(data, function (name, val) {
                if (!name) return;
                if (i != 0) {
                    hashStr += "&";
                }
                i++;
                if (Array.isArray(val)) {
                    $.each(val, function (j, t) {
                        if (j == val.length - 1) {
                            hashStr += name + "=" + t;
                        } else {
                            hashStr += name + "=" + t + '&';
                        }

                    });
                } else {
                    hashStr += name + "=" + val;
                }
            });
            return hashStr;
        }
    };
}();

var A = function () {
    return {
        data: {},
        init: function (refresh) {
            var t = this;
            $.ajax({
                url: G.baseUrl + "/api/auth/user/resources",
                xhrFields: {
                    withCredentials: true
                },
                beforeSend: function (request) {
                    var cookie = $.cookie("_stok");
                    if (cookie) {
                        cookie = cookie.replace(/ /g, '+');
                    }
                    request.setRequestHeader("_stok", cookie);
                },
                success: function (data, status, xhr) {
                    var o = {};
                    $.each(data['user-resources'], function (i, v1) {
                        var o1 = o[v1['type']] = {};
                        $.each(v1['data-resources'], function (j, v2) {
                            var o2 = o1[v2['data']] = {};
                            $.each(v2['operations'], function (k, v3) {
                                o2[v3['type']] = 1;
                            });
                        })
                    });
                    t.data = o;
                    if (refresh) {
                        $.each(H.listeners, function (name, val) {
                            val.obj.$apply(function () {
                                val.fun.call(val.obj, H.data);
                            });
                        });
                    }
                },
                error: function (err) {
                    t.data = {};
                    if (refresh) {
                        $.each(H.listeners, function (name, val) {
                            val.obj.$apply(function () {
                                val.fun.call(val.obj, H.data);
                            });
                        });
                    }
                }
            });
        },
        canDo: function (type, op, id) {
            var d = this.data;
            if (!d) {
                return false;
            }
            if (d[type] && d[type][id] && d[type][id][op]) {
                return true;
            }
            if (d[type] && d[type]['*'] && d[type]['*'][op]) {
                return true;
            }
            return false;
        }
    };
}();
var L = function () {
    return {
        visitPage: function (bu) {
            var b = 'none';
            var locationPath = window.location.pathname;
            if (bu) b = bu;
            $.ajax({
                url: G.baseUrl + '/api/ubt?uri=' + locationPath + '&bu=' + b,
                xhrFields: {
                    withCredentials: true
                },
                success: function (data) {

                },
                error: function (err) {

                }
            });
        }
    };
}();
var T = function () {
    return {
        getText: function (count) {
            if (!isNaN(count)) {
                if (count > 1000) {
                    var a = count / 1000;
                    var b = Math.round(a * 10) / 10;
                    return b + 'K';
                } else {
                    count = Math.round(count * 10) / 10;
                    return count;
                }
            } else {
                return '--';
            }
        },
        getPercent: function (count, total) {
            if (total == 0) {
                return '50.0%';
            }
            if (!isNaN(count) && count >= 0) {
                var a = count / total;
                var b = Math.round(a * 1000);
                var left = Math.round(b / 10);
                var right = b % 10;

                return left + '%';
            } else {
                return '--';
            }
        }
    };
}();
//On Load
$(function () {
    $(window).hashchange(function () {
        H.hashChanged();
    });
    $(window).hashchange();
    $('#submmit-question').click(function (e) {
        var v = $('#comment-text').val();
        if (!v || v.trim().length == 0) {
            $('#questionModal').modal('hide');
        } else {
            $.ajax({
                url: "/api/auth/current/user",
                xhrFields: {
                    withCredentials: true
                },
                success: function (data) {
                    var user = data['name'];
                    var content = v;

                    var d = {
                        user: user,
                        description: content
                    };

                    $.ajax({
                        url: G.baseUrl + "/api/feedback/add",
                        xhrFields: {
                            withCredentials: true
                        },
                        dataType: "json",
                        type: 'POST',
                        data: JSON.stringify(d),
                        success: function (data) {
                            swal({
                                title: "感谢您的反馈!",
                                text: "我们会尽快查看您的意见、建议，并及时采纳。",
                                imageUrl: "/static/img/thumbs-up.jpg"
                            });
                        },
                        error: function (err) {
                            alert('提交失败! Message: ' + err.responseText)
                        }
                    });
                },
                error: function (err) {
                }
            });
        }
    });
});

//On Load: Disable backspace key
$(function () {
    document.getElementsByTagName("body")[0].onkeydown = function (event) {
        if (event.keyCode == 8) {

            var elem = event.srcElement || event.target;

            var name = elem.nodeName;
            if (name != 'INPUT' && name != 'TEXTAREA') {
                return _stopIt(event);
            }
            var type_e = elem.type.toUpperCase();
            if (name == 'INPUT' && (type_e != 'NUMBER' && type_e != 'TEXT' && type_e != 'TEXTAREA' && type_e != 'PASSWORD' && type_e != 'FILE')) {
                return _stopIt(event);
            }
            if (name == 'INPUT' && (elem.readOnly == true || elem.disabled == true)) {
                return _stopIt(event);
            }
        }
    }
});

function _stopIt(e) {
    if (e.returnValue) {
        e.returnValue = false;
    }
    if (e.preventDefault) {
        e.preventDefault();
    }
    return false;
}


var questionModalApp = angular.module("questionModalApp", ['http-auth-interceptor']);
questionModalApp.controller("questionModalCtrl", function ($scope) {
    $scope.resource = H.resource;

    $scope.hashChanged = function () {
        $scope.resource = H.resource;
    };
    H.addListener("questionModalApp", $scope, $scope.hashChanged);
});
angular.bootstrap(document.getElementById("questionModal"), ['questionModalApp']);

//Global Component: Environment
var brandApp = angular.module('brandApp', ['http-auth-interceptor']);
brandApp.controller('brandController', function ($scope) {
    $scope.data = {
        homeUrl: "/portal",
        envs: ["pro", "uat", "fws", 'fra-aws', 'sin-aws', 'ymq-aws']
    };
    $scope.generateEnvLink = function (env) {
//        H.clearData();
        var path = window.location.pathname;
        if (path.indexOf("user") > 0) {
            var data = _.clone(H.data);
            data.env = env;
            return path + H.generateHashStr(data);
        } else if (path.indexOf("statistics") > 0) {
            return '/portal/statistics';
        } else if (path.indexOf("slb") > 0) {
            return '/portal/slbs';
        } else if (path.indexOf("vs") > 0) {
            return '/portal/vses';
        } else if (path.indexOf("group") > 0) {
            return '/portal/groups';
        } else if (path.indexOf("app") > 0) {
            return '/portal/apps';
        } else if (path.indexOf("bu") > 0) {
            return '/portal/bus';
        } else if (path.indexOf("tools") > 0) {
            return '/portal/tools';
        } else if (path.indexOf("polic") > 0) {
            return '/portal/policies';
        } else if (path.indexOf("dr") > 0) {
            return '/portal/drs';
        } else {
            return '/portal/home#?env=' + env;
        }
    };
    $scope.isCurrentENV = function (env) {
        if (G.env == env) {
            return "label-info";
        }
    };
    $scope.hashChanged = function (hashData) {
        $scope.resource = H.resource;
        // Do nothing, just to render page.
        $scope.resource = H.resource;
    };
    H.addListener("brandApp", $scope, $scope.hashChanged);
});


//Global Component: TopLinks
var topLinksApp = angular.module('topLinksApp', ['http-auth-interceptor']);
topLinksApp.controller('topLinksController', function ($scope) {
    $scope.resource = H.resource;

    var globalenv = window.localStorage.getItem("globalenv");
    $scope.data = {
        members: [
            {
                name: 'Home',
                url: '/portal/user-home',
                icon: 'fa fa-home',
                keyword: '/user'
            },
            {
                name: 'Slb',
                url: '/portal/slbs',
                icon: 'fa fa-th-large',
                keyword: '/slb'
            },
            {
                name: 'Vs',
                url: '/portal/vses',
                icon: 'fa fa-th-list',
                keyword: '/vs'
            },
            {
                name: 'Group',
                url: '/portal/groups',
                icon: 'fa fa-th',
                keyword: '/group'
            },
            {
                name: 'Policy',
                url: '/portal/policies',
                icon: 'fa fa-map-signs',
                keyword: '/policy'
            },

            {
                name: 'App',
                url: '/portal/apps',
                icon: 'fa fa-sitemap',
                keyword: '/app'
            },
            {
                name: 'Statistics',
                url: '/portal/statistics',
                icon: 'fa fa-bookmark',
                keyword: '/statistics'
            },
            {
                name: 'Tools',
                url: '/portal/tools',
                icon: 'fa fa-wrench',
                keyword: '/tools'
            }
        ]
    };

    $scope.isSelectedLink = function (linkname) {
        var currentPathname = window.location.pathname;
        if (linkname.indexOf('/user') > 0 && currentPathname.indexOf('/user') > 0) {
            return "selected-toplink";
        } else if (linkname.indexOf('/slb') > 0 && currentPathname.indexOf('/slb') > 0) {
            return "selected-toplink";
        } else if (linkname.indexOf('/vs') > 0 && currentPathname.indexOf('/vs') > 0) {
            if (currentPathname.indexOf('/tools/vs') > 0) {
                return "default-toplink";
            }
            return "selected-toplink";
        } else if (linkname.indexOf('/group') > 0 && currentPathname.indexOf('/group') > 0) {
            return "selected-toplink";
        } else if (linkname.indexOf('/app') > 0 && currentPathname.indexOf('/app') > 0) {
            return "selected-toplink";
        } else if (linkname.indexOf('/statistics') > 0 && currentPathname.indexOf('/statistics') > 0) {
            return "selected-toplink";
        } else if (linkname.indexOf('/tools') > 0 && currentPathname.indexOf('/tools') > 0) {
            return "selected-toplink";
        } else if (linkname.indexOf('/polic') > 0 && currentPathname.indexOf('/polic') > 0) {
            return "selected-toplink";
        } else if (linkname.indexOf('/dr') > 0 && currentPathname.indexOf('/dr') > 0 && currentPathname.indexOf('/tools') == -1 && currentPathname.indexOf('/user') == -1) {
            return "selected-toplink";
        } else {
            return "default-toplink";
        }
    };
    $scope.iconClass = function (x) {
        return x.icon;
    };
    $scope.generateTopLink = function (x) {
        return x.url;
    };
    $scope.hashChanged = function () {
        $scope.resource = H.resource;
    };
    H.addListener("topLinksApp", $scope, $scope.hashChanged);
});

angular.bootstrap(document.getElementById("top-links"), ['topLinksApp']);

var languageApp = angular.module('languageApp', ['http-auth-interceptor']);
languageApp.controller('languageController', function ($scope) {
    $scope.resource = H.resource;
    $scope.selected = '中文';

    $scope.data = {
        languages: [
            '中文',
            'English'
        ]
    };
    $scope.changeLanguage = function (c) {
        var l = c == '中文' ? 'ch' : 'en';
        $.cookie('_language', '', {expires: -1});
        $.cookie("_language", l, {path: '/'});
        window.location.reload();
    };

    $scope.isSelected = function (c) {
        var s = $scope.selected;
        return c == s ? true : false;
    };

    $scope.hashChanged = function () {
        $scope.resource = H.resource;
        // cookie
        var cookie = $.cookie("_language");
        if (cookie) {
            $scope.selected = cookie == 'ch' ? '中文' : 'English';
        }
    };
    H.addListener("languageApp", $scope, $scope.hashChanged);
});
angular.bootstrap(document.getElementById("language-area"), ['languageApp']);

//Global Component: foot
var footApp = angular.module('footApp', ['http-auth-interceptor']);
footApp.controller('footController', function ($scope) {
    $scope.resource = H.resource;


    $scope.hashChanged = function () {
        $scope.resource = H.resource;
        $scope.currentUrl = window.location.href;
    };
    $scope.getFullUrl = function () {
        var url = window.location.href;
        if (!url) return;
        var t = encodeToGb2312(window.location.hash);

        var origin = window.location.origin;
        var path = window.location.pathname;
        return origin + path + t;
    };
    H.addListener("footApp", $scope, $scope.hashChanged);
});

angular.bootstrap(document.getElementById("foot-area"), ['footApp']);


//Global Component: UserInfo
var userInfoApp = angular.module('userInfoApp', ['http-auth-interceptor']);
userInfoApp.controller('userInfoController', function ($scope, $http) {
    $scope.resource = H.resource;
    var resource = $scope.resource;
    $scope.query = {
        "user": "",
        "email": "",
        "hassuper": ""
    };
    // Send api request to determine whether current client is authorized\myHttpResponseInterceptor
    $http(
        {
            method: "get",
            url: "/api/auth/current/user"
        }
    ).success(function (response) {
        if (!response) return;

        if (response.code) {
            // Exception happen
        }
        else {
            $scope.query.user = response['display-name'];
            $scope.query.email = response.mail;
            $scope.query.bu = response.department;
        }
    });


    $scope.showSuperAdmin = function () {
        return $scope.query.hassuper;
    };
    $scope.hashChanged = function () {
        $scope.resource = H.resource;
        $scope.env = G.env;
        $scope.query.hassuper = A.canDo("Auth", "AUTH", '*');
        L.visitPage($scope.query.bu);
    };
    H.addListener("userInfoApp", $scope, $scope.hashChanged);
});
angular.bootstrap(document.getElementById("user-info"), ['userInfoApp']);

//BreadCrumb Component for slbs
var breadCrumbApp = angular.module('breadCrumbApp', ['http-auth-interceptor']);
breadCrumbApp.controller('breadCrumbController', function ($scope) {
    $scope.breadlist = getbreadlist();

    function getbreadlist() {
        var locationPath = window.location.pathname;
        var breadlist = locationPath.substring("/portal".length + 1).split("/");
        // mapping
        var m = {
            'tools': '工具',
            'user': '用户',
            'log': '操作日志',
            'traffic': '流量监控',
            'conf': 'Nginx Conf',
            'edit': '编辑',
            'seperate-edit': 'vs拆分编辑',
            'seperates': 'vs拆分列表',
            'home': ' '
        };
        return _.map(breadlist, function (v) {
            v = v.trim();
            return m[v] || v;
        });
    }

    $scope.hashChanged = function () {
        $scope.resource = H.resource;
    };
    H.addListener("breadCrumbApp", $scope, $scope.hashChanged);
});
angular.bootstrap(document.getElementById("breadcrumb-area"), ['breadCrumbApp']);

//Utils:
if (typeof U == 'undefined' || !U) {
    U = {
        isValidDate: function (d) {
            if (Object.prototype.toString.call(d) === "[object Date]") {
                // it is a date
                if (isNaN(d.getTime())) {  // d.valueOf() could also work
                    return false;
                } else {
                    return true;
                }
            } else {
                return false;
            }
        },
        hashCode: function (str) {
            var hash = 0;
            for (var i = 0; i < str.length; i++) {
                hash = str.charCodeAt(i) + ((hash << 5) - hash);
            }
            return hash;
        },
        int2RGB: function (i) {
            var c = (i & 0x00FFFFFF)
                .toString(16)
                .toUpperCase();
            return "00000".substring(0, 6 - c.length) + c;
        },
        string2RGB: function (str) {
            return U.int2RGB(U.hashCode(str));
        },
        sortObjectFileds: function (o) {
            if (typeof o == 'boolean' ||
                typeof o == 'string' ||
                typeof o == 'number' ||
                typeof o == 'undefined' ||
                typeof o == 'function' ||
                o == null
            ) {
                return o;
            }

            var sorted = {}, key, a = [];
            if (o.constructor === Array) {
                sorted = [];
            }

            for (key in o) {
                if (o.hasOwnProperty(key)) {
                    a.push(key);
                }
            }
            a.sort();
            for (key = 0; key < a.length; key++) {
                sorted[a[key]] = U.sortObjectFileds(o[a[key]]);
            }
            return sorted;
        },
        lastMethod: function () {
        }
    };
}
(function () {
    var Modal = {};
    Modal.adjustBody_beforeShow = function () {
        var body_scrollHeight = $('body')[0].scrollHeight;
        var docHeight = document.documentElement.clientHeight;
        if (body_scrollHeight > docHeight) {
            $('body').css({
                'overflow': 'auto',
                'margin-right': '0'
            });
        } else {
            $('body').css({
                'overflow': 'auto',
                'margin-right': '0'
            });
            $('.modal').css({'overflow-y': 'auto'})
        }
    }
    Modal.adjustBody_afterShow = function () {
        var body_scrollHeight = $('body')[0].scrollHeight;
        var docHeight = document.documentElement.clientHeight;
        if (body_scrollHeight > docHeight) {
            $('body').css({
                'overflow': 'auto',
                'margin-right': '0'
            });
        } else {
            $('body').css({
                'overflow': 'auto',
                'margin-right': '0'
            });
        }
    }
    $('.modal').on('show.bs.modal', function (event) {
        Modal.adjustBody_beforeShow();
    });
    $('.modal').on('hidden.bs.modal', function (event) {
        Modal.adjustBody_afterShow();
    });
})();

Array.prototype.sortBu = function () {
    var bu_mapping = {
        机票: 0,
        酒店: 1,
        度假: 2,
        玩乐: 3,
        搜索: 4,

        邮轮: 5,
        天海邮轮: 6,

        火车票: 7,
        汽车票: 8,
        车船: 9,
        租车: 10,
        国际专车: 11,
        国内专车: 12,


        财务: 13,
        金融支付: 14,
        '金融支付(非金融SBU)': 15,
        消费金融: 16,
        金融服务: 17,
        金融礼品卡: 18,
        金融_征信: 19,


        营销: 20,
        国际业务: 21,
        港澳: 22,
        尚诚: 23,


        攻略: 24,

        包团定制: 25,
        高端旅游: 26,

        商旅: 27,
        酒会: 28,
        会奖: 29,


        系统: 30,
        风险控制: 31,
        基础业务: 32,
        信息安全: 33,

        网站运营: 34,
        框架: 35,
        应用架构: 36,
        技术中心: 37,
        技术管理中心: 38,
        通信技术中心: 39,
        创新工场: 40,
        服务: 41,
        '用户研究与设计': 42
    };

    var a = this;
    return a.sort(function (c, d) {
        if (bu_mapping[c] != undefined && bu_mapping[d] != undefined) {
            return bu_mapping[c] - bu_mapping[d];
        }
    });
};
Array.prototype.split = function (dot) {
    var a = this.join(dot);
    return a.split(dot);
};
Array.prototype.toLowerCase = function () {
    return this;
};

String.prototype.startsWith = function (searchString, position) {
    position = position || 0;
    return this.substr(position, searchString.length) === searchString;
};
String.prototype.endsWith = function (searchString, position) {
    var subjectString = this.toString();
    if (typeof position !== 'number' || !isFinite(position) || Math.floor(position) !== position || position > subjectString.length) {
        position = subjectString.length;
    }
    position -= searchString.length;
    var lastIndex = subjectString.indexOf(searchString, position);
    return lastIndex !== -1 && lastIndex === position;
};
