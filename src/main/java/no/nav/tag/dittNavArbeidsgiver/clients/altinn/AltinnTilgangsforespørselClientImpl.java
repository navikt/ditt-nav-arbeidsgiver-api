package no.nav.tag.dittNavArbeidsgiver.clients.altinn;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.dittNavArbeidsgiver.clients.altinn.dto.DelegationRequest;
import no.nav.tag.dittNavArbeidsgiver.clients.altinn.dto.Søknadsstatus;
import no.nav.tag.dittNavArbeidsgiver.models.AltinnTilgangsforespørsel;
import no.nav.tag.dittNavArbeidsgiver.models.AltinnTilgangssøknadsskjema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Profile({"prod", "dev"})
@Component
public class AltinnTilgangsforespørselClientImpl implements AltinnTilgangsforespørselClient {
    private final RestTemplate restTemplate;
    private final HttpHeaders altinnHeaders;
    private final UriComponentsBuilder altinnUriBuilder;
    private final URI altinnUri;
    private final ParameterizedTypeReference<Søknadsstatus> søknadsstatusType;
    private final ParameterizedTypeReference<DelegationRequest> delegationRequestType;

    @Autowired
    public AltinnTilgangsforespørselClientImpl(RestTemplate restTemplate, AltinnConfig altinnConfig) {
        this.restTemplate = restTemplate;

        this.altinnUriBuilder = UriComponentsBuilder.fromUriString(
                altinnConfig.getProxyFallbackUrl()
                        + "/ekstern/altinn/api/serviceowner/delegationRequests?ForceEIAuthentication"
        );


        this.altinnUri = altinnUriBuilder.build().toUri();
        log.info("altinnUri: {}", altinnUri.toString());

        this.altinnHeaders = new HttpHeaders();
        this.altinnHeaders.add("accept", "application/hal+json");
        this.altinnHeaders.add("apikey", altinnConfig.getAltinnHeader());
        this.altinnHeaders.add("x-nav-apikey", altinnConfig.getAPIGwHeader());

        this.søknadsstatusType = new ParameterizedTypeReference<>() {
        };

        this.delegationRequestType = new ParameterizedTypeReference<>() {
        };
    }


    @Override
    public List<AltinnTilgangsforespørsel> hentAlleSøknader() {
        var resultat = new ArrayList<AltinnTilgangsforespørsel>();
        URI uri = altinnUri;

        do {
            var request = RequestEntity.get(uri).headers(altinnHeaders).build();
            var response = restTemplate.exchange(request, søknadsstatusType);

            if (response.getStatusCode() != HttpStatus.OK) {
                var msg = String.format("Henting av status på tilgangssøknader feilet med http-status %s", response.getStatusCode());
                log.error(msg);
                throw new RuntimeException(msg);
            }

            var body = response.getBody();
            if (body.embedded.delegationRequests.isEmpty()) {
                uri = null;
            } else {
                uri = altinnUriBuilder
                        .cloneBuilder()
                        .queryParam("continuation", body.continuationtoken)
                        .build()
                        .toUri();
            }

            body.embedded
                    .delegationRequests
                    .stream()
                    .map(søknadDTO -> {
                        var søknad = new AltinnTilgangsforespørsel();
                        søknad.setId(søknadDTO.Guid);
                        søknad.setOrgnr(søknadDTO.OfferedBy);
                        søknad.setFnr(søknadDTO.CoveredBy);
                        søknad.setStatus(søknadDTO.RequestStatus);
                        søknad.setCreatedDateTime(søknadDTO.Created);
                        søknad.setLastChangedDateTime(søknadDTO.LastChanged);
                        søknad.setServiceCode(søknadDTO.RequestResources.get(0).ServiceCode);
                        søknad.setServiceEdition(søknadDTO.RequestResources.get(0).ServiceEditionCode);
                        søknad.setSubmitUrl(søknadDTO.links.sendRequest.href);
                        return søknad;
                    })
                    .filter(forespørsel -> {
                        if (forespørsel.isValid()) {
                            return true;
                        }
                        log.error("Mottatt altinn tilgangsforespørsel er ugyldig: {}", forespørsel);
                        return false;
                    })
                    .collect(Collectors.toCollection(() -> resultat));
        } while (uri != null);

        return resultat;
    }

    @Override
    public AltinnTilgangsforespørsel sendSøknad(AltinnTilgangssøknadsskjema søknadsskjema) {
        var requestResource = new DelegationRequest.RequestResource();
        requestResource.ServiceCode = søknadsskjema.serviceCode;
        requestResource.ServiceEditionCode = søknadsskjema.serviceEdition;

        var delegationRequest = new DelegationRequest();
        delegationRequest.CoveredBy = søknadsskjema.fnr;
        delegationRequest.OfferedBy = søknadsskjema.orgnr;
        delegationRequest.RedirectUrl = søknadsskjema.redirectUrl;
        delegationRequest.RequestResources = List.of(requestResource);

        var request = RequestEntity
                .post(altinnUri)
                .headers(altinnHeaders)
                .body(delegationRequest);

        var response = restTemplate.exchange(request, delegationRequestType);

        if (response.getStatusCode() != HttpStatus.OK) {
            var msg = String.format("Ny tilgangssøknad i altinn feilet med http-status %s", response.getStatusCode());
            log.error(msg);
            throw new RuntimeException(msg);
        }

        var body = response.getBody();

        var svar = new AltinnTilgangsforespørsel();
        svar.setStatus(body.RequestStatus);
        svar.setSubmitUrl(body.links.sendRequest.href);
        return svar;
    }

    @Override
    public void delete(String guid) {
        var url = altinnUriBuilder.cloneBuilder()
                .pathSegment(guid)
                .build()
                .toUri();

        var request = RequestEntity
                .delete(url)
                .headers(altinnHeaders)
                .build();
        var response = restTemplate.exchange(request, Void.class);

        if (response.getStatusCode() != HttpStatus.OK) {
            var msg = String.format("sletting av tilgangssøknad i altinn feilet med http-status %s", response.getStatusCode());
            log.error(msg);
            throw new RuntimeException(msg);
        }
    }
}
