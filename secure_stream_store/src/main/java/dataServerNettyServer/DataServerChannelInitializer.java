package dataServerNettyServer;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.util.concurrent.EventExecutorGroup;

import dataServerProtocol.DataServerProtocol.*;

public class DataServerChannelInitializer extends ChannelInitializer<SocketChannel> {
    private DataServerRequestManager manager;
    private EventExecutorGroup dbHandlerPool;
    public DataServerChannelInitializer(DataServerRequestManager manager, EventExecutorGroup dbHandlerPool) {
        this.manager = manager;
        this.dbHandlerPool = dbHandlerPool;
    }
    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline p = ch.pipeline();
        p.addLast(new ProtobufVarint32FrameDecoder());
        p.addLast(new ProtobufDecoder(RequestMessage.getDefaultInstance()));
        p.addLast(new ProtobufVarint32LengthFieldPrepender());
        p.addLast(new ProtobufEncoder());
        p.addLast(dbHandlerPool, new DataServerRequestHandler(manager));
    }
}
