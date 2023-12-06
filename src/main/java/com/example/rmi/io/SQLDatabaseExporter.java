package com.example.rmi.io;


import com.example.rmi.component.Column;
import com.example.rmi.component.Database;
import com.example.rmi.component.Row;
import com.example.rmi.component.Table;
import com.example.rmi.component.column.ColorInvlColumn;
import com.example.rmi.component.column.ColumnType;

import java.math.BigDecimal;
import java.sql.*;

public class SQLDatabaseExporter {

    public static void exportDatabase(Database localDatabase, String jdbcUrl, String username, String password) {
        try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password)) {
            try {
                connection.setAutoCommit(false);
                dropAllTables(connection);
                for (Table table : localDatabase.tables) {
                    exportTable(connection, table);
                }
                connection.commit();
            } catch (SQLException ex) {
                connection.rollback();
                ex.printStackTrace();
            } finally {
                connection.setAutoCommit(false);
            }
        }catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void exportTable(Connection connection, Table table) throws SQLException {
        // Split DROP TABLE and CREATE TABLE into separate statements
        String createTableSQL = generateCreateTableSQL(table);

        try (PreparedStatement createTableStatement = connection.prepareStatement(createTableSQL)) {
            createTableStatement.execute(); // Execute CREATE TABLE statement
        }

        // Insert data
        String insertDataSQL = generateInsertDataSQL(table);
        try (PreparedStatement insertDataStatement = connection.prepareStatement(insertDataSQL)) {
            for (Row row : table.rows) {
                setPreparedStatementParameters(insertDataStatement, table, row);
                insertDataStatement.addBatch();
            }
            insertDataStatement.executeBatch();
        }
    }


    private static String generateCreateTableSQL(Table table) {
        StringBuilder sqlBuilder = new StringBuilder("CREATE TABLE ")
                .append(table.name)
                .append(" (");

        for (Column column : table.columns) {
            String columnNameWithType = column.name;
            if (column.type.equals(ColumnType.COLORINVL.name())) {
                ColorInvlColumn colorInvlColumn = (ColorInvlColumn) column;
                String min = colorInvlColumn.getMin();
                String max = colorInvlColumn.getMax();
                columnNameWithType += "_" + min  + "_" + max;
            }
            columnNameWithType += "_" + column.type;
            sqlBuilder.append(columnNameWithType)
                    .append(" ")
                    .append("VARCHAR(256)")
                    .append(", ");
        }

        sqlBuilder.delete(sqlBuilder.length() - 2, sqlBuilder.length());
        sqlBuilder.append(");");

        return sqlBuilder.toString();
    }


    private static String generateInsertDataSQL(Table table) {
        StringBuilder sqlBuilder = new StringBuilder("INSERT INTO ")
                .append(table.name)
                .append(" (");

        for (Column column : table.columns) {
            String columnName = column.name;
            if (column.type.equals(ColumnType.COLORINVL.name())) {
                ColorInvlColumn colorInvlColumn = (ColorInvlColumn) column;
                String min = colorInvlColumn.getMin();
                String max = colorInvlColumn.getMax();
                columnName += "_" + min  + "_" + max;
            }
            columnName += "_" + column.type;
            sqlBuilder.append(columnName).append(", ");
        }

        sqlBuilder.delete(sqlBuilder.length() - 2, sqlBuilder.length());
        sqlBuilder.append(") VALUES (");

        for (int i = 0; i < table.columns.size(); i++) {
            sqlBuilder.append("?, ");
        }

        sqlBuilder.delete(sqlBuilder.length() - 2, sqlBuilder.length());
        sqlBuilder.append(");");

        return sqlBuilder.toString();
    }



    private static void setPreparedStatementParameters(PreparedStatement statement, Table table, Row row) throws SQLException {
        for (int i = 0; i < table.columns.size(); i++) {
            String data = row.getAt(i);
            if (data == null || data.isEmpty()) {
                // If the data is empty, set it as null or keep it as an empty string based on the column type
                if (table.columns.get(i).type.equals("INT") || table.columns.get(i).type.equals("REAL")) {
                    statement.setNull(i + 1, Types.NULL); // Set null for INT and REAL types
                } else {
                    statement.setString(i + 1, data); // Set empty string for other types
                }
            } else {
                // Parse and set data based on column type
                if (table.columns.get(i).type.equals("INT")) {
                    statement.setInt(i + 1, Integer.parseInt(data));
                } else if (table.columns.get(i).type.equals("REAL")) {
                    statement.setBigDecimal(i + 1, new BigDecimal(data));
                } else {
                    statement.setString(i + 1, data);
                }
            }
        }
    }


    private static void dropAllTables(Connection connection) throws SQLException {
        PreparedStatement ps = connection.prepareStatement("SHOW TABLES");
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            String tableName = rs.getString(1);
            PreparedStatement dropStatement = connection.prepareStatement("DROP TABLE IF EXISTS " + tableName);
            dropStatement.execute();
        }
        rs.close();
        ps.close();
    }
}