package state;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import exceptions.CouldNotStoreException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class ProducerProfile {
    private String owner;
    private long producerId;
    private String producerName;
    private String producerAddress;
    private int producerPort;
    private String serverAddress;
    private int serverPort;
    @JsonIgnore
    private String profilePath;
    private String keyStorePath;

    public ProducerProfile() {
    }

    public ProducerProfile(String owner, long producerId, String producerName, String producerAddress, int producerPort,
                           String profilePath, String keyStorePath, String serverAddress, int serverPort) {
        this.owner = owner;
        this.producerId = producerId;
        this.producerName = producerName;
        this.producerAddress = producerAddress;
        this.producerPort = producerPort;
        this.profilePath = profilePath;
        this.keyStorePath = keyStorePath;
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
    }

    public static ProducerProfile localProfileFromFile(String path) throws IOException {
        File profilePath = new File(path);
        if (!profilePath.exists()) {
            throw new FileNotFoundException();
        }
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        mapper.registerModule(new ParameterNamesModule(JsonCreator.Mode.PROPERTIES));
        mapper.registerModule(new Jdk8Module());

        ProducerProfile profile = mapper.readValue(profilePath, ProducerProfile.class);
        profile.setProfilePath(profilePath.getAbsolutePath());
        return profile;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProducerProfile that = (ProducerProfile) o;
        return serverPort == that.serverPort &&
                serverAddress.equals(that.serverAddress);
    }

    private void setProfilePath(String profilePath) {
        this.profilePath = profilePath;
    }
    public String getServerAddress() {
        return this.serverAddress;
    }
    public void setServerAddress(String serverAddress) {
        this.serverAddress = serverAddress;
    }
    public int getServerPort() {
        return this.serverPort;
    }
    public void setServerPort(int port) {
        this.serverPort = port;
    }

    public String getOwner() {
        return owner;
    }

    public long getProducerId() {
        return producerId;
    }

    public String getProducerName() {
        return producerName;
    }

    public String getProducerAddress() {
        return producerAddress;
    }

    public int getProducerPort() {
        return producerPort;
    }

    public boolean syncProfile() throws CouldNotStoreException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        if (profilePath != null) {
            try {
                File parent = new File(profilePath).getParentFile();
                if (parent != null) {
                    parent.mkdirs();
                }
                mapper.writerWithDefaultPrettyPrinter().writeValue(new FileOutputStream(profilePath), this);
            } catch (IOException e) {
                throw new CouldNotStoreException("Error occurred during storing of the profile: " + e.getMessage());
            }
        }
        // TODO: Actually check if changes occurred since the last write.
        return true;
    }

}
