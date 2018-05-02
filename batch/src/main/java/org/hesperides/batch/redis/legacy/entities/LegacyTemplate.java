package org.hesperides.batch.redis.legacy.entities;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;
import org.hesperides.domain.templatecontainer.entities.Template;
import org.hesperides.domain.templatecontainer.entities.TemplateContainer;

@Value
public class LegacyTemplate {
    String name;
    String filename;
    String location;
    String content;
    String namespace;
    Template.Rights rights;

    @SerializedName("version_id")
    Long versionId;

    public Template toDomainTemplate(TemplateContainer.Key key){
        return new Template(name,filename,location,content,rights,versionId,key);
    }

    public TemplateContainer.Key getKeyFromNamespace(){
        String[] temp = namespace.split("#");
        return new TemplateContainer.Key(temp[1],temp[2],temp[3] == "WORKINGCOPY" ? TemplateContainer.Type.workingcopy : TemplateContainer.Type.release);
    }
}