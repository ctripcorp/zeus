var SlbShardingService = Class.extend({
    init: function ($http, $q) {
        var shardingDao = SlbShardingRepository.create($http);
        this.shardingDao = shardingDao;
        var slbsDao = SlbsRepository.create($http);
        this.slbsDao = slbsDao;
        this.$q = $q;
    },
    saveSlbSharding: function (data) {
        return this.shardingDao.saveSlbSharding(data);
    },
    newSlbData: function (shardingId) {
        return this.shardingDao.newSlbData(shardingId);
    },
    getSlbShardingById: function (id) {
        return this.shardingDao.getSlbShardingById(id);
    },
    getSlbShardings: function () {
        var slbsPromise = this.slbsDao.getSlbs('extended');
        var shardingPromise = this.shardingDao.getSlbShardings();

        return this.$q.all([slbsPromise, shardingPromise]).then(function (data) {
            var slbs = data[0].data.slbs;
            slbs=_.indexBy(slbs,'id');
            var shardings = data[1].data;

            var result = new C().parseSlbSharding(shardings, slbs);
            return result;
        });
    }
});