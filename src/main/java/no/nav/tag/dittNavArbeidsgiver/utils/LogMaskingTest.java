package no.nav.tag.dittNavArbeidsgiver.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LogMaskingTest implements InitializingBean {

    /* Log noe som ligner på et fødselsnummer, så det er lett å sjekke i
     * kibana om masking er configurert riktig.
     */
    @Override
    public void afterPropertiesSet() {
        log.info("Følgende skal være maskert: 11223344455.");
    }
}
