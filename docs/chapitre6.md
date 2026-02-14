# Scénario ajout d'un point d'Intérêt (POI) sur la carte

Ce chapitre explore la fonctionnalité permettant d'ajouter un point d'intérêt sur la carte d'un groupe. Ce scénario met en lumière comment l'application gère l'interaction avec la carte et la persistance de données géospatiales.

## Présentation du Scénario : Ajout d'un Point d'Intérêt (POI)

Lors d'une excursion ou d'une visite, un guide peut souhaiter marquer des lieux significatifs (points de rencontre, sites d'intérêt, dangers potentiels). L'application GuideGroup permet d'ajouter ces \"Points d'Intérêt\" (POI) sur la carte du groupe, les rendant visibles par tous les participants. Ce scénario implique la détection d'une interaction cartographique, la collecte d'informations sur le POI, et son enregistrement.

## Les Classes Impliquées dans le Scénario d'Ajout de POI

Les classes principales impliquées dans ce scénario sont :

-   ****'MapScreen'**** : La ****Vue**** (Composable Jetpack Compose) qui affiche la carte et gère les interactions (long-clic pour ajouter un POI).

-   ****'MapViewModel'**** : Le ****ViewModel**** qui gère l'état de l'UI de la carte, y compris l'affichage des POI existants et le déclenchement de l'ajout de nouveaux.

-   ****'AddPointOfInterestUseCase'**** : Un ****UseCase**** de la couche Domaine qui encapsule la logique d'ajout d'un POI.

-   ****'PointOfInterestRepository'**** : L'****interface de Référentiel**** pour les POI, définissant le contrat d'accès aux données.

-   ****'PointOfInterestRepositoryImpl'**** : L'****implémentation concrète**** du Référentiel, qui interagit avec Firebase Firestore.

-   ****'AuthRepository'**** : L'interface de Référentiel pour l'authentification, utilisée pour vérifier le rôle de l'utilisateur (doit être un guide) et obtenir son ID.

-   ****'PointOfInterest'**** : Le ****modèle de données**** représentant un point d'intérêt.

-   ****'MapUiState'**** : Un modèle d'état spécifique à l'UI de la carte, exposé par le ViewModel, incluant potentiellement l'état d'un dialogue d'ajout de POI.

-   ****'Result'**** : La classe scellée générique pour la gestion des résultats d'opération.

-   ****'FirestoreHelper'**** : Une classe utilitaire pour interagir avec Firebase Firestore.

## Diagramme UML des Classes du Scénario d'Ajout de POI

Ce diagramme UML de classes illustre les relations statiques entre les composants clés du scénario d'ajout de POI.

![Diagramme de classe - scénario ajout POI](images/diagramme_classe_scenario_ajout_POI_latex.svg)

****Explication du Diagramme :****

-   La ****'MapScreen'**** (Vue) interagit avec le ****'MapViewModel'**** pour afficher la carte et les POI, et pour déclencher l'ajout d'un nouveau POI.

-   Le ****'MapViewModel'**** dépend d'un ****'AddPointOfInterestUseCase'**** pour la logique d'ajout, d'un ****'PointOfInterestRepository'**** pour la gestion des POI existants, et d'un ****'AuthRepository'**** pour obtenir le rôle de l'utilisateur.

-   Le ****'AddPointOfInterestUseCase'**** dépend de l'interface ****'PointOfInterestRepository'****.

-   L'implémentation concrète ****'PointOfInterestRepositoryImpl'**** gère la persistance des POI via ****'FirestoreHelper'**** dans Firebase Firestore.

-   Le ****'PointOfInterest'**** est le modèle de données central pour les POI.

-   ****'MapUiState'**** est le modèle d'état UI spécifique à l'écran de la carte.

## Analyse Détaillée des Composants et du Code

### Le Modèle (Couches Domaine et Données)

1.  PointOfInterest.kt (Modèle de Données)

    -   ****Rôle :**** Représente une entité de point d'intérêt. Elle contient des informations telles que les coordonnées géographiques, le nom, la description, l'ID du groupe, l'ID de l'utilisateur qui l'a ajouté, et l'horodatage. Ce modèle est indépendant de la source de données et de l'UI.

2.  PointOfInterestRepository.kt (Interface du Référentiel de POI)

    -   ****Rôle :**** Définit le contrat pour toutes les opérations CRUD (Create, Read, Update, Delete) liées aux points d'intérêt. Cette interface fait partie de la ****couche Domaine****. Le reste de l'application interagit avec les POI **uniquement** via cette interface.

3.  PointOfInterestRepositoryImpl.kt (Implémentation du Référentiel de POI)

    -   ****Rôle :**** Cette classe fait partie de la ****couche Données****. Elle implémente PointOfInterestRepository et contient la logique concrète pour interagir avec ****Firebase Firestore**** via firestoreHelper pour ajouter ou récupérer les POI. Le getPointsOfInterestForGroup utilise la capacité de Firestore à émettre des mises à jour en temps réel via un 'Flow', assurant que les POI s'affichent instantanément.

4.  AddPointOfInterestUseCase.kt (Use Case d'Ajout de POI)

    -   ****Rôle :**** Fait partie de la ****couche Domaine****. Ce Use Case encapsule la logique métier \"Ajouter un point d'intérêt\". Il dépend de l'interface PointOfInterestRepository.

### Le ViewModel (Couche Présentation)

1.  MapUiState.kt (Modèle d'État UI pour la Carte)

    Rôle : Représente l'état complexe de l'interface utilisateur de l'écran de la carte. Il inclut les localisations, les POI, les états de chargement, les messages d'erreur et des indicateurs pour l'interaction avec le dialogue d'ajout de POI.

2.  MapViewModel.kt (Extrait pertinent pour l'ajout de POI)

    Rôle : Fait partie de la couche Présentation. Le MapViewModel gère la logique de présentation pour l'écran de la carte.
    Il expose un StateFlow\<MapUiState\> (uiState) que la Vue observe pour réagir aux changements d'état (affichage du dialogue d'ajout de POI, erreurs, chargement).
    loadUserRole() : Récupère le rôle de l'utilisateur pour déterminer si l'ajout de POI est autorisé.
    onMapLongClick(latLng: LatLng) : Cette fonction est appelée par la Vue lorsqu'un long-clic est détecté sur la carte. Si l'utilisateur est un guide, elle met à jour l'état pour afficher un dialogue de saisie d'informations pour le POI.
    addPointOfInterest(name: String, description: String) : Cette fonction est appelée par la Vue lorsque le guide confirme l'ajout du POI. Elle construit l'objet PointOfInterest et le délègue au AddPointOfInterestUseCase.
    loadPointsOfInterest() : Observe les POI existants du PointOfInterestRepository en temps réel pour les afficher sur la carte.

### La Vue (Couche Présentation)

1.  MapScreen.kt (Extrait pertinent pour l'ajout de POI)

    Rôle : C'est la Vue concrète de notre architecture pour l'écran de la carte. Elle fait partie de la couche Présentation.\
    Elle affiche la carte Google Maps et est responsable de la gestion des interactions utilisateur spécifiques à la carte, comme le long-clic.\
    Le paramètre onMapLongClick de GoogleMap est configuré pour appeler viewModel.onMapLongClick(latLng), déléguant la gestion de l'événement au ViewModel.\
    Elle observe les StateFlow (uiState) du MapViewModel pour :\
    Afficher les marqueurs des POI (uiState.pointsOfInterest).\
    Contrôler l'affichage du dialogue d'ajout de POI (uiState.showAddPoiDialog).\
    Afficher les messages d'erreur (uiState.errorMessage, uiState.addPoiError).\
    Gérer l'état de chargement du dialogue (uiState.isAddingPoi).\
    Le AddPointOfInterestDialog est un composable séparé qui collecte les informations du POI et délègue l'action de confirmation à viewModel.addPointOfInterest().

## Diagramme de Séquence UML : Scénario d'ajout d'un Point d'Intérêt

Ce diagramme visualise le flux dynamique des interactions lors de l'ajout d'un point d'intérêt.

![Diagramme de séquence - scénario ajout d'un POI](images/diagramme_sequence_scenario_ajout_POI_latex.svg)

Explication Détaillée des Étapes du Diagramme :

**Phase 1: Initialisation et Vérification du Rôle**

Utilisateur -\> MapScreen : L'utilisateur accède à l'écran de la carte pour un groupe spécifique.\
MapScreen -\> MapViewModel : La MapScreen s'initialise et commence à observer les StateFlow du MapViewModel (uiState et pointsOfInterest).\
MapViewModel -\> AuthRepository (Interface) : Le MapViewModel demande le profil de l'utilisateur courant pour déterminer son rôle (guide ou participant). Cela est crucial pour autoriser ou non l'ajout de POI.\
AuthRepositoryImpl -\> FirestoreHelper -\> Firebase Firestore DB : L'implémentation du référentiel d'authentification interagit avec Firestore pour obtenir les détails du profil utilisateur.\
Firebase Firestore DB --\> AuthRepositoryImpl --\> MapViewModel : Le rôle de l'utilisateur est renvoyé au ViewModel.\
MapViewModel -\> MapViewModel : Le ViewModel met à jour uiState.userRole afin que la Vue puisse adapter son comportement (ex: désactiver l'action de long-clic si l'utilisateur n'est pas un guide).\
MapViewModel -\> PointOfInterestRepository (Interface) : Le ViewModel initie également le chargement des POI existants du groupe en appelant PointOfInterestRepository.getPointsOfInterestForGroup().\
PointOfInterestRepositoryImpl -\> FirestoreHelper -\> Firebase Firestore DB : L'implémentation du référentiel de POI établit une écoute en temps réel sur la collection pointsOfInterest du groupe dans Firestore.\
Firebase Firestore DB --\> FirestoreHelper --\> PointOfInterestRepositoryImpl --\> MapViewModel : Firestore renvoie continuellement des listes de POI mises à jour via un Flow au ViewModel.\
MapViewModel -\> MapScreen : Le ViewModel met à jour uiState.pointsOfInterest, ce qui déclenche la recomposition de la MapScreen et l'affichage des marqueurs des POI existants sur la carte.

**Phase 2: Déclenchement de l'Ajout de POI**

Utilisateur -\> MapScreen : L'utilisateur effectue un long-clic sur un point de la carte où il souhaite ajouter un POI. Les coordonnées (latLng) de ce point sont capturées.\
MapScreen -\> MapViewModel : La MapScreen appelle la méthode onMapLongClick(latLng) du MapViewModel, lui passant les coordonnées.\
MapViewModel -\> MapViewModel : Le ViewModel vérifie le userRole dans son uiState.\
Si userRole est \"guide\" : Le ViewModel met à jour son uiState pour activer l'affichage du dialogue d'ajout de POI (showAddPoiDialog = true) et stocke temporairement les coordonnées du POI (tempPoiLocation = latLng).\
Si userRole n'est pas \"guide\" : Le ViewModel met à jour son uiState pour afficher un message d'erreur (ex: via un Snackbar) indiquant que seuls les guides peuvent ajouter des POI.\
MapViewModel --\> MapScreen : L'état uiState est mis à jour, ce qui déclenche la recomposition de la MapScreen.\
MapScreen -\> Utilisateur : Si l'utilisateur est un guide, la MapScreen affiche le dialogue \"Ajouter un Point d'Intérêt\", invitant l'utilisateur à saisir le nom et la description du POI.

**Phase 3: Saisie des Informations et Confirmation**

Utilisateur -\> MapScreen : L'utilisateur saisit le nom et la description dans le dialogue et clique sur le bouton \"Ajouter\".\
MapScreen -\> MapViewModel : La MapScreen appelle la méthode addPointOfInterest(name, description) du MapViewModel, lui passant les informations saisies.\
MapViewModel -\> MapViewModel : Le ViewModel construit un objet PointOfInterest complet, en utilisant les coordonnées temporaires stockées (tempPoiLocation), l'ID de l'utilisateur courant (currentUserId), le nom, la description et un ID unique généré. Il met à jour uiState pour indiquer que l'ajout est en cours (isAddingPoi = true) et effacer les erreurs précédentes.\
MapViewModel -\> AddPointOfInterestUseCase : Le ViewModel délègue l'opération d'ajout au AddPointOfInterestUseCase.\
AddPointOfInterestUseCase -\> PointOfInterestRepository (Interface) : Le Use Case appelle la méthode addPointOfInterest() sur l'interface du référentiel.\
PointOfInterestRepositoryImpl -\> FirestoreHelper -\> Firebase Firestore DB : L'implémentation concrète du référentiel utilise FirestoreHelper pour écrire le nouvel objet PointOfInterest dans la collection Firestore groups/groupId/pointsOfInterest.\
Firebase Firestore DB --\> FirestoreHelper --\> PointOfInterestRepositoryImpl --\> AddPointOfInterestUseCase --\> MapViewModel : Le résultat de l'opération d'écriture (succès ou échec) est renvoyé sous forme de Flow\<Result\<Unit\>\> et propagé jusqu'au ViewModel.\
MapViewModel -\> MapViewModel : Le ViewModel collecte le Flow et gère le résultat :\
Si Result.Success : Le ViewModel met à jour uiState pour fermer le dialogue (showAddPoiDialog = false) et désactiver l'état de chargement (isAddingPoi = false).\
Si Result.Error : Le ViewModel met à jour uiState pour inclure le message d'erreur (addPoiError = \"...\"), qui sera affiché dans le dialogue.\
MapViewModel --\> MapScreen : L'état uiState est mis à jour, ce qui déclenche la recomposition de la MapScreen.\
MapScreen -\> Utilisateur : Si l'ajout a réussi, le dialogue se ferme. Si une erreur est survenue, le message d'erreur est affiché dans le dialogue.

**Phase 4: Synchronisation en Temps Réel et Affichage du Nouveau POI**

Firebase Firestore DB -\> FirestoreHelper -\> PointOfInterestRepositoryImpl : Dès que le nouveau POI est écrit dans Firestore, la \"listener\" (écouteur en temps réel) établie par getPointsOfInterestForGroup() détecte ce changement et envoie une nouvelle liste de POI mise à jour.\
PointOfInterestRepositoryImpl --\> MapViewModel : Le Flow\<List\<PointOfInterest\>\> émet cette nouvelle liste au MapViewModel.\
MapViewModel -\> MapScreen : Le MapViewModel met à jour son uiState.pointsOfInterest StateFlow avec la nouvelle liste, ce qui déclenche la recomposition de la MapScreen.\
MapScreen -\> Utilisateur : La MapScreen se met à jour et le nouveau marqueur du POI apparaît instantanément sur la carte pour l'utilisateur qui l'a ajouté, et pour tous les autres membres du groupe qui regardent également la carte.\
Ce scénario met en évidence la robustesse de l'architecture MVVM pour gérer les interactions complexes d'UI, la validation des rôles, et la persistance des données en temps réel.\
Le Chapitre 6 marque la fin de l'étude de trois scénarios clés avec leurs diagrammes de classes, code et diagrammes de séquence détaillés.
