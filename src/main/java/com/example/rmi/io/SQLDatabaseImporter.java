package com.example.rmi.io;

import com.example.rmi.DatabaseManager;
import com.example.rmi.component.Row;
import com.example.rmi.component.column.ColumnType;

import java.sql.*;

public class SQLDatabaseImporter {
    private SQLDatabaseImporter(){}

    public static void importDatabase(String jdbcUrl, String username, String password) throws SQLException {
        try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password)) {
            DatabaseManager.getInstance().createDB("ImportedDB"); // Change as needed

            Statement statement = connection.createStatement();
            // MySQL query for fetching table names
            ResultSet resultSet = statement.executeQuery(
                    "SELECT table_name FROM information_schema.tables WHERE table_schema = DATABASE();");

            int tableIndex = 0;
            while (resultSet.next()) {
                System.out.println(tableIndex);
                String tableName = resultSet.getString("table_name");
                DatabaseManager.getInstance().addTable(tableName);

                importTable(connection, tableName, tableIndex++);
            }
        }
    }

    private static void importTable(Connection connection, String tableName, int tableIndex) throws SQLException {
        Statement statement = connection.createStatement();

        ResultSet columnResultSet = statement.executeQuery("SELECT column_name, data_type FROM information_schema.columns WHERE table_name = '" + tableName + "' AND table_schema = DATABASE();");

        while (columnResultSet.next()) {
            String fullColumnName = columnResultSet.getString("column_name");
            String[] parts = fullColumnName.split("_"); // Splitting by "_"
            String columnName = parts[0];
            String columnType = parts[parts.length-1];
            if (columnType.equals(ColumnType.COLORINVL.name())){
                for (int i = 1; i < parts.length-3; i++) {
                    columnName += "_" + parts[i];
                }
            } else {
                for (int i = 1; i < parts.length-1; i++) {
                    columnName += "_" + parts[i];
                }
            }
            ColumnType columnType1 = ColumnType.valueOf(columnType);
            if (columnType.equals(ColumnType.COLORINVL.name())){
                DatabaseManager.getInstance().addColumn(tableIndex, columnName, columnType1, parts[parts.length-3]  , parts[parts.length-2]);
            } else {
                DatabaseManager.getInstance().addColumn(tableIndex, columnName, columnType1, "", "");
            }

        }

        // Query to select all data from the table
        ResultSet dataResultSet = statement.executeQuery("SELECT * FROM " + tableName + ";");

        while (dataResultSet.next()) {
            Row row = new Row();

            for (int i = 1; i <= dataResultSet.getMetaData().getColumnCount(); i++) {
                // Fetch each column's data as a string
                String data = dataResultSet.getString(i);
                row.values.add(data); // Add the data to the row object
            }

            // Add the constructed row to the table in the local database
            DatabaseManager.getInstance().addRow(tableIndex, row);
        }
    }
}
