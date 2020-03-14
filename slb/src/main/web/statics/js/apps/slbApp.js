/**
 * Created by ygshen on 2017/6/2.
 */

function SlbApp(hashData, $http, $q, env) {
    // Local data
    this.hashData = hashData;
    this.$q = $q;
    this.env = env;

    // Dependency Service
    this.paramService = new ParamService(hashData);
    this.requestService = new ApiService($http);
    this.queryService = new QueryService();
    this.esService = new ESService(G[env].urls.es);
};
SlbApp.prototype = new App();

SlbApp.prototype.getQueries = function () {
    var hashData = this.hashData;
    var slbId = hashData.slbId;
    var infoParam = {
        type: 'info'
    };
    var logParam = {
        targetId: slbId,
        type: 'slb',
        count: 3
    };

    var groupsParam = {
        type: 'extended',
        slbId: slbId
    };
    var queries = [
        {
            key: 'slbId',
            type: 'normal',
            symbol: 'slbId'
        }
    ];
    var params = this.getQueryString(queries);

    var slbExtendedQueryString = G.baseUrl + "/api/slb?type=extended";
    slbExtendedQueryString += '&' + params.join('&');

    var onlineQueryString = G.baseUrl + "/api/slb?mode=online";
    onlineQueryString += '&' + params.join('&');

    var infoQueryString = G.baseUrl + "/api/slbs";

    var slbVsesString = G.baseUrl + '/api/vses?type=extended';
    slbVsesString += '&' + params.join('&');

    var vsStasticsString = G.baseUrl + '/api/statistics/vses?type=extended';
    vsStasticsString += '&' + params.join('&');

    var currentUserString = '/api/auth/current/user';

    var slbServerString = G.baseUrl + '/api/statistics/slbServers';

    var logQueryString = G.baseUrl + '/api/logs';

    var groupsQueryString = G.baseUrl + '/api/groups';

    return {
        slbs_info: {
            url: infoQueryString,
            params: infoParam,
            method: 'GET'
        },
        slb_online: {
            url: onlineQueryString,
            method: 'GET'
        },
        slb_extended: {
            url: slbExtendedQueryString,
            method: 'GET'
        },
        slb_vses: {
            url: slbVsesString,
            method: 'GET'
        },

        vses_stastics: {
            url: vsStasticsString,
            method: 'GET'
        },

        current_user: {
            url: currentUserString,
            method: 'GET'
        },

        slb_server: {
            url: slbServerString,
            method: 'GET'
        },
        slb_log: {
            url: logQueryString,
            params: logParam,
            method: 'GET'
        },
        slb_groups: {
            url: groupsQueryString,
            params: groupsParam,
            method: 'GET'
        }
    }
};
SlbApp.prototype.request = function (query) {
    var out = getRequestData();
    return out(this, this.$q, query);
};

SlbApp.prototype.convertIDC = function (idc) {
    return this.esService.mappingIDC(idc) || idc;
};

SlbApp.prototype.getEsHtml = function (query) {
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
SlbApp.prototype.getWebinfoHtml = function (env, server) {
    var webinfoLink = G[env]['urls']['webinfo'] + '/#/relation/server/' + server;
    if (server == '-' || !server) {
        return '<div class="system-link">' +
            '<span class="pull-left">-</span>' +
            '</div>';
    }
    return '<div class="system-link">' +
        '<a class="pull-left webinfo" title="Webinfo" target="_blank" href="' + webinfoLink + '">WebInfo</a>' +
        '</div>';
};

function getRequestData() {
    var result = {};
    var request;
    var f = function (self, $q, query) {
        var slbsInforRequest = query['slbs_info'];
        var slbOnlineRequest = query['slb_online'];
        var slbExtendedRequest = query['slb_extended'];
        var slbVsesRequest = query['slb_vses'];
        var vsesStasticsRequest = query['vses_stastics'];
        var currentUserRequest = query['current_user'];
        var slbServersStasticsRequest = query['slb_server'];
        var slbCurrentLogsRequest = query['slb_log'];
        var groupsRequest = query['slb_groups'];

        var requests = [self.sendRequest(slbsInforRequest, function (res, code) {
            result['slbs_info'] = _.values(self.getAllData(res['slbs'], 'id'));
        }),
            self.sendRequest(slbCurrentLogsRequest, function (res, code) {
                result['slb_log'] = _.values(self.getAllData(res['operation-log-datas'], 'date-time'));
            }),
            self.sendRequest(slbOnlineRequest, function (res, code) {
                if (code == 200) {
                    result['slb_online'] = {'data': _.values(self.getAllData([res], 'id'))[0]};
                } else {
                    result['slb_online'] = {"data": "No Online Data", "version": "Unknow"};
                }
            }),
            self.sendRequest(slbExtendedRequest, function (res, code) {
                result['slb_extended'] = _.values(self.getAllData([res], 'id'))[0];
            }),
            self.sendRequest(slbVsesRequest, function (res, code) {
                result['slb_vses'] = _.values(self.getAllData(res['virtual-servers'], 'id'));
            }),
            self.sendRequest(currentUserRequest, function (res, code) {
                result['current_user'] = _.values(self.getAllData([res], 'id'))[0];
            }),
            self.sendRequest(slbServersStasticsRequest, function (res, code) {
                result['slb_server'] = self.getAllData(res['slb-server-qpses'], 'ip');
            }),
            self.sendRequest(vsesStasticsRequest, function (res, code) {
                result['vses_stastics'] = self.getAllData(res['vs-metas'], 'vs-id');
            }),
            self.sendRequest(groupsRequest, function (res, code) {
                result['slb_groups'] = _.values(self.getAllData(res['groups'], 'id'));
            })];

        request = $q.all(requests);

        return {
            result: result,
            request: request
        }
    };

    return f;
}
