package com.ctrip.zeus.flow.splitvs.model;

public class SplitVsStatus {
    public static final String CREATED = "CREATED";
    public static final String START_BIND_NEW_VS = "START_BIND_NEW_VS";
    public static final String FINISH_BIND_NEW_VS = "FINISH_BIND_NEW_VS";
    public static final String FAIL_BIND_NEW_VS = "FAIL_BIND_NEW_VS";

    public static final String START_SPLIT_VS = "START_SPLIT_VS";
    public static final String FINISH_SPLIT_VS = "FINISH_SPLIT_VS";
    public static final String FAIL_SPLIT_VS = "FAIL_SPLIT_VS";

    public static final String DISABLED = "DISABLED";
    
    public final static String STEP_DOING = "DOING";
    public final static String STEP_SUCCESS = "SUCCESS";
    public final static String STEP_FAIL = "FAIL";
    public static final String FAIL_ROLLBACK = "FAIL_ROLLBACK";
    public static final String FINISH_ROLLBACK = "FINISH_ROLLBACK";
    public static final String START_ROLLBACK = "START_ROLLBACK";
}
