package com.example.gateway.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthGasPrice;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.response.PollingTransactionReceiptProcessor;

import java.math.BigInteger;

@Component
public class EthereumAnchorClient {
    @Value("${blockchain.rpc-url:}") private String rpcUrl;
    @Value("${blockchain.private-key:}") private String privateKey;
    @Value("${blockchain.chain-id:0}") private long chainId;
    @Value("${blockchain.explorer-base-url:}") private String explorerBaseUrl;
    @Value("${blockchain.gas-limit:100000}") private long gasLimit;

    public boolean isConfigured() {
        return rpcUrl != null && !rpcUrl.isBlank() && privateKey != null && !privateKey.isBlank() && chainId > 0;
    }

    public AnchorReceipt anchor(String sha256) throws Exception {
        if (!isConfigured()) throw new IllegalStateException("区块链环境变量未配置");
        if (sha256 == null || !sha256.matches("[0-9a-f]{64}")) throw new IllegalArgumentException("摘要格式无效");
        Web3j web3j = Web3j.build(new HttpService(rpcUrl));
        try {
            Credentials credentials = Credentials.create(privateKey.trim());
            EthGasPrice gasResponse = web3j.ethGasPrice().send();
            if (gasResponse.hasError()) throw new IllegalStateException("读取 Gas 价格失败：" + gasResponse.getError().getMessage());
            BigInteger price = gasResponse.getGasPrice();
            RawTransactionManager manager = new RawTransactionManager(web3j, credentials, chainId);
            EthSendTransaction sent = manager.sendTransaction(price, BigInteger.valueOf(gasLimit),
                    credentials.getAddress(), "0x" + sha256, BigInteger.ZERO);
            if (sent.hasError()) throw new IllegalStateException("发送存证交易失败：" + sent.getError().getMessage());
            TransactionReceipt receipt = new PollingTransactionReceiptProcessor(web3j, 1_000L, 45)
                    .waitForTransactionReceipt(sent.getTransactionHash());
            if (!receipt.isStatusOK()) throw new IllegalStateException("存证交易执行失败：" + receipt.getTransactionHash());
            return new AnchorReceipt(chainId, credentials.getAddress(), receipt.getTransactionHash(),
                    receipt.getBlockNumber().longValueExact());
        } finally {
            web3j.shutdown();
        }
    }

    public String explorerUrl(String transactionHash) {
        if (transactionHash == null || transactionHash.isBlank() || explorerBaseUrl == null || explorerBaseUrl.isBlank()) return null;
        return explorerBaseUrl.replaceAll("/+$", "") + "/tx/" + transactionHash;
    }

    public boolean verify(String transactionHash, String sha256) throws Exception {
        if (!isConfigured() || transactionHash == null || sha256 == null) return false;
        Web3j web3j = Web3j.build(new HttpService(rpcUrl));
        try {
            var response = web3j.ethGetTransactionByHash(transactionHash).send();
            if (response.hasError()) throw new IllegalStateException("读取链上交易失败：" + response.getError().getMessage());
            return response.getTransaction()
                    .map(transaction -> ("0x" + sha256).equalsIgnoreCase(transaction.getInput()))
                    .orElse(false);
        } finally {
            web3j.shutdown();
        }
    }

    public record AnchorReceipt(long chainId, String fromAddress, String transactionHash, long blockNumber) {}
}
