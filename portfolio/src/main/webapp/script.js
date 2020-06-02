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

async function getComments(){
    const resp = await fetch('/data');
    var comments = await resp.json();
    const responses = document.getElementById('comment-content');
    for(i = 0; i < comments.length; i++){
        responses.innerHTML += comments[i] + "<br>"
    }
}
