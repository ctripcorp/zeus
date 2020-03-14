/**
 * Created by ygshen on 2017/5/16.
 */
function App(){

};

App.prototype.getQueryString = function (collections) {
    var params = [];
    var self = this.paramService;
    $.each(collections, function (i, item) {
        var hashKey = item.key;
        var slbKey = item.slb;
        var command = item.type;
        var symbol = item.symbol;

        var pair = self.extractParam(hashKey, slbKey);
        var str = self.getParamString(command, pair);
        if (str && str != '') {
            params.push(symbol + '=' + str);
        }
    });
    return params;
};

App.prototype.sendRequest = function (request,callback) {
    return this.requestService.Request(request,callback);
};

App.prototype.getAllData = function (datasets, orderKey) {
    var self = this.queryService;
    return self.get(datasets, 'all', orderKey, '', '');
};
App.prototype.getFilterData = function (datasets, orderKey, filter) {
    var self = this.queryService;
    return self.get(datasets,'filter', orderKey, '', filter);
};

App.prototype.getGroupData = function (datasets, groupByKey) {
    var self = this.queryService;
    return self.get(datasets,'group', groupByKey, '', '');
};
App.prototype.getGroupFilterData = function (groupByKey, filter) {
    var self = this.queryService;
    return self.get('groupfilter', groupByKey, '', filter);
};



