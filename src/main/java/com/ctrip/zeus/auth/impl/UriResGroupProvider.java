package com.ctrip.zeus.auth.impl;

import com.ctrip.zeus.auth.Authorize;
import com.ctrip.zeus.auth.ResourceGroupProvider;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Path;
import java.lang.reflect.Method;

/**
 * User: mag
 * Date: 4/22/2015
 * Time: 2:51 PM
 */
public class UriResGroupProvider implements ResourceGroupProvider {
    @Override
    public String provideResourceGroup(Method method, HttpServletRequest request) {
        if (request == null){
            return null;
        }
        int groupHint = getGroupHintFromMethod(method);
        String uriTemplate = getTemplateFromMethod(method);

        String uri = request.getRequestURI();
        String[] uriParts = uri.split("/");
        if (uriParts.length < groupHint || groupHint < 0){
            return null;
        }
        if (groupHint == 0){
            return findGroupFromTemplate(uri, uriTemplate);
        }

        return uriParts[groupHint];
    }

    private String getTemplateFromMethod(Method method) {
        Class declaredClass = method.getDeclaringClass();
        Path pathAnno = method.getAnnotation(Path.class);
        Path classPathAnno = (Path)declaredClass.getAnnotation(Path.class);

        if (classPathAnno == null || pathAnno == null){
            return null;
        }
        return classPathAnno.value() + pathAnno.value();
    }

    private int getGroupHintFromMethod(Method method) {
        Authorize authorize = method.getAnnotation(Authorize.class);
        if (authorize == null){
            return -1;
        }
        return authorize.uriGroupHint();
    }


    private String findGroupFromTemplate(String uri, String uriTemplate) {
        if (uriTemplate == null || uriTemplate.isEmpty()
                || uri == null || uri.isEmpty()) {
            return null;
        }

        int bracketStart = uriTemplate.indexOf("{");
        if (bracketStart == -1){
            return null;
        }
        String templPrefix = uriTemplate.substring(0, bracketStart);
        int uriTemplIdx = uri.indexOf(templPrefix);
        if (uriTemplIdx == -1){
            return null;
        }
        int groupStart = uriTemplIdx + templPrefix.length();

        StringBuffer groupBuf = new StringBuffer();
        for (int i = groupStart;i<uri.length();i++){
            char gChar = uri.charAt(i);
            if (gChar == '/'){
                break;
            }
            groupBuf.append(gChar);
        }
        return groupBuf.toString();
    }

    public static void main(String[] args) {
        UriResGroupProvider provider = new UriResGroupProvider();
        String group = provider.findGroupFromTemplate("/api/app/app12345","/app/{appName:[a-zA-Z0-9_-]+}");
        System.out.println(group);
    }
}
