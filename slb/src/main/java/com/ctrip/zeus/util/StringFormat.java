package com.ctrip.zeus.util;

/**
 * Created by fanqq on 2015/3/24.
 */
public class StringFormat {
    public static String format(String data)
    {
        StringBuilder sb = new StringBuilder(2*data.length());
        int tabCount = 0 ;
        int tmpI = 0;

        for (int i = 0 ; i < data.length() ; i ++)
        {
            if (data.charAt(i)=='{'){
                tabCount++;
            }else if (data.charAt(i)=='}'){
                tabCount--;
            }
            if (data.charAt(i)=='\n'){
                sb.append(data.substring(tmpI,i)).append('\n').append(getTabByCount(tabCount));
                tmpI=i+1;
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
