var SlbsService = Class.extend({
    init: function ($http) {
        var slbdao = SlbsRepository.create($http);
        this.slbdao = slbdao;
    },


    getAllSlbs: function (type) {
        var slbdao = this.slbdao;
        var slbsPromise = slbdao.getSlbs(type);

        return slbsPromise.then(function (data) {
            var slbs = data.data;
            return new C().parseSlbs(slbs);
        });
    },
    getSlbById: function (id, type) {
        var slbdao = this.slbdao;
        var slbPrmomise = slbdao.getSlbById(id, type);
        return slbPrmomise.then(function (data) {
            var slb = data;
            return new C().parseSlb(slb);
        });
    },
    getSlbByName: function (name, type) {
        var slbdao = this.slbdao;
        var slbPrmomise = slbdao.getSlbByName(name, type);
        return slbPrmomise.then(function (data) {
            var slb = data;
            return new C().parseSlb(slb);
        });
    },
    getAllAppsMeta: function () {
        // return the promise
        return this.appdao.getAppsMeta();
    }
});