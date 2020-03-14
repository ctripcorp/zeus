function newChart(chartDOM) {
    var chart = echarts.init(chartDOM);
    chart.showLoading({
        text: 'Loading',
        effect: 'whirling'
    });
    return chart;
}

function resizeChart(chart, legendData) {
    var c = $(chart.getDom());
    var t = $('<div style="position:absolute;font-size:12px"></div>');
    t.width(c.width());
    _.each(legendData, function (v, i) {
        t.append($('<div style="float:left;">' + v + '</div>'));
    });
    t.append($('<div style="clear:both;">0</div>'));
    $('body').append(t);
    var h = t.height();
    t.detach();
    c.height(c.height() + h + 15);
    chart.resize();
}

function loadChartData(chart, res, title, description, startTime, interval, needMark, groupByCount, groupByFun, xAxisFunc) {
    var groupArr = res['time-series-group-list'];
    var length = 0;
    if (groupArr) {
        length = groupArr.length;
    }
    if (length == 0) {
        showNoDataChart(chart, title);
        return;
    }


    var legendData = [];
    var series = [];
    var xPoints = [];

    var n = length;
    if (groupByCount && (!isNaN(groupByCount)) && groupByCount > 0 && groupByCount < length) {
        n = groupByCount;
    }
    for (var i = 0; i < n; i++) {
        var v = groupArr[i];
        var l = '';
        $.each(v['time-series-group'], function (k, v) {
            l += v;
        });
        legendData.push(l);

        var dataPoints = v['data-points']['data-points'];
        var first = dataPoints.indexOf(null);
        if (first != -1) {
            dataPoints.splice(first - 1, 1);
        }
        $.each(dataPoints, function (i, v) {
            if (v === null) {
                dataPoints[i] = '-';
            }
        });
        var s = {
            name: l,
            type: 'line',
            symbol: 'none',
            data: dataPoints
        };
        if (needMark) {
            s.markLine = {
                data: [
                    {type: 'average', name: '平均值'}
                ]
            };
            s.markPoint = {
                data: [
                    {type: 'max', name: '最大值'},
                    {type: 'min', name: '最小值'}
                ]
            };
        }
        series.push(s);

    }

    //build x-points
    if (groupArr.length > 0) {
        var len = groupArr[0]['data-points']['data-points'].length;
        for (var j = 0; j < len; j++) {
            xPoints.push($.format.date(startTime, 'HH:mm'));
            startTime += interval;
        }
    }

    var option = {
        title: {
            text: title,
            subtext: description,
            textStyle: {
                fontSize: 14
            },
            subtextStyle: {
                align: 'right'
            }
        },
        tooltip: {
            trigger: 'axis',
            formatter: function (a) {
                var str = '<div>';
                str += '<div>' + a[0].name + '</div>';
                for (var i = 0; i < a.length; i++) {
                    var sName = a[i].seriesName;
                    try{
                        if(constants && constants.idcMapping2[sName]){
                            sName = sName+'('+constants.idcMapping2[sName]+')'
                        }
                        // if there is axis convert function

                        if(xAxisFunc){
                            sName = xAxisFunc(sName);
                        }
                    }catch (ex){

                    }

                    str += '<div style="padding-left:10px"><i class="fa fa-circle" style="color: ' + a[i].color + '; padding-right: 5px"></i>' + sName + ':<span style="padding-left:5px">' + a[i].value + '</span></div>';
                }
                str += '</div>';

                return str;
            }
        },
        grid: {
            x: 62,
            y: 50,
            x2: 60,
            height: 300
        },
        legend: {
            left: 40,
            height: 20,
            top: 380,
            formatter: function (name) {
                try{
                    if(constants && constants.idcMapping2){
                        var idcs = constants.idcMapping2;
                        var text = idcs[name];
                        if (text) {
                            name = name + '(' + text + ')'
                        }
                    }

                    if(xAxisFunc){
                        name = xAxisFunc(name);
                    }
                }catch (ex){

                }
                return echarts.format.truncateText(name, 480, '14px Microsoft Yahei', '…');
            },
            tooltip: {
                show: true,
                formatter: function (a) {
                    return 'test';
                }
            },
            data: legendData
        },
        toolbox: {
            show: true,
            feature: {
                myTool1: {
                    show: true,
                    readOnly: false,
                    title: '查看分组信息',
                    icon: 'path://M432.45,595.444c0,2.177-4.661,6.82-11.305,6.82c-6.475,0-11.306-4.567-11.306-6.82s4.852-6.812,11.306-6.812C427.841,588.632,432.452,593.191,432.45,595.444L432.45,595.444z M421.155,589.876c-3.009,0-5.448,2.495-5.448,5.572s2.439,5.572,5.448,5.572c3.01,0,5.449-2.495,5.449-5.572C426.604,592.371,424.165,589.876,421.155,589.876L421.155,589.876z M421.146,591.891c-1.916,0-3.47,1.589-3.47,3.549c0,1.959,1.554,3.548,3.47,3.548s3.469-1.589,3.469-3.548C424.614,593.479,423.062,591.891,421.146,591.891L421.146,591.891zM421.146,591.891',
                    onclick: function (option, chart) {
                        showLegends(option, chart, legendData, groupByFun);

                    }
                },
                star: {show: true},
                dataView: {show: true, readOnly: true},
                restore: {show: true},
                saveAsImage: {show: true}
            }
        },
        calculable: true,
        xAxis: [
            {
                type: 'category',
                data: xPoints,
                boundaryGap: false
            }
        ],
        yAxis: [
            {
                type: 'value',
                axisLabel: {
                    formatter: '{value}'
                }
            }
        ],
        series: series
    };
    resizeChart(chart, legendData);
    chart.setOption(option);
    chart.hideLoading();
}

