package com.wzy.game.server.controller;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.wzy.game.server.model.*;
import com.wzy.game.server.service.GameLogicServer;
import io.netty.channel.ChannelHandlerContext;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

import static com.wzy.game.server.constant.ConstVar.RESPONSE_CODE_FAID_REQ_EXCEPTION;

/** 推倒和麻将controller */
@Api(tags = "推倒和麻将controller",value = "推倒和麻将controller")
@RequestMapping("/kdd")
@RestController
public class KoudiandianController {

    private final static Logger logger = LoggerFactory.getLogger(KoudiandianController.class);
    ChannelHandlerContext ctx = null;

    /**
     * 查看文档
     * @param model
     * @return
     */
    public String index(Model model) {
        return "index";
    }


    /**
     * 大众麻将起手牌请求
     * @param req
     * @return
     */
    @ApiOperation(value = "推倒和麻将起手牌请求",notes = "推倒和麻将起手牌请求")
    @RequestMapping(value = "/initcard", method = RequestMethod.POST)
    @ResponseBody
    public String ermjInitCard(@RequestBody ReqErmjInitCard req) {
        int resultCode = -1;                //消息处理后的结果表示码
        Object ack = new AckErmjInitCard();
        try {
//            resultCode = InitCardServer.getInstance().doAction(req, (AckErmjInitCard) ack);//业务处理
        }catch (Exception e) {
            logger.error("{} 发牌执行 错误 {} ", e);
            ((AckErmjInitCard) ack).setResponseCode(RESPONSE_CODE_FAID_REQ_EXCEPTION);
            return JSONObject.toJSONString(ack);
        }
        return JSONObject.toJSONString(ack);
    }

    @ApiOperation(value = "推倒和麻将摸牌请求",notes = "推倒和麻将摸牌请求")
    @RequestMapping(value = "/nextcard", method = RequestMethod.POST)
    @ResponseBody
    public String ermjNextCard(@RequestBody ReqNextCard req) {
        int resultCode = -1;                //消息处理后的结果表示码
        Object ack = new AckErmjNextCard();
        try {
//            resultCode = NextCardServer.getInstance().doAction(req,(AckErmjNextCard)ack);//业务处理
        }catch (Exception e) {
            logger.error("{} 发牌执行 错误 {} ", ctx.channel().remoteAddress(), e);
            ((AckErmjNextCard) ack).setResponseCode(RESPONSE_CODE_FAID_REQ_EXCEPTION);
            return JSONObject.toJSONString(ack);
        }
        return JSONObject.toJSONString(ack);
    }


    @ApiOperation(value = "推倒和麻将打牌请求",notes = "推倒和麻将打牌请求")
    @RequestMapping(value = "/gamelogic", method = RequestMethod.POST)
    @ResponseBody
    public String ermjGameLogic(@RequestBody ReqGameLogic req) {
        int resultCode = -1;                //消息处理后的结果表示码
        Object ack = new AckErmjGameLogic();
        try {
            resultCode = GameLogicServer.getInstance().doAction(req,(AckErmjGameLogic)ack);//业务处理
        }catch (Exception e) {
            logger.error("{} 发牌执行 错误 {} ", JSON.toJSONString(req));
            ((AckErmjGameLogic) ack).setResponseCode(RESPONSE_CODE_FAID_REQ_EXCEPTION);
            return JSONObject.toJSONString(ack);
        }
        return JSONObject.toJSONString(ack);
    }

    @ApiOperation(value = "推倒和麻将打牌请求",notes = "推倒和麻将打牌请求")
    @RequestMapping(value = "/gamelogic/json", method = RequestMethod.POST)
    @ResponseBody
    public String ermjGameLogic(@RequestBody JSONObject req) {
        int resultCode = -1;                //消息处理后的结果表示码
        Object ack = new AckErmjGameLogic();
        try {
            resultCode = GameLogicServer.getInstance().doAction(JSON.parseObject(req.toJSONString(), ReqGameLogic.class),(AckErmjGameLogic)ack);//业务处理
        }catch (Exception e) {
            logger.error("{} 发牌执行 错误 {} ", ctx.channel().remoteAddress(), e);
            ((AckErmjGameLogic) ack).setResponseCode(RESPONSE_CODE_FAID_REQ_EXCEPTION);
            return JSONObject.toJSONString(ack);
        }
        return JSONObject.toJSONString(ack);
    }

    @ApiOperation(value = "推倒和麻将结算结果",notes = "推倒和麻将结算结果")
    @RequestMapping(value = "/gameresult", method = RequestMethod.POST)
    @ResponseBody
    public String ermjGameResult(@RequestBody ReqGameLogic req) {
        int resultCode = -1;                //消息处理后的结果表示码
        logger.info(JSONObject.toJSONString(req));
        HashMap ack = new HashMap();
        ack.put("responseCode",0);
        ack.put("msg","保存成功");
        return JSONObject.toJSONString(ack);
    }
}
