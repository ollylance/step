// Copyright 2020 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.data;

import com.google.cloud.secretmanager.v1.AccessSecretVersionResponse;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretVersionName;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;

import com.google.gson.*;
import javax.net.ssl.HttpsURLConnection;

public final class VerifyRecaptcha {

    private static String API_SECRET = "put your secret here";

    public static boolean verify(String gRecaptchaResponse) throws IOException {
        try {
            //calls POST request to reCAPTCHA verify site
            accessSecret();
            URL url = new URL("https://www.google.com/recaptcha/api/siteverify");
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

            connection.setDoOutput(true);
            DataOutputStream outStream = new DataOutputStream(connection.getOutputStream());
            outStream.writeBytes("secret="+API_SECRET+"&response=" + gRecaptchaResponse);

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            StringBuffer response = new StringBuffer();

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
            
            JsonObject jsonObject = new JsonParser().parse(response.toString()).getAsJsonObject();
            return jsonObject.get("success").getAsBoolean();
		} catch (Exception e) {
			return false;
		}
    }

    public static void accessSecret() throws IOException {
        // TODO(developer): Replace these variables before running the sample.
        String projectId = "ollylance-step-2020";
        String secretId = "RECAPTCHA_API_KEY";
        String versionId = "latest";
        accessSecretVersion(projectId, secretId, versionId);
    }

    // Access the payload for the given secret version if one exists. The version
    // can be a version number as a string (e.g. "5") or an alias (e.g. "latest").
    public static void accessSecretVersion(String projectId, String secretId, String versionId)
        throws IOException {
        System.out.println("hi1");
        // Initialize client that will be used to send requests. This client only needs to be created
        // once, and can be reused for multiple requests. After completing all of your requests, call
        // the "close" method on the client to safely clean up any remaining background resources.
        try (SecretManagerServiceClient client = SecretManagerServiceClient.create()) {
            System.out.println("hi2");
            SecretVersionName secretVersionName = SecretVersionName.of(projectId, secretId, versionId);
            System.out.println("hi3");
            // Access the secret version.
            AccessSecretVersionResponse response = client.accessSecretVersion(secretVersionName);

            // Print the secret payload.
            //
            // WARNING: Do not print the secret in a production environment - this
            // snippet is showing how to access the secret material.
            String payload = response.getPayload().getData().toStringUtf8();
            System.out.printf("Plaintext: %s\n", payload);
        }
    }
}