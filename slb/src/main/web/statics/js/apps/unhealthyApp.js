/**
 * Created by ygshen on 2017/7/5.
 */

function UnhealthyApp(hashData, $http, $q) {
    this.hashData = hashData;
    this.$q = $q;

    this.paramService = new ParamService(hashData);
    this.requestService = new ApiService($http);
    this.queryService = new QueryService();
};
UnhealthyApp.prototype = new App();
UnhealthyApp.prototype.getQueries = function (params) {
    var diagnoseRequest={
        method: 'GET',
        url: G.baseUrl+'/api/diagnose/group/query?'+params.join('&')
    };

    return {
        'diagnose': diagnoseRequest
    };
};
UnhealthyApp.prototype.request = function (query) {
    var out = getRequestData();
    return out(this, query, this.$q);
};
var getRequestData = function () {
    var result={};
    var request;

    var f = function (self,query, $q) {
        var diagnoseRequest = query['diagnose'];

        var array = [
            self.sendRequest(diagnoseRequest, function (response, code) {
                result['diagnose'] = self.getGroupData(response,'target-id');
            })
        ];

         request = $q.all(array);

        return {
            result: result,
            request:request
        };
    };
    return f;
};

UnhealthyApp.prototype.getAllUnhealthyByCount= function (diagnose, countBy, issue, title) {
    var diagnose_values = _.flatten(_.values(diagnose));
    diagnose_values = _.filter(diagnose_values, function (v) {
        var b=true;
        var a = v['target-type']=='group'
        if(issue){
            b= v['diagnose-result'].indexOf(issue)!=-1;
        }
        return a && b;
    });

    var indexByType = _.countBy(diagnose_values, function (v) {
        return v[countBy];
    });

    var member=indexByType[apiDiagnoseType.member];
    var healthy=indexByType[apiDiagnoseType.healthy];
    var change=indexByType[apiDiagnoseType.change];

    var sortedIndexType={};
    if(member){
        sortedIndexType[apiDiagnoseType.member] = member;
    }
    if(healthy){
        sortedIndexType[apiDiagnoseType.healthy] = healthy;
    }
    if(change){
        sortedIndexType[apiDiagnoseType.change] = change;
    }

    if(sortedIndexType && _.keys(sortedIndexType).length>0) indexByType = sortedIndexType;

    var data= [0];

    var keys = _.keys(indexByType);

    keys = _.map(keys, function (s) {
        s = s.replace('超过','>');
        return s;
    });

    if(_.values(indexByType).length>0){
        data = _.values(indexByType);
    }
    return {
        chart: {
            type: 'column'
        },

        xAxis: {
            categories: keys,
            crosshair: true
        },
        yAxis: {
            min: 0,
            title: {
                text: '个数'
            }
        },
        tooltip: {
            headerFormat: '<span style="font-size:10px">{point.key}</span><table>',
            pointFormat: '<tr><td style="color:{series.color};padding:0">{series.name}: </td>' +
            '<td style="padding:0"><b>{point.y:.0f}个</b></td></tr>',
            footerFormat: '</table>',
            shared: true,
            useHTML: true
        },
        plotOptions: {
            series: {
                cursor: 'pointer',
                events: {
                    click: function (event) {

                    }
                },
                dataLabels: {
                    enabled: true,
                    style: {
                        color: 'black'
                    }
                }
            }
        },
        legend:{
            enabled: false
        },
        series: [
            {
                name: '问题',
                data:data,
                maxPointWidth: 50,
                type: 'column'
            }
        ],
        title: {
            text: title
        },
        credits: {
            enabled: false
        }
    };
};

