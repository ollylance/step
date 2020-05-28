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

/**
 * Adds a random greeting to the page.
 */

function resizeOverlay(){
    const background = document.getElementById("background-overlay");
    const img = document.getElementById("enlarged-img"); 
    img.style.maxHeight = (window.innerHeight * .75) + "px";
    img.style.top = window.pageYOffset + "px";
    img.style.maxWidth =  "60%";
    background.style.width = "100%";
    background.style.height = window.innerHeight + "px";
    background.style.top = window.pageYOffset + "px";
    background.style.left = 0;
}

function openImg(imgURL){
    const background = document.getElementById("background-overlay");
    background.style.backgroundColor = "rgba(71, 71, 71, .8)";
    background.style.width = "100%";
    background.style.height = window.innerHeight + "px";
    background.style.top = window.pageYOffset + "px";
    background.style.left = 0;
    background.style.zIndex = 2;

    const img = document.getElementById("enlarged-img"); 
    img.src = imgURL;
    img.alt = "enlarged gallery image";
    img.style.width = "auto";
    img.style.height = "auto";
    img.style.maxWidth =  "60%";
    img.style.maxHeight = (window.innerHeight * .75) + "px";
    img.style.top = window.pageYOffset + "px";
    img.style.zIndex = 3;

    var body = document.getElementsByTagName("body");
    body[0].style.overflow = "hidden";

    window.addEventListener('resize', resizeOverlay);

    background.addEventListener('click', function(){
        background.style.width =  0;
        background.style.height =  0;
        background.style.zIndex = -1;
        img.style.width = 0;
        img.style.height = 0;
        var body = document.getElementsByTagName("body");
        body[0].style.overflow = "visible";
        window.removeEventListener('resize', resizeOverlay);
    });
}


function addRandomGreeting() {
  const greetings =
      ['Hello world!', '¡Hola Mundo!', '你好，世界！', 'Bonjour le monde!'];

  // Pick a random greeting.
  const greeting = greetings[Math.floor(Math.random() * greetings.length)];

  // Add it to the page.
  const greetingContainer = document.getElementById('greeting-container');
  greetingContainer.innerText = greeting;
}
