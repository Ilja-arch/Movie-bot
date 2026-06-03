# Movie Recommendation Telegram Bot

##  About the project

This is a Telegram bot built with **Java and Spring Boot** that helps users find movie recommendations and popular films.
Users can interact with the bot, receive movie suggestions, and save movies they like for later.

This project was built as a personal portfolio project to practice backend development.

---

##  Features

* Movie recommendations via Telegram bot
* User interaction handling
* Saving user preferences
* Integration with an external movie API
* Persistent data storage (database)
* REST API backend (Spring Boot)

---

## Technologies used

* Java 17+
* Spring Boot
* Spring Data JPA
* SQLite
* Lombok
* Mockito (testing)
* Telegram Bot API
* Maven
* Git

---

##  How to run the project

1. Clone the repository:

```bash
git clone https://github.com/Ilja-arch/Movie-bot
```

2. Open the project in IntelliJ IDEA

3. Configure the database in `application.properties`

4. Run the application:

```bash
mvn spring-boot:run
```

---

##  How it works

* The user sends a message to the bot in Telegram
* The bot processes the request using a Spring Boot backend
* The backend fetches movie data from an external API
* The bot returns recommendations to the user
* The user can save and delete movies from their personal list

---

## Author

Ilja Sokolnikovs
