package com.prateek.domain;

public enum OrderStatus {
    PENDING, FILLED, CANCELLED, PARTIALLY_FILLED, ERROR, SUCCESS
}
// Order Table in our app is used to store order history of user.
// it stores past buys / sells of user.