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

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;

import com.google.gson.*;
import javax.net.ssl.HttpsURLConnection;

public final class VerifyRecaptcha {
    public static boolean verify(String gRecaptchaResponse) throws IOException {
        try {
            //calls POST request to reCAPTCHA verify site
            URL url = new URL("https://www.google.com/recaptcha/api/siteverify");
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

            connection.setDoOutput(true);
            DataOutputStream outStream = new DataOutputStream(connection.getOutputStream());
            outStream.writeBytes("secret=6Le34qMZAAAAAB3J7ZRmJMeqdDINivuCdK-S7a6x"+"&response=" + gRecaptchaResponse);

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
            getServletContext().log("reCAPTCHA error:" + e);
			return false;
		}
    }
}