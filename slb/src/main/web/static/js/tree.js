var m = [80, 30, 80, 80],
    w = 1280 - m[1] - m[3],
    h = 600 - m[0] - m[2],
    i = 0,
    root;

var tree;
var diagonal;
var vis;

function treeinit(showArea) {
    tree = d3.layout.tree()
        .size([h, w]);

    diagonal = d3.svg.diagonal()
        .projection(function (d) {
            return [d.y, d.x];
        });

    vis = d3.select(showArea).append("svg:svg")
        .attr("width", w + m[1] + m[3])
        .attr("height", h + m[0] + m[2])
        .append("svg:g")
        .attr("transform", "translate(" + m[3] + "," + m[0] + ")");

}
function drawtree(json) {
    root = json;
    root.x0 = h / 2;
    root.y0 = 0;

    function toggleAll(d) {
        if (d.children) {
            d.children.forEach(toggleAll);
            toggle(d);
        }
    }
    // Initialize the display to show a few nodes.

    update(root);
}

function update(source) {
    var duration = d3.event && d3.event.altKey ? 5000 : 500;

    // Compute the new tree layout.
    var nodes = tree.nodes(root).reverse();

    // Normalize for fixed-depth.
    nodes.forEach(function (d) {
        d.y = d.depth * 180;
    });

    // Update the nodes…
    var node = vis.selectAll("g.node")
        .data(nodes, function (d) {
            return d.id || (d.id = ++i);
        });

    // Enter any new nodes at the parent's previous position.
    var nodeEnter = node.enter().append("svg:g")
        .attr("class", "node")
        .attr("transform", function (d) {
            return "translate(" + source.y0 + "," + source.x0 + ")";
        })
        .on("click", function (d) {
            toggle(d);
            update(d);
        });

    nodeEnter.append("svg:circle")
        .attr("r", 1e-6)
        .style("fill", function (d) {
            return d._children ? "lightsteelblue" : "#fff";
        });

    var transform="rotate(0 10,100)";
    var transform_normal="rotate(0 0,0)";

    nodeEnter.append("svg:text")
        .attr("x", function (d) {
            return d.children || d._children ? -10 : 10;
        })
        .attr("dy", ".35em")
        .attr('transform', function (d) {
            if(d.children!=undefined){
                return transform;
            }else{
                return transform_normal;
            }
        })
        .attr("text-anchor", function (d) {
            return d.children || d._children ? "end" : "start";
        }).html(function (d) {
            var v='';
            var x = (d.children==undefined)?5:-5;
            $.each(d.name, function (i, item) {
                var y=i==0?0:(10*i+10);
                if(item.indexOf('%')!=-1){
                    v += '<tspan style="fill: red" x="'+x+'" y="'+y+'"><title>'+item+'</title>'+shortFor(item)+'</tspan>';
                }else{
                    v += '<tspan x="'+x+'" y="'+y+'"><title>'+item+'</title>'+shortFor(item)+'</tspan>';
                }
            });
            return v;
        })
        .style("fill-opacity", 1e-6);

    // Transition nodes to their new position.
    var nodeUpdate = node.transition()
        .duration(duration)
        .attr("transform", function (d) {
            return "translate(" + d.y + "," + d.x + ")";
        });

    nodeUpdate.select("circle")
        .attr("r", 4.5)
        .style("fill", function (d) {
            return d._children ? "lightsteelblue" : "#fff";
        });

    nodeUpdate.select("text")
        .style("fill-opacity", 1);

    // Transition exiting nodes to the parent's new position.
    var nodeExit = node.exit().transition()
        .duration(duration)
        .attr("transform", function (d) {
            return "translate(" + source.y + "," + source.x + ")";
        })
        .remove();

    nodeExit.select("circle")
        .attr("r", 1e-6);

    nodeExit.select("text")
        .style("fill-opacity", 1e-6);

    // Update the links…
    var link = vis.selectAll("path.link")
        .data(tree.links(nodes), function (d) {
            return d.target.id;
        });

    // Enter any new links at the parent's previous position.
    link.enter().insert("svg:path", "g")
        .attr("class", "link")
        .attr("d", function (d) {
            var o = {x: source.x0, y: source.y0};
            return diagonal({source: o, target: o});
        })
        .transition()
        .duration(duration)
        .attr("d", diagonal);

    // Transition links to their new position.
    link.transition()
        .duration(duration)
        .attr("d", diagonal);

    // Transition exiting nodes to the parent's new position.
    link.exit().transition()
        .duration(duration)
        .attr("d", function (d) {
            var o = {x: source.x, y: source.y};
            return diagonal({source: o, target: o});
        })
        .remove();

    // Stash the old positions for transition.
    nodes.forEach(function (d) {
        d.x0 = d.x;
        d.y0 = d.y;
    });
}


// Toggle children.
function toggle(d) {
    if (d.children) {
        d._children = d.children;
        d.children = null;
    } else {
        d.children = d._children;
        d._children = null;
    }
}

function shortFor(name){
    var start='';
    var end='';
    if(name.length>15){
        start=name.substring(0,10);
        end=name.substring(name.length-5,name.length-1);
    }
    if(start && end) return start+'...'+end;
    else return name;
}

