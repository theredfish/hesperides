package org.hesperides.batch;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import lombok.extern.java.Log;
import org.axonframework.eventhandling.GenericEventMessage;
import org.axonframework.eventsourcing.GenericDomainEventMessage;
import org.axonframework.eventsourcing.eventstore.EmbeddedEventStore;
import org.axonframework.messaging.MetaData;
import org.hesperides.batch.redis.*;
import org.hesperides.domain.modules.ModuleCreatedEvent;
import org.hesperides.domain.modules.entities.Module;
import org.hesperides.domain.security.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Component;

import static org.axonframework.commandhandling.model.AggregateLifecycle.apply;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@Log
@Component
@Profile("batch")
public class Unnamed {

    private ApplicationRunner titledRunner(String title, ApplicationRunner rr){
        return args -> {
            log.info(title.toUpperCase() + " : ");
            rr.run(args);
        };
    }
    @Autowired
    EmbeddedEventStore eventBus;

    @Bean
    ApplicationRunner moduleImport(RedisTemplate<String,LegacyEvent> rt){
        return titledRunner("moduleImport",args ->{
            RedisConnection redisConnection = null;
            ListOperations<String,LegacyEvent> mod = rt.opsForList();

            Map<String,Long> moduleList = new HashMap<>();
            try{
                redisConnection =  rt.getConnectionFactory().getConnection();
                ScanOptions options = ScanOptions.scanOptions().match("module*").count(10).build();
                Cursor c = redisConnection.scan(options);
                while (c.hasNext()){
                    String name = new String((byte[]) c.next());
                    moduleList.put(name,mod.size(name));
                }
            }finally {
                redisConnection.close();
            }

            for(Map.Entry<String,Long> entry : moduleList.entrySet()){
                log.info(entry.getKey() + " : " + entry.getValue() + " events");
//                ToBeRenamed(mod.index(entry.getKey(),0L),0L);
                for(Long i = 0L; i < entry.getValue();i++){
                    LegacyEvent tmp = mod.index(entry.getKey(),i);
                    log.info("event"+i+" : "+tmp.eventType);
                    ToBeRenamed(tmp,i);
                }
            }
        });
    }

    private void ToBeRenamed(LegacyEvent event,Long index) {
        ObjectMapper mapper = new ObjectMapper();
        Long timestamp = event.timestamp;
        Supplier<Instant>supplier = ()-> Instant.ofEpochMilli(timestamp);
        Gson gson = new Gson();
        GenericEventMessage eventMessage;
        GenericDomainEventMessage domainEventMessage = null;
        switch (event.getEventType()){
            case LegacyModuleCreatedEvent.EVENT_TYPE :
                LegacyModuleCreatedEvent lmce = gson.fromJson(event.data,LegacyModuleCreatedEvent.class);
                ModuleCreatedEvent forged = new ModuleCreatedEvent(new Module(lmce.getModule().getKey(),lmce.getModule().getTechnos(),lmce.getModule().getVersionId()),new User(event.user));
                eventMessage = new GenericEventMessage(forged,MetaData.emptyInstance());
                domainEventMessage = new GenericDomainEventMessage("ModuleAggregate",lmce.getModule().getKey().toString(),index,eventMessage,supplier);
                break;
            case LegacyModuleUpdatedEvent.EVENT_TYPE :
//                LegacyTemplateCreatedEvent chier =  gson.fromJson(event.data,LegacyTemplateCreatedEvent.class);
//                log.info(chier.getTemplate().toString());
                break;
            case LegacyModuleDeletedEvent.EVENT_TYPE:
                break;

            case LegacyTemplateCreatedEvent.EVENT_TYPE:
                LegacyTemplateCreatedEvent ltce = gson.fromJson(event.data,LegacyTemplateCreatedEvent.class);
                Module
                eventMessage = new GenericEventMessage()
                break;
            case LegacyTemplateUpdatedEvent.EVENT_TYPE:
                break;
            case LegacyTemplateDeletedEvent.EVENT_TYPE:
                break;
                default:
                    throw new UnsupportedOperationException("Deserialization for class " + event.getEventType() + " is not implemented");

        }
        try{
            eventBus.publish(domainEventMessage);

        }
        catch (Exception e){
            log.info("Aie");
        }
    }

    @Bean
    RedisTemplate<String,LegacyEvent> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String,LegacyEvent> template = new RedisTemplate<>();

        RedisSerializer<LegacyEvent> values = new Jackson2JsonRedisSerializer<>(LegacyEvent.class);
        RedisSerializer keys = new StringRedisSerializer();

        template.setConnectionFactory(redisConnectionFactory);
        template.setKeySerializer(keys);
        template.setHashKeySerializer(keys);
        template.setValueSerializer(values);
        template.setHashValueSerializer(values);

        return template;
    }
}



//                    log.info(data.getLegacyModule().toString());
//                    ObjectMapper mapper = new ObjectMapper();
//                    Map<String,LegacyModuleCreatedEvent> map = mapper.readValue(tmp.data, Map.class);
//                    for(Map.Entry<String,?> entrybis : map.entrySet() ){
//                        log.info(entrybis.getKey() + " " + entrybis.getValue());
//                    }
//
//
//
//
//
//    Long burp = 1506607131563L;
//    Supplier<Instant>sup = ()-> Instant.ofEpochMilli(burp);
//    Module.Key key = new Module.Key("module","1.0",Module.Type.workingcopy);
//    Module module = new Module(key,null,0L);
//    User user = new User("bob");
//
//    //correspond à la clef de l'event stockée dans la collection de l'entité sur redis (soustrait de 1)
//    //e.g. le premier event d'une entitée
//    //TODO inclure cette donnée dans le token store
//    Long sequenceNumber = 0L;
//
//
//    ModuleCreatedEvent event = new ModuleCreatedEvent(module,user);
//    GenericEventMessage eventMessage = new GenericEventMessage(event,MetaData.emptyInstance());
//    GenericDomainEventMessage domainEventMessage = new GenericDomainEventMessage("ModuleAggregate",key.toString(),sequenceNumber,eventMessage,sup);
//
//
//    public void publish(){
////        eventBus.publish(domainEventMessage);
//    }
//
//    public void getredis(){
//
//    }
//
//    public String Decode(LegacyEvent legacyEvent){
//        String payload = legacyEvent.getData();
//
//        //TODO Escaping de la data en instant,
//        return payload;
//    }
//
////TODO Conversion du timestamp
////    public Instant toInstant(Long timestamp){
////        return new Instant().;
////    }