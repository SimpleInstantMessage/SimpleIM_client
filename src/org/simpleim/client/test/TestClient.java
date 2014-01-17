package org.simpleim.client.test;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

public class TestClient {

	private final String host;
	private final int port;

	public TestClient(String host, int port) {
		this.host = host;
		this.port = port;
	}

	public void run() throws Exception {
		EventLoopGroup group = new NioEventLoopGroup();
		try {
			Bootstrap b = new Bootstrap();
			// Configure the client.
			b.group(group)
			 .channel(NioSocketChannel.class)
			 .option(ChannelOption.TCP_NODELAY, true)
			 .handler(new TestClientHandlerInitializer());

			// Start the client.
			ChannelFuture f = b.connect(host, port).sync();

			// Wait until the connection is closed.
			f.channel().closeFuture().sync();
		} finally {
			// Shut down the event loop to terminate all threads.
			group.shutdownGracefully();
		}
	}

	public static void main(String[] args) throws Exception {
		// Print usage if no argument is specified.
		if (args.length != 2) {
			System.err.println(
					"Usage: " + TestClient.class.getSimpleName() +
					" <host> <port>");
			return;
		}

		// Parse options.
		final String host = args[0];
		final int port = Integer.parseInt(args[1]);

		new TestClient(host, port).run();
	}
}
