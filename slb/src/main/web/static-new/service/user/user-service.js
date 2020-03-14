var UsersService = Class.extend({
    init: function ($http) {
        var userdao = UserRepository.create($http);
        this.userdao = userdao;
    },

    getLoginUser: function () {
        var userdao = this.userdao;
        var userPromise = userdao.current();

        return userPromise.then(function (data) {
            var user = data.data;
            return user;
        });
    },
    getUserAuthResources: function () {
        var userdao = this.userdao;
        var resourcePromise = userdao.resources();
        return resourcePromise;
    }
});