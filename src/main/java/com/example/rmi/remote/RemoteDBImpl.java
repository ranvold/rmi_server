package com.example.rmi.remote;

import com.example.rmi.DatabaseManager;
import com.example.rmi.component.*;
import com.example.rmi.component.column.ColumnType;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public class RemoteDBImpl implements RemoteDB{

  @Override
  public List<Row> getRows(int tableIndex) throws RemoteException {
    return DatabaseManager.database.tables.get(tableIndex).rows;
  }

  @Override
  public List<Column> getColumns(int tableIndex) throws RemoteException {
    return DatabaseManager.database.tables.get(tableIndex).columns;
  }

  @Override
  public List<TableData> getTablesData() throws RemoteException {
    List<Table> tables = DatabaseManager.database.tables;
    List<TableData> names = new ArrayList<>();
    for (int i = 0; i < tables.size(); i++) {
      names.add(new TableData(tables.get(i).name,i));
    }
    return names;
  }

  @Override
  public Boolean createTable(String name) throws RemoteException {
    return DatabaseManager.getInstance().addTable(name);
  }

  @Override
  public Boolean addRow(int tableIndex) throws RemoteException {
    return DatabaseManager.getInstance().addRow(tableIndex,new Row());
  }

  @Override
  public Boolean addColumn(int tableIndex, String name, ColumnType columnType, String min, String max) throws RemoteException {
    if(columnType == ColumnType.COLORINVL) {
      return DatabaseManager.getInstance().addColumn(tableIndex,name,columnType, min, max);
    }

    return DatabaseManager.getInstance().addColumn(tableIndex,name,columnType, "", "");
  }
  @Override
  public Boolean deleteTable(int tableIndex) throws RemoteException {
    return DatabaseManager.getInstance().deleteTable(tableIndex);
  }

  @Override
  public Boolean deleteColumn(int tableIndex, int columnIndex) throws RemoteException {
    return DatabaseManager.getInstance().deleteColumn(tableIndex,columnIndex);
  }

  @Override
  public Boolean deleteRow(int tableIndex, int rowIndex) throws RemoteException {
    return DatabaseManager.getInstance().deleteRow(tableIndex,rowIndex);
  }

  @Override
  public Boolean editCell(int tableIndex, int rowIndex, int columnIndex, String value) throws RemoteException {
    return DatabaseManager.getInstance().updateCellValue(value, tableIndex, columnIndex, rowIndex);
  }

  @Override
  public void createTestTable() throws RemoteException {
    try {
      DatabaseManager dbManager = DatabaseManager.getInstance();
      dbManager.populateTable();
    }
    catch (Exception e){
      System.out.println(e);
    }
    System.out.println("Table created");
  }

  @Override
  public Boolean tablesMultiply(int tableIndex1, int tableIndex2) throws RemoteException {
    return DatabaseManager.getInstance().tablesIntersection(tableIndex1, tableIndex2);
  }
}
