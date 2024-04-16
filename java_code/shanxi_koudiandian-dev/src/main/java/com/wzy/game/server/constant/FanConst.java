package com.wzy.game.server.constant;

import java.util.ArrayList;
import java.util.List;

public class FanConst {

    public static final int[] fourWind = {0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 3, 3, 3, 0, 0, 0};
    public static final int[] empty = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    public static final int[] threeDragon = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 3, 3};
    public static final int[] nine = {3, 1, 1, 1, 1, 1, 1, 1, 3, 0, 0, 0, 0, 0, 0, 0};
    public static final int[] baiwandan = {0, 0, 0, 0, 0, 2, 4, 4, 4, 0, 0, 0, 0, 0, 0, 0};
    public static final int[] fourKong = {4, 4, 4, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    public static final int[] lianSevenPairs = {2, 2, 2, 2, 2, 2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    //64
    public static final int[] smallFourWind = {0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 3, 3, 3, 0, 0, 0};
    public static final int[] allZi = {0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 3, 3, 3, 2, 0, 0};
    public static final int[] fourAnKe = {3, 3, 3, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    public static final int[] twoDragon = {2, 2, 2, 0, 2, 0, 2, 2, 2, 0, 0, 0, 0, 0, 0, 0};

    //48
    public static final int[] fourSameShun = {4, 4, 4, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    public static final int[] fourLianKe = {3, 3, 3, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

    //32
    public static final int[] fourLianShun = {1, 2, 2, 2, 2, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    public static final int[] ThreeKong = {4, 4, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    public static final int[] hunYaoJiuH = {3, 0, 0, 0, 0, 0, 0, 0, 3, 3, 3, 3, 3, 3, 3, 3};
    //24
    public static final int[] sevenPair = {2, 2, 2, 2, 2, 2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    public static final int[] allWan =  {0, 0, 0, 0, 0, 0, 0, 0, 0, -1, -1, -1, -1, -1, -1, -1};
    public static final int[] threeLianShun =  {1, 1, 2, 1, 2, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    public static final int[] threeLianKe =  {3, 3, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    //16
    public static final int[] lianNine =  {1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0};
    public static final int[] threeSameShun =  {3, 3, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    public static final int[] threeAnKe =  {3, 3, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};



    //
    public static final List<List<Integer>> fourWind(){
        List<List<Integer>> all = new ArrayList<>();
        List<Integer> one = new ArrayList<>();
        for(int i =0;i<fourWind.length;i++){
            one.add(fourWind[i]);
        }
        return  all;
    }



    public static final List<List<Integer>> threeDragon(){
        List<List<Integer>> all = new ArrayList<>();
        List<Integer> one = new ArrayList<>();
        for(int i =0;i<threeDragon.length;i++){
            one.add(threeDragon[i]);
        }
        return  all;
    }

    public static final List<List<Integer>> smallFourWind(){
        List<List<Integer>> all = new ArrayList<>();
        for (int j = 0; j < 4; j++) {
            List<Integer> one = new ArrayList<>();

            for(int i =0;i<fourWind.length;i++){
                if(i==9+j){
                    one.add(2);
                }else {
                    one.add(fourWind[i]);
                }
            }
            all.add(one);
        }
        return  all;
    }
    public static final List<List<Integer>> smallThreeDragon(){
        List<List<Integer>> all = new ArrayList<>();
        for (int j = 0; j < 4; j++) {
            List<Integer> one = new ArrayList<>();

            for(int i =0;i<threeDragon.length;i++){
                if(i==13+j){
                    one.add(2);
                }else {
                    one.add(threeDragon[i]);
                }
            }
            all.add(one);
        }
        return  all;
    }

    public static final List<List<Integer>> nine(){
        List<List<Integer>> all = new ArrayList<>();
        for(int i =0;i<9;i++){
            List<Integer> one = new ArrayList<>();
            for (int j = 0; j < nine.length; j++) {
                if(j==i){
                    one.add(nine[j]+1);
                }else{
                    one.add(nine[j]);
                }
            }
            all.add(one);
        }
        return all;
    }
    public static final List<List<Integer>> baiWanDan(){
        List<List<Integer>> all = new ArrayList<>();
        boolean moreThan100 = true;
        while (moreThan100){
            int count =0;
            for (int i = 0; i < 9; i++) {
                count = count+(i+1)*baiwandan[i];
            }
            if(count>90) {
                List<Integer> one = new ArrayList<>();
                int max =0;
                int min =0;
                for (int i = 0; i < 16; i++) {
                    if(baiwandan[i]>0){
                        if(max+min==0){
                            if(baiwandan[i]<4) {
                                min = i;
                            }else{
                                min = i-1;
                            }
                        }
                        max = i;

                    }
                    one.add(baiwandan[i]);
                }
                baiwandan[max]=baiwandan[max]-1;
                baiwandan[min]=baiwandan[min]+1;
                all.add(one);
            }else{
                moreThan100=false;
                break;
            }
        }
        return  all;
    }
    //暂时不使用
    public static final List<List<Integer>> fourKong(){
        List<List<Integer>> all = new ArrayList<>();
        for(int i =0;i<13;i++){
            List<Integer> one = new ArrayList<>();
            for (int j = 0; j < empty.length; j++) {
                if(j==i){
                    one.add(nine[j]+1);
                }else{
                    one.add(nine[j]);
                }
            }
            all.add(one);
        }
        return all;
    }
    //64
    public static final List<List<Integer>> lianSevenPair(){
        List<List<Integer>> all = new ArrayList<>();
        for(int i =0;i<3;i++){
            List<Integer> one = new ArrayList<>();
            for (int j = 0; j < empty.length; j++) {
                if(j>=i&&j<9){
                    one.add(2);
                }else{
                    one.add(0);
                }
            }
            all.add(one);
        }
        return all;
    }
    public static final List<List<Integer>> allZi(){
        List<List<Integer>> all = new ArrayList<>();
        for(int i =0;i<3;i++){
            List<Integer> one = new ArrayList<>();
            for (int j = 0; j < empty.length; j++) {
                if(j>=i&&j<9){
                    one.add(2);
                }else{
                    one.add(0);
                }
            }
            all.add(one);
        }
        return all;
    }
    public static final List<List<Integer>> twoDragonMeet(){
        List<List<Integer>> all = new ArrayList<>();
            List<Integer> one = new ArrayList<>();
            for (int j = 0; j < twoDragon.length; j++) {
                    one.add(twoDragon[j]);
            }
        return all;
    }

    public static final List<List<Integer>> fourSameShun(){
        List<List<Integer>> all = new ArrayList<>();
        for(int i =0;i<7;i++){
            List<Integer> one = new ArrayList<>();
            for (int j = 0; j < empty.length; j++) {
                if(j>=i&&j<i+3){
                    one.add(4);
                }else{
                    one.add(0);
                }
            }
            all.add(one);
        }
        return all;
    }
    public static final List<List<Integer>> fourLianKe(){
        List<List<Integer>> all = new ArrayList<>();
        for(int i =0;i<6;i++){
            List<Integer> one = new ArrayList<>();
            for (int j = 0; j < empty.length; j++) {
                if(j>=i&&j<i+4){
                    one.add(3);
                }else{
                    one.add(0);
                }
            }
            all.add(one);
        }
        return all;
    }
    public static final List<List<Integer>> fourLianShun(){
        List<List<Integer>> all = new ArrayList<>();
            for(int i =0;i<4;i++){
                List<Integer> one = new ArrayList<>(16);
                for (int j = 0; j < empty.length-6; j++) {
                    if(j==i){
                        one.add(1);
                        one.add(2);
                        one.add(3);
                        one.add(3);
                        one.add(2);
                        one.add(1);
                    }else{
                        one.add(0);
                    }

                }
                all.add(one);
            }
        for(int i =0;i<1;i++){
            List<Integer> one = new ArrayList<>(16);
            for (int j = 0; j < empty.length-8; j++) {
                if(j==i){
                    one.add(1);
                    one.add(1);
                    one.add(2);
                    one.add(1);
                    one.add(2);
                    one.add(1);
                    one.add(2);
                    one.add(1);
                    one.add(1);
                }else{
                    one.add(0);
                }

            }
            all.add(one);
        }
        return all;
    }
    public static final List<List<Integer>> threeSameShun(){
        List<List<Integer>> all = new ArrayList<>();
        for(int i =0;i<7;i++){
            List<Integer> one = new ArrayList<>();
            for (int j = 0; j < empty.length; j++) {
                if(j>=i&&j<i+3){
                    one.add(3);
                }else{
                    one.add(0);
                }
            }
            all.add(one);
        }
        return all;
    }
    public static final List<List<Integer>> threeLianKe(){
        List<List<Integer>> all = new ArrayList<>();
        for(int i =0;i<7;i++){
            List<Integer> one = new ArrayList<>();
            for (int j = 0; j < empty.length; j++) {
                if(j>=i&&j<i+3){
                    one.add(3);
                }else{
                    one.add(0);
                }
            }
            all.add(one);
        }
        return all;
    }

    /**
     * 清龙
     * @return
     */
    public static final List<List<Integer>> threeLianShun(){
        List<List<Integer>> all = new ArrayList<>();
        for(int i =0;i<5;i++){
            List<Integer> one = new ArrayList<>(16);
            for (int j = 0; j < empty.length-5; j++) {
                if(j==i){
                    one.add(1);
                    one.add(2);
                    one.add(3);
                    one.add(2);
                    one.add(1);
                }else{
                    one.add(0);
                }

            }
            all.add(one);
        }
        for(int i =0;i<4;i++){
            List<Integer> one = new ArrayList<>(16);
            for (int j = 0; j < empty.length-7; j++) {
                if(j==i){
                    one.add(1);
                    one.add(1);
                    one.add(2);
                    one.add(1);
                    one.add(2);
                    one.add(1);
                    one.add(1);
                }else{
                    one.add(0);
                }

            }
            all.add(one);
        }
        for(int i =0;i<1;i++){
            List<Integer> one = new ArrayList<>(16);
            for (int j = 0; j < empty.length-9; j++) {
                if(j==i){
                    one.add(1);
                    one.add(1);
                    one.add(1);
                    one.add(1);
                    one.add(1);
                    one.add(1);
                    one.add(1);
                    one.add(1);
                    one.add(1);
                }else{
                    one.add(0);
                }

            }
            all.add(one);
        }
        return all;
    }

    public static void main(String[] args) {
        System.out.println(baiWanDan());
        System.out.println(nine());
        System.out.println(lianSevenPair());
        System.out.println(fourLianKe());
        System.out.println(fourLianShun());
    }
}
