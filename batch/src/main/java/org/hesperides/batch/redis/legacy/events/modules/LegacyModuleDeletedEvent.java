package org.hesperides.batch.redis.legacy.events.modules;

import lombok.Value;
import org.hesperides.batch.redis.legacy.events.LegacyInterface;
import org.hesperides.domain.modules.ModuleDeletedEvent;
import org.hesperides.domain.security.User;
import org.hesperides.domain.templatecontainer.entities.TemplateContainer;

@Value
public class LegacyModuleDeletedEvent implements LegacyInterface {
    public static final String EVENT_TYPE = "com.vsct.dt.hesperides.templating.modules.ModuleDeletedEvent";

    String moduleName;
    String moduleVersion;
    boolean workingCopy;

    @Override
    public TemplateContainer.Key getKey() {
        return new TemplateContainer.Key(moduleName,moduleVersion,workingCopy ? TemplateContainer.Type.workingcopy : TemplateContainer.Type.release);
    }

    @Override
    public Object toDomainEvent(User user) {
        return new ModuleDeletedEvent(getKey(),user);
    }
}
