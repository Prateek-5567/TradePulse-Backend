package com.prateek.service;



import com.prateek.model.Asset;
import com.prateek.model.Coin;
import com.prateek.model.User;

import java.util.List;

public interface AssetService {
    Asset createAsset(User user, Coin coin, double quantity);

    Asset getAssetById(Long assetId);

    Asset getAssetByUserAndId(Long userId,Long assetId);

    List<Asset> getUsersAssets(Long userId);

    Asset updateAsset(Long assetId,double quantity) throws Exception;

    Asset findAssetByUserIdAndCoinId(Long userId,String coinId) throws Exception;

    void deleteAsset(Long assetId);
}
/*
AssetService is a service-layer interface that defines operations for managing user assets (cryptocurrency holdings),
while AssetServiceImplementation provides the concrete business logic and database interaction for those operations.

This service represents the portfolio management layer of your trading platform.
    Every user owns assets (coins + quantity + buy price)
    This service handles:
        Creating assets when buying
        Updating assets when buying/selling more
        Fetching assets for display
        Deleting assets when holdings become negligible
    It acts as a bridge between business logic (orders) and database (AssetsRepository).
 */

/*    User  Asset  =  stock holdings related operations.
AssetService Interface:
| Method                       | Purpose                          |
| ---------------------------- | -------------------------------- |
| createAsset()                | Create new asset entry(new stock)|
| getAssetById()               | Fetch asset by ID                |
| getAssetByUserAndId()        | Fetch asset with user validation |
| getUsersAssets()             | Get all assets of a user         |
| updateAsset()                | Modify quantity                  |
| findAssetByUserIdAndCoinId() | Locate asset for a specific coin |
| deleteAsset()                | Remove asset                     |

 */
