/**
 * Created by ygshen on 2017/5/16.
 */
function ApiCommand(execute) {
    this.execute = execute;
};

function RestCommand() {
    var fn = function (http, entity, path, param, credential) {
        var request = {};
        if (entity) {
            // Post request
            request = {
                method: 'POST',
                url: path,
                data: entity
            };
        } else {
            request = {
                method: 'GET',
                url: path
            };
        }
        if (param) {
            request.params = param;
            if (param.isPut) {
                request.method = 'PUT';
            }
        }

        if (credential) {
            request.withCredentials = false;
        }

        return http(request);
    };
    return new ApiCommand(fn);
};

function MongoCommand() {
    var fn = function (collection, entity, path, callback, query) {
        return collection.request(query, callback(err, result));
    };
    return new ApiCommand(fn);
};


// GET Command Executor
function GetApiCommandExecuter(connection, path, param, credential) {
    this.connection = connection;
    this.path = path;
    this.param = param;
    this.credential = credential;
}

GetApiCommandExecuter.prototype.execute = function (command) {
    var connection = this.connection;
    var entity = undefined;
    var path = this.path;
    var param = this.param;
    var credential = this.credential;
    return command.execute(connection, entity, path, param, credential);
};

// POST Command Executor
function PostApiCommandExecuter(connection, entity, path, param, credential) {
    this.connection = connection;
    this.entity = entity;
    this.path = path;
    this.param = param;
    this.credential = credential;
}

PostApiCommandExecuter.prototype.execute = function (command) {
    var connection = this.connection;
    var entity = this.entity;
    var path = this.path;
    var param = this.param;
    var credential = this.credential;
    return command.execute(connection, entity, path, param, credential);
};