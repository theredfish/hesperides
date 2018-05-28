package org.hesperides.batch.redis;

import com.google.gson.annotations.SerializedName;
import lombok.Value;
import org.hesperides.batch.LegacyModule;
import org.hesperides.domain.templatecontainer.entities.Template;
//import org.hibernate.validator.constraints.NotEmpty;
//
//import javax.validation.constraints.NotNull;

@Value
public class LegacyTemplateUpdatedEvent implements LegacyInterface{
    public static final String EVENT_TYPE ="com.vsct.dt.hesperides.templating.modules.ModuleTemplateUpdatedEvent" ;
//    @NotNull
//    @NotEmpty
    @SerializedName("moduleName")
    String name;

//    @NotNull
//    @NotEmpty
    String version;
//
//    @NotNull
//    @NotEmpty
    @SerializedName("updated")
    Template template;
}
