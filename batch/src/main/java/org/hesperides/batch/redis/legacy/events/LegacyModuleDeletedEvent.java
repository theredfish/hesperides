package org.hesperides.batch.redis.legacy.events;

import lombok.Value;
import org.hesperides.domain.templatecontainer.entities.TemplateContainer;

@Value
public class LegacyModuleDeletedEvent {
    public static final String EVENT_TYPE = "com.vsct.dt.hesperides.templating.modules.ModuleDeletedEvent";

    String moduleName;
    String moduleVersion;
    boolean workingCopy;

    public TemplateContainer.Key key(){
        return new TemplateContainer.Key(moduleName,moduleVersion,workingCopy ? TemplateContainer.Type.workingcopy : TemplateContainer.Type.release);
    }
}
