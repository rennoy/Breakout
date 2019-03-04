package com.algotrader.breakout.handlers;

import ch.algotrader.entity.trade.MarketOrder;
import ch.algotrader.enumeration.Side;
import ch.algotrader.simulation.Simulator;
import com.algotrader.breakout.client.FxQuoteClient;
import com.algotrader.breakout.domain.Quote;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class QuoteHandler implements QuotesHandler {

    private final FxQuoteClient fxQuoteClient;

    private final Simulator simulator;

    public QuoteHandler(FxQuoteClient fxQuoteClient, Simulator simulator) {
        this.fxQuoteClient = fxQuoteClient;
        this.simulator = simulator;
    }

    /**
     * Runs the full strategy - a buffer of the last 10 observations is kept in memory -
     * This could be prevented if instead of computing the standard deviation as SUM[(Close-Avg)**2]/(N-1), instead,
     * we compute a standard deviation based on squared returns SUM[((Close-prevClose))**2]/(N-1)
     */
    public void runStrategy() {

        fxQuoteClient.getQuotes()
                .map(quote -> {
                    this.simulator.setCurrentPrice(quote.getClose().doubleValue());
                    return quote;
                })
                .buffer(10, 1)
                .filter(quotes -> quotes.size() == 10)
                .map(quotes -> new Object[] {
                        quotes.get(quotes.size()-1),                    // last quote of the 10-size buffered sample
                        this.bollingerBandIndicator(quotes, 2L)      // indicators
                })
                .subscribe(
                    v -> {
                        Quote lastQuote = (Quote) v[0];
                        Integer[] indicators = (Integer[]) v[1];

                        Long position = simulator.getPosition() == null ? 0 : simulator.getPosition().getQuantity();

                        if (position == 0 && indicators[0] > 0) {
                            simulator.sendOrder(new MarketOrder(Side.SELL,
                                    (long) (simulator.getCashBalance() / lastQuote.getClose().doubleValue())));
                            log.info("Date: " + lastQuote.getDateTime()
                                    + " Position short: " + simulator.getPosition().getQuantity()
                                    + " - Price" + lastQuote.getClose());
                        } else if (position == 0 && indicators[1] > 0) {
                            simulator.sendOrder(new MarketOrder(Side.BUY,
                                    (long) (simulator.getCashBalance() / lastQuote.getClose().doubleValue())));
                            log.info("Date: " + lastQuote.getDateTime()
                                    + " Position long: " + simulator.getPosition().getQuantity()
                                    + " - Price" + lastQuote.getClose());
                        } else if (position > 0 && indicators[2] > 0) {
                            simulator.sendOrder(new MarketOrder(Side.SELL, Math.abs(simulator.getPosition().getQuantity())));
                            log.info("Date: " + lastQuote.getDateTime()
                                   + " No position - cash balance: " + simulator.getCashBalance()
                                   + " - Price" + lastQuote.getClose());
                        } else if (position < 0 && indicators[2] < 0) {
                            simulator.sendOrder(new MarketOrder(Side.BUY, Math.abs(simulator.getPosition().getQuantity())));
                            log.info("Date: " + lastQuote.getDateTime()
                                   + " No position - cash balance: " + simulator.getCashBalance()
                                   + " - Price" + lastQuote.getClose());
                        }
                    }
                )

                ;
    }
}
