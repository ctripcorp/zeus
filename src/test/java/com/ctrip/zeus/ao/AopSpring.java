package com.ctrip.zeus.ao;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.HashMap;

/**
 * Created by fanqq on 2015/3/26.
 */
@Component
@Aspect
@Order
public class AopSpring {

    private static HashMap<String,Checker> validaters = new HashMap<>();


    @Around("execution(* com.ctrip.zeus.service..*Repository.*(..)) || " +
            "execution(* com.ctrip.zeus.service..*Service.*(..)) ")
    public Object validate(ProceedingJoinPoint point) throws Throwable {
        Object target = point.getTarget();
        String name = point.getSignature().getName();
        String key = target.getClass().getName()+"."+name;
        String simpkey = target.getClass().getSimpleName()+"."+name;


        System.out.println("Started Check: "+key);
        Checker c = validaters.get(key);
        if (c==null)
        {
            c=validaters.get(simpkey);
        }

        Object res = point.proceed();
        if (c!=null)
        {
            c.check();
            System.out.println("End Check: "+key);
        }else {
            System.out.println("Skeped Check : No Checkers for "+key);
        }

        return res;

    }

    public static void setup()
    {
        validaters.clear();
    }

    public static void teardown()
    {
        validaters.clear();
    }

    public static void clear()
    {
        validaters.clear();
    }
    public  static Checker addChecker(String key , Checker checker)
    {
        return validaters.put(key,checker);
    }
    public  static Checker removeChecker(String key){
        return validaters.remove(key);
    }
}
