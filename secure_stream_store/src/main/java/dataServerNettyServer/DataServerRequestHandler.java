package dataServerNettyServer;

import crypto.CryptoContentFactory;
import index.Chunk;
import index.Digest;
import index.blockindex.node.NodeContent;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import dataServerProtocol.DataServerProtocol.*;

import java.math.BigInteger;
import java.util.Date;


public class DataServerRequestHandler extends SimpleChannelInboundHandler<RequestMessage> {
    private DataServerRequestManager manager;
    public DataServerRequestHandler(DataServerRequestManager manager) {
        super();
        this.manager = manager;
    }
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RequestMessage msg) throws Exception {
        try {
            MessageRequestType type = msg.getType();
            System.out.println("____________________");
            System.out.println("Time: "+new Date());
            System.out.println("type: "+type);
            switch (type) {
                case Create_Stream:
                    CreateStream msgCreateStream = msg.getCreateStream();
                    manager.createStream(ctx, msgCreateStream.getUserName(), msgCreateStream.getStreamId());
                    break;
                case Delete_Stream:
                    DeleteStream msgDeleteStream = msg.getDeleteStream();
                    manager.deleteStream(ctx, msgDeleteStream.getUserName(), msgDeleteStream.getStreamId());
                    break;
                case Add_Chunk:
                    AddChunk msgAddChunk = msg.getAddChunk();
                    Chunk chunk = new Chunk(msgAddChunk.getChunkID(), msgAddChunk.getEncryptedChunkBytes().toByteArray());
                    NodeContent[] meta = CryptoContentFactory.createNodeContentsForRequest(msgAddChunk.getEncryptedDigestList());
                    manager.addChunk(ctx, msgAddChunk.getUserName(), msgAddChunk.getCorrespondingStreamID(), chunk, meta);
                    break;
                case Add_Chunk_New:
                    AddChunkNew msgAddChunkNew = msg.getAddChunkNew();
                    Chunk newChunk = new Chunk(msgAddChunkNew.getChunkID(), msgAddChunkNew.getEncryptedChunkBytes().toByteArray());
                    Digest digest = new Digest(msgAddChunkNew.getSum(), msgAddChunkNew.getCount(),
                            msgAddChunkNew.getSquare(), false);
                    if (msgAddChunkNew.getHasMac()){
                        digest.setHasMAC(true);
                        digest.setSumMac(new BigInteger(msgAddChunkNew.getSumMacBytes().toByteArray()));
                        digest.setCountMac(new BigInteger(msgAddChunkNew.getCountMacBytes().toByteArray()));
                        digest.setSquareMac(new BigInteger(msgAddChunkNew.getSquareMacBytes().toByteArray()));
                    }
                    manager.addChunkNew(ctx, msgAddChunkNew.getUserName(), msgAddChunkNew.getStreamID(), newChunk, digest);
                    break;
                case Add_Chunk_NewDigest:
                    AddChunkNewDigest acnd = msg.getAddChunkNewDigest();
                    Chunk newDigestChunk = new Chunk(acnd.getChunkID(), acnd.getEncryptedChunkBytes().toByteArray());
                    Digest newDigest = new Digest(acnd.getSum(), acnd.getCount(), acnd.getSquare(), acnd.getCount1(),
                            acnd.getCount2(), acnd.getCount3(), acnd.getCount4(), acnd.getCount5(), acnd.getCount6(), false);
                    if (acnd.getHasMac()){
                        newDigest.setHasMAC(true);
                        newDigest.setSumMac(new BigInteger(acnd.getSumMacBytes().toByteArray()));
                        newDigest.setCountMac(new BigInteger(acnd.getCountMacBytes().toByteArray()));
                        newDigest.setSquareMac(new BigInteger(acnd.getSquareMacBytes().toByteArray()));
                        newDigest.setCount1Mac(new BigInteger(acnd.getCount1MacBytes().toByteArray()));
                        newDigest.setCount2Mac(new BigInteger(acnd.getCount2MacBytes().toByteArray()));
                        newDigest.setCount3Mac(new BigInteger(acnd.getCount3MacBytes().toByteArray()));
                        newDigest.setCount4Mac(new BigInteger(acnd.getCount4MacBytes().toByteArray()));
                        newDigest.setCount5Mac(new BigInteger(acnd.getCount5MacBytes().toByteArray()));
                        newDigest.setCount6Mac(new BigInteger(acnd.getCount6MacBytes().toByteArray()));
                    }
                    manager.addChunkNew(ctx, acnd.getUserName(), acnd.getStreamID(), newDigestChunk, newDigest);
                    break;

                case Get_Chunks:
                    GetChunks msgGetChunks = msg.getGetChunks();
                    manager.getChunks(ctx, msgGetChunks.getUserName(), msgGetChunks.getStreamID(), msgGetChunks.getChunkIdFrom(),msgGetChunks.getChunkIdTo());
                    break;
                case Get_Statistics:
                    GetStatistics msgGetStatistics = msg.getGetStatistics();
                    manager.getStatistics(ctx, msgGetStatistics.getUserName(), msgGetStatistics.getStreamID(),
                            msgGetStatistics.getChunkIdFrom(), msgGetStatistics.getChunkIdTo(),
                            msgGetStatistics.getGranularity(), msgGetStatistics.getMetaDataIdList().stream().mapToInt(i->i).toArray());
                    break;
                case Get_StatisticAll:
                    GetStatisticAll msgGetStatisticAll = msg.getGetStatisticAll();
                    manager.getStatisticAll(ctx, msgGetStatisticAll.getUserName(), msgGetStatisticAll.getStreamID(),
                            msgGetStatisticAll.getChunkIdFrom(), msgGetStatisticAll.getChunkIdTo(),
                            msgGetStatisticAll.getMetaDataIdList().stream().mapToInt(i->i).toArray());
                    break;
                case Get_Statistics_New:
                    GetStatisticsNew msgGetStatisticsNew = msg.getGetStatisticsNew();
                    /*manager.getStatisticsNew(ctx, msgGetStatisticsNew.getUserName(), msgGetStatisticsNew.getStreamID(),
                            msgGetStatisticsNew.getChunkIdFrom(), msgGetStatisticsNew.getChunkIdTo(), msgGetStatisticsNew.getGranularity());*/
                    manager.getStatisticsNewDigest(ctx, msgGetStatisticsNew.getUserName(), msgGetStatisticsNew.getStreamID(),
                            msgGetStatisticsNew.getChunkIdFrom(), msgGetStatisticsNew.getChunkIdTo(), msgGetStatisticsNew.getGranularity());
                    break;
                case Get_Statistics_All_New:
                    GetStatisticAllNew msgGetStatisticAllNew = msg.getGetStatisticAllNew();
                    /*manager.getStatisticAllNew(ctx, msgGetStatisticAllNew.getUserName(), msgGetStatisticAllNew.getStreamID(),
                            msgGetStatisticAllNew.getChunkIdFrom(), msgGetStatisticAllNew.getChunkIdTo());*/
                    manager.getStatisticAllNewDigest(ctx, msgGetStatisticAllNew.getUserName(), msgGetStatisticAllNew.getStreamID(),
                            msgGetStatisticAllNew.getChunkIdFrom(), msgGetStatisticAllNew.getChunkIdTo());
                    break;
            }
            System.out.println("________over________");
            System.out.println("");

        } catch (Exception e) {
            ctx.writeAndFlush(ResponseMessage.newBuilder()
                    .setType(MessageResponseType.Error_Response)
                    .setErrorResponse(ErrorResponse.newBuilder().setId(1000).setMessage("error").build())
                    .build());
        }
    }
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        ctx.close();
    }
}
