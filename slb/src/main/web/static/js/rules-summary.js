var request0 = {
    method: 'GET',
    url: G.baseUrl + '/api/groups'
};

var request1 = {
    method: 'GET',
    url: G.baseUrl + '/api/groups?type=info&anyTag=autoManaged'
};

var request2 = {
    method: 'GET',
    url: G.baseUrl + '/api/rules'
};

var origin = {};
var groups;
var rules;
var rules2;

var q1 = $http(request1).success(function (r, code) {
    groups = r['groups'];
});

var q2 = $http(request2).success(function (r, code) {
    rules = _.map(_.filter(r['rules'], function (s) {
        return s['target-type'].toLowerCase() == 'group';
    }), function (v) {

        v = parseInt(v['target-id']);
        return v;
    });


    rules2 = _.map(_.filter(r['rules'], function (s) {
        return s['target-type'].toLowerCase() == 'group';
    }), function (v) {
        return v;
    });

});

var q3 = $http(request0).success(function (r, code) {
    _.mapObject(_.indexBy(r['groups'], 'id'), function (v, k, item) {
        origin[k] = v['app-id'];
    });
});

$q.all([q1, q2, q3]).then(function () {

    var gids = _.pluck(groups, 'id');
    var rids = _.uniq(rules);


    var result = _.difference(rids, gids);

    var apps = [];
    var apprules = {};

    var rulebygroupid = _.indexBy(_.filter(rules2, function (s) {
        return result.indexOf(parseInt(s['target-id'])) != -1;
    }), 'target-id');

    _.map(result, function (v) {
        if (apps.indexOf(origin[v]) == -1) {
            apps.push(origin[v]);
        }
    });
    console.log(apps);

    var f = _.countBy(_.values(rulebygroupid), function (s) {
        return s['rule-type'];
    });
    console.log();