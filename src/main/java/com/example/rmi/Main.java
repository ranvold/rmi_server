package com.example.rmi;

import com.example.rmi.remote.RemoteDBImpl;

import java.rmi.AlreadyBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class Main {

  public static final String UNIQUE_BINDING_NAME = "server.db";

  public static void main(String[] args)
      throws RemoteException, AlreadyBoundException, InterruptedException {

    DatabaseManager dbManager = DatabaseManager.getInstance();
    dbManager.createDB("DB");
    dbManager.populateTable();
    dbManager.populateTable();

    final RemoteDBImpl server = new RemoteDBImpl();
    final Registry registry = LocateRegistry.createRegistry(8081);
    Remote stub = UnicastRemoteObject.exportObject(server,0);
    registry.bind(UNIQUE_BINDING_NAME,stub);

    Thread.sleep(Integer.MAX_VALUE);
  }
}