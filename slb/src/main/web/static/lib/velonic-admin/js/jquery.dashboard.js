/**
* Theme: Velonic Admin Template
* Author: Coderthemes
* Module/App: Dashboard Application
*/

!function($) {
    "use strict";

    var Dashboard = function() {
        this.$body = $("body")
    };

    //initializing various charts and components
    Dashboard.prototype.init = function() {
        /**
        * Morris charts
        */

         
        //Line chart
        Morris.Area({
            element: 'morris-area-example',
            lineWidth: 0,
            data: [
                { y: '2006', a: 10, b: 20 },
                { y: '2007', a: 75,  b: 65 },
                { y: '2008', a: 50,  b: 40 },
                { y: '2009', a: 60, b: 60 },
                { y: '2010', a: 75,  b: 65 },
                { y: '2011', a: 50,  b: 40 },
                { y: '2012', a: 95,  b: 95 },
                { y: '2013', a: 50,  b: 40 },
                { y: '2014', a: 75,  b: 65 },
                { y: '2015', a: 20, b: 30 }
            ],
            xkey: 'y',
            ykeys: ['a', 'b'],
            labels: ['Series A', 'Series B'],
            resize: true,
            pointSize: 0,
            smooth: true,
            fillOpacity: 0.7,
            hideHover: 'auto',
            gridLineColor: '#eef0f2',
            lineColors: ['#ebc142', '#03a9f4']
        });

        //Bar chart
        Morris.Bar({
            element: 'morris-bar-example',
            data: [
                    { y: 'Day1', a: 75,  b: 65 , c: 20 },
                    { y: 'Day2', a: 50,  b: 40 , c: 50 },
                    { y: 'Day3', a: 75,  b: 65 , c: 95 },
                    { y: 'Day4', a: 50,  b: 40 , c: 22 },
                    { y: 'Day5', a: 75,  b: 65 , c: 56 }
            ],
            xkey: 'y',
            ykeys: ['a', 'b', 'c'],
            labels: ['Series A', 'Series B', 'Series C'],
            gridLineColor: '#eef0f2',
            barSizeRatio: 0.5,
            numLines: 6,
            barGap: 6,
            resize: true,
            hideHover: 'auto',
            barColors: ['#ebc142', '#03a9f4', '#009688']
        });


        //Chat application -> You can initialize/add chat application in any page.
        $.ChatApp.init();
    },
    //init dashboard
    $.Dashboard = new Dashboard, $.Dashboard.Constructor = Dashboard
    
}(window.jQuery),

//initializing dashboad
function($) {
    "use strict";
    $.Dashboard.init()
}(window.jQuery);



