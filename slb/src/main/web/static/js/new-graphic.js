/**
 * Created by ygshen on 2017/6/28.
 */
var TrafficPolicyGraphics = function () {
    return {
        draw: function (container, nodes, env, policyId, diagramresource) {
            var vsColor = '#337ab7';
            var groupColor = '#03a9f4';
            var policyColor = '#ebccd1';
            var lineColor = '#aaa';
            var textColor = '#b5192c';

            var vsTextColor ='white';
            var policyTextColor ='#a94442';
            var groupTextColor ='white';

            for (var i = 0; i < nodes.length; i++) {
                var vsNodes = nodes[i].vsNodes;
                var groupNodes = nodes[i].groupNodes;

                var vsCount = vsNodes.length;
                var groupsCount = groupNodes.length;

                var yStart = 40;
                // 4 rows
                var nodeHeight = 120;
                // 50 characters
                var nodeWidth = 250;


                var policyNodeWidth = 92;

                var n = Math.max(vsCount, groupsCount);

                // how many vses are there
                var mapHeight = 400;
                if(n>3 && n<7){
                    mapHeight = 1000;
                }else if(n>7 || n==7){
                    mapHeight = 1200;
                }
                var e = Math.floor((mapHeight - yStart) / n);


                var width = $('#' + container).width();
                width = width>0?(width - 60):1000;
                var vsColWidth = Math.floor(width / 10) * 4;
                var policyColWidth = Math.floor(width / 10) * 2;
                var groupColWidth = Math.floor(width / 10) * 4;

                var vsMax = vsCount * e + yStart;
                var groupMax = groupsCount * e + yStart;

                var maxy = Math.max(vsMax, groupMax);

                var middleStart = {
                    x: 0,
                    y: 0
                };
                var middleEnd = {
                    x: 0,
                    y: 0
                };

                // labels
                var vsLable = {
                    x: Math.floor(vsColWidth / 2),
                    y: yStart
                };
                var policyLable = {
                    x: Math.floor(policyColWidth / 2) + vsColWidth,
                    y: yStart
                };

                var groupsLabel = {
                    x: Math.floor(groupColWidth / 2) + vsColWidth + policyColWidth,
                    y: yStart
                };


                // Seperators
                var start_vsPolicySeperator = {
                    x: vsColWidth,
                    y: yStart
                };
                var end_vsPolicySeperator = {
                    x: vsColWidth,
                    y: maxy
                };

                var start_policyGroupSeperator = {
                    x: vsColWidth + policyColWidth,
                    y: yStart
                };
                var end_policyGroupSeperator = {
                    x: vsColWidth + policyColWidth,
                    y: maxy
                };


                // Nodes
                var vsStart = {
                    x: 10
                };
                // 60 is padding to the right
                var groupsStart = {
                    x: vsColWidth + policyColWidth + groupColWidth - nodeWidth + 20
                };

                var t = Highcharts.chart(container, {
                    chart: {
                        height: maxy,
                        backgroundColor: 'white',
                        events: {
                            load: function () {
                                // Draw the flow chart
                                var ren = this.renderer;

                                // Seperator
                                ren.path(['M', start_vsPolicySeperator.x, start_vsPolicySeperator.y, 'L', end_vsPolicySeperator.x, end_vsPolicySeperator.y])
                                    .attr({
                                        'stroke-width': 2,
                                        stroke: 'silver',
                                        dashstyle: 'dash'
                                    })
                                    .add();

                                ren.path(['M', start_policyGroupSeperator.x, start_policyGroupSeperator.y, 'L', end_policyGroupSeperator.x, end_policyGroupSeperator.y])
                                    .attr({
                                        'stroke-width': 2,
                                        stroke: 'silver',
                                        dashstyle: 'dash'
                                    })
                                    .add();

                                // Headers
                                ren.label(diagramresource['客户端'], vsLable.x, vsLable.y)
                                    .css({
                                        fontWeight: 'bold'
                                    })
                                    .add();


                                var padding = 30;
                                var currentY = 0;
                                var verticalStart = {
                                    x: 0,
                                    y: 0
                                };
                                var verticalEnd = {
                                    x: 0,
                                    y: 0
                                };

                                var eachHeight = padding + nodeHeight;
                                var topPadding = Math.floor((maxy - yStart - vsNodes.length * eachHeight) / 2);

                                $.each(vsNodes, function (i, item) {
                                    currentY = topPadding + yStart + i * eachHeight;
                                    ren.label(item.name, vsStart.x, currentY)
                                        .attr({
                                            fill: vsColor,
                                            stroke: 'white',
                                            'stroke-width': 2,
                                            padding: 5,
                                            width: nodeWidth,
                                            height: nodeHeight,
                                            r: 5
                                        })
                                        .css({
                                            color: vsTextColor
                                        })
                                        .add()
                                        .shadow(true);


                                    // Draw line from client to VS
                                    var myStartPoint = {
                                        x: vsStart.x + nodeWidth + 10,
                                        y: currentY + Math.floor(nodeHeight / 2)
                                    };
                                    var myEndPoint = {
                                        x: start_vsPolicySeperator.x,
                                        y: myStartPoint.y
                                    };


                                    ren.path(['M', myStartPoint.x, myStartPoint.y, 'L', myEndPoint.x, myEndPoint.y])
                                        .attr({
                                            'stroke-width': 2,
                                            stroke: lineColor
                                        })
                                        .add();
                                    ren.label(diagramresource['请求'], myStartPoint.x + 10, myStartPoint.y - 20)
                                        .css({
                                            fontSize: '10px',
                                            color: textColor
                                        })
                                        .add();
                                    if (i == 0) {
                                        verticalStart = myEndPoint;
                                    }
                                    verticalEnd = myEndPoint;
                                });

                                middleStart = {
                                    x: verticalStart.x,
                                    y: Math.floor((verticalEnd.y - verticalStart.y) / 2) + verticalStart.y
                                };
                                middleEnd = {
                                    x: middleStart.x + 30,
                                    y: middleStart.y
                                };

                                if (verticalStart.y != verticalEnd.y) {
                                    ren.path(['M', verticalStart.x, verticalStart.y, 'L', verticalEnd.x, verticalEnd.y])
                                        .attr({
                                            'stroke-width': 2,
                                            stroke: lineColor
                                        })
                                        .add();
                                }

                                ren.path(['M', middleStart.x, middleStart.y, 'L', middleEnd.x, middleEnd.y, 'L', middleEnd.x - 5, middleEnd.y + 5, 'M', middleEnd.x, middleEnd.y, 'L', middleEnd.x - 5, middleEnd.y - 5])
                                    .attr({
                                        'stroke-width': 2,
                                        stroke: lineColor
                                    })
                                    .add();

                                ren.label(diagramresource['策略'], policyLable.x, policyLable.y)
                                    .css({
                                        fontWeight: 'bold'
                                    })
                                    .add();


                                ren.label('<a class="status-red" target="_blank" href="/portal/policy#?env='+env+'&policyId='+policyId+'">'+diagramresource['策略']+'</a>', middleEnd.x, middleEnd.y - Math.floor(nodeHeight / 2),'rect',0,0,true)
                                    .attr({
                                        fill: policyColor,
                                        stroke: 'white',
                                        'stroke-width': 2,
                                        'padding': 35 ,
                                        r: 5
                                    })
                                    .css({
                                        color: policyTextColor
                                    })
                                    .add()
                                    .shadow(true);

                                var nextStart = {
                                    x: middleEnd.x + policyNodeWidth,
                                    y: middleEnd.y
                                };

                                ren.path(['M', nextStart.x, nextStart.y, 'L', start_policyGroupSeperator.x, nextStart.y])
                                    .attr({
                                        'stroke-width': 2,
                                        stroke: lineColor
                                    })
                                    .add();


                                ren.label(diagramresource['应用'], groupsLabel.x, groupsLabel.y)
                                    .css({
                                        fontWeight: 'bold'
                                    })
                                    .add();
                                //groupNodes = groupNodes.slice(0,max-1);
                                topPadding = Math.floor((maxy - yStart - groupNodes.length * eachHeight) / 2);
                                $.each(groupNodes, function (i, v) {
                                    var nextEnd = {
                                        x: groupsStart.x,
                                        y: topPadding + yStart + i * eachHeight
                                    };

                                    var middle = nextEnd.y + Math.floor(nodeHeight / 2);

                                    ren.path(['M', start_policyGroupSeperator.x, middle, 'L', nextEnd.x, middle, 'L', nextEnd.x - 5, middle + 5, 'M', nextEnd.x, middle, 'L', nextEnd.x - 5, middle - 5])
                                        .attr({
                                            'stroke-width': 2,
                                            stroke: lineColor
                                        })
                                        .add();


                                    ren.label(v.name, nextEnd.x, nextEnd.y,'rect',10,0,true)
                                        .attr({
                                            fill: groupColor,
                                            stroke: 'white',
                                            'stroke-width': 2,
                                            padding: 15,
                                            width: nodeWidth,
                                            r: 5
                                        })
                                        .css({
                                            color: groupTextColor
                                        })
                                        .add()
                                        .shadow(true);

                                    ren.label(v.icon, nextEnd.x+nodeWidth-5, nextEnd.y+5,'circle', 0,0,true)
                                        .add()
                                        .shadow(true);

                                    ren.label(v.weight, start_policyGroupSeperator.x, middle - 25)
                                        .css({
                                            fontWeight: 'bold',
                                            fontSize: '20px',
                                            color: textColor
                                        })
                                        .add();

                                    if (i == 0) {
                                        verticalStart = {
                                            x: start_policyGroupSeperator.x,
                                            y: middle
                                        }
                                    }
                                    verticalEnd = {
                                        x: start_policyGroupSeperator.x,
                                        y: middle
                                    };

                                });
                                ren.path(['M', verticalStart.x, verticalStart.y, 'L', verticalEnd.x, verticalEnd.y])
                                    .attr({
                                        'stroke-width': 2,
                                        stroke: lineColor
                                    })
                                    .add();
                            }
                        }
                    },
                    title: {
                        text: '',
                        style: {
                            color: 'black'
                        }
                    },
                    credits: {
                        enabled: false
                    }
                });
                t.container.style.fontSize = '4em';
            }
        },
        writePolicy: function (policyId, data, container,env,diagramresource) {

            var maxCharacter=40;

            var datamap = _.groupBy(data, function (item) {
                return item.policy.id;
            });

            var data = _.map(data, function (item) {
                return {
                    policyId: item.policy.id,
                    vses: datamap[item.policy.id]
                };
            });
            data = _.uniq(data, function (item, k, policyId) {
                return item.policyId;
            });


            data = _.filter(data, function (v) {
                return v.policyId == policyId ||v.policyId=='-';
            });

            var nodes = _.map(data, function (item) {
                var vses = item.vses;
                var top = vses[0];

                vses = _.map(vses, function (vs) {
                    vs = vs.vs;
                    var domains = vs.domains;
                    domains = _.map(domains, function (a) {
                        if (a.length > maxCharacter) {
                            a = a.substring(0, maxCharacter) + '...';
                        }

                        return a;
                    });
                    var idc = 'IDC:' + vs.idc;
                    var soaops = vs.soaops || [];
                    var slbops = vs.slbops || [];

                    if (soaops.length == 0 && slbops.length == 0) {
                        domains = domains.slice(0, 3);
                    } else {
                        if (soaops.length != 0) {
                            soaops = soaops.slice(0, 1);
                            soaops.push('...');
                        } else {
                            slbops = slbops.slice(0, 1);
                            slbops.push('...');
                        }

                        if (domains.length > 1) {
                            domains = domains.slice(0, 1);
                            domains.push('...');
                        }
                    }

                    var domainText = domains.join('<br />');
                    domainText += '<br />';

                    var soaText = soaops.join('<br />');
                    var slbText = slbops.join('<br />');

                    if (soaText) {
                        soaText = diagramresource['SOA操作:'] + soaText + '<br />';
                    }
                    if (slbText) {
                        slbText = diagramresource['SLB子目录:'] + slbText + '<br />';
                    }

                    var name = domainText + soaText + slbText + idc + '<br />';

                    return {
                        name: name
                    };
                });

                var groups = _.map(top.groups, function (group) {

                    var id = group.id;

                    var appId = group['app-id'] || 'UNKNOWN';
                    var appName = group['app-name'] || 'UNKNOWN';
                    var re = appName + '(' + appId + ')';
                    if (re.length > 50) {
                        re = re.substring(0, 45) + '...';
                    } else if (re.length < 50) {
                        var extra = new Array(50 - re.length).join(' ');
                        re += extra.toString();
                    }

                    var weight = group.weight;
                    var language = group.language?group.language.toUpperCase():diagramresource['未知语言'];
                    var icon='<i class="fa fa-question status-red" style="font-size: 20px"></i><br /><b class="status-red">'+diagramresource['未知']+'</b>';
                    if(language=='JAVA'){
                        icon='<i class="fa fa-coffee status-red" style="font-size: 20px"></i><br /><b class="status-red">Java</b>';
                    }
                    if(language=='.NET'){
                        icon='<i class="fa fa-windows status-white" style="font-size: 20px;"></i><br /><b class="status-white">Net</b>';
                    }
                    var idc = group.idc || 'UNKNOWN';
                    var idcCode= group['idc_code'] || 'UNKNOWN';

                    idc = diagramresource['访问入口IDC:'] + idc;
                    idcCode = diagramresource['部署IDC:'] + idcCode;


                    return {
                        name: '<a style="color: white" target="_blank" href="/portal/group#?env='+env+'&groupId='+id+'"><ul class="chart-ul">' +
                        '<li>Group ID:' + id + '</li>' +
                        '<li>App:' + re + '</li>' +
                        '<li>'+idc+'</li>' +
                        '<li>'+idcCode+'</li>' +
                        '</ul></a>',
                        weight: weight,
                        icon: icon

                    }

                });

                return {
                    vsNodes: vses,
                    groupNodes: groups
                }
            });

            this.draw(container, nodes,env, policyId, diagramresource);

        },
        writePolicyMetric: function (container, category, series, diagramresource) {

            var chart = Highcharts.chart(container, {
                chart: {
                    type: 'line'
                },
                title: {
                    text: diagramresource['当前策略上应用的流量']
                },
                subtitle: {
                    text: diagramresource['当前策略所分担的灰度流量']
                },
                xAxis: {
                    categories: category

                },
                yAxis: {
                    title: {
                        text: diagramresource['个数']
                    }
                },
                plotOptions: {
                    line: {
                        enableMouseTracking: false
                    }
                },
                series: series,
                credits: {
                    enabled: false
                }
            });
            return chart;
        }
    }
}();