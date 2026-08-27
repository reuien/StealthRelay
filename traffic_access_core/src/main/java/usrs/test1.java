package usrs;

import org.apache.commons.lang3.tuple.Pair;
import statistics.StaticticForFederation;
import streamHandling.FederationToken;

import java.util.ArrayList;
import java.util.List;

public class test1 {
    public static void main(String[] args) throws Exception {


        for (int i = 1; i < 61; i++) {
            System.out.print(i+", ");
        }
        System.out.println();
        /*String usr = "Ming";
        *//*long s1 = -3871462595478055209L;
        long s2 = -3982030394752971626L;
        long s3 = -1525112748457737371L;*//*
        long s1 = -7832768410856679542L;
        long s2 = 4630614936012544442L;
        long s3 = -3080625998358705799L;

        ArrayList<Pair<String, Long>> nameAndStreamList = new ArrayList<>();
        nameAndStreamList.add(Pair.of(usr, s1));
        nameAndStreamList.add(Pair.of(usr, s2));
        nameAndStreamList.add(Pair.of(usr, s3));

        *//*long fromTime = 1716897600000L;
        long toTime = 1716901200000L;*//*

        long fromTime = 1716991620000L;
        long toTime = 1716994619000L;

        String usrNameDC = "HONG";  // 用户名
        DataConsumer consumer = new DataConsumer(usrNameDC);
        FederationToken fToken = consumer.getFederationToken(usrNameDC, nameAndStreamList, fromTime, toTime);
        StaticticForFederation res = consumer.getFederationInfo(fToken, nameAndStreamList, fromTime, toTime);
        System.out.println("-----------");
        System.out.println(res.toString());*/





    }
}
