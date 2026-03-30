<?php
use Psr\Http\Message\ResponseInterface as Response;
use Psr\Http\Message\ServerRequestInterface as Request;
use Slim\Factory\AppFactory;



require __DIR__ . '/../vendor/autoload.php';

$app = AppFactory::create();
$app->addBodyParsingMiddleware();

error_reporting(E_ALL);
ini_set('display_errors', 1);

$connection = mysqli_connect("mysql.jwuclasses.com","elamothe","adsjks934232_3","elamothe") or die("Unable to connect to database");


//$routeCollector = $app->getRouteCollector();
//$routeCollector->setDefaultInvocationStrategy(new RequestResponseArgs());

//Route that returns all the teams
$app->get('/teams', function (Request $request, Response $response, array $args) 
    use ($connection) {
    $res = mysqli_query($connection,"select * from Team");
    $teams=[];  //Array to store the teams
    //Get each team and put into an array of key value pairs
    while($row = mysqli_fetch_assoc($res)){
        $teams[] = $row;
    }

    $payload = json_encode($teams); //Turn array into JSON for transferable data

    $response->getBody()->write($payload);  
    return $response;   //Return JSON team data in $response
});

//Route that returns all the teams with players
$app->get('/players', function (Request $request, Response $response, array $args) 
    use ($connection) {
        $res = mysqli_query($connection, "select * from Player WHERE team_id = 'Outlaws'");
        $players=[];

        while($row = mysqli_fetch_assoc($res)){
            $players[] = $row;
        }

        $payload = json_encode($players);

        $response->getBody()->write($payload);
        return $response;
    });

$app->post('/login', function(Request $request, Response $response)
    use ($connection) {

    $params = (array)$request->getParsedBody();

    $email = $params['email'] ?? '';
    $password = $params['password'] ?? '';

    $res = mysqli_query(
        $connection,
        "SELECT * FROM Account WHERE email = '$email' AND password = '$password'"
    );

    $row = mysqli_fetch_assoc($res);

    if ($row) {
        $payload = json_encode([
            "success" => true,
            "email" => $email,
            "is_admin" => (int)$row['is_admin']
        ]);
    } else {
        $payload = json_encode([
            "success" => false,
            "email" => $email,
            "is_admin" => 0
        ]);
    }

    $response->getBody()->write($payload);

    return $response->withHeader('Content-Type', 'application/json');
});

$app->post('/signup', function(Request $request, Response $response, array $args)
    use ($connection) {

    $params = (array)$request->getParsedBody();
    $email = $params["email"] ?? '';
    $password = $params["password"] ?? '';

    $res = mysqli_query($connection, "INSERT INTO Account (email, password) VALUES ('$email', '$password')"
    );

    if ($res) {
        $payload = json_encode(["success" => true]);
    } else {
        $payload = json_encode(["success" => false]);
    }

    $response->getBody()->write($payload);

    return $response->withHeader('Content-Type', 'application/json');
});


$app->post('/createAdmin', function(Request $request, Response $response, array $args)
    use ($connection) {

    $params = (array)$request->getParsedBody();
    $email = $params["email"] ?? '';

    $res = mysqli_query(
        $connection,
        "UPDATE Account SET is_admin = 1 WHERE email = '$email'"
    );

    if ($res) {
        $payload = json_encode(["success" => true]);
    } else {
        $payload = json_encode(["success" => false]);
    }

    $response->getBody()->write($payload);

    return $response->withHeader('Content-Type', 'application/json');
});


$app->post('/createGame', function(Request $request, Response $response, array $args) 
use ($connection) {

    // Get POST parameters safely
    $params = (array)$request->getParsedBody();
    $home_team_id = isset($params["home_team_id"]) ? $params["home_team_id"] : '';
    $away_team_id = isset($params["away_team_id"]) ? $params["away_team_id"] : '';

    // Current timestamp
    $date = date("Y-m-d H:i:s");

    // Make sure IDs are numbers to avoid SQL injection
    $home_team_id = mysqli_real_escape_string($connection, $home_team_id);
    $away_team_id = mysqli_real_escape_string($connection, $away_team_id);

    // Run the INSERT query
    $sql = "INSERT INTO Game (home_team_id, away_team_id, start_time) 
            VALUES ('$home_team_id', '$away_team_id', '$date')";

    $res = mysqli_query($connection, $sql);

    // Prepare JSON payload
    if ($res) {
        // Get the last inserted ID (game_id)
        $game_id = mysqli_insert_id($connection);

        $payload = [
            "success" => true,
            "game_id" => (int)$game_id
        ];
    } else {
        // Include error for debugging if needed
        $payload = [
            "success" => false,
            "error" => mysqli_error($connection)
        ];
    }

    // Clear any previous output to prevent <br> or warnings
    if (ob_get_length()) {
        ob_clean();
    }

    // Return JSON
    $response->getBody()->write(json_encode($payload));
    return $response->withHeader('Content-Type', 'application/json');
});

