package controllerNettyServer;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import exceptions.CouldNotReceiveException;
import exceptions.CouldNotStoreException;
import exceptions.InvalidQueryException;
import exceptions.QueryFailedException;
import mpc.User;
import org.apache.commons.lang3.tuple.Pair;
import streamHandling.FederationRequest;
import streamHandling.FederationToken;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.*;

import static mpc.DHKeyExchange.MPC;

public class PrivacyControllerManager {
    private String path;
    private List<String> usrList = new ArrayList<>();
    private HashMap<String, PrivacyController> controllerHashMap = new HashMap<>();
    private PrivacyController curController;

    public PrivacyControllerManager(){}

    public PrivacyController getPrivacyControllerForUsr(String usrName) throws CouldNotStoreException,
            CertificateException, IOException, KeyStoreException, NoSuchAlgorithmException, QueryFailedException {
        if(usrList.contains(usrName)){
            curController = controllerHashMap.get(usrName);
        }else {
            curController = createPrivacyControllerForUsr(usrName);
            controllerHashMap.put(usrName, curController);
            usrList.add(usrName);
        }
        return curController;
    }
    public PrivacyController createPrivacyControllerForUsr(String usrName) throws CouldNotStoreException,
            CertificateException, IOException, KeyStoreException, NoSuchAlgorithmException, QueryFailedException {
        PrivacyController usrController = new PrivacyController(usrName);
        return usrController;
    }

    public HashMap<String, PrivacyController> getControllersForMPC(List<String> mpcUsrList)
            throws CouldNotStoreException, CertificateException, IOException, KeyStoreException,
            NoSuchAlgorithmException, QueryFailedException {
        HashMap<String, PrivacyController> mpcControllers = new HashMap<>();
        for (String mpcUsr : mpcUsrList){
            mpcControllers.put(mpcUsr, getPrivacyControllerForUsr(mpcUsr));
        }
        return mpcControllers;
    }
    public ArrayList<Long> getFederationKeys(ArrayList<User> usrListMPC, List<Long> streamIdList, long fromTime,
                                             long toTime) throws CouldNotReceiveException, InvalidQueryException {

        try {
            MPC(usrListMPC); //安全多方计算，DH交换密钥
        } catch (Exception e) {

            System.out.println("安全多方计算，DH交换密钥失败");
        }

        for (int i = 0; i < usrListMPC.size(); i++) {
            User curUsr = usrListMPC.get(i);
            long curStreamId = streamIdList.get(i);
            long p = curUsr.getP();
            ArrayList<Long> mpcKeys = new ArrayList<>();
            ArrayList<Long> edgeKeys = curUsr.getController().getEdgeKeys(curStreamId, fromTime, toTime);
            for (long edgeKey : edgeKeys){
                mpcKeys.add(edgeKey+p);
            }
            curUsr.setMpcKeys(mpcKeys);
        }

        /*ArrayList<Long> federationKeys = new ArrayList<>(Collections.nCopies(3, 0L));*/
        ArrayList<Long> federationKeys = new ArrayList<>(Collections.nCopies(9, 0L));
        for (User curUsr : usrListMPC){
            ArrayList<Long> curKeys = curUsr.getMpcKeys();
            for (int i = 0; i < federationKeys.size(); i++) {
                federationKeys.set(i, federationKeys.get(i)+curKeys.get(i));
            }
        }
        return federationKeys;
    }

    public boolean checkFederationRequest(FederationRequest fq){
        return true;
    }

