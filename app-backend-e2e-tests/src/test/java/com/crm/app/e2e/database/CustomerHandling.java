package com.crm.app.e2e.database;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class CustomerHandling {
    public static String getActivationToken(DataSource dataSource, String emailAddress) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement psUpdate = connection.prepareStatement("SELECT token from app.customer_activation WHERE customer_id = (SELECT customer_id from app.customer WHERE email_address=?)")) {
            psUpdate.setString(1, emailAddress);
            ResultSet rs = psUpdate.executeQuery();
            if (rs.next()) {
                return rs.getString(1);
            }
        } catch (Exception e) {
            throw new IllegalStateException("getActivationToken failed", e);
        }
        return "";
    }

    public static String getPageVisits(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement psUpdate = connection.prepareStatement("SELECT page_Id from app.page_visits")) {
            ResultSet rs = psUpdate.executeQuery();
            if (rs.next()) {
                return rs.getString(1);
            }
        } catch (Exception e) {
            throw new IllegalStateException("getPageVisits failed", e);
        }
        return "";
    }

    public static Long getVerificationTaskId(DataSource dataSource, String emailAddress, String taxId) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement psUpdate = connection.prepareStatement("""
                     SELECT verification_task_id from app.customer_verification_task
                         WHERE customer_id = (SELECT customer_id from app.customer WHERE email_address=?)
                         AND task_description=?
                     """)) {
            psUpdate.setString(1, emailAddress);
            psUpdate.setString(2, "taxId:" + taxId);
            ResultSet rs = psUpdate.executeQuery();
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (Exception e) {
            throw new IllegalStateException("getVerificationTaskId failed", e);
        }
        return 0L;
    }
}
