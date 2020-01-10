package com.vboly.util;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

/**
 * ClassName:ChildChannelHandler
 * Function: TODO ADD FUNCTION.
 * @author hxy
 */
public class ChildChannelHandler extends ChannelInitializer<SocketChannel> {

    private static final int READ_IDEL_TIME_OUT = 0;    // 读超时
    private static final int WRITE_IDEL_TIME_OUT = 0;   // 写超时
    private static final int ALL_IDEL_TIME_OUT = 360;   // 所有超时

    @Override
    protected void initChannel(SocketChannel e) throws Exception {
        ChannelPipeline pipeline = e.pipeline();

        // 设置30秒没有读到数据，则触发一个READER_IDLE事件。
        pipeline.addLast(new IdleStateHandler(READ_IDEL_TIME_OUT, WRITE_IDEL_TIME_OUT, ALL_IDEL_TIME_OUT,  TimeUnit.SECONDS));
        pipeline.addLast(new NettyIdleStateHandler());

        // HttpServerCodec：将请求和应答消息解码为HTTP消息
        pipeline.addLast("http-codec", new HttpServerCodec());

        // HttpObjectAggregator：将HTTP消息的多个部分合成一条完整的HTTP消息
        pipeline.addLast("aggregator", new HttpObjectAggregator(65535));

        // ChunkedWriteHandler：向客户端发送HTML5文件
        pipeline.addLast("http-chunked", new ChunkedWriteHandler());

        // 用于处理websocket, /ws为访问websocket时的uri
        pipeline.addLast(new WebSocketServerProtocolHandler("/ws"));

        // 在管道中添加我们自己的接收数据实现方法
        pipeline.addLast("handler", new MyWebSocketServerHandler());
    }
}
