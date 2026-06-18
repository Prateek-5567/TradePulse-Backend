package com.prateek.service;


import com.prateek.model.Asset;
import com.prateek.model.Coin;
import com.prateek.model.User;
import com.prateek.repository.AssetsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AssetServiceImplementation implements  AssetService {
    private final AssetsRepository assetRepository; // there is a asset table in database And we are using Spring Data JPA methods for operations on data.

    @Autowired                  // constructor injection.
    public AssetServiceImplementation(AssetsRepository assetRepository) {
        this.assetRepository = assetRepository;
    }

    @Override
    public Asset createAsset(User user, Coin coin, double quantity) {
        Asset asset = new Asset();

        asset.setQuantity(quantity);
        asset.setBuyPrice(coin.getCurrentPrice());
        asset.setCoin(coin);
        asset.setUser(user);

        return assetRepository.save(asset);
    }


//    public Asset buyAsset(User user, Coin coin, Long quantity) { instead we have a create asset
//        return createAsset(user,coin,quantity);
//    }

    public Asset getAssetById(Long assetId) {
        return assetRepository.findById(assetId)
                .orElseThrow(() -> new IllegalArgumentException("Asset not found"));
    }

    @Override
    public Asset getAssetByUserAndId(Long userId, Long assetId) {
        return assetRepository.findByIdAndUserId(assetId,userId);
    }

    @Override
    public List<Asset> getUsersAssets(Long userId) {
        return assetRepository.findByUserId(userId);
    }



    @Override
    public Asset updateAsset(Long assetId, double quantity) throws Exception {

        Asset oldAsset=getAssetById(assetId);
        if(oldAsset==null){
            throw new Exception("Asset not found...");
        }
        oldAsset.setQuantity(quantity+ oldAsset.getQuantity());

        return assetRepository.save(oldAsset);
    }

    @Override
    public Asset findAssetByUserIdAndCoinId(Long userId, String coinId) throws Exception {
        return assetRepository.findByUserIdAndCoinId(userId,coinId);
    }


    public void deleteAsset(Long assetId) {
        assetRepository.deleteById(assetId);
    }

}

/*
| Feature             | Description                                  |
| ------------------- | -------------------------------------------- |
| Portfolio Tracking  | Stores user’s crypto holdings                |
| Buy Price Storage   | Captures price at purchase time              |
| Incremental Updates | Adds/subtracts quantity instead of replacing |
| User Isolation      | Assets linked per user                       |
| Exception Handling  | Throws error when asset not found            |

 */