package org.hesperides.batch.redis.legacy.events.technos;

import com.google.gson.annotations.SerializedName;
import org.hesperides.batch.redis.legacy.entities.LegacyTemplate;
import org.hesperides.batch.redis.legacy.events.LegacyInterface;
import org.hesperides.domain.modules.TemplateUpdatedEvent;
import org.hesperides.domain.security.User;
import org.hesperides.domain.templatecontainer.entities.TemplateContainer;

public class LegacyTechnoTemplateUpdatedEvent implements LegacyInterface {
    @SerializedName("updated")
    LegacyTemplate legacyTemplate;

    @Override
    public TemplateContainer.Key getKey() {
        return legacyTemplate.getKeyFromNamespace();
    }

    @Override
    public Object toDomainEvent(User user) {
        TemplateContainer.Key key = getKey();
        //TODO determiner si il y a un interet/risque à utiliser la même méthode que celle du module
        return new TemplateUpdatedEvent(key,legacyTemplate.toDomainTemplate(key),user);
    }
}
