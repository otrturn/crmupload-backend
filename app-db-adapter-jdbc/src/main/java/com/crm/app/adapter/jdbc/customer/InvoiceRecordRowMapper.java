package com.crm.app.adapter.jdbc.customer;

import com.crm.app.dto.Customer;
import com.crm.app.dto.InvoiceRecord;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class InvoiceRecordRowMapper implements RowMapper<InvoiceRecord> {

    @Override
    public InvoiceRecord mapRow(ResultSet rs, int rowNum) throws SQLException {

        InvoiceRecord invoiceRecord = new InvoiceRecord();

        invoiceRecord.setInvoiceId(rs.getLong("invoice_id"));
        invoiceRecord.setInvoiceNo(rs.getString("invoice_no"));
        invoiceRecord.setInvoiceDate(rs.getTimestamp("invoice_date"));
        invoiceRecord.setInvoiceDueDate(rs.getTimestamp("invoice_due_date"));
        invoiceRecord.setTaxValue(rs.getBigDecimal("tax_value"));
        invoiceRecord.setTaxAmount(rs.getBigDecimal("tax_amount"));
        invoiceRecord.setNetAmount(rs.getBigDecimal("net_amount"));
        invoiceRecord.setAmount(rs.getBigDecimal("amount"));
        invoiceRecord.setInvoiceImage(rs.getBytes("invoice_image"));

        Customer customer = new Customer(
                rs.getLong("customer_id"),
                null,
                null,
                rs.getString("firstname"),
                rs.getString("lastname"),
                null,
                rs.getString("email_address"),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );


        invoiceRecord.setCustomer(customer);

        return invoiceRecord;
    }
}
