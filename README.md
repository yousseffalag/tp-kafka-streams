# 🚀 TP Kafka Streams — Analyse de Données Texte

> Traitement en temps réel de messages texte avec **Kafka Streams** : nettoyage, filtrage et routage vers des topics Kafka distincts.

---

## 📋 Description

Ce projet implémente un pipeline de streaming avec l'API **Kafka Streams DSL** en Java. Il lit des messages bruts depuis un topic Kafka, les nettoie, les valide selon plusieurs règles, puis les route automatiquement vers le bon topic de sortie.

| Topic | Rôle |
|---|---|
| `text-input` | Source — messages bruts entrants |
| `text-clean` | Destination — messages valides et nettoyés |
| `text-dead-letter` | Destination — messages invalides rejetés |

---

## ⚙️ Pipeline de traitement

```
text-input
    │
    ▼
[Nettoyage]
  • trim()
  • replaceAll("\\s+", " ")
  • toUpperCase()
    │
    ▼
[Filtrage]
  • Non vide
  • Longueur ≤ 100 caractères
  • Aucun mot interdit (HACK, SPAM, XXX)
    │
    ├── valide ──► text-clean
    └── invalide ► text-dead-letter
```

---

## 🛠️ Stack technique

- **Langage** : Java (Maven)
- **API** : Kafka Streams DSL (`mapValues`, `filter`, `to`)
- **Broker** : Apache Kafka (Docker)
- **Bootstrap server** : `localhost:9092`

---

## 🚀 Démarrage rapide

### 1. Prérequis

- Docker & Docker Compose
- Java 11+
- Maven

### 2. Démarrer le broker Kafka

```bash
docker compose up -d
```

### 3. Créer les topics

```bash
docker exec --workdir /opt/kafka/bin -it broker sh

./kafka-topics.sh --create --topic text-input \
    --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1

./kafka-topics.sh --create --topic text-clean \
    --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1

./kafka-topics.sh --create --topic text-dead-letter \
    --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1
```

### 4. Compiler et lancer l'application

```bash
mvn clean package
java -jar target/tp-kafka-streams-*.jar
```

---

## 🧪 Tests

### Envoyer des messages

```bash
./kafka-console-producer.sh --topic text-input --bootstrap-server localhost:9092
>Bonjour tout le monde
>Lorem ipsum dolor sit amet...
>Bonjour, ceci est un spam
```

### Lire les messages valides

```bash
./kafka-console-consumer.sh --topic text-clean \
    --bootstrap-server localhost:9092 --from-beginning
```

### Lire les messages rejetés

```bash
./kafka-console-consumer.sh --topic text-dead-letter \
    --bootstrap-server localhost:9092 --from-beginning
```

---

## 📊 Résultats attendus

| Message envoyé | Raison de rejet | Topic de sortie |
|---|---|---|
| `Bonjour tout le monde` | — | `text-clean` |
| `Lorem ipsum dolor sit amet...` | — | `text-clean` |
| `Lorem ipsum dolor sit amet... hhajdkk akkdlls ...` (> 100 chars) | Trop long | `text-dead-letter` |
| `Bonjour, ceci est un spam` | Mot interdit | `text-dead-letter` |

---

## 👤 Auteur

**FALAG Youssef** — Filière BDCC, Université Hassan II Casablanca  
Encadré par **Pr. Abdelmajid BOUSSELHAM** — Année universitaire 2025–2026
