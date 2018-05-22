package org.hesperides.batch;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
public class LegacyTemplate {
    String name;
    String filename;
    String location;
    String content;
    Rights rights;

    @SerializedName("moduleVersion")
    Long versionId;
//    Module.Key moduleKey;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Rights {
        FileRights user;
        FileRights group;
        FileRights other;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FileRights {
        Boolean read;
        Boolean write;
        Boolean execute;
    }
}