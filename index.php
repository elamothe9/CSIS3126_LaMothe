<?php
use Psr\Http\Message\ResponseInterface as Response;
use Psr\Http\Message\ServerRequestInterface as Request;
use Slim\Factory\AppFactory;



require __DIR__ . '/../vendor/autoload.php';

$app = AppFactory::create();

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
        $res = mysqli_query($connection, "select * from Player");
        $players=[];

        while($row = mysqli_fetch_assoc($res)){
            $players[] = $row;
        }

        $payload = json_encode($players);

        $response->getBody()->write($payload);
        return $response;
    });



$app->run();