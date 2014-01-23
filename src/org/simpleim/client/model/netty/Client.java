package org.simpleim.client.model.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import org.simpleim.common.handler.BaseHandlerInitializer;

public class Client {

	private final String host;
	private final int port;
	private final ChannelHandler handler;

	public Client(String host, int port, ChannelHandler handler) {
		this.host = host;
		this.port = port;
		this.handler = handler;
	}

	/**
	 * start connecting to server
	 * @return connect future, that is <p><code>new Bootstrap().sth().connect(host, port);</code></p>
	 */
	public ChannelFuture run() {
		final EventLoopGroup group = new NioEventLoopGroup();
		Bootstrap b = new Bootstrap();
		// Configure the client.
		b.group(group)
		 .channel(NioSocketChannel.class)
		 .option(ChannelOption.TCP_NODELAY, true)
		 .handler(new BaseHandlerInitializer<SocketChannel>() {
			@Override
			protected void initChannel(SocketChannel ch) throws Exception {
				super.initChannel(ch);
				//business logic 一般有状态，所以为每个Channel创建一个新实例
				ch.pipeline().addLast("handler", handler);
			}
		 });

		// Start the client.
		ChannelFuture f = b.connect(host, port);
		f.channel().closeFuture().addListener(new ChannelFutureListener() {
			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				// Shut down the event loop to terminate all threads.
				group.shutdownGracefully();
			}
		});

		return f;
	}
}
