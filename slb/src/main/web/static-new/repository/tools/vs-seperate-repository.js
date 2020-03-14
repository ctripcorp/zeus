var VsSeperateRepository = Repository.extend(
    {
        init: function ($http, command) {
            command = command || new RestCommand();
            this.callSuper($http, command);
            this.base = '/api/flow/vs/split';
            this.mergepath = '/api/flow/vs/merge';
        },
        createNewSplit: function (data) {
            var path = this.base + '/new';
            return this.post(data, path);
        },
        updateSplit: function (data) {
            var path = this.base + '/update';
            return this.post(data, path);
        },
        getSplit: function (id) {
            var path = this.base + '/get';
            var param = {
                id: id
            };
            return this.get(path, param);
        },
        deleteSplit: function (id) {
            var path = this.base + '/delete';
            var param = {
                id: id
            };
            return this.get(path, param);
        },
        listSplits: function () {
            var path = this.base + '/list';
            return this.get(path);
        },
        bindSplit: function (id) {
            var path = this.base + '/bind/new/vs';
            var param = {
                id: id
            };
            return this.get(path, param);
        },
        activateSplit: function (id) {
            var path = this.base + '/split';
            var param = {
                id: id
            };
            return this.get(path, param);
        },
        revertSplit: function (id) {
            var path = this.base + '/rollback';
            var param = {
                id: id
            };
            return this.get(path, param);
        },
        validateSplit:function (vsId) {
            var path = this.base+'/validate';
            var param ={
                vsId: vsId
            };
            return this.get(path, param);
        },
        listMerges: function () {
            var path = this.mergepath + '/list';
            return this.get(path);
        },
        createNewMerge: function (data) {
            var path = this.mergepath + '/new';
            return this.post(data, path);
        },
        updateMerge: function (data) {
            var path = this.mergepath + '/update';
            return this.post(data, path);
        },
        cleanMerge: function (id) {
            var path = this.mergepath + '/clean';
            var param = {
                id: id
            };
            return this.get(path, param);
        },
        deleteMerge: function (id) {
            var path = this.mergepath + '/delete';
            var param = {
                id: id
            };
            return this.get(path, param);
        },
        getMergeById: function (id) {
            var path = this.mergepath + '/get';
            var param = {
                id: id
            };
            return this.get(path, param);
        },
        bindMerge: function (id) {
            var path = this.mergepath + '/bind/new/vs';
            var param = {
                id: id
            };
            return this.get(path, param);
        },
        mergeVs: function (id) {
            var path = this.mergepath + '/merge';
            var param = {
                id: id
            };
            return this.get(path, param);
        },
        validateMerge:function (vsIds) {
            var path = this.mergepath+'/validate';
            var param ={
                vsId: vsIds
            };
            return this.get(path, param);
        },
        revertMerge: function (id) {
            var path = this.mergepath + '/rollback';
            var param = {
                id: id
            };
            return this.get(path, param);
        },
        getCertificateByDomain: function (domain) {
            return new Promise(function (resolve, reject) {
                resolve
            });
        }
    });