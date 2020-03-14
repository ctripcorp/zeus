/**
 * Created by ygshen on 2017/5/16.
 */
function queryCommand(execute){
    this.execute = execute;
};

function queryAllCommand(datasets){
    var datasets = datasets;

    var fn = function (filter,orderBy,cols) {
        if(datasets && _.keys(datasets).length>0){
            return _.indexBy(datasets, orderBy);
        }
        return {};
    };
    return new queryCommand(fn);
};
function queryByCommand(datasets){
    var datasets = datasets;
    var fn = function (filter,orderBy,cols) {
        if(datasets && _.keys(datasets).length>0){
            var c = _.indexBy(datasets, orderBy);
            return filter.apply(c,[]);
        }
        return {};
    };
    return new queryCommand(fn);
};

function groupAllCommand(datasets){
    var datasets=datasets;
    var fn= function (filter,groupById,cols) {
        if(datasets && datasets.length>0){
            return _.groupBy(datasets, function (v) {
                return v[groupById];
            });
        }else{
            return {};
        }
    };
    return new queryCommand(fn);
};
function groupByCommand(datasets){
    var datasets = datasets;
    var fn = function (filter, groupById, cols) {
        if(datasets && datasets.length>0){
            var groupBy = _.groupBy(datasets, function (v) {
                return v[groupById];
            });

            return filter.apply(groupBy,[]);
        }else return {};
    };
    return new queryCommand(fn);
};


function queryExecuter(){

};
queryExecuter.prototype.execute= function (command, orderBy, cols, filter) {
    return command.execute(filter, orderBy, cols);
};