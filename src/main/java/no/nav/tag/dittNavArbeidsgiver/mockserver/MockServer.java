package no.nav.tag.dittNavArbeidsgiver.mockserver;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.net.URL;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static java.nio.charset.StandardCharsets.UTF_8;

@Profile({"dev"})
@Slf4j
@Component
public class MockServer {

    public static final String SERVICE_EDITION = "1";
    public static final String SERVICE_CODE = "4936";
    public static final String FNR_MED_SKJEMATILGANG = "01065500791";
    public static final String FNR_MED_ORGANISASJONER = "00000000000";

    @SneakyThrows
    @Autowired
    MockServer(
            @Value("${altinn.altinnUrl}") String altinnUrl,
            @Value("${mock.port}") int port,
            @Value("${sts.stsUrl}") String stsUrl,
            @Value("${aad.aadAccessTokenURL}") String aadUrl,
            @Value("${aktorregister.aktorUrl}") String aktorUrl,
            @Value("${digisyfo.sykemeldteURL}") String sykemeldteUrl,
            @Value("${digisyfo.syfooppgaveurl}") String syfoOpggaveUrl,
            @Value("${digisyfo.digisyfoUrl}") String digisyfoUrl,
            @Value("${pdl.pdlUrl}") String pdlUrl,
            @Value("${aareg.aaregArbeidsforhold}") String aaregArbeidsforholdUrl,
            @Value("${aareg.aaregArbeidsgivere}") String aaregArbeidsgivereUrl,
            @Value("${ereg.url}") String eregUrl
    ) {
        log.info("starter mockserveren");
        WireMockServer server = new WireMockServer(new WireMockConfiguration().port(port).extensions(new ResponseTemplateTransformer(true)));
        String altinnPath = new URL(altinnUrl).getPath();
        String stsPath = new URL(stsUrl).getPath();
        String aadPath = new URL(aadUrl).getPath();
        String aktorPath = new URL(aktorUrl).getPath();
        String sykemeldtePath = new URL(sykemeldteUrl).getPath();
        String syfoOppgavePath = new URL(syfoOpggaveUrl).getPath();
        String syfoNarmesteLederPath = new URL(digisyfoUrl).getPath();
        String aaregArbeidsforholdPath = new URL(aaregArbeidsforholdUrl).getPath();
        String aaregArbeidsgiverePath = new URL(aaregArbeidsgivereUrl).getPath();
        String pdlPath = new URL(pdlUrl).getPath();
        String eregPath = new URL(eregUrl).getPath();
        mocktilgangTilSkjemForBedrift(server,altinnPath);
        mockOrganisasjoner(server, altinnPath);
        mockInvalidSSN(server, altinnPath);
        mockForPath(server, altinnPath + "authorization/roles", "roles.json");
        mockForPath(server, stsPath, "STStoken.json");
        mockForPath(server, aadPath, "aadtoken.json");
        mockForPath(server, aktorPath, "aktorer.json");
        mockForPath(server, sykemeldtePath, "sykemeldinger.json");
        mockForPath(server, syfoOppgavePath, "syfoOppgaver.json");
        mockForPath(server, syfoNarmesteLederPath, "narmesteLeder.json");
        mockForPath(server, pdlPath,"pdlRespons.json");
        mockForPath(server, aaregArbeidsforholdPath,"tomRespons.json");
        mockForPath(server, aaregArbeidsgiverePath,"arbeidsgiveroversiktaareg.json");
        mockForPath(server, eregPath,"enhetsregisteret.json");
        mockArbeidsforholdmedJuridiskEnhet(server, aaregArbeidsforholdPath);
        server.start();
    }

    private static void mockOrganisasjoner(WireMockServer server, String altinnPath) {
        server.stubFor(WireMock.get(WireMock.urlPathEqualTo(altinnPath + "reportees/"))
                .withQueryParam("subject", equalTo(FNR_MED_ORGANISASJONER))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(hentStringFraFil("organisasjoner.json"))
                ));
    }
    private static void mockAaRegMedHeader(WireMockServer server, String aaRegPath){
        server.stubFor(WireMock.get(WireMock.urlPathEqualTo(aaRegPath + ""))
                .withQueryParam("subject", equalTo(FNR_MED_SKJEMATILGANG))
                .withQueryParam("serviceCode", equalTo(SERVICE_CODE))
                .withQueryParam("serviceEdition", equalTo(SERVICE_EDITION))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(hentStringFraFil("rettigheterTilSkjema.json"))
                ));
    }
    private static void mocktilgangTilSkjemForBedrift(WireMockServer server, String altinnPath) {
        server.stubFor(WireMock.get(WireMock.urlPathEqualTo(altinnPath + "reportees/"))
                .withQueryParam("subject", equalTo(FNR_MED_SKJEMATILGANG))
                .withQueryParam("serviceCode", equalTo(SERVICE_CODE))
                .withQueryParam("serviceEdition", equalTo(SERVICE_EDITION))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(hentStringFraFil("rettigheterTilSkjema.json"))
                ));
    }

    private static void mockArbeidsforholdmedJuridiskEnhet(WireMockServer server, String path) {
        server.stubFor(WireMock.get(WireMock.urlPathEqualTo(path))
                .withHeader("Nav-Opplysningspliktigident", equalTo("983887457") )
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(hentStringFraFil("arbeidsforholdrespons.json"))
                ));
    }

    private static void mockInvalidSSN(WireMockServer server, String altinnPath) {
        server.stubFor(WireMock.get(WireMock.urlPathEqualTo(altinnPath + "reportees/"))
                .withQueryParam("subject", notMatching(FNR_MED_ORGANISASJONER + "|" + FNR_MED_SKJEMATILGANG))
                .willReturn(WireMock.aResponse().withStatusMessage("Invalid socialSecurityNumber").withStatus(400)
                        .withHeader("Content-Type", "application/octet-stream")
                ));
    }

    private static void mockForPath(WireMockServer server, String path, String responseFile){
        server.stubFor(WireMock.any(WireMock.urlPathMatching(path + ".*"))
            .willReturn(WireMock.aResponse()
                .withHeader("Content-Type","application/json")
                .withBody(hentStringFraFil(responseFile))
        ));
    }

    @SneakyThrows
    private static String hentStringFraFil(String filnavn) {
        return IOUtils.toString(MockServer.class.getClassLoader().getResourceAsStream("mock/" + filnavn), UTF_8);
    }
}
