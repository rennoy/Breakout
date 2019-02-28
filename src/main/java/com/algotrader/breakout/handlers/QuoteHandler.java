package com.algotrader.breakout.handlers;

import ch.algotrader.entity.trade.MarketOrder;
import ch.algotrader.enumeration.Side;
import ch.algotrader.simulation.Simulator;
import com.algotrader.breakout.client.FxQuoteClient;
import com.algotrader.breakout.domain.Quote;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Slf4j
@Component
public class QuoteHandler {

    private final FxQuoteClient fxQuoteClient;

    private final Simulator simulator;

    public QuoteHandler(FxQuoteClient fxQuoteClient, Simulator simulator) {
        this.fxQuoteClient = fxQuoteClient;
        this.simulator = simulator;
    }

    public void runStrategy() {

        fxQuoteClient.getQuotes()
                .map(quote -> {
                    this.simulator.setCurrentPrice(quote.getClose().doubleValue());
                    return quote;
                })
                .buffer(10, 1)
                .filter(quotes -> quotes.size() == 10)
                .subscribe(
                    quotes -> {

                        BigDecimal mean = QuotesHandler.getMean.apply(quotes);
                        BigDecimal stdev = QuotesHandler.getSumSq.apply(quotes);

                        Quote lastQuote = quotes.get(quotes.size()-1);
                        BigDecimal close = lastQuote.getClose();

                        Long position = simulator.getPosition() == null ? 0 : simulator.getPosition().getQuantity();
                        Integer aboveBb = QuotesHandler.aboveBollingerBandIndicator.apply(quotes, 2L);
                        Integer belowBb = QuotesHandler.belowBollingerBandIndicator.apply(quotes, 2L);
                        Integer aboveMean = close.compareTo(mean);

                        if (position == 0 && aboveBb > 0) {
                            simulator.sendOrder(new MarketOrder(Side.SELL,
                                    (long) (simulator.getCashBalance() / close.doubleValue())));
                            log.info("Date: " + lastQuote.getDateTime() + " Position short: " + simulator.getPosition().getQuantity() + " - Price" + lastQuote.getClose());
                        } else if (position == 0 && belowBb > 0) {
                            simulator.sendOrder(new MarketOrder(Side.BUY,
                                    (long) (simulator.getCashBalance() / close.doubleValue())));
                            log.info("Date: " + lastQuote.getDateTime() + " Position long: " + simulator.getPosition().getQuantity() + " - Price" + lastQuote.getClose());
                        } else if (position > 0 && aboveMean > 0) {
                            simulator.sendOrder(new MarketOrder(Side.SELL, Math.abs(simulator.getPosition().getQuantity())));
                            log.info("Date: " + lastQuote.getDateTime() + " No position - cash balance: " + simulator.getCashBalance() + " - Price" + lastQuote.getClose());
                        } else if (position < 0 && aboveMean < 0) {
                            simulator.sendOrder(new MarketOrder(Side.BUY, Math.abs(simulator.getPosition().getQuantity())));
                            log.info("Date: " + lastQuote.getDateTime() + " No position - cash balance: " + simulator.getCashBalance() + " - Price" + lastQuote.getClose());
                        }
                    }
                )

                ;
    }
}
