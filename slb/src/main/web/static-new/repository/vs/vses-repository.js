var VsesRepository = Repository.extend(
    {
        init: function ($http, command) {
            command = command || new RestCommand();
            this.callSuper($http, command);
            this.base = G.baseUrl;
        },
        getVses: function (type) {
            var path = this.base + '/api/vses';
            var param = undefined;

            if (type) {
                param = {
                    type: type
                };
            }
            return this.get(path, param);
        },
        getVsById: function (type, vsId) {
            var path = this.base + '/api/vs';
            type = type || 'extended';

            var param = {
                type: type,
                vsId: vsId
            };

            return this.get(path, param);
        },
        getVsByIds: function (type, vsIds) {
            var path = this.base + '/api/vses';
            type = type || 'extended';
            path += "?vsId=" + vsIds.join(',');
            var param = {
                type: type
            };

            return this.get(path, param);
        }
    }
);