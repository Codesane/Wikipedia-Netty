import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;


/* Next up is our custom ChannelHandler. You can see that by the name this is an UpstreamHandler.
 * An UpstreamHandler can receive anything that the Server receives, additionally, an DownstreamHandler can 
 * capture packets that the server is about to send. However depending on where in the hierarchy level the handler
 * is placed, it can be triggered in different states.
 * |------|              |------|            |------|
 * | DSH1 |->-("ABCDE")->| DSH2 |->-("BCD")->| DSH3 |->-("DCB")->[WEB]
 * |------|			     |------|            |------|
 * 
 * Above are three DownstreamHandlers each one with a specific task.
 * The first (DSH1) DownstreamHandler is the DelimiterBasedFrameDecoder that just output
 * a String "ABCDE" down the stream. The second (DSH2) DownstreamHandler intercepts the output from the
 * previous DownstreamHandler and performs its specific logic on the input which in this case
 * is to remove the vowels. Now the third (DSH3) DownstreamHandler will intercept the  outgoing message
 * and it is assigned to reverse the order of the letters. When there's no DonstreamHandlers left in the
 * ChannelPipeline the output will be sent to the client/server.
 * 
 * The same principle applies to UpstreamHandlers. If you want to combine the functionality
 * of the SimpleChannelDownstreamHandler and the SimpleChannelUpstreamHandler there is the class called
 * SimpleChannelHandler. The SimpleChannelHandler class implements both the Down- and Up-stream interfaces
 * which allows the handler to manage messages going both ways. 
 * 
 * In this example, the SimpleChannelUpstreamHandler will be used.
 * 
 *  */
public class MyMessageHandler extends SimpleChannelUpstreamHandler {
	
	private Logger logger = Logger.getLogger(MyMessageHandler.class.getSimpleName());
	
	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		/* The messageReceived method is where all the messages that passes this UpstreamHandler
		 * will be caught. Below we can use the class MessageEvents getMessage() to retrieve
		 * the message it has intercepted. */
		System.out.println("Server :: Received a new message saying: " + e.getMessage());
		
		/* We can also use the class MessageEvents getChannel() to retrieve the Channel object
		 * created by the ChannelFactory we instantiated in the Server class. We can then use the Channel
		 * to perform a write() operation on the pipeline (This will go downstream from beginning of the 
		 * pipeline to the end).
		 * It is important that you append a newline separator, '\n', if you want the ChannelBuffer to be
		 * cleaned and forwarded from the FrameDelimiter. By appending the 'delimiter' you send the
		 * String to its target destination. */
		e.getChannel().write("Hello, client! Your IP is " + e.getRemoteAddress() + "!\n" +
				"We received your message saying: " + e.getMessage() + "\n");
		
		/* We must not forget to call the super.messageReceived(...) for our superclass. If you do not do this,
		 * the message will be stuck in the pipeline. */
		super.messageReceived(ctx, e);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
		/* Override method for exceptions. It's good practice to Log the errors that occur in your
		 * errors. */
		logger.log(Level.SEVERE, e.getCause().toString());
		
		/* We always call the method superclass. */
		super.exceptionCaught(ctx, e);
	}

	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		System.out.println("Server :: " + e.getChannel().getRemoteAddress() + " has connected!");
		/* We can specifically handle new connections. 
		 * For example add the Channel to a ChannelGroup. */
		
		/* We always call the method superclass. */
		super.channelConnected(ctx, e);
	}

	@Override
	public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		/* We can also handle when a disconnection occur.
		 * Here we could remove the Channel from abovementioned ChannelGroup. */
		System.out.println("Server :: " + e.getChannel().getRemoteAddress() + " has disconnected from the Server.");
		/* We always call the method superclass. */
		super.channelDisconnected(ctx, e);
	}
	
	
	
}
