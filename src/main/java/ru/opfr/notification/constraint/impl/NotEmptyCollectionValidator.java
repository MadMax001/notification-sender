package ru.opfr.notification.constraint.impl;

import ru.opfr.notification.constraint.NotEmptyCollection;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Collection;
import java.util.Objects;

public class NotEmptyCollectionValidator implements ConstraintValidator<NotEmptyCollection, Collection> {
    @Override
    public boolean isValid(Collection ts, ConstraintValidatorContext constraintValidatorContext) {
        return Objects.nonNull(ts) && !ts.isEmpty();
    }
}
