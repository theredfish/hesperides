package org.hesperides.batch.redis.legacy.events;

import lombok.Value;
import org.hesperides.domain.templatecontainer.entities.TemplateContainer;

@Value
public class LegacyTemplateDeletedEvent {
    public static final String EVENT_TYPE = "com.vsct.dt.hesperides.templating.modules.ModuleTemplateDeletedEvent";

    String moduleName;
    String moduleVersion;
    String templateName;

    public TemplateContainer.Key key(){
        return new TemplateContainer.Key(moduleName,moduleVersion,TemplateContainer.Type.workingcopy);
    }
}
