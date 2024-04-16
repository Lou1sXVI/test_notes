package com.wzy.game.server.task;

import com.wzy.game.server.netty.serialize.JSONSerializer;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.AsciiString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/*
 * 机器人业务模型
 */
public class RobotActionRunner implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(RobotActionRunner.class);

    ChannelHandlerContext ctx = null;
    String reqMsg;  //请求的消息,是一个json
    String url;    //请求的url
    String contentType; //请求的类型

    private static final AsciiString CONTENT_TYPE = AsciiString.cached("Content-Type");
    private static final AsciiString CONTENT_LENGTH = AsciiString.cached("Content-Length");
    private static final AsciiString CONNECTION = AsciiString.cached("Connection");
    private static final AsciiString KEEP_ALIVE = AsciiString.cached("keep-alive");

    public RobotActionRunner(ChannelHandlerContext ctx, String reqMsg, String url, String contentType) {
        this.ctx = ctx;
        this.reqMsg = reqMsg;
        this.url = url.toLowerCase();
        this.contentType = contentType;
    }

    @Override
    public void run() {
    }

    private void sendSuccessResponse(Object ack, HttpResponseStatus status) {
        byte[] content = new JSONSerializer().serialize(ack);
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, status, Unpooled.wrappedBuffer(content));
        response.headers().set(CONTENT_TYPE, "application/json");
        response.headers().set("charset", "utf-8");
        response.headers().setInt(CONTENT_LENGTH, response.content().readableBytes());
        ctx.writeAndFlush(response);
        ctx.close();
    }

    private void sendErrorResponse(StringBuilder stringBuilder) {
        byte[] content = stringBuilder.toString().getBytes();
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST, Unpooled.wrappedBuffer(content));
        response.headers().set(CONTENT_TYPE, "text/plain");
        response.headers().set("charset", "utf-8");
        response.headers().setInt(CONTENT_LENGTH, response.content().readableBytes());
        ctx.writeAndFlush(response);
        ctx.close();
    }
}
