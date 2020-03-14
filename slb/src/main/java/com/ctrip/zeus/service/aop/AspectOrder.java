package com.ctrip.zeus.service.aop;

/**
 * Created by zhoumy on 2015/7/8.
 */

/**
 * What happens when multiple pieces of advice all want to run at the same join point?
 * Spring AOP follows the same precedence rules as AspectJ to determine the order of advice execution.
 * The highest precedence advice runs first "on the way in" (so given two pieces of before advice,
 * the one with highest precedence runs first).
 * "On the way out" from a join point, the highest precedence advice runs last
 * (so given two pieces of after advice, the one with the highest precedence will run second).
 */
public class AspectOrder {
    public static int InterceptException = 0;
    public static int Trace = 1500;
    public static int Transaction = 1000;
    public static int CatReport = 3;
    public static int DescriptionIntercept = 1;
    public static int AuthAutoFill = 120;
}
