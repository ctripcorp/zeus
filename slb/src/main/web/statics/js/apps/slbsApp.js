/**
 * Created by ygshen on 2017/5/16.
 */
function SlbsApp(hashData, $http, $q, env) {
    this.hashData = hashData;
    this.$q = $q;
    this.env = env;

    // Dependency Service
    this.paramService = new ParamService(hashData);
    this.requestService = new ApiService($http);
    this.queryService = new QueryService();
    this.esService = new ESService(G[env].urls.es);
};
SlbsApp.prototype = new App();

SlbsApp.prototype.getQueries = function () {

    var queryString = G.baseUrl + "/api/slbs?type=extended";
    /**
     * key: hashData.{{key}}*
     * type: slb query type: ['property','normal'].
     * symbol: used for slb admin to query
     * */
    var queries = [
        {
            key: 'pci',
            slb: 'pci',
            type: 'property',
            symbol: 'anyProp'
        },
        {
            key: 'idcs',
            slb: 'idc',
            type: 'property',
            symbol: 'anyProp'
        },
        {
            key: 'zones',
            slb: 'zone',
            type: 'property',
            symbol: 'anyProp'
        },
        {
            key: 'tags',
            type: 'normal',
            symbol: 'anyTag'
        },
        {
            key: 'domains',
            type: 'normal',
            symbol: 'domain'
        },
        {
            key: 'statuses',
            type: 'property',
            slb: 'status',
            symbol: 'anyProp'
        },
        {
            key: 'slbId',
            type: 'normal',
            symbol: 'slbId'
        },
        {
            key: 'slbName',
            type: 'normal',
            symbol: 'fuzzyName'
        },
        {
            key: 'hostIp',
            type: 'normal',
            symbol: 'ip'
        }
    ];
    var params = this.getQueryString(queries);
    return {
        'slbs_extended': {
            url: queryString + '&' + params.join('&'),
            method: 'GET'
        },
        'slbs_stastics': {
            method: 'GET',
            url: G.baseUrl + "/api/statistics/slbs"
        },
        'slbs_server_stastics': {
            method: 'GET',
            url: G.baseUrl + "/api/statistics/slbServers"
        }
    };
};
SlbsApp.prototype.request = function (query) {
    var out = getDataFunc();
    return out(this, this.$q, query);
};
var getDataFunc = function () {

    var result = {};
    var promise;

    var f = function (self, $q, query) {
        var slbsrequest = query['slbs_extended'];
        var slbstastics = query['slbs_stastics'];
        var slbserverstastics = query['slbs_server_stastics'];

        var requests = [
            self.sendRequest(slbstastics, function (res, code) {
                result['slb-stastics'] = self.getAllData(res['slb-metas'], 'slb-id');
            }),

            self.sendRequest(slbserverstastics, function (res, code) {
                result['slb-server-stastics'] = self.getAllData(res['slb-server-qpses'], 'ip');
            }),
            self.sendRequest(slbsrequest, function (res, code) {
                result['slbs'] = _.values(self.getAllData(res.slbs, 'id'));
            })
        ];

        promise = $q.all(requests);
        return {
            result: result,
            request: promise
        }
    };

    return f;
};
SlbsApp.prototype.getAllSlbs = function (orderKey) {
    var self = this.queryService;
    return self.get('all', orderKey, '', '');
};
SlbsApp.prototype.getTableData = function (slbs, stastics) {
    var cvip = this.hashData.vip;
    slbs = _.map(slbs,
        function (i) {
            var slbId = i.id;

            var ps = _.indexBy(i['properties'], 'name');
            var vips = _.indexBy(i['vips'], 'ip');
            // vip filter since no vip query param in api
            if (cvip) {
                if (!vips[cvip]) return undefined;
            }

            var statusText = '';
            var statusObj = ps['status'];
            if (statusObj) {
                statusText = statusObj['value'];
            }
            var stastic = stastics[slbId];

            return {
                id: i['id'],
                name: i['name'],
                vips: i['vips'],
                appCount: stastic ? (stastic['app-count'] || 0) : 0,
                vsCount: stastic ? (stastic['vs-count'] || 0) : 0,
                groupCount: stastic ? (stastic['group-count'] || 0) : 0,
                memberCount: stastic ? (stastic['member-count'] || 0) : 0,
                serverCount: stastic ? (stastic['group-server-count'] || 0) : 0,
                qps: stastic ? (stastic['qps'] || 0) : 0,
                'slb-servers': i['slb-servers'],
                properties: i['properties'],
                slbServersCount: i['slb-servers'].length || 0,
                idc: ps['idc'] ? ps['idc'].value : '-',
                status: statusText
            }
        });

    return _.reject(slbs, function (i) {
        return i == undefined;
    });
};
SlbsApp.prototype.getStasticSummaryData = function (stastics) {
    var result = {
        'vs-count': 0,
        'app-count': 0,
        'group-count': 0,
        'group-server-count': 0,
        'member-count': 0,
        'qps': 0
    };

    $.each(_.keys(result), function (i, item) {
        result[item] = reduce(item, _.values(stastics));
    });
    return result;
};
SlbsApp.prototype.getStatusSummaryData = function (slbs) {
    var m = _.countBy(slbs, function (item) {
        var ps = _.find(item['properties'], function (v) {
            return v.name == 'status';
        });

        if (ps && ps.value) return ps.value.toLowerCase();
        return '-';
    });

    return m;

};

SlbsApp.prototype.convertIDC = function (idc) {
    return this.esService.mappingIDC(idc) || idc;
};

SlbsApp.prototype.getEsHtml = function (query) {
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


function reduce(key, data) {
    return _.reduce(data, function (a, b) {
        return a + b[key];
    }, 0);
};