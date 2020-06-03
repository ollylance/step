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
import com.google.sps.data.Comment;
import com.google.sps.data.Data;
import com.google.gson.Gson;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/** Servlet that makes a new comment from form and puts it into Datastore.**/
@WebServlet("/data")
public class DataServlet extends HttpServlet {

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String name = request.getParameter("name");
        String comment = request.getParameter("comment-input");
        String stars = request.getParameter("stars");
        long timestamp = System.currentTimeMillis();

        Entity commentEntity = new Entity("Comment");
        commentEntity.setProperty("name", name);
        commentEntity.setProperty("comment", comment);
        commentEntity.setProperty("stars", stars);
        commentEntity.setProperty("timestamp", timestamp);

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        datastore.put(commentEntity);
        response.sendRedirect("/comments.html");
  }

  @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        //some cursor knowledge based on/used from https://cloud.google.com/appengine/docs/standard/java/datastore/query-cursors
        int numComments = 4;
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        FetchOptions fetchOptions = FetchOptions.Builder.withLimit(numComments);
        String startCursor = request.getParameter("cursor");
        int dir = Integer.parseInt(request.getParameter("dir"));

        //*else if* checks to see if trying to go back on first page
        if(startCursor != null){
            fetchOptions.startCursor(Cursor.fromWebSafeString(startCursor));
        } else if(startCursor == null && dir == -1){
            response.sendRedirect("/comments.html");
            return;
        }
        Query query = new Query("Comment").addSort("stars", SortDirection.DESCENDING).addSort(Entity.KEY_RESERVED_PROPERTY);
        if(dir == -1){
            query = query.reverse();   
        }

        PreparedQuery res = datastore.prepare(query);
        QueryResultList<Entity> results;
        try {
            results = res.asQueryResultList(fetchOptions);
            //reverses results if going back a page so results display forward
            if(dir == -1){
                startCursor = results.getCursor().toWebSafeString();
                query = query.reverse();
                res = datastore.prepare(query);
                fetchOptions.startCursor(results.getCursor());
                results = res.asQueryResultList(fetchOptions);
            }
        } catch (IllegalArgumentException e) {
            // IllegalArgumentException happens when an invalid cursor is used.
            // A user could have manually entered a bad cursor in the URL or there
            // may have been an internal implementation detail change in App Engine.
            // Redirect to the page without the cursor parameter to show something
            // rather than an error.
            response.sendRedirect("/comments.html");
            return;
        }

        ArrayList<Comment> comments = new ArrayList<Comment>();
        for (Entity entity : results) {
            long id = entity.getKey().getId();
            String name = (String)entity.getProperty("name");
            String comment = (String)entity.getProperty("comment");
            int stars = Integer.parseInt((String)entity.getProperty("stars"));
            long timestamp = (long) entity.getProperty("timestamp");

            Comment newComment = new Comment(id, name, comment, stars, timestamp);
            comments.add(newComment);
        }

        String cursorString = results.getCursor().toWebSafeString();
        Data data = new Data(comments, "&cursor="+startCursor, "&cursor="+cursorString);
        Gson gson = new Gson();
        response.setContentType("application/json;");
        response.getWriter().println(gson.toJson(data));
  }
}
