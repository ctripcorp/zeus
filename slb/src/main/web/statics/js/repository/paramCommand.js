/**
 * Created by ygshen on 2017/5/16.
 */
function parameterCommand(execute){
    this.execute = execute;
};

function normalParamCommand(){
    var fn= function (pair) {
        var array=[];
        for(var p in pair){
            if(typeof (p)!='function'){
                array=pair[p];
            }
        }
        return array.split(',');
    };

    return new parameterCommand(fn);
};

function propertyParamCommand(){
    var fn= function (pair) {
        var str='';
        for(var p in pair){
            if(typeof (p)!='function'){
                var s = _.map(pair[p], function (v) {
                    return p+':'+v;
                });
                str = s.join(',');
            }
        }

        return str;
    };

    return new parameterCommand(fn);
};

function parameterExecuter(){

};

parameterExecuter.prototype.execute= function (command,pair) {
    return command.execute(pair);
};

