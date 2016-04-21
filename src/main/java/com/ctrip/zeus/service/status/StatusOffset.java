package com.ctrip.zeus.service.status;

import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;
import org.springframework.stereotype.Service;

/**
 * Created by fanqq on 2015/10/10.
 */
@Service("statusOffset")
public class StatusOffset {
    public static final int MEMBER_OPS = 0 ;
    public static final int PULL_OPS = 1 ;
    public static final int HEALTH_CHECK = 2 ;
    public static final int HEALTHY = 3 ;

    private static DynamicStringProperty defaultStatus = DynamicPropertyFactory.getInstance().getStringProperty("offset.status.default", null);

    public int getDefaultStatus(){
        String defaultStatusString = defaultStatus.get();
        if (defaultStatusString == null){
            return 2;
        }
        int result = 2;//Default Value
        String[] tmp = defaultStatusString.split(";");
        for (String pair : tmp){
            String [] pairArray = pair.split("=");
            if (pairArray.length == 2){
                int offset = Integer.parseInt(pairArray[0]);
                switch (offset){
                    case MEMBER_OPS:
                        if (pairArray[1].equals("false"))
                        {
                            result = result | (1<<MEMBER_OPS);
                        }else if (pairArray[1].equals("true"))
                        {
                            result = result & ~(1<<MEMBER_OPS);
                        }
                        break;
                    case PULL_OPS:
                        if (pairArray[1].equals("false"))
                        {
                            result = result | (1<<PULL_OPS);
                        }else if (pairArray[1].equals("true"))
                        {
                            result = result & ~(1<<PULL_OPS);
                        }
                        break;
                    case HEALTH_CHECK:
                        if (pairArray[1].equals("false"))
                        {
                            result = result | (1<<HEALTH_CHECK);
                        }else if (pairArray[1].equals("true"))
                        {
                            result = result & ~(1<<HEALTH_CHECK);
                        }
                        break;
                    case HEALTHY:
                        if (pairArray[1].equals("false"))
                        {
                            result = result | (1<<HEALTHY);
                        }else if (pairArray[1].equals("true"))
                        {
                            result = result & ~(1<<HEALTHY);
                        }
                        break;
                }
            }
        }
        return result;
    }

}
