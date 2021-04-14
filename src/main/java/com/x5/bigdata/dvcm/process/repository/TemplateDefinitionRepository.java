package com.x5.bigdata.dvcm.process.repository;

import com.x5.bigdata.dvcm.process.entity.OfferTemplate;
import com.x5.bigdata.dvcm.process.entity.TemplateDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TemplateDefinitionRepository extends JpaRepository<TemplateDefinition, UUID> {

    TemplateDefinition getTemplateDefinitionByOfferTemplate(OfferTemplate offerTemplate);
}
