```mermaid
classDiagram
    class Card {
        <<abstract>>
        +int id
        +play()*
    }

    class AnimalCard {
        +createNewAnimal() Animal
    }

    class TraitCard {
        +createNewTrait() Trait
    }

    class Animal {
        +int id
        +int size

        +int foodEaten
        +bool isAlive
        +listTraits <List>
        +addTrait(Trait)
        +feed(int)
        +needFood() int
        +die()
    }

    class Trait {
        <<abstract>>
        +int id
        +TraitType traitType
        +onAttack(Animal predator, Animal victim) Boolean
        +onFeed(Animal animal, Game game)
    }

    class FatTrait {
        +int fatReserve
    }

    class PredatorTrait {
    }

    class AquaticTrait {
    }

    enum TraitType {
        PREDATOR
        AQUATIC
        FAT
        BURROWING
        POISONOUS
    }

    class Player {
        +String name
        +int playerId
        +int score
        +<List> listOfAnimals
        +<List> hand
        +addCard(Card)
        +playCard(Card, Animal?) Boolean
    }

    enum Phase {
        DEVELOPMENT
        CLIMATE
        FEEDING
        EXTINCTION
    }

    class Game {
        +int gameId
        +<List> players
        +int foodPool
        +List deck
        +List discardCard
        +Phase currentPhase
        +int currentPlayerId
        +List moves
        +startGame()
        +nextPhase()
        +developmentPhase()
        +climatePhase()
        +feedingPhase()
        +diePhase()
        +calculateFinalScore()
        +getWinner() Player
        +canPlayCard(Player, Card, Animal?) Boolean
        +recordMove(Move)
        +isValidMove(Move) Boolean
    }

    class Move {
        +int moveId
        +int gameId
        +int playerId
        +ActionType actionType
        +int cardId
        +int? targetAnimalId
        +int? foodAmount
    }

    enum ActionType {
        PLAY_ANIMAL_CARD
        PLAY_TRAIT_CARD
        FEED_HERBIVORE
        ATTACK_PREDATOR
        PASS
    }

    class Statistics {
        +computeWinRate(int playerId) double
        +computeAverageScore(int playerId) double
        +getLeaderboard() List~Player~
    }



    Card <|-- AnimalCard
    Card <|-- TraitCard
    AnimalCard ..> Animal : creates
    TraitCard ..> Trait : creates
    Trait <|-- FatTrait
    Trait <|-- PredatorTrait
    Trait <|-- AquaticTrait
    Trait --> TraitType
    Animal --> Trait : contains
    Player --> Animal : owns
    Game --> Player : has
    Game --> Move : stores
    Statistics ..> Game : works on