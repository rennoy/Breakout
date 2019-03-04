package com.algotrader.breakout.client;

import com.algotrader.breakout.domain.Quote;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Flux;

@RunWith(SpringRunner.class)
@SpringBootTest
public class FxQuoteClientTest {

    @Autowired
    private FxQuoteClient fxQuoteClient;

    @Test
    public void testGetQuotes() {

        Flux<Quote> quotes = fxQuoteClient.getQuotes();
        Assert.assertNotNull(quotes);

        Quote firstQuote = quotes.blockFirst();

        Assert.assertNotNull(firstQuote);
        Assert.assertEquals(firstQuote.getClass(), Quote.class);
        Assert.assertNotNull(firstQuote.getDateTime());
        Assert.assertNotNull(firstQuote.getClose());
        Assert.assertNotNull(firstQuote.getHigh());
        Assert.assertNotNull(firstQuote.getLow());
        Assert.assertNotNull(firstQuote.getOpen());
    }
}
