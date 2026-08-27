package usrs;

import crypto.DigestCrypto.DigestEncryption;
import keyDerivation.KeyDerivationTree;
import keyDerivation.KeyDerivationTreeFactory;
import streamHandling.PrivacyPolicy;
import streamHandling.Token;

public class PrivacyTransformer {
    public long getTokenID(PrivacyPolicy privacyPolicy){
        return privacyPolicy.getPrivacyPolicyId();
    }
    public long getStreamID(PrivacyPolicy privacyPolicy){
        return privacyPolicy.getCorrespondingStream().getId();
    }
    public long[] getMessageInterval(PrivacyPolicy privacyPolicy){
        return privacyPolicy.getChunkInterval();
    }
    public static KeyDerivationTree createKeyDerivationTreeForToken(Token token){
        KeyDerivationTree keyDerivationTree = KeyDerivationTreeFactory.getNewDefaultKDTree(token.getNodes(), token.getTreeDepth());
        return keyDerivationTree;
    }
    public static DigestEncryption createCryptoUtil(KeyDerivationTree kdt){
        DigestEncryption decUtil = new DigestEncryption(kdt);
        return decUtil;
    }
/*    public static HashMap<Long, Message> privacyTransform(DigestEncryption crypto, HashMap<Long, Message> encMessages, long[] messageInterval){
        HashMap<Long, Message> decMessages = new HashMap<>();
        long messageIdStart = messageInterval[0];
        long messageIdEnd = (messageInterval[1]-1);
        for(long messageId = messageIdStart; messageId <= messageIdEnd; messageId++){
            Message curMessage = encMessages.get(messageId);
            long decValue = crypto.decryptSingleMsgWithId(curMessage.getValue(),curMessage.getMessageID());
            Message decMessage = new Message(curMessage.getCorrespondingStreamID(), curMessage.getMessageID(),
                    curMessage.getTimestamp(), decValue, false);
            decMessages.put(curMessage.getMessageID(),decMessage);
        }
        return decMessages;
    }*/
/*    public AggrMessage privacyTransformForAggrMessage(DigestEncryption crypto, AggrMessage encAggrMessages){
        long messageFrom = encAggrMessages.getMessageIDFrom();
        long messageTo = encAggrMessages.getMessageIDTo();
        long decValue = crypto.decrypt(encAggrMessages.getAggrValue(), messageFrom, messageTo);
        AggrMessage decAggrMessage = new AggrMessage(encAggrMessages.getCorrespondingStreamID(),
                messageFrom, messageTo, decValue, false);
        return decAggrMessage;
    }*/

}
