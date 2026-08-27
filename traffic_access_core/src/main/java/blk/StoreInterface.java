package blk;

/**
 * @BelongsProject: test
 * @BelongsPackage: org.example
 * @Author: HuiYang He
 * @CreateTime: 2024-05-15  12:41
 * @Description: 合约交互类
 * @Version: 1.0*/



import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.StaticGasProvider;
import org.web3j.utils.Convert;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class StoreInterface {
    private static final String PRIVATE_KEY = System.getenv().getOrDefault("BLOCKCHAIN_PRIVATE_KEY", "");
    private static final String LOCAL_INFURA_URL = System.getenv().getOrDefault("BLOCKCHAIN_LOCAL_RPC_URL", "http://127.0.0.1:8545");
    private static final String SEPOLIA_INFURA_URL = System.getenv().getOrDefault("BLOCKCHAIN_SEPOLIA_RPC_URL", "");
    private static final String LOCAL_CONTRACT_ADDRESS = "0xEdF5D60E0dc5f7964A4970F5fdBB46c0895d8Ab5";
    private static final String SEPOLIA_CONTRACT_ADDRESS = "0x3885f2B59D84df1bf1cdf8629DbA7E871ED27778";
    private static final BigInteger LOCAL_CHAIN_ID = BigInteger.valueOf(666); // 本地网络的链ID
    private static final BigInteger SEPOLIA_CHAIN_ID = BigInteger.valueOf(11155111); // Sepolia测试网络的链ID

    private Web3j web3j;
    private DataStorage dataStorage;
    private BigInteger chainId;
    private boolean configured;

    public StoreInterface(boolean useLocalNetwork) {
        String infuraUrl = useLocalNetwork ? LOCAL_INFURA_URL : SEPOLIA_INFURA_URL;
        String contractAddress = useLocalNetwork ? LOCAL_CONTRACT_ADDRESS : SEPOLIA_CONTRACT_ADDRESS;
        chainId = useLocalNetwork ? LOCAL_CHAIN_ID : SEPOLIA_CHAIN_ID;
        configured = !infuraUrl.isBlank() && !PRIVATE_KEY.isBlank();
        if (!configured) return;

        web3j = Web3j.build(new HttpService(infuraUrl));
        Credentials credentials = Credentials.create(PRIVATE_KEY);

        // 设置自定义gas价格和gas限制
        BigInteger gasPrice = Convert.toWei("200", Convert.Unit.GWEI).toBigInteger(); // 200 Gwei
        BigInteger gasLimit = BigInteger.valueOf(3000000); // 自定义gas limit，根据需要设置

        // 定义静态gas提供者
        ContractGasProvider gasProvider = new StaticGasProvider(gasPrice, gasLimit);

        // 创建交易管理器，启用EIP-155重放保护
        TransactionManager transactionManager = new RawTransactionManager(web3j, credentials, chainId.longValue());

        // 加载合约
        dataStorage = DataStorage.load(contractAddress, web3j, transactionManager, gasProvider);
    }

    public void addData(String consumerUsrName, String ownerUsrName, long policyId, long streamID, long startTimeLong,
                        long endTimeLong, long granularity, long startChunkId, long endChunkId) throws Exception {
        ensureConfigured();
        // 添加数据
        BigInteger privacyPolicyId = BigInteger.valueOf(policyId);
        BigInteger streamId = BigInteger.valueOf(streamID);
        BigInteger startTime = BigInteger.valueOf(startTimeLong);
        BigInteger endTime = BigInteger.valueOf(endTimeLong);
        BigInteger minGranularity = BigInteger.valueOf(granularity);

        List<BigInteger> chunkInterval = Arrays.asList(startChunkId, endChunkId).stream()
                .map(BigInteger::valueOf)
                .collect(Collectors.toList());

        // 发送交易并获取交易哈希
        TransactionReceipt receipt = dataStorage.addData(
                consumerUsrName,
                ownerUsrName,
                privacyPolicyId,
                streamId,
                startTime,
                endTime,
                minGranularity,
                chunkInterval
        ).send();

        System.out.println("Transaction Hash: " + receipt.getTransactionHash());
    }

    public void queryData(String ownerUsrName, long policyId) throws Exception {
        ensureConfigured();
        // 查询数据
        BigInteger privacyPolicyId = BigInteger.valueOf(policyId);

        List<DataStorage.PrivacyPolicy> results = dataStorage.queryData(ownerUsrName, privacyPolicyId).send();

        for (DataStorage.PrivacyPolicy result : results) {
            System.out.println("consumerUsrName: " + result.consumerUsrName);
            System.out.println("ownerUsrName: " + result.ownerUsrName);
            System.out.println("privacyPolicyId: " + result.privacyPolicyId);
            System.out.println("streamId: " + result.streamId);
            System.out.println("startTime: " + result.startTime);
            System.out.println("endTime: " + result.endTime);
            System.out.println("minGranularity: " + result.minGranularity);
            System.out.println("chunkInterval: " + result.chunkInterval);
            System.out.println();
        }
    }

    private void ensureConfigured() {
        if (!configured) throw new IllegalStateException("区块链未配置，请通过环境变量设置 RPC 与私钥");
    }
}
