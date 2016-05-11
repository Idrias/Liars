class Environment {
  InputChecker iC;
  Game game;
  Network network;

  Environment() {
    iC = new InputChecker();
    game = new Game();
    network = new Network();
  }
}

///////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////
class Game {
  Server_GUI gui;
  ArrayList<Server_Card> all_cards;
  ArrayList<Player> players;
  ArrayList<Server_Card> sta_game;
  ArrayList<Server_Card> lastcards;
  Game() {
    gui = new Server_GUI();
  }

  void update() {
    statecheck();
    gui.draw();
  }

  void statecheck() {
    if (players.size() > 0 && STATE == 0) {
      start.state = true;
    } else start.state = false;
  }
}



///////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////
class InputChecker {
  boolean mouseCatched;

  InputChecker() {
    mouseCatched = false;
  }

  void checkInput() {
    checkMouse();
  }


  void checkMouse() {
    if (mousePressed && !mouseCatched) {
      mouseCatched = true;

      boolean kickflag = false;
      boolean maketurnflag = false;

      if (start.checkclick()) 
        STATE = 1;

      else if (reset.checkclick()) {
        STATE = 0; 
        reset_vars();
      } else if (kick.checkclick()) {
        kickflag=true;
      } else if (giveturn.checkclick()) {
        maketurnflag=true;
      }

      for (Player player : players) {
        println("checking", player.alias);
        if (maketurnflag && player.entryselected) {
          player.theirturn=true;
          server.write("+eot<><>;");
          server.write("+msg<"+player.alias+" ist jetzt am Zug!><>;");
          server.write("+dsp<"+player.id+"><>;");
          println("ACHTUNG SERVER ADMIN HAT SPIEL MANUPULIERT");
        } else if (maketurnflag && !player.entryselected) {
          player.theirturn = false;
        } else if (kickflag && player.entryselected) {
          println("Disconnecting", player.alias);
          toDisconnect.add(player.id);
        }
      } 


      for (Player player : players) {        
        if (mouseX<540 && mouseY > player.entryY-10 && mouseY < player.entryY) {
          println(player.alias, "got clicked");
          player.entryselected = !player.entryselected;
          for (Player playeralt : players) {
            if (!playeralt.id.equals(player.id))
              playeralt.entryselected = false;
          }
        }
      }


      for (Player testplayer : players) {
        if (testplayer.entryselected) {
          kick.state = true;
          giveturn.state = true;
          return;
        }
        kick.state = false;
        giveturn.state = false;
      }
    } else if (!mousePressed) mouseCatched = false;
  }

  void keyPressed() {
    if (key=='p') {
      STATE=1;
    } else if (key=='r') {
      STATE = 0;
      reset_vars();
    } else if (key=='f' && !freddy) {
      freddy = true;
      server.write("+fre<+><>;");
    } else if (key=='b') {
      server.write("+bom<><>;");
    }
  }

  void keyReleased() {
    if (key=='f') {
      freddy = false;
      server.write("+fre<-><>;");
    }
  }
}

///////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////
class Player {
  Client client;
  String id;
  int done;
  int entryY = 0;
  String alias = "none";  
  ArrayList<Server_Card> cards;
  boolean theirturn = false;
  boolean entryselected = false;

  Player(Client p_client, String p_id) {
    client = p_client;
    id = p_id;
    done = 0;
    cards = new ArrayList<Server_Card>();
  }
}


class Server_Card {
  String farbe;
  String id;
  boolean hidden;

  Server_Card(String p_farbe, String p_id, boolean p_hidden) {
    farbe = p_farbe;
    id = p_id;
    hidden = p_hidden;
  }
}



///////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////
class Button {
  float xpos, ypos, radx, rady;
  color col;
  String text;
  boolean state;
  color disabledcol;
  color activecol;

  Button(float p_xpos, float p_ypos, float p_radx, float p_rady, String p_text, boolean p_state) {
    xpos = p_xpos;
    ypos = p_ypos;
    radx = p_radx;
    rady = p_rady;
    text = p_text;
    state = p_state;

    disabledcol = color(#808393);
    activecol = color(#3CD63D);
    checkstate();
  }


  void checkstate() {
    if (state) col = activecol;
    else col = disabledcol;
  }


  boolean checkclick() {
    if (mouseX > xpos && mouseX < xpos+radx) {
      if (mouseY > ypos && mouseY < ypos+rady) {
        if (state) return true;
      }
    }
    return false;
  }


  void draw() {
    checkstate();
    fill(col);
    rect(xpos, ypos, radx, rady);
    fill(0);
    text(text, xpos+radx/2, ypos+rady/2);
  }
}