package com.hnh6625.cinema;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Transactional
public class UserTableMigrationTest extends AbstractIntegrationTest {
    @Autowired
    JdbcTemplate jdbc;

    @Test
    void flywayAppliesFirstMigration() {
        Boolean success = jdbc.queryForObject(
                "select success from flyway_schema_history where version = '1' ", Boolean.class
        );
        assertThat(success).isTrue();
    }

    @Test
    void validUserCanBeInserted() {
        insertUser("a@example.com","+84901234567","USER");
        Integer count = jdbc.queryForObject("select count(*) from users", Integer.class);
        assertThat(count).isEqualTo(1);
    }

    @Test
    void emailMustBeUniqueIgnoringCase() {
        insertUser("Anna@Example.com", "+84901234567", "USER");
        assertThatThrownBy(() -> insertUser("anna@example.com", "+84901234568", "USER"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void phoneMustBeUnique() {
        insertUser("a@example.com", "+84901234567", "USER");
        assertThatThrownBy(() -> insertUser("b@example.com", "+84901234567", "USER"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void phoneMustBeInInternationalFormat() {
        assertThatThrownBy(() -> insertUser("a@example.com", "0901234567", "USER"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void roleMustBeKnown() {
        assertThatThrownBy(() -> insertUser("a@example.com", "+84901234567", "SUPERUSER"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private void insertUser(String email, String phone, String role) {
        jdbc.update("""
                insert into users (email, phone, password_hash, role, created_at)
                values (?, ?, 'test-hash', ?, now())
                """, email, phone, role);
    }
}
