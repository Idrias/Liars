void serverEvent(Server theServer, Client theClient) {
  if(STATE==0) {
    int playerid = 1;
  
    for (int i=0; i<players.size(); i++) {
      if ((playerid<10 && players.get(i).id.equals("0" + playerid)) || (playerid>=10 && players.get(i).id.equals(""+playerid))) {
        playerid++; 
        i=-1; // CHANGE!
      }
    }
    
    String sid = "";
    if(playerid>=10) sid = ""+playerid;
    else sid = "0"+playerid;
    
    players.add(new Player(theClient, sid));
    println("WE HAVE PLAYER " + sid);
    server.write("+npl<"+sid+"><>;");
  }
  
  else server.disconnect(theClient);
}


void disconnectEvent(Client theClient) {
   for(int i = 0; i<players.size(); i++) {
     Player player = players.get(i);
     if(player.client == theClient) {
       players.remove(player);
       storedmsgs.add("+msg<"+players.get(i).alias+">< left the server!>;");
     
       for(Server_Card card : players.get(i).cards) {
         sta_game.add(card);
         storedmsgs.add("+gst<"+(card.farbe)+"><"+(card.id)+">;");
       } 
       
       
        int awaitednum = 1;
        for(Player player2 : players) {
          if(int(player2.id) != awaitednum) {
        String newid = ""+awaitednum;
        if(awaitednum<10) newid = "0"+newid;
        server.write("+npi<"+player2.id+"><"+newid+">;");
        player2.id = newid;
      }
      awaitednum++;
    }
     }
   }
}