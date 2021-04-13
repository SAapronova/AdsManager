package com.x5.bigdata.dvcm.process.validators;

public final class ValidationMessages {
    private ValidationMessages() {
        throw new IllegalStateException("Utility class");
    }

    public static final String CAMPAIGN_ALREADY_EXISTS = "Кампания уже зарегистрирована";
    public static final String WRONG_PERIOD = "Дата начала не может быть больше даты окончания";
    public static final String WRONG_POST_PERIOD = "Дата окончания пост периода не может быть меньше даты окончания";
}
