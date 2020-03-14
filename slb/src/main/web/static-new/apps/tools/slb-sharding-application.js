var SlbShardingApplication = Class.extend({
    init: function ($http, $q, env) {
        this.env = env;
        this.slbsservice = SlbsService.create($http);
        this.shardingservice = SlbShardingService.create($http, $q);
        if ((typeof VsMigrationService)!=="undefined") {
            this.vsmigrationservice = VsMigrationService.create($http);
        }
    },
    getSlbDetail: function (id) {
        return this.slbsservice.getSlbById(id, 'extended');
    },
    saveSlbSharding: function (data) {
        return this.shardingservice.saveSlbSharding(data);
    },
    newSlbData: function (shardingId) {
        return this.shardingservice.newSlbData(shardingId);
    },
    newVsMigration: function (shardingId) {
        return this.vsmigrationservice.newVsMigration(shardingId);
    },
    newVsMigration: function (shardingId) {
        return this.vsmigrationservice.newVsMigration(shardingId);
    },
    getSlbShardingById: function (id) {
        return this.shardingservice.getSlbShardingById(id);
    },
    getSlbShardings: function () {
        return this.shardingservice.getSlbShardings();
    }
});