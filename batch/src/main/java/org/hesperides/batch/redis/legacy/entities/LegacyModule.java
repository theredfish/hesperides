package org.hesperides.batch.redis.legacy.entities;

import com.google.gson.annotations.SerializedName;
import lombok.Value;
import org.hesperides.domain.modules.entities.Module;
import org.hesperides.domain.technos.entities.Techno;
import org.hesperides.domain.templatecontainer.entities.Template;
import org.hesperides.domain.templatecontainer.entities.TemplateContainer;
import java.util.ArrayList;
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

    public Module toDomainModule(List<LegacyTemplate> legacyTemplates){
        TemplateContainer.Key key = getKey();
        List<Template> templates = new ArrayList<>();

        if (legacyTemplates != null) {
            legacyTemplates.forEach(legacyTemplate -> templates.add(legacyTemplate.toDomainTemplate(key)));
        }
        //TODO implémentation techno
        return new Module(getKey(),templates,null,versionId);

    }
}
