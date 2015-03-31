package com.ctrip.zeus.util;

/**
 * Created by fanqq on 2015/3/24.
 */
public class StringFormat {
    public static String format(String data)
    {
        StringBuilder sb = new StringBuilder(2*data.length());
        int tabcount = 0 ;
        int tmp_i = 0;

        for (int i = 0 ; i < data.length() ; i ++)
        {
            if (data.charAt(i)=='{'){
                tabcount++;
            }else if (data.charAt(i)=='}'){
                tabcount--;
            }
            if (data.charAt(i)=='\n'){
                sb.append(data.substring(tmp_i,i)).append(getTabByCount(tabcount));
                tmp_i=i+1;
            }

        }
        return sb.toString();
    }

    private static String getTabByCount(int count){
        StringBuilder sb = new StringBuilder(64);
        for (int i = 0 ; i < count ; i++)
        {
            sb.append("    ");
        }
        return sb.toString();
    }
}
