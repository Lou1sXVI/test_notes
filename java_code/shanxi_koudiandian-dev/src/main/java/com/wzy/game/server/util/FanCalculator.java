package com.wzy.game.server.util;

import com.wzy.game.server.ai.Group;
import com.wzy.game.server.ai.ParamMode;
import com.wzy.game.server.model.FanInfo;
import com.wzy.game.server.model.HuFanInfo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

import static com.wzy.game.server.util.CodeUtil.getTileCode;

public class FanCalculator {
    /* 1表示可以有,2表示必须有,3表示必须有刻子,4表示有杠,5表示所有为5的有其中一个;6表示有两个，7表示有3个
        0-13 十三太保->全中
     */
    public static long[] fanPoints = {100000};
    public static List<HuFanInfo> fanInfoAllList = loadFanDetail();

    private final static String[] fanList = {"混幺九", "小三元", "小四喜", "大三元", "大四喜", "绿一色", "紫气东来", "全中"};
    private static final int[][] fanType = {


            {//混幺九21
                    1, 0, 0, 0, 0, 0, 0, 0, 1,// 万
                    1, 0, 0, 0, 0, 0, 0, 0, 1,// 条
                    1, 0, 0, 0, 0, 0, 0, 0, 1,// 饼
                    1, 1, 1, 1, 1, 1, 1
            },
            {//小三元23
                    1, 1, 1, 1, 1, 1, 1, 1, 1,// 万
                    1, 1, 1, 1, 1, 1, 1, 1, 1,// 条
                    1, 1, 1, 1, 1, 1, 1, 1, 1,// 饼
                    1, 1, 1, 1, 2, 2, 2
            },
            {//小四喜22
                    1, 1, 1, 1, 1, 1, 1, 1, 1,// 万
                    1, 1, 1, 1, 1, 1, 1, 1, 1,// 条
                    1, 1, 1, 1, 1, 1, 1, 1, 1,// 饼
                    2, 2, 2, 2, 1, 1, 1
            },

            {//大三元
                    1, 1, 1, 1, 1, 1, 1, 1, 1// 万
                    , 1, 1, 1, 1, 1, 1, 1, 1, 1// 条
                    , 1, 1, 1, 1, 1, 1, 1, 1, 1// 饼
                    , 1, 1, 1, 1, 3, 3, 3
            },
            {//大四喜
                    1, 1, 1, 1, 1, 1, 1, 1, 1// 万
                    , 1, 1, 1, 1, 1, 1, 1, 1, 1// 条
                    , 1, 1, 1, 1, 1, 1, 1, 1, 1// 饼
                    , 3, 3, 3, 3, 1, 1, 1
            },
            {//混幺九21
                    1, 0, 0, 0, 0, 0, 0, 0, 1,// 万
                    1, 0, 0, 0, 0, 0, 0, 0, 1,// 条
                    1, 0, 0, 0, 0, 0, 0, 0, 1,// 饼
                    1, 1, 1, 1, 1, 1, 1
            },
            {//大四喜
                    0, 0, 0, 0, 0, 0, 0, 0, 0,// 万
                    0, 0, 0, 0, 0, 0, 0, 0, 0,// 万
                    0, 0, 0, 0, 0, 0, 0, 0, 0,// 万
                    1, 1, 1, 1, 1, 1, 1
            }
    };

    private static final int[] mainFanIndex = {
            0, //混幺九
            1, //小三
            2, //小四
            3, //大三
            4, //大四
            5, //131
            6, //字一色

    };

    public static HuFanInfo findNearest() {
        return new HuFanInfo();
    }

