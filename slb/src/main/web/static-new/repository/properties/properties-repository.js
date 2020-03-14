var PropertiesRepository = Repository.extend(
    {
        init: function ($http, command) {
            command = command || new RestCommand();
            this.callSuper($http, command);
            this.base = G.baseUrl;
        },
        getSlbProperties: function () {
            var path = this.base + '/api/properties?type=slb';
            return this.get(path);
        },
        getGroupProperties: function () {
            var path = this.base + '/api/properties?type=group';
            return this.get(path);
        },
        getVSProperties: function () {
            var path = this.base + '/api/properties?type=vs';
            return this.get(path);
        },
        setProperty: function (pname, pvalue, type, target) {
            var path = this.base + '/api/property/set?type='+type+'&pname='+pname+'&pvalue='+pvalue+'&targetId='+target;
            return this.get(path);
        }
    });