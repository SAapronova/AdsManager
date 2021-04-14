package com.x5.bigdata.dvcm.process.service;

import com.x5.bigdata.dvcm.process.dto.CampaignSegmentDto;
import com.x5.bigdata.dvcm.process.dto.OfferDataDto;
import com.x5.bigdata.dvcm.process.entity.TemplateDefinition;
import com.x5.bigdata.dvcm.process.exception.ValidationException;
import com.x5.bigdata.dvcm.process.exception.ValidationItem;
import com.x5.bigdata.dvcm.process.repository.TemplateDefinitionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.x5.bigdata.dvcm.process.validators.ValidationMessages.NOT_POSITIVE_VALUE;
import static java.lang.String.format;

@Slf4j
@RequiredArgsConstructor
@Service
public class TemplateDefinitionServiceImpl implements TemplateDefinitionService {
    private final TemplateDefinitionRepository repository;

    @Override
    public void validateOfferData(List<CampaignSegmentDto> segments) {
        List<ValidationItem> errors = new ArrayList<>();
        for (int i = 0; i < segments.size(); i++) {
            CampaignSegmentDto segmentDto = segments.get(i);
            OfferDataDto offerDataDto = segmentDto.getOfferData();
            TemplateDefinition def = repository.getTemplateDefinitionByOfferTemplate(segmentDto.getOfferTemplate());

            if (Boolean.TRUE.equals(def.getHasPoints())) {
                validatePositive(offerDataDto.getPoints(), "points", i, errors);
            }
            if (Boolean.TRUE.equals(def.getHasMinSum())) {
                validatePositive(offerDataDto.getMinSum(), "min_sum", i, errors);
            }
            if (Boolean.TRUE.equals(def.getHasPurchases())) {
                validatePositive(offerDataDto.getPurchases(), "purchases_num", i, errors);
            }
            if (Boolean.TRUE.equals(def.getHasRewardPeriod())) {
                validatePositive(offerDataDto.getRewardPeriod(), "reward_period", i, errors);
            }
        }
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }

    private void validatePositive(Integer value, String name, int index, List<ValidationItem> errors) {
        if (Optional.ofNullable(value).orElse(0) < 1) {
            errors.add(new ValidationItem(format("segments[%s].%s", index, name), String.valueOf(value), NOT_POSITIVE_VALUE));
        }
    }
}
