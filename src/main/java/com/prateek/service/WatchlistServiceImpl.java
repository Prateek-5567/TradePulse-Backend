package com.prateek.service;

import com.prateek.model.Coin;
import com.prateek.model.User;
import com.prateek.model.Watchlist;
import com.prateek.repository.WatchlistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class WatchlistServiceImpl implements WatchlistService {

    @Autowired
    private WatchlistRepository watchlistRepository;

    @Override
    @Transactional(readOnly = true)
    public Watchlist findUserWatchlist(Long userId) throws Exception {
        Watchlist watchlist = watchlistRepository.findByUserId(userId);
        if (watchlist == null) {
            throw new Exception("Watchlist not found");
        }
        // Initialize lazy collection
        watchlist.getCoins().size();
        return watchlist;
    }

    @Override    // ye read only nahi hoga.
    public Watchlist createWatchList(User user) {
        Watchlist watchlist = new Watchlist();
        watchlist.setUser(user);
        return watchlistRepository.save(watchlist);
    }

    @Override
    @Transactional(readOnly = true)
    public Watchlist findById(Long id) throws Exception {
        Optional<Watchlist> optionalWatchlist = watchlistRepository.findById(id);
        if (optionalWatchlist.isEmpty()) {
            throw new Exception("Watchlist not found");
        }
        optionalWatchlist.get().getCoins().size();
        return optionalWatchlist.get();
    }

    @Override   // this also can not be readOnly Transactional
    public Coin addItemToWatchlist(Coin coin, User user) throws Exception {
        Watchlist watchlist = findUserWatchlist(user.getId());

        if (watchlist.getCoins().contains(coin)) {
            watchlist.getCoins().remove(coin);
        } else {
            watchlist.getCoins().add(coin);
        }

        watchlistRepository.save(watchlist);
        return coin;
    }
}