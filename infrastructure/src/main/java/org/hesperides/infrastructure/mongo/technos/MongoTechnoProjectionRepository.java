/*
 *
 * This file is part of the Hesperides distribution.
 * (https://github.com/voyages-sncf-technologies/hesperides)
 * Copyright (c) 2016 VSCT.
 *
 * Hesperides is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, version 3.
 *
 * Hesperides is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */
package org.hesperides.infrastructure.mongo.technos;

import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.hesperides.domain.technos.*;
import org.hesperides.domain.technos.entities.Techno;
import org.hesperides.domain.technos.queries.TechnoView;
import org.hesperides.domain.templatecontainers.entities.TemplateContainer;
import org.hesperides.domain.templatecontainers.queries.AbstractPropertyView;
import org.hesperides.domain.templatecontainers.queries.TemplateView;
import org.hesperides.infrastructure.mongo.templatecontainers.AbstractPropertyDocument;
import org.hesperides.infrastructure.mongo.templatecontainers.KeyDocument;
import org.hesperides.infrastructure.mongo.templatecontainers.TemplateDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.hesperides.domain.framework.Profiles.FAKE_MONGO;
import static org.hesperides.domain.framework.Profiles.MONGO;

@Profile({MONGO, FAKE_MONGO})
@Repository
public class MongoTechnoProjectionRepository implements TechnoProjectionRepository {

    private final MongoTechnoRepository technoRepository;

    @Autowired
    public MongoTechnoProjectionRepository(MongoTechnoRepository technoRepository) {
        this.technoRepository = technoRepository;
    }

    /*** EVENT HANDLERS ***/

    @EventSourcingHandler
    @Override
    public void onTechnoCreatedEvent(TechnoCreatedEvent event) {
        TechnoDocument technoDocument = new TechnoDocument(event.getTechno());
        technoDocument.extractPropertiesAndSave(technoRepository);
    }

    @Override
    public void onTechnoDeletedEvent(TechnoDeletedEvent event) {
        KeyDocument keyDocument = new KeyDocument(event.getTechnoKey());
        technoRepository.deleteByKey(keyDocument);
    }

    @EventSourcingHandler
    @Override
    public void onTemplateAddedToTechnoEvent(TemplateAddedToTechnoEvent event) {
        KeyDocument keyDocument = new KeyDocument(event.getTechnoKey());
        TechnoDocument technoDocument = technoRepository.findByKey(keyDocument);
        TemplateDocument templateDocument = new TemplateDocument(event.getTemplate());
        technoDocument.addTemplate(templateDocument);
        technoDocument.extractPropertiesAndSave(technoRepository);
    }

    @Override
    public void onTechnoTemplateUpdatedEvent(TechnoTemplateUpdatedEvent event) {
        KeyDocument keyDocument = new KeyDocument(event.getTechnoKey());
        TechnoDocument technoDocument = technoRepository.findByKey(keyDocument);
        TemplateDocument templateDocument = new TemplateDocument(event.getTemplate());
        technoDocument.updateTemplate(templateDocument);
        technoDocument.extractPropertiesAndSave(technoRepository);
    }

    @Override
    public void onTechnoTemplateDeletedEvent(TechnoTemplateDeletedEvent event) {
        KeyDocument keyDocument = new KeyDocument(event.getTechnoKey());
        TechnoDocument technoDocument = technoRepository.findByKey(keyDocument);
        technoDocument.removeTemplate(event.getTemplateName());
        technoDocument.extractPropertiesAndSave(technoRepository);
    }

    /*** QUERY HANDLERS ***/

    @QueryHandler
    @Override
    public Optional<TemplateView> onGetTemplateQuery(GetTemplateQuery query) {
        Optional<TemplateView> optionalTemplateView = Optional.empty();
        TemplateContainer.Key key = query.getTechnoKey();

        KeyDocument keyDocument = new KeyDocument(query.getTechnoKey());
        Optional<TechnoDocument> optionalTechnoDocument = technoRepository.findOptionalByKeyAndTemplatesName(keyDocument, query.getTemplateName());

        if (optionalTechnoDocument.isPresent()) {
            TemplateDocument templateDocument = optionalTechnoDocument.get().getTemplates().stream()
                    .filter(template -> template.getName().equalsIgnoreCase(query.getTemplateName()))
                    .findAny().get();
            optionalTemplateView = Optional.of(templateDocument.toTemplateView(key));
        }
        return optionalTemplateView;
    }

    @QueryHandler
    @Override
    public Boolean onTechnoAlreadyExistsQuery(TechnoAlreadyExistsQuery query) {
        KeyDocument keyDocument = new KeyDocument(query.getTechnoKey());
        Optional<TechnoDocument> technoDocument = technoRepository.findOptionalByKey(keyDocument);
        return technoDocument.isPresent();
    }

    @Override
    public List<TemplateView> onGetTemplatesQuery(GetTemplatesQuery query) {
        List<TemplateView> templateViews = new ArrayList<>();
        TemplateContainer.Key key = query.getTechnoKey();

        KeyDocument keyDocument = new KeyDocument(query.getTechnoKey());
        Optional<TechnoDocument> optionalTechnoDocument = technoRepository.findOptionalByKey(keyDocument);

        if (optionalTechnoDocument.isPresent()) {
            templateViews = optionalTechnoDocument.get().getTemplates().stream()
                    .map(templateDocument -> templateDocument.toTemplateView(key))
                    .collect(Collectors.toList());
        }
        return templateViews;
    }

    @Override
    public Optional<TechnoView> onGetTechnoQuery(GetTechnoQuery query) {
        Optional<TechnoView> optionalTechnoView = Optional.empty();
        KeyDocument keyDocument = new KeyDocument(query.getTechnoKey());
        Optional<TechnoDocument> optionalTechnoDocument = technoRepository.findOptionalByKey(keyDocument);
        if (optionalTechnoDocument.isPresent()) {
            optionalTechnoView = Optional.of(optionalTechnoDocument.get().toTechnoView());
        }
        return optionalTechnoView;
    }

    @Override
    public List<TechnoView> onSearchTechnosQuery(SearchTechnosQuery query) {
        String[] values = query.getInput().split(" ");
        String name = values.length >= 1 ? values[0] : "";
        String version = values.length >= 2 ? values[1] : "";

        Pageable pageableRequest = new PageRequest(0, 10); //TODO Sortir cette valeur dans le fichier de configuration
        List<TechnoDocument> technoDocuments = technoRepository.findAllByKeyNameLikeAndAndKeyVersionLike(name, version, pageableRequest);
        return technoDocuments.stream().map(TechnoDocument::toTechnoView).collect(Collectors.toList());
    }

    @Override
    public List<AbstractPropertyView> onGetTechnoPropertiesQuery(GetTechnoPropertiesQuery query) {
        KeyDocument keyDocument = new KeyDocument(query.getTechnoKey());
        TechnoDocument technoDocument = technoRepository.findByKey(keyDocument);
        return AbstractPropertyDocument.toAbstractPropertyViews(technoDocument.getProperties());
    }

    public List<TechnoDocument> getTechnoDocumentsFromDomainInstances(List<Techno> technos) {
        List<TechnoDocument> technoDocuments = null;
        if (technos != null) {
            List<KeyDocument> keyDocuments = technos.stream().map(techno -> new KeyDocument(techno.getKey())).collect(Collectors.toList());
            technoDocuments = technoRepository.findAllByKeyIn(keyDocuments);
        }
        return technoDocuments;
    }
}