function loadSimplePieChartData(chart, seriesData, title) {
    if (seriesData.length == 0) {
        showNoDataPieChart(chart, title);
        return;
    }
    var data = _.pluck(seriesData, 'name');
    var option = {
        title: {
            text: title,
            x: 'center'
        },
        tooltip: {
            trigger: 'item',
            formatter: "{a} <br/>{b} : {c} ({d}%)"
        },
        legend: {
            orient: 'vertical',
            x: 'left',
            data: data
        },
        toolbox: {
            show: true,
            feature: {
                mark: {show: true},
                dataView: {show: true, readOnly: false},
                magicType: {
                    show: true,
                    type: ['pie', 'funnel'],
                    option: {
                        funnel: {
                            x: '25%',
                            width: '50%',
                            funnelAlign: 'left',
                            max: 1548
                        }
                    }
                },
                restore: {show: true},
                saveAsImage: {show: true}
            }
        },
        calculable: true,
        series: [
            {
                name: title,
                type: 'pie',
                radius: '55%',
                center: ['50%', '60%'],
                data: seriesData
            }
        ]
    };

    chart.setOption(option);
    chart.hideLoading();
}

function loadPieChartData(chart, res, title, description, startTime, interval, groupByType) {
    var groupArr = res['time-series-group-list'];
    var length = 0;
    if (groupArr) {
        length = groupArr.length;
    }

    if (length == 0) {
        showNoDataPieChart(chart, title);
        return;
    }

    var data = [];
    var seriesData = [];
    if (groupArr.length > 0) {
        if (groupByType == "status") {
            $.each(groupArr, function (i, val) {
                if (val['time-series-group'] && val['time-series-group'][groupByType]) {
                    if (val['time-series-group'][groupByType] != "200") {
                        data.push(val['time-series-group'][groupByType]);
                    }
                }
                if (val['data-points'] && val['data-points'].total) {
                    if (val['time-series-group'][groupByType] != "200") {
                        seriesData.push(
                            {
                                value: val['data-points'].total,
                                name: val['time-series-group'][groupByType]
                            }
                        );
                    }
                }
            })
        }
        else {
            $.each(groupArr, function (i, val) {
                if (val['time-series-group'] && val['time-series-group'][groupByType]) {
                    data.push(val['time-series-group'][groupByType]);
                }
                if (val['data-points'] && val['data-points'].total) {
                    seriesData.push(
                        {
                            value: val['data-points'].total,
                            name: val['time-series-group'][groupByType]
                        }
                    );
                }
            })
        }
    }

    var option = {
        title: {
            text: title,
            x: 'center'
        },
        tooltip: {
            trigger: 'item',
            formatter: "{a} <br/>{b} : {c} ({d}%)"
        },
        legend: {
            orient: 'vertical',
            x: 'left',
            data: data
        },
        toolbox: {
            show: true,
            feature: {
                mark: {show: true},
                dataView: {show: true, readOnly: false},
                magicType: {
                    show: true,
                    type: ['pie', 'funnel'],
                    option: {
                        funnel: {
                            x: '25%',
                            width: '50%',
                            funnelAlign: 'left',
                            max: 1548
                        }
                    }
                },
                restore: {show: true},
                saveAsImage: {show: true}
            }
        },
        calculable: true,
        series: [
            {
                name: title,
                type: 'pie',
                radius: '55%',
                center: ['50%', '60%'],
                data: seriesData
            }
        ]
    };

    chart.setOption(option);
    chart.hideLoading();
}

