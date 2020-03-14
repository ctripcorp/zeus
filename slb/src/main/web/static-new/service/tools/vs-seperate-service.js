var VsSeperateService = Class.extend(
    {
        init: function ($http) {
            var splitdao = VsSeperateRepository.create($http);
            this.splitdao = splitdao;
        },

        createNewSplit: function (data) {
            return this.splitdao.createNewSplit(data);
        },
        updateSplit: function (data) {
            return this.splitdao.updateSplit(data);
        },
        getSplit: function (id) {
            return this.splitdao.getSplit(id);
        },
        deleteSplit: function (id) {
            return this.splitdao.deleteSplit(id);
        },
        listSplits: function () {
            return this.splitdao.listSplits();
        },
        bindSplit: function (id) {
            return this.splitdao.bindSplit(id);
        },
        activateSplit: function (id) {
            return this.splitdao.activateSplit(id);
        },
        validateSplit: function (vsId) {
            return this.splitdao.validateSplit(vsId);
        },
        revertSplit:function (id) {
            return this.splitdao.revertSplit(id);
        },
        listMerges: function () {
            return this.splitdao.listMerges();
        },
        createNewMerge: function (data) {
            return this.splitdao.createNewMerge(data);
        },
        updateMerge: function (data) {
            return this.splitdao.updateMerge(data);
        },
        cleanMerge: function (id) {
            return this.splitdao.cleanMerge(id);
        },
        deleteMerge: function (id) {
            return this.splitdao.deleteMerge(id);
        },
        getMergeById: function (id) {
            return this.splitdao.getMergeById(id);
        },
        bindMerge: function (id) {
            return this.splitdao.bindMerge(id);
        },
        mergeVs: function (id) {
            return this.splitdao.mergeVs(id);
        },
        validateMerge: function (vsIds) {
            return this.splitdao.validateMerge(vsIds);
        },
        revertMerge:function (id) {
            return this.splitdao.revertMerge(id);
        },
        getCertificateByDomain: function (domain) {
            return this.splitdao.getCertificateByDomain(domain);
        }
    });