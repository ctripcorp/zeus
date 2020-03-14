var vsSeperateObj = {

    name: '',
    id: '',
    setId: function (id) {
        this.id = id;
    },
    getId: function () {
        return this.id;
    },
    status: '',
    setStatus: function (status) {
        this.status = status;
    },
    'created-time': '',
    // Data zone
    Steps: {},

    Stages: {},

    Data: {
        originVs: {},
        targetVs: {
            total: 2
        },
        createdVs: {}
    },
    validation: {
        'one-domain-error': '当前Virtual Server只有一个domain',
        'status-error': '当前Virtual Sever 状态不是激活状态'
    },

    // initialize
    initialize: function () {
        this.Steps = {
            'select-vs': {
                index: 0,
                vs: ''
            }
        };
    },
    // Steps
    newSelectVsStep: function (vsId) {
        this.Steps['select-vs'] = {
            'select-vs': {
                index: 0,
                vs: vsId
            }
        };
    },

    newAddVsStep: function () {
        this.Steps['add-vs'] = {
            index: 1,
            vses: ''
        };
    },
    newCreateVsStep: function () {
        this.Steps['create-vs'] = {
            index: 2,
            vses: ''
        };
    },

    getSteps: function () {
        return this.Steps;
    },

    // First step
    setOriginVs: function (vsId) {
        this.Steps['select-vs'].vs = vsId;
        // trigger select vs
        this.trigger('select-vs');
    },
    getOriginVs: function () {
        return this.Steps['select-vs'].vs;
    },

    setOriginVsData: function (vs, invalidMsg) {
        this.Data.originVs = vs;
        if (invalidMsg) this.Data.originVs.error = invalidMsg;
    },

    getOriginVsData: function () {
        return this.Data.originVs;
    },
    validateOriginVs: function (vs) {
        var errorMsgObj = this.validation;
        var status = vs.status;
        if (!status || status.toLowerCase() != 'activated') return new SlbException(errorMsgObj['status-error']);
        var domains = vs.domains;
        if (!domains || domains.length == 1) return new SlbException(errorMsgObj['one-domain-error']);
        return true;
    },


    // Second step
    setTargetVs: function (vs) {
        if (!this.Data.targetVs.vses) {
            this.Data.targetVs.vses = [vs];
        } else {
            this.Data.targetVs.vses.push(vs);
        }
    },
    replaceTargetVs: function (index, vs) {
        this.Data.targetVs.vses[index] = vs;
    },
    getTargetVsData: function () {
        return this.Data.targetVs;
    },
    resetTargetVs: function () {
        this.Data.targetVs.vses = '';
    },
    getTargetVsDomains: function () {
        var originVs = this.Data.originVs;
        var targetVs = this.Data.targetVs.vses;

        var originVsDomains = _.pluck(originVs.domains, 'name');
        var targetVsDomains = targetVs ? _.flatten(targetVs) : [];

        // Not has slot to share?
        if (targetVsDomains.length == originVsDomains.length) {
            return new SlbException("域名已经被分配完!");
        } else {
            var domains = _.difference(originVsDomains, targetVsDomains);
            return _.map(domains, function (v) {
                return {
                    domain: v
                }
            });
        }
    },

    // Third step
    setCreatedVs: function (vses) {
        this.Data.createdVs = vses;
    },
    getCreatedVs: function (vses) {
        return this.Data.createdVs;
    },

    // Stages
    setStage: function (key, stage) {
        // remove the old
        this.Stages[key] = stage;
    },
    getStage: function (key) {
        return this.Stages[key];
    },

    getFailedStages: function () {
        return _.pick(this.Stages, function (v, k, item) {
            return v.status && v.status.toLowerCase() == 'fail';
        });
    },

    startRevert: function () {
        this.trigger('start-revert');
    },

    // Parse do entity to view entity
    toEntity: function (entity) {
        var mapping = constants.vsSplitStatus;
        var name = entity.name;
        var id = entity.id;
        var status = entity.status;
        var createdTime = entity['create-time'];
        var sourceVsId = entity['source-vs-id'];
        var targetVses = entity['domain-groups'];
        var newVsIds = entity['new-vs-ids'];

        var createdStage = entity['created'];
        var createAndBindStage = entity['create-and-bind-new-vs'];
        var splitStage = entity['split-vs'];
        var rollbackStage = entity['rollback'];


        this.name = name;
        this.id = id;
        this.status = mapping[status] || status;
        this.statusdo = status;
        this['create-time'] = createdTime;
        if (sourceVsId) {
            this.newSelectVsStep(sourceVsId);
            this.newAddVsStep();
            this.setOriginVsData({
                id: sourceVsId
            });
        }

        if (targetVses && targetVses.length > 0) {
            this.Data.targetVs.total = targetVses.length;
            this.Data.targetVs.vses = targetVses;
        }

        if (createdStage) {
            this.Stages['created'] = createdStage;
        }else{
            delete this.Stages['created'];
        }

        if (createAndBindStage) {
            this.Stages['create-and-bind-new-vs'] = createAndBindStage;
        }else{
            delete this.Stages['create-and-bind-new-vs'];
        }

        if (splitStage) {
            this.Stages['split-vs'] = splitStage;
        }else{
            delete this.Stages['split-vs'];
        }
        if (rollbackStage) {
            this.Stages['rollback'] = rollbackStage;
        }else{
            delete this.Stages['rollback'];
        }
        if ((newVsIds && newVsIds.length > 0) || createAndBindStage || splitStage) {
            this.newCreateVsStep();
        }

        return this;
    },
    // Parse view entity to do entity
    toEntityDo: function () {
        var result = {};

        if (this.name) result.name = this.name;

        if (this.id) result.id = this.id;

        if (this.status) result.status = this.statusdo;

        if (this['created-time']) result['created-time'] = this['created-time'];

        // source vs id
        if (this.Data.originVs.id) result['source-vs-id'] = this.Data.originVs.id;

        // target domains
        var targets = this.Data.targetVs.vses;
        if (targets && targets.length > 0) {
            var isBlank = _.flatten(targets);
            if (isBlank.length > 0) {
                result['domain-groups'] = targets;
            }
        }

        // created vs
        var createdVs = this.Data.createdVs;
        if (createdVs && _.keys(createdVs).length > 0) {
            result['new-vs-ids'] = _.keys(createdVs);
        }

        return result;
    }
};

