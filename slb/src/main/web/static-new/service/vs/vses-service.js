var VsesService = Class.extend({
    init: function ($http, $q) {
        try {
            var vsesdao = VsesRepository.create($http);
            this.vsesdao = vsesdao;

            var vsesstatisticsdao = VsStatisticsRepository.create($http);
            this.vsesstatisticsdao = vsesstatisticsdao;

            var slbsdao = SlbsRepository.create($http);
            this.slbsdao = slbsdao;

        } catch (e) {
            // do nothing
        }

        this.$q = $q;
    },

    getVsById: function (id) {
        var vsesdao = this.vsesdao;
        var vsesstatisticsdao = this.vsesstatisticsdao;
        var slbsdao = this.slbsdao;

        var vspromise;
        var statisticspromise;

        var $q = this.$q;
        var promises = [];

        var vs;
        var statistics;

        if (vsesdao) {
            vspromise = vsesdao.getVsById('extended', id).success(function (data, code) {
                if (code != 200) {
                    vs = -1;
                } else {
                    vs = data;
                }
            });
            promises.push(vspromise);
        }
        if (vsesstatisticsdao) {
            statisticspromise = vsesstatisticsdao.getVsStatisticsById(id).success(function (data, code) {
                if (code != 200) {
                    statistics = -1;
                } else {
                    statistics = data;
                }
            });
            promises.push(statisticspromise);
        }

        var temp = {};

        var promise1 = this.$q.all(promises).then(function () {
                if (vs == -1 || statistics == -1) {
                    temp = new SlbException('Api Failed');
                }

                temp = {
                    vs: vs,
                    statistics: statistics
                };

                return temp;
            }
        );


        var temp2 = {};
        var f1 = promise1.then(function (data) {
            if (temp.message) temp2 = temp;

            if (temp.vs && temp.statistics) {
                temp2 = new C().parseVsWithStatics(temp.vs, temp.statistics);
                temp2.type = 'merge';
            } else if (temp.vs) {
                temp2 = temp.vs;
                temp2.type = 'vs';
            } else if (temp.statistics) {
                temp2 = temp.statistics;
                temp2.type = 'statistics';
            }
            return temp2;
        });

        var f2 = f1.then(function (data) {
            var vs = temp2;
            if (vs.message || vs.type == 'statistics') {
                return vs;
            }
            var vsslbs = vs['slb-ids'];

            var temp3 = [];
            $.each(vsslbs, function (i, slb) {
                temp3.push(slbsdao.getSlbById(slb, 'info'));
            });

            return $q.all(temp3);
        });

        return f2.then(function (data) {
            var vs = data[0].data;
            if (vs.message || vs.type == 'statistics') {
                return vs;
            }
            var slbs = [];
            _.map(data, function (d) {
                slbs.push(d.data);
            });
            var slbsObj = _.indexBy(slbs, 'id');

            temp2.slbs = _.map(temp2['slb-ids'], function (v) {
                return slbsObj[v];
            });
            return temp2;
        });
    },

    getVsByIds: function (ids) {
        var vsesdao = this.vsesdao;
        var vsesstatisticsdao = this.vsesstatisticsdao;
        var slbsdao = this.slbsdao;

        var vspromise;
        var statisticspromise;

        var $q = this.$q;
        var promises = [];

        var vs;
        var statistics;


        if (vsesdao) {
            vspromise = vsesdao.getVsByIds('extended', ids).success(function (data, code) {
                if (code != 200) {
                    vs = -1;
                } else {
                    vs = data;
                }
            });
            promises.push(vspromise);
        }
        if (vsesstatisticsdao) {
            statisticspromise = vsesstatisticsdao.getVsStatisticsByIds(ids).success(function (data, code) {
                if (code != 200) {
                    statistics = -1;
                } else {
                    statistics = data;
                }
            });
            promises.push(statisticspromise);
        }

        var temp = {};

        var promise1 = this.$q.all(promises).then(function () {
                if (vs == -1 || statistics == -1) {
                    temp = new SlbException('Api Failed');
                }else{
                    temp = {
                        vs: vs,
                        statistics: statistics
                    };
                }

                return temp;
            }
        );


        var f1 = promise1.then(function (data) {
            var slbIds = [];
            var result = {};

            if(data.message){
                return data;
            }

            if (data.vs && data.vs !== -1 && data.vs['virtual-servers']) {
                result.vses = _.indexBy(data.vs['virtual-servers'], 'id');
            }
            if (data.statistics && data.statistics !== -1 && data.statistics['vs-metas']) {
                result.statistics = _.indexBy(data.statistics['vs-metas'], 'vs-id');
            }
            if (data.vs && data.vs !== -1 && data.vs["virtual-servers"]) {
                data.vs["virtual-servers"].forEach(function (v) {
                    if (v["slb-ids"] && v["slb-ids"].length > 0) {
                        v["slb-ids"].forEach(function (x) {
                            if (slbIds.indexOf(x) < 0) {
                                slbIds.push(x);
                            }
                        })
                    }
                })
            }
            var slbTmpRequest = []
            if (slbsdao) {
                $.each(slbIds, function (i, slb) {
                    slbTmpRequest.push(slbsdao.getSlbById(slb, 'info'));
                });

                return $q.all(slbTmpRequest).then(function (slbDatas) {
                    var slbs = [];
                    _.map(slbDatas, function (d) {
                        slbs.push(d.data);
                    });
                    result.slbs = _.indexBy(slbs, 'id');
                    return result;
                });
            } else {
                return new Promise(function (resolve) {
                    resolve(result);
                    return result;
                });
            }
        });

        return f1.then(function (data) {
            return data;
        })
    }
});