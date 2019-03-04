package com.algotrader.breakout.client;

import com.algotrader.breakout.domain.Quote;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.stream.BaseStream;

/**
 *
 */
@Slf4j
@Setter
@Component
@ConfigurationProperties("fxquotes")
public class FxQuoteClient {

    private String name;

    /**
     * Parsing a structured text file with columns datetime - open - low - high - close into a Quote object
     * @return a flux or single quotes
     */
    public Flux<Quote> getQuotes() {

        Path path = Paths.get(name);

        return Flux.using(
                () ->
                    Files.lines(path)
                         .skip(1)
                         .map(line -> {
                                 try {
                                     String dateString = line.split(",")[0].toString();
                                     SimpleDateFormat parser = new SimpleDateFormat("dd.MM.yyyy");
                                     Date date = parser.parse(line.split(",")[0]);
                                     return new Quote(
                                             date,
                                             new BigDecimal(line.split(",")[1]),
                                             new BigDecimal(line.split(",")[2]),
                                             new BigDecimal(line.split(",")[3]),
                                             new BigDecimal(line.split(",")[4])
                                     );
                                 } catch (Exception e) {
                                     log.debug(e.getMessage());
                                     log.error(e.getStackTrace().toString());
                                     return null;
                                 }
                            }
                          )
                ,
                Flux::fromStream,
                BaseStream::close
                );

    }
}
