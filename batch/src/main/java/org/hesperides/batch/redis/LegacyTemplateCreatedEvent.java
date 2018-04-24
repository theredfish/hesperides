package org.hesperides.batch.redis;

import com.google.gson.annotations.SerializedName;
import lombok.Value;
import org.hesperides.batch.LegacyModule;
import org.hesperides.batch.LegacyTemplate;
import org.hesperides.domain.modules.entities.Template;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;

@Value
public class LegacyTemplateCreatedEvent implements LegacyInterface{
    public static final String EVENT_TYPE = "com.vsct.dt.hesperides.templating.modules.ModuleTemplateCreatedEvent";
    @NotNull
    @NotEmpty
    @SerializedName("moduleName")
    String name;

    @NotNull
    @NotEmpty
    @SerializedName("moduleVersion")
    String version;

    @NotNull
    @NotEmpty
    @SerializedName("created")
    LegacyTemplate template;
}
