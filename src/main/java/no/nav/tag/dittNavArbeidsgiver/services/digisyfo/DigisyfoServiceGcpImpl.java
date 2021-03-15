package no.nav.tag.dittNavArbeidsgiver.services.digisyfo;


import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.dittNavArbeidsgiver.models.DigisyfoNarmesteLederRespons;
import no.nav.tag.dittNavArbeidsgiver.services.tokenExchange.TokenExchangeClient;
import no.nav.tag.dittNavArbeidsgiver.utils.TokenUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import static no.nav.security.token.support.core.JwtTokenConstants.AUTHORIZATION_HEADER;

@Profile({"dev-gcp", "prod-gcp", "local"})
@Slf4j
@Service
@Setter
public class DigisyfoServiceGcpImpl implements DigisyfoService{

    private final RestTemplate restTemplate;
    private TokenExchangeClient tokenExchangeClient;
    private final TokenUtils tokenUtils;

    String proxyUrl = "https://min-side-arbeidsgiver-proxy.dev-fss-pub.nais.io/narmesteleder";

    public DigisyfoServiceGcpImpl(RestTemplate restTemplate, TokenExchangeClient tokenExchangeClient, TokenUtils tokenUtils) {
        this.restTemplate = restTemplate;
        this.tokenExchangeClient = tokenExchangeClient;
        this.tokenUtils = tokenUtils;
    }

    @Override
    public DigisyfoNarmesteLederRespons getNarmesteledere(String fnr) {
        String url = proxyUrl;
        try {
            return hentNarmesteLederFraDigiSyfo(getRequestEntity(), url);
        } catch (RestClientException e1) {
                log.error(" Digisyfo Exception: ", e1);
                throw new RuntimeException(" Digisyfo Exception: " + e1);
            }
    }

    private DigisyfoNarmesteLederRespons hentNarmesteLederFraDigiSyfo(HttpEntity<String> entity, String url) {
        ResponseEntity<DigisyfoNarmesteLederRespons> respons = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                DigisyfoNarmesteLederRespons.class);
        if (respons.getStatusCode() != HttpStatus.OK) {
            String message = "Kall mot digisyfo feiler med HTTP-" + respons.getStatusCode();
            log.error(message);
            throw new RuntimeException(message);
        }
        return respons.getBody();
    }

    private HttpEntity <String> getRequestEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(AUTHORIZATION_HEADER, "Bearer " +  tokenExchangeClient.exchangeToken(tokenUtils.getTokenForInnloggetBruker()).getAccess_token());
        return new HttpEntity<>(headers);
    }


}