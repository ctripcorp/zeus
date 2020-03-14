/**
 * Created by ygshen on 2017/8/2.
 */
function UserApp(env){
    this.env=env;

    this.esService = new ESService(G[env].urls.es);
};

UserApp.prototype.convertIDC = function (idc) {
    return this.esService.mappingIDC(idc) || idc;
};

UserApp.prototype.getEsHtml = function (query) {
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


