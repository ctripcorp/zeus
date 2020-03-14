package com.ctrip.zeus.service.mail.templet;

import com.ctrip.zeus.service.mail.templet.util.HtmlBuilder;

import java.util.List;

/**
 * Created by fanqq on 2017/4/20.
 */
public class AuthApplyMailTemple {
    private HtmlBuilder htmlBuilder;

    private static AuthApplyMailTemple instance = new AuthApplyMailTemple();

    public AuthApplyMailTemple() {
        htmlBuilder = new HtmlBuilder();
    }

    public static AuthApplyMailTemple getInstance() {
        return instance;
    }

    public String buildApply(String user, String type, List<Long> targetId, String op, String url) {
        StringBuilder sb = new StringBuilder(516);
        htmlBuilder.addTitle(sb, "SLB 权限申请");
        htmlBuilder.startPanel(sb);
        htmlBuilder.setPanelHeader(sb, "SLB权限申请");
        htmlBuilder.startPanelBody(sb);
        htmlBuilder.addLine(sb, "User:" + user);
        htmlBuilder.addLine(sb, "Type:" + type);
        htmlBuilder.addLine(sb, "TargetIds:" + targetId.toString());
        htmlBuilder.addLine(sb, "Ops:" + op);
        htmlBuilder.addLink(sb, new String[]{url, "点击审批"});
        htmlBuilder.endPanelBody(sb);
        htmlBuilder.endPanel(sb);
        return sb.toString();
    }

    public String notifyApplyResult(String user, String type, List<Long> targetId, String op, String approvedBy) {
        StringBuilder sb = new StringBuilder(516);
        htmlBuilder.addTitle(sb, "SLB 权限申请");
        htmlBuilder.startPanel(sb);
        htmlBuilder.setPanelHeader(sb, "SLB权限申请成功");
        htmlBuilder.startPanelBody(sb);
        htmlBuilder.addLine(sb, "User:" + user);
        htmlBuilder.addLine(sb, "Type:" + type);
        htmlBuilder.addLine(sb, "TargetIds:" + targetId.toString());
        htmlBuilder.addLine(sb, "Ops:" + op);
        if (approvedBy != null && !approvedBy.isEmpty()) {
            htmlBuilder.addLine(sb, "ApprovedBy:" + approvedBy);
        }
        htmlBuilder.endPanelBody(sb);
        htmlBuilder.endPanel(sb);
        return sb.toString();
    }
}
