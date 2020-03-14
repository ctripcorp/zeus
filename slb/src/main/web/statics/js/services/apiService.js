/**
 * Created by ygshen on 2017/5/11.
 */
function ApiService(connection, serviceType){
    this.connection = connection;

    // Commands type support REST and MongoDB
    var commands ={
        'rest': new RestCommand(),
        'mongo': new MongoCommand()
    };

    // Service type passed
    this.command = commands[serviceType] || commands['rest'];
};

ApiService.prototype.Request = function (request, callback) {
    var command = this.command;
    var connection = this.connection;

    var executer = new ApiCommandExecuter(connection,request);
    return executer.execute(command,callback);
};







