# bank-account-kata

## Requirements

1. Deposit and Withdrawal
2. Account statement (date, amount, balance)
3. Statement printing  

The expected result is a service API, and its underlying implementation, that meets the expressed needs.
Nothing more, especially no UI, no persistence.

## User Stories

### US 1:

In order to save money.  
As a bank client.  
I want to make a deposit in my account.  

### US 2:

In order to retrieve some or all of my savings.  
As a bank client.  
I want to make a withdrawal from my account.  

### US 3:

In order to check my operations.  
As a bank client.  
I want to see the history (operation, date, amount, balance) of my operations. 

## Demos

The OpenAPI specification of the service can be found at http://localhost:8080/swagger-ui/index.html

A sample account with id **3fa85f64-5717-4562-b3fc-2c963f66afa6** is already hardcoded at startup for demos purpose (which is also the value by default in Swagger UI examples when you click on **Try It Out** button), but you can certainly create a new one as wish. This sample account contains already 8 statements (withdrawing + disposit) so that you can play directly with the pagination without recreating the statements.


Here are some brief example for the endpoints:
### Create an account:
POST /account
```
{
  "type": "CHECKING",
  "balance": 0,
  "statements": []
}
```
![image](https://user-images.githubusercontent.com/10930605/151705714-b6596852-0f69-4f9f-b0de-2cc2fcc57915.png)

### Get an account:
GET /account/_**{account_id}**_
![image](https://user-images.githubusercontent.com/10930605/151705737-54bb7001-2ded-42c7-9fed-e09cfaea492c.png)


### Add an operation to the account:
POST /account/_**{account_id}**_/statements
```
{
  "type": "DEPOSIT",
  "amount": 15
}
```
![image](https://user-images.githubusercontent.com/10930605/151705766-955c206d-2426-4cd5-bac6-f2ee807258d3.png)

### Print the statements of an account:
GET /account/_**{account_id}**_/statements?page=0&size=10&sort=date,asc

***Note: I only implemented the sorting by date (which should be done automatically if we have the repository layer, but for the demo we don't)***
![image](https://user-images.githubusercontent.com/10930605/151705787-9cb0d0a9-b7ca-478c-a495-1cf1e9432264.png)

Here's the result of printing the sample account with this `GET /account/3fa85f64-5717-4562-b3fc-2c963f66afa6/statements?page=0&size=4&sort=date,desc`
```
{
  "statements": [
    {
      "date": "30/01/2022 15:49:21",
      "type": "DEPOSIT",
      "amount": 5000
    },
    {
      "date": "11/01/2022 22:38:48",
      "type": "DEPOSIT",
      "amount": 100
    },
    {
      "date": "10/01/2022 21:38:48",
      "type": "WITHDRAWAL",
      "amount": 7
    },
    {
      "date": "09/01/2022 20:38:48",
      "type": "WITHDRAWAL",
      "amount": 6
    }
  ],
  "currentPage": 0,
  "totalPages": 3,
  "totalStatements": 9,
  "accountBalance": 5102
}
```
### Exception handlers
There are two exceptions implemented with user-friendly error message when:
1. Account not found
2. Withdrawal of an amount which makes the account balance negative
