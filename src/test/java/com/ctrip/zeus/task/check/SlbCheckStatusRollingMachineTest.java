package com.ctrip.zeus.task.check;

import com.ctrip.zeus.status.entity.GroupServerStatus;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by zhoumy on 2016/12/6.
 */
public class SlbCheckStatusRollingMachineTest {
    @Test
    public void bitwiseStatus() throws Exception {
        SlbCheckStatusRollingMachine m = new SlbCheckStatusRollingMachine();
        Assert.assertEquals(0, m.bitwiseStatus(new GroupServerStatus().setHealthy(true).setMember(true).setPull(true).setServer(true)));
        Assert.assertEquals(1, m.bitwiseStatus(new GroupServerStatus().setHealthy(false).setMember(true).setPull(true).setServer(true)));
        Assert.assertEquals(1, m.bitwiseStatus(new GroupServerStatus().setHealthy(false).setMember(false).setPull(true).setServer(true)));
    }

}