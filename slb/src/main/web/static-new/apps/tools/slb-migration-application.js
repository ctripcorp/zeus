var SlbMigrationApplication = Class.extend({
    init: function ($http, $q, env) {
        this.$q = $q;
        this.env = env;
        this.propertiesservice = PropertiesService.create($http);
        this.slbsservice = SlbsService.create($http);
        this.slbMigrationService = SlbMigrationService.create($http);
    },
    getAllSlbs: function () {
        var slbsservice = this.slbsservice;
        var slbsPromise = slbsservice.getAllSlbs('extended');

        return slbsPromise.then(function (data) {
            return data;
        });
    },
    generateSlbData: function () {
        var slbIdcsPromise = this.propertiesservice.getSlbProperties();
        return slbIdcsPromise;
    },
    getSlbMigrationById: function (id) {
        var slbMigrationByIdPromise = this.slbMigrationService.getSlbMigrationById(id);
        return slbMigrationByIdPromise;
    },
    saveNewSlbMigration: function (data) {
        var slbMigrationByIdPromise = this.slbMigrationService.saveNewSlbMigration(data);
        return slbMigrationByIdPromise;
    },
    updateSlbMigration: function (data) {
        var slbMigrationUpdatePromise = this.slbMigrationService.updateSlbMigration(data);
        return slbMigrationUpdatePromise;
    },

    updateServersSlbMigration: function (data) {
        var slbMigrationUpdatePromise = this.slbMigrationService.updateServersSlbMigration(data);
        return slbMigrationUpdatePromise;
    },
    bypassSlbMigration: function (id, stage) {
        var slbMigrationUpdatePromise = this.slbMigrationService.bypassSlbMigration(id, stage);
        return slbMigrationUpdatePromise;
    },
    getSlbMigrations: function (slbs) {
        var slbsMigrations = this.slbMigrationService.getSlbMigrations(slbs);
        return slbsMigrations;
    },
    getSlbMigrationsWithStatus: function (resource) {
        var countMigrations = this.slbMigrationService.getSlbMigrationsWithStatus();

        var status = {};
        return countMigrations.then(function (data2) {
            $.each(data2, function (k, v) {
                status[k] = v;
            });
            return status;
        });
    },

    createCmsData: function (migrationId) {
        return this.slbMigrationService.createCmsData(migrationId);
    },
    createCdngData: function (migrationId) {
        return this.slbMigrationService.createCdngData(migrationId);
    },
    createRouteData: function (migrationId) {
        return this.slbMigrationService.createRouteData(migrationId);
    },
    createDeployData: function (migrationId) {
        return this.slbMigrationService.createDeployData(migrationId);
    },
    createConfigData: function (migrationId) {
        return this.slbMigrationService.createConfigData(migrationId);
    },
    revertChanges: function (migrationId) {
        return this.slbMigrationService.revertChanges(migrationId);
    }
});
