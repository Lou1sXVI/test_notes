//package com.wzy.game.server.model;
//
//import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
//import org.bson.types.ObjectId;
//import org.springframework.data.annotation.Id;
//
//import java.util.Date;
//import java.util.HashMap;
//
//@JsonIgnoreProperties(ignoreUnknown = true)
//public class CloudHistory {
//    @Id
//    private ObjectId oid;
//    private HashMap reqPara;//请求参数
//    private String  service;
//    private String  resBody;
//    private HashMap reqBody;
//    private Date    reqTime;
//    private String  IPAddress;
//    private String  user;
//    private String  url;
//    private String code;
//
//    public String getCode() {
//        return code;
//    }
//
//    public void setCode(String code) {
//        this.code = code;
//    }
//
//
//    public HashMap getReqPara() {
//        return reqPara;
//    }
//
//    public void setReqPara(HashMap reqPara) {
//        this.reqPara = reqPara;
//    }
//
//    public String getService() {
//        return service;
//    }
//
//    public void setService(String service) {
//        this.service = service;
//    }
//
//    public String getResBody() {
//        return resBody;
//    }
//
//    public void setResBody(String resBody) {
//        this.resBody = resBody;
//    }
//
//    public HashMap getReqBody() {
//        return reqBody;
//    }
//
//    public void setReqBody(HashMap reqBody) {
//        this.reqBody = reqBody;
//    }
//
//    public Date getReqTime() {
//        return reqTime;
//    }
//
//    public void setReqTime(Date reqTime) {
//        this.reqTime = reqTime;
//    }
//
//    public String getIPAddress() {
//        return IPAddress;
//    }
//
//    public void setIPAddress(String IPAddress) {
//        this.IPAddress = IPAddress;
//    }
//
//    public String getUser() {
//        return user;
//    }
//
//    public void setUser(String user) {
//        this.user = user;
//    }
//
//    public String getUrl() {
//        return url;
//    }
//
//    public void setUrl(String url) {
//        this.url = url;
//    }
//
//    public ObjectId getOid() {
//        return oid;
//    }
//}