UnhealthyApp.prototype.getIssueData= function (diagnose,issue) {
    var diagnose_values = _.flatten(_.values(diagnose));
    diagnose_values = _.filter(diagnose_values, function (v) {
        var b=true;
        var a = v['target-type']=='group'
        if(issue){
            b= v['diagnose-result'].indexOf(issue)!=-1;
        }
        return a && b;
    });

    return _.map(diagnose_values, function (v) {
        var groupId = v['target-id'];
        var app = v['description']['应用名称(App)'];
        var idc = v['description']['IDC/Env'];

        var reg = /(.*)\((.*)\)/;
        var operatorStr=v['description']['操作用户(时间)'];

        var operator='-';
        var operate_time='-';
        if(operatorStr){
            var regResult = reg.exec(operatorStr);
            if(regResult && regResult.length>2){
                operator= reg.exec(operatorStr)[1];
                operate_time= reg.exec(operatorStr)[2];
            }
        }
        if(operate_time=='-') operate_time = v['appear-time'] || '-';


        var diagnoseStr = v['diagnose-result'];
        var s =diagnoseStr.split(':');
        var diagnose_result=s[1];

        var pull_out_reason='-';
        var pull_out_ips='-';

        if(v['description']["拉出原因"]){
            pull_out_reason=v['description']["拉出原因"];

        }
        if(v['description']["IP"]){
            pull_out_ips=v['description']["IP"];
        }

        var solution='-';
        solution = v['solution'];

        return {
            app: app,
            idc: idc.split('/')[0],
            group: groupId,
            operator: operator,
            optime: operate_time,
            operation: diagnose_result,
            pullreason:pull_out_reason,
            pullips: pull_out_ips,
            solution:solution
        }
    });

};

