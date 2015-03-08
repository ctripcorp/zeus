package com.ctrip.zeus.nginx;

import com.ctrip.zeus.model.entity.Slb;
import org.junit.Test;

import static org.junit.Assert.*;

public class SlbConfGeneratorTest {

    @Test
    public void testGenerate() throws Exception {
        System.out.println(new SlbConfGenerator().generate(new Slb().setNginxWorkerProcesses(2)));
    }
}