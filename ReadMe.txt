particularités :
- l'annuaire port 12000 ne fait pas partie de l'anneau et renvoie la liste des ports sur écoute des 5 derniers clients
- chaque client N se connecte seulement au client N-1 (puis N+1 se connecte à N) et ne connait que les adresses/ports de N-1 et N+1
- messages en json de types "ring" (modification du l'anneau), "msg" (envoi d'un message sur le tchat) ou "salon" (changement de salon0)
- il y a 4 salons fixes et publics (les clients recoivent les msgs de tous les salons mais n'affichent que ceux du leur)

execution :

terminal à Projet_SD/bin
java -cp ../java-json.jar;. Annuaire

terminaux à Projet_SD/bin
java -cp ../java-json.jar;. Client
