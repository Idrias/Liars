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
    if (mouseX > xpos-radx && mouseX < xpos+radx) {
      if (mouseY > ypos - rady && mouseY < ypos+rady) {
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
    text(text, xpos+25, ypos+rady/2+5);
  }
}