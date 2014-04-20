<!doctype html>
<html>
<head>
<?php include '../_head.php' ?>
<title>Graph - Server - Zerograph</title>
</head>
<body>

<?php include '../_header.php' ?>

<?php include '_menu.php' ?>

<main>

<h1>Graph</h1>
<p>The <strong>Graph</strong> resource represents a Neo4j graph database
exposed as a Zerograph service.
</p>

<h2>Methods</h2>

<h3>GET Graph {"host": &hellip;, "port": &hellip;}</h3>
<p>Fetch a representation of the specified graph database service, if such a
service exists.
</p>

<h3>PATCH Graph {"host": &hellip;, "port": &hellip;}</h3>
<p>Fetch a representation of the specified graph database service, creating a
new graph if none exists.
</p>

<h3>DELETE Graph {"host": &hellip;, "port": &hellip;}</h3>
<p>Drop the graph database instance bound to the port specified.
</p>

</main>

<?php include '_footer.php' ?>
</body>
</html>
