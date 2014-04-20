<!doctype html>
<html>
<head>
<?php include '../_head.php' ?>
<title>NodeSet - Server - Zerograph</title>
</head>
<body>

<?php include '../_header.php' ?>

<?php include '_menu.php' ?>

<main>

<h1>NodeSet</h1>
<p>The <strong>NodeSet</strong> resource represents a group of nodes that
share a common label and (optionally) property.
</p>


<h2>Methods</h2>

<h3>GET NodeSet {"label": &hellip;, "key": &hellip;, "value": &hellip;}</h3>
<p>Fetch all nodes with the specified label, property key and property value.
If <em>key</em> is null or missing, only the label will be used for matching.
</p>
<pre><code><strong>GET NodeSet {"label":"Person","key":"family_name","value":"Smith"}</strong>
body:
- !Node {"id":1,"labels":["Person"],"properties":{"given_name":"Alice","family_name":"Smith"}}
- !Node {"id":2,"labels":["Person"],"properties":{"given_name":"Bob","family_name":"Smith"}}
- !Node {"id":3,"labels":["Person"],"properties":{"given_name":"Carol","family_name":"Smith"}}
foot: {"nodes_matched":3}
</code></pre>

<h3>PATCH NodeSet {"label": &hellip;, "key": &hellip;, "value": &hellip;}</h3>
<p>Create a node with the specified label and property if none exists. Return
all nodes with these criteria.
</p>

<h3>DELETE NodeSet {"label": &hellip;, "key": &hellip;, "value": &hellip;}</h3>
<p>Delete all nodes with the specified label, property key and property value.
If <em>key</em> is null or missing, all nodes with that label will be deleted.
</p>


</main>

<?php include '_footer.php' ?>
</body>
</html>
