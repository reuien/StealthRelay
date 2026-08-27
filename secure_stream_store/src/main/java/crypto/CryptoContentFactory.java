package crypto;

import index.blockindex.node.NodeContent;
import dataServerProtocol.DataServerProtocol;

import java.util.List;

public class CryptoContentFactory {

    public static final byte LONG_TYPE = 4;
    public static final byte LONG_MAC_TYPE = 6;

    public static NodeContent decodeNodeContent(byte[] data) {
        if (data.length < 1)
            throw new RuntimeException("Decode NodeContentFailed");
        switch (data[0]) {
            case LONG_TYPE:
                throw new RuntimeException("Not available in this codebase");
            case LONG_MAC_TYPE:
                return LongMacNodeNodeContent.decode(data);
        }
        throw new RuntimeException("Decode NodeContent Failed");
    }

    public static NodeContent[] createNodeContentsForRequest(List<DataServerProtocol.MetaData> datas) {
        NodeContent[] result = new NodeContent[datas.size()];
        int count = 0;
        for (DataServerProtocol.MetaData metaData : datas) {
            result[count] = decodeNodeContent(metaData.getEncryptedMetaDataBytes().toByteArray());
            count++;
        }
        return result;
    }
}
