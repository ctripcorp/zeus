/**
 * Created by ygshen on 2017/5/11.
 */

// strategy super area
function strategy(){

};
strategy.prototype.activate= function (item) {

};
strategy.prototype.deactivate = function (item) {

};
strategy.prototype.delete= function (item) {

};
strategy.prototype.query = function (param) {

};
strategy.prototype.save = function () {

};
strategy.prototype.update = function () {

};



// slb strategy area
function slbStrategy(){

};

slbStrategy.prototype.activate= function (slb) {

};
slbStrategy.prototype.query= function (param) {

};


slbStrategy.prototype = new strategy();



// Context area
function context(strategy){
    this.strategy = strategy;
};
context.prototype.set = function (strategy) {
    this.strategy = strategy;
};
context.prototype.activate = function () {
    this.strategy.activate();
};
context.prototype.deactivate = function () {
    this.strategy.deactivate();
};
context.prototype.delete = function () {
    this.strategy.delete();
};
