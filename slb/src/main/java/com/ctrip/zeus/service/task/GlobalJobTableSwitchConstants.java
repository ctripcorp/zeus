package com.ctrip.zeus.service.task;

/**
 * @Discription
 **/
public class GlobalJobTableSwitchConstants {
    public static final String READ_OLD_ENABLED = "db.refactor.global_job.read.old";
    public static final String WRITE_NEW_ENABLED = "db.refactor.global_job.write.new";
    public static final String WRITE_OLD_ENABLED = "db.refactor.global_job.write.old";

    public static final boolean WRITE_OLD_ENABLED_DEFAULT = true;
    public static final boolean WRITE_NEW_ENABLED_DEFAULT = false;
    public static final boolean READ_OLD_ENABLED_DEFAULT = true;
}
