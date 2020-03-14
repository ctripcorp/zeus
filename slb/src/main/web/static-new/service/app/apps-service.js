var AppService = Class.extend({
    init: function ($http, $q) {
        var appdao = AppRepository.create($http);
        var userdao = UserRepository.create($http);

        this.appdao = appdao;
        this.userdao = userdao;

        this.$q = $q;
    },

    getAppsByAppId: function (appId) {
        var appdao = this.appdao;
        var userdao = this.userdao;

        var appsPromise = appdao.getAppsByAppId(appId);
        var usersPromise = userdao.getUsers();

        return this.$q.all([appsPromise, usersPromise]).then(function (data) {
            // Since the apps and users re-orgnization is time wasty. so wrap it in promise
            var apps = data[0].data;
            var users = data[1].data;
            return new C().parseApps(apps, users);
        });
    },

    getAppsByAppIds: function (appIds) {
        var appdao = this.appdao;
        var userdao = this.userdao;

        var appsPromise = appdao.getAppsByAppIds(appIds);
        var usersPromise = userdao.getUsers();

        return this.$q.all([appsPromise, usersPromise]).then(function (data) {
            // Since the apps and users re-orgnization is time wasty. so wrap it in promise
            var apps = data[0].data;
            var users = data[1].data;
            return new C().parseApps(apps, users);
        });
    },

    getAllApps: function () {
        var appdao = this.appdao;
        var userdao = this.userdao;

        var appsPromise = appdao.getApps();
        var usersPromise = userdao.getUsers();

        return this.$q.all([appsPromise, usersPromise]).then(function (data) {
            // Since the apps and users re-orgnization is time wasty. so wrap it in promise
            var apps = data[0].data;
            var users = data[1].data;
            return new C().parseApps(apps, users);
        });
    },
    getAllAppsMeta: function () {
        // return the promise
        return this.appdao.getAppsMeta();
    }
});