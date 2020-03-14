var TrafficPolicyGraphics = function() {
    return {
        draw:function(data, container){
            var height = 0;
            var nodes = [];
            var edges = [];
            for(var i = 0; i < data.length; i++) {
                var res = this.drawOne(i, data[i],height);
                height += res.height;
                nodes = nodes.concat(res.nodes);
                edges = edges.concat(res.edges);
            }

            container.html('');
            container.height(height);
            this.drawGraph(nodes, edges, container);
        },
        drawOne: function(index, data, startY){
            vs = data.vs;
            vsId = index + '-' +vs.id;
            policy = data.policy;
            policyId = index + '-' + policy.id;
            groups = data.groups;
            count = groups.length;

            groupHeight = 100;
            vsX = 0;
            vsY = startY + groupHeight*(count -1)/2;
            policyX = 500;
            policyY = startY + groupHeight*(count - 1)/2;
            groupX = 700;

            vsLabel = '';
            $.each(vs.domains, function (i, val) {
                vsLabel += val + "\n";
            });

            if(vs.soaops && vs.soaops.length>0){
                vsLabel+='soa操作:';
            }
            $.each(vs.soaops, function (i, val) {
                val=val.toLowerCase();
                if(i!=vs.soaops.length-1){
                    vsLabel+=val+',';
                }else{
                    vsLabel+=val;
                }
            });
            if(vs.slbops && vs.slbops.length>0){
                vsLabel+='子入口:';
            }
            $.each(vs.slbops, function (i, val) {
                val=val.toLowerCase();
                if(i!=vs.slbops.length-1){
                    vsLabel+='/'+val+',';
                }else{
                    vsLabel+='/'+val;
                }
            });

            vsLabel+='\n';
            vsLabel += vs.idc;

            nodes = [];
            edges = [];

            nodes.push({
                data: { id: vsId, label:vsLabel},
                classes:'vs',
                position: { x: vsX, y: vsY }
            });

            nodes.push({
                data: { id: policyId, label:policy.name},
                classes:'policy',
                position: { x: policyX, y: policyY }
            });

            edges.push({ data: { source: vsId, target: policyId}});

            $.each(groups, function (i, g) {
                var gLabel = 'G: ' + g.id + "/" + g.name + "\n" + 'A: ' +  g["app-id"] + "/" + g["app-name"] + "\n" + g.idc;
                var gId =   index + '-' +g.id;
                nodes.push({
                    data: { id: gId, label: gLabel},
                    classes:'group',
                    position: { x: groupX, y: startY + groupHeight*i }
                });

                edges.push(
                    { data: { source: policyId, target:  gId, name: g.weight }, classes: 'weight' }
                );
            });

            return {
                nodes:nodes,
                edges:edges,
                height:count*groupHeight + 80 + (vs.domains.length>8?(vs.domains.length-8)*20:0)
            };
        },
        drawGraph:function(nodes, edges, container){
            cy = cytoscape({
                container: container,
                userZoomingEnabled: false,
                style: cytoscape.stylesheet()
                    .selector('node')
                    .css({
                        'label': 'data(label)',
                        'text-wrap': 'wrap',
                        'text-valign': 'center',
                        'font-size':18,
                        'width': 50,
                        'height': 50
                    })
                    .selector('edge')
                    .css({
                        'content': 'data(name)',
                        'width': 2,
                        'font-size':18,
                        'line-color': '#888',
                        'target-arrow-shape': 'triangle',
                        'target-arrow-color': '#888',
                        'source-arrow-color': '#888',
                        'curve-style': 'bezier'
                    })
                    .selector('.vs')
                    .css({
                        'color':'#333b4d',
                        'text-valign': 'bottom',
                        'text-halign': 'right'
                    })
                    .selector('.policy')
                    .css({
                        'background-color': '#F79646',
                        'text-outline-color': '#F79646',
                        'line-color': '#F79646',
                        'target-arrow-color': '#F79646',
                        'color': '#f75026',
                        'text-valign': 'top',
                        'text-halign': 'left'
                    })
                    .selector('.group')
                    .css({
                        'background-color': '#93CDDD',
                        'text-outline-color': '#93CDDD',
                        'line-color': '#93CDDD',
                        'target-arrow-color': '#93CDDD',
                        'text-valign': 'center',
                        'text-halign': 'right',
                        'color':'#333b4d'
                    })
                    .selector('.weight')
                    .css({
                        'background-color': '#F79646',
                        'text-outline-color': '#F79646',
                        'line-color': '#F79646',
                        'target-arrow-color': '#F79646',
                        'text-outline-color': 'red',
                        'text-outline-width': 5,
                        'font-size':24,
                        'color': '#FFF'
                    }),

                elements: {
                    nodes:nodes,
                    edges:edges
                },

                layout: {
                    name: 'preset'
                }
            });

        }
    }
}();