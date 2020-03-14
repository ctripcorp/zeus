var SlbsRepository = Repository.extend(
    {
        init: function ($http, command) {
            command = command || new RestCommand();
            this.callSuper($http, command);
            this.base = G.baseUrl;
        },
        getSlbs: function (type) {
            var path = this.base + '/api/slbs';
            var param;
            if (type) {
                param = {
                    type: type
                };
            }

            return this.get(path, param);
        },
        getSlbById: function (id, type) {
            var path = this.base+'/api/slb';
            var param={};
            if(type){
                param.type = type;
            }
            if(id){
                param.slbId = id;
            }

            return this.get(path,param);
        },
        getSlbByName: function (name, type) {
            var path = this.base+'/api/slb';
            var param={};
            if(type){
                param.type = type;
            }
            if(name){
                param.slbName = name;
            }

            return this.get(path,param);
        }
    }
);