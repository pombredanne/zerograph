<!doctype html>
<html>
<head>
<?php include '../_head.php' ?>
<title>Rel - Server - Zerograph</title>
</head>
<body>

<?php include '../_header.php' ?>

<?php include '_menu.php' ?>

<main>

<h1>Rel</h1>
<p>The <strong>Rel</strong> resource represents an individual Neo4j graph
database relationship identified by internal relationship ID.
</p>


<h2>Methods</h2>

<h3>GET Rel {"id": &hellip;}</h3>
<p>Fetch the path segment containing the relationship identified by
<em>id</em>.
</p>
<pre><code><strong>GET Rel {"id":123}</strong>
body: !Path [!Node {"id":1,"labels":[],"properties":{}},!Rel {"id":123,"type":"KNOWS","properties":{}},!Node {"id":2,"properties":{}}]
</code></pre>

<h3>SET Rel {"id": &hellip;, "properties": &hellip;}</h3>
<p>Replace the properties on the relationship identified by <em>id</em>
and return the path segment containing the updated relationship.
</p>

<h3>PATCH Rel {"id": &hellip;, "properties": &hellip;}</h3>
<p>Supplement the properties on the relationship identified by <em>id</em>
and return the path segment containing the updated relationship.
</p>

<h3>CREATE Rel {"start": &hellip;, "end": &hellip;, "type": &hellip;, "properties": &hellip;}</h3>
<p>Create a new relationship with the type and properties specified and return
the surrounding path segment.
</p>

<h3>DELETE Rel {"id": &hellip;}</h3>
<p>Delete the relationship identified by <em>id</em>.
</p>

</main>

<?php include '_footer.php' ?>
</body>
</html>
