package org.simpleim.client.test;

import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

import org.simpleim.common.handler.BaseHandlerInitializer;

/**
 * Creates a newly configured {@link ChannelPipeline} for a new channel.
 */
public class TestClientHandlerInitializer extends BaseHandlerInitializer<SocketChannel> {

	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		super.initChannel(ch);
		//business logic 有状态，所以为每个Channel创建一个新实例
		ch.pipeline().addLast("handler", new TestClientHandler());
	}

}