    public static List<HuFanInfo> findNearest(ParamMode act, int[] hand) {
//        List<FanInfo> fanInfoList = new ArrayList<>();
        List<HuFanInfo> fanInfoList = new ArrayList<>();

        int[] handCopy = hand.clone();
        List<Group> groups = act.getMyGroups();
        List<Integer> groupIndex = new ArrayList<>();
        for (int g = 0; g < groups.size(); g++) {
            Group group = groups.get(g);
            List<Integer> cards = group.getCards();
            for (int c = 0; c < Math.min(cards.size(), 4); c++) {
                handCopy[cards.get(c)]++;
            }
            groupIndex.add(cards.get(0));
        }

        for (int index : mainFanIndex) {
            int[] curFan = fanType[index];
            FanTiles ft = FanTiles.fanArrayToFanTiles(curFan);
            Map<Integer, Integer> keyTilesNum = new HashMap<>();
            Map<Integer, Integer> usefulTilesNum = new HashMap<>();
            for (int i = 0; i < 34; i++) {
                if (handCopy[i] > 0) {
                    if (ft.isKeyTile(i)) {
                        keyTilesNum.put(i, handCopy[i]);
                    } else if (ft.isUsefulTile(i)) {
                        usefulTilesNum.put(i, handCopy[i]);
                    }
                }
            }
//            if(index==14){
//                int baiHuaCount=0;
//                int tileCount =0;
//                for (int i = 0; i < 27; i++) {
//                    baiHuaCount+=handCopy[i]*(i%9+1);
//                    tileCount++;
//                }
//                if(baiHuaCount<90){
//                    continue;
//                }
//            }
            int keyless = 0;
            Set<Integer> keyTiles = ft.getKeyTiles();
            for (Integer key : keyTiles) {
                int n = keyTilesNum.getOrDefault(key, 0);
                if (n < 3) {
                    keyless += (3 - n);
                }
            }
            int dis = keyless;
            List<Map.Entry<Integer, Integer>> sortedUseFul =
                    usefulTilesNum.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                            .collect(Collectors.toList());
            int uNum = Math.min(ft.usefulNum(), sortedUseFul.size());
            for (Map.Entry<Integer, Integer> e : sortedUseFul.subList(0, uNum)) {
                if (e.getValue() < 3) {
                    dis += (3 - e.getValue());
                }
            }
            if (ft.usefulNum() > sortedUseFul.size()) {
                dis += 3 * (ft.usefulNum() - sortedUseFul.size());
            }
            dis--;
            if (index == 8) {
                int countWindType = 0;
                int windMax = 0;
                int windTotal = 0;
                int windMaxType = -1;
                for (Map.Entry<Integer, Integer> e : sortedUseFul.subList(0, uNum)) {
                    if (e.getKey() > 26 && e.getKey() < 34) {
                        windTotal += e.getValue();
                        countWindType++;
                        if (e.getValue() > windMax) {
                            windMax = e.getValue();
                            windMaxType = e.getKey();
                        }
                    }
                }
                if (countWindType > 1) {
                    dis = dis + (windTotal - windMax);
                    int finalWindMaxType = windMaxType;
                    sortedUseFul.removeIf(e ->
                            (e.getKey() > 26 && e.getKey() < 34 && e.getKey().compareTo(finalWindMaxType) != 0));
                }
            }
            HuFanInfo fanInfo = fanInfoAllList.get(index);
            fanInfo.setDistance(dis);
            fanInfo.setKeyLess(keyless);
            fanInfo.setKeyTiles(new ArrayList<>(keyTilesNum.keySet()));
            fanInfo.setUselessTiles(new ArrayList<>(usefulTilesNum.keySet()));
            fanInfoList.add(fanInfo);
        }

//        for (int i = 0; i < fanType.length; i++) {
//            List<Integer> keyTiles = new ArrayList<>();
//            List<Integer> useless = new ArrayList<>();
//            int[] fanThis = fanType[i];
//            int distance = 14;
//            int keyLess = 0;
//            boolean haveCount5 = false;
//            for (int j = 0; j < fanThis.length; j++) {
//                switch (fanThis[j]) {
//                    case 0:
//                        if (handCopy[j] > 0) {
////                            distance= distance+handCopy[j];
//                            useless.add(j);
//                            if (groupIndex.contains(j)) {
//                                distance = 100;
//                            }
//                        }
//                        break;
//                    case 1:
//                        distance -= handCopy[j];
//                        break;
//                    case 2:
//                        if (handCopy[j] == 0) {
//                            keyLess++;
//                        }
//                        distance -= handCopy[j];
//                        keyTiles.add(j);
//                        break;
//                    case 3:
//                        if (handCopy[j] == 0) {
//                            keyLess = keyLess + 1;
//                        }
//                        distance -= handCopy[j];
//                        break;
//                    case 5:
//                        if (!haveCount5) {
//                            if (handCopy[27] + handCopy[28] + handCopy[28] + handCopy[29] == 0) {
//                                keyLess++;
//                            }
//                            distance -= Math.max(handCopy[27], Math.max(handCopy[28], Math.max(handCopy[29], handCopy[30])));
//                            haveCount5 = true;
//                        }
//                        break;
//
//                }
//            }
//            if (distance < 4 && keyLess < 2) {
//                FanInfo fanInfo = new FanInfo();
//               // System.out.println(fanInfoAllList.get(i).getFanName());
//                fanInfo=fanInfoAllList.get(i);
//                fanInfo.setDistance(distance);
//                fanInfo.setKeyLess(keyLess);
//                fanInfo.setKeyTiles(keyTiles);
//                fanInfo.setUselessTiles(useless);
//                fanInfoList.add(fanInfo);
//            }
//        }


        //check 131
        if (groups.isEmpty()) {
            int[] curFan = fanType[0];
            List<Integer> key131 = new ArrayList<>();
            for (int i = 0; i < 34; i++) {
                if (curFan[i] > 0 && handCopy[i] > 0) {
                    key131.add(i);
                }
            }
            HuFanInfo fanInfo = fanInfoAllList.get(0);
            fanInfo.setDistance(13 - key131.size());
            fanInfo.setKeyLess(13 - key131.size());
            fanInfo.setKeyTiles(key131);
            fanInfo.setUselessTiles(Collections.emptyList());
//            if(fanInfo.getDistance()-act.getHunNum()<4&&fanInfo.getKeyLess()-act.getHunNum()<2){
            fanInfoList.add(fanInfo);
//            }
        }
        fanInfoList.sort(new Comparator<HuFanInfo>() {
            @Override
            public int compare(HuFanInfo o1, HuFanInfo o2) {
                int disDiff = o1.getDistance() - o2.getDistance();
                if (disDiff != 0) return disDiff;
                return o1.getKeyLess() - o2.getKeyLess();
            }
        });

        return fanInfoList;
    }


