package org.hesperides.batch.redis.legacy.events;

import com.google.gson.annotations.SerializedName;
import lombok.Value;
import org.hesperides.batch.redis.legacy.entities.LegacyTemplate;
import org.hesperides.domain.templatecontainer.entities.Template;
import org.hesperides.domain.templatecontainer.entities.TemplateContainer;

@Value
public class LegacyTemplateCreatedEvent {
    public static final String EVENT_TYPE = "com.vsct.dt.hesperides.templating.modules.ModuleTemplateCreatedEvent";

    String moduleName;
    String moduleVersion;
    @SerializedName("created")
    LegacyTemplate Lc;

    public Template toDomainTemplate(){
        return new Template(Lc.getName(),Lc.getFilename(),Lc.getLocation(),Lc.getContent(),Lc.getRights(),Lc.getVersionId(),new TemplateContainer.Key(moduleName,moduleVersion,TemplateContainer.Type.workingcopy));
    }
}
