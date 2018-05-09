package org.hesperides.infrastructure.mongo.technos.commands;

import org.axonframework.eventsourcing.EventSourcingHandler;
import org.hesperides.domain.technos.TemplateCreatedEvent;
import org.hesperides.domain.technos.TemplateDeletedEvent;
import org.hesperides.domain.technos.TemplateUpdatedEvent;
import org.hesperides.domain.technos.commands.TechnoTemplateCommandsRepository;
import org.hesperides.domain.templatecontainer.entities.TemplateContainer;
import org.hesperides.infrastructure.mongo.technos.MongoTechnoRepository;
import org.hesperides.infrastructure.mongo.technos.TechnoDocument;
import org.hesperides.infrastructure.mongo.templatecontainer.TemplateDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;

import static org.hesperides.domain.Profiles.EMBEDDED_MONGO;
import static org.hesperides.domain.Profiles.FAKE_MONGO;
import static org.hesperides.domain.Profiles.MONGO;

@Profile({MONGO, EMBEDDED_MONGO, FAKE_MONGO})
@Repository
public class MongoTechnoTemplateCommandsRepository implements TechnoTemplateCommandsRepository {

    private final MongoTechnoRepository repository;

    @Autowired
    public MongoTechnoTemplateCommandsRepository(MongoTechnoRepository repository) {
        this.repository = repository;
    }

    @Override
    @EventSourcingHandler
    public void on(TemplateCreatedEvent event) {
        TemplateContainer.Key key = event.getTechnoKey();
        TechnoDocument technoDocument = repository.findByNameAndVersionAndWorkingCopy(key.getName(), key.getVersion(), key.isWorkingCopy());
        TemplateDocument newTemplate = TemplateDocument.fromDomain(event.getTemplate());
        if (technoDocument.getTemplates() == null) {
            technoDocument.setTemplates(new ArrayList<>());
        }
        technoDocument.getTemplates().add(newTemplate);
        repository.save(technoDocument);
    }

    @Override
    @EventSourcingHandler
    public void on(TemplateUpdatedEvent event) {
        TemplateContainer.Key key = event.getTechnoKey();
        TechnoDocument techno = repository.findByNameAndVersionAndWorkingCopy(key.getName(), key.getVersion(), key.isWorkingCopy());
        for (int i = 0; i < techno.getTemplates().size(); i++) {
            if (techno.getTemplates().get(i).getName().equalsIgnoreCase(event.getTemplate().getName())) {
                techno.getTemplates().set(i, TemplateDocument.fromDomain(event.getTemplate()));
                break;
            }
        }
        repository.save(techno);

    }

    @Override
    @EventSourcingHandler
    public void on(TemplateDeletedEvent event) {
        TemplateContainer.Key key = event.getTechnoKey();
        TechnoDocument techno = repository.findByNameAndVersionAndWorkingCopy(key.getName(), key.getVersion(), key.isWorkingCopy());
        techno.getTemplates().removeIf(template -> template.getName().equalsIgnoreCase(event.getTemplateName()));
        repository.save(techno);
    }
}
