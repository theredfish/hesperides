package org.hesperides.batch.legacy.events.modules;

import com.google.gson.annotations.SerializedName;
import lombok.Value;
import org.hesperides.batch.legacy.entities.LegacyTemplate;
import org.hesperides.batch.legacy.events.LegacyInterface;
import org.hesperides.domain.modules.TemplateCreatedEvent;
import org.hesperides.domain.security.User;
import org.hesperides.domain.templatecontainer.entities.TemplateContainer;

@Value
public class LegacyModuleTemplateCreatedEvent implements LegacyInterface {
    public static final String EVENT_TYPE = "com.vsct.dt.hesperides.templating.modules.ModuleTemplateCreatedEvent";

    String moduleName;
    String moduleVersion;
    @SerializedName("created")
    LegacyTemplate legacyTemplate;
//
//    public Template toDomainTemplate(){
//        return LegacyTemplate.;
//    }

    @Override
    public TemplateCreatedEvent toDomainEvent(User user) {
        TemplateContainer.Key key = getKey();
        return new TemplateCreatedEvent(key,legacyTemplate.toDomainTemplate(key),user);
    }

    @Override
    public TemplateContainer.Key getKey(){
        return new TemplateContainer.Key(
                moduleName,
                moduleVersion,
                TemplateContainer.VersionType.workingcopy);
    }
}
