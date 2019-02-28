package com.algotrader.breakout.handlers;

import com.algotrader.breakout.domain.Quote;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public interface QuotesHandler {

    Function<List<Quote>, BigDecimal> getSum = quotes ->
            quotes.stream().map(Quote::getClose).reduce(BigDecimal.ZERO, BigDecimal::add);

    Function<List<Quote>, BigDecimal> getMean = quotes -> {
        BigDecimal count = new BigDecimal(quotes.size());
        return getSum.apply(quotes).divide(count);
    };

    Function<List<Quote>, BigDecimal> getSumSq = quotes -> {
        BigDecimal mean = getMean.apply(quotes);
        return quotes.stream()
                .map(q -> q.getClose().subtract(mean).multiply(q.getClose().subtract(mean)))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    };

    Function<List<Quote>, BigDecimal> getStdDev = quotes -> {
        BigDecimal count = new BigDecimal(quotes.size());
        BigDecimal sqSum = getSumSq.apply(quotes);
        return BigDecimal.valueOf(
                StrictMath.sqrt(sqSum.divide(count.subtract(BigDecimal.ONE), RoundingMode.HALF_DOWN).doubleValue()));
    };

    BiFunction<List<Quote>, Long, Integer> aboveBollingerBandIndicator = (quotes, k) -> {
        Quote lastQuote = quotes.get(quotes.size()-1);
        BigDecimal close = lastQuote.getClose();
        return close.compareTo(getMean.apply(quotes).add(getStdDev.apply(quotes).multiply(BigDecimal.valueOf(k))));
    };

    BiFunction<List<Quote>, Long, Integer> belowBollingerBandIndicator = (quotes, k) -> {
        Quote lastQuote = quotes.get(quotes.size()-1);
        BigDecimal close = lastQuote.getClose();
        return getMean.apply(quotes).subtract(getStdDev.apply(quotes).multiply(BigDecimal.valueOf(k))).compareTo(close);
    };

}
