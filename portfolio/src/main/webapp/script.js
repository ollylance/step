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


function addRandomQuotes() {
  const quotes =
      ['Hello world!', '¡Hola Mundo!', '你好，世界！', 'Bonjour le monde!'];

  const quote = quotes[Math.floor(Math.random() * greetings.length)];

  const quoteContainer = document.getElementById('quote-container');
  quoteContainer.innerText = quote;
}
