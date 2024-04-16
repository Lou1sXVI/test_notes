package com.wzy.game.server.util;

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;

import java.util.ArrayList;
import java.util.List;

public class CardAdapter {
    public static String CARDSTR[] = {
            "1w", "2w", "3w", "4w", "5w", "6w", "7w", "8w", "9w",
            "1t", "2t", "3t", "4t", "5t", "6t", "7t", "8t", "9t",
            "1b", "2b", "3b", "4b", "5b", "6b", "7b", "8b", "9b",
            "Ef","Sf","Wf","Nf",
            "Zj","Fj","Bj",
            "F1","F2","F3","F4","F5","F6","F7","F8"
    };

    public static int CardHex[] = {
            0x001, 0x011, 0x021, 0x031, 0x041, 0x051 , 0x061, 0x071, 0x081,
            0x101,0x111,0x121,0x131,0x141,0x151,0x161,0x171,0x181,
            0x201,0x211,0x221,0x231,0x241,0x251,0x261,0x271,0x281,
            0x301,0x311,0x321,0x331,
            0x401,0x411,0x421
    };

    public static BidiMap<Integer,String> cardTransverter;//key 为C编码，value 为java编码

    public static BidiMap<Integer,String> cardTransverterForStand;//key 为C编码，value 为java编码
    public static BidiMap<Integer,String> cardTransHex;//转为16进制
    static {
        if(cardTransverter == null){
            cardTransverter = new DualHashBidiMap<>();
        }

        for(int i=0;i<CARDSTR.length;i++){
            cardTransverter.put(i,CARDSTR[i].toUpperCase());
        }
        if(cardTransHex == null){
            cardTransHex = new DualHashBidiMap<>();
        }

        for(int i=0;i<CardHex.length;i++){
            cardTransHex.put(CardHex[i],CARDSTR[i].toUpperCase());
        }

        if(cardTransverterForStand == null){
            cardTransverterForStand = new DualHashBidiMap<>();
        }
        for(int i=0;i<CARDSTR.length;i++){
            cardTransverterForStand.put(i,CARDSTR[i]);
        }
    }



    public static List<Integer> codeFromStr(String str){
        if(str == null || str.equals("")){
            return new ArrayList<>();
        }
        List<Integer> ret = new ArrayList<>();
        int i = 0;
        int size = str.length();
        while((i+2) <= size){
            int code =cardTransverter.getKey(str.substring(i,i+2).toUpperCase());
            ret.add(code);
            i = i+2;
        }
        return ret;
    }

    public static void main(String[] args) {
        System.out.println(cardTransverter);
    }




}
