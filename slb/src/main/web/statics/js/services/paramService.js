/**
 * Created by ygshen on 2017/5/16.
 */
function ParamService(hashData) {
    this.hashData = hashData;
    this.commands={
        'property': new propertyParamCommand(),
        'normal': new normalParamCommand()
    };
};
ParamService.prototype.getParamString = function (type, pair) {
    var p = this.commands[type];

    var executer = new parameterExecuter();
    return executer.execute(p, pair);
};
ParamService.prototype.extractParam = function (hashKey, slbKey) {
    var p = this.hashData[hashKey];
    var result = {};

    if (!p) return result;

    if (!slbKey) slbKey = hashKey;

    if (!Array.isArray(p)) p = [p];

    result[slbKey] = p;

    return result;
};