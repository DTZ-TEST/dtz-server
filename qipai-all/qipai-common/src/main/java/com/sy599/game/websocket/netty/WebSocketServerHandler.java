package com.sy599.game.websocket.netty;

import com.sy.mainland.util.PropertiesFileLoader;
import com.sy599.game.GameServerConfig;
import com.sy599.game.assistant.AssisServlet;
import com.sy599.game.character.Player;
import com.sy599.game.common.action.ActionServlet;
import com.sy599.game.common.constant.LangMsg;
import com.sy599.game.gcommand.GeneralCommand;
import com.sy599.game.gcommand.login.LoginCommand;
import com.sy599.game.gcommand.login.UnionLoginCommand;
import com.sy599.game.manager.PlayerManager;
import com.sy599.game.webservice.GroupServlet;
import com.sy599.game.websocket.WebSocketManager;
import com.sy599.game.websocket.netty.coder.MessageUnit;
import com.sy599.game.util.LangHelp;
import com.sy599.game.util.LogUtil;
import com.sy599.game.webservice.OnlineNoticeServlet;
import com.sy599.game.websocket.MyWebSocket;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import com.sy599.game.websocket.netty.coder.MessageCoder;
import com.sy599.game.websocket.netty.coder.NettyHttpServletRequest;
import com.sy599.game.websocket.netty.coder.NettyHttpServletResponse;
import com.sy599.game.websocket.netty.handshaker.WebSocketServerHandshaker13;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by lz
 */