function showNoDataPieChart(chart, title) {
    var option = {
        title: {
            text: title,
            subtext: 'No Data',
            x: 'center'
        },
        tooltip: {
            trigger: 'item',
            formatter: "{a} <br/>{b} : {c} ({d}%)"
        },
        legend: {
            orient: 'vertical',
            x: 'left',
            data: []
        },
        toolbox: {
            show: true,
            feature: {
                mark: {show: true},
                dataView: {show: true, readOnly: false},
                magicType: {
                    show: true,
                    type: ['pie', 'funnel'],
                    option: {
                        funnel: {
                            x: '25%',
                            width: '50%',
                            funnelAlign: 'left',
                            max: 1548
                        }
                    }
                },
                restore: {show: true},
                saveAsImage: {show: true}
            }
        },
        calculable: true,
        series: [
            {
                name: title,
                type: 'pie',
                radius: '55%',
                center: ['50%', '60%'],
                data: []
            }
        ]
    };

    chart.setOption(option);
    chart.hideLoading();
}

function showNoDataChart(chart, title) {
    var option = {
        title: {
            text: title,
            subtext: 'No Data',
            left: 'center',
            top: 'center',
            textStyle: {
                fontSize: 18,
                color: '#ccc'
            }
        },
        xAxis: [
            {
                type: 'category',
                data: [],
                boundaryGap: false
            }
        ],
        yAxis: [
            {
                type: 'value',
                axisLabel: {
                    formatter: '{value}'
                }
            }
        ],
        series: [{
            name: '',
            type: 'line',
            symbol: 'none',
            data: []
        }]
    };
    chart.setOption(option);
    chart.hideLoading();
}

function showLegends(option, chart, legendData, groupByFun) {
    var box = $(toolTemplate);
    var chartDom = $(chart.getDom());
    chartDom.append(box);
    $('.close-btn', box).click(function () {
        box.detach();
    });

    var c = $('.content', box);
    c.height(chartDom.height() - 80);

    if (groupByFun) {
        _.each(legendData, function (d) {
            c.append('<a style="display:block;padding:5px 20px;" target="_blank" href="' + groupByFun(d) + '">' + d + '</a>')
        });
    } else {
        var area = $('<textarea readonly="" style="width: 100%; height: 100%; font-family: monospace; font-size: 14px; line-height: 1.6rem; color: rgb(0, 0, 0); border-color: rgb(51, 51, 51); background-color: rgb(255, 255, 255);"></textarea>');
        c.append(area);

        var txt = '';
        _.each(legendData, function (d) {
            txt += d + "\n\n";
        });
        area.val(txt);
    }
}

var toolTemplate = ''
    + '<div style="position: absolute; left: 5px; top: 5px; bottom: 5px; right: 5px; background-color: rgb(255, 255, 255);">'
    + '    <h4 style="margin: 10px 20px; color: rgb(0, 0, 0);">查看分组</h4>'
    + '    <div class="content" style="border:1px solid #ccc;display: block; width: 100%; overflow: hidden; height: 100px;">'
    + '    </div>'
    + '    <div style="position: absolute; bottom: 0px; left: 0px; right: 0px;">'
    + '        <div class="close-btn" style="float: right; margin-right: 20px; border: none; cursor: pointer; padding: 2px 5px; font-size: 12px; border-radius: 3px; color: rgb(255, 255, 255); background-color: rgb(194, 53, 49);">关闭</div>'
    + '    </div>'
    + '</div>';


