package org.hesperides.batch.redis;

import com.google.gson.annotations.SerializedName;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.hesperides.batch.LegacyEvent;
import org.hesperides.batch.LegacyModule;
import org.hesperides.domain.modules.entities.Module;
import org.hesperides.domain.modules.entities.Template;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;

@Value
//@EqualsAndHashCode(callSuper = true)
public class LegacyModuleCreatedEvent implements LegacyInterface {
    public static final String EVENT_TYPE = "com.vsct.dt.hesperides.templating.modules.ModuleCreatedEvent";

    @SerializedName("moduleCreated")
    LegacyModule module;

    Template[] templates;

}
