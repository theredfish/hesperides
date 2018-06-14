package org.hesperides.batch.legacy.entities;

import com.google.gson.annotations.SerializedName;
import lombok.Value;
import org.hesperides.domain.modules.entities.Module;
import org.hesperides.domain.technos.entities.Techno;
import org.hesperides.domain.templatecontainer.entities.Template;
import org.hesperides.domain.templatecontainer.entities.TemplateContainer;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Value
public class LegacyModule  {

    String name;
    String version;
    @SerializedName("working_copy")
    boolean workingCopy;
    List<LegacyTechno> technos;
    @SerializedName("version_id")
    Long versionId;


    public Module.VersionType getModuleType() {
        return workingCopy ? Module.VersionType.workingcopy : Module.VersionType.release;
    }

    public LegacyModule(String name, String version, boolean workingCopy, List<LegacyTechno> technos, Long versionId) {
        this.name = name;
        this.version = version;
        this.workingCopy = workingCopy;
        this.technos = technos;
        this.versionId = versionId;
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
        return new Module(getKey(),templates,
                technos !=null ? technos.stream().map(LegacyTechno::toDomainInstance).collect(Collectors.toList()):null,versionId);

    }
    public List<Techno> getTechno(){
        List<Techno> retour = new ArrayList<>();
        technos.forEach(techno -> retour.add(techno.toDomainInstance()));
        return retour;
    }
}
