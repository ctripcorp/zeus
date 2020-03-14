var VsStatisticsRepository = Repository.extend(
    {
        init: function ($http, command) {
            command = command || new RestCommand();
            this.callSuper($http, command);
            this.base = G.baseUrl;
        },
        getVsesStatistics: function () {
            var path = this.base + '/api/statistics/vses';
            return this.get(path);
        },
        getVsStatisticsById: function (vsId) {
            var path = this.base + '/api/statistics/vses';

            var param = {
                vsId: vsId
            };

            return this.get(path, param);
        },
        getVsStatisticsByIds: function (vsIds) {
            var path = this.base + '/api/statistics/vses';
            var query = "?";
            vsIds.forEach(function (id) {
                query += "vsId=" + id + "&";
            });
            path += query
            return this.get(path);
        }
    }
);