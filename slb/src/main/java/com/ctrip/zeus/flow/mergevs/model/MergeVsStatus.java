package com.ctrip.zeus.flow.mergevs.model;

public class MergeVsStatus {

    public static final String CREATED = "CREATED";
    public static final String DISABLED = "DISABLED";

    public static final String START_BIND_NEW_VS = "START_BIND_NEW_VS";
    public static final String FINISH_BIND_NEW_VS = "FINISH_BIND_NEW_VS";
    public static final String FAIL_BIND_NEW_VS = "FAIL_BIND_NEW_VS";

    public static final String START_MERGE_VS = "START_MERGE_VS";
    public static final String FINISH_MERGE_VS = "FINISH_MERGE_VS";

    public static final String FAIL_MERGE_VS = "FAIL_MERGE_VS";

    public final static String STEP_DOING = "DOING";
    public final static String STEP_SUCCESS = "SUCCESS";
    public final static String STEP_FAIL = "FAIL";

    public static final String FAIL_CLEAN = "FAIL_CLEAN";
    public static final String FINISH_CLEAN = "FINISH_CLEAN";
    public static final String START_CLEAN = "START_CLEAN";
    public static final String FAIL_ROLLBACK = "FAIL_ROLLBACK";
    public static final String START_ROLLBACK = "START_ROLLBACK";
    public static final String FINISH_ROLLBACK = "FINISH_ROLLBACK";
}
