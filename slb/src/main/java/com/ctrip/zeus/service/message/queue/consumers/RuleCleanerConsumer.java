package com.ctrip.zeus.service.message.queue.consumers;

import com.ctrip.zeus.lock.DbLockFactory;
import com.ctrip.zeus.lock.DistLock;
import com.ctrip.zeus.model.queue.Message;
import com.ctrip.zeus.service.message.queue.AbstractConsumer;
import com.ctrip.zeus.service.model.RuleRepository;
import com.ctrip.zeus.service.model.common.RuleTargetType;
import com.netflix.config.DynamicBooleanProperty;
import com.netflix.config.DynamicPropertyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;


/**
 * Created by ygshen on 2018/11/20.
 */
@Service("ruleCleanerConsumer")
public class RuleCleanerConsumer extends AbstractConsumer {

    @Resource
    private DbLockFactory dbLockFactory;

    @Resource
    private RuleRepository ruleRepository;

    private final int timeout = 1000;

    private static Logger logger = LoggerFactory.getLogger(RuleCleanerConsumer.class);

    private DynamicBooleanProperty alwaysTurnOnRuleBoolean = DynamicPropertyFactory.getInstance().getBooleanProperty("always.turn.on.rule", true);

    @Override
    public void onDeleteGroup(List<Message> messages) {
        clearRule(messages, RuleTargetType.GROUP);
    }

    @Override
    public void onDeleteVs(List<Message> messages) {
        clearRule(messages, RuleTargetType.VS);
    }

    @Override
    public void onDeleteSlb(List<Message> messages) {
        clearRule(messages, RuleTargetType.SLB);
    }

    @Override
    public void onDeletePolicy(List<Message> messages) {
        clearRule(messages, RuleTargetType.TRAFFIC_POLICY);
    }

    private void clearRule(List<Message> messages, RuleTargetType targetType) {
        boolean turnOnRuleBoolean = alwaysTurnOnRuleBoolean.getValue();
        if (!turnOnRuleBoolean) return;

        for (Message msg : messages) {
            Long targetId = msg.getTargetId();

            if(targetId==null) return;

            String targetIdString = targetId.toString();
            DistLock lock = dbLockFactory.newLock("deleteRule" + targetType.toString() + "_" + targetId);
            try {
                lock.lock(timeout);
            } catch (Exception e) {
                logger.error("[RuleCleanerConsumer]Failed to retry lock with error message" + e.getMessage());
                return;
            }

            try {
                ruleRepository.removeRulesByTarget(targetType, targetIdString);
            } catch (Exception e) {
                logger.error("[RuleCleanerConsumer]Failed to clean rules related to " + targetType.toString() + ". Error message: " + e.getMessage());
            } finally {
                lock.unlock();
            }
        }
    }
}
