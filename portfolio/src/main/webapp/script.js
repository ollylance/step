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


function resizeOverlay(){
    const background = document.getElementById("background-clicked");
    const img = document.getElementById("enlarged-clicked");
    img.style.maxHeight = (window.innerHeight * .75) + "px";
    img.style.top = window.pageYOffset + "px";
    background.style.height = window.innerHeight + "px";
    background.style.top = window.pageYOffset + "px";
}

//sets attributes for image and background overlay to display
//when image is clicked; then monitors for next event
function openImg(elem){
    imgURL = elem.src;
    const background = document.getElementById("background-overlay");
    background.removeAttribute("id");
    background.setAttribute("id", "background-clicked");
    background.style.height = window.innerHeight + "px";
    background.style.top = window.pageYOffset + "px";

    const img = document.getElementById("enlarged-img"); 
    img.src = imgURL;
    img.alt = "enlarged gallery image";
    img.removeAttribute("id");
    img.setAttribute("id", "enlarged-clicked");
    img.style.maxHeight = (window.innerHeight * .75) + "px";
    img.style.top = window.pageYOffset + "px";
    
    // locks the screen so user can not scroll when image is open
    var body = document.getElementsByTagName("body");
    body[0].style.overflow = "hidden";

    //when window is resized, resizes the background overlay and image
    window.addEventListener('resize', resizeOverlay);

    //detects when the background is clicked next to close 
    //the image and background overlay
    background.addEventListener('click', function(){
        background.removeAttribute("id");
        img.removeAttribute("id");
        background.setAttribute("id", "background-overlay");
        img.setAttribute("id", "enlarged-img");
    
        var body = document.getElementsByTagName("body");
        body[0].style.overflow = "visible";
        window.removeEventListener('resize', resizeOverlay);
    });
}


function addRandomQuote() {
  const quotes =
      ['“Be the change that you wish to see in the world." - Mahatma Gandhi',
      '“The best way to predict the future is to invent it.” – Alan Kay', 
      '“How wonderful it is that nobody need wait a single moment before starting to improve the world.” – Anne Frank', 
      '“Imagination is everything. It is the preview of life’s coming attractions.” – Albert Einstein',
      '“Darkness cannot drive out darkness; only light can do that. Hate cannot drive out hate; only love can do that.” – Martin Luther King, Jr.',
      '“Don’t walk in front of me, I may not follow. Don’t walk behind me, I may not lead. Walk beside me and be my friend.” – Albert Camus',
      '“The only true wisdom is knowing that you know nothing.” – Socrates'];

  const quote = quotes[Math.floor(Math.random() * quotes.length)];
  const quoteContainer = document.getElementById('quote-container');
  quoteContainer.innerText = quote;
}

function addElem(container, type, content){
    const newElem = document.createElement(type);
    newElem.innerText = content;
    container.appendChild(newElem);
}

function createNewComment(commentData){
    var name = commentData.name;
    var stars = commentData.stars;
    var comment = commentData.comment;
    
    const commentContainer = document.createElement('div');
    commentContainer.classList.add("comment-container");
    addElem(commentContainer, "h3", name);
    addElem(commentContainer, "h4", stars+"/5");
    addElem(commentContainer, "p", comment);
    return commentContainer;
}

function setClasses(pages, tablePage, notNull){
    if(notNull){
        if(pages[0].classList.contains("page-link")) pages[0].classList.remove("page-link");
        if(pages[1].classList.contains("page-link")) pages[1].classList.remove("page-link");
        if(!pages[0].classList.contains("no-page")) pages[0].classList.add("no-page");
        if(!pages[1].classList.contains("no-page")) pages[1].classList.add("no-page");
        if(!tablePage.classList.contains("no-page-table")) tablePage.classList.add("no-page-table");
    } else{
        if(!pages[0].classList.contains("page-link")) pages[0].classList.add("page-link");
        if(!pages[1].classList.contains("page-link")) pages[1].classList.add("page-link");
        if(pages[0].classList.contains("no-page")) pages[0].classList.remove("no-page");
        if(pages[1].classList.contains("no-page")) pages[1].classList.remove("no-page");
        if(tablePage.classList.contains("no-page-table")) tablePage.classList.remove("no-page-table");
    }
}

//sets links and changes css with classes
function loadPageNavigation(links, pageNumber){
    var prevLink = 'javascript:getComments("' + links[0] +'", "-1", "false")';
    var nextLink = 'javascript:getComments("' + links[1] +'", "1", "false")';
    var prevPages = document.getElementsByClassName('prev-page');
    var nextPages = document.getElementsByClassName('next-page');
    const prevPageTable = document.getElementById('prev-page-div');
    const currPageTable = document.getElementById('curr-page-div');
    const nextPageTable = document.getElementById('next-page-div');
    if(links[0] != null){
        prevPages[0].href = prevLink;
        prevPages[1].href = prevLink;
        prevPageTable.innerText = pageNumber-1;
        setClasses(prevPages, prevPageTable, false);
    } else{
        prevPages[0].href = "";
        prevPages[1].href = "";
        prevPageTable.innerText = "";
        setClasses(prevPages, prevPageTable, true);
    }
    
    currPageTable.innerText = pageNumber;

    if(links[1] != null){
        nextPages[0].href = nextLink;
        nextPages[1].href = nextLink;
        nextPageTable.innerText = pageNumber+1;
        setClasses(nextPages, nextPageTable, false);
    } else{
        nextPages[0].href = "";
        nextPages[1].href = "";
        nextPageTable.innerText = "";
        setClasses(nextPages, nextPageTable, true);
    }
}

function loadHTML(commentData){
    const responses = document.getElementById('comment-content');
    responses.innerHTML = "";
    var comments = commentData.comments;
    for(i = 0; i < comments.length; i++){
        responses.appendChild(createNewComment(comments[i]));
    }
    loadPageNavigation(commentData.links, commentData.pageNumber);
}


//gets comments data and then adds two links to 
//link to the previous page and next page  
async function getComments(cursor, dir, reload){
    var sort = document.getElementById("sort").value;
    var numComments = document.getElementById("numComments").value;
    var auth2 = gapi.auth2.getAuthInstance();
    var id = auth2.currentUser.get().getId();
    const resp = await fetch('/data?id='+id+'&reload='+reload+'&numComments='+numComments+'&sort='+sort+'&dir='+dir+cursor);
    var commentData = await resp.json();
    loadHTML(commentData);
}

//appends the user id to the comment
async function addComment(){
    var auth2 = gapi.auth2.getAuthInstance();
    var id = auth2.currentUser.get().getId();
    if(id == null){
        alert("You need to be signed in to submit a comment");
        return;
    }
    var form = document.getElementById("add-comment");
    var formData = new FormData(form);
    formData.append("id", id);
    const params = new URLSearchParams();
    for(const pair of formData){
        params.append(pair[0], pair[1]);
    }
    const res = await fetch('/data?'+params.toString(), {method:'POST'});
}

async function onSignIn(googleUser) {
    // The ID token you need to pass to your backend:
    gapi.load('auth2', function() {
        var auth2 = gapi.auth2.init();
    });
    var id_token = googleUser.getAuthResponse().id_token;
    const res = await fetch('/tokensignin?token_id='+id_token, {method:'POST'});
}

function signOut() {
    var auth2 = gapi.auth2.getAuthInstance();
    auth2.signOut().then(function () {
      console.log('User signed out.');
    });
}