package com.algotrader.breakout.handlers;

import com.algotrader.breakout.domain.Quote;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 *  Handles all signal computation once a new quote is provided
 */
public interface QuotesHandler {

    /**
     * Returns basic stats using reactive
     * TODO: create stats class - using the DoubleSummaryStatistics model for BigDecimal -
     *
     * @param quotes buffer list
     * @return BigDecimal vector of close, trailing sum, trailing mean
     */
    default BigDecimal[] getFirstOrderStats(List<Quote> quotes) {
        return Flux.fromIterable(quotes)
                .filter(qs -> qs != null)
                .map(q -> new BigDecimal[]{q.getClose(), q.getClose(), BigDecimal.ONE})
                .reduce((a, b) ->
                        new BigDecimal[]{b[0], a[1].add(b[1]), a[2].add(BigDecimal.ONE)})
                .map(v -> new BigDecimal[]
                        {v[0], v[1], v[1].divide(v[2], RoundingMode.HALF_DOWN)})
                .block();
    }


    /**
     * Returns second order stats using reactive - second order stats use first order stat stream already
     *
     * @param quotes buffer list
     * @return BigDecimal standard deviation - TODO: sqrt of BigDecimal
     */
    default BigDecimal getSecondOrderStats(List<Quote> quotes) {
        BigDecimal[] stats = getFirstOrderStats(quotes);
        return Flux.fromIterable(quotes)
                .map(q -> new BigDecimal[]{(q.getClose().subtract(stats[2])).pow(2), BigDecimal.ZERO})
                .reduce((a, b) -> new BigDecimal[]{a[0].add(b[0]), a[1].add(BigDecimal.ONE)})
                .map(v -> BigDecimal.valueOf(Math.sqrt(v[0].divide(v[1], RoundingMode.HALF_DOWN).doubleValue())))
                .block();
    }


    /**
     * Returns the close position wrt to the upper, lower bollinger bands, and mean
     * TODO: create Signal class
     *
     * @param quotes flux
     * @param k std deviations from the mean
     * @return integers -
     * first integer == 1 if close above the upper bollinger band
     * first integer == 1 if close above the mean
     * first integer == 1 if close below the lower bollinger band
     */
    default Integer[] bollingerBandIndicator(List<Quote> quotes, Long k) {

        BigDecimal[] firstOrderStats = getFirstOrderStats(quotes);
        BigDecimal stdev = getSecondOrderStats(quotes);

        return bollingerBandIndicator(firstOrderStats, stdev, k);
    }

    /**
     * Returns the close position wrt to the upper, lower bollinger bands, and mean
     * TODO: create Signal class
     *
     * @param firstOrderStats close, sum and mean
     * @param stdev standard deviation
     * @param k std deviations from the mean
     * @return integers -
     * first integer == 1 if close above the upper bollinger band
     * first integer == 1 if close above the mean
     * first integer == 1 if close below the lower bollinger band
     */
    default Integer[] bollingerBandIndicator(BigDecimal[] firstOrderStats, BigDecimal stdev, Long k) {

        Integer aboveMean = 1;
        Integer belowLower = 0;

        Integer aboveUpper = firstOrderStats[0].compareTo(
                firstOrderStats[2].add(stdev.multiply(BigDecimal.valueOf(k))));

        if (aboveUpper < 1) {
            belowLower = firstOrderStats[2].subtract(stdev.multiply(BigDecimal.valueOf(k)))
                    .compareTo(firstOrderStats[0]);

            aboveMean = belowLower == 1 ? 0 : firstOrderStats[0].compareTo(firstOrderStats[2]);
        }

        return new Integer[]{aboveUpper, belowLower, aboveMean};
    }

}
