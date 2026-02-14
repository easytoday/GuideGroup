# Diagrammes d'utilisation uml

## Diagrammes de cas d'utilisation

Les diagrammes de cas d'utilisation permettent modéliser les exigences fonctionnelles d'un système. ils décrivent :

-   ****Ce que le système fait :**** les fonctionnalités ou services qu'il offre.

-   ****Qui interagit avec le système :**** les acteurs (utilisateurs ou systèmes externes).

-   ****Comment les acteurs interagissent avec les fonctionnalités :**** les relations entre acteurs et cas d'utilisation.

Ils représentent une vue de haut niveau, sans s'attarder sur les détails d'implémentation.

## Identification des acteurs de guidegroup

Pour GuideGroup, nous avons identifié trois acteurs principaux qui interagissent avec le système :

-   **Utilisateur (user) :**
    C'est l'acteur de base et le plus générique. Il représente toute personne qui utilise l'application, qu'elle soit un guide ou un simple participant. Les cas d'utilisation associés à cet acteur sont des fonctionnalités universelles disponibles pour tous les utilisateurs.

-   **Guide :**
    Le \"guide\" est un type spécifique d'utilisateur avec des privilèges et responsabilités étendus. Un guide est généralement la personne qui initie et gère l'activité de groupe. Cet acteur hérite des capacités de l'utilisateur et en possède des spécifiques.

-   **Participant :**
    Le \"participant\" est l'autre type spécifique d'utilisateur. il rejoint un groupe créé par un guide et interagit principalement au sein de ce groupe. cet acteur hérite également des capacités de l'utilisateur et en possède des spécifiques.

## Diagramme de cas d'utilisation global de guidegroup

Ce diagramme offre une vue d'ensemble complète des interactions entre les acteurs et les fonctionnalités principales de l'application.

![Diagramme de classe - cas d'utilisation](images/diagramme_use_cases.svg)


## Description Détaillée des Cas d'Utilisation

### Gestion des Comptes

**UC1** : S'inscrire\
Description : Permet à un nouvel utilisateur de créer un compte dans l'application en fournissant son email, mot de passe et un nom d'utilisateur. Il choisit également son rôle initial (Guide ou Participant).\
Acteurs : Utilisateur\
**UC2** : Se connecter\
Description : Permet à un utilisateur enregistré de s'authentifier pour accéder aux fonctionnalités de l'application.\
Acteurs : Utilisateur\
**UC3** : Se déconnecter
Description : Permet à un utilisateur de fermer sa session et de ne plus être connecté.
Acteurs : Utilisateur

### Gestion des Groupes

**UC4** : Créer un groupe\
Description : Permet à un guide de fonder un nouveau groupe, en lui attribuant un nom et une description. Un code unique est généré pour permettre aux participants de rejoindre.\
Acteurs : Guide\
**UC5** : Rejoindre un groupe\
Description : Permet à un participant de s'intégrer à un groupe existant en utilisant le code d'invitation fourni par le guide.\
Acteurs : Participant\
**UC6** : Quitter un groupe\
Description : Permet à un participant de se retirer volontairement d'un groupe auquel il appartient.\
Acteurs : Participant\
**UC7** : Voir la liste des groupes\
Description : Permet à l'utilisateur de consulter tous les groupes auxquels il est actuellement membre.\
Acteurs : Utilisateur\
**UC8** : Voir les détails d'un groupe\
Description : Permet à l'utilisateur d'accéder aux informations spécifiques d'un groupe, comme sa description, les membres actifs, etc.\
Acteurs : Utilisateur\

### Communication (Chat)

**UC9** : Envoyer un message texte\
Description : Permet à un participant d'envoyer un message textuel à l'ensemble des membres d'un groupe via le chat intégré.\
Acteurs : Participant\
Relation : Inclut Envoyer un message média (cette relation signifie que l'envoi de média fait partie du processus général d'envoi de message, ou est une extension de celui-ci).\
**UC10** : Envoyer un message média\
Description : Permet à un participant de partager des fichiers multimédias (photos, vidéos courtes, audio) dans le chat de groupe.\
Acteurs : Participant\
**UC11** : Voir l'historique du chat\
Description : Permet à l'utilisateur de consulter tous les messages précédemment envoyés et reçus dans un groupe spécifique.\
Acteurs : Utilisateur\

### Localisation et Cartographie

**UC12** : Voir sa localisation sur la carte\
Description : Permet à l'utilisateur de visualiser sa propre position géographique sur la carte interactive de l'application.\
Acteurs : Utilisateur\
**UC13** : Voir la localisation des membres du groupe\
Description : Permet à un participant de suivre en temps réel la position des autres membres de son groupe sur la carte.\
Acteurs : Participant\
**UC14** : Ajouter un point d'intérêt (POI)\
Description : Permet à un guide de marquer un emplacement spécifique sur la carte du groupe, en lui donnant un nom et une brève description, afin que tous les membres du groupe puissent le voir.\
Acteurs : Guide\
**UC15** : Voir les points d'intérêt\
Description : Permet à l'utilisateur de visualiser tous les points d'intérêt qui ont été ajoutés par les guides sur la carte du groupe.\
Acteurs : Utilisateur\
**UC16** : Démarrer le suivi de localisation\
Description : Permet à un guide d'activer le partage de la localisation en temps réel pour tous les membres du groupe, et de commencer lui-même à partager sa propre position.\
Acteurs : Guide\
**UC17** : Arrêter le suivi de localisation\
Description : Permet à un guide de désactiver le partage de la localisation en temps réel pour l'ensemble du groupe.\
Acteurs : Guide\
**UC18** : Gérer les permissions de localisation\
Description : Permet à l'utilisateur de gérer les autorisations de localisation nécessaires pour le fonctionnement de l'application (ex: autorisation d'accéder à la localisation en arrière-plan).\
Acteurs : Utilisateur\
Ce chapitre a fourni une vue d'ensemble de l'application GuideGroup. Le prochain chapitre commencera l'exploration des scénarios MVVM avec les diagrammes de séquence et les extraits de code (en annexe).


**←** [Chapitre 2 : Architecture générale](chapitre2.md) | [Chapitre 4 : Scénario connexion d'un Utilisateur](chapitre4.md) **→**