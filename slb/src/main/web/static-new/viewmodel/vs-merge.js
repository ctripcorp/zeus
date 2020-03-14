var vsMergeObj = {
    id: '',
    name: '',
    vsesValid:'',
    status: '',
    'source-vses': {},

    'target-vs': {},
    validation: {
        'ssl-error': '当前VS的SSL与已选择VS的SSL冲突',
        'status-error': '当前Virtual Sever 状态不是激活状态'
    },
    stages: {},

    'create-time': '',

    getId: function () {
        return this.id;
    },

    getStagesCount: function () {
        return _.keys(this.stages).length;
    },
    getStage: function (key) {
        return this.stages[key];
    },
    addStage: function (key, stage) {
        if (!this.stages[key]) {
            this.stages[key] = stage
        }
    },
    replaceStage: function (key, stage) {
        this.stages[key] = stage
    },
    validateSourceVs: function (vs) {
        var errorMsgObj = this.validation;
        var status = vs.status;
        if (!status || status.toLowerCase() != 'activated') return new SlbException(errorMsgObj['status-error']);
        var ssl = vs.ssl;

        var values = _.pluck(_.values(this['source-vses']), 'ssl');
        var uniq = _.uniq(values);
        if (uniq.length == 0) return true;

        if (ssl != uniq[0]) return new SlbException(errorMsgObj['ssl-error']);
        return true;
    },
    addSourceVs: function (vs) {
        this['source-vses'] = _.extend(this['source-vses'], vs);
    },
    removeSourceVs: function (vsId) {
        delete this['source-vses'][vsId];
    },
    getSourceVses: function () {
        return this['source-vses'];
    },
    getSourceVsSSL: function () {
        var sources = this['source-vses'];
        var values = _.pluck(_.values(sources), 'ssl');
        var uniq = _.uniq(values);
        return uniq[0];
    },
    getSourceVsDomains: function () {
        var sources = this['source-vses'];
        var values = _.pluck(_.values(sources), 'domains');
        return _.pluck(_.flatten(values), 'name');
    },
    getTargetVs: function () {
        return this['target-vs'];
    },
    finishCreation: function () {
        this.trigger('finish-creation');
    },
    finishLoading: function () {
        this.trigger('finish-loading');
    },
    startRevert: function () {
        this.trigger('start-revert');
    },
    getFailedStages: function () {
        return _.pick(this.stages, function (v, k, item) {
            return v.status && (v.status.toLowerCase() == 'fail');
        });
    },
    toMergeDo: function () {
        var mergeDo = {};

        if (this.id) mergeDo.id = this.id;
        if (this.name) mergeDo.name = this.name;
        if (this.cid) mergeDo.cid = this.cid;
        if (this.status) mergeDo.status = this.status;
        if (this['source-vses']) {
            mergeDo['source-vs-id'] = _.keys(this['source-vses']);
        }

        return mergeDo;
    },
    toMerge: function (mergeDo, vses) {
        var notExistedVs={
            name: '已下线',
            status:'未知',
            idc: '未知',
            zone:'未知',
            domains:[{
                name: '-'
            }],
            statistics:{
                "app-count" : 0,
                "group-server-count" : 0,
                "qps" : 0,
                "group-count" : 0,
                "member-count" : 0
            }
        };
        var mapping = constants.vsMergeStatus;

        this.id = mergeDo.id;
        this.name = mergeDo.name;
        var status = mergeDo.status;

        this.status = mapping[status] || status;

        this.cid = mergeDo.cid;

        // source vses
        var sourceVsIds = mergeDo['source-vs-id'];
        var sourceVses = {};
        $.each(sourceVsIds, function (i, v) {
            notExistedVs.id = v;
            sourceVses[v] =(vses && vses[v]) ? vses[v] : notExistedVs;
        });

        this['source-vses'] = sourceVses;

        // target vs

        var targetVsId = mergeDo['new-vs-id'];
        if (targetVsId) {
            notExistedVs.id = targetVsId;
            this['target-vs']={};
            this['target-vs'][targetVsId] = (vses && vses[targetVsId])? vses[targetVsId] :  notExistedVs;
        }

        // stages
        if (mergeDo['created']) {
            this.stages['created'] = mergeDo['created'];
        }else{
            delete this.stages['created']
        }

        if (mergeDo['create-and-bind-new-vs']) {
            this.stages['create-and-bind-new-vs'] = mergeDo['create-and-bind-new-vs'];
        }else{
            delete this.stages['create-and-bind-new-vs']
        }

        if (mergeDo['merge-vs']) {
            this.stages['merge-vs'] = mergeDo['merge-vs'];
        }else{
            delete this.stages['merge-vs']
        }

        if (mergeDo['clean-vs']) {
            this.stages['clean-vs'] = mergeDo['clean-vs'];
        }else{
            delete this.stages['clean-vs']
        }

        if (mergeDo['rollback']) {
            this.stages['rollback'] = mergeDo['rollback'];
        }else{
            delete this.stages['rollback']
        }

        if (mergeDo['create-time']) {
            this['create-time'] = mergeDo['create-time'];
        }else{
            delete this['create-time']
        }
    }
};