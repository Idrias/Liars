class Network {
  String appendix = "";

  Network() {
  }


  void push (String command, String tag1, String tag2) {
    client.write(command+"<"+tag1+"><"+tag2+">;");
  }


  void pull() {

    if (!client.active() && game.connectionStatus && game.hadConnection) {
      game.connectionStatus = false; 
      game.board.load_background("disconnected.jpg");
    }
    
    else if(!client.active() && game.connectionStatus && !game.hadConnection) {
      game.connectionStatus = false; 
      game.board.load_background("disconnected.jpg"); //!!
      //game.dctime -= 5000; ?????????
    }


    if (client.available()!=0) {
      String incoming = appendix + client.readString();
      println("APPENDIX:", appendix);
      println("INCOMING:", incoming);
      appendix = "";
      ArrayList<String> msgs = new ArrayList<String>();
    
      String happen = "";

      for (int i=0; i < incoming.length(); i++) {
        if (incoming.charAt(i) == ';') {
          msgs.add(happen); 
          happen="";
        } else happen += incoming.charAt(i);
      }
      if(happen.length() > 0) {appendix += happen; println("adding", happen, "to", appendix);}
      decode(msgs);
    }
  }


  void decode(ArrayList<String> msgs) {
    for (String s : msgs) {
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
    if      (command.equals("+p"+game.playerid))                                  {game.board.sta_player.add(new Card(tag1, tag2, false)); /*println("Now we have", game.board.sta_player.size(), "cards");*/}
    else if (command.equals("+gst"))                                              game.board.sta_game.add(new Card(tag1, tag2, true));
    else if (command.equals("+msg"))                                              game.board.msgs.add(tag1+tag2);
    else if (command.equals("+npl") && game.playerid.equals("999"))               game.playerid = tag1; 
    else if (command.equals("-pst") && tag1.equals("all") && tag2.equals("all"))  game.board.sta_player = new ArrayList<Card>();
    else if (command.equals("-gst") && tag1.equals("all") && tag2.equals("all"))  game.board.sta_game = new ArrayList<Card>();
    else if (command.equals("+nsa")) game.stage = int(tag2);
    else if (command.equals("+dsp") && tag1.equals(game.playerid))                {game.myTurn = true; game.firstTurn = true; par = new PlayAsRequest();}
    else if (command.equals("+dsp") && !tag1.equals(game.playerid))               game.board.playingas = "null";
    else if (command.equals("+npt") && tag1.equals(game.playerid))                {game.myTurn = true; game.firstTurn = false;} 
    else if (command.equals("+paa"))                                              game.board.playingas = tag1;
    else if (command.equals("+tsc"))                                              {Card card = new Card(tag1, tag2, true); for(int i=0; i<game.board.sta_game.size(); i++) {if(game.board.sta_game.get(i).farbe.equals(tag1) && game.board.sta_game.get(i).id.equals(tag2)) {game.board.sta_game.remove(i); i=-1;}} card.hidden=false; game.board.sta_game.add(card);}
    else if (command.equals("+con"))                                              {game.board.playingas="null";}  
    else if (command.equals("+res") && tag1.equals("ALL") && tag2.equals("ALL"))  {println("here"); String id = game.playerid; game = new Game(); game.playerid = id;}
    else if (command.equals("+fre") && tag1.equals("+"))                          {game.board.load_background("fnaf.jpg");}
    else if (command.equals("+fre") && tag1.equals("-"))                          {game.board.load_background("board.jpg");}
    else if (command.equals("+npi") && tag1.equals(game.playerid))                {game.playerid = tag2;}
    else if (command.equals("+eot"))                                              {game.myTurn = false; game.firstTurn=false;}
    else if (command.equals("+sub"))                                              {game.board.subt = tag1;}
    else if (command.equals("+bom"))                                              {game.board.bombers.add(new Bomber());}
    else if (command.equals("+cen"))                                              {audio.play("sound", "cena", true, false);}
  }
}