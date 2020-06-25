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

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.extensions.appengine.http.UrlFetchTransport;
import com.google.api.client.json.gson.GsonFactory;
import java.io.IOException;
import java.util.Collections;

public final class IdentityProvider {

    private final String stringToken;
    private GoogleIdToken idToken;
    private boolean tokenVerified;
    private Payload payload;

    public IdentityProvider(String stringToken) {
        this.stringToken = stringToken;
        verifyToken();
        setPayload();
    }

    private void verifyToken(){
        UrlFetchTransport transport = new UrlFetchTransport();
        GsonFactory gson = new GsonFactory();
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(transport, gson)
            .setAudience(Collections.singletonList("653342157222-tprfu5283rhi6m8gasi33pteu3su0cle.apps.googleusercontent.com"))
            .build();
        if (this.stringToken == null || this.stringToken.isEmpty()) {
            this.tokenVerified = false;
            return;
        }
        try {
            this.idToken = GoogleIdToken.parse(gson, this.stringToken);
            if (verifier.verify(this.idToken)) {
                this.tokenVerified = true;
            }
        } catch (Exception e) {
            this.tokenVerified = false;
        }

    }

    private void setPayload(){
        if (this.tokenVerified == true) {
            this.payload = this.idToken.getPayload();
        }
    }

    public boolean getTokenVerified(){
        return this.tokenVerified;
    }

    public Payload getPayload(){
        return this.payload;
    }
}