    public static List<HuFanInfo> findNearestWithWd(ParamMode act, int[] hand, List<Integer> canDropBeWinAll) {
//        List<FanInfo> fanInfoList = new ArrayList<>();
        List<HuFanInfo> fanInfoList = new ArrayList<>();

        int[] handCopy = hand.clone();
        List<Group> groups = act.getMyGroups();
        List<Integer> groupIndex = new ArrayList<>();
        for (int g = 0; g < groups.size(); g++) {
            Group group = groups.get(g);
            List<Integer> cards = group.getCards();
            for (int c = 0; c < Math.min(cards.size(), 4); c++) {
                handCopy[cards.get(c)]++;
            }
            groupIndex.add(cards.get(0));
        }

        for (int index : mainFanIndex) {
            int[] curFan = fanType[index];
            FanTiles ft = FanTiles.fanArrayToFanTiles(curFan);
            Map<Integer, Integer> keyTilesNum = new HashMap<>();
            Map<Integer, Integer> usefulTilesNum = new HashMap<>();
            for (int i = 0; i < 34; i++) {
                if (handCopy[i] > 0) {
                    if (ft.isKeyTile(i)) {
                        keyTilesNum.put(i, handCopy[i]);
                    } else if (ft.isUsefulTile(i)) {
                        usefulTilesNum.put(i, handCopy[i]);
                    }
                }
            }
            int keyless = 0;
            Set<Integer> keyTiles = ft.getKeyTiles();
            for (Integer key : keyTiles) {
                int n = keyTilesNum.getOrDefault(key, 0);
                if (n < 3) {
                    keyless += (3 - n);
                }
            }
            int dis = keyless;
            List<Map.Entry<Integer, Integer>> sortedUseFul =
                    usefulTilesNum.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                            .collect(Collectors.toList());
            int uNum = Math.min(ft.usefulNum(), sortedUseFul.size());
            for (Map.Entry<Integer, Integer> e : sortedUseFul.subList(0, uNum)) {
                if (e.getValue() < 3) {
                    dis += (3 - e.getValue());
                }
            }
            if (ft.usefulNum() > sortedUseFul.size()) {
                dis += 3 * (ft.usefulNum() - sortedUseFul.size());
            }
            dis--;
            if (index == 8) {
                int countWindType = 0;
                int windMax = 0;
                int windTotal = 0;
                int windMaxType = -1;
                for (Map.Entry<Integer, Integer> e : sortedUseFul) {
                    if (e.getKey() > 26 && e.getKey() < 31) {
                        windTotal += e.getValue();
                        countWindType++;
                        if (e.getValue() > windMax) {
                            windMax = e.getValue();
                            windMaxType = e.getKey();
                        }
                    }
                }
                if (countWindType > 1) {
                    dis = dis + (windTotal - windMax);
                    int finalWindMaxType = windMaxType;
                    sortedUseFul.removeIf(e ->
                            (e.getKey() > 26 && e.getKey() < 31 && e.getKey() != finalWindMaxType));
                    for (int i = 27; i < 31; i++) {
                        if (usefulTilesNum.containsKey(i) && i != windMaxType) {
                            usefulTilesNum.remove(i);
                        }
                    }
                }
            }
            HuFanInfo fanInfo = fanInfoAllList.get(index);

            if (canDropBeWinAll.size() > 0 && dis - act.getHunNum() <= 1) {
                int[] tilesUsefulness = new int[34];
                List<Integer> useLess = new ArrayList<>(usefulTilesNum.keySet());
                for (int i = 0; i < 34; i++) {
                    if (keyTiles.contains(i)) {
                        tilesUsefulness[i]++;
                    }
                    if (!keyTiles.contains(i) && !useLess.contains(i)) {
                        tilesUsefulness[i]--;
                    }
                }
                int tile = -1;
                int usefulPoint = 100;
                for (int i = 0; i < tilesUsefulness.length; i++) {
                    if (hand[i] > 0) {
                        if (tilesUsefulness[i] < usefulPoint) {
                            usefulPoint = tilesUsefulness[i];
                            tile = i;
                        }
                    }
                }
                if (canDropBeWinAll.contains(tile)) {
                    dis = 0;
                }
            }
            fanInfo.setDistance(dis);
            fanInfo.setKeyLess(keyless);
            fanInfo.setKeyTiles(new ArrayList<>(keyTilesNum.keySet()));
            fanInfo.setUselessTiles(new ArrayList<>(usefulTilesNum.keySet()));

            fanInfoList.add(fanInfo);
        }

        //check 131
        if (groups.isEmpty()) {
            int[] curFan = fanType[0];
            List<Integer> key131 = new ArrayList<>();
            for (int i = 0; i < 34; i++) {
                if (curFan[i] > 0 && handCopy[i] > 0) {
                    key131.add(i);
                }
            }
            HuFanInfo fanInfo = fanInfoAllList.get(0);
            fanInfo.setDistance(13 - key131.size());
            fanInfo.setKeyLess(13 - key131.size());
            fanInfo.setKeyTiles(key131);
            fanInfo.setUselessTiles(Collections.emptyList());

            fanInfoList.add(fanInfo);

        }
        fanInfoList.sort(new Comparator<HuFanInfo>() {
            @Override
            public int compare(HuFanInfo o1, HuFanInfo o2) {
                int disDiff = o1.getDistance() - o2.getDistance();
                if (disDiff != 0) return disDiff;
                return o1.getKeyLess() - o2.getKeyLess();
            }
        });

        return fanInfoList;
    }

