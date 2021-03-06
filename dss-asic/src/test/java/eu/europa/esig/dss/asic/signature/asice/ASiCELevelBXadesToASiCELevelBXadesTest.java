package eu.europa.esig.dss.asic.signature.asice;

import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.junit.Test;

import eu.europa.esig.dss.DSSDocument;
import eu.europa.esig.dss.InMemoryDocument;
import eu.europa.esig.dss.SignatureAlgorithm;
import eu.europa.esig.dss.SignatureForm;
import eu.europa.esig.dss.SignatureLevel;
import eu.europa.esig.dss.SignaturePackaging;
import eu.europa.esig.dss.SignatureValue;
import eu.europa.esig.dss.ToBeSigned;
import eu.europa.esig.dss.asic.ASiCSignatureParameters;
import eu.europa.esig.dss.asic.signature.ASiCService;
import eu.europa.esig.dss.test.TestUtils;
import eu.europa.esig.dss.test.gen.CertificateService;
import eu.europa.esig.dss.test.mock.MockPrivateKeyEntry;
import eu.europa.esig.dss.validation.CertificateVerifier;
import eu.europa.esig.dss.validation.CommonCertificateVerifier;
import eu.europa.esig.dss.validation.SignedDocumentValidator;
import eu.europa.esig.dss.validation.reports.Reports;
import eu.europa.esig.dss.validation.reports.wrapper.DiagnosticData;

public class ASiCELevelBXadesToASiCELevelBXadesTest {

	@Test
	public void test() throws Exception {
		DSSDocument documentToSign = new InMemoryDocument("Hello Wolrd !".getBytes(), "test.text");

		CertificateService certificateService = new CertificateService();
		MockPrivateKeyEntry privateKeyEntry = certificateService.generateCertificateChain(SignatureAlgorithm.RSA_SHA256);

		ASiCSignatureParameters signatureParameters = new ASiCSignatureParameters();
		signatureParameters.bLevel().setSigningDate(new Date());
		signatureParameters.setSigningCertificate(privateKeyEntry.getCertificate());
		signatureParameters.setCertificateChain(privateKeyEntry.getCertificateChain());
		signatureParameters.setSignaturePackaging(SignaturePackaging.ENVELOPING);
		signatureParameters.setSignatureLevel(SignatureLevel.ASiC_E_BASELINE_B);
		signatureParameters.aSiC().setUnderlyingForm(SignatureForm.XAdES);

		CertificateVerifier certificateVerifier = new CommonCertificateVerifier();
		ASiCService service = new ASiCService(certificateVerifier);

		ToBeSigned dataToSign = service.getDataToSign(documentToSign, signatureParameters);
		SignatureValue signatureValue = TestUtils.sign(SignatureAlgorithm.RSA_SHA256, privateKeyEntry, dataToSign);
		DSSDocument signedDocument = service.signDocument(documentToSign, signatureParameters, signatureValue);

		privateKeyEntry = certificateService.generateCertificateChain(SignatureAlgorithm.RSA_SHA256);
		signatureParameters.bLevel().setSigningDate(new Date());
		signatureParameters.setSigningCertificate(privateKeyEntry.getCertificate());
		signatureParameters.setCertificateChain(privateKeyEntry.getCertificateChain());
		signatureParameters.setSignaturePackaging(SignaturePackaging.ENVELOPING);
		signatureParameters.setSignatureLevel(SignatureLevel.ASiC_E_BASELINE_B);
		signatureParameters.aSiC().setUnderlyingForm(SignatureForm.XAdES);

		certificateVerifier = new CommonCertificateVerifier();
		service = new ASiCService(certificateVerifier);

		dataToSign = service.getDataToSign(signedDocument, signatureParameters);
		signatureValue = TestUtils.sign(SignatureAlgorithm.RSA_SHA256, privateKeyEntry, dataToSign);
		DSSDocument resignedDocument = service.signDocument(signedDocument, signatureParameters, signatureValue);

		SignedDocumentValidator validator = SignedDocumentValidator.fromDocument(resignedDocument);
		validator.setCertificateVerifier(new CommonCertificateVerifier());

		Reports reports = validator.validateDocument();

		// reports.print();
		DiagnosticData diagnosticData = reports.getDiagnosticData();

		for (String id : diagnosticData.getSignatureIdList()) {
			assertTrue(diagnosticData.isBLevelTechnicallyValid(id));
		}
	}
}
