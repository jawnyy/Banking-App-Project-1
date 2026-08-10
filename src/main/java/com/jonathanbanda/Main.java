package com.jonathanbanda;

import com.jonathanbanda.config.AppConfig;
import com.jonathanbanda.config.DAOFactory;
import com.jonathanbanda.dao.AccountDAO;
import com.jonathanbanda.dao.CustomerDAO;
import com.jonathanbanda.dao.TransactionDAO;
import com.jonathanbanda.presentation.ConsoleApp;
import com.jonathanbanda.service.AccountService;
import com.jonathanbanda.service.CustomerService;
import com.jonathanbanda.service.TransactionService;

public class Main {
    public static void main(String[] args) {
        AppConfig config = new AppConfig();
        DAOFactory daoFactory = new DAOFactory(config);

        CustomerDAO customerDAO = daoFactory.getCustomerDAO();
        AccountDAO accountDAO = daoFactory.getAccountDAO();
        TransactionDAO transactionDAO = daoFactory.getTransactionDAO();

        CustomerService customerService = new CustomerService(customerDAO);
        AccountService accountService = new AccountService(accountDAO);
        TransactionService transactionService = new TransactionService(transactionDAO, accountDAO);

        ConsoleApp app = new ConsoleApp(customerService, accountService, transactionService);
        app.run();
    }
}