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


function resizeOverlay() {
    const background = document.getElementById("background-clicked");
    const img = document.getElementById("enlarged-clicked");
    img.style.maxHeight = (window.innerHeight * .75) + "px";
    img.style.top = window.pageYOffset + "px";
    background.style.height = window.innerHeight + "px";
    background.style.top = window.pageYOffset + "px";
}

//sets attributes for image and background overlay to display
//when image is clicked; then monitors for next event
function openImg(elem) {
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
    background.addEventListener('click', function() {
        background.removeAttribute("id");
        img.removeAttribute("id");
        img.src = "";
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

function addElem(container, type, content, id) {
    const newElem = document.createElement(type);
    newElem.innerText = content;
    if (type == "div") {
        newElem.id = "delete-comment";
        newElem.addEventListener("click", function() {
            deleteComment(id);
        });
    }
    container.appendChild(newElem);
}

function createNewComment(commentData) {
    var name = commentData.name;
    var stars = commentData.stars;
    var comment = commentData.comment;

    const commentContainer = document.createElement('div');
    commentContainer.classList.add("comment-container");
    addElem(commentContainer, "h3", name);
    addElem(commentContainer, "h4", stars+"/5");
    addElem(commentContainer, "p", comment);

    if (commentData.showTrash) {
        addElem(commentContainer, "div", "Delete", commentData.id);
    }
    return commentContainer;
}

function setClasses(pages, tablePage, notNull) {
    if (notNull) {
        if (pages[0].classList.contains("page-link")) pages[0].classList.remove("page-link");
        if (pages[1].classList.contains("page-link")) pages[1].classList.remove("page-link");
        if (!pages[0].classList.contains("no-page")) pages[0].classList.add("no-page");
        if (!pages[1].classList.contains("no-page")) pages[1].classList.add("no-page");
        if (!tablePage.classList.contains("no-page-table")) tablePage.classList.add("no-page-table");
    } else {
        if (!pages[0].classList.contains("page-link")) pages[0].classList.add("page-link");
        if (!pages[1].classList.contains("page-link")) pages[1].classList.add("page-link");
        if (pages[0].classList.contains("no-page")) pages[0].classList.remove("no-page");
        if (pages[1].classList.contains("no-page")) pages[1].classList.remove("no-page");
        if (tablePage.classList.contains("no-page-table")) tablePage.classList.remove("no-page-table");
    }
}

//sets links and changes css with classes
function loadPageNavigation(pLink, nLink, pageNumber) {
    var prevLink = 'javascript:getComments("' + pLink +'", "-1", "false", "'+ pageNumber +'")';
    var nextLink = 'javascript:getComments("' + nLink +'", "1", "false", "'+ pageNumber +'")';
    var prevPages = document.getElementsByClassName('prev-page');
    var nextPages = document.getElementsByClassName('next-page');
    const prevPageTable = document.getElementById('prev-page-div');
    const currPageTable = document.getElementById('curr-page-div');
    const nextPageTable = document.getElementById('next-page-div');
    if (pLink != null) {
        prevPages[0].href = prevLink;
        prevPages[1].href = prevLink;
        prevPageTable.innerText = pageNumber-1;
        setClasses(prevPages, prevPageTable, false);
    } else {
        prevPages[0].href = "";
        prevPages[1].href = "";
        prevPageTable.innerText = "";
        setClasses(prevPages, prevPageTable, true);
    }
    
    currPageTable.innerText = pageNumber;

    if (nLink != null) {
        nextPages[0].href = nextLink;
        nextPages[1].href = nextLink;
        nextPageTable.innerText = pageNumber+1;
        setClasses(nextPages, nextPageTable, false);
    } else {
        nextPages[0].href = "";
        nextPages[1].href = "";
        nextPageTable.innerText = "";
        setClasses(nextPages, nextPageTable, true);
    }
}

function loadHTML(commentData) {
    const responses = document.getElementById('comment-content');
    responses.innerHTML = "";
    var comments = commentData.comments;
    for (i = 0; i < comments.length; i++) {
        responses.appendChild(createNewComment(comments[i]));
    }
    loadPageNavigation(commentData.prevLink, commentData.nextLink, commentData.pageNumber);
}

//gets comments data and then adds two links to 
//link to the previous page and next page  
async function getComments(cursor, dir, reload, pageNum) {
    var sort = document.getElementById("sort").value;
    var numComments = document.getElementById("numComments").value;
    var auth2 = gapi.auth2.getAuthInstance();
    var currentProfileToken = auth2.currentUser.get().getAuthResponse().id_token;
    if (currentProfileToken == undefined) currentProfileToken = "";
    const resp = await fetch('/data?currentProfileToken='+currentProfileToken+'&reload='+reload+'&numComments='+numComments+'&sort='+sort+'&dir='+dir+'&pageNumber='+pageNum+cursor);
    var commentData = await resp.json();
    loadHTML(commentData);
}

//appends the user id to the comment
async function addComment() {
    var auth2 = gapi.auth2.getAuthInstance();
    var currentProfileToken = auth2.currentUser.get().getAuthResponse().id_token;
    if (currentProfileToken == undefined) {
        alert("You need to be signed in to submit a comment");
        return;
    }
    var form = document.getElementById("add-comment");
    var formData = new FormData(form);
    formData.append("personIdToken", currentProfileToken);
    const params = new URLSearchParams();
    for (const pair of formData) {
        params.append(pair[0], pair[1]);
    }
    const res = await fetch('/data?'+params.toString(), {method:'POST'});
}

async function deleteComment(commentId) {
    var auth2 = gapi.auth2.getAuthInstance();
    var currentProfileToken = auth2.currentUser.get().getAuthResponse().id_token;
    if(currentProfileToken == undefined) {
        alert("You need to be signed in to delete a comment");
        return;
    }
    const res = await fetch('/delete-comment?currentProfileToken='+currentProfileToken+'&commentId='+commentId, {method:'POST'});
    await getComments("", 0, true, "1");
}

function loadProfileHTML(profileData){
    const profileImg = document.getElementById('user-profile-img');
    const userName = document.getElementById('user-name');
    const google = document.getElementById('google-signin');
    const signout = document.getElementById('signout');
    profileImg.src = profileData[0];
    if (profileImg.classList.contains("hide")) profileImg.classList.remove("hide");
    userName.innerText = profileData[1] + ' ' + profileData[2];
    if (userName.classList.contains("hide")) userName.classList.remove("hide");
    if (!google.classList.contains("hide")) google.classList.add("hide");
    if (signout.classList.contains("hide")) signout.classList.remove("hide");
}

async function getProfileInfo(){
    var auth2 = gapi.auth2.getAuthInstance();
    var currentProfileToken = auth2.currentUser.get().getAuthResponse().id_token;
    if (currentProfileToken == undefined) currentProfileToken = "";
    const resp = await fetch('/profile?currentProfileToken='+currentProfileToken);
    var profileData = await resp.json();
    loadProfileHTML(profileData);
}

function hideProfileInfo(){
    const signout = document.getElementById('signout');
    const profileImg = document.getElementById('user-profile-img');
    const userName = document.getElementById('user-name');
    const google = document.getElementById('google-signin');
    profileImg.src = "";
    if (!profileImg.classList.contains("hide")) profileImg.classList.add("hide");
    userName.innerText = "";
    if (!userName.classList.contains("hide")) userName.classList.add("hide");
    if (google.classList.contains("hide")) google.classList.remove("hide");
    if (!signout.classList.contains("hide")) signout.classList.add("hide");
}

function onSignIn(googleUser) {
    // The ID token you need to pass to your backend:
    gapi.load('auth2', function() {
        var auth2 = gapi.auth2.init();
        auth2.currentUser.get().getId();
        var id_token = googleUser.getAuthResponse().id_token;
        const res = fetch('/profile?token_id='+id_token, {method:'POST'});
        getComments("", 0, false, "1");
        getProfileInfo();
        return;
    });
    return;
}

function signOut() {
    var auth2 = gapi.auth2.getAuthInstance();
    auth2.signOut().then(function () {
        getComments("", 0, true, "1");
        hideProfileInfo();
    });
}
