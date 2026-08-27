package controllerNettyServer;

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
import java.util.*;

public class Profile {
    @JsonIgnore
    private String path;
    private String profileName;
    private Map<String, List<Long>> usrStreams = new HashMap<>();
    public Profile() {
    }
    public Profile(String path, String profileName) {
        this.path = path;
        this.profileName = profileName;
        this.usrStreams = new HashMap<String, List<Long>>();
    }

    public Profile(String path, String profileName, Map<String, List<Long>> usrStreams) {
        this.path = path;
        this.profileName = profileName;
        this.usrStreams = usrStreams;
    }

    public static Profile localProfileFromFile(String path) throws IOException {
        File profilePath = new File(path);

        if (!profilePath.exists()) {
            throw new FileNotFoundException();
        }
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        mapper.registerModule(new ParameterNamesModule(JsonCreator.Mode.PROPERTIES));
        mapper.registerModule(new Jdk8Module());

        Profile profile = mapper.readValue(profilePath, Profile.class);
        profile.setPath(profilePath.getAbsolutePath());
        return profile;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Profile that = (Profile) o;
        return usrStreams.equals(that.usrStreams);
    }
    @Override
    public int hashCode() {
        return Objects.hash(usrStreams);
    }
    private void setPath(String profilePath) {
        this.path = profilePath;
    }
    public Map<String, List<Long>> getUsrStreams() {
        return usrStreams;
    }
    public String getProfileName() {
        return this.profileName;
    }
    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }
    public boolean addStream(String usrName, long streamID) throws CouldNotStoreException {
        if (usrStreams.containsKey(usrName)){
            List<Long> streams = usrStreams.get(usrName);
            streams.add(streamID);
            usrStreams.replace(usrName, streams);
        }
        else {
            List<Long> newStreams2 = new ArrayList<>();
            newStreams2.add(streamID);
            usrStreams.put(usrName, newStreams2);
        }
        System.out.println(usrStreams);
        return(syncProfile(true));
    }
    public boolean deleteStream(String usrName, long streamID) throws CouldNotStoreException {
        List<Long> newStreams = usrStreams.get(usrName);
        newStreams.remove(streamID);
        usrStreams.replace(usrName, newStreams);

        System.out.println(usrStreams);
        return(syncProfile(true));
    }
    public List<Long> getStreams(String usrName) {
        return usrStreams.get(usrName);
    }
    public boolean syncProfile(boolean force) throws CouldNotStoreException {
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

}
