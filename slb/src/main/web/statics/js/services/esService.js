/**
 * Created by ygshen on 2017/8/2.
 */
function ESService(esUrl) {
    this.es = esUrl;
};

ESService.prototype.mappingIDC = function (chinese) {
    var mapping = {
        金桥: 'SHAJQ',
        欧阳: 'SHAOY',
        南通: 'NTGXH',
        福泉: 'SHAFQ',
        金钟: 'SHAJZ',
        日阪: 'SHARB',
        'SHARB': 'SHARB'
    };

    return mapping[chinese];
};

ESService.prototype.getESLink = function (env, query) {
    if (!this.es) return '-';
    return this.es + '?query=' + query;
};
