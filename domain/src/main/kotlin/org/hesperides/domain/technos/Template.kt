package org.hesperides.domain.technos

import org.axonframework.commandhandling.TargetAggregateIdentifier
import org.hesperides.domain.templatecontainer.entities.Template
import org.hesperides.domain.security.User
import org.hesperides.domain.security.UserEvent
import org.hesperides.domain.templatecontainer.entities.TemplateContainer

// Command
data class CreateTemplateCommand(@TargetAggregateIdentifier val technoKey: TemplateContainer.Key, val template: Template, val user: User)
data class UpdateTemplateCommand(@TargetAggregateIdentifier val technoKey: TemplateContainer.Key, val template: Template, val user: User)
data class DeleteTemplateCommand(@TargetAggregateIdentifier val technoKey: TemplateContainer.Key, val templateName: String, val user: User)

// Event
data class TemplateCreatedEvent(val technoKey: TemplateContainer.Key, val template: Template, override val user: User) : UserEvent(user)
data class TemplateDeletedEvent(val technoKey: TemplateContainer.Key, val templateName: String, override val user: User) : UserEvent(user)
data class TemplateUpdatedEvent(val technoKey: TemplateContainer.Key, val template: Template, override val user: User) : UserEvent(user)

// Query
data class GetTemplateByNameQuery(val technoKey: TemplateContainer.Key, val templateName: String)
data class GetModuleTemplatesQuery(val technoKey: TemplateContainer.Key)
