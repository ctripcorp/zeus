var VsShardingRepository = Repository.extend(
    {
        init: function ($http, command) {
            command = command || new RestCommand();
            this.callSuper($http, command);
            this.base = G.baseUrl+'/api/flow/slb/sharding';
        },

        newVsMigration: function (shardingId) {
            var path = this.base + '/vs/migration';
            var param = {};

            if(shardingId){
                param.id = shardingId;
            }

            return this.get(path, param);
        }
    });