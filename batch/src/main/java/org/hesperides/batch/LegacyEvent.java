package org.hesperides.batch;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LegacyEvent {
    String eventType;
    String data;
    Long timestamp;
    String user;

}
