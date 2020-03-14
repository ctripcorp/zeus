var SlbMigrationService = Class.extend({
    init: function ($http) {
        var slbMigrationDao = SlbMigrationRepository.create($http);
        this.slbMigrationDao = slbMigrationDao;

        this.statusMapping = {
            'CREATED': '已创建',
            'START_CREATE_CMS_GROUP': '资源申请',
            'FAIL_CREATE_CMS_GROUP': '资源申请',
            "FINISH_CREATE_CMS_GROUP": '资源申请',
            'START_EXPANSION': '资源申请',
            'DOING_EXPANSION': '资源申请',
            'FINISH_EXPANSION': '资源申请',
            'FAIL_EXPANSION': '资源申请',
            'START_CREATE_CMS_ROUTE': '资源申请',
            'FINISH_CREATE_CMS_ROUTE': '资源申请',
            'FAIL_CREATE_CMS_ROUTE': '资源申请',
            'START_DEPLOYMENT': '资源申请',
            'DEPLOYING': '资源申请',
            'FINISH_DEPLOYMENT': '资源申请',
            'FAIL_DEPLOYMENT': '资源申请',
            'START_CREATE_SLB': '资源申请',
            'FINISH_CREATE_SLB': '成功创建',
            'FAIL_CREATE_SLB': '资源申请'
        };
    },
    getSlbMigrationById: function (id, type) {
        type = type || 'extended';
        var migrationPromise = this.slbMigrationDao.getSlbMigrationById(id, type);
        return migrationPromise;
    },

    getSlbMigrations: function (slbs) {
        var that = this;
        var migrationPromise = this.slbMigrationDao.getSlbMigrations();
        return migrationPromise.then(function (data) {
            var migrations = new C().parseSlbMigrations(data.data, that.statusMapping, slbs);
            return migrations;
        });
    },
    getSlbMigrationsWithStatus: function () {
        var that = this;
        var promise = this.getSlbMigrations();
        return promise.then(function (data) {
            var v = _.countBy(data, 'status');
            // var statusRevers = that.statusMapping;
            // $.each(statusRevers,function (i, s) {
            //     if(!v[s]) v[s]=0;
            // });
            return v;

        });
    },
    saveNewSlbMigration: function (data) {
        var migrationPromise = this.slbMigrationDao.saveNewSlbMigration(data);
        return migrationPromise;
    },
    updateServersSlbMigration: function (data) {
        var migrationPromise = this.slbMigrationDao.updateServersSlbMigration(data);
        return migrationPromise;
    },
    updateSlbMigration: function (data) {
        var migrationPromise = this.slbMigrationDao.updateSlbMigration(data);
        return migrationPromise;
    },
    bypassSlbMigration: function (id, stage) {
        var migrationPromise = this.slbMigrationDao.bypassSlbMigration(id, stage);
        return migrationPromise;
    },
    createCmsData: function (id) {
        var migrationPromise = this.slbMigrationDao.createCmsData(id);
        return migrationPromise;
    },
    createCdngData: function (id) {
        var migrationPromise = this.slbMigrationDao.createCdngData(id);
        return migrationPromise;
    },
    createRouteData: function (id) {
        var migrationPromise = this.slbMigrationDao.createRouteData(id);
        return migrationPromise;
    },
    createDeployData: function (id) {
        var migrationPromise = this.slbMigrationDao.createDeployData(id);
        return migrationPromise;
    },
    createConfigData: function (id) {
        var migrationPromise = this.slbMigrationDao.createConfigData(id);
        return migrationPromise;
    },
    revertChanges: function (id) {
        var revertPromise = this.slbMigrationDao.revert(id);
        return revertPromise;
    }
});