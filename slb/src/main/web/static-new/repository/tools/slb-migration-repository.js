var SlbMigrationRepository = Repository.extend(
    {
        init: function ($http, command) {
            command = command || new RestCommand();
            this.callSuper($http, command);
            this.base = G.baseUrl + '/api/flow/slb/creating';
        },
        getSlbMigrationById: function (id, type) {
            var path = this.base + '/get';
            var param = {};
            if (type) {
                param.type = type;
            }
            if (id) {
                param.id = id;
            }

            return this.get(path, param);
        },
        getSlbMigrations: function () {
            var path = this.base + '/list';

            return this.get(path, {});
        },
        saveNewSlbMigration: function (data) {
            var path = this.base + '/new';

            return this.post(data, path, {});
        },
        updateServersSlbMigration: function (data) {
            var path = this.base + '/update/servers';
            return this.post(data, path);
        },
        bypassSlbMigration: function (id, stage) {
            var path = this.base + '/bypass';
            var parm = {id: id};
            if (stage) {
                parm.stage = stage;
            }
            return this.get(path, parm);
        },
        updateSlbMigration: function (data) {
            var path = this.base + '/update';
            return this.post(data, path, {});
        },
        createCmsData: function (id) {
            var param = {};
            var path = this.base + '/createGroup';
            if (id) {
                param.id = id;
            }
            return this.get(path, param);
        },
        createCdngData: function (id) {
            var param = {};
            var path = this.base + '/expand';
            if (id) {
                param.id = id;
            }
            return this.get(path, param);
        },
        createRouteData: function (id) {
            var param = {};
            var path = this.base + '/createRoute';
            if (id) {
                param.id = id;
            }
            return this.get(path, param);
        },
        createDeployData: function (id) {
            var param = {};
            var path = this.base + '/deploy';
            if (id) {
                param.id = id;
            }
            return this.get(path, param);
        },
        createConfigData: function (id) {
            var param = {};
            var path = this.base + '/config';
            if (id) {
                param.id = id;
            }
            return this.get(path, param);
        },
        revert: function (id) {
            var param = {};
            if (id) {
                param.id = id;
            }
            var path = this.base + '/rollback';

            return this.get(path, param);
        }
    });