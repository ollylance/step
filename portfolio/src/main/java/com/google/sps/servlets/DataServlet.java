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
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import com.google.sps.data.Comment;
import com.google.sps.data.VerifyRecaptcha;
import com.google.sps.data.IdentityProvider;
import com.google.sps.data.PageInfo;
import com.google.gson.Gson;
import javax.servlet.RequestDispatcher;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;

/** Servlet that makes a new comment from form and puts it into Datastore.**/
@WebServlet("/data")
public class DataServlet extends HttpServlet {

    private int addPageNumber(int dir, int pageNumber) {
        if (dir > 0) {
            pageNumber += 1;
        } else if (dir < 0) {
            pageNumber -= 1;
        }
        return pageNumber;
    }

    private boolean lastPage(QueryResultList<Entity> results, String sort) {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        FetchOptions fetchOptions = FetchOptions.Builder.withLimit(1);
        Cursor startCursor = results.getCursor();
        fetchOptions.startCursor(startCursor);
        Query query = getQueryType(sort);
        PreparedQuery res = datastore.prepare(query);
        results = res.asQueryResultList(fetchOptions);
        if (results.isEmpty()) {
            return true;
        }
        return false;
    }

    private Query getQueryType(String sort) {
        Query query;
        switch(sort) {
            case "new": 
                        query = new Query("Comment").addSort("timestamp", SortDirection.DESCENDING).addSort(Entity.KEY_RESERVED_PROPERTY);
                        break;
            case "old": 
                        query = new Query("Comment").addSort("timestamp", SortDirection.ASCENDING).addSort(Entity.KEY_RESERVED_PROPERTY);
                        break;
            case "high":
                        query = new Query("Comment").addSort("stars", SortDirection.DESCENDING).addSort(Entity.KEY_RESERVED_PROPERTY);
                        break;
            case "low": 
                        query = new Query("Comment").addSort("stars", SortDirection.ASCENDING).addSort(Entity.KEY_RESERVED_PROPERTY);
                        break;
            default: 
                        query = new Query("Comment").addSort("timestamp", SortDirection.DESCENDING).addSort(Entity.KEY_RESERVED_PROPERTY);
        }
        return query;
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String name = request.getParameter("name");
        String comment = request.getParameter("comment-input");
        String stars = request.getParameter("stars");
        long timestamp = System.currentTimeMillis();
        String gRecaptchaResponse = request.getParameter("g-recaptcha-response");
        //checks if reCAPTCHA is successful
        boolean success = VerifyRecaptcha.verify(gRecaptchaResponse);
        //checks if profile is verified and then initializes the current profile id;
        String stringToken = request.getParameter("stringToken");
        IdentityProvider identity = new IdentityProvider(stringToken);
        if (!success) {
            return;
        }
        if (identity.getTokenVerified()) {
            Entity commentEntity = new Entity("Comment");
            commentEntity.setProperty("personId", identity.getPayload().getSubject());
            commentEntity.setProperty("name", name);
            commentEntity.setProperty("comment", comment);
            commentEntity.setProperty("stars", stars);
            commentEntity.setProperty("timestamp", timestamp);

            DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
            datastore.put(commentEntity);
            response.sendRedirect("/comments.html");
        } else {
            getServletContext().log("Token not verified");
        }
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        //some cursor knowledge based on/used from https://cloud.google.com/appengine/docs/standard/java/datastore/query-cursors
        int numComments = Integer.parseInt(request.getParameter("numComments"));
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        FetchOptions fetchOptions = FetchOptions.Builder.withLimit(numComments);
        String startCursor = request.getParameter("cursor");
        int pageNumber = Integer.parseInt(request.getParameter("pageNumber"));
        boolean reload = Boolean.parseBoolean(request.getParameter("reload"));
        int dir = Integer.parseInt(request.getParameter("dir"));
        String sort = request.getParameter("sort");
        boolean lastPage = false;

        //checks if profile is verified and then initializes the current profile id;
        String stringToken = request.getParameter("stringToken");
        IdentityProvider identity = new IdentityProvider(stringToken);

        if (startCursor != null) {
            fetchOptions.startCursor(Cursor.fromWebSafeString(startCursor));
        }
        if (reload) {
            pageNumber = 1;
        }

        pageNumber = addPageNumber(dir, pageNumber);

        Query query = getQueryType(sort);
        if (dir < 0) {
            query = query.reverse();   
        }

        PreparedQuery res = datastore.prepare(query);
        QueryResultList<Entity> results;
        try {
            results = res.asQueryResultList(fetchOptions);
            //reverses results if going back a page so results display forward
            if (dir < 0) {
                startCursor = results.getCursor().toWebSafeString();
                query = query.reverse();
                res = datastore.prepare(query);
                fetchOptions.startCursor(results.getCursor());
                results = res.asQueryResultList(fetchOptions);
            }
            //checks if this is the last page
            if (results.size() == numComments) {
                lastPage = lastPage(results, sort);
            } else if (results.size() < numComments) {
                lastPage = true;
            }
        } catch (IllegalArgumentException e) {
            // IllegalArgumentException happens when an invalid cursor is used.
            // A user could have manually entered a bad cursor in the URL or there
            // may have been an internal implementation detail change in App Engine.
            // Redirect to the page without the cursor parameter to show something
            // rather than an error.
            getServletContext().log(e, "invalid cursor is used");
            response.sendRedirect("/comments.html");
            return;
        }

        ArrayList<Comment> comments = new ArrayList<Comment>();
        for (Entity entity : results) {
            long commentId = entity.getKey().getId();
            String personId = (String)entity.getProperty("personId");
            boolean showTrash = false;
            if (identity.getTokenVerified() && personId.equals(identity.getPayload().getSubject())) {
                showTrash = true;
            }
            String name = (String)entity.getProperty("name");
            String comment = (String)entity.getProperty("comment");
            int stars = Integer.parseInt((String)entity.getProperty("stars"));
            long timestamp = (long) entity.getProperty("timestamp");
            Comment newComment = new Comment(commentId, name, comment, stars, timestamp, showTrash);
            comments.add(newComment);
        }

        String cursorString = results.getCursor().toWebSafeString();
        PageInfo data;
        //checks if on last page or first page
        if (pageNumber == 1 && lastPage) {
            data = new PageInfo(comments, null, null, pageNumber);
        }
        else if (pageNumber == 1) {
            data = new PageInfo(comments, null, "&cursor="+cursorString, pageNumber);
        } else if (lastPage) {
            data = new PageInfo(comments, "&cursor="+startCursor, null, pageNumber);
        } else {
            data = new PageInfo(comments, "&cursor="+startCursor, "&cursor="+cursorString, pageNumber);
        }
        response.setContentType("application/json;");
        response.getWriter().println(new Gson().toJson(data));
    }
}

