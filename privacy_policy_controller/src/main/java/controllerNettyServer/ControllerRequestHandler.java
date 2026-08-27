package controllerNettyServer;

import controllerProtocol.ControllerProtocol.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.Date;


public class ControllerRequestHandler extends SimpleChannelInboundHandler<RequestMessage> {
    private ControllerRequestManager manager;
    public ControllerRequestHandler(ControllerRequestManager manager) {
        super();
        this.manager = manager;
    }
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RequestMessage msg) throws Exception {
        try {
            MessageRequestType type = msg.getType();
            System.out.println("_________");
            System.out.println("Time: "+new Date());
            System.out.println("type: "+type);
            switch (type) {
                case Key_Agreement:
                    KeyAgreementRequest ka = msg.getKeyAgreement();
                    manager.keyAgreement(ctx, ka.getUserName(), ka.getPubKey().toByteArray());
                    break;
                case Create_Stream:
                    CreateStream cs = msg.getCreateStream();
                    manager.createStream(ctx, cs.getUserName(), cs.getStreamId(), cs.getPubKey().toByteArray());
                    break;
                case Delete_Stream:
                    DeleteStream ds = msg.getDeleteStream();
                    manager.deleteStream(ctx, ds.getUserName(), ds.getStreamId());
                    break;
                case Create_PrivacyPolicy:
                    CreatePrivacyPolicy cpp = msg.getCreatePrivacyPolicy();
                    manager.createPrivacyPolicy(ctx, cpp.getConsumerName(), cpp.getOwnerName(), cpp.getPolicyId(),
                            cpp.getStreamId(), cpp.getStartTime(), cpp.getEndTime(), cpp.getMinGranularity());
                    break;
                case Delete_PrivacyPolicy:
                    DeletePrivacyPolicy dpp = msg.getDeletePrivacyPolicy();
                    manager.deletePrivacyPolicy(ctx, dpp.getOwnerName(), dpp.getPolicyId());
                    break;
                case Request_Token:
                    RequestToken rt = msg.getRequestToken();
                    manager.requestToken(ctx, rt.getConsumerName(), rt.getOwnerName(), rt.getPolicyId(),
                            rt.getStreamId(), rt.getStartTime(), rt.getEndTime(), rt.getGranularity());
                    break;
                case Create_FederationPolicy:
                    CreateFederationPolicy cfp = msg.getCreateFederationPolicy();
                    manager.createFederationPolicy(ctx, cfp.getConsumerName(), cfp.getOwnerName(), cfp.getPolicyId(),
                            cfp.getStreamId(), cfp.getStartTime(), cfp.getEndTime());
                    break;
                case Delete_FederationPolicy:
                    DeleteFederationPolicy dfp = msg.getDeleteFederationPolicy();
                    manager.deleteFederationPolicy(ctx, dfp.getOwnerName(), dfp.getPolicyId());
                    break;
                case Request_FederationToken:
                    RequestFederationToken rft = msg.getRequestFederationToken();
                    manager.requestFederationToken(ctx, rft.getRequestContent().toByteArray());
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