    private static List<HuFanInfo> loadFanDetail() {
        Resource resource = new ClassPathResource("fan.txt");
        InputStream is;
        List<HuFanInfo> fanInfoList = new ArrayList<>();
        List<Long> fanPointList = new ArrayList<>();
        {
            try {
                is = resource.getInputStream();
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);
                String data = "";
                while ((data = br.readLine()) != null) {
                    if (StringUtils.isNotBlank(data)) {
                        String[] fanStrs = data.split(",");
                        HuFanInfo fanInfo = new HuFanInfo();
//                        fanInfo.setIndex(Integer.parseInt(fanStrs[0]));
                        fanInfo.setFanNameChinese(fanStrs[0]);
                        fanInfo.setPoint(Long.parseLong((fanStrs[1])));
                        fanInfo.setFanName((fanStrs[4]));
                        fanInfo.setStatus((fanStrs[2]));
                        fanPointList.add((Long.parseLong((fanStrs[1]))));
                        fanInfoList.add(fanInfo);
                    }
                }

                br.close();
                isr.close();
                is.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        fanPoints = new long[fanPointList.size()];
        for (int i = 0; i < fanPointList.size(); i++) {
            fanPoints[i] = fanPointList.get(i);
        }
        return fanInfoList;
    }

    /**
     * 判断混一色和清一色的主番
     * @param tileArr
     * @param groups
     * @return
     */
    public static int getMainSuitWithHonors(int[] tileArr, List<Group> groups) {
        int mainSuit = -1;
        int setWithHonorCount= 0;
        int honorCount =0;
        if (!groups.isEmpty()) {
            int mainSuitNum = 0;
            for (Group group : groups) {
                int s = group.getCards().get(0)/ 9;
                if(s<3) {
                    if (mainSuit < 0 || mainSuit == s) {
                        mainSuit = s;
                        mainSuitNum += 3;
                    } else {
                        return -1;
                    }
                }else{
                    setWithHonorCount++;
                    mainSuitNum += 3;
                }
            }
            if (groups.size() >= 3) {
                return mainSuit;
            }
            if (mainSuit >= 0) {
                int[] tmp = Arrays.copyOfRange(tileArr, 9 * mainSuit, 9 * (mainSuit + 1));
                mainSuitNum += (Arrays.stream(tmp).sum());
                if (mainSuitNum >= 9) {
                    return mainSuit;
                }
            }
        } else {
            int usefulHonor=0;
            int windPair = 0;
            int dragPair = 0;
            for (int i = 27; i < 31; i++) {
                if(tileArr[i] >=2){
                    windPair++;
                    usefulHonor+=tileArr[i];
                }
            }
            for (int i = 31; i < 34; i++) {
                if(tileArr[i] >=2){
                    dragPair++;
                    usefulHonor+=tileArr[i];
                }
            }
            for (int i = 0; i < 3; i++) {
                int[] tmp = Arrays.copyOfRange(tileArr, 9 * i, 9 * (i + 1));
                int num = Arrays.stream(tmp).sum();
                if (num >= 9) {
                    return i+3;
                }
                if(num+usefulHonor>=10){
                    return i+3;
                }
            }
        }
        return -1;
    }
    public static void main(String[] args) {
        ParamMode pm = new ParamMode();
        pm.setHunNum(2);
        String handStr = "4b4b5b5b5b5b6b6b6b6bSfBjF1F1";
        handStr = "9w9t9b9b9b7b7b8b8b8bBjBjFjZj";
        int hand[] = {
                0, 0, 0, 0, 0, 0, 0, 0, 0,// 万
                0, 0, 0, 0, 0, 0, 0, 0, 0,// 条
                0, 0, 0, 0, 0, 1, 0, 2, 0,// 饼
                0, 4, 0, 4, 0, 2, 0
        };
        int fNum = 0;
        Arrays.fill(hand, 0);
        for (int i = 0; i < handStr.length() / 2; i++) {
            String h = handStr.substring(i * 2, i * 2 + 2);
            int tile = getTileCode(h);
            if (h.contains("F")) {
                fNum++;
            } else {
                hand[getTileCode(h)]++;
            }
        }
        pm.setHunNum(fNum);
        List<HuFanInfo> fanInfos = findNearest(pm, hand);

        fanInfos.forEach(e -> System.out.println(e.getFanName() + " " + e.getDistance() + " " + e.getKeyLess()));
        for (HuFanInfo f : fanInfos) {
            System.out.println(f.toString() + " test ");
        }
    }

}
