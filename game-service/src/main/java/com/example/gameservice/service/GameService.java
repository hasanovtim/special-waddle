package com.example.gameservice.service;

import com.example.gameservice.entity.GameEntity;
import com.example.gameservice.exception.InsufficientFundsException;
import com.example.gameservice.exception.WalletException;
import com.example.gameservice.response.GameResponse;
import com.example.gameservice.response.WalletResponse;
import com.example.gameservice.repository.GameRepository;
import com.example.gameservice.request.GameRequest;
import com.example.gameservice.request.WalletRequest;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GameService {
    private final GameRepository gameRepository;
    private final WalletClient walletClient;

    @Transactional
    public GameResponse placeBet(String gameId, GameRequest gameRequest) {
        String playerId = gameRequest.getPlayerId();
        BigDecimal betAmount = gameRequest.getAmount();
        log.info(String.format("Place a bet. Game: %s Player: %s Amount: %s", gameId,
            playerId, gameRequest.getAmount()));
        WalletResponse wallet;
        try {
            wallet = walletClient.getWallet(playerId);
        } catch (Exception error) {
            throw new WalletException(error.getMessage());
        }
        if (wallet.getBalance().compareTo(betAmount) < 0) {
            throw new InsufficientFundsException(playerId);
        }

        walletClient.withDraw(playerId, new WalletRequest(betAmount));
        gameRepository.save(new GameEntity(gameId, playerId, betAmount));

        return new GameResponse(playerId, gameId, betAmount);
    }

    public List<GameEntity> getBets(String gameId) {
        log.info(String.format("Get bets for game: %s", gameId));
        return gameRepository.findByGameId(gameId);
    }

    public List<GameEntity> getAllBets() {
        log.info("Get all bets");
        return gameRepository.findAll();
    }
}
