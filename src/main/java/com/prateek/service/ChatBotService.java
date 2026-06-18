package com.prateek.service;

import com.prateek.model.CoinDTO;
import com.prateek.model.User;
import com.prateek.response.ApiResponse;

public interface ChatBotService {
    ApiResponse getCoinDetails(String prompt, User user);
    CoinDTO getCoinByName(String coinName);
    String simpleChat(String prompt, User user);
}


//package com.prateek.service;
//
//import com.prateek.model.CoinDTO;
//import com.prateek.response.ApiResponse;
//
//public interface ChatBotService {
//    ApiResponse getCoinDetails(String coinName);
//
//    CoinDTO getCoinByName(String coinName);
//
//    String simpleChat(String prompt);
//}
