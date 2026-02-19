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
    $res = mysqli_query($connection,"select * from Team where team_id = 'Phantoms'");
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

$app->post('/login', function(Request $request, Response $response, array $args)
    use ($connection) {
        //$params = (array)$request->getParsedBody();

        //Try instead of previous line if doesn't work
        $params = json_decode(file_get_contents("php://input"), true);
        

        $email = $params['email'];
        $password = $params['password'];
        

        $res = mysqli_query($connection, "SELECT * FROM Account WHERE email = '$email' AND password = '$password'");
        $row = mysqli_fetch_assoc($res);
        
        if($row){
            $is_admin = $row['is_admin'];
            $response->getBody()->write(json_encode(["success" => true, "email" => $email, "password" => $password, "is_admin" => $is_admin]));
        } else{
            $response->getBody()->write(json_encode(["success" => false, "email" => $email, "password" => $password, "is_admin" => $is_admin]));
        }
        
        return $response->withHeader('Content-Type', 'application/json');
        

    });


$app->post('/signup', function(Request $request, Response $response, array $args)
    use ($connection) {

    $params = json_decode(file_get_contents("php://input"), true);

    $email = $params["email"] ?? '';
    $password = $params["password"] ?? '';

    $res = mysqli_query(
        $connection,
        "INSERT INTO Account (email, password) VALUES ('$email', '$password')"
    );

    if ($res) {
        $payload = json_encode(["success" => true]);
    } else {
        $payload = json_encode(["success" => false]);
    }

    $response->getBody()->write($payload);

    return $response->withHeader('Content-Type', 'application/json');
});



$app->run();