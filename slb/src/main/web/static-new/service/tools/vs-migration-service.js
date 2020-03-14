var VsMigrationService = Class.extend({
    init: function ($http) {
        var migrationDao = VsShardingRepository.create($http);
        this.migrationDao = migrationDao;
    },
    newVsMigration: function (shardingId) {
        return this.migrationDao.newVsMigration(shardingId);
    }
});