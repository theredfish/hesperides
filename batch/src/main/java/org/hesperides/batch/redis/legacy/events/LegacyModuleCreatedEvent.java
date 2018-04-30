package org.hesperides.batch.redis.legacy.events;

import com.google.gson.annotations.SerializedName;
import lombok.Value;
import org.hesperides.batch.redis.legacy.entities.LegacyModule;
import org.hesperides.domain.modules.entities.Module;
import org.hesperides.domain.templatecontainer.entities.Template;

import java.util.List;

@Value
//@EqualsAndHashCode(callSuper = true)
public class LegacyModuleCreatedEvent  {
    public static final String EVENT_TYPE = "com.vsct.dt.hesperides.templating.modules.ModuleCreatedEvent";

    @SerializedName("moduleCreated")
    LegacyModule module;


    List<Template> templates;




    public Module toDomainModule(){
        return new Module(module.getKey(),
               templates,
               module.getTechnos(),
                module.getVersionId());
    }

}
