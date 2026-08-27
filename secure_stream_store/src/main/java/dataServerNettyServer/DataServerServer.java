package dataServerNettyServer;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataServerServer {
    private final int dataServerPort = 1101;
    private final int aThreads = 2;
    private final int cThreads = 16;
    private final int wThreads = 32;
    private static final Logger LOGGER = LoggerFactory.getLogger(DataServerServer.class);

    public static void main(String[] args) {
        new DataServerServer().start();
    }
    public DataServerServer() {
    }
    public void start() {
        EventLoopGroup serverGroup = new NioEventLoopGroup(aThreads);
        EventLoopGroup workerGroup = new NioEventLoopGroup(cThreads);
        EventExecutorGroup group = new DefaultEventExecutorGroup(wThreads);

        DataServerChannelInitializer initializer = new DataServerChannelInitializer(
                new DataServerRequestManager(), group);

        try {
            ServerBootstrap bootStrap = new ServerBootstrap();
            bootStrap.group(serverGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(initializer)
                    //.option(ChannelOption.TCP_NODELAY, true)
                    .childOption(ChannelOption.TCP_NODELAY, true);
            // Bind to port
            bootStrap.bind(dataServerPort).sync().channel().closeFuture().sync();
        } catch (InterruptedException e) {
            LOGGER.error("Netty服务器中断", e);
        } finally {
            serverGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            group.shutdownGracefully();
            LOGGER.info("Netty服务器关闭");
        }

    }

}