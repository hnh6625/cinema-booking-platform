package com.hnh6625.cinema.auth;



import com.hnh6625.cinema.auth.exception.InvalidPhoneNumberException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PhoneNumberNormalizerTest {

    @ParameterizedTest
    @CsvSource({
            "0901234567, +84901234567",
            "+84901234567, +84901234567",
            "84901234567, +84901234567",
            "'090 123 4567', +84901234567",
            "090-123-4567, +84901234567",
            "'  0901234567  ', +84901234567",
            "0351234567, +84351234567",
            "0771234567, +84771234567",
            "0891234567, +84891234567"
    })
    void normalize_convertsValidNumbersToE164(String raw, String expected) {
        assertThat(PhoneNumberNormalizer.normalize(raw)).isEqualTo(expected);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {
            "   ",
            "abc",
            "0123456789",
            "0201234567",
            "090123456",
            "09012345678",
            "+8490123456",
            "+12025550100"
    })
    void normalize_rejectInvalidNumbers(String raw) {
        assertThatThrownBy(() -> PhoneNumberNormalizer.normalize(raw))
                .isInstanceOf(InvalidPhoneNumberException.class);
    }

}
