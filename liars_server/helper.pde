void disconnectTask() {
  for (String name : toDisconnect) {
    for (int i=0; i<players.size(); i++) {
      if (name.equals(players.get(i).id)) {
        server.disconnect(players.get(i).client); 
        toDisconnect=new ArrayList<String>();
      }
    }
  }
}

void give_cards() {
  ArrayList<Server_Card> cards = new ArrayList<Server_Card>();
  for (Server_Card card : all_cards) cards.add(card); // Gleichsetzen nicht möglich da sonst reference!

  while (cards.size() / players.size() >= 1) {
    for (Player player : players) {
      int givenCard = int(random(cards.size()));
      println(givenCard);
      println(cards.size());
      player.cards.add(cards.get(givenCard));
      server.write("+p"+player.id+"<"+cards.get(givenCard).farbe+"><"+cards.get(givenCard).id+">;");
      cards.remove(givenCard);
    }
  }

  for (Server_Card card : cards) {
    sta_game.add(card);
    println(card.farbe, card.id);
    server.write("+gst<"+card.farbe+"><"+card.id+">;");
  }
}


String findCardOwner(String farbe, String id) {
  for (Player player : players) {
    for (Server_Card card : player.cards) {
      if (card.farbe == farbe && card.id== id)
        return player.id;
    }
  }

  for (Server_Card card : sta_game) {
    if (card.farbe == farbe && card.id== id)
      return "GAMESTACK";
  }

  return "ERROR: NOT FOUND";
}



Player getById(String id) {
  for (Player player : players) {
    if (player.id.equals(id)) return player;
  }

  Player Gustaf = null;
  return Gustaf;
}