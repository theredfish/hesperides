package org.hesperides.presentation.inputs;

import com.google.gson.annotations.SerializedName;
import lombok.Value;
import org.hesperides.domain.modules.entities.Module;
import org.hesperides.domain.templatecontainer.entities.TemplateContainer;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.util.Set;
import java.util.stream.Collectors;

@Value
public final class ModuleInput {
    @NotNull
    @NotEmpty
    String name;

    @NotNull
    @NotEmpty
    String version;

    @SerializedName("working_copy")
    boolean isWorkingCopy;

    Set<TechnoInput> technos;

    @SerializedName("version_id")
    Long versionId;

    Set<TemplateInput> templates;

    public Module toDomainInstance() {
        TemplateContainer.Key moduleKey = new TemplateContainer.Key(name,version,isWorkingCopy ? TemplateContainer.Type.workingcopy : TemplateContainer.Type.release);
        return new Module(moduleKey,
                templates !=null ? templates.stream().map(templateInput -> templateInput.toDomainInstance(moduleKey)).collect(Collectors.toList()) : null,
               null,
                versionId
        );
    }

}
