package io.netty.example.lqw;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * @author liuqingwen
 * @version 1.0
 * @Desc
 * @date 2023/5/29 15:02
 */
public class RedisCli {

    public static void main(String[] args) throws InterruptedException {

        NioEventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new SimpleChannelInboundHandler<Object>() {
                                @Override
                                protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
                                    ByteBuf buf = (ByteBuf) msg;
                                    byte[] bytes = null;
                                    if (buf.hasArray()) {
                                        bytes = buf.array();
                                    } else {
                                        int length = buf.readableBytes();
                                        bytes = new byte[length]; // 创建可读字节的数组
                                        buf.getBytes(buf.readerIndex(), bytes); // 将字节复制到该数组
                                    }
                                    System.out.println(new String(bytes));
                                    System.out.println(Arrays.toString(bytes));

                                    ctx.disconnect();
                                }

                                // https://redis.io/docs/reference/protocol-spec/
                                // https://redis.com.cn/topics/protocol.html
                                // https://zhuanlan.zhihu.com/p/345327284
                                @Override
                                public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                    String end = "\r\n";
                                    // https://blog.csdn.net/lonely_fireworks/article/details/7962171
//                                    String format = String.format("*3%s$3%<sset%<s$2%<shh%<s$3%<sliu%<s", end); // +OK
                                    String format = String.format("*2%s$3%<sget%<s$2%<shh%<s", end); // get 数组 $
//                                    String format = String.format("*2%s$6%<sexists%<s$2%<shh%<s", end); // key是否存在，返回整数 :1
//                                    String format = String.format("*1%s$8%<slastsave%<s", end); // Unix时间戳   :number
                                    System.out.println(format);
                                    ctx.writeAndFlush(Unpooled.copiedBuffer(format.getBytes(Charset.defaultCharset())));
                                }
                            });
                        }
                    }).remoteAddress(new InetSocketAddress("10.211.55.4", 6379));

            ChannelFuture sync = bootstrap.connect().sync();
            sync.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }

    }

}
