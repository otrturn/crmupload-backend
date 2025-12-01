package com.crm.app.adapter.jdbc.consumer;

import com.crm.app.port.consumer.Consumer;
import com.crm.app.port.consumer.ConsumerRepositoryPort;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcConsumerRepository implements ConsumerRepositoryPort {

    private final NamedParameterJdbcTemplate jdbc;

    public JdbcConsumerRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public boolean emailExists(String emailAddress) {
        String sql = """
            SELECT COUNT(*)
            FROM app.consumer
            WHERE email_address = :email
            """;

        Long count = jdbc.queryForObject(sql,
                new MapSqlParameterSource("email", emailAddress),
                Long.class);
        return count != null && count > 0;
    }

    @Override
    public long nextConsumerId() {
        String sql = "SELECT nextval('app.sequence_consumer')";
        Long next = jdbc.getJdbcOperations().queryForObject(sql, Long.class);
        if (next == null) {
            throw new IllegalStateException("Could not obtain next consumer_id");
        }
        return next;
    }

    @Override
    public void insertConsumer(Consumer consumer) {
        String sql = """
            INSERT INTO app.consumer (
                consumer_id, user_id, firstname, lastname,
                email_address, phone_number,
                adrline1, adrline2, postalcode, city, country
            )
            VALUES (
                :consumerId, :userId, :firstname, :lastname,
                :email, :phone,
                :adr1, :adr2, :postal, :city, :country
            )
            """;

        var params = new MapSqlParameterSource()
                .addValue("consumerId", consumer.consumerId())
                .addValue("userId", consumer.userId())
                .addValue("firstname", consumer.firstname())
                .addValue("lastname", consumer.lastname())
                .addValue("email", consumer.emailAddress())
                .addValue("phone", consumer.phoneNumber())
                .addValue("adr1", consumer.adrline1())
                .addValue("adr2", consumer.adrline2())
                .addValue("postal", consumer.postalcode())
                .addValue("city", consumer.city())
                .addValue("country", consumer.country());

        jdbc.update(sql, params);
    }
}