class Prescreen {
  PImage pi_saloon;
  PImage pi_dame;
  PImage pi_paste;
  boolean mouseProcessed;
  Textbox serverip;
  Textbox playername;
  Button start;

  Prescreen() {
    pi_saloon = loadImage("/assets/etc/saloon.png");
    pi_dame = loadImage("/assets/cards/karo/dame.jpg");
    pi_paste = loadImage("/assets/etc/paste.png");

    pi_saloon.resize(width, height);
    pi_dame.resize(width/15, 0);
    pi_paste.resize(24, 0);
  
    serverip = new Textbox(209*width/1000, 568*height/600, width/10, height/40);
    playername = new Textbox(552*width/1000, 568*height/600, width/10, height/40);
    
    serverip.content = lastip;
    playername.content = lastname;
    mouseProcessed = false;
    
    start = new Button(801*width/1000, 569*height/600, 58*width/1000, 20*height/600, "Connect", false);
  }

  void draw() {
    image(pi_saloon, 0, 0);
    image(pi_dame, width-pi_dame.width, height-pi_dame.height);
    image(pi_paste, width*311/1000, height*558/600);
    
    serverip.draw();
    playername.draw();
    start.draw();

    fill(0, 255, 0);
    textSize(15);
    text("Server-IP:", 68*width/1000, 566*height/600);
    text("Your Name:", 405*width/1000, 566*height/600);
    text("Credits", 967*width/1000, 493*height/600);
    textSize(11);
    
    text("v."+CLIENT_VERSION, 20, 5);
  }

  void checkclick() {
    if (mousePressed && !mouseProcessed) {
      mouseProcessed = true;
      if(mouseX>(width*311/1000) && mouseX<(width*311/1000)+24 && mouseY>(height*558/600) && mouseY < (height*558/600)+24) {
       serverip.content += GClip.paste();
      } else if (mouseX > (width-prescreen.pi_dame.width) && mouseX < width && mouseY > height- prescreen.pi_dame.height && mouseY < height) {
        game.stage = 5;
      } else if (serverip.checkclick()) {
        serverip.active=true; 
        playername.active=false;
      } else if (playername.checkclick()) {
        playername.active=true; 
        serverip.active=false;
      }
    }
    else if(!mousePressed) mouseProcessed = false;
  }
}

void pregame() {
  prescreen.checkclick();
  prescreen.draw();
  
  if(prescreen.playername.content.length()>0 && prescreen.serverip.content.length()>0) prescreen.start.state = true;
  else prescreen.start.state = false;
  
  if(mousePressed && prescreen.start.checkclick()) {
    connect();
  }
}

void connect() {
     if(prescreen.serverip.content.equals("x")) {prescreen.serverip.content="127.0.0.1";}
     if(prescreen.playername.content.equals("x")) {prescreen.playername.content="Player"+hour()+minute()+second();}
     client = new Client(this, prescreen.serverip.content, 6878);
     game.stage++;
     println(prescreen.playername.content);
     lastname = prescreen.playername.content;
     lastip = prescreen.serverip.content;
}

void keyPressed() {
  if(key=='+') audio.muteAmbient();
  else if(key=='#') audio.muteSound();
  else if(key=='-') audio.play("sound", "cena", true, false);
  
  if (game.stage==-1) {;
    
    if(key==TAB) {
      if(prescreen.playername.active) {prescreen.playername.active = false; prescreen.serverip.active=true;}
      else if(prescreen.serverip.active) {prescreen.serverip.active = false; prescreen.playername.active=true;}
      else prescreen.serverip.active = true;
    }
    
    if(key==RETURN || key==ENTER) {
      if(prescreen.start.state) connect();
    }
    
    if(key==TAB || key==ENTER || key==RETURN || key==ESC || key==DELETE || key==SHIFT || key==ALT || key==CODED) return;
    
    Textbox target;
    
    if (prescreen.serverip.active) {
      target = prescreen.serverip;
    } else if (prescreen.playername.active) {
      target = prescreen.playername;
    } else return;

    if (key==BACKSPACE && target != null) {
      if (target.content.length() > 0)
        target.content = target.content.substring(0, target.content.length()-1);
    } else if (target!=null) target.content += key;
  }
}