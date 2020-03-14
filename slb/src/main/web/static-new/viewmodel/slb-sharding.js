var slbSharding = {
    id: '',
    name: '',
    status: '',
    // 创建时间
    createTime: '',
    targetSlbId: '',
    // 新建SLB的进度
    newSlbEntity: '',
    // VS迁移的进度
    vsMigrationEntity: '',
    toShardingDo: function () {
        var target = {};
        if (this.name) target.name = this.name;

        if (this.targetSlbId) target['related-slb-id'] = this.targetSlbId;

        target['created-time'] = $.format.date(new Date().getTime(), 'yyyy-MM-dd HH:mm:ss');

        target.status = 'new';

        return target;
    },

    toSharding: function (shardingDo) {
        if (shardingDo) {
            //this.id = shardingDo.id;
            this.name = shardingDo.name;
            this.status = shardingDo.status;
            this.targetSlbId = shardingDo['related-slb-id'];
            this.newSlbEntity = shardingDo['slb-creating-entity'];
            this.createTime = shardingDo['create-time'];
            this.vsMigrationEntity = shardingDo['vs-migration'];
            return this;
        }
        return undefined;
    },

    init: function (name, targetSlbId, status, createdTime, slbEntity, vsEntity) {
        this.name = name;
        if (targetSlbId) {
            this.targetSlbId = targetSlbId;
        }
        if (status) {
            this.status = status;
        }

        if (createdTime) {
            this.createTime = createdTime;
        }
        if (slbEntity) {
            this.newSlbEntity = slbEntity;
        }
        if (vsEntity) {
            this.vsMigrationEntity = vsEntity;
        }
    },

    setTargetSlbId: function (id) {
        var that = this;
        that.targetSlbId = id;
    },

    getTargetSlbId: function () {
        return this.targetSlbId;
    },

    setName: function (name) {
        this.name = name;
    },

    getSteps: function () {
        return this.steps;
    },

    getSlbEntity: function () {
        var entity = this.newSlbEntity;
        var statusMapping = constants.slbCreationStatusMap;
        entity.status = statusMapping[entity.status] || '已创建';
        return entity;
    },

    getVsMigrationEntity: function () {
        var entity = this.vsMigrationEntity;
        if(entity){
            var content = JSON.parse(entity.content);
            var status = content.status;
            var migrationStatus = constants.vsMigrationStatus;
            entity.content = content;
            entity.status = migrationStatus[status];
            return entity;
        }
    }
};

var slbShardings = {
    count: '',
    data: [],
    init: function (shardings) {
        this.data = shardings;
        this.count = shardings.length;
    },
    statusCountMap: {

    },
    countByStatus: function (resource) {
        var statusMapping = constants.slbShardingStatusMap;

        var resultMapping = _.invert(statusMapping);

        var shardings = this.data;
        var c= _.countBy(shardings, function (v) {
            return v.status;
        });
        var result={};
        $.each(resultMapping,function (text, type) {
            if(c[text]){
                if (resource.tools) {
                    var translated = resource["tools"]["sharding"]["js"][text];
                    result[translated] = c[text];
                } else {
                    result[text] = c[text];
                }
                return;
            }
            if (resource.tools) {
                var translated = resource["tools"]["sharding"]["js"][text];
                result[translated] = 0;
            } else {
                result[text] = 0;
            }
        });
        return result;
    },
    getData: function () {
        return this.data;
    },
    getCount: function () {
        return this.count;
    }
};
