# Conclusion

## Conclusion

Dans ce document nous avons d'abord présenté au chapitre 1, l'application les fonctionnalités prncipales ainsi que les téchnologies utilisées pour sa conception. Nous sommes ensuite entré dans le coeur de son fonctionnement : dans le chapitre 2 nous avons présenté l'architecture MVVM (Model View View Model) composé des couches donées et domaine, interface utilisateur et ViewModel pour faire le pont entre les deux premières couches. Nous avons également mis l'accent sur l'injection de dépendances avec hilt qui permet un découplage fort entre objet. Dans le chapitre 3 nous avons présenté en détail les cas d'utilisation. Au chapitre 3, nous avons présenté en détail le scenario de connexion d'un utilisateur avec le diagramme de classe et de séquence afin de montrer la dynamique du scenario. Nous avons continué dans le chapitre 5 avec la présentation du scenario d'envoi d'un message texte dans la messageie du groupe, avec le détail des diagramme de classe et de séquence. Dans le chapotre 6 nous terminons par le scenario d'ajout d'un point d'intérêt (POI) sur la carte, nous avons également réalisé les diagramme de classe et de séquences.

Cette application nous a permis de mettre en oeuvre une architecture robuste. Sa mise en oeuvre a été difficile et les problèmes de cohérene et la compatibilité des versions de plugins entre eux a été déllicate. Nous avions opté pour la gestion des variants de build un mock et un prod. Le mock nous a posé des difficulté (avec le fichier build.gradle (niveau module)), nous avons mis cette fonctionnalité en entre parathèse.

L'application GuideGroup est à ce jour un prototype qui va contiuer à intégrer de nouvelles fonctionnalités.

Cette pourrait pourrait être améliorée par une implémentation qui prendrait en compte d'autres plateforme mobiles et web. Ce concept permettrait le déploiement de l'application sur Iphone et sur le web. Ce concept d'application mutiplateforme n'est pas nouveau mais il est maintenant facilité par kotlin multiplateforme.
