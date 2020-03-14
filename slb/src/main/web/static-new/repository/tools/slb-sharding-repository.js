var SlbShardingRepository = Repository.extend(
    {
        init: function ($http, command) {
            command = command || new RestCommand();
            this.callSuper($http, command);
            this.base = G.baseUrl+'/api/flow/slb/sharding';
        },
        saveSlbSharding: function (data) {
            var path = this.base + '/new';

            return this.post(data, path, {});
        },
        newSlbData: function (shardingId) {
            var path = this.base + '/create/slb';
            var param = {
                id: shardingId
            };
            return this.get(path, param);
        },
        getSlbShardingById: function (shardingId) {
            var path = this.base + '/get';
            var param = {
                id: shardingId
            };
            return this.get(path, param);
        },
        getSlbShardings: function () {
            var path = this.base + '/list';
            return this.get(path);
        }
    });