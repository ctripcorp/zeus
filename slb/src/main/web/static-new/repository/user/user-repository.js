var UserRepository = Repository.extend(
    {
        init: function ($http, command) {
            command = command || new RestCommand();
            this.callSuper($http, command);
            this.base = G.baseUrl;
        },
        getUsers: function () {
            var path = this.base + '/api/auth/users';
            return this.get(path);
        },
        current: function () {
            var path = '/api/auth/current/user';
            return this.get(path);
        },
        metas: function () {
            var path = this.base + '/api/meta/users';
            var params = {
                q: '',
                timestamp: new Date().getTime()
            };
            this.get(path, params);
        },
        detail: function (userName) {
            var path = '/api/auth/user';
            var params = {
                userName: userName
            };
            return this.get(path, params);
        },
        resources: function () {
            var path = '/api/auth/user/resources';
            return this.get(path, {});
        }
    });