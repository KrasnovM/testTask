package ru.krasnovm;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import okhttp3.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.*;

public class CrptApi {
    private final TimeUnit timeUnit;
    private final int requestLimit;
    private final BlockingDeque<Long> requests;
    //public static final StringBuilder BASE_URL = new StringBuilder("https://ismp.ru");
    public static final StringBuilder BASE_URL = new StringBuilder("https://localhost");

    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        this.timeUnit = timeUnit;
        this.requestLimit = Math.max(requestLimit, 0);
        requests = new LinkedBlockingDeque<>();
    }

    public int createDocumentForProduct(String jsonDocument, String signature) {
        synchronized (this) {

            if (!requests.isEmpty()) {
                while (requests.peekFirst() - requests.peekLast() > timeUnit.toMillis(1)) {
                    try {
                        requests.takeLast();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
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

    private int savePostRequest(String jsonDocument, String signature) {
        final MediaType JSON = MediaType.get("application/json; charset=utf-8");
        final String path = "/api/v3/lk/documents/create";

        //enable https
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .connectionSpecs(Arrays.asList(
                        ConnectionSpec.MODERN_TLS,
                        ConnectionSpec.COMPATIBLE_TLS))
                .build();

        //forming request
        RequestBody requestBody = RequestBody.create(jsonDocument, JSON);
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



}
