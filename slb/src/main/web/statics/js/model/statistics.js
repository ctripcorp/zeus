/**
 * Created by ygshen on 2017/6/27.
 */
// statistics .net to java progress charts

var netjavaProgressChartClass=function(bu, legend, url) {
    this.category = bu;
    this.legend =legend;
    this.url = url;
};
netjavaProgressChartClass.prototype.navigate= function (env) {
    if(this.legend){
        window.location.href=this.url+'#?env='+env+'&buName='+this.category+'&migrationStatus='+this.legend;
    }else{
        window.location.href=this.url+'#?env='+env+'&buName='+this.category;
    }
};

netjavaDeactivateChartClass=function(last, legend, url) {
    this.category = last;
    this.legend =legend;
    this.url = url;
};
netjavaDeactivateChartClass.prototype.navigate= function (env) {
    window.location.href=this.url+'#?env='+env+'&last='+this.category;
};

netjavaCompleteChartClass=function(last, legend, url) {
    this.category = last;
    this.legend =legend;
    this.url = url;
};
netjavaCompleteChartClass.prototype.navigate= function (env) {
    window.location.href=this.url+'#?env='+env+'&migrationDoneWeek='+this.category;
};


var netjavaLangugateChartClass = function (bu, legend, url) {
    this.category = bu;
    this.legend =legend;
    this.url = url;
};
netjavaLangugateChartClass.prototype.navigate= function (env) {
    window.location.href=this.url+'#?env='+env+'&buName='+this.category+'&language='+this.legend;
};

var netjavaLangugaHistoryChartClass = function (bu, legend, url) {
    this.category = bu;
    this.legend =legend;
    this.url = url;
};
netjavaLangugaHistoryChartClass.prototype.navigate= function (env) {
    window.location.href=this.url+'#?env='+env+'&groupLanguages='+this.legend;
};

function getChartClassData(type, series,legend, prefix){
    var obj;
    switch (type){
        case 'progress':
            obj = new netjavaProgressChartClass(series,legend, prefix);
            break;
        case 'deactivate':
            obj = new netjavaDeactivateChartClass(series,legend,prefix);
            break;
        case 'complete':
            obj = new netjavaCompleteChartClass(series,legend,prefix);
            break;
        case 'language':
            obj = new netjavaLangugateChartClass(series,legend,prefix);
            break;
        case 'languagehistory':
            obj = new netjavaLangugaHistoryChartClass(series,legend,prefix);
            break;
        default : break;
    }

    return obj;
}



