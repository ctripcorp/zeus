/**
 * Created by ygshen on 2017/5/11.
 */

// Super class Area
function superClass(id,name,createdTime,version){
    this.id =id;
    this.name = name;
    this['created-time']=createdTime;
    this.version = version;
}
superClass.prototype.serialize= function () {
    var obj={};
    for(var p in this){
        if(typeof(this[p]!=='function'))
            obj[p] = this[p];
    }
    return JSON.stringify(obj);
};
superClass.prototype.deserialize= function (str) {
    var obj={};
    try{
        obj = JSON.parse(str);
    }catch (e){}
    return obj;
};
superClass.prototype.activate= function (acitvateFn) {
    acitvateFn.apply(this);
};
superClass.prototype.deActivate= function () {
};
superClass.prototype.delete= function () {
};
superClass.prototype.update= function (other) {
    for(var p in this){
        if(typeof(this[p]!=='function'))
            this.p = other[p];
    }
    return this;
};

// SLB Server Class Area
function slbServer(ip, host){
    this.ip = ip;
    this['host-name']=host;
};

// VIP Class Area
function vip(ip){
    this.ip = ip;
};


// SLB class Area
function slb(id,name,version,updateTime, slbServers, vips, nginxBin, nginxConf,workCount){
    this['slb-servers']=slbServers;
    this['vips']=vips;
    this['nginx-bin']=nginxBin;
    this['nginx-conf']=nginxConf;
    this['nginx-worker-processes']=workCount;
    var proto = superClass.prototype.constructor;
    proto.apply(this,[id,name,version,updateTime]);
}
slb.prototype.activate = function () {
    var slb_strategy = new slbStrategy();

    var ctx = new context(slb_strategy);
    ctx.activate(this);
};
slb.prototype.deactivate = function () {
    var slb_strategy = new slbStrategy();

    var ctx = new context(slb_strategy);
    ctx.deactivate(this);
};
slb.prototype.delete = function () {
    var slb_strategy = new slbStrategy();

    var ctx = new context(slb_strategy);
    ctx.delete(this);
};
slb.prototype=new superClass();


// SLB Meta data
function slbMetaClass(slbId, qps, mbc,gsc,gc,ac,vsc){
    this['slb-id'] = slbId;
    this.qps = qps;
    this['member-count']=mbc;
    this['group-server-count']=gsc;
    this['group-count']=gc;
    this['app-count']=ac;
    this['vs-count']=vsc;
};
slbMetaClass.prototype=new superClass();

