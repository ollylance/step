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

package com.google.sps.servlets;

import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.QueryResultList;
import com.google.appengine.api.datastore.Key;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import com.google.sps.data.Comment;
import com.google.sps.data.PageInfo;
import com.google.sps.data.IdentityProvider;
import com.google.gson.Gson;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;

/** Servlet that makes a new comment from form and puts it into Datastore.**/
@WebServlet("/delete-comment")
public class DeleteCommentServlet extends HttpServlet {

    private Entity getEntry(String property, String entryKey){
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Query query = new Query("Comment");
        FetchOptions fetchOptions = FetchOptions.Builder.withLimit(1000000);
        PreparedQuery res = datastore.prepare(query);
        QueryResultList<Entity> results;
        try {
            results = res.asQueryResultList(fetchOptions);
        } catch (IllegalArgumentException e) {
            getServletContext().log(e, "Unable to retrieve query when deleting entry");
            return null;
        }
        for (Entity entity : results) {
            String id = Long.toString(entity.getKey().getId());
            if (id.equals(entryKey)) return entity;
        }
        return null;
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String commentId = request.getParameter("commentId");
        //checks if profile is verified and then initializes the current profile id;
        String stringToken = request.getParameter("stringToken");
        IdentityProvider identity = new IdentityProvider(stringToken);
        
        Entity comment = getEntry("id", commentId);
        if (identity.getTokenVerified() && comment.getProperty("personId").equals(identity.getPayload().getSubject())) {
            DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
            datastore.delete(comment.getKey());
        }
        response.sendRedirect("/comments.html");
    }
}


