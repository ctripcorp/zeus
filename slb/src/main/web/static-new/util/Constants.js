var constants = {

    slbCreationStatusMap: {
        'CREATED': '已创建',
        'START_CREATE_CMS_GROUP': '资源申请',
        'FAIL_CREATE_CMS_GROUP': '资源申请',
        "FINISH_CREATE_CMS_GROUP": '资源申请',
        'START_EXPANSION': '资源申请',
        'DOING_EXPANSION': '资源申请',
        'FINISH_EXPANSION': '资源申请',
        'FAIL_EXPANSION': '资源申请',
        'START_CREATE_CMS_ROUTE': '资源申请',
        'FINISH_CREATE_CMS_ROUTE': '资源申请',
        'FAIL_CREATE_CMS_ROUTE': '资源申请',
        'START_DEPLOYMENT': '资源申请',
        'DEPLOYING': '资源申请',
        'FINISH_DEPLOYMENT': '资源申请',
        'FAIL_DEPLOYMENT': '资源申请',
        'START_CREATE_SLB': '资源申请',
        'FINISH_CREATE_SLB': '成功创建',
        'FAIL_CREATE_SLB': '资源申请'
    },
    slbShardingStatusMap: {
        'STATUS_CREATED': 'SLB拆分已创建',
        'STATUS_CREATING_SLB': '正在创建SLB',
        'STATUS_FINISH_CREATING_SLB': 'SLB创建完成',
        'STATUS_VS_MIGRATION': '正在迁移VS',
        'STATUS_FINISHED': '拆分完成'
    },
    vsMigrationStatus: {
        'preCreate': '已创建',
        'new': '待发布',
        'test': '正在测试',
        'dns': 'DNS切换中',
        'monitor': '持续监控',
        'clean': '配置待清理',
        'delete': '迁移待结束',
        'done': '已完成'
    },
    vsMergeStatus: {
        FINISH_MERGE_VS: '完成待清理',
        START_MERGE_VS: '开始',
        FAIL_MERGE_VS: '失败',
        FINISH_BIND_NEW_VS: '进行中',
        FAIL_BIND_NEW_VS: '失败',
        FAIL_BIND_NEW_VS: '失败',
        FINISH_ROLLBACK: '回退成功',
        FINISH_CLEAN: '完成清理',
        CREATED: '创建成功',
        START_BIND_NEW_VS: '创建新的VS',
        START_ROLLBACK: '正在回退'
    },
    vsSplitStatus: {
        CREATED: '已创建',
        START_BIND_NEW_VS: '进行中',
        FINISH_BIND_NEW_VS: '完成绑定',
        FAIL_BIND_NEW_VS: '绑定失败',
        CREATED: '创建成功',
        FINISH_ROLLBACK: '回退成功',
        START_ROLLBACK: '正在回退',
        START_SPLIT_VS: '进行中',
        FAIL_SPLIT_VS: '拆分失败',
        FINISH_SPLIT_VS: '完成拆分'
    },
    idcMapping: {
        金桥: 'SHAJQ',
        欧阳: 'SHAOY',
        南通: 'NTGXH',
        福泉: 'SHAFQ',
        金钟: 'SHAJZ',
        八楼欧阳: 'SHAOYN',
        SHARB:'SHARB',
        日阪:'SHARB'
    },
    statusMapping: {
        activated: '已激活',
        deactivated: '未激活',
        tobeactivated: '有变更'
    },
    statusClassMapping: {
        activated: 'status-green',
        deactivated: 'status-red',
        tobeactivated: 'status-yellow'
    },
    idcMapping2: {
        'SHAJQ': '金桥',
        'SHAOY': '欧阳',
        'NTGXH': '南通',
        'SHAFQ': '福泉',
        'SHAJZ': '金钟',
        SHAOYN: '欧阳八楼',
        'SHARB':'日阪'
    },
    appTypeMapping: {
        service: '服务',
        application: '应用'
    },
    shardingColor: {
        成功创建: 'status-green',
        资源申请: 'status-yellow',
        未开始: 'status-gray',
        已完成: 'status-green',
        SLB拆分已创建: 'status-gray',
        SLB创建完成: 'status-green',
        正在创建SLB: 'status-yellow',
        正在迁移VS: 'status-yellow',
        拆分完成: 'status-green'
    }
}
