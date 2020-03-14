/**
 * Created by ygshen on 2017/5/16.
 */
function QueryService() {

};
QueryService.prototype.get = function (datasets, type,orderBy,cols,filter) {
    var commands ={
        'all':  new queryAllCommand(datasets),
        'filter': new queryByCommand(datasets),
        'group': new groupAllCommand(datasets),
        'groupfilter': new groupByCommand(datasets)
    };
    var command = commands[type];
    var executer = new queryExecuter();
    return executer.execute(command, orderBy, cols, filter);
};