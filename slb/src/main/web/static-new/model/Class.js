function Class() {}

Class.extend = function extend(props) {

    var prototype = new this();
    var _super = this.prototype;

    for (var name in props) {

        if (typeof props[name] == "function"
            && typeof _super[name] == "function") {

            prototype[name] = (function (super_fn, fn) {
                return function () {
                    var tmp = this.callSuper;

                    this.callSuper = super_fn;

                    var ret = fn.apply(this, arguments);

                    this.callSuper = tmp;

                    if (!this.callSuper) {
                        delete this.callSuper;
                    }
                    return ret;
                }
            })(_super[name], props[name])
        } else {
            prototype[name] = props[name];
        }
    }

    function Class() {}

    Class.prototype = prototype;
    Class.prototype.constructor = Class;

    Class.extend =  extend;
    Class.create = Class.prototype.create = function () {

        var instance = new this();

        if (instance.init) {
            instance.init.apply(instance, arguments);
        }

        return instance;
    }

    return Class;
}



function SlbException(msg){
    this.message = msg;
};