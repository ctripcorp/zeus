var C = function () {

};


C.prototype.parseUsers = function (users) {
    if (users.code) return new SlbException("Error for get users api");

    var index = _.indexBy(users.users, function (v) {
        return v['user-name'];
    });
    return _.mapObject(index, function (value, key) {
        value.name = value['user-name'];
        return value;
    });
};

C.prototype.parseDr = function (dr) {
    if (dr && dr.code) return new SlbException(dr.message);
    return dr;
};
C.prototype.parseDrDeleteResult = function (result) {
    if (result && result.code) return new SlbException("Error for save dr api");
    return result;
};
C.prototype.parseDrLog = function (result) {
    if (result && result.code) return new SlbException("Error get dr latest logs");

    var logs = result['operation-log-datas'];

    $.each(logs, function (i, item) {
        var data = item.data;
        var isJson = IsJsonString(data);

        var version = '-';

        if (isJson) {
            data = JSON.parse(data);
            if (data['dr-datas'] && data['dr-datas'].length > 0) {
                version = data['dr-datas'][0].version;
            }
        }
        item['dr-version'] = version;

        item['description'] = data.description;
    });
    return logs;
};
C.prototype.parseSlbs = function (slbs) {
    if (slbs.code) return new SlbException("Error for get slbs api");

    var index = _.indexBy(slbs.slbs, function (v) {
        return v.id;
    });
    return _.mapObject(index, function (value, key) {
        if (value.properties) {
            value.properties = _.mapObject(_.indexBy(value.properties, function (v) {
                return v.name.toLowerCase();
            }), function (v, k) {
                v = v.value;
                return v;
            });
        }

        value['slb-servers'] = _.indexBy(value['slb-servers'], function (v) {
            return v['ip'];
        });

        return value;
    });
};
C.prototype.parseProperties = function (properties) {
    if (properties.code) return new SlbException("Slb properties api return error, " + properties.message);
    var g = _.groupBy(properties.properties, function (v) {
        return v.name.toLowerCase();
    });
    return _.mapObject(g, function (value, key, item) {
        value = _.uniq(_.pluck(value, 'value'));
        return value;
    });
};

C.prototype.parseSlb = function (slb) {
    if (slb.code) return new SlbException("Error for get slb api");
    return _.mapObject(slb, function (value, key) {
        if (key == 'properties') {
            value = _.mapObject(_.indexBy(value, function (v) {
                return v.name.toLowerCase();
            }), function (v, k) {
                v = v.value;
                return v;
            });
        }
        if (key == 'slb-servers') {
            value = _.indexBy(value, function (v) {
                return v['ip'];
            });
        }
        return value;
    });
};
C.prototype.parseApps = function (apps, users) {
    if (apps.code) return new SlbException("Either users or apps is not available");

    if (users.users) {
        users = _.indexBy(users.users, function (v) {
            return v['user-name'];
        });
    }


    apps = _.indexBy(apps.apps, function (v) {
        return v['app-id'];
    });

    var composedapps = _.mapObject(apps, function (value, key) {
        var name = value.owner;
        value.owner = {
            name: name,
            'chinese-name': users[name] ? (users[name]['chinese-name'] || 'unknown') : 'unknown',
            email: users[name] ? (users[name]['email'] || value['owner-email']) : 'unknown',
            id: users[name] ? users[name].id : 'unknown'
        };
        return value;
    });
    return composedapps;
};
C.prototype.parseGroups = function (groups, status, statics, vses, apps, users) {
    if (groups.code) return new SlbException("Error for get groups");
    var index = _.indexBy(groups.groups, function (v) {
        return v.id;
    });

    if (vses && users && status) {
        return _.mapObject(index, function (value, key) {
            var tags = value.tags;
            var owners = _.filter(tags, function (v) {
                if (v.indexOf('owner_') != -1) return v;
            });
            var focus = _.filter(tags, function (v) {
                if (v.indexOf('user_') != -1) return v;
            });

            owners = _.map(owners, function (v) {
                var alias = v.substring(6, v.length);
                return users[alias];
            });

            focus = _.map(focus, function (v) {
                var alias = v.substring(5, v.length);
                return users[alias];
            });

            value.owners = _.indexBy(owners, function (v) {
                return v ? v.name : '-';
            });

            value.focused = _.indexBy(focus, function (v) {
                return v ? v.name : '-';
            });

            value.properties = _.mapObject(_.indexBy(value.properties, function (v) {
                return v.name.toLowerCase();
            }), function (value, key) {
                value = value.value;
                return value;
            });
            value.qps = statics[key] ? statics[key].qps : '-';

            value['group-servers'] = _.indexBy(_.map(value['group-servers'], function (v) {
                var sKey = v.ip + '/' + v.port;
                v.status = status[key]['group-server-statuses'][sKey];
                return v;
            }), function (v) {
                return v.ip + '/' + v.port;
            });
            value['group-virtual-servers'] = _.indexBy(_.map(value['group-virtual-servers'], function (v) {
                var vsId = v['virtual-server'].id;
                v['virtual-server'] = vses[vsId];
                return v;
            }), function (v) {
                return v['virtual-server'].id;
            });
            return value;
        });
    }
    return index;
};
C.prototype.parseVses = function (vses, slbs) {
    if (vses.code) return new SlbException("Error for get vses");
    var index = _.indexBy(vses['virtual-servers'], function (v) {
        return v.id;
    });

    return _.mapObject(index, function (value, key) {
        value.properties = _.mapObject(_.indexBy(value.properties, function (s) {
            return s.name.toLowerCase();
        }), function (v, k) {
            v = v.value;
            return v;
        });

        value['slb-ids'] = _.indexBy(_.map(value['slb-ids'], function (v) {
            return slbs[v];
        }), function (v) {
            return v.id;
        });
        return value;
    });
};
C.prototype.parseGroupStatus = function (status) {
    if (status.code) return new SlbException("Error for get group status api");

    var index = _.indexBy(status['group-statuses'], function (v) {
        return v['group-id'];
    });
    return _.mapObject(index, function (value, key) {
        value['group-server-statuses'] = _.indexBy(value['group-server-statuses'], function (s) {
            return s.ip + '/' + s.port;
        });
        return value;
    });
};
C.prototype.parseGroupStastics = function (stastics) {
    if (stastics.code) return new SlbException("Error for get group stastics api");

    var index = _.indexBy(stastics['group-metas'], function (v) {
        return v['group-id'];
    });
    return index;
};