UnhealthyApp.prototype.initTable= function (memberData, changeData, healthyData,paging) {
    if(paging==undefined) paging=false;

    $("#membertable").bootstrapTable({
        toolbar: '.member-toolbar',
        columns: [[
            {
                field: 'app',
                title: 'App',
                align: 'left',
                resetWidth: true,
                valign: 'middle',
                sortable: true,
                width: '250px',
                formatter: function (value, row, index) {
                    var  str='<a href="/portal/group#?env='+ G.env+'&groupId='+row.group+'">'+value+'</a>';
                    return str;
                }
            },
            {
                field: 'idc',
                title: 'IDC',
                align: 'left',
                width: '140px',
                valign: 'middle',
                sortable: true
            },
            {
                field: 'pullips',
                title: '拉出机器',
                align: 'left',
                resetWidth: true,
                valign: 'middle',
                sortable: true
            },
            {
                field: 'group',
                title: 'Group',
                align: 'left',
                resetWidth: true,
                valign: 'middle',
                sortable: true,
                visible:false,
                formatter: function (value, row, index) {
                    var  str='<a href="/portal/group#?env='+ G.env+'&groupId='+row.group+'">'+value+'</a>';
                    return str;
                }
            },
            {
                field: 'pullreason',
                title: '拉出原因',
                align: 'left',
                resetWidth: true,
                valign: 'middle',
                sortable: true
            },
            {
                field: 'operator',
                title: '操作者',
                align: 'left',
                valign: 'middle',
                sortable: true,
                formatter: function (value, row, index) {
                    var  str='<a href="/portal/group#?env='+ G.env+'&groupId='+value+'">'+value+'</a>';
                    return str;
                }
            },

            {
                field: 'optime',
                title: '操作时间',
                align: 'left',
                width: '225px',
                valign: 'middle',
                sortable: true

            },
            {
                field: 'operation',
                title: '服务器拉出天数',
                align: 'left',
                valign: 'middle',
                width: '140px',
                sortable: true,
                formatter: function (value, row, index) {
                    if(value.indexOf('超过')!=-1){
                        value = value.replace('超过','>');

                    }
                    var str = '<span class="status-red">' + value + '</span>';
                    return str;
                }
            }
        ], []],
        search: true,
        showRefresh: true,
        showColumns: true,
        sortName: 'operation-time',
        sortOrder: 'desc',
        data: memberData,
        classes: "table-bordered  table-hover table-striped table",
        minimumCountColumns: 2,
        pagination: paging,
        idField: 'id',
        detailView: true,
        detailFormatter: function (arg1, arg2) {
            var titleStr='<h1>当前问题解决方案</h1>';
            var dataArray=arg2.solution;
            var str='<ul>';
            $.each(dataArray, function (i, item) {
                var current = item;
                var title = current[0];

                str+='<li><h5 class="status-red">'+title+'</h5>';

                str+='<ul>';
                for(var j=1;j<current.length;j++){
                    str+='<li>'+current[j]+'</li>';
                }
                str+='</ul>';
                str+='</li>';
            });
            str+='</ul>';
            return titleStr+str;
        },
        pageSize: 20,
        resizable: true,
        resizeMode: 'overflow',
        sidePagination: 'client',
        pageList: [20, 40, 80, 200],
        responseHandler: "responseHandler",
        formatLoadingMessage: function () {
            return  "<img class='loading-img' src='/static/img/loading_100.gif' /> 正在加载Member拉出应用";
        },
        formatNoMatches: function () {
            return '<i class="fa status-alert fa-exclamation-triangle" aria-hidden="true"></i> 没有找到任何Member拉出应用';
        }
    });
    $("#changetable").bootstrapTable({
        toolbar: '.change-toolbar',
        columns: [[
            {
                field: 'app',
                title: 'App',
                align: 'left',
                resetWidth: true,
                valign: 'middle',
                width: '250px',
                sortable: true,
                formatter: function (value, row, index) {
                    var  str='<a href="/portal/group#?env='+ G.env+'&groupId='+row.group+'">'+value+'</a>';
                    return str;
                }
            },
            {
                field: 'idc',
                title: 'IDC',
                align: 'left',
                valign: 'middle',
                width: '140px',
                sortable: true
            },
            {
                field: 'group',
                title: 'Group',
                align: 'left',
                resetWidth: true,
                valign: 'middle',
                sortable: true,
                visible:false,
                formatter: function (value, row, index) {
                    var  str='<a href="/portal/group#?env='+ G.env+'&groupId='+row.group+'">'+value+'</a>';
                    return str;
                }
            },

            {
                field: 'operator',
                title: '操作者',
                align: 'left',
                valign: 'middle',
                sortable: true,
                formatter: function (value, row, index) {
                    var  str='<a href="/portal/group#?env='+ G.env+'&groupId='+value+'">'+value+'</a>';
                    return str;
                }
            },

            {
                field: 'optime',
                title: '操作时间',
                align: 'left',
                valign: 'middle',
                sortable: true
            },
            {
                field: 'operation',
                title: '应用变更天数',
                align: 'left',
                valign: 'middle',
                width: '140px',
                sortable: true,
                formatter: function (value, row, index) {
                    if(value.indexOf('超过')!=-1){
                        value = value.replace('超过','>');

                    }


                    var str = '<span class="status-red">' + value + '</span>';
                    return str;
                }
            }
        ], []],
        search: true,
        showRefresh: true,
        showColumns: true,
        sortName: 'operation-time',
        sortOrder: 'desc',
        data: changeData,
        classes: "table-bordered  table-hover table-striped table",
        minimumCountColumns: 2,
        pagination: paging,
        idField: 'id',
        pageSize: 20,
        resizable: true,
        detailView: true,
        detailFormatter: function (arg1, arg2) {
            var dataArray=arg2.solution;
            var titleStr='<h1>当前问题解决方案</h1>';
            var str='<ul>';
            $.each(dataArray, function (i, item) {
                var current = item;
                var title = current[0];

                str+='<li><h5 class="status-red">'+title+'</h5>';

                str+='<ul>';
                for(var j=1;j<current.length;j++){
                    str+='<li>'+current[j]+'</li>';
                }
                str+='</ul>';
                str+='</li>';
            });
            str+='</ul>';
            return titleStr+str;
        },
        resizeMode: 'overflow',
        sidePagination: 'client',
        pageList: [20, 40, 80, 200],
        responseHandler: "responseHandler",
        formatLoadingMessage: function () {
            return  "<img class='loading-img' src='/static/img/loading_100.gif' /> 正在加载Member拉出应用";
        },
        formatNoMatches: function () {
            return '<i class="fa status-alert fa-exclamation-triangle" aria-hidden="true"></i> 没有找到任何Member拉出应用';
        }
    });
    $("#healthytable").bootstrapTable({
        toolbar: '.healthy-toolbar',
        columns: [[
            {
                field: 'app',
                title: 'App',
                align: 'left',
                resetWidth: true,
                width: '250px',
                valign: 'middle',
                sortable: true,
                formatter: function (value, row, index) {
                    var  str='<a href="/portal/group#?env='+ G.env+'&groupId='+row.group+'">'+value+'</a>';
                    return str;
                }
            },
            {
                field: 'idc',
                title: 'IDC',
                align: 'left',
                width: '140px',
                valign: 'middle',
                sortable: true
            },
            {
                field: 'group',
                title: 'Group',
                align: 'left',
                resetWidth: true,
                valign: 'middle',
                sortable: true,
                visible:false,
                formatter: function (value, row, index) {
                    var  str='<a href="/portal/group#?env='+ G.env+'&groupId='+row.group+'">'+value+'</a>';
                    return str;
                }
            },

            {
                field: 'optime',
                title: '操作时间',
                align: 'left',
                valign: 'middle',
                sortable: true
            },
            {
                field: 'operation',
                title: '失败频率',
                align: 'left',
                valign: 'middle',
                width: '140px',
                sortable: true,
                formatter: function (value, row, index) {
                    if(value.indexOf('超过')!=-1){
                        value = value.replace('超过','>');

                    }
                    var s = value.split('>');
                    s=s[1]+'/'+s[0];
                    var str = '<span class="status-red">' + s + '</span>';
                    return str;
                }
            }
        ], []],
        search: true,
        showRefresh: true,
        showColumns: true,
        sortName: 'operation-time',
        sortOrder: 'desc',
        data: healthyData,
        classes: "table-bordered  table-hover table-striped table",
        minimumCountColumns: 2,
        pagination: paging,
        idField: 'id',
        pageSize: 20,
        resizable: true,
        detailView: true,
        detailFormatter: function (arg1, arg2) {
            var dataArray=arg2.solution;
            var titleStr='<h1>当前问题解决方案</h1>';
            var str='<ul>';
            $.each(dataArray, function (i, item) {
                var current = item;
                var title = current[0];

                str+='<li><h5 class="status-red">'+title+'</h5>';

                str+='<ul>';
                for(var j=1;j<current.length;j++){
                    str+='<li>'+current[j]+'</li>';
                }
                str+='</ul>';
                str+='</li>';
            });
            str+='</ul>';
            return titleStr+str;
        },
        resizeMode: 'overflow',
        sidePagination: 'client',
        pageList: [20, 40, 80, 200],
        responseHandler: "responseHandler",
        formatLoadingMessage: function () {
            return  "<img class='loading-img' src='/static/img/loading_100.gif' /> 正在加载Member拉出应用";
        },
        formatNoMatches: function () {
            return '<i class="fa status-alert fa-exclamation-triangle" aria-hidden="true"></i> 没有找到任何Member拉出应用';
        }
    });
    $('#membertable').bootstrapTable("load", memberData);
    $('#changetable').bootstrapTable("load", changeData);
    $('#healthytable').bootstrapTable("load", healthyData);

};
UnhealthyApp.prototype.getLogInfo= function (query) {
    var out = getLoginData();
    return out(this,query,this.$q);
};
var getLoginData= function () {
    var result={};
    var request;
    var fn = function (self, query, $q) {
        var array=[
            self.sendRequest(query['auth'], function (response, code) {
                result['user'] = self.getAllData([response],'name');
            })
        ];

        request = $q.all(array);
        return {
            result: request,
            request: request
        }
    };
    return fn;
};

var unhealthyText={
    all: '问题诊断汇总',
    member: '服务器长期拉出',
    healthy: '健康检测不稳定',
    change: '长期变更未生效',
    gc: 'GC不健康'
};
var apiUnhealthyText={
    member: '应用服务器拉出',
    healthy: '健康监测频繁拉入拉出',
    change: '应用配置变更后未激活',
    gc: 'GC不健康'
};

var apiDiagnoseType={
    member: '应用Member长期拉出',
    change: '应用配置变更后未激活',
    healthy: '频繁健康检测失败诊断'
}