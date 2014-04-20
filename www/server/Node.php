<!doctype html>
<html>
<head>
<?php include '../_head.php' ?>
<title>Node - Server - Zerograph</title>
</head>
<body>

<?php include '../_header.php' ?>

<?php include '_menu.php' ?>

<main>

<h1>Node</h1>
<p>The <strong>Node</strong> resource represents an individual Neo4j graph
database node identified by internal node ID.
</p>


<h2>Methods</h2>

<h3>GET Node {"id": &hellip;}</h3>
<p>Fetch the node identified by <em>id</em>.
</p>
<pre><code><strong>GET Node {"id":1}</strong>
body: !Node {"id":1,"labels":["Person"],"properties":{"name":"Alice"}}
</code></pre>

<h3>SET Node {"id": &hellip;, "labels": &hellip;, "properties": &hellip;}</h3>
<p>Replace the labels and properties on the node identified by <em>id</em>
and return the updated node.
</p>

<h3>PATCH Node {"id": &hellip;, "labels": &hellip;, "properties": &hellip;}</h3>
<p>Supplement the labels and properties on the node identified by
<em>id</em> and return the updated node.
</p>

<h3>CREATE Node {"labels": &hellip;, "properties": &hellip;}</h3>
<p>Create and return a new node with the labels and properties specified.
</p>

<h3>DELETE Node {"id": &hellip;}</h3>
<p>Delete the node identified by <em>id</em>.
</p>


</main>

<?php include '_footer.php' ?>
</body>
</html>
