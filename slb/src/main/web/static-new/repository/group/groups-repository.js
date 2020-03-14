var GroupsRepository = Repository.extend(
    {
        init: function ($http, command) {
            command = command || new RestCommand();
            this.callSuper($http, command);
            this.base = G.baseUrl;
            this.env = G.env;
        },
        getGroupsByAppId: function (appId, type) {
            var path = this.base + '/api/groups';
            var param = {
                appId: appId
            };
            if (type) {
                param.type = type;
            }

            return this.get(path, param);
        },
        getGroupsByIds: function (ids, type) {
            var path = this.base + '/api/groups';

            var param = {};
            if (type) {
                param.type = 'extended';
            }
            if (ids && ids.length > 0) {
                param.groupId = ids.join(',');
            }
            var promise = this.get(path, param);
            return promise;
        },
        getGroups: function (type) {
            var path = this.base + '/api/groups';
            var param = {};
            if (type) {
                param.type = 'extended';
            }
            return this.get(path, param);
        },
        getGroupsStatusByAppId: function (appId) {
            var path = this.base + '/api/status/groups';
            var param = {
                appId: appId
            };
            return this.get(path, param);
        },
        getGroupsStatus: function () {
            var path = this.base + '/api/status/groups';
            return this.get(path);
        },
        getGroupsStatics: function () {
            var path = this.base + '/api/statistics/groups';
            return this.get(path, undefined);
        }
    }
);