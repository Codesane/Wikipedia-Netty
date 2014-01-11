import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.frame.DelimiterBasedFrameDecoder;
import org.jboss.netty.handler.codec.frame.Delimiters;
import org.jboss.netty.handler.codec.string.StringDecoder;
import org.jboss.netty.handler.codec.string.StringEncoder;
import org.jboss.netty.util.CharsetUtil;


public class NettyServer {
	
	/** In our first step we have to create a bootstrap. The bootstrap holds important classes such as the
	 *  ChannelFactory and the PipelineFactory. */
	private final ServerBootstrap bootstrap;
	
	public NettyServer() {
		
		/* The next step requires us to create the resource managers. 
		 * Netty lets us choose how we want our Channels to be creates during runtime. By default the
		 * ChannelFactory will actually use the exact same setup as below without the need to supply
		 * any parameters, however for this example we'll stick with the cachedThreadPools.
		 * 
		 * The reason for why you would want to use ExecutorServices is because Threads are expensive to create.
		 * Using the ExecutorService class we reduce the amount of work the Java Virtual Machine has to do 
		 * when creating new Threads by caching old Threads and reusing them instead of deallocate / allocate. */
		final ExecutorService bossThreadPool = Executors.newCachedThreadPool();
		final ExecutorService workerThreadPool = Executors.newCachedThreadPool();
		
		
		/* The ChannelFactory is responsible for the I/O Netty performs.
		 * Having two ThreadPools supplied by the ExecutorServices you can adjust how quickly you wish your
		 * application to perform. What makes Netty so incredibly quick is that it is taking full advantage
		 * of the Java NIO libraries, which stands for New I/O. 
		 * You can change the Nio to Oio, this can be useful when designing Mobile applications for Android.
		 * Nio is known to be a little too heavy for mobile applications, by some considered a bad practice. */
		final ChannelFactory channelFactory = 
				new NioServerSocketChannelFactory(bossThreadPool, workerThreadPool);
		
		/* We supply the ChannelFactory to the ServerBootstrap class when invoking its' constructor. */
		this.bootstrap = new ServerBootstrap(channelFactory);
		
		/* We set a maximum character buffer length for our Delimiter. */
		final int ALLOWED_CHARACTER_BUFFER_SIZE = 8192;
		
		/* Now, the most important part! We supply the ServerBootstrap with a ChannelPipelineFactory,
		 * then we Override the interface getPipeline method. Inside this method we define our
		 * handlers. Since Netty uses ChannelBuffers, for simplicity we use the StringDecoder/Encoder handlers
		 * to decode and encode our I/O data. We also supply a DelimiterBasedFrameDecoder to detect when our
		 * input is supposed to be separated into a chunk.
		 * 
		 * Consider the following ChannelBuffer data:           
		 * [N] [e] [t] [t] [y] [ ] [ ] [ ] [ ] [ ] [ ] [ ] ... [ ] [ ] [ ]
		 * If the server received the above 5 letters it would wait until a special character (the line delimiter) 
		 * was intercepted by the stream.
		 * Now we append a '\n' to our buffer.
		 * [N] [e] [t] [t] [y] [\n] [ ] [ ] [ ] [ ] [ ] [ ] ... [ ] [ ] [ ]
		 * The data is now ready to be returned by the ChannelBuffer, we receive it in our
		 * messageReceived(...) or handleUpstream(...) method in our handler(s). (handleDownstream(...) for outgoing messages)
		 * 
		 * */
		this.bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
			@Override
			public ChannelPipeline getPipeline() throws Exception {
				return Channels.pipeline(
					new StringDecoder(CharsetUtil.UTF_8), // UpstreamHandler
					new StringEncoder(CharsetUtil.UTF_8), // DownstreamHandler
					new DelimiterBasedFrameDecoder(ALLOWED_CHARACTER_BUFFER_SIZE, Delimiters.lineDelimiter()), // Upstream
					new MyMessageHandler() // Custom ChannelUpstreamHandler
				);
			}
		});
		
		final int LISTEN_PORT = 53233;
		
		/* We use a channel when attempting to bind the ServerBootstrap to ensure if the operation was successful */
		Channel acceptor = this.bootstrap.bind(new InetSocketAddress(LISTEN_PORT));
		
		/* We make sure the Server could bind to the port by calling the isBound() method in the Channel class. */
		if(!acceptor.isBound()) {
			System.err.println("Server :: Error! Unable to bind to port " + LISTEN_PORT);
			System.exit(-1);
		}
		
		System.out.println("Server :: Successfully bound to port " + LISTEN_PORT + "!" +
				"\nAwaiting new connections...");
	}
	
	public void shutdownServer() {
		/* It is considered good practice to always release your resources when you are done using the ServerBootstrap.
		 * The releaseExternalResources() method will clear the boss- and worker-thread pool. */
		this.bootstrap.releaseExternalResources();
	}
	
	public static void main(String[] args) {
		new NettyServer();
	}
	
}
