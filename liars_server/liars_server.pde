static String SERVER_VERSION = "0.9.3";

// TODO IMPLEMENT "DONE" as player var!
// Server crasht wenn nachfolgende ID nicht mehr da ist |FIXED!
// First player skip: nachfolgender kann nicht als auslegen |Workaround
// recommendation: sort cards  

void setup() {
  server = new Server(this, 6878);
  size(800, 400);
  setup_vars();
}


void draw() {
  for(String name : toDisconnect) {
    for(int i=0; i<players.size(); i++) {
      if(name.equals(players.get(i).id)) {server.disconnect(players.get(i).client); toDisconnect=new ArrayList<String>();}
    }
  }
  checkMouse();
  drawinfo();
  if        (STATE == 0)   waitstate();
  else if   (STATE == 1)   play();
}


void checkMouse() {
  if(mousePressed && !gotMousePress) {
    gotMousePress = true;
    
    
     boolean kickflag = false;
     boolean maketurnflag = false;
     
     if(kick.checkclick()) {kickflag=true;}
     if(giveturn.checkclick()) {maketurnflag=true;}
     
     for (Player player : players) {
       println("checking", player.alias);
       if(maketurnflag && player.entryselected) {
         player.theirturn=true;
         server.write("+eot<><>;");
         server.write("+msg<"+player.alias+" ist jetzt am Zug!><>;");
         server.write("+dsp<"+player.id+"><>;");
         println("ACHTUNG SERVER ADMIN HAT SPIEL MANUPULIERT");
       }
       else if(maketurnflag && !player.entryselected) {
         player.theirturn = false;
       }
       
       else if(kickflag && player.entryselected) {
         println("Disconnecting", player.alias);
         toDisconnect.add(player.id);
       }
     } 
     
     
    for(Player player : players) {        
      if(mouseX<540 && mouseY > player.entryY-10 && mouseY < player.entryY) {
        println(player.alias, "got clicked");
        player.entryselected = !player.entryselected;
        for(Player playeralt : players) {
          if(!playeralt.id.equals(player.id))
            playeralt.entryselected = false;
        }
      }
    }
    
     
    for(Player testplayer : players) {
      if(testplayer.entryselected) {
        kick.state = true;
        giveturn.state = true;
        return;
      }
      kick.state = false;
      giveturn.state = false;
    }
  }
  
  else if(!mousePressed) gotMousePress = false;
}


void keyReleased() {
  if(key=='f') {
    freddy = false;
    server.write("+fre<-><>;");
  }
}

void keyPressed() {
  if (key=='p') {
    STATE=1;
  }
  
  else if(key=='r') {
    STATE = 0;
    reset_vars();
  }
  
  else if(key=='f' && !freddy) {
    freddy = true;
    server.write("+fre<+><>;");
  }
}

void drawinfo() {
  background(0);
  stroke(255);
  line(545, 300, width, 300);
  line(545, 0, 545, height);
  
  giveturn.draw();
  kick.draw();
  fill(255);
  text("IP", 10, 20);
  text("ID", 100, 20);
  text("Alias", 190, 20);
  text("No. of cards", 280, 20);
  text("Done", 370, 20);
  text("Has turn", 450, 20);
  int yoffset = 24;
  if(yoffset*(players.size()+4)> 3*height/4) yoffset = height / (players.size()+4);
  
  for(Player player : players) {
    player.entryY = 20+yoffset;
    text(player.client.ip(), 10, 20+yoffset);
    text(player.id, 100, 20+yoffset);
    text(player.alias, 190, 20+yoffset);
    text(player.cards.size(), 280, 20+yoffset);
    text(player.done, 370, 20+yoffset);
    text(int(player.theirturn), 450, 20+yoffset);
    
    if(player.entryselected) {
      noFill();
      stroke(#3CD63D);
      rect(5, player.entryY-10, 535, 10);
    }
    
    yoffset+=14;
  }
}

void getplayerturn() {
  for(Player player : players) {
    if(player.theirturn) println("PLAYER", player.id, "HAS HIS TURN");
  }
}