package producerNettyServer;

import producerProtocol.ProducerProtocol.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.Date;


public class producerRequestHandler extends SimpleChannelInboundHandler<RequestMessage> {
    private producerRequestManager manager;
    public producerRequestHandler(producerRequestManager manager) {
        super();
        this.manager = manager;
    }
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RequestMessage msg) throws Exception {
        try {
            MessageRequestType type = msg.getType();
            System.out.println("__________收到请求__________");
            System.out.println("Time: "+ new Date());
            System.out.println("type: "+ type);
            switch (type) {
                case Register_Producer:
                    System.out.println("Register_Producer");
                    RegisterProducer rp = msg.getRegisterProducer();
                    manager.registerProducer(ctx, rp.getUserName(), rp.getProducerId(), rp.getProducerName(),
                            rp.getProducerAddress(), rp.getProducerPort());
                    break;
                case Link_Producer:
                    LinkProducer lp = msg.getLinkProducer();
                    manager.linkProducer(ctx, lp.getUserName(), lp.getProducerId(), lp.getProducerName());
                    break;
                case Key_Agreement:
                    KeyAgreementRequest ka = msg.getKeyAgreement();

                    System.out.println("producerId: "+ka.getProducerId());
                    manager.keyAgreement(ctx, ka.getUserName(), ka.getProducerId(), ka.getPubKey().toByteArray());
                    break;
                case Create_Stream:
                    CreateStream cs = msg.getCreateStream();
                    manager.createStream(ctx, cs.getUserName(), cs.getProducerId(), cs.getStreamId(), cs.getPubKey().toByteArray());
                    break;
                case Delete_Stream:
                    DeleteStream ds = msg.getDeleteStream();
                    manager.deleteStream(ctx, ds.getUserName(), ds.getStreamId());
                    break;
                case Upload_Data:
                    UploadData ud = msg.getUploadData();
                    manager.uploadData(ctx, ud.getStreamId());
                    break;
                case Upload_Data_Live:
                    UploadDataLive udl = msg.getUploadDataLive();
                    manager.uploadDataLive(ctx, udl.getStreamId());
                    break;
            }
            System.out.println("___________处理完毕_____________");
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
