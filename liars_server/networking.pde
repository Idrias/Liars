
void recv() {
  Client client;

  do {
    client = server.available();
    
    
    if (client != null) {
      String incoming = client.readString();
      ArrayList<String> msgs = new ArrayList<String>();
      
      String happen = "";

      for (int i=0; i < incoming.length(); i++) {
        if (incoming.charAt(i) == ';') {
          msgs.add(happen); 
          println(happen);
          happen="";
        } else happen += incoming.charAt(i);
      }
      if(happen.length()>0) {
        // appendix!! n
      }
      
      
      decode(msgs);
    }
  } while (client != null);
}



void decode(ArrayList<String> msgs) {
  for (String s : msgs) {
    println(s);
    if(s.length()>4) get_tags(s, s.substring(0, 4));
  }
}


void get_tags(String msg, String command) {
  String tag1 = "";
  String tag2 = "";
  int open1 = 0;
  int close1 = 0;

  for (int i=0; i<msg.length(); i++) {
    if (msg.charAt(i) == '<') open1 = i;
    else if (msg.charAt(i) == '>') close1 = i;

    if (open1 != 0 && close1 != 0) {
      tag1 = msg.substring(open1+1, close1);
      tag2 = msg.substring(close1+2, msg.length()-1);
      break;
    }
  }
  execute(command, tag1, tag2);
}


void execute(String command, String tag1, String tag2) {
  if (command.equals("+gst")) {
    sta_game.add(new Server_Card(tag1, tag2, true));
    server.write(command+"<"+tag1+"><"+tag2+">;");
 
    for(Player player : players) {
      for(Server_Card card : player.cards) {
        if(card.farbe.equals(tag1) && card.id.equals(tag2)) {
          if(lastplayer == player) lastcards.add(card);
          else {lastplayer=player; lastcards=new ArrayList<Server_Card>(); lastcards.add(card);}
        }
      }
    }
    for(int i=0; i<lastplayer.cards.size(); i++) {
      if(lastplayer.cards.get(i).farbe.equals(tag1) && lastplayer.cards.get(i).id.equals(tag2)) {
        lastplayer.cards.remove(i);
        i=-1;
      } 
    }
  }
  
  else if(command.equals("+npn")) {
        Player player = getById(tag1);
        
        if(player!=null) {
          player.alias=tag2;
          if(player.alias.length()>10) player.alias = player.alias.substring(0,10);
          println(tag1, "is now", tag2);
          server.write("+msg<"+tag2+">< joined the server!>;");
        }
  }
  
  
  else if(command.equals("+eot")) {
    server.write("+msg<"+getById(tag1).alias+" hat "+lastcards.size()+">< Karten gelegt.;");
    server.write("+msg<"+getById(tag1).alias+" hat noch "+getById(tag1).cards.size()+" Karten!><>;");
    getById(tag1).theirturn = false;
    int lastPlayer = int(tag1);
    int newPlayer = lastPlayer+1;
    if(newPlayer > players.size()) {newPlayer = 1;}
    
    String newPlayerS = ""+newPlayer;
    if(newPlayer<10) newPlayerS = "0" + newPlayerS;
    getById(newPlayerS).theirturn = true;
    
    int numfinished = 0;
    while(getById(newPlayerS).cards.size()==0) {
      getById(newPlayerS).theirturn=false;
        server.write("+msg<><"+getById(newPlayerS).alias+", ist schon fertig!>;");
        if(winner.equals("nowinner")) {winner = "a winner"; server.write("+msg<><"+getById(newPlayerS).alias+", HAT GEWONNEN!>;");}
        lastPlayer = int(newPlayer);
        newPlayer = lastPlayer+1;
        if(newPlayer > players.size()) {newPlayer = 1;}
        newPlayerS = ""+newPlayer;
        if(newPlayer<10) newPlayerS = "0" + newPlayerS;
        getById(newPlayerS).theirturn = true;
      
      numfinished++;
      if(numfinished>=players.size()) {
        server.write("+msg<Alle sind schon fertig!><>;");
        STATE++;
        return;
      }
      
    }
    server.write("+msg<"+getById(newPlayerS).alias+" ist jetzt am Zug!><>;");
    server.write("+npt<"+newPlayerS+"><>;");
    getById(newPlayerS).theirturn = true;
    println("NEXT TURN: " + newPlayerS);
    

    server.write("+msg< >< >;");
  }
  
  else if(command.equals("+paa")) {playingas = tag1; server.write("+paa<"+tag1+"><>;");}
  
  else if(command.equals("+lam")) {
    for (Player player : players) {if(player.theirturn) server.write("+msg<"+player.alias+">< glaubt: Lüge!>;");}
    for(Server_Card card : lastcards) {
      server.write("+tsc<"+card.farbe+"><"+card.id+">;");
      if(!card.id.equals(playingas)) {lied = true; server.write("+pll<"+tag1+"><>;");}
      
      continueschedule = millis()+2500;
  }
    
  }
  
  else if (command.equals("-dl4")) {
    String msgstring = "";
    for(Player player : players) {
      for(int i = 0; i<player.cards.size(); i++) { // !!!!!
        if(player.cards.get(i).id.equals(tag1)) {
          player.cards.remove(player.cards.get(i));
          println("removed a " + tag1);
          msgstring = player.alias;
          i=-1;
        }
      }
    }
    println("+msg<"+msgstring+" hat alle ><"+tag1.toUpperCase()+" vom Spiel entfernt.>;");
    server.write("+msg<"+msgstring+" hat alle ><"+tag1.toUpperCase()+" vom Spiel entfernt.>;");
  }
}