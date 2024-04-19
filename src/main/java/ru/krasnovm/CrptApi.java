package ru.krasnovm;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import okhttp3.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.*;

public class CrptApi {
    private final TimeUnit timeUnit;
    private final int requestLimit;
    private final Deque<Long> requests;
    //public static final StringBuilder BASE_URL = new StringBuilder("https://ismp.ru");
    public static final StringBuilder BASE_URL = new StringBuilder("https://localhost");

    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        this.timeUnit = timeUnit;
        this.requestLimit = Math.max(requestLimit, 0);
        requests = new LinkedList<>();
    }

    public int createDocumentForProduct(JsonDoc jsonDocument, String signature) {
        synchronized (this) {
            if (!requests.isEmpty()) {
                while (requests.peekFirst() - requests.peekLast() > timeUnit.toMillis(1)) {
                    requests.pollLast();
                }
            }

            if (requests.size() >= requestLimit) {
                //blocked
                return -1;
            }

            requests.offerFirst(System.currentTimeMillis());
        }

        if (savePostRequest(jsonDocument, signature) > 0) {
            return 1;
        }

        return 0; //request wasn't successful
    }

    private int savePostRequest(JsonDoc jsonDocument, String signature) {
        final MediaType JSON = MediaType.get("application/json; charset=utf-8");
        final String path = "/api/v3/lk/documents/create";

        //enable https
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .connectionSpecs(Arrays.asList(
                        ConnectionSpec.MODERN_TLS,
                        ConnectionSpec.COMPATIBLE_TLS))
                .build();

        //forming request
        RequestBody requestBody = RequestBody.create(
                createJsonString(jsonDocument),
                JSON
        );
        Request request = new Request.Builder()
                .url(BASE_URL.append(path).toString())
                .addHeader("Content-Type", "application/json")
                .post(requestBody)
                .build();

        //sending request
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                return -1;
            }
            if (response.body() != null) {
                System.out.println(response.body().string());
                return 1;
            }
        } catch (IOException e) {
            System.out.println("Ошибка подключения: " + e);
        }
        return -1;
    }

    private String createJsonString(JsonDoc jsonDoc) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode rootNode = mapper.createObjectNode();

        ObjectNode description = mapper.createObjectNode();
        description.put("participantInn", jsonDoc.getParticipantInn());
        rootNode.set("description", description);

        rootNode.put("doc_id", jsonDoc.getDocId());
        rootNode.put("doc_status", jsonDoc.getDocStatus());
        rootNode.put("doc_type", jsonDoc.getDocType());
        rootNode.put("importRequest", jsonDoc.getImportRequest());
        rootNode.put("owner_inn", jsonDoc.getOwnerInn());
        rootNode.put("participant_inn", jsonDoc.getParticipantInn());
        rootNode.put("producer_inn", jsonDoc.getProducerInn());
        rootNode.put("production_date", jsonDoc.getProductionDate().toString());
        rootNode.put("production_type", jsonDoc.getProductionType());

        ObjectNode products = mapper.createObjectNode();
        products.put("certificate_document", jsonDoc.getCertificateDocument());
        products.put("certificate_document_date", jsonDoc.getCertificateDocumentDate().toString());
        products.put("certificate_document_number", jsonDoc.getCertificateDocumentNumber());
        products.put("owner_inn", jsonDoc.getOwnerInn());
        products.put("producer_inn", jsonDoc.getProducerInn());
        products.put("production_date", jsonDoc.getProductionDate().toString());
        products.put("tnved_code", jsonDoc.getTnvedCode());
        products.put("uit_code", jsonDoc.getUitCode());
        products.put("uitu_code", jsonDoc.getUituCode());
        rootNode.set("products", products);

        rootNode.put("reg_date", jsonDoc.getRegDate().toString());
        rootNode.put("reg_number", jsonDoc.getRegNumber());

        return rootNode.toPrettyString();
    }

    public static class JsonDoc {
        private final String participantInn;
        private final String docId;
        private final String docStatus;
        private final String docType;
        private final Boolean importRequest;
        private final String ownerInn;
        private final String producerInn;
        private final Date productionDate;
        private final String productionType;
        private final String certificateDocument;
        private final Date certificateDocumentDate;
        private final String certificateDocumentNumber;
        private final String tnvedCode;
        private final String uitCode;
        private final String uituCode;
        private final Date regDate;
        private final String regNumber;

        public JsonDoc(String participantInn, String docId, String docStatus, String docType, Boolean importRequest,
                       String ownerInn, String producerInn, Date productionDate, String productionType,
                       String certificateDocument, Date certificateDocumentDate, String certificateDocumentNumber,
                       String tnvedCode, String uitCode, String uituCode, Date regDate, String regNumber) {
            this.participantInn = participantInn;
            this.docId = docId;
            this.docStatus = docStatus;
            this.docType = docType;
            this.importRequest = importRequest;
            this.ownerInn = ownerInn;
            this.producerInn = producerInn;
            this.productionDate = productionDate;
            this.productionType = productionType;
            this.certificateDocument = certificateDocument;
            this.certificateDocumentDate = certificateDocumentDate;
            this.certificateDocumentNumber = certificateDocumentNumber;
            this.tnvedCode = tnvedCode;
            this.uitCode = uitCode;
            this.uituCode = uituCode;
            this.regDate = regDate;
            this.regNumber = regNumber;
        }

        public String getParticipantInn() {
            return participantInn;
        }

        public String getDocId() {
            return docId;
        }

        public String getDocStatus() {
            return docStatus;
        }

        public String getDocType() {
            return docType;
        }

        public Boolean getImportRequest() {
            return importRequest;
        }

        public String getOwnerInn() {
            return ownerInn;
        }

        public String getProducerInn() {
            return producerInn;
        }

        public Date getProductionDate() {
            return productionDate;
        }

        public String getProductionType() {
            return productionType;
        }

        public String getCertificateDocument() {
            return certificateDocument;
        }

        public Date getCertificateDocumentDate() {
            return certificateDocumentDate;
        }

        public String getCertificateDocumentNumber() {
            return certificateDocumentNumber;
        }

        public String getTnvedCode() {
            return tnvedCode;
        }

        public String getUitCode() {
            return uitCode;
        }

        public String getUituCode() {
            return uituCode;
        }

        public Date getRegDate() {
            return regDate;
        }

        public String getRegNumber() {
            return regNumber;
        }
    }

    //testing
    public static void main(String[] args) {
        final String jsonDoc = getJsonDocString();

        CrptApi api = new CrptApi(TimeUnit.SECONDS, 2);

        ExecutorService executorService = Executors.newFixedThreadPool(10);
        Runnable runnable = () -> {
            int res = api.createDocumentForProduct(jsonDoc, "signed");

            switch (res) {
                case 1 -> System.out.println("success");
                case 0 -> System.out.println("request was failed");
                case -1 -> System.out.println("too many requests");
            }
        };
        executorService.submit(runnable);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        executorService.submit(runnable);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        executorService.submit(runnable);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        executorService.shutdown();
    }

    private static String getJsonDocString() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode rootNode = mapper.createObjectNode();

        ObjectNode description = mapper.createObjectNode();
        description.put("participantInn", "string");
        rootNode.set("description", description);

        rootNode.put("doc_id", "string");
        rootNode.put("doc_status", "string");
        rootNode.put("doc_type", "LP_INTRODUCE_GOODS");
        rootNode.put("importRequest", true);
        rootNode.put("owner_inn", "string");
        rootNode.put("participant_inn", "string");
        rootNode.put("producer_inn", "string");
        rootNode.put("production_date", "2020");
        rootNode.put("production_type", "string");

        ObjectNode products = mapper.createObjectNode();
        products.put("certificate_document", "string");
        products.put("certificate_document_date", "2020-01-23");
        products.put("certificate_document_number", "string");
        products.put("owner_inn", "string");
        products.put("producer_inn", "string");
        products.put("production_date", "2020-01-23");
        products.put("tnved_code", "string");
        products.put("uit_code", "string");
        products.put("uitu_code", "string");
        rootNode.set("products", products);

        rootNode.put("reg_date", "2020-01-23");
        rootNode.put("reg_number", "string");

        return rootNode.toPrettyString();
    }
}
