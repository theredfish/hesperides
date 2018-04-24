package org.hesperides.batch.redis;

import com.google.gson.annotations.SerializedName;
import lombok.Value;
import org.hesperides.batch.LegacyModule;
import org.hesperides.domain.modules.entities.Module;
import org.hesperides.domain.security.User;
import org.hesperides.domain.templatecontainer.entities.TemplateContainer;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;

@Value
//@EqualsAndHashCode(callSuper = true)
public class LegacyModuleCreatedEvent implements LegacyInterface {
    public static final String EVENT_TYPE = "com.vsct.dt.hesperides.templating.modules.ModuleCreatedEvent";

    @SerializedName("moduleCreated")
    LegacyModule module;

    @SerializedName("templates")
    TemplateContainer templateContainer;

    public Module toDomainModule(){
        return new Module(module.getKey(),
               templateContainer.getTemplates(),
               module.getTechnos(),
                module.getVersionId())
    }

}
