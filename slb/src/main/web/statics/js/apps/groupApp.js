/**
 * Created by ygshen on 2017/8/9.
 */
function GroupApp(hashData, $http, $q, env){
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
GroupApp.prototype = new App();

GroupApp.prototype.convertIDC = function (idc) {
    return this.esService.mappingIDC(idc) || idc;
};
GroupApp.prototype.getEsHtml = function (query) {
    var esLink =this.esService.getESLink(this.env, query);
    if(esLink=='-'){
        return '<div class="system-link">' +
            '<span class="pull-left">-</span>' +
            '</div>';
    }
    return '<div class="system-link">' +
        '<a class="pull-left es" title="ES" target="_blank" href="' +esLink+ '">ES</a>' +
        '</div>';
};
