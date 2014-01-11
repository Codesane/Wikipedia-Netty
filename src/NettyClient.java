import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.codec.frame.DelimiterBasedFrameDecoder;
import org.jboss.netty.handler.codec.frame.Delimiters;
import org.jboss.netty.handler.codec.string.StringDecoder;
import org.jboss.netty.handler.codec.string.StringEncoder;
import org.jboss.netty.util.CharsetUtil;


public class NettyClient {
	
	/* When coding the Client there any very little things that differ.
	 * The name ClientBootstrap has changed, the Server uses ServerBootstrap. */
	private final ClientBootstrap bootstrap;
	
	public NettyClient() {
		final ExecutorService bossThreadPool = Executors.newCachedThreadPool();
		final ExecutorService workerThreadPool = Executors.newCachedThreadPool();
		
		final ChannelFactory factory = new NioClientSocketChannelFactory(bossThreadPool, workerThreadPool);
		
		/* Everything is equal, except for the Bootstrap name. Here we construct the
		 * ClientBootstrap using equal parameters as the ServerBootstrap.  */
		this.bootstrap = new ClientBootstrap(factory);
		
		final int ALLOWED_CHARACTER_BUFFER_SIZE = 8192;
		
		this.bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
			@Override
			public ChannelPipeline getPipeline() throws Exception {
				return Channels.pipeline(
					new StringDecoder(CharsetUtil.UTF_8),
					new StringEncoder(CharsetUtil.UTF_8),
					new DelimiterBasedFrameDecoder(ALLOWED_CHARACTER_BUFFER_SIZE, Delimiters.lineDelimiter()),
					
					/* We also add our Clients own ChannelHandler. */
					new ClientChannelHandler()
				);
			}
		});
		
		/* Instead of only defining the port, we also define the host IP Address that we're supposed
		 * to connect to. In this case I'm running the Server on my own machine. */
		final String HOSTNAME = "localhost";
		final int LISTEN_PORT = 53233;
		
		/* Now we perform an asynchronous operation. Hence we have to invoke the ChannelFuture.awaitUninterruptibly()
		 * method to wait for it to complete. (Tip: It's a good habit to add a timeout to the awaitUninterruptibly
		 * method to hinder a possibly system jam. */
		ChannelFuture connection = this.bootstrap.connect(
			new InetSocketAddress(HOSTNAME, LISTEN_PORT)
		);
		/* We access the isSuccess method flag to determine the status of the connection attempt. If the
		 * isSuccess() method returns true we can send messages through the connection. */
		if(!connection.awaitUninterruptibly().isSuccess()) {
			System.err.println("Client :: Unable to connect to host " + HOSTNAME + ":" + LISTEN_PORT);
			System.exit(-1);
		}
		
		
		System.out.println("Client :: Successfully connected to host " + HOSTNAME + ":" + LISTEN_PORT);
		
		/* We create a Channel called comLink. The ChannelFuture will provide us with a Channel that can be
		 * used to write messages across the stream. */
		Channel comLink = connection.getChannel();
		
		/* We output the String message through the DownstreamHandlers down to the Sink where it is 
		 * sent to the Server. */
		comLink.write("Hello, Server!\n");
		
	}
	
	private class ClientChannelHandler extends SimpleChannelUpstreamHandler {
		
		@Override
		public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
			/* Extremely simple variant of a handler. Only for testing the communication
			 * between the two applications. */
			System.out.println("Received a message from the server: " + e.getMessage());
			super.messageReceived(ctx, e);
		}
		
	}
	/* We launch the application after the Server has started and then try to connect. */
	public static void main(String[] args) {
		new NettyClient();
	}
}
