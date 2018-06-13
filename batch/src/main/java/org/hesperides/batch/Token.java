package org.hesperides.batch;

import lombok.Data;
import org.hesperides.domain.templatecontainer.entities.TemplateContainer;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "token")
public class Token {

    public static final int WIP = 0;
    public static final int OK = 1;
    public static final int DELETED = 2;
    public static final int KO = 3;

    @Id
    private String key;
    private String type;
    private int status;
    private int legacyEventCount;
    private int refonteEventCount;
    @Nullable
    private TemplateContainer.Key refonteKey;

    public Token(String key, String type) {
        this.key = key;
        this.type = type;
        this.legacyEventCount = 0;
        this.refonteEventCount = 0;
        this.status = WIP;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setRefonteEventCount(int refonteEventCount) {
        this.refonteEventCount = refonteEventCount;
    }

    public void setLegacyEventCount(int legacyEventCount){
        this.legacyEventCount =legacyEventCount;
        setStatus(0);
    }


}
