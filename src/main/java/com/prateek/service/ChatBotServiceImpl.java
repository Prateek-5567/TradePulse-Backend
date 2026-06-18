package com.prateek.service;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import com.prateek.model.Asset;
import com.prateek.model.CoinDTO;
import com.prateek.model.User;
import com.prateek.model.Wallet;
import com.prateek.response.ApiResponse;
import com.prateek.response.FunctionResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class ChatBotServiceImpl implements ChatBotService {

    @Value("${gemini.api.key}")
    private String API_KEY;

    @Autowired
    private WalletService walletService;

    @Autowired
    private AssetService assetService;

    // ─── Helper: build user context string ───────────────────────────────────

    private String buildUserContext(User user) {
        StringBuilder context = new StringBuilder();

        context.append("USER PROFILE:\n");
        context.append("Name: ").append(user.getFullName()).append("\n");
        context.append("Email: ").append(user.getEmail()).append("\n\n");

        try {
            Wallet wallet = walletService.getUserWallet(user);
            context.append("WALLET BALANCE: $").append(wallet.getBalance()).append("\n\n");
        } catch (Exception e) {
            context.append("WALLET BALANCE: unavailable\n\n");
        }

        try {
            List<Asset> assets = assetService.getUsersAssets(user.getId());
            context.append("CURRENT HOLDINGS:\n");
            if (assets == null || assets.isEmpty()) {
                context.append("No holdings yet.\n");
            } else {
                for (Asset asset : assets) {
                    context.append("- ")
                            .append(asset.getCoin().getName())
                            .append(": ").append(asset.getQuantity()).append(" units")
                            .append(" (avg buy price: $").append(asset.getBuyPrice()).append(")\n");
                }
            }
        } catch (Exception e) {
            context.append("HOLDINGS: unavailable\n");
        }

        return context.toString();
    }

    // ─── Helper: convert number types to double ───────────────────────────────

    private double convertToDouble(Object value) {
        if (value instanceof Integer) return ((Integer) value).doubleValue();
        else if (value instanceof Long) return ((Long) value).doubleValue();
        else if (value instanceof Double) return (Double) value;
        else throw new IllegalArgumentException("Unsupported type: " + value.getClass().getName());
    }

    // ─── Helper: map coin symbols to CoinGecko IDs ───────────────────────────

    private String mapToCoinGeckoId(String coinName) {
        if (coinName == null) return "";
        String normalized = coinName.trim().toLowerCase();
        switch (normalized) {
            case "btc":
            case "bitcoin":
                return "bitcoin";
            case "eth":
            case "ethereum":
                return "ethereum";
            case "sol":
            case "solana":
                return "solana";
            case "ada":
            case "cardano":
                return "cardano";
            case "xrp":
            case "ripple":
                return "ripple";
            case "doge":
            case "dogecoin":
                return "dogecoin";
            case "dot":
            case "polkadot":
                return "polkadot";
            case "matic":
            case "polygon":
                return "polygon";
            case "trx":
            case "tron":
                return "tron";
            case "ltc":
            case "litecoin":
                return "litecoin";
            case "avax":
            case "avalanche":
                return "avalanche-2";
            case "link":
            case "chainlink":
                return "chainlink";
            case "uni":
            case "uniswap":
                return "uniswap";
            case "shib":
            case "shiba-inu":
            case "shiba inu":
                return "shiba-inu";
            default:
                return normalized;
        }
    }

    // ─── Helper: fetch coin data from CoinGecko ───────────────────────────────

    public CoinDTO makeApiRequest(String currencyName) {
        try {
            String mappedId = mapToCoinGeckoId(currencyName);
            System.out.println("coin name " + currencyName + " mapped to " + mappedId);
            String url = "https://api.coingecko.com/api/v3/coins/" + mappedId;

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<Map> responseEntity = restTemplate.getForEntity(url, Map.class);
            Map<String, Object> responseBody = responseEntity.getBody();

            if (responseBody != null) {
                Map<String, Object> image = (Map<String, Object>) responseBody.get("image");
                Map<String, Object> marketData = (Map<String, Object>) responseBody.get("market_data");

                CoinDTO coinInfo = new CoinDTO();
                coinInfo.setId((String) responseBody.get("id"));
                coinInfo.setSymbol((String) responseBody.get("symbol"));
                coinInfo.setName((String) responseBody.get("name"));
                coinInfo.setImage((String) image.get("large"));
                coinInfo.setCurrentPrice(convertToDouble(((Map<String, Object>) marketData.get("current_price")).get("usd")));
                coinInfo.setMarketCap(convertToDouble(((Map<String, Object>) marketData.get("market_cap")).get("usd")));
                coinInfo.setMarketCapRank((int) responseBody.get("market_cap_rank"));
                coinInfo.setTotalVolume(convertToDouble(((Map<String, Object>) marketData.get("total_volume")).get("usd")));
                coinInfo.setHigh24h(convertToDouble(((Map<String, Object>) marketData.get("high_24h")).get("usd")));
                coinInfo.setLow24h(convertToDouble(((Map<String, Object>) marketData.get("low_24h")).get("usd")));
                coinInfo.setPriceChange24h(convertToDouble(marketData.get("price_change_24h")));
                coinInfo.setPriceChangePercentage24h(convertToDouble(marketData.get("price_change_percentage_24h")));
                coinInfo.setMarketCapChange24h(convertToDouble(marketData.get("market_cap_change_24h")));
                coinInfo.setMarketCapChangePercentage24h(convertToDouble(marketData.get("market_cap_change_percentage_24h")));
                coinInfo.setCirculatingSupply(convertToDouble(marketData.get("circulating_supply")));
                coinInfo.setTotalSupply(convertToDouble(marketData.get("total_supply")));
                return coinInfo;
            }
        } catch (Exception e) {
            System.err.println("Error fetching coin details for " + currencyName + ": " + e.getMessage());
        }
        return null;
    }

    // ─── Helper: first Gemini call to extract function args ───────────────────

    public FunctionResponse getFunctionResponse(String prompt) {
        String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + API_KEY;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String requestBody = "{\n" +
                "  \"contents\": [\n" +
                "    {\n" +
                "      \"parts\": [\n" +
                "        {\n" +
                "          \"text\": \"" + prompt + "\"\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ],\n" +
                "  \"tools\": [\n" +
                "    {\n" +
                "      \"functionDeclarations\": [\n" +
                "        {\n" +
                "          \"name\": \"getCoinDetails\",\n" +
                "          \"description\": \"Get the coin details from given currency object\",\n" +
                "          \"parameters\": {\n" +
                "            \"type\": \"OBJECT\",\n" +
                "            \"properties\": {\n" +
                "              \"currencyName\": {\n" +
                "                \"type\": \"STRING\",\n" +
                "                \"description\": \"The currency name, id, symbol.\"\n" +
                "              },\n" +
                "              \"currencyData\": {\n" +
                "                \"type\": \"STRING\",\n" +
                "                \"description\": \"Currency Data id, symbol, name, image, current_price, market_cap, market_cap_rank, fully_diluted_valuation, total_volume, high_24h, low_24h, price_change_24h, price_change_percentage_24h, market_cap_change_24h, market_cap_change_percentage_24h, circulating_supply, total_supply, max_supply, ath, ath_change_percentage, ath_date, atl, atl_change_percentage, atl_date, last_updated.\"\n" +
                "              }\n" +
                "            },\n" +
                "            \"required\": [\"currencyName\", \"currencyData\"]\n" +
                "          }\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.postForEntity(GEMINI_API_URL, requestEntity, String.class);

        String responseBody = response.getBody();
        ReadContext ctx = JsonPath.parse(responseBody);

        String currencyName = ctx.read("$.candidates[0].content.parts[0].functionCall.args.currencyName");
        String currencyData = ctx.read("$.candidates[0].content.parts[0].functionCall.args.currencyData");
        String name = ctx.read("$.candidates[0].content.parts[0].functionCall.name");

        FunctionResponse res = new FunctionResponse();
        res.setCurrencyName(currencyName);
        res.setCurrencyData(currencyData);
        res.setFunctionName(name);

        System.out.println(name + " ------- " + currencyName + "-----" + currencyData);
        return res;
    }

    // ─── Interface methods ────────────────────────────────────────────────────

    @Override
    public String simpleChat(String prompt, User user) {
        String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + API_KEY;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String userContext = buildUserContext(user);

        // Define system instruction
        JSONObject systemInstruction = new JSONObject();
        JSONObject systemPart = new JSONObject();
        systemPart.put("text",
                "You are an elite AI Trading Assistant for the TradePulse platform. " +
                        "You are fully trained to answer any query on cryptocurrency trading, technical indicators, blockchain, risk management, and the crypto markets. " +
                        "When the user asks for live prices, market cap, 24h highs/lows, or real-time rates of a specific coin (like Bitcoin, Ethereum, SOL, etc.), you MUST use the `getCoinDetails` tool to fetch real-time market data first, and then integrate those live rates naturally in your response. " +
                        "Only answer questions related to cryptocurrency, trading, portfolio, market analysis, coins, wallets, and transactions. " +
                        "Politely decline anything unrelated to crypto or this app. " +
                        "Use the following live user data to give personalized advice:\n\n" +
                        userContext
        );
        systemInstruction.put("parts", new JSONArray().put(systemPart));

        // Create the primary request body
        JSONObject requestBody = new JSONObject();
        requestBody.put("system_instruction", systemInstruction);

        JSONArray contentsArray = new JSONArray();
        JSONObject contentsObject = new JSONObject();
        JSONArray partsArray = new JSONArray();
        JSONObject textObject = new JSONObject();
        textObject.put("text", prompt);
        partsArray.put(textObject);
        contentsObject.put("role", "user");
        contentsObject.put("parts", partsArray);
        contentsArray.put(contentsObject);
        requestBody.put("contents", contentsArray);

        // Add the getCoinDetails function tool declaration
        JSONArray toolsArray = new JSONArray();
        JSONObject toolObject = new JSONObject();
        JSONArray funcDecls = new JSONArray();
        
        JSONObject funcObject = new JSONObject();
        funcObject.put("name", "getCoinDetails");
        funcObject.put("description", "Get crypto currency data (current price, market cap, rank, high 24h, low 24h, total volume, supply, etc.) for a given coin by name, ID, or symbol.");
        
        JSONObject parameters = new JSONObject();
        parameters.put("type", "OBJECT");
        
        JSONObject properties = new JSONObject();
        JSONObject currencyNameProp = new JSONObject();
        currencyNameProp.put("type", "STRING");
        currencyNameProp.put("description", "The currency name, ID, or symbol (e.g. bitcoin, BTC, ethereum, ETH, solana, SOL).");
        properties.put("currencyName", currencyNameProp);
        
        JSONObject currencyDataProp = new JSONObject();
        currencyDataProp.put("type", "STRING");
        currencyDataProp.put("description", "A brief description of what data to fetch.");
        properties.put("currencyData", currencyDataProp);
        
        parameters.put("properties", properties);
        parameters.put("required", new JSONArray().put("currencyName").put("currencyData"));
        
        funcObject.put("parameters", parameters);
        funcDecls.put(funcObject);
        toolObject.put("functionDeclarations", funcDecls);
        toolsArray.put(toolObject);
        requestBody.put("tools", toolsArray);

        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody.toString(), headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.postForEntity(GEMINI_API_URL, requestEntity, String.class);
        String responseBody = response.getBody();
        System.out.println("First Gemini Response: " + responseBody);

        // Check if the response contains a functionCall
        boolean hasFunctionCall = false;
        String currencyName = null;
        String currencyData = null;
        String functionName = null;

        try {
            JSONObject jsonResponse = new JSONObject(responseBody);
            JSONArray candidates = jsonResponse.optJSONArray("candidates");
            if (candidates != null && candidates.length() > 0) {
                JSONObject candidate = candidates.getJSONObject(0);
                JSONObject content = candidate.optJSONObject("content");
                if (content != null) {
                    JSONArray parts = content.optJSONArray("parts");
                    if (parts != null && parts.length() > 0) {
                        JSONObject part = parts.getJSONObject(0);
                        if (part.has("functionCall")) {
                            JSONObject functionCall = part.getJSONObject("functionCall");
                            functionName = functionCall.optString("name");
                            JSONObject args = functionCall.optJSONObject("args");
                            if (args != null) {
                                currencyName = args.optString("currencyName");
                                currencyData = args.optString("currencyData");
                                hasFunctionCall = true;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error parsing Gemini response for function call: " + e.getMessage());
        }

        // If a function call is requested, execute the tool and make a second call to Gemini
        if (hasFunctionCall && "getCoinDetails".equals(functionName) && currencyName != null) {
            CoinDTO coin = makeApiRequest(currencyName);
            String apiResponse = (coin != null) ? coin.toString() : "{\"error\": \"Could not retrieve live price data for " + currencyName + ".\"}";

            JSONObject secondRequestBody = new JSONObject();
            secondRequestBody.put("system_instruction", systemInstruction);
            
            JSONArray secondContentsArray = new JSONArray();
            
            // 1. User original prompt
            JSONObject userQueryContent = new JSONObject();
            userQueryContent.put("role", "user");
            userQueryContent.put("parts", new JSONArray().put(new JSONObject().put("text", prompt)));
            secondContentsArray.put(userQueryContent);
            
            // 2. Model's function call response
            JSONObject modelResponseContent = new JSONObject();
            modelResponseContent.put("role", "model");
            
            JSONObject funcCallPart = new JSONObject();
            JSONObject funcCallDetail = new JSONObject();
            funcCallDetail.put("name", "getCoinDetails");
            
            JSONObject argsObject = new JSONObject();
            argsObject.put("currencyName", currencyName);
            argsObject.put("currencyData", currencyData != null ? currencyData : "all");
            
            funcCallDetail.put("args", argsObject);
            funcCallPart.put("functionCall", funcCallDetail);
            
            modelResponseContent.put("parts", new JSONArray().put(funcCallPart));
            secondContentsArray.put(modelResponseContent);
            
            // 3. Function result
            JSONObject functionResultContent = new JSONObject();
            functionResultContent.put("role", "function");
            
            JSONObject funcResponsePart = new JSONObject();
            JSONObject funcResponseDetail = new JSONObject();
            funcResponseDetail.put("name", "getCoinDetails");
            
            JSONObject responseContentObject = new JSONObject();
            responseContentObject.put("name", "getCoinDetails");
            
            JSONObject parsedApiResponse;
            try {
                parsedApiResponse = new JSONObject(apiResponse);
            } catch (Exception e) {
                parsedApiResponse = new JSONObject().put("error", apiResponse);
            }
            responseContentObject.put("content", parsedApiResponse);
            
            funcResponseDetail.put("response", responseContentObject);
            funcResponsePart.put("functionResponse", funcResponseDetail);
            
            functionResultContent.put("parts", new JSONArray().put(funcResponsePart));
            secondContentsArray.put(functionResultContent);
            
            secondRequestBody.put("contents", secondContentsArray);
            secondRequestBody.put("tools", toolsArray);

            HttpEntity<String> secondRequestEntity = new HttpEntity<>(secondRequestBody.toString(), headers);
            ResponseEntity<String> secondResponse = restTemplate.postForEntity(GEMINI_API_URL, secondRequestEntity, String.class);
            
            System.out.println("Second Gemini Response: " + secondResponse.getBody());
            return secondResponse.getBody();
        }

        return responseBody;
    }

    @Override
    public ApiResponse getCoinDetails(String prompt, User user) {
        String userContext = buildUserContext(user);

        FunctionResponse res = getFunctionResponse(prompt);
        String apiResponse = makeApiRequest(res.getCurrencyName()).toString();

        String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + API_KEY;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String body = "{\n" +
                "  \"system_instruction\": {\n" +
                "    \"parts\": [{\"text\": \"You are a crypto trading assistant. " +
                "Use this user data for personalized advice:\\n\\n" + userContext + "\"}]\n" +
                "  },\n" +
                "  \"contents\": [\n" +
                "    {\n" +
                "      \"role\": \"user\",\n" +
                "      \"parts\": [{\"text\": \"" + prompt + "\"}]\n" +
                "    },\n" +
                "    {\n" +
                "      \"role\": \"model\",\n" +
                "      \"parts\": [{\n" +
                "        \"functionCall\": {\n" +
                "          \"name\": \"getCoinDetails\",\n" +
                "          \"args\": {\n" +
                "            \"currencyName\": \"" + res.getCurrencyName() + "\",\n" +
                "            \"currencyData\": \"" + res.getCurrencyData() + "\"\n" +
                "          }\n" +
                "        }\n" +
                "      }]\n" +
                "    },\n" +
                "    {\n" +
                "      \"role\": \"function\",\n" +
                "      \"parts\": [{\n" +
                "        \"functionResponse\": {\n" +
                "          \"name\": \"getCoinDetails\",\n" +
                "          \"response\": {\n" +
                "            \"name\": \"getCoinDetails\",\n" +
                "            \"content\": " + apiResponse + "\n" +
                "          }\n" +
                "        }\n" +
                "      }]\n" +
                "    }\n" +
                "  ],\n" +
                "  \"tools\": [\n" +
                "    {\n" +
                "      \"functionDeclarations\": [\n" +
                "        {\n" +
                "          \"name\": \"getCoinDetails\",\n" +
                "          \"description\": \"Get crypto currency data from given currency object.\",\n" +
                "          \"parameters\": {\n" +
                "            \"type\": \"OBJECT\",\n" +
                "            \"properties\": {\n" +
                "              \"currencyName\": {\"type\": \"STRING\", \"description\": \"The currency Name, id, symbol.\"},\n" +
                "              \"currencyData\": {\"type\": \"STRING\", \"description\": \"The currency data id, symbol, current price, image, market cap etc.\"}\n" +
                "            },\n" +
                "            \"required\": [\"currencyName\", \"currencyData\"]\n" +
                "          }\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        HttpEntity<String> request = new HttpEntity<>(body, headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.postForEntity(GEMINI_API_URL, request, String.class);

        System.out.println("Response: " + response.getBody());
        ReadContext ctx = JsonPath.parse(response.getBody());

        String text = ctx.read("$.candidates[0].content.parts[0].text");
        ApiResponse ans = new ApiResponse();
        ans.setMessage(text);
        return ans;
    }

    @Override
    public CoinDTO getCoinByName(String coinName) {
        return this.makeApiRequest(coinName);
    }
}