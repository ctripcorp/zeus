//package test;
//
//import com.ctrip.zeus.model.entity.GroupServerStatus;
//import com.ctrip.zeus.model.entity.GroupStatus;
//import com.ctrip.zeus.model.entity.ServerStatus;
//
///**
// * @author:xingchaowang
// * @date: 3/19/2015.
// */
//public class Demo2 {
//
//    public static void main(String[] args) {
//        GroupStatus s = new GroupStatus().setGroupName("html5Home")
//                .addGroupServerStatus(new GroupServerStatus()
//                        .setIp("192.168.1.1")
//                        .setServer(true).setMember(true).setUp(true))
//                .addGroupServerStatus(new GroupServerStatus()
//                        .setIp("192.168.1.2")
//                        .setServer(true).setMember(true).setUp(true));
//
//
//        ServerStatus s1 = new ServerStatus()
//                .setIp("192.168.1.1").setUp(true)
//                .addGroupName("html5Home")
//                .addGroupName("html5Hotel")
//                .addGroupName("html5Flight");
//
//        System.out.println(String.format(GroupStatus.JSON, s));
//        System.out.println(String.format(GroupStatus.JSON, s1));
//    }
//}
