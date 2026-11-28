package com.hnh6625.cinema.cinema.dto;

import com.hnh6625.cinema.cinema.entity.SeatType;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

public class RoomRequestValidationTest {
    private static final Validator validator =
            Validation.buildDefaultValidatorFactory().getValidator();

    private Set<String> violatedProperties(RoomRequest request) {
        return validator.validate(request).stream()
                .map(violation -> violation.getPropertyPath().toString())
                .collect(Collectors.toSet());
    }

    private SeatRowRequest row(String label, Integer seatCount, SeatType type) {
        return new SeatRowRequest(label, seatCount, type);
    }

    private List<SeatRowRequest> validRows() {
        return List.of(row("A", 10, SeatType.STANDARD), row("B", 8, SeatType.VIP));
    }

    private List<SeatRowRequest> rows(int count) {
        return IntStream.rangeClosed(1, count)
                .mapToObj(i -> row("R" + i, 1, SeatType.STANDARD))
                .toList();
    }

    @Test
    void validRequest_hasNoViolations() {
        assertThat(violatedProperties(new RoomRequest("Room 1", validRows()))).isEmpty();
    }

    @Test
    void blankName_isRejected() {
        assertThat(violatedProperties(new RoomRequest("   ", validRows())))
                .containsExactly("name");
    }

    @Test
    void nameOfMaxLength_isAccepted_butOneMoreIsRejected() {
        assertThat(violatedProperties(new RoomRequest("a".repeat(100), validRows()))).isEmpty();
        assertThat(violatedProperties(new RoomRequest("a".repeat(101), validRows())))
                .containsExactly("name");
    }

    @Test
    void emptyOrMissingRows_areRejected() {
        assertThat(violatedProperties(new RoomRequest("Room 1", List.of())))
                .containsExactly("rows");
        assertThat(violatedProperties(new RoomRequest("Room 1", null)))
                .containsExactly("rows");
    }

    @Test
    void rowsOfMaxCount_areAccepted_butOneMoreIsRejected() {
        assertThat(violatedProperties(new RoomRequest("Room 1", rows(26)))).isEmpty();
        assertThat(violatedProperties(new RoomRequest("Room 1", rows(27))))
                .containsExactly("rows");
    }

    @Test
    void seatCountOutOfRange_isRejectedWithRowPath() {
        assertThat(violatedProperties(new RoomRequest("Room 1",
                List.of(row("A", 0, SeatType.STANDARD)))))
                .containsExactly("rows[0].seatCount");
        assertThat(violatedProperties(new RoomRequest("Room 1",
                List.of(row("A", 51, SeatType.STANDARD)))))
                .containsExactly("rows[0].seatCount");
        assertThat(violatedProperties(new RoomRequest("Room 1",
                List.of(row("A", null, SeatType.STANDARD)))))
                .containsExactly("rows[0].seatCount");
    }

    @Test
    void seatCountBoundaries_areAccepted() {
        assertThat(violatedProperties(new RoomRequest("Room 1",
                List.of(row("A", 1, SeatType.STANDARD), row("B", 50, SeatType.VIP)))))
                .isEmpty();
    }

    @Test
    void invalidLabel_isRejectedWithRowPath() {
        assertThat(violatedProperties(new RoomRequest("Room 1",
                List.of(row("   ", 10, SeatType.STANDARD)))))
                .containsExactly("rows[0].label");
        assertThat(violatedProperties(new RoomRequest("Room 1",
                List.of(row("ABCD", 10, SeatType.STANDARD)))))
                .containsExactly("rows[0].label");
        assertThat(violatedProperties(new RoomRequest("Room 1",
                List.of(row("ABC", 10, SeatType.STANDARD)))))
                .isEmpty();
    }

    @Test
    void missingType_isRejectedWithRowPath() {
        assertThat(violatedProperties(new RoomRequest("Room 1",
                List.of(row("A", 10, null)))))
                .containsExactly("rows[0].type");
    }

    @Test
    void invalidRowAfterValidOne_reportsItsOwnIndex() {
        assertThat(violatedProperties(new RoomRequest("Room 1",
                List.of(row("A", 10, SeatType.STANDARD), row("B", 0, SeatType.STANDARD)))))
                .containsExactly("rows[1].seatCount");
    }
}