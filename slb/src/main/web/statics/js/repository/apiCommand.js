/**
 * Created by ygshen on 2017/5/16.
 */
function ApiCommand(execute){
    this.execute = execute;
};

function RestCommand(){
    var fn = function (http,request,callback) {
        return http(request).success(
            function (response, code) {
                if(callback) callback(response,code);
            }
        );
    };
    return new ApiCommand(fn);
};

function MongoCommand(){
    var fn = function(collection,request, callback, query){
        return collection.request(query,callback(err,result));
    };
    return new ApiCommand(fn);
};

function ApiCommandExecuter(connection, request){
    this.connection = connection;
    this.request = request;
}
ApiCommandExecuter.prototype.execute= function (command,callback) {
    var connection = this.connection;
    var request = this.request;
    return command.execute(connection,request,callback);
};