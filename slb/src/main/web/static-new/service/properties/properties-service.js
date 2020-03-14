var PropertiesService = Class.extend({
    init: function ($http) {
        var propertiesdao = PropertiesRepository.create($http);
        this.propertiesdao = propertiesdao;
    },
    getSlbProperties: function () {
        var propertiesdao = this.propertiesdao;
        var propertiespromise = propertiesdao.getSlbProperties();
        return propertiespromise.then(function (data) {
            var properties = data.data;
            return new C().parseProperties(properties);
        });
    },
    setProperty: function (pname, pvalue, type, target) {
        var propertiesdao = this.propertiesdao;
        var promise = propertiesdao.setProperty(pname, pvalue, type, target);
        return promise;
    }
});