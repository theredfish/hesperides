package org.hesperides.batch.redis.legacy.events.modules;

import com.google.gson.annotations.SerializedName;
import org.hesperides.batch.redis.legacy.entities.LegacyModule;
import org.hesperides.batch.redis.legacy.events.LegacyInterface;
import org.hesperides.domain.modules.ModuleTechnosUpdatedEvent;
import org.hesperides.domain.security.User;
import org.hesperides.domain.templatecontainer.entities.TemplateContainer;

public class LegacyModuleUpdatedEvent implements LegacyInterface {

    @SerializedName("updated")
    LegacyModule module;


    @Override
    public TemplateContainer.Key getKey() {
        return module.getKey();
    }

    @Override
    public Object toDomainEvent(User user) {
        return new ModuleTechnosUpdatedEvent(getKey(),module.getTechno(),module.getVersionId(),user);
    }

    @Override
    public String getKeyString() {
        return module.getKey().toString("module");
    }
}
