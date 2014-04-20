<!doctype html>
<html>
<head>
<?php include '../_head.php' ?>
<title>RelSet - Server - Zerograph</title>
</head>
<body>

<?php include '../_header.php' ?>

<?php include '_menu.php' ?>

<main>

<h1>RelSet</h1>
<p>The <strong>RelSet</strong> resource represents a group of relationships that
share common end points and/or type.


<h2>Methods</h2>

<h3>GET RelSet {"start": &hellip;, "end": &hellip;, "type": &hellip;}</h3>
<p>Fetch all path segments that contain relationships with the specified end 
points and/or type. All criteria can be null or missing but at least one end
point must be provided.
</p>

<h3>PATCH RelSet {"start": &hellip;, "end": &hellip;, "type": &hellip;}</h3>
<p>Ensure at least one relationship exists with the specified end points and
type and return all matching path segments. All criteria must be provided.
</p>

<h3>DELETE RelSet {"start": &hellip;, "end": &hellip;, "type": &hellip;}</h3>
<p>Delete all path segments that contain relationships with the specified end 
points and/or type. All criteria can be null or missing but at least one end
point must be provided.
</p>


</main>

<?php include '_footer.php' ?>
</body>
</html>
