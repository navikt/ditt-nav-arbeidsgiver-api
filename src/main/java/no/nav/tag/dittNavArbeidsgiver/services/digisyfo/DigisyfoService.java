package no.nav.tag.dittNavArbeidsgiver.services.digisyfo;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.dittNavArbeidsgiver.models.DigisyfoNarmesteLederRespons;
import no.nav.tag.dittNavArbeidsgiver.services.aad.AadAccessToken;
import no.nav.tag.dittNavArbeidsgiver.services.aad.AccesstokenClient;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import static no.nav.security.token.support.core.JwtTokenConstants.AUTHORIZATION_HEADER;

@Slf4j
@Service
@Setter
@ConfigurationProperties("digisyfo")
public class DigisyfoService {

    private final AccesstokenClient accesstokenClient;
    private final RestTemplate restTemplate;

    String digisyfoUrl;

    public DigisyfoService(AccesstokenClient accesstokenClient, RestTemplate restTemplate) {
        this.accesstokenClient = accesstokenClient;
        this.restTemplate = restTemplate;
    }

    public DigisyfoNarmesteLederRespons getNarmesteledere(String aktørId) {
        try {
            return hentNarmesteLederFraDigiSyfo(aktørId);
        } catch (RestClientException e1) {
            AadAccessToken token = accesstokenClient.hentAccessToken();
            log.warn("Kall mot digisyfo feilet - kan skyldes utløpt token. expires_in: {}, ext_expires_in: {}, expires_on: {}", 
                    token.getExpires_in(),
                    token.getExt_expires_in(), 
                    token.getExpires_on(), 
                    e1);
            accesstokenClient.evict();
            try {
                return hentNarmesteLederFraDigiSyfo(aktørId);
            } catch (RestClientException e2) {
                log.error(" Digisyfo Exception: ", e2);
                throw new RuntimeException(" Digisyfo Exception: " + e2);
            }
        }
    }

    private DigisyfoNarmesteLederRespons hentNarmesteLederFraDigiSyfo(String aktørId) {
        String url = UriComponentsBuilder.fromHttpUrl(digisyfoUrl + aktørId).toUriString();
        ResponseEntity<DigisyfoNarmesteLederRespons> respons = restTemplate.exchange(
                url,
                HttpMethod.GET, 
                getRequestEntity(),
                DigisyfoNarmesteLederRespons.class);
        
        if (respons.getStatusCode() != HttpStatus.OK) {
            String message = "Kall mot digisyfo feiler med HTTP-" + respons.getStatusCode();
            log.error(message);
            throw new RuntimeException(message);
        }
        return respons.getBody();
    }

    private HttpEntity<String> getRequestEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(AUTHORIZATION_HEADER, "Bearer " + accesstokenClient.hentAccessToken().getAccess_token());
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        return new HttpEntity<>(headers);
    }
}

