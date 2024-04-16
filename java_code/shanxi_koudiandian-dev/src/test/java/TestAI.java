import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.wzy.game.server.ai.AILevel5;
import com.wzy.game.server.model.*;
import com.wzy.game.server.service.GameLogicServer;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.io.*;
import java.util.ArrayList;
import java.util.List;


public class TestAI {
    @Test
    public void testremove(){
        int[] hands = {1,2,3,0,0,0,1,1,1,
                1,0,1,1,2,3,0,0,0,
                1,0,1,1,2,3,0,0,0,};
        int[] res = AILevel5.getInstance().removeShun(hands);
        System.out.println("");
    }

    @Test
    public void pailiTest() {

        String s =
//                readJsonFile("D:\\Work@wzy\\data\\tdh_control.json");
                readJsonFile("D:\\Work@wzy\\data\\jn_tdh_control_2000.json");
        s = s.replace("\r\n", "");
        s = s.replaceAll(" ", "");
        String[] strings = s.split(",\\{\"_id\"");
        int i=0;
        int j=0;
        int k=0;
        for (String a : strings) {
            if (!StringUtils.contains(a, "{\"_id\"")) {
                a = "{  \"_id\"".concat(a);
            }
            JSONObject jsonObject = JSON.parseObject(a);
            String res = jsonObject.getString("resBody");
            JSONObject resBody = JSON.parseObject(res);
            String card = resBody.getString("cards");
            ReqControlCard reqControlCard=JSON.parseObject(String.valueOf(jsonObject.getJSONObject("reqBody")),ReqControlCard.class);
            AckControlCard ackControlCard=new AckControlCard();
//            ackControlCard=new CardControlServer().controlCard(reqControlCard);
            System.out.println(ackControlCard.getCards());
            if(ackControlCard.getCards().equals(card)){
                j++;
            }
            i++;
            if(i==1999){
                break;
            }
        }
        System.out.println("equa"+j+"/"+i);

        JSONArray a = JSON.parseArray(s);

    }

    public static String readJsonFile(String fileName) {
        String jsonStr = "D:\\Work@wzy\\data\\tdh_control.json";
        try {
            File jsonFile = new File(fileName);
            FileReader fileReader = new FileReader(jsonFile);
            Reader reader = new InputStreamReader(new FileInputStream(jsonFile), "utf-8");
            int ch = 0;
            StringBuffer sb = new StringBuffer();
            while ((ch = reader.read()) != -1) {
                sb.append((char) ch);
            }
            fileReader.close();
            reader.close();
            jsonStr = sb.toString();
            return jsonStr;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String sortTile(String tiles){
        List<String> list = new ArrayList<>(16);
        for (int i = 0; i < tiles.length(); i+=2) {
            list.add(tiles.substring(i,i+2));
        }
        list.sort((s1, s2) -> {
            int res = Character.compare(s1.charAt(1), s2.charAt(1));
            if (res == 0) {
                return Character.compare(s1.charAt(0), s2.charAt(0));
            }
            return res;
        });
        StringBuilder ss = new StringBuilder();
        for (String s : list) {
            if(!ss.toString().endsWith(s.substring(1))) {
                ss.append(",");
            }
            ss.append(s);
        }
        return ss.toString();
    }


    @Test
    public void gameTest() throws Exception {
        ReqTdhGameLogic reqTdhGameLogic = new ReqTdhGameLogic();
        String s = "{\"gameId\":\"3lXs5WP-8q66tUWtNSmcT\",\"fuNo\":2,\"curPos\":0,\"userInfo\":[[10000,1],[10000,1],[10000,1],[10000,1]],\"fuTotal\":12,\"level\":3,\"players\":[{\"isTing\":0,\"tiles\":\"6w7w9w7b9b2t3t4t5t5t6t7t8t9t\",\"sets\":[],\"position\":0},{\"isTing\":0,\"tiles\":\"9w8w7w6w5w4w3w5t3t2t\",\"sets\":[\"SfSfSf\"],\"position\":1},{\"isTing\":1,\"tiles\":\"9w8w7w6w5w3w3w9t8t7t\",\"sets\":[\"NfNfNf\"],\"position\":2},{\"isTing\":0,\"tiles\":\"2w2w1b1b2b1t2t3t3t4t4tZjZj\",\"sets\":[],\"position\":3}],\"remain\":50,\"history\":\"2:3:Ef,1:0:7w,2:0:Ef,1:1:4w,2:1:3b,1:2:9w,2:2:Wf,1:3:Zj,2:3:Wf,1:0:7t,2:0:Bj,1:1:4b,6:1:4b,1:2:Nf,2:2:Fj,1:3:4w,2:3:Nf,14:2:Nf,2:2:2t,1:3:2t,2:3:Bj,1:0:Sf,6:0:Sf,14:1:Sf,2:1:7b,1:2:3w,2:2:9w,1:3:3t,2:3:Fj,1:0:5t,2:0:Fj,1:1:1w,6:1:1w,1:2:2b,6:2:2b,1:3:1b,2:3:7t,1:0:8t,2:0:2w,1:1:Wf,6:1:Wf,1:2:5b,6:2:5b,1:3:1b,2:3:5b,1:0:Bj,6:0:Bj,1:1:5b,6:1:5b,1:2:Wf,6:2:Wf,1:3:4t,2:3:2b,1:0:6w,2:0:4b,1:1:9b,6:1:9b,1:2:7t,19:2:5w,1:3:2w,2:3:1w,1:0:Sf,6:0:Sf,1:1:9b,6:1:9b,1:2:9b,6:2:9b,1:3:2b,2:3:4w,1:0:3t\",\"userId\":0,\"dealer\":3,\"legalAct\":[3,4,5,7,8,14],\"wall\":\"8w4w5w5t6t6t1b8w6b6wBj1t4wEf7b3b4bNf6b5b8b1w8tZjFj9t8b8t4b2w1w3w6b1t1b3b7t7w3bZj6b7b2b8b4t1t8b9tEf6t\"}";
        //String s = "{\"curPos\":3,\"dealer\":0,\"grade\":2,\"history\":\"6:2:6w,1:3:Wf\",\"legalAct\":[7],\"level\":0,\"maxFan\":0,\"players\":[{\"isTing\":0,\"point\":0,\"position\":0,\"sets\":[],\"tiles\":\"4w2b8t1t1w2w1b5w8t1t6b8b4w\"},{\"isTing\":0,\"point\":0,\"position\":1,\"sets\":[],\"tiles\":\"9w3bNf5w4t6t4t9b4b9b4t4w1w\"},{\"isTing\":0,\"point\":0,\"position\":2,\"sets\":[],\"tiles\":\"2t5t8t9t1b1b2b2b4b4b6b5b6w\"},{\"isTing\":0,\"point\":0,\"position\":3,\"sets\":[],\"tiles\":\"3w5w7w8w3t4t5t6t9t3b5b6b7b8b\"}],\"remain\":0,\"rule\":{\"smallHu\":4,\"smallPao\":8},\"userID\":0}";

        AckErmjGameLogic ack = new AckErmjGameLogic();
        ReqGameLogic req = JSON.parseObject(s, ReqGameLogic.class);

        System.out.println(sortTile(req.getPlayers().get(0).getTiles()));
        System.out.println(sortTile(req.getPlayers().get(1).getTiles()));
        System.out.println(sortTile(req.getPlayers().get(2).getTiles()));
        System.out.println(sortTile(req.getPlayers().get(3).getTiles()));

        GameLogicServer.getInstance().doAction(req,ack);
//        try {
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
        System.out.println(JSON.toJSONString(ack));
    }
}
