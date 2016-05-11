void waitstate() { //<>//
  gameStarted = false;
  if (frameCount % 30 == 0) server.write("+nsa<stage><2>;");
  recv();
}

void schedule() {

  if (continueschedule!=0 && millis() > continueschedule) {
    continueschedule=0;
    getplayerturn();
    server.write("+con<><>;");
    server.write("-gst<all><all>;");
    println("GAFHAF");
    for (Player player : players) {
      println(player.id, "check");
      if (player.theirturn) { //!!!!//
        if (lied) {
          println("THEY LIED");
          server.write("+msg<"+lastplayer.alias+" hat gelogen!><>;");
          server.write("+dsp<"+player.id+"><>;"); 
          println("NEXT TURN: " + player.id); 
          server.write("+msg< >< >;"); 
          server.write("+msg<"+player.alias+">< ist jetzt am Zug!>;");
          for (Server_Card card : sta_game) {
            lastplayer.cards.add(card); 
            server.write("+p"+lastplayer.id+"<"+card.farbe+"><"+card.id+">;");
          }
        } else {
          println("THEY DID NOT LIE");
          server.write("+msg<"+lastplayer.alias+" hat die Wahrheit gesagt!><>;");
          for (Server_Card card : sta_game) {
            player.cards.add(card); 
            server.write("+p"+player.id+"<"+card.farbe+"><"+card.id+">;");
          }
          player.theirturn=false;
          String  newPlayerS = "";
          int lastPlayer = int(player.id);
          int donecount = 0;
          do {
            int newPlayer = lastPlayer+1;
            if (newPlayer > players.size()) {
              newPlayer = 1;
            } 

            newPlayerS = ""+newPlayer;
            if (newPlayer<10) newPlayerS = "0" + newPlayerS;

            lastPlayer = newPlayer;
            donecount++;
            if (donecount>players.size()) {
              server.write("+msg<Alle Spieler sind fertig!><>;");
              break;
            }
            println("HALLOOOOO:", newPlayerS);
          } while (getById(newPlayerS).cards.size() == 0);

          getById(newPlayerS).theirturn = true;
          server.write("+msg< >< >;");
          server.write("+msg<"+getById(newPlayerS).alias+" ist jetzt am Zug!><>;");
          server.write("+dsp<"+newPlayerS+"><>;");
          // THEIR TURN!!!!! MAKE IIT
          println("NEXT TURN: " + newPlayerS);
          //server.write("+msg<"+null+"><>");
          //server.write("+msg<"+null+"><>");
        }
        break; ///!
      } else println("not their turn:", player.id);
    }


    println("resetting");
    lied = false;
    sta_game = new ArrayList<Server_Card>();
    lastplayer = null;
    lastcards = new ArrayList<Server_Card>();
  }
}


void play() {
  schedule();
  for (String msg : storedmsgs) 
    server.write(msg);
  storedmsgs = new ArrayList<String>();
  recv();

  if (frameCount%240 == 0 && players.size() > 1) {
    for (Player player : players) {
      int damencount = 0;
      for (Server_Card card : player.cards) {
        if (card.id.equals("dame")) damencount++;
      }
      if (damencount==4 && players.size()>0) {
        STATE = 0;
        reset_vars();
        server.write("+msg<Das Spiel wurde automatisch beendet:><>;");
        server.write("+msg<"+player.alias+">< hatte vier Damen!>;");
        server.write("+msg< ><>;");
        return;
      }
    }
  }

  if (!gameStarted) {
    gameStarted = true;
    server.write("+nsa<stage><3>;");
    give_cards();

    Player startplayer = getById(findCardOwner("herz", "7"));
    if (startplayer==null) startplayer = getById(findCardOwner("karo", "7"));
    if (startplayer==null) startplayer = getById(findCardOwner("kreuz", "7"));
    if (startplayer==null) startplayer = getById(findCardOwner("pik", "7"));
    if (startplayer==null) startplayer = players.get(int(random(players.size())));
    println(startplayer.id, "ist Startspieler");
    startplayer.theirturn=true;
    server.write("+dsp<"+startplayer.id+"><>;");
    server.write("+msg< ><>;");   
    server.write("+msg<---|||---><>;");
    server.write("+msg<Spielstart><>;");
    server.write("+msg<"+startplayer.alias+">< beginnt!>;");
    server.write("+msg< ><>;");
  }
}