    public FederationToken getFederationToken(FederationRequest fq) throws CouldNotStoreException, CertificateException,
            IOException, KeyStoreException, NoSuchAlgorithmException, CouldNotReceiveException, InvalidQueryException,
            QueryFailedException {

        if(checkFederationRequest(fq)){
            String consumerName = fq.getConsumerName();
            ArrayList<Pair<String, Long>> nameAndStreamList = fq.getNameAndStreamList();
            long fromTime = fq.getFromTime();
            long toTime = fq.getToTime();

            System.out.println(consumerName);
            System.out.println(nameAndStreamList);
            System.out.println(fromTime);
            System.out.println(toTime);


            List<Long> streamIdList = new ArrayList<>();
            List<Long> streamStartTimeList = new ArrayList<>();
            List<Long> chunkSizeList = new ArrayList<>();

            ArrayList<User> usrListMPC = new ArrayList<>();
            for (int i = 0; i < nameAndStreamList.size(); i++) {
                System.out.println(i);
                String curName = nameAndStreamList.get(i).getLeft();
                System.out.println(curName);
                User usr = new User(curName, getPrivacyControllerForUsr(curName));
                System.out.println(usr);
                usrListMPC.add(usr);
            }

            for (int i = 0; i < nameAndStreamList.size(); i++) {
                System.out.println(i);
                User curUsr = usrListMPC.get(i);
                System.out.println(curUsr.getName().equals(nameAndStreamList.get(i).getLeft()));
                assert curUsr.getName().equals(nameAndStreamList.get(i).getLeft());
                long curStreamId = nameAndStreamList.get(i).getRight();
                streamIdList.add(curStreamId);
                Pair<Long, Long> ts = curUsr.getController().getSteamInfo(curStreamId);
                streamStartTimeList.add(ts.getLeft());
                chunkSizeList.add(ts.getRight());
            }
            ArrayList<Long> federationKeys = getFederationKeys(usrListMPC, streamIdList, fromTime, toTime);
            for (User usr : usrListMPC){
                System.out.println(usr);
            }

            return new FederationToken(consumerName, streamIdList, streamStartTimeList, chunkSizeList, federationKeys);
        }
        return null;
    }



    private void setPath(String pcmPath) {
        this.path = pcmPath;
    }
    public void addUsr(String usrName) throws CouldNotStoreException, CertificateException, IOException,
            KeyStoreException, NoSuchAlgorithmException, QueryFailedException {
        if(!usrList.contains(usrName)){
            curController = createPrivacyControllerForUsr(usrName);
            controllerHashMap.put(usrName, curController);
            usrList.add(usrName);
        }
    }

    public List<String> getUsrList() {
        return usrList;
    }

    public HashMap<String, PrivacyController> getControllerHashMap() {
        return controllerHashMap;
    }

    public PrivacyController getCurController() {
        return curController;
    }


/*    public static PrivacyControllerManager localPCMFromFile(String path) throws IOException {
        File profilePath = new File(path);
        if (!profilePath.exists()) {
            throw new FileNotFoundException();
        }
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        mapper.registerModule(new ParameterNamesModule(JsonCreator.Mode.PROPERTIES));
        mapper.registerModule(new Jdk8Module());

        PrivacyControllerManager pcm = mapper.readValue(profilePath, PrivacyControllerManager.class);
        pcm.setPath(profilePath.getAbsolutePath());
        return pcm;
    }

    public boolean syncPCM() throws CouldNotStoreException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        if (path != null) {
            try {
                mapper.writerWithDefaultPrettyPrinter().writeValue(new FileOutputStream(path), this);
            } catch (IOException e) {
                throw new CouldNotStoreException("Error occurred during storing of the profile: " + e.getMessage());
            }
        }
        // TODO: Actually check if changes occurred since the last write.
        return true;
    }

    public static PrivacyControllerManager getPrivacyControllerManager() throws CouldNotStoreException, IOException {
        String pathPCM = "privacy_policy_controller/src/main/java/profile/PrivacyControllerManager.jks";
        if(!(new File(pathPCM)).exists()){
            PrivacyControllerManager pcm = new PrivacyControllerManager();
            pcm.setPath(pathPCM);
            if(pcm.syncPCM()){
                return pcm;
            }else {
                throw new CouldNotStoreException("Error occurred during storing of the profile");
            }
        }else {
            return localPCMFromFile(pathPCM);
        }
    }*/







}