public class WebSocketServerHandler extends
        SimpleChannelInboundHandler<Object> {

    private static final Logger LOGGER = LoggerFactory.getLogger("msg");

//    public static ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    private final static Map<Channel, MessageUnit> channelMessageUnitMap = new ConcurrentHashMap<>();

    private WebSocketServerHandshaker handshaker;

    public static volatile boolean isOpen = false;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx,
                                Object msg) throws Exception { // (1)

//        System.out.println("server msgClass:" + msg.getClass());
        if (isOpen) {
            if ((msg instanceof WebSocketFrame))
                handleWebSocketFrame(ctx, msg);
            else if ((msg instanceof FullHttpRequest))
                handleHttpFullRequest(ctx, msg);
            else if ((msg instanceof ByteBuf))
                handleSocketRequest(ctx, msg);
        }
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {  // (2)
//        Channel incoming = ctx.channel();
//        for (Channel channel : channels) {
//            channel.writeAndFlush(new TextWebSocketFrame("[SERVER] - " + incoming.remoteAddress() + " 加入"));
//        }
//        channels.add(ctx.channel());
//        System.out.println("Client:" + incoming.remoteAddress() + "加入:total=" + channels.size());
        if (PropertiesFileLoader.isWindows())
            System.out.println("channel total=" + NettyUtil.channelUserMap.size() + ",user total=" + PlayerManager.getInstance().getPlayerCount() + ",channel=" + ctx.channel()+ ",currentThread=" + Thread.currentThread().getName());

    }


    private void exit(ChannelHandlerContext ctx) {
        Channel channel = ctx.channel();
        channelMessageUnitMap.remove(channel);
        String channelId = channel.id().asShortText();
        NettyUtil.PACKAGE_MAP.remove(channelId);
        NettyUtil.SSL_MAP.remove(channelId);
        String ip = NettyUtil.userIpMap.remove(channel);
        Long user = NettyUtil.channelUserMap.remove(channel);
        if (user != null) {
            Player player;
            MyWebSocket myWebSocket = WebSocketManager.webSocketMap.get(user);
            if (myWebSocket != null) {
                player = myWebSocket.getPlayer();
            }else{
                player = null;
            }

            if (player == null){
                player = PlayerManager.getInstance().getPlayer(user);
            }

            if (player != null){
                if (myWebSocket == null){
                    myWebSocket = player.getMyWebSocket();
                }

                if (myWebSocket == null || myWebSocket.isLoginError()) {
                    LogUtil.msgLog.info("--webSocket close loginerr name:{},userId:{},ip:{},currentThread={}", player.getName(), player.getUserId(), ip, Thread.currentThread().getName());
                } else {
                    LogUtil.msgLog.info("--webSocket close name:{},userId:{},ip:{},currentThread={}", player.getName(), player.getUserId(), ip, Thread.currentThread().getName());
                }
                player.exit(channelId);
            }
        }
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {  // (3)
//        Channel incoming = ctx.channel();
//        for (Channel channel : channels) {
//            channel.writeAndFlush(new TextWebSocketFrame("[SERVER] - " + incoming.remoteAddress() + " 离开"));
//        }
//        channels.remove(ctx.channel());
//        System.out.println("Client:" + incoming.remoteAddress() + "离开:total=" + channels.size());
        exit(ctx);
        if (PropertiesFileLoader.isWindows())
            System.out.println("channel total=" + NettyUtil.channelUserMap.size() + ",user total=" + PlayerManager.getInstance().getPlayerCount() + ",channel=" + ctx.channel()+ ",currentThread=" + Thread.currentThread().getName());
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception { // (5)
//        Channel incoming = ctx.channel();
//        System.out.println("Client:" + incoming.remoteAddress() + "在线:total=" + channels.size());
//        System.out.println("channel total=" + channelUserMap.size() + ",user total=" + PlayerManager.getInstance().getPlayerCount());

    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception { // (6)
//        Channel incoming = ctx.channel();
//        System.out.println("Client:" + incoming.remoteAddress() + "掉线:total=" + channels.size());
//        System.out.println("channel total=" + channelUserMap.size() + ",user total=" + PlayerManager.getInstance().getPlayerCount());

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
//        Channel incoming = ctx.channel();
//
//        Connections.remove(ctx);
//
//        channelMessageUnitMap.remove(incoming);
//        System.out.println("Client:" + incoming.remoteAddress() + "异常:total=" + Connections.size());

//
        exit(ctx);
        //当出现异常就关闭连接
        if (PropertiesFileLoader.isWindows())
            System.out.println("channel total=" + NettyUtil.channelUserMap.size() + ",user total=" + PlayerManager.getInstance().getPlayerCount() + ",channel=" + ctx.channel()+ ",currentThread=" + Thread.currentThread().getName());
//        LOGGER.error("error msg:" + cause.getMessage(), cause);

        ctx.close();
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        super.channelReadComplete(ctx);
        ctx.flush();
    }

    private void handleSocketRequest(ChannelHandlerContext ctx, Object msg) {
//        LOGGER.info("handleSocketRequest:" + msg.toString());
        serviceHandle(ctx, (ByteBuf) msg, 0);
    }

    private void handleHttpFullRequest(ChannelHandlerContext ctx, Object message) {
        FullHttpRequest request = (FullHttpRequest) message;
        String uri = request.uri();
        HttpMethod method = request.method();
        boolean useSsl = SslUtil.hasSslHandler(ctx);
        boolean isWebSocket = false;
        String ip = NettyUtil.loadRequestIp(ctx, request);

        try {
            if (!request.decoderResult().isSuccess()) {
                NettyUtil.sendHttpResponse(ctx, new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST));
                return;
            }

            if (method == HttpMethod.GET) {
                isWebSocket = isWebSocketUpgrade(request);
                if (isWebSocket) {
                    NettyUtil.PACKAGE_MAP.remove(ctx.channel().id().asShortText());
                    // 正常WebSocket的Http连接请求，构造握手响应返回
                    String webSocketURL = (useSsl ? "wss://" : "ws://") + request.headers().get(HttpHeaderNames.HOST) + ":" + GameServerConfig.SERVER_PORT + (uri.equals("/") ? "" : uri);
                    CharSequence version = request.headers().get(HttpHeaderNames.SEC_WEBSOCKET_VERSION);
                    if (version != null && version.equals(WebSocketVersion.V13.toHttpHeaderValue())) {
                        // Version 13 of the wire protocol - RFC 6455 (version 17 of the draft hybi specification).
                        handshaker = new WebSocketServerHandshaker13(
                                webSocketURL, null, false, 65536);
//                        handshaker = new WebSocketServerHandshaker13( webSocketURL, null, false, 65536);
                    } else {
                        WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(webSocketURL, null, false);
                        handshaker = wsFactory.newHandshaker(request);
                    }

                    if (handshaker == null) {
                        // 无法处理的websocket版本
                        WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
                    } else {
                        // 向客户端发送websocket握手,完成握手
                        NettyUtil.userIpMap.put(ctx.channel(), ip);
                        handshaker.handshake(ctx.channel(), request);
                    }
                } else {
                    httpHandle(ctx, request);
                }
            } else if (method == HttpMethod.POST) {
                httpHandle(ctx, request);
            } else {
                NettyUtil.sendHttpResponse(ctx, new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST));
            }
        } catch (Exception e) {
            LOGGER.error("error msg:" + e.getMessage(), e);
        }finally {
            LogUtil.msgLog.info("Request remoteAddress:{},ip={},method={},useSsl={},webSocket={},currentThread={}", NettyUtil.getRemoteAddr(ctx), ip, method, useSsl, isWebSocket, Thread.currentThread().getName());
        }

    }

    private void handleWebSocketFrame(ChannelHandlerContext ctx, Object message0) {
        try {
            WebSocketFrame msg = (WebSocketFrame) message0;

            Channel incoming = ctx.channel();

            if (msg instanceof TextWebSocketFrame) {
                if (PropertiesFileLoader.isWindows()) {
                    incoming.writeAndFlush(new TextWebSocketFrame("server is ok^_^[you]:" + ((TextWebSocketFrame) msg).text()));
                }
            } else if (msg instanceof BinaryWebSocketFrame) {
                BinaryWebSocketFrame webSocketFrame = (BinaryWebSocketFrame) msg;
                serviceHandle(ctx, webSocketFrame.content(), 1);

            } else if (msg instanceof PingWebSocketFrame) {
                LOGGER.info("recieved PingWebSocketFrame from channel {}", ctx.channel());
                incoming.writeAndFlush(
                        new PongWebSocketFrame(msg.content().retain()));
            } else if (msg instanceof PongWebSocketFrame) {
                LOGGER.info("recieved PongWebSocketFrame from channel {}", ctx.channel());
//            incoming.writeAndFlush(
//                    new PingWebSocketFrame(msg.content().retain()));
            } else if (msg instanceof CloseWebSocketFrame) {
                LOGGER.info("recieved CloseWebSocketFrame from channel {}", ctx.channel());
                incoming.writeAndFlush(msg.retain()).addListener(
                        ChannelFutureListener.CLOSE_ON_FAILURE);
            } else if (msg instanceof ContinuationWebSocketFrame) {

            }
        } catch (Exception e) {
            LOGGER.error("error msg:" + e.getMessage(), e);
        }
    }

    private static final boolean isWebSocketUpgrade(FullHttpRequest req) {
        HttpHeaders headers = req.headers();
        if (req.method() == HttpMethod.GET) {
            String temp = headers.get(HttpHeaderNames.UPGRADE);
            if (temp != null && temp.toLowerCase().contains("websocket")) {
                temp = headers.get(HttpHeaderNames.CONNECTION);
                if (temp != null && temp.toLowerCase().contains("upgrade")) {
                    return true;
                }
            }
        }
        return false;
    }

    private final static void serviceHandle(ChannelHandlerContext ctx, ByteBuf byteBuf, int channelMark) {
        byte[] result;
        int length = byteBuf.readableBytes();
        if (byteBuf.hasArray()) {
            //堆栈缓冲区
            byte[] array = byteBuf.array();
            int offset = byteBuf.arrayOffset() + byteBuf.readerIndex();
            result = new byte[length];
            System.arraycopy(array, offset, result, 0, length);
        } else {
            //直接缓冲区
            byte[] array = new byte[length];
            byteBuf.getBytes(byteBuf.readerIndex(), array);
            result = array;
        }

        try {
            MessageUnit message = MessageCoder.decode(result, channelMessageUnitMap.get(ctx.channel()));

            if (message == null) {
                channelMessageUnitMap.remove(ctx.channel());
                return;
            }

            if (message.complete()) {
//                System.out.println("接收完毕：msg = " + messageUnit.simple());
//                System.out.println("recieved BinaryWebSocketFrame msg=" + messageUnit.simple() + ",content=" + new String(messageUnit.content(), "UTF-8"));
                channelMessageUnitMap.remove(ctx.channel());

                long startTime = System.currentTimeMillis();

                Long user = NettyUtil.channelUserMap.get(ctx.channel());

                Player player;
                if (user == null) {
                    player = null;
                } else {
                    player = PlayerManager.getInstance().getPlayer(user);
                }

                try {
                    if (message != null) {
                        //如果是登陆的消息
                        if (message.getMsgType() == WebSocketMsgType.cs_login) {
                            LoginCommand acton = new LoginCommand();
                            MyWebSocket myWebSocket = new MyWebSocket();
                            myWebSocket.setChannelMark(channelMark);
                            myWebSocket.stCtx(ctx);
                            acton.setCtx(ctx);

                            player = acton.login(message, myWebSocket);
                            // 没有登录成功
                            if (player == null) {
                                LogUtil.msg(acton.getLoginUserId() + " 登录失败--> player is null");
                                ctx.close();
                                return;
                            }
                        } else if (message.getMsgType() == WebSocketMsgType.union_login) {
                            UnionLoginCommand acton = new UnionLoginCommand();
                            MyWebSocket myWebSocket = new MyWebSocket();
                            myWebSocket.setChannelMark(channelMark);
                            myWebSocket.stCtx(ctx);
                            acton.setCtx(ctx);

                            int mark = acton.login(message, myWebSocket);
                            // 没有登录成功
                            if (mark != 1) {
                                ctx.close();
                            }
                            return;
                        } else {
                            Object obj = GeneralCommand.getInstance().isGMessage(message, player);
                            boolean ret;
                            if (obj instanceof Boolean) {
                                ret = (Boolean) obj;

                                if (player == null && !ret){
                                    ctx.close();
                                    return;
                                }
                            } else {
                                if (player == null) {
                                    ctx.close();
                                    return;
                                }

                                ret = (Boolean) (((Object[]) obj)[0]);
                                player = (Player) (((Object[]) obj)[1]);
                            }

                            if (player != null) {
                                if (ret) {
                                    // 执行了通用消息，游戏之前的C房，加房等走此处
                                    GeneralCommand.getInstance().execute(ctx, player, message);
                                } else {
                                    //所有玩家准备完毕后 游戏开始，游戏通讯走此处
                                    PlayerManager.getInstance().process(player, message);
                                }
                            }
                        }
                    }
                    if (player != null) {
                        player.changeActionCount(1);
                        player.setSyncTime(new Date());
                    }
                    long totaltime = System.currentTimeMillis() - startTime;
                    if (totaltime > 50) {
//                        String s = "socket totaltime-->uid:" + (player != null ? player.getUserId() : "") + " t:" + message.getMsgType() + " " + LogUtil.printlnLog(message.getMessage()) + " time:"
//                                + totaltime + " start:" + startTime;
//                        LogUtil.monitor_i(s);

                        StringBuilder sb = new StringBuilder("socket|xnlog");
                        sb.append("|").append((player != null ? player.getUserId():0));
                        sb.append("|").append(message.getMsgType());
                        sb.append("|").append(LogUtil.printlnLog(message.getMessage()));
                        sb.append("|").append(totaltime);
                        sb.append("|").append(startTime);
                        LogUtil.monitor_i(sb.toString());
                    }

                } catch (Exception e) {
                    if (message != null) {
                        LogUtil.e("onWebSocketBinary" + " " + message.getMsgType() + " " + LogUtil.printlnLog(message.getMessage()), e);

                    } else {
                        LogUtil.e("onWebSocketBinary", e);
                    }
                    if (player != null) {
                        player.writeErrMsg(LangHelp.getMsg(LangMsg.code_16));
                    } else {
                        ctx.close();
                    }
                } finally {

                }
            } else {
                channelMessageUnitMap.put(ctx.channel(), message);
                System.out.println("正在接收：" + message.currentLength() + "/" + message.getLength() + ",msg = " + message.simple());
                LOGGER.info("正在接收：{}/{},msg={}", message.currentLength(), message.getLength(), message.simple());
            }
        } catch (Exception e) {
            channelMessageUnitMap.remove(ctx.channel());
            LOGGER.error("decode fail:" + e.getMessage(), e);
        } finally {
            byteBuf.clear();
        }
    }

    private static final void httpHandle(ChannelHandlerContext ctx, FullHttpRequest request) {
        try {
            String uri = request.uri();
            HttpServlet servlet;
            if (uri.startsWith("/qipai/pdk.do")) {
                servlet = new ActionServlet();
            } else if (uri.startsWith("/online/notice.do")) {
                servlet = new OnlineNoticeServlet();
            } else if (uri.startsWith("/assistant/pdk.do")) {
                servlet = new AssisServlet();
            } else if (uri.startsWith("/group/msg.do")) {
                servlet = new GroupServlet();
            } else {
                if (uri.startsWith("/favicon.ico") || "/".equals(uri)) {
                    NettyUtil.sendHttpResponse(ctx, new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK));
                } else {
                    LOGGER.warn("uri not exists:uri=" + uri);
                    NettyUtil.sendHttpResponse(ctx, new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND));
                }
                return;
            }
//                LOGGER.info("request headers={}",request.headers());
            DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
            HttpServletRequest req = new NettyHttpServletRequest(ctx, request);
            HttpServletResponse res = new NettyHttpServletResponse(ctx, response);
            try {
                servlet.service(req, res);
            } catch (Throwable t) {
                LOGGER.warn("uri exception:uri=" + uri + ",msg=" + t.getMessage(), t);
            } finally {
                if (!((NettyHttpServletResponse) res).isFlush()) {
                    res.getWriter().flush();
                    res.getWriter().close();
                }
            }
        } catch (Exception e) {
            LOGGER.error("httpHandle Exception:" + e.getMessage(), e);
        }
    }
}
