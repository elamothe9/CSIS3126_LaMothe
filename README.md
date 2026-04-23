# ⚾ Wiffle Ball Stat Tracker

A full-stack mobile and backend application for tracking wiffle ball games, including play-by-play logging, player stats, and game management.

---

## 📦 Tech Stack

* **Frontend:** Android (Kotlin, Volley)
* **Backend:** PHP (Slim Framework)
* **Database:** MySQL

---

## 🔗 Repository

GitHub: https://github.com/elamothe9/CSIS3126_LaMothe.git

---

## 🛠️ Setup Instructions

### 1. Clone the Repository

```bash
git clone https://github.com/elamothe9/CSIS3126_LaMothe.git
cd CSIS3126_LaMothe
```

---

## 🗄️ 2. Database Setup (MySQL)

### Create Database

```sql
CREATE DATABASE DesignProject;
USE DesignProject;
```

---

### Create Tables

```sql
CREATE TABLE Team (
    team_id VARCHAR(25) PRIMARY KEY
);

CREATE TABLE Account (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    is_admin TINYINT(1) NOT NULL DEFAULT 0
);

CREATE TABLE Player (
    player_id INT AUTO_INCREMENT PRIMARY KEY,
    team_id VARCHAR(50) NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    FOREIGN KEY (team_id) REFERENCES Team(team_id)
);

CREATE TABLE Game (
    game_id INT AUTO_INCREMENT PRIMARY KEY,
    home_team_id VARCHAR(25) NOT NULL,
    away_team_id VARCHAR(25) NOT NULL,
    final_home_score INT DEFAULT 0,
    final_away_score INT DEFAULT 0,
    start_time DATETIME DEFAULT NULL,
    end_time DATETIME DEFAULT NULL,
    FOREIGN KEY (home_team_id) REFERENCES Team(team_id),
    FOREIGN KEY (away_team_id) REFERENCES Team(team_id)
);

CREATE TABLE Play (
    play_id INT AUTO_INCREMENT PRIMARY KEY,
    game_id INT NOT NULL,
    inning INT NOT NULL,
    half ENUM('TOP','BOTTOM') NOT NULL,
    home_score INT NOT NULL DEFAULT 0,
    away_score INT NOT NULL DEFAULT 0,
    pitcher_id INT NOT NULL,
    batter_id INT NOT NULL,
    first_base_runner_id INT DEFAULT NULL,
    second_base_runner_id INT DEFAULT NULL,
    third_base_runner_id INT DEFAULT NULL,
    balls INT DEFAULT 0,
    strikes INT DEFAULT 0,
    outs INT DEFAULT 0,
    single_hit TINYINT(1) DEFAULT 0,
    double_hit TINYINT(1) DEFAULT 0,
    triple_hit TINYINT(1) DEFAULT 0,
    homerun TINYINT(1) DEFAULT 0,
    strikeout TINYINT(1) DEFAULT 0,
    walk TINYINT(1) DEFAULT 0,
    fielders_choice TINYINT(1) DEFAULT 0,
    batted_out TINYINT(1) DEFAULT 0,
    double_play TINYINT(1) DEFAULT 0,
    triple_play TINYINT(1) DEFAULT 0,
    FOREIGN KEY (game_id) REFERENCES Game(game_id),
    FOREIGN KEY (pitcher_id) REFERENCES Player(player_id),
    FOREIGN KEY (batter_id) REFERENCES Player(player_id),
    FOREIGN KEY (first_base_runner_id) REFERENCES Player(player_id),
    FOREIGN KEY (second_base_runner_id) REFERENCES Player(player_id),
    FOREIGN KEY (third_base_runner_id) REFERENCES Player(player_id)
);

CREATE TABLE Play_Run (
    play_id INT NOT NULL,
    runner_id INT NOT NULL,
    PRIMARY KEY (play_id, runner_id),
    FOREIGN KEY (play_id) REFERENCES Play(play_id),
    FOREIGN KEY (runner_id) REFERENCES Player(player_id)
);
```

---

### Insert Initial Data

