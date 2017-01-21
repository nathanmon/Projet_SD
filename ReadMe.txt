particularités :
- l'annuaire port 12000 ne fait pas partie de l'anneau et renvoie la liste des ports sur écoute des 5 derniers clients
- chaque client N se connecte seulement au client N-1 (puis N+1 se connecte à N) et ne connait que les adresses/ports de N-1 et N+1
- messages en json de types "ring" (modification de l'anneau), "hello" (envoi du port sur ecoute au nouveau client n+1), "msg" (envoi d'un message sur le tchat), "newSalon" (creation d'un nouveau salon) ou "salon" (changement de salon)
- il y a 4 salons fixes et publics (tous les clients recoivent les msgs de tous les salons mais n'affichent que ceux du leur)

execution :
Lancer Annuaire puis des Clients

en ligne de commande :
à Projet_SD/bin :
java -cp ../java-json.jar;. Annuaire
java -cp ../java-json.jar;. Client

protocole :
- annuaire
le client N se connecte à l'annuaire (port 12000),
il lui envoie le port qu'il utilisera pour écouter le flux dans l'anneau
l'annuaire lui envoie la liste des 5 derniers clients à s'être connectés
- joindre l'anneau
le client tente de se connecter au client N-1, sinon N-2, etc.
lorsque le client N-1 détecte la nouvelle connexion (thread Client) il relance le thread MessageListener
pour que N+1 parle à N : le client N envoie un message dans l'anneau en envoyant "oldPort" : le port auquel N parle maintenant, "newPort" : le port sur lequel N écoute
- quitter l'anneau
pour que N+1 parle à N-1 : lorsque N-1 détecte la déconnexion de N, il envoie un message "oldPort" : le port sur lequel écoutait N, "newPort" : le port sur lequel N+1 écoute
la variable "precedent" est utile lorsque l'anneau se compose de 2 clients : en cas de déconnexion si le precedent est égal au suivant il ne restera qu'un client qui ne pourra donc pas joindre N-1
- envoi de messages
tous les messages sont en json, envoyes et reçus par Client.envoyer(JSONObject x) et JSONObject Client.lire()
c'est le thread MessageListener qui attend la réception de messages de l'anneau
ils font le tour complet de l'anneau, jusqu'à que le client N retrouve son id dans json.getInt("id")
les messages de l'utilisateur sont écouter soit par un actionListener dans la classe Client via l'interface graphique soit par le thread Messagerie via le terminal