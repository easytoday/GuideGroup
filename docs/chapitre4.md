# Scénario connexion d'un Utilisateur

Ce chapitre est le premier d'une série d'études de cas détaillées qui décomposent des fonctionnalités clés de l'application GuideGroup. Pour chaque scénario, nous allons explorer :

-   Les classes impliquées à travers les différentes couches de l'architecture MVVM.

-   Un diagramme UML des classes pour visualiser les relations.

-   Une analyse détaillée du code pour chaque composant (Modèle, ViewModel, Vue).

-   Un diagramme de séquence UML pour illustrer la dynamique des interactions.

## Présentation du Scénario : Connexion d'un Utilisateur

Le scénario de connexion montre la manière dont l'application GuideGroup gère les identifiants de l'utilisateur, interagit avec un service d'authentification externe (Firebase Auth) et met à jour l'interface utilisateur en fonction du succès ou de l'échec de la connexion.

## Les Classes Impliquées dans le Scénario de Connexion

Nous allons nous concentrer sur les classes suivantes pour ce scénario :

-   ****'LoginScreen'**** : La ****Vue**** (Composable Jetpack Compose) qui affiche l'interface de connexion.

-   ****'LoginViewModel'**** : Le ****ViewModel**** qui gère l'état de l'UI de connexion et la logique de présentation.

-   ****'SignInUseCase'**** : Un ****UseCase**** de la couche Domaine qui encapsule la logique métier de l'authentification.

-   ****'AuthRepository'**** : L'****interface de Référentiel**** pour l'authentification, définissant le contrat.

-   ****'AuthRepositoryImpl'**** : L'****implémentation concrète**** du Référentiel, qui interagit avec Firebase.

-   ****'User'**** : Le ****modèle de données**** représentant un utilisateur de l'application.

-   ****'AuthUiState'**** : Un modèle d'état spécifique à l'UI de connexion, exposé par le ViewModel.

-   ****'Result'**** : Une classe scellée (sealed class) générique pour gérer les différents états d'une opération asynchrone (Loading, Success, Error).

-   ****'FirebaseAuth'**** : Le service externe de Firebase pour l'authentification.

