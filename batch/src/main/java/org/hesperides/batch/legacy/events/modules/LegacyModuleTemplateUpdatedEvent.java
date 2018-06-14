package org.hesperides.batch.legacy.events.modules;

import com.google.gson.annotations.SerializedName;
import lombok.Value;
import org.hesperides.batch.legacy.entities.LegacyTemplate;
import org.hesperides.batch.legacy.events.LegacyInterface;
import org.hesperides.domain.modules.TemplateUpdatedEvent;
import org.hesperides.domain.security.User;
import org.hesperides.domain.templatecontainer.entities.TemplateContainer;

@Value
public class LegacyModuleTemplateUpdatedEvent implements LegacyInterface {
    public static final String EVENT_TYPE ="com.vsct.dt.hesperides.templating.modules.ModuleTemplateUpdatedEvent" ;
    String moduleName;
    String moduleVersion;
    @SerializedName("updated")
    LegacyTemplate legacyTemplate;

    @Override
    public TemplateUpdatedEvent toDomainEvent(User user) {
        TemplateContainer.Key key = getKey();
        return new TemplateUpdatedEvent(key,legacyTemplate.toDomainTemplate(key),user);
    }
    @Override
    public TemplateContainer.Key getKey(){
        return new TemplateContainer.Key(
                moduleName,
                moduleVersion,
                TemplateContainer.VersionType.workingcopy);
    }
}
