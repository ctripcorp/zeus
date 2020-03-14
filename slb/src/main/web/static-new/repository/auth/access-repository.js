var AccessRepository = Repository.extend(
    {
        init: function ($http, command) {
            command = command || new RestCommand();
            this.callSuper($http, command);
            this.base = G.baseUrl;
        },
        apply: function (env, userId, targets, targetType, operations) {
            var url = this.base +
                '/api/auth/apply/mail';

            $.each(targets, function (i, k) {
                var seperator = i == 0 ? '?' : '&';
                url += (seperator + 'targetId=' + k);
            });

            var params = {
                env: env,
                type: targetType,
                userName: userId,
                op: operations.join(',')
            };

            return this.get(url, params);
        }
    });