$app->get('/getPlayers/{team_id}', function(Request $request, Response $response, array $args)
use($connection) {
    $team_id = $args['team_id'];

    // Escape input to prevent SQL injection
    $team_id = mysqli_real_escape_string($connection, $team_id);

    $res = mysqli_query($connection,
        "SELECT player_id, team_id, first_name, last_name FROM Player WHERE team_id = '$team_id'"
    );

    $players = [];
    while ($row = mysqli_fetch_assoc($res)) {
        $players[] = [
            "player_id" => (int)$row['player_id'],
            "team_id" => $row['team_id'],
            "first_name" => $row['first_name'],
            "last_name" => $row['last_name']
        ];
    }

    $payload = json_encode([
        "success" => true,
        "players" => $players
    ]);

    $response->getBody()->write($payload);
    return $response->withHeader('Content-Type', 'application/json');
});

$app->post('/insertPlay', function(Request $request, Response $response, array $args)
use ($connection) {

    $params = (array)$request->getParsedBody();

    // Required fields
    $game_id = $params["game_id"] ?? 0;
    $inning = $params["inning"] ?? 1;
    $half = $params["half"] ?? "TOP";

    $home_score = $params["home_score"] ?? 0;
    $away_score = $params["away_score"] ?? 0;

    $pitcher_id = $params["pitcher_id"] ?? 0;
    $batter_id = $params["batter_id"] ?? 0;

    // Nullable runners
    $first_base_runner_id = isset($params["first_base_runner_id"]) ? $params["first_base_runner_id"] : "NULL";
    $second_base_runner_id = isset($params["second_base_runner_id"]) ? $params["second_base_runner_id"] : "NULL";
    $third_base_runner_id = isset($params["third_base_runner_id"]) ? $params["third_base_runner_id"] : "NULL";

    $balls = $params["balls"] ?? 0;
    $strikes = $params["strikes"] ?? 0;
    $outs = $params["outs"] ?? 0;

    // Results
    $single_hit = $params["single_hit"] ?? 0;
    $double_hit = $params["double_hit"] ?? 0;
    $triple_hit = $params["triple_hit"] ?? 0;
    $homerun = $params["homerun"] ?? 0;
    $strikeout = $params["strikeout"] ?? 0;
    $walk = $params["walk"] ?? 0;
    $fielders_choice = $params["fielders_choice"] ?? 0;
    $batted_out = $params["batted_out"] ?? 0;
    $double_play = $params["double_play"] ?? 0;
    $triple_play = $params["triple_play"] ?? 0;

    // Build query
    $sql = "
        INSERT INTO Play (
            game_id, inning, half,
            home_score, away_score,
            pitcher_id, batter_id,
            first_base_runner_id, second_base_runner_id, third_base_runner_id,
            balls, strikes, outs,
            single_hit, double_hit, triple_hit, homerun,
            strikeout, walk, fielders_choice, batted_out,
            double_play, triple_play
        ) VALUES (
            '$game_id', '$inning', '$half',
            '$home_score', '$away_score',
            '$pitcher_id', '$batter_id',
            $first_base_runner_id, $second_base_runner_id, $third_base_runner_id,
            '$balls', '$strikes', '$outs',
            '$single_hit', '$double_hit', '$triple_hit', '$homerun',
            '$strikeout', '$walk', '$fielders_choice', '$batted_out',
            '$double_play', '$triple_play'
        )
    ";

    $res = mysqli_query($connection, $sql);

    if ($res) {
        $payload = json_encode([
            "success" => true
        ]);
    } else {
        $payload = json_encode([
            "success" => false,
            "error" => mysqli_error($connection)
        ]);
    }

    $response->getBody()->write($payload);
    return $response->withHeader('Content-Type', 'application/json');
});

$app->run();