var TrafficPolicyGraphics = function() {
    return {
        draw:function(policies, data, container, activateFunc,deactivateFunc, weightFunc,toPolicyFunc, toPolicyMetrics, toTestFunc, showLable){
            var datamap= _.groupBy(data, function (item) {
                return item.policy.id;
            });
            policies= _.indexBy(policies,'id');

            data = _.map(data, function (item) {
                var status='-';
                var s =_.find(policies[item.policy.id].properties, function (i) {
                    return i.name=='status'
                });
                if(s) status= s.value.toLowerCase();

                var canDoActivate=item.rights.canActivatePolicy;
                var canDoUpdate = item.rights.canUpdatePolicy;
                var canDoNetGroupDeactivate = item.rights.canDeactivateNetGroup;

                return {
                    policyId: item.policy.id,
                    policyName: item.policy.name,
                    status:status,
                    rights:{
                        canActivate: canDoActivate,
                        canUpdate: canDoUpdate,
                        canDeactivateGroup: canDoNetGroupDeactivate
                    },
                    vses:datamap[item.policy.id]
                };
            });
            data = _.uniq(data, function (item, k, policyId) {
                return item.policyId;
            });

            var height=100;
            var nodes = [];
            var edges = [];

            for(var j=0;j<data.length;j++){
                if(showLable!=false){
                    var javacan= _.find(data[j].vses[0].groups, function (item) {
                        if(item.language && item.weight){
                            return item.language.toLowerCase()=='java' && item.weight=='100.0%';
                        }
                        return false;
                    });
                    var netcan = _.find(data[j].vses[0].groups, function (item) {
                        if(item.language && item.weight) {
                            return item.language.toLowerCase()=='.net' && item.weight=='0.0%';
                        }
                         return false;
                    });

                    var java= _.find(data[j].vses[0].groups, function (item) {
                        if(item.language && item.weight){
                            return item.language.toLowerCase()=='java';
                        }
                        return false;
                    });
                    var net = _.find(data[j].vses[0].groups, function (item) {
                        if(item.language && item.weight) {
                            return item.language.toLowerCase()=='.net';
                        }
                        return false;
                    });

                    var statuscan=data[j].status=='activated';

                    var p1=javacan&netcan&statuscan;

                    var canActivate = true;
                    var canUpdate = true;
                    var canDeactivateGroup = data[j].rights.canDeactivateGroup;

                    var res = this.drawLable(data[j].policyId,data[j].policyName,data[j].status,java.id,net.id,p1&canDeactivateGroup, canActivate, canUpdate,height);
                    nodes = nodes.concat(res.nodes);
                    edges = edges.concat(res.edges);
                    height+=100;
                }
                for(var i = 0; i < data[j].vses.length; i++) {
                    var res = this.drawOne(i, data[j].vses[i],height);
                    height += res.height;
                    nodes = nodes.concat(res.nodes);
                    edges = edges.concat(res.edges);
                }
            }

            container.html('');
            container.height(height);
            this.drawGraph(nodes, edges, container, activateFunc,deactivateFunc, weightFunc, toPolicyFunc,toPolicyMetrics,toTestFunc);
        },
        drawLable: function (id, name, status, java, net, candeactivate, canActivate,canUpdate, height) {
            var w = $(window).width()-300;

            nodes = [];
            nodes.push({
                data: { id: 'policy-name-'+id, label:name},
                classes:'pName',
                position: { x: 380, y: height }
            });
            var snode;
            switch (status){
                case 'activated':
                {
                    snode = {
                        data: {id: 'bt-status-' + id, label: '已激活'},
                        classes: 'status-activated',
                        position: {x: w - 580, y: height}
                    };
                    break;
                }
                case 'deactivated':{
                    snode={
                        data: { id: 'bt-status-'+id, label:'未激活'},
                        classes:'status-deactivated',
                        position: { x: w-580, y: height}
                    };
                    break;
                }

                case 'tobeactivated':{
                    snode={
                        data: { id: 'bt-changed-'+id, label:'有变更(Diff)'},
                        classes:'status-tobeactivated',
                        position: { x: w-590, y: height}
                    };
                    break;
                }
                default :break;
            }

            if(snode){
                nodes.push(snode);
            }
            if(candeactivate && status!='deactivated'){
                nodes.push({
                    data: { id: 'bt-deactivate-'+id, label:'下线.NET应用'},
                    classes:'btn3',
                    position: { x: w, y: height }
                });
            }
            nodes.push({
                data: { id: 'bt-testabc-'+id, label:'灰度测试'},
                classes:'btn2',
                position: { x: w-140, y: height }
            });

            nodes.push({
                data: { id: 'bt-metrics-'+id, label:'监控'},
                classes:'btn2',
                position: { x: w-250, y: height }
            });

            if(canUpdate){
                nodes.push({
                    data: { id: 'bt-weight-'+id, label:'更新'},
                    classes:'btn1',
                    position: { x: w-360, y: height}
                });
            }

            if(canActivate){
                nodes.push({
                    data: { id: 'bt-activate-'+id, label:'激活上线'},
                    classes:'btn2',
                    position: { x: w-470, y: height }
                });
            }

            return {
                nodes:nodes,
                edges:[],
                height:height
            };
        },
        drawOne: function(index, data, startY){
            vs = data.vs;
            vsId = index + '-' +vs.id;
            policy = data.policy;
            policyId = index + '-' + policy.id;
            groups = data.groups;
            count = groups.length;

            groupHeight = 100;
            vsX = 30;
            vsY = startY + groupHeight*(count -1)/2;
            policyX = 500;
            policyY = startY + groupHeight*(count - 1)/2;
            groupX = 700;

            vsLabel = '';
            $.each(vs.domains, function (i, val) {
                vsLabel += val + "\n";
            });

            if(vs.soaops.length>0){
                vsLabel+='soa操作:';
            }
            $.each(vs.soaops, function (i, val) {
                val=val.toLowerCase();

                if(i!=vs.soaops.length-1){
                    vsLabel+=val+',\n';
                }else{
                    vsLabel+=val;
                }
            });
            if(vs.slbops.length>0){
                vsLabel+='子入口:';
            }
            $.each(vs.slbops, function (i, val) {
                val=val.toLowerCase();
                if(i!=vs.slbops.length-1){
                    vsLabel+='/'+val+',\n';
                }else{
                    vsLabel+='/'+val;
                }
            });

            vsLabel+='\n';
            vsLabel += vs.idc;

            nodes = [];
            edges = [];
            nodes.push({
                data: { id: vsId+policyId, label:vsLabel},
                classes:'vs',
                position: { x: vsX, y: vsY }
            });

            nodes.push({
                data: { id: policyId+policyId, label:policy.name},
                classes:'policy',
                position: { x: policyX, y: policyY }
            });

            edges.push({ data: { source: vsId+policyId, target: policyId+policyId}});

            $.each(groups, function (i, g) {
                var gLabel = 'G: ' + g.id + "/" + g.name + "\n" + 'A: ' +  g["app-id"] + "/" + g["app-name"] + "\n" + g.idc;
                var gId =   index + '-' +g.id;
                nodes.push({
                    data: { id: gId+policyId, label: gLabel},
                    classes:'group',
                    position: { x: groupX, y: startY + groupHeight*i }
                });

                edges.push(
                    { data: { source: policyId+policyId, target:  gId+policyId, name: g.weight }, classes: 'weight' }
                );
            });

            return {
                nodes:nodes,
                edges:edges,
                height:count*groupHeight + 80 + (vs.domains.length>8?(vs.domains.length-8)*20:0)
            };
        },
        drawGraph:function(nodes, edges, container, activateFunc,deactivateFunc, weightFunc,toPolicyFunc, toMetricsFunc,toTestFunc){

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
                    }).selector('.pName')
                    .css({
                        'font-size':30,
                        'color': '#333b4d',
                        'background-color':'#ffffff',
                        'shape':'rectangle',
                        'width': 800,
                        'height': 50
                    }).selector('.btn1')
                    .css({
                        'font-size':18,
                        'color': 'white',
                        shape:'rectangle',
                        'background-color':'#5bc0de',
                        'width': 100,
                        'height': 40
                    }).selector('.status-activated')
                    .css({
                        'font-size':25,
                        'color': 'green',
                        'width': 1,
                        'height': 1
                    }).selector('.status-deactivated')
                    .css({
                        'font-size':25,
                        'color': 'gray',
                        'width': 1,
                        'height': 1
                    }).selector('.status-tobeactivated')
                    .css({
                        'font-size':25,
                        'color': '#c7c412',
                        'background-color':'white',
                        'width': 100,
                        'height': 50
                    }).selector('.btn2')
                    .css({
                        'font-size':18,
                        'color': 'white',
                        'background-color':'#5bc0de',
                        shape:'rectangle',
                        'width': 100,
                        'height': 40
                    }).selector('.btn3')
                    .css({
                        'font-size':18,
                        'color': 'white',
                        'background-color':'#5bc0de',
                        shape:'rectangle',
                        'width': 150,
                        'height': 40
                    }),
                elements: {
                    nodes:nodes,
                    edges:edges
                },

                layout: {
                    name: 'preset'
                }
            });

            cy.on('tap', 'node', { foo: 'bar' }, function(evt){
                var node = evt.cyTarget;
                var nodeId=node.id();
                var len = nodeId.length;

                var pId;
                var java;
                var net;

                if(nodeId.startsWith('bt-weight')){
                    pId = nodeId.substring(10,len);
                    weightFunc(pId);
                }
                else if(nodeId.startsWith('bt-activate')){
                    pId = nodeId.substring(12,len);
                    activateFunc(pId);
                }  else if(nodeId.startsWith('policy-name')){
                    pId = nodeId.substring(12,len);
                    toPolicyFunc(pId);
                }  else if(nodeId.startsWith('bt-changed')){
                    pId = nodeId.substring(11,len);
                    activateFunc(pId);
                }
                else if(nodeId.startsWith('bt-deactivate')){
                    pId = nodeId.substring(14,len);
                    deactivateFunc(pId);
                }else if(nodeId.startsWith('bt-metrics')){
                    pId = nodeId.substring(11,len);
                    toMetricsFunc(pId);
                }else if(nodeId.startsWith('bt-testabc')){
                    pId = nodeId.substring(11,len);
                    toTestFunc(pId);
                }

                else return ;
            });
            cy.reset();
            cy.panningEnabled(false);
            cy.autoungrabify(true);
        }
    }
}();