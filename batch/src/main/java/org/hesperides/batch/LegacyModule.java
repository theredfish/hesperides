package org.hesperides.batch;

import com.google.gson.annotations.SerializedName;
import lombok.Value;
import org.hesperides.domain.modules.entities.Module;
import org.hesperides.domain.technos.entities.Techno;

import java.util.List;

@Value
public class LegacyModule {

    String name;
    String version;
    @SerializedName("working_copy")
    boolean workingCopy;
    List<Techno> technos;
    @SerializedName("version_id")
    Long versionId;

    public Module.VersionType getModuleType() {
        return workingCopy ? Module.VersionType.workingcopy : Module.VersionType.release;
    }

    public Module.Key getKey(){
        return new Module.Key(this.name,this.version,this.getModuleType());
    }
}
