package org.hesperides.batch.redis.legacy.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import lombok.Value;
import org.hesperides.domain.modules.entities.Module;
import org.hesperides.domain.technos.entities.Techno;
import org.hesperides.domain.templatecontainer.entities.TemplateContainer;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.net.URI;
import java.util.Collection;
import java.util.List;

@Value
public class LegacyModule  {

    String name;
    String version;
    @SerializedName("working_copy")
    boolean workingCopy;
    List<Techno> technos;
    @SerializedName("version_id")
    Long versionId;

    public Module.Type getModuleType() {
        return workingCopy ? Module.Type.workingcopy : Module.Type.release;
    }

    public Module.Key getKey(){
        return new Module.Key(name,version,this.getModuleType());
    }
}
