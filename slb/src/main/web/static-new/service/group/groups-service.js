var GroupsService = Class.extend({
    init: function ($http, $q) {
        var appdao = AppRepository.create($http);
        var userdao = UserRepository.create($http);
        var vsesdao = VsesRepository.create($http);
        var groupdao = GroupsRepository.create($http);
        var slbdao = SlbsRepository.create($http);

        this.appdao = appdao;
        this.userdao = userdao;
        this.vsdao = vsesdao;
        this.groupdao = groupdao;
        this.slbdao = slbdao;

        this.$q = $q;
    },

    getGroupsByAppId: function (appId, type) {
        var that = this;
        var appdao = this.appdao;
        var userdao = this.userdao;
        var vsesdao = this.vsdao;
        var groupdao = this.groupdao;
        var slbdao = this.slbdao;

        var appsPromise = appdao.getAppsByAppId(appId);
        var usersPromise = userdao.getUsers();
        var vsesPromises = vsesdao.getVses('extended');
        var groupsPromise = groupdao.getGroupsByAppId(appId, 'extended');
        var groupsStatusPromise = groupdao.getGroupsStatusByAppId(appId);

        var groupsStaticsPromise = groupdao.getGroupsStatics();
        var slbsPromise = slbdao.getSlbs("extended");

        var allPromises = [groupsPromise];
        var otherPromises = [appsPromise, groupsStatusPromise, vsesPromises, usersPromise, slbsPromise, groupsStaticsPromise];
        if (type == 'extended') {
            allPromises = allPromises.concat(otherPromises)
        }

        return this.$q.all(allPromises).then(function (data) {
            return that.assembleGroupsData(data);
        });
    },
    getGroups: function (type) {
        var that = this;

        var appdao = this.appdao;
        var userdao = this.userdao;
        var vsesdao = this.vsdao;
        var groupdao = this.groupdao;
        var slbdao = this.slbdao;

        var appsPromise = null;
        var usersPromise = userdao.getUsers();
        var vsesPromises = vsesdao.getVses('extended');
        var groupsPromise = groupdao.getGroups('extended');
        var groupsStatusPromise = groupdao.getGroupsStatus();
        var groupsStaticsPromise = groupdao.getGroupsStatics();
        var slbsPromise = slbdao.getSlbs("extended");

        var allPromises = [groupsPromise];
        var otherPromises = [appsPromise, groupsStatusPromise, vsesPromises, usersPromise, slbsPromise, groupsStaticsPromise];
        if (type == 'extended') {
            allPromises = allPromises.concat(otherPromises)
        }

        return this.$q.all(allPromises).then(function (data) {
            return that.assembleGroupsData(data);
        });
    },
    getAllAppsMeta: function () {
        // return the promise
        return this.appdao.getAppsMeta();
    },
    assembleGroupsData:function (data) {
        var hasError = _.filter(data, function (v) {
            return v && v.data.code;
        });

        if (hasError && hasError.length > 0) return new SlbException("Error for get groups api");

        var groups = data[0].data;

        var groupsStatus;
        var vses;
        var users;
        var slbs;
        var apps;
        var stastics;
        // if there are some errors

        if (data[2]) {
            groupsStatus = new C().parseGroupStatus(data[2].data)
        }
        if (data[4]) {
            users = new C().parseUsers(data[4].data);
        }
        if (data[1]) {
            apps = new C().parseApps(data[1].data, users);
        }
        if (data[5]) {
            slbs = new C().parseSlbs(data[5].data);
        }
        if (data[3]) {
            vses = new C().parseVses(data[3].data, slbs);
        }
        if (data[6]) {
            stastics = new C().parseGroupStastics(data[6].data);
        }
        var result = new C().parseGroups(groups, groupsStatus, stastics, vses, apps, users);
        return result;
    }
});