```sql
INSERT INTO Team (team_id)
VALUES ('Phantoms'), ('Crimson Devils'), ('Outlaws'), ('Venom');

INSERT INTO Player (team_id, first_name, last_name) VALUES
('Phantoms', 'Andrew', 'Chandler'),
('Phantoms', 'Hector', 'Campbell'),
('Phantoms', 'Anthony', 'Chandler'),
('Phantoms', 'Jamie', 'Mills'),
('Phantoms', 'Vaughn', 'Larkin'),

('Crimson Devils', 'Tucker', 'Hall'),
('Crimson Devils', 'Baxter', 'Lowell'),
('Crimson Devils', 'Max', 'Fontana'),
('Crimson Devils', 'Jacob', 'Kunin'),

('Outlaws', 'Casey', 'Hockenbury'),
('Outlaws', 'Tristan', 'Boutin'),
('Outlaws', 'River', 'Koval'),
('Outlaws', 'Eddie', 'Hockenbury'),

('Venom', 'Robbie', 'Fragola'),
('Venom', 'Graeham', 'Spitellie'),
('Venom', 'Owen', 'Seremeth'),
('Venom', 'Storm', 'Dusablon');
```

---

## 📊 3. Create Views

### Hitting Stats

```sql
CREATE VIEW hitting_stats AS
SELECT
    pl.batter_id,
    COUNT(pl.play_id) AS PA,
    SUM(CASE WHEN COALESCE(pl.walk,0)=0 THEN 1 ELSE 0 END) AS AB,
    SUM(pl.single_hit + pl.double_hit + pl.triple_hit + pl.homerun) AS Hits,
    SUM(pl.single_hit) AS Singles,
    SUM(pl.double_hit) AS Doubles,
    SUM(pl.triple_hit) AS Triples,
    SUM(pl.homerun) AS Homeruns,
    SUM(pl.strikeout) AS SO,
    SUM(pl.walk) AS BB,
    COUNT(pr.runner_id) AS Runs,
    CASE
        WHEN SUM(CASE WHEN pl.walk=0 THEN 1 ELSE 0 END)=0 THEN 0
        ELSE SUM(pl.single_hit + pl.double_hit + pl.triple_hit + pl.homerun) /
             SUM(CASE WHEN pl.walk=0 THEN 1 ELSE 0 END)
    END AS AVG
FROM Play pl
LEFT JOIN Play_Run pr
    ON pl.play_id = pr.play_id
    AND pr.runner_id = pl.batter_id
GROUP BY pl.batter_id;
```

---

### Pitching Stats

```sql
CREATE VIEW pitching_stats AS
SELECT
    pl.pitcher_id,
    SUM(pl.outs) AS total_outs,
    SUM(pl.outs)/3.0 AS IP,
    SUM(pl.single_hit + pl.double_hit + pl.triple_hit + pl.homerun) AS Hits,
    COUNT(pr.runner_id) AS Runs,
    SUM(pl.strikeout) AS K,
    SUM(pl.walk) AS BB,
    CASE
        WHEN SUM(pl.outs)=0 THEN 0
        ELSE (COUNT(pr.runner_id)*3.0)/SUM(pl.outs)
    END AS RA,
    CASE
        WHEN SUM(pl.outs)=0 THEN 0
        ELSE (SUM(pl.walk) + SUM(pl.single_hit + pl.double_hit + pl.triple_hit + pl.homerun))
             / (SUM(pl.outs)/3.0)
    END AS WHIP
FROM Play pl
LEFT JOIN Play_Run pr
    ON pl.play_id = pr.play_id
GROUP BY pl.pitcher_id;
```

---

## 🌐 4. Backend Setup (PHP + Slim)

### Requirements

* PHP 8+
* Composer
* MySQL

---

### Install Dependencies

```bash
composer install
```

---

### Configure Database Connection

Update your PHP connection:

```php
$connection = mysqli_connect(
    "your-host",
    "your-username",
    "your-password",
    "DesignProject"
);
```

---

### Run the API

```bash
php -S localhost:8080
```

Or deploy using Apache/Nginx.

---

## 📱 5. Android Setup

1. Open the project in **Android Studio**
2. Locate the API base URL in `ApiService`
3. Update it to your server:

```kotlin
const val BASE_URL = "http://YOUR_SERVER_URL/"
```

---

## 🔐 Authentication

* Passwords are stored securely using hashing:

```php
password_hash($password, PASSWORD_BCRYPT);
```

* Login verification uses:

```php
password_verify($inputPassword, $storedHash);
```

---

## ▶️ Running the App

1. Start your MySQL server and ensure the database is created
2. Start the PHP backend
3. Run the Android app in an emulator or device
4. Create an account and begin tracking games

---
