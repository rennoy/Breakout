package com.algotrader.breakout.handlers;

import com.algotrader.breakout.domain.Quote;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math.stat.descriptive.moment.StandardDeviation;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;
import java.math.MathContext;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class QuoteHandlerTest {

    @Autowired
    private QuoteHandler quoteHandler;

    private final List<Quote> prices = new ArrayList<>();
    private final Random random = new Random();
    private final MathContext mathContext = new MathContext(6);
    private final SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");

    private Flux<Quote> source;

    private Quote updateQuote(Quote quote) {
        BigDecimal priceChange = quote.getClose()
                .multiply(new BigDecimal(0.001 * this.random.nextDouble(), this.mathContext), this.mathContext);

        return new Quote(
                new Date(quote.getDateTime().getTime() + TimeUnit.DAYS.toMillis(1)),
                quote.getClose().add(priceChange),
                quote.getClose().add(priceChange),
                quote.getClose().add(priceChange),
                quote.getClose().add(priceChange)
                );
    }

    @Before
    public void init() throws Exception {

        Quote quote = new Quote(
                df.parse("01/01/2019"),
                new BigDecimal(0.9507, this.mathContext),
                new BigDecimal(0.9507, this.mathContext),
                new BigDecimal(0.9507, this.mathContext),
                new BigDecimal(0.9507, this.mathContext));

        source = Flux.generate(() -> 0,
                (index, sink) -> {
                    Quote updateQuote = updateQuote(quote);
                    sink.next(updateQuote);
                    return ++index % 100;
                })
                .map(q -> (Quote)q)
        ;
    }

    @Test
    public void testGetFirstOrderStats() {

        source.take(10).buffer(10).subscribe( quotes -> {

                    BigDecimal[] stats = quoteHandler.getFirstOrderStats(quotes);

                    DoubleSummaryStatistics dstats = quotes.stream().map(Quote::getClose).collect(
                            Collectors.summarizingDouble(BigDecimal::doubleValue));

                    Assert.assertNotNull(stats);
                    Assert.assertEquals(stats.length, 3);
                    Assert.assertEquals(stats[0], quotes.get(quotes.size() - 1).getClose());
                    Assert.assertTrue(Math.abs(stats[1].doubleValue() - dstats.getSum()) < 0.00000001);
                    Assert.assertTrue(Math.abs(stats[2].doubleValue() - dstats.getAverage()) < 0.00000001);
                }
        );

    }

    @Test
    public void testGetSecondOrderStats() {

        source.take(10).buffer(10).subscribe( quotes -> {

                    double dstdev = new StandardDeviation()
                            .evaluate(quotes.stream().map(Quote::getClose)
                                    .map(BigDecimal::doubleValue)
                                    .mapToDouble(d -> d)
                                    .toArray());

                    BigDecimal stdev = quoteHandler.getSecondOrderStats(quotes);

                    Assert.assertNotNull(stdev);
                    Assert.assertTrue(Math.abs(stdev.doubleValue() - dstdev) < 0.00000001);
                }
        );
    }
}
