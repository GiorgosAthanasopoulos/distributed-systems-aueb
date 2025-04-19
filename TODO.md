# TO-DOs

## WorkerService

- handleFilterStoresRequest, handleShowSalesFoodTypeRequest, handleShowSalesStoreTypeRequest: send request to reducer
  - after reducer and workers connecting, master should send reducer info
    to workers so they can communicate with each other

## Master

- handleStats

## DummyClient

- Filter stores
- Buy products

## Client

- When receiving a response, check the requests you haven't received a response for, and then check if that request is a list/filter type of request. if it is cast the request and get the data from that.

## Worker, Reducer, Master, Client

- Handle responses (maybe re-send if failure? with same id for tracking)

## Reducer

- Implement reducer
