var LogRepository = Repository.extend(
    {
        init: function ($http, command) {
            command = command || new RestCommand();
            this.callSuper($http, command);
            this.base = G.baseUrl;
        },
        getLogs: function (fromDate, toDate, targetId, type) {
            var path = this.base + '/api/groups';
            var param = {
                appId: appId
            };
            if (fromDate) {
                param.fromDate = fromDate;
            }
            if (toDate) {
                param.toDate = toDate;
            }
            if (targetId) {
                param.targetId = targetId;
            }
            if (type) {
                param.type = type;
            }

            return this.get(path, param);
        }
    }
);