C.prototype.parseSlbMigrations = function (migrationsDo, statusMapping, slbs) {
    if (migrationsDo.code) return new SlbException("Error for get migrations api");
    var index = _.indexBy(migrationsDo, function (v) {
        return v.id;
    });
    return _.mapObject(index, function (v, k, item) {
        var status = statusMapping[v.status.toUpperCase()];
        var name = v.name;
        var id = v.id;
        var slbData = v['slb-data'][0];
        var idc = slbData.idc;
        var pci = slbData.pci ? '是' : '否';
        var zone = slbData['internet-access'] ? '外网' : '内网';
        var bgp = slbData.bgp ? '是' : '否';
        var vips = slbData.vips;
        var serverCount = slbData['server-count'];
        var slbId = slbData['new-slb-id'] || '-';

        var slbName;
        if (slbs) {
            slbName = slbs[slbId] ? slbs[slbId].name : '';
        }

        v = {
            id: id,
            name: name,
            idc: idc,
            pci: pci,
            zone: zone,
            bgp: bgp,
            vips: vips,
            serverCount: serverCount,
            slbId: slbId,
            slbName: slbName,
            status: status
        };
        return v;
    });
};

C.prototype.parseSlbSharding = function (shardingDos, slbs) {
    var statusMapping = constants.slbShardingStatusMap;
    return _.map(shardingDos, function (v) {
        v.status = statusMapping[v.status] || v.status;
        var from = slbs[v['related-slb-id']];
        if (!from) from = {
            idc: '',
            zone: ''
        };
        var ps = from.properties;
        ps = _.indexBy(ps, function (p) {
            return p.name.toLowerCase();
        });
        from.idc = ps.idc ? ps.idc.value : '未知IDC';
        from.zone = ps.zone ? ps.zone.value : '未知网段';

        v.fromSlb = from;
        var targets = v['slb-creating-entity']['slb-data'];
        var status = constants.slbCreationStatusMap[v['slb-creating-entity'].status] || v['slb-creating-entity'].status;
        var id = v['slb-creating-entity'].id;
        v.targetSlb = _.map(targets, function (t) {
            var mapping = _.invert(constants.idcMapping);
            var idc = mapping[t['idc'].toUpperCase()];
            var slb = t['new-slb-id'] ? slbs[t['new-slb-id']] : undefined;
            return {
                id: id,
                idc: idc,
                slb: slb,
                status: status
            }
        });

        var value = v['vs-migration'];
        if (value) {
            var content = JSON.parse(value.content);
            value.migrationStatus = constants.vsMigrationStatus[content.status] || content.status;
            v.vsMigration = value;
        }

        return v;
    });
};

C.prototype.parseVsWithStatics = function (vs, statistics) {
    var sobj = _.indexBy(statistics['vs-metas'], 'vs-id');
    vs.statistics = sobj[vs.id];
    var properties = vs.properties;

    if (properties) {
        properties = _.indexBy(properties, function (v) {
            return v.name.toLowerCase();
        });

        vs.status = properties['status']?properties['status'].value:'';
        vs.idc = properties['idc']?properties['idc'].value:'';
        vs.zone = properties['zone']?properties['zone'].value:'';
    }
    return vs;
};

function IsJsonString(str) {
    try {
        JSON.parse(str);
    } catch (e) {
        return false;
    }
    return true;
}