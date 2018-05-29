package org.hesperides.batch.redis.legacy.events.modules;

import com.google.gson.annotations.SerializedName;
import lombok.Value;
import org.hesperides.batch.redis.legacy.entities.LegacyModule;
import org.hesperides.batch.redis.legacy.entities.LegacyTemplate;
import org.hesperides.batch.redis.legacy.events.LegacyInterface;
import org.hesperides.domain.modules.ModuleCreatedEvent;
import org.hesperides.domain.modules.entities.Module;
import org.hesperides.domain.security.User;
import org.hesperides.domain.templatecontainer.entities.Template;
import org.hesperides.domain.templatecontainer.entities.TemplateContainer;

import java.util.List;

@Value
//@EqualsAndHashCode(callSuper = true)
public class LegacyModuleCreatedEvent implements LegacyInterface {
    public static final String EVENT_TYPE = "com.vsct.dt.hesperides.templating.modules.ModuleCreatedEvent";

    @SerializedName("moduleCreated")
    LegacyModule module;


    List<LegacyTemplate> templates;


    @Override
    public TemplateContainer.Key getKey() {
        return (module.getKey());
    }

    @Override
    public Object toDomainEvent(User user) {
        return new ModuleCreatedEvent(module.toDomainModule(templates),user);
    }

}
