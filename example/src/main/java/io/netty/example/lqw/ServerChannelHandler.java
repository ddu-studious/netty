package io.netty.example.lqw;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import javax.management.ObjectName;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author liuqingwen
 * @version 1.0
 * @Desc
 * @date 2023/3/26 16:17
 */
public class ServerChannelHandler extends ChannelInboundHandlerAdapter {

    List<Channel> channels = new ArrayList<Channel>();

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        String cOnline = channel.remoteAddress() + "，已经上线了。";
        this.notifyClients(cOnline);
        channels.add(channel);
        System.out.println(cOnline);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        channels.remove(channel);
        String cOutline = channel.remoteAddress() + "，下线了。";
        this.notifyClients(cOutline);
        System.out.println(cOutline);
    }

    protected void notifyClients(String msg) {
        channels.stream().forEach(
                c -> {
                    c.writeAndFlush(Unpooled.copiedBuffer((msg + new Date()).getBytes(Charset.defaultCharset())));
                }
        );
    }
}