-   ****'FirestoreHelper'**** : Une classe utilitaire pour interagir avec Firebase Firestore (pour récupérer le profil utilisateur après l'authentification).

## Diagramme UML des Classes du Scénario de Connexion

Ce diagramme UML de classes illustre les relations statiques entre les composants clés du scénario de connexion.

 ![Diagramme de classes - Scénario de connexion](images/diagramme_classe_scenario_connexion_latex.svg)


Explication du Diagramme :

La LoginScreen (Vue) observe et interagit avec le LoginViewModel.\
Le LoginViewModel dépend d'un SignInUseCase (injecté).\
Le SignInUseCase dépend de l'interface AuthRepository (injectée).\
L'implémentation concrète AuthRepositoryImpl implémente l'interface AuthRepository.\
AuthRepositoryImpl dépend des services externes FirebaseAuth et FirestoreHelper (injectés) pour la logique d'authentification et la récupération des données utilisateur.\
Les modèles de données comme User et l'état de l'UI AuthUiState sont utilisés à travers les couches pertinentes.\
La classe Result est une abstraction pour gérer les états des opérations asynchrones.

## Analyse Détaillée des Composants et du Code

Nous allons maintenant examiner le code de chaque composant, en commençant par les éléments du \"Modèle\" (couches Domaine et Données), puis le ViewModel, et enfin la Vue.

### Le Modèle (Couches Domaine et Données)

1.  User.kt (Modèle de Données)

    Rôle : Représente l'entité utilisateur. C'est un modèle simple et indépendant de toute source de données ou UI. Il est utilisé à travers toutes les couches.

2.  Result.kt (pour la Gestion des Résultats)

    Rôle : Une classe générique qui encapsule l'état d'une opération asynchrone (chargement en cours, succès de récupération des données, ou échec avec une exception). C'est une pratique courante pour la gestion des erreurs et des états dans les flux de données.

3.  AuthRepository.kt (Interface du Référentiel d'Authentification)

    Rôle : Définit le contrat pour toutes les opérations d'authentification et de gestion des utilisateurs. Cette interface fait partie de la couche Domaine. Le reste de l'application interagit avec l'authentification uniquement via cette interface, garantissant le découplage.

4.  AuthRepositoryImpl.kt (Implémentation du Référentiel d'Authentification)

    Rôle : Cette classe fait partie de la couche Données. Elle implémente AuthRepository et contient la logique concrète pour interagir avec Firebase Authentication (firebaseAuth) pour se connecter ou s'inscrire, et avec Firebase Firestore (firestoreHelper) pour stocker/récupérer les profils utilisateurs.
    Grâce à l'architecture MVVM : cette classe isole la complexité des API Firebase des couches supérieures. Grâce à Hilt, FirebaseAuth et FirestoreHelper sont injectés, rendant AuthRepositoryImpl facile à tester en remplaçant ces dépendances par des mocks.

5.  SignInUseCase.kt (Use Case d'Authentification)

    Rôle : Fait partie de la couche Domaine. Ce Use Case encapsule la logique métier \"Se connecter\". Il dépend de l'interface AuthRepository.
    Grâce à l'architecture MVVM : Il sépare la logique métier de la logique de présentation (dans le ViewModel). Un ViewModel ne devrait pas directement appeler un Repository. Le Use Case agit comme un orchestrateur d'opérations métier, et rend le ViewModel plus léger.

### Le ViewModel (Couche Présentation)

1.  AuthUiState.kt (Modèle d'État UI)

    Rôle : Un simple Data Class qui représente l'état actuel de l'interface utilisateur de l'écran de connexion. Le ViewModel mettra à jour cet état, et la Vue l'observera pour réagir en conséquence (afficher un indicateur de chargement, un message d'erreur, ou naviguer).

2.  LoginViewModel.kt (ViewModel)
    Rôle : Fait partie de la couche Présentation. Le LoginViewModel est le lien entre la LoginScreen et la logique métier d'authentification.
    Il expose un StateFlow\<AuthUiState\> (uiState) que la Vue observe pour réagir aux changements d'état (chargement, succès, erreur).
    La méthode signIn() est appelée par la Vue. Elle déclenche la logique d'authentification en appelant le SignInUseCase.
    Il traite les résultats du UseCase (Result.Success ou Result.Error) et met à jour l'état de l'UI en conséquence.
    Grâce à l'architecture MVVM : Il ne contient aucune logique d'UI Android, ce qui le rend indépendant de la plateforme et facile à tester (sans émulateur). Il gère l'état de la Vue.

### La Vue (Couche Présentation)

1.  LoginScreen.kt (Composable Jetpack Compose)

    Rôle : C'est la Vue concrète de notre architecture. Elle fait partie de la couche Présentation.
    Elle observe les StateFlow exposés par le LoginViewModel (uiState) pour se reconstruire automatiquement lorsque l'état change (ex: afficher/cacher un spinner de chargement, afficher un message d'erreur).
    Elle ne contient aucune logique métier. Lorsqu'un utilisateur clique sur \"Se connecter\", elle appelle simplement la méthode signIn du LoginViewModel, lui déléguant la responsabilité.
    hiltViewModel() est utilisé ici pour que Hilt fournisse l'instance correcte du LoginViewModel.
    collectAsStateWithLifecycle() est une fonction Jetpack Compose qui collecte le StateFlow en tenant compte du cycle de vie du Composable, évitant les fuites de mémoire.
    LaunchedEffect est utilisé pour déclencher des effets de bord (comme la navigation) en réaction aux changements d'état du ViewModel (ici, uiState.isSignInSuccess).

## Diagramme de Séquence UML : Scénario de Connexion Utilisateur

Ce diagramme visualise le flux dynamique des interactions entre les composants de l'application GuideGroup lors du processus de connexion.

![Diagramme de séquence - Connexion utilisateur](images/diagramme_sequence_scenario_connexion_latex.svg)

Explication Détaillée des Étapes du Diagramme :

Utilisateur -\> LoginScreen (Vue) : L'utilisateur interagit avec l'interface graphique en saisissant ses identifiants (email, mot de passe) et en cliquant sur le bouton de connexion.\
LoginScreen -\> LoginViewModel : La Vue, étant \"passive\", délègue l'action à son LoginViewModel en appelant la méthode signIn(email, password).\
LoginViewModel -\> LoginViewModel : Le ViewModel reçoit l'action. Sa première responsabilité est de gérer l'état de l'UI. Il met à jour un StateFlow interne (~uiState~) pour indiquer que l'opération de connexion est en cours (isLoading = true). La Vue, qui observe ce StateFlow, réagit immédiatement en affichant un indicateur de chargement et en désactivant le bouton de connexion.\
LoginViewModel -\> SignInUseCase : Le ViewModel, qui ne gère pas la logique métier complexe, appelle le SignInUseCase (un composant de la couche Domaine) pour exécuter l'opération de connexion.\
SignInUseCase -\> AuthRepository (Interface) : Le SignInUseCase appelle la méthode signIn sur l'interface AuthRepository. Il ne se soucie pas de l'implémentation concrète.\
AuthRepositoryImpl -\> FirebaseAuth : L'implémentation concrète du référentiel (AuthRepositoryImpl) utilise l'API de Firebase Authentication (FirebaseAuth) pour tenter d'authentifier l'utilisateur.\
FirebaseAuth --\> AuthRepositoryImpl : Firebase renvoie le résultat de l'authentification (succès ou échec, et si succès, l'UID de l'utilisateur).\
AuthRepositoryImpl -\> FirestoreHelper : Si l'authentification Firebase réussit, AuthRepositoryImpl utilise le FirestoreHelper pour récupérer le profil complet de l'utilisateur (User model) depuis Firebase Firestore, car FirebaseAuth ne fournit que des informations d'authentification de base.\
FirestoreHelper -\> Firebase Firestore DB : Le FirestoreHelper interagit avec la base de données Firestore pour lire le document utilisateur.\
Firebase Firestore DB --\> FirestoreHelper : La base de données renvoie les données du profil utilisateur.\
FirestoreHelper --\> AuthRepositoryImpl : Le FirestoreHelper renvoie le profil utilisateur à l'implémentation du référentiel.\
AuthRepositoryImpl --\> SignInUseCase : L'AuthRepositoryImpl encapsule le résultat (succès avec l'objet User, ou erreur) dans un Flow\<Result\<User\>\> et le renvoie au SignInUseCase.\
SignInUseCase --\> LoginViewModel : Le SignInUseCase transmet le Flow\<Result\<User\>\> au LoginViewModel.\
LoginViewModel -\> LoginViewModel (Collecte du Flow) : Le LoginViewModel collecte le Flow et réagit à son émission :
Si Result.Success : Le ViewModel met à jour son uiState pour indiquer le succès (isSignInSuccess = true).\
Si Result.Error : Le ViewModel met à jour son uiState pour inclure le message d'erreur (errorMessage = \"...\").\
LoginViewModel --\> LoginScreen : Le StateFlow uiState du ViewModel est mis à jour, ce que la Vue observe.\
LoginScreen -\> Utilisateur :
Si succès : La Vue détecte isSignInSuccess = true via son LaunchedEffect et déclenche la navigation vers l'écran principal.\
Si échec : La Vue détecte errorMessage non nul et affiche le message d'erreur à l'utilisateur.\
Ce scénario de connexion illustre parfaitement la séparation des responsabilités de MVVM et l'efficacité de l'injection de dépendances pour construire un flux de travail robuste et maintenable.\
Le Chapitre 4. a détaillé le scénario de connexion, Le Chapitre 5, expose le scénario d'envoi de message.

**←** [Chapitre 3 : Diagramme de cas d'utilisation](chapitre3.md) | [Chapitre 5 : scénario d'Envoi de messages](chapitre5.md) **→**