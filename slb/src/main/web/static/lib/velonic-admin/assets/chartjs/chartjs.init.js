
/**
* Theme: Velonic Admin Template
* Author: Coderthemes
* Chart Js Page
*/

!function($) {
    "use strict";

    var ChartJs = function() {};

    ChartJs.prototype.respChart = function respChart(selector,type,data, options) {
        // get selector by context
        var ctx = selector.get(0).getContext("2d");
        // pointing parent container to make chart js inherit its width
        var container = $(selector).parent();

        // enable resizing matter
        $(window).resize( generateChart );

        // this function produce the responsive Chart JS
        function generateChart(){
            // make chart width fit with its container
            var ww = selector.attr('width', $(container).width() );
            switch(type){
                case 'Line':
                    new Chart(ctx).Line(data, options);
                    break;
                case 'Doughnut':
                    new Chart(ctx).Doughnut(data, options);
                    break;
                case 'Pie':
                    new Chart(ctx).Pie(data, options);
                    break;
                case 'Bar':
                    new Chart(ctx).Bar(data, options);
                    break;
                case 'Radar':
                    new Chart(ctx).Radar(data, options);
                    break;
                case 'PolarArea':
                    new Chart(ctx).PolarArea(data, options);
                    break;
            }
            // Initiate new chart or Redraw

        };
        // run function - render chart at first load
        generateChart();
    },
    //init
    ChartJs.prototype.init = function() {
        //creating lineChart
        var data = {
            labels : ["January","February","March","April","May","June","July"],
            datasets : [
                {
                    fillColor : "rgba(235, 193, 66, 0.4)",
                    strokeColor : "rgba(235, 193, 66, 1)",
                    pointColor : "#ebc142",
                    pointStrokeColor : "#fff",
                    data : [33,52,63,92,50,53,46]
                },
                {
                    fillColor : "rgba(3, 169, 244, 0.4)",
                    strokeColor : "rgba(3, 169, 244, 1)",
                    pointColor : "rgba(3, 169, 244, 1)",
                    pointStrokeColor : "#fff",
                    data : [22,20,30,60,29,25,12]
                },
                {
                    fillColor : "rgba(0, 150, 136, 0.4)",
                    strokeColor : "rgba(0, 150, 136, 1)",
                    pointColor : "rgba(0, 150, 136, 1)",
                    pointStrokeColor : "#fff",
                    data : [14,16,12,5,32,9,33]
                }

            ]
        };
        
        this.respChart($("#lineChart"),'Line',data);

        //donut chart
        var data1 = [
            {
                        value: 30,
                        color:"#ebc142"
                    },
                    {
                        value : 50,
                        color : "#03a9f4"
                    },
                    {
                        value : 100,
                        color : "#009688"
                    },
                    {
                        value : 40,
                        color : "#95D7BB"
                    },
                    {
                        value : 120,
                        color : "#4D5360"
                    }

        ]
        this.respChart($("#doughnut"),'Doughnut',data1);


        //Pie chart
        var data2 = [
            {
                value: 40,
                color:"#ebc142"
            },
            {
                value : 80,
                color : "#03a9f4"
            },
            {
                value : 70,
                color : "#009688"
            }
        ]
        this.respChart($("#pie"),'Pie',data2);


        //barchart
        var data3 = {
            labels : ["January","February","March","April","May","June","July"],
                    datasets : [
                        {
                            fillColor : "rgba(235, 193, 66, 0.4)",
                            strokeColor : "rgba(235, 193, 66, 0.4)",
                            data : [65,59,90,81,56,55,40]
                        },
                        {
                            fillColor : "rgba(0, 150, 136, 0.4)",
                            strokeColor : "rgba(0, 150, 136, 0.4)",
                            data : [28,48,40,19,96,27,100]
                        }
                    ]
        }
        this.respChart($("#bar"),'Bar',data3);

        //radar chart
        var data4 = {
            labels : ["Eating","Drinking","Sleeping","Designing","Coding","Partying","Running"],
            datasets : [
                {
                    fillColor : "rgba(235, 193, 66, 0.5)",
                    strokeColor : "rgba(235, 193, 66, 0.9)",
                    pointColor : "rgba(235, 193, 66, 1)",
                    pointStrokeColor : "#fff",
                    data : [65,59,90,81,56,55,40]
                },
                {
                    fillColor : "rgba(0, 150, 136, 0.5)",
                    strokeColor : "rgba(0, 150, 136, 0.9)",
                    pointColor : "rgba(0, 150, 136, 1)",
                    pointStrokeColor : "#fff",
                    data : [28,48,40,19,96,27,100]
                }
            ]
        }
        this.respChart($("#radar"),'Radar',data4);

        //Polar area chart
        var data5 = [
            {
                value : 30,
                color: "#ebc142"
            },
            {
                value : 90,
                color: "#03a9f4"
            },
            {
                value : 24,
                color: "#009688"
            },
            {
                value : 58,
                color: "#f13c6e"
            },
            {
                value : 82,
                color: "#615ca8"
            },
            {
                value : 8,
                color: "#1ca8dd"
            }
        ]
        this.respChart($("#polarArea"),'PolarArea',data5);
    },
    $.ChartJs = new ChartJs, $.ChartJs.Constructor = ChartJs

}(window.jQuery),

//initializing 
function($) {
    "use strict";
    $.ChartJs.init()
}(window.jQuery);