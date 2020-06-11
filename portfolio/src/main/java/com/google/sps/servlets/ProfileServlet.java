// Copyright 2019 Google LLC
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

package com.google.sps.servlets;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.QueryResultList;
import com.google.appengine.api.datastore.Key;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.extensions.appengine.http.UrlFetchTransport;
import com.google.api.client.json.gson.GsonFactory;

/** Servlet that makes a validated id token and stores data in database.**/
// referenced from https://developers.google.com/identity/sign-in/web/backend-auth
@WebServlet("/profile")
public class ProfileServlet extends HttpServlet {

    //returns a profile with a specific name in a property
    private Entity getProfile(String profileId) {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Query query = new Query("Profile");
        FetchOptions fetchOptions = FetchOptions.Builder.withLimit(1000000);
        PreparedQuery res = datastore.prepare(query);
        QueryResultList<Entity> results;
        try {
            results = res.asQueryResultList(fetchOptions);
        } catch (IllegalArgumentException e) {
            return null;
        }
        for (Entity entity : results) {
            if (entity.getProperty("id").equals(profileId)) return entity;
        }
        return null;
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        UrlFetchTransport transport = new UrlFetchTransport();
        GsonFactory gson = new GsonFactory();
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(transport, gson)
            .setAudience(Collections.singletonList("653342157222-tprfu5283rhi6m8gasi33pteu3su0cle.apps.googleusercontent.com"))
            .build();

        String tokenString = request.getParameter("token_id");
        GoogleIdToken idToken = GoogleIdToken.parse(gson, tokenString);
        try {
            if (verifier.verify(idToken)) {
                Payload payload = idToken.getPayload();
                //if the specific id is not already in the database
                if (getProfile(payload.getSubject()) == null) {
                    Entity profileEntity = new Entity("Profile");
                    profileEntity.setProperty("id", payload.getSubject());
                    profileEntity.setProperty("fname", (String) payload.get("given_name"));
                    profileEntity.setProperty("lname", (String) payload.get("family_name"));
                    profileEntity.setProperty("email", (String) payload.getEmail());
                    profileEntity.setProperty("emailVerfied", Boolean.valueOf(payload.getEmailVerified()));
                    profileEntity.setProperty("picUrl", (String) payload.get("picture"));

                    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
                    datastore.put(profileEntity);
                }
            }
        } catch (Exception e) {
            System.err.println("Token not verified:" + e);
        }
        response.sendRedirect("/comments.html");
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        UrlFetchTransport transport = new UrlFetchTransport();
        GsonFactory gson = new GsonFactory();
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(transport, gson)
            .setAudience(Collections.singletonList("653342157222-tprfu5283rhi6m8gasi33pteu3su0cle.apps.googleusercontent.com"))
            .build();

        String currentProfile = null;
        String currentProfileToken = request.getParameter("currentProfileToken");
        if (!currentProfileToken.equals("")) {
            GoogleIdToken idToken = GoogleIdToken.parse(gson, currentProfileToken);
            try {
                if (verifier.verify(idToken)) {
                    Payload payload = idToken.getPayload();
                    //if the specific id is not already in the database
                    Entity profile = getProfile(payload.getSubject());
                    if (profile != null) {
                        ArrayList<String> profileData = new ArrayList<String>();
                        String picUrl = (String) profile.getProperty("picUrl");
                        profileData.add(picUrl);
                        String fname = (String) profile.getProperty("fname");
                        profileData.add(fname);
                        String lname = (String) profile.getProperty("lname");
                        profileData.add(lname);

                        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
                        Gson gsonHelper = new Gson();
                        response.setContentType("application/json;");
                        response.getWriter().println(gsonHelper.toJson(profileData));
                        return;
                    }
                    response.sendRedirect("/comments.html");
                }
            } catch (Exception e) {
                System.err.println("Token not verified:" + e);
                response.sendRedirect("/comments.html");
            }
        }
    }
}

