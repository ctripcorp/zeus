var Repository = Class.extend({
    init: function (connection, command, credential) {
        // TODO: paramize the commands detection
        this.connection = connection;
        this.command = command;
        this.credential = credential || false;
    },
    get: function (path, param) {
        var command = this.command;
        var connection = this.connection;
        var credential = this.credential;

        var executer = new GetApiCommandExecuter(connection, path, param, credential);
        return executer.execute(command);
    },
    post: function (entity, path, param) {
        var command = this.command;
        var connection = this.connection;
        var credential = this.credential;

        var executer = new PostApiCommandExecuter(connection, entity, path, param, credential);
        return executer.execute(command);
    },
    put: function (entity, path, param) {
        param = param || {};
        param.isPut = true;

        return this.post(entity, path, param);
    }
});