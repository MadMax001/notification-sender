package ru.opfr.notification.constraint;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.opfr.notification.constraint.impl.NotEmptyCollectionValidator;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class NotEmptyCollectionValidatorTest {
    private NotEmptyCollectionValidator validator;

    @BeforeEach
    void setUp() {
        validator = new NotEmptyCollectionValidator();
    }

    @Test
    void validateForSingletonCollection() {
        assertTrue(validator.isValid(Collections.singletonList(new Object()), null));
    }

    @Test
    void validateForEmptyCollection() {
        assertFalse(validator.isValid(Collections.emptyList(), null));
    }

    @Test
    void validateForNullCollection() {
        assertFalse(validator.isValid(null, null));
    }

}