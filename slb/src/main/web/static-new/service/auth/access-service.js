var AccessService = Class.extend({
    init: function ($http, env) {
        var userdao = UserRepository.create($http);
        this.userdao = userdao;

        var accessdao = AccessRepository.create($http);
        this.accessdao = accessdao;

        this.env = env;
    },

    applyRight: function (targets, targetType, operations) {
        // get the current login user
        var promise = this.userdao.current();
        var that = this;
        return promise.then(function (v) {
            if (v && v.data) {
                var userId = v.data.name;
                var accesspromise = that.accessdao.apply(that.env, userId, targets, targetType, operations);

                return accesspromise;
            } else {
                return new Promise(function (resolve, reject) {
                    reject({
                        data: {
                            code: 500
                        }
                    });
                });
            }
        });
    }
});