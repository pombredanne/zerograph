<!doctype html>
<html>
<head>
<?php include '../../_head.php' ?>
<title>Node - Python Client - Zerograph</title>
</head>
<body>

<?php include '../../_header.php' ?>

<?php include '_menu.php' ?>

<main>

<h1>Node</h1>

<p>The <strong>zerograph.Node</strong> class represents a graph node that may
or may not be <em>bound</em> to a corresponding node in a remote graph
database. Each Node instance can contain zero or more mutable labels as well as
a set of properties held as key:value pairs.
</p>


<h2>Construction</h2>

<h3>Node(*labels, **properties)</h3>
<p>The standard constructor allows one or more labels to be supplied along with
a set of named properties. For example:
</p>
<pre><code>&gt;&gt;&gt; from zerograph import Node
&gt;&gt;&gt; alice = Node("Person", name="Alice", age=33)
&gt;&gt;&gt; alice.labels
{'Person'}
&gt;&gt;&gt; alice.properties
{'age': 33, 'name': 'Alice'}
</code></pre>

<h3>Node.from_yaml(&hellip;)</h3>
<p>Node instances can be hydrated from YAML serialisations. They are denoted by
the <strong>!Node</strong> tag which should provide label and property details
within an associated mapping:
<pre><code>&gt;&gt;&gt; yaml.load('!Node {"labels":["Person"],"properties":{"name":"Alice"}}')
(:Person {"name":"Alice"})
</code></pre>


<h2>Labels &amp; Properties</h2>
<p>A set of label strings may be attached to a Node. As a set, these are
inherently unordered and duplicates are not permitted.
</p>

<h3>labels</h3>

<h3>properties</h3>


<h2>Binding &amp; Synchronisation</h2>

<p>Nodes can exist solely within a client application or may alternatively be
bound to a remote node within a Neo4j graph database. The binding is realised
by storing two values within the node that provide a link to the remote entity:
a Graph object and a unique ID.
</p>
<p>Updates to either entity within a bound pair are <em>not</em> automatically
synchronised to the other entity. The <strong>pull</strong> and
<strong>push</strong> methods are instead provided for this purpose.
</p>

<h3>bind(graph, id)</h3>
<p>Bind the local entity to a remote entity identified by the specified
<em>graph</em> and <em>id</em>.
</p>

<h3>bound</h3>

<h3>bound_graph</h3>

<h3>bound_id</h3>

<h3>exists</h3>

<h3>pull()</h3>

<h3>push()</h3>

<h3>unbind()</h3>


<h2>Representation</h2>

<h3>to_cypher()</h3>

<h3>to_geoff()</h3>

<h3>to_yaml()</h3>


<h2>Operations</h2>

<h3>Equality</h3>
<p>Nodes are considered equal if they have an identical set of labels and
identical properties. Binding makes no difference to equality.
</p>


</main>

<?php include '_footer.php' ?>
</body>
</html>
