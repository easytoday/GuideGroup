# Introduction et vue d'ensemble de l'application GuideGroup

## Introduction 

Ce document a pour objectif de fournir une description détaillée de l'application mobile \"GuideGroup\", conçue pour faciliter l'orientation et la communication au sein de groupes lors de déplacements, d'événements ou de visites.

L'application GuideGroup permet à des groupes de personnes qui se déplacent ensemble, de garder le contact, de partager des points d'intérêt et de \"chatter\" par un canal de communication unifié. Elle la gestion du groupe, la localisation en temps réel et la communication interactive.

Le code source de l'application est disponible sur github à l'adresse suivante : <https://github.com/easytoday/GuideGroup.git>.

## Objectifs et fonctionnalités clés de l'application

L'application GuideGroup offre plusieurs fonctionnalités, voici les principales :

### Gestion des groupes

Les utilisateurs peuvent créer des groupes, les rejoindre et les quitter. Chaque groupe est une entité distincte avec ses propres membres, son chat et ses points d'intérêt partagés.

-   **Création de Groupe :** Permet aux guides de démarrer de nouvelles sessions de groupe.

-   **Rejoindre un Groupe :** Les participants peuvent intégrer un groupe existant via un code unique.

-   **Gestion des Rôles :** Distinction claire entre les \"Guides\" (qui peuvent initier le suivi et ajouter des POI) et les \"Participants\".

### Localisation en temps réel

La localisation en temps réel est essentielle pour l'orientation et la sécurité du groupe.

-   **Suivi de Localisation :** Affichage en direct de la position de tous les membres du groupe sur une carte interactive. Cette fonctionnalité est activée et désactivée par le guide.

-   **Points d'Intérêt (POI) :** Les guides peuvent marquer des lieux significatifs sur la carte, visibles par tous les membres du groupe.

### Communication intégrée

Un canal de communication dédié pour chaque groupe afin de faciliter les échanges.

-   ****Chat de Groupe :**** Messagerie texte en temps réel entre tous les membres du groupe.

-   ****Partage de Médias :**** Possibilité d'envoyer des photos ou d'autres médias dans le chat pour enrichir la communication.

## Public Cible

GuideGroup permet d'aider les :

-   ****Guides touristiques et groupes de voyage :**** Pour maintenir la cohésion et l'orientation des participants.

-   ****Événements de groupe :**** Festivals, conférences, excursions où la localisation et la communication sont nécessaires.

-   ****Familles et amis :**** Pour les sorties en plein air, les randonnées ou les grands rassemblements.

## Aperçu des principales technologies utilisées

L'application GuideGroup est développée sur la plateforme Android, elle utilise principalement les technologies suivantes:

-   ****Langage de Programmation :**** Kotlin

-   ****Framework UI :**** Jetpack Compose (pour une UI moderne et déclarative)

-   ****Gestion de l'État et Architecture :**** MVVM (Model-View-ViewModel)

-   ****Injection de Dépendances :**** Hilt (basé sur Dagger 2, pour la gestion des dépendances)

-   ****Backend as a Service (BaaS) :**** Firebase (Authentication, Firestore, Storage) pour la gestion des utilisateurs, la base de données en temps réel et le stockage des fichiers.

-   ****Services de Cartographie :**** Google Maps Platform (pour l'affichage de la carte et la gestion de la localisation).

Le prochain chapitre aborde l'architecture de l'application GuideGroup.


 **→** [Chapitre 2 : Architecture logicielle générale](chapitre2.md)