var AppRepository = Repository.extend(
    {
        init: function ($http, command) {
            command = command || new RestCommand();
            this.callSuper($http, command);
            this.base = G.baseUrl;
            this.env = G.env;
        },
        getApps: function () {
            var path = this.base + '/api/apps';
            return this.get(path, undefined);
        },
        getAppsMeta: function () {
            var path = this.base + '/api/meta/apps';
            var param = {
                timestamp: new Date().getTime()
            };
            return this.get(path, param);
        },
        getAppsByAppId: function (appId) {
            var path = this.base + '/api/apps';
            var param = {
                appId: appId
            };
            return this.get(path, param);
        },
        getAppsByAppIds: function (appIds) {
            var path = this.base + '/api/apps';
            appIds = _.map(appIds, function (v) {
                return 'appId=' + v;
            });
            path += '?' + appIds.join('&');

            return this.get(path, undefined);
        }